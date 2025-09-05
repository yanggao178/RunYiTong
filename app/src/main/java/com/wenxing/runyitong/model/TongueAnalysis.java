package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;

/**
 * 舌诊分析结果模型类
 * 对应后端 TongueAnalysis 数据类
 */
public class TongueAnalysis {
    @SerializedName("tongue_body")
    private TongueBody tongueBody;        // 舌质分析
    
    @SerializedName("tongue_coating")
    private TongueCoating tongueCoating;  // 舌苔分析

    /**
     * 默认构造函数
     */
    public TongueAnalysis() {
    }

    /**
     * 带参数的构造函数
     * @param tongueBody 舌质分析
     * @param tongueCoating 舌苔分析
     */
    public TongueAnalysis(TongueBody tongueBody, TongueCoating tongueCoating) {
        this.tongueBody = tongueBody;
        this.tongueCoating = tongueCoating;
    }

    // Getter 方法
    public TongueBody getTongueBody() {
        return tongueBody;
    }

    public TongueCoating getTongueCoating() {
        return tongueCoating;
    }

    // Setter 方法
    public void setTongueBody(TongueBody tongueBody) {
        this.tongueBody = tongueBody;
    }

    public void setTongueCoating(TongueCoating tongueCoating) {
        this.tongueCoating = tongueCoating;
    }

    @Override
    public String toString() {
        return "TongueAnalysis{" +
                "tongueBody=" + tongueBody +
                ", tongueCoating=" + tongueCoating +
                '}';
    }
}