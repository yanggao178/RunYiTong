package com.wenteng.frontend_android.api;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * 注册响应模型类
 */
public class RegisterResponse implements Serializable {
    @SerializedName("user_id")
    private Long userId;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("phone")
    private String phone;
    
    @SerializedName("token")
    private String token;
    
    @SerializedName("register_type")
    private String registerType;
    
    @SerializedName("created_at")
    private String createdAt;
    
    public RegisterResponse() {}
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getRegisterType() {
        return registerType;
    }
    
    public void setRegisterType(String registerType) {
        this.registerType = registerType;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "RegisterResponse{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", registerType='" + registerType + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}