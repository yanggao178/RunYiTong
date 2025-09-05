package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

/**
 * 处方数据模型
 * 用于表示用户的处方信息
 */
public class Prescription {
    @SerializedName("id")
    private int id;
    
    @SerializedName("user_id")
    private int userId;
    
    @SerializedName("symptoms")
    private String symptoms;
    
    @SerializedName("diagnosis")
    private String diagnosis;
    
    @SerializedName("prescription_content")
    private String prescriptionContent;
    
    @SerializedName("doctor_name")
    private String doctorName;
    
    @SerializedName("status")
    private String status; // "draft", "issued", "dispensed"
    
    @SerializedName("image_url")
    private String imageUrl;
    
    @SerializedName("created_time")
    private Date createdTime;
    
    @SerializedName("updated_time")
    private Date updatedTime;
    
    // 以下字段为Android端扩展字段，可能不在后端API中
    @SerializedName("doctor_id")
    private int doctorId;
    
    @SerializedName("patient_name")
    private String patientName;
    
    @SerializedName("prescription_date")
    private Date prescriptionDate;
    
    @SerializedName("notes")
    private String notes;
    
    @SerializedName("items")
    private List<PrescriptionItem> items;
    
    @SerializedName("total_amount")
    private double totalAmount;
    
    @SerializedName("hospital_name")
    private String hospitalName;
    
    @SerializedName("department_name")
    private String departmentName;

    /**
     * 默认构造函数
     */
    public Prescription() {
    }

    /**
     * 主要字段构造函数（基于后端API字段）
     */
    public Prescription(int id, int userId, String symptoms, String diagnosis, 
                       String prescriptionContent, String doctorName, String status, 
                       String imageUrl, Date createdTime, Date updatedTime) {
        this.id = id;
        this.userId = userId;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
        this.prescriptionContent = prescriptionContent;
        this.doctorName = doctorName;
        this.status = status;
        this.imageUrl = imageUrl;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }

    /**
     * 完整构造函数（包含所有字段，主要用于向后兼容）
     */
    public Prescription(int id, int userId, int doctorId, String doctorName, 
                       String patientName, String diagnosis, Date prescriptionDate, 
                       String status, String notes, List<PrescriptionItem> items, 
                       double totalAmount, String hospitalName, String departmentName,
                       String symptoms, String prescriptionContent, String imageUrl,
                       Date createdTime, Date updatedTime) {
        this.id = id;
        this.userId = userId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.patientName = patientName;
        this.diagnosis = diagnosis;
        this.prescriptionDate = prescriptionDate;
        this.status = status;
        this.notes = notes;
        this.items = items;
        this.totalAmount = totalAmount;
        this.hospitalName = hospitalName;
        this.departmentName = departmentName;
        // 新增的后端字段
        this.symptoms = symptoms;
        this.prescriptionContent = prescriptionContent;
        this.imageUrl = imageUrl;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }

    // Getter和Setter方法
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

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public Date getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(Date prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<PrescriptionItem> getItems() {
        return items;
    }

    public void setItems(List<PrescriptionItem> items) {
        this.items = items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    // 新增字段的getter和setter方法
    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getPrescriptionContent() {
        return prescriptionContent;
    }

    public void setPrescriptionContent(String prescriptionContent) {
        this.prescriptionContent = prescriptionContent;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    /**
     * 获取状态显示文本
     */
    public String getStatusText() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case "draft":
                return "草稿";
            case "issued":
                return "已开具";
            case "dispensed":
                return "已配药";
            // 向后兼容旧状态值
            case "active":
                return "有效";
            case "completed":
                return "已完成";
            case "cancelled":
                return "已取消";
            default:
                return "未知";
        }
    }

    /**
     * 检查处方是否有效（已开具或已配药）
     */
    public boolean isActive() {
        return "issued".equals(status) || "dispensed".equals(status) || "active".equals(status);
    }

    /**
     * 检查处方是否为草稿状态
     */
    public boolean isDraft() {
        return "draft".equals(status);
    }
}