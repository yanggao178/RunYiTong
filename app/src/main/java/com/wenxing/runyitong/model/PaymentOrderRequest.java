package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;

/**
 * 支付订单请求模型
 */
public class PaymentOrderRequest {
    @SerializedName("product_id")
    private int productId;
    
    @SerializedName("quantity")
    private int quantity;
    
    @SerializedName("timeout_express")
    private String timeoutExpress;
    
    public PaymentOrderRequest() {
        this.quantity = 1;
        this.timeoutExpress = "30m";
    }
    
    public PaymentOrderRequest(int productId, int quantity, String timeoutExpress) {
        this.productId = productId;
        this.quantity = quantity;
        this.timeoutExpress = timeoutExpress;
    }
    
    // Getters and Setters
    public int getProductId() {
        return productId;
    }
    
    public void setProductId(int productId) {
        this.productId = productId;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public String getTimeoutExpress() {
        return timeoutExpress;
    }
    
    public void setTimeoutExpress(String timeoutExpress) {
        this.timeoutExpress = timeoutExpress;
    }
}