package com.wenxing.runyitong.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.wenxing.runyitong.R;

public class ModifyPaymentPasswordActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText etOldPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private Button btnConfirm;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_payment_password);
        
        initViews();
        setupToolbar();
        setupClickListeners();
        initSharedPreferences();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etOldPassword = findViewById(R.id.et_old_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnConfirm = findViewById(R.id.btn_confirm);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("修改支付密码");
        }
        
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void setupClickListeners() {
        btnConfirm.setOnClickListener(v -> {
            modifyPaymentPassword();
        });
    }
    
    private void initSharedPreferences() {
        sharedPreferences = getSharedPreferences("payment_settings", MODE_PRIVATE);
    }
    
    private void modifyPaymentPassword() {
        String oldPassword = etOldPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        
        // 验证输入
        if (TextUtils.isEmpty(oldPassword)) {
            Toast.makeText(this, "请输入旧密码", Toast.LENGTH_SHORT).show();
            etOldPassword.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "请输入新密码", Toast.LENGTH_SHORT).show();
            etNewPassword.requestFocus();
            return;
        }
        
        if (newPassword.length() != 6) {
            Toast.makeText(this, "支付密码必须为6位数字", Toast.LENGTH_SHORT).show();
            etNewPassword.requestFocus();
            return;
        }
        
        if (!newPassword.matches("\\d{6}")) {
            Toast.makeText(this, "支付密码只能包含数字", Toast.LENGTH_SHORT).show();
            etNewPassword.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "请确认新密码", Toast.LENGTH_SHORT).show();
            etConfirmPassword.requestFocus();
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "两次输入的新密码不一致", Toast.LENGTH_SHORT).show();
            etConfirmPassword.requestFocus();
            return;
        }
        
        // 验证旧密码
        String savedPassword = sharedPreferences.getString("payment_password", "");
        if (!oldPassword.equals(savedPassword)) {
            Toast.makeText(this, "旧密码错误", Toast.LENGTH_SHORT).show();
            etOldPassword.requestFocus();
            return;
        }
        
        // 检查新密码是否与旧密码相同
        if (oldPassword.equals(newPassword)) {
            Toast.makeText(this, "新密码不能与旧密码相同", Toast.LENGTH_SHORT).show();
            etNewPassword.requestFocus();
            return;
        }
        
        // 保存新密码
        saveNewPassword(newPassword);
        
        Toast.makeText(this, "支付密码修改成功", Toast.LENGTH_SHORT).show();
        finish();
    }
    
    private void saveNewPassword(String newPassword) {
        sharedPreferences.edit()
                .putString("payment_password", newPassword)
                .putBoolean("has_payment_password", true)
                .apply();
    }
}