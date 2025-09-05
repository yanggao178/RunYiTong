package com.wenxing.runyitong.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.wenxing.runyitong.R;
import android.widget.LinearLayout;
import com.wenxing.runyitong.api.ApiClient;
import com.wenxing.runyitong.api.ApiService;
import com.wenxing.runyitong.api.ApiResponse;
import com.wenxing.runyitong.api.RegisterResponse;
import com.wenxing.runyitong.api.RegisterRequest;
import com.wenxing.runyitong.api.SmsCodeResponse;
import com.wenxing.runyitong.utils.ValidationManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    // 注册模式枚举
    public enum RegisterMode {
        PASSWORD,  // 账户密码注册
        SMS        // 手机验证码注册
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    // 当前注册模式
    private RegisterMode currentMode = RegisterMode.PASSWORD;

    // UI组件 - 通用
    private Button btnPasswordMode, btnSmsMode;
    private LinearLayout layoutPasswordMode, layoutSmsMode;
    private Button btnRegister, btnSendCode, btnBackToLogin;
    private ProgressBar pbRegisterLoading, pbSendCodeLoading;

    // UI组件 - 密码注册模式
    private TextInputLayout tilUsername, tilEmail, tilPassword, tilConfirmPassword;
    private EditText etUsername, etEmail, etPassword, etConfirmPassword;

    // UI组件 - 短信注册模式
    private TextInputLayout tilUsernameSms, tilPhone, tilVerificationCode, tilPasswordSms, tilConfirmPasswordSms;
    private EditText etUsernameSms, etPhone, etVerificationCode, etPasswordSms, etConfirmPasswordSms;

    // 倒计时相关
    private CountDownTimer countDownTimer;
    private boolean isCountingDown = false;
    private int countdown = 60;
    private boolean isCodeSent = false;
    
    // API服务
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d(TAG, "RegisterActivity onCreate started");
        setContentView(R.layout.activity_register);
        
        try {
            android.util.Log.d(TAG, "开始初始化视图组件");
            initViewsRobust();
            
            android.util.Log.d(TAG, "开始设置输入验证");
            setupInputValidation();
            
            android.util.Log.d(TAG, "开始设置点击监听器");
            setupClickListeners();
            
            android.util.Log.d(TAG, "开始设置动画");
            setupAnimations();
            
            // 初始化API服务
            android.util.Log.d(TAG, "初始化API服务");
            apiService = ApiClient.getApiService();
            
            // 所有布局都支持模式切换，初始化默认为密码注册模式
            android.util.Log.d(TAG, "切换到默认密码注册模式");
            switchToPasswordMode();
            
            android.util.Log.d(TAG, "RegisterActivity初始化完成");
        } catch (Exception e) {
            android.util.Log.e(TAG, "初始化失败: " + e.getMessage(), e);
            // 显示错误提示但不关闭Activity
            android.widget.Toast.makeText(this, "页面加载出现问题，请重试", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void initViewsRobust() {
        try {
            android.util.Log.d(TAG, "开始初始化视图组件");
            
            // 初始化模式切换按钮
            btnPasswordMode = findViewById(R.id.btn_password_mode);
            android.util.Log.d(TAG, "密码模式按钮初始化: " + (btnPasswordMode != null ? "成功" : "失败"));
            
            btnSmsMode = findViewById(R.id.btn_sms_mode);
            android.util.Log.d(TAG, "短信模式按钮初始化: " + (btnSmsMode != null ? "成功" : "失败"));
            
            // 初始化模式容器
            layoutPasswordMode = findViewById(R.id.layout_password_mode);
            android.util.Log.d(TAG, "密码注册布局初始化: " + (layoutPasswordMode != null ? "成功" : "失败"));
            
            layoutSmsMode = findViewById(R.id.layout_sms_mode);
            android.util.Log.d(TAG, "短信注册布局初始化: " + (layoutSmsMode != null ? "成功" : "失败"));

            // 初始化密码注册模式组件
            tilUsername = findViewById(R.id.til_username);
            android.util.Log.d(TAG, "用户名输入布局初始化: " + (tilUsername != null ? "成功" : "失败"));
            
            tilEmail = findViewById(R.id.til_email);
            android.util.Log.d(TAG, "邮箱输入布局初始化: " + (tilEmail != null ? "成功" : "失败"));
            
            tilPassword = findViewById(R.id.til_password);
            android.util.Log.d(TAG, "密码输入布局初始化: " + (tilPassword != null ? "成功" : "失败"));
            
            tilConfirmPassword = findViewById(R.id.til_confirm_password);
            android.util.Log.d(TAG, "确认密码输入布局初始化: " + (tilConfirmPassword != null ? "成功" : "失败"));
            
            etUsername = findViewById(R.id.et_username);
            android.util.Log.d(TAG, "用户名输入框初始化: " + (etUsername != null ? "成功" : "失败"));
            
            etEmail = findViewById(R.id.et_email);
            android.util.Log.d(TAG, "邮箱输入框初始化: " + (etEmail != null ? "成功" : "失败"));
            
            etPassword = findViewById(R.id.et_password);
            android.util.Log.d(TAG, "密码输入框初始化: " + (etPassword != null ? "成功" : "失败"));
            
            etConfirmPassword = findViewById(R.id.et_confirm_password);
            android.util.Log.d(TAG, "确认密码输入框初始化: " + (etConfirmPassword != null ? "成功" : "失败"));

            // 初始化短信注册模式组件
            tilUsernameSms = findViewById(R.id.til_username_sms);
            android.util.Log.d(TAG, "短信模式用户名输入布局初始化: " + (tilUsernameSms != null ? "成功" : "失败"));
            
            tilPhone = findViewById(R.id.til_phone);
            android.util.Log.d(TAG, "手机号输入布局初始化: " + (tilPhone != null ? "成功" : "失败"));
            
            tilVerificationCode = findViewById(R.id.til_verification_code);
            android.util.Log.d(TAG, "验证码输入布局初始化: " + (tilVerificationCode != null ? "成功" : "失败"));
            
            tilPasswordSms = findViewById(R.id.til_password_sms);
            android.util.Log.d(TAG, "短信模式密码输入布局初始化: " + (tilPasswordSms != null ? "成功" : "失败"));
            
            tilConfirmPasswordSms = findViewById(R.id.til_confirm_password_sms);
            android.util.Log.d(TAG, "短信模式确认密码输入布局初始化: " + (tilConfirmPasswordSms != null ? "成功" : "失败"));
            
            etUsernameSms = findViewById(R.id.et_username_sms);
            android.util.Log.d(TAG, "短信模式用户名输入框初始化: " + (etUsernameSms != null ? "成功" : "失败"));
            
            etPhone = findViewById(R.id.et_phone);
            android.util.Log.d(TAG, "手机号输入框初始化: " + (etPhone != null ? "成功" : "失败"));
            
            etVerificationCode = findViewById(R.id.et_verification_code);
            android.util.Log.d(TAG, "验证码输入框初始化: " + (etVerificationCode != null ? "成功" : "失败"));
            
            etPasswordSms = findViewById(R.id.et_password_sms);
            android.util.Log.d(TAG, "短信模式密码输入框初始化: " + (etPasswordSms != null ? "成功" : "失败"));
            
            etConfirmPasswordSms = findViewById(R.id.et_confirm_password_sms);
            android.util.Log.d(TAG, "短信模式确认密码输入框初始化: " + (etConfirmPasswordSms != null ? "成功" : "失败"));

            // 初始化通用按钮
            btnRegister = findViewById(R.id.btn_register);
            android.util.Log.d(TAG, "注册按钮初始化: " + (btnRegister != null ? "成功" : "失败"));
            
            btnSendCode = findViewById(R.id.btn_send_code);
            android.util.Log.d(TAG, "发送验证码按钮初始化: " + (btnSendCode != null ? "成功" : "失败"));
            
            btnBackToLogin = findViewById(R.id.btn_back_to_login);
            android.util.Log.d(TAG, "返回登录按钮初始化: " + (btnBackToLogin != null ? "成功" : "失败"));
            

            // 初始化进度条
            pbRegisterLoading = findViewById(R.id.pb_register_loading);
            android.util.Log.d(TAG, "注册进度条初始化: " + (pbRegisterLoading != null ? "成功" : "失败"));
            
            pbSendCodeLoading = findViewById(R.id.pb_send_code_loading);
            android.util.Log.d(TAG, "发送验证码进度条初始化: " + (pbSendCodeLoading != null ? "成功" : "失败"));

            // 检查关键组件是否成功初始化
            if (btnRegister == null) {
                android.util.Log.e(TAG, "关键组件初始化失败");
                android.util.Log.e(TAG, "btnRegister: " + (btnRegister != null));
                android.widget.Toast.makeText(this, "页面初始化失败，请重试", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 检查模式切换组件（所有布局都应该有）
            if (btnPasswordMode == null || btnSmsMode == null || 
                layoutPasswordMode == null || layoutSmsMode == null) {
                android.util.Log.w(TAG, "模式切换组件缺失，可能影响功能");
                android.util.Log.w(TAG, "btnPasswordMode: " + (btnPasswordMode != null));
                android.util.Log.w(TAG, "btnSmsMode: " + (btnSmsMode != null));
                android.util.Log.w(TAG, "layoutPasswordMode: " + (layoutPasswordMode != null));
                android.util.Log.w(TAG, "layoutSmsMode: " + (layoutSmsMode != null));
            }

            android.util.Log.d(TAG, "所有视图组件初始化成功");
        } catch (Exception e) {
            android.util.Log.e(TAG, "初始化视图时发生异常: " + e.getMessage(), e);
            android.widget.Toast.makeText(this, "页面加载失败，请重试", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void setupInputValidation() {
        // 设置密码注册模式的输入验证监听器
        setupPasswordModeListeners();
        
        // 设置短信注册模式的输入验证监听器
        setupSmsModeListeners();
    }

    /**
     * 设置密码注册模式的输入验证监听器
     */
    private void setupPasswordModeListeners() {
        if (etUsername != null) {
            etUsername.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    validateUsername();
                }
                
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
        
        if (etEmail != null) {
            etEmail.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    validateEmail();
                }
                
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
        
        if (etPassword != null) {
            etPassword.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    validatePassword();
                }
                
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
        
        if (etConfirmPassword != null) {
            etConfirmPassword.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    validateConfirmPassword();
                }
                
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    /**
     * 设置短信注册模式的输入验证监听器
     */
    private void setupSmsModeListeners() {
        if (etUsernameSms != null) {
            etUsernameSms.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    validateUsernameSms();
                }
                
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
        
        if (etPhone != null) {
            etPhone.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    validatePhone();
                }
                
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
        
        if (etVerificationCode != null) {
            etVerificationCode.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    validateVerificationCode();
                }
                
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
        
        if (etPasswordSms != null) {
            etPasswordSms.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    validatePasswordSms();
                }
                
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
        
        if (etConfirmPasswordSms != null) {
            etConfirmPasswordSms.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    validateConfirmPasswordSms();
                }
                
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupClickListeners() {
        // 设置模式切换按钮监听器
        if (btnPasswordMode != null) {
            btnPasswordMode.setOnClickListener(v -> switchToPasswordMode());
        }
        
        if (btnSmsMode != null) {
            btnSmsMode.setOnClickListener(v -> switchToSmsMode());
        }
        
        // 安全的点击监听器设置，添加空指针检查
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> performRegister());
        }
        
        if (btnSendCode != null) {
            btnSendCode.setOnClickListener(v -> sendVerificationCode());
        }
        
        // 为验证码输入框的endIcon设置点击监听器（用于小屏幕布局）
        if (tilVerificationCode != null) {
            tilVerificationCode.setEndIconOnClickListener(v -> {
                android.util.Log.d(TAG, "验证码输入框endIcon被点击");
                sendVerificationCode();
            });
        }
        
        if (btnBackToLogin != null) {
                btnBackToLogin.setOnClickListener(v -> {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    /**
     * 切换到密码注册模式
     */
    private void switchToPasswordMode() {
        try {
            android.util.Log.d(TAG, "开始切换到密码注册模式");
            currentMode = RegisterMode.PASSWORD;
            
            // 更新按钮状态
            if (btnPasswordMode != null) {
                btnPasswordMode.setSelected(true);
                android.util.Log.d(TAG, "密码模式按钮设置为选中状态");
            } else {
                android.util.Log.e(TAG, "密码模式按钮为null");
            }
            
            if (btnSmsMode != null) {
                btnSmsMode.setSelected(false);
                android.util.Log.d(TAG, "短信模式按钮设置为未选中状态");
            } else {
                android.util.Log.e(TAG, "短信模式按钮为null");
            }
            
            // 显示/隐藏对应的布局
            if (layoutPasswordMode != null) {
                layoutPasswordMode.setVisibility(View.VISIBLE);
                android.util.Log.d(TAG, "密码注册布局设置为可见");
            } else {
                android.util.Log.e(TAG, "密码注册布局为null");
            }
            
            if (layoutSmsMode != null) {
                layoutSmsMode.setVisibility(View.GONE);
                android.util.Log.d(TAG, "短信注册布局设置为隐藏");
            } else {
                android.util.Log.e(TAG, "短信注册布局为null");
            }
            
            // 隐藏发送验证码按钮
            if (btnSendCode != null) {
                btnSendCode.setVisibility(View.GONE);
                android.util.Log.d(TAG, "发送验证码按钮设置为隐藏");
            } else {
                android.util.Log.e(TAG, "发送验证码按钮为null");
            }
            
            android.util.Log.d(TAG, "成功切换到密码注册模式");
        } catch (Exception e) {
            android.util.Log.e(TAG, "切换到密码注册模式时发生异常: " + e.getMessage(), e);
        }
    }

    /**
     * 切换到短信验证码注册模式
     */
    private void switchToSmsMode() {
        try {
            android.util.Log.d(TAG, "开始切换到短信验证码注册模式");
            currentMode = RegisterMode.SMS;
            
            // 更新按钮状态
            if (btnPasswordMode != null) {
                btnPasswordMode.setSelected(false);
                android.util.Log.d(TAG, "密码模式按钮设置为未选中状态");
            } else {
                android.util.Log.e(TAG, "密码模式按钮为null");
            }
            
            if (btnSmsMode != null) {
                btnSmsMode.setSelected(true);
                android.util.Log.d(TAG, "短信模式按钮设置为选中状态");
            } else {
                android.util.Log.e(TAG, "短信模式按钮为null");
            }
            
            // 显示/隐藏对应的布局
            if (layoutPasswordMode != null) {
                layoutPasswordMode.setVisibility(View.GONE);
                android.util.Log.d(TAG, "密码注册布局设置为隐藏");
            } else {
                android.util.Log.e(TAG, "密码注册布局为null");
            }
            
            if (layoutSmsMode != null) {
                layoutSmsMode.setVisibility(View.VISIBLE);
                android.util.Log.d(TAG, "短信注册布局设置为可见");
            } else {
                android.util.Log.e(TAG, "短信注册布局为null");
            }
            
            // 显示发送验证码按钮
            if (btnSendCode != null) {
                btnSendCode.setVisibility(View.VISIBLE);
                android.util.Log.d(TAG, "发送验证码按钮设置为可见");
            } else {
                android.util.Log.e(TAG, "发送验证码按钮为null");
            }
            
            android.util.Log.d(TAG, "成功切换到短信验证码注册模式");
        } catch (Exception e) {
            android.util.Log.e(TAG, "切换到短信验证码注册模式时发生异常: " + e.getMessage(), e);
        }
    }

    private void setupAnimations() {
        try {
            Animation slideInAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
            android.view.View formContainer = findViewById(R.id.form_container);
            if (formContainer != null && slideInAnimation != null) {
                formContainer.startAnimation(slideInAnimation);
            }
        } catch (Exception e) {
            android.util.Log.w("RegisterActivity", "动画设置失败: " + e.getMessage());
            // 动画失败不影响核心功能，继续执行
        }
    }

    private void performRegister() {
        if (!validateAllInputs()) {
            return;
        }
        
        showRegisterLoading(true);
        
        // 根据当前模式选择注册逻辑
        if (currentMode == RegisterMode.PASSWORD) {
            performPasswordRegister();
        } else {
            performSmsRegister();
        }
    }
    
    /**
     * 执行简化的SMS注册（适用于小屏幕布局）
     */
    private void performSimplifiedSmsRegister() {
        // 获取小屏幕布局的输入数据
        String username = etUsername.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String verificationCode = etVerificationCode.getText().toString().trim();
        String password = etPassword.getText().toString();
        
        android.util.Log.d(TAG, "执行简化SMS注册: " + username + ", 手机号: " + phone);
        
        // 创建注册请求对象
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(username);
        registerRequest.setPhone(phone);
        registerRequest.setVerification_code(verificationCode);
        registerRequest.setPassword(password);
        
        // 调用SMS注册 API
        Call<ApiResponse<RegisterResponse>> call = apiService.registerWithSms(registerRequest);
        call.enqueue(new Callback<ApiResponse<RegisterResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<RegisterResponse>> call, Response<ApiResponse<RegisterResponse>> response) {
                showRegisterLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<RegisterResponse> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        RegisterResponse registerData = apiResponse.getData();
                        android.util.Log.d(TAG, "简化SMS注册成功: " + registerData.toString());
                        
                        // 保存用户ID到SharedPreferences
                        if (registerData.getUserId() != null) {
                            getSharedPreferences("app_state", MODE_PRIVATE)
                                    .edit()
                                    .putInt("user_id", registerData.getUserId().intValue())
                                    .apply();
                            android.util.Log.d(TAG, "用户ID已保存: " + registerData.getUserId());
                        }
                        
                        Toast.makeText(RegisterActivity.this, "注册成功！", Toast.LENGTH_SHORT).show();
                        
                        // 跳转到登录页面
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.putExtra("registered_username", username);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "注册失败: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "注册失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<RegisterResponse>> call, Throwable t) {
                showRegisterLoading(false);
                android.util.Log.e(TAG, "简化SMS注册请求失败: " + t.getMessage(), t);
                Toast.makeText(RegisterActivity.this, "网络错误，请稍后重试", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * 执行密码注册
     */
    private void performPasswordRegister() {
        // 获取密码注册模式的输入数据
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        
        android.util.Log.d(TAG, "执行密码注册: " + username);
        
        // 创建注册请求对象
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(username);
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        
        // 调用密码注册API（JSON格式）
        Call<ApiResponse<RegisterResponse>> call = apiService.registerWithPassword(registerRequest);
        call.enqueue(new Callback<ApiResponse<RegisterResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<RegisterResponse>> call, Response<ApiResponse<RegisterResponse>> response) {
                showRegisterLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<RegisterResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        RegisterResponse registerData = apiResponse.getData();
                        android.util.Log.d(TAG, "密码注册成功: " + registerData.toString());
                        
                        // 保存用户ID到SharedPreferences
                        if (registerData.getUserId() != null) {
                            getSharedPreferences("app_state", MODE_PRIVATE)
                                    .edit()
                                    .putInt("user_id", registerData.getUserId().intValue())
                                    .apply();
                            android.util.Log.d(TAG, "用户ID已保存: " + registerData.getUserId());
                        }
                        
                        Toast.makeText(RegisterActivity.this, "注册成功！欢迎 " + registerData.getUsername(), Toast.LENGTH_SHORT).show();
                        
                        // 跳转到登录页面
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.putExtra("username", registerData.getUsername());
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "注册失败: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "注册失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<RegisterResponse>> call, Throwable t) {
                showRegisterLoading(false);
                android.util.Log.e(TAG, "密码注册请求失败: " + t.getMessage(), t);
                Toast.makeText(RegisterActivity.this, "网络错误，请稍后重试", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * 执行短信验证码注册
     */
    private void performSmsRegister() {
        // 获取短信注册模式的输入数据
        String username = etUsernameSms.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String verificationCode = etVerificationCode.getText().toString().trim();
        String password = etPasswordSms.getText().toString();
        
        android.util.Log.d(TAG, "执行短信验证码注册: " + username + ", 手机号: " + phone);
        
        // 创建注册请求对象
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(username);
        registerRequest.setPhone(phone);
        registerRequest.setVerification_code(verificationCode);
        registerRequest.setPassword(password);
        
        // 调用短信注册API（JSON格式）
        Call<ApiResponse<RegisterResponse>> call = apiService.registerWithSms(registerRequest);
        call.enqueue(new Callback<ApiResponse<RegisterResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<RegisterResponse>> call, Response<ApiResponse<RegisterResponse>> response) {
                showRegisterLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<RegisterResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        RegisterResponse registerData = apiResponse.getData();
                        android.util.Log.d(TAG, "短信注册成功: " + registerData.toString());
                        
                        // 保存用户ID到SharedPreferences
                        if (registerData.getUserId() != null) {
                            getSharedPreferences("app_state", MODE_PRIVATE)
                                    .edit()
                                    .putInt("user_id", registerData.getUserId().intValue())
                                    .apply();
                            android.util.Log.d(TAG, "用户ID已保存: " + registerData.getUserId());
                        }
                        
                        Toast.makeText(RegisterActivity.this, "注册成功！欢迎 " + registerData.getUsername(), Toast.LENGTH_SHORT).show();
                        
                        // 跳转到登录页面
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.putExtra("username", registerData.getUsername());
                        intent.putExtra("phone", registerData.getPhone());
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "注册失败: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "注册失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<RegisterResponse>> call, Throwable t) {
                showRegisterLoading(false);
                android.util.Log.e(TAG, "短信注册请求失败: " + t.getMessage(), t);
                Toast.makeText(RegisterActivity.this, "网络错误，请稍后重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendVerificationCode() {
        String phone = etPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!validatePhone()) {
            return;
        }
        
        // 防止重复发送
        if (isCountingDown) {
            Toast.makeText(this, "请等待倒计时结束后再发送", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 显示发送中状态
        updateSendCodeState("sending");
        
        // 调用发送验证码API
        Call<ApiResponse<SmsCodeResponse>> call = apiService.sendSmsCode(phone);
        call.enqueue(new Callback<ApiResponse<SmsCodeResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<SmsCodeResponse>> call, Response<ApiResponse<SmsCodeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<SmsCodeResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        SmsCodeResponse smsData = apiResponse.getData();
                        android.util.Log.d(TAG, "验证码发送成功: " + smsData.toString());
                        
                        Toast.makeText(RegisterActivity.this, "验证码已发送到 " + phone, Toast.LENGTH_SHORT).show();
                        isCodeSent = true;
                        
                        // 启动倒计时
                        startCountdown();
                    } else {
                        // 发送失败，恢复状态
                        updateSendCodeState("normal");
                        Toast.makeText(RegisterActivity.this, "验证码发送失败: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 网络错误，恢复状态
                    updateSendCodeState("normal");
                    Toast.makeText(RegisterActivity.this, "验证码发送失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<SmsCodeResponse>> call, Throwable t) {
                android.util.Log.e(TAG, "验证码发送请求失败: " + t.getMessage(), t);
                
                // 网络错误，恢复状态
                updateSendCodeState("normal");
                Toast.makeText(RegisterActivity.this, "网络错误，请稍后重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 统一管理发送验证码的状态（适配按钮和endIcon两种布局）
     * @param state "normal" - 正常状态, "sending" - 发送中, "countdown" - 倒计时状态
     */
    private void updateSendCodeState(String state) {
        // 检查是否使用endIcon模式（小屏幕布局或没有发送按钮的布局）
        boolean useEndIcon = (btnSendCode == null && tilVerificationCode != null);
        
        if (useEndIcon) {
            android.util.Log.d(TAG, "使用endIcon模式，更新状态: " + state);
            switch (state) {
                case "sending":
                    tilVerificationCode.setEndIconDrawable(null); // 移除图标表示加载中
                    if (pbSendCodeLoading != null) {
                        pbSendCodeLoading.setVisibility(View.VISIBLE);
                    }
                    break;
                case "normal":
                    tilVerificationCode.setEndIconDrawable(getDrawable(R.drawable.ic_arrow_forward));
                    if (pbSendCodeLoading != null) {
                        pbSendCodeLoading.setVisibility(View.GONE);
                    }
                    break;
                case "countdown":
                    tilVerificationCode.setEndIconDrawable(null); // 倒计时期间不显示图标
                    if (pbSendCodeLoading != null) {
                        pbSendCodeLoading.setVisibility(View.GONE);
                    }
                    break;
            }
        }
        
        // 对于有发送按钮的布局
        if (btnSendCode != null) {
            android.util.Log.d(TAG, "使用按钮模式，更新状态: " + state);
            switch (state) {
                case "sending":
                    btnSendCode.setEnabled(false);
                    btnSendCode.setText("发送中...");
                    break;
                case "normal":
                    btnSendCode.setEnabled(true);
                    btnSendCode.setText("发送验证码");
                    break;
                case "countdown":
                    btnSendCode.setEnabled(false);
                    // 倒计时文本在startCountdown方法中设置
                    break;
            }
        }
    }

    private void showSendCodeLoading(boolean show) {
        try {
            if (show) {
                if (pbSendCodeLoading != null) {
                    pbSendCodeLoading.setVisibility(View.VISIBLE);
                }
                if (btnSendCode != null) {
                    btnSendCode.setText("");
                }
            } else {
                if (pbSendCodeLoading != null) {
                    pbSendCodeLoading.setVisibility(View.GONE);
                }
                if (btnSendCode != null) {
                    btnSendCode.setText("发送验证码");
                }
            }
        } catch (Exception e) {
            android.util.Log.w("RegisterActivity", "显示发送验证码加载状态失败: " + e.getMessage());
        }
    }

    private void startCountdown() {
        isCountingDown = true;
        countdown = 60;
        
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        
        // 设置倒计时状态
        updateSendCodeState("countdown");
        
        // 检查是否使用endIcon模式
        boolean useEndIcon = (btnSendCode == null && tilVerificationCode != null);
        
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                countdown = (int) (millisUntilFinished / 1000);
                
                // endIcon模式：在hint中显示倒计时
                if (useEndIcon && etVerificationCode != null) {
                    etVerificationCode.setHint("验证码（" + countdown + "s后可重发）");
                }
                
                // 按钮模式：在按钮中显示倒计时
                if (btnSendCode != null) {
                    btnSendCode.setText(countdown + "s后重发");
                    btnSendCode.setEnabled(false);
                }
            }
            
            @Override
            public void onFinish() {
                isCountingDown = false;
                
                // 恢复正常状态
                updateSendCodeState("normal");
                
                // endIcon模式：恢复原始 hint
                if (useEndIcon && etVerificationCode != null) {
                    etVerificationCode.setHint("验证码（点击右侧图标发送）");
                }
                
                isCodeSent = false;
                android.util.Log.d(TAG, "验证码倒计时结束，可以重新发送");
            }
        };
        
        countDownTimer.start();
        android.util.Log.d(TAG, "开始验证码倒计时: 60秒");
    }

    private boolean validateAllInputs() {
        // 根据当前模式选择验证逻辑
        if (currentMode == RegisterMode.PASSWORD) {
            return validateUsername() && validateEmail() && validatePassword() && validateConfirmPassword();
        } else {
            return validateUsernameSms() && validatePhone() && validateVerificationCode() && 
                   validatePasswordSms() && validateConfirmPasswordSms();
        }
    }

    private boolean validateUsernameSms() {
        String username = etUsernameSms.getText().toString().trim();
        ValidationManager.ValidationResult result = ValidationManager.validateUsername(username);
        if (!result.isValid()) {
            tilUsernameSms.setError(result.getErrorMessage());
            return false;
        }
        
        tilUsernameSms.setError(null);
        return true;
    }

    private boolean validatePasswordSms() {
        String password = etPasswordSms.getText().toString();
        if (TextUtils.isEmpty(password)) {
            tilPasswordSms.setError("密码不能为空");
            return false;
        }
        
        ValidationManager.ValidationResult result = ValidationManager.validatePassword(password);
        if (!result.isValid()) {
            tilPasswordSms.setError(result.getErrorMessage());
            return false;
        }
        
        tilPasswordSms.setError(null);
        return true;
    }

    private boolean validateConfirmPasswordSms() {
        String password = etPasswordSms.getText().toString();
        String confirmPassword = etConfirmPasswordSms.getText().toString();
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPasswordSms.setError("确认密码不能为空");
            return false;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPasswordSms.setError("两次输入的密码不一致");
            return false;
        } else {
            tilConfirmPasswordSms.setError(null);
            return true;
        }
    }

    private boolean validateUsername() {
        String username = etUsername.getText().toString().trim();
        ValidationManager.ValidationResult result = ValidationManager.validateUsername(username);
        if (!result.isValid()) {
            tilUsername.setError(result.getErrorMessage());
            return false;
        }
        
        tilUsername.setError(null);
        return true;
    }

    private boolean validateEmail() {
        String email = etEmail.getText().toString().trim();
        ValidationManager.ValidationResult result = ValidationManager.validateEmail(email);
        if (!result.isValid()) {
            tilEmail.setError(result.getErrorMessage());
            return false;
        }
        
        tilEmail.setError(null);
        return true;
    }

    private boolean validatePhone() {
        String phone = etPhone.getText().toString().trim();
        ValidationManager.ValidationResult result = ValidationManager.validatePhone(phone);
        if (!result.isValid()) {
            tilPhone.setError(result.getErrorMessage());
            return false;
        }
        
        tilPhone.setError(null);
        return true;
    }

    private boolean validateVerificationCode() {
        String code = etVerificationCode.getText().toString().trim();
        ValidationManager.ValidationResult result = ValidationManager.validateVerificationCode(code);
        if (!result.isValid()) {
            tilVerificationCode.setError(result.getErrorMessage());
            return false;
        }
        
        tilVerificationCode.setError(null);
        return true;
    }

    private boolean validatePassword() {
        String password = etPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("密码不能为空");
            return false;
        }
        
        ValidationManager.ValidationResult result = ValidationManager.validatePassword(password);
        if (!result.isValid()) {
            tilPassword.setError(result.getErrorMessage());
            return false;
        }
        
        tilPassword.setError(null);
        return true;
    }

    private boolean validateConfirmPassword() {
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("确认密码不能为空");
            return false;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("两次输入的密码不一致");
            return false;
        } else {
            tilConfirmPassword.setError(null);
            return true;
        }
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone != null && phone.matches("^1[3-9]\\d{9}$");
    }

    private boolean isValidEmail(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void showRegisterLoading(boolean show) {
        try {
            if (show) {
                if (pbRegisterLoading != null) {
                    pbRegisterLoading.setVisibility(View.VISIBLE);
                }
                if (btnRegister != null) {
                    btnRegister.setEnabled(false);
                    btnRegister.setText("注册中...");
                }
            } else {
                if (pbRegisterLoading != null) {
                    pbRegisterLoading.setVisibility(View.GONE);
                }
                if (btnRegister != null) {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("注册");
                }
            }
        } catch (Exception e) {
            android.util.Log.w("RegisterActivity", "显示加载状态失败: " + e.getMessage());
        }
    }
}