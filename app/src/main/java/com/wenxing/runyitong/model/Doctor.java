package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Doctor implements Serializable {
    @SerializedName("id")
    private int id; // 医生ID
    
    @SerializedName("name")
    private String name; // 医生姓名
    
    @SerializedName("title")
    private String title; // 职称（如：主任医师、副主任医师）
    
    @SerializedName("department_id")
    private int departmentId; // 所属科室ID
    
    @SerializedName("department_name")
    private String departmentName; // 所属科室名称
    
    @SerializedName("hospital_id")
    private int hospitalId; // 所属医院ID
    
    @SerializedName("hospital_name")
    private String hospitalName; // 所属医院名称
    
    @SerializedName("specialties")
    private List<String> specialties; // 专业特长列表
    
    @SerializedName("experience_years")
    private int experienceYears; // 从业年限
    
    @SerializedName("education")
    private String education; // 教育背景
    
    @SerializedName("introduction")
    private String introduction; // 个人简介
    
    @SerializedName("available_times")
    private List<String> availableTimes; // 可预约时间段
    
    // 无参构造函数
    public Doctor() {
    }
    
    // 有参构造函数
    public Doctor(int id, String name, String title, int departmentId, String departmentName,
                 int hospitalId, String hospitalName, List<String> specialties, 
                 int experienceYears, String education, String introduction, 
                 List<String> availableTimes) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.specialties = specialties;
        this.experienceYears = experienceYears;
        this.education = education;
        this.introduction = introduction;
        this.availableTimes = availableTimes;
    }
    
    // Getter和Setter方法
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
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
    
    public List<String> getSpecialties() {
        return specialties;
    }
    
    public void setSpecialties(List<String> specialties) {
        this.specialties = specialties;
    }
    
    public int getExperienceYears() {
        return experienceYears;
    }
    
    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }
    
    public String getEducation() {
        return education;
    }
    
    public void setEducation(String education) {
        this.education = education;
    }
    
    public String getIntroduction() {
        return introduction;
    }
    
    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }
    
    public List<String> getAvailableTimes() {
        return availableTimes;
    }
    
    public void setAvailableTimes(List<String> availableTimes) {
        this.availableTimes = availableTimes;
    }
    
    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", departmentId=" + departmentId +
                ", departmentName='" + departmentName + '\'' +
                ", hospitalId=" + hospitalId +
                ", hospitalName='" + hospitalName + '\'' +
                ", specialties=" + specialties +
                ", experienceYears=" + experienceYears +
                ", education='" + education + '\'' +
                ", introduction='" + introduction + '\'' +
                ", availableTimes=" + availableTimes +
                '}';
    }
}