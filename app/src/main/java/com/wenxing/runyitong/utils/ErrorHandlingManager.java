package com.wenxing.runyitong.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import retrofit2.HttpException;

/**
 * 错误处理管理器
 * 提供统一的错误处理机制，包括网络错误、API错误、异常恢复等
 */
public class ErrorHandlingManager {
    
    private static final String TAG = "ErrorHandlingManager";
    
    /**
     * 错误类型枚举
     */
    public enum ErrorType {
        NETWORK_ERROR,          // 网络连接错误
        TIMEOUT_ERROR,          // 超时错误
        SERVER_ERROR,           // 服务器错误
        CLIENT_ERROR,           // 客户端错误
        AUTH_ERROR,             // 认证错误
        VALIDATION_ERROR,       // 验证错误
        UNKNOWN_ERROR           // 未知错误
    }
    
    /**
     * 错误信息类
     */
    public static class ErrorInfo {
        private final ErrorType type;
        private final String userMessage;
        private final String technicalMessage;
        private final int errorCode;
        private final boolean isRetryable;
        private final String[] suggestions;
        
        public ErrorInfo(ErrorType type, String userMessage, String technicalMessage, 
                        int errorCode, boolean isRetryable, String... suggestions) {
            this.type = type;
            this.userMessage = userMessage;
            this.technicalMessage = technicalMessage;
            this.errorCode = errorCode;
            this.isRetryable = isRetryable;
            this.suggestions = suggestions;
        }
        
        public ErrorType getType() { return type; }
        public String getUserMessage() { return userMessage; }
        public String getTechnicalMessage() { return technicalMessage; }
        public int getErrorCode() { return errorCode; }
        public boolean isRetryable() { return isRetryable; }
        public String[] getSuggestions() { return suggestions; }
    }
    
    /**
     * 处理网络请求异常
     * @param context 上下文
     * @param throwable 异常
     * @return 错误信息
     */
    public static ErrorInfo handleNetworkError(Context context, Throwable throwable) {
        if (throwable == null) {
            return new ErrorInfo(ErrorType.UNKNOWN_ERROR, 
                "发生未知错误", "Throwable is null", -1, false, 
                "请重试或联系客服");
        }
        
        Log.e(TAG, "处理网络错误: " + throwable.getClass().getSimpleName(), throwable);
        
        // 检查网络连接
        if (!isNetworkAvailable(context)) {
            return new ErrorInfo(ErrorType.NETWORK_ERROR,
                "网络连接不可用", "No network connection", 0, true,
                "请检查网络连接", "确保WiFi或移动数据已开启", "稍后重试");
        }
        
        // HTTP异常
        if (throwable instanceof HttpException) {
            HttpException httpEx = (HttpException) throwable;
            int code = httpEx.code();
            
            switch (code) {
                case 400:
                    return new ErrorInfo(ErrorType.CLIENT_ERROR,
                        "请求参数错误", "Bad Request (400)", code, false,
                        "请检查输入信息", "确保所有必填字段已填写");
                        
                case 401:
                    return new ErrorInfo(ErrorType.AUTH_ERROR,
                        "登录已过期，请重新登录", "Unauthorized (401)", code, false,
                        "请重新登录", "检查用户名和密码是否正确");
                        
                case 403:
                    return new ErrorInfo(ErrorType.AUTH_ERROR,
                        "没有访问权限", "Forbidden (403)", code, false,
                        "联系管理员获取权限");
                        
                case 404:
                    return new ErrorInfo(ErrorType.CLIENT_ERROR,
                        "请求的资源不存在", "Not Found (404)", code, false,
                        "请检查操作是否正确", "稍后重试");
                        
                case 429:
                    return new ErrorInfo(ErrorType.CLIENT_ERROR,
                        "请求过于频繁，请稍后再试", "Too Many Requests (429)", code, true,
                        "等待一段时间后重试", "避免频繁操作");
                        
                case 500:
                    return new ErrorInfo(ErrorType.SERVER_ERROR,
                        "服务器内部错误", "Internal Server Error (500)", code, true,
                        "稍后重试", "如果问题持续，请联系客服");
                        
                case 502:
                case 503:
                case 504:
                    return new ErrorInfo(ErrorType.SERVER_ERROR,
                        "服务暂时不可用", "Service Unavailable (" + code + ")", code, true,
                        "稍后重试", "服务器可能正在维护");
                        
                default:
                    if (code >= 400 && code < 500) {
                        return new ErrorInfo(ErrorType.CLIENT_ERROR,
                            "请求错误 (" + code + ")", "Client Error (" + code + ")", code, false,
                            "请检查输入信息", "稍后重试");
                    } else if (code >= 500) {
                        return new ErrorInfo(ErrorType.SERVER_ERROR,
                            "服务器错误 (" + code + ")", "Server Error (" + code + ")", code, true,
                            "稍后重试", "如果问题持续，请联系客服");
                    }
            }
        }
        
        // 超时异常
        if (throwable instanceof SocketTimeoutException || throwable instanceof TimeoutException) {
            return new ErrorInfo(ErrorType.TIMEOUT_ERROR,
                "请求超时", "Request timeout", 0, true,
                "检查网络连接", "稍后重试", "如果网络较慢，请耐心等待");
        }
        
        // 网络连接异常
        if (throwable instanceof UnknownHostException) {
            return new ErrorInfo(ErrorType.NETWORK_ERROR,
                "无法连接到服务器", "Unknown host", 0, true,
                "检查网络连接", "确认服务器地址是否正确", "稍后重试");
        }
        
        // IO异常
        if (throwable instanceof IOException) {
            return new ErrorInfo(ErrorType.NETWORK_ERROR,
                "网络连接异常", "IO Exception: " + throwable.getMessage(), 0, true,
                "检查网络连接", "稍后重试");
        }
        
        // 其他异常
        return new ErrorInfo(ErrorType.UNKNOWN_ERROR,
            "发生未知错误", throwable.getMessage(), -1, true,
            "稍后重试", "如果问题持续，请联系客服");
    }
    
