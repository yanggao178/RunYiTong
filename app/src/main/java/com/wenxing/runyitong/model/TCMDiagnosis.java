package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;

/**
 * 中医诊断结果模型类
 * 对应后端 TCMDiagnosis 数据类
 */
public class TCMDiagnosis {
    @SerializedName("syndrome_pattern")
    private String syndromePattern;      // 主要证候类型
    
    @SerializedName("constitution_type")
    private String constitutionType;     // 体质类型判断
    
    @SerializedName("organ_function")
    private String organFunction;        // 脏腑功能分析
    
    @SerializedName("qi_blood_status")
    private String qiBloodStatus;        // 气血状态分析
    
    @SerializedName("pathological_factors")
    private String pathologicalFactors;  // 病理因素分析
    
    @SerializedName("organ_systems")
    private String organSystems;         // 涉及脏腑系统

    /**
     * 默认构造函数
     */
    public TCMDiagnosis() {
    }

    /**
     * 带参数的构造函数
     * @param syndromePattern 主要证候类型
     * @param constitutionType 体质类型判断
     * @param organFunction 脏腑功能分析
     * @param qiBloodStatus 气血状态分析
     * @param pathologicalFactors 病理因素分析
     * @param organSystems 涉及脏腑系统
     */
    public TCMDiagnosis(String syndromePattern, String constitutionType, 
                       String organFunction, String qiBloodStatus,
                       String pathologicalFactors, String organSystems) {
        this.syndromePattern = syndromePattern;
        this.constitutionType = constitutionType;
        this.organFunction = organFunction;
        this.qiBloodStatus = qiBloodStatus;
        this.pathologicalFactors = pathologicalFactors;
        this.organSystems = organSystems;
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

    public String getPathologicalFactors() {
        return pathologicalFactors;
    }

    public String getOrganSystems() {
        return organSystems;
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

    public void setPathologicalFactors(String pathologicalFactors) {
        this.pathologicalFactors = pathologicalFactors;
    }

    public void setOrganSystems(String organSystems) {
        this.organSystems = organSystems;
    }

    @Override
    public String toString() {
        return "TCMDiagnosis{" +
                "syndromePattern='" + syndromePattern + '\'' +
                ", constitutionType='" + constitutionType + '\'' +
                ", organFunction='" + organFunction + '\'' +
                ", qiBloodStatus='" + qiBloodStatus + '\'' +
                ", pathologicalFactors='" + pathologicalFactors + '\'' +
                ", organSystems='" + organSystems + '\'' +
                '}';
    }
}