package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;

/**
 * 面色分析结果模型类
 * 对应后端 Complexion 数据类
 */
public class Complexion {
    @SerializedName("color")
    private String color;        // 面色（红润/苍白/萎黄/青紫等）
    
    @SerializedName("luster")
    private String luster;       // 光泽度（有神/无神等）
    
    @SerializedName("texture")
    private String texture;      // 皮肤质地
    
    @SerializedName("distribution")
    private String distribution; // 色泽分布特点

    /**
     * 默认构造函数
     */
    public Complexion() {
    }

    /**
     * 带参数的构造函数
     * @param color 面色
     * @param luster 光泽度
     * @param texture 皮肤质地
     * @param distribution 色泽分布特点
     */
    public Complexion(String color, String luster, String texture, String distribution) {
        this.color = color;
        this.luster = luster;
        this.texture = texture;
        this.distribution = distribution;
    }

    // Getter 方法
    public String getColor() {
        return color;
    }

    public String getLuster() {
        return luster;
    }

    public String getTexture() {
        return texture;
    }

    public String getDistribution() {
        return distribution;
    }

    // Setter 方法
    public void setColor(String color) {
        this.color = color;
    }

    public void setLuster(String luster) {
        this.luster = luster;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    @Override
    public String toString() {
        return "Complexion{" +
                "color='" + color + '\'' +
                ", luster='" + luster + '\'' +
                ", texture='" + texture + '\'' +
                ", distribution='" + distribution + '\'' +
                '}';
    }
}