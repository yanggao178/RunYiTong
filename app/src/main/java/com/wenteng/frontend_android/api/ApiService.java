package com.wenteng.frontend_android.api;

import com.wenteng.frontend_android.model.Product;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    
    /**
     * 获取商品列表
     * @param skip 跳过的数量
     * @param limit 限制数量
     * @param search 搜索关键词
     * @param category 商品分类
     * @return 商品列表响应
     */
    @GET("api/products/")
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
    @GET("api/products/{id}")
    Call<ApiResponse<Product>> getProduct(@Path("id") int productId);
}