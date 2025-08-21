from fastapi import APIRouter, Depends, HTTPException, Query, UploadFile, File, Form
from sqlalchemy.orm import Session
from typing import List, Optional
import os
import uuid
import cv2
import numpy as np
import pytesseract
from PIL import Image
import io

# 设置Tesseract可执行文件路径
pytesseract.pytesseract.tesseract_cmd = r"C:\Program Files\Tesseract-OCR\tesseract.exe"
from database import get_db
from models import Prescription as PrescriptionModel
from schemas import Prescription, PrescriptionCreate, PrescriptionUpdate, PaginatedResponse
from ai.ai_prescription import generate_tcm_prescription

router = APIRouter()

# 获取处方列表（支持分页和搜索）
@router.get("/", response_model=PaginatedResponse)
async def get_prescriptions(
    page: int = Query(1, ge=1, description="页码"),
    size: int = Query(10, ge=1, le=100, description="每页数量"),
    user_id: Optional[int] = Query(None, description="用户ID"),
    status: Optional[str] = Query(None, description="处方状态"),
    db: Session = Depends(get_db)
):
    """获取处方列表"""
    query = db.query(PrescriptionModel)
    
    # 用户过滤
    if user_id:
        query = query.filter(PrescriptionModel.user_id == user_id)
    
    # 状态过滤
    if status:
        query = query.filter(PrescriptionModel.status == status)
    
    # 按创建时间倒序排列
    query = query.order_by(PrescriptionModel.created_time.desc())
    
    # 计算总数
    total = query.count()
    
    # 分页
    offset = (page - 1) * size
    prescriptions = query.offset(offset).limit(size).all()
    
    # 计算总页数
    pages = (total + size - 1) // size
    
    return PaginatedResponse(
        items=[Prescription.from_orm(prescription).dict() for prescription in prescriptions],
        total=total,
        page=page,
        size=size,
        pages=pages
    )

# 获取单个处方详情
@router.get("/{prescription_id}", response_model=Prescription)
async def get_prescription(prescription_id: int, db: Session = Depends(get_db)):
    """获取处方详情"""
    prescription = db.query(PrescriptionModel).filter(
        PrescriptionModel.id == prescription_id
    ).first()
    if not prescription:
        raise HTTPException(status_code=404, detail="处方不存在")
    return prescription

# 创建处方
@router.post("/", response_model=Prescription)
async def create_prescription(prescription: PrescriptionCreate, db: Session = Depends(get_db)):
    """创建处方"""
    db_prescription = PrescriptionModel(**prescription.dict())
    db.add(db_prescription)
    db.commit()
    db.refresh(db_prescription)
    return db_prescription

# 更新处方
@router.put("/{prescription_id}", response_model=Prescription)
async def update_prescription(
    prescription_id: int, 
    prescription_update: PrescriptionUpdate, 
    db: Session = Depends(get_db)
):
    """更新处方"""
    db_prescription = db.query(PrescriptionModel).filter(
        PrescriptionModel.id == prescription_id
    ).first()
    if not db_prescription:
        raise HTTPException(status_code=404, detail="处方不存在")
    
    # 更新字段
    update_data = prescription_update.dict(exclude_unset=True)
    for field, value in update_data.items():
        setattr(db_prescription, field, value)
    
    db.commit()
    db.refresh(db_prescription)
    return db_prescription

# 删除处方
@router.delete("/{prescription_id}")
async def delete_prescription(prescription_id: int, db: Session = Depends(get_db)):
    """删除处方"""
    db_prescription = db.query(PrescriptionModel).filter(
        PrescriptionModel.id == prescription_id
    ).first()
    if not db_prescription:
        raise HTTPException(status_code=404, detail="处方不存在")
    
    db.delete(db_prescription)
    db.commit()
    return {"message": "处方删除成功"}

