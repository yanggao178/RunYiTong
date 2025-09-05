package com.wenxing.runyitong.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

/**
 * 悬浮窗权限管理工具类
 * 处理SYSTEM_ALERT_WINDOW权限的检查、请求和管理
 */
public class OverlayPermissionManager {
    private static final String TAG = "OverlayPermissionManager";
    public static final int REQUEST_OVERLAY_PERMISSION = 1001;
    
    // 静态回调存储，用于处理权限返回结果
    private static PermissionCallback pendingCallback;
    
    /**
     * 检查是否已获得悬浮窗权限
     * @param context 上下文
     * @return true表示已获得权限，false表示未获得权限
     */
    public static boolean hasOverlayPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        // Android 6.0以下版本默认有权限
        return true;
    }
    
    /**
     * 请求悬浮窗权限
     * @param activity Activity实例
     * @param callback 权限结果回调
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestOverlayPermission(Activity activity, PermissionCallback callback) {
        if (hasOverlayPermission(activity)) {
            Log.d(TAG, "悬浮窗权限已获得");
            if (callback != null) {
                callback.onPermissionGranted();
            }
            return;
        }
        
        // 存储回调以便在权限返回时使用
        pendingCallback = callback;
        
        // 显示权限说明对话框
        new AlertDialog.Builder(activity)
                .setTitle("需要悬浮窗权限")
                .setMessage("为了在微信界面上显示搜索指引，需要开启悬浮窗权限。" + System.lineSeparator() + System.lineSeparator() + "请在设置页面中找到本应用，开启'显示在其他应用的上层'权限。")
                .setPositiveButton("去设置", (dialog, which) -> {
                    openOverlayPermissionSettings(activity);
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    Log.d(TAG, "用户取消权限申请");
                    if (pendingCallback != null) {
                        pendingCallback.onPermissionDenied();
                        pendingCallback = null;
                    }
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }
    
    /**
     * 打开悬浮窗权限设置页面
     * @param activity Activity实例
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void openOverlayPermissionSettings(Activity activity) {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
            Log.d(TAG, "跳转到悬浮窗权限设置页面");
        } catch (Exception e) {
            Log.e(TAG, "打开权限设置页面失败: " + e.getMessage());
            // 如果无法打开特定应用的权限页面，尝试打开通用权限管理页面
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivity(intent);
            } catch (Exception ex) {
                Log.e(TAG, "打开应用详情页面也失败: " + ex.getMessage());
            }
        }
    }
    
    /**
     * 处理权限请求返回结果
     * @param activity Activity实例
     * @param requestCode 请求码
     */
    public static void handlePermissionResult(Activity activity, int requestCode) {
        if (requestCode == REQUEST_OVERLAY_PERMISSION && pendingCallback != null) {
            if (hasOverlayPermission(activity)) {
                Log.d(TAG, "悬浮窗权限获取成功");
                pendingCallback.onPermissionGranted();
            } else {
                Log.d(TAG, "悬浮窗权限获取失败");
                pendingCallback.onPermissionDenied();
            }
            pendingCallback = null;
        }
    }
    
    /**
     * 权限结果回调接口
     */
    public interface PermissionCallback {
        /**
         * 权限获取成功
         */
        void onPermissionGranted();
        
        /**
         * 权限获取失败
         */
        void onPermissionDenied();
    }
    
    /**
     * 检查并请求悬浮窗权限（便捷方法）
     * @param activity Activity实例
     * @param callback 权限结果回调
     */
    public static void checkAndRequestOverlayPermission(Activity activity, PermissionCallback callback) {
        if (hasOverlayPermission(activity)) {
            Log.d(TAG, "悬浮窗权限已获得");
            if (callback != null) {
                callback.onPermissionGranted();
            }
        } else {
            Log.d(TAG, "需要申请悬浮窗权限");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestOverlayPermission(activity, callback);
            } else {
                // Android 6.0以下版本默认有权限
                if (callback != null) {
                    callback.onPermissionGranted();
                }
            }
        }
    }
}