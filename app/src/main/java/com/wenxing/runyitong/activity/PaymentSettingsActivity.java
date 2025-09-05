package com.wenxing.runyitong.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.wenxing.runyitong.R;

public class PaymentSettingsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private LinearLayout layoutModifyPassword;
    private LinearLayout layoutSetPassword;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_settings);
        
        initViews();
        setupToolbar();
        setupClickListeners();
        initSharedPreferences();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        layoutModifyPassword = findViewById(R.id.layout_modify_password);
        layoutSetPassword = findViewById(R.id.layout_set_password);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("支付设置");
        }
        
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void setupClickListeners() {
        // 修改支付密码点击事件
        layoutModifyPassword.setOnClickListener(v -> {
            if (hasPaymentPassword()) {
                // 如果已设置支付密码，打开修改密码页面
                Intent intent = new Intent(this, ModifyPaymentPasswordActivity.class);
                startActivity(intent);
            } else {
                // 如果未设置支付密码，提示先设置密码
                Toast.makeText(this, "请先设置支付密码", Toast.LENGTH_SHORT).show();
            }
        });
        
        // 设置支付密码点击事件
        layoutSetPassword.setOnClickListener(v -> {
            if (hasPaymentPassword()) {
                // 如果已设置支付密码，提示已设置
                Toast.makeText(this, "您已设置支付密码，如需修改请点击修改支付密码", Toast.LENGTH_SHORT).show();
            } else {
                // 如果未设置支付密码，启动设置密码页面
                Intent intent = new Intent(this, SetPaymentPasswordActivity.class);
                startActivity(intent);
            }
        });
    }
    
    private void initSharedPreferences() {
        sharedPreferences = getSharedPreferences("payment_settings", MODE_PRIVATE);
    }
    
    /**
     * 检查是否已设置支付密码
     */
    private boolean hasPaymentPassword() {
        return sharedPreferences.getBoolean("has_payment_password", false);
    }
    
    /**
     * 保存支付密码设置状态
     */
    private void savePaymentPasswordStatus(boolean hasPassword) {
        sharedPreferences.edit()
                .putBoolean("has_payment_password", hasPassword)
                .apply();
    }
}