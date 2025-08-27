from openai import OpenAI
from typing import Dict, Any, Optional
import json
import os
import re
import logging
from dataclasses import dataclass
from dotenv import load_dotenv
import base64
import requests

# 加载环境变量
load_dotenv()

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@dataclass
class TCMPrescription:
    syndrome_type: dict      # 详细辨证分型
    treatment_method: dict   # 详细治法
    main_prescription: dict  # 详细主方信息
    composition: list        # 组成药材及剂量
    usage: dict              # 详细用法
    contraindications: dict  # 详细禁忌


@dataclass
class MedicalImageAnalysis:
    image_type: str          # 影像类型 (X-ray, CT, MRI, Ultrasound, PET-CT)
    findings: dict           # 影像发现
    diagnosis: dict          # 诊断结果
    recommendations: dict    # 建议
    severity: str            # 严重程度
    confidence: float        # 置信度


def _clean_json_content(content: str) -> str:
    """清理和修复JSON内容
    
    Args:
        content: 原始JSON字符串
        
    Returns:
        str: 清理后的JSON字符串
    """
    cleaned_content = content.strip()
    
    # 移除markdown代码块标记
    if cleaned_content.startswith('```json'):
        cleaned_content = cleaned_content[7:]
    elif cleaned_content.startswith('```'):
        cleaned_content = cleaned_content[3:]
    if cleaned_content.endswith('```'):
        cleaned_content = cleaned_content[:-3]
    
    cleaned_content = cleaned_content.strip()
    
    # 修复字符串中的换行符和特殊字符
    cleaned_content = re.sub(r'"([^"]*?)\n([^"]*?)"', r'"\1 \2"', cleaned_content, flags=re.DOTALL)
    cleaned_content = re.sub(r'"([^"]*?)\r([^"]*?)"', r'"\1 \2"', cleaned_content, flags=re.DOTALL)
    cleaned_content = re.sub(r'"([^"]*?)\t([^"]*?)"', r'"\1 \2"', cleaned_content, flags=re.DOTALL)
    
    # 修复常见格式问题
    cleaned_content = re.sub(r',\s*,', ',', cleaned_content)  # 移除重复逗号
    cleaned_content = re.sub(r',\s*}', '}', cleaned_content)   # 移除对象末尾多余逗号
    cleaned_content = re.sub(r',\s*]', ']', cleaned_content)   # 移除数组末尾多余逗号
    
    # 确保JSON结构完整
    open_braces = cleaned_content.count('{')
    close_braces = cleaned_content.count('}')
    if open_braces > close_braces:
        cleaned_content += '}' * (open_braces - close_braces)
    
    return cleaned_content


def _get_default_prescription() -> Dict[str, Any]:
    """获取默认的处方结构
    
    Returns:
        Dict[str, Any]: 默认处方数据
    """
    return {
        "syndrome_type": {
            "main_syndrome": "解析失败，请重试", 
            "secondary_syndrome": "", 
            "disease_location": "", 
            "disease_nature": "", 
            "pathogenesis": ""
        },
        "treatment_method": {
            "main_method": "请重新分析", 
            "auxiliary_method": "", 
            "treatment_priority": "", 
            "care_principle": ""
        },
        "main_prescription": {
            "formula_name": "暂无", 
            "formula_source": "", 
            "formula_analysis": "", 
            "modifications": ""
        },
        "composition": [],
        "usage": {
            "preparation_method": "请咨询医师", 
            "administration_time": "", 
            "treatment_course": ""
        },
        "contraindications": {
            "contraindications": "请咨询医师", 
            "dietary_restrictions": "", 
            "lifestyle_care": "", 
            "precautions": ""
        }
    }


