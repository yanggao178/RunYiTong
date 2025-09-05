package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;

/**
 * 舌苔分析结果模型类
 * 对应后端 TongueCoating 数据类
 */
public class TongueCoating {
    @SerializedName("color")
    private String color;      // 苔色（白/黄/灰/黑等）
    
    @SerializedName("thickness")
    private String thickness;  // 苔质厚薄（薄/厚等）
    
    @SerializedName("moisture")
    private String moisture;   // 润燥程度（润/燥等）
    
    @SerializedName("texture")
    private String texture;    // 苔质性状（腻/腐等）

    /**
     * 默认构造函数
     */
    public TongueCoating() {
    }

    /**
     * 带参数的构造函数
     * @param color 苔色
     * @param thickness 苔质厚薄
     * @param moisture 润燥程度
     * @param texture 苔质性状
     */
    public TongueCoating(String color, String thickness, String moisture, String texture) {
        this.color = color;
        this.thickness = thickness;
        this.moisture = moisture;
        this.texture = texture;
    }

    // Getter 方法
    public String getColor() {
        return color;
    }

    public String getThickness() {
        return thickness;
    }

    public String getMoisture() {
        return moisture;
    }

    public String getTexture() {
        return texture;
    }

    // Setter 方法
    public void setColor(String color) {
        this.color = color;
    }

    public void setThickness(String thickness) {
        this.thickness = thickness;
    }

    public void setMoisture(String moisture) {
        this.moisture = moisture;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }

    @Override
    public String toString() {
        return "TongueCoating{" +
                "color='" + color + '\'' +
                ", thickness='" + thickness + '\'' +
                ", moisture='" + moisture + '\'' +
                ", texture='" + texture + '\'' +
                '}';
    }
}