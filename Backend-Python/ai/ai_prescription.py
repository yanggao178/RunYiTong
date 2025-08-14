from openai import OpenAI
from typing import Dict, Any
import json
from dataclasses import dataclass

@dataclass
class TCMPrescription:
    syndrome_type: str       # 辨证分型
    treatment_method: str    # 治法
    main_prescription: str   # 主方名称
    composition: list        # 组成药材及剂量
    usage: str               # 煎服法
    contraindications: str   # 禁忌

def generate_tcm_prescription(
    symptoms: str,
    api_key: str = "sk-22f1f66085c94bdd9b246ddfd199cf1f",
    patient_info: Dict[str, Any] = None,
    model: str = "deepseek-chat",
    max_tokens: int = 1000
) -> TCMPrescription:
    """
    基于大模型生成中药处方

    Args:
        symptoms: 患者症状描述
        api_key: DeepSeek API密钥
        patient_info: 患者信息字典，包含年龄、性别等
        model: 使用的大模型版本
        max_tokens: 最大输出长度

    Returns:
        TCMPrescription: 结构化处方数据

    Raises:
        ValueError: 当API调用失败时
    """
    # 构建患者上下文
    patient_context = ""
    if patient_info:
        patient_context = (
            f"患者信息: 年龄{patient_info.get('age', '未知')}岁，"
            f"性别{patient_info.get('gender', '未知')}，"
            f"过敏史: {','.join(patient_info.get('allergies', []))}\n"
        )

    prompt = f"""你是一名有30年临床经验的中医主任医师，请根据以下信息开具中药处方：

{patient_context}
主要症状: {symptoms}

请严格按照以下步骤思考：
1. 中医辨证分型（如"风寒表证"）
2. 确定治法（如"辛温解表"）
3. 选择主方（经典方剂名称）
4. 详细组成（药材及精确剂量）
5. 煎服方法
6. 特别注意事项和禁忌

要求：
- 药材剂量需符合《中国药典》标准
- 标注君臣佐使关系
- 如有加减需说明原因
- 妊娠禁忌药材需特别标注

请返回严格遵循以下JSON格式的内容：
{{
    "辨证": "",
    "治法": "",
    "主方": "",
    "组成": [
        {{"药材": "", "剂量": "", "角色": ""}}
    ],
    "用法": "",
    "禁忌": ""
}}"""

    try:
        client = OpenAI(
            api_key=api_key,
            base_url="https://api.deepseek.com/v1"
        )
        response = client.chat.completions.create(
            model=model,
            messages=[
                {"role": "system", "content": "你是一名资深中医专家"},
                {"role": "user", "content": prompt}
            ],
            temperature=0.3,
            max_tokens=max_tokens,
            response_format={"type": "json_object"}
        )

        result = json.loads(response.choices[0].message.content)
        return TCMPrescription(
            syndrome_type=result["辨证"],
            treatment_method=result["治法"],
            main_prescription=result["主方"],
            composition=result["组成"],
            usage=result["用法"],
            contraindications=result["禁忌"]
        )

    except Exception as e:
        raise ValueError(f"处方生成失败: {str(e)}")

