package com.wenxing.runyitong.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.wenxing.runyitong.R;

public class IdentityVerificationActivity extends AppCompatActivity {

    private ImageView ivBack;
    private EditText etRealName;
    private EditText etIdCard;
    private Button btnSubmit;
    private String actualIdCard = ""; // 存储实际的身份证号

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identity_verification);
        
        initViews();
        loadVerificationInfo();
        setupClickListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        etRealName = findViewById(R.id.et_real_name);
        etIdCard = findViewById(R.id.et_id_card);
        btnSubmit = findViewById(R.id.btn_submit);
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
                
                // 移除所有星号，获取实际输入的数字和字母
                StringBuilder actualInput = new StringBuilder();
                for (char c : input.toCharArray()) {
                    if (c != '*') {
                        actualInput.append(c);
                    }
                }
                
                actualIdCard = actualInput.toString();
                
                // 如果长度超过10位，后面的用星号替换显示
                if (actualIdCard.length() > 10) {
                    StringBuilder maskedId = new StringBuilder();
                    maskedId.append(actualIdCard.substring(0, 10));
                    for (int i = 10; i < actualIdCard.length(); i++) {
                        maskedId.append('*');
                    }
                    s.replace(0, s.length(), maskedId.toString());
                } else {
                    s.replace(0, s.length(), actualIdCard);
                }
                
                isUpdating = false;
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
        
        // 这里可以添加实际的提交逻辑
        // 目前只是显示成功提示
        Toast.makeText(this, "实名认证信息已提交，等待审核", Toast.LENGTH_LONG).show();
        
        // 可以保存到SharedPreferences或发送到服务器
        saveVerificationInfo(realName, idCard);
        
        // 返回上一页
        finish();
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