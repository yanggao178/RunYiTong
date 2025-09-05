package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;

/**
 * 面诊分析结果模型类
 * 对应后端 FacialAnalysis 数据类
 */
public class FacialAnalysis {
    @SerializedName("complexion")
    private Complexion complexion;         // 面色分析
    
    @SerializedName("facial_features")
    private FacialFeatures facialFeatures; // 五官分析
    
    @SerializedName("facial_regions")
    private FacialRegions facialRegions;   // 面部区域分析

    /**
     * 默认构造函数
     */
    public FacialAnalysis() {
    }

    /**
     * 带参数的构造函数
     * @param complexion 面色分析
     * @param facialFeatures 五官分析
     * @param facialRegions 面部区域分析
     */
    public FacialAnalysis(Complexion complexion, FacialFeatures facialFeatures, 
                         FacialRegions facialRegions) {
        this.complexion = complexion;
        this.facialFeatures = facialFeatures;
        this.facialRegions = facialRegions;
    }

    // Getter 方法
    public Complexion getComplexion() {
        return complexion;
    }

    public FacialFeatures getFacialFeatures() {
        return facialFeatures;
    }

    public FacialRegions getFacialRegions() {
        return facialRegions;
    }

    // Setter 方法
    public void setComplexion(Complexion complexion) {
        this.complexion = complexion;
    }

    public void setFacialFeatures(FacialFeatures facialFeatures) {
        this.facialFeatures = facialFeatures;
    }

    public void setFacialRegions(FacialRegions facialRegions) {
        this.facialRegions = facialRegions;
    }

    @Override
    public String toString() {
        return "FacialAnalysis{" +
                "complexion=" + complexion +
                ", facialFeatures=" + facialFeatures +
                ", facialRegions=" + facialRegions +
                '}';
    }
}