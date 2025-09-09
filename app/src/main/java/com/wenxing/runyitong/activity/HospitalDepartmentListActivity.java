package com.wenxing.runyitong.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.content.SharedPreferences;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wenxing.runyitong.R;
import com.wenxing.runyitong.adapter.DepartmentAdapter;
import com.wenxing.runyitong.adapter.DoctorAdapter;
import com.wenxing.runyitong.api.ApiClient;
import com.wenxing.runyitong.api.ApiResponse;
import com.wenxing.runyitong.api.ApiService;
import com.wenxing.runyitong.api.DepartmentListResponse;
import com.wenxing.runyitong.api.DoctorListResponse;
import com.wenxing.runyitong.model.Department;
import com.wenxing.runyitong.model.Doctor;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 医院科室列表Activity
 * 展示某个医院的科室信息，点击科室后显示对应医生列表
 */
public class HospitalDepartmentListActivity extends AppCompatActivity {

    private static final String TAG = "HospitalDeptListActivity";
    public static final String EXTRA_HOSPITAL_ID = "extra_hospital_id";
    public static final String EXTRA_HOSPITAL_NAME = "extra_hospital_name";
    public static final String EXTRA_HOSPITAL_INFO = "extra_hospital_info";

    // UI组件
    private Toolbar toolbar;
    private TextView tvTitle;
    private ImageView ivBack;
    private TextView tvHospitalName;
    private TextView tvHospitalInfo;
    private TextView tvDepartmentsTitle;
    private TextView tvDoctorsTitle;
    private RecyclerView recyclerDepartments;
    private RecyclerView recyclerDoctors;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyMessage;
    private Button btnRefresh;
    private LinearLayout layoutDoctorsTitle;

    // 数据
    private int hospitalId;
    private String hospitalName;
    private String hospitalInfo;
    private List<Department> departmentList = new ArrayList<>();
    private List<Doctor> doctorList = new ArrayList<>();
    private Department selectedDepartment = null;

    // 适配器
    private DepartmentAdapter departmentAdapter;
    private DoctorAdapter doctorAdapter;

