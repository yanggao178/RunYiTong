package com.wenxing.xunyitong.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.textfield.TextInputLayout;
import com.wenxing.xunyitong.MainActivity;
import com.wenxing.xunyitong.R;
import com.wenxing.xunyitong.api.ApiClient;
import com.wenxing.xunyitong.api.ApiResponse;
import com.wenxing.xunyitong.api.ApiService;
import com.wenxing.xunyitong.api.LoginRequest;
import com.wenxing.xunyitong.api.LoginResponse;
import com.wenxing.xunyitong.utils.UXEnhancementUtils;
import com.wenxing.xunyitong.utils.ValidationManager;
import com.wenxing.xunyitong.utils.SecurityManager;
import com.wenxing.xunyitong.utils.ErrorHandlingManager;
import com.wenxing.xunyitong.utils.PerformanceManager;
import com.wenxing.xunyitong.utils.AccessibilityManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etPhone, etVerificationCode;
    private TextInputLayout tilUsername, tilPassword, tilPhone, tilVerificationCode;
    private Button btnLogin, btnRegister, btnSendCode, btnSmsLogin;
    private TextView tvForgotPassword;
    private boolean isCodeSent = false;
    private int countdown = 60;
    
    // Loading indicators
    private ProgressBar pbLoginLoading, pbSendCodeLoading;
    
    // SharedPreferences相关
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "user_login_state";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USERNAME = "username";
    private static final String USER_ID = "user_id";
    
    // 安全管理器
    private SecurityManager securityManager;
    
    // 性能监控
    private PerformanceManager.PageLoadMonitor pageLoadMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d("LoginActivity", "onCreate started");
        
        // 启动性能监控
        pageLoadMonitor = new PerformanceManager.PageLoadMonitor("LoginActivity");
        
        // 优化Activity内存使用
        PerformanceManager.optimizeActivityMemory(this);
        
        // 检查Activity状态
        if (isFinishing() || isDestroyed()) {
            Log.w("LoginActivity", "Activity is finishing or destroyed, aborting onCreate");
            return;
        }
        
        setContentView(R.layout.activity_login);
        Log.d("LoginActivity", "Layout set successfully");
        
        // 直接初始化视图，不使用try-catch包装
        initViewsRobust();
        
        // 强制检查按钮初始化状态，确保手机验证码登录按钮能正常工作
        forceSmsLoginButtonSetup();
        
        // 标记页面加载完成
        if (pageLoadMonitor != null) {
            pageLoadMonitor.onPageLoadComplete();
        }
        
        Log.d("LoginActivity", "onCreate completed successfully");
    }
    
    /**
     * 强制检查手机验证码登录按钮的初始化状态，确保其能正常工作
     */
    private void forceSmsLoginButtonSetup() {
        Log.d("LoginActivity", "=== 强制检查SMS登录按钮设置 ===");
        
        try {
            // 直接通过findViewById获取按钮
            Button smsLoginButton = findViewById(R.id.btn_sms_login);
            
            if (smsLoginButton != null) {
                Log.d("LoginActivity", "SMS登录按钮找到了！✓");
                Log.d("LoginActivity", "Visibility: " + 
                    (smsLoginButton.getVisibility() == View.VISIBLE ? "VISIBLE" : 
                     smsLoginButton.getVisibility() == View.GONE ? "GONE" : "INVISIBLE"));
                Log.d("LoginActivity", "Enabled: " + smsLoginButton.isEnabled());
                Log.d("LoginActivity", "Clickable: " + smsLoginButton.isClickable());
                
                // 如果按钮被隐藏或禁用，强制启用它
                if (smsLoginButton.getVisibility() != View.VISIBLE) {
                    Log.w("LoginActivity", "按钮被隐藏，强制显示");
                    smsLoginButton.setVisibility(View.VISIBLE);
                }
                
                if (!smsLoginButton.isEnabled()) {
                    Log.w("LoginActivity", "按钮被禁用，强制启用");
                    smsLoginButton.setEnabled(true);
                }
                
                if (!smsLoginButton.isClickable()) {
                    Log.w("LoginActivity", "按钮不可点击，强制设置为可点击");
                    smsLoginButton.setClickable(true);
                }
                
                // 强制设置点击监听器
                smsLoginButton.setOnClickListener(v -> {
                    Log.d("LoginActivity", "手机验证码登录按钮被点击！");
                    Toast.makeText(LoginActivity.this, "打开手机验证码登录对话框", Toast.LENGTH_SHORT).show();
                    showSmsLoginDialog();
                });
                
                // 更新成员变量
                btnSmsLogin = smsLoginButton;
                
                Log.d("LoginActivity", "SMS登录按钮设置完成！✓");
                
            } else {
                Log.e("LoginActivity", "SMS登录按钮不存在！✗");
                Log.e("LoginActivity", "布局文件中可能缺少 R.id.btn_sms_login");
            }
            
        } catch (Exception e) {
            Log.e("LoginActivity", "SMS登录按钮设置出错: " + e.getMessage(), e);
        }
        
        Log.d("LoginActivity", "=== SMS登录按钮检查完成 ===");
    }
    
    /**
     * 根据activity_login.xml布局文件初始化所有视图组件
     * 包括输入框、按钮、加载指示器等UI元素的初始化和配置
     */
    private void initViewsRobust() {
        Log.d("LoginActivity", "Starting robust view initialization based on activity_login.xml...");
        
        // 初始化SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // 初始化安全管理器
        securityManager = new SecurityManager(this);
        
        // 初始化TextInputLayout容器 - 基于activity_login.xml中实际存在的组件
        tilUsername = findViewById(R.id.til_username);           // 用户名输入框容器
        tilPassword = findViewById(R.id.til_password);           // 密码输入框容器
        
        // 初始化EditText输入框 - 对应XML中的TextInputEditText
        etUsername = findViewById(R.id.et_username);             // 用户名输入框
        etPassword = findViewById(R.id.et_password);             // 密码输入框
        
        // 初始化按钮组件 - 基于XML中的Button定义
        btnLogin = findViewById(R.id.btn_login);                // 主登录按钮
        btnSmsLogin = findViewById(R.id.btn_sms_login);          // 手机验证码登录按钮
        btnRegister = findViewById(R.id.btn_register);          // 注册按钮
        
        // 初始化其他UI组件
        tvForgotPassword = findViewById(R.id.tv_forgot_password); // 忘记密码文本
        
        // 初始化加载指示器
        pbLoginLoading = findViewById(R.id.pb_login_loading);     // 登录加载动画
        
        // 初始化容器组件（可选，用于动画或布局控制）
        CardView logoArea = findViewById(R.id.logo_area);        // Logo区域
        CardView formContainer = findViewById(R.id.form_container); // 表单容器
        RelativeLayout buttonContainer = findViewById(R.id.button_container); // 按钮容器
        
        // 注意：以下组件在activity_login.xml中不存在，设置为null以避免空指针异常
        tilPhone = null;                                      // SMS登录对话框中使用
        tilVerificationCode = null;                           // SMS登录对话框中使用
        etPhone = null;                                       // SMS登录对话框中使用
        etVerificationCode = null;                            // SMS登录对话框中使用
        btnSendCode = null;                                   // SMS登录对话框中使用
        pbSendCodeLoading = null;                             // SMS登录对话框中使用
        
        // 记录关键视图的初始化状态
        logViewInitializationStatus();
        
        // 设置事件监听器 - 确保视图存在后再设置
        setupEventListeners();
        
        // 设置输入验证（只为主要输入框）
        setupMainInputValidation();
        
        // 设置UI动画效果
        setupUIAnimations();
        
        // 处理从其他Activity传来的参数
        handleIntentExtras();
        
        Log.d("LoginActivity", "Robust view initialization completed successfully");
    }
    
    /**
     * 记录视图初始化状态，用于调试
     */
    private void logViewInitializationStatus() {
        Log.d("LoginActivity", "=== View Initialization Status (Based on activity_login.xml) ===");
        
        // 主要输入组件（在布局文件中存在）
        Log.d("LoginActivity", "tilUsername: " + (tilUsername != null ? "✓" : "✗"));
        Log.d("LoginActivity", "tilPassword: " + (tilPassword != null ? "✓" : "✗"));
        Log.d("LoginActivity", "etUsername: " + (etUsername != null ? "✓" : "✗"));
        Log.d("LoginActivity", "etPassword: " + (etPassword != null ? "✓" : "✗"));
        
        // 按钮组件（在布局文件中存在）
        Log.d("LoginActivity", "btnLogin: " + (btnLogin != null ? "✓" : "✗"));
        Log.d("LoginActivity", "btnSmsLogin: " + (btnSmsLogin != null ? "✓ (ID: R.id.btn_sms_login)" : "✗ (ID: R.id.btn_sms_login not found)"));
        Log.d("LoginActivity", "btnRegister: " + (btnRegister != null ? "✓" : "✗"));
        
        // 其他UI组件（在布局文件中存在）
        Log.d("LoginActivity", "tvForgotPassword: " + (tvForgotPassword != null ? "✓" : "✗"));
        Log.d("LoginActivity", "pbLoginLoading: " + (pbLoginLoading != null ? "✓" : "✗"));
        
        // SMS相关组件（预期不在主布局中存在，只在对话框中使用）
        Log.d("LoginActivity", "tilPhone: " + (tilPhone != null ? "✓" : "✗ (Expected - SMS dialog only)"));
        Log.d("LoginActivity", "tilVerificationCode: " + (tilVerificationCode != null ? "✓" : "✗ (Expected - SMS dialog only)"));
        Log.d("LoginActivity", "etPhone: " + (etPhone != null ? "✓" : "✗ (Expected - SMS dialog only)"));
        Log.d("LoginActivity", "etVerificationCode: " + (etVerificationCode != null ? "✓" : "✗ (Expected - SMS dialog only)"));
        Log.d("LoginActivity", "btnSendCode: " + (btnSendCode != null ? "✓" : "✗ (Expected - SMS dialog only)"));
        Log.d("LoginActivity", "pbSendCodeLoading: " + (pbSendCodeLoading != null ? "✓" : "✗ (Expected - SMS dialog only)"));
        
        // 验证布局文件中的按钮是否可用
        try {
            View smsLoginView = findViewById(R.id.btn_sms_login);
            Log.d("LoginActivity", "Direct findViewById for btn_sms_login: " + (smsLoginView != null ? "✓" : "✗"));
            if (smsLoginView != null) {
                Log.d("LoginActivity", "btn_sms_login visibility: " + 
                    (smsLoginView.getVisibility() == View.VISIBLE ? "VISIBLE" : 
                     smsLoginView.getVisibility() == View.GONE ? "GONE" : "INVISIBLE"));
                Log.d("LoginActivity", "btn_sms_login enabled: " + smsLoginView.isEnabled());
            }
        } catch (Exception e) {
            Log.e("LoginActivity", "Error checking btn_sms_login directly: " + e.getMessage());
        }
        
        Log.d("LoginActivity", "=================================");
    }
    
    /**
     * 设置所有事件监听器
     */
    private void setupEventListeners() {
        // 设置按钮点击监听器 - 每个按钮单独检查，不要求全部存在
        setupClickListeners();
        
        // 设置忘记密码点击事件
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
        }
    }
    
    /**
     * 设置输入验证逻辑
     */
    private void setupInputValidation() {
        // 主要输入框验证（用户名和密码）
        if (etUsername != null && etPassword != null) {
            setupMainInputValidation();
        }
        
        // 设置输入框聚焦动画
        setupInputFocusAnimations();
        
        // 注意：SMS相关输入框在主布局中不存在，只在对话框中使用
        // 所以不需要在这里设置 SMS 输入验证
    }
    
    /**
     * 设置UI动画效果
     */
    private void setupUIAnimations() {
        try {
            setupInputFocusAnimations();
        } catch (Exception e) {
            Log.w("LoginActivity", "Failed to setup UI animations, continuing without them", e);
        }
    }
    
    /**
     * 处理忘记密码点击事件
     */
    private void handleForgotPassword() {
        // TODO: 实现忘记密码功能
        Toast.makeText(this, "忘记密码功能开发中...", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 设置主要输入框（用户名和密码）的验证逻辑
     */
    private void setupMainInputValidation() {
        // 用户名输入框验证
        if (etUsername != null) {
            etUsername.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // 清除之前的错误提示
                    if (tilUsername != null) {
                        UXEnhancementUtils.clearError(tilUsername);
                    }
                }
                
                @Override
                public void afterTextChanged(Editable s) {
                    String username = s.toString().trim();
                    if (!TextUtils.isEmpty(username)) {
                        validateUsername(username);
                    }
                }
            });
        }
        
        // 密码输入框验证
        if (etPassword != null) {
            etPassword.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // 清除之前的错误提示
                    if (tilPassword != null) {
                        UXEnhancementUtils.clearError(tilPassword);
                    }
                }
                
                @Override
                public void afterTextChanged(Editable s) {
                    String password = s.toString().trim();
                    if (!TextUtils.isEmpty(password)) {
                        validatePassword(password);
                    }
                }
            });
        }
        
        Log.d("LoginActivity", "Main input validation setup completed");
    }
    
    /**
     * 设置SMS登录相关输入框的验证逻辑
     */
    private void setupSmsInputValidation() {
        // 手机号输入框验证
        if (etPhone != null) {
            etPhone.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // 清除之前的错误提示
                    if (tilPhone != null) {
                        tilPhone.setError(null);
                    }
                }
                
                @Override
                public void afterTextChanged(Editable s) {
                    String phone = s.toString().trim();
                    if (!TextUtils.isEmpty(phone)) {
                        validatePhone(phone);
                    }
                }
            });
        }
        
        // 验证码输入框验证
        if (etVerificationCode != null) {
            etVerificationCode.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // 清除之前的错误提示
                    if (tilVerificationCode != null) {
                        tilVerificationCode.setError(null);
                    }
                }
                
                @Override
                public void afterTextChanged(Editable s) {
                    String code = s.toString().trim();
                    if (!TextUtils.isEmpty(code)) {
                        validateVerificationCode(code);
                    }
                }
            });
        }
        
        Log.d("LoginActivity", "SMS input validation setup completed");
    }
    
    /**
     * 处理从其他Activity传来的Intent参数
     */
    private void handleIntentExtras() {
        try {
            Intent intent = getIntent();
            Log.d("LoginActivity", "开始处理Intent参数, intent: " + (intent != null ? "存在" : "为空"));
            
            if (intent != null) {
                // 获取从注册页面传来的用户名
                String username = intent.getStringExtra("username");
                String phone = intent.getStringExtra("phone");
                
                Log.d("LoginActivity", "Intent参数 - username: " + username + ", phone: " + phone);
                Log.d("LoginActivity", "输入框状态 - etUsername: " + (etUsername != null ? "存在" : "为空"));
                
                if (!TextUtils.isEmpty(username)) {
                    if (etUsername != null) {
                        etUsername.setText(username);
                        etUsername.setEnabled(true);
                        etUsername.setFocusable(true);
                        etUsername.setFocusableInTouchMode(true);
                        Log.d("LoginActivity", "成功设置用户名: " + username);
                    } else {
                        Log.w("LoginActivity", "etUsername为空，无法设置用户名: " + username);
                    }
                } else {
                    Log.d("LoginActivity", "用户名参数为空或null");
                }
                
                // 注意：手机号参数在主布局中没有对应的输入框
                // 只在SMS对话框中才有etPhone，所以这里只记录日志
                if (!TextUtils.isEmpty(phone)) {
                    Log.d("LoginActivity", "手机号参数: " + phone + " (将用于SMS登录对话框)");
                } else {
                    Log.d("LoginActivity", "手机号参数为空或null");
                }
                
                // 确保所有输入框都可以输入
                enableAllInputFields();
            } else {
                Log.d("LoginActivity", "Intent为空，没有参数需要处理");
            }
        } catch (Exception e) {
            Log.e("LoginActivity", "处理Intent参数时出错", e);
        }
    }
    
    /**
     * 确保所有输入框都可以正常输入
     */
    private void enableAllInputFields() {
        try {
            if (etUsername != null) {
                etUsername.setEnabled(true);
                etUsername.setFocusable(true);
                etUsername.setFocusableInTouchMode(true);
            }
            
            if (etPassword != null) {
                etPassword.setEnabled(true);
                etPassword.setFocusable(true);
                etPassword.setFocusableInTouchMode(true);
            }
            
            // 注意：etPhone 和 etVerificationCode 在主布局中不存在，
            // 只在SMS登录对话框中使用，所以这里不需要处理
            
            Log.d("LoginActivity", "所有主要输入框已启用");
        } catch (Exception e) {
            Log.w("LoginActivity", "启用输入框时出错", e);
        }
    }
    
    /**
     * 设置输入框聚焦动画
     */
    private void setupInputFocusAnimations() {
        // 用户名输入框聚焦动画
        if (etUsername != null && tilUsername != null) {
            etUsername.setOnFocusChangeListener((v, hasFocus) -> {
                UXEnhancementUtils.handleInputFocusAnimation(tilUsername, hasFocus);
                if (hasFocus) {
                    UXEnhancementUtils.setInputBackgroundState(etUsername, "focused");
                } else {
                    UXEnhancementUtils.setInputBackgroundState(etUsername, "normal");
                }
            });
        }
        
        // 密码输入框聚焦动画
        if (etPassword != null && tilPassword != null) {
            etPassword.setOnFocusChangeListener((v, hasFocus) -> {
                UXEnhancementUtils.handleInputFocusAnimation(tilPassword, hasFocus);
                if (hasFocus) {
                    UXEnhancementUtils.setInputBackgroundState(etPassword, "focused");
                } else {
                    UXEnhancementUtils.setInputBackgroundState(etPassword, "normal");
                }
            });
        }
    }
    
    private void logInitializationError(Exception e) {
        Log.e("LoginActivity", "Initialization error occurred, but continuing with available views", e);
        // 不再显示错误对话框或简化界面，让Activity正常运行
    }
    
    // 已移除简化登录功能 - 不再需要
    /*
    private void showSimplifiedLogin() {
        try {
            Log.d("LoginActivity", "Showing simplified login interface");
            
            // 隐藏所有可能有问题的视图
            hideProblematicViews();
            
            // 创建简化的登录界面
            createSimplifiedLoginInterface();
            
            Toast.makeText(this, "已切换到简化登录模式", Toast.LENGTH_LONG).show();
            
        } catch (Exception e) {
            Log.e("LoginActivity", "Failed to show simplified login", e);
            Toast.makeText(this, "无法显示登录界面，请重启应用", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    */
    
    /*
    private void hideProblematicViews() {
        // 隐藏可能有问题的复杂视图
        try {
            View formContainer = findViewById(R.id.form_container);
            if (formContainer != null) {
                formContainer.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.w("LoginActivity", "Could not hide form container", e);
        }
    }
    */
    
    /*
    // 已移除简化登录界面相关方法 - 不再需要
    private void createSimplifiedLoginInterface() {
        // 创建一个简化的登录界面
        try {
            // 查找根布局
            View rootView = findViewById(android.R.id.content);
            if (rootView instanceof ViewGroup) {
                ViewGroup rootGroup = (ViewGroup) rootView;
                
                // 创建简化的登录表单
                LinearLayout simplifiedForm = new LinearLayout(this);
                simplifiedForm.setOrientation(LinearLayout.VERTICAL);
                simplifiedForm.setPadding(48, 48, 48, 48);
                simplifiedForm.setGravity(android.view.Gravity.CENTER);
                
                // 添加标题
                TextView title = new TextView(this);
                title.setText("AI医疗助手 - 登录");
                title.setTextSize(24);
                title.setTextColor(getResources().getColor(android.R.color.white));
                title.setGravity(android.view.Gravity.CENTER);
                title.setPadding(0, 0, 0, 32);
                simplifiedForm.addView(title);
                
                // 添加用户名输入框
                EditText simpleUsername = new EditText(this);
                simpleUsername.setHint("用户名");
                simpleUsername.setTextColor(getResources().getColor(android.R.color.black));
                simpleUsername.setBackgroundColor(getResources().getColor(android.R.color.white));
                simpleUsername.setPadding(16, 16, 16, 16);
                LinearLayout.LayoutParams usernameParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                usernameParams.setMargins(0, 0, 0, 16);
                simpleUsername.setLayoutParams(usernameParams);
                simplifiedForm.addView(simpleUsername);
                
                // 添加密码输入框
                EditText simplePassword = new EditText(this);
                simplePassword.setHint("密码");
                simplePassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                simplePassword.setTextColor(getResources().getColor(android.R.color.black));
                simplePassword.setBackgroundColor(getResources().getColor(android.R.color.white));
                simplePassword.setPadding(16, 16, 16, 16);
                LinearLayout.LayoutParams passwordParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                passwordParams.setMargins(0, 0, 0, 24);
                simplePassword.setLayoutParams(passwordParams);
                simplifiedForm.addView(simplePassword);
                
                // 添加登录按钮
                Button simpleLoginBtn = new Button(this);
                simpleLoginBtn.setText("登录");
                simpleLoginBtn.setTextColor(getResources().getColor(android.R.color.white));
                simpleLoginBtn.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
                LinearLayout.LayoutParams loginBtnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                loginBtnParams.setMargins(0, 0, 0, 16);
                simpleLoginBtn.setLayoutParams(loginBtnParams);
                simpleLoginBtn.setOnClickListener(v -> {
                    String username = simpleUsername.getText().toString().trim();
                    String password = simplePassword.getText().toString().trim();
                    performSimpleLogin(username, password);
                });
                simplifiedForm.addView(simpleLoginBtn);
                
                // 添加返回按钮
                Button backBtn = new Button(this);
                backBtn.setText("返回");
                backBtn.setTextColor(getResources().getColor(android.R.color.white));
                backBtn.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                backBtn.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                backBtn.setOnClickListener(v -> finish());
                simplifiedForm.addView(backBtn);
                
                // 添加到根布局
                rootGroup.addView(simplifiedForm);
                
                Log.d("LoginActivity", "Simplified login interface created successfully");
            }
        } catch (Exception e) {
            Log.e("LoginActivity", "Failed to create simplified interface", e);
            throw e;
        }
    }
    
    private void performSimpleLogin(String username, String password) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 简化的登录逻辑
        Toast.makeText(this, "登录功能开发中...", Toast.LENGTH_SHORT).show();
        
        // 模拟登录成功，返回主界面
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            // 保存登录状态和用户ID
            saveLoginState(true, username);
            
            // 在实际应用中，这里应该从API响应中获取真实的用户ID
            // 这里使用1作为模拟的用户ID
            int simulatedUserId = 1;
            
            // 保存用户ID
            sharedPreferences.edit()
                    .putInt("user_id", simulatedUserId)
                    .apply();
            
            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
            finish();
        }, 1000);
    }
    */
    
    private void initViews() {
        try {
            Log.d("LoginActivity", "Starting view initialization...");
            
            // 验证布局是否正确加载
            View rootView = findViewById(android.R.id.content);
            if (rootView == null) {
                throw new RuntimeException("Root view not found - layout may not be loaded");
            }
            Log.d("LoginActivity", "Root view found successfully");
            
            // 尝试查找一个简单的视图来验证布局加载
            View logoArea = findViewById(R.id.logo_area);
            if (logoArea == null) {
                Log.e("LoginActivity", "Logo area not found - layout loading failed");
                throw new RuntimeException("Layout loading failed - logo_area not found");
            }
            Log.d("LoginActivity", "Logo area found - layout loaded successfully");
            
            // 初始化输入框容器（先初始化容器）
            Log.d("LoginActivity", "Initializing TextInputLayouts...");
            tilUsername = findViewById(R.id.til_username);
            tilPassword = findViewById(R.id.til_password);
            tilPhone = findViewById(R.id.til_phone);
            tilVerificationCode = findViewById(R.id.til_verification_code);
            
            // 详细检查每个TextInputLayout
            if (tilUsername == null) {
                Log.e("LoginActivity", "til_username not found in layout");
                // 尝试列出所有可用的视图ID进行调试
                logAvailableViewIds();
                throw new RuntimeException("Username TextInputLayout not found - ID: til_username");
            }
            Log.d("LoginActivity", "til_username found successfully");
            
            if (tilPassword == null) {
                Log.e("LoginActivity", "til_password not found in layout");
                throw new RuntimeException("Password TextInputLayout not found - ID: til_password");
            }
            Log.d("LoginActivity", "til_password found successfully");
            
            // 初始化输入框
            Log.d("LoginActivity", "Initializing EditTexts...");
            etUsername = findViewById(R.id.et_username);
            etPassword = findViewById(R.id.et_password);
            etPhone = findViewById(R.id.et_phone);
            etVerificationCode = findViewById(R.id.et_verification_code);
            
            // 检查关键输入框是否找到
            if (etUsername == null) {
                Log.e("LoginActivity", "et_username not found in layout");
                throw new RuntimeException("Username EditText not found - ID: et_username");
            }
            Log.d("LoginActivity", "et_username found successfully");
            
            if (etPassword == null) {
                Log.e("LoginActivity", "et_password not found in layout");
                throw new RuntimeException("Password EditText not found - ID: et_password");
            }
            Log.d("LoginActivity", "et_password found successfully");
            
            // 初始化按钮
            btnLogin = findViewById(R.id.btn_login);
            btnRegister = findViewById(R.id.btn_register);
            btnSendCode = findViewById(R.id.btn_send_code);
            btnSmsLogin = findViewById(R.id.btn_sms_login);
            tvForgotPassword = findViewById(R.id.tv_forgot_password);
            
            // 检查关键按钮是否找到
            if (btnLogin == null) {
                throw new RuntimeException("Login button not found");
            }
            
            if (btnSmsLogin == null) {
                Log.e("LoginActivity", "btn_sms_login not found in layout");
                throw new RuntimeException("SMS Login button not found - ID: btn_sms_login");
            }
            Log.d("LoginActivity", "btn_sms_login found successfully");
            
            // Initialize loading indicators
            pbLoginLoading = findViewById(R.id.pb_login_loading);
            pbSendCodeLoading = findViewById(R.id.pb_send_code_loading);
            
            setupInputValidation();
            setupInputFocusAnimations();
            
            Log.d("LoginActivity", "All views initialized successfully");
            
        } catch (Exception e) {
            Log.e("LoginActivity", "Error initializing views", e);
            throw e; // 重新抛出异常，让onCreate处理
        }
    }
    

    
    private void setupClickListeners() {
        Log.d("LoginActivity", "=== Setting up click listeners ===");
        
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> performLogin());
            Log.d("LoginActivity", "btnLogin click listener set ✓");
        } else {
            Log.w("LoginActivity", "btnLogin is null, skipping click listener ✗");
        }
        
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> {
                // 跳转到注册页面
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                // 添加界面切换动画
                overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out);
            });
            Log.d("LoginActivity", "btnRegister click listener set ✓");
        } else {
            Log.w("LoginActivity", "btnRegister is null, skipping click listener ✗");
        }
        
        if (btnSendCode != null) {
            btnSendCode.setOnClickListener(v -> sendVerificationCode());
            Log.d("LoginActivity", "btnSendCode click listener set ✓");
        } else {
            Log.d("LoginActivity", "btnSendCode is null (normal - dialog button) ✓");
        }
        
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                Toast.makeText(this, "忘记密码功能开发中...", Toast.LENGTH_SHORT).show();
            });
            Log.d("LoginActivity", "tvForgotPassword click listener set ✓");
        } else {
            Log.w("LoginActivity", "tvForgotPassword is null, skipping click listener ✗");
        }
        
        if (btnSmsLogin != null) {
            btnSmsLogin.setOnClickListener(v -> {
                Log.d("LoginActivity", "手机验证码登录按钮被点击");
                showSmsLoginDialog();
            });
            Log.d("LoginActivity", "btnSmsLogin click listener set ✓");
        } else {
            Log.e("LoginActivity", "btnSmsLogin is null, click listener NOT set ✗");
        }
        
        Log.d("LoginActivity", "=== Click listeners setup completed ===");
    }
    
    private void performLogin() {
        // 安全获取输入内容
        String username = (etUsername != null) ? etUsername.getText().toString().trim() : "";
        String password = (etPassword != null) ? etPassword.getText().toString().trim() : "";
        String phone = (etPhone != null) ? etPhone.getText().toString().trim() : "";
        String verificationCode = (etVerificationCode != null) ? etVerificationCode.getText().toString().trim() : "";
        
        // 检查是否使用手机验证码登录
        if (!TextUtils.isEmpty(phone) && !TextUtils.isEmpty(verificationCode)) {
            performPhoneLogin(phone, verificationCode);
            return;
        }
        
        // 检查登录安全性
        SecurityManager.LoginAttemptResult attemptResult = securityManager.checkLoginAttempt();
        if (!attemptResult.isAllowed()) {
            UXEnhancementUtils.showEnhancedError(tilUsername, attemptResult.getMessage(), btnLogin);
            UXEnhancementUtils.showEnhancedToast(LoginActivity.this, attemptResult.getMessage(), "error");
            return;
        }
        
        // 传统用户名密码登录
        if (TextUtils.isEmpty(username)) {
            if (tilUsername != null) {
                UXEnhancementUtils.showEnhancedError(tilUsername, "请输入用户名", etUsername);
            }
            if (etUsername != null) {
                etUsername.requestFocus();
            }
            return;
        }
        
        if (!validateUsername(username)) {
            if (etUsername != null) {
                etUsername.requestFocus();
            }
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            if (tilPassword != null) {
                UXEnhancementUtils.showEnhancedError(tilPassword, "请输入密码", etPassword);
            }
            if (etPassword != null) {
                etPassword.requestFocus();
            }
            return;
        }
        
        if (!validatePassword(password)) {
            if (etPassword != null) {
                etPassword.requestFocus();
            }
            return;
        }
        
        // 显示加载状态
        showLoginLoading(true);
        
        // 调用后端API进行登录验证
        ApiService apiService = ApiClient.getApiService();
        LoginRequest loginRequest = new LoginRequest(username, password);
        Call<ApiResponse<LoginResponse>> call = apiService.loginUser(loginRequest);
        
        call.enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                // 隐藏加载状态
                showLoginLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<LoginResponse> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        LoginResponse loginData = apiResponse.getData();
                        
                        // 记录登录成功
                        securityManager.recordLoginSuccess();
                        
                        // 保存登录状态和用户信息
                        saveLoginState(true, loginData.getUsername());
                        saveUserInfo(loginData);
                        
                        // 显示成功反馈
                        UXEnhancementUtils.showSuccessFeedback(tilUsername, btnLogin);
                        UXEnhancementUtils.showEnhancedToast(LoginActivity.this, "登录成功！", "success");
                        
                        // 延迟跳转，让用户看到成功动画
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }, 1000);
                    } else {
                        // 记录登录失败
                        securityManager.recordLoginFailure();
                        
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "登录失败";
                        
                        // 检查是否需要显示剩余尝试次数
                        int remainingAttempts = securityManager.checkLoginAttempt().getRemainingAttempts();
                        if (remainingAttempts > 0 && remainingAttempts <= 3) {
                            errorMsg += "（剩余" + remainingAttempts + "次尝试）";
                        }
                        
                        UXEnhancementUtils.showEnhancedError(tilPassword, errorMsg, btnLogin);
                        UXEnhancementUtils.showEnhancedToast(LoginActivity.this, errorMsg, "error");
                    }
                } else {
                    ErrorHandlingManager.ErrorInfo errorInfo = ErrorHandlingManager.handleNetworkError(
                        LoginActivity.this, new Exception("HTTP " + response.code()));
                    UXEnhancementUtils.showEnhancedToast(LoginActivity.this, 
                        errorInfo.getUserMessage(), "error");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                // 隐藏加载状态
                showLoginLoading(false);
                
                // 使用错误处理管理器处理错误
                ErrorHandlingManager.ErrorInfo errorInfo = ErrorHandlingManager.handleNetworkError(
                    LoginActivity.this, t);
                ErrorHandlingManager.logError(errorInfo, "登录请求失败");
                
                // 显示用户友好的错误消息
                String userMessage = ErrorHandlingManager.formatUserFriendlyMessage(errorInfo);
                UXEnhancementUtils.showEnhancedToast(LoginActivity.this, userMessage, "error");
                
                // 如果是网络错误，显示重试提示
                if (errorInfo.getType() == ErrorHandlingManager.ErrorType.NETWORK_ERROR) {
                    UXEnhancementUtils.showEnhancedError(tilUsername, 
                        "网络连接问题，请检查后重试", btnLogin);
                }
            }
        });
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
            // 保存登录状态，使用手机号作为用户名
            saveLoginState(true, phone);
            
            // 在实际应用中，这里应该从API响应中获取真实的用户ID
            // 这里使用1作为模拟的用户ID
            int simulatedUserId = 1;
            
            // 保存用户信息
            sharedPreferences.edit()
                    .putInt("user_id", simulatedUserId)
                    .apply();
            
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
        try {
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
        } catch (Exception e) {
            Log.e("LoginActivity", "Setup animations error: " + e.getMessage());
        }
        
        // 为按钮添加点击动画
        setupButtonAnimations();
    }
    
    private void setupButtonAnimations() {
        // 登录按钮点击动画
        if (btnLogin != null) {
            btnLogin.setOnTouchListener((v, event) -> {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    try {
                        Animation scaleDown = AnimationUtils.loadAnimation(this, R.anim.button_scale);
                        v.startAnimation(scaleDown);
                    } catch (Exception e) {
                        Log.e("LoginActivity", "Button animation error: " + e.getMessage());
                    }
                }
                return false; // 让其他点击事件继续处理
            });
        }
        
        // 发送验证码按钮点击动画
        if (btnSendCode != null) {
            btnSendCode.setOnTouchListener((v, event) -> {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    try {
                        Animation scaleDown = AnimationUtils.loadAnimation(this, R.anim.button_scale);
                        v.startAnimation(scaleDown);
                    } catch (Exception e) {
                        Log.e("LoginActivity", "Button animation error: " + e.getMessage());
                    }
                }
                return false;
            });
        }
        
        // 注册按钮点击动画
        if (btnRegister != null) {
            btnRegister.setOnTouchListener((v, event) -> {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    try {
                        Animation scaleDown = AnimationUtils.loadAnimation(this, R.anim.button_scale);
                        v.startAnimation(scaleDown);
                    } catch (Exception e) {
                        Log.e("LoginActivity", "Button animation error: " + e.getMessage());
                    }
                }
                return false;
            });
        }
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
    
    // 验证方法 - 使用新的ValidationManager
    private boolean validateUsername(String username) {
        if (TextUtils.isEmpty(username)) {
            return true; // 空值在其他地方处理
        }
        
        ValidationManager.ValidationResult result = ValidationManager.validateUsername(username);
        if (!result.isValid()) {
            UXEnhancementUtils.showEnhancedError(tilUsername, result.getErrorMessage(), etUsername);
            return false;
        }
        
        UXEnhancementUtils.clearError(tilUsername);
        return true;
    }
    
    private boolean validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            return true; // 空值在其他地方处理
        }
        
        ValidationManager.ValidationResult result = ValidationManager.validatePassword(password);
        if (!result.isValid()) {
            UXEnhancementUtils.showEnhancedError(tilPassword, result.getErrorMessage(), etPassword);
            return false;
        }
        
        UXEnhancementUtils.clearError(tilPassword);
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
            if (pbLoginLoading != null) {
                pbLoginLoading.setVisibility(View.VISIBLE);
            }
            if (btnLogin != null) {
                UXEnhancementUtils.setButtonLoadingState(btnLogin, true, "登录");
            }
        } else {
            if (pbLoginLoading != null) {
                pbLoginLoading.setVisibility(View.GONE);
            }
            if (btnLogin != null) {
                UXEnhancementUtils.setButtonLoadingState(btnLogin, false, "登录");
            }
        }
    }
    
    private void logMemoryUsage(String tag) {
        try {
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            long maxMemory = runtime.maxMemory();
            Log.d("LoginActivity", tag + " - Memory usage: " + (usedMemory / 1024 / 1024) + "MB / " + (maxMemory / 1024 / 1024) + "MB");
        } catch (Exception e) {
            Log.e("LoginActivity", "Error logging memory usage", e);
        }
    }
    
    private void logAvailableViewIds() {
        try {
            Log.d("LoginActivity-Debug", "Attempting to find available views for debugging...");
            
            // 尝试查找一些常见的视图来确认布局是否加载
            String[] commonIds = {
                "logo_area", "form_container", "button_container",
                "til_username", "til_password", "til_phone", "til_verification_code",
                "et_username", "et_password", "et_phone", "et_verification_code",
                "btn_login", "btn_register", "btn_send_code"
            };
            
            for (String idName : commonIds) {
                try {
                    int resId = getResources().getIdentifier(idName, "id", getPackageName());
                    if (resId != 0) {
                        View view = findViewById(resId);
                        Log.d("LoginActivity-Debug", String.format(
                            "ID: %s, ResId: %d, View: %s",
                            idName,
                            resId,
                            view != null ? view.getClass().getSimpleName() : "null"
                        ));
                    } else {
                        Log.d("LoginActivity-Debug", "ID not found in resources: " + idName);
                    }
                } catch (Exception e) {
                    Log.e("LoginActivity-Debug", "Error checking ID: " + idName, e);
                }
            }
            
        } catch (Exception e) {
             Log.e("LoginActivity-Debug", "Error in logAvailableViewIds", e);
         }
     }
     
     private void initViewsFallback() {
         Log.d("LoginActivity", "Starting fallback view initialization...");
         
         try {
             // 使用更加健壮的方法查找视图
             // 首先确保Activity和布局状态正常
             if (isFinishing() || isDestroyed()) {
                 throw new RuntimeException("Activity is finishing or destroyed");
             }
             
             // 使用getWindow().getDecorView()来确保布局已加载
             View decorView = getWindow().getDecorView();
             if (decorView == null) {
                 throw new RuntimeException("DecorView is null");
             }
             
             // 直接执行初始化，不使用异步
             initViewsFallbackInternal();
             
         } catch (Exception e) {
             Log.e("LoginActivity", "Error in fallback initialization", e);
             throw e;
         }
     }
     
     private void initViewsFallbackInternal() {
         Log.d("LoginActivity", "Executing fallback internal initialization...");
         
         // 使用资源ID直接查找，而不依赖R.id常量
         try {
             // 获取包名用于资源查找
             String packageName = getPackageName();
             
             // 查找TextInputLayout
             int tilUsernameId = getResources().getIdentifier("til_username", "id", packageName);
             int tilPasswordId = getResources().getIdentifier("til_password", "id", packageName);
             int tilPhoneId = getResources().getIdentifier("til_phone", "id", packageName);
             int tilVerificationCodeId = getResources().getIdentifier("til_verification_code", "id", packageName);
             
             if (tilUsernameId == 0) {
                 throw new RuntimeException("Cannot find til_username resource ID");
             }
             if (tilPasswordId == 0) {
                 throw new RuntimeException("Cannot find til_password resource ID");
             }
             
             tilUsername = findViewById(tilUsernameId);
             tilPassword = findViewById(tilPasswordId);
             tilPhone = findViewById(tilPhoneId);
             tilVerificationCode = findViewById(tilVerificationCodeId);
             
             if (tilUsername == null) {
                 throw new RuntimeException("til_username view is null even with valid resource ID");
             }
             if (tilPassword == null) {
                 throw new RuntimeException("til_password view is null even with valid resource ID");
             }
             
             Log.d("LoginActivity", "TextInputLayouts found successfully in fallback method");
             
             // 查找EditText
             int etUsernameId = getResources().getIdentifier("et_username", "id", packageName);
             int etPasswordId = getResources().getIdentifier("et_password", "id", packageName);
             int etPhoneId = getResources().getIdentifier("et_phone", "id", packageName);
             int etVerificationCodeId = getResources().getIdentifier("et_verification_code", "id", packageName);
             
             if (etUsernameId == 0) {
                 throw new RuntimeException("Cannot find et_username resource ID");
             }
             if (etPasswordId == 0) {
                 throw new RuntimeException("Cannot find et_password resource ID");
             }
             
             etUsername = findViewById(etUsernameId);
             etPassword = findViewById(etPasswordId);
             etPhone = findViewById(etPhoneId);
             etVerificationCode = findViewById(etVerificationCodeId);
             
             if (etUsername == null) {
                 throw new RuntimeException("et_username view is null even with valid resource ID");
             }
             if (etPassword == null) {
                 throw new RuntimeException("et_password view is null even with valid resource ID");
             }
             
             Log.d("LoginActivity", "EditTexts found successfully in fallback method");
             
             // 查找按钮
             int btnLoginId = getResources().getIdentifier("btn_login", "id", packageName);
             int btnRegisterId = getResources().getIdentifier("btn_register", "id", packageName);
             int btnSendCodeId = getResources().getIdentifier("btn_send_code", "id", packageName);
             int tvForgotPasswordId = getResources().getIdentifier("tv_forgot_password", "id", packageName);
             
             btnLogin = findViewById(btnLoginId);
             btnRegister = findViewById(btnRegisterId);
             btnSendCode = findViewById(btnSendCodeId);
             tvForgotPassword = findViewById(tvForgotPasswordId);
             
             if (btnLogin == null) {
                 throw new RuntimeException("btn_login view is null");
             }
             
             Log.d("LoginActivity", "Buttons found successfully in fallback method");
             
             // 查找加载指示器
             int pbLoginLoadingId = getResources().getIdentifier("pb_login_loading", "id", packageName);
             int pbSendCodeLoadingId = getResources().getIdentifier("pb_send_code_loading", "id", packageName);
             
             pbLoginLoading = findViewById(pbLoginLoadingId);
             pbSendCodeLoading = findViewById(pbSendCodeLoadingId);
             
             // 设置输入验证
             setupInputValidation();
             setupInputFocusAnimations();
             
             Log.d("LoginActivity", "Fallback view initialization completed successfully");
             
         } catch (Exception e) {
             Log.e("LoginActivity", "Error in fallback internal initialization", e);
             throw e;
         }
     }
    
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("LoginActivity", "onStart called");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("LoginActivity", "onResume called");
        
        // 确保所有输入框都可以正常输入
        enableAllInputFields();
        
        logMemoryUsage("onResume");
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("LoginActivity", "onPause called");
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        Log.d("LoginActivity", "onStop called");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("LoginActivity", "onDestroy called");
        logMemoryUsage("onDestroy");
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            // 保存当前状态
            if (etUsername != null) {
                outState.putString("username", etUsername.getText().toString());
            }
            if (etPhone != null) {
                outState.putString("phone", etPhone.getText().toString());
            }
            outState.putBoolean("isCodeSent", isCodeSent);
            outState.putInt("countdown", countdown);
            Log.d("LoginActivity", "State saved successfully");
        } catch (Exception e) {
            Log.e("LoginActivity", "Error saving instance state", e);
        }
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        try {
            // 恢复保存的状态
            if (savedInstanceState != null) {
                String username = savedInstanceState.getString("username", "");
                String phone = savedInstanceState.getString("phone", "");
                isCodeSent = savedInstanceState.getBoolean("isCodeSent", false);
                countdown = savedInstanceState.getInt("countdown", 60);
                
                if (etUsername != null && !TextUtils.isEmpty(username)) {
                    etUsername.setText(username);
                }
                if (etPhone != null && !TextUtils.isEmpty(phone)) {
                    etPhone.setText(phone);
                }
                
                // 如果验证码已发送且还在倒计时中，恢复倒计时
                if (isCodeSent && countdown > 0 && countdown < 60) {
                    startCountdown();
                }
                
                Log.d("LoginActivity", "State restored successfully");
            }
        } catch (Exception e) {
            Log.e("LoginActivity", "Error restoring instance state", e);
        }
    }
    
    /**
     * 保存登录状态到SharedPreferences
     */
    private void saveLoginState(boolean isLoggedIn, String username) {
        if (sharedPreferences != null) {
            sharedPreferences.edit()
                    .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
                    .putString(KEY_USERNAME, username)
                    .apply();
            Log.d("LoginActivity", "登录状态已保存: " + isLoggedIn + ", 用户名: " + username);
        }
    }
    
    /**
     * 保存用户详细信息到SharedPreferences
     */
    private void saveUserInfo(LoginResponse loginData) {
        if (sharedPreferences != null && loginData != null) {
            sharedPreferences.edit()
                    .putInt("user_id", loginData.getUserId())
                    .putString("email", loginData.getEmail())
                    .putString("full_name", loginData.getFullName())
                    .putString("phone", loginData.getPhone())
                    .putString("avatar_url", loginData.getAvatarUrl())
                    .putString("access_token", loginData.getAccessToken())
                    .putString("token_type", loginData.getTokenType())
                    .apply();
            Log.d("LoginActivity", "Token已保存: " + loginData.getAccessToken());
        }
    }
    
    /**
     * 显示手机验证码登录对话框
     */
    private void showSmsLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sms_login, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        
        // 获取对话框中的控件
        EditText etDialogPhone = dialogView.findViewById(R.id.et_phone_dialog);
        EditText etDialogCode = dialogView.findViewById(R.id.et_verification_code_dialog);
        TextInputLayout tilDialogCode = dialogView.findViewById(R.id.til_verification_code_dialog);
        LinearLayout layoutSendCodeContainer = dialogView.findViewById(R.id.layout_send_code_container);
        TextView tvSendCodeHint = dialogView.findViewById(R.id.tv_send_code_hint);
        Button btnDialogSendCode = dialogView.findViewById(R.id.btn_send_code_dialog);
        Button btnDialogLogin = dialogView.findViewById(R.id.btn_login_dialog);
        Button btnDialogCancel = dialogView.findViewById(R.id.btn_cancel_dialog);
        
        // 移除验证码输入框的焦点和点击监听器功能
        
        // 验证码输入框右侧图标点击事件
        tilDialogCode.setEndIconOnClickListener(v -> {
            String phone = etDialogPhone.getText().toString().trim();
            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "请先输入手机号码", Toast.LENGTH_SHORT).show();
                etDialogPhone.requestFocus();
                return;
            }
            if (!isValidPhoneNumber(phone)) {
                Toast.makeText(this, "请输入有效的手机号码", Toast.LENGTH_SHORT).show();
                etDialogPhone.requestFocus();
                return;
            }
            
            // 模拟发送验证码
            simulateSendSMS(phone);
            Toast.makeText(this, "验证码已发送到 " + phone, Toast.LENGTH_SHORT).show();
            
            // 开始倒计时
            startDialogCountdown(btnDialogSendCode);
        });
        
        // 发送验证码文字提示点击事件
        tvSendCodeHint.setOnClickListener(v -> {
            String phone = etDialogPhone.getText().toString().trim();
            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "请先输入手机号码", Toast.LENGTH_SHORT).show();
                etDialogPhone.requestFocus();
                return;
            }
            if (!isValidPhoneNumber(phone)) {
                Toast.makeText(this, "请输入有效的手机号码", Toast.LENGTH_SHORT).show();
                etDialogPhone.requestFocus();
                return;
            }
            
            // 模拟发送验证码
            simulateSendSMS(phone);
            Toast.makeText(this, "验证码已发送到 " + phone, Toast.LENGTH_SHORT).show();
            
            // 开始倒计时
            startDialogCountdown(btnDialogSendCode);
        });
        
        // 发送验证码按钮点击事件
        btnDialogSendCode.setOnClickListener(v -> {
            String phone = etDialogPhone.getText().toString().trim();
            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "请先输入手机号码", Toast.LENGTH_SHORT).show();
                etDialogPhone.requestFocus();
                return;
            }
            if (!isValidPhoneNumber(phone)) {
                Toast.makeText(this, "请输入有效的手机号码", Toast.LENGTH_SHORT).show();
                etDialogPhone.requestFocus();
                return;
            }
            
            // 模拟发送验证码
            simulateSendSMS(phone);
            Toast.makeText(this, "验证码已发送到 " + phone, Toast.LENGTH_SHORT).show();
            
            // 开始倒计时
            startDialogCountdown(btnDialogSendCode);
        });
        
        // 登录按钮点击事件
        btnDialogLogin.setOnClickListener(v -> {
            String phone = etDialogPhone.getText().toString().trim();
            String code = etDialogCode.getText().toString().trim();
            
            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "请输入手机号码", Toast.LENGTH_SHORT).show();
                etDialogPhone.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(code)) {
                Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
                etDialogCode.requestFocus();
                return;
            }
            
            // 执行手机验证码登录
            performSmsLogin(phone, code);
            dialog.dismiss();
        });
        
        // 取消按钮点击事件
        btnDialogCancel.setOnClickListener(v -> dialog.dismiss());
        
        // 设置对话框样式
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        dialog.show();
    }
    
    /**
     * 对话框发送验证码倒计时
     */
    private void startDialogCountdown(Button sendButton) {
        sendButton.setEnabled(false);
        
        new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                sendButton.setText("重新发送(" + millisUntilFinished / 1000 + "s)");
            }
            
            @Override
            public void onFinish() {
                sendButton.setEnabled(true);
                sendButton.setText("发送验证码");
            }
        }.start();
    }
    
    /**
     * 执行手机验证码登录
     */
    private void performSmsLogin(String phone, String code) {
        // 这里可以调用现有的手机登录逻辑
        performPhoneLogin(phone, code);
    }
    
    /**
     * 显示或隐藏发送验证码按钮容器，带有平滑动画效果
     * @param container 发送验证码按钮容器
     * @param show 是否显示
     */
    private void showSendCodeButton(LinearLayout container, boolean show) {
        if (container == null) {
            Log.w("LoginActivity", "发送验证码按钮容器为null");
            return;
        }
        
        if (show) {
            // 显示按钮容器
            if (container.getVisibility() != View.VISIBLE) {
                container.setVisibility(View.VISIBLE);
                
                // 淡入动画
                container.animate()
                    .alpha(1.0f)
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(300)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .withStartAction(() -> {
                        container.setAlpha(0.0f);
                        container.setScaleX(0.8f);
                        container.setScaleY(0.8f);
                    })
                    .start();
                
                Log.d("LoginActivity", "显示发送验证码按钮容器");
            }
        } else {
            // 隐藏按钮容器
            if (container.getVisibility() == View.VISIBLE) {
                // 淡出动画
                container.animate()
                    .alpha(0.0f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(200)
                    .setInterpolator(new android.view.animation.AccelerateInterpolator())
                    .withEndAction(() -> {
                        container.setVisibility(View.GONE);
                    })
                    .start();
                
                Log.d("LoginActivity", "隐藏发送验证码按钮容器");
            }
        }
    }
    
    /**
     * 设置可访问性支持
     */
    private void setupAccessibilitySupport() {
        try {
            // 检查是否启用了无障碍服务
            boolean isAccessibilityEnabled = AccessibilityManager.isAccessibilityEnabled(this);
            Log.d("LoginActivity", "无障碍服务状态: " + (isAccessibilityEnabled ? "已启用" : "未启用"));
            
            // 为主要输入框设置可访问性
            if (tilUsername != null) {
                AccessibilityManager.setupTextInputLayoutAccessibility(tilUsername, "用户名输入", "请输入您的用户名");
            }
            
            if (tilPassword != null) {
                AccessibilityManager.setupTextInputLayoutAccessibility(tilPassword, "密码输入", "请输入您的密码");
            }
            
            // 为按钮设置可访问性
            if (btnLogin != null) {
                AccessibilityManager.setupAccessibility(btnLogin, "登录按钮", "点击进行账户登录");
            }
            
            if (btnSmsLogin != null) {
                AccessibilityManager.setupAccessibility(btnSmsLogin, "手机验证码登录按钮", "点击使用手机验证码登录");
            }
            
            if (btnRegister != null) {
                AccessibilityManager.setupAccessibility(btnRegister, "注册按钮", "点击注册新账户");
            }
            
            if (tvForgotPassword != null) {
                AccessibilityManager.setupAccessibility(tvForgotPassword, "忘记密码链接", "点击找回密码");
            }
            
            // 设置键盘导航顺序
            if (etUsername != null && etPassword != null && btnLogin != null) {
                AccessibilityManager.setupKeyboardNavigation(etUsername, etPassword, btnLogin, btnSmsLogin, btnRegister);
            }
            
            Log.d("LoginActivity", "可访问性支持设置完成");
        } catch (Exception e) {
            Log.e("LoginActivity", "设置可访问性支持失败", e);
        }
    }
}