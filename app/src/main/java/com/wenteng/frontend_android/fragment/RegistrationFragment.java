package com.wenteng.frontend_android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.cardview.widget.CardView;
import com.wenteng.frontend_android.R;
import com.wenteng.frontend_android.api.ApiClient;
import com.wenteng.frontend_android.api.ApiService;
import com.wenteng.frontend_android.api.ApiResponse;
import com.wenteng.frontend_android.api.DepartmentListResponse;
import com.wenteng.frontend_android.api.DoctorListResponse;
import com.wenteng.frontend_android.api.HospitalListResponse;
import com.wenteng.frontend_android.model.*;
import com.wenteng.frontend_android.adapter.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;
import java.util.ArrayList;

public class RegistrationFragment extends Fragment {
    
    // UI控件
    private Button btnHospitalRegistration, btnDoctorRegistration;
    private LinearLayout contentArea, layoutHospitalList;
    private RecyclerView recyclerViewHospitals, recyclerViewDoctors, recyclerViewDepartments;
    private Spinner spinnerTimeSlots;
    private EditText editTextSymptoms, editTextPatientName, editTextPatientPhone, editTextPatientId;
    private Button btnConfirmAppointment;
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
        btnConfirmAppointment = view.findViewById(R.id.btn_confirm_appointment);
        progressBar = view.findViewById(R.id.progress_bar);
        textViewSelectedInfo = view.findViewById(R.id.text_selected_info);
        
        // 设置RecyclerView布局管理器
        recyclerViewHospitals.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewDoctors.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewDepartments.setLayoutManager(new LinearLayoutManager(getContext()));
    }
    
    private void initApiService() {
        apiService = ApiClient.getInstance().getApiService();
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
        
        // 确认预约按钮点击
        btnConfirmAppointment.setOnClickListener(v -> confirmAppointment());
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
        // 显示医院列表容器和医院列表，隐藏医生列表
        layoutHospitalList.setVisibility(View.VISIBLE);
        recyclerViewHospitals.setVisibility(View.VISIBLE);
        recyclerViewDoctors.setVisibility(View.GONE);
        recyclerViewDepartments.setVisibility(View.GONE);
        
        android.util.Log.d("RegistrationFragment", "显示医院挂号界面，RecyclerView可见性: " + recyclerViewHospitals.getVisibility());
        
        updateButtonSelection();
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
        showLoading(true);
        
        apiService.getHospitals().enqueue(new Callback<ApiResponse<HospitalListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<HospitalListResponse>> call, Response<ApiResponse<HospitalListResponse>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    hospitalList = response.body().getData().getHospitals();
                    android.util.Log.d("RegistrationFragment", "接收到医院数据，数量: " + (hospitalList != null ? hospitalList.size() : 0));
                    if (hospitalList != null && !hospitalList.isEmpty()) {
                        android.util.Log.d("RegistrationFragment", "第一个医院: " + hospitalList.get(0).getName());
                    }
                    setupHospitalAdapter();
                } else {
                    android.util.Log.e("RegistrationFragment", "API响应失败: " + response.code());
                    showError("加载医院列表失败");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<HospitalListResponse>> call, Throwable t) {
                showLoading(false);
                showError("网络错误：" + t.getMessage());
            }
        });
    }
    
    private void loadDoctors() {
        showLoading(true);
        
        apiService.getDoctors(null, null).enqueue(new Callback<ApiResponse<DoctorListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<DoctorListResponse>> call, Response<ApiResponse<DoctorListResponse>> response) {
                showLoading(false);
                android.util.Log.d("RegistrationFragment", "医生API响应码: " + response.code());
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    doctorList = response.body().getData().getDoctors();
                    android.util.Log.d("RegistrationFragment", "接收到医生数据，数量: " + (doctorList != null ? doctorList.size() : 0));
                    if (doctorList != null && !doctorList.isEmpty()) {
                        android.util.Log.d("RegistrationFragment", "第一个医生: " + doctorList.get(0).getName());
                    }
                    setupDoctorAdapter();
                } else {
                    android.util.Log.e("RegistrationFragment", "API响应失败: " + response.code());
                    if (response.body() != null) {
                        android.util.Log.e("RegistrationFragment", "响应体: " + response.body().toString());
                    }
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
        showLoading(true);
        
        apiService.getHospitalDepartments(hospitalId).enqueue(new Callback<ApiResponse<DepartmentListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<DepartmentListResponse>> call, Response<ApiResponse<DepartmentListResponse>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    departmentList = response.body().getData().getDepartments();
                    setupDepartmentAdapter();
                    recyclerViewDepartments.setVisibility(View.VISIBLE);
                } else {
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
        hospitalAdapter = new HospitalAdapter(getContext(), hospitalList, hospital -> {
            selectedHospital = hospital;
            updateSelectedInfo();
            loadDepartmentsByHospital(hospital.getId());
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
        showLoading(true);
        
        apiService.getDoctors(departmentId, hospitalId).enqueue(new Callback<ApiResponse<DoctorListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<DoctorListResponse>> call, Response<ApiResponse<DoctorListResponse>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    doctorList = response.body().getData().getDoctors();
                    setupDoctorAdapter();
                    recyclerViewDoctors.setVisibility(View.VISIBLE);
                } else {
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
    
    private void confirmAppointment() {
        // 验证输入
        String patientName = editTextPatientName.getText().toString().trim();
        String patientPhone = editTextPatientPhone.getText().toString().trim();
        String patientId = editTextPatientId.getText().toString().trim();
        String symptoms = editTextSymptoms.getText().toString().trim();
        
        if (patientName.isEmpty() || patientPhone.isEmpty() || patientId.isEmpty()) {
            showError("请填写完整的患者信息");
            return;
        }
        
        if (selectedDoctor == null) {
            showError("请选择医生");
            return;
        }
        
        if (selectedTimeSlot == null || selectedTimeSlot.isEmpty()) {
            showError("请选择预约时间");
            return;
        }
        
        // 创建预约对象
        Appointment appointment = new Appointment();
        appointment.setPatientName(patientName);
        appointment.setPatientPhone(patientPhone);
        appointment.setPatientIdCard(patientId);
        appointment.setDoctorId(selectedDoctor.getId());
        appointment.setDoctorName(selectedDoctor.getName());
        appointment.setHospitalId(selectedDoctor.getHospitalId());
        appointment.setHospitalName(selectedDoctor.getHospitalName());
        appointment.setDepartmentId(selectedDoctor.getDepartmentId());
        appointment.setDepartmentName(selectedDoctor.getDepartmentName());
        appointment.setAppointmentTime(selectedTimeSlot);
        appointment.setSymptoms(symptoms);
        appointment.setStatus("待确认");
        
        // 显示预约成功信息
        showSuccess("预约成功！\n" +
                   "医生：" + selectedDoctor.getName() + "\n" +
                   "医院：" + selectedDoctor.getHospitalName() + "\n" +
                   "科室：" + selectedDoctor.getDepartmentName() + "\n" +
                   "时间：" + selectedTimeSlot);
        
        // 清空表单
        clearForm();
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
}