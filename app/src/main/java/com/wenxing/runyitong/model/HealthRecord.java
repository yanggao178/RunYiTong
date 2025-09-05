package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 健康档案模型类
 * 用于表示用户的个人健康信息档案
 */
public class HealthRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    @SerializedName("id")
    private int id;
    
    @SerializedName("user_id")
    private int userId;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("gender")
    private String gender;
    
    @SerializedName("birthdate")
    private String birthdate;
    
    @SerializedName("height")
    private double height;
    
    @SerializedName("weight")
    private double weight;
    
    @SerializedName("blood_type")
    private String bloodType;
    
    @SerializedName("allergies")
    private String allergies;
    
    @SerializedName("chronic_diseases")
    private String chronicDiseases;
    
    @SerializedName("medications")
    private String medications;
    
    @SerializedName("family_history")
    private String familyHistory;
    
    @SerializedName("emergency_contact_name")
    private String emergencyContactName;
    
    @SerializedName("emergency_contact_phone")
    private String emergencyContactPhone;
    
    @SerializedName("created_at")
    private Date createdAt;
    
    @SerializedName("updated_at")
    private Date updatedAt;
    
    @SerializedName("physical_exams")
    private List<PhysicalExamReport> physicalExamReports;

    // 构造函数、getter和setter方法
    public HealthRecord() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getChronicDiseases() {
        return chronicDiseases;
    }

    public void setChronicDiseases(String chronicDiseases) {
        this.chronicDiseases = chronicDiseases;
    }

    public String getMedications() {
        return medications;
    }

    public void setMedications(String medications) {
        this.medications = medications;
    }

    public String getFamilyHistory() {
        return familyHistory;
    }

    public void setFamilyHistory(String familyHistory) {
        this.familyHistory = familyHistory;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<PhysicalExamReport> getPhysicalExamReports() {
        return physicalExamReports;
    }

    public void setPhysicalExamReports(List<PhysicalExamReport> physicalExamReports) {
        this.physicalExamReports = physicalExamReports;
    }

    @Override
    public String toString() {
        return "HealthRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", birthdate='" + birthdate + '\'' +
                ", height=" + height +
                ", weight=" + weight +
                ", bloodType='" + bloodType + '\'' +
                ", allergies='" + allergies + '\'' +
                ", chronicDiseases='" + chronicDiseases + '\'' +
                ", medications='" + medications + '\'' +
                ", familyHistory='" + familyHistory + '\'' +
                ", emergencyContactName='" + emergencyContactName + '\'' +
                ", emergencyContactPhone='" + emergencyContactPhone + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", physicalExamReports=" + physicalExamReports +
                '}';
    }
}