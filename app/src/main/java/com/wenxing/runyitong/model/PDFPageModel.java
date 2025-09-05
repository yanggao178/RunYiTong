package com.wenxing.runyitong.model;

import android.graphics.Bitmap;

public class PDFPageModel {
    public enum LoadingState {
        PENDING,    // 等待加载
        LOADING,    // 正在加载
        LOADED,     // 加载完成
        ERROR       // 加载错误
    }
    
    private int pageNumber;
    private String pageTitle;
    private Bitmap pageBitmap;
    private LoadingState loadingState;
    private String errorMessage;
    private long loadStartTime;
    private long loadEndTime;
    private float zoomLevel = 1.0f;
    private int originalWidth;
    private int originalHeight;
    private boolean isBookmarked = false;
    
    public PDFPageModel() {
        this.loadingState = LoadingState.PENDING;
    }
    
    public PDFPageModel(int pageNumber, String pageTitle) {
        this.pageNumber = pageNumber;
        this.pageTitle = pageTitle;
        this.loadingState = LoadingState.PENDING;
    }
    
    // Getters and Setters
    public int getPageNumber() {
        return pageNumber;
    }
    
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }
    
    public String getPageTitle() {
        return pageTitle;
    }
    
    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }
    
    public Bitmap getPageBitmap() {
        return pageBitmap;
    }
    
    public void setPageBitmap(Bitmap pageBitmap) {
        // 清理旧的bitmap以防止内存泄漏
        if (this.pageBitmap != null && !this.pageBitmap.isRecycled() && this.pageBitmap != pageBitmap) {
            this.pageBitmap.recycle();
        }
        
        this.pageBitmap = pageBitmap;
        if (pageBitmap != null && !pageBitmap.isRecycled()) {
            this.originalWidth = pageBitmap.getWidth();
            this.originalHeight = pageBitmap.getHeight();
        } else {
            this.originalWidth = 0;
            this.originalHeight = 0;
        }
    }
    
    public LoadingState getLoadingState() {
        return loadingState;
    }
    
    public void setLoadingState(LoadingState loadingState) {
        if (loadingState == null) {
            return;
        }
        
        this.loadingState = loadingState;
        
        if (loadingState == LoadingState.LOADING) {
            this.loadStartTime = System.currentTimeMillis();
            this.loadEndTime = 0; // 重置结束时间
            this.errorMessage = null; // 清除之前的错误信息
        } else if (loadingState == LoadingState.LOADED || loadingState == LoadingState.ERROR) {
            this.loadEndTime = System.currentTimeMillis();
        }
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public long getLoadStartTime() {
        return loadStartTime;
    }
    
    public void setLoadStartTime(long loadStartTime) {
        this.loadStartTime = loadStartTime;
    }
    
    public long getLoadEndTime() {
        return loadEndTime;
    }
    
    public void setLoadEndTime(long loadEndTime) {
        this.loadEndTime = loadEndTime;
    }
    
    public float getZoomLevel() {
        return zoomLevel;
    }
    
    public void setZoomLevel(float zoomLevel) {
        // 验证缩放级别的有效性
        if (zoomLevel <= 0) {
            this.zoomLevel = 1.0f;
        } else if (zoomLevel > 10.0f) {
            this.zoomLevel = 10.0f;
        } else {
            this.zoomLevel = zoomLevel;
        }
    }
    
    public int getOriginalWidth() {
        return originalWidth;
    }
    
    public void setOriginalWidth(int originalWidth) {
        this.originalWidth = originalWidth;
    }
    
    public int getOriginalHeight() {
        return originalHeight;
    }
    
    public void setOriginalHeight(int originalHeight) {
        this.originalHeight = originalHeight;
    }
    
    public boolean isBookmarked() {
        return isBookmarked;
    }
    
    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }
    
    // Utility methods
    public long getLoadDuration() {
        if (loadStartTime > 0 && loadEndTime > 0 && loadEndTime >= loadStartTime) {
            return loadEndTime - loadStartTime;
        }
        return 0;
    }
    
    public String getLoadDurationString() {
        long duration = getLoadDuration();
        if (duration > 0) {
            if (duration < 1000) {
                return duration + "ms";
            } else {
                return String.format("%.1fs", duration / 1000.0);
            }
        }
        return "";
    }
    
    public void resetLoadingState() {
        this.loadingState = LoadingState.PENDING;
        this.loadStartTime = 0;
        this.loadEndTime = 0;
        this.errorMessage = null;
    }
    
    public boolean isValidPage() {
        return pageNumber >= 0;
    }
    
    public int getScaledWidth() {
        return Math.round(originalWidth * zoomLevel);
    }
    
    public int getScaledHeight() {
        return Math.round(originalHeight * zoomLevel);
    }
    
    public boolean isLoaded() {
        return loadingState == LoadingState.LOADED && pageBitmap != null && !pageBitmap.isRecycled();
    }
    
    public boolean hasBitmap() {
        return pageBitmap != null && !pageBitmap.isRecycled();
    }
    
    public boolean isLoading() {
        return loadingState == LoadingState.LOADING;
    }
    
    public boolean hasError() {
        return loadingState == LoadingState.ERROR;
    }
    
    public void cleanup() {
        if (pageBitmap != null && !pageBitmap.isRecycled()) {
            pageBitmap.recycle();
        }
        pageBitmap = null;
        errorMessage = null;
        loadStartTime = 0;
        loadEndTime = 0;
        originalWidth = 0;
        originalHeight = 0;
    }
    
    @Override
    public String toString() {
        return "PDFPageModel{" +
                "pageNumber=" + pageNumber +
                ", pageTitle='" + (pageTitle != null ? pageTitle : "null") + '\'' +
                ", loadingState=" + loadingState +
                ", zoomLevel=" + zoomLevel +
                ", isBookmarked=" + isBookmarked +
                ", hasBitmap=" + hasBitmap() +
                ", loadDuration=" + getLoadDurationString() +
                '}';
    }
}