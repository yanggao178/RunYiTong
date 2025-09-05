package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Hospital implements Serializable {
    @SerializedName("id")
    private int id; // 医院ID
    
    @SerializedName("name")
    private String name; // 医院名称
    
    @SerializedName("address")
    private String address; // 医院地址
    
    @SerializedName("phone")
    private String phone; // 联系电话
    
    @SerializedName("level")
    private String level; // 医院等级（如：三甲）
    
    @SerializedName("description")
    private String description; // 医院描述
    
    @SerializedName("departments")
    private List<String> departments; // 可用科室ID列表
    
    @SerializedName("official_account_id")
    private String officialAccountId; // 公众号原始ID
    
    @SerializedName("wechat_id")
    private String wechatId; // 微信号
    
    @SerializedName("created_time")
    private Date createdTime; // 创建时间
    
    @SerializedName("updated_time")
    private Date updatedTime; // 更新时间
    
    // 无参构造函数
    public Hospital() {
    }
    
    // 有参构造函数
    public Hospital(int id, String name, String address, String phone, String level, 
                   String description, List<String> departments) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.level = level;
        this.description = description;
        this.departments = departments;
    }
    
    // 全参构造函数
    public Hospital(int id, String name, String address, String phone, String level, 
                   String description, List<String> departments, String officialAccountId, 
                   String wechatId, Date createdTime, Date updatedTime) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.level = level;
        this.description = description;
        this.departments = departments;
        this.officialAccountId = officialAccountId;
        this.wechatId = wechatId;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }
    
    // Getter和Setter方法
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getLevel() {
        return level;
    }
    
    public void setLevel(String level) {
        this.level = level;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<String> getDepartments() {
        return departments;
    }
    
    public void setDepartments(List<String> departments) {
        this.departments = departments;
    }
    
    public String getOfficialAccountId() {
        return officialAccountId;
    }
    
    public void setOfficialAccountId(String officialAccountId) {
        this.officialAccountId = officialAccountId;
    }
    
    public String getWechatId() {
        return wechatId;
    }
    
    public void setWechatId(String wechatId) {
        this.wechatId = wechatId;
    }
    
    public Date getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
    
    public Date getUpdatedTime() {
        return updatedTime;
    }
    
    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }
    
    @Override
    public String toString() {
        return "Hospital{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", level='" + level + '\'' +
                ", description='" + description + '\'' +
                ", departments=" + departments +
                ", officialAccountId='" + officialAccountId + '\'' +
                ", wechatId='" + wechatId + '\'' +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                '}';
    }
}