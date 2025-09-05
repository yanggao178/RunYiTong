package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;

/**
 * 中医面诊完整结果模型类
 * 对应后端 FaceDiagnosisResult 数据类
 */
public class FaceDiagnosisResult {
    @SerializedName("image_type")
    private String imageType;                         // 影像类型
    
    @SerializedName("facial_analysis")
    private FacialAnalysis facialAnalysis;            // 面诊分析
    
    @SerializedName("tcm_diagnosis")
    private TCMFaceDiagnosis tcmDiagnosis;           // 中医诊断
    
    @SerializedName("recommendations")
    private TCMFaceRecommendations recommendations;   // 调理建议
    
    @SerializedName("severity")
    private String severity;                          // 严重程度（轻度/中度/重度）
    
    @SerializedName("confidence")
    private float confidence;                         // 置信度

    /**
     * 默认构造函数
     */
    public FaceDiagnosisResult() {
    }

    /**
     * 带参数的构造函数
     * @param imageType 影像类型
     * @param facialAnalysis 面诊分析
     * @param tcmDiagnosis 中医诊断
     * @param recommendations 调理建议
     * @param severity 严重程度
     * @param confidence 置信度
     */
    public FaceDiagnosisResult(String imageType, FacialAnalysis facialAnalysis, 
                              TCMFaceDiagnosis tcmDiagnosis, TCMFaceRecommendations recommendations,
                              String severity, float confidence) {
        this.imageType = imageType;
        this.facialAnalysis = facialAnalysis;
        this.tcmDiagnosis = tcmDiagnosis;
        this.recommendations = recommendations;
        this.severity = severity;
        this.confidence = confidence;
    }

    // Getter 方法
    public String getImageType() {
        return imageType;
    }

    public FacialAnalysis getFacialAnalysis() {
        return facialAnalysis;
    }

    public TCMFaceDiagnosis getTcmDiagnosis() {
        return tcmDiagnosis;
    }

    public TCMFaceRecommendations getRecommendations() {
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

    public void setFacialAnalysis(FacialAnalysis facialAnalysis) {
        this.facialAnalysis = facialAnalysis;
    }

    public void setTcmDiagnosis(TCMFaceDiagnosis tcmDiagnosis) {
        this.tcmDiagnosis = tcmDiagnosis;
    }

    public void setRecommendations(TCMFaceRecommendations recommendations) {
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
        return "FaceDiagnosisResult{" +
                "imageType='" + imageType + '\'' +
                ", facialAnalysis=" + facialAnalysis +
                ", tcmDiagnosis=" + tcmDiagnosis +
                ", recommendations=" + recommendations +
                ", severity='" + severity + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}