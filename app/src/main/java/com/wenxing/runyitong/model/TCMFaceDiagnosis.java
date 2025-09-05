package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;

/**
 * 中医面诊诊断结果模型类
 * 对应后端 TCMFaceDiagnosis 数据类
 */
public class TCMFaceDiagnosis {
    @SerializedName("syndrome_pattern")
    private String syndromePattern;   // 主要证候类型
    
    @SerializedName("constitution_type")
    private String constitutionType;  // 体质类型判断
    
    @SerializedName("organ_function")
    private String organFunction;     // 脏腑功能状态
    
    @SerializedName("qi_blood_status")
    private String qiBloodStatus;     // 气血状态评估

    /**
     * 默认构造函数
     */
    public TCMFaceDiagnosis() {
    }

    /**
     * 带参数的构造函数
     * @param syndromePattern 主要证候类型
     * @param constitutionType 体质类型判断
     * @param organFunction 脏腑功能状态
     * @param qiBloodStatus 气血状态评估
     */
    public TCMFaceDiagnosis(String syndromePattern, String constitutionType, 
                           String organFunction, String qiBloodStatus) {
        this.syndromePattern = syndromePattern;
        this.constitutionType = constitutionType;
        this.organFunction = organFunction;
        this.qiBloodStatus = qiBloodStatus;
    }

    // Getter 方法
    public String getSyndromePattern() {
        return syndromePattern;
    }

    public String getConstitutionType() {
        return constitutionType;
    }

    public String getOrganFunction() {
        return organFunction;
    }

    public String getQiBloodStatus() {
        return qiBloodStatus;
    }

    // Setter 方法
    public void setSyndromePattern(String syndromePattern) {
        this.syndromePattern = syndromePattern;
    }

    public void setConstitutionType(String constitutionType) {
        this.constitutionType = constitutionType;
    }

    public void setOrganFunction(String organFunction) {
        this.organFunction = organFunction;
    }

    public void setQiBloodStatus(String qiBloodStatus) {
        this.qiBloodStatus = qiBloodStatus;
    }

    @Override
    public String toString() {
        return "TCMFaceDiagnosis{" +
                "syndromePattern='" + syndromePattern + '\'' +
                ", constitutionType='" + constitutionType + '\'' +
                ", organFunction='" + organFunction + '\'' +
                ", qiBloodStatus='" + qiBloodStatus + '\'' +
                '}';
    }
}