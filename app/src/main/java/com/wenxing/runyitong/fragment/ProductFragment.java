package com.wenxing.runyitong.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
// 需要添加的导入语句
import android.content.Intent;
import com.wenxing.runyitong.ProductDetailActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wenxing.runyitong.R;
import com.wenxing.runyitong.adapter.ProductAdapter;
import com.wenxing.runyitong.model.Product;
import com.wenxing.runyitong.api.ApiClient;
import com.wenxing.runyitong.api.ApiResponse;
import com.wenxing.runyitong.api.ProductListResponse;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.net.SocketTimeoutException;
import java.net.ConnectException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;

public class ProductFragment extends Fragment {

    private RecyclerView productRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private List<Product> filteredProductList;
    private EditText searchEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        android.util.Log.d("ProductFragment", "onCreateView called");
        
        try {
            View view = inflater.inflate(R.layout.fragment_product, container, false);
            if (view == null) {
                android.util.Log.e("ProductFragment", "Failed to inflate layout");
                return null;
            }
            
            android.util.Log.d("ProductFragment", "Layout inflated successfully");
            initViews(view);
            initData();
            setupRecyclerView();
            setupSearchFunction();
            android.util.Log.d("ProductFragment", "Fragment initialization completed");
            return view;
        } catch (Exception e) {
            android.util.Log.e("ProductFragment", "Error in onCreateView", e);
            return null;
        }
    }

    private void initViews(View view) {
        try {
            productRecyclerView = view.findViewById(R.id.product_recycler_view);
            searchEditText = view.findViewById(R.id.search_edit_text);
            
            if (productRecyclerView == null) {
                showError("无法找到商品列表视图");
            }
            if (searchEditText == null) {
                showError("无法找到搜索框");
            }
        } catch (Exception e) {
            showError("初始化视图失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initData() {
        productList = new ArrayList<>();
        filteredProductList = new ArrayList<>();
        
        // 从后端API获取商品数据
        loadProductsFromApi();
    }
    private void loadProductsFromApi() {
        loadProductsFromApiWithRetry(0);
    }
    
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    
    private void loadProductsFromApiWithRetry(int retryCount) {
        final int MAX_RETRIES = 3;
        
        // 检查网络连接
        if (!isNetworkAvailable()) {
            showError("网络连接不可用，请检查网络设置");
            return;
        }
        
        if (retryCount == 0) {
            showError("正在加载商品数据...");
        } else {
            showError("重试加载商品数据... (" + (retryCount + 1) + "/" + (MAX_RETRIES + 1) + ")");
        }
        
        Call<ApiResponse<ProductListResponse>> call = ApiClient.getApiService().getProducts(0, 50, null, null);

        call.enqueue(new Callback<ApiResponse<ProductListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProductListResponse>> call,
                                   Response<ApiResponse<ProductListResponse>> response) {

                // 1. 先检查HTTP状态码
                showError("HTTP状态码: " + response.code());

                // 2. 检查响应体是否为空
                if (response.body() == null) {
                    showError("响应体为null");
                    try {
                        showError("错误响应: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                // 3. 解析业务响应
                ApiResponse<ProductListResponse> apiResponse = response.body();
                if (!apiResponse.isSuccess()) {
                    showError("业务逻辑失败: " + apiResponse.getMessage());
                    return;
                }

                // 4. 检查数据是否有效
                if (apiResponse.getData() == null || apiResponse.getData().getItems() == null) {
                    showError("商品数据为null");
                    return;
                }

                // 5. 处理成功数据
                List<Product> products = apiResponse.getData().getItems();
                showError("成功获取 " + products.size() + " 个商品");

                // 更新UI代码
                if (products != null && !products.isEmpty()) {
                    // 清空并更新商品列表
                    productList.clear();
                    productList.addAll(products);
                    
                    // 更新过滤列表
                    filteredProductList.clear();
                    filteredProductList.addAll(products);
                    
                    // 通知适配器数据已更改
                    if (productAdapter != null) {
                        productAdapter.notifyDataSetChanged();
                    }
                    
                    showError("商品列表已更新，共 " + products.size() + " 个商品");
                } else {
                    showError("获取到的商品列表为空");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ProductListResponse>> call, Throwable t) {
                final int MAX_RETRIES = 3;
                
                // 打印详细错误信息
                showError("网络请求失败: " + t.getClass().getSimpleName() + ": " + t.getMessage());
                t.printStackTrace();

                // 特殊处理常见异常
                String errorMessage = "";
                if (t instanceof SocketTimeoutException) {
                    errorMessage = "连接超时";
                } else if (t instanceof ConnectException) {
                    errorMessage = "无法连接服务器";
                } else if (t instanceof SSLHandshakeException) {
                    errorMessage = "证书验证失败";
                } else {
                    errorMessage = "网络连接异常";
                }
                
                // 实现重试逻辑
                if (retryCount < MAX_RETRIES) {
                    showError(errorMessage + "，正在重试...");
                    // 延迟重试，避免频繁请求
                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadProductsFromApiWithRetry(retryCount + 1);
                        }
                    }, 2000); // 延迟2秒重试
                } else {
                    showError(errorMessage + "，请检查网络连接或稍后重试");
                    // 可以在这里添加手动重试按钮或其他用户操作
                }
            }
        });
    }
//    private void loadProductsFromApi() {
//        showError("开始加载商品数据..."); // 调试信息
//        Call<ApiResponse<ProductListResponse>> call = ApiClient.getApiService().getProducts(0, 50, null, null);
//
//        call.enqueue(new Callback<ApiResponse<ProductListResponse>>() {
//            @Override
//            public void onResponse(Call<ApiResponse<ProductListResponse>> call, Response<ApiResponse<ProductListResponse>> response) {
//                showError("收到响应，状态码: " + response.code()); // 调试信息
//                if (response.isSuccessful() && response.body() != null) {
//                    ApiResponse<ProductListResponse> apiResponse = response.body();
//                    showError("API响应成功: " + apiResponse.isSuccess()); // 调试信息
//                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
//                        List<Product> products = apiResponse.getData().getItems();
//                        if (products != null) {
//                            showError("获取到 " + products.size() + " 个商品"); // 调试信息
//                            productList.clear();
//                            productList.addAll(products);
//                            filteredProductList.clear();
//                            filteredProductList.addAll(products);
//
//                            // 更新UI
//                            if (productAdapter != null) {
//                                productAdapter.notifyDataSetChanged();
//                            }
//                        }
//                    } else {
//                        showError("获取商品数据失败: " + apiResponse.getMessage());
//                    }
//                } else {
//                    showError("网络请求失败，状态码: " + response.code() + ", 错误: " + response.message());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ApiResponse<ProductListResponse>> call, Throwable t) {
//                showError("网络连接失败: " + t.getMessage());
//                t.printStackTrace(); // 打印完整错误堆栈
//            }
//        });
//    }
    
    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        try {
            if (productRecyclerView == null || getContext() == null) {
                showError("无法设置商品列表，视图或上下文为空");
                return;
            }
            
            productAdapter = new ProductAdapter(getContext(), filteredProductList);
            productRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            productRecyclerView.setAdapter(productAdapter);

            // 设置商品点击事件
            productAdapter.setOnItemClickListener(product -> {
                try {
                    if (getActivity() != null && product != null) {
                        // 启动商品详情页
                        Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
                        intent.putExtra("product", product);
                        startActivity(intent);
                    } else {
                        showError("无法打开商品详情");
                    }
                } catch (Exception e) {
                    showError("打开商品详情失败: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            showError("设置商品列表失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupSearchFunction() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 文本变化前的操作
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 文本变化时的操作
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 文本变化后的操作
            }
        });
    }

    private void filterProducts(String query) {
        try {
            if (filteredProductList == null || productList == null) {
                showError("商品列表未初始化");
                return;
            }
            
            filteredProductList.clear();
            if (query == null || query.isEmpty()) {
                filteredProductList.addAll(productList);
            } else {
                String lowerCaseQuery = query.toLowerCase();
                for (Product product : productList) {
                    if (product != null && product.getName() != null && 
                        product.getName().toLowerCase().contains(lowerCaseQuery)) {
                        filteredProductList.add(product);
                    }
                }
            }
            
            if (productAdapter != null) {
                productAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            showError("搜索商品失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}