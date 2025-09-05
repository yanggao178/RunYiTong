package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SymptomAnalysis {
    @SerializedName("symptoms")
    private String symptoms;
    
    @SerializedName("analysis")
    private String analysis;
    
    @SerializedName("syndrome_type")
    private SyndromeType syndromeType;
    
    @SerializedName("treatment_method")
    private TreatmentMethod treatmentMethod;
    
    @SerializedName("main_prescription")
    private MainPrescription mainPrescription;
    
    @SerializedName("composition")
    private List<MedicineComposition> composition;
    
    @SerializedName("usage")
    private Usage usage;
    
    @SerializedName("contraindications")
    private Contraindications contraindications;
    
    @SerializedName("western_medicine")
    private WesternMedicine westernMedicine;
    
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
    
    public SyndromeType getSyndromeType() {
        return syndromeType;
    }
    
    public void setSyndromeType(SyndromeType syndromeType) {
        this.syndromeType = syndromeType;
    }
    
    public TreatmentMethod getTreatmentMethod() {
        return treatmentMethod;
    }
    
    public void setTreatmentMethod(TreatmentMethod treatmentMethod) {
        this.treatmentMethod = treatmentMethod;
    }
    
    public MainPrescription getMainPrescription() {
        return mainPrescription;
    }
    
    public void setMainPrescription(MainPrescription mainPrescription) {
        this.mainPrescription = mainPrescription;
    }
    
    public List<MedicineComposition> getComposition() {
        return composition;
    }
    
    public void setComposition(List<MedicineComposition> composition) {
        this.composition = composition;
    }
    
    public Usage getUsage() {
        return usage;
    }
    
    public void setUsage(Usage usage) {
        this.usage = usage;
    }
    
    public Contraindications getContraindications() {
        return contraindications;
    }
    
    public void setContraindications(Contraindications contraindications) {
        this.contraindications = contraindications;
    }
    
    public WesternMedicine getWesternMedicine() {
        return westernMedicine;
    }
    
    public void setWesternMedicine(WesternMedicine westernMedicine) {
        this.westernMedicine = westernMedicine;
    }
    
    // 内部类：辨证分型
    public static class SyndromeType {
        @SerializedName("main_syndrome")
        private String mainSyndrome;
        @SerializedName("secondary_syndrome")
        private String secondarySyndrome;
        @SerializedName("disease_location")
        private String diseaseLocation;
        @SerializedName("disease_nature")
        private String diseaseNature;
        @SerializedName("pathogenesis")
        private String pathogenesis;
        
        public SyndromeType() {}
        
        public String getMainSyndrome() { return mainSyndrome; }
        public void setMainSyndrome(String mainSyndrome) { this.mainSyndrome = mainSyndrome; }
        public String getSecondarySyndrome() { return secondarySyndrome; }
        public void setSecondarySyndrome(String secondarySyndrome) { this.secondarySyndrome = secondarySyndrome; }
        public String getDiseaseLocation() { return diseaseLocation; }
        public void setDiseaseLocation(String diseaseLocation) { this.diseaseLocation = diseaseLocation; }
        public String getDiseaseNature() { return diseaseNature; }
        public void setDiseaseNature(String diseaseNature) { this.diseaseNature = diseaseNature; }
        public String getPathogenesis() { return pathogenesis; }
        public void setPathogenesis(String pathogenesis) { this.pathogenesis = pathogenesis; }
    }
    
    // 内部类：治疗方法
    public static class TreatmentMethod {
        @SerializedName("main_method")
        private String mainMethod;
        @SerializedName("auxiliary_method")
        private String auxiliaryMethod;
        @SerializedName("treatment_priority")
        private String treatmentPriority;
        @SerializedName("care_principle")
        private String carePrinciple;
        
        public TreatmentMethod() {}
        
        public String getMainMethod() { return mainMethod; }
        public void setMainMethod(String mainMethod) { this.mainMethod = mainMethod; }
        public String getAuxiliaryMethod() { return auxiliaryMethod; }
        public void setAuxiliaryMethod(String auxiliaryMethod) { this.auxiliaryMethod = auxiliaryMethod; }
        public String getTreatmentPriority() { return treatmentPriority; }
        public void setTreatmentPriority(String treatmentPriority) { this.treatmentPriority = treatmentPriority; }
        public String getCarePrinciple() { return carePrinciple; }
        public void setCarePrinciple(String carePrinciple) { this.carePrinciple = carePrinciple; }
    }
    
    // 内部类：主方
    public static class MainPrescription {
        @SerializedName("formula_name")
        private String formulaName;
        @SerializedName("formula_source")
        private String formulaSource;
        @SerializedName("formula_analysis")
        private String formulaAnalysis;
        @SerializedName("modifications")
        private String modifications;
        
        public MainPrescription() {}
        
        public String getFormulaName() { return formulaName; }
        public void setFormulaName(String formulaName) { this.formulaName = formulaName; }
        public String getFormulaSource() { return formulaSource; }
        public void setFormulaSource(String formulaSource) { this.formulaSource = formulaSource; }
        public String getFormulaAnalysis() { return formulaAnalysis; }
        public void setFormulaAnalysis(String formulaAnalysis) { this.formulaAnalysis = formulaAnalysis; }
        public String getModifications() { return modifications; }
        public void setModifications(String modifications) { this.modifications = modifications; }
    }
    
    // 内部类：用法
    public static class Usage {
        @SerializedName("preparation_method")
        private String preparationMethod;
        @SerializedName("administration_time")
        private String administrationTime;
        @SerializedName("treatment_course")
        private String treatmentCourse;
        
        public Usage() {}
        
        public String getPreparationMethod() { return preparationMethod; }
        public void setPreparationMethod(String preparationMethod) { this.preparationMethod = preparationMethod; }
        public String getAdministrationTime() { return administrationTime; }
        public void setAdministrationTime(String administrationTime) { this.administrationTime = administrationTime; }
        public String getTreatmentCourse() { return treatmentCourse; }
        public void setTreatmentCourse(String treatmentCourse) { this.treatmentCourse = treatmentCourse; }
    }
    
    // 内部类：禁忌
    public static class Contraindications {
        @SerializedName("contraindications")
        private String contraindications;
        @SerializedName("dietary_restrictions")
        private String dietaryRestrictions;
        @SerializedName("lifestyle_care")
        private String lifestyleCare;
        @SerializedName("precautions")
        private String precautions;
        
        public Contraindications() {}
        
        public String getContraindications() { return contraindications; }
        public void setContraindications(String contraindications) { this.contraindications = contraindications; }
        public String getDietaryRestrictions() { return dietaryRestrictions; }
        public void setDietaryRestrictions(String dietaryRestrictions) { this.dietaryRestrictions = dietaryRestrictions; }
        public String getLifestyleCare() { return lifestyleCare; }
        public void setLifestyleCare(String lifestyleCare) { this.lifestyleCare = lifestyleCare; }
        public String getPrecautions() { return precautions; }
        public void setPrecautions(String precautions) { this.precautions = precautions; }
    }
    
    // 内部类：药材组成
    public static class MedicineComposition {
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
        
        public MedicineComposition() {}
        
        public String getHerb() {
            return herb;
        }
        
        public void setHerb(String herb) {
            this.herb = herb;
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
        
        public String getFunction() {
            return function;
        }
        
        public void setFunction(String function) {
            this.function = function;
        }
        
        public String getPreparation() {
            return preparation;
        }
        
        public void setPreparation(String preparation) {
            this.preparation = preparation;
        }
    }
    
    // 内部类：西医诊疗
    public static class WesternMedicine {
        @SerializedName("diagnosis")
        private Diagnosis diagnosis;
        @SerializedName("treatment")
        private Treatment treatment;
        @SerializedName("medication")
        private Medication medication;
        
        public WesternMedicine() {}
        
        public Diagnosis getDiagnosis() { return diagnosis; }
        public void setDiagnosis(Diagnosis diagnosis) { this.diagnosis = diagnosis; }
        public Treatment getTreatment() { return treatment; }
        public void setTreatment(Treatment treatment) { this.treatment = treatment; }
        public Medication getMedication() { return medication; }
        public void setMedication(Medication medication) { this.medication = medication; }
    }
    
    // 内部类：西医诊断
    public static class Diagnosis {
        @SerializedName("possible_diagnosis")
        private String possibleDiagnosis;
        @SerializedName("differential_diagnosis")
        private String differentialDiagnosis;
        @SerializedName("recommended_tests")
        private String recommendedTests;
        @SerializedName("pathological_mechanism")
        private String pathologicalMechanism;
        
        public Diagnosis() {}
        
        public String getPossibleDiagnosis() { return possibleDiagnosis; }
        public void setPossibleDiagnosis(String possibleDiagnosis) { this.possibleDiagnosis = possibleDiagnosis; }
        public String getDifferentialDiagnosis() { return differentialDiagnosis; }
        public void setDifferentialDiagnosis(String differentialDiagnosis) { this.differentialDiagnosis = differentialDiagnosis; }
        public String getRecommendedTests() { return recommendedTests; }
        public void setRecommendedTests(String recommendedTests) { this.recommendedTests = recommendedTests; }
        public String getPathologicalMechanism() { return pathologicalMechanism; }
        public void setPathologicalMechanism(String pathologicalMechanism) { this.pathologicalMechanism = pathologicalMechanism; }
    }
    
    // 内部类：西医治疗
    public static class Treatment {
        @SerializedName("drug_therapy")
        private String drugTherapy;
        @SerializedName("non_drug_therapy")
        private String nonDrugTherapy;
        @SerializedName("lifestyle_intervention")
        private String lifestyleIntervention;
        @SerializedName("prevention_measures")
        private String preventionMeasures;
        
        public Treatment() {}
        
        public String getDrugTherapy() { return drugTherapy; }
        public void setDrugTherapy(String drugTherapy) { this.drugTherapy = drugTherapy; }
        public String getNonDrugTherapy() { return nonDrugTherapy; }
        public void setNonDrugTherapy(String nonDrugTherapy) { this.nonDrugTherapy = nonDrugTherapy; }
        public String getLifestyleIntervention() { return lifestyleIntervention; }
        public void setLifestyleIntervention(String lifestyleIntervention) { this.lifestyleIntervention = lifestyleIntervention; }
        public String getPreventionMeasures() { return preventionMeasures; }
        public void setPreventionMeasures(String preventionMeasures) { this.preventionMeasures = preventionMeasures; }
    }
    
    // 内部类：西医用药
    public static class Medication {
        @SerializedName("drug_selection")
        private String drugSelection;
        @SerializedName("administration_method")
        private String administrationMethod;
        @SerializedName("adverse_reactions")
        private String adverseReactions;
        @SerializedName("drug_interactions")
        private String drugInteractions;
        
        public Medication() {}
        
        public String getDrugSelection() { return drugSelection; }
        public void setDrugSelection(String drugSelection) { this.drugSelection = drugSelection; }
        public String getAdministrationMethod() { return administrationMethod; }
        public void setAdministrationMethod(String administrationMethod) { this.administrationMethod = administrationMethod; }
        public String getAdverseReactions() { return adverseReactions; }
        public void setAdverseReactions(String adverseReactions) { this.adverseReactions = adverseReactions; }
        public String getDrugInteractions() { return drugInteractions; }
        public void setDrugInteractions(String drugInteractions) { this.drugInteractions = drugInteractions; }
    }
}