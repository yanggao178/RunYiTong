package com.wenteng.frontend_android.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SymptomAnalysis {
    @SerializedName("symptoms")
    private String symptoms;
    
    @SerializedName("analysis")
    private String analysis;
    
    @SerializedName("syndrome_type")
    private String syndromeType;
    
    @SerializedName("treatment_method")
    private String treatmentMethod;
    
    @SerializedName("main_prescription")
    private String mainPrescription;
    
    @SerializedName("composition")
    private List<MedicineComposition> composition;
    
    @SerializedName("usage")
    private String usage;
    
    @SerializedName("contraindications")
    private String contraindications;
    
    // 构造函数
    public SymptomAnalysis() {}
    
    // Getter和Setter方法
    public String getSymptoms() {
        return symptoms;
    }
    
    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }
    
    public String getAnalysis() {
        return analysis;
    }
    
    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }
    
    public String getSyndromeType() {
        return syndromeType;
    }
    
    public void setSyndromeType(String syndromeType) {
        this.syndromeType = syndromeType;
    }
    
    public String getTreatmentMethod() {
        return treatmentMethod;
    }
    
    public void setTreatmentMethod(String treatmentMethod) {
        this.treatmentMethod = treatmentMethod;
    }
    
    public String getMainPrescription() {
        return mainPrescription;
    }
    
    public void setMainPrescription(String mainPrescription) {
        this.mainPrescription = mainPrescription;
    }
    
    public List<MedicineComposition> getComposition() {
        return composition;
    }
    
    public void setComposition(List<MedicineComposition> composition) {
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
    
    // 内部类：药材组成
    public static class MedicineComposition {
        @SerializedName("药材")
        private String medicine;
        
        @SerializedName("剂量")
        private String dosage;
        
        @SerializedName("角色")
        private String role;
        
        public MedicineComposition() {}
        
        public String getMedicine() {
            return medicine;
        }
        
        public void setMedicine(String medicine) {
            this.medicine = medicine;
        }
        
        public String getDosage() {
            return dosage;
        }
        
        public void setDosage(String dosage) {
            this.dosage = dosage;
        }
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
    }
}