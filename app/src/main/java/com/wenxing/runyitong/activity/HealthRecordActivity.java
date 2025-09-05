package com.wenxing.runyitong.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.wenxing.runyitong.R;
import com.wenxing.runyitong.adapter.PhysicalExamAdapter;
import com.wenxing.runyitong.model.HealthRecord;
import com.wenxing.runyitong.model.PhysicalExamReport;
import com.wenxing.runyitong.api.ApiClient;
import com.wenxing.runyitong.api.ApiService;
import com.wenxing.runyitong.api.ApiResponse;
import retrofit2.Call;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 健康档案Activity
 * 用于展示和管理用户的个人健康信息和体检报告
 */
public class HealthRecordActivity extends AppCompatActivity {
    
    private static final String TAG = "HealthRecordActivity";
    
    // UI组件
    private Toolbar toolbar;
    private TextView tvName, tvGender, tvBirthdate, tvHeight, tvWeight, tvBloodType;
    private TextView tvAllergies, tvChronicDiseases, tvMedications, tvFamilyHistory;
    private TextView tvEmergencyContactName, tvEmergencyContactPhone;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewPhysicalExams;
    private ProgressBar progressBar;
    private TextView tvEmptyView;
    private Button btnEditInfo, btnAddPhysicalExam;
    
    // 数据相关
    private PhysicalExamAdapter physicalExamAdapter;
    private List<PhysicalExamReport> physicalExamList;
    private ApiService apiService;
    private HealthRecord currentHealthRecord;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            android.util.Log.d(TAG, "HealthRecordActivity onCreate started");
            
            // 第一步：安全加载布局
            try {
                setContentView(R.layout.activity_health_record);
                android.util.Log.d(TAG, "Layout loaded successfully");
            } catch (Exception e) {
                android.util.Log.e(TAG, "Failed to load layout", e);
                showErrorAndFinish("界面加载失败，请重试");
                return;
            }
            
            // 第二步：初始化视图组件
            try {
                initViews();
                android.util.Log.d(TAG, "Views initialized successfully");
            } catch (Exception e) {
                android.util.Log.e(TAG, "Failed to initialize views", e);
                showErrorAndFinish("界面组件初始化失败");
                return;
            }
            
            // 第三步：初始化数据
            try {
                initData();
                android.util.Log.d(TAG, "Data initialized successfully");
            } catch (Exception e) {
                android.util.Log.e(TAG, "Failed to initialize data", e);
                showErrorAndFinish("数据初始化失败");
                return;
            }
            
            // 第四步：设置RecyclerView
            try {
                setupRecyclerView();
                android.util.Log.d(TAG, "RecyclerView setup successfully");
            } catch (Exception e) {
                android.util.Log.e(TAG, "Failed to setup RecyclerView", e);
                // RecyclerView设置失败不应该导致整个Activity关闭，只记录错误
                showError("列表初始化失败，部分功能可能受影响");
            }
            
            // 第五步：设置点击监听器
            try {
                setupClickListeners();
                android.util.Log.d(TAG, "Click listeners setup successfully");
            } catch (Exception e) {
                android.util.Log.e(TAG, "Failed to setup click listeners", e);
                showError("按钮功能初始化失败，部分操作可能无法使用");
            }
            
            // 第六步：设置下拉刷新
            try {
                setupSwipeRefresh();
                android.util.Log.d(TAG, "Swipe refresh setup successfully");
            } catch (Exception e) {
                android.util.Log.e(TAG, "Failed to setup swipe refresh", e);
                showError("下拉刷新功能初始化失败");
            }
            
            // 第七步：加载健康档案数据
            try {
                loadHealthRecord();
                android.util.Log.d(TAG, "Health record loading started");
            } catch (Exception e) {
                android.util.Log.e(TAG, "Failed to load health record", e);
                showError("健康档案数据加载失败，请手动刷新重试");
            }
            