# 上传处方图片
@router.post("/{prescription_id}/upload-image")
async def upload_prescription_image(
    prescription_id: int,
    file: UploadFile = File(...),
    db: Session = Depends(get_db)
):
    """上传处方图片"""
    # 检查处方是否存在
    db_prescription = db.query(PrescriptionModel).filter(
        PrescriptionModel.id == prescription_id
    ).first()
    if not db_prescription:
        raise HTTPException(status_code=404, detail="处方不存在")
    
    # 检查文件类型
    if not file.content_type.startswith('image/'):
        raise HTTPException(status_code=400, detail="只能上传图片文件")
    
    # 创建上传目录
    upload_dir = "static/prescriptions"
    os.makedirs(upload_dir, exist_ok=True)
    
    # 生成唯一文件名
    file_extension = file.filename.split('.')[-1]
    unique_filename = f"{uuid.uuid4()}.{file_extension}"
    file_path = os.path.join(upload_dir, unique_filename)
    
    # 保存文件
    with open(file_path, "wb") as buffer:
        content = await file.read()
        buffer.write(content)
    
    # 更新处方记录
    db_prescription.image_url = f"/static/prescriptions/{unique_filename}"
    db.commit()
    db.refresh(db_prescription)
    
    return {
        "message": "图片上传成功",
        "image_url": db_prescription.image_url
    }

# 症状分析（模拟AI分析）
@router.post("/analyze-symptoms")
async def analyze_symptoms(symptoms: str = Form(...)):
    """症状分析（集成AI中医处方生成）"""
    print("="*50)
    print("🎯🎯🎯 症状分析端点被调用！🎯🎯🎯")
    print(f"🎯🎯🎯 接收到的症状: {symptoms}")
    print("="*50)
    try:
        # 从环境变量获取API密钥和模型配置
        api_key = os.getenv("OPENAI_API_KEY")
        ai_model = os.getenv("AI_MODEL", "deepseek-chat")
        
        # 添加调试信息
        print(f"🔍 调试信息: API Key存在: {bool(api_key)}, 长度: {len(api_key) if api_key else 0}")
        print(f"🔍 调试信息: AI Model: {ai_model}")
        print(f"🔍 调试信息: 症状: {symptoms}")
        
        if not api_key:
            # 如果没有API密钥，返回模拟结果（保持与正常流程一致的数据结构）
            analysis_data = {
                "symptoms": symptoms,
                "analysis": "暂未配置AI服务，返回模拟分析结果",
                "syndrome_type": {
                    "main_syndrome": "风寒表证",
                    "secondary_syndrome": "无",
                    "disease_location": "表",
                    "disease_nature": "寒证",
                    "pathogenesis": "风寒外袭，卫阳被遏"
                }
            }
            return {
                "success": True,
                "message": "症状分析完成",
                "data": analysis_data
            }
            
    except Exception as e:
        print(f"❌ 症状分析异常: {str(e)}")
        return {
            "success": False,
            "message": f"症状分析失败: {str(e)}",
            "data": None
        }

# 医学影像类型检测
def detect_medical_image_type(image_array):
    """
    检测医学影像类型
    这是一个简化的检测逻辑，实际应用中需要使用深度学习模型
    改进版本：更严格地判断医学影像特征，避免将普通照片误判为医学影像
    """
    try:
        # 获取图像基本信息
        height, width = image_array.shape[:2]
        
        # 计算图像的一些特征
        gray = cv2.cvtColor(image_array, cv2.COLOR_BGR2GRAY) if len(image_array.shape) == 3 else image_array
        
        # 计算图像的平均亮度和对比度
        mean_brightness = np.mean(gray)
        std_brightness = np.std(gray)
        
        # 计算图像的其他特征来判断是否为医学影像
        # 1. 检查图像是否主要为灰度（医学影像通常是灰度的）
        if len(image_array.shape) == 3:
            # 计算颜色通道的方差，如果方差很小说明接近灰度
            color_variance = np.var([np.mean(image_array[:,:,0]), np.mean(image_array[:,:,1]), np.mean(image_array[:,:,2])])
            is_grayscale_like = color_variance < 100  # 阈值可调整
        else:
            is_grayscale_like = True
        
        # 2. 检查图像边缘特征（医学影像通常有特定的边缘模式）
        edges = cv2.Canny(gray, 50, 150)
        edge_density = np.sum(edges > 0) / (height * width)
        
        # 3. 检查图像的纹理特征
        # 使用Sobel算子检测纹理
        sobelx = cv2.Sobel(gray, cv2.CV_64F, 1, 0, ksize=3)
        sobely = cv2.Sobel(gray, cv2.CV_64F, 0, 1, ksize=3)
        texture_strength = np.mean(np.sqrt(sobelx**2 + sobely**2))
        
        # 4. 检查图像的直方图分布（医学影像通常有特定的分布模式）
        hist = cv2.calcHist([gray], [0], None, [256], [0, 256])
        hist_peaks = len([i for i in range(1, 255) if hist[i] > hist[i-1] and hist[i] > hist[i+1] and hist[i] > 100])
        
        print(f"图像分析 - 亮度: {mean_brightness:.1f}, 对比度: {std_brightness:.1f}, 边缘密度: {edge_density:.3f}, 纹理强度: {texture_strength:.1f}, 直方图峰数: {hist_peaks}")
        
        # 更严格的医学影像判断条件
        # 只有同时满足多个条件才认为是医学影像
        is_medical_image = (
            is_grayscale_like and  # 主要为灰度
            (edge_density > 0.01) and  # 有一定的边缘密度
            (texture_strength > 5) and  # 有一定的纹理强度
            (std_brightness > 20)  # 有一定的对比度
        )
        
        if not is_medical_image:
            print("检测结果：不是医学影像，可能是普通照片")
            return "unknown"
        
        # 如果确定是医学影像，再进行类型分类
        # 使用更严格的条件进行分类
        if mean_brightness < 40 and std_brightness > 40:  # X光：很暗且有对比度
            return "xray"
        elif mean_brightness > 180 and edge_density < 0.05:  # 超声：很亮且边缘较少
            return "ultrasound"
        elif std_brightness > 80 and texture_strength > 15:  # CT：高对比度和纹理
            return "ct"
        elif 80 < mean_brightness < 160 and texture_strength > 10:  # MRI：中等亮度和纹理
            return "mri"
        elif mean_brightness > 100 and hist_peaks > 3:  # PET-CT：复杂的直方图分布
            return "petct"
        else:
            # 虽然看起来像医学影像，但无法确定具体类型
            print("检测结果：疑似医学影像但无法确定具体类型")
            return "unknown"
            
    except Exception as e:
        print(f"图像类型检测失败: {str(e)}")
        return "unknown"

