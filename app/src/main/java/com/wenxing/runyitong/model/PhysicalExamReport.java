package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;
import com.google.gson.Gson;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 体检报告模型类
 * 用于表示用户的体检报告信息
 */
public class PhysicalExamReport implements Serializable {
    private static final long serialVersionUID = 1L;
    @SerializedName("id")
    private int id;
    
    @SerializedName("health_record_id")
    private int healthRecordId;
    
    @SerializedName("report_name")
    private String reportName;
    
    @SerializedName("exam_date")
    private String examDate;
    
    @SerializedName("hospital_name")
    private String hospitalName;
    
    @SerializedName("summary")
    private String summary;
    
    @SerializedName("doctor_comments")
    private String doctorComments;
    
    // 使用Object类型以灵活处理多种JSON格式
    @SerializedName("key_findings")
    private Object keyFindings;
    
    @SerializedName("normal_items")
    private Object normalItems;
    
    @SerializedName("abnormal_items")
    private Object abnormalItems;
    
    @SerializedName("recommendations")
    private String recommendations;
    
    @SerializedName("report_url")
    private String reportUrl;
    
    @SerializedName("created_time")
    private Date createdAt;
    
    @SerializedName("updated_time")
    private Date updatedAt;

    // 构造函数、getter和setter方法
    public PhysicalExamReport() {
    }

    public PhysicalExamReport(int id, int healthRecordId, String reportName, 
                             String examDate, String hospitalName, String summary, 
                             String doctorComments, Object keyFindings, 
                             Object normalItems, Object abnormalItems, 
                             String recommendations, String reportUrl, 
                             Date createdAt, Date updatedAt) {
        this.id = id;
        this.healthRecordId = healthRecordId;
        this.reportName = reportName;
        this.examDate = examDate;
        this.hospitalName = hospitalName;
        this.summary = summary;
        this.doctorComments = doctorComments;
        this.keyFindings = keyFindings;
        this.normalItems = normalItems;
        this.abnormalItems = abnormalItems;
        this.recommendations = recommendations;
        this.reportUrl = reportUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHealthRecordId() {
        return healthRecordId;
    }

    public void setHealthRecordId(int healthRecordId) {
        this.healthRecordId = healthRecordId;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getExamDate() {
        return examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDoctorComments() {
        return doctorComments;
    }

    public void setDoctorComments(String doctorComments) {
        this.doctorComments = doctorComments;
    }

    // 获取标准化的keyFindings数据（统一转换为Map<String, String>）
    public Map<String, String> getKeyFindings() {
        return convertToMap(keyFindings);
    }

    // 设置keyFindings数据
    public void setKeyFindings(Object keyFindings) {
        this.keyFindings = keyFindings;
    }

    // 获取标准化的normalItems数据
    public Map<String, String> getNormalItems() {
        return convertToMap(normalItems);
    }

    // 设置normalItems数据
    public void setNormalItems(Object normalItems) {
        this.normalItems = normalItems;
    }

    // 获取标准化的abnormalItems数据
    public Map<String, String> getAbnormalItems() {
        return convertToMap(abnormalItems);
    }

    // 设置abnormalItems数据
    public void setAbnormalItems(Object abnormalItems) {
        this.abnormalItems = abnormalItems;
    }
    
    // 将不同类型的JSON数据转换为Map<String, String>
    @SuppressWarnings("unchecked")
    private Map<String, String> convertToMap(Object data) {
        Map<String, String> resultMap = new HashMap<>();
        
        if (data == null) {
            return resultMap;
        }
        
        try {
            // 如果已经是Map类型，直接转换
            if (data instanceof Map) {
                Map<String, Object> mapData = (Map<String, Object>) data;
                for (Map.Entry<String, Object> entry : mapData.entrySet()) {
                    resultMap.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : "");
                }
            } 
            // 如果是List类型，使用索引作为键
            else if (data instanceof List) {
                List<Object> listData = (List<Object>) data;
                for (int i = 0; i < listData.size(); i++) {
                    Object item = listData.get(i);
                    resultMap.put(String.valueOf(i), item != null ? item.toString() : "");
                }
            } 
            // 如果是String类型，尝试解析为JSON或直接存储
            else if (data instanceof String) {
                String stringData = (String) data;
                // 尝试将字符串解析为JSON对象
                try {
                    if (stringData.startsWith("{")) {
                        // 简单的JSON对象解析
                        Map<String, Object> mapData = new Gson().fromJson(stringData, Map.class);
                        for (Map.Entry<String, Object> entry : mapData.entrySet()) {
                            resultMap.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : "");
                        }
                    } else if (stringData.startsWith("[")) {
                        // 简单的JSON数组解析
                        List<Object> listData = new Gson().fromJson(stringData, List.class);
                        for (int i = 0; i < listData.size(); i++) {
                            Object item = listData.get(i);
                            resultMap.put(String.valueOf(i), item != null ? item.toString() : "");
                        }
                    } else if (stringData.contains("=")) {
                        // 处理键值对格式的字符串，如"血压=正常;血糖=偏高"
                        String[] pairs = stringData.split(";|,|\n");
                        for (String pair : pairs) {
                            if (pair.contains("=")) {
                                String[] parts = pair.split("=", 2);
                                if (parts.length == 2) {
                                    resultMap.put(parts[0].trim(), parts[1].trim());
                                }
                            }
                        }
                    } else {
                        // 如果都不是，将整个字符串作为一个条目
                        resultMap.put("findings", stringData);
                    }
                } catch (Exception e) {
                    // 解析失败时，将整个字符串作为一个条目
                    resultMap.put("findings", stringData);
                }
            } 
            // 其他类型直接转换为字符串
            else {
                resultMap.put("value", data.toString());
            }
        } catch (Exception e) {
            // 出现任何异常，返回空Map
            resultMap.clear();
        }
        
        return resultMap;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public String getReportUrl() {
        return reportUrl;
    }

    public void setReportUrl(String reportUrl) {
        this.reportUrl = reportUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "PhysicalExamReport{" +
                "id=" + id +
                ", healthRecordId=" + healthRecordId +
                ", reportName='" + reportName + '\'' +
                ", examDate='" + examDate + '\'' +
                ", hospitalName='" + hospitalName + '\'' +
                ", summary='" + summary + '\'' +
                ", doctorComments='" + doctorComments + '\'' +
                ", keyFindings=" + keyFindings +
                ", normalItems=" + normalItems +
                ", abnormalItems=" + abnormalItems +
                ", recommendations='" + recommendations + '\'' +
                ", reportUrl='" + reportUrl + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}