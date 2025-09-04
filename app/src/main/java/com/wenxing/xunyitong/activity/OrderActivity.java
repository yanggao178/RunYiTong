package com.wenxing.xunyitong.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.wenxing.xunyitong.R;
import com.wenxing.xunyitong.adapter.OrderAdapter;
import java.util.ArrayList;
import java.util.List;

public class OrderActivity extends AppCompatActivity {

    private RecyclerView ordersRecyclerView;
    private List<Order> orderList;
    private List<Order> allOrderList;
    private OrderAdapter orderAdapter;
    
    // 状态筛选按钮
    private Button btnAll, btnPendingShipment, btnPendingReceipt, btnCompleted;
    private String currentFilter = "全部";

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

    private void initViews() {
        ordersRecyclerView = findViewById(R.id.orders_recycler_view);
        
        // 初始化状态筛选按钮
        btnAll = findViewById(R.id.btn_all);
        btnPendingShipment = findViewById(R.id.btn_pending_shipment);
        btnPendingReceipt = findViewById(R.id.btn_pending_receipt);
        btnCompleted = findViewById(R.id.btn_completed);
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
        btnAll.setOnClickListener(v -> filterOrders("全部"));
        btnPendingShipment.setOnClickListener(v -> filterOrders("待发货"));
        btnPendingReceipt.setOnClickListener(v -> filterOrders("待收货"));
        btnCompleted.setOnClickListener(v -> filterOrders("已完成"));
    }
    
    private void filterOrders(String status) {
        currentFilter = status;
        updateButtonStyles();
        
        orderList.clear();
        if ("全部".equals(status)) {
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
        // 示例订单数据
        allOrderList.add(new Order("ORD001", "中药材套装", "已发货", "¥299.00"));
        allOrderList.add(new Order("ORD002", "养生茶叶", "待收货", "¥158.00"));
        allOrderList.add(new Order("ORD003", "保健品", "已完成", "¥89.00"));
        allOrderList.add(new Order("ORD004", "滋补汤料", "待发货", "¥128.00"));
        allOrderList.add(new Order("ORD005", "养生枸杞", "待收货", "¥68.00"));
        
        // 初始显示全部订单
        filterOrders("全部");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 订单数据模型
    public static class Order {
        private String orderId;
        private String productName;
        private String status;
        private String price;

        public Order(String orderId, String productName, String status, String price) {
            this.orderId = orderId;
            this.productName = productName;
            this.status = status;
            this.price = price;
        }

        // Getters
        public String getOrderId() { return orderId; }
        public String getProductName() { return productName; }
        public String getStatus() { return status; }
        public String getPrice() { return price; }
    }
}