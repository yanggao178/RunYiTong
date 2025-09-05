package com.wenxing.runyitong.model;

import java.util.Map;
import java.util.HashMap;
import com.google.gson.annotations.SerializedName;

/**
 * 医学影像分析结果模型类
 * 与服务端MedicalImageAnalysis类保持一致
 * 用于存储AI医学影像分析的完整结果数据
 */
public class MedicalImageAnalysis {
    
    /**
     * 影像类型（xray, ct, ultrasound, mri, petct）
     */
    @SerializedName("image_type")
    private String imageType;
    
    /**
     * 影像发现结果
     * 包含primary_findings, secondary_findings, abnormalities, normal_findings, image_quality等字段
     */
    @SerializedName("findings")
    private Map<String, Object> findings;
    
    /**
     * 诊断结果
     * 包含primary_diagnosis, differential_diagnosis, diagnostic_confidence, severity_level, prognosis等字段
     */
    @SerializedName("diagnosis")
    private Map<String, Object> diagnosis;
    
    /**
     * 建议和推荐
     * 包含immediate_actions, follow_up, treatment, lifestyle, further_examinations, specialist_referral等字段
     */
    @SerializedName("recommendations")
    private Map<String, Object> recommendations;
    
    /**
     * 严重程度评估
     */
    @SerializedName("severity")
    private String severity;
    
    /**
     * AI分析置信度（0.0-1.0）
     */
    @SerializedName("confidence")
    private double confidence;
    
    /**
     * 默认构造函数
     */
    public MedicalImageAnalysis() {
        this.findings = new HashMap<>();
        this.diagnosis = new HashMap<>();
        this.recommendations = new HashMap<>();
    }
    
    /**
     * 完整构造函数
     * @param imageType 影像类型
     * @param findings 影像发现
     * @param diagnosis 诊断结果
     * @param recommendations 建议推荐
     * @param severity 严重程度
     * @param confidence 置信度
     */
    public MedicalImageAnalysis(String imageType, Map<String, Object> findings, 
                               Map<String, Object> diagnosis, Map<String, Object> recommendations,
                               String severity, double confidence) {
        this.imageType = imageType;
        this.findings = findings != null ? findings : new HashMap<>();
        this.diagnosis = diagnosis != null ? diagnosis : new HashMap<>();
        this.recommendations = recommendations != null ? recommendations : new HashMap<>();
        this.severity = severity;
        this.confidence = confidence;
    }
    
    // ==================== Getter和Setter方法 ====================
    
    public String getImageType() {
        return imageType;
    }
    
    public void setImageType(String imageType) {
        this.imageType = imageType;
    }
    
    public Map<String, Object> getFindings() {
        return findings;
    }
    
    public void setFindings(Map<String, Object> findings) {
        this.findings = findings != null ? findings : new HashMap<>();
    }
    
    public Map<String, Object> getDiagnosis() {
        return diagnosis;
    }
    
    public void setDiagnosis(Map<String, Object> diagnosis) {
        this.diagnosis = diagnosis != null ? diagnosis : new HashMap<>();
    }
    
    public Map<String, Object> getRecommendations() {
        return recommendations;
    }
    
    public void setRecommendations(Map<String, Object> recommendations) {
        this.recommendations = recommendations != null ? recommendations : new HashMap<>();
    }
    
