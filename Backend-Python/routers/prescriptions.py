from re import A
from fastapi import APIRouter, Depends, HTTPException, Query, UploadFile, File, Form
from sqlalchemy.orm import Session
from sqlalchemy.exc import SQLAlchemyError, IntegrityError, OperationalError
from typing import List, Optional
import os
import uuid
import cv2
import numpy as np
import pytesseract
from PIL import Image
import io
import logging
import time
import asyncio
import tempfile

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# 配置Tesseract路径
pytesseract.pytesseract.tesseract_cmd = r"C:\Program Files\Tesseract-OCR\tesseract.exe"

try:
    from database import get_db
    from models import Prescription as PrescriptionModel
    from schemas import Prescription, PrescriptionCreate, PrescriptionUpdate, PaginatedResponse
except ImportError as e:
    logger.error(f"导入模块失败: {e}")
    raise HTTPException(status_code=500, detail=f"服务器配置错误: {str(e)}")

# 导入AI处方生成功能
try:
    from ai.ai_prescription import generate_tcm_prescription
except ImportError as e:
    logger.warning(f"AI处方生成模块导入失败: {e}")
    generate_tcm_prescription = None

# 导入DashScope医学影像分析功能
try:
    from ai.ai_prescription import (
        analyze_medical_image_dashscope,
        analyze_medical_image_dashscope_simple,
        format_image_analysis_result,
        analyze_tcm_tongue_diagnosis_dashscope,
        analyze_tcm_face_diagnosis_dashscope
    )
except ImportError as e:
    logger.warning(f"DashScope医学影像分析模块导入失败: {e}")
    analyze_medical_image_dashscope = None
    analyze_medical_image_dashscope_simple = None
    format_image_analysis_result = None
    analyze_tcm_tongue_diagnosis_dashscope = None
    analyze_tcm_face_diagnosis_dashscope = None

router = APIRouter()

# 网络连接检查函数
async def check_network_connection():
    """检查网络连接状态"""
    try:
        # 这里可以添加实际的网络连接检查逻辑
        return True
    except Exception as e:
        logger.error(f"网络连接检查失败: {e}")
        return False

# 数据库连接重试函数
def retry_db_operation(operation, max_retries=3, delay=1):
    """数据库操作重试机制"""
    for attempt in range(max_retries):
        try:
            return operation()
        except OperationalError as e:
            if attempt == max_retries - 1:
                logger.error(f"数据库连接失败，已重试{max_retries}次: {e}")
                raise HTTPException(
                    status_code=503, 
                    detail="数据库连接失败，请检查网络连接或稍后重试"
                )
            logger.warning(f"数据库操作失败，第{attempt + 1}次重试: {e}")
            time.sleep(delay)
        except Exception as e:
            logger.error(f"数据库操作异常: {e}")
            raise

# 健康检查端点
@router.get("/health")
async def health_check():
    """健康检查端点"""
    try:
        network_ok = await check_network_connection()
        return {
            "status": "healthy" if network_ok else "degraded",
            "network": "connected" if network_ok else "disconnected",
            "service": "prescriptions",
            "timestamp": str(uuid.uuid4())
        }
    except Exception as e:
        logger.error(f"健康检查失败: {e}")
        return {
            "status": "unhealthy",
            "error": str(e),
            "service": "prescriptions"
        }

# 简单测试端点
@router.get("/test-simple")
async def test_simple():
    """简单测试端点"""
    try:
        network_ok = await check_network_connection()
        if not network_ok:
            raise HTTPException(
                status_code=503, 
                detail="网络连接异常，请检查网络连接后重试"
            )
        return {
            "message": "处方路由工作正常",
            "network_status": "connected",
            "timestamp": str(uuid.uuid4())
        }
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"测试端点异常: {e}")
        raise HTTPException(
            status_code=500, 
            detail=f"服务异常: {str(e)}"
        )

