package com.wenteng.frontend_android.model;

import java.util.List;

public class PrescriptionAnalysis {
    private String ocr_text;
    private String analysis_type;
    private String syndrome_type;
    private String treatment_method;
    private String main_prescription;
    private List<HerbComposition> composition;
    private String usage;
    private String contraindications;
    private String confidence;
    private List<String> detected_herbs;
    private List<String> possible_symptoms;
    private List<String> recommendations;
    private String message;
    private String ai_error;
    private String error_details;
    
    // 内部类：药材组成
    public static class HerbComposition {
        private String 药材;
        private String 剂量;
        private String 角色;
        
        public HerbComposition() {}
        
        public HerbComposition(String 药材, String 剂量, String 角色) {
            this.药材 = 药材;
            this.剂量 = 剂量;
            this.角色 = 角色;
        }
        
        // Getter和Setter
        public String get药材() { return 药材; }
        public void set药材(String 药材) { this.药材 = 药材; }
        
        public String get剂量() { return 剂量; }
        public void set剂量(String 剂量) { this.剂量 = 剂量; }
        
        public String get角色() { return 角色; }
        public void set角色(String 角色) { this.角色 = 角色; }
        
        @Override
        public String toString() {
            return 药材 + " " + 剂量 + " (" + 角色 + ")";
        }
    }
    
    // 构造函数
    public PrescriptionAnalysis() {}
    
    // Getter和Setter方法
    public String getOcrText() {
        return ocr_text;
    }
    
    public void setOcrText(String ocr_text) {
        this.ocr_text = ocr_text;
    }
    
    public String getAnalysisType() {
        return analysis_type;
    }
    
    public void setAnalysisType(String analysis_type) {
        this.analysis_type = analysis_type;
    }
    
    public String getSyndromeType() {
        return syndrome_type;
    }
    
    public void setSyndromeType(String syndrome_type) {
        this.syndrome_type = syndrome_type;
    }
    
    public String getTreatmentMethod() {
        return treatment_method;
    }
    
    public void setTreatmentMethod(String treatment_method) {
        this.treatment_method = treatment_method;
    }
    
    public String getMainPrescription() {
        return main_prescription;
    }
    
    public void setMainPrescription(String main_prescription) {
        this.main_prescription = main_prescription;
    }
    
    public List<HerbComposition> getComposition() {
        return composition;
    }
    
    public void setComposition(List<HerbComposition> composition) {
        this.composition = composition;
    }
    
    public String getUsage() {
        return usage;
    }
    
    public void setUsage(String usage) {
        this.usage = usage;
    }
    
    public String getContraindications() {
        return contraindications;
    }
    
    public void setContraindications(String contraindications) {
        this.contraindications = contraindications;
    }
    
    public String getConfidence() {
        return confidence;
    }
    
    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }
    
    public List<String> getDetectedHerbs() {
        return detected_herbs;
    }
    
    public void setDetectedHerbs(List<String> detected_herbs) {
        this.detected_herbs = detected_herbs;
    }
    
    public List<String> getPossibleSymptoms() {
        return possible_symptoms;
    }
    
    public void setPossibleSymptoms(List<String> possible_symptoms) {
        this.possible_symptoms = possible_symptoms;
    }
    
    public List<String> getRecommendations() {
        return recommendations;
    }
    
    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getAiError() {
        return ai_error;
    }
    
    public void setAiError(String ai_error) {
        this.ai_error = ai_error;
    }
    
    public String getErrorDetails() {
        return error_details;
    }
    
    public void setErrorDetails(String error_details) {
        this.error_details = error_details;
    }
    
    @Override
    public String toString() {
        return "PrescriptionAnalysis{" +
                "ocr_text='" + ocr_text + '\'' +
                ", analysis_type='" + analysis_type + '\'' +
                ", syndrome_type='" + syndrome_type + '\'' +
                ", treatment_method='" + treatment_method + '\'' +
                ", main_prescription='" + main_prescription + '\'' +
                ", confidence='" + confidence + '\'' +
                '}';
    }
}