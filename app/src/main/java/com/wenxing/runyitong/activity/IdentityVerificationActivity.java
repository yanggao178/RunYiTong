package com.wenxing.runyitong.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.wenxing.runyitong.R;
import com.wenxing.runyitong.api.ApiClient;
import com.wenxing.runyitong.api.ApiService;
import com.wenxing.runyitong.model.IdentityVerificationRequest;
import com.wenxing.runyitong.model.IdentityVerificationResult;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IdentityVerificationActivity extends AppCompatActivity {
    private static final String TAG = "IdentityVerificationActivity";

    private ImageView ivBack;
    private EditText etRealName;
    private EditText etIdCard;
    private Button btnSubmit;
    private String actualIdCard = ""; // 存储实际的身份证号
    
    private ApiService apiService;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identity_verification);
        
        initViews();
        initData();
        loadVerificationInfo();
        setupClickListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        etRealName = findViewById(R.id.et_real_name);
        etIdCard = findViewById(R.id.et_id_card);
        btnSubmit = findViewById(R.id.btn_submit);
    }
    
    private void initData() {
        apiService = ApiClient.getApiService();
        sharedPreferences = getSharedPreferences("user_login_state", MODE_PRIVATE);
    }

    private void setupClickListeners() {
        // 返回按钮
        ivBack.setOnClickListener(v -> finish());
        
        // 提交按钮
        btnSubmit.setOnClickListener(v -> submitVerification());
        
        // 身份证号输入监听
        setupIdCardMask();
    }
    
    private void setupIdCardMask() {
        // 简单处理：直接存储实际输入的身份证号，不做实时掩码
        etIdCard.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;
                
                isUpdating = true;
                String input = s.toString();
                
                // 直接存储实际输入的身份证号
                actualIdCard = input;
                
                isUpdating = false;
            }
        });
        
        // 只有在失去焦点时才应用掩码，让用户可以正常输入完整的18位身份证号
        etIdCard.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && !TextUtils.isEmpty(actualIdCard) && actualIdCard.length() > 10) {
                    // 当失去焦点且身份证号长度超过10位时，显示掩码
                    StringBuilder maskedId = new StringBuilder();
                    maskedId.append(actualIdCard.substring(0, 10));
                    for (int i = 10; i < actualIdCard.length(); i++) {
                        maskedId.append('*');
                    }
                    etIdCard.setText(maskedId.toString());
                } else if (hasFocus && !TextUtils.isEmpty(actualIdCard)) {
                    // 当获得焦点时，显示完整的身份证号，方便用户编辑
                    etIdCard.setText(actualIdCard);
                }
            }
        });
    }

    private void submitVerification() {
        String realName = etRealName.getText().toString().trim();
        String idCard = actualIdCard.trim(); // 使用实际的身份证号
        
        // 验证输入
        if (TextUtils.isEmpty(realName)) {
            Toast.makeText(this, "请输入真实姓名", Toast.LENGTH_SHORT).show();
            etRealName.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(idCard)) {
            Toast.makeText(this, "请输入身份证号", Toast.LENGTH_SHORT).show();
            etIdCard.requestFocus();
            return;
        }
        
        // 验证身份证号格式（简单验证）
        if (!isValidIdCard(idCard)) {
            Toast.makeText(this, "请输入正确的身份证号", Toast.LENGTH_SHORT).show();
            etIdCard.requestFocus();
            return;
        }
        
        // 检查用户是否登录
        String accessToken = sharedPreferences.getString("access_token", "");
        if (TextUtils.isEmpty(accessToken)) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // 禁用提交按钮，防止重复提交
        btnSubmit.setEnabled(false);
        btnSubmit.setText("正在提交...");
        
        // 发送网络请求
        submitToServer(realName, idCard);
    }
    
    /**
     * 发送实名认证信息到服务器
     */
    private void submitToServer(String realName, String idCard) {
        Log.d(TAG, "开始提交实名认证信息: " + realName + ", " + idCard.substring(0, 6) + "****");
        
        IdentityVerificationRequest request = new IdentityVerificationRequest(realName, idCard);
        
        Call<IdentityVerificationResult> call = apiService.submitIdentityVerification(request);
        call.enqueue(new Callback<IdentityVerificationResult>() {
            @Override
            public void onResponse(Call<IdentityVerificationResult> call, Response<IdentityVerificationResult> response) {
                Log.d(TAG, "网络请求响应: " + response.code());
                
                // 恢复按钮状态
                runOnUiThread(() -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("提交认证");
                });
                
                if (response.isSuccessful() && response.body() != null) {
                    IdentityVerificationResult result = response.body();
                    Log.d(TAG, "请求成功: " + result.getMessage());
                    
                    runOnUiThread(() -> {
                        if (result.isSuccess()) {
                            Toast.makeText(IdentityVerificationActivity.this, 
                                result.getMessage(), Toast.LENGTH_LONG).show();
                            
                            // 保存到本地缓存
                            saveVerificationInfo(realName, idCard);
                            
                            // 返回上一页
                           // finish();
                        } else {
                            Toast.makeText(IdentityVerificationActivity.this, 
                                "提交失败: " + result.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Log.e(TAG, "服务器响应失败: " + response.code() + " - " + response.message());
                    runOnUiThread(() -> {
                        String errorMsg = "提交失败，请稍后重试";
                        if (response.code() == 401) {
                            errorMsg = "登录状态已过期，请重新登录";
                        } else if (response.code() == 400) {
                            errorMsg = "请求参数有误，请检查输入信息";
                        }
                        Toast.makeText(IdentityVerificationActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    });
                }
            }
            
            @Override
            public void onFailure(Call<IdentityVerificationResult> call, Throwable t) {
                Log.e(TAG, "网络请求失败: " + t.getMessage(), t);
                
                runOnUiThread(() -> {
                    // 恢复按钮状态
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("提交认证");
                    
                    String errorMsg = "网络错误，请检查网络连接";
                    if (t.getMessage() != null) {
                        if (t.getMessage().contains("timeout")) {
                            errorMsg = "请求超时，请稍后重试";
                        } else if (t.getMessage().contains("Connection refused")) {
                            errorMsg = "无法连接到服务器";
                        }
                    }
                    Toast.makeText(IdentityVerificationActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    /**
     * 简单的身份证号验证
     */
    private boolean isValidIdCard(String idCard) {
        // 18位身份证号的简单格式验证
        if (idCard.length() != 18) {
            return false;
        }
        
        // 前17位必须是数字
        for (int i = 0; i < 17; i++) {
            if (!Character.isDigit(idCard.charAt(i))) {
                return false;
            }
        }
        
        // 最后一位可以是数字或X
        char lastChar = idCard.charAt(17);
        return Character.isDigit(lastChar) || lastChar == 'X' || lastChar == 'x';
    }
    
    /**
     * 加载已保存的认证信息
     */
    private void loadVerificationInfo() {
        String savedRealName = getSharedPreferences("identity_verification", MODE_PRIVATE)
            .getString("real_name", "");
        String savedIdCard = getSharedPreferences("identity_verification", MODE_PRIVATE)
            .getString("id_card", "");
        
        if (!TextUtils.isEmpty(savedRealName)) {
            etRealName.setText(savedRealName);
        }
        
        if (!TextUtils.isEmpty(savedIdCard)) {
            actualIdCard = savedIdCard;
            // 显示时后8位用星号替换
            if (savedIdCard.length() > 10) {
                String maskedId = savedIdCard.substring(0, 10) + "********";
                etIdCard.setText(maskedId);
            } else {
                etIdCard.setText(savedIdCard);
            }
        }
    }
    
    /**
     * 保存认证信息到本地
     */
    private void saveVerificationInfo(String realName, String idCard) {
        getSharedPreferences("identity_verification", MODE_PRIVATE)
            .edit()
            .putString("real_name", realName)
            .putString("id_card", idCard)
            .putBoolean("is_verified", true)
            .apply();
    }
}