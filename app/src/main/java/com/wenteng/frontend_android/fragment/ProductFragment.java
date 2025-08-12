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
        Call<ApiResponse<ProductListResponse>> call = ApiClient.getApiService().getProducts(0, 50, null, null);
        
        call.enqueue(new Callback<ApiResponse<ProductListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProductListResponse>> call, Response<ApiResponse<ProductListResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ProductListResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Product> products = apiResponse.getData().getItems();
                        if (products != null) {
                            productList.clear();
                            productList.addAll(products);
                            filteredProductList.clear();
                            filteredProductList.addAll(products);
                            
                            // 更新UI
                            if (productAdapter != null) {
                                productAdapter.notifyDataSetChanged();
                            }
                        }
                    } else {
                        showError("获取商品数据失败: " + apiResponse.getMessage());
                    }
                } else {
                    showError("网络请求失败，请检查网络连接");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<ProductListResponse>> call, Throwable t) {
                showError("网络连接失败: " + t.getMessage());
            }
        });
    }
    
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