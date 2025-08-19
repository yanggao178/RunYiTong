package com.wenteng.frontend_android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.wenteng.frontend_android.R;
import com.wenteng.frontend_android.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etPhone, etVerificationCode;
    private TextInputLayout tilUsername, tilPassword, tilPhone, tilVerificationCode;
    private Button btnLogin, btnRegister, btnSendCode;
    private TextView tvForgotPassword;
    private boolean isCodeSent = false;
    private int countdown = 60;
    
    // Loading indicators
    private ProgressBar pbLoginLoading, pbSendCodeLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        initViews();
        setupClickListeners();
        setupAnimations();
    }
    
    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etPhone = findViewById(R.id.et_phone);
        etVerificationCode = findViewById(R.id.et_verification_code);
        
        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        tilPhone = findViewById(R.id.til_phone);
        tilVerificationCode = findViewById(R.id.til_verification_code);
        
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        btnSendCode = findViewById(R.id.btn_send_code);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        
        // Initialize loading indicators
        pbLoginLoading = findViewById(R.id.pb_login_loading);
        pbSendCodeLoading = findViewById(R.id.pb_send_code_loading);
        
        setupInputValidation();
        setupInputFocusAnimations();
    }
    
    private void setupInputValidation() {
        // 用户名实时验证
        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tilUsername != null) {
                    tilUsername.setError(null);
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                validateUsername(s.toString());
            }
        });
        
        // 密码实时验证
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tilPassword != null) {
                    tilPassword.setError(null);
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                validatePassword(s.toString());
            }
        });
        
        // 手机号实时验证
        etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tilPhone != null) {
                    tilPhone.setError(null);
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                validatePhone(s.toString());
            }
        });
        
        // 验证码实时验证
        etVerificationCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tilVerificationCode != null) {
                    tilVerificationCode.setError(null);
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                validateVerificationCode(s.toString());
            }
        });
    }
    
    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> performLogin());
        
        btnRegister.setOnClickListener(v -> {
            // 跳转到注册页面
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            // 添加界面切换动画
            overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out);
        });
        
        btnSendCode.setOnClickListener(v -> sendVerificationCode());
        
        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "忘记密码功能开发中...", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String verificationCode = etVerificationCode.getText().toString().trim();
        
        // 检查是否使用手机验证码登录
        if (!TextUtils.isEmpty(phone) && !TextUtils.isEmpty(verificationCode)) {
            performPhoneLogin(phone, verificationCode);
            return;
        }
        
        // 传统用户名密码登录
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("请输入用户名");
            etUsername.requestFocus();
            return;
        }
        
        if (!validateUsername(username)) {
            etUsername.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("请输入密码");
            etPassword.requestFocus();
            return;
        }
        
        if (!validatePassword(password)) {
            etPassword.requestFocus();
            return;
        }
        
        // 显示加载状态
        showLoginLoading(true);
        
        // 模拟网络请求延迟
        new android.os.Handler().postDelayed(() -> {
            // 隐藏加载状态
            showLoginLoading(false);
            
            // 简单的演示登录逻辑
            if (username.equals("admin") && password.equals("123456")) {
                Toast.makeText(this, "登录成功！", Toast.LENGTH_SHORT).show();
                
                // 跳转到主页面
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
            }
        }, 2000); // 2秒延迟模拟网络请求
    }
    
    private void performPhoneLogin(String phone, String verificationCode) {
        if (!validatePhone(phone)) {
            etPhone.requestFocus();
            return;
        }
        
        if (!isCodeSent) {
            tilVerificationCode.setError("请先发送验证码");
            return;
        }
        
        if (!validateVerificationCode(verificationCode)) {
            etVerificationCode.requestFocus();
            return;
        }
        
        // 模拟验证码验证（实际应用中应该调用后端API验证）
        if (verificationCode.equals("123456")) {
            Toast.makeText(this, "手机验证码登录成功！", Toast.LENGTH_SHORT).show();
            
            // 跳转到主页面
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            tilVerificationCode.setError("验证码错误，请重新输入");
        }
    }
    
    private void sendVerificationCode() {
        String phone = etPhone.getText().toString().trim();
        
        if (!validatePhone(phone)) {
            etPhone.requestFocus();
            return;
        }
        
        if (isCodeSent) {
            Toast.makeText(this, "验证码已发送，请稍后再试", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 模拟发送验证码
        simulateSendSMS(phone);
    }
    
    private void simulateSendSMS(String phone) {
        // 显示发送验证码加载状态
        showSendCodeLoading(true);
        
        // 模拟网络请求延迟
        new android.os.Handler().postDelayed(() -> {
            // 隐藏加载状态
            showSendCodeLoading(false);
            
            // 模拟短信发送服务
            Toast.makeText(this, "验证码已发送到 " + phone + "\n演示验证码: 123456", Toast.LENGTH_LONG).show();
            
            isCodeSent = true;
            btnSendCode.setEnabled(false);
            
            // 开始倒计时
            startCountdown();
        }, 1500); // 1.5秒延迟模拟网络请求
    }
    
    private void showSendCodeLoading(boolean show) {
        if (show) {
            pbSendCodeLoading.setVisibility(View.VISIBLE);
            btnSendCode.setEnabled(false);
            btnSendCode.setText("发送中...");
        } else {
            pbSendCodeLoading.setVisibility(View.GONE);
            btnSendCode.setEnabled(true);
            btnSendCode.setText("发送验证码");
        }
    }
    
    private void setupAnimations() {
        // Logo区域淡入动画
        View logoArea = findViewById(R.id.logo_area);
        if (logoArea != null) {
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            logoArea.startAnimation(fadeIn);
        }
        
        // 表单区域从下方滑入
        View formContainer = findViewById(R.id.form_container);
        if (formContainer != null) {
            Animation slideInBottom = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
            slideInBottom.setStartOffset(200); // 延迟200ms开始
            formContainer.startAnimation(slideInBottom);
        }
        
        // 按钮区域从下方滑入
        View buttonContainer = findViewById(R.id.button_container);
        if (buttonContainer != null) {
            Animation slideInBottom = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
            slideInBottom.setStartOffset(400); // 延迟400ms开始
            buttonContainer.startAnimation(slideInBottom);
        }
        
        // 为按钮添加点击动画
        setupButtonAnimations();
    }
    
    private void setupButtonAnimations() {
        // 登录按钮点击动画
        btnLogin.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                Animation scaleDown = AnimationUtils.loadAnimation(this, R.anim.button_scale);
                v.startAnimation(scaleDown);
            }
            return false; // 让其他点击事件继续处理
        });
        
        // 发送验证码按钮点击动画
        btnSendCode.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                Animation scaleDown = AnimationUtils.loadAnimation(this, R.anim.button_scale);
                v.startAnimation(scaleDown);
            }
            return false;
        });
        
        // 注册按钮点击动画
        btnRegister.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                Animation scaleDown = AnimationUtils.loadAnimation(this, R.anim.button_scale);
                v.startAnimation(scaleDown);
            }
            return false;
        });
     }
     
     private void setupInputFocusAnimations() {
         // 为所有输入框添加获得焦点时的动画
         View.OnFocusChangeListener focusAnimationListener = (v, hasFocus) -> {
             if (hasFocus) {
                 Animation focusAnimation = AnimationUtils.loadAnimation(this, R.anim.input_focus);
                 v.startAnimation(focusAnimation);
             }
         };
         
         etUsername.setOnFocusChangeListener(focusAnimationListener);
         etPhone.setOnFocusChangeListener(focusAnimationListener);
         etVerificationCode.setOnFocusChangeListener(focusAnimationListener);
         etPassword.setOnFocusChangeListener(focusAnimationListener);
     }
     
     private void startCountdown() {
        countdown = 60;
        android.os.Handler handler = new android.os.Handler();
        
        Runnable countdownRunnable = new Runnable() {
            @Override
            public void run() {
                if (countdown > 0) {
                    btnSendCode.setText(countdown + "s后重发");
                    countdown--;
                    handler.postDelayed(this, 1000);
                } else {
                    btnSendCode.setText("发送验证码");
                    btnSendCode.setEnabled(true);
                    isCodeSent = false;
                }
            }
        };
        
        handler.post(countdownRunnable);
    }
    
    private boolean isValidPhoneNumber(String phone) {
        return phone != null && phone.matches("^1[3-9]\\d{9}$");
    }
    
    // 验证方法
    private boolean validateUsername(String username) {
        if (TextUtils.isEmpty(username)) {
            return true; // 空值在其他地方处理
        }
        
        if (username.length() < 3) {
            tilUsername.setError("用户名至少需要3个字符");
            return false;
        }
        
        if (username.length() > 20) {
            tilUsername.setError("用户名不能超过20个字符");
            return false;
        }
        
        if (!username.matches("^[a-zA-Z0-9_\u4e00-\u9fa5]+$")) {
            tilUsername.setError("用户名只能包含字母、数字、下划线和中文");
            return false;
        }
        
        tilUsername.setError(null);
        return true;
    }
    
    private boolean validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            return true; // 空值在其他地方处理
        }
        
        if (password.length() < 6) {
            tilPassword.setError("密码至少需要6个字符");
            return false;
        }
        
        if (password.length() > 20) {
            tilPassword.setError("密码不能超过20个字符");
            return false;
        }
        
        tilPassword.setError(null);
        return true;
    }
    
    private boolean validatePhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError("请输入手机号");
            return false;
        }
        
        if (!isValidPhoneNumber(phone)) {
            tilPhone.setError("请输入有效的11位手机号");
            return false;
        }
        
        tilPhone.setError(null);
        return true;
    }
    
    private boolean validateVerificationCode(String code) {
        if (TextUtils.isEmpty(code)) {
            tilVerificationCode.setError("请输入验证码");
            return false;
        }
        
        if (code.length() != 6) {
            tilVerificationCode.setError("验证码应为6位数字");
            return false;
        }
        
        if (!code.matches("^\\d{6}$")) {
            tilVerificationCode.setError("验证码只能包含数字");
            return false;
        }
        
        tilVerificationCode.setError(null);
        return true;
    }
    
    private void showLoginLoading(boolean show) {
        if (show) {
            pbLoginLoading.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
            btnLogin.setText("登录中...");
        } else {
            pbLoginLoading.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            btnLogin.setText("登录");
        }
    }
}