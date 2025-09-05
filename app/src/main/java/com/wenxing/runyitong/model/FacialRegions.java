package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;

/**
 * 面部区域分析模型类
 * 对应后端 FacialRegions 数据类
 */
public class FacialRegions {
    @SerializedName("forehead")
    private String forehead; // 额部对应心肺功能
    
    @SerializedName("cheeks")
    private String cheeks;   // 面颊对应脾胃功能
    
    @SerializedName("chin")
    private String chin;     // 下颏对应肾功能
    
    @SerializedName("temples")
    private String temples;  // 太阳穴区域分析

    /**
     * 默认构造函数
     */
    public FacialRegions() {
    }

    /**
     * 带参数的构造函数
     * @param forehead 额部对应心肺功能
     * @param cheeks 面颊对应脾胃功能
     * @param chin 下颏对应肾功能
     * @param temples 太阳穴区域分析
     */
    public FacialRegions(String forehead, String cheeks, String chin, String temples) {
        this.forehead = forehead;
        this.cheeks = cheeks;
        this.chin = chin;
        this.temples = temples;
    }

    // Getter 方法
    public String getForehead() {
        return forehead;
    }

    public String getCheeks() {
        return cheeks;
    }

    public String getChin() {
        return chin;
    }

    public String getTemples() {
        return temples;
    }

    // Setter 方法
    public void setForehead(String forehead) {
        this.forehead = forehead;
    }

    public void setCheeks(String cheeks) {
        this.cheeks = cheeks;
    }

    public void setChin(String chin) {
        this.chin = chin;
    }

    public void setTemples(String temples) {
        this.temples = temples;
    }

    @Override
    public String toString() {
        return "FacialRegions{" +
                "forehead='" + forehead + '\'' +
                ", cheeks='" + cheeks + '\'' +
                ", chin='" + chin + '\'' +
                ", temples='" + temples + '\'' +
                '}';
    }
}