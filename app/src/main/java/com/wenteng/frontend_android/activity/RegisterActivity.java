package com.wenteng.frontend_android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.wenteng.frontend_android.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPhone, etVerificationCode, etPassword, etConfirmPassword;
    private Button btnRegister, btnSendCode;
    private TextView tvBackToLogin;
    private CountDownTimer countDownTimer;
    private boolean isCodeSent = false;
    private int countdown = 60;
    private TextInputLayout tilUsername, tilEmail, tilPhone, tilVerificationCode, tilPassword, tilConfirmPassword;
    private ProgressBar pbRegisterLoading, pbSendCodeLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        try {
            initViewsRobust();
            setupInputValidation();
            setupClickListeners();
            setupAnimations();
        } catch (Exception e) {
            android.util.Log.e("RegisterActivity", "初始化失败: " + e.getMessage(), e);
            // 显示错误提示但不关闭Activity
            android.widget.Toast.makeText(this, "页面加载出现问题，请重试", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void initViewsRobust() {
        // 安全的视图查找，避免空指针异常
        try {
            etUsername = findViewById(R.id.et_username);
            etEmail = findViewById(R.id.et_email);
            etPhone = findViewById(R.id.et_phone);
            etVerificationCode = findViewById(R.id.et_verification_code);
            etPassword = findViewById(R.id.et_password);
            etConfirmPassword = findViewById(R.id.et_confirm_password);
            
            btnRegister = findViewById(R.id.btn_register);
            btnSendCode = findViewById(R.id.btn_send_code);
            tvBackToLogin = findViewById(R.id.btn_back_to_login);
            
            tilUsername = findViewById(R.id.til_username);
            tilEmail = findViewById(R.id.til_email);
            tilPhone = findViewById(R.id.til_phone);
            tilVerificationCode = findViewById(R.id.til_verification_code);
            tilPassword = findViewById(R.id.til_password);
            tilConfirmPassword = findViewById(R.id.til_confirm_password);
            
            pbRegisterLoading = findViewById(R.id.pb_register_loading);
            pbSendCodeLoading = findViewById(R.id.pb_send_code_loading);
            
            // 验证关键视图是否成功加载
            if (etUsername == null || etPassword == null || btnRegister == null) {
                throw new RuntimeException("关键视图加载失败");
            }
            
            android.util.Log.d("RegisterActivity", "视图初始化成功");
        } catch (Exception e) {
            android.util.Log.e("RegisterActivity", "视图初始化失败: " + e.getMessage(), e);
            throw e; // 重新抛出异常供上层处理
        }
    }

    private void setupInputValidation() {
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

    private void setupClickListeners() {
        // 安全的点击监听器设置，添加空指针检查
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> performRegister());
        }
        
        if (btnSendCode != null) {
            btnSendCode.setOnClickListener(v -> sendVerificationCode());
        }
        
        if (tvBackToLogin != null) {
            tvBackToLogin.setOnClickListener(v -> {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
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
        
        // 模拟注册过程
        new android.os.Handler().postDelayed(() -> {
            showRegisterLoading(false);
            Toast.makeText(this, "注册成功！", Toast.LENGTH_SHORT).show();
            
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, 2000);
    }

    private void sendVerificationCode() {
        String phone = etPhone.getText().toString().trim();
        if (!isValidPhoneNumber(phone)) {
            tilPhone.setError("请输入正确的手机号码");
            return;
        }
        
        showSendCodeLoading(true);
        btnSendCode.setEnabled(false);
        
        // 模拟发送验证码
        new android.os.Handler().postDelayed(() -> {
            showSendCodeLoading(false);
            isCodeSent = true;
            startCountdown();
            Toast.makeText(this, "验证码已发送", Toast.LENGTH_SHORT).show();
        }, 1500);
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

    private boolean validateAllInputs() {
        return validateUsername() && validateEmail() && validatePhone() && 
               validateVerificationCode() && validatePassword() && validateConfirmPassword();
    }

    private boolean validateUsername() {
        String username = etUsername.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("用户名不能为空");
            return false;
        } else if (username.length() < 3) {
            tilUsername.setError("用户名至少3个字符");
            return false;
        } else {
            tilUsername.setError(null);
            return true;
        }
    }

    private boolean validateEmail() {
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("邮箱不能为空");
            return false;
        } else if (!isValidEmail(email)) {
            tilEmail.setError("请输入正确的邮箱格式");
            return false;
        } else {
            tilEmail.setError(null);
            return true;
        }
    }

    private boolean validatePhone() {
        String phone = etPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError("手机号不能为空");
            return false;
        } else if (!isValidPhoneNumber(phone)) {
            tilPhone.setError("请输入正确的手机号码");
            return false;
        } else {
            tilPhone.setError(null);
            return true;
        }
    }

    private boolean validateVerificationCode() {
        String code = etVerificationCode.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            tilVerificationCode.setError("验证码不能为空");
            return false;
        } else if (code.length() != 6) {
            tilVerificationCode.setError("验证码应为6位数字");
            return false;
        } else {
            tilVerificationCode.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String password = etPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("密码不能为空");
            return false;
        } else if (password.length() < 6) {
            tilPassword.setError("密码至少6个字符");
            return false;
        } else {
            tilPassword.setError(null);
            return true;
        }
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