package com.wenteng.frontend_android.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;

public class Book implements Serializable {
    @SerializedName("id")
    private int id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("author")
    private String author;
    
    @SerializedName("category")
    private String category;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("cover_url")
    private String coverUrl;
    
    @SerializedName("publish_date")
    private Date publishDate;
    
    @SerializedName("created_time")
    private Date createdTime;
    
    @SerializedName("updated_time")
    private Date updatedTime;

    // 无参构造函数
    public Book() {
    }

    // 全参构造函数
    public Book(int id, String name, String author, String category, String description, String coverUrl, Date publishDate) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.category = category;
        this.description = description;
        this.coverUrl = coverUrl;
        this.publishDate = publishDate;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
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
        return "Book{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", coverUrl='" + coverUrl + '\'' +
                ", publishDate=" + publishDate +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                '}';
    }
}