# 症状分析端点
@router.post("/analyze-symptoms")
async def analyze_symptoms(symptoms: str = Form(...)):
    """症状分析（集成AI中医处方生成）"""
    logger.info(f"🎯 症状分析端点被调用，症状: {symptoms}")
    
    try:
        # 检查网络连接
        network_ok = await check_network_connection()
        logger.info(f"网络连接状态: {network_ok}")
        
        if not network_ok:
            logger.warning("网络连接异常")
            response = {
                "success": False,
                "message": "网络连接异常，请检查网络连接后重试",
                "error_code": "NETWORK_ERROR",
                "data": None
            }
            logger.info(f"返回响应: {response}")
            return response
        
        # 验证输入
        if not symptoms or len(symptoms.strip()) == 0:
            logger.error("症状描述为空")
            raise HTTPException(
                status_code=400, 
                detail="症状描述不能为空"
            )
        
        logger.info(f"开始AI分析症状: {symptoms}")
        
        # 检查AI功能是否可用
        if generate_tcm_prescription is None:
            logger.warning("AI处方生成功能不可用")
            response = {
                "success": False,
                "message": "AI处方生成功能暂时不可用",
                "error_code": "AI_UNAVAILABLE",
                "data": {
                    "symptoms": symptoms,
                    "analysis": "AI处方生成功能暂时不可用，请检查系统配置",
                    "network_status": "connected",
                    "timestamp": str(uuid.uuid4())
                }
            }
        else:
            try:
                # 调用AI生成中医处方
                logger.info("正在调用AI生成中医处方...")
                prescription = generate_tcm_prescription(
                    symptoms=symptoms,
                    patient_info=None,  # 可以后续扩展患者信息
                    max_tokens=1500
                )
                
                logger.info("AI处方生成成功")
                
                # 构建响应数据
                response = {
                    "success": True,
                    "message": "AI症状分析完成",
                    "data": {
                        "symptoms": symptoms,
                        "syndrome_type": prescription.syndrome_type,
                        "treatment_method": prescription.treatment_method,
                        "main_prescription": prescription.main_prescription,
                        "composition": prescription.composition,
                        "usage": prescription.usage,
                        "contraindications": prescription.contraindications,
                        "network_status": "connected",
                        "timestamp": str(uuid.uuid4())
                    }
                }
                
            except Exception as ai_error:
                logger.error(f"AI分析失败: {ai_error}")
                # AI失败时返回基础分析结果
                response = {
                    "success": False,
                    "message": f"AI分析暂时不可用: {str(ai_error)}",
                    "error_code": "AI_ERROR",
                    "data": {
                        "symptoms": symptoms,
                        "analysis": "AI服务暂时不可用，请稍后重试",
                        "network_status": "connected",
                        "timestamp": str(uuid.uuid4())
                    }
                }
        
        logger.info(f"症状分析完成，返回响应: {response}")
        return response
        
    except HTTPException as he:
        logger.error(f"HTTP异常: {he}")
        raise
    except Exception as e:
        logger.error(f"症状分析异常: {e}")
        response = {
            "success": False,
            "message": f"症状分析失败: {str(e)}",
            "error_code": "ANALYSIS_ERROR",
            "data": None
        }
        logger.info(f"异常响应: {response}")
        return response


# 医学影像分析端点
@router.post("/analyze-xray")
async def analyze_xray_image(image: UploadFile = File(...)):
    """X光影像AI分析"""
    logger.info(f"🩻 X光影像分析端点被调用，文件: {image.filename}")
    return await analyze_medical_image(image, "xray")


@router.post("/analyze-ct")
async def analyze_ct_image(image: UploadFile = File(...)):
    """CT影像AI分析"""
    logger.info(f"🏥 CT影像分析端点被调用，文件: {image.filename}")
    return await analyze_medical_image(image, "ct")


@router.post("/analyze-ultrasound")
async def analyze_ultrasound_image(image: UploadFile = File(...)):
    """B超影像AI分析"""
    logger.info(f"🔊 B超影像分析端点被调用，文件: {image.filename}")
    return await analyze_medical_image(image, "ultrasound")


