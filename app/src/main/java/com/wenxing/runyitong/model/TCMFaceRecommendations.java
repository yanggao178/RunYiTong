package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;

/**
 * 中医面诊调理建议模型类
 * 对应后端 TCMFaceRecommendations 数据类
 */
public class TCMFaceRecommendations {
    @SerializedName("dietary_therapy")
    private String dietaryTherapy;      // 食疗建议
    
    @SerializedName("lifestyle_adjustment")
    private String lifestyleAdjustment; // 生活调理
    
    @SerializedName("herbal_suggestions")
    private String herbalSuggestions;   // 中药调理方向
    
    @SerializedName("acupoint_massage")
    private String acupointMassage;     // 穴位按摩建议

    /**
     * 默认构造函数
     */
    public TCMFaceRecommendations() {
    }

    /**
     * 带参数的构造函数
     * @param dietaryTherapy 食疗建议
     * @param lifestyleAdjustment 生活调理
     * @param herbalSuggestions 中药调理方向
     * @param acupointMassage 穴位按摩建议
     */
    public TCMFaceRecommendations(String dietaryTherapy, String lifestyleAdjustment, 
                                 String herbalSuggestions, String acupointMassage) {
        this.dietaryTherapy = dietaryTherapy;
        this.lifestyleAdjustment = lifestyleAdjustment;
        this.herbalSuggestions = herbalSuggestions;
        this.acupointMassage = acupointMassage;
    }

    // Getter 方法
    public String getDietaryTherapy() {
        return dietaryTherapy;
    }

    public String getLifestyleAdjustment() {
        return lifestyleAdjustment;
    }

    public String getHerbalSuggestions() {
        return herbalSuggestions;
    }

    public String getAcupointMassage() {
        return acupointMassage;
    }

    // Setter 方法
    public void setDietaryTherapy(String dietaryTherapy) {
        this.dietaryTherapy = dietaryTherapy;
    }

    public void setLifestyleAdjustment(String lifestyleAdjustment) {
        this.lifestyleAdjustment = lifestyleAdjustment;
    }

    public void setHerbalSuggestions(String herbalSuggestions) {
        this.herbalSuggestions = herbalSuggestions;
    }

    public void setAcupointMassage(String acupointMassage) {
        this.acupointMassage = acupointMassage;
    }

    @Override
    public String toString() {
        return "TCMFaceRecommendations{" +
                "dietaryTherapy='" + dietaryTherapy + '\'' +
                ", lifestyleAdjustment='" + lifestyleAdjustment + '\'' +
                ", herbalSuggestions='" + herbalSuggestions + '\'' +
                ", acupointMassage='" + acupointMassage + '\'' +
                '}';
    }
}