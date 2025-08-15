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
    try:
        # 从环境变量获取API密钥和模型配置
        api_key = os.getenv("OPENAI_API_KEY")
        ai_model = os.getenv("AI_MODEL", "deepseek-chat")
        if not api_key:
            # 如果没有API密钥，返回模拟结果
            analysis_data = {
                "symptoms": symptoms,
                "analysis": "暂未配置AI服务，返回模拟分析结果",
                "syndrome_type": "风寒表证",
                "treatment_method": "辛温解表",
                "main_prescription": "桂枝汤加减",
                "composition": [
                    {"药材": "桂枝", "剂量": "10g", "角色": "君药"},
                    {"药材": "白芍", "剂量": "10g", "角色": "臣药"},
                    {"药材": "生姜", "剂量": "6g", "角色": "佐药"},
                    {"药材": "大枣", "剂量": "3枚", "角色": "使药"},
                    {"药材": "甘草", "剂量": "6g", "角色": "使药"}
                ],
                "usage": "水煎服，每日1剂，分2次温服",
                "contraindications": "孕妇慎用，高血压患者注意监测血压"
            }
            return {
                "success": True,
                "message": "症状分析完成",
                "data": analysis_data
            }
        
        # 调用AI生成处方
        prescription = generate_tcm_prescription(
            symptoms=symptoms,
            api_key=api_key,
            patient_info=None,  # 可以根据需要传入患者信息
            model=ai_model,
            max_tokens=1000
        )
        
        analysis_data = {
            "symptoms": symptoms,
            "analysis": "AI中医辨证分析完成",
            "syndrome_type": prescription.syndrome_type,
            "treatment_method": prescription.treatment_method,
            "main_prescription": prescription.main_prescription,
            "composition": prescription.composition,
            "usage": prescription.usage,
            "contraindications": prescription.contraindications
        }
        return {
            "success": True,
            "message": "AI症状分析完成",
            "data": analysis_data
        }
        
    except Exception as e:
        # 如果AI调用失败，返回错误信息
        analysis_data = {
            "symptoms": symptoms,
            "analysis": f"AI分析失败: {str(e)}",
            "syndrome_type": "分析失败",
            "treatment_method": "请咨询专业中医师",
            "main_prescription": "暂无",
            "composition": [],
            "usage": "请遵医嘱",
            "contraindications": "请咨询医生"
        }
        return {
            "success": False,
            "message": f"分析失败: {str(e)}",
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