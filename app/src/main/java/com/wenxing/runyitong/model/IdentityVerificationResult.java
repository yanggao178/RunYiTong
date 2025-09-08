package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;

/**
 * 实名认证结果包装类
 * 对应后端的 IdentityVerificationResult
 */
public class IdentityVerificationResult {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private IdentityVerificationResponse data;
    
    public IdentityVerificationResult() {}
    
    public IdentityVerificationResult(boolean success, String message, IdentityVerificationResponse data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public IdentityVerificationResponse getData() {
        return data;
    }
    
    public void setData(IdentityVerificationResponse data) {
        this.data = data;
    }
}