package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ProductDetail implements Serializable {
    @SerializedName("id")
    private int id; // 商品ID
    
    @SerializedName("name")
    private String name; // 商品名称
    
    @SerializedName("price")
    private double price; // 商品价格
    
    @SerializedName("original_price")
    private double originalPrice; // 原价
    
    @SerializedName("description")
    private String description; // 商品描述
    
    @SerializedName("detailed_description")
    private String detailedDescription; // 详细描述
    
    @SerializedName("main_image_url")
    private String mainImageUrl; // 主图片URL
    
    @SerializedName("image_urls")
    private List<String> imageUrls; // 图片URL列表
    
    @SerializedName("category")
    private String category; // 商品分类
    
    @SerializedName("sub_category")
    private String subCategory; // 子分类
    
    @SerializedName("stock")
    private int stock; // 库存数量
    
    @SerializedName("sales_volume")
    private int salesVolume; // 销量
    
    @SerializedName("created_time")
    private Date createdTime; // 创建时间
    
    @SerializedName("updated_time")
    private Date updatedTime; // 更新时间
    
    @SerializedName("specification")
    private String specification; // 规格
    
    @SerializedName("manufacturer")
    private String manufacturer; // 制造商
    
    @SerializedName("pharmacy_name")
    private String pharmacyName; // 药店名
    
    @SerializedName("ingredients")
    private String ingredients; // 成分
    
    @SerializedName("usage")
    private String usage; // 使用方法
    
    @SerializedName("storage_method")
    private String storageMethod; // 储存方法
    
    @SerializedName("expiry_date")
    private Date expiryDate; // 有效期
    
    @SerializedName("reviews")
    private List<Review> reviews; // 用户评价列表
    
    @SerializedName("average_rating")
    private double averageRating; // 平均评分
    
    @SerializedName("related_products")
    private List<Product> relatedProducts; // 相关商品列表

    // 无参构造函数
    public ProductDetail() {
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

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetailedDescription() {
        return detailedDescription;
    }

    public void setDetailedDescription(String detailedDescription) {
        this.detailedDescription = detailedDescription;
    }

    public String getMainImageUrl() {
        return mainImageUrl;
    }

    public void setMainImageUrl(String mainImageUrl) {
        this.mainImageUrl = mainImageUrl;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getSalesVolume() {
        return salesVolume;
    }

    public void setSalesVolume(int salesVolume) {
        this.salesVolume = salesVolume;
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

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getStorageMethod() {
        return storageMethod;
    }

    public void setStorageMethod(String storageMethod) {
        this.storageMethod = storageMethod;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public List<Product> getRelatedProducts() {
        return relatedProducts;
    }

    public void setRelatedProducts(List<Product> relatedProducts) {
        this.relatedProducts = relatedProducts;
    }

    // 用户评价内部类
    public static class Review implements Serializable {
        @SerializedName("id")
        private int id;
        
        @SerializedName("user_id")
        private int userId;
        
        @SerializedName("user_name")
        private String userName;
        
        @SerializedName("rating")
        private int rating;
        
        @SerializedName("comment")
        private String comment;
        
        @SerializedName("review_time")
        private Date reviewTime;
        
        @SerializedName("images")
        private List<String> images;

        // Getter和Setter方法
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

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public int getRating() {
            return rating;
        }

        public void setRating(int rating) {
            this.rating = rating;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public Date getReviewTime() {
            return reviewTime;
        }

        public void setReviewTime(Date reviewTime) {
            this.reviewTime = reviewTime;
        }

        public List<String> getImages() {
            return images;
        }

        public void setImages(List<String> images) {
            this.images = images;
        }
    }
}