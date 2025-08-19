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
        
        initViews();
        setupInputValidation();
        setupClickListeners();
        setupAnimations();
    }

    private void initViews() {
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
        btnRegister.setOnClickListener(v -> performRegister());
        btnSendCode.setOnClickListener(v -> sendVerificationCode());
        tvBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setupAnimations() {
        Animation slideInAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        findViewById(R.id.form_container).startAnimation(slideInAnimation);
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
        if (show) {
            pbSendCodeLoading.setVisibility(View.VISIBLE);
            btnSendCode.setText("");
        } else {
            pbSendCodeLoading.setVisibility(View.GONE);
            btnSendCode.setText("发送验证码");
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
        if (show) {
            pbRegisterLoading.setVisibility(View.VISIBLE);
            btnRegister.setEnabled(false);
            btnRegister.setText("注册中...");
        } else {
            pbRegisterLoading.setVisibility(View.GONE);
            btnRegister.setEnabled(true);
            btnRegister.setText("注册");
        }
    }
}