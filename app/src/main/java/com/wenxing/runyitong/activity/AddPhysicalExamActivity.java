package com.wenxing.runyitong.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.wenxing.runyitong.R;
import com.wenxing.runyitong.api.ApiClient;
import com.wenxing.runyitong.api.ApiResponse;
import com.wenxing.runyitong.api.ApiService;
import com.wenxing.runyitong.model.PhysicalExamReport;

import retrofit2.Call;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddPhysicalExamActivity extends AppCompatActivity {

    private static final String TAG = "AddPhysicalExamActivity";
    public static final int RESULT_SUCCESS = 1;
    
    private EditText etReportName, etExamDate, etHospitalName, etSummary, etDoctorOpinion;
    private EditText etKeyFindings, etNormalItems, etAbnormalItems, etSuggestions;
    private Button btnSave;
    private Calendar calendar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_physical_exam);
        
        // 初始化Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("添加体检报告");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // 初始化UI组件
        initViews();
        
        // 初始化日历
        calendar = Calendar.getInstance();
        updateExamDateText();
        
        // 设置日期选择器
        etExamDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        
        // 设置保存按钮点击事件
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePhysicalExamReport();
            }
        });
    }
    
    private void initViews() {
        etReportName = findViewById(R.id.et_report_name);
        etExamDate = findViewById(R.id.et_exam_date);
        etHospitalName = findViewById(R.id.et_hospital_name);
        etSummary = findViewById(R.id.et_summary);
        etDoctorOpinion = findViewById(R.id.et_doctor_opinion);
        etKeyFindings = findViewById(R.id.et_key_findings);
        etNormalItems = findViewById(R.id.et_normal_items);
        etAbnormalItems = findViewById(R.id.et_abnormal_items);
        etSuggestions = findViewById(R.id.et_suggestions);
        btnSave = findViewById(R.id.btn_save);
    }
    
    private void updateExamDateText() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etExamDate.setText(sdf.format(calendar.getTime()));
    }
    
    private void showDatePickerDialog() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, 
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(year, monthOfYear, dayOfMonth);
                        updateExamDateText();
                    }
                }, year, month, day);
        
        datePickerDialog.show();
    }
    
    private void savePhysicalExamReport() {
        // 检查Activity是否已销毁
        if (isFinishing() || isDestroyed()) {
            Log.d(TAG, "Activity已销毁，取消savePhysicalExamReport请求");
            return;
        }
        
        // 获取表单数据
        String reportName = etReportName.getText().toString().trim();
        String examDate = etExamDate.getText().toString().trim();
        String hospitalName = etHospitalName.getText().toString().trim();
        String summary = etSummary.getText().toString().trim();
        String doctorOpinion = etDoctorOpinion.getText().toString().trim();
        String keyFindings = etKeyFindings.getText().toString().trim();
        String normalItems = etNormalItems.getText().toString().trim();
        String abnormalItems = etAbnormalItems.getText().toString().trim();
        String suggestions = etSuggestions.getText().toString().trim();
        
        // 验证表单
        if (TextUtils.isEmpty(reportName)) {
            Toast.makeText(this, "请输入报告名称", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (TextUtils.isEmpty(examDate)) {
            Toast.makeText(this, "请选择检查日期", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (TextUtils.isEmpty(hospitalName)) {
            Toast.makeText(this, "请输入医院名称", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 创建新的体检报告对象
        PhysicalExamReport newReport = new PhysicalExamReport();
        // 注意：ID由服务器生成，这里不设置
        newReport.setHealthRecordId(getCurrentUserId()); // 使用真实的用户健康档案ID
        newReport.setReportName(reportName);
        newReport.setExamDate(examDate);
        newReport.setHospitalName(hospitalName);
        newReport.setSummary(summary);
        newReport.setDoctorComments(doctorOpinion);
        
        // 由于PhysicalExamReport中的这些字段是Map类型，我们需要将String转换为Map
        // 这里简单处理，实际应用中可能需要更复杂的解析逻辑
        Map<String, String> keyFindingsMap = new HashMap<>();
        if (!TextUtils.isEmpty(keyFindings)) {
            keyFindingsMap.put("findings", keyFindings);
        }
        newReport.setKeyFindings(keyFindingsMap);
        
        Map<String, String> normalItemsMap = new HashMap<>();
        if (!TextUtils.isEmpty(normalItems)) {
            normalItemsMap.put("items", normalItems);
        }
        newReport.setNormalItems(normalItemsMap);
        
        Map<String, String> abnormalItemsMap = new HashMap<>();
        if (!TextUtils.isEmpty(abnormalItems)) {
            abnormalItemsMap.put("items", abnormalItems);
        }
        newReport.setAbnormalItems(abnormalItemsMap);
        
        newReport.setRecommendations(suggestions);
        newReport.setReportUrl(""); // 暂时为空
        newReport.setCreatedAt(new Date());
        newReport.setUpdatedAt(new Date());
        
        try {
            // 检查access_token是否存在
            String accessToken = ApiClient.getInstance().getAccessToken();
            if (accessToken == null || accessToken.isEmpty()) {
                Log.e(TAG, "access_token不存在，跳转到登录界面");
                runOnUiThread(() -> {
                    Toast.makeText(AddPhysicalExamActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddPhysicalExamActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                });
                return;
            }
            
            // 记录access_token长度
            Log.d(TAG, "access_token长度: " + accessToken.length());
            
            // 获取当前用户ID
            int userId = getCurrentUserId();
            if (userId == -1) {
                Log.e(TAG, "无法获取用户ID，跳转到登录界面");
                runOnUiThread(() -> {
                    Toast.makeText(AddPhysicalExamActivity.this, "用户信息异常，请重新登录", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddPhysicalExamActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                });
                return;
            }
            
            // 记录请求信息
            Log.d(TAG, "请求添加体检报告: user_id=" + userId);
            
            // 显示加载状态
            showLoading(true);
            
            // 创建ApiService实例
            ApiService apiService = ApiClient.getApiService();
            
            if (apiService == null) {
                Log.e(TAG, "apiService未初始化");
                showLoading(false);
                Toast.makeText(this, "网络服务初始化失败", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 调用API添加体检报告
            apiService.addPhysicalExamReport(userId, newReport).enqueue(new retrofit2.Callback<ApiResponse<PhysicalExamReport>>() {
                @Override
                public void onResponse(Call<ApiResponse<PhysicalExamReport>> call, retrofit2.Response<ApiResponse<PhysicalExamReport>> response) {
                    try {
                        showLoading(false);
                        
                        // 记录响应状态码和头信息
                        Log.d(TAG, "添加体检报告响应状态码: " + response.code() + ", 响应头: " + response.headers().toString());
                        
                        if (response.code() == 422) {
                            Log.e(TAG, "422数据验证错误，检查请求参数格式");
                            String errorBody = "";
                            if (response.errorBody() != null) {
                                try {
                                    errorBody = response.errorBody().string();
                                    Log.e(TAG, "422错误详情: " + errorBody);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            final String finalErrorBody = errorBody;
                            runOnUiThread(() -> {
                                showDetailedError("数据验证失败", "请检查输入的数据格式是否正确。\n错误详情：" + finalErrorBody);
                            });
                            return;
                        }
                        
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            // API调用成功，获取添加后的体检报告
                            PhysicalExamReport addedReport = response.body().getData();
                            
                            Log.d(TAG, "体检报告保存成功，准备返回数据到上一页面");
                            Log.d(TAG, "服务器返回数据: " + (addedReport != null ? "non-null" : "null"));
                            
                            // 确保数据完整性 - 增强版验证
                            PhysicalExamReport reportToReturn;
                            if (addedReport != null) {
                                Log.d(TAG, "服务器返回数据详情:");
                                Log.d(TAG, "  ID: " + addedReport.getId());
                                Log.d(TAG, "  报告名称: '" + addedReport.getReportName() + "'");
                                Log.d(TAG, "  检查日期: '" + addedReport.getExamDate() + "'");
                                Log.d(TAG, "  医院名称: '" + addedReport.getHospitalName() + "'");
                                Log.d(TAG, "  摘要: '" + addedReport.getSummary() + "'");
                                
                                // 验证服务器返回数据的关键字段
                                boolean isReportNameValid = !TextUtils.isEmpty(addedReport.getReportName());
                                boolean isExamDateValid = !TextUtils.isEmpty(addedReport.getExamDate());
                                boolean isHospitalNameValid = !TextUtils.isEmpty(addedReport.getHospitalName());
                                boolean isSummaryValid = !TextUtils.isEmpty(addedReport.getSummary());
                                
                                boolean isDataComplete = isReportNameValid && isExamDateValid && isHospitalNameValid;
                                
                                Log.d(TAG, "数据完整性检查:");
                                Log.d(TAG, "  报告名称有效: " + isReportNameValid);
                                Log.d(TAG, "  检查日期有效: " + isExamDateValid);
                                Log.d(TAG, "  医院名称有效: " + isHospitalNameValid);
                                Log.d(TAG, "  摘要有效: " + isSummaryValid);
                                Log.d(TAG, "  整体数据完整: " + isDataComplete);
                                
                                if (isDataComplete) {
                                    Log.d(TAG, "服务器返回数据完整，使用服务器数据");
                                    reportToReturn = addedReport;
                                    
                                    // 如果摘要为空，使用本地数据补充
                                    if (!isSummaryValid && !TextUtils.isEmpty(newReport.getSummary())) {
                                        Log.d(TAG, "补充摘要信息从本地数据");
                                        reportToReturn.setSummary(newReport.getSummary());
                                    }
                                } else {
                                    Log.w(TAG, "服务器返回数据不完整，合并本地数据和服务器数据");
                                    reportToReturn = newReport; // 使用本地数据作为基础
                                    
                                    // 优先使用服务器返回的有效数据
                                    if (addedReport.getId() > 0) {
                                        reportToReturn.setId(addedReport.getId());
                                        Log.d(TAG, "使用服务器返回的ID: " + addedReport.getId());
                                    } else {
                                        reportToReturn.setId((int)(System.currentTimeMillis() % 10000));
                                        Log.d(TAG, "生成临时ID: " + reportToReturn.getId());
                                    }
                                    
                                    // 补充缺失的字段
                                    if (isReportNameValid) {
                                        reportToReturn.setReportName(addedReport.getReportName());
                                    }
                                    if (isExamDateValid) {
                                        reportToReturn.setExamDate(addedReport.getExamDate());
                                    }
                                    if (isHospitalNameValid) {
                                        reportToReturn.setHospitalName(addedReport.getHospitalName());
                                    }
                                    if (isSummaryValid) {
                                        reportToReturn.setSummary(addedReport.getSummary());
                                    }
                                }
                            } else {
                                Log.w(TAG, "服务器返回数据为空，使用本地创建的数据");
                                reportToReturn = newReport;
                                // 设置一个临时ID
                                reportToReturn.setId((int)(System.currentTimeMillis() % 10000));
                                Log.d(TAG, "使用本地数据，生成临时ID: " + reportToReturn.getId());
                            }
                            
                            // 最终数据验证和日志
                            Log.d(TAG, "最终返回的报告数据:");
                            Log.d(TAG, "  ID: " + reportToReturn.getId());
                            Log.d(TAG, "  名称: " + reportToReturn.getReportName());
                            Log.d(TAG, "  日期: " + reportToReturn.getExamDate());
                            Log.d(TAG, "  医院: " + reportToReturn.getHospitalName());
                            Log.d(TAG, "  摘要: " + (TextUtils.isEmpty(reportToReturn.getSummary()) ? "[空]" : reportToReturn.getSummary().substring(0, Math.min(20, reportToReturn.getSummary().length())) + "..."));
                            
                            // 返回到上一个Activity，并传递新创建的体检报告
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("new_physical_exam", reportToReturn);
                            resultIntent.putExtra("operation_type", "add");
                            resultIntent.putExtra("report_id", reportToReturn.getId());
                            resultIntent.putExtra("report_name", reportToReturn.getReportName()); // 额外传递名称用于验证
                            setResult(RESULT_SUCCESS, resultIntent);
                            
                            // 在主线程显示成功消息并关闭页面
                            runOnUiThread(() -> {
                                Toast.makeText(AddPhysicalExamActivity.this, "体检报告添加成功", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "准备关闭AddPhysicalExamActivity，返回到健康档案页面");
                                finish();
                            });
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
                            
                            // API调用成功但返回了错误
                            String errorMsg = "添加体检报告失败: " + (response.body() != null ? response.body().getMessage() : "未知错误");
                            Log.e(TAG, "添加体检报告失败，错误体: " + errorBody);
                            Toast.makeText(AddPhysicalExamActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing add physical exam report response", e);
                        showLoading(false);
                        Toast.makeText(AddPhysicalExamActivity.this, "处理添加结果时出错", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<PhysicalExamReport>> call, Throwable t) {
                    Log.e(TAG, "Failed to call addPhysicalExamReport API", t);
                    showLoading(false);
                    Toast.makeText(AddPhysicalExamActivity.this, "网络请求失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error saving physical exam report", e);
            showLoading(false);
            Toast.makeText(this, "添加体检报告时出错", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 显示/隐藏加载状态
     */
    private void showLoading(boolean show) {
        try {
            // 查找进度条和保存按钮
            View progressBar = findViewById(R.id.progress_bar);
            Button btnSave = findViewById(R.id.btn_save);
            
            if (progressBar != null) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
            
            if (btnSave != null) {
                btnSave.setEnabled(!show);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in showLoading", e);
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
    
    // 获取当前登录用户的ID
    private int getCurrentUserId() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("user_login_state", MODE_PRIVATE);
            return sharedPreferences.getInt("user_id", -1);
        } catch (Exception e) {
            Log.e(TAG, "获取用户ID失败: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * 显示详细的错误对话框
     */
    private void showDetailedError(String title, String message) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        
        try {
            new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing detailed error dialog", e);
            // 如果对话框显示失败，使用Toast作为备选
            Toast.makeText(this, title + ": " + message, Toast.LENGTH_LONG).show();
        }
    }
}