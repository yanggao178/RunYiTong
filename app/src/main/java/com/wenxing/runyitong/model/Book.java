package com.wenxing.runyitong.model;

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
    
    @SerializedName("pdf_file_path")
    private String pdfFilePath;
    
    @SerializedName("file_size")
    private Integer fileSize;

    // 无参构造函数
    public Book() {
    }
    
    // 实用方法
    
    /**
     * 检查书籍是否有有效的名称
     */
    public boolean hasValidName() {
        return name != null && !name.trim().isEmpty();
    }
    
    /**
     * 检查书籍是否有有效的作者
     */
    public boolean hasValidAuthor() {
        return author != null && !author.trim().isEmpty();
    }
    
    /**
     * 检查书籍是否有PDF文件路径
     */
    public boolean hasPdfFile() {
        return pdfFilePath != null && !pdfFilePath.trim().isEmpty();
    }
    
    /**
     * 检查书籍是否有封面URL
     */
    public boolean hasCoverUrl() {
        return coverUrl != null && !coverUrl.trim().isEmpty();
    }
    
    /**
     * 获取格式化的文件大小
     */
    public String getFormattedFileSize() {
        if (fileSize == null || fileSize <= 0) {
            return "未知大小";
        }
        
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }
    
    /**
     * 检查书籍数据是否完整
     */
    public boolean isDataComplete() {
        return hasValidName() && hasValidAuthor() && category != null && !category.trim().isEmpty();
    }
    
    /**
     * 创建书籍的副本
     */
    public Book copy() {
        Book copy = new Book();
        copy.id = this.id;
        copy.name = this.name;
        copy.author = this.author;
        copy.category = this.category;
        copy.description = this.description;
        copy.coverUrl = this.coverUrl;
        copy.publishDate = this.publishDate != null ? new Date(this.publishDate.getTime()) : null;
        copy.createdTime = this.createdTime != null ? new Date(this.createdTime.getTime()) : null;
        copy.updatedTime = this.updatedTime != null ? new Date(this.updatedTime.getTime()) : null;
        copy.pdfFilePath = this.pdfFilePath;
        copy.fileSize = this.fileSize;
        return copy;
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
        this.name = name != null ? name.trim() : null;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author != null ? author.trim() : null;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category != null ? category.trim() : null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : null;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl != null ? coverUrl.trim() : null;
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
    
    public String getPdfFilePath() {
        return pdfFilePath;
    }
    
    public void setPdfFilePath(String pdfFilePath) {
        this.pdfFilePath = pdfFilePath != null ? pdfFilePath.trim() : null;
    }
    
    public Integer getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Integer fileSize) {
        // 确保文件大小不为负数
        if (fileSize != null && fileSize < 0) {
            this.fileSize = 0;
        } else {
            this.fileSize = fileSize;
        }
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
                ", pdfFilePath='" + pdfFilePath + '\'' +
                ", fileSize=" + fileSize +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Book book = (Book) obj;
        
        if (id != book.id) return false;
        if (name != null ? !name.equals(book.name) : book.name != null) return false;
        if (author != null ? !author.equals(book.author) : book.author != null) return false;
        if (category != null ? !category.equals(book.category) : book.category != null) return false;
        if (description != null ? !description.equals(book.description) : book.description != null) return false;
        if (coverUrl != null ? !coverUrl.equals(book.coverUrl) : book.coverUrl != null) return false;
        if (publishDate != null ? !publishDate.equals(book.publishDate) : book.publishDate != null) return false;
        if (createdTime != null ? !createdTime.equals(book.createdTime) : book.createdTime != null) return false;
        if (updatedTime != null ? !updatedTime.equals(book.updatedTime) : book.updatedTime != null) return false;
        if (pdfFilePath != null ? !pdfFilePath.equals(book.pdfFilePath) : book.pdfFilePath != null) return false;
        return fileSize != null ? fileSize.equals(book.fileSize) : book.fileSize == null;
    }
    
    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (coverUrl != null ? coverUrl.hashCode() : 0);
        result = 31 * result + (publishDate != null ? publishDate.hashCode() : 0);
        result = 31 * result + (createdTime != null ? createdTime.hashCode() : 0);
        result = 31 * result + (updatedTime != null ? updatedTime.hashCode() : 0);
        result = 31 * result + (pdfFilePath != null ? pdfFilePath.hashCode() : 0);
        result = 31 * result + (fileSize != null ? fileSize.hashCode() : 0);
        return result;
    }
}