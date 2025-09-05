package com.wenxing.runyitong.api;

import com.google.gson.annotations.SerializedName;
import com.wenxing.runyitong.model.Appointment;
import java.util.List;
import java.io.Serializable;

/**
 * 预约列表响应模型类
 */
public class AppointmentListResponse implements Serializable {
    @SerializedName("items")
    private List<Appointment> items;
    
    @SerializedName("total")
    private int total;
    
    @SerializedName("skip")
    private int skip;
    
    @SerializedName("limit")
    private int limit;
    
    /**
     * 无参构造函数
     */
    public AppointmentListResponse() {}
    
    /**
     * 有参构造函数
     * @param items 预约列表
     * @param total 总数
     * @param skip 跳过数量
     * @param limit 限制数量
     */
    public AppointmentListResponse(List<Appointment> items, int total, int skip, int limit) {
        this.items = items;
        this.total = total;
        this.skip = skip;
        this.limit = limit;
    }
    
    /**
     * 获取预约列表
     * @return 预约列表
     */
    public List<Appointment> getItems() {
        return items;
    }
    
    /**
     * 设置预约列表
     * @param items 预约列表
     */
    public void setItems(List<Appointment> items) {
        this.items = items;
    }
    
    /**
     * 获取总数
     * @return 总数
     */
    public int getTotal() {
        return total;
    }
    
    /**
     * 设置总数
     * @param total 总数
     */
    public void setTotal(int total) {
        this.total = total;
    }
    
    /**
     * 获取跳过数量
     * @return 跳过数量
     */
    public int getSkip() {
        return skip;
    }
    
    /**
     * 设置跳过数量
     * @param skip 跳过数量
     */
    public void setSkip(int skip) {
        this.skip = skip;
    }
    
    /**
     * 获取限制数量
     * @return 限制数量
     */
    public int getLimit() {
        return limit;
    }
    
    /**
     * 设置限制数量
     * @param limit 限制数量
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }
    
    @Override
    public String toString() {
        return "AppointmentListResponse{" +
                "items=" + items +
                ", total=" + total +
                ", skip=" + skip +
                ", limit=" + limit +
                '}';
    }
}