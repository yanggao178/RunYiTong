package com.wenxing.runyitong.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.wenxing.runyitong.R;
import com.wenxing.runyitong.ProductDetailActivity;
import com.wenxing.runyitong.adapter.ProductAdapter;
import com.wenxing.runyitong.api.ApiClient;
import com.wenxing.runyitong.api.ApiResponse;
import com.wenxing.runyitong.api.ProductListResponse;
import com.wenxing.runyitong.model.Product;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 显示特定药店所有商品的Activity
 */
public class PharmacyProductsActivity extends AppCompatActivity {
    private static final String TAG = "PharmacyProductsActivity";
    public static final String EXTRA_PHARMACY_NAME = "pharmacy_name";
    
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvPharmacyName;
    private ImageView btnBack;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private String pharmacyName;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharmacy_products);
        
        // 获取传入的药店名称
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_PHARMACY_NAME)) {
            pharmacyName = intent.getStringExtra(EXTRA_PHARMACY_NAME);
        }
        
        if (pharmacyName == null || pharmacyName.isEmpty()) {
            Toast.makeText(this, "药店名称不能为空", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        initData();
        setupRecyclerView();
        loadPharmacyProducts();
        setupEvents();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.pharmacy_products_recycler_view);
        swipeRefreshLayout = findViewById(R.id.pharmacy_products_refresh_layout);
        tvPharmacyName = findViewById(R.id.pharmacy_name_title);
        btnBack = findViewById(R.id.btn_back);
        
        // 设置页面标题为药店名称
        if (tvPharmacyName != null) {
            tvPharmacyName.setText(pharmacyName);
        }
    }
    
    private void initData() {
        productList = new ArrayList<>();
    }
    
    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(this, productList);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(productAdapter);
        
        // 设置商品点击事件
        productAdapter.setOnItemClickListener(product -> {
            // 跳转到商品详情页
            Intent intent = new Intent(PharmacyProductsActivity.this, ProductDetailActivity.class);
            intent.putExtra("product", product);
            startActivity(intent);
        });
    }
    
    private void setupEvents() {
        // 返回按钮点击事件
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        
        // 下拉刷新事件
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadPharmacyProducts);
        }
    }
    
    private void loadPharmacyProducts() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }
        
        // 调用API获取指定药店的商品列表，参数名与ApiService接口保持一致
        Call<ApiResponse<ProductListResponse>> call = ApiClient.getApiService()
                .getProductsByPharmacy(pharmacyName);
        
        call.enqueue(new Callback<ApiResponse<ProductListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProductListResponse>> call, 
                                   Response<ApiResponse<ProductListResponse>> response) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ProductListResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Product> products = apiResponse.getData().getItems();
                        productList.clear();
                        if (products != null) {
                            productList.addAll(products);
                        }
                        productAdapter.notifyDataSetChanged();
                        
                        if (productList.isEmpty()) {
                            Toast.makeText(PharmacyProductsActivity.this, 
                                    "该药店暂无商品", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(PharmacyProductsActivity.this, 
                                "获取商品失败: " + apiResponse.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PharmacyProductsActivity.this, 
                            "网络请求失败", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<ProductListResponse>> call, Throwable t) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                Log.e(TAG, "加载药店商品失败", t);
                Toast.makeText(PharmacyProductsActivity.this, 
                        "网络连接失败: " + t.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}