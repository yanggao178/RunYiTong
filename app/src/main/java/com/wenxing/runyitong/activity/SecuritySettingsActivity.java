package com.wenxing.runyitong.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.wenxing.runyitong.R;
import com.wenxing.runyitong.utils.SecurityManager;

/**
 * 账号与安全设置页面
 * 提供注销账号等安全相关功能
 */
public class SecuritySettingsActivity extends AppCompatActivity {

    private static final String TAG = "SecuritySettingsActivity";
    private Button btnLogoutAccount;
    private SecurityManager securityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_settings);
        
        initViews();
        setupToolbar();
        setupClickListeners();
        initSecurityManager();
    }

    private void initViews() {
        btnLogoutAccount = findViewById(R.id.btn_logout_account);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("账号与安全");
        }
    }

    private void setupClickListeners() {
        // 注销账号按钮点击事件
        btnLogoutAccount.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void initSecurityManager() {
        securityManager = new SecurityManager(this);
    }

    /**
     * 显示注销账号确认对话框
     */
    private void showLogoutConfirmationDialog() {
        // 创建自定义样式的对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        
        // 设置对话框图标
        builder.setIcon(R.drawable.ic_warning_amber)
                .setTitle("确认注销账号")
                .setMessage("注销账号将清除您的所有登录状态，您需要重新登录才能使用应用。确定要注销吗？")
                // 添加取消按钮
                .setNegativeButton("取消", (dialog, which) -> {
                    // 关闭对话框
                    dialog.dismiss();
                })
                // 添加注销按钮
                .setPositiveButton("注销", null); // 按钮点击事件在onShowListener中设置
        
        // 创建对话框
        AlertDialog dialog = builder.create();
        
        // 监听对话框显示完成事件，设置按钮样式
        dialog.setOnShowListener(dialogInterface -> {
            // 获取资源
            Resources res = getResources();
            int buttonPadding = res.getDimensionPixelSize(R.dimen.button_padding);
            float buttonTextSize = res.getDimension(R.dimen.button_text_size);
            
            // 设置取消按钮样式
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            if (negativeButton != null) {
                negativeButton.setTextColor(res.getColor(R.color.text_primary));
                negativeButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonTextSize);
                // 设置按钮内边距
                negativeButton.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);
                // 设置按钮背景
                negativeButton.setBackground(res.getDrawable(R.drawable.rounded_corner_background));
            }
            
            // 设置确定按钮样式为危险按钮
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (positiveButton != null) {
                positiveButton.setTextColor(Color.WHITE);
                positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, buttonTextSize);
                // 设置按钮内边距
                positiveButton.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);
                // 设置危险按钮背景
                positiveButton.setBackground(res.getDrawable(R.drawable.btn_danger_background));
                
                // 设置点击事件
                positiveButton.setOnClickListener(v -> {
                    dialog.dismiss();
                    performLogout();
                });
            }
        });
        
        // 设置对话框内容区域的内边距
        if (dialog.getWindow() != null) {
            // 使用自定义圆角背景
            dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_dialog_background));
            
            // 设置对话框宽度为屏幕宽度的80%，但不超过500dp
            WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int dialogMaxWidth = Math.min((int)(screenWidth * 0.8f), 500);
            layoutParams.width = dialogMaxWidth;
            dialog.getWindow().setAttributes(layoutParams);
        }
        
        // 显示对话框
        dialog.show();
    }

    /**
     * 执行注销账号操作
     */
    private void performLogout() {
        try {
            // 获取SharedPreferences
            android.content.SharedPreferences sharedPreferences = getSharedPreferences("user_login_state", MODE_PRIVATE);
            
            // 清除登录状态
            sharedPreferences.edit()
                    .putBoolean("is_logged_in", false)
                    .putString("username", "")
                    .putInt("user_id", -1)
                    .putString("access_token", "")
                    .apply();
            
            // 显示退出成功提示
            Toast.makeText(this, "已成功退出登录", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "用户已退出登录");
            
            // 跳转到登录页面
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            
            // 关闭当前页面
            finish();
        } catch (Exception e) {
            Log.e(TAG, "退出登录失败: " + e.getMessage(), e);
            Toast.makeText(this, "退出登录失败，请重试", Toast.LENGTH_SHORT).show();
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
}