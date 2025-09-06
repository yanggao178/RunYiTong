package com.wenxing.runyitong.api;

import com.wenxing.runyitong.model.PrescriptionCreate;
import com.wenxing.runyitong.model.Product;
import com.wenxing.runyitong.model.Book;
import com.wenxing.runyitong.model.BookPage;
import com.wenxing.runyitong.model.SymptomAnalysis;
import com.wenxing.runyitong.model.HealthRecord;
import com.wenxing.runyitong.model.PhysicalExamReport;
import com.wenxing.runyitong.model.OCRResult;
import com.wenxing.runyitong.model.PrescriptionAnalysis;
import com.wenxing.runyitong.model.MedicalImageAnalysis;
import com.wenxing.runyitong.model.ImageUploadResult;
import com.wenxing.runyitong.model.TongueDiagnosisResult;
import com.wenxing.runyitong.model.FaceDiagnosisResult;
import com.wenxing.runyitong.model.Department;
import com.wenxing.runyitong.model.Appointment;
import com.wenxing.runyitong.model.Prescription;
import com.wenxing.runyitong.model.PrescriptionItem;
import com.wenxing.runyitong.model.PaymentOrderRequest;
import com.wenxing.runyitong.model.PaymentOrderResponse;
import com.wenxing.runyitong.model.Address;
import com.wenxing.runyitong.model.Order;
import com.wenxing.runyitong.api.OrderListResponse;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import retrofit2.http.Streaming;
import okhttp3.ResponseBody;
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
    @GET("api/v1/products/{id}")
    Call<ApiResponse<Product>> getProductDetail(@Path("id") int productId);
    
    /**
     * 根据药店名称获取商品列表
     * @param pharmacyName 药店名称
     * @return 商品列表响应
     */
    @GET("api/v1/products/pharmacy")
    Call<ApiResponse<ProductListResponse>> getProductsByPharmacy(@Query("pharmacy_name") String pharmacyName);
    

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
     * 获取单个书籍详情
     * @param bookId 书籍ID
     * @return 书籍详情响应
     */
    @GET("api/v1/books/{id}")
    Call<ApiResponse<Book>> getBook(@Path("id") int bookId);
    
    /**
     * 获取书籍页面内容（分页）
     * @param bookId 书籍ID
     * @param page 页码
     * @param size 每页数量
     * @return 书籍页面列表响应
     */
    @GET("api/v1/books/{id}/pages")
    Call<ApiResponse<List<BookPage>>> getBookPages(
        @Path("id") int bookId,
        @Query("page") int page,
        @Query("size") int size
    );
    
    /**
     * 下载PDF文件
     * @param bookId 书籍ID
     * @return PDF文件流
     */
    @Streaming
    @GET("api/v1/books/{id}/download")
    Call<ResponseBody> downloadBookPdf(@Path("id") int bookId);
    
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
    Call<ApiResponse<MedicalImageAnalysis>> analyzeXRayImage(@Part MultipartBody.Part image);
    
    /**
     * CT影像智能分析
     * @param image CT影像文件
     * @return CT分析结果
     */
    @Multipart
    @POST("api/v1/prescriptions/analyze-ct")
    Call<ApiResponse<MedicalImageAnalysis>> analyzeCTImage(@Part MultipartBody.Part image);
    
    /**
     * B超影像智能分析
     * @param image B超影像文件
     * @return B超分析结果
     */
    @Multipart
    @POST("api/v1/prescriptions/analyze-ultrasound")
    Call<ApiResponse<MedicalImageAnalysis>> analyzeUltrasoundImage(@Part MultipartBody.Part image);
    
    /**
     * MRI影像智能分析
     * @param image MRI影像文件
     * @return MRI分析结果
     */
    @Multipart
    @POST("api/v1/prescriptions/analyze-mri")
    Call<ApiResponse<MedicalImageAnalysis>> analyzeMRIImage(@Part MultipartBody.Part image);
    
    /**
     * PET-CT影像智能分析
     * @param image PET-CT影像文件
     * @return PET-CT分析结果
     */
    @Multipart
    @POST("api/v1/prescriptions/analyze-petct")
    Call<ApiResponse<MedicalImageAnalysis>> analyzePETCTImage(@Part MultipartBody.Part image);
    
    /**
     * 中医舌诊智能分析
     * @param image 舌诊图片文件
     * @return 舌诊分析结果
     */
    @Multipart
    @POST("api/v1/prescriptions/analyze-tongue")
    Call<ApiResponse<TongueDiagnosisResult>> analyzeTongueImage(@Part MultipartBody.Part image);
    
    /**
     * 中医面诊智能分析
     * @param image 面诊图片文件
     * @return 面诊分析结果
     */
    @Multipart
    @POST("api/v1/prescriptions/analyze-face")
    Call<ApiResponse<FaceDiagnosisResult>> analyzeFaceImage(@Part MultipartBody.Part image);
    
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
     * 获取用户预约列表
     * @param userId 用户ID
     * @param skip 跳过数量
     * @param limit 限制数量
     * @return 预约列表响应
     */
    @GET("api/v1/appointments/user/{user_id}")
    Call<ApiResponse<AppointmentListResponse>> getUserAppointments(
        @Path("user_id") int userId,
        @Query("skip") int skip,
        @Query("limit") int limit
    );
    
    /**
     * 创建新预约
     * @param appointment 预约信息
     * @return 创建结果响应
     */
    @POST("api/v1/appointments/")
    Call<ApiResponse<Appointment>> createAppointment(@Body AppointmentCreate appointment);
    
    /**
     * 更新预约状态
     * @param appointmentId 预约ID
     * @param status 新状态
     * @return 更新结果响应
     */
    @PUT("api/v1/appointments/{appointment_id}/status")
    Call<ApiResponse<Appointment>> updateAppointmentStatus(
        @Path("appointment_id") int appointmentId,
        @Query("status") String status
    );
    
    /**
     * 取消预约
     * @param appointmentId 预约ID
     * @return 取消结果响应
     */
    @DELETE("api/v1/appointments/{appointment_id}")
    Call<ApiResponse<Object>> cancelAppointment(@Path("appointment_id") int appointmentId);
    
    /**
     * 用户密码注册
     * @param request 注册请求对象
     * @return 注册结果响应
     */
    @POST("api/v1/users/register")
    Call<ApiResponse<RegisterResponse>> registerWithPassword(
        @Body RegisterRequest request
    );
    
    /**
     * 用户密码注册（兼容旧版本）
     * @param username 用户名
     * @param email 邮箱
     * @param password 密码
     * @return 注册结果响应
     */
    @FormUrlEncoded
    @POST("api/v1/users/register-form")
    Call<ApiResponse<RegisterResponse>> registerWithPasswordForm(
        @Field("username") String username,
        @Field("email") String email,
        @Field("password") String password
    );
    
    /**
     * 用户短信注册（JSON格式）
     * @param registerRequest 注册请求对象
     * @return 注册结果响应
     */
    @POST("api/v1/users/register-with-sms")
    Call<ApiResponse<RegisterResponse>> registerWithSms(@Body RegisterRequest registerRequest);

    /**
     * 用户短信注册（表单格式，兼容旧版本）
     * @param username 用户名
     * @param phone 手机号
     * @param verificationCode 验证码
     * @param password 密码
     * @return 注册结果响应
     */
    @FormUrlEncoded
    @POST("api/v1/users/register-with-sms")
    Call<ApiResponse<RegisterResponse>> registerWithSmsForm(
        @Field("username") String username,
        @Field("phone") String phone,
        @Field("verification_code") String verificationCode,
        @Field("password") String password
    );
    
    /**
     * 发送短信验证码
     * @param phone 手机号码
     * @return 短信验证码响应
     */
    @FormUrlEncoded
    @POST("api/v1/users/send-sms-code")
    Call<ApiResponse<SmsCodeResponse>> sendSmsCode(@Field("phone") String phone);
    
    /**
     * 用户登录
     * @param loginRequest 登录请求对象
     * @return 登录结果响应
     */
    @POST("api/v1/users/login")
    Call<ApiResponse<LoginResponse>> loginUser(
        @Body LoginRequest loginRequest
    );

    /**
     * 用户登录（兼容旧版本表单格式）
     * @param username 用户名
     * @param password 密码
     * @return 登录结果响应
     */
    @FormUrlEncoded
    @POST("api/v1/users/login-form")
    Call<ApiResponse<LoginResponse>> loginUserForm(
        @Field("username") String username,
        @Field("password") String password
    );
    
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

    // ==================== 收货地址相关接口 ====================
    
    /**
     * 获取用户的收货地址列表
     * @param userId 用户ID
     * @return 地址列表响应
     */
    @GET("api/v1/addresses/user/{user_id}")
    Call<AddressListResponse> getUserAddresses(@Path("user_id") int userId);
    
    /**
     * 添加新的收货地址
     * @param address 地址信息
     * @return 地址创建响应
     */
    @POST("api/v1/addresses/")
    Call<ApiResponse<Address>> addAddress(@Body Address address);
    
    /**
     * 更新收货地址
     * @param addressId 地址ID
     * @param address 更新后的地址信息
     * @return 地址更新响应
     */
    @PUT("api/v1/addresses/{address_id}")
    Call<ApiResponse<Address>> updateAddress(
        @Path("address_id") int addressId,
        @Body Address address
    );
    
    /**
     * 删除收货地址
     * @param addressId 地址ID
     * @return 删除结果响应
     */
    @DELETE("api/v1/addresses/{address_id}")
    Call<ApiResponse<Object>> deleteAddress(@Path("address_id") int addressId);
    
    /**
     * 设置默认收货地址
     * @param addressId 地址ID
     * @return 设置结果响应
     */
    @PUT("api/v1/addresses/{address_id}/default")
    Call<ApiResponse<Address>> setDefaultAddress(@Path("address_id") int addressId);
    
    /**
     * 健康检查端点
     * @return 健康检查响应
     */
    @GET("api/v1/prescriptions/health")
    Call<Object> healthCheck();
    
    // ==================== 订单相关接口 ====================
    
    /**
     * 获取用户订单列表
     * @param userId 用户ID
     * @param skip 跳过数量
     * @param limit 限制数量
     * @param status 订单状态（可选，全部/待发货/待收货/已完成）
     * @return 订单列表响应（直接返回，不经过ApiResponse包装）
     */
    @GET("api/v1/orders/user/{user_id}")
    Call<OrderListResponse> getUserOrders(
        @Path("user_id") int userId,
        @Query("skip") int skip,
        @Query("limit") int limit,
        @Query("status") String status
    );
    
    /**
     * 根据订单ID获取订单详情
     * @param orderId 订单ID
     * @return 订单详情响应
     */
    @GET("api/v1/orders/{order_id}")
    Call<ApiResponse<Order>> getOrderDetail(
        @Path("order_id") String orderId
    );
    
    // ==================== 处方相关接口 ====================
    
    /**
     * 获取用户处方列表
     * @param userId 用户ID
     * @return 处方列表响应
     */
    @GET("api/v1/prescriptions/user/{user_id}")
    Call<List<Prescription>> getUserPrescriptions(
        @Path("user_id") int userId
    );
    
    /**
     * 根据处方ID获取处方详情
     * @param prescriptionId 处方ID
     * @return 处方详情响应
     */
    @GET("api/v1/prescriptions/{prescriptionId}")
    Call<ApiResponse<Prescription>> getPrescriptionById(
        @Path("prescriptionId") int prescriptionId
    );
    
    /**
     * 获取处方项目列表
     * @param prescriptionId 处方ID
     * @return 处方项目列表响应
     */
    @GET("api/v1/prescriptions/{prescriptionId}/items")
    Call<ApiResponse<List<PrescriptionItem>>> getPrescriptionItems(
        @Path("prescriptionId") int prescriptionId
    );
    
    /**
     * 创建新处方
     * @param prescriptionCreate 处方创建请求
     * @return 处方创建响应
     */
    @POST("api/v1/prescriptions/create")
    Call<ApiResponse<Prescription>> createPrescription(
        @Body PrescriptionCreate prescriptionCreate
    );
    
    /**
     * 更新处方状态
     * @param prescriptionId 处方ID
     * @param status 新状态
     * @return 更新结果响应
     */
    @PUT("api/v1/prescriptions/{prescriptionId}/status")
    Call<ApiResponse<String>> updatePrescriptionStatus(
        @Path("prescriptionId") int prescriptionId,
        @Field("status") String status
    );
    
    // ==================== 健康档案相关接口 ====================
    
    /**
     * 获取用户健康档案
     * @param userId 用户ID
     * @return 健康档案响应
     */
    @GET("api/v1/health-records/{user_id}")
    Call<ApiResponse<HealthRecord>> getHealthRecord(@Path("user_id") int userId);
    
    /**
     * 更新用户健康档案
     * @param userId 用户ID
     * @param healthRecord 健康档案数据
     * @return 更新结果响应
     */
    @PUT("api/v1/health-records/{user_id}")
    Call<ApiResponse<HealthRecord>> updateHealthRecord(
            @Path("user_id") int userId,
            @Body HealthRecord healthRecord
    );
    
    /**
     * 获取用户体检报告列表
     * @param userId 用户ID
     * @param skip 跳过数量
     * @param limit 限制数量
     * @return 体检报告列表响应
     */
    @GET("api/v1/health-records/{user_id}/physical-exams")
    Call<ApiResponse<List<PhysicalExamReport>>> getPhysicalExamReports(
            @Path("user_id") int userId,
            @Query("skip") int skip,
            @Query("limit") int limit
    );
    
    /**
     * 添加新的体检报告
     * @param userId 用户ID
     * @param report 体检报告数据
     * @return 添加结果响应
     */
    @PUT("api/v1/health-records/{user_id}/physical-exams-add")
    Call<ApiResponse<PhysicalExamReport>> addPhysicalExamReport(
            @Path("user_id") int userId,
            @Body PhysicalExamReport report
    );
    
    /**
     * 删除体检报告
     * @param reportId 体检报告ID
     * @return 删除结果响应
     */
    @DELETE("api/v1/health-records/physical-exams/{report_id}")
    Call<ApiResponse<Object>> deletePhysicalExamReport(@Path("report_id") int reportId);
}