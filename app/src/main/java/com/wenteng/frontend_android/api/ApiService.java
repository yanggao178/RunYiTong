package com.wenteng.frontend_android.api;

import com.wenteng.frontend_android.model.Product;
import com.wenteng.frontend_android.model.Book;
import com.wenteng.frontend_android.model.SymptomAnalysis;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
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
     * 症状分析接口
     * @param symptoms 症状描述
     * @return 症状分析响应
     */
    @FormUrlEncoded
    @POST("api/v1/prescriptions/analyze-symptoms")
    Call<ApiResponse<SymptomAnalysis>> analyzeSymptoms(@Field("symptoms") String symptoms);
}