@router.post("/analyze-mri")
async def analyze_mri_image(image: UploadFile = File(...)):
    """MRI影像AI分析"""
    logger.info(f"🧠 MRI影像分析端点被调用，文件: {image.filename}")
    return await analyze_medical_image(image, "mri")


@router.post("/analyze-petct")
async def analyze_petct_image(image: UploadFile = File(...)):
    """PET-CT影像AI分析"""
    logger.info(f"⚛️ PET-CT影像分析端点被调用，文件: {image.filename}")
    return await analyze_medical_image(image, "petct")

@router.post("/analyze-tongue")
async def analyze_tcm_tongue_diagnosis(image: UploadFile = File(...)):
    """AI中医舌诊专业分析"""
    logger.info(f"🏥 AI中医舌诊端点被调用，文件: {image.filename}")
    
    try:
        # 验证文件类型
        if not image.content_type or not image.content_type.startswith('image/'):
            logger.error(f"无效的文件类型: {image.content_type}")
            return {
                "success": False,
                "message": "请上传有效的图片文件",
                "error_code": "IMAGE_TYPE_MISMATCH",
                "data": None
            }
        
        # 检查文件大小（限制为10MB）
        content = await image.read()
        if len(content) > 10 * 1024 * 1024:
            logger.error(f"文件过大: {len(content)} bytes")
            return {
                "success": False,
                "message": "图片文件大小不能超过10MB",
                "error_code": "FILE_TOO_LARGE",
                "data": None
            }
        
        # 重置文件指针
        await image.seek(0)
        
        # 检查网络连接
        network_ok = await check_network_connection()
        if not network_ok:
            logger.warning("网络连接异常，返回模拟分析结果")
            return generate_mock_tcm_tongue_analysis()
        
        # 检查AI功能是否可用
        if analyze_tcm_tongue_diagnosis_dashscope is None:
            logger.warning("AI中医舌诊功能不可用，返回模拟分析结果")
            return generate_mock_tcm_tongue_analysis()
        
        try:
            # 保存临时图像文件
            # extension = os.path.splitext(image.filename)[1]
            # temp_image_path = f"temp_tongue_{uuid.uuid4().hex}{extension}"
            # with open(temp_image_path, "wb") as temp_file:
            #     temp_file.write(content)
                # 创建临时文件
            with tempfile.NamedTemporaryFile(delete=False, suffix=os.path.splitext(image.filename)[1]) as tmp_file:
                # 将上传的文件内容写入临时文件
                content = await image.read()
                tmp_file.write(content)
                temp_file_path = tmp_file.name
            
            try:
                logger.info(f"开始AI中医舌诊分析...{temp_file_path}")
                # 调用专门的中医舌诊分析函数
                ai_result = analyze_tcm_tongue_diagnosis_dashscope(
                    image_path=temp_file_path,
                    image_type="舌诊"
                )
                
                logger.info("AI中医舌诊分析成功")
                return {
                    "success": True,
                    "message": "AI中医舌诊分析完成",
                    "data": ai_result
                }
            except Exception as e:
                error_str = str(e)
                if "IMAGE_TYPE_MISMATCH" in error_str:
                    logger.warning(f"图像类型不匹配: {error_str}")
                    return {
                        "success": False,
                        "message": "上传的图像类型与请求的分析类型不匹配",
                        "error_code": "IMAGE_TYPE_MISMATCH",
                        "data": None
                    }
                else:
                    return generate_mock_tcm_tongue_analysis()
            finally:
                # 清理临时文件
                if os.path.exists(temp_file_path):
                    os.remove(temp_file_path)
            
        except Exception as ai_error:
            logger.error(f"AI中医舌诊分析失败: {ai_error}")
            return generate_mock_tcm_tongue_analysis()

    except Exception as e:
        logger.error(f"中医舌诊分析异常: {e}")
        return {
            "success": False,
            "message": f"舌诊分析失败: {str(e)}",
            "data": None
        }

