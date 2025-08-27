from dataclasses import dataclass
from typing import Optional


@dataclass
class TongueBody:
    """舌质分析结果"""
    color: str  # 舌质颜色（淡红/红/深红/紫等）
    shape: str  # 舌体形态（正常/胖大/瘦薄等）
    texture: str  # 舌质纹理（嫩/老等）
    mobility: str  # 舌体活动度


@dataclass
class TongueCoating:
    """舌苔分析结果"""
    color: str  # 苔色（白/黄/灰/黑等）
    thickness: str  # 苔质厚薄（薄/厚等）
    moisture: str  # 润燥程度（润/燥等）
    texture: str  # 苔质性状（腻/腐等）


@dataclass
class TongueAnalysis:
    """舌诊分析结果"""
    tongue_body: TongueBody  # 舌质分析
    tongue_coating: TongueCoating  # 舌苔分析


@dataclass
class TCMDiagnosis:
    """中医诊断结果"""
    syndrome_pattern: str  # 主要证候类型
    constitution_type: str  # 体质类型判断
    pathological_factors: str  # 病理因素分析
    organ_systems: str  # 涉及脏腑系统


@dataclass
class TCMRecommendations:
    """中医调理建议"""
    dietary_therapy: str  # 食疗建议
    lifestyle_adjustment: str  # 生活调理
    herbal_suggestions: str  # 中药调理方向
    follow_up: str  # 复诊建议


@dataclass
class TongueDiagnosisResult:
    """中医舌诊完整结果"""
    image_type: str  # 影像类型
    tongue_analysis: TongueAnalysis  # 舌诊分析
    tcm_diagnosis: TCMDiagnosis  # 中医诊断
    recommendations: TCMRecommendations  # 调理建议
    severity: str  # 严重程度（轻度/中度/重度）
    confidence: float  # 置信度


# 中医面诊相关数据类

@dataclass
class Complexion:
    """面色分析结果"""
    color: str  # 面色（红润/苍白/萎黄/青紫等）
    luster: str  # 光泽度（有神/无神等）
    texture: str  # 皮肤质地
    distribution: str  # 色泽分布特点


@dataclass
class FacialFeatures:
    """五官特征分析"""
    eyes: str  # 眼部特征分析
    nose: str  # 鼻部特征分析
    mouth: str  # 口唇特征分析
    ears: str  # 耳部特征分析


@dataclass
class FacialRegions:
    """面部区域分析"""
    forehead: str  # 额部对应心肺功能
    cheeks: str  # 面颊对应脾胃功能
    chin: str  # 下颏对应肾功能
    temples: str  # 太阳穴区域分析


@dataclass
class FacialAnalysis:
    """面诊分析结果"""
    complexion: Complexion  # 面色分析
    facial_features: FacialFeatures  # 五官分析
    facial_regions: FacialRegions  # 面部区域分析


@dataclass
class TCMFaceDiagnosis:
    """中医面诊诊断结果"""
    syndrome_pattern: str  # 主要证候类型
    constitution_type: str  # 体质类型判断
    organ_function: str  # 脏腑功能状态
    qi_blood_status: str  # 气血状态评估


@dataclass
class TCMFaceRecommendations:
    """中医面诊调理建议"""
    dietary_therapy: str  # 食疗建议
    lifestyle_adjustment: str  # 生活调理
    herbal_suggestions: str  # 中药调理方向
    acupoint_massage: str  # 穴位按摩建议


@dataclass
class FaceDiagnosisResult:
    """中医面诊完整结果"""
    image_type: str  # 影像类型
    facial_analysis: FacialAnalysis  # 面诊分析
    tcm_diagnosis: TCMFaceDiagnosis  # 中医诊断
    recommendations: TCMFaceRecommendations  # 调理建议
    severity: str  # 严重程度（轻度/中度/重度）
    confidence: float  # 置信度