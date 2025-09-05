package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Appointment implements Serializable {
    @SerializedName("id")
    private int id; // 预约ID
    
    @SerializedName("patient_name")
    private String patientName; // 患者姓名
    
    @SerializedName("patient_phone")
    private String patientPhone; // 患者电话
    
    @SerializedName("patient_id_card")
    private String patientIdCard; // 患者身份证号
    
    @SerializedName("doctor_id")
    private int doctorId; // 医生ID
    
    @SerializedName("doctor_name")
    private String doctorName; // 医生姓名
    
    @SerializedName("hospital_id")
    private int hospitalId; // 医院ID
    
    @SerializedName("hospital_name")
    private String hospitalName; // 医院名称
    
    @SerializedName("department_id")
    private int departmentId; // 科室ID
    
    @SerializedName("department_name")
    private String departmentName; // 科室名称
    
    @SerializedName("appointment_date")
    private String appointmentDate; // 预约日期
    
    @SerializedName("appointment_time")
    private String appointmentTime; // 预约时间段
    
    @SerializedName("symptoms")
    private String symptoms; // 症状描述
    
    @SerializedName("status")
    private String status; // 预约状态（待确认、已确认、已完成、已取消）
    
    @SerializedName("created_at")
    private String createdAt; // 创建时间
    
    @SerializedName("updated_at")
    private String updatedAt; // 更新时间
    
    // 无参构造函数
    public Appointment() {
    }
    
    // 有参构造函数
    public Appointment(int id, String patientName, String patientPhone, String patientIdCard,
                      int doctorId, String doctorName, int hospitalId, String hospitalName,
                      int departmentId, String departmentName, String appointmentDate,
                      String appointmentTime, String symptoms, String status,
                      String createdAt, String updatedAt) {
        this.id = id;
        this.patientName = patientName;
        this.patientPhone = patientPhone;
        this.patientIdCard = patientIdCard;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.symptoms = symptoms;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getter和Setter方法
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getPatientName() {
        return patientName;
    }
    
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
    
    public String getPatientPhone() {
        return patientPhone;
    }
    
    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }
    
    public String getPatientIdCard() {
        return patientIdCard;
    }
    
    public void setPatientIdCard(String patientIdCard) {
        this.patientIdCard = patientIdCard;
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
    
    public int getHospitalId() {
        return hospitalId;
    }
    
    public void setHospitalId(int hospitalId) {
        this.hospitalId = hospitalId;
    }
    
    public String getHospitalName() {
        return hospitalName;
    }
    
    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }
    
    public int getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }
    
    public String getDepartmentName() {
        return departmentName;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
    
    public String getAppointmentDate() {
        return appointmentDate;
    }
    
    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }
    
    public String getAppointmentTime() {
        return appointmentTime;
    }
    
    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }
    
    public String getSymptoms() {
        return symptoms;
    }
    
    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", patientName='" + patientName + '\'' +
                ", patientPhone='" + patientPhone + '\'' +
                ", patientIdCard='" + patientIdCard + '\'' +
                ", doctorId=" + doctorId +
                ", doctorName='" + doctorName + '\'' +
                ", hospitalId=" + hospitalId +
                ", hospitalName='" + hospitalName + '\'' +
                ", departmentId=" + departmentId +
                ", departmentName='" + departmentName + '\'' +
                ", appointmentDate='" + appointmentDate + '\'' +
                ", appointmentTime='" + appointmentTime + '\'' +
                ", symptoms='" + symptoms + '\'' +
                ", status='" + status + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}