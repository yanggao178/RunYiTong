package com.wenxing.runyitong.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wenxing.runyitong.R;
import com.wenxing.runyitong.api.ApiClient;
import com.wenxing.runyitong.api.ApiService;
import com.wenxing.runyitong.api.ApiResponse;
import com.wenxing.runyitong.api.DepartmentListResponse;
import com.wenxing.runyitong.api.DoctorListResponse;
import com.wenxing.runyitong.api.HospitalListResponse;
import com.wenxing.runyitong.model.*;
import com.wenxing.runyitong.adapter.*;
import com.wenxing.runyitong.activity.LoginActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

public class RegistrationFragment extends Fragment {
    
    // UI控件
    private Button btnHospitalRegistration, btnDoctorRegistration;
    private LinearLayout contentArea, layoutHospitalList;
    private RecyclerView recyclerViewHospitals, recyclerViewDoctors, recyclerViewDepartments;
    private Spinner spinnerTimeSlots;
    private EditText editTextSymptoms, editTextPatientName, editTextPatientPhone, editTextPatientId;
    private ProgressBar progressBar;
    private TextView textViewSelectedInfo;
    
    // 数据相关
    private ApiService apiService;
    private List<Hospital> hospitalList = new ArrayList<>();
    private List<Doctor> doctorList = new ArrayList<>();
    private List<Department> departmentList = new ArrayList<>();
    private HospitalAdapter hospitalAdapter;
    private DoctorAdapter doctorAdapter;
    private DepartmentAdapter departmentAdapter;
    
