package com.wenxing.runyitong.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.util.Log;

import androidx.fragment.app.Fragment;

import java.util.List;

/**
 * 诊断工具类
 * 用于检查应用状态和诊断问题
 */
public class DiagnosticUtils {
    
    private static final String TAG = "DiagnosticUtils";
    
    /**
     * 检查Fragment状态
     */
    public static boolean checkFragmentState(Fragment fragment, String fragmentName) {
        if (fragment == null) {
            Log.e(TAG, fragmentName + " is null");
            return false;
        }
        
        Log.d(TAG, "Checking " + fragmentName + " state:");
        Log.d(TAG, "  - isAdded: " + fragment.isAdded());
        Log.d(TAG, "  - isDetached: " + fragment.isDetached());
        Log.d(TAG, "  - isRemoving: " + fragment.isRemoving());
        Log.d(TAG, "  - isVisible: " + fragment.isVisible());
        Log.d(TAG, "  - isResumed: " + fragment.isResumed());
        Log.d(TAG, "  - getActivity: " + (fragment.getActivity() != null ? "not null" : "null"));
        Log.d(TAG, "  - getContext: " + (fragment.getContext() != null ? "not null" : "null"));
        
        boolean isValid = fragment.isAdded() && !fragment.isDetached() && fragment.getActivity() != null;
        Log.d(TAG, "  - Fragment state valid: " + isValid);
        
        return isValid;
    }
    
    /**
     * 检查Activity状态
     */
    public static boolean checkActivityState(Activity activity, String activityName) {
        if (activity == null) {
            Log.e(TAG, activityName + " is null");
            return false;
        }
        
        Log.d(TAG, "Checking " + activityName + " state:");
        Log.d(TAG, "  - isFinishing: " + activity.isFinishing());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Log.d(TAG, "  - isDestroyed: " + activity.isDestroyed());
        }
        Log.d(TAG, "  - hasWindowFocus: " + activity.hasWindowFocus());
        
        boolean isValid = !activity.isFinishing();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            isValid = isValid && !activity.isDestroyed();
        }
        
        Log.d(TAG, "  - Activity state valid: " + isValid);
        
        return isValid;
    }
    
    /**
     * 检查Intent是否可以被解析
     */
    public static boolean checkIntentResolvable(Context context, Intent intent) {
        if (context == null || intent == null) {
            Log.e(TAG, "Context or Intent is null");
            return false;
        }
        
        try {
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
            
            Log.d(TAG, "Intent resolution check:");
            Log.d(TAG, "  - Intent: " + intent.toString());
            Log.d(TAG, "  - Component: " + (intent.getComponent() != null ? intent.getComponent().toString() : "null"));
            Log.d(TAG, "  - Resolvable activities: " + activities.size());
            
            if (activities.size() > 0) {
                for (ResolveInfo info : activities) {
                    Log.d(TAG, "  - Found activity: " + info.activityInfo.name);
                }
                return true;
            } else {
                Log.w(TAG, "  - No activities found to handle this intent");
                return false;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error checking intent resolvability", e);
            return false;
        }
    }
    
    /**
     * 记录内存使用情况
     */
    public static void logMemoryUsage(String context) {
        try {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            long maxMemory = runtime.maxMemory();
            
            double usagePercent = (usedMemory * 100.0) / maxMemory;
            
            Log.d(TAG, "Memory Usage [" + context + "]:");
            Log.d(TAG, "  - Used: " + (usedMemory / 1024 / 1024) + "MB");
            Log.d(TAG, "  - Free: " + (freeMemory / 1024 / 1024) + "MB");
            Log.d(TAG, "  - Total: " + (totalMemory / 1024 / 1024) + "MB");
            Log.d(TAG, "  - Max: " + (maxMemory / 1024 / 1024) + "MB");
            Log.d(TAG, "  - Usage: " + String.format("%.1f%%", usagePercent));
            
            if (usagePercent > 80) {
                Log.w(TAG, "  - WARNING: High memory usage detected!");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error logging memory usage", e);
        }
    }
    
    /**
     * 记录系统内存信息
     */
    public static void logSystemMemoryInfo(Context context) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                am.getMemoryInfo(memoryInfo);
                
                Log.d(TAG, "System Memory Info:");
                Log.d(TAG, "  - Available: " + (memoryInfo.availMem / 1024 / 1024) + "MB");
                Log.d(TAG, "  - Total: " + (memoryInfo.totalMem / 1024 / 1024) + "MB");
                Log.d(TAG, "  - Low memory: " + memoryInfo.lowMemory);
                Log.d(TAG, "  - Threshold: " + (memoryInfo.threshold / 1024 / 1024) + "MB");
                
                if (memoryInfo.lowMemory) {
                    Log.w(TAG, "  - WARNING: System is in low memory state!");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error logging system memory info", e);
        }
    }
    
    /**
     * 检查应用权限
     */
    public static void checkAppPermissions(Context context) {
        try {
            String[] permissions = {
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
            };
            
            Log.d(TAG, "App Permissions Check:");
            for (String permission : permissions) {
                int result = context.checkSelfPermission(permission);
                Log.d(TAG, "  - " + permission + ": " + 
                    (result == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "DENIED"));
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error checking app permissions", e);
        }
    }
    
    /**
     * 执行完整的诊断检查
     */
    public static void performFullDiagnostic(Context context, String tag) {
        Log.d(TAG, "=== Full Diagnostic Check [" + tag + "] ===");
        
        try {
            // 内存检查
            logMemoryUsage(tag);
            logSystemMemoryInfo(context);
            
            // 权限检查
            checkAppPermissions(context);
            
            // 设备信息
            Log.d(TAG, "Device Info:");
            Log.d(TAG, "  - Brand: " + Build.BRAND);
            Log.d(TAG, "  - Model: " + Build.MODEL);
            Log.d(TAG, "  - Android: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
            Log.d(TAG, "  - Manufacturer: " + Build.MANUFACTURER);
            
        } catch (Exception e) {
            Log.e(TAG, "Error performing full diagnostic", e);
        }
        
        Log.d(TAG, "=== End Diagnostic Check ===");
    }
}