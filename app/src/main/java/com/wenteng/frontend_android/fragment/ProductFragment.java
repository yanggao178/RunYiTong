package com.wenteng.frontend_android.fragment;

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
import com.wenteng.frontend_android.ProductDetailActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wenteng.frontend_android.R;
import com.wenteng.frontend_android.adapter.ProductAdapter;
import com.wenteng.frontend_android.model.Product;
import com.wenteng.frontend_android.api.ApiClient;
import com.wenteng.frontend_android.api.ApiResponse;
import com.wenteng.frontend_android.api.ProductListResponse;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.net.SocketTimeoutException;
import java.net.ConnectException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;

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
        View view = inflater.inflate(R.layout.fragment_product, container, false);
        initViews(view);
        initData();
        setupRecyclerView();
        setupSearchFunction();
        return view;
    }

    private void initViews(View view) {
        productRecyclerView = view.findViewById(R.id.product_recycler_view);
        searchEditText = view.findViewById(R.id.search_edit_text);
    }

    private void initData() {
        productList = new ArrayList<>();
        filteredProductList = new ArrayList<>();
        
        // 从后端API获取商品数据
        loadProductsFromApi();
    }
    private void loadProductsFromApi() {
        showError("开始加载商品数据...");
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
                // 6. 重点检查这里打印的异常
                showError("网络请求失败: " + t.getClass().getSimpleName() + ": " + t.getMessage());
                t.printStackTrace();

                // 特殊处理常见异常
                if (t instanceof SocketTimeoutException) {
                    showError("连接超时，请检查网络");
                } else if (t instanceof ConnectException) {
                    showError("无法连接服务器，请检查API地址");
                } else if (t instanceof SSLHandshakeException) {
                    showError("证书验证失败");
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
        productAdapter = new ProductAdapter(getContext(), filteredProductList);
        productRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        productRecyclerView.setAdapter(productAdapter);

        // 设置商品点击事件
        productAdapter.setOnItemClickListener(product -> {
            // 启动商品详情页
            Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
            intent.putExtra("product", product);
            startActivity(intent);
        });
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
        filteredProductList.clear();
        if (query.isEmpty()) {
            filteredProductList.addAll(productList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredProductList.add(product);
                }
            }
        }
        productAdapter.notifyDataSetChanged();
    }
}