package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * 支付订单响应模型
 */
public class PaymentOrderResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("order_string")
    private String orderString;
    
    @SerializedName("order_info")
    private OrderInfo orderInfo;
    
    public PaymentOrderResponse() {}
    
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
    
    public String getOrderString() {
        return orderString;
    }
    
    public void setOrderString(String orderString) {
        this.orderString = orderString;
    }
    
    public OrderInfo getOrderInfo() {
        return orderInfo;
    }
    
    public void setOrderInfo(OrderInfo orderInfo) {
        this.orderInfo = orderInfo;
    }
    
    /**
     * 订单信息内部类
     */
    public static class OrderInfo {
        @SerializedName("out_trade_no")
        private String outTradeNo;
        
        @SerializedName("subject")
        private String subject;
        
        @SerializedName("body")
        private String body;
        
        @SerializedName("total_amount")
        private String totalAmount;
        
        @SerializedName("product_name")
        private String productName;
        
        @SerializedName("quantity")
        private int quantity;
        
        @SerializedName("unit_price")
        private double unitPrice;
        
        @SerializedName("app_pay_params")
        private Map<String, String> appPayParams;
        
        public OrderInfo() {}
        
        // Getters and Setters
        public String getOutTradeNo() {
            return outTradeNo;
        }
        
        public void setOutTradeNo(String outTradeNo) {
            this.outTradeNo = outTradeNo;
        }
        
        public String getSubject() {
            return subject;
        }
        
        public void setSubject(String subject) {
            this.subject = subject;
        }
        
        public String getBody() {
            return body;
        }
        
        public void setBody(String body) {
            this.body = body;
        }
        
        public String getTotalAmount() {
            return totalAmount;
        }
        
        public void setTotalAmount(String totalAmount) {
            this.totalAmount = totalAmount;
        }
        
        public String getProductName() {
            return productName;
        }
        
        public void setProductName(String productName) {
            this.productName = productName;
        }
        
        public int getQuantity() {
            return quantity;
        }
        
        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
        
        public double getUnitPrice() {
            return unitPrice;
        }
        
        public void setUnitPrice(double unitPrice) {
            this.unitPrice = unitPrice;
        }
        
        public Map<String, String> getAppPayParams() {
            return appPayParams;
        }
        
        public void setAppPayParams(Map<String, String> appPayParams) {
            this.appPayParams = appPayParams;
        }
    }
}