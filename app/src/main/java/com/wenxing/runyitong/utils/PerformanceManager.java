package com.wenxing.runyitong.utils;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 性能优化管理器
 * 提供内存管理、网络请求优化、页面加载优化等功能
 */
public class PerformanceManager {
    
    private static final String TAG = "PerformanceManager";
    
    // 单例实例
    private static volatile PerformanceManager instance;
    
    // 线程池
    private final ExecutorService backgroundExecutor;
    private final Handler mainHandler;
    
    // 网络请求缓存
    private final ConcurrentHashMap<String, CacheEntry> requestCache;
    
    // 内存监控
    private final Runtime runtime;
    
    // 常量
    private static final int CACHE_EXPIRE_TIME_MS = 5 * 60 * 1000; // 5分钟
    private static final int MAX_CACHE_SIZE = 50;
    private static final long MEMORY_WARNING_THRESHOLD = 80; // 80%内存使用率警告
    
    private PerformanceManager() {
        backgroundExecutor = Executors.newFixedThreadPool(3);
        mainHandler = new Handler(Looper.getMainLooper());
        requestCache = new ConcurrentHashMap<>();
        runtime = Runtime.getRuntime();
        
        // 启动内存监控
        startMemoryMonitoring();
    }
    
    public static PerformanceManager getInstance() {
        if (instance == null) {
            synchronized (PerformanceManager.class) {
                if (instance == null) {
                    instance = new PerformanceManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 缓存条目
     */
    private static class CacheEntry {
        final Object data;
        final long timestamp;
        
        CacheEntry(Object data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRE_TIME_MS;
        }
    }
    
    /**
     * 页面加载性能监控
     */
    public static class PageLoadMonitor {
        private final String pageName;
        private final long startTime;
        private long endTime;
        
        public PageLoadMonitor(String pageName) {
            this.pageName = pageName;
            this.startTime = System.currentTimeMillis();
            Log.d(TAG, "页面开始加载: " + pageName);
        }
        
        public void onPageLoadComplete() {
            endTime = System.currentTimeMillis();
            long loadTime = endTime - startTime;
            Log.d(TAG, "页面加载完成: " + pageName + ", 耗时: " + loadTime + "ms");
            
            if (loadTime > 3000) {
                Log.w(TAG, "页面加载较慢: " + pageName + ", 建议优化");
            }
        }
        
        public void onViewReady(View view) {
            if (view != null) {
                view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        long layoutTime = System.currentTimeMillis() - startTime;
                        Log.d(TAG, "布局完成: " + pageName + ", 耗时: " + layoutTime + "ms");
                    }
                });
            }
        }
    }
    
    /**
     * 异步执行任务
     * @param task 后台任务
     * @param callback 主线程回调
     * @param <T> 结果类型
     * @return Future对象，可用于取消任务
     */
    public <T> Future<?> executeAsync(BackgroundTask<T> task, MainThreadCallback<T> callback) {
        return backgroundExecutor.submit(() -> {
            try {
                T result = task.execute();
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onSuccess(result);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "后台任务执行失败", e);
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onError(e);
                    }
                });
            }
        });
    }
    
    /**
     * 后台任务接口
     */
    public interface BackgroundTask<T> {
        T execute() throws Exception;
    }
    
    /**
     * 主线程回调接口
     */
    public interface MainThreadCallback<T> {
        void onSuccess(T result);
        void onError(Exception error);
    }
    
    /**
     * 缓存网络请求结果
     * @param key 缓存键
     * @param data 缓存数据
     */
    public void cacheRequestResult(String key, Object data) {
        if (key == null || data == null) return;
        
        // 如果缓存太大，清理过期条目
        if (requestCache.size() >= MAX_CACHE_SIZE) {
            cleanExpiredCache();
        }
        
        requestCache.put(key, new CacheEntry(data));
        Log.d(TAG, "缓存请求结果: " + key);
    }
    
    /**
     * 获取缓存的请求结果
     * @param key 缓存键
     * @return 缓存数据，如果不存在或已过期返回null
     */
    public Object getCachedResult(String key) {
        if (key == null) return null;
        
        CacheEntry entry = requestCache.get(key);
        if (entry == null) return null;
        
        if (entry.isExpired()) {
            requestCache.remove(key);
            return null;
        }
        
        Log.d(TAG, "使用缓存结果: " + key);
        return entry.data;
    }
    
    /**
     * 清理过期缓存
     */
    private void cleanExpiredCache() {
        int removedCount = 0;
        for (String key : requestCache.keySet()) {
            CacheEntry entry = requestCache.get(key);
            if (entry != null && entry.isExpired()) {
                requestCache.remove(key);
                removedCount++;
            }
        }
        Log.d(TAG, "清理过期缓存: " + removedCount + " 个条目");
    }
    
    /**
     * 清空所有缓存
     */
    public void clearAllCache() {
        requestCache.clear();
        Log.d(TAG, "已清空所有缓存");
    }
    
    /**
     * 获取当前内存使用情况
     * @return 内存使用百分比
     */
    public double getMemoryUsagePercentage() {
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        return (double) usedMemory / maxMemory * 100;
    }
    
    /**
     * 检查内存使用情况
     * @return 内存状态信息
     */
    public MemoryInfo getMemoryInfo() {
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        double usagePercentage = (double) usedMemory / maxMemory * 100;
        boolean isLowMemory = usagePercentage > MEMORY_WARNING_THRESHOLD;
        
        return new MemoryInfo(usedMemory, totalMemory, maxMemory, usagePercentage, isLowMemory);
    }
    
    /**
     * 内存信息类
     */
    public static class MemoryInfo {
        public final long usedMemory;
        public final long totalMemory;
        public final long maxMemory;
        public final double usagePercentage;
        public final boolean isLowMemory;
        
        public MemoryInfo(long usedMemory, long totalMemory, long maxMemory, 
                         double usagePercentage, boolean isLowMemory) {
            this.usedMemory = usedMemory;
            this.totalMemory = totalMemory;
            this.maxMemory = maxMemory;
            this.usagePercentage = usagePercentage;
            this.isLowMemory = isLowMemory;
        }
        
        public String getFormattedInfo() {
            return String.format("内存使用: %.1f%% (%dMB/%dMB)", 
                usagePercentage, 
                usedMemory / 1024 / 1024, 
                maxMemory / 1024 / 1024);
        }
    }
    
    /**
     * 启动内存监控
     */
    private void startMemoryMonitoring() {
        backgroundExecutor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(30000); // 每30秒检查一次
                    
                    MemoryInfo memoryInfo = getMemoryInfo();
                    if (memoryInfo.isLowMemory) {
                        Log.w(TAG, "内存使用率较高: " + memoryInfo.getFormattedInfo());
                        
                        // 自动清理缓存
                        cleanExpiredCache();
                        
                        // 建议进行垃圾回收
                        System.gc();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    Log.e(TAG, "内存监控异常", e);
                }
            }
        });
    }
    
    /**
     * 优化Activity内存使用
     * @param activity Activity实例
     */
    public static void optimizeActivityMemory(Activity activity) {
        if (activity == null) return;
        
        // 使用WeakReference防止内存泄漏
        WeakReference<Activity> weakActivity = new WeakReference<>(activity);
        
        // 在Activity销毁时清理资源
        activity.getApplication().registerActivityLifecycleCallbacks(
            new android.app.Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityDestroyed(Activity destroyedActivity) {
                    if (destroyedActivity == weakActivity.get()) {
                        // 清理操作
                        getInstance().clearActivityCache(destroyedActivity.getClass().getSimpleName());
                        activity.getApplication().unregisterActivityLifecycleCallbacks(this);
                    }
                }
                
                @Override
                public void onActivityCreated(Activity activity, android.os.Bundle savedInstanceState) {}
                @Override
                public void onActivityStarted(Activity activity) {}
                @Override
                public void onActivityResumed(Activity activity) {}
                @Override
                public void onActivityPaused(Activity activity) {}
                @Override
                public void onActivityStopped(Activity activity) {}
                @Override
                public void onActivitySaveInstanceState(Activity activity, android.os.Bundle outState) {}
            });
    }
    
    /**
     * 清理特定Activity的缓存
     * @param activityName Activity名称
     */
    private void clearActivityCache(String activityName) {
        int removedCount = 0;
        for (String key : requestCache.keySet()) {
            if (key.contains(activityName)) {
                requestCache.remove(key);
                removedCount++;
            }
        }
        if (removedCount > 0) {
            Log.d(TAG, "清理Activity缓存: " + activityName + ", " + removedCount + " 个条目");
        }
    }
    
    /**
     * 延迟执行任务
     * @param task 任务
     * @param delayMs 延迟时间（毫秒）
     * @return Runnable，可用于取消任务
     */
    public Runnable postDelayed(Runnable task, long delayMs) {
        Runnable wrapper = () -> {
            try {
                task.run();
            } catch (Exception e) {
                Log.e(TAG, "延迟任务执行失败", e);
            }
        };
        
        mainHandler.postDelayed(wrapper, delayMs);
        return wrapper;
    }
    
    /**
     * 移除延迟任务
     * @param task 要移除的任务
     */
    public void removeDelayedTask(Runnable task) {
        mainHandler.removeCallbacks(task);
    }
    
    /**
     * 批量处理任务
     * @param tasks 任务列表
     * @param batchSize 批次大小
     * @param batchDelay 批次间延迟（毫秒）
     */
    public void processBatch(java.util.List<Runnable> tasks, int batchSize, long batchDelay) {
        if (tasks == null || tasks.isEmpty()) return;
        
        backgroundExecutor.submit(() -> {
            for (int i = 0; i < tasks.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, tasks.size());
                java.util.List<Runnable> batch = tasks.subList(i, endIndex);
                
                // 在主线程执行批次任务
                mainHandler.post(() -> {
                    for (Runnable task : batch) {
                        try {
                            task.run();
                        } catch (Exception e) {
                            Log.e(TAG, "批次任务执行失败", e);
                        }
                    }
                });
                
                // 批次间延迟
                if (i + batchSize < tasks.size()) {
                    try {
                        Thread.sleep(batchDelay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });
    }
    
    /**
     * 防抖动处理
     * @param key 防抖键
     * @param task 任务
     * @param delayMs 延迟时间
     */
    private final ConcurrentHashMap<String, Runnable> debounceMap = new ConcurrentHashMap<>();
    
    public void debounce(String key, Runnable task, long delayMs) {
        // 取消之前的任务
        Runnable previousTask = debounceMap.get(key);
        if (previousTask != null) {
            mainHandler.removeCallbacks(previousTask);
        }
        
        // 创建新任务
        Runnable newTask = () -> {
            debounceMap.remove(key);
            task.run();
        };
        
        debounceMap.put(key, newTask);
        mainHandler.postDelayed(newTask, delayMs);
    }
    
    /**
     * 关闭性能管理器
     */
    public void shutdown() {
        if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdown();
        }
        clearAllCache();
        debounceMap.clear();
    }
}