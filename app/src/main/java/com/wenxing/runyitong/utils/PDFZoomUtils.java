package com.wenxing.runyitong.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class PDFZoomUtils {
    private static final String TAG = "PDFZoomUtils";
    private static final String PREFS_NAME = "pdf_zoom_prefs";
    private static final String KEY_ZOOM_LEVEL = "zoom_level";
    private static final String KEY_AUTO_FIT = "auto_fit";
    private static final String KEY_ZOOM_MODE = "zoom_mode";
    
    // 缩放级别常量
    public static final float MIN_ZOOM = 0.25f;  // 25%
    public static final float MAX_ZOOM = 5.0f;   // 500%
    public static final float DEFAULT_ZOOM = 1.0f; // 100%
    
    // 预设缩放级别
    public static final float[] PRESET_ZOOM_LEVELS = {
        0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 2.5f, 3.0f, 4.0f, 5.0f
    };
    
    // 缩放模式
    public enum ZoomMode {
        MANUAL,      // 手动缩放
        FIT_WIDTH,   // 适应宽度
        FIT_HEIGHT,  // 适应高度
        FIT_PAGE,    // 适应页面
        AUTO         // 自动选择
    }
    
    /**
     * 验证缩放级别是否有效
     */
    public static boolean isValidZoomLevel(float zoom) {
        return zoom >= MIN_ZOOM && zoom <= MAX_ZOOM;
    }
    
    /**
     * 限制缩放级别在有效范围内
     */
    public static float clampZoomLevel(float zoom) {
        return Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));
    }
    
    /**
     * 获取下一个缩放级别
     */
    public static float getNextZoomLevel(float currentZoom, boolean zoomIn) {
        float targetZoom = currentZoom;
        
        if (zoomIn) {
            // 放大：找到第一个大于当前缩放的预设值
            for (float preset : PRESET_ZOOM_LEVELS) {
                if (preset > currentZoom + 0.01f) { // 添加小的容差
                    targetZoom = preset;
                    break;
                }
            }
            // 如果没找到，使用当前值的1.25倍
            if (targetZoom == currentZoom) {
                targetZoom = currentZoom * 1.25f;
            }
        } else {
            // 缩小：找到第一个小于当前缩放的预设值
            for (int i = PRESET_ZOOM_LEVELS.length - 1; i >= 0; i--) {
                if (PRESET_ZOOM_LEVELS[i] < currentZoom - 0.01f) {
                    targetZoom = PRESET_ZOOM_LEVELS[i];
                    break;
                }
            }
            // 如果没找到，使用当前值的0.8倍
            if (targetZoom == currentZoom) {
                targetZoom = currentZoom * 0.8f;
            }
        }
        
        return clampZoomLevel(targetZoom);
    }
    
    /**
     * 获取最接近的预设缩放级别
     */
    public static float getNearestPresetZoom(float zoom) {
        float nearestZoom = PRESET_ZOOM_LEVELS[0];
        float minDifference = Math.abs(zoom - nearestZoom);
        
        for (float preset : PRESET_ZOOM_LEVELS) {
            float difference = Math.abs(zoom - preset);
            if (difference < minDifference) {
                minDifference = difference;
                nearestZoom = preset;
            }
        }
        
        return nearestZoom;
    }
    
    /**
     * 计算适应宽度的缩放级别
     */
    public static float calculateFitWidthZoom(int pageWidth, int containerWidth, int padding) {
        if (pageWidth <= 0 || containerWidth <= 0) {
            return DEFAULT_ZOOM;
        }
        
        float availableWidth = containerWidth - (padding * 2);
        float zoom = availableWidth / pageWidth;
        
        return clampZoomLevel(zoom);
    }
    
    /**
     * 计算适应高度的缩放级别
     */
    public static float calculateFitHeightZoom(int pageHeight, int containerHeight, int padding) {
        if (pageHeight <= 0 || containerHeight <= 0) {
            return DEFAULT_ZOOM;
        }
        
        float availableHeight = containerHeight - (padding * 2);
        float zoom = availableHeight / pageHeight;
        
        return clampZoomLevel(zoom);
    }
    
    /**
     * 计算适应页面的缩放级别
     */
    public static float calculateFitPageZoom(int pageWidth, int pageHeight, 
                                           int containerWidth, int containerHeight, int padding) {
        float fitWidthZoom = calculateFitWidthZoom(pageWidth, containerWidth, padding);
        float fitHeightZoom = calculateFitHeightZoom(pageHeight, containerHeight, padding);
        
        // 选择较小的缩放级别以确保整个页面都能显示
        return Math.min(fitWidthZoom, fitHeightZoom);
    }
    
    /**
     * 根据缩放模式计算缩放级别
     */
    public static float calculateZoomForMode(ZoomMode mode, int pageWidth, int pageHeight,
                                           int containerWidth, int containerHeight, 
                                           int padding, float currentZoom) {
        switch (mode) {
            case FIT_WIDTH:
                return calculateFitWidthZoom(pageWidth, containerWidth, padding);
            case FIT_HEIGHT:
                return calculateFitHeightZoom(pageHeight, containerHeight, padding);
            case FIT_PAGE:
                return calculateFitPageZoom(pageWidth, pageHeight, containerWidth, containerHeight, padding);
            case AUTO:
                // 自动模式：根据页面比例选择最合适的模式
                float pageRatio = (float) pageWidth / pageHeight;
                float containerRatio = (float) containerWidth / containerHeight;
                
                if (pageRatio > containerRatio) {
                    // 页面更宽，适应宽度
                    return calculateFitWidthZoom(pageWidth, containerWidth, padding);
                } else {
                    // 页面更高，适应高度
                    return calculateFitHeightZoom(pageHeight, containerHeight, padding);
                }
            case MANUAL:
            default:
                return currentZoom;
        }
    }
    
    /**
     * 保存缩放设置到SharedPreferences
     */
    public static void saveZoomSettings(Context context, float zoomLevel, ZoomMode zoomMode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        editor.putFloat(KEY_ZOOM_LEVEL, zoomLevel);
        editor.putString(KEY_ZOOM_MODE, zoomMode.name());
        editor.apply();
        
        Log.d(TAG, "保存缩放设置: " + zoomLevel + ", 模式: " + zoomMode);
    }
    
    /**
     * 从SharedPreferences加载缩放设置
     */
    public static float loadZoomLevel(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getFloat(KEY_ZOOM_LEVEL, DEFAULT_ZOOM);
    }
    
    /**
     * 从SharedPreferences加载缩放模式
     */
    public static ZoomMode loadZoomMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String modeName = prefs.getString(KEY_ZOOM_MODE, ZoomMode.MANUAL.name());
        
        try {
            return ZoomMode.valueOf(modeName);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "无效的缩放模式: " + modeName + ", 使用默认模式");
            return ZoomMode.MANUAL;
        }
    }
    
    /**
     * 获取缩放级别的显示文本
     */
    public static String getZoomDisplayText(float zoom) {
        return Math.round(zoom * 100) + "%";
    }
    
    /**
     * 获取缩放模式的显示文本
     */
    public static String getZoomModeDisplayText(ZoomMode mode) {
        switch (mode) {
            case MANUAL:
                return "手动缩放";
            case FIT_WIDTH:
                return "适应宽度";
            case FIT_HEIGHT:
                return "适应高度";
            case FIT_PAGE:
                return "适应页面";
            case AUTO:
                return "自动选择";
            default:
                return "未知模式";
        }
    }
    
    /**
     * 计算缩放后的尺寸
     */
    public static int[] calculateScaledSize(int originalWidth, int originalHeight, float zoom) {
        int scaledWidth = Math.round(originalWidth * zoom);
        int scaledHeight = Math.round(originalHeight * zoom);
        return new int[]{scaledWidth, scaledHeight};
    }
    
    /**
     * 检查是否需要重新计算缩放
     */
    public static boolean shouldRecalculateZoom(ZoomMode mode, int oldContainerWidth, int oldContainerHeight,
                                               int newContainerWidth, int newContainerHeight) {
        if (mode == ZoomMode.MANUAL) {
            return false;
        }
        
        // 如果容器尺寸发生显著变化，需要重新计算
        float widthChange = Math.abs(newContainerWidth - oldContainerWidth) / (float) oldContainerWidth;
        float heightChange = Math.abs(newContainerHeight - oldContainerHeight) / (float) oldContainerHeight;
        
        return widthChange > 0.1f || heightChange > 0.1f; // 变化超过10%时重新计算
    }
    
    /**
     * 获取推荐的缩放级别（基于设备和内容）
     */
    public static float getRecommendedZoom(Context context, int pageWidth, int pageHeight,
                                         int containerWidth, int containerHeight) {
        // 基于屏幕密度和页面尺寸推荐合适的缩放级别
        float density = context.getResources().getDisplayMetrics().density;
        
        // 计算适应页面的缩放
        float fitPageZoom = calculateFitPageZoom(pageWidth, pageHeight, containerWidth, containerHeight, 16);
        
        // 根据屏幕密度调整
        float adjustedZoom = fitPageZoom * (density / 2.0f); // 假设基准密度为2.0
        
        return clampZoomLevel(adjustedZoom);
    }
    
    /**
     * 清除保存的缩放设置
     */
    public static void clearZoomSettings(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        Log.d(TAG, "已清除缩放设置");
    }
}