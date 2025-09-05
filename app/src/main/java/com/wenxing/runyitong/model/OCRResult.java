package com.wenxing.runyitong.model;

public class OCRResult {
    private String extracted_text;
    private int text_length;
    private boolean has_chinese;
    private String confidence;
    private String error_details;
    
    // 构造函数
    public OCRResult() {}
    
    public OCRResult(String extracted_text, int text_length, boolean has_chinese, String confidence) {
        this.extracted_text = extracted_text;
        this.text_length = text_length;
        this.has_chinese = has_chinese;
        this.confidence = confidence;
    }
    
    // Getter和Setter方法
    public String getExtractedText() {
        return extracted_text;
    }
    
    public void setExtractedText(String extracted_text) {
        this.extracted_text = extracted_text;
    }
    
    public int getTextLength() {
        return text_length;
    }
    
    public void setTextLength(int text_length) {
        this.text_length = text_length;
    }
    
    public boolean isHasChinese() {
        return has_chinese;
    }
    
    public void setHasChinese(boolean has_chinese) {
        this.has_chinese = has_chinese;
    }
    
    public String getConfidence() {
        return confidence;
    }
    
    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }
    
    public String getErrorDetails() {
        return error_details;
    }
    
    public void setErrorDetails(String error_details) {
        this.error_details = error_details;
    }
    
    @Override
    public String toString() {
        return "OCRResult{" +
                "extracted_text='" + extracted_text + '\'' +
                ", text_length=" + text_length +
                ", has_chinese=" + has_chinese +
                ", confidence='" + confidence + '\'' +
                ", error_details='" + error_details + '\'' +
                '}';
    }
}