    // API服务
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_department_list);

        // 初始化API服务
        initApiService();

        // 初始化UI组件
        initViews();

        // 初始化数据
        initData();

        // 设置适配器
        setupAdapters();

        // 设置监听器
        setupListeners();

        // 加载科室列表
        loadDepartments();
    }

    private void initApiService() {
        // 初始化ApiClient（如果尚未初始化）
        if (ApiClient.getAppContext() == null) {
            ApiClient.initialize(getApplicationContext());
        }
        apiService = ApiClient.getApiService();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTitle = findViewById(R.id.tv_title);
        ivBack = findViewById(R.id.iv_back);
        tvHospitalName = findViewById(R.id.tv_hospital_name);
        tvHospitalInfo = findViewById(R.id.tv_hospital_info);
        tvDepartmentsTitle = findViewById(R.id.tv_departments_title);
        tvDoctorsTitle = findViewById(R.id.tv_doctors_title);
        recyclerDepartments = findViewById(R.id.recycler_departments);
        recyclerDoctors = findViewById(R.id.recycler_doctors);
        progressBar = findViewById(R.id.progress_bar);
        layoutEmpty = findViewById(R.id.layout_empty);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);
        btnRefresh = findViewById(R.id.btn_refresh);
        layoutDoctorsTitle = findViewById(R.id.layout_doctors_title);

        // 设置RecyclerView布局管理器
        recyclerDepartments.setLayoutManager(new LinearLayoutManager(this));
        recyclerDoctors.setLayoutManager(new LinearLayoutManager(this));

        // 设置标题栏
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void initData() {
        // 获取从其他页面传递过来的医院信息
        Intent intent = getIntent();
        if (intent != null) {
            hospitalId = intent.getIntExtra(EXTRA_HOSPITAL_ID, -1);
            hospitalName = intent.getStringExtra(EXTRA_HOSPITAL_NAME);
            hospitalInfo = intent.getStringExtra(EXTRA_HOSPITAL_INFO);
        }

        // 检查医院ID是否有效
        if (hospitalId <= 0) {
            Log.e(TAG, "无效的医院ID: " + hospitalId);
            Toast.makeText(this, "无法加载医院信息", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 设置医院信息
        tvTitle.setText("医院科室");
        tvHospitalName.setText(hospitalName != null ? hospitalName : "未知医院");
        tvHospitalInfo.setText(hospitalInfo != null ? hospitalInfo : "加载中...");
    }

    private void setupAdapters() {
        // 创建科室适配器
        departmentAdapter = new DepartmentAdapter(departmentList, department -> {
            // 处理科室点击事件
            selectedDepartment = department;
            loadDoctors(hospitalId, department.getId());
        });
        recyclerDepartments.setAdapter(departmentAdapter);

        // 创建医生适配器
        doctorAdapter = new DoctorAdapter(doctorList, doctor -> {
            // 处理医生点击事件，可以打开医生详情页面或预约页面
            Toast.makeText(HospitalDepartmentListActivity.this, 
                    "选择医生：" + doctor.getName(), Toast.LENGTH_SHORT).show();
            // 这里可以根据需要跳转到医生详情页面或预约页面
        });
        recyclerDoctors.setAdapter(doctorAdapter);
    }

    private void setupListeners() {
        // 返回按钮点击事件
        ivBack.setOnClickListener(v -> finish());

        // 刷新按钮点击事件
        btnRefresh.setOnClickListener(v -> {
            if (selectedDepartment == null) {
                // 如果没有选中科室，则刷新科室列表
                loadDepartments();
            } else {
                // 如果已选中科室，则刷新医生列表
                loadDoctors(hospitalId, selectedDepartment.getId());
            }
        });
    }

    private void loadDepartments() {
        // 检查Fragment是否已附加到Activity
        if (isFinishing() || isDestroyed()) {
            Log.d(TAG, "Activity已销毁，取消loadDepartments请求");
            return;
        }

        // 检查access_token是否存在
        String accessToken = ApiClient.getInstance().getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            Log.e(TAG, "access_token不存在，跳转到登录界面");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        // 记录请求信息
        Log.d(TAG, "请求医院部门列表: hospital_id=" + hospitalId);

        showLoading(true);

        if (apiService == null) {
            Log.e(TAG, "apiService未初始化");
            showLoading(false);
            showError("网络服务初始化失败");
            return;
        }

        apiService.getHospitalDepartments(hospitalId).enqueue(new Callback<ApiResponse<DepartmentListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<DepartmentListResponse>> call, Response<ApiResponse<DepartmentListResponse>> response) {
                showLoading(false);
                
                // 记录响应状态码
                Log.d(TAG, "获取科室列表响应状态码: " + response.code());

                if (response.code() == 401) {
                    Log.e(TAG, "401认证失败，清除无效token并跳转到登录界面");
                    clearLoginState();
                    Intent intent = new Intent(HospitalDepartmentListActivity.this, LoginActivity.class);
                    startActivity(intent);
                    return;
                }

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    departmentList.clear();
                    departmentList.addAll(response.body().getData().getDepartments());
                    departmentAdapter.notifyDataSetChanged();
                    
                    // 显示或隐藏空状态
                    if (departmentList.isEmpty()) {
                        showEmpty("暂无科室信息");
                    } else {
                        showContent();
                    }
                } else {
                    // 读取错误体内容
                    String errorBody = "";
                    if (response.errorBody() != null) {
                        try {
                            errorBody = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Log.e(TAG, "加载科室列表失败，错误体: " + errorBody);
                    showError("加载科室列表失败");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DepartmentListResponse>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "网络错误：" + t.getMessage());
                showError("网络错误：" + t.getMessage());
            }
        });
    }

    private void loadDoctors(int hospitalId, int departmentId) {
        // 检查Activity状态
        if (isFinishing() || isDestroyed()) {
            Log.d(TAG, "Activity已销毁，取消loadDoctors请求");
            return;
        }

        // 检查access_token是否存在
        String accessToken = ApiClient.getInstance().getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            Log.e(TAG, "access_token不存在，跳转到登录界面");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        // 记录请求信息
        Log.d(TAG, "请求医生列表: department_id=" + departmentId + ", hospital_id=" + hospitalId);

        showLoading(true);

        if (apiService == null) {
            Log.e(TAG, "apiService未初始化");
            showLoading(false);
            showError("网络服务初始化失败");
            return;
        }

        apiService.getDoctors(departmentId, hospitalId).enqueue(new Callback<ApiResponse<DoctorListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<DoctorListResponse>> call, Response<ApiResponse<DoctorListResponse>> response) {
                showLoading(false);
                
                // 记录响应状态码
                Log.d(TAG, "获取医生列表响应状态码: " + response.code());

                if (response.code() == 401) {
                    Log.e(TAG, "401认证失败，清除无效token并跳转到登录界面");
                    clearLoginState();
                    Intent intent = new Intent(HospitalDepartmentListActivity.this, LoginActivity.class);
                    startActivity(intent);
                    return;
                }

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    doctorList.clear();
                    doctorList.addAll(response.body().getData().getDoctors());
                    doctorAdapter.notifyDataSetChanged();
                    
                    // 显示医生列表，隐藏科室列表
                    recyclerDepartments.setVisibility(View.GONE);
                    tvDepartmentsTitle.setVisibility(View.GONE);
                    recyclerDoctors.setVisibility(View.VISIBLE);
                    layoutDoctorsTitle.setVisibility(View.VISIBLE);
                    
                    // 更新标题
                    if (selectedDepartment != null) {
                        tvTitle.setText(selectedDepartment.getName() + "医生");
                    }
                    
                    // 显示或隐藏空状态
                    if (doctorList.isEmpty()) {
                        showEmpty("该科室暂无医生信息");
                    } else {
                        showContent();
                    }
                } else {
                    // 读取错误体内容
                    String errorBody = "";
                    if (response.errorBody() != null) {
                        try {
                            errorBody = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Log.e(TAG, "加载医生列表失败，错误体: " + errorBody);
                    showError("加载医生列表失败");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DoctorListResponse>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "网络错误：" + t.getMessage());
                showError("网络错误：" + t.getMessage());
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerDepartments.setVisibility(show ? View.GONE : recyclerDepartments.getVisibility());
        recyclerDoctors.setVisibility(show ? View.GONE : recyclerDoctors.getVisibility());
        layoutEmpty.setVisibility(show ? View.GONE : View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        // 如果没有数据，则显示空状态
        if (departmentList.isEmpty() && doctorList.isEmpty()) {
            showEmpty(message);
        }
    }

    private void showEmpty(String message) {
        layoutEmpty.setVisibility(View.VISIBLE);
        tvEmptyMessage.setText(message);
        recyclerDepartments.setVisibility(View.GONE);
        recyclerDoctors.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private void showContent() {
        layoutEmpty.setVisibility(View.GONE);
        recyclerDepartments.setVisibility(selectedDepartment == null ? View.VISIBLE : View.GONE);
        recyclerDoctors.setVisibility(selectedDepartment == null ? View.GONE : View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    // 清除登录状态
    private void clearLoginState() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("user_login_state", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("access_token");
            editor.remove("user_id");
            editor.apply();
            Log.d(TAG, "已清除登录状态");
        } catch (Exception e) {
            Log.e(TAG, "清除登录状态失败: " + e.getMessage());
        }
    }
}