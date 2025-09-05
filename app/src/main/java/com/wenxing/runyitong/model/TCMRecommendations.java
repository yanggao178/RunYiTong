package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;

/**
 * 中医调理建议模型类
 * 对应后端 TCMRecommendations 数据类
 */
public class TCMRecommendations {
    @SerializedName("dietary_therapy")
    private String dietaryTherapy;      // 食疗建议
    
    @SerializedName("lifestyle_adjustment")
    private String lifestyleAdjustment; // 生活调理
    
    @SerializedName("herbal_suggestions")
    private String herbalSuggestions;   // 中药调理方向
    
    @SerializedName("follow_up")
    private String followUp;            // 复诊建议

    /**
     * 默认构造函数
     */
    public TCMRecommendations() {
    }

    /**
     * 带参数的构造函数
     * @param dietaryTherapy 食疗建议
     * @param lifestyleAdjustment 生活调理
     * @param herbalSuggestions 中药调理方向
     * @param followUp 复诊建议
     */
    public TCMRecommendations(String dietaryTherapy, String lifestyleAdjustment, 
                             String herbalSuggestions, String followUp) {
        this.dietaryTherapy = dietaryTherapy;
        this.lifestyleAdjustment = lifestyleAdjustment;
        this.herbalSuggestions = herbalSuggestions;
        this.followUp = followUp;
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

    public String getFollowUp() {
        return followUp;
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

    public void setFollowUp(String followUp) {
        this.followUp = followUp;
    }

    @Override
    public String toString() {
        return "TCMRecommendations{" +
                "dietaryTherapy='" + dietaryTherapy + '\'' +
                ", lifestyleAdjustment='" + lifestyleAdjustment + '\'' +
                ", herbalSuggestions='" + herbalSuggestions + '\'' +
                ", followUp='" + followUp + '\'' +
                '}';
    }
}