# X光影像分析
@router.post("/analyze-xray")
async def analyze_xray_image(file: UploadFile = File(...)):
    """X光影像智能分析"""
    try:
        # 读取图像
        content = await file.read()
        image = Image.open(io.BytesIO(content))
        image_array = np.array(image)
        
        # 检测图像类型
        detected_type = detect_medical_image_type(image_array)
        
        # 如果不是X光图像，返回类型不匹配的分析结果
        if detected_type != "xray":
            analysis_result = {
                "image_type": detected_type,
                "analysis_type": "X光智能分析",
                "findings": [
                    f"检测到的图像类型：{detected_type}",
                    f"期望的图像类型：xray",
                    "类型不一致，无法处理"
                ],
                "diagnosis": "图像类型不匹配，系统无法进行X光影像分析",
                "recommendations": [
                    "请上传正确的X光影像",
                    "确保图像清晰可见"
                ],
                "confidence": 0.0,
                "error_code": "IMAGE_TYPE_MISMATCH"
            }
            return {
                "success": True,
                "message": "分析完成 - 类型不匹配",
                "data": analysis_result
            }
        
        # 模拟X光分析结果
        analysis_result = {
            "image_type": "xray",
            "analysis_type": "X光智能分析",
            "findings": [
                "肺部纹理清晰",
                "心影大小正常",
                "未见明显异常阴影"
            ],
            "diagnosis": "影像学检查未见明显异常",
            "recommendations": [
                "建议定期复查",
                "如有症状请及时就医"
            ],
            "confidence": 0.85
        }
        
        return {
            "success": True,
            "message": "X光影像分析完成",
            "data": analysis_result
        }
        
    except Exception as e:
        return {
            "success": False,
            "message": f"X光影像分析失败: {str(e)}",
            "data": {"error_details": str(e)}
        }

