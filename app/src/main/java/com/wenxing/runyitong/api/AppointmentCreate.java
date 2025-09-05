package com.wenxing.runyitong.api;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * 创建预约请求模型类
 */
public class AppointmentCreate implements Serializable {
    @SerializedName("patient_name")
    private String patientName; // 患者姓名
    
    @SerializedName("patient_phone")
    private String patientPhone; // 患者电话
    
    @SerializedName("patient_id_card")
    private String patientIdCard; // 患者身份证号
    
    @SerializedName("doctor_id")
    private int doctorId; // 医生ID
    
    @SerializedName("hospital_id")
    private int hospitalId; // 医院ID
    
    @SerializedName("department_id")
    private int departmentId; // 科室ID
    
    @SerializedName("appointment_date")
    private String appointmentDate; // 预约日期
    
    @SerializedName("appointment_time")
    private String appointmentTime; // 预约时间段
    
    @SerializedName("symptoms")
    private String symptoms; // 症状描述
    
    /**
     * 无参构造函数
     */
    public AppointmentCreate() {}
    
    /**
     * 有参构造函数
     * @param patientName 患者姓名
     * @param patientPhone 患者电话
     * @param patientIdCard 患者身份证号
     * @param doctorId 医生ID
     * @param hospitalId 医院ID
     * @param departmentId 科室ID
     * @param appointmentDate 预约日期
     * @param appointmentTime 预约时间段
     * @param symptoms 症状描述
     */
    public AppointmentCreate(String patientName, String patientPhone, String patientIdCard,
                           int doctorId, int hospitalId, int departmentId,
                           String appointmentDate, String appointmentTime, String symptoms) {
        this.patientName = patientName;
        this.patientPhone = patientPhone;
        this.patientIdCard = patientIdCard;
        this.doctorId = doctorId;
        this.hospitalId = hospitalId;
        this.departmentId = departmentId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.symptoms = symptoms;
    }
    
    /**
     * 获取患者姓名
     * @return 患者姓名
     */
    public String getPatientName() {
        return patientName;
    }
    
    /**
     * 设置患者姓名
     * @param patientName 患者姓名
     */
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
    
    /**
     * 获取患者电话
     * @return 患者电话
     */
    public String getPatientPhone() {
        return patientPhone;
    }
    
    /**
     * 设置患者电话
     * @param patientPhone 患者电话
     */
    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }
    
    /**
     * 获取患者身份证号
     * @return 患者身份证号
     */
    public String getPatientIdCard() {
        return patientIdCard;
    }
    
    /**
     * 设置患者身份证号
     * @param patientIdCard 患者身份证号
     */
    public void setPatientIdCard(String patientIdCard) {
        this.patientIdCard = patientIdCard;
    }
    
    /**
     * 获取医生ID
     * @return 医生ID
     */
    public int getDoctorId() {
        return doctorId;
    }
    
    /**
     * 设置医生ID
     * @param doctorId 医生ID
     */
    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }
    
    /**
     * 获取医院ID
     * @return 医院ID
     */
    public int getHospitalId() {
        return hospitalId;
    }
    
    /**
     * 设置医院ID
     * @param hospitalId 医院ID
     */
    public void setHospitalId(int hospitalId) {
        this.hospitalId = hospitalId;
    }
    
    /**
     * 获取科室ID
     * @return 科室ID
     */
    public int getDepartmentId() {
        return departmentId;
    }
    
    /**
     * 设置科室ID
     * @param departmentId 科室ID
     */
    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }
    
    /**
     * 获取预约日期
     * @return 预约日期
     */
    public String getAppointmentDate() {
        return appointmentDate;
    }
    
    /**
     * 设置预约日期
     * @param appointmentDate 预约日期
     */
    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }
    
    /**
     * 获取预约时间段
     * @return 预约时间段
     */
    public String getAppointmentTime() {
        return appointmentTime;
    }
    
    /**
     * 设置预约时间段
     * @param appointmentTime 预约时间段
     */
    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }
    
    /**
     * 获取症状描述
     * @return 症状描述
     */
    public String getSymptoms() {
        return symptoms;
    }
    
    /**
     * 设置症状描述
     * @param symptoms 症状描述
     */
    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }
    
    @Override
    public String toString() {
        return "AppointmentCreate{" +
                "patientName='" + patientName + '\'' +
                ", patientPhone='" + patientPhone + '\'' +
                ", patientIdCard='" + patientIdCard + '\'' +
                ", doctorId=" + doctorId +
                ", hospitalId=" + hospitalId +
                ", departmentId=" + departmentId +
                ", appointmentDate='" + appointmentDate + '\'' +
                ", appointmentTime='" + appointmentTime + '\'' +
                ", symptoms='" + symptoms + '\'' +
                '}';
    }
}