    // 选择状态
    private boolean isHospitalMode = true; // true: 按医院挂号, false: 按医生挂号
    private Hospital selectedHospital;
    private Doctor selectedDoctor;
    private Department selectedDepartment;
    private String selectedTimeSlot;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registration, container, false);
        
        initViews(view);
        initApiService();
        setupClickListeners();
        
        // 设置初始按钮状态
        updateButtonSelection();
        
        // 默认显示按医院挂号
        showHospitalRegistration();
        
        return view;
    }
    
    private void initViews(View view) {
        btnHospitalRegistration = view.findViewById(R.id.btn_hospital_registration);
        btnDoctorRegistration = view.findViewById(R.id.btn_doctor_registration);
        contentArea = view.findViewById(R.id.content_area);
        layoutHospitalList = view.findViewById(R.id.layout_hospital_list);
        recyclerViewHospitals = view.findViewById(R.id.recycler_hospitals);
        recyclerViewDoctors = view.findViewById(R.id.recycler_doctors);
        recyclerViewDepartments = view.findViewById(R.id.recycler_departments);
        spinnerTimeSlots = view.findViewById(R.id.spinner_time_slots);
        editTextSymptoms = view.findViewById(R.id.edit_symptoms);
        editTextPatientName = view.findViewById(R.id.edit_patient_name);
        editTextPatientPhone = view.findViewById(R.id.edit_patient_phone);
        editTextPatientId = view.findViewById(R.id.edit_patient_id);
        progressBar = view.findViewById(R.id.progress_bar);
        textViewSelectedInfo = view.findViewById(R.id.text_selected_info);
        
        // 设置RecyclerView布局管理器
        recyclerViewHospitals.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewDoctors.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewDepartments.setLayoutManager(new LinearLayoutManager(getContext()));
    }
    
    private void initApiService() {
        apiService = ApiClient.getApiService();
    }
    
    private void setupClickListeners() {
        // 按医院挂号按钮点击
        btnHospitalRegistration.setOnClickListener(v -> {
            if (!isHospitalMode) {
                isHospitalMode = true;
                updateButtonSelection();
                showHospitalRegistration();
            }
        });
        
        // 按医生挂号按钮点击
        btnDoctorRegistration.setOnClickListener(v -> {
            if (isHospitalMode) {
                isHospitalMode = false;
                updateButtonSelection();
                showDoctorRegistration();
            }
        });
    }
    
    private void updateButtonSelection() {
        if (isHospitalMode) {
            btnHospitalRegistration.setSelected(true);
            btnDoctorRegistration.setSelected(false);
        } else {
            btnHospitalRegistration.setSelected(false);
            btnDoctorRegistration.setSelected(true);
        }
    }
    
    private void showHospitalRegistration() {
        // 显示医院列表，隐藏医生列表
        layoutHospitalList.setVisibility(View.VISIBLE);
        recyclerViewHospitals.setVisibility(View.VISIBLE);
        recyclerViewDoctors.setVisibility(View.GONE);
        textViewSelectedInfo.setText("请选择医院");
        
        // 隐藏医生列表容器 - 添加getView()空检查以避免空指针异常
        if (getView() != null) {
            View layoutDoctorList = getView().findViewById(R.id.layout_doctor_list);
            if (layoutDoctorList != null) {
                layoutDoctorList.setVisibility(View.GONE);
            }
        }
        
        // 加载医院列表数据
        loadHospitals();
    }
    
    private void showDoctorRegistration() {
        // 显示医生列表，隐藏医院列表容器和医院列表
        layoutHospitalList.setVisibility(View.GONE);
        recyclerViewHospitals.setVisibility(View.GONE);
        
        // 显示医生列表容器和医生RecyclerView
        View layoutDoctorList = getView().findViewById(R.id.layout_doctor_list);
        if (layoutDoctorList != null) {
            layoutDoctorList.setVisibility(View.VISIBLE);
        }
        recyclerViewDoctors.setVisibility(View.VISIBLE);
        
        loadDoctors();
    }
    
    private void loadHospitals() {
        // 检查Fragment是否已附加到Activity
        if (!isAdded()) {
            Log.d("RegistrationFragment", "Fragment未附加到Activity，取消loadHospitals请求");
            return;
        }
        
        // 检查access_token是否存在
        String accessToken = ApiClient.getInstance().getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            Log.e("RegistrationFragment", "access_token不存在，跳转到登录界面");
            requireActivity().runOnUiThread(() -> {
                showError("请先登录");
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
            });
            return;
        }
        
        // 记录access_token长度和请求信息
        Log.d("RegistrationFragment", "access_token长度: " + accessToken.length() + ", 请求医院列表");
        
        showLoading(true);
        
        if (apiService == null) {
            Log.e("RegistrationFragment", "apiService未初始化");
            showLoading(false);
            showError("网络服务初始化失败");
            return;
        }
        
        apiService.getHospitals().enqueue(new Callback<ApiResponse<HospitalListResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<HospitalListResponse>> call, Response<ApiResponse<HospitalListResponse>> response) {
                showLoading(false);
                
                // 记录响应状态码和头信息
                Log.d("RegistrationFragment", "获取医院列表响应状态码: " + response.code() + ", 响应头: " + response.headers().toString());
                
                if (response.code() == 401) {
                    Log.e("RegistrationFragment", "401认证失败，清除无效token并跳转到登录界面");
                    clearLoginState();
                    requireActivity().runOnUiThread(() -> {
                        showError("登录已过期，请重新登录");
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        startActivity(intent);
                    });
                    return;
                }
                
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    ApiResponse<HospitalListResponse> apiResponse = response.body();
                    if(apiResponse.isSuccess()){
                        hospitalList = apiResponse.getData().getHospitals();
                        setupHospitalAdapter();
                    } else {
                        android.util.Log.e("RegistrationFragment", "加载医院列表失败");
                        showError("加载医院列表失败");
                    }
                } else {
                    // 读取错误体内容
                    String errorBody = "";
                    if (response.errorBody() != null) {
                        try {
                            errorBody = response.errorBody().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    android.util.Log.e("RegistrationFragment", "加载医院列表失败，响应体: " + errorBody);
                    showError("加载医院列表失败");
                }
            }
                        
            @Override
            public void onFailure(Call<ApiResponse<HospitalListResponse>> call, Throwable t) {
                showLoading(false);
                android.util.Log.e("RegistrationFragment", "网络请求失败: " + t.getMessage(), t);
                showError("网络请求失败，请检查网络连接");
            }
        });
    }
    
    private void loadDoctors() {
        // 检查Fragment是否已附加到Activity
        if (!isAdded()) {
            Log.d("RegistrationFragment", "Fragment未附加到Activity，取消loadDoctors请求");
            return;
        }
        
        // 检查access_token是否存在
        String accessToken = ApiClient.getInstance().getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            Log.e("RegistrationFragment", "access_token不存在，跳转到登录界面");
            requireActivity().runOnUiThread(() -> {
                showError("请先登录");
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
            });
            return;
        }
        
        // 记录access_token长度和请求信息
        Log.d("RegistrationFragment", "access_token长度: " + accessToken.length() + ", 请求医生列表");
        
        showLoading(true);
        
        if (apiService == null) {
            Log.e("RegistrationFragment", "apiService未初始化");
            showLoading(false);
            showError("网络服务初始化失败");
            return;
        }
        
        apiService.getDoctors(null, null).enqueue(new Callback<ApiResponse<DoctorListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<DoctorListResponse>> call, Response<ApiResponse<DoctorListResponse>> response) {
                showLoading(false);
                
                // 记录响应状态码和头信息
                Log.d("RegistrationFragment", "获取医生列表响应状态码: " + response.code() + ", 响应头: " + response.headers().toString());
                
                if (response.code() == 401) {
                    Log.e("RegistrationFragment", "401认证失败，清除无效token并跳转到登录界面");
                    clearLoginState();
                    requireActivity().runOnUiThread(() -> {
                        showError("登录已过期，请重新登录");
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        startActivity(intent);
                    });
                    return;
                }
                
                android.util.Log.d("RegistrationFragment", "医生API响应码: " + response.code());
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    doctorList = response.body().getData().getDoctors();
                    android.util.Log.d("RegistrationFragment", "接收到医生数据，数量: " + (doctorList != null ? doctorList.size() : 0));
                    if (doctorList != null && !doctorList.isEmpty()) {
                        android.util.Log.d("RegistrationFragment", "第一个医生: " + doctorList.get(0).getName());
                    }
                    setupDoctorAdapter();
                } else {
                    // 读取错误体内容
                    String errorBody = "";
                    if (response.errorBody() != null) {
                        try {
                            errorBody = response.errorBody().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    android.util.Log.e("RegistrationFragment", "API响应失败: " + response.code() + ", 错误体: " + errorBody);
                    showError("加载医生列表失败");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<DoctorListResponse>> call, Throwable t) {
                showLoading(false);
                android.util.Log.e("RegistrationFragment", "网络错误: " + t.getMessage());
                showError("网络错误：" + t.getMessage());
            }
        });
    }
    
    private void loadDepartmentsByHospital(int hospitalId) {
        // 检查Fragment是否已附加到Activity
        if (!isAdded()) {
            Log.d("RegistrationFragment", "Fragment未附加到Activity，取消loadDepartmentsByHospital请求");
            return;
        }
        
        // 检查access_token是否存在
        String accessToken = ApiClient.getInstance().getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            Log.e("RegistrationFragment", "access_token不存在，跳转到登录界面");
            requireActivity().runOnUiThread(() -> {
                showError("请先登录");
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
            });
            return;
        }
        
        // 记录access_token长度和请求信息
        Log.d("RegistrationFragment", "access_token长度: " + accessToken.length() + ", 请求医院部门列表: hospital_id=" + hospitalId);
        
        showLoading(true);
        
        if (apiService == null) {
            Log.e("RegistrationFragment", "apiService未初始化");
            showLoading(false);
            showError("网络服务初始化失败");
            return;
        }
        
        apiService.getHospitalDepartments(hospitalId).enqueue(new Callback<ApiResponse<DepartmentListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<DepartmentListResponse>> call, Response<ApiResponse<DepartmentListResponse>> response) {
                showLoading(false);
                
                // 记录响应状态码和头信息
                Log.d("RegistrationFragment", "获取科室列表响应状态码: " + response.code() + ", 响应头: " + response.headers().toString());
                
                if (response.code() == 401) {
                    Log.e("RegistrationFragment", "401认证失败，清除无效token并跳转到登录界面");
                    clearLoginState();
                    requireActivity().runOnUiThread(() -> {
                        showError("登录已过期，请重新登录");
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        startActivity(intent);
                    });
                    return;
                }
                
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    departmentList = response.body().getData().getDepartments();
                    setupDepartmentAdapter();
                    recyclerViewDepartments.setVisibility(View.VISIBLE);
                } else {
                    // 读取错误体内容
                    String errorBody = "";
                    if (response.errorBody() != null) {
                        try {
                            errorBody = response.errorBody().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.e("RegistrationFragment", "加载科室列表失败，错误体: " + errorBody);
                    showError("加载科室列表失败");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<DepartmentListResponse>> call, Throwable t) {
                showLoading(false);
                showError("网络错误：" + t.getMessage());
            }
        });
    }
    
    private void setupHospitalAdapter() {

        android.util.Log.d("RegistrationFragment", "设置医院适配器，医院列表大小: " + (hospitalList != null ? hospitalList.size() : 0));
        // 使用RegistrationFragment.this作为上下文，避免getContext()可能返回null的问题
        hospitalAdapter = new HospitalAdapter(getActivity(), hospitalList, hospital -> {
            if (hospital == null) {
                android.util.Log.e("RegistrationFragment", "医院对象为空，无法跳转");
                return;
            }
            selectedHospital = hospital;
            updateSelectedInfo();
            // 跳转到HospitalDepartmentListActivity
            Activity activity = getActivity();
            if (activity == null) {
                android.util.Log.e("RegistrationFragment", "Activity上下文为空，无法启动新的Activity");
                return;
            }
           // loadDepartmentsByHospital(hospital.getId());
            Intent intent = new Intent(activity, com.wenxing.runyitong.activity.HospitalDepartmentListActivity.class);
            intent.putExtra(com.wenxing.runyitong.activity.HospitalDepartmentListActivity.EXTRA_HOSPITAL_ID, hospital.getId());
            // 处理可能为null的医院名称
            String hospitalName = hospital.getName() != null ? hospital.getName() : "未知医院";
            intent.putExtra(com.wenxing.runyitong.activity.HospitalDepartmentListActivity.EXTRA_HOSPITAL_NAME, hospitalName);
            // 安全地拼接地址和电话，处理可能为null的情况
            String address = hospital.getAddress() != null ? hospital.getAddress() : "";
            String phone = hospital.getPhone() != null ? hospital.getPhone() : "";
            intent.putExtra(com.wenxing.runyitong.activity.HospitalDepartmentListActivity.EXTRA_HOSPITAL_INFO, address + " " + phone);
            startActivity(intent);
        });
        recyclerViewHospitals.setAdapter(hospitalAdapter);
        android.util.Log.d("RegistrationFragment", "医院适配器设置完成");
    }
    
    private void setupDoctorAdapter() {
        android.util.Log.d("RegistrationFragment", "设置医生适配器，医生列表大小: " + (doctorList != null ? doctorList.size() : 0));
        if (doctorList != null && !doctorList.isEmpty()) {
            android.util.Log.d("RegistrationFragment", "第一个医生: " + doctorList.get(0).getName());
        }
        doctorAdapter = new DoctorAdapter(doctorList, doctor -> {
            selectedDoctor = doctor;
            updateSelectedInfo();
            setupTimeSlots(doctor.getAvailableTimes());
        });
        recyclerViewDoctors.setAdapter(doctorAdapter);
        android.util.Log.d("RegistrationFragment", "医生适配器设置完成");
    }
    
    private void setupDepartmentAdapter() {
        departmentAdapter = new DepartmentAdapter(departmentList, department -> {
            selectedDepartment = department;
            updateSelectedInfo();
            // 根据选择的医院和科室加载医生
            loadDoctorsByHospitalAndDepartment(selectedHospital.getId(), department.getId());
        });
        recyclerViewDepartments.setAdapter(departmentAdapter);
    }
    
    private void loadDoctorsByHospitalAndDepartment(int hospitalId, int departmentId) {
        // 检查Fragment是否已附加到Activity
        if (!isAdded()) {
            Log.d("RegistrationFragment", "Fragment未附加到Activity，取消loadDoctorsByHospitalAndDepartment请求");
            return;
        }
        
        // 检查access_token是否存在
        String accessToken = ApiClient.getInstance().getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            Log.e("RegistrationFragment", "access_token不存在，跳转到登录界面");
            requireActivity().runOnUiThread(() -> {
                showError("请先登录");
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
            });
            return;
        }
        
        // 记录access_token长度和请求信息
        Log.d("RegistrationFragment", "access_token长度: " + accessToken.length() + ", 请求医生列表: department_id=" + departmentId + ", hospital_id=" + hospitalId);
        
        showLoading(true);
        
        if (apiService == null) {
            Log.e("RegistrationFragment", "apiService未初始化");
            showLoading(false);
            showError("网络服务初始化失败");
            return;
        }
        
        apiService.getDoctors(departmentId, hospitalId).enqueue(new Callback<ApiResponse<DoctorListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<DoctorListResponse>> call, Response<ApiResponse<DoctorListResponse>> response) {
                showLoading(false);
                
                // 记录响应状态码和头信息
                Log.d("RegistrationFragment", "获取特定科室医生列表响应状态码: " + response.code() + ", 响应头: " + response.headers().toString());
                
                if (response.code() == 401) {
                    Log.e("RegistrationFragment", "401认证失败，清除无效token并跳转到登录界面");
                    clearLoginState();
                    requireActivity().runOnUiThread(() -> {
                        showError("登录已过期，请重新登录");
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        startActivity(intent);
                    });
                    return;
                }
                
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    doctorList = response.body().getData().getDoctors();
                    setupDoctorAdapter();
                    recyclerViewDoctors.setVisibility(View.VISIBLE);
                } else {
                    // 读取错误体内容
                    String errorBody = "";
                    if (response.errorBody() != null) {
                        try {
                            errorBody = response.errorBody().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.e("RegistrationFragment", "加载医生列表失败，错误体: " + errorBody);
                    showError("加载医生列表失败");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<DoctorListResponse>> call, Throwable t) {
                showLoading(false);
                showError("网络错误：" + t.getMessage());
            }
        });
    }
    
    private void setupTimeSlots(List<String> availableTimes) {
        if (availableTimes != null && !availableTimes.isEmpty()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
                android.R.layout.simple_spinner_item, availableTimes);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTimeSlots.setAdapter(adapter);
            spinnerTimeSlots.setVisibility(View.VISIBLE);
            
            spinnerTimeSlots.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedTimeSlot = availableTimes.get(position);
                }
                
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }
    
    private void updateSelectedInfo() {
        StringBuilder info = new StringBuilder("已选择：");
        
        if (isHospitalMode) {
            if (selectedHospital != null) {
                info.append(selectedHospital.getName());
            }
            if (selectedDepartment != null) {
                info.append(" - ").append(selectedDepartment.getName());
            }
            if (selectedDoctor != null) {
                info.append(" - ").append(selectedDoctor.getName());
            }
        } else {
            if (selectedDoctor != null) {
                info.append(selectedDoctor.getName())
                    .append(" (").append(selectedDoctor.getHospitalName())
                    .append(" - ").append(selectedDoctor.getDepartmentName()).append(")");
            }
        }
        
        textViewSelectedInfo.setText(info.toString());
    }
    

    
    private void clearForm() {
        editTextPatientName.setText("");
        editTextPatientPhone.setText("");
        editTextPatientId.setText("");
        editTextSymptoms.setText("");
        selectedHospital = null;
        selectedDoctor = null;
        selectedDepartment = null;
        selectedTimeSlot = null;
        textViewSelectedInfo.setText("请选择挂号方式");
        spinnerTimeSlots.setVisibility(View.GONE);
        recyclerViewDepartments.setVisibility(View.GONE);
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
    
    private void showSuccess(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
    
    // 清除登录状态
    private void clearLoginState() {
        try {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_login_state", android.content.Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("access_token");
            editor.remove("user_id");
            editor.apply();
            Log.d("RegistrationFragment", "已清除登录状态");
        } catch (Exception e) {
            Log.e("RegistrationFragment", "清除登录状态失败: " + e.getMessage());
        }
    }
    
    // 获取当前登录用户的ID
    private int getCurrentUserId() {
        try {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_login_state", android.content.Context.MODE_PRIVATE);
            return sharedPreferences.getInt("user_id", -1);
        } catch (Exception e) {
            Log.e("RegistrationFragment", "获取用户ID失败: " + e.getMessage());
            return -1;
        }
    }
}