@router.post("/analyze-face")
async def analyze_face_image(image: UploadFile = File(...)):
    """中医面诊AI分析"""
    logger.info(f"⚛️ 中医面诊分析端点被调用，文件: {image.filename}")
    
    try:
        # 验证文件类型
        if not image.content_type or not image.content_type.startswith('image/'):
            logger.error(f"无效的文件类型: {image.content_type}")
            return {
                "success": False,
                "message": "请上传有效的图片文件",
                "error_code": "IMAGE_TYPE_MISMATCH",
                "data": None
            }
        
        # 检查文件大小（限制为10MB）
        content = await image.read()
        if len(content) > 10 * 1024 * 1024:
            logger.error(f"文件过大: {len(content)} bytes")
            return {
                "success": False,
                "message": "图片文件大小不能超过10MB",
                "error_code": "FILE_TOO_LARGE",
                "data": None
            }
        
        # 重置文件指针
        await image.seek(0)
        
        # 检查网络连接
        network_ok = await check_network_connection()
        if not network_ok:
            logger.warning("网络连接异常，返回模拟分析结果")
            return generate_mock_tcm_face_analysis()
        
        # 检查AI功能是否可用
        if analyze_tcm_face_diagnosis_dashscope is None:
            logger.warning("AI中医面诊功能不可用，返回模拟分析结果")
            return generate_mock_tcm_face_analysis()
        
        try:
            # 保存临时图像文件
            extension = os.path.splitext(image.filename)[1]
            temp_image_path = f"temp_face_{uuid.uuid4().hex}{extension}"
            with open(temp_image_path, "wb") as temp_file:
                temp_file.write(content)
            
            try:
                logger.info("开始AI中医面诊分析...")
                # 调用专门的中医面诊分析函数
                ai_result = analyze_tcm_face_diagnosis_dashscope(
                    image_path=temp_image_path,
                    image_type="面诊"
                )
                
                logger.info("AI中医面诊分析成功")
                logger.info(f"{ai_result}")
                return {
                    "success": True,
                    "message": "AI中医面诊分析完成",
                    "data": ai_result
                }
            except Exception as e:
                error_str = str(e)
                if "IMAGE_TYPE_MISMATCH" in error_str:
                    logger.warning(f"图像类型不匹配: {error_str}")
                    return {
                        "success": False,
                        "message": "上传的图像类型与请求的分析类型不匹配",
                        "error_code": "IMAGE_TYPE_MISMATCH",
                        "data": None
                    }
                else:
                    return generate_mock_tcm_face_analysis()
            finally:
                # 清理临时文件
                if os.path.exists(temp_image_path):
                    os.remove(temp_image_path)
            
        except Exception as ai_error:
            logger.error(f"AI中医面诊分析失败: {ai_error}")
            # AI分析失败时返回模拟结果
            return generate_mock_tcm_face_analysis()
        
    except Exception as e:
        logger.error(f"中医面诊分析异常: {e}")
        return {
            "success": False,
            "message": f"面诊分析失败: {str(e)}",
            "data": None
        }    

