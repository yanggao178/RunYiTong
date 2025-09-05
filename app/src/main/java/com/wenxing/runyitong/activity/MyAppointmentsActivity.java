package com.wenxing.runyitong.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.wenxing.runyitong.R;
import com.wenxing.runyitong.adapter.AppointmentAdapter;
import com.wenxing.runyitong.model.Appointment;
import com.wenxing.runyitong.api.ApiClient;
import com.wenxing.runyitong.api.ApiService;
import com.wenxing.runyitong.api.ApiResponse;
import com.wenxing.runyitong.api.AppointmentListResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 我的预约Activity - 显示用户的挂号信息
 */
public class MyAppointmentsActivity extends AppCompatActivity {
    
    private static final String TAG = "MyAppointmentsActivity";
    
    // UI组件
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyView;
    
    // 数据相关
    private AppointmentAdapter appointmentAdapter;
    private List<Appointment> appointmentList;
    private ApiService apiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_appointments);
        
        initViews();
        initData();
        setupRecyclerView();
        setupSwipeRefresh();
        loadAppointments();
    }
    
    /**
     * 初始化视图组件
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        recyclerView = findViewById(R.id.recycler_view_appointments);
        progressBar = findViewById(R.id.progress_bar);
        tvEmptyView = findViewById(R.id.tv_empty_view);
        
        // 设置工具栏
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("我的预约");
        }
    }
    
    /**
     * 初始化数据
     */
    private void initData() {
        appointmentList = new ArrayList<>();
        apiService = ApiClient.getInstance().getApiService();
    }
    
    /**
     * 设置RecyclerView
     */
    private void setupRecyclerView() {
        appointmentAdapter = new AppointmentAdapter(this);
        appointmentAdapter.updateAppointments(appointmentList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(appointmentAdapter);
        
        // 设置点击事件
        appointmentAdapter.setOnAppointmentClickListener(new AppointmentAdapter.OnAppointmentClickListener() {
            @Override
            public void onViewDetails(Appointment appointment) {
                // 处理预约项点击事件
                showAppointmentDetails(appointment);
            }
            
            @Override
            public void onCancelAppointment(Appointment appointment, int position) {
                // 处理取消预约
                cancelAppointment(appointment);
            }
        });
    }
    
    /**
     * 设置下拉刷新
     */
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadAppointments();
            }
        });
        
        // 设置刷新颜色
        swipeRefreshLayout.setColorSchemeResources(
            R.color.primary_color,
            R.color.accent_color
        );
    }
    
    /**
     * 加载预约数据
     */
    private void loadAppointments() {
        // 检查Activity状态
        if (isFinishing() || isDestroyed()) {
            Log.w(TAG, "Activity is finishing or destroyed, skipping API call");
            return;
        }
        
        showLoading(true);
        
        // 获取当前登录用户ID
        int userId = getCurrentUserId();
        if (userId == -1) {
            Log.e(TAG, "User is not logged in");
            showLoading(false);
            swipeRefreshLayout.setRefreshing(false);
            showError("请先登录");
            loadMockData();
            updateEmptyView();
            return;
        }
        
        // 检查access_token是否存在
        String accessToken = ApiClient.getInstance().getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            Log.e(TAG, "Access token is null or empty");
            showLoading(false);
            swipeRefreshLayout.setRefreshing(false);
            showError("认证信息无效，请重新登录");
            // 清除无效的登录状态
            clearLoginState();
            loadMockData();
            updateEmptyView();
            return;
        }
        
        Log.d(TAG, "Access token length: " + accessToken.length());
        
        // 检查ApiService是否初始化成功
        if (apiService == null) {
            Log.e(TAG, "ApiService is not initialized");
            showLoading(false);
            swipeRefreshLayout.setRefreshing(false);
            showError("服务初始化失败，请重试");
            loadMockData();
            updateEmptyView();
            return;
        }
        
        Log.d(TAG, "ApiService is available, making API call for user: " + userId);
        
        // 调用API获取预约数据
        Call<ApiResponse<AppointmentListResponse>> call = apiService.getUserAppointments(userId, 0, 20);
        Log.d(TAG, "API call created: " + call.request().url());
        Log.d(TAG, "API call headers before interceptor: " + call.request().headers());
        
        call.enqueue(new Callback<ApiResponse<AppointmentListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AppointmentListResponse>> call, 
                                 Response<ApiResponse<AppointmentListResponse>> response) {
                try {
                    Log.d(TAG, "API response received with code: " + response.code());
                    Log.d(TAG, "API response headers: " + response.headers());
                    
                    if (response.code() == 401) {
                        // 处理401认证错误
                        Log.e(TAG, "Authentication failed (401) for user: " + userId);
                        showError("认证失败，请重新登录");
                        // 清除无效的登录状态和token
                        clearLoginState();
                        // 加载模拟数据
                        loadMockData();
                        return;
                    }
                    
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<AppointmentListResponse> apiResponse = response.body();
                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            List<Appointment> appointments = apiResponse.getData().getItems();
                            if (appointments != null) {
                                appointmentList.clear();
                                appointmentList.addAll(appointments);
                                appointmentAdapter.notifyDataSetChanged();
                                Log.d(TAG, "Successfully loaded " + appointments.size() + " appointments");
                            } else {
                                Log.w(TAG, "API response data items is null");
                                // 加载模拟数据
                                loadMockData();
                            }
                        } else {
                            Log.w(TAG, "API call succeeded but returned failure: " + apiResponse.getMessage());
                            // API返回失败，使用模拟数据
                            loadMockData();
                        }
                    } else {
                        Log.e(TAG, "API call failed with code: " + response.code());
                        // 尝试读取错误体
                        if (response.errorBody() != null) {
                            try {
                                String errorBody = response.errorBody().string();
                                Log.e(TAG, "Error body: " + errorBody);
                            } catch (IOException e) {
                                Log.e(TAG, "Failed to read error body", e);
                            }
                        }
                        // 网络请求失败，使用模拟数据
                        loadMockData();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing API response", e);
                    showError("处理数据时发生错误: " + e.getMessage());
                    loadMockData();
                } finally {
                    showLoading(false);
                    swipeRefreshLayout.setRefreshing(false);
                    updateEmptyView();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<AppointmentListResponse>> call, Throwable t) {
                try {
                    showLoading(false);
                    swipeRefreshLayout.setRefreshing(false);
                    Log.e(TAG, "API call failed: " + t.getMessage(), t);
                    showError("网络连接失败: " + t.getMessage());
                    
                    // 网络请求失败，使用模拟数据
                    loadMockData();
                } catch (Exception e) {
                    Log.e(TAG, "Error in onFailure callback", e);
                } finally {
                    updateEmptyView();
                }
            }
        });
    }
    
    /**
     * 加载模拟数据
     */
    private void loadMockData() {
        try {
            appointmentList.clear();
            
            // 创建模拟预约数据
            Appointment appointment1 = new Appointment();
            appointment1.setId(1);
            appointment1.setPatientName("张三");
            appointment1.setPatientPhone("138****1234");
            appointment1.setDoctorName("李医生");
            appointment1.setHospitalName("市人民医院");
            appointment1.setDepartmentName("内科");
            appointment1.setAppointmentDate("2024-01-20");
            appointment1.setAppointmentTime("09:00-09:30");
            appointment1.setStatus("已确认");
            appointment1.setSymptoms("头痛、发热");
            appointment1.setCreatedAt("2024-01-15 10:30:00");
            
            Appointment appointment2 = new Appointment();
            appointment2.setId(2);
            appointment2.setPatientName("李四");
            appointment2.setPatientPhone("139****5678");
            appointment2.setDoctorName("王医生");
            appointment2.setHospitalName("中医院");
            appointment2.setDepartmentName("骨科");
            appointment2.setAppointmentDate("2024-01-22");
            appointment2.setAppointmentTime("14:30-15:00");
            appointment2.setStatus("待确认");
            appointment2.setSymptoms("腰痛");
            appointment2.setCreatedAt("2024-01-16 15:20:00");
            
            Appointment appointment3 = new Appointment();
            appointment3.setId(3);
            appointment3.setPatientName("王五");
            appointment3.setPatientPhone("137****9012");
            appointment3.setDoctorName("赵医生");
            appointment3.setHospitalName("儿童医院");
            appointment3.setDepartmentName("儿科");
            appointment3.setAppointmentDate("2024-01-18");
            appointment3.setAppointmentTime("10:00-10:30");
            appointment3.setStatus("已完成");
            appointment3.setSymptoms("咳嗽、流鼻涕");
            appointment3.setCreatedAt("2024-01-12 09:15:00");
            
            appointmentList.add(appointment1);
            appointmentList.add(appointment2);
            appointmentList.add(appointment3);
            
            appointmentAdapter.notifyDataSetChanged();
            updateEmptyView();
            Log.d(TAG, "Loaded mock appointment data");
        } catch (Exception e) {
            Log.e(TAG, "Error loading mock data", e);
        } finally {
            showLoading(false);
            swipeRefreshLayout.setRefreshing(false);
        }
    }
    
    /**
     * 显示预约详情
     */
    private void showAppointmentDetails(Appointment appointment) {
        // TODO: 实现预约详情显示
        String details = "预约详情:\n" +
                "患者：" + appointment.getPatientName() + "\n" +
                "医生：" + appointment.getDoctorName() + "\n" +
                "医院：" + appointment.getHospitalName() + "\n" +
                "科室：" + appointment.getDepartmentName() + "\n" +
                "时间：" + appointment.getAppointmentDate() + " " + appointment.getAppointmentTime() + "\n" +
                "状态：" + appointment.getStatus() + "\n" +
                "症状：" + appointment.getSymptoms();
        
        Toast.makeText(this, details, Toast.LENGTH_LONG).show();
    }
    
    /**
     * 取消预约
     */
    private void cancelAppointment(Appointment appointment) {
        // TODO: 实现取消预约功能
        Toast.makeText(this, "取消预约: " + appointment.getDoctorName(), Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 获取当前用户ID
     */
    private int getCurrentUserId() {
        try {
            // 从SharedPreferences获取用户ID
            SharedPreferences sharedPreferences = getSharedPreferences("user_login_state", MODE_PRIVATE);
            int userId = sharedPreferences.getInt("user_id", -1);
            Log.d(TAG, "Retrieved user ID: " + userId);
            return userId;
        } catch (Exception e) {
            Log.e(TAG, "Failed to get current user ID", e);
            return -1;
        }
    }
    
    /**
     * 清除登录状态和无效的token
     */
    private void clearLoginState() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("user_login_state", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("access_token");
            editor.remove("user_id");
            editor.apply();
            Log.d(TAG, "Login state and token cleared due to authentication failure");
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear login state", e);
        }
    }
    
    /**
     * 显示/隐藏加载状态
     */
    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            tvEmptyView.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * 更新空视图显示
     */
    private void updateEmptyView() {
        if (appointmentList.isEmpty()) {
            tvEmptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * 显示错误信息
     */
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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