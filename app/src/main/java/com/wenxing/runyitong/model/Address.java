package com.wenxing.runyitong.model;

import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;

/**
 * 收货地址模型类
 */
public class Address implements Serializable {
    @SerializedName("id")
    private int id; // 地址ID
    
    @SerializedName("user_id")
    private int userId; // 用户ID
    
    @SerializedName("name")
    private String name; // 收件人姓名
    
    @SerializedName("phone")
    private String phone; // 联系电话
    
    @SerializedName("province")
    private String province; // 省份
    
    @SerializedName("city")
    private String city; // 城市
    
    @SerializedName("district")
    private String district; // 区县
    
    @SerializedName("detail_address")
    private String detailAddress; // 详细地址
    
    @SerializedName("is_default")
    private boolean isDefault; // 是否默认地址
    
    @SerializedName("latitude")
    private String latitude; // 纬度
    
    @SerializedName("longitude")
    private String longitude; // 经度
    
    @SerializedName("created_time")
    private Date createdTime; // 创建时间
    
    @SerializedName("updated_time")
    private Date updatedTime; // 更新时间

    // 无参构造函数
    public Address() {
    }

    // 带参构造函数（用于添加新地址）
    public Address(int userId, String name, String phone, String province, String city, String district, String detailAddress, boolean isDefault, String latitude, String longitude) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.province = province;
        this.city = city;
        this.district = district;
        this.detailAddress = detailAddress;
        this.isDefault = isDefault;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    // 带参构造函数（包含创建时间和更新时间）
    public Address(int userId, String name, String phone, String province, String city, String district, String detailAddress, boolean isDefault, String latitude, String longitude, Date createdTime, Date updatedTime) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.province = province;
        this.city = city;
        this.district = district;
        this.detailAddress = detailAddress;
        this.isDefault = isDefault;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
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

    // 重写toString方法，便于调试
    @Override
    public String toString() {
        return "Address{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", district='" + district + '\'' +
                ", detailAddress='" + detailAddress + '\'' +
                ", isDefault=" + isDefault +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                '}';
    }

    // 重写equals方法，用于比较两个地址对象
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Address address = (Address) obj;
        return id == address.id;
    }

    // 重写hashCode方法
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}