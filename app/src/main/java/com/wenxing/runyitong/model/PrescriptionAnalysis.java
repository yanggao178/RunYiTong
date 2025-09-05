package com.wenxing.runyitong.model;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class PrescriptionAnalysis {
    @SerializedName("ocr_text")
    private String ocr_text;
    @SerializedName("analysis_type")
    private String analysis_type;
    @SerializedName("syndrome_type")
    private SyndromeType syndrome_type;
    @SerializedName("treatment_method")
    private TreatmentMethod treatment_method;
    @SerializedName("main_prescription")
    private MainPrescription main_prescription;
    @SerializedName("composition")
    private List<HerbComposition> composition;
    @SerializedName("usage")
    private String usage;
    @SerializedName("contraindications")
    private String contraindications;
    @SerializedName("confidence")
    private String confidence;
    @SerializedName("detected_herbs")
    private List<String> detected_herbs;
    @SerializedName("possible_symptoms")
    private List<String> possible_symptoms;
    @SerializedName("recommendations")
    private List<String> recommendations;
    @SerializedName("message")
    private String message;
    @SerializedName("ai_error")
    private String ai_error;
    @SerializedName("error_details")
    private String error_details;
    @SerializedName("error_code")
    private String error_code;
    @SerializedName("image_type")
    private String image_type;
    @SerializedName("findings")
    private List<String> findings;
    @SerializedName("diagnosis")
    private String diagnosis;
    
    // 医学影像分析相关字段
    @SerializedName("analysis_result")
    private String analysisResult;
    
    @SerializedName("confidence_score")
    private double confidenceScore;
    
    @SerializedName("analysis_timestamp")
    private String analysisTimestamp;
    
    // 后端医学影像分析返回的字段
    @SerializedName("image_quality")
    private String imageQuality;
    
    @SerializedName("main_findings")
    private List<String> mainFindings;
    
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("data")
    private Object data;
    
    // 内部类：辨证分型
    public static class SyndromeType {
        @SerializedName("main_syndrome")
        private String main_syndrome;
        @SerializedName("secondary_syndrome")
        private String secondary_syndrome;
        @SerializedName("disease_location")
        private String disease_location;
        @SerializedName("disease_nature")
        private String disease_nature;
        @SerializedName("pathogenesis")
        private String pathogenesis;
        
        public SyndromeType() {}
        
        // Getter和Setter
        public String getMainSyndrome() { return main_syndrome; }
        public void setMainSyndrome(String main_syndrome) { this.main_syndrome = main_syndrome; }
        
        public String getSecondarySyndrome() { return secondary_syndrome; }
        public void setSecondarySyndrome(String secondary_syndrome) { this.secondary_syndrome = secondary_syndrome; }
        
        public String getDiseaseLocation() { return disease_location; }
        public void setDiseaseLocation(String disease_location) { this.disease_location = disease_location; }
        
        public String getDiseaseNature() { return disease_nature; }
        public void setDiseaseNature(String disease_nature) { this.disease_nature = disease_nature; }
        
        public String getPathogenesis() { return pathogenesis; }
        public void setPathogenesis(String pathogenesis) { this.pathogenesis = pathogenesis; }
    }
    
    // 内部类：治疗方法
    public static class TreatmentMethod {
        @SerializedName("main_method")
        private String main_method;
        @SerializedName("auxiliary_method")
        private String auxiliary_method;
        @SerializedName("treatment_priority")
        private String treatment_priority;
        @SerializedName("care_principle")
        private String care_principle;
        
        public TreatmentMethod() {}
        
        // Getter和Setter
        public String getMainMethod() { return main_method; }
        public void setMainMethod(String main_method) { this.main_method = main_method; }
        
        public String getAuxiliaryMethod() { return auxiliary_method; }
        public void setAuxiliaryMethod(String auxiliary_method) { this.auxiliary_method = auxiliary_method; }
        
        public String getTreatmentPriority() { return treatment_priority; }
        public void setTreatmentPriority(String treatment_priority) { this.treatment_priority = treatment_priority; }
        
        public String getCarePrinciple() { return care_principle; }
        public void setCarePrinciple(String care_principle) { this.care_principle = care_principle; }
    }
    
    // 内部类：主方信息
    public static class MainPrescription {
        @SerializedName("prescription_name")
        private String formula_name;
        @SerializedName("source")
        private String formula_source;
        @SerializedName("formula_analysis")
        private String formula_analysis;
        @SerializedName("modification")
        private String modifications;
        
        public MainPrescription() {}
        
        // Getter和Setter
        public String getFormulaName() { return formula_name; }
        public void setFormulaName(String formula_name) { this.formula_name = formula_name; }
        
        public String getFormulaSource() { return formula_source; }
        public void setFormulaSource(String formula_source) { this.formula_source = formula_source; }
        
        public String getFormulaAnalysis() { return formula_analysis; }
        public void setFormulaAnalysis(String formula_analysis) { this.formula_analysis = formula_analysis; }
        
        public String getModifications() { return modifications; }
        public void setModifications(String modifications) { this.modifications = modifications; }
    }
    
    // 内部类：用法用量
    public static class Usage {
        @SerializedName("preparation_method")
        private String preparation_method;
        @SerializedName("administration_time")
        private String administration_time;
        @SerializedName("dietary_restrictions")
        private String dietary_restrictions;
        @SerializedName("lifestyle_care")
        private String lifestyle_care;
        
        public Usage() {}
        
        // Getter和Setter
        public String getPreparationMethod() { return preparation_method; }
        public void setPreparationMethod(String preparation_method) { this.preparation_method = preparation_method; }
        
        public String getAdministrationTime() { return administration_time; }
        public void setAdministrationTime(String administration_time) { this.administration_time = administration_time; }
        
        public String getDietaryRestrictions() { return dietary_restrictions; }
        public void setDietaryRestrictions(String dietary_restrictions) { this.dietary_restrictions = dietary_restrictions; }
        
        public String getLifestyleCare() { return lifestyle_care; }
        public void setLifestyleCare(String lifestyle_care) { this.lifestyle_care = lifestyle_care; }
    }
    
    // 内部类：禁忌注意事项
    public static class Contraindications {
        @SerializedName("contraindications")
        private String contraindications;
        @SerializedName("dietary_restrictions")
        private String dietary_restrictions;
        @SerializedName("lifestyle_care")
        private String lifestyle_care;
        @SerializedName("precautions")
        private String precautions;
        
        public Contraindications() {}
        
        // Getter和Setter
        public String getContraindications() { return contraindications; }
        public void setContraindications(String contraindications) { this.contraindications = contraindications; }
        
        public String getDietaryRestrictions() { return dietary_restrictions; }
        public void setDietaryRestrictions(String dietary_restrictions) { this.dietary_restrictions = dietary_restrictions; }
        
        public String getLifestyleCare() { return lifestyle_care; }
        public void setLifestyleCare(String lifestyle_care) { this.lifestyle_care = lifestyle_care; }
        
        public String getPrecautions() { return precautions; }
        public void setPrecautions(String precautions) { this.precautions = precautions; }
    }

    // 内部类：药材组成
    public static class HerbComposition {
        @SerializedName("herb")
        private String herb;
        @SerializedName("dosage")
        private String dosage;
        @SerializedName("role")
        private String role;
        @SerializedName("function")
        private String function;
        @SerializedName("preparation")
        private String preparation;
        
        public HerbComposition() {}
        
        public HerbComposition(String herb, String dosage, String role) {
            this.herb = herb;
            this.dosage = dosage;
            this.role = role;
        }
        
        // Getter和Setter
        public String getHerb() { return herb; }
        public void setHerb(String herb) { this.herb = herb; }
        
        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getFunction() { return function; }
        public void setFunction(String function) { this.function = function; }
        
        public String getPreparation() { return preparation; }
        public void setPreparation(String preparation) { this.preparation = preparation; }
        
        @Override
        public String toString() {
            return herb + " " + dosage + " (" + role + ")";
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
    
    public SyndromeType getSyndromeType() {
        return syndrome_type;
    }
    
    public void setSyndromeType(SyndromeType syndrome_type) {
        this.syndrome_type = syndrome_type;
    }
    
    public TreatmentMethod getTreatmentMethod() {
        return treatment_method;
    }
    
    public void setTreatmentMethod(TreatmentMethod treatment_method) {
        this.treatment_method = treatment_method;
    }
    
    public MainPrescription getMainPrescription() {
        return main_prescription;
    }
    
    public void setMainPrescription(MainPrescription main_prescription) {
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
    
    public String getErrorCode() {
        return error_code;
    }
    
    public void setErrorCode(String error_code) {
        this.error_code = error_code;
    }
    
    public String getImageType() {
        return image_type;
    }
    
    public void setImageType(String image_type) {
        this.image_type = image_type;
    }
    
    public List<String> getFindings() {
        return findings;
    }
    
    public void setFindings(List<String> findings) {
        this.findings = findings;
    }
    
    public String getDiagnosis() {
        return diagnosis;
    }
    
    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }
    
    // 医学影像分析相关字段的getter和setter
    public String getAnalysisResult() {
        return analysisResult;
    }
    
    public void setAnalysisResult(String analysisResult) {
        this.analysisResult = analysisResult;
    }
    
    public double getConfidenceScore() {
        return confidenceScore;
    }
    
    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
    
    public String getAnalysisTimestamp() {
        return analysisTimestamp;
    }
    
    public void setAnalysisTimestamp(String analysisTimestamp) {
        this.analysisTimestamp = analysisTimestamp;
    }
    
    // 新增字段的getter和setter方法
    public String getImageQuality() {
        return imageQuality;
    }
    
    public void setImageQuality(String imageQuality) {
        this.imageQuality = imageQuality;
    }
    
    public List<String> getMainFindings() {
        return mainFindings;
    }
    
    public void setMainFindings(List<String> mainFindings) {
        this.mainFindings = mainFindings;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
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