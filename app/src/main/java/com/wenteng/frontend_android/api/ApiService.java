package com.wenteng.frontend_android.api;

import com.wenteng.frontend_android.model.Product;
import com.wenteng.frontend_android.model.Book;
import com.wenteng.frontend_android.model.SymptomAnalysis;
import com.wenteng.frontend_android.model.OCRResult;
import com.wenteng.frontend_android.model.PrescriptionAnalysis;
import com.wenteng.frontend_android.model.ImageUploadResult;
import com.wenteng.frontend_android.model.Department;
import com.wenteng.frontend_android.model.PaymentOrderRequest;
import com.wenteng.frontend_android.model.PaymentOrderResponse;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import retrofit2.http.Body;
import java.util.List;

public interface ApiService {
    
    /**
     * 获取商品列表
     * @param skip 跳过的数量
     * @param limit 限制数量
     * @param search 搜索关键词
     * @param category 商品分类
     * @return 商品列表响应
     */
    @GET("api/v1/products/")
    Call<ApiResponse<ProductListResponse>> getProducts(
        @Query("skip") int skip,
        @Query("limit") int limit,
        @Query("search") String search,
        @Query("category") String category
    );
    
    /**
     * 获取单个商品详情
     * @param productId 商品ID
     * @return 商品详情响应
     */
    @GET("api/v1/products/{id}/")
    Call<ApiResponse<Product>> getProduct(@Path("id") int productId);
    
    /**
     * 获取中医古籍列表
     * @return 中医古籍列表响应
     */
    @GET("api/v1/books/chinese-medicine")
    Call<ApiResponse<List<Book>>> getChineseMedicineBooks();
    
    /**
     * 获取西医经典列表
     * @return 西医经典列表响应
     */
    @GET("api/v1/books/western-medicine")
    Call<ApiResponse<List<Book>>> getWesternMedicineBooks();
    
    /**
     * 获取单个图书详情
     * @param bookId 图书ID
     * @return 图书详情响应
     */
    @GET("api/v1/books/{id}")
    Call<ApiResponse<Book>> getBook(@Path("id") int bookId);
    
    /**
     * AI症状分析
     * @param symptoms 症状描述
     * @return 分析结果响应
     */
    @FormUrlEncoded
    @POST("api/v1/prescriptions/analyze-symptoms")
    Call<ApiResponse<SymptomAnalysis>> analyzeSymptoms(@Field("symptoms") String symptoms);
    
    /**
     * OCR文字识别
     * @param image 图片文件
     * @return OCR识别结果
     */
    @Multipart
    @POST("api/v1/prescriptions/ocr-text-recognition")
    Call<ApiResponse<OCRResult>> ocrTextRecognition(@Part MultipartBody.Part image);
    
    /**
     * 处方图片智能分析
     * @param image 处方图片文件
     * @return 智能分析结果
     */
    @Multipart
    @POST("api/v1/prescriptions/analyze-prescription-image")
    Call<ApiResponse<PrescriptionAnalysis>> analyzePrescriptionImage(@Part MultipartBody.Part image);
    
    /**
     * 通用图片上传
     * @param image 图片文件
     * @return 上传结果
     */
    @Multipart
    @POST("api/v1/prescriptions/upload-image")
    Call<ApiResponse<ImageUploadResult>> uploadImage(@Part MultipartBody.Part image);
    
    /**
     * X光影像智能分析
     * @param image X光影像文件
     * @return X光分析结果
     */
    @Multipart
    @POST("api/v1/prescriptions/analyze-xray")
    Call<ApiResponse<PrescriptionAnalysis>> analyzeXRayImage(@Part MultipartBody.Part image);
    
    /**
     * CT影像智能分析
     * @param image CT影像文件
     * @return CT分析结果
     */
    @Multipart
    @POST("api/v1/prescriptions/analyze-ct")
    Call<ApiResponse<PrescriptionAnalysis>> analyzeCTImage(@Part MultipartBody.Part image);
    
