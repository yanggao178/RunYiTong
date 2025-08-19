package com.wenteng.frontend_android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import com.wenteng.frontend_android.R;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Switch switchNotifications;
    private Switch switchDarkMode;
    private Switch switchAutoUpdate;
    private CardView cardAbout;
    private CardView cardPrivacy;
    private CardView cardHelp;
    private LinearLayout layoutAddress, layoutSecurity, layoutIdentity, layoutPayment;
    private TextView tvVersionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        initViews();
        setupToolbar();
        setupClickListeners();
        loadSettings();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        switchNotifications = findViewById(R.id.switch_notifications);
        switchDarkMode = findViewById(R.id.switch_dark_mode);
        switchAutoUpdate = findViewById(R.id.switch_auto_update);
        cardAbout = findViewById(R.id.card_about);
        cardPrivacy = findViewById(R.id.card_privacy);
        cardHelp = findViewById(R.id.card_help);
        layoutAddress = findViewById(R.id.layout_address);
        layoutSecurity = findViewById(R.id.layout_security);
        layoutIdentity = findViewById(R.id.layout_identity);
        layoutPayment = findViewById(R.id.layout_payment);
        tvVersionName = findViewById(R.id.tv_version_name);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("设置");
        }
    }
    
    private void setupClickListeners() {
        // 通知设置
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 保存通知设置
            saveNotificationSetting(isChecked);
        });
        
        // 深色模式设置
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 保存深色模式设置
            saveDarkModeSetting(isChecked);
        });
        
        // 自动更新设置
        switchAutoUpdate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 保存自动更新设置
            saveAutoUpdateSetting(isChecked);
        });
        
        // 关于我们
        cardAbout.setOnClickListener(v -> {
            // 打开关于页面
            showAboutDialog();
        });
        
        // 隐私政策
        cardPrivacy.setOnClickListener(v -> {
            // 打开隐私政策页面
            showPrivacyPolicy();
        });
        
        // 帮助与反馈
        cardHelp.setOnClickListener(v -> {
            // 打开帮助页面
            showHelpDialog();
        });
        
        // 账户管理点击事件
        layoutAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddressActivity.class);
            startActivity(intent);
        });
        
        layoutSecurity.setOnClickListener(v -> {
            android.widget.Toast.makeText(this, "账号与安全功能开发中", android.widget.Toast.LENGTH_SHORT).show();
        });
        
        // 认证与支付点击事件
        layoutIdentity.setOnClickListener(v -> {
            Intent intent = new Intent(this, IdentityVerificationActivity.class);
            startActivity(intent);
        });
        
        layoutPayment.setOnClickListener(v -> {
            Intent intent = new Intent(this, PaymentSettingsActivity.class);
            startActivity(intent);
        });
    }
    
    private void loadSettings() {
        // 从SharedPreferences加载设置
        android.content.SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        
        switchNotifications.setChecked(prefs.getBoolean("notifications_enabled", true));
        switchDarkMode.setChecked(prefs.getBoolean("dark_mode_enabled", false));
        switchAutoUpdate.setChecked(prefs.getBoolean("auto_update_enabled", true));
        
        // 设置版本信息
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            tvVersionName.setText("版本 " + versionName);
        } catch (Exception e) {
            tvVersionName.setText("版本 1.0.0");
        }
    }
    
    private void saveNotificationSetting(boolean enabled) {
        android.content.SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        prefs.edit().putBoolean("notifications_enabled", enabled).apply();
    }
    
    private void saveDarkModeSetting(boolean enabled) {
        android.content.SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        prefs.edit().putBoolean("dark_mode_enabled", enabled).apply();
        // 这里可以添加切换主题的逻辑
    }
    
    private void saveAutoUpdateSetting(boolean enabled) {
        android.content.SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        prefs.edit().putBoolean("auto_update_enabled", enabled).apply();
    }
    
    private void showAboutDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("关于AI医疗助手")
            .setMessage("AI医疗助手是一款智能医疗服务应用，提供预约挂号、处方分析、健康管理等功能。\n\n开发团队：文腾科技\n联系邮箱：support@wenteng.com")
            .setPositiveButton("确定", null)
            .show();
    }
    
    private void showPrivacyPolicy() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("隐私政策")
            .setMessage("我们重视您的隐私保护。本应用收集的个人信息仅用于提供医疗服务，不会泄露给第三方。\n\n详细的隐私政策请访问我们的官方网站查看。")
            .setPositiveButton("确定", null)
            .show();
    }
    
    private void showHelpDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("帮助与反馈")
            .setMessage("如果您在使用过程中遇到问题，可以通过以下方式联系我们：\n\n客服热线：400-123-4567\n客服邮箱：help@wenteng.com\n工作时间：周一至周五 9:00-18:00")
            .setPositiveButton("确定", null)
            .show();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}