package com.wenxing.runyitong.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.wenxing.runyitong.R;
import com.wenxing.runyitong.model.HealthRecord;
import com.wenxing.runyitong.api.ApiService;
import com.wenxing.runyitong.api.ApiClient;
import com.wenxing.runyitong.api.ApiResponse;
import retrofit2.Call;
import android.widget.ProgressBar;

/**
 * 编辑健康信息Activity
 * 用于录入和修改用户的个人健康信息
 */
public class EditHealthInfoActivity extends AppCompatActivity {
    
    private static final String TAG = "EditHealthInfoActivity";
    private static final String PREFS_NAME = "user_login_state";
    
    // UI组件
    private Toolbar toolbar;
    private EditText etName, etBirthdate, etHeight, etWeight;
    private RadioGroup rgGender;
    private EditText etBloodType, etAllergies, etChronicDiseases, etMedications;
    private EditText etFamilyHistory, etEmergencyContactName, etEmergencyContactPhone;
    private Button btnSave;
    
    // 数据相关
    private HealthRecord healthRecord;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_health_info);
        
        initViews();
        initData();
        setupClickListeners();
    }
    
    /**
     * 初始化视图组件
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        
        // 基本信息组件
        etName = findViewById(R.id.et_name);
        etBirthdate = findViewById(R.id.et_birthdate);
        etHeight = findViewById(R.id.et_height);
        etWeight = findViewById(R.id.et_weight);
        rgGender = findViewById(R.id.rg_gender);
        etBloodType = findViewById(R.id.et_blood_type);
        
        // 健康状况组件
        etAllergies = findViewById(R.id.et_allergies);
        etChronicDiseases = findViewById(R.id.et_chronic_diseases);
        etMedications = findViewById(R.id.et_medications);
        etFamilyHistory = findViewById(R.id.et_family_history);
        
        // 紧急联系人组件
        etEmergencyContactName = findViewById(R.id.et_emergency_contact_name);
        etEmergencyContactPhone = findViewById(R.id.et_emergency_contact_phone);
        
        // 保存按钮
        btnSave = findViewById(R.id.btn_save);
        
        // 设置工具栏
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("编辑健康信息");
        }
    }
    
    /**
     * 初始化数据
     */
    private void initData() {
        // 获取从上一个页面传递过来的健康档案数据
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("health_record")) {
            healthRecord = (HealthRecord) intent.getSerializableExtra("health_record");
            if (healthRecord != null) {
                // 填充表单数据
                fillFormData();
            }
        }
        
        // 如果没有健康档案数据，创建一个新的
        if (healthRecord == null) {
            healthRecord = new HealthRecord();
        }
    }
    
    /**
     * 填充表单数据
     */
    private void fillFormData() {
        if (healthRecord == null) {
            return;
        }
        
        // 填充基本信息
        etName.setText(healthRecord.getName());
        etBirthdate.setText(healthRecord.getBirthdate());
        if (healthRecord.getHeight() > 0) {
            etHeight.setText(String.valueOf(healthRecord.getHeight()));
        }
        if (healthRecord.getWeight() > 0) {
            etWeight.setText(String.valueOf(healthRecord.getWeight()));
        }
        
        // 设置性别
        String gender = healthRecord.getGender();
        if (gender != null) {
            if ("男".equals(gender)) {
                rgGender.check(R.id.rb_male);
            } else if ("女".equals(gender)) {
                rgGender.check(R.id.rb_female);
            }
        }
        
        // 填充血型
        etBloodType.setText(healthRecord.getBloodType());
        
        // 填充健康状况信息
        etAllergies.setText(healthRecord.getAllergies());
        etChronicDiseases.setText(healthRecord.getChronicDiseases());
        etMedications.setText(healthRecord.getMedications());
        etFamilyHistory.setText(healthRecord.getFamilyHistory());
        
        // 填充紧急联系人信息
        etEmergencyContactName.setText(healthRecord.getEmergencyContactName());
        etEmergencyContactPhone.setText(healthRecord.getEmergencyContactPhone());
    }
    
    /**
     * 设置点击事件监听器
     */
    private void setupClickListeners() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 保存健康信息
                saveHealthInfo();
            }
        });
    }
    
    /**
     * 保存健康信息
     */
    private void saveHealthInfo() {
        // 验证表单数据
        if (!validateForm()) {
            return;
        }

        // 保存表单数据到健康档案对象
        saveFormDataToHealthRecord();

        try {
            // 显示加载状态
            showLoading(true);

            // 获取当前登录用户的ID
            int userId = getCurrentUserId();
            if (userId <= 0) {
                showLoading(false);
                Toast.makeText(EditHealthInfoActivity.this, "用户未登录，请先登录", Toast.LENGTH_SHORT).show();
                return;
            }

            // 创建ApiService实例
            ApiService apiService = ApiClient.getApiService();

            // 调用API更新健康档案
            apiService.updateHealthRecord(userId, healthRecord).enqueue(new retrofit2.Callback<ApiResponse<HealthRecord>>() {
                @Override
                public void onResponse(Call<ApiResponse<HealthRecord>> call, retrofit2.Response<ApiResponse<HealthRecord>> response) {
                    try {
                        showLoading(false);
                        
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            // API调用成功，获取更新后的健康档案数据
                            HealthRecord updatedHealthRecord = response.body().getData();
                            
                            // 返回上一个页面，并传递更新后的健康档案数据
                            Intent intent = new Intent();
                            intent.putExtra("health_record", updatedHealthRecord);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            // API调用成功但返回了错误
                            String errorMsg = "保存健康信息失败: " + (response.body() != null ? response.body().getMessage() : "未知错误");
                            Toast.makeText(EditHealthInfoActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        android.util.Log.e(TAG, "Error processing update health record response", e);
                        showLoading(false);
                        Toast.makeText(EditHealthInfoActivity.this, "处理保存结果时出错", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<HealthRecord>> call, Throwable t) {
                    android.util.Log.e(TAG, "Failed to call updateHealthRecord API", t);
                    showLoading(false);
                    Toast.makeText(EditHealthInfoActivity.this, "网络请求失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error saving health info", e);
            showLoading(false);
            Toast.makeText(this, "保存健康信息时出错", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 显示/隐藏加载状态
     */
    private void showLoading(boolean show) {
        try {
            ProgressBar progressBar = findViewById(R.id.progress_bar);
            if (progressBar != null) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
            
            Button btnSave = findViewById(R.id.btn_save);
            if (btnSave != null) {
                btnSave.setEnabled(!show);
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error in showLoading", e);
        }
    }
    
    /**
     * 验证表单数据
     * @return 表单验证是否通过
     */
    private boolean validateForm() {
        // 验证姓名
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "请输入姓名", Toast.LENGTH_SHORT).show();
            etName.requestFocus();
            return false;
        }
        
        // 验证出生日期
        String birthdate = etBirthdate.getText().toString().trim();
        if (birthdate.isEmpty()) {
            Toast.makeText(this, "请输入出生日期", Toast.LENGTH_SHORT).show();
            etBirthdate.requestFocus();
            return false;
        }
        
        // 验证身高
        String heightStr = etHeight.getText().toString().trim();
        if (!heightStr.isEmpty()) {
            try {
                double height = Double.parseDouble(heightStr);
                if (height <= 0) {
                    Toast.makeText(this, "身高必须大于0", Toast.LENGTH_SHORT).show();
                    etHeight.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "身高必须是有效数字", Toast.LENGTH_SHORT).show();
                etHeight.requestFocus();
                return false;
            }
        }
        
        // 验证体重
        String weightStr = etWeight.getText().toString().trim();
        if (!weightStr.isEmpty()) {
            try {
                double weight = Double.parseDouble(weightStr);
                if (weight <= 0) {
                    Toast.makeText(this, "体重必须大于0", Toast.LENGTH_SHORT).show();
                    etWeight.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "体重必须是有效数字", Toast.LENGTH_SHORT).show();
                etWeight.requestFocus();
                return false;
            }
        }
        
        // 验证紧急联系人信息
        String emergencyContactName = etEmergencyContactName.getText().toString().trim();
        String emergencyContactPhone = etEmergencyContactPhone.getText().toString().trim();
        
        if (!emergencyContactName.isEmpty() && emergencyContactPhone.isEmpty()) {
            Toast.makeText(this, "请输入紧急联系人电话", Toast.LENGTH_SHORT).show();
            etEmergencyContactPhone.requestFocus();
            return false;
        }
        
        if (!emergencyContactPhone.isEmpty() && emergencyContactName.isEmpty()) {
            Toast.makeText(this, "请输入紧急联系人姓名", Toast.LENGTH_SHORT).show();
            etEmergencyContactName.requestFocus();
            return false;
        }
        
        // 验证电话号码格式
        if (!emergencyContactPhone.isEmpty() && !isValidPhoneNumber(emergencyContactPhone)) {
            Toast.makeText(this, "请输入有效的电话号码", Toast.LENGTH_SHORT).show();
            etEmergencyContactPhone.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查电话号码是否有效
     * @param phoneNumber 电话号码
     * @return 是否有效
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        // 简单的中国大陆手机号码验证规则
        String phoneRegex = "^1[3-9]\\d{9}$";
        return phoneNumber.matches(phoneRegex);
    }
    
    /**
     * 保存表单数据到健康档案对象
     */
    private void saveFormDataToHealthRecord() {
        if (healthRecord == null) {
            healthRecord = new HealthRecord();
        }
        
        // 保存基本信息
        healthRecord.setName(etName.getText().toString().trim());
        healthRecord.setBirthdate(etBirthdate.getText().toString().trim());
        
        // 保存身高
        String heightStr = etHeight.getText().toString().trim();
        if (!heightStr.isEmpty()) {
            healthRecord.setHeight(Double.parseDouble(heightStr));
        }
        
        // 保存体重
        String weightStr = etWeight.getText().toString().trim();
        if (!weightStr.isEmpty()) {
            healthRecord.setWeight(Double.parseDouble(weightStr));
        }
        
        // 保存性别
        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == R.id.rb_male) {
            healthRecord.setGender("男");
        } else if (selectedGenderId == R.id.rb_female) {
            healthRecord.setGender("女");
        }
        
        // 保存血型
        healthRecord.setBloodType(etBloodType.getText().toString().trim());
        
        // 保存健康状况信息
        healthRecord.setAllergies(etAllergies.getText().toString().trim());
        healthRecord.setChronicDiseases(etChronicDiseases.getText().toString().trim());
        healthRecord.setMedications(etMedications.getText().toString().trim());
        healthRecord.setFamilyHistory(etFamilyHistory.getText().toString().trim());
        
        // 保存紧急联系人信息
        healthRecord.setEmergencyContactName(etEmergencyContactName.getText().toString().trim());
        healthRecord.setEmergencyContactPhone(etEmergencyContactPhone.getText().toString().trim());
        
        // 更新时间戳
        java.util.Date now = new java.util.Date();
        if (healthRecord.getCreatedAt() == null) {
            healthRecord.setCreatedAt(now);
        }
        healthRecord.setUpdatedAt(now);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 获取当前登录用户的ID
     * @return 用户ID，如果未登录则返回-1
     */
    private int getCurrentUserId() {
        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getInt("user_id", -1);
    }
}