import json
from typing import Dict, Any, Union
from tcm_diagnosis_models import (
    TongueBody, TongueCoating, TongueAnalysis, TCMDiagnosis, TCMRecommendations, TongueDiagnosisResult,
    Complexion, FacialFeatures, FacialRegions, FacialAnalysis, TCMFaceDiagnosis, TCMFaceRecommendations, FaceDiagnosisResult
)


def parse_tongue_diagnosis_json(json_data: Union[str, Dict[str, Any]]) -> TongueDiagnosisResult:
    """解析中医舌诊JSON数据为dataclass对象
    
    Args:
        json_data: JSON字符串或字典数据
        
    Returns:
        TongueDiagnosisResult: 舌诊结果对象
        
    Raises:
        ValueError: 当JSON数据格式不正确时
        KeyError: 当缺少必要字段时
    """
    if isinstance(json_data, str):
        try:
            data = json.loads(json_data)
        except json.JSONDecodeError as e:
            raise ValueError(f"无效的JSON格式: {e}")
    else:
        data = json_data
    
    try:
        # 解析舌质分析
        tongue_body_data = data['tongue_analysis']['tongue_body']
        tongue_body = TongueBody(
            color=tongue_body_data['color'],
            shape=tongue_body_data['shape'],
            texture=tongue_body_data['texture'],
            mobility=tongue_body_data['mobility']
        )
        
        # 解析舌苔分析
        tongue_coating_data = data['tongue_analysis']['tongue_coating']
        tongue_coating = TongueCoating(
            color=tongue_coating_data['color'],
            thickness=tongue_coating_data['thickness'],
            moisture=tongue_coating_data['moisture'],
            texture=tongue_coating_data['texture']
        )
        
        # 组合舌诊分析
        tongue_analysis = TongueAnalysis(
            tongue_body=tongue_body,
            tongue_coating=tongue_coating
        )
        
        # 解析中医诊断
        tcm_diagnosis_data = data['tcm_diagnosis']
        tcm_diagnosis = TCMDiagnosis(
            syndrome_pattern=tcm_diagnosis_data['syndrome_pattern'],
            constitution_type=tcm_diagnosis_data['constitution_type'],
            pathological_factors=tcm_diagnosis_data['pathological_factors'],
            organ_systems=tcm_diagnosis_data['organ_systems']
        )
        
        # 解析调理建议
        recommendations_data = data['recommendations']
        recommendations = TCMRecommendations(
            dietary_therapy=recommendations_data['dietary_therapy'],
            lifestyle_adjustment=recommendations_data['lifestyle_adjustment'],
            herbal_suggestions=recommendations_data['herbal_suggestions'],
            follow_up=recommendations_data['follow_up']
        )
        
        # 创建完整结果
        result = TongueDiagnosisResult(
            image_type=data['image_type'],
            tongue_analysis=tongue_analysis,
            tcm_diagnosis=tcm_diagnosis,
            recommendations=recommendations,
            severity=data['severity'],
            confidence=float(data['confidence'])
        )
        
        return result
        
    except KeyError as e:
        raise KeyError(f"缺少必要字段: {e}")
    except (TypeError, ValueError) as e:
        raise ValueError(f"数据类型错误: {e}")


def parse_face_diagnosis_json(json_data: Union[str, Dict[str, Any]]) -> FaceDiagnosisResult:
    """解析中医面诊JSON数据为dataclass对象
    
    Args:
        json_data: JSON字符串或字典数据
        
    Returns:
        FaceDiagnosisResult: 面诊结果对象
        
    Raises:
        ValueError: 当JSON数据格式不正确时
        KeyError: 当缺少必要字段时
    """
    if isinstance(json_data, str):
        try:
            data = json.loads(json_data)
        except json.JSONDecodeError as e:
            raise ValueError(f"无效的JSON格式: {e}")
    else:
        data = json_data
    
    try:
        # 解析面色分析
        complexion_data = data['facial_analysis']['complexion']
        complexion = Complexion(
            color=complexion_data['color'],
            luster=complexion_data['luster'],
            texture=complexion_data['texture'],
            distribution=complexion_data['distribution']
        )
        
        # 解析五官特征
        facial_features_data = data['facial_analysis']['facial_features']
        facial_features = FacialFeatures(
            eyes=facial_features_data['eyes'],
            nose=facial_features_data['nose'],
            mouth=facial_features_data['mouth'],
            ears=facial_features_data['ears']
        )
        
        # 解析面部区域
        facial_regions_data = data['facial_analysis']['facial_regions']
        facial_regions = FacialRegions(
            forehead=facial_regions_data['forehead'],
            cheeks=facial_regions_data['cheeks'],
            chin=facial_regions_data['chin'],
            temples=facial_regions_data['temples']
        )
        
        # 组合面诊分析
        facial_analysis = FacialAnalysis(
            complexion=complexion,
            facial_features=facial_features,
            facial_regions=facial_regions
        )
        
        # 解析中医诊断
        tcm_diagnosis_data = data['tcm_diagnosis']
        tcm_diagnosis = TCMFaceDiagnosis(
            syndrome_pattern=tcm_diagnosis_data['syndrome_pattern'],
            constitution_type=tcm_diagnosis_data['constitution_type'],
            organ_function=tcm_diagnosis_data['organ_function'],
            qi_blood_status=tcm_diagnosis_data['qi_blood_status']
        )
        
        # 解析调理建议
        recommendations_data = data['recommendations']
        recommendations = TCMFaceRecommendations(
            dietary_therapy=recommendations_data['dietary_therapy'],
            lifestyle_adjustment=recommendations_data['lifestyle_adjustment'],
            herbal_suggestions=recommendations_data['herbal_suggestions'],
            acupoint_massage=recommendations_data['acupoint_massage']
        )
        
        # 创建完整结果
        result = FaceDiagnosisResult(
            image_type=data['image_type'],
            facial_analysis=facial_analysis,
            tcm_diagnosis=tcm_diagnosis,
            recommendations=recommendations,
            severity=data['severity'],
            confidence=float(data['confidence'])
        )
        
        return result
        
    except KeyError as e:
        raise KeyError(f"缺少必要字段: {e}")
    except (TypeError, ValueError) as e:
        raise ValueError(f"数据类型错误: {e}")