    /**
     * 检查网络是否可用
     * @param context 上下文
     * @return 是否可用
     */
    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager connectivityManager = 
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            
            if (connectivityManager == null) {
                return false;
            }
            
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } catch (Exception e) {
            Log.e(TAG, "检查网络状态失败", e);
            return false;
        }
    }
    
    /**
     * 获取网络类型
     * @param context 上下文
     * @return 网络类型描述
     */
    public static String getNetworkType(Context context) {
        try {
            ConnectivityManager connectivityManager = 
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            
            if (connectivityManager == null) {
                return "未知";
            }
            
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
                return "无连接";
            }
            
            switch (activeNetworkInfo.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    return "WiFi";
                case ConnectivityManager.TYPE_MOBILE:
                    return "移动数据";
                case ConnectivityManager.TYPE_ETHERNET:
                    return "以太网";
                default:
                    return "其他";
            }
        } catch (Exception e) {
            Log.e(TAG, "获取网络类型失败", e);
            return "未知";
        }
    }
    
    /**
     * 处理API响应错误
     * @param errorCode API错误代码
     * @param errorMessage API错误消息
     * @return 错误信息
     */
    public static ErrorInfo handleApiError(int errorCode, String errorMessage) {
        switch (errorCode) {
            case 1001:
                return new ErrorInfo(ErrorType.VALIDATION_ERROR,
                    "用户名或密码错误", "Invalid credentials", errorCode, false,
                    "请检查用户名和密码", "确认大小写是否正确");
                    
            case 1002:
                return new ErrorInfo(ErrorType.AUTH_ERROR,
                    "账户已被锁定", "Account locked", errorCode, false,
                    "联系管理员解锁账户", "或等待锁定时间结束");
                    
            case 1003:
                return new ErrorInfo(ErrorType.VALIDATION_ERROR,
                    "验证码错误或已过期", "Invalid verification code", errorCode, false,
                    "重新获取验证码", "检查验证码是否输入正确");
                    
            case 2001:
                return new ErrorInfo(ErrorType.VALIDATION_ERROR,
                    "用户名已存在", "Username already exists", errorCode, false,
                    "尝试其他用户名", "添加数字或下划线");
                    
            case 2002:
                return new ErrorInfo(ErrorType.VALIDATION_ERROR,
                    "邮箱已被注册", "Email already registered", errorCode, false,
                    "使用其他邮箱", "或找回已有账户");
                    
            case 2003:
                return new ErrorInfo(ErrorType.VALIDATION_ERROR,
                    "手机号已被注册", "Phone already registered", errorCode, false,
                    "使用其他手机号", "或找回已有账户");
                    
            case 3001:
                return new ErrorInfo(ErrorType.SERVER_ERROR,
                    "短信发送失败", "SMS sending failed", errorCode, true,
                    "稍后重试", "检查手机号是否正确");
                    
            case 3002:
                return new ErrorInfo(ErrorType.CLIENT_ERROR,
                    "短信发送过于频繁", "SMS rate limit exceeded", errorCode, true,
                    "等待60秒后重试", "避免频繁请求验证码");
                    
            default:
                return new ErrorInfo(ErrorType.UNKNOWN_ERROR,
                    errorMessage != null ? errorMessage : "未知错误", 
                    "API Error: " + errorCode, errorCode, true,
                    "稍后重试", "如果问题持续，请联系客服");
        }
    }
    
    /**
     * 格式化用户友好的错误消息
     * @param errorInfo 错误信息
     * @return 格式化的消息
     */
    public static String formatUserFriendlyMessage(ErrorInfo errorInfo) {
        StringBuilder message = new StringBuilder();
        message.append(errorInfo.getUserMessage());
        
        if (errorInfo.getSuggestions().length > 0) {
            message.append(System.lineSeparator()).append(System.lineSeparator()).append("建议：");
            for (int i = 0; i < errorInfo.getSuggestions().length; i++) {
                message.append(System.lineSeparator()).append("• ").append(errorInfo.getSuggestions()[i]);
            }
        }
        
        return message.toString();
    }
    
    /**
     * 错误恢复策略
     */
    public static class RecoveryStrategy {
        private final String action;
        private final String description;
        private final Runnable recovery;
        
        public RecoveryStrategy(String action, String description, Runnable recovery) {
            this.action = action;
            this.description = description;
            this.recovery = recovery;
        }
        
        public String getAction() { return action; }
        public String getDescription() { return description; }
        public void execute() { 
            if (recovery != null) {
                recovery.run();
            }
        }
    }
    
    /**
     * 获取错误恢复策略
     * @param errorInfo 错误信息
     * @param retryAction 重试操作
     * @return 恢复策略数组
     */
    public static RecoveryStrategy[] getRecoveryStrategies(ErrorInfo errorInfo, Runnable retryAction) {
        java.util.List<RecoveryStrategy> strategies = new java.util.ArrayList<>();
        
        // 如果错误可重试，添加重试策略
        if (errorInfo.isRetryable() && retryAction != null) {
            strategies.add(new RecoveryStrategy("重试", "再次尝试当前操作", retryAction));
        }
        
        // 根据错误类型添加特定策略
        switch (errorInfo.getType()) {
            case NETWORK_ERROR:
                strategies.add(new RecoveryStrategy("检查网络", "检查WiFi或移动数据连接", null));
                break;
                
            case AUTH_ERROR:
                strategies.add(new RecoveryStrategy("重新登录", "清除登录状态并重新登录", null));
                break;
                
            case TIMEOUT_ERROR:
                strategies.add(new RecoveryStrategy("稍后重试", "等待网络环境改善后重试", null));
                break;
                
            case VALIDATION_ERROR:
                strategies.add(new RecoveryStrategy("检查输入", "确认所有输入信息正确", null));
                break;
                
            case SERVER_ERROR:
                strategies.add(new RecoveryStrategy("联系客服", "如果问题持续，请联系客服", null));
                break;
        }
        
        return strategies.toArray(new RecoveryStrategy[0]);
    }
    
    /**
     * 记录错误日志
     * @param errorInfo 错误信息
     * @param context 附加上下文信息
     */
    public static void logError(ErrorInfo errorInfo, String context) {
        Log.e(TAG, String.format(
            "Error occurred - Type: %s, Code: %d, Context: %s, Technical: %s, User: %s",
            errorInfo.getType().name(),
            errorInfo.getErrorCode(),
            context,
            errorInfo.getTechnicalMessage(),
            errorInfo.getUserMessage()
        ));
        
        // 这里可以添加错误上报到服务器的逻辑
        // reportErrorToServer(errorInfo, context);
    }
    
    /**
     * 简化的错误处理方法
     * @param context 上下文
     * @param throwable 异常
     * @param contextInfo 上下文信息
     * @return 用户友好的错误消息
     */
    public static String handleErrorSimple(Context context, Throwable throwable, String contextInfo) {
        ErrorInfo errorInfo = handleNetworkError(context, throwable);
        logError(errorInfo, contextInfo);
        return formatUserFriendlyMessage(errorInfo);
    }
}