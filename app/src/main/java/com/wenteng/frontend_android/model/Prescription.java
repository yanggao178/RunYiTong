package com.wenteng.frontend_android.model;

import java.util.Date;
import java.util.List;

/**
 * 处方数据模型
 * 用于表示用户的处方信息
 */
public class Prescription {
    private int id;
    private int userId;
    private int doctorId;
    private String doctorName;
    private String patientName;
    private String diagnosis;
    private Date prescriptionDate;
    private String status; // "active", "completed", "cancelled"
    private String notes;
    private List<PrescriptionItem> items;
    private double totalAmount;
    private String hospitalName;
    private String departmentName;

    /**
     * 默认构造函数
     */
    public Prescription() {
    }

    /**
     * 完整构造函数
     */
    public Prescription(int id, int userId, int doctorId, String doctorName, 
                       String patientName, String diagnosis, Date prescriptionDate, 
                       String status, String notes, List<PrescriptionItem> items, 
                       double totalAmount, String hospitalName, String departmentName) {
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

    /**
     * 获取状态显示文本
     */
    public String getStatusText() {
        switch (status) {
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
     * 检查处方是否有效
     */
    public boolean isActive() {
        return "active".equals(status);
    }
}