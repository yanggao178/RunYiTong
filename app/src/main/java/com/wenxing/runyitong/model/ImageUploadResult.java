package com.wenxing.runyitong.model;

public class ImageUploadResult {
    private String filename;
    private String url;
    private String file_size;
    private String upload_time;
    private String message;
    private String error_details;
    
    // 构造函数
    public ImageUploadResult() {}
    
    public ImageUploadResult(String filename, String url, String file_size, String upload_time) {
        this.filename = filename;
        this.url = url;
        this.file_size = file_size;
        this.upload_time = upload_time;
    }
    
    // Getter和Setter方法
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getFileSize() {
        return file_size;
    }
    
    public void setFileSize(String file_size) {
        this.file_size = file_size;
    }
    
    public String getUploadTime() {
        return upload_time;
    }
    
    public void setUploadTime(String upload_time) {
        this.upload_time = upload_time;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getErrorDetails() {
        return error_details;
    }
    
    public void setErrorDetails(String error_details) {
        this.error_details = error_details;
    }
    
    @Override
    public String toString() {
        return "ImageUploadResult{" +
                "filename='" + filename + '\'' +
                ", url='" + url + '\'' +
                ", file_size='" + file_size + '\'' +
                ", upload_time='" + upload_time + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}