    public String getSeverity() {
        return severity;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    public double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
    
    // ==================== Findings相关便捷方法 ====================
    
    /**
     * 获取主要发现
     * @return 主要发现描述
     */
    public String getPrimaryFindings() {
        if (findings != null && findings.containsKey("primary_findings")) {
            Object value = findings.get("primary_findings");
            return value != null ? value.toString() : null;
        }
        return null;
    }
    
    /**
     * 获取次要发现
     * @return 次要发现描述
     */
    public String getSecondaryFindings() {
        if (findings != null && findings.containsKey("secondary_findings")) {
            Object value = findings.get("secondary_findings");
            return value != null ? value.toString() : null;
        }
        return null;
    }
    
    /**
     * 获取异常表现
     * @return 异常表现描述
     */
    public String getAbnormalities() {
        if (findings != null && findings.containsKey("abnormalities")) {
            Object value = findings.get("abnormalities");
            return value != null ? value.toString() : null;
        }
        return null;
    }
    
    /**
     * 获取正常发现
     * @return 正常发现描述
     */
    public String getNormalFindings() {
        if (findings != null && findings.containsKey("normal_findings")) {
            Object value = findings.get("normal_findings");
            return value != null ? value.toString() : null;
        }
        return null;
    }
    
    /**
     * 获取影像质量评估
     * @return 影像质量描述
     */
    public String getImageQualityAssessment() {
        if (findings != null && findings.containsKey("image_quality")) {
            Object value = findings.get("image_quality");
            return value != null ? value.toString() : null;
        }
        return null;
    }
    
    // ==================== Diagnosis相关便捷方法 ====================
    
    /**
     * 获取主要诊断
     * @return 主要诊断描述
     */
    public String getPrimaryDiagnosis() {
        if (diagnosis != null && diagnosis.containsKey("primary_diagnosis")) {
            Object value = diagnosis.get("primary_diagnosis");
            return value != null ? value.toString() : null;
        }
        return null;
    }
    
    /**
     * 获取鉴别诊断
     * @return 鉴别诊断描述
     */
    public String getDifferentialDiagnosis() {
        if (diagnosis != null && diagnosis.containsKey("differential_diagnosis")) {
            Object value = diagnosis.get("differential_diagnosis");
            return value != null ? value.toString() : null;
        }
        return null;
    }
    
    /**
     * 获取诊断置信度
     * @return 诊断置信度字符串
     */
    public String getDiagnosticConfidence() {
        if (diagnosis != null && diagnosis.containsKey("diagnostic_confidence")) {
            Object value = diagnosis.get("diagnostic_confidence");
            return value != null ? value.toString() : null;
        }
        return null;
    }
    
    /**
     * 获取严重程度级别
     * @return 严重程度级别描述
     */
    public String getSeverityLevel() {
        if (diagnosis != null && diagnosis.containsKey("severity_level")) {
            Object value = diagnosis.get("severity_level");
            return value != null ? value.toString() : null;
        }
        return null;
    }
    
    /**
     * 获取预后评估
     * @return 预后评估描述
     */
    public String getPrognosis() {
        if (diagnosis != null && diagnosis.containsKey("prognosis")) {
            Object value = diagnosis.get("prognosis");
            return value != null ? value.toString() : null;
        }
        return null;
    }
    
    // ==================== Recommendations相关便捷方法 ====================
    
    /**
     * 获取即时处理建议
     * @return 即时处理建议描述
     */
    public String getImmediateActions() {
        if (recommendations != null && recommendations.containsKey("immediate_actions")) {
            Object value = recommendations.get("immediate_actions");
            return value != null ? value.toString() : null;
        }
        return null;
    }
    
    /**
     * 获取随访建议
     * @return 随访建议描述
     */
    public String getFollowUp() {
        if (recommendations != null && recommendations.containsKey("follow_up")) {
            Object value = recommendations.get("follow_up");
            return value != null ? value.toString() : null;
        }
        return null;
    }
    
    /**
     * 获取治疗建议
     * @return 治疗建议描述
     */
    public String getTreatmentRecommendations() {
        if (recommendations != null && recommendations.containsKey("treatment")) {
            Object value = recommendations.get("treatment");
            return value != null ? value.toString() : null;
        }
        return null;
    }
    
    /**
     * 获取生活方式建议
     * @return 生活方式建议描述
     */
    public String getLifestyleRecommendations() {
        if (recommendations != null && recommendations.containsKey("lifestyle")) {
            Object value = recommendations.get("lifestyle");
            return value != null ? value.toString() : null;
        }
        return null;
    }
    
    /**
     * 获取进一步检查建议
     * @return 进一步检查建议描述
     */
    public String getFurtherExaminations() {
        if (recommendations != null && recommendations.containsKey("further_examinations")) {
            Object value = recommendations.get("further_examinations");
            return value != null ? value.toString() : null;
        }
        return null;
    }
    
    /**
     * 获取专科转诊建议
     * @return 专科转诊建议描述
     */
    public String getSpecialistReferral() {
        if (recommendations != null && recommendations.containsKey("specialist_referral")) {
            Object value = recommendations.get("specialist_referral");
            return value != null ? value.toString() : null;
        }
        return null;
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 检查分析结果是否有效
     * @return 如果有基本的分析数据则返回true
     */
    public boolean isValid() {
        return imageType != null && !imageType.trim().isEmpty() &&
               (findings != null && !findings.isEmpty() ||
                diagnosis != null && !diagnosis.isEmpty() ||
                recommendations != null && !recommendations.isEmpty());
    }
    
    /**
     * 获取置信度百分比字符串
     * @return 格式化的置信度百分比
     */
    public String getConfidencePercentage() {
        return String.format("%.1f%%", confidence * 100);
    }
    
    /**
     * 检查是否有异常发现
     * @return 如果有异常发现则返回true
     */
    public boolean hasAbnormalities() {
        String abnormalities = getAbnormalities();
        return abnormalities != null && !abnormalities.trim().isEmpty() &&
               !abnormalities.toLowerCase().contains("未发现") &&
               !abnormalities.toLowerCase().contains("无明显");
    }
    
    /**
     * 获取简化的分析摘要
     * @return 分析结果摘要
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        
        String primaryDiagnosis = getPrimaryDiagnosis();
        if (primaryDiagnosis != null && !primaryDiagnosis.trim().isEmpty()) {
            summary.append("诊断: ").append(primaryDiagnosis);
        }
        
        if (severity != null && !severity.trim().isEmpty()) {
            if (summary.length() > 0) summary.append(" | ");
            summary.append("严重程度: ").append(severity);
        }
        
        if (confidence > 0) {
            if (summary.length() > 0) summary.append(" | ");
            summary.append("置信度: ").append(getConfidencePercentage());
        }
        
        return summary.toString();
    }
    
    @Override
    public String toString() {
        return "MedicalImageAnalysis{" +
                "imageType='" + imageType + '\'' +
                ", findings=" + (findings != null ? findings.size() + " items" : "null") +
                ", diagnosis=" + (diagnosis != null ? diagnosis.size() + " items" : "null") +
                ", recommendations=" + (recommendations != null ? recommendations.size() + " items" : "null") +
                ", severity='" + severity + '\'' +
                ", confidence=" + confidence +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        MedicalImageAnalysis that = (MedicalImageAnalysis) obj;
        
        if (Double.compare(that.confidence, confidence) != 0) return false;
        if (imageType != null ? !imageType.equals(that.imageType) : that.imageType != null) return false;
        if (findings != null ? !findings.equals(that.findings) : that.findings != null) return false;
        if (diagnosis != null ? !diagnosis.equals(that.diagnosis) : that.diagnosis != null) return false;
        if (recommendations != null ? !recommendations.equals(that.recommendations) : that.recommendations != null) return false;
        return severity != null ? severity.equals(that.severity) : that.severity == null;
    }
    
    @Override
    public int hashCode() {
        int result;
        long temp;
        result = imageType != null ? imageType.hashCode() : 0;
        result = 31 * result + (findings != null ? findings.hashCode() : 0);
        result = 31 * result + (diagnosis != null ? diagnosis.hashCode() : 0);
        result = 31 * result + (recommendations != null ? recommendations.hashCode() : 0);
        result = 31 * result + (severity != null ? severity.hashCode() : 0);
        temp = Double.doubleToLongBits(confidence);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}