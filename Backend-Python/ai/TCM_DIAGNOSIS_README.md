# 中医诊断数据类使用说明

本文档介绍如何使用中医舌诊和面诊的数据类（dataclass）来处理AI分析结果。

## 文件结构

```
ai/
├── tcm_diagnosis_models.py     # 数据类定义
├── tcm_diagnosis_parser.py     # JSON解析器
├── tcm_diagnosis_example.py    # 使用示例
└── TCM_DIAGNOSIS_README.md     # 本说明文档
```

## 主要组件

### 1. 数据类模型 (tcm_diagnosis_models.py)

#### 中医舌诊相关类

- **TongueBody**: 舌质分析结果
  - `color`: 舌质颜色（淡红/红/深红/紫等）
  - `shape`: 舌体形态（正常/胖大/瘦薄等）
  - `texture`: 舌质纹理（嫩/老等）
  - `mobility`: 舌体活动度

- **TongueCoating**: 舌苔分析结果
  - `color`: 苔色（白/黄/灰/黑等）
  - `thickness`: 苔质厚薄（薄/厚等）
  - `moisture`: 润燥程度（润/燥等）
  - `texture`: 苔质性状（腻/腐等）

- **TongueAnalysis**: 舌诊分析结果
  - `tongue_body`: 舌质分析
  - `tongue_coating`: 舌苔分析

- **TCMDiagnosis**: 中医诊断结果
  - `syndrome_pattern`: 主要证候类型
  - `constitution_type`: 体质类型判断
  - `pathological_factors`: 病理因素分析
  - `organ_systems`: 涉及脏腑系统

- **TCMRecommendations**: 中医调理建议
  - `dietary_therapy`: 食疗建议
  - `lifestyle_adjustment`: 生活调理
  - `herbal_suggestions`: 中药调理方向
  - `follow_up`: 复诊建议

- **TongueDiagnosisResult**: 中医舌诊完整结果
  - `image_type`: 影像类型
  - `tongue_analysis`: 舌诊分析
  - `tcm_diagnosis`: 中医诊断
  - `recommendations`: 调理建议
  - `severity`: 严重程度（轻度/中度/重度）
  - `confidence`: 置信度

#### 中医面诊相关类

- **Complexion**: 面色分析结果
- **FacialFeatures**: 五官特征分析
- **FacialRegions**: 面部区域分析
- **FacialAnalysis**: 面诊分析结果
- **TCMFaceDiagnosis**: 中医面诊诊断结果
- **TCMFaceRecommendations**: 中医面诊调理建议
- **FaceDiagnosisResult**: 中医面诊完整结果

### 2. JSON解析器 (tcm_diagnosis_parser.py)

#### 主要函数

- **parse_tongue_diagnosis_json()**: 解析舌诊JSON为dataclass对象
- **parse_face_diagnosis_json()**: 解析面诊JSON为dataclass对象
- **parse_tcm_diagnosis_json()**: 自动识别类型并解析
- **tongue_diagnosis_to_dict()**: 将舌诊对象转换为字典
- **face_diagnosis_to_dict()**: 将面诊对象转换为字典

## 使用方法

### 1. 基本使用

```python
from tcm_diagnosis_parser import parse_tcm_diagnosis_json

# AI返回的JSON字符串
ai_response = '''
{
    "image_type": "中医舌诊",
    "tongue_analysis": {
        "tongue_body": {
            "color": "淡红",
            "shape": "正常",
            "texture": "嫩",
            "mobility": "灵活"
        },
        "tongue_coating": {
            "color": "白",
            "thickness": "薄",
            "moisture": "润",
            "texture": "正常"
        }
    },
    "tcm_diagnosis": {
        "syndrome_pattern": "脾胃虚弱",
        "constitution_type": "气虚质",
        "pathological_factors": "脾气不足，运化失常",
        "organ_systems": "脾胃系统"
    },
    "recommendations": {
        "dietary_therapy": "多食健脾益气食物",
        "lifestyle_adjustment": "规律作息，适量运动",
        "herbal_suggestions": "四君子汤加减",
        "follow_up": "2周后复诊"
    },
    "severity": "轻度",
    "confidence": 0.85
}
'''

# 解析为dataclass对象
result = parse_tcm_diagnosis_json(ai_response)

# 访问数据
print(f"舌质颜色: {result.tongue_analysis.tongue_body.color}")
print(f"证候类型: {result.tcm_diagnosis.syndrome_pattern}")
print(f"食疗建议: {result.recommendations.dietary_therapy}")
```

### 2. 手动创建对象

```python
from tcm_diagnosis_models import (
    TongueBody, TongueCoating, TongueAnalysis, 
    TCMDiagnosis, TCMRecommendations, TongueDiagnosisResult
)

# 创建舌质分析
tongue_body = TongueBody(
    color="淡红",
    shape="正常",
    texture="嫩",
    mobility="灵活"
)

# 创建舌苔分析
tongue_coating = TongueCoating(
    color="白",
    thickness="薄",
    moisture="润",
    texture="正常"
)

# 组合舌诊分析
tongue_analysis = TongueAnalysis(
    tongue_body=tongue_body,
    tongue_coating=tongue_coating
)

# 创建完整结果
result = TongueDiagnosisResult(
    image_type="中医舌诊",
    tongue_analysis=tongue_analysis,
    tcm_diagnosis=tcm_diagnosis,
    recommendations=recommendations,
    severity="轻度",
    confidence=0.85
)
```