async def analyze_medical_image(image: UploadFile, image_type: str):
    """通用医学影像AI分析函数"""
    try:
        # 验证文件类型
        if not image.content_type or not image.content_type.startswith('image/'):
            logger.error(f"无效的文件类型: {image.content_type}")
            return {
                "success": False,
                "message": "请上传有效的图片文件",
                "error_code": "IMAGE_TYPE_MISMATCH",
                "data": None
            }
        
        # 检查文件大小（限制为10MB）
        content = await image.read()
        if len(content) > 10 * 1024 * 1024:
            logger.error(f"文件过大: {len(content)} bytes")
            return {
                "success": False,
                "message": "图片文件大小不能超过10MB",
                "error_code": "FILE_TOO_LARGE",
                "data": None
            }
        
        # 重置文件指针
        await image.seek(0)
        
        # 检查网络连接
        network_ok = await check_network_connection()
        if not network_ok:
            logger.warning("网络连接异常，返回模拟分析结果")
            return generate_mock_medical_analysis(image_type)
        
        # 检查AI功能是否可用
        if analyze_medical_image_dashscope is None:
            logger.warning("AI功能不可用，返回模拟分析结果")
            return generate_mock_medical_analysis(image_type)
        
        try:
            # 这里可以集成真实的医学影像AI分析
            # 目前使用基于影像类型的智能分析逻辑
            logger.info(f"开始AI分析{get_image_type_name(image_type)}影像...")
            
            # 构建针对医学影像的分析提示
            analysis_prompt = f"请分析这张{get_image_type_name(image_type)}医学影像，提供专业的医学诊断建议。"
            
            # 调用AI分析（应该是图像分析）
            # ai_result = generate_tcm_prescription(
            #     symptoms=analysis_prompt,
            #     patient_info=None,
            #     max_tokens=1000
            # )
            
            # 保存临时图像文件
            extension = os.path.splitext(image.filename)[1]
            temp_image_path = f"temp_image_{uuid.uuid4().hex}{extension}"
            with open(temp_image_path, "wb") as temp_file:
                temp_file.write(content)
            
            try:
                # 转换影像类型格式：小写转换为后端期望的格式
                image_type_mapping = {
                    "xray": "X-ray",
                    "ct": "CT", 
                    "ultrasound": "Ultrasound",
                    "mri": "MRI",
                    "petct": "PET-CT",
                    "tongue": "中医舌诊",
                    "face": "中医面诊"
                }
                backend_image_type = image_type_mapping.get(image_type, image_type)
                
                ai_result = analyze_medical_image_dashscope(
                    image_path=temp_image_path,
                    image_type=backend_image_type,
                    patient_info=None
                )
            except Exception as e:
                # 检查是否为图像类型不匹配错误
                error_str = str(e)
                if "IMAGE_TYPE_MISMATCH" in error_str:
                    logger.warning(f"图像类型不匹配: {error_str}")
                    return {
                        "success": False,
                        "message": "上传的图像类型与请求的分析类型不匹配",
                        "error_code": "IMAGE_TYPE_MISMATCH",
                        "data": None
                    }
                else:
                    return generate_mock_medical_analysis()
            finally:
                # 清理临时文件
                if os.path.exists(temp_image_path):
                    os.remove(temp_image_path)
            
            # 构建医学影像分析响应
            # analysis_data = {
            #     "image_type": image_type,
            #     "image_type_display": get_image_type_name(image_type),
            #     "analysis_result": {
            #         "image_quality": "良好",
            #         "main_findings": ai_result.main_prescription if ai_result.main_prescription else "AI分析完成",
            #         "recommendations": ai_result.usage if ai_result.usage else "请咨询专业医师获取详细解读",
            #         "syndrome_analysis": ai_result.syndrome_type if ai_result.syndrome_type else None,
            #         "treatment_suggestions": ai_result.treatment_method if ai_result.treatment_method else None
            #     },
            #     "confidence_score": 0.85,
            #     "analysis_timestamp": str(uuid.uuid4()),
            #     "disclaimer": "此为AI辅助分析结果，仅供参考，请以专业医师诊断为准。"
            # }
            analysis_data = {
                "image_type": image_type,
                "findings": ai_result.findings,
                "diagnosis": ai_result.diagnosis,
                "recommendations": ai_result.recommendations,
                "severity": ai_result.severity,
                "confidence": ai_result.confidence
            }
            
            logger.info(f"{get_image_type_name(image_type)}影像AI分析成功")
            return {
                "success": True,
                "message": f"{get_image_type_name(image_type)}影像AI分析完成",
                "data": analysis_data
            }
            
        except Exception as ai_error:
            logger.error(f"AI分析失败: {ai_error}")
            
            # 其他AI分析失败时返回模拟结果
            return generate_mock_medical_analysis(image_type)
        
    except Exception as e:
        logger.error(f"医学影像分析异常: {e}")
        return {
            "success": False,
            "message": f"影像分析失败: {str(e)}",
            "data": None
        }


