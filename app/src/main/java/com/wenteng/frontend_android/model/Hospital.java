package com.wenteng.frontend_android.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
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
    private List<Integer> departments; // 可用科室ID列表
    
    // 无参构造函数
    public Hospital() {
    }
    
    // 有参构造函数
    public Hospital(int id, String name, String address, String phone, String level, 
                   String description, List<Integer> departments) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.level = level;
        this.description = description;
        this.departments = departments;
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
    
    public List<Integer> getDepartments() {
        return departments;
    }
    
    public void setDepartments(List<Integer> departments) {
        this.departments = departments;
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
                '}';
    }
}