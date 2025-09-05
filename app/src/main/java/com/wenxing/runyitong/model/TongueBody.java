package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;

/**
 * 舌质分析结果模型类
 * 对应后端 TongueBody 数据类
 */
public class TongueBody {
    @SerializedName("color")
    private String color;      // 舌质颜色（淡红/红/深红/紫等）
    
    @SerializedName("shape")
    private String shape;      // 舌体形态（正常/胖大/瘦薄等）
    
    @SerializedName("texture")
    private String texture;    // 舌质纹理（嫩/老等）
    
    @SerializedName("mobility")
    private String mobility;   // 舌体活动度

    /**
     * 默认构造函数
     */
    public TongueBody() {
    }

    /**
     * 带参数的构造函数
     * @param color 舌质颜色
     * @param shape 舌体形态
     * @param texture 舌质纹理
     * @param mobility 舌体活动度
     */
    public TongueBody(String color, String shape, String texture, String mobility) {
        this.color = color;
        this.shape = shape;
        this.texture = texture;
        this.mobility = mobility;
    }

    // Getter 方法
    public String getColor() {
        return color;
    }

    public String getShape() {
        return shape;
    }

    public String getTexture() {
        return texture;
    }

    public String getMobility() {
        return mobility;
    }

    // Setter 方法
    public void setColor(String color) {
        this.color = color;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }

    public void setMobility(String mobility) {
        this.mobility = mobility;
    }

    @Override
    public String toString() {
        return "TongueBody{" +
                "color='" + color + '\'' +
                ", shape='" + shape + '\'' +
                ", texture='" + texture + '\'' +
                ", mobility='" + mobility + '\'' +
                '}';
    }
}