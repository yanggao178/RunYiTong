package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;

/**
 * 实名认证请求模型类
 * 对应后端的 IdentityVerificationCreateRequest
 */
public class IdentityVerificationRequest {
    @SerializedName("real_name")
    private String realName;
    
    @SerializedName("id_card_number")
    private String idCardNumber;
    
    public IdentityVerificationRequest() {}
    
    public IdentityVerificationRequest(String realName, String idCardNumber) {
        this.realName = realName;
        this.idCardNumber = idCardNumber;
    }
    
    // Getters and Setters
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
}