def generate_mock_tcm_tongue_analysis():
    """生成模拟的中医舌诊分析结果"""
    mock_analysis = {
        "image_type": "中医舌诊",
        "tongue_analysis": {
            "tongue_body": {
                "color": "未显示",
                "shape": "未显示",
                "texture": "未显示",
                "mobility": "未显示"
            },
            "tongue_coating": {
                "color": "未显示",
                "thickness": "未显示",
                "moisture": "未显示",
                "texture": "未显示"
            }
        },
        "tcm_diagnosis": {
            "syndrome_pattern": "未显示",
            "constitution_type": "未显示",
            "pathological_factors": "未显示",
            "organ_systems": "未显示"
        },
        "recommendations": {
            "dietary_therapy": "未显示",
            "lifestyle_adjustment": "未显示",
            "herbal_suggestions": "未显示",
            "follow_up": "复诊建议"
        },
        "severity": "未显示",
        "confidence": 0.85
    }
    
    return {
        "success": True,
        "message": "模拟中医舌诊分析完成",
        "data": mock_analysis
    }

def generate_mock_tcm_face_analysis():
    """生成模拟的中医面诊分析结果"""
    mock_analysis = {
        "image_type": "中医面诊",
        "facial_analysis": {
            "complexion": {
                "color_tone": "未显示",
                "luster": "未显示",
                "texture": "未显示",
                "moisture": "未显示"
            },
            "facial_features": {
                "eyes": "未显示",
                "nose": "未显示",
                "mouth": "未显示",
                "ears": "未显示"
            },
            "facial_regions": {
                "forehead": "未显示",
                "cheeks": "未显示",
                "chin": "未显示",
                "temples": "未显示"
            }
        },
        "tcm_diagnosis": {
            "syndrome_pattern": "未显示",
            "constitution_type": "未显示",
            "organ_function": "未显示",
            "qi_blood_status": "未显示"
        },
        "recommendations": {
            "dietary_therapy": "未显示",
            "lifestyle_adjustment": "未显示",
            "herbal_suggestions": "未显示",
            "acupoint_massage": "未显示"
        },
        "severity": "未显示",
        "confidence": 0.85
    }
    
    return {
        "success": True,
        "message": "模拟中医面诊分析完成",
        "data": mock_analysis
    }

def generate_mock_medical_analysis(image_type: str):
    """生成模拟的医学影像分析结果"""
    # mock_analyses = {
    #     "xray": {
    #         "image_quality": "良好",
    #         "main_findings": "双肺纹理清晰，心影大小正常，未见明显异常阴影",
    #         "recommendations": "定期复查，保持健康生活方式"
    #     },
    #     "ct": {
    #         "image_quality": "优秀",
    #         "main_findings": "肺实质密度均匀，纵隔结构正常，未见占位性病变",
    #         "recommendations": "影像表现正常，建议定期体检"
    #     },
    #     "ultrasound": {
    #         "image_quality": "良好",
    #         "main_findings": "肝脏大小形态正常，胆囊壁光滑，脾脏回声均匀",
    #         "recommendations": "超声检查未见异常，注意饮食健康"
    #     },
    #     "mri": {
    #         "image_quality": "优秀",
    #         "main_findings": "脑实质信号正常，脑室系统无扩张，未见异常强化灶",
    #         "recommendations": "MRI检查结果正常，继续观察"
    #     },
    #     "petct": {
    #         "image_quality": "优秀",
    #         "main_findings": "全身代谢活动正常，未见异常高代谢灶，淋巴结无肿大",
    #         "recommendations": "PET-CT检查未见异常，定期随访"
    #     }
    # }
    
    # mock_data = mock_analyses.get(image_type, {
    #     "image_quality": "良好",
    #     "main_findings": "影像分析完成",
    #     "recommendations": "请咨询专业医师获取详细解读"
    # })
    
    # analysis_data = {
    #     "image_type": image_type,
    #     "image_type_display": get_image_type_name(image_type),
    #     "analysis_result": mock_data,
    #     "confidence_score": 0.75,
    #     "analysis_timestamp": str(uuid.uuid4()),
    #     "disclaimer": "此为模拟分析结果，仅供参考，请以专业医师诊断为准。"
    # }
    analysis_data = {
        "image_type": image_type,
        "findings": {
            "primary_findings": "影像分析失败，请重试",
            "secondary_findings": "",
            "anatomical_structures": "",
            "abnormalities": ""
        },
        "diagnosis": {
            "primary_diagnosis": "请咨询专业医师",
            "differential_diagnosis": "",
            "diagnostic_confidence": "低",
            "additional_tests_needed": ""
        },
        "recommendations": {
            "immediate_actions": "请咨询专业医师",
            "follow_up": "",
            "lifestyle_modifications": "",
            "monitoring": ""
        },
        "severity": "未知",
        "confidence": 0.0
    }
    return {
        "success": True,
        "message": f"{get_image_type_name(image_type)}影像分析完成（模拟结果）",
        "data": analysis_data
    }