    /**
     * B超影像智能分析
     * @param image B超影像文件
     * @return B超分析结果
     */
    @Multipart
    @POST("api/v1/prescriptions/analyze-ultrasound")
    Call<ApiResponse<PrescriptionAnalysis>> analyzeUltrasoundImage(@Part MultipartBody.Part image);
    
    /**
     * MRI影像智能分析
     * @param image MRI影像文件
     * @return MRI分析结果
     */
    @Multipart
    @POST("api/v1/prescriptions/analyze-mri")
    Call<ApiResponse<PrescriptionAnalysis>> analyzeMRIImage(@Part MultipartBody.Part image);
    
    /**
     * PET-CT影像智能分析
     * @param image PET-CT影像文件
     * @return PET-CT分析结果
     */
    @Multipart
    @POST("api/v1/prescriptions/analyze-petct")
    Call<ApiResponse<PrescriptionAnalysis>> analyzePETCTImage(@Part MultipartBody.Part image);
    
    /**
     * 获取科室列表
     * @return 科室列表响应
     */
    @GET("api/v1/appointments/departments")
    Call<ApiResponse<DepartmentListResponse>> getDepartments();
    
    /**
     * 获取医院列表
     * @return 医院列表响应
     */
    @GET("api/v1/appointments/hospitals")
    Call<ApiResponse<HospitalListResponse>> getHospitals();
    
    /**
     * 获取医生列表
     * @param departmentId 科室ID（可选）
     * @param hospitalId 医院ID（可选）
     * @return 医生列表响应
     */
    @GET("api/v1/appointments/doctors")
    Call<ApiResponse<DoctorListResponse>> getDoctors(
        @Query("department_id") Integer departmentId,
        @Query("hospital_id") Integer hospitalId
    );
    
    /**
     * 获取指定医院的科室列表
     * @param hospitalId 医院ID
     * @return 科室列表响应
     */
    @GET("api/v1/appointments/hospitals/{hospital_id}/departments")
    Call<ApiResponse<DepartmentListResponse>> getHospitalDepartments(@Path("hospital_id") int hospitalId);
    
    /**
     * 根据医院ID获取科室列表
     * @param hospitalId 医院ID
     * @return 科室列表
     */
    @GET("api/v1/appointments/hospitals/{hospital_id}/departments")
    Call<List<Department>> getDepartmentsByHospital(@Path("hospital_id") int hospitalId);
    
    /**
     * 用户密码注册
     * @param username 用户名
     * @param email 邮箱
     * @param password 密码
     * @return 注册结果响应
     */
    @FormUrlEncoded
    @POST("api/v1/auth/register/password")
    Call<ApiResponse<RegisterResponse>> registerWithPassword(
        @Field("username") String username,
        @Field("email") String email,
        @Field("password") String password
    );
    
    /**
     * 用户短信验证码注册
     * @param username 用户名
     * @param phone 手机号
     * @param verificationCode 验证码
     * @param password 密码
     * @return 注册结果响应
     */
    @FormUrlEncoded
    @POST("api/v1/auth/register/sms")
    Call<ApiResponse<RegisterResponse>> registerWithSms(
        @Field("username") String username,
        @Field("phone") String phone,
        @Field("verification_code") String verificationCode,
        @Field("password") String password
    );
    
    /**
     * 发送短信验证码
     * @param phone 手机号
     * @return 发送结果响应
     */
    @FormUrlEncoded
    @POST("api/v1/auth/send-sms-code")
    Call<ApiResponse<SmsCodeResponse>> sendSmsCode(@Field("phone") String phone);
    
    /**
     * 创建支付宝支付订单
     * @param request 支付订单请求参数
     * @return 支付订单响应
     */
    @POST("api/v1/payments/alipay/create-order")
    Call<ApiResponse<PaymentOrderResponse>> createAlipayOrder(
        @Body PaymentOrderRequest request
    );
    
    /**
     * 创建微信支付订单
     * @param request 支付订单请求参数
     * @return 支付订单响应
     */
    @POST("api/v1/payments/wechat/create-order")
    Call<ApiResponse<PaymentOrderResponse>> createWechatOrder(
        @Body PaymentOrderRequest request
    );
}