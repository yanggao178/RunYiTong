package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 医院实体类
 * 对应数据库表：hospitals
 */
public class Hospital implements Serializable {
    // 序列化版本ID
    private static final long serialVersionUID = 1L;
    
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
    
    // 新增字段
    @SerializedName("slug")
    private String slug; // 医院唯一标识符
    
    @SerializedName("short_description")
    private String shortDescription; // 医院简介
    
    @SerializedName("category_id")
    private int categoryId; // 分类ID
    
    @SerializedName("department_id")
    private int departmentId; // 部门ID
    
    @SerializedName("email")
    private String email; // 电子邮件
    
    @SerializedName("website")
    private String website; // 医院网站
    
    @SerializedName("rating")
    private float rating; // 评分
    
    @SerializedName("featured_image_url")
    private String featuredImageUrl; // 特色图片URL
    
    @SerializedName("services_offered")
    private String servicesOffered; // 提供的服务
    
    @SerializedName("tags")
    private String tags; // 标签
    
    @SerializedName("status")
    private String status; // 状态
    
    @SerializedName("is_featured")
    private boolean isFeatured; // 是否精选
    
    @SerializedName("is_affiliated")
    private boolean isAffiliated; // 是否关联
    
    // 无参构造函数
    public Hospital() {
    }
    
    // 有参构造函数（基础字段）
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
    
    // 全参构造函数（包含所有字段）
    public Hospital(int id, String name, String address, String phone, String level, 
                   String description, List<String> departments, String officialAccountId, 
                   String wechatId, Date createdTime, Date updatedTime, String slug,
                   String shortDescription, int categoryId, int departmentId, String email,
                   String website, float rating, String featuredImageUrl, String servicesOffered,
                   String tags, String status, boolean isFeatured, boolean isAffiliated) {
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
        this.slug = slug;
        this.shortDescription = shortDescription;
        this.categoryId = categoryId;
        this.departmentId = departmentId;
        this.email = email;
        this.website = website;
        this.rating = rating;
        this.featuredImageUrl = featuredImageUrl;
        this.servicesOffered = servicesOffered;
        this.tags = tags;
        this.status = status;
        this.isFeatured = isFeatured;
        this.isAffiliated = isAffiliated;
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
    
    // 新增字段的Getter和Setter方法
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    public String getShortDescription() {
        return shortDescription;
    }
    
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }
    
    public int getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
    
    public int getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getWebsite() {
        return website;
    }
    
    public void setWebsite(String website) {
        this.website = website;
    }
    
    public float getRating() {
        return rating;
    }
    
    public void setRating(float rating) {
        this.rating = rating;
    }
    
    public String getFeaturedImageUrl() {
        return featuredImageUrl;
    }
    
    public void setFeaturedImageUrl(String featuredImageUrl) {
        this.featuredImageUrl = featuredImageUrl;
    }
    
    public String getServicesOffered() {
        return servicesOffered;
    }
    
    public void setServicesOffered(String servicesOffered) {
        this.servicesOffered = servicesOffered;
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
    
    public void setFeatured(boolean isFeatured) {
        this.isFeatured = isFeatured;
    }
    
    public boolean isAffiliated() {
        return isAffiliated;
    }
    
    public void setAffiliated(boolean isAffiliated) {
        this.isAffiliated = isAffiliated;
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
                ", slug='" + slug + '\'' +
                ", shortDescription='" + shortDescription + '\'' +
                ", categoryId=" + categoryId +
                ", departmentId=" + departmentId +
                ", email='" + email + '\'' +
                ", website='" + website + '\'' +
                ", rating=" + rating +
                ", featuredImageUrl='" + featuredImageUrl + '\'' +
                ", servicesOffered='" + servicesOffered + '\'' +
                ", tags='" + tags + '\'' +
                ", status='" + status + '\'' +
                ", isFeatured=" + isFeatured +
                ", isAffiliated=" + isAffiliated +
                '}';
    }
}