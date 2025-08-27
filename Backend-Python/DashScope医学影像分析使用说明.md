# 阿里云DashScope医学影像分析功能使用说明

## 概述

本文档介绍如何使用新添加的阿里云灵积（DashScope）API进行医学影像分析。该功能适合企业级应用，提供高质量的医学影像AI分析服务。

## 功能特点

- ✅ 支持多种医学影像类型：X-ray、CT、MRI、Ultrasound、PET-CT
- ✅ 使用阿里云DashScope API，适合企业应用
- ✅ 支持多种视觉模型：qwen-vl-plus、qwen-vl-max
- ✅ 完整的错误处理和重试机制
- ✅ 自动回退到默认结果
- ✅ 结构化的分析结果输出
- ✅ 患者信息上下文支持

## 环境配置

### 1. 安装依赖

确保已安装以下Python包：
```bash
pip install requests python-dotenv pillow
```

### 2. 设置API密钥

#### 方法一：环境变量（推荐）
```bash
# Linux/Mac
export DASHSCOPE_API_KEY="your-dashscope-api-key"

# Windows
set DASHSCOPE_API_KEY=your-dashscope-api-key
```

#### 方法二：.env文件
在项目根目录创建`.env`文件：
```
DASHSCOPE_API_KEY=your-dashscope-api-key
```

#### 方法三：函数参数
直接在函数调用时传入API密钥。

## 使用方法

### 1. 基础使用

```python
from ai.ai_prescription import analyze_medical_image_dashscope
import base64

# 读取图像并转换为Base64
with open('chest_xray.jpg', 'rb') as f:
    image_data = base64.b64encode(f.read()).decode('utf-8')

# 分析医学影像
analysis = analyze_medical_image_dashscope(
    image_data=image_data,
    image_type='X-ray',
    patient_info={
        'age': 45,
        'gender': '女',
        'medical_history': '高血压病史5年'
    },
    model='qwen-vl-plus'
)

# 获取分析结果
print(f"影像类型: {analysis.image_type}")
print(f"主要发现: {analysis.findings['main_findings']}")
print(f"诊断结果: {analysis.diagnosis['primary_diagnosis']}")
print(f"严重程度: {analysis.severity}")
print(f"置信度: {analysis.confidence}")
```

### 2. 简化版使用

```python
from ai.ai_prescription import analyze_medical_image_dashscope_simple

# 使用简化版函数
result = analyze_medical_image_dashscope_simple(
    image_data=image_data,
    image_type='CT',
    patient_info={'age': 30, 'gender': '男'}
)

# 检查分析是否成功
if result['success']:
    print("分析成功！")
    print(result['formatted_result'])
else:
    print(f"分析失败: {result['error']}")
    print("使用默认结果:")
    print(result['formatted_result'])
```

### 3. 高级配置

```python
# 使用高级配置
analysis = analyze_medical_image_dashscope(
    image_data=image_data,
    image_type='MRI',
    patient_info={
        'age': 55,
        'gender': '男',
        'medical_history': '糖尿病、高血压'
    },
    api_key='your-custom-api-key',  # 自定义API密钥
    model='qwen-vl-max',           # 使用更高级的模型
    max_tokens=6000,               # 增加输出长度
    max_retries=5                  # 增加重试次数
)
```

## 支持的参数

### analyze_medical_image_dashscope()

| 参数 | 类型 | 必需 | 默认值 | 说明 |
|------|------|------|--------|------|
| image_data | str | ✅ | - | Base64编码的图像数据 |
| image_type | str | ✅ | - | 影像类型 (X-ray/CT/MRI/Ultrasound/PET-CT) |
| patient_info | dict | ❌ | None | 患者基本信息 |
| api_key | str | ❌ | None | DashScope API密钥 |
| model | str | ❌ | qwen-vl-plus | 视觉模型 (qwen-vl-plus/qwen-vl-max) |
| max_tokens | int | ❌ | 4000 | 最大输出长度 |
| max_retries | int | ❌ | 3 | 最大重试次数 |

### 患者信息格式

```python
patient_info = {
    'age': 45,                    # 年龄
    'gender': '女',               # 性别
    'medical_history': '高血压'    # 病史
}
```

## 返回结果格式

### MedicalImageAnalysis对象

```python
class MedicalImageAnalysis:
    image_type: str          # 影像类型
    findings: dict           # 影像发现
    diagnosis: dict          # 诊断结果
    recommendations: dict    # 建议
    severity: str            # 严重程度
    confidence: float        # 置信度
```

### 简化版返回格式

```python
{
    "success": True,                    # 是否成功
    "image_type": "X-ray",             # 影像类型
    "findings": {...},                 # 影像发现
    "diagnosis": {...},                # 诊断结果
    "recommendations": {...},          # 建议
    "severity": "轻度",                # 严重程度
    "confidence": 0.85,               # 置信度
    "formatted_result": "..."         # 格式化的文本结果
}
```

## 错误处理

### 常见错误类型

1. **API密钥错误**
   ```python
   ValueError: 请设置DASHSCOPE_API_KEY环境变量或提供api_key参数
   ```

2. **网络连接错误**
   ```python
   ValueError: DashScope影像分析失败，已重试3次。网络连接失败: ...
   ```

3. **API调用失败**
   ```python
   ValueError: DashScope影像分析失败，已重试3次。API认证失败: ...
   ```

### 错误处理示例

```python
try:
    analysis = analyze_medical_image_dashscope(
        image_data=image_data,
        image_type='X-ray'
    )
    print("分析成功！")
except ValueError as e:
    print(f"分析失败: {e}")
    # 使用默认结果或其他处理逻辑
except Exception as e:
    print(f"未知错误: {e}")
```

## 性能优化建议

1. **模型选择**
   - `qwen-vl-plus`: 速度快，适合一般分析
   - `qwen-vl-max`: 精度高，适合复杂分析

2. **图像优化**
   - 建议图像大小不超过10MB
   - 支持JPEG、PNG格式
   - 确保图像清晰度足够

3. **并发控制**
   - 避免同时发起过多请求
   - 合理设置重试次数和超时时间

## 测试验证

运行测试脚本验证功能：

```bash
python test_dashscope_image_analysis.py
```

## 注意事项

1. **API配额**: 注意DashScope API的调用配额限制
2. **数据安全**: 确保患者数据的隐私和安全
3. **医疗免责**: AI分析结果仅供参考，不能替代专业医师诊断
4. **网络环境**: 确保网络连接稳定，API调用可能需要较长时间

## 技术支持

如有问题，请检查：
1. API密钥是否正确设置
2. 网络连接是否正常
3. 图像数据是否有效
4. 参数格式是否正确

---

**更新日期**: 2025-01-17  
**版本**: 1.0.0  
**作者**: AI Assistant