### 3. 错误处理

```python
from tcm_diagnosis_parser import parse_tcm_diagnosis_json

try:
    result = parse_tcm_diagnosis_json(json_data)
    # 处理结果
except ValueError as e:
    print(f"JSON格式错误: {e}")
except KeyError as e:
    print(f"缺少必要字段: {e}")
except Exception as e:
    print(f"其他错误: {e}")
```

### 4. 转换为字典

```python
from tcm_diagnosis_parser import tongue_diagnosis_to_dict

# 将dataclass对象转换为字典
result_dict = tongue_diagnosis_to_dict(result)

# 转换为JSON字符串
import json
json_string = json.dumps(result_dict, ensure_ascii=False, indent=2)
```

## JSON数据结构

### 中医舌诊JSON结构

```json
{
    "image_type": "中医舌诊",
    "tongue_analysis": {
        "tongue_body": {
            "color": "舌质颜色",
            "shape": "舌体形态",
            "texture": "舌质纹理",
            "mobility": "舌体活动度"
        },
        "tongue_coating": {
            "color": "苔色",
            "thickness": "苔质厚薄",
            "moisture": "润燥程度",
            "texture": "苔质性状"
        }
    },
    "tcm_diagnosis": {
        "syndrome_pattern": "主要证候类型",
        "constitution_type": "体质类型判断",
        "pathological_factors": "病理因素分析",
        "organ_systems": "涉及脏腑系统"
    },
    "recommendations": {
        "dietary_therapy": "食疗建议",
        "lifestyle_adjustment": "生活调理",
        "herbal_suggestions": "中药调理方向",
        "follow_up": "复诊建议"
    },
    "severity": "轻度/中度/重度",
    "confidence": 0.85
}
```

### 中医面诊JSON结构

```json
{
    "image_type": "中医面诊",
    "facial_analysis": {
        "complexion": {
            "color": "面色",
            "luster": "光泽度",
            "texture": "皮肤质地",
            "distribution": "色泽分布特点"
        },
        "facial_features": {
            "eyes": "眼部特征分析",
            "nose": "鼻部特征分析",
            "mouth": "口唇特征分析",
            "ears": "耳部特征分析"
        },
        "facial_regions": {
            "forehead": "额部对应心肺功能",
            "cheeks": "面颊对应脾胃功能",
            "chin": "下颏对应肾功能",
            "temples": "太阳穴区域分析"
        }
    },
    "tcm_diagnosis": {
        "syndrome_pattern": "主要证候类型",
        "constitution_type": "体质类型判断",
        "organ_function": "脏腑功能状态",
        "qi_blood_status": "气血状态评估"
    },
    "recommendations": {
        "dietary_therapy": "食疗建议",
        "lifestyle_adjustment": "生活调理",
        "herbal_suggestions": "中药调理方向",
        "acupoint_massage": "穴位按摩建议"
    },
    "severity": "轻度/中度/重度",
    "confidence": 0.85
}
```

## 运行示例

```bash
# 在Backend-Python目录下运行
python ai/tcm_diagnosis_example.py
```

这将运行所有示例，包括：
- 舌诊数据解析示例
- 面诊数据解析示例
- 自动解析功能示例
- 手动创建对象示例

## 注意事项

1. **数据完整性**: 确保JSON数据包含所有必要字段
2. **类型安全**: 使用dataclass提供类型提示和验证
3. **错误处理**: 始终包含适当的异常处理
4. **置信度**: confidence字段应为0-1之间的浮点数
5. **中医术语**: 使用标准的中医术语和表达方式

## 扩展性

如需添加新的字段或修改结构：

1. 更新 `tcm_diagnosis_models.py` 中的dataclass定义
2. 相应更新 `tcm_diagnosis_parser.py` 中的解析函数
3. 更新 `ai_prescription.py` 中的提示词JSON结构
4. 运行示例文件验证更改

## 集成到现有系统

在 `prescriptions.py` 中使用：

```python
from ai.tcm_diagnosis_parser import parse_tcm_diagnosis_json

# 在analyze_medical_image_dashscope函数中
if image_type in ["中医舌诊", "中医面诊"]:
    # 获取AI响应后
    try:
        structured_result = parse_tcm_diagnosis_json(ai_response)
        # 使用结构化数据
        return {
            "success": True,
            "analysis": structured_result,
            "raw_response": ai_response
        }
    except Exception as e:
        # 降级到原始响应
        return {
            "success": True,
            "analysis": ai_response,
            "parsing_error": str(e)
        }
```

这样可以在保持向后兼容的同时，为中医诊断提供结构化的数据处理能力。