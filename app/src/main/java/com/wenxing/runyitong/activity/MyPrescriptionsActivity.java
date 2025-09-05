package com.wenxing.runyitong.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.wenxing.runyitong.R;
import com.wenxing.runyitong.adapter.PrescriptionAdapter;
import com.wenxing.runyitong.api.ApiClient;
import com.wenxing.runyitong.api.ApiService;
import com.wenxing.runyitong.model.Prescription;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 我的处方Activity
 * 显示用户的处方信息列表
 */
public class MyPrescriptionsActivity extends AppCompatActivity {
    private static final String TAG = "MyPrescriptionsActivity";
    
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView btnBack;
    private PrescriptionAdapter prescriptionAdapter;
    private List<Prescription> prescriptionList;
    private ApiService apiService;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_prescriptions);
        
        initViews();
        initData();
        setupRecyclerView();
        loadPrescriptions();
    }

    /**
     * 初始化视图组件
     */
    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_prescriptions);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_prescriptions);
        btnBack = findViewById(R.id.btn_back);
        
        // 设置返回按钮点击事件
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        
        // 设置下拉刷新监听器
        swipeRefreshLayout.setOnRefreshListener(this::loadPrescriptions);
        swipeRefreshLayout.setColorSchemeResources(
            R.color.primary_color,
            R.color.accent_color
        );
    }

    /**
     * 初始化数据
     */
    private void initData() {
        prescriptionList = new ArrayList<>();
        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        sharedPreferences = getSharedPreferences("user_login_state", MODE_PRIVATE);
    }

    /**
     * 设置RecyclerView
     */
    private void setupRecyclerView() {
        prescriptionAdapter = new PrescriptionAdapter(this);
        prescriptionAdapter.updatePrescriptions(prescriptionList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(prescriptionAdapter);
        
        // 设置点击事件
        prescriptionAdapter.setOnPrescriptionClickListener(new PrescriptionAdapter.OnPrescriptionClickListener() {
            @Override
            public void onViewDetails(Prescription prescription) {
                // 处理处方项点击事件
                showPrescriptionDetails(prescription);
            }
            
            @Override
            public void onUpdateStatus(Prescription prescription, int position) {
                // 处理更新处方状态
                updatePrescriptionStatus(prescription);
            }
        });
    }

    /**
     * 加载处方数据
     */
    private void loadPrescriptions() {
        swipeRefreshLayout.setRefreshing(true);
        
        // 获取用户ID
        int userId = sharedPreferences.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            finish();
            return;
        }
        
        Call<List<Prescription>> call = apiService.getUserPrescriptions(userId);
        call.enqueue(new Callback<List<Prescription>>() {
            @Override
            public void onResponse(Call<List<Prescription>> call, 
                                 Response<List<Prescription>> response) {
                swipeRefreshLayout.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Prescription> prescriptions = response.body();
                    prescriptionList.clear();
                    prescriptionList.addAll(prescriptions);
                    prescriptionAdapter.updatePrescriptions(prescriptionList);
                    
                    if (prescriptionList.isEmpty()) {
                        Toast.makeText(MyPrescriptionsActivity.this, 
                            "暂无处方信息", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MyPrescriptionsActivity.this, 
                        "获取处方信息失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Prescription>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Log.e(TAG, "加载处方列表失败", t);
                Toast.makeText(MyPrescriptionsActivity.this, 
                    "网络连接失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 显示处方详情
     * @param prescription 处方对象
     */
    private void showPrescriptionDetails(Prescription prescription) {
        // TODO: 实现处方详情显示逻辑
        Toast.makeText(this, "查看处方详情: " + prescription.getId(), Toast.LENGTH_SHORT).show();
    }

    /**
     * 更新处方状态
     * @param prescription 处方对象
     */
    private void updatePrescriptionStatus(Prescription prescription) {
        // TODO: 实现处方状态更新逻辑
        Toast.makeText(this, "更新处方状态: " + prescription.getId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 页面恢复时刷新数据
        loadPrescriptions();
    }
}