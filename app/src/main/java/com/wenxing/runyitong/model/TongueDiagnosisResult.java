package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;

/**
 * 中医舌诊完整结果模型类
 * 对应后端 TongueDiagnosisResult 数据类
 */
public class TongueDiagnosisResult {
    @SerializedName("image_type")
    private String imageType;                    // 影像类型
    
    @SerializedName("tongue_analysis")
    private TongueAnalysis tongueAnalysis;       // 舌诊分析
    
    @SerializedName("tcm_diagnosis")
    private TCMDiagnosis tcmDiagnosis;          // 中医诊断
    
    @SerializedName("recommendations")
    private TCMRecommendations recommendations;  // 调理建议
    
    @SerializedName("severity")
    private String severity;                     // 严重程度（轻度/中度/重度）
    
    @SerializedName("confidence")
    private float confidence;                    // 置信度

    /**
     * 默认构造函数
     */
    public TongueDiagnosisResult() {
    }

    /**
     * 带参数的构造函数
     * @param imageType 影像类型
     * @param tongueAnalysis 舌诊分析
     * @param tcmDiagnosis 中医诊断
     * @param recommendations 调理建议
     * @param severity 严重程度
     * @param confidence 置信度
     */
    public TongueDiagnosisResult(String imageType, TongueAnalysis tongueAnalysis, 
                                TCMDiagnosis tcmDiagnosis, TCMRecommendations recommendations,
                                String severity, float confidence) {
        this.imageType = imageType;
        this.tongueAnalysis = tongueAnalysis;
        this.tcmDiagnosis = tcmDiagnosis;
        this.recommendations = recommendations;
        this.severity = severity;
        this.confidence = confidence;
    }

    // Getter 方法
    public String getImageType() {
        return imageType;
    }

    public TongueAnalysis getTongueAnalysis() {
        return tongueAnalysis;
    }

    public TCMDiagnosis getTcmDiagnosis() {
        return tcmDiagnosis;
    }

    public TCMRecommendations getRecommendations() {
        return recommendations;
    }

    public String getSeverity() {
        return severity;
    }

    public float getConfidence() {
        return confidence;
    }

    // Setter 方法
    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public void setTongueAnalysis(TongueAnalysis tongueAnalysis) {
        this.tongueAnalysis = tongueAnalysis;
    }

    public void setTcmDiagnosis(TCMDiagnosis tcmDiagnosis) {
        this.tcmDiagnosis = tcmDiagnosis;
    }

    public void setRecommendations(TCMRecommendations recommendations) {
        this.recommendations = recommendations;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    @Override
    public String toString() {
        return "TongueDiagnosisResult{" +
                "imageType='" + imageType + '\'' +
                ", tongueAnalysis=" + tongueAnalysis +
                ", tcmDiagnosis=" + tcmDiagnosis +
                ", recommendations=" + recommendations +
                ", severity='" + severity + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}