package com.wenxing.runyitong.api;

import java.io.Serializable;

/**
 * 用户登录请求模型
 * 与后端LoginRequest模型保持一致
 */
public class LoginRequest implements Serializable {
    private String username;
    private String password;

    /**
     * 默认构造函数
     */
    public LoginRequest() {}

    /**
     * 构造函数
     * @param username 用户名
     * @param password 密码
     */
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * 获取用户名
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取密码
     * @return 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}