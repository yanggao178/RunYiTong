package com.wenxing.runyitong.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.wenxing.runyitong.R;
import com.wenxing.runyitong.utils.UXEnhancementUtils;
import com.wenxing.runyitong.utils.PerformanceManager;
import com.wenxing.runyitong.utils.ErrorHandlingManager;

public class MemberRegistrationActivity extends AppCompatActivity {

    private static final String TAG = "MemberRegistrationActivity";
    private static final int MEMBERSHIP_FEE = 299; // 一年会员费用

    private Button btnSubscribe;
    private CheckBox cbAgreement;
    private RadioGroup rgPaymentMethod;
    private ProgressBar pbSubscribeLoading;
    private CardView formContainer;

    // SharedPreferences相关
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "user_login_state";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USERNAME = "username";
    private static final String USER_ID = "user_id";

    // 性能监控
    private PerformanceManager.PageLoadMonitor pageLoadMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");

        // 启动性能监控
        pageLoadMonitor = new PerformanceManager.PageLoadMonitor("MemberRegistrationActivity");

        // 优化Activity内存使用
        PerformanceManager.optimizeActivityMemory(this);

        // 检查Activity状态
        if (isFinishing() || isDestroyed()) {
            Log.w(TAG, "Activity is finishing or destroyed, aborting onCreate");
            return;
        }

        setContentView(R.layout.activity_member_registration);
        Log.d(TAG, "Layout set successfully");

        // 初始化视图
        initViews();

        // 设置点击监听器
        setupClickListeners();

        // 标记页面加载完成
        if (pageLoadMonitor != null) {
            pageLoadMonitor.onPageLoadComplete();
        }

        Log.d(TAG, "onCreate completed successfully");
    }

    /**
     * 初始化所有视图组件
     */
    private void initViews() {
        try {
            // 初始化按钮
            btnSubscribe = findViewById(R.id.btn_subscribe);
            cbAgreement = findViewById(R.id.cb_agreement);
            rgPaymentMethod = findViewById(R.id.rg_payment_method);
            pbSubscribeLoading = findViewById(R.id.pb_subscribe_loading);
            formContainer = findViewById(R.id.form_container);

            // 默认选中支付宝
            RadioButton rbAlipay = findViewById(R.id.rb_alipay);
            if (rbAlipay != null) {
                rbAlipay.setChecked(true);
            }

            // 初始化SharedPreferences
            sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

            // UI增强效果通过XML布局文件设置

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            // 使用现有的错误处理方法
            String errorMessage = ErrorHandlingManager.handleErrorSimple(this, e, "初始化会员注册页面");
            UXEnhancementUtils.showEnhancedToast(this, errorMessage, "error");
        }
    }

    /**
     * 设置点击监听器
     */
    private void setupClickListeners() {
        try {
            // 立即开通按钮点击事件
            if (btnSubscribe != null) {
                btnSubscribe.setOnClickListener(v -> handleSubscribeClick());
            }

            // 协议勾选框变化事件
            if (cbAgreement != null) {
                cbAgreement.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (btnSubscribe != null) {
                        btnSubscribe.setEnabled(isChecked);
                        // 更新按钮样式
                        if (isChecked) {
                        // 设置按钮激活状态
                        btnSubscribe.setAlpha(1.0f);
                    } else {
                        // 设置按钮非激活状态
                        btnSubscribe.setAlpha(0.5f);
                    }
                    }
                });
            }

            // 初始状态下，如果协议未勾选，禁用按钮
            if (cbAgreement != null && btnSubscribe != null && !cbAgreement.isChecked()) {
                btnSubscribe.setEnabled(false);
            // 设置按钮非激活状态
            btnSubscribe.setAlpha(0.5f);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage(), e);
            Toast.makeText(this, "设置交互功能失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 处理立即开通按钮点击事件
     */
    private void handleSubscribeClick() {
        Log.d(TAG, "Subscribe button clicked");

        // 检查用户是否登录
        if (!isUserLoggedIn()) {
            Log.w(TAG, "User not logged in, redirecting to login");
            Toast.makeText(this, "请先登录账户", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        // 检查协议是否勾选
        if (cbAgreement == null || !cbAgreement.isChecked()) {
            Log.w(TAG, "User hasn't agreed to terms");
            Toast.makeText(this, "请阅读并同意会员服务协议和隐私政策", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取选中的支付方式
        String paymentMethod = getSelectedPaymentMethod();
        Log.d(TAG, "Selected payment method: " + paymentMethod);

        // 显示加载状态
        showLoading(true);

        // 模拟支付流程
        simulatePayment(paymentMethod);
    }

    /**
     * 检查用户是否登录
     */
    private boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * 获取选中的支付方式
     */
    private String getSelectedPaymentMethod() {
        int selectedId = rgPaymentMethod.getCheckedRadioButtonId();
        RadioButton selectedButton = findViewById(selectedId);
        return selectedButton != null ? selectedButton.getText().toString() : "支付宝";
    }

    /**
     * 模拟支付流程
     */
    private void simulatePayment(final String paymentMethod) {
        // 模拟网络请求延迟
        new android.os.Handler().postDelayed(() -> {
            try {
                // 模拟支付结果 - 这里简化为直接成功
                boolean paymentSuccess = true;

                if (paymentSuccess) {
                    Log.d(TAG, "Payment successful with " + paymentMethod);
                    // 保存会员状态
                    saveMemberStatus();
                    // 显示成功提示
                    Toast.makeText(MemberRegistrationActivity.this, "恭喜您，会员开通成功！", Toast.LENGTH_LONG).show();
                    // 设置结果并返回上一个页面
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Log.w(TAG, "Payment failed");
                    Toast.makeText(MemberRegistrationActivity.this, "支付失败，请重试", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Payment simulation error: " + e.getMessage(), e);
                Toast.makeText(MemberRegistrationActivity.this, "支付过程中发生错误", Toast.LENGTH_SHORT).show();
            } finally {
                // 隐藏加载状态
                showLoading(false);
            }
        }, 2000); // 2秒延迟模拟网络请求
    }

    /**
     * 保存会员状态
     */
    private void saveMemberStatus() {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            // 保存会员开通状态和有效期
            long expireTime = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000); // 一年后过期
            editor.putBoolean("is_member", true);
            editor.putLong("member_expire_time", expireTime);
            editor.putInt("member_fee", MEMBERSHIP_FEE);
            editor.apply();
            Log.d(TAG, "Member status saved successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error saving member status: " + e.getMessage(), e);
        }
    }

    /**
     * 显示或隐藏加载状态
     */
    private void showLoading(boolean show) {
        if (pbSubscribeLoading != null && btnSubscribe != null) {
            if (show) {
                pbSubscribeLoading.setVisibility(View.VISIBLE);
                btnSubscribe.setText("");
                btnSubscribe.setEnabled(false);
            } else {
                pbSubscribeLoading.setVisibility(View.GONE);
                btnSubscribe.setText("立即开通");
                btnSubscribe.setEnabled(cbAgreement != null && cbAgreement.isChecked());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        // 清理资源
        pageLoadMonitor = null;
    }
}