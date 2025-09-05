package com.wenxing.runyitong.api;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * 短信验证码响应模型类
 */
public class SmsCodeResponse implements Serializable {
    @SerializedName("phone")
    private String phone;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("expires_in")
    private int expiresIn;
    
    public SmsCodeResponse() {}
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public int getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    @Override
    public String toString() {
        return "SmsCodeResponse{" +
                "phone='" + phone + '\'' +
                ", message='" + message + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }
}