package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Product implements Serializable {
    @SerializedName("id")
    private int id; // 商品ID
    
    @SerializedName("name")
    private String name; // 商品名称
    
    @SerializedName("slug")
    private String slug; // 商品Slug
    
    @SerializedName("price")
    private double price; // 商品价格
    
    @SerializedName("original_price")
    private double originalPrice; // 原价
    
    @SerializedName("description")
    private String description; // 商品描述
    
    @SerializedName("short_description")
    private String shortDescription; // 简短描述
    
    @SerializedName("featured_image_file")
    private String featuredImageFile; // 主图片文件
    
    @SerializedName("category_id")
    private int categoryId; // 分类ID
    
    @SerializedName("category")
    private String category; // 商品分类名称
    
    @SerializedName("category_name")
    private String categoryName; // 分类名称
    
    @SerializedName("department_id")
    private int departmentId; // 部门ID
    
    @SerializedName("stock_quantity")
    private int stockQuantity; // 库存数量
    
    @SerializedName("min_stock_level")
    private int minStockLevel; // 最低库存水平
    
    @SerializedName("sku")
    private String sku; // 库存单位
    
    @SerializedName("barcode")
    private String barcode; // 条形码
    
    @SerializedName("weight")
    private double weight; // 重量
    
    @SerializedName("dimensions")
    private String dimensions; // 尺寸
    
    @SerializedName("gallery_images")
    private List<String> galleryImages; // 图库图片
    
    @SerializedName("tags")
    private String tags; // 标签
    
    @SerializedName("status")
    private String status; // 状态
    
    @SerializedName("is_featured")
    private boolean isFeatured; // 是否精选
    
    @SerializedName("is_prescription_required")
    private boolean isPrescriptionRequired; // 是否需要处方
    
    @SerializedName("manufacturer")
    private String manufacturer; // 制造商
    
    @SerializedName("pharmacy_name")
    private String pharmacyName; // 药店名
    
    @SerializedName("expiry_date")
    private Date expiryDate; // 有效期
    
    @SerializedName("usage_instructions")
    private String usageInstructions; // 使用说明
    
    @SerializedName("side_effects")
    private String sideEffects; // 副作用
    
    @SerializedName("contraindications")
    private String contraindications; // 禁忌症
    
    @SerializedName("views_count")
    private int viewsCount; // 浏览次数
    
    @SerializedName("sales_count")
    private int salesCount; // 销售数量
    
    @SerializedName("created_at")
    private Date createdAt; // 创建时间
    
    @SerializedName("updated_at")
    private Date updatedAt; // 更新时间
    
    @SerializedName("specification")
    private String specification; // 规格
    
    @SerializedName("purchase_count")
    private int purchaseCount; // 购买人数

    // 无参构造函数
    public Product() {
    }

    // 有参构造函数 - 简化版
    public Product(int id, String name, double price, String description, String featuredImageFile) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.featuredImageFile = featuredImageFile;
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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
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

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getFeaturedImageFile() {
        return featuredImageFile;
    }

    public void setFeaturedImageFile(String featuredImageFile) {
        this.featuredImageFile = featuredImageFile;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public int getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(int minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public List<String> getGalleryImages() {
        return galleryImages;
    }

    public void setGalleryImages(List<String> galleryImages) {
        this.galleryImages = galleryImages;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }

    public boolean isPrescriptionRequired() {
        return isPrescriptionRequired;
    }

    public void setPrescriptionRequired(boolean prescriptionRequired) {
        isPrescriptionRequired = prescriptionRequired;
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

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getUsageInstructions() {
        return usageInstructions;
    }

    public void setUsageInstructions(String usageInstructions) {
        this.usageInstructions = usageInstructions;
    }

    public String getSideEffects() {
        return sideEffects;
    }

    public void setSideEffects(String sideEffects) {
        this.sideEffects = sideEffects;
    }

    public String getContraindications() {
        return contraindications;
    }

    public void setContraindications(String contraindications) {
        this.contraindications = contraindications;
    }

    public int getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(int viewsCount) {
        this.viewsCount = viewsCount;
    }

    public int getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(int salesCount) {
        this.salesCount = salesCount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
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
                ", name='" + name + "'" +
                ", price=" + price +
                ", description='" + description + "'" +
                ", featuredImageFile='" + featuredImageFile + "'" +
                ", category='" + category + "'" +
                ", stockQuantity=" + stockQuantity +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", manufacturer='" + manufacturer + "'" +
                ", pharmacyName='" + pharmacyName + "'" +
                '}';
    }
}