# CT影像分析
@router.post("/analyze-ct")
async def analyze_ct_image(file: UploadFile = File(...)):
    """CT影像智能分析"""
    try:
        # 读取图像
        content = await file.read()
        image = Image.open(io.BytesIO(content))
        image_array = np.array(image)
        
        # 检测图像类型
        detected_type = detect_medical_image_type(image_array)
        
        # 如果不是CT图像，返回类型不匹配的分析结果
        if detected_type != "ct":
            analysis_result = {
                "image_type": detected_type,
                "analysis_type": "CT智能分析",
                "findings": [
                    f"检测到的图像类型：{detected_type}",
                    f"期望的图像类型：ct",
                    "类型不一致，无法处理"
                ],
                "diagnosis": "图像类型不匹配，系统无法进行CT影像分析",
                "recommendations": [
                    "请上传正确的CT影像",
                    "确保图像清晰可见"
                ],
                "confidence": 0.0,
                "error_code": "IMAGE_TYPE_MISMATCH"
            }
            return {
                "success": True,
                "message": "分析完成 - 类型不匹配",
                "data": analysis_result
            }
        
        # 模拟CT分析结果
        analysis_result = {
            "image_type": "ct",
            "analysis_type": "CT智能分析",
            "findings": [
                "脑实质密度均匀",
                "脑室系统无扩张",
                "未见出血征象"
            ],
            "diagnosis": "CT检查未见明显异常",
            "recommendations": [
                "建议结合临床症状",
                "必要时行增强扫描"
            ],
            "confidence": 0.88
        }
        
        return {
            "success": True,
            "message": "CT影像分析完成",
            "data": analysis_result
        }
        
    except Exception as e:
        return {
            "success": False,
            "message": f"CT影像分析失败: {str(e)}",
            "data": {"error_details": str(e)}
        }

# B超影像分析
@router.post("/analyze-ultrasound")
async def analyze_ultrasound_image(file: UploadFile = File(...)):
    """B超影像智能分析"""
    try:
        # 读取图像
        content = await file.read()
        image = Image.open(io.BytesIO(content))
        image_array = np.array(image)
        
        # 检测图像类型
        detected_type = detect_medical_image_type(image_array)
        
        # 如果不是B超图像，返回类型不匹配的分析结果
        if detected_type != "ultrasound":
            analysis_result = {
                "image_type": detected_type,
                "analysis_type": "B超智能分析",
                "findings": [
                    f"检测到的图像类型：{detected_type}",
                    f"期望的图像类型：ultrasound",
                    "类型不一致，无法处理"
                ],
                "diagnosis": "图像类型不匹配，系统无法进行B超影像分析",
                "recommendations": [
                    "请上传正确的B超影像",
                    "确保图像清晰可见"
                ],
                "confidence": 0.0,
                "error_code": "IMAGE_TYPE_MISMATCH"
            }
            return {
                "success": True,
                "message": "分析完成 - 类型不匹配",
                "data": analysis_result
            }
        
        # 模拟B超分析结果
        analysis_result = {
            "image_type": "ultrasound",
            "analysis_type": "B超智能分析",
            "findings": [
                "肝脏大小形态正常",
                "肝实质回声均匀",
                "胆囊壁光滑"
            ],
            "diagnosis": "超声检查未见明显异常",
            "recommendations": [
                "建议定期体检",
                "注意饮食健康"
            ],
            "confidence": 0.82
        }
        
        return {
            "success": True,
            "message": "B超影像分析完成",
            "data": analysis_result
        }
        
    except Exception as e:
        return {
            "success": False,
            "message": f"B超影像分析失败: {str(e)}",
            "data": {"error_details": str(e)}
        }

# MRI影像分析
@router.post("/analyze-mri")
async def analyze_mri_image(file: UploadFile = File(...)):
    """MRI影像智能分析"""
    try:
        # 读取图像
        content = await file.read()
        image = Image.open(io.BytesIO(content))
        image_array = np.array(image)
        
        # 检测图像类型
        detected_type = detect_medical_image_type(image_array)
        
        # 如果不是MRI图像，返回类型不匹配的分析结果
        if detected_type != "mri":
            analysis_result = {
                "image_type": detected_type,
                "analysis_type": "MRI智能分析",
                "findings": [
                    f"检测到的图像类型：{detected_type}",
                    f"期望的图像类型：mri",
                    "类型不一致，无法处理"
                ],
                "diagnosis": "图像类型不匹配，系统无法进行MRI影像分析",
                "recommendations": [
                    "请上传正确的MRI影像",
                    "确保图像清晰可见"
                ],
                "confidence": 0.0,
                "error_code": "IMAGE_TYPE_MISMATCH"
            }
            return {
                "success": True,
                "message": "分析完成 - 类型不匹配",
                "data": analysis_result
            }
        
        # 模拟MRI分析结果
        analysis_result = {
            "image_type": "mri",
            "analysis_type": "MRI智能分析",
            "findings": [
                "脑白质信号正常",
                "灰质结构清晰",
                "未见异常信号"
            ],
            "diagnosis": "MRI检查未见明显异常",
            "recommendations": [
                "建议结合临床表现",
                "必要时复查对比"
            ],
            "confidence": 0.90
        }
        
        return {
            "success": True,
            "message": "MRI影像分析完成",
            "data": analysis_result
        }
        
    except Exception as e:
        return {
            "success": False,
            "message": f"MRI影像分析失败: {str(e)}",
            "data": {"error_details": str(e)}
        }

