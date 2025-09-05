package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;

public class BookPage {
    @SerializedName("id")
    private int id;
    
    @SerializedName("book_id")
    private int bookId;
    
    @SerializedName("page_number")
    private int pageNumber;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("content")
    private String content;
    
    @SerializedName("image_url")
    private String imageUrl;
    
    @SerializedName("created_time")
    private String createdTime;
    
    @SerializedName("updated_time")
    private String updatedTime;
    
    // 构造函数
    public BookPage() {}
    
    public BookPage(int id, int bookId, int pageNumber, String title, String content) {
        this.id = id;
        this.bookId = bookId;
        this.pageNumber = pageNumber;
        this.title = title;
        this.content = content;
    }
    
    // Getter和Setter方法
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getBookId() {
        return bookId;
    }
    
    public void setBookId(int bookId) {
        this.bookId = bookId;
    }
    
    public int getPageNumber() {
        return pageNumber;
    }
    
    public void setPageNumber(int pageNumber) {
        this.pageNumber = Math.max(0, pageNumber); // 确保页码不为负数
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title != null ? title.trim() : null;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content != null ? content.trim() : null;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl != null ? imageUrl.trim() : null;
    }
    
    public String getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime != null ? createdTime.trim() : null;
    }
    
    public String getUpdatedTime() {
        return updatedTime;
    }
    
    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime != null ? updatedTime.trim() : null;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BookPage bookPage = (BookPage) obj;
        return id == bookPage.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
    
    @Override
    public String toString() {
        return "BookPage{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", pageNumber=" + pageNumber +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", createdTime='" + createdTime + '\'' +
                ", updatedTime='" + updatedTime + '\'' +
                '}';
    }
    
    // 实用方法
    
    /**
     * 检查页面是否有有效的标题
     */
    public boolean hasValidTitle() {
        return title != null && !title.trim().isEmpty();
    }
    
    /**
     * 检查页面是否有内容
     */
    public boolean hasContent() {
        return content != null && !content.trim().isEmpty();
    }
    
    /**
     * 检查页面是否有图片URL
     */
    public boolean hasImageUrl() {
        return imageUrl != null && !imageUrl.trim().isEmpty();
    }
    
    /**
     * 检查页面数据是否完整
     */
    public boolean isDataComplete() {
        return hasValidTitle() && hasContent() && pageNumber >= 0;
    }
    
    /**
     * 获取内容预览（前100个字符）
     */
    public String getContentPreview() {
        if (!hasContent()) {
            return "无内容";
        }
        return content.length() > 100 ? content.substring(0, 100) + "..." : content;
    }
    
    /**
     * 创建页面的副本
     */
    public BookPage copy() {
        BookPage copy = new BookPage();
        copy.id = this.id;
        copy.bookId = this.bookId;
        copy.pageNumber = this.pageNumber;
        copy.title = this.title;
        copy.content = this.content;
        copy.imageUrl = this.imageUrl;
        copy.createdTime = this.createdTime;
        copy.updatedTime = this.updatedTime;
        return copy;
    }
}