package com.wenxing.runyitong.utils;

import android.util.Log;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.wenxing.runyitong.api.ApiClient;
import com.wenxing.runyitong.api.ApiResponse;
import com.wenxing.runyitong.api.ApiService;
import com.wenxing.runyitong.model.FaceDiagnosisResult;

/**
 * 网络请求调试辅助类
 * 用于调试Android应用中的网络请求问题，特别是回调函数执行问题
 */
public class NetworkDebugHelper {
    private static final String TAG = "NetworkDebugHelper";
    
    /**
     * 调试接口，用于接收调试信息
     */
    public interface DebugCallback {
        void onDebugMessage(String message);
        void onSuccess(String message);
        void onError(String message);
    }
    
    /**
     * 测试面诊API连接
     * @param imagePart 图片数据
     * @param debugCallback 调试回调
     */
    public static void testFaceDiagnosisConnection(MultipartBody.Part imagePart, DebugCallback debugCallback) {
        Log.d(TAG, "=== 开始网络请求调试测试 ===");
        debugCallback.onDebugMessage("开始网络请求调试测试");
        
        try {
            // 获取API服务实例
            ApiService apiService = ApiClient.getApiService();
            Log.d(TAG, "API服务实例获取成功: " + (apiService != null));
            debugCallback.onDebugMessage("API服务实例: " + (apiService != null ? "成功" : "失败"));
            
            if (apiService == null) {
                debugCallback.onError("API服务实例为null");
                return;
            }
            
            // 创建API调用
            Call<ApiResponse<FaceDiagnosisResult>> call = apiService.analyzeFaceImage(imagePart);
            Log.d(TAG, "API调用对象创建成功: " + (call != null));
            debugCallback.onDebugMessage("API调用对象: " + (call != null ? "成功" : "失败"));
            
            if (call == null) {
                debugCallback.onError("API调用对象为null");
                return;
            }
            
            // 记录请求信息
            Log.d(TAG, "请求URL: " + call.request().url());
            Log.d(TAG, "请求方法: " + call.request().method());
            Log.d(TAG, "请求头: " + call.request().headers());
            debugCallback.onDebugMessage("请求URL: " + call.request().url());
            debugCallback.onDebugMessage("请求方法: " + call.request().method());
            
            // 执行异步请求
            Log.d(TAG, "开始执行异步请求...");
            debugCallback.onDebugMessage("开始执行异步请求...");
            
            call.enqueue(new Callback<ApiResponse<FaceDiagnosisResult>>() {
                @Override
                public void onResponse(Call<ApiResponse<FaceDiagnosisResult>> call, Response<ApiResponse<FaceDiagnosisResult>> response) {
                    Log.d(TAG, "=== onResponse回调被调用 ===");
                    debugCallback.onDebugMessage("✅ onResponse回调被调用");
                    
                    try {
                        Log.d(TAG, "响应码: " + response.code());
                        Log.d(TAG, "响应消息: " + response.message());
                        Log.d(TAG, "响应成功: " + response.isSuccessful());
                        debugCallback.onDebugMessage("响应码: " + response.code());
                        debugCallback.onDebugMessage("响应成功: " + response.isSuccessful());
                        
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<FaceDiagnosisResult> apiResponse = response.body();
                            Log.d(TAG, "API响应体不为null");
                            Log.d(TAG, "API成功标志: " + apiResponse.isSuccess());
                            Log.d(TAG, "API消息: " + apiResponse.getMessage());
                            
                            debugCallback.onDebugMessage("API响应体: 不为null");
                            debugCallback.onDebugMessage("API成功: " + apiResponse.isSuccess());
                            debugCallback.onDebugMessage("API消息: " + apiResponse.getMessage());
                            
                            if (apiResponse.isSuccess()) {
                                FaceDiagnosisResult data = apiResponse.getData();
                                Log.d(TAG, "面诊数据: " + (data != null ? "存在" : "为null"));
                                debugCallback.onSuccess("面诊API调用成功，数据: " + (data != null ? "存在" : "为null"));
                            } else {
                                Log.d(TAG, "API返回失败: " + apiResponse.getMessage());
                                debugCallback.onError("API返回失败: " + apiResponse.getMessage());
                            }
                        } else {
                            Log.e(TAG, "HTTP响应失败或响应体为null");
                            Log.e(TAG, "响应体: " + response.body());
                            Log.e(TAG, "错误体: " + response.errorBody());
                            debugCallback.onError("HTTP响应失败: " + response.code() + " " + response.message());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onResponse处理异常: " + e.getMessage(), e);
                        debugCallback.onError("onResponse处理异常: " + e.getMessage());
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<FaceDiagnosisResult>> call, Throwable t) {
                    Log.d(TAG, "=== onFailure回调被调用 ===");
                    debugCallback.onDebugMessage("❌ onFailure回调被调用");
                    
                    try {
                        Log.e(TAG, "网络请求失败: " + t.getClass().getSimpleName());
                        Log.e(TAG, "错误消息: " + t.getMessage());
                        Log.e(TAG, "调用是否被取消: " + call.isCanceled());
                        
                        debugCallback.onError("网络请求失败: " + t.getClass().getSimpleName());
                        debugCallback.onError("错误消息: " + t.getMessage());
                        debugCallback.onError("调用取消: " + call.isCanceled());
                        
                        // 详细的异常类型分析
                        if (t instanceof java.net.ConnectException) {
                            debugCallback.onError("连接异常: 无法连接到服务器");
                        } else if (t instanceof java.net.SocketTimeoutException) {
                            debugCallback.onError("超时异常: 请求超时");
                        } else if (t instanceof java.io.IOException) {
                            debugCallback.onError("IO异常: 网络IO错误");
                        } else if (t instanceof com.google.gson.JsonSyntaxException) {
                            debugCallback.onError("JSON异常: 响应格式错误");
                        } else {
                            debugCallback.onError("未知异常: " + t.getClass().getSimpleName());
                        }
                        
                        // 打印完整的堆栈跟踪
                        Log.e(TAG, "完整异常信息:", t);
                        
                    } catch (Exception e) {
                        Log.e(TAG, "onFailure处理异常: " + e.getMessage(), e);
                        debugCallback.onError("onFailure处理异常: " + e.getMessage());
                    }
                }
            });
            
            Log.d(TAG, "异步请求已提交，等待回调...");
            debugCallback.onDebugMessage("异步请求已提交，等待回调...");
            
        } catch (Exception e) {
            Log.e(TAG, "测试过程中发生异常: " + e.getMessage(), e);
            debugCallback.onError("测试异常: " + e.getMessage());
        }
    }
    
    /**
     * 测试基本的网络连接
     * @param debugCallback 调试回调
     */
    public static void testBasicConnection(DebugCallback debugCallback) {
        Log.d(TAG, "=== 开始基本连接测试 ===");
        debugCallback.onDebugMessage("开始基本连接测试");
        
        try {
            ApiService apiService = ApiClient.getApiService();
            
            // 测试健康检查端点
            Call<Object> healthCall = apiService.healthCheck();
            
            healthCall.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    Log.d(TAG, "健康检查onResponse被调用");
                    debugCallback.onDebugMessage("✅ 健康检查onResponse被调用");
                    debugCallback.onDebugMessage("健康检查响应码: " + response.code());
                    
                    if (response.isSuccessful()) {
                        debugCallback.onSuccess("基本网络连接正常");
                    } else {
                        debugCallback.onError("健康检查失败: " + response.code());
                    }
                }
                
                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Log.d(TAG, "健康检查onFailure被调用");
                    debugCallback.onDebugMessage("❌ 健康检查onFailure被调用");
                    debugCallback.onError("基本连接失败: " + t.getMessage());
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "基本连接测试异常: " + e.getMessage(), e);
            debugCallback.onError("基本连接测试异常: " + e.getMessage());
        }
    }
    
    /**
     * 记录当前线程信息
     */
    public static void logThreadInfo() {
        Thread currentThread = Thread.currentThread();
        Log.d(TAG, "当前线程: " + currentThread.getName());
        Log.d(TAG, "是否为主线程: " + (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()));
        Log.d(TAG, "线程ID: " + currentThread.getId());
    }
    
    /**
     * 记录网络配置信息
     */
    public static void logNetworkConfig() {
        try {
            Log.d(TAG, "=== 网络配置信息 ===");
            Log.d(TAG, "Retrofit实例: " + (ApiClient.getRetrofitInstance() != null));
            Log.d(TAG, "API服务实例: " + (ApiClient.getApiService() != null));
            
            // 记录基础URL
            if (ApiClient.getRetrofitInstance() != null) {
                Log.d(TAG, "基础URL: " + ApiClient.getRetrofitInstance().baseUrl());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "记录网络配置异常: " + e.getMessage(), e);
        }
    }
}