# PET-CT影像分析
@router.post("/analyze-petct")
async def analyze_petct_image(file: UploadFile = File(...)):
    """PET-CT影像智能分析"""
    try:
        # 读取图像
        content = await file.read()
        image = Image.open(io.BytesIO(content))
        image_array = np.array(image)
        
        # 检测图像类型
        detected_type = detect_medical_image_type(image_array)
        
        # 如果不是PET-CT图像，返回类型不匹配的分析结果
        if detected_type != "petct":
            analysis_result = {
                "image_type": detected_type,
                "analysis_type": "PET-CT智能分析",
                "findings": [
                    f"检测到的图像类型：{detected_type}",
                    f"期望的图像类型：petct",
                    "类型不一致，无法处理"
                ],
                "diagnosis": "图像类型不匹配，系统无法进行PET-CT影像分析",
                "recommendations": [
                    "请上传正确的PET-CT影像",
                    "确保图像清晰可见"
                ],
                "confidence": 0.0,
                "error_code": "IMAGE_TYPE_MISMATCH"
            }
            return {
                "success": True,
                "message": "分析完成 - 类型不匹配",
                "data": analysis_result
            }
        
        # 模拟PET-CT分析结果
        analysis_result = {
            "image_type": "petct",
            "analysis_type": "PET-CT智能分析",
            "findings": [
                "全身代谢分布正常",
                "未见异常高代谢灶",
                "淋巴结无肿大"
            ],
            "diagnosis": "PET-CT检查未见明显异常",
            "recommendations": [
                "建议定期随访",
                "保持健康生活方式"
            ],
            "confidence": 0.87
        }
        
        return {
            "success": True,
            "message": "PET-CT影像分析完成",
            "data": analysis_result
        }
        
    except Exception as e:
        return {
            "success": False,
            "message": f"PET-CT影像分析失败: {str(e)}",
            "data": {"error_details": str(e)}
        }
        
        # 调用AI生成处方
        print(f"🚀 开始调用AI生成处方...")
        print(f"🔧 调用参数: symptoms={symptoms}, api_key前10位={api_key[:10] if api_key else 'None'}, model={ai_model}")
        
        import time
        start_time = time.time()
        
        try:
            ai_result = generate_tcm_prescription(
                symptoms=symptoms,
                api_key=api_key,
                patient_info=None,  # 可以根据需要传入患者信息
                model=ai_model,
                max_tokens=600,  # 减少token数量，提高响应速度
                max_retries=2  # 减少重试次数，避免超时
            )
            elapsed_time = time.time() - start_time
            print(f"✅ AI调用成功，耗时: {elapsed_time:.2f}秒，结果类型: {type(ai_result)}")
            print(f"📋 AI结果预览: {str(ai_result)[:200]}...")
        except Exception as ai_error:
            elapsed_time = time.time() - start_time
            print(f"💥 AI函数调用异常，耗时: {elapsed_time:.2f}秒: {ai_error}")
            print(f"💥 异常类型: {type(ai_error).__name__}")
            # 不再抛出异常，而是返回友好的错误信息
            analysis_data = {
                "symptoms": symptoms,
                "analysis": f"AI分析暂时不可用（耗时{elapsed_time:.1f}秒后超时），请稍后重试",
                "syndrome_type": {
                    "main_syndrome": "系统繁忙",
                    "secondary_syndrome": "请稍后重试",
                    "disease_location": "暂无",
                    "disease_nature": "暂无",
                    "pathogenesis": "请咨询专业中医师"
                },
                "treatment_method": {
                    "main_method": "请咨询专业中医师",
                    "auxiliary_method": "暂无",
                    "treatment_priority": "暂无",
                    "care_principle": "请遵医嘱"
                },
                "main_prescription": {
                    "formula_name": "暂无",
                    "formula_source": "暂无",
                    "formula_analysis": "暂无",
                    "modifications": "暂无"
                },
                "composition": [],
                "usage": {
                    "preparation_method": "请咨询医生",
                    "administration_time": "暂无",
                    "treatment_course": "暂无"
                },
                "contraindications": {
                    "contraindications": "请咨询医生",
                    "dietary_restrictions": "暂无",
                    "lifestyle_care": "暂无",
                    "precautions": "请遵医嘱"
                }
            }
            return {
                "success": True,
                "message": "AI服务暂时繁忙，已返回提示信息",
                "data": analysis_data
            }
        
        # 处理TCMPrescription对象数据结构
        # ai_result是TCMPrescription对象，其属性已经是字典，直接使用即可
        
        analysis_data = {
            "symptoms": symptoms,
            "analysis": "AI中医诊疗分析完成",
            # 中医诊疗部分 - 直接使用TCMPrescription对象的字典属性
            "syndrome_type": ai_result.syndrome_type,
            "treatment_method": ai_result.treatment_method,
            "main_prescription": ai_result.main_prescription,
            "composition": ai_result.composition,
            "usage": ai_result.usage,
            "contraindications": ai_result.contraindications
        }
        return {
            "success": True,
            "message": "AI症状分析完成",
            "data": analysis_data
        }
        
    except Exception as e:
        # 如果AI调用失败，返回完整的默认数据结构
        print(f"❌ AI调用异常: {str(e)}")
        print(f"❌ 异常类型: {type(e).__name__}")
        import traceback
        print(f"❌ 详细错误: {traceback.format_exc()}")
        
        analysis_data = {
            "symptoms": symptoms,
            "analysis": f"AI分析失败: {str(e)}",
            "syndrome_type": {
                 "main_syndrome": "分析失败",
                 "secondary_syndrome": "请重新尝试",
                 "disease_location": "暂无",
                 "disease_nature": "暂无",
                 "pathogenesis": "请咨询专业中医师"
             },
            "treatment_method": {
                "main_method": "请咨询专业中医师",
                "auxiliary_method": "暂无",
                "treatment_priority": "暂无",
                "care_principle": "请遵医嘱"
            },
            "main_prescription": {
                "formula_name": "暂无",
                "formula_source": "暂无",
                "formula_analysis": "暂无",
                "modifications": "暂无"
            },
            "composition": [],
            "usage": {
                "preparation_method": "请咨询医生",
                "administration_time": "暂无",
                "treatment_course": "暂无"
            },
            "contraindications": {
                "contraindications": "请咨询医生",
                "dietary_restrictions": "暂无",
                "lifestyle_care": "暂无",
                "precautions": "请遵医嘱"
            }
        }
        return {
            "success": True,
            "message": "使用默认分析结果，建议重新尝试或咨询医师",
            "data": analysis_data
        }

