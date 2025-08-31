package com.wenteng.frontend_android.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;

public class Product implements Serializable {
    @SerializedName("id")
    private int id; // 商品ID
    
    @SerializedName("name")
    private String name; // 商品名称
    
    @SerializedName("price")
    private double price; // 商品价格
    
    @SerializedName("description")
    private String description; // 商品描述
    
    @SerializedName("image_url")
    private String imageUrl; // 商品图片URL
    
    @SerializedName("category")
    private String category; // 商品分类
    
    @SerializedName("stock")
    private int stock; // 库存数量
    
    @SerializedName("created_time")
    private Date createdTime; // 创建时间
    
    @SerializedName("updated_time")
    private Date updatedTime; // 更新时间
    
    @SerializedName("specification")
    private String specification; // 规格
    
    @SerializedName("manufacturer")
    private String manufacturer;
    
    @SerializedName("pharmacy_name")
    private String pharmacyName; // 药店名
    
    @SerializedName("purchase_count")
    private int purchaseCount; // 购买人数

    // 无参构造函数
    public Product() {
    }

    // 有参构造函数
    public Product(int id, String name, double price, String description, String imageUrl, 
                  String category, int stock, Date createdTime, Date updatedTime) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.stock = stock;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
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

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getPharmacyName() {
        return pharmacyName;
    }

    public void setPharmacyName(String pharmacyName) {
        this.pharmacyName = pharmacyName;
    }

    public int getPurchaseCount() {
        return purchaseCount;
    }

    public void setPurchaseCount(int purchaseCount) {
        this.purchaseCount = purchaseCount;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", category='" + category + '\'' +
                ", stock=" + stock +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                '}';
    }
}