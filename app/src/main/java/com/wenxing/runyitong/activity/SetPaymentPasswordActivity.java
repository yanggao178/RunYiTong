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

public class SetPaymentPasswordActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnConfirm;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_payment_password);
        
        initViews();
        setupToolbar();
        setupClickListeners();
        initSharedPreferences();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnConfirm = findViewById(R.id.btn_confirm);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("设置支付密码");
        }
        
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void setupClickListeners() {
        btnConfirm.setOnClickListener(v -> {
            setPaymentPassword();
        });
    }
    
    private void initSharedPreferences() {
        sharedPreferences = getSharedPreferences("payment_settings", MODE_PRIVATE);
    }
    
    private void setPaymentPassword() {
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        
        // 验证输入
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入支付密码", Toast.LENGTH_SHORT).show();
            etPassword.requestFocus();
            return;
        }
        
        if (password.length() != 6) {
            Toast.makeText(this, "支付密码必须为6位数字", Toast.LENGTH_SHORT).show();
            etPassword.requestFocus();
            return;
        }
        
        if (!password.matches("\\d{6}")) {
            Toast.makeText(this, "支付密码只能包含数字", Toast.LENGTH_SHORT).show();
            etPassword.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "请确认支付密码", Toast.LENGTH_SHORT).show();
            etConfirmPassword.requestFocus();
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
            etConfirmPassword.requestFocus();
            return;
        }
        
        // 保存支付密码
        savePaymentPassword(password);
        
        Toast.makeText(this, "支付密码设置成功", Toast.LENGTH_SHORT).show();
        finish();
    }
    
    private void savePaymentPassword(String password) {
        sharedPreferences.edit()
                .putString("payment_password", password)
                .putBoolean("has_payment_password", true)
                .apply();
    }
}