package com.wenteng.frontend_android.api;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * 短信验证码响应模型类
 */
public class SmsCodeResponse implements Serializable {
    @SerializedName("phone")
    private String phone;
    
    @SerializedName("code_sent")
    private boolean codeSent;
    
    @SerializedName("expires_in")
    private int expiresIn;
    
    @SerializedName("sent_at")
    private String sentAt;
    
    @SerializedName("retry_after")
    private int retryAfter;
    
    public SmsCodeResponse() {}
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public boolean isCodeSent() {
        return codeSent;
    }
    
    public void setCodeSent(boolean codeSent) {
        this.codeSent = codeSent;
    }
    
    public int getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public String getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(String sentAt) {
        this.sentAt = sentAt;
    }
    
    public int getRetryAfter() {
        return retryAfter;
    }
    
    public void setRetryAfter(int retryAfter) {
        this.retryAfter = retryAfter;
    }
    
    @Override
    public String toString() {
        return "SmsCodeResponse{" +
                "phone='" + phone + '\'' +
                ", codeSent=" + codeSent +
                ", expiresIn=" + expiresIn +
                ", sentAt='" + sentAt + '\'' +
                ", retryAfter=" + retryAfter +
                '}';
    }
}