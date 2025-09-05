package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Department implements Serializable {
    @SerializedName("id")
    private int id; // 科室ID
    
    @SerializedName("name")
    private String name; // 科室名称
    
    @SerializedName("description")
    private String description; // 科室描述
    
    // 无参构造函数
    public Department() {
    }
    
    // 有参构造函数
    public Department(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return "Department{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}