def get_image_type_name(image_type: str) -> str:
    """获取影像类型的中文名称"""
    type_names = {
        "xray": "X光",
        "ct": "CT",
        "ultrasound": "B超",
        "mri": "MRI",
        "petct": "PET-CT",
        "tongue": "中医舌诊",
        "face": "中医面诊"
    }
    return type_names.get(image_type, "未知类型")

# 获取处方列表
@router.get("/list", response_model=PaginatedResponse)
async def get_prescriptions(
    page: int = Query(1, ge=1, description="页码"),
    size: int = Query(10, ge=1, le=100, description="每页数量"),
    user_id: Optional[int] = Query(None, description="用户ID"),
    status: Optional[str] = Query(None, description="处方状态"),
    db: Session = Depends(get_db)
):
    """获取处方列表"""
    def db_operation():
        try:
            # 构建查询
            query = db.query(PrescriptionModel)
            
            if user_id:
                query = query.filter(PrescriptionModel.user_id == user_id)
            if status:
                query = query.filter(PrescriptionModel.status == status)
            
            # 计算总数
            total = query.count()
            
            # 分页查询
            prescriptions = query.offset((page - 1) * size).limit(size).all()
            
            return {
                "items": prescriptions,
                "total": total,
                "page": page,
                "size": size,
                "pages": (total + size - 1) // size
            }
        except OperationalError as e:
            logger.error(f"数据库连接错误: {e}")
            raise
        except SQLAlchemyError as e:
            logger.error(f"数据库查询错误: {e}")
            raise HTTPException(status_code=500, detail="数据库查询失败")
    
    try:
        return retry_db_operation(db_operation)
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"获取处方列表异常: {e}")
        raise HTTPException(status_code=500, detail=f"获取处方列表失败: {str(e)}")

# 创建新处方
@router.post("/create", response_model=Prescription)
async def create_prescription(prescription: PrescriptionCreate, db: Session = Depends(get_db)):
    """创建新处方"""
    def db_operation():
        try:
            db_prescription = PrescriptionModel(**prescription.dict())
            db.add(db_prescription)
            db.commit()
            db.refresh(db_prescription)
            return db_prescription
        except IntegrityError as e:
            db.rollback()
            logger.error(f"数据完整性错误: {e}")
            raise HTTPException(status_code=400, detail="数据完整性错误，请检查输入数据")
        except OperationalError as e:
            db.rollback()
            logger.error(f"数据库连接错误: {e}")
            raise
        except SQLAlchemyError as e:
            db.rollback()
            logger.error(f"数据库操作错误: {e}")
            raise HTTPException(status_code=500, detail="数据库操作失败")
    
    try:
        return retry_db_operation(db_operation)
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"创建处方异常: {e}")
        raise HTTPException(status_code=500, detail=f"创建处方失败: {str(e)}")

