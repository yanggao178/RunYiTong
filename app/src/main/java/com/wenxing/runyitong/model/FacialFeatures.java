package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;

/**
 * 五官特征分析模型类
 * 对应后端 FacialFeatures 数据类
 */
public class FacialFeatures {
    @SerializedName("eyes")
    private String eyes;  // 眼部特征分析
    
    @SerializedName("nose")
    private String nose;  // 鼻部特征分析
    
    @SerializedName("mouth")
    private String mouth; // 口唇特征分析
    
    @SerializedName("ears")
    private String ears;  // 耳部特征分析

    /**
     * 默认构造函数
     */
    public FacialFeatures() {
    }

    /**
     * 带参数的构造函数
     * @param eyes 眼部特征分析
     * @param nose 鼻部特征分析
     * @param mouth 口唇特征分析
     * @param ears 耳部特征分析
     */
    public FacialFeatures(String eyes, String nose, String mouth, String ears) {
        this.eyes = eyes;
        this.nose = nose;
        this.mouth = mouth;
        this.ears = ears;
    }

    // Getter 方法
    public String getEyes() {
        return eyes;
    }

    public String getNose() {
        return nose;
    }

    public String getMouth() {
        return mouth;
    }

    public String getEars() {
        return ears;
    }

    // Setter 方法
    public void setEyes(String eyes) {
        this.eyes = eyes;
    }

    public void setNose(String nose) {
        this.nose = nose;
    }

    public void setMouth(String mouth) {
        this.mouth = mouth;
    }

    public void setEars(String ears) {
        this.ears = ears;
    }

    @Override
    public String toString() {
        return "FacialFeatures{" +
                "eyes='" + eyes + '\'' +
                ", nose='" + nose + '\'' +
                ", mouth='" + mouth + '\'' +
                ", ears='" + ears + '\'' +
                '}';
    }
}