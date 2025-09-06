package com.wenxing.runyitong.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.wenxing.runyitong.R;
import com.wenxing.runyitong.adapter.OrderAdapter;
import com.wenxing.runyitong.api.ApiClient;
import com.wenxing.runyitong.api.ApiService;
import com.wenxing.runyitong.api.ApiResponse;
import com.wenxing.runyitong.model.Order;
import com.wenxing.runyitong.api.OrderListResponse;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderActivity extends AppCompatActivity {

    private RecyclerView ordersRecyclerView;
    private List<Order> orderList;
    private List<Order> allOrderList;
    private OrderAdapter orderAdapter;
    
    // 状态筛选按钮
    private Button btnAll, btnPendingShipment, btnPendingReceipt, btnCompleted;
    private String currentFilter = "";
    
    // 加载状态和空状态
    private ProgressBar progressBar;
    private TextView emptyStateTextView;
    
    private ApiService apiService;
    
    // 当前用户ID，实际应用中应从登录状态获取
    private int currentUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_order);
            
            initViews();
            setupToolbar();
            setupRecyclerView();
            setupFilterButtons();
            loadOrderData();
        } catch (Exception e) {
            e.printStackTrace();
            // 如果出现异常，显示错误信息并关闭Activity
            Toast.makeText(this, "订单页面加载失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * 从SharedPreferences获取用户信息
     */
    private void getUserInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_login_state", MODE_PRIVATE);
        currentUserId = sharedPreferences.getInt("user_id", -1);
    }

    private void initViews() {
        ordersRecyclerView = findViewById(R.id.orders_recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        emptyStateTextView = findViewById(R.id.empty_state_text);
        
        // 初始化状态筛选按钮
        btnAll = findViewById(R.id.btn_all);
        btnPendingShipment = findViewById(R.id.btn_pending_shipment);
        btnPendingReceipt = findViewById(R.id.btn_pending_receipt);
        btnCompleted = findViewById(R.id.btn_completed);
        
        // 初始化API服务
        apiService = ApiClient.getApiService();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("我的订单");
        }
    }

    private void setupRecyclerView() {
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        allOrderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(this, orderList);
        ordersRecyclerView.setAdapter(orderAdapter);
    }
    
    private void setupFilterButtons() {
        btnAll.setOnClickListener(v -> filterOrders(""));
        btnPendingShipment.setOnClickListener(v -> filterOrders("待发货"));
        btnPendingReceipt.setOnClickListener(v -> filterOrders("待收货"));
        btnCompleted.setOnClickListener(v -> filterOrders("已完成"));
    }
    
    private void filterOrders(String status) {
        currentFilter = status;
        updateButtonStyles();
        
        orderList.clear();
        if ("".equals(status)) {
            orderList.addAll(allOrderList);
        } else {
            for (Order order : allOrderList) {
                if (order.getStatus().equals(status)) {
                    orderList.add(order);
                }
            }
        }
        // 通知适配器数据已更改
        if (orderAdapter != null) orderAdapter.notifyDataSetChanged();
    }
    
    private void updateButtonStyles() {
        // 重置所有按钮样式
        btnAll.setTextColor(getResources().getColor(android.R.color.darker_gray));
        btnPendingShipment.setTextColor(getResources().getColor(android.R.color.darker_gray));
        btnPendingReceipt.setTextColor(getResources().getColor(android.R.color.darker_gray));
        btnCompleted.setTextColor(getResources().getColor(android.R.color.darker_gray));
        
        // 设置当前选中按钮样式
        switch (currentFilter) {
            case "全部":
                btnAll.setTextColor(getResources().getColor(R.color.colorPrimary));
                break;
            case "待发货":
                btnPendingShipment.setTextColor(getResources().getColor(R.color.colorPrimary));
                break;
            case "待收货":
                btnPendingReceipt.setTextColor(getResources().getColor(R.color.colorPrimary));
                break;
            case "已完成":
                btnCompleted.setTextColor(getResources().getColor(R.color.colorPrimary));
                break;
        }
    }

    private void loadOrderData() {
        // 显示加载进度
        showLoading(true);

        getUserInfo();
        if(currentUserId == -1)
            return;
        // 调用API获取订单列表 - 注意：后端直接返回OrderListResponse对象，不是包装在ApiResponse中
        Call<OrderListResponse> call = apiService.getUserOrders(
            currentUserId, 
            0, 
            100, 
            ""
        );
        
        call.enqueue(new Callback<OrderListResponse>() {
            @Override
            public void onResponse(Call<OrderListResponse> call, Response<OrderListResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    OrderListResponse orderListResponse = response.body();
                    
                    // 检查后端返回的success状态
                    if (orderListResponse.isSuccess()) {
                        allOrderList.clear();
                        if(orderListResponse.getOrders() != null && !orderListResponse.getOrders().isEmpty()){
                            allOrderList.addAll(orderListResponse.getOrders());
                        }
                        // 应用当前筛选条件
                        filterOrders(currentFilter);
                        updateEmptyState();
                    } else {
                        // 后端返回失败状态
                        String errorMsg = orderListResponse.getMessage();
                        Toast.makeText(OrderActivity.this, "获取订单失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                        loadMockOrderData();
                        filterOrders(currentFilter);
                    }
                } else {
                    // 请求失败，使用模拟数据
                    loadMockOrderData();
                    filterOrders(currentFilter);
                    Toast.makeText(OrderActivity.this, "网络请求失败，显示模拟数据", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<OrderListResponse> call, Throwable t) {
                showLoading(false);
                // 网络请求失败，使用模拟数据
                loadMockOrderData();
                filterOrders(currentFilter);
                Toast.makeText(OrderActivity.this, "网络连接失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadMockOrderData() {
        // 模拟订单数据，当API调用失败时使用
        allOrderList.clear();
        
        Order order1 = new Order();
        order1.setOrderId("ORD001");
        order1.setProductName("中药材套装");
        order1.setStatus("待发货");
        order1.setPrice("¥299.00");
        order1.setCreateTime("2023-12-10 14:30:00");
        allOrderList.add(order1);
        
        Order order2 = new Order();
        order2.setOrderId("ORD002");
        order2.setProductName("养生茶叶");
        order2.setStatus("待收货");
        order2.setPrice("¥158.00");
        order2.setCreateTime("2023-12-08 09:15:00");
        allOrderList.add(order2);
        
        Order order3 = new Order();
        order3.setOrderId("ORD003");
        order3.setProductName("保健品");
        order3.setStatus("已完成");
        order3.setPrice("¥89.00");
        order3.setCreateTime("2023-12-05 16:45:00");
        allOrderList.add(order3);
        
        Order order4 = new Order();
        order4.setOrderId("ORD004");
        order4.setProductName("滋补汤料");
        order4.setStatus("待发货");
        order4.setPrice("¥128.00");
        order4.setCreateTime("2023-12-03 11:20:00");
        allOrderList.add(order4);
        
        Order order5 = new Order();
        order5.setOrderId("ORD005");
        order5.setProductName("养生枸杞");
        order5.setStatus("待收货");
        order5.setPrice("¥68.00");
        order5.setCreateTime("2023-12-01 10:05:00");
        allOrderList.add(order5);
    }
    
    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        ordersRecyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        emptyStateTextView.setVisibility(View.GONE);
    }
    
    private void updateEmptyState() {
        if (orderList.isEmpty()) {
            emptyStateTextView.setVisibility(View.VISIBLE);
            ordersRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateTextView.setVisibility(View.GONE);
            ordersRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}