def _get_default_image_analysis(image_type: str) -> Dict[str, Any]:
    """获取默认的医学影像分析结构
    
    Args:
        image_type: 影像类型
        
    Returns:
        Dict[str, Any]: 默认影像分析数据
    """
    return {
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


def _validate_input(symptoms: str, patient_info: Optional[Dict[str, Any]]) -> None:
    """验证输入参数
    
    Args:
        symptoms: 症状描述
        patient_info: 患者信息
        
    Raises:
        ValueError: 当输入无效时
    """
    if not symptoms or not symptoms.strip():
        raise ValueError("症状描述不能为空")
    
    if len(symptoms.strip()) < 2:
        raise ValueError("症状描述过短，请提供更详细的信息")
    
    if patient_info is not None and not isinstance(patient_info, dict):
        raise ValueError("患者信息必须是字典格式")


def _validate_image_input(image_data: str, image_type: str, patient_info: Optional[Dict[str, Any]]) -> None:
    """验证医学影像分析输入参数
    
    Args:
        image_data: Base64编码的图像数据
        image_type: 影像类型
        patient_info: 患者信息
        
    Raises:
        ValueError: 当输入无效时
    """
    if not image_data or not image_data.strip():
        raise ValueError("图像数据不能为空")
    
    valid_image_types = ['X-ray', 'CT', 'MRI', 'Ultrasound', 'PET-CT']
    if image_type not in valid_image_types:
        raise ValueError(f"不支持的影像类型: {image_type}，支持的类型: {', '.join(valid_image_types)}")
    
    if patient_info is not None and not isinstance(patient_info, dict):
        raise ValueError("患者信息必须是字典格式")
    
    # 验证Base64格式
    try:
        base64.b64decode(image_data)
    except Exception:
        raise ValueError("图像数据格式无效，必须是有效的Base64编码")


def _build_optimized_prompt(patient_context: str, symptoms: str) -> str:
    """构建优化的AI提示词（简化版，提高响应速度）
    
    Args:
        patient_context: 患者上下文信息
        symptoms: 症状描述
        
    Returns:
        str: 优化后的提示词
    """
    return f"""# 中医症状分析

## 患者信息
{patient_context}
**症状**: {symptoms}

## 任务要求
请快速分析并返回JSON格式结果，包含：
1. 辨证分型（主证、病位、病性、病机）
2. 治疗方法（主要治法、辅助治法）
3. 方剂选择（方名、来源、分析）
4. 药物组成（药材、剂量、角色）
5. 用法用量（煎服方法、服用时间）
6. 注意事项（禁忌、饮食、调护）

## 输出格式
严格按照以下JSON结构返回：

{{
    "syndrome_type": {{
        "main_syndrome": "主证型名称",
        "secondary_syndrome": "兼证或无",
        "disease_location": "病位脏腑",
        "disease_nature": "寒热虚实性质",
        "pathogenesis": "病机分析"
    }},
    "treatment_method": {{
        "main_method": "主要治法",
        "auxiliary_method": "辅助治法",
        "treatment_priority": "标本缓急",
        "care_principle": "调护要点"
    }},
    "main_prescription": {{
        "formula_name": "方剂名称",
        "formula_source": "出处典籍",
        "formula_analysis": "方义解析",
        "modifications": "加减说明"
    }},
    "composition": [
        {{
            "herb": "药材名称",
            "dosage": "用量(g)",
            "role": "君臣佐使",
            "function": "主要功效",
            "preparation": "炮制方法"
        }}
    ],
    "usage": {{
        "preparation_method": "煎煮方法",
        "administration_time": "服用时间",
        "treatment_course": "疗程建议"
    }},
    "contraindications": {{
        "contraindications": "禁忌人群",
        "dietary_restrictions": "饮食禁忌",
        "lifestyle_care": "起居调护",
        "precautions": "注意事项"
    }}
}}

## 质量标准
1. **专业性**: 使用准确的中医术语和理论
2. **安全性**: 确保用药安全，标注禁忌
3. **实用性**: 提供具体可操作的指导
4. **完整性**: 各字段内容完整，JSON格式正确
5. **个性化**: 针对具体症状进行分析

请基于以上要求，为患者提供专业的中医诊疗方案。"""


def _build_image_analysis_prompt(patient_context: str, image_type: str) -> str:
    """构建医学影像分析的AI提示词
    
    Args:
        patient_context: 患者上下文信息
        image_type: 影像类型
        
    Returns:
        str: 医学影像分析提示词
    """
    return f"""# 医学影像分析

## 患者信息
{patient_context}
**影像类型**: {image_type}

## 任务要求
请作为专业的医学影像诊断专家，分析提供的{image_type}影像，并返回JSON格式的详细分析结果。

## 分析要点
1. **影像发现**: 详细描述可见的解剖结构和异常发现
2. **诊断评估**: 基于影像特征提供可能的诊断
3. **严重程度**: 评估病变的严重程度
4. **建议措施**: 提供后续检查和治疗建议
5. **置信度**: 评估诊断的可靠性

## 输出格式
严格按照以下JSON结构返回：

{{
    "image_type": "{image_type}",
    "findings": {{
        "primary_findings": "主要影像发现",
        "secondary_findings": "次要发现或无",
        "anatomical_structures": "可见解剖结构描述",
        "abnormalities": "异常表现详细描述"
    }},
    "diagnosis": {{
        "primary_diagnosis": "主要诊断考虑",
        "differential_diagnosis": "鉴别诊断",
        "diagnostic_confidence": "高/中/低",
        "additional_tests_needed": "建议的进一步检查"
    }},
    "recommendations": {{
        "immediate_actions": "即时处理建议",
        "follow_up": "随访建议",
        "lifestyle_modifications": "生活方式调整",
        "monitoring": "监测要点"
    }},
    "severity": "轻度/中度/重度/危重",
    "confidence": 0.85
}}

## 专业要求
1. **准确性**: 基于影像特征进行客观分析
2. **安全性**: 对不确定的发现保持谨慎态度
3. **完整性**: 提供全面的分析和建议
4. **规范性**: 使用标准医学术语
5. **实用性**: 提供可操作的临床建议

请基于提供的{image_type}影像进行专业分析。"""


def generate_tcm_prescription(
    symptoms: str,
    patient_info: Optional[Dict[str, Any]] = None,
    api_key: Optional[str] = None,
    model: str = "deepseek-chat",
    max_tokens: int = 10000,
    max_retries: int = 3
) -> TCMPrescription:
    """
    根据症状生成中医处方
    
    Args:
        symptoms (str): 患者症状描述
        patient_info (dict, optional): 患者基本信息，包含年龄、性别等
        api_key (str, optional): OpenAI API密钥，如果未提供则从环境变量读取
        model: 使用的大模型版本
        max_tokens: 最大输出长度
        max_retries: 最大重试次数，默认为3
    
    Returns:
        TCMPrescription: 包含完整中医处方信息的对象
    
    Raises:
        ValueError: 当输入参数无效时
        Exception: 当API调用失败或数据解析错误时
    """
    # 输入验证
    _validate_input(symptoms, patient_info)
    
    # 获取API密钥
    if api_key is None:
        api_key = os.getenv('OPENAI_API_KEY')
        if not api_key:
            # 如果环境变量中没有，使用默认密钥
            api_key = "sk-68c5c58759294023b55914d2996a8d6b"
    
    logger.info(f"开始生成中医处方，症状：{symptoms[:50]}...")
    
    # 重试机制
    for attempt in range(max_retries):
        try:
            logger.info(f"第{attempt + 1}次尝试生成处方")
            
            # 构建患者上下文
            patient_context = ""
            if patient_info:
                patient_context = (
                    f"患者信息: 年龄{patient_info.get('age', '未知')}岁，"
                    f"性别{patient_info.get('gender', '未知')}，"
                    f"过敏史: {','.join(patient_info.get('allergies', []))}"
                )
            
            # 构建优化的prompt
            prompt = _build_optimized_prompt(patient_context, symptoms)
            
            # 调用AI API
            client = OpenAI(
                api_key=api_key,
                base_url="https://api.deepseek.com/v1"
            )
            
            response = client.chat.completions.create(
                model=model,
                messages=[
                    {"role": "system", "content": "你是一名资深的中医专家，精通中医理论和临床实践。请简洁准确地回答。"},
                    {"role": "user", "content": prompt}
                ],
                temperature=0.1,  # 降低随机性，提高响应速度
                max_tokens=min(max_tokens, 800),  # 限制最大token数，提高响应速度
                response_format={"type": "json_object"},
                timeout=60  # 设置60秒超时
            )
            
            # 获取AI响应内容
            ai_content = response.choices[0].message.content
            logger.info(f"AI响应长度: {len(ai_content)} 字符")
            
            # 尝试解析JSON
            try:
                result = json.loads(ai_content)
                logger.info("JSON解析成功")
            except json.JSONDecodeError as json_error:
                logger.warning(f"JSON解析失败: {json_error}，尝试修复")
                
                # 使用清理函数修复JSON
                try:
                    cleaned_content = _clean_json_content(ai_content)
                    result = json.loads(cleaned_content)
                    logger.info("JSON修复成功")
                except Exception as repair_error:
                    logger.error(f"JSON修复失败: {repair_error}")
                    if attempt == max_retries - 1:  # 最后一次尝试
                        result = _get_default_prescription()
                    else:
                        continue  # 重试
            
            # 构建TCMPrescription对象
            try:
                prescription = TCMPrescription(
                    syndrome_type=result.get("syndrome_type", {}),
                    treatment_method=result.get("treatment_method", {}),
                    main_prescription=result.get("main_prescription", {}),
                    composition=result.get("composition", []),
                    usage=result.get("usage", {}),
                    contraindications=result.get("contraindications", {})
                )
                
                logger.info("处方生成成功")
                return prescription
                
            except Exception as e:
                logger.error(f"数据转换错误: {e}")
                if attempt == max_retries - 1:  # 最后一次尝试
                    default_data = _get_default_prescription()
                    return TCMPrescription(
                        syndrome_type=default_data["syndrome_type"],
                        treatment_method=default_data["treatment_method"],
                        main_prescription=default_data["main_prescription"],
                        composition=default_data["composition"],
                        usage=default_data["usage"],
                        contraindications=default_data["contraindications"]
                    )
                else:
                    continue  # 重试
                    
        except Exception as e:
            logger.error(f"第{attempt + 1}次尝试失败: {e}")
            
            # 根据异常类型提供更具体的错误信息
            error_type = type(e).__name__
            error_msg = str(e).lower()
            
            if "timeout" in error_msg or "timed out" in error_msg:
                specific_error = f"网络请求超时: {str(e)}"
            elif "connection" in error_msg or "network" in error_msg:
                specific_error = f"网络连接失败: {str(e)}"
            elif "api" in error_msg or "unauthorized" in error_msg or "401" in error_msg:
                specific_error = f"API认证失败: {str(e)}"
            elif "rate limit" in error_msg or "429" in error_msg:
                specific_error = f"API调用频率限制: {str(e)}"
            elif "500" in error_msg or "502" in error_msg or "503" in error_msg:
                specific_error = f"服务器错误: {str(e)}"
            else:
                specific_error = f"未知错误: {str(e)}"
            
            if attempt == max_retries - 1:  # 最后一次尝试
                raise ValueError(f"处方生成失败，已重试{max_retries}次。{specific_error}")
            else:
                logger.warning(f"第{attempt + 1}次尝试失败，将重试: {specific_error}")
                continue  # 重试
    
    # 如果所有重试都失败，返回默认处方
    logger.warning("所有重试都失败，返回默认处方")
    default_data = _get_default_prescription()
    return TCMPrescription(
        syndrome_type=default_data["syndrome_type"],
        treatment_method=default_data["treatment_method"],
        main_prescription=default_data["main_prescription"],
        composition=default_data["composition"],
        usage=default_data["usage"],
        contraindications=default_data["contraindications"]
    )


def analyze_medical_image(
    image_data: str,
    image_type: str,
    patient_info: Optional[Dict[str, Any]] = None,
    api_key: Optional[str] = None,
    model: str = "gpt-4-vision-preview",
    max_tokens: int = 4000,
    max_retries: int = 3
) -> MedicalImageAnalysis:
    """
    分析医学影像并生成诊断报告
    
    Args:
        image_data (str): Base64编码的图像数据
        image_type (str): 影像类型 (X-ray, CT, MRI, Ultrasound, PET-CT)
        patient_info (dict, optional): 患者基本信息
        api_key (str, optional): OpenAI API密钥
        model (str): 使用的视觉模型
        max_tokens (int): 最大输出长度
        max_retries (int): 最大重试次数
    
    Returns:
        MedicalImageAnalysis: 包含完整影像分析信息的对象
    
    Raises:
        ValueError: 当输入参数无效时
        Exception: 当API调用失败或数据解析错误时
    """
    # 输入验证
    _validate_image_input(image_data, image_type, patient_info)
    
    # 获取API密钥
    if api_key is None:
        api_key = os.getenv('OPENAI_API_KEY')
        if not api_key:
            # 如果环境变量中没有，使用默认密钥
            api_key = "sk-68c5c58759294023b55914d2996a8d6b"
    
    logger.info(f"开始分析{image_type}医学影像...")
    
    # 重试机制
    for attempt in range(max_retries):
        try:
            logger.info(f"第{attempt + 1}次尝试分析影像")
            
            # 构建患者上下文
            patient_context = ""
            if patient_info:
                patient_context = (
                    f"患者信息: 年龄{patient_info.get('age', '未知')}岁，"
                    f"性别{patient_info.get('gender', '未知')}，"
                    f"病史: {patient_info.get('medical_history', '无')}"
                )
            
            # 构建分析提示词
            prompt = _build_image_analysis_prompt(patient_context, image_type)
            
            # 调用AI API
            client = OpenAI(
                api_key=api_key,
                base_url="https://api.openai.com/v1"  # 使用OpenAI的视觉API
            )
            
            response = client.chat.completions.create(
                model=model,
                messages=[
                    {
                        "role": "system", 
                        "content": "你是一名资深的医学影像诊断专家，精通各种医学影像的解读和诊断。请客观、准确地分析影像。"
                    },
                    {
                        "role": "user",
                        "content": [
                            {"type": "text", "text": prompt},
                            {
                                "type": "image_url",
                                "image_url": {
                                    "url": f"data:image/jpeg;base64,{image_data}"
                                }
                            }
                        ]
                    }
                ],
                temperature=0.1,
                max_tokens=max_tokens,
                timeout=120  # 影像分析需要更长时间
            )
            
            # 获取AI响应内容
            ai_content = response.choices[0].message.content
            logger.info(f"AI响应长度: {len(ai_content)} 字符")
            
            # 尝试解析JSON
            try:
                result = json.loads(ai_content)
                logger.info("JSON解析成功")
            except json.JSONDecodeError as json_error:
                logger.warning(f"JSON解析失败: {json_error}，尝试修复")
                
                # 使用清理函数修复JSON
                try:
                    cleaned_content = _clean_json_content(ai_content)
                    result = json.loads(cleaned_content)
                    logger.info("JSON修复成功")
                except Exception as repair_error:
                    logger.error(f"JSON修复失败: {repair_error}")
                    if attempt == max_retries - 1:  # 最后一次尝试
                        result = _get_default_image_analysis(image_type)
                    else:
                        continue  # 重试
            
            # 构建MedicalImageAnalysis对象
            try:
                analysis = MedicalImageAnalysis(
                    image_type=result.get("image_type", image_type),
                    findings=result.get("findings", {}),
                    diagnosis=result.get("diagnosis", {}),
                    recommendations=result.get("recommendations", {}),
                    severity=result.get("severity", "未知"),
                    confidence=float(result.get("confidence", 0.0))
                )
                
                logger.info("影像分析成功")
                return analysis
                
            except Exception as e:
                logger.error(f"数据转换错误: {e}")
                if attempt == max_retries - 1:  # 最后一次尝试
                    default_data = _get_default_image_analysis(image_type)
                    return MedicalImageAnalysis(
                        image_type=default_data["image_type"],
                        findings=default_data["findings"],
                        diagnosis=default_data["diagnosis"],
                        recommendations=default_data["recommendations"],
                        severity=default_data["severity"],
                        confidence=default_data["confidence"]
                    )
                else:
                    continue  # 重试
                    
        except Exception as e:
            logger.error(f"第{attempt + 1}次尝试失败: {e}")
            
            # 根据异常类型提供更具体的错误信息
            error_type = type(e).__name__
            error_msg = str(e).lower()
            
            if "timeout" in error_msg or "timed out" in error_msg:
                specific_error = f"网络请求超时: {str(e)}"
            elif "connection" in error_msg or "network" in error_msg:
                specific_error = f"网络连接失败: {str(e)}"
            elif "api" in error_msg or "unauthorized" in error_msg or "401" in error_msg:
                specific_error = f"API认证失败: {str(e)}"
            elif "rate limit" in error_msg or "429" in error_msg:
                specific_error = f"API调用频率限制: {str(e)}"
            elif "500" in error_msg or "502" in error_msg or "503" in error_msg:
                specific_error = f"服务器错误: {str(e)}"
            else:
                specific_error = f"未知错误: {str(e)}"
            
            if attempt == max_retries - 1:  # 最后一次尝试
                raise ValueError(f"影像分析失败，已重试{max_retries}次。{specific_error}")
            else:
                logger.warning(f"第{attempt + 1}次尝试失败，将重试: {specific_error}")
                continue  # 重试
    
    # 如果所有重试都失败，返回默认分析结果
    logger.warning("所有重试都失败，返回默认分析结果")
    default_data = _get_default_image_analysis(image_type)
    return MedicalImageAnalysis(
        image_type=default_data["image_type"],
        findings=default_data["findings"],
        diagnosis=default_data["diagnosis"],
        recommendations=default_data["recommendations"],
        severity=default_data["severity"],
        confidence=default_data["confidence"]
    )


def analyze_medical_image_simple(
    image_data: str,
    image_type: str,
    patient_info: Optional[Dict[str, Any]] = None
) -> Dict[str, Any]:
    """
    简化版医学影像分析函数，返回字典格式结果
    
    Args:
        image_data (str): Base64编码的图像数据
        image_type (str): 影像类型
        patient_info (dict, optional): 患者基本信息
    
    Returns:
        Dict[str, Any]: 分析结果字典
    """
    try:
        analysis = analyze_medical_image(image_data, image_type, patient_info)
        return {
            "success": True,
            "image_type": analysis.image_type,
            "findings": analysis.findings,
            "diagnosis": analysis.diagnosis,
            "recommendations": analysis.recommendations,
            "severity": analysis.severity,
            "confidence": analysis.confidence,
            "analysis_result": format_image_analysis_result(analysis)
        }
    except Exception as e:
        logger.error(f"简化影像分析失败: {e}")
        default_data = _get_default_image_analysis(image_type)
        return {
            "success": False,
            "error": str(e),
            "image_type": image_type,
            "findings": default_data["findings"],
            "diagnosis": default_data["diagnosis"],
            "recommendations": default_data["recommendations"],
            "severity": default_data["severity"],
            "confidence": default_data["confidence"],
            "analysis_result": "影像分析失败，请重试或咨询专业医师"
        }


def format_image_analysis_result(analysis: MedicalImageAnalysis) -> str:
    """
    格式化医学影像分析结果为可读文本
    
    Args:
        analysis: 医学影像分析对象
    
    Returns:
        str: 格式化的分析结果文本
    """
    try:
        result_text = f"**{analysis.image_type}影像分析报告**\n\n"
        
        # 主要发现
        if analysis.findings.get("primary_findings"):
            result_text += f"**主要发现**: {analysis.findings['primary_findings']}\n"
        
        # 诊断结果
        if analysis.diagnosis.get("primary_diagnosis"):
            result_text += f"**诊断考虑**: {analysis.diagnosis['primary_diagnosis']}\n"
        
        # 严重程度
        if analysis.severity and analysis.severity != "未知":
            result_text += f"**严重程度**: {analysis.severity}\n"
        
        # 建议
        if analysis.recommendations.get("immediate_actions"):
            result_text += f"**处理建议**: {analysis.recommendations['immediate_actions']}\n"
        
        # 置信度
        if analysis.confidence > 0:
            confidence_percent = int(analysis.confidence * 100)
            result_text += f"**分析置信度**: {confidence_percent}%\n"
        
        result_text += "\n*注意：此分析仅供参考，请咨询专业医师获取准确诊断*"
        
        return result_text
        
    except Exception as e:
        logger.error(f"格式化分析结果失败: {e}")
        return f"{analysis.image_type}影像分析完成，请查看详细结果或咨询专业医师"


def analyze_medical_image_dashscope(
    image_path: str,
    image_type: str,
    patient_info: Optional[Dict[str, Any]] = None,
    api_key: Optional[str] = None,
    model: str = "qwen-vl-plus",
    max_tokens: int = 4000,
    max_retries: int = 3
) -> MedicalImageAnalysis:
    """
    使用阿里云灵积（DashScope）API分析医学影像并生成诊断报告
    
    Args:
        image_path (str): 图像文件路径
        image_type (str): 影像类型 (X-ray, CT, MRI, Ultrasound, PET-CT)
        patient_info (dict, optional): 患者基本信息
        api_key (str, optional): DashScope API密钥
        model (str): 使用的视觉模型 (qwen-vl-plus, qwen-vl-max)
        max_tokens (int): 最大输出长度
        max_retries (int): 最大重试次数
    
    Returns:
        MedicalImageAnalysis: 包含完整影像分析信息的对象
    
    Raises:
        ValueError: 当输入参数无效时
        Exception: 当API调用失败或数据解析错误时
    """
    # 验证图像文件路径
    if not image_path or not isinstance(image_path, str):
        raise ValueError("图像路径不能为空")
    
    if not os.path.exists(image_path):
        raise ValueError(f"图像文件不存在: {image_path}")
    
    # 读取图像文件并转换为Base64
    try:
        with open(image_path, "rb") as image_file:
            image_data = base64.b64encode(image_file.read()).decode('utf-8')
        logger.info(f"成功读取图像文件: {image_path}")
    except Exception as e:
        raise ValueError(f"读取图像文件失败: {str(e)}")
    
    # 输入验证
    _validate_image_input(image_data, image_type, patient_info)
    
    # 获取API密钥
    if api_key is None:
        api_key = os.getenv('DASHSCOPE_API_KEY')
        if not api_key:
            raise ValueError("请设置DASHSCOPE_API_KEY环境变量或提供api_key参数")
    
    logger.info(f"开始使用DashScope分析{image_type}医学影像...")
    
    # # DashScope API配置
    # url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation"
    # headers = {
    #     "Authorization": f"Bearer {api_key}",
    #     "Content-Type": "application/json"
    # }
    
    # 重试机制
    for attempt in range(max_retries):
        try:
            logger.info(f"第{attempt + 1}次尝试分析影像")
            
            # 构建患者上下文
            patient_context = ""
            if patient_info:
                patient_context = (
                    f"患者信息: 年龄{patient_info.get('age', '未知')}岁，"
                    f"性别{patient_info.get('gender', '未知')}，"
                    f"病史: {patient_info.get('medical_history', '无')}"
                )
            
            # 构建分析提示词
            prompt = _build_image_analysis_prompt(patient_context, image_type)
            
            # 构建DashScope请求数据
            # data = {
            #     "model": model,
            #     "input": {
            #         "messages": [
            #             {
            #                 "role": "system",
            #                 "content": "你是一名资深的医学影像诊断专家，精通各种医学影像的解读和诊断。请客观、准确地分析影像，并以JSON格式返回结果。"
            #             },
            #             {
            #                 "role": "user",
            #                 "content": [
            #                     {
            #                         "text": prompt
            #                     },
            #                     {
            #                         "image": f"data:image/jpg;base64,{image_data}"
            #                     }
            #                 ]
            #             }
            #         ]
            #     },
            #     "parameters": {
            #         "max_tokens": max_tokens,
            #         "temperature": 0.1,
            #         "top_p": 0.8
            #     }
            # }
            
            # # 调用DashScope API
            # response = requests.post(url, headers=headers, json=data, timeout=120)
            
            # if response.status_code != 200:
            #     error_msg = f"DashScope API调用失败: {response.status_code} - {response.text}"
            #     logger.error(error_msg)
            #     if attempt == max_retries - 1:
            #         raise Exception(error_msg)
            #     continue
            
            # result_data = response.json()
            
            # # 检查API响应状态
            # if result_data.get("code") and result_data["code"] != "Success":
            #     error_msg = f"DashScope API返回错误: {result_data.get('code')} - {result_data.get('message', '未知错误')}"
            #     logger.error(error_msg)
            #     if attempt == max_retries - 1:
            #         raise Exception(error_msg)
            #     continue

            client = OpenAI(
                api_key=api_key,
                base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
            )

            completion = client.chat.completions.create(
                model=model,
                # 此处以qwen-vl-max-latest为例，可按需更换模型名称。模型列表：https://help.aliyun.com/zh/model-studio/models
                messages=[
                    {
                        "role": "system",
                        "content": [{"type": "text", "text": "你是一名资深的医学影像诊断专家，精通各种医学影像的解读和诊断。请客观、准确地分析影像，并以JSON格式返回结果。"}],
                    },
                    {
                        "role": "user",
                        "content": [
                            {
                                "type": "image_url",
                                "image_url": {
                                    "url": f"../routers/{image_path}"
                                },
                            },
                            {"type": "text", "text":prompt},
                        ],
                    },
                ],
                temperature=0.1,  # 降低随机性，提高响应速度
                max_tokens=max_tokens,  # 限制最大token数，提高响应速度
                response_format={"type": "json_object"},
                timeout=60  # 设置60秒超时
            )
            print(completion.choices[0].message.content)
            # 获取AI响应内容
            ai_content = completion.choices[0].message.content
            logger.info(f"DashScope AI响应长度: {len(ai_content)} 字符")
            
            # 尝试解析JSON
            try:
                result = json.loads(ai_content)
                logger.info("JSON解析成功")
            except json.JSONDecodeError as json_error:
                logger.warning(f"JSON解析失败: {json_error}，尝试修复")
                
                # 使用清理函数修复JSON
                try:
                    cleaned_content = _clean_json_content(ai_content)
                    result = json.loads(cleaned_content)
                    logger.info("JSON修复成功")
                except Exception as repair_error:
                    logger.error(f"JSON修复失败: {repair_error}")
                    if attempt == max_retries - 1:  # 最后一次尝试
                        result = _get_default_image_analysis(image_type)
                    else:
                        continue  # 重试
            
            # 构建MedicalImageAnalysis对象
            try:
                analysis = MedicalImageAnalysis(
                    image_type=result.get("image_type", image_type),
                    findings=result.get("findings", {}),
                    diagnosis=result.get("diagnosis", {}),
                    recommendations=result.get("recommendations", {}),
                    severity=result.get("severity", "未知"),
                    confidence=float(result.get("confidence", 0.0))
                )
                
                logger.info("DashScope影像分析成功")
                return analysis
                
            except Exception as e:
                logger.error(f"数据转换错误: {e}")
                if attempt == max_retries - 1:  # 最后一次尝试
                    default_data = _get_default_image_analysis(image_type)
                    return MedicalImageAnalysis(
                        image_type=default_data["image_type"],
                        findings=default_data["findings"],
                        diagnosis=default_data["diagnosis"],
                        recommendations=default_data["recommendations"],
                        severity=default_data["severity"],
                        confidence=default_data["confidence"]
                    )
                else:
                    continue  # 重试
                    
        except Exception as e:
            logger.error(f"第{attempt + 1}次尝试失败: {e}")
            
            # 根据异常类型提供更具体的错误信息
            error_type = type(e).__name__
            error_msg = str(e).lower()
            
            if "timeout" in error_msg or "timed out" in error_msg:
                specific_error = f"网络请求超时: {str(e)}"
            elif "connection" in error_msg or "network" in error_msg:
                specific_error = f"网络连接失败: {str(e)}"
            elif "api" in error_msg or "unauthorized" in error_msg or "401" in error_msg:
                specific_error = f"API认证失败: {str(e)}"
            elif "rate limit" in error_msg or "429" in error_msg:
                specific_error = f"API调用频率限制: {str(e)}"
            elif "500" in error_msg or "502" in error_msg or "503" in error_msg:
                specific_error = f"服务器错误: {str(e)}"
            else:
                specific_error = f"未知错误: {str(e)}"
            
            if attempt == max_retries - 1:  # 最后一次尝试
                raise ValueError(f"DashScope影像分析失败，已重试{max_retries}次。{specific_error}")
            else:
                logger.warning(f"第{attempt + 1}次尝试失败，将重试: {specific_error}")
                continue  # 重试
    
    # 如果所有重试都失败，返回默认分析结果
    logger.warning("所有重试都失败，返回默认分析结果")
    default_data = _get_default_image_analysis(image_type)
    return MedicalImageAnalysis(
        image_type=default_data["image_type"],
        findings=default_data["findings"],
        diagnosis=default_data["diagnosis"],
        recommendations=default_data["recommendations"],
        severity=default_data["severity"],
        confidence=default_data["confidence"]
    )


def analyze_medical_image_dashscope_simple(
    image_path: str,
    image_type: str,
    patient_info: Optional[Dict[str, Any]] = None
) -> Dict[str, Any]:
    """
    使用DashScope API分析医学影像的简化版本
    
    Args:
        image_path (str): 图像文件路径
        image_type (str): 影像类型
        patient_info (dict, optional): 患者基本信息
    
    Returns:
        Dict[str, Any]: 包含分析结果的字典
    """
    try:
        analysis = analyze_medical_image_dashscope(image_path, image_type, patient_info)
        
        return {
            "success": True,
            "image_type": analysis.image_type,
            "findings": analysis.findings,
            "diagnosis": analysis.diagnosis,
            "recommendations": analysis.recommendations,
            "severity": analysis.severity,
            "confidence": analysis.confidence,
            "formatted_result": format_image_analysis_result(analysis)
        }
        
    except Exception as e:
        logger.error(f"DashScope简化分析失败: {e}")
        
        # 返回默认结果
        default_data = _get_default_image_analysis(image_type)
        default_analysis = MedicalImageAnalysis(
            image_type=default_data["image_type"],
            findings=default_data["findings"],
            diagnosis=default_data["diagnosis"],
            recommendations=default_data["recommendations"],
            severity=default_data["severity"],
            confidence=default_data["confidence"]
        )
        
        return {
            "success": False,
            "error": str(e),
            "image_type": default_analysis.image_type,
            "findings": default_analysis.findings,
            "diagnosis": default_analysis.diagnosis,
            "recommendations": default_analysis.recommendations,
            "severity": default_analysis.severity,
            "confidence": default_analysis.confidence,
            "formatted_result": format_image_analysis_result(default_analysis)
        }

