package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;

/**
 * 订单数据模型
 */
public class Order {
    @SerializedName("id")
    private int id;
    
    @SerializedName("order_id")
    private String orderId;
    
    @SerializedName("user_id")
    private int userId;
    
    @SerializedName("product_name")
    private String productName;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("price")
    private String price;
    
    @SerializedName("create_time")
    private String createTime;
    
    @SerializedName("pay_time")
    private String payTime;
    
    @SerializedName("shipping_address")
    private String shippingAddress;
    
    public Order() {}

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getPayTime() {
        return payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}