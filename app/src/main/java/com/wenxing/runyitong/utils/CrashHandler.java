package com.wenxing.runyitong.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 全局异常处理器
 * 用于捕获和记录应用程序中未处理的异常
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    
    private static final String TAG = "CrashHandler";
    private static final String CRASH_DIR = "crashes";
    
    private static CrashHandler instance;
    private Context context;
    private Thread.UncaughtExceptionHandler defaultHandler;
    
    private CrashHandler() {}
    
    public static CrashHandler getInstance() {
        if (instance == null) {
            synchronized (CrashHandler.class) {
                if (instance == null) {
                    instance = new CrashHandler();
                }
            }
        }
        return instance;
    }
    
    /**
     * 初始化崩溃处理器
     * @param context 应用上下文
     */
    public void init(Context context) {
        this.context = context.getApplicationContext();
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        Log.d(TAG, "CrashHandler initialized");
    }
    
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.e(TAG, "Uncaught exception in thread: " + thread.getName(), ex);
        
        try {
            // 收集崩溃信息
            String crashInfo = collectCrashInfo(ex);
            
            // 保存崩溃信息到文件
            saveCrashInfoToFile(crashInfo);
            
            // 记录到系统日志
            Log.e(TAG, "Crash info saved: " + System.lineSeparator() + crashInfo);
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling crash", e);
        }
        
        // 调用系统默认的异常处理器
        if (defaultHandler != null) {
            defaultHandler.uncaughtException(thread, ex);
        }
    }
    
    /**
     * 收集崩溃信息
     */
    private String collectCrashInfo(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        
        // 时间戳
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sb.append("Crash Time: ").append(dateFormat.format(new Date())).append(System.lineSeparator());
        sb.append(System.lineSeparator());
        
        // 应用信息
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            sb.append("App Version: ").append(pi.versionName).append(" (").append(pi.versionCode).append(")").append(System.lineSeparator());
            sb.append("Package Name: ").append(pi.packageName).append(System.lineSeparator());
        } catch (Exception e) {
            sb.append("App Info: Unable to collect").append(System.lineSeparator());
        }
        sb.append(System.lineSeparator());
        
        // 设备信息
        sb.append("Device Info:").append(System.lineSeparator());
        sb.append("Brand: ").append(Build.BRAND).append(System.lineSeparator());
        sb.append("Model: ").append(Build.MODEL).append(System.lineSeparator());
        sb.append("Device: ").append(Build.DEVICE).append(System.lineSeparator());
        sb.append("Android Version: ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")").append(System.lineSeparator());
        sb.append(System.lineSeparator());
        
        // 内存信息
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        sb.append("Memory Info:").append(System.lineSeparator());
        sb.append("Used: ").append(usedMemory / 1024 / 1024).append("MB").append(System.lineSeparator());
        sb.append("Free: ").append(freeMemory / 1024 / 1024).append("MB").append(System.lineSeparator());
        sb.append("Total: ").append(totalMemory / 1024 / 1024).append("MB").append(System.lineSeparator());
        sb.append("Max: ").append(maxMemory / 1024 / 1024).append("MB").append(System.lineSeparator());
        sb.append("Usage: ").append(String.format("%.1f%%", (usedMemory * 100.0 / maxMemory))).append(System.lineSeparator());
        sb.append(System.lineSeparator());
        
        // 异常堆栈
        sb.append("Exception Stack Trace:").append(System.lineSeparator());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        sb.append(sw.toString());
        
        return sb.toString();
    }
    
    /**
     * 保存崩溃信息到文件
     */
    private void saveCrashInfoToFile(String crashInfo) {
        try {
            // 创建崩溃日志目录
            File crashDir = new File(context.getFilesDir(), CRASH_DIR);
            if (!crashDir.exists()) {
                crashDir.mkdirs();
            }
            
            // 生成文件名
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String fileName = "crash_" + dateFormat.format(new Date()) + ".txt";
            File crashFile = new File(crashDir, fileName);
            
            // 写入文件
            FileOutputStream fos = new FileOutputStream(crashFile);
            fos.write(crashInfo.getBytes());
            fos.close();
            
            Log.d(TAG, "Crash info saved to: " + crashFile.getAbsolutePath());
            
            // 清理旧的崩溃文件（保留最近10个）
            cleanOldCrashFiles(crashDir);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to save crash info to file", e);
        }
    }
    
    /**
     * 清理旧的崩溃文件
     */
    private void cleanOldCrashFiles(File crashDir) {
        try {
            File[] files = crashDir.listFiles();
            if (files != null && files.length > 10) {
                // 按修改时间排序
                java.util.Arrays.sort(files, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
                
                // 删除最旧的文件
                for (int i = 0; i < files.length - 10; i++) {
                    if (files[i].delete()) {
                        Log.d(TAG, "Deleted old crash file: " + files[i].getName());
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cleaning old crash files", e);
        }
    }
    
    /**
     * 获取最近的崩溃文件
     */
    public File getLatestCrashFile() {
        try {
            File crashDir = new File(context.getFilesDir(), CRASH_DIR);
            if (!crashDir.exists()) {
                return null;
            }
            
            File[] files = crashDir.listFiles();
            if (files == null || files.length == 0) {
                return null;
            }
            
            // 找到最新的文件
            File latestFile = files[0];
            for (File file : files) {
                if (file.lastModified() > latestFile.lastModified()) {
                    latestFile = file;
                }
            }
            
            return latestFile;
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting latest crash file", e);
            return null;
        }
    }
}