def parse_tcm_diagnosis_json(json_data: Union[str, Dict[str, Any]]) -> Union[TongueDiagnosisResult, FaceDiagnosisResult]:
    """根据影像类型自动解析中医诊断JSON数据
    
    Args:
        json_data: JSON字符串或字典数据
        
    Returns:
        Union[TongueDiagnosisResult, FaceDiagnosisResult]: 对应的诊断结果对象
        
    Raises:
        ValueError: 当JSON数据格式不正确或影像类型不支持时
    """
    if isinstance(json_data, str):
        try:
            data = json.loads(json_data)
        except json.JSONDecodeError as e:
            raise ValueError(f"无效的JSON格式: {e}")
    else:
        data = json_data
    
    image_type = data.get('image_type', '')
    
    if image_type == '中医舌诊':
        return parse_tongue_diagnosis_json(data)
    elif image_type == '中医面诊':
        return parse_face_diagnosis_json(data)
    else:
        raise ValueError(f"不支持的影像类型: {image_type}")


# 工具函数：将dataclass对象转换为字典
def tongue_diagnosis_to_dict(result: TongueDiagnosisResult) -> Dict[str, Any]:
    """将舌诊结果对象转换为字典
    
    Args:
        result: 舌诊结果对象
        
    Returns:
        Dict[str, Any]: 字典格式的结果
    """
    return {
        'image_type': result.image_type,
        'tongue_analysis': {
            'tongue_body': {
                'color': result.tongue_analysis.tongue_body.color,
                'shape': result.tongue_analysis.tongue_body.shape,
                'texture': result.tongue_analysis.tongue_body.texture,
                'mobility': result.tongue_analysis.tongue_body.mobility
            },
            'tongue_coating': {
                'color': result.tongue_analysis.tongue_coating.color,
                'thickness': result.tongue_analysis.tongue_coating.thickness,
                'moisture': result.tongue_analysis.tongue_coating.moisture,
                'texture': result.tongue_analysis.tongue_coating.texture
            }
        },
        'tcm_diagnosis': {
            'syndrome_pattern': result.tcm_diagnosis.syndrome_pattern,
            'constitution_type': result.tcm_diagnosis.constitution_type,
            'pathological_factors': result.tcm_diagnosis.pathological_factors,
            'organ_systems': result.tcm_diagnosis.organ_systems
        },
        'recommendations': {
            'dietary_therapy': result.recommendations.dietary_therapy,
            'lifestyle_adjustment': result.recommendations.lifestyle_adjustment,
            'herbal_suggestions': result.recommendations.herbal_suggestions,
            'follow_up': result.recommendations.follow_up
        },
        'severity': result.severity,
        'confidence': result.confidence
    }


def face_diagnosis_to_dict(result: FaceDiagnosisResult) -> Dict[str, Any]:
    """将面诊结果对象转换为字典
    
    Args:
        result: 面诊结果对象
        
    Returns:
        Dict[str, Any]: 字典格式的结果
    """
    return {
        'image_type': result.image_type,
        'facial_analysis': {
            'complexion': {
                'color': result.facial_analysis.complexion.color,
                'luster': result.facial_analysis.complexion.luster,
                'texture': result.facial_analysis.complexion.texture,
                'distribution': result.facial_analysis.complexion.distribution
            },
            'facial_features': {
                'eyes': result.facial_analysis.facial_features.eyes,
                'nose': result.facial_analysis.facial_features.nose,
                'mouth': result.facial_analysis.facial_features.mouth,
                'ears': result.facial_analysis.facial_features.ears
            },
            'facial_regions': {
                'forehead': result.facial_analysis.facial_regions.forehead,
                'cheeks': result.facial_analysis.facial_regions.cheeks,
                'chin': result.facial_analysis.facial_regions.chin,
                'temples': result.facial_analysis.facial_regions.temples
            }
        },
        'tcm_diagnosis': {
            'syndrome_pattern': result.tcm_diagnosis.syndrome_pattern,
            'constitution_type': result.tcm_diagnosis.constitution_type,
            'organ_function': result.tcm_diagnosis.organ_function,
            'qi_blood_status': result.tcm_diagnosis.qi_blood_status
        },
        'recommendations': {
            'dietary_therapy': result.recommendations.dietary_therapy,
            'lifestyle_adjustment': result.recommendations.lifestyle_adjustment,
            'herbal_suggestions': result.recommendations.herbal_suggestions,
            'acupoint_massage': result.recommendations.acupoint_massage
        },
        'severity': result.severity,
        'confidence': result.confidence
    }