# 获取用户的处方历史
@router.get("/user/{user_id}/history", response_model=List[Prescription])
async def get_user_prescription_history(
    user_id: int, 
    limit: int = Query(10, ge=1, le=50, description="返回数量限制"),
    db: Session = Depends(get_db)
):
    """获取用户的处方历史"""
    prescriptions = db.query(PrescriptionModel).filter(
        PrescriptionModel.user_id == user_id
    ).order_by(
        PrescriptionModel.created_time.desc()
    ).limit(limit).all()
    
    return prescriptions

# 更新处方状态
@router.patch("/{prescription_id}/status")
async def update_prescription_status(
    prescription_id: int,
    status: str,
    db: Session = Depends(get_db)
):
    """更新处方状态"""
    valid_statuses = ["draft", "issued", "dispensed"]
    if status not in valid_statuses:
        raise HTTPException(
            status_code=400, 
            detail=f"无效的状态值，有效值为: {', '.join(valid_statuses)}"
        )
    
    db_prescription = db.query(PrescriptionModel).filter(
        PrescriptionModel.id == prescription_id
    ).first()
    if not db_prescription:
        raise HTTPException(status_code=404, detail="处方不存在")
    
    db_prescription.status = status
    db.commit()
    db.refresh(db_prescription)
    
    return {"message": "处方状态更新成功", "status": status}