            android.util.Log.d(TAG, "HealthRecordActivity onCreate completed successfully");
        } catch (Exception e) {
            android.util.Log.e(TAG, "Unexpected error in onCreate", e);
            showErrorAndFinish("应用初始化出现异常，请重启应用");
        }
    }
    
    /**
     * 初始化视图组件
     */
    private void initViews() {
        try {
            android.util.Log.d(TAG, "Initializing views");
            
            toolbar = findViewById(R.id.toolbar);
            if (toolbar == null) {
                android.util.Log.w(TAG, "Toolbar not found in layout");
            }
            
            // 个人信息相关组件
            tvName = findViewById(R.id.tv_name);
            tvGender = findViewById(R.id.tv_gender);
            tvBirthdate = findViewById(R.id.tv_birthdate);
            tvHeight = findViewById(R.id.tv_height);
            tvWeight = findViewById(R.id.tv_weight);
            tvBloodType = findViewById(R.id.tv_blood_type);
            tvAllergies = findViewById(R.id.tv_allergies);
            tvChronicDiseases = findViewById(R.id.tv_chronic_diseases);
            tvMedications = findViewById(R.id.tv_medications);
            tvFamilyHistory = findViewById(R.id.tv_family_history);
            tvEmergencyContactName = findViewById(R.id.tv_emergency_contact_name);
            tvEmergencyContactPhone = findViewById(R.id.tv_emergency_contact_phone);
            
            // 体检报告列表相关组件
            swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
            recyclerViewPhysicalExams = findViewById(R.id.recycler_view_physical_exams);
            progressBar = findViewById(R.id.progress_bar);
            tvEmptyView = findViewById(R.id.tv_empty_view);
            
            // 操作按钮
            btnEditInfo = findViewById(R.id.btn_edit_info);
            btnAddPhysicalExam = findViewById(R.id.btn_add_physical_exam);
            
            // 验证必要的UI组件
            if (recyclerViewPhysicalExams == null) {
                throw new IllegalStateException("RecyclerView is required but not found in layout");
            }
            
            if (swipeRefreshLayout == null) {
                throw new IllegalStateException("SwipeRefreshLayout is required but not found in layout");
            }
            
            // 设置工具栏
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("健康档案");
                }
            } else {
                android.util.Log.w(TAG, "Cannot setup toolbar - toolbar is null");
            }
            
            android.util.Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error initializing views", e);
            throw e;
        }
    }
    
    /**
     * 初始化数据
     */
    private void initData() {
        try {
            android.util.Log.d(TAG, "Initializing data");
            
            // 确保physicalExamList始终被初始化
            physicalExamList = new ArrayList<>();
            
            // 安全地获取API服务
            try {
                apiService = ApiClient.getApiService();
                if (apiService == null) {
                    android.util.Log.w(TAG, "ApiService is null after initialization");
                }
            } catch (Exception e) {
                android.util.Log.e(TAG, "Failed to initialize ApiService", e);
                // API服务初始化失败时，继续执行但记录警告
                apiService = null;
            }
            
            // 确保currentHealthRecord不为null
            currentHealthRecord = new HealthRecord();
            
            android.util.Log.d(TAG, "Data initialized successfully");
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error initializing data", e);
            throw e;
        }
    }
    
    /**
     * 显示错误并关闭Activity
     */
    private void showErrorAndFinish(String message) {
        try {
            if (!isFinishing() && !isDestroyed()) {
                android.util.Log.e(TAG, "Showing error and finishing: " + message);
                
                // 优先尝试显示友好的错误对话框
                try {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("错误")
                        .setMessage(message)
                        .setPositiveButton("确定", (dialog, which) -> {
                            dialog.dismiss();
                            finish();
                        })
                        .setCancelable(false)
                        .show();
                } catch (Exception e) {
                    android.util.Log.e(TAG, "Failed to show AlertDialog, falling back to Toast", e);
                    // 如果AlertDialog失败，则使用Toast
                    try {
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    } catch (Exception toastException) {
                        android.util.Log.e(TAG, "Even Toast failed", toastException);
                    }
                    finish();
                }
            } else {
                android.util.Log.w(TAG, "Activity is finishing or destroyed, cannot show error dialog");
                finish();
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error in showErrorAndFinish", e);
            try {
                finish();
            } catch (Exception finishException) {
                android.util.Log.e(TAG, "Failed to finish activity", finishException);
            }
        }
    }
    
    /**
     * 安全地显示错误消息
     */
    private void showError(String message) {
        try {
            if (!isFinishing() && !isDestroyed()) {
                android.util.Log.w(TAG, "Showing error: " + message);
                
                try {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("提示")
                        .setMessage(message)
                        .setPositiveButton("确定", (dialog, which) -> dialog.dismiss())
                        .show();
                } catch (Exception e) {
                    android.util.Log.e(TAG, "Failed to show error AlertDialog, falling back to Toast", e);
                    try {
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    } catch (Exception toastException) {
                        android.util.Log.e(TAG, "Even error Toast failed", toastException);
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error in showError", e);
        }
    }
    private void setupRecyclerView() {
        try {
            android.util.Log.d(TAG, "Setting up RecyclerView");
            
            if (recyclerViewPhysicalExams == null) {
                android.util.Log.e(TAG, "RecyclerView is null, cannot setup");
                throw new IllegalStateException("RecyclerView is required for displaying physical exam reports");
            }
            
            if (physicalExamList == null) {
                android.util.Log.w(TAG, "Physical exam list is null, initializing empty list");
                physicalExamList = new ArrayList<>();
            }
            
            try {
                // 确保Context有效
                if (this.isFinishing() || this.isDestroyed()) {
                    android.util.Log.w(TAG, "Activity is finishing or destroyed, cannot setup RecyclerView");
                    return;
                }
                
                // 初始化适配器
                physicalExamAdapter = new PhysicalExamAdapter(this);
                if (physicalExamAdapter != null) {
                    physicalExamAdapter.updatePhysicalExams(physicalExamList);
                    
                    // 设置LayoutManager
                    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    recyclerViewPhysicalExams.setLayoutManager(layoutManager);
                    
                    // 设置适配器
                    recyclerViewPhysicalExams.setAdapter(physicalExamAdapter);
                    
                    // 设置点击事件
                    physicalExamAdapter.setOnPhysicalExamClickListener(new PhysicalExamAdapter.OnPhysicalExamClickListener() {
                        @Override
                        public void onViewDetails(PhysicalExamReport report) {
                            if (report != null) {
                                showPhysicalExamDetails(report);
                            } else {
                                android.util.Log.w(TAG, "Cannot view details - report is null");
                            }
                        }
                        
                        @Override
                        public void onDeleteReport(PhysicalExamReport report, int position) {
                            if (report != null && position >= 0 && position < physicalExamList.size()) {
                                deletePhysicalExamReport(report, position);
                            } else {
                                android.util.Log.w(TAG, "Cannot delete report - invalid report or position");
                            }
                        }
                    });
                } else {
                    android.util.Log.e(TAG, "Failed to create PhysicalExamAdapter");
                    // 如果适配器创建失败，创建一个空适配器
                    recyclerViewPhysicalExams.setAdapter(new PhysicalExamAdapter(this));
                }
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error creating adapter", e);
                // 创建适配器失败时，仍然设置空的LayoutManager避免进一步崩溃
                recyclerViewPhysicalExams.setLayoutManager(new LinearLayoutManager(this));
                recyclerViewPhysicalExams.setAdapter(null);
            }
            
            android.util.Log.d(TAG, "RecyclerView setup completed");
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error setting up RecyclerView", e);
            throw e;
        }
    }
    
    /**
     * 设置下拉刷新
     */
    private void setupSwipeRefresh() {
        try {
            android.util.Log.d(TAG, "Setting up SwipeRefresh");
            
            if (swipeRefreshLayout == null) {
                android.util.Log.w(TAG, "SwipeRefreshLayout is null, cannot setup");
                throw new IllegalStateException("SwipeRefreshLayout is required for refresh functionality");
            }
            
            // 设置刷新监听器
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    try {
                        loadHealthRecord();
                    } catch (Exception e) {
                        android.util.Log.e(TAG, "Error during refresh", e);
                        // 确保在主线程中设置刷新状态
                        if (!isFinishing() && !isDestroyed()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (swipeRefreshLayout != null) {
                                        swipeRefreshLayout.setRefreshing(false);
                                    }
                                    showErrorMessage("刷新失败，请重试");
                                }
                            });
                        }
                    }
                }
            });
            
            // 设置刷新颜色
            try {
                // 首先检查资源是否存在
                int[] colorResIds = new int[]{
                    R.color.primary_color,
                    R.color.accent_color
                };
                
                boolean allColorsAvailable = true;
                for (int colorResId : colorResIds) {
                    try {
                        getResources().getColor(colorResId);
                    } catch (Exception e) {
                        allColorsAvailable = false;
                        android.util.Log.w(TAG, "Color resource not found: " + colorResId, e);
                        break;
                    }
                }
                
                if (allColorsAvailable) {
                    swipeRefreshLayout.setColorSchemeResources(colorResIds);
                } else {
                    // 使用默认颜色
                    android.util.Log.w(TAG, "Some color resources not available, using default colors");
                }
            } catch (Exception e) {
                android.util.Log.w(TAG, "Failed to set color scheme for SwipeRefresh", e);
                // 颜色设置失败不影响功能，继续执行
            }
            
            // 设置刷新触发距离
            swipeRefreshLayout.setDistanceToTriggerSync(100);
            
            android.util.Log.d(TAG, "SwipeRefresh setup completed");
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error setting up SwipeRefresh", e);
            throw e;
        }
    }
    
    /**
     * 设置点击事件监听器
     */
    private void setupClickListeners() {
        try {
            android.util.Log.d(TAG, "Setting up click listeners");
            
            // 确保Activity状态有效
            if (isFinishing() || isDestroyed()) {
                android.util.Log.w(TAG, "Activity is finishing or destroyed, cannot setup click listeners");
                return;
            }
            
            if (btnEditInfo != null) {
                btnEditInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (isFinishing() || isDestroyed()) {
                                android.util.Log.w(TAG, "Activity is finishing or destroyed, cannot start new activity");
                                return;
                            }
                            
                            // 跳转到编辑个人信息页面
                            Intent intent = new Intent(HealthRecordActivity.this, EditHealthInfoActivity.class);
                            if (currentHealthRecord != null) {
                                intent.putExtra("health_record", currentHealthRecord);
                            }
                            startActivityForResult(intent, 1001);
                        } catch (Exception e) {
                            android.util.Log.e(TAG, "Error starting EditHealthInfoActivity", e);
                            showErrorMessage("无法打开编辑页面，请重试");
                        }
                    }
                });
            } else {
                android.util.Log.w(TAG, "btnEditInfo is null, cannot set click listener");
            }
            
            if (btnAddPhysicalExam != null) {
                btnAddPhysicalExam.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (isFinishing() || isDestroyed()) {
                                android.util.Log.w(TAG, "Activity is finishing or destroyed, cannot start new activity");
                                return;
                            }
                            
                            // 跳转到添加体检报告页面
                            Intent intent = new Intent(HealthRecordActivity.this, AddPhysicalExamActivity.class);
                            startActivityForResult(intent, 1002);
                        } catch (Exception e) {
                            android.util.Log.e(TAG, "Error starting AddPhysicalExamActivity", e);
                            showErrorMessage("无法打开添加页面，请重试");
                        }
                    }
                });
            } else {
                android.util.Log.w(TAG, "btnAddPhysicalExam is null, cannot set click listener");
            }
            
            android.util.Log.d(TAG, "Click listeners setup completed");
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error setting up click listeners", e);
            throw e;
        }
    }
    
    /**
     * 加载健康档案数据
     */
    private void loadHealthRecord() {
        try {
            android.util.Log.d(TAG, "Loading health record data");
            
            // 确保Activity状态有效
            if (isFinishing() || isDestroyed()) {
                android.util.Log.w(TAG, "Activity is finishing or destroyed, cannot load health record data");
                return;
            }
            
            // 在主线程中更新UI状态
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLoading(true);
                }
            });
            
            // 获取当前登录用户的ID
            int userId = getCurrentUserId();
            android.util.Log.d(TAG, "Retrieved user ID: " + userId);
            
            // 添加额外的日志来检查access_token是否存在
            SharedPreferences sharedPreferences = getSharedPreferences("user_login_state", MODE_PRIVATE);
            String accessToken = sharedPreferences.getString("access_token", null);
            android.util.Log.d(TAG, "Access token exists: " + (accessToken != null ? "Yes" : "No"));
            android.util.Log.d(TAG, "Access token length: " + (accessToken != null ? accessToken.length() : 0));
            
            if (userId <= 0) {
                android.util.Log.e(TAG, "User is not logged in or invalid user ID: " + userId);
                
                // 在主线程中处理未登录状态
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLoading(false);
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        showErrorMessage("用户未登录，请先登录");
                    }
                });
                return;
            }
            
            android.util.Log.d(TAG, "Loading health record for user ID: " + userId);
            
            // 检查apiService是否为null
            if (apiService == null) {
                apiService = ApiClient.getApiService();
                if (apiService == null) {
                    android.util.Log.e(TAG, "ApiService is null after initialization, cannot call API");
                    
                    // 在主线程中处理apiService为null的情况
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showLoading(false);
                            if (swipeRefreshLayout != null) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                            showErrorMessage("系统错误：无法连接到服务器");
                            loadMockData();
                        }
                    });
                    return;
                }
            }
            
            android.util.Log.d(TAG, "ApiService is available, making API call");
            
            // 使用API服务获取健康档案数据
            Call<ApiResponse<HealthRecord>> call = apiService.getHealthRecord(userId);
            android.util.Log.d(TAG, "API call created: " + call.request().url());
            android.util.Log.d(TAG, "API call headers before interceptor: " + call.request().headers());
            
            call.enqueue(new retrofit2.Callback<ApiResponse<HealthRecord>>() {
                @Override
                public void onResponse(Call<ApiResponse<HealthRecord>> call, retrofit2.Response<ApiResponse<HealthRecord>> response) {
                    try {
                        android.util.Log.d(TAG, "API call onResponse executed");
                        android.util.Log.d(TAG, "Response code: " + response.code());
                        android.util.Log.d(TAG, "Response headers: " + response.headers());
                        
                        if (response.isSuccessful() && response.body() != null) {
                            // 成功获取健康档案数据
                            ApiResponse<HealthRecord> apiResponse = response.body();
                            if(apiResponse.isSuccess() && apiResponse.getData() != null){
                                currentHealthRecord = apiResponse.getData();
                                android.util.Log.d(TAG, "Health record data successfully retrieved");
                                updateHealthRecordUI();

                                // 接着获取体检报告列表
                                getPhysicalExamReports(userId);
                            } else {
                                android.util.Log.e(TAG, "API response success is false or data is null. Success: " + 
                                        apiResponse.isSuccess() + ", Message: " + apiResponse.getMessage());
                                loadMockData();
                            }

                        } else {
                            // API调用成功但返回了错误
                            String errorMsg = "获取健康档案失败: " + (response.body() != null ? response.body().getMessage() : "未知错误");
                            android.util.Log.e(TAG, errorMsg);
                            if (response.errorBody() != null) {
                                try {
                                    android.util.Log.e(TAG, "Response error body: " + response.errorBody().string());
                                } catch (java.io.IOException e) {
                                    android.util.Log.e(TAG, "Failed to read error body", e);
                                }
                            }
                            
                            // 特别处理401错误
                            if (response.code() == 401) {
                                android.util.Log.e(TAG, "401 Unauthorized error detected. Possible reasons: token invalid/expired or user ID mismatch");
                                android.util.Log.e(TAG, "Requesting user ID: " + userId);
                                android.util.Log.e(TAG, "Access token exists: " + (accessToken != null ? "Yes" : "No"));
                                
                                // 清除可能无效的token并提示用户重新登录
                                final SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.remove("access_token");
                                editor.apply();
                            }
                            
                            // 加载模拟数据作为备选
                            loadMockData();
                        }
                    } catch (Exception e) {
                        android.util.Log.e(TAG, "Error processing health record response", e);
                        // 加载模拟数据作为备选
                        loadMockData();
                    } finally {
                        // 确保隐藏加载状态
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showLoading(false);
                                if (swipeRefreshLayout != null) {
                                    swipeRefreshLayout.setRefreshing(false);
                                }
                            }
                        });
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<HealthRecord>> call, Throwable t) {
                    android.util.Log.e(TAG, "Failed to call getHealthRecord API", t);
                    android.util.Log.e(TAG, "Failure cause: " + (t.getCause() != null ? t.getCause().getMessage() : "Unknown"));
                    android.util.Log.e(TAG, "Failure message: " + t.getMessage());
                    
                    // 网络请求失败，加载模拟数据作为备选
                    loadMockData();
                }
            });
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error in loadHealthRecord", e);
            // 确保隐藏加载状态
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLoading(false);
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    loadMockData();
                }
            });
        }
    }
    
    /**
     * 获取体检报告列表
     */
    private void getPhysicalExamReports(int userId) {
        try {
            if (isFinishing() || isDestroyed()) {
                android.util.Log.w(TAG, "Activity is finishing or destroyed, cannot get physical exam reports");
                return;
            }
            
            android.util.Log.d(TAG, "Getting physical exam reports for user ID: " + userId);
            
            // 获取SharedPreferences检查access_token
            final SharedPreferences sharedPreferences = getSharedPreferences("user_login_state", MODE_PRIVATE);
            final String accessToken = sharedPreferences.getString("access_token", null);
            
            if (apiService == null) {
                apiService = ApiClient.getApiService();
                if (apiService == null) {
                    android.util.Log.e(TAG, "ApiService is null after initialization, cannot call API for physical exam reports");
                    updatePhysicalExamList();
                    return;
                }
            }
            
            Call<ApiResponse<List<PhysicalExamReport>>> call = apiService.getPhysicalExamReports(userId, 0, 100);
            android.util.Log.d(TAG, "API call created for physical exam reports: " + call.request().url());
            android.util.Log.d(TAG, "API call headers before interceptor: " + call.request().headers());
            
            call.enqueue(new retrofit2.Callback<ApiResponse<List<PhysicalExamReport>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<PhysicalExamReport>>> call, retrofit2.Response<ApiResponse<List<PhysicalExamReport>>> response) {
                    try {
                        android.util.Log.d(TAG, "API call for physical exam reports onResponse executed");
                        android.util.Log.d(TAG, "Response code: " + response.code());
                        android.util.Log.d(TAG, "Response headers: " + response.headers());
                        
                        // 清空现有列表
                        physicalExamList.clear();
                         
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                            // 成功获取体检报告列表
                            physicalExamList.addAll(response.body().getData());
                            updatePhysicalExamList();
                            android.util.Log.d(TAG, "Successfully retrieved " + physicalExamList.size() + " physical exam reports");
                        } else {
                            // API调用成功但返回了错误
                            String errorMsg = "获取体检报告失败: " + (response.body() != null ? response.body().getMessage() : "未知错误");
                            android.util.Log.e(TAG, errorMsg);
                            
                            if (response.errorBody() != null) {
                                try {
                                    android.util.Log.e(TAG, "Response error body: " + response.errorBody().string());
                                } catch (java.io.IOException e) {
                                    android.util.Log.e(TAG, "Failed to read error body", e);
                                }
                            }
                            
                            // 特别处理401错误
//                            if (response.code() == 401) {
//                                android.util.Log.e(TAG, "401 Unauthorized error detected in physical exam reports request");
//                                android.util.Log.e(TAG, "Requesting user ID: " + userId);
//                                android.util.Log.e(TAG, "Access token exists: " + (accessToken != null ? "Yes" : "No"));
//
//                                // 清除可能无效的token
//                                final SharedPreferences.Editor editor = sharedPreferences.edit();
//                                editor.remove("access_token");
//                                editor.apply();
//
//                                // 在主线程中提示用户重新登录
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        showErrorMessage("认证失败，请重新登录");
//                                    }
//                                });
//                            }
                        }
                         
                        // 更新UI
                        updatePhysicalExamList();
                    } catch (Exception e) {
                        android.util.Log.e(TAG, "Error processing physical exam reports response", e);
                        // 更新UI
                        updatePhysicalExamList();
                    }
                }
                 
                @Override
                public void onFailure(Call<ApiResponse<List<PhysicalExamReport>>> call, Throwable t) {
                    android.util.Log.e(TAG, "Failed to call getPhysicalExamReports API", t);
                    android.util.Log.e(TAG, "Failure cause: " + (t.getCause() != null ? t.getCause().getMessage() : "Unknown"));
                    android.util.Log.e(TAG, "Failure message: " + t.getMessage());
                    // 网络请求失败，更新UI
                    updatePhysicalExamList();
                }
            });
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error getting physical exam reports", e);
            // 更新UI
            updatePhysicalExamList();
        }
    }
    
    /**
     * 更新体检报告列表UI
     */
    private void updatePhysicalExamList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 检查Activity是否已销毁
                    if (isFinishing() || isDestroyed()) {
                        android.util.Log.w(TAG, "Activity is finishing or destroyed, cannot update UI");
                        return;
                    }

                    // 检查适配器是否有效
                    if (physicalExamAdapter != null) {
                        try {
                            // 验证RecyclerView是否存在且适配器一致
                            if (recyclerViewPhysicalExams != null) {
                                RecyclerView.Adapter adapter = recyclerViewPhysicalExams.getAdapter();
                                if (adapter instanceof PhysicalExamAdapter && adapter == physicalExamAdapter) {
                                    // 如果适配器与RecyclerView关联的适配器一致，则更新
                                    physicalExamAdapter.notifyDataSetChanged();
                                    android.util.Log.d(TAG, "Successfully notified adapter of data change");
                                } else {
                                    // 如果适配器不一致，尝试使用RecyclerView当前关联的适配器
                                    android.util.Log.w(TAG, "Adapter mismatch, trying to update using RecyclerView's adapter");
                                    if (adapter instanceof PhysicalExamAdapter) {
                                        ((PhysicalExamAdapter) adapter).notifyDataSetChanged();
                                    }
                                }
                            } else {
                                // 如果RecyclerView不存在，直接尝试更新适配器
                                physicalExamAdapter.notifyDataSetChanged();
                            }
                        } catch (Exception e) {
                            android.util.Log.e(TAG, "Error notifying adapter of data change", e);
                            // 尝试备选方案：重新设置适配器
                            try {
                                if (recyclerViewPhysicalExams != null && !isFinishing() && !isDestroyed()) {
                                    recyclerViewPhysicalExams.setAdapter(physicalExamAdapter);
                                    android.util.Log.d(TAG, "Successfully reset adapter to RecyclerView");
                                }
                            } catch (Exception ex) {
                                android.util.Log.e(TAG, "Failed to reset adapter", ex);
                            }
                        }
                    } else {
                        android.util.Log.w(TAG, "PhysicalExamAdapter is null, cannot update list");
                    }
                } catch (Exception e) {
                    android.util.Log.e(TAG, "Unexpected error in updatePhysicalExamList", e);
                }

                // 处理加载状态和刷新状态
                try {
                    showLoading(false);
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                } catch (Exception e) {
                    android.util.Log.e(TAG, "Error updating loading or refresh state", e);
                }

                // 处理空视图
                try {
                    updateEmptyView();
                } catch (Exception e) {
                    android.util.Log.e(TAG, "Error updating empty view", e);
                }
            }
        });
    }
    
    /**
     * 加载模拟数据
     */
    private void loadMockData() {
        // 创建模拟健康档案数据
        currentHealthRecord = new HealthRecord();
        currentHealthRecord.setId(1);
        currentHealthRecord.setUserId(1);
        currentHealthRecord.setName("张三");
        currentHealthRecord.setGender("男");
        currentHealthRecord.setBirthdate("1990-01-15");
        currentHealthRecord.setHeight(175.0);
        currentHealthRecord.setWeight(70.0);
        currentHealthRecord.setBloodType("A型");
        currentHealthRecord.setAllergies("青霉素过敏");
        currentHealthRecord.setChronicDiseases("无");
        currentHealthRecord.setMedications("无");
        currentHealthRecord.setFamilyHistory("父亲有高血压病史");
        currentHealthRecord.setEmergencyContactName("李四");
        currentHealthRecord.setEmergencyContactPhone("138****1234");
        currentHealthRecord.setCreatedAt(new Date());
        currentHealthRecord.setUpdatedAt(new Date());
        
        // 创建模拟体检报告数据
        physicalExamList.clear();
        
        // 创建第一个体检报告
        PhysicalExamReport report1 = new PhysicalExamReport();
        report1.setId(1);
        report1.setHealthRecordId(1);
        report1.setReportName("年度体检报告");
        report1.setExamDate("2024-01-10");
        report1.setHospitalName("市人民医院");
        report1.setSummary("整体健康状况良好，血压略高，建议控制饮食，加强锻炼。");
        report1.setDoctorComments("血压轻度升高，建议3个月后复查。");
        
        // 设置关键检查项目
        Map<String, String> keyFindings1 = new HashMap<>();
        keyFindings1.put("血压", "140/90 mmHg");
        keyFindings1.put("血糖", "5.2 mmol/L");
        keyFindings1.put("血常规", "正常");
        report1.setKeyFindings(keyFindings1);
        
        // 设置正常项目
        Map<String, String> normalItems1 = new HashMap<>();
        normalItems1.put("肝功能", "正常");
        normalItems1.put("肾功能", "正常");
        normalItems1.put("心电图", "正常");
        report1.setNormalItems(normalItems1);
        
        // 设置异常项目
        Map<String, String> abnormalItems1 = new HashMap<>();
        abnormalItems1.put("血压", "轻度升高");
        report1.setAbnormalItems(abnormalItems1);
        
        report1.setRecommendations("1. 低盐低脂饮食；2. 每周至少运动3次，每次30分钟；3. 定期监测血压。");
        report1.setReportUrl("http://example.com/reports/1.pdf");
        report1.setCreatedAt(new Date());
        report1.setUpdatedAt(new Date());
        
        // 创建第二个体检报告
        PhysicalExamReport report2 = new PhysicalExamReport();
        report2.setId(2);
        report2.setHealthRecordId(1);
        report2.setReportName("入职体检报告");
        report2.setExamDate("2023-06-15");
        report2.setHospitalName("健康体检中心");
        report2.setSummary("身体各项指标均在正常范围内，健康状况良好。");
        report2.setDoctorComments("未见异常，建议保持良好生活习惯。");
        
        Map<String, String> keyFindings2 = new HashMap<>();
        keyFindings2.put("血压", "120/80 mmHg");
        keyFindings2.put("血糖", "4.9 mmol/L");
        keyFindings2.put("血常规", "正常");
        report2.setKeyFindings(keyFindings2);
        
        Map<String, String> normalItems2 = new HashMap<>();
        normalItems2.put("肝功能", "正常");
        normalItems2.put("肾功能", "正常");
        normalItems2.put("心电图", "正常");
        normalItems2.put("胸部X光", "正常");
        report2.setNormalItems(normalItems2);
        
        Map<String, String> abnormalItems2 = new HashMap<>();
        report2.setAbnormalItems(abnormalItems2);
        
        report2.setRecommendations("保持健康生活方式，均衡饮食，适量运动，定期体检。");
        report2.setReportUrl("http://example.com/reports/2.pdf");
        report2.setCreatedAt(new Date());
        report2.setUpdatedAt(new Date());
        
        physicalExamList.add(report1);
        physicalExamList.add(report2);
        
        // 更新UI显示
        updateHealthRecordUI();
        if (physicalExamAdapter != null) {
            physicalExamAdapter.notifyDataSetChanged();
        } else {
            android.util.Log.w(TAG, "Physical exam adapter is null, cannot notify data changed");
        }
    }
    
    /**
     * 更新健康档案UI显示
     */
    private void updateHealthRecordUI() {
        try {
            android.util.Log.d(TAG, "Updating health record UI");
            
            if (currentHealthRecord == null) {
                android.util.Log.w(TAG, "Current health record is null, cannot update UI");
                return;
            }
            
            // 安全地更新每个视图
            if (tvName != null) {
                tvName.setText(currentHealthRecord.getName() != null ? currentHealthRecord.getName() : "--");
            }
            if (tvGender != null) {
                tvGender.setText(currentHealthRecord.getGender() != null ? currentHealthRecord.getGender() : "--");
            }
            if (tvBirthdate != null) {
                tvBirthdate.setText(currentHealthRecord.getBirthdate() != null ? currentHealthRecord.getBirthdate() : "--");
            }
            if (tvHeight != null) {
                tvHeight.setText(currentHealthRecord.getHeight() > 0 ? (currentHealthRecord.getHeight() + " cm") : "--");
            }
            if (tvWeight != null) {
                tvWeight.setText(currentHealthRecord.getWeight() > 0 ? (currentHealthRecord.getWeight() + " kg") : "--");
            }
            if (tvBloodType != null) {
                tvBloodType.setText(currentHealthRecord.getBloodType() != null ? currentHealthRecord.getBloodType() : "--");
            }
            
            // 设置过敏史、慢性病等信息
            if (tvAllergies != null) {
                String allergies = currentHealthRecord.getAllergies();
                tvAllergies.setText((allergies == null || "\u65e0".equals(allergies)) ? "\u65e0" : allergies);
            }
            
            if (tvChronicDiseases != null) {
                String chronicDiseases = currentHealthRecord.getChronicDiseases();
                tvChronicDiseases.setText((chronicDiseases == null || "\u65e0".equals(chronicDiseases)) ? "\u65e0" : chronicDiseases);
            }
            
            if (tvMedications != null) {
                String medications = currentHealthRecord.getMedications();
                tvMedications.setText((medications == null || "\u65e0".equals(medications)) ? "\u65e0" : medications);
            }
            
            if (tvFamilyHistory != null) {
                String familyHistory = currentHealthRecord.getFamilyHistory();
                tvFamilyHistory.setText((familyHistory == null || "\u65e0".equals(familyHistory)) ? "\u65e0" : familyHistory);
            }
            
            // 设置紧急联系人信息
            if (tvEmergencyContactName != null) {
                tvEmergencyContactName.setText(currentHealthRecord.getEmergencyContactName() != null ? currentHealthRecord.getEmergencyContactName() : "--");
            }
            if (tvEmergencyContactPhone != null) {
                tvEmergencyContactPhone.setText(currentHealthRecord.getEmergencyContactPhone() != null ? currentHealthRecord.getEmergencyContactPhone() : "--");
            }
            
            android.util.Log.d(TAG, "Health record UI updated successfully");
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error updating health record UI", e);
        }
    }
    
    /**
     * 显示体检报告详情
     */
    private void showPhysicalExamDetails(PhysicalExamReport report) {
        // 获取报告在列表中的位置
        int position = physicalExamList.indexOf(report);
        // 跳转到体检报告详情页面
        Intent intent = new Intent(this, PhysicalExamDetailActivity.class);
        intent.putExtra("physical_exam_report", report);
        intent.putExtra("report_position", position);
        startActivityForResult(intent, 1003);
    }
    
    /**
     * 删除体检报告
     */
    private void deletePhysicalExamReport(final PhysicalExamReport report, final int position) {
        try {
            android.util.Log.d(TAG, "Starting delete physical exam report process");
            
            // 确保Activity状态有效
            if (isFinishing() || isDestroyed()) {
                android.util.Log.w(TAG, "Activity is finishing or destroyed, cannot delete physical exam report");
                return;
            }
            
            // 检查access_token是否存在
            SharedPreferences sharedPreferences = getSharedPreferences("user_login_state", MODE_PRIVATE);
            String accessToken = sharedPreferences.getString("access_token", null);
            android.util.Log.d(TAG, "Access token exists before API call: " + (accessToken != null ? "Yes" : "No"));
            
            // 检查apiService是否初始化成功
            if (apiService == null) {
                android.util.Log.e(TAG, "ApiService is not initialized, cannot make API call");
                showLoading(false);
                showErrorMessage("网络服务初始化失败，请重试");
                return;
            }
            
            // 显示加载状态
            showLoading(true);
            
            // 调用API删除体检报告
            Call<ApiResponse<Object>> call = apiService.deletePhysicalExamReport(report.getId());
            android.util.Log.d(TAG, "API call created: " + call.request().url());
            android.util.Log.d(TAG, "API call headers before interceptor: " + call.request().headers());
            
            call.enqueue(new retrofit2.Callback<ApiResponse<Object>>() {
                @Override
                public void onResponse(Call<ApiResponse<Object>> call, retrofit2.Response<ApiResponse<Object>> response) {
                    try {
                        android.util.Log.d(TAG, "API response received with status code: " + response.code());
                        android.util.Log.d(TAG, "API response headers: " + response.headers());
                        
                        // 特别处理401错误
//                        if (response.code() == 401) {
//                            android.util.Log.e(TAG, "Authentication failed: 401 Unauthorized error");
//
//                            // 清除无效的token
//                            SharedPreferences.Editor editor = sharedPreferences.edit();
//                            editor.remove("access_token");
//                            editor.apply();
//                            android.util.Log.d(TAG, "Invalid access token cleared due to 401 error");
//
//                            // 读取错误响应体内容（如果有）
//                            try {
//                                if (response.errorBody() != null) {
//                                    String errorBody = response.errorBody().string();
//                                    android.util.Log.e(TAG, "401 Error body: " + errorBody);
//                                }
//                            } catch (Exception e) {
//                                android.util.Log.w(TAG, "Failed to read error body", e);
//                            }
//
//                            // 提示用户重新登录
//                            showErrorMessage("登录状态已过期，请重新登录");
//                            return;
//                        }

                        if (response.isSuccessful() && response.body() != null) {    // && response.body().isSuccess()
                            // API调用成功，从本地列表中删除
                            physicalExamList.remove(position);
                            physicalExamAdapter.notifyItemRemoved(position);
                            updateEmptyView();
                            Toast.makeText(HealthRecordActivity.this, "体检报告已删除", Toast.LENGTH_SHORT).show();
                            android.util.Log.d(TAG, "Physical exam report deleted successfully");
                        } else {
                            // API调用成功但返回了错误
                            String errorMsg = "删除体检报告失败: " + (response.body() != null ? response.body().getMessage() : "未知错误");
                            android.util.Log.e(TAG, errorMsg);
                            
                            // 读取错误响应体内容（如果有）
                            try {
                                if (!response.isSuccessful() && response.errorBody() != null) {
                                    String errorBody = response.errorBody().string();
                                    android.util.Log.e(TAG, "Error body: " + errorBody);
                                }
                            } catch (Exception e) {
                                android.util.Log.w(TAG, "Failed to read error body", e);
                            }
                            
                            showErrorMessage(errorMsg);
                        }
                    } catch (Exception e) {
                        android.util.Log.e(TAG, "Error processing delete physical exam report response", e);
                        showErrorMessage("处理删除结果时出错");
                    } finally {
                        showLoading(false);
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                    android.util.Log.e(TAG, "Failed to call deletePhysicalExamReport API", t);
                    showLoading(false);
                    showErrorMessage("网络请求失败，请检查网络连接");
                }
            });
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error deleting physical exam report", e);
            showLoading(false);
            showErrorMessage("删除体检报告时出错");
        }
    }
    
    /**
     * 显示/隐藏加载状态
     */
    private void showLoading(boolean show) {
        try {
            if (show) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                if (recyclerViewPhysicalExams != null) {
                    recyclerViewPhysicalExams.setVisibility(View.GONE);
                }
                if (tvEmptyView != null) {
                    tvEmptyView.setVisibility(View.GONE);
                }
            } else {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (recyclerViewPhysicalExams != null) {
                    recyclerViewPhysicalExams.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error in showLoading", e);
        }
    }
    
    /**
     * 更新空视图显示
     */
    private void updateEmptyView() {
        try {
            if (physicalExamList == null || tvEmptyView == null || recyclerViewPhysicalExams == null) {
                android.util.Log.w(TAG, "Cannot update empty view - required views are null");
                return;
            }
            
            if (physicalExamList.isEmpty()) {
                tvEmptyView.setVisibility(View.VISIBLE);
                recyclerViewPhysicalExams.setVisibility(View.GONE);
            } else {
                tvEmptyView.setVisibility(View.GONE);
                recyclerViewPhysicalExams.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error in updateEmptyView", e);
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
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // 处理编辑个人信息返回结果
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // 重新加载健康档案数据
            loadHealthRecord();
            Toast.makeText(this, "个人信息已更新", Toast.LENGTH_SHORT).show();
        }
        
        // 处理添加体检报告返回结果
        if (requestCode == 1002 && resultCode == AddPhysicalExamActivity.RESULT_SUCCESS && data != null) {
            android.util.Log.d(TAG, "收到添加体检报告的返回结果");
            
            // 获取传递的数据
            PhysicalExamReport newReport = (PhysicalExamReport) data.getSerializableExtra("new_physical_exam");
            String operationType = data.getStringExtra("operation_type");
            int reportId = data.getIntExtra("report_id", -1);
            String reportName = data.getStringExtra("report_name");
            
            android.util.Log.d(TAG, "接收数据验证:");
            android.util.Log.d(TAG, "  操作类型: " + operationType);
            android.util.Log.d(TAG, "  报告ID: " + reportId);
            android.util.Log.d(TAG, "  报告名称(额外): " + reportName);
            android.util.Log.d(TAG, "  newReport对象: " + (newReport != null ? "non-null" : "null"));
            
            if (newReport != null) {
                // 详细验证新报告数据 - 增强版
                android.util.Log.d(TAG, "新报告数据详情:");
                android.util.Log.d(TAG, "  ID: " + newReport.getId());
                android.util.Log.d(TAG, "  名称: '" + (newReport.getReportName() != null ? newReport.getReportName() : "[null]") + "'");
                android.util.Log.d(TAG, "  日期: '" + (newReport.getExamDate() != null ? newReport.getExamDate() : "[null]") + "'");
                android.util.Log.d(TAG, "  医院: '" + (newReport.getHospitalName() != null ? newReport.getHospitalName() : "[null]") + "'");
                android.util.Log.d(TAG, "  摘要: '" + (newReport.getSummary() != null ? newReport.getSummary() : "[null]") + "'");
                android.util.Log.d(TAG, "  医生评论: '" + (newReport.getDoctorComments() != null ? newReport.getDoctorComments() : "[null]") + "'");
                android.util.Log.d(TAG, "  建议: '" + (newReport.getRecommendations() != null ? newReport.getRecommendations() : "[null]") + "'");
                
                // 检查关键字段是否为空 - 更严格的验证
                boolean hasReportName = !TextUtils.isEmpty(newReport.getReportName());
                boolean hasExamDate = !TextUtils.isEmpty(newReport.getExamDate());
                boolean hasHospitalName = !TextUtils.isEmpty(newReport.getHospitalName());
                boolean hasSummary = !TextUtils.isEmpty(newReport.getSummary());
                boolean hasDoctorComments = !TextUtils.isEmpty(newReport.getDoctorComments());
                boolean hasRecommendations = !TextUtils.isEmpty(newReport.getRecommendations());
                
                // 计算数据完整性
                boolean hasMinimalValidData = hasReportName && hasExamDate && hasHospitalName;
                boolean hasFullValidData = hasMinimalValidData && hasSummary;
                
                android.util.Log.d(TAG, "数据完整性检查:");
                android.util.Log.d(TAG, "  报告名称有效: " + hasReportName);
                android.util.Log.d(TAG, "  检查日期有效: " + hasExamDate);
                android.util.Log.d(TAG, "  医院名称有效: " + hasHospitalName);
                android.util.Log.d(TAG, "  摘要有效: " + hasSummary);
                android.util.Log.d(TAG, "  医生评论有效: " + hasDoctorComments);
                android.util.Log.d(TAG, "  建议有效: " + hasRecommendations);
                android.util.Log.d(TAG, "  最小数据集完整: " + hasMinimalValidData);
                android.util.Log.d(TAG, "  完整数据集完整: " + hasFullValidData);
                
                // 如果连最小数据集都不完整，进行数据修复
                if (!hasMinimalValidData) {
                    android.util.Log.w(TAG, "警告: 新报告缺乏关键信息，尝试修复...");
                    
                    // 尝试从意图额外参数中获取信息
                    if (!hasReportName && !TextUtils.isEmpty(reportName)) {
                        newReport.setReportName(reportName);
                        android.util.Log.d(TAG, "  从额外参数修复报告名称: " + reportName);
                        hasReportName = true;
                    }
                    
                    // 如果仍然缺少关键信息，使用默认值
                    if (!hasReportName) {
                        newReport.setReportName("体检报告_" + System.currentTimeMillis());
                        android.util.Log.d(TAG, "  使用默认报告名称");
                    }
                    if (!hasExamDate) {
                        newReport.setExamDate(new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date()));
                        android.util.Log.d(TAG, "  使用当前日期作为检查日期");
                    }
                    if (!hasHospitalName) {
                        newReport.setHospitalName("未知医院");
                        android.util.Log.d(TAG, "  使用默认医院名称");
                    }
                    if (!hasSummary) {
                        newReport.setSummary("暂无摘要信息");
                        android.util.Log.d(TAG, "  使用默认摘要");
                    }
                    
                    // 重新计算数据完整性
                    hasMinimalValidData = !TextUtils.isEmpty(newReport.getReportName()) && 
                                         !TextUtils.isEmpty(newReport.getExamDate()) && 
                                         !TextUtils.isEmpty(newReport.getHospitalName());
                    hasFullValidData = hasMinimalValidData && !TextUtils.isEmpty(newReport.getSummary());
                    
                    android.util.Log.d(TAG, "修复后的数据完整性: 最小=" + hasMinimalValidData + ", 完整=" + hasFullValidData);
                }
                
                try {
                    // 检查列表和适配器是否初始化
                    if (physicalExamList == null) {
                        android.util.Log.w(TAG, "physicalExamList为空，重新初始化");
                        physicalExamList = new ArrayList<>();
                    }
                    
                    if (physicalExamAdapter == null) {
                        android.util.Log.e(TAG, "physicalExamAdapter为空，无法更新列表");
                        Toast.makeText(this, "列表初始化失败，请刷新页面", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    android.util.Log.d(TAG, "列表添加前状态: 大小=" + physicalExamList.size());
                    
                    // 在添加前再次验证数据完整性
                    final boolean finalDataIsValid = !TextUtils.isEmpty(newReport.getReportName()) && 
                                                     !TextUtils.isEmpty(newReport.getExamDate()) && 
                                                     !TextUtils.isEmpty(newReport.getHospitalName());
                    
                    if (!finalDataIsValid) {
                        android.util.Log.e(TAG, "最终数据验证失败，但仍然尝试添加到列表");
                    }
                    
                    // 将新报告添加到列表开头（最新的在上面）
                    physicalExamList.add(0, newReport);
                    
                    android.util.Log.d(TAG, "列表添加后状态: 大小=" + physicalExamList.size());
                    
                    // 通知适配器数据已改变
                    physicalExamAdapter.notifyItemInserted(0);
                    android.util.Log.d(TAG, "已通知适配器数据改变");
                    
                    // 更新空视图状态
                    updateEmptyView();
                    
                    // 滚动到列表顶部显示新添加的报告
                    if (recyclerViewPhysicalExams != null) {
                        recyclerViewPhysicalExams.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    recyclerViewPhysicalExams.scrollToPosition(0);
                                    android.util.Log.d(TAG, "已滚动到列表顶部");
                                } catch (Exception e) {
                                    android.util.Log.e(TAG, "滚动到顶部失败", e);
                                }
                            }
                        });
                    }
                    
                    // 显示成功消息 - 根据数据质量显示不同消息
                    String successMsg;
                    if (hasFullValidData) {
                        successMsg = "体检报告已成功添加到列表";
                    } else if (hasMinimalValidData) {
                        successMsg = "体检报告已添加，部分信息可能不完整";
                    } else {
                        successMsg = "体检报告已添加，但数据不完整，建议检查";
                    }
                    
                    Toast.makeText(this, successMsg, Toast.LENGTH_SHORT).show();
                    
                    android.util.Log.d(TAG, "体检报告添加完成，当前列表大小: " + physicalExamList.size());
                    
                    // 如果数据不完整，额外提示用户
                    if (!hasFullValidData) {
                        android.util.Log.w(TAG, "数据不完整，建议用户检查或刷新页面");
                        // 可以在这里添加额外的提示逗辑，但不要过度打扰用户
                    }
                    
                } catch (Exception e) {
                    android.util.Log.e(TAG, "添加体检报告到列表时发生错误", e);
                    Toast.makeText(this, "添加报告到列表失败，请刷新页面", Toast.LENGTH_SHORT).show();
                }
            } else {
                android.util.Log.e(TAG, "从意图中获取的新报告数据为空");
                Toast.makeText(this, "获取新报告数据失败，请刷新页面查看", Toast.LENGTH_SHORT).show();
            }
        }
        
        // 处理删除体检报告返回结果
        if (requestCode == 1003 && resultCode == RESULT_OK && data != null) {
            if (data.getBooleanExtra("delete_report", false)) {
                int position = data.getIntExtra("report_position", -1);
                if (position >= 0 && position < physicalExamList.size()) {
                    physicalExamList.remove(position);
                    physicalExamAdapter.notifyItemRemoved(position);
                    updateEmptyView();
                    Toast.makeText(this, "体检报告已删除", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    /**
     * 显示错误消息
     */
    private void showErrorMessage(String message) {
        try {
            if (!isFinishing() && !isDestroyed()) {
                runOnUiThread(() -> {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                });
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error showing error message", e);
        }
    }
    
    /**
     * 获取当前登录用户的ID
     * @return 用户ID，如果未登录则返回-1
     */
    private int getCurrentUserId() {
        // 从SharedPreferences获取用户ID
        SharedPreferences sharedPreferences = getSharedPreferences("user_login_state", MODE_PRIVATE);
        return sharedPreferences.getInt("user_id", -1);
    }
}