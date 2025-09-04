package com.wenxing.xunyitong.api;

import java.io.Serializable;

/**
 * 用户注册请求模型
 */
public class RegisterRequest implements Serializable {
    private String username;
    private String email;
    private String password;
    private String full_name;
    private String phone;
    private String avatar_url;
    private String verification_code;

    public RegisterRequest() {}

    public RegisterRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public RegisterRequest(String username, String email, String password, String full_name, String phone) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.full_name = full_name;
        this.phone = phone;
    }

    // Getters and Setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getVerification_code() {
        return verification_code;
    }

    public void setVerification_code(String verification_code) {
        this.verification_code = verification_code;
    }

    @Override
    public String toString() {
        return "RegisterRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", full_name='" + full_name + '\'' +
                ", phone='" + phone + '\'' +
                ", avatar_url='" + avatar_url + '\'' +
                '}';
    }
}