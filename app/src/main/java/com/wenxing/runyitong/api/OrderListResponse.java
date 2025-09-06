package com.wenxing.runyitong.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import com.wenxing.runyitong.model.Order;

/**
 * 订单列表响应模型 - 匹配后端OrderListResponse结构
 */
public class OrderListResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private List<Order> data;
    
    @SerializedName("total")
    private int total;
    
    @SerializedName("page")
    private int page;
    
    @SerializedName("size")
    private int size;
    
    public OrderListResponse() {}

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

    public List<Order> getData() {
        return data;
    }

    public void setData(List<Order> data) {
        this.data = data;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
    
    // 为了向后兼容，保留原有的getOrders方法
    public List<Order> getOrders() {
        return data;
    }

    public void setOrders(List<Order> orders) {
        this.data = orders;
    }
}