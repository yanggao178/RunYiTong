from openai import OpenAI
from typing import Dict, Any, Optional
import json
import os
import re
import logging
from dataclasses import dataclass
from dotenv import load_dotenv

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


def generate_tcm_prescription(
    symptoms: str,
    patient_info: Optional[Dict[str, Any]] = None,
    api_key: Optional[str] = None,
    model: str = "deepseek-chat",
    max_tokens: int = 1000,
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
            api_key = "sk-22f1f66085c94bdd9b246ddfd199cf1f"
    
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
            if attempt == max_retries - 1:  # 最后一次尝试
                raise ValueError(f"处方生成失败，已重试{max_retries}次: {str(e)}")
            else:
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

