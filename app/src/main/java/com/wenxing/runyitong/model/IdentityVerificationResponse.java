package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

/**
 * 实名认证响应模型类
 * 对应后端的 IdentityVerificationResponse
 */
public class IdentityVerificationResponse {
    @SerializedName("id")
    private int id;
    
    @SerializedName("user_id")
    private int userId;
    
    @SerializedName("real_name")
    private String realName;
    
    @SerializedName("id_card_number")
    private String idCardNumber; // 只显示部分信息，如 ****1234
    
    @SerializedName("status")
    private String status; // pending, verified, rejected
    
    @SerializedName("verification_time")
    private Date verificationTime;
    
    @SerializedName("created_at")
    private Date createdAt;
    
    @SerializedName("updated_at")
    private Date updatedAt;
    
    public IdentityVerificationResponse() {}
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getRealName() {
        return realName;
    }
    
    public void setRealName(String realName) {
        this.realName = realName;
    }
    
    public String getIdCardNumber() {
        return idCardNumber;
    }
    
    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Date getVerificationTime() {
        return verificationTime;
    }
    
    public void setVerificationTime(Date verificationTime) {
        this.verificationTime = verificationTime;
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
    
    /**
     * 获取状态的中文描述
     */
    public String getStatusDescription() {
        switch (status) {
            case "pending":
                return "待审核";
            case "verified":
                return "已认证";
            case "rejected":
                return "已拒绝";
            default:
                return "未知状态";
        }
    }
}