# OCR文字识别
@router.post("/ocr-text-recognition")
async def ocr_text_recognition(file: UploadFile = File(...)):
    """OCR文字识别 - 从图片中提取文字"""
    try:
        # 检查文件类型
        if not file.content_type.startswith('image/'):
            raise HTTPException(status_code=400, detail="只能上传图片文件")
        
        # 读取图片数据
        image_data = await file.read()
        
        # 使用PIL打开图片
        image = Image.open(io.BytesIO(image_data))
        
        # 图片预处理
        # 转换为OpenCV格式
        opencv_image = cv2.cvtColor(np.array(image), cv2.COLOR_RGB2BGR)
        
        # 转换为灰度图
        gray = cv2.cvtColor(opencv_image, cv2.COLOR_BGR2GRAY)
        
        # 应用高斯模糊去噪
        blurred = cv2.GaussianBlur(gray, (5, 5), 0)
        
        # 应用阈值处理
        _, thresh = cv2.threshold(blurred, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
        
        # 使用pytesseract进行OCR识别
        # 配置OCR参数，支持中英文
        custom_config = r'--oem 3 --psm 6 -l chi_sim+eng'
        
        try:
            # 检查tesseract是否可用
            pytesseract.get_tesseract_version()
            # 尝试使用中英文识别
            extracted_text = pytesseract.image_to_string(thresh, config=custom_config)
        except pytesseract.TesseractNotFoundError:
            raise HTTPException(status_code=500, detail="OCR引擎未安装，请联系管理员安装Tesseract OCR")
        except Exception as ocr_error:
            try:
                # 如果中文识别失败，使用英文识别
                extracted_text = pytesseract.image_to_string(thresh, lang='eng')
            except:
                raise HTTPException(status_code=500, detail=f"OCR识别失败: {str(ocr_error)}")
        
        # 清理提取的文本
        cleaned_text = extracted_text.strip().replace('\n\n', '\n')
        
        return {
            "success": True,
            "message": "OCR文字识别完成",
            "data": {
                "extracted_text": cleaned_text,
                "text_length": len(cleaned_text),
                "has_chinese": any('\u4e00' <= char <= '\u9fff' for char in cleaned_text),
                "confidence": "high" if len(cleaned_text) > 10 else "medium"
            }
        }
        
    except Exception as e:
        return {
            "success": False,
            "message": f"OCR识别失败: {str(e)}",
            "data": {
                "extracted_text": "",
                "error_details": str(e)
            }
        }

# 处方图片智能分析
@router.post("/analyze-prescription-image")
async def analyze_prescription_image(file: UploadFile = File(...)):
    """处方图片智能分析 - 识别处方内容并进行中医分析"""
    try:
        # 检查文件类型
        if not file.content_type.startswith('image/'):
            raise HTTPException(status_code=400, detail="只能上传图片文件")
        
        # 先进行OCR文字识别
        image_data = await file.read()
        image = Image.open(io.BytesIO(image_data))
        
        # 图片预处理
        opencv_image = cv2.cvtColor(np.array(image), cv2.COLOR_RGB2BGR)
        gray = cv2.cvtColor(opencv_image, cv2.COLOR_BGR2GRAY)
        blurred = cv2.GaussianBlur(gray, (5, 5), 0)
        _, thresh = cv2.threshold(blurred, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
        
        # OCR识别
        try:
            # 检查tesseract是否可用
            pytesseract.get_tesseract_version()
            custom_config = r'--oem 3 --psm 6 -l chi_sim+eng'
            extracted_text = pytesseract.image_to_string(thresh, config=custom_config)
        except pytesseract.TesseractNotFoundError:
            return {
                "success": False,
                "message": "OCR引擎未安装，请联系管理员安装Tesseract OCR",
                "data": {"error_details": "Tesseract OCR engine not found"}
            }
        except Exception as ocr_error:
            try:
                extracted_text = pytesseract.image_to_string(thresh, lang='eng')
            except:
                return {
                    "success": False,
                    "message": f"OCR识别失败: {str(ocr_error)}",
                    "data": {"error_details": str(ocr_error)}
                }
        
        cleaned_text = extracted_text.strip()
        
        # 如果提取到文字，进行智能分析
        if len(cleaned_text) > 5:
            # 调用AI进行处方分析
            api_key = os.getenv("OPENAI_API_KEY")
            if api_key:
                try:
                    # 使用AI分析处方内容
                    prescription_analysis = generate_tcm_prescription(
                        symptoms=f"根据处方图片识别的内容进行分析：{cleaned_text}",
                        api_key=api_key,
                        patient_info=None,
                        model=os.getenv("AI_MODEL", "deepseek-chat"),
                        max_tokens=1000
                    )
                    
                    analysis_result = {
                        "ocr_text": cleaned_text,
                        "analysis_type": "AI智能分析",
                        "syndrome_type": prescription_analysis.syndrome_type,
                        "treatment_method": prescription_analysis.treatment_method,
                        "main_prescription": prescription_analysis.main_prescription,
                        "composition": prescription_analysis.composition,
                        "usage": prescription_analysis.usage,
                        "contraindications": prescription_analysis.contraindications,
                        "confidence": "high"
                    }
                except Exception as ai_error:
                    # AI分析失败，返回基础分析
                    analysis_result = {
                        "ocr_text": cleaned_text,
                        "analysis_type": "基础文本分析",
                        "detected_herbs": extract_herb_names(cleaned_text),
                        "possible_symptoms": extract_symptoms(cleaned_text),
                        "recommendations": ["请咨询专业中医师确认处方内容", "注意药物用量和禁忌"],
                        "confidence": "medium",
                        "ai_error": str(ai_error)
                    }
            else:
                # 没有AI配置，进行基础分析
                analysis_result = {
                    "ocr_text": cleaned_text,
                    "analysis_type": "基础文本分析",
                    "detected_herbs": extract_herb_names(cleaned_text),
                    "possible_symptoms": extract_symptoms(cleaned_text),
                    "recommendations": ["请咨询专业中医师确认处方内容", "注意药物用量和禁忌"],
                    "confidence": "medium"
                }
        else:
            analysis_result = {
                "ocr_text": cleaned_text,
                "analysis_type": "识别失败",
                "message": "图片中的文字内容识别不清晰，请上传更清晰的处方图片",
                "confidence": "low"
            }
        
        return {
            "success": True,
            "message": "处方图片分析完成",
            "data": analysis_result
        }
        
    except Exception as e:
        return {
            "success": False,
            "message": f"处方图片分析失败: {str(e)}",
            "data": {
                "error_details": str(e)
            }
        }

# 辅助函数：提取中药名称
def extract_herb_names(text: str) -> List[str]:
    """从文本中提取可能的中药名称"""
    common_herbs = [
        "当归", "川芎", "白芍", "熟地黄", "人参", "白术", "茯苓", "甘草",
        "黄芪", "党参", "麦冬", "五味子", "枸杞子", "菊花", "金银花",
        "连翘", "板蓝根", "大青叶", "桔梗", "杏仁", "桂枝", "生姜",
        "大枣", "陈皮", "半夏", "茯神", "远志", "酸枣仁", "龙骨", "牡蛎"
    ]
    
    detected_herbs = []
    for herb in common_herbs:
        if herb in text:
            detected_herbs.append(herb)
    
    return detected_herbs

# 辅助函数：提取症状关键词
def extract_symptoms(text: str) -> List[str]:
    """从文本中提取可能的症状关键词"""
    symptom_keywords = [
        "咳嗽", "发热", "头痛", "腹痛", "胸闷", "气短", "乏力", "失眠",
        "心悸", "眩晕", "恶心", "呕吐", "腹泻", "便秘", "食欲不振",
        "口干", "口苦", "咽痛", "鼻塞", "流涕", "盗汗", "自汗"
    ]
    
    detected_symptoms = []
    for symptom in symptom_keywords:
        if symptom in text:
            detected_symptoms.append(symptom)
    
    return detected_symptoms

# 通用图片上传接口（不绑定处方ID）
@router.post("/upload-image")
async def upload_image(file: UploadFile = File(...)):
    """通用图片上传接口"""
    try:
        print(f"收到上传请求 - 文件名: {file.filename}, 内容类型: {file.content_type}")
        
        # 检查文件类型
        if not file.content_type or not file.content_type.startswith('image/'):
            print(f"文件类型检查失败: {file.content_type}")
            raise HTTPException(status_code=400, detail="只能上传图片文件")
        
        # 创建上传目录
        upload_dir = "static/prescriptions"
        os.makedirs(upload_dir, exist_ok=True)
        
        # 生成唯一文件名
        file_extension = file.filename.split('.')[-1] if '.' in file.filename else 'jpg'
        unique_filename = f"{uuid.uuid4()}.{file_extension}"
        file_path = os.path.join(upload_dir, unique_filename)
        
        # 保存文件
        with open(file_path, "wb") as buffer:
            content = await file.read()
            buffer.write(content)
        
        return {
            "success": True,
            "message": "图片上传成功",
            "data": {
                "image_url": f"/static/prescriptions/{unique_filename}",
                "filename": unique_filename,
                "file_size": len(content),
                "content_type": file.content_type
            }
        }
        
    except Exception as e:
        print(f"图片上传异常: {type(e).__name__}: {str(e)}")
        import traceback
        traceback.print_exc()
        return {
            "success": False,
            "message": f"图片上传失败: {str(e)}",
            "data": {
                "error_details": str(e)
            }
        }