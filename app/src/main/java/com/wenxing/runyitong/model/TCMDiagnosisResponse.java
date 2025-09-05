package com.wenxing.runyitong.model;

import com.google.gson.annotations.SerializedName;

/**
 * 中医诊断API响应包装类
 * 用于统一处理舌诊和面诊的API响应格式
 */
public class TCMDiagnosisResponse<T> {
    @SerializedName("success")
    private boolean success;        // 请求是否成功
    
    @SerializedName("message")
    private String message;         // 响应消息
    
    @SerializedName("data")
    private T data;                 // 响应数据（可以是TongueDiagnosisResult或FaceDiagnosisResult）
    
    @SerializedName("error_code")
    private String errorCode;       // 错误代码（可选）

    /**
     * 默认构造函数
     */
    public TCMDiagnosisResponse() {
    }

    /**
     * 带参数的构造函数
     * @param success 请求是否成功
     * @param message 响应消息
     * @param data 响应数据
     */
    public TCMDiagnosisResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * 带错误代码的构造函数
     * @param success 请求是否成功
     * @param message 响应消息
     * @param data 响应数据
     * @param errorCode 错误代码
     */
    public TCMDiagnosisResponse(boolean success, String message, T data, String errorCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
    }

    // Getter 方法
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getErrorCode() {
        return errorCode;
    }

    // Setter 方法
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * 创建成功响应的静态方法
     * @param message 成功消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应对象
     */
    public static <T> TCMDiagnosisResponse<T> success(String message, T data) {
        return new TCMDiagnosisResponse<>(true, message, data);
    }

    /**
     * 创建失败响应的静态方法
     * @param message 错误消息
     * @param errorCode 错误代码
     * @param <T> 数据类型
     * @return 失败响应对象
     */
    public static <T> TCMDiagnosisResponse<T> error(String message, String errorCode) {
        return new TCMDiagnosisResponse<>(false, message, null, errorCode);
    }

    /**
     * 创建失败响应的静态方法（无错误代码）
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 失败响应对象
     */
    public static <T> TCMDiagnosisResponse<T> error(String message) {
        return new TCMDiagnosisResponse<>(false, message, null);
    }

    @Override
    public String toString() {
        return "TCMDiagnosisResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", errorCode='" + errorCode + '\'' +
                '}';
    }
}