# 获取用户处方列表
@router.get("/user/{user_id}", response_model=List[Prescription])
async def get_user_prescriptions(user_id: int, db: Session = Depends(get_db)):
    """获取指定用户的处方列表"""
    def db_operation():
        try:
            prescriptions = db.query(PrescriptionModel).filter(
                PrescriptionModel.user_id == user_id
            ).order_by(PrescriptionModel.created_time.desc()).all()
            return prescriptions
        except OperationalError as e:
            logger.error(f"数据库连接错误: {e}")
            raise
        except SQLAlchemyError as e:
            logger.error(f"数据库查询错误: {e}")
            raise HTTPException(status_code=500, detail="数据库查询失败")
    
    try:
        return retry_db_operation(db_operation)
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"获取用户处方列表异常: {e}")
        raise HTTPException(status_code=500, detail=f"获取用户处方列表失败: {str(e)}")

# 以下是参数化路由，必须放在最后以避免路由冲突

# 获取单个处方
@router.get("/id/{prescription_id}", response_model=Prescription)
async def get_prescription(prescription_id: int, db: Session = Depends(get_db)):
    """获取单个处方"""
    def db_operation():
        try:
            prescription = db.query(PrescriptionModel).filter(
                PrescriptionModel.id == prescription_id
            ).first()
            if not prescription:
                raise HTTPException(status_code=404, detail="处方不存在")
            return prescription
        except HTTPException:
            raise
        except OperationalError as e:
            logger.error(f"数据库连接错误: {e}")
            raise
        except SQLAlchemyError as e:
            logger.error(f"数据库查询错误: {e}")
            raise HTTPException(status_code=500, detail="数据库查询失败")
    
    try:
        return retry_db_operation(db_operation)
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"获取处方异常: {e}")
        raise HTTPException(status_code=500, detail=f"获取处方失败: {str(e)}")

# 更新处方
@router.put("/id/{prescription_id}", response_model=Prescription)
async def update_prescription(
    prescription_id: int, 
    prescription_update: PrescriptionUpdate, 
    db: Session = Depends(get_db)
):
    """更新处方"""
    def db_operation():
        try:
            prescription = db.query(PrescriptionModel).filter(
                PrescriptionModel.id == prescription_id
            ).first()
            if not prescription:
                raise HTTPException(status_code=404, detail="处方不存在")
            
            for key, value in prescription_update.dict(exclude_unset=True).items():
                setattr(prescription, key, value)
            
            db.commit()
            db.refresh(prescription)
            return prescription
        except HTTPException:
            raise
        except IntegrityError as e:
            db.rollback()
            logger.error(f"数据完整性错误: {e}")
            raise HTTPException(status_code=400, detail="数据完整性错误，请检查输入数据")
        except OperationalError as e:
            db.rollback()
            logger.error(f"数据库连接错误: {e}")
            raise
        except SQLAlchemyError as e:
            db.rollback()
            logger.error(f"数据库操作错误: {e}")
            raise HTTPException(status_code=500, detail="数据库操作失败")
    
    try:
        return retry_db_operation(db_operation)
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"更新处方异常: {e}")
        raise HTTPException(status_code=500, detail=f"更新处方失败: {str(e)}")

# 删除处方
@router.delete("/id/{prescription_id}")
async def delete_prescription(prescription_id: int, db: Session = Depends(get_db)):
    """删除处方"""
    def db_operation():
        try:
            prescription = db.query(PrescriptionModel).filter(
                PrescriptionModel.id == prescription_id
            ).first()
            if not prescription:
                raise HTTPException(status_code=404, detail="处方不存在")
            
            db.delete(prescription)
            db.commit()
            return {"message": "处方删除成功"}
        except HTTPException:
            raise
        except OperationalError as e:
            db.rollback()
            logger.error(f"数据库连接错误: {e}")
            raise
        except SQLAlchemyError as e:
            db.rollback()
            logger.error(f"数据库操作错误: {e}")
            raise HTTPException(status_code=500, detail="数据库操作失败")
    
    try:
        return retry_db_operation(db_operation)
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"删除处方异常: {e}")
        raise HTTPException(status_code=500, detail=f"删除处方失败: {str(e)}")


