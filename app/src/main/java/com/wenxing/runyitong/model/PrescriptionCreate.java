package com.wenxing.runyitong.model;

/**
 * 处方创建请求模型
 * 对应后端的PrescriptionCreate schema
 */
public class PrescriptionCreate {
    private int user_id;
    private String symptoms;
    private String diagnosis;
    private String prescription_content;
    private String doctor_name;
    private String image_url;

    // 构造方法
    public PrescriptionCreate() {}

    public PrescriptionCreate(int userId, String symptoms, String diagnosis, 
                            String prescriptionContent, String doctorName, String imageUrl) {
        this.user_id = userId;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
        this.prescription_content = prescriptionContent;
        this.doctor_name = doctorName;
        this.image_url = imageUrl;
    }

    // Getter和Setter方法
    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getPrescription_content() {
        return prescription_content;
    }

    public void setPrescription_content(String prescription_content) {
        this.prescription_content = prescription_content;
    }

    public String getDoctor_name() {
        return doctor_name;
    }

    public void setDoctor_name(String doctor_name) {
        this.doctor_name = doctor_name;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    @Override
    public String toString() {
        return "PrescriptionCreate{" +
                "user_id=" + user_id +
                ", symptoms='" + symptoms + '\'' +
                ", diagnosis='" + diagnosis + '\'' +
                ", prescription_content='" + prescription_content + '\'' +
                ", doctor_name='" + doctor_name + '\'' +
                ", image_url='" + image_url + '\'' +
                '}';
    }
}