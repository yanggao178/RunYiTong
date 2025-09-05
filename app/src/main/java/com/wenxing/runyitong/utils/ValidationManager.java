package com.wenxing.runyitong.utils;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.wenxing.runyitong.R;

import java.util.regex.Pattern;

/**
 * 统一输入验证管理器
 * 提供实时验证反馈、密码强度检测、统一验证规则等功能
 */
public class ValidationManager {
    
    private static final String TAG = "ValidationManager";
    
    // 验证规则常量
    private static final int USERNAME_MIN_LENGTH = 3;
    private static final int USERNAME_MAX_LENGTH = 20;
    private static final int PASSWORD_MIN_LENGTH = 6;
    private static final int PASSWORD_MAX_LENGTH = 20;
    private static final int VERIFICATION_CODE_LENGTH = 6;
    
    // 正则表达式模式
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern VERIFICATION_CODE_PATTERN = Pattern.compile("^\\d{6}$");
    
    // 密码强度级别
    public enum PasswordStrength {
        WEAK(1, "弱", Color.parseColor("#F44336")),
        MEDIUM(2, "中等", Color.parseColor("#FF9800")),
        STRONG(3, "强", Color.parseColor("#4CAF50")),
        VERY_STRONG(4, "很强", Color.parseColor("#2196F3"));
        
        private final int level;
        private final String description;
        private final int color;
        
        PasswordStrength(int level, String description, int color) {
            this.level = level;
            this.description = description;
            this.color = color;
        }
        
        public int getLevel() { return level; }
        public String getDescription() { return description; }
        public int getColor() { return color; }
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final String errorMessage;
        private final PasswordStrength passwordStrength;
        
        public ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
            this.passwordStrength = null;
        }
        
        public ValidationResult(boolean isValid, String errorMessage, PasswordStrength passwordStrength) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
            this.passwordStrength = passwordStrength;
        }
        
        public boolean isValid() { return isValid; }
        public String getErrorMessage() { return errorMessage; }
        public PasswordStrength getPasswordStrength() { return passwordStrength; }
    }
    
    /**
     * 实时用户名验证
     * @param inputLayout 输入框布局
     * @param editText 输入框
     */
    public static void setupUsernameValidation(TextInputLayout inputLayout, EditText editText) {
        if (inputLayout == null || editText == null) return;
        
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                UXEnhancementUtils.clearError(inputLayout);
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                String username = s.toString().trim();
                if (!TextUtils.isEmpty(username)) {
                    ValidationResult result = validateUsername(username);
                    if (!result.isValid()) {
                        UXEnhancementUtils.showEnhancedError(inputLayout, result.getErrorMessage(), editText);
                    } else {
                        UXEnhancementUtils.showSuccessFeedback(inputLayout, editText);
                    }
                }
            }
        });
    }
    
    /**
     * 实时密码验证和强度检测
     * @param inputLayout 输入框布局
     * @param editText 输入框
     * @param strengthIndicator 密码强度指示器（可选）
     * @param strengthText 密码强度文本（可选）
     */
    public static void setupPasswordValidation(TextInputLayout inputLayout, EditText editText, 
                                             ProgressBar strengthIndicator, TextView strengthText) {
        if (inputLayout == null || editText == null) return;
        
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                UXEnhancementUtils.clearError(inputLayout);
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                String password = s.toString();
                if (!TextUtils.isEmpty(password)) {
                    ValidationResult result = validatePassword(password);
                    
                    if (!result.isValid()) {
                        UXEnhancementUtils.showEnhancedError(inputLayout, result.getErrorMessage(), editText);
                        hidePasswordStrength(strengthIndicator, strengthText);
                    } else {
                        UXEnhancementUtils.clearError(inputLayout);
                        if (result.getPasswordStrength() != null) {
                            showPasswordStrength(result.getPasswordStrength(), strengthIndicator, strengthText);
                        }
                    }
                } else {
                    hidePasswordStrength(strengthIndicator, strengthText);
                }
            }
        });
    }
    
    /**
     * 实时邮箱验证
     * @param inputLayout 输入框布局
     * @param editText 输入框
     */
    public static void setupEmailValidation(TextInputLayout inputLayout, EditText editText) {
        if (inputLayout == null || editText == null) return;
        
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                UXEnhancementUtils.clearError(inputLayout);
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString().trim();
                if (!TextUtils.isEmpty(email)) {
                    ValidationResult result = validateEmail(email);
                    if (!result.isValid()) {
                        UXEnhancementUtils.showEnhancedError(inputLayout, result.getErrorMessage(), editText);
                    } else {
                        UXEnhancementUtils.showSuccessFeedback(inputLayout, editText);
                    }
                }
            }
        });
    }
    
    /**
     * 实时手机号验证
     * @param inputLayout 输入框布局
     * @param editText 输入框
     */
    public static void setupPhoneValidation(TextInputLayout inputLayout, EditText editText) {
        if (inputLayout == null || editText == null) return;
        
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                UXEnhancementUtils.clearError(inputLayout);
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                String phone = s.toString().trim();
                if (!TextUtils.isEmpty(phone)) {
                    ValidationResult result = validatePhone(phone);
                    if (!result.isValid()) {
                        UXEnhancementUtils.showEnhancedError(inputLayout, result.getErrorMessage(), editText);
                    } else {
                        UXEnhancementUtils.showSuccessFeedback(inputLayout, editText);
                    }
                }
            }
        });
    }
    
    /**
     * 实时验证码验证
     * @param inputLayout 输入框布局
     * @param editText 输入框
     */
    public static void setupVerificationCodeValidation(TextInputLayout inputLayout, EditText editText) {
        if (inputLayout == null || editText == null) return;
        
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                UXEnhancementUtils.clearError(inputLayout);
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                String code = s.toString().trim();
                if (!TextUtils.isEmpty(code)) {
                    ValidationResult result = validateVerificationCode(code);
                    if (!result.isValid()) {
                        UXEnhancementUtils.showEnhancedError(inputLayout, result.getErrorMessage(), editText);
                    } else {
                        UXEnhancementUtils.showSuccessFeedback(inputLayout, editText);
                    }
                }
            }
        });
    }
    
    /**
     * 验证用户名
     * @param username 用户名
     * @return 验证结果
     */
    public static ValidationResult validateUsername(String username) {
        if (TextUtils.isEmpty(username)) {
            return new ValidationResult(false, "用户名不能为空");
        }
        
        if (username.length() < USERNAME_MIN_LENGTH) {
            return new ValidationResult(false, "用户名至少需要" + USERNAME_MIN_LENGTH + "个字符");
        }
        
        if (username.length() > USERNAME_MAX_LENGTH) {
            return new ValidationResult(false, "用户名不能超过" + USERNAME_MAX_LENGTH + "个字符");
        }
        
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return new ValidationResult(false, "用户名只能包含字母、数字、下划线和中文");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * 验证密码
     * @param password 密码
     * @return 验证结果
     */
    public static ValidationResult validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            return new ValidationResult(false, "密码不能为空");
        }
        
        if (password.length() < PASSWORD_MIN_LENGTH) {
            return new ValidationResult(false, "密码至少需要" + PASSWORD_MIN_LENGTH + "个字符");
        }
        
        if (password.length() > PASSWORD_MAX_LENGTH) {
            return new ValidationResult(false, "密码不能超过" + PASSWORD_MAX_LENGTH + "个字符");
        }
        
        PasswordStrength strength = calculatePasswordStrength(password);
        return new ValidationResult(true, null, strength);
    }
    
    /**
     * 验证邮箱
     * @param email 邮箱
     * @return 验证结果
     */
    public static ValidationResult validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return new ValidationResult(false, "邮箱不能为空");
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return new ValidationResult(false, "请输入正确的邮箱格式");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * 验证手机号
     * @param phone 手机号
     * @return 验证结果
     */
    public static ValidationResult validatePhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return new ValidationResult(false, "手机号不能为空");
        }
        
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return new ValidationResult(false, "请输入正确的11位手机号");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * 验证验证码
     * @param code 验证码
     * @return 验证结果
     */
    public static ValidationResult validateVerificationCode(String code) {
        if (TextUtils.isEmpty(code)) {
            return new ValidationResult(false, "验证码不能为空");
        }
        
        if (code.length() != VERIFICATION_CODE_LENGTH) {
            return new ValidationResult(false, "验证码应为" + VERIFICATION_CODE_LENGTH + "位数字");
        }
        
        if (!VERIFICATION_CODE_PATTERN.matcher(code).matches()) {
            return new ValidationResult(false, "验证码只能包含数字");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * 验证确认密码
     * @param password 原密码
     * @param confirmPassword 确认密码
     * @return 验证结果
     */
    public static ValidationResult validateConfirmPassword(String password, String confirmPassword) {
        if (TextUtils.isEmpty(confirmPassword)) {
            return new ValidationResult(false, "确认密码不能为空");
        }
        
        if (!password.equals(confirmPassword)) {
            return new ValidationResult(false, "两次输入的密码不一致");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * 计算密码强度
     * @param password 密码
     * @return 密码强度
     */
    private static PasswordStrength calculatePasswordStrength(String password) {
        if (TextUtils.isEmpty(password)) {
            return PasswordStrength.WEAK;
        }
        
        int score = 0;
        
        // 长度加分
        if (password.length() >= 8) score += 1;
        if (password.length() >= 12) score += 1;
        
        // 字符类型加分
        if (password.matches(".*[a-z].*")) score += 1; // 小写字母
        if (password.matches(".*[A-Z].*")) score += 1; // 大写字母
        if (password.matches(".*\\d.*")) score += 1;     // 数字
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\\\\|,.<>/?].*")) score += 1; // 特殊字符
        
        // 复杂度加分
        if (password.length() >= 10 && score >= 3) score += 1;
        
        // 根据得分返回强度
        if (score <= 2) return PasswordStrength.WEAK;
        else if (score <= 4) return PasswordStrength.MEDIUM;
        else if (score <= 6) return PasswordStrength.STRONG;
        else return PasswordStrength.VERY_STRONG;
    }
    
    /**
     * 显示密码强度
     * @param strength 密码强度
     * @param strengthIndicator 强度指示器
     * @param strengthText 强度文本
     */
    private static void showPasswordStrength(PasswordStrength strength, 
                                           ProgressBar strengthIndicator, TextView strengthText) {
        if (strengthIndicator != null) {
            strengthIndicator.setProgress(strength.getLevel() * 25);
            strengthIndicator.getProgressDrawable().setColorFilter(
                strength.getColor(), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        
        if (strengthText != null) {
            strengthText.setText("密码强度: " + strength.getDescription());
            strengthText.setTextColor(strength.getColor());
        }
    }
    
    /**
     * 隐藏密码强度指示
     * @param strengthIndicator 强度指示器
     * @param strengthText 强度文本
     */
    private static void hidePasswordStrength(ProgressBar strengthIndicator, TextView strengthText) {
        if (strengthIndicator != null) {
            strengthIndicator.setProgress(0);
        }
        
        if (strengthText != null) {
            strengthText.setText("");
        }
    }
    
    /**
     * 快速验证所有字段
     * @param fields 字段数组，每个元素包含 [字段名, 字段值, 验证类型]
     * @return 第一个验证失败的结果，如果全部通过则返回成功结果
     */
    public static ValidationResult validateAllFields(String[]... fields) {
        for (String[] field : fields) {
            if (field.length < 3) continue;
            
            String fieldName = field[0];
            String fieldValue = field[1];
            String validationType = field[2];
            
            ValidationResult result;
            switch (validationType.toLowerCase()) {
                case "username":
                    result = validateUsername(fieldValue);
                    break;
                case "password":
                    result = validatePassword(fieldValue);
                    break;
                case "email":
                    result = validateEmail(fieldValue);
                    break;
                case "phone":
                    result = validatePhone(fieldValue);
                    break;
                case "verification_code":
                    result = validateVerificationCode(fieldValue);
                    break;
                default:
                    continue;
            }
            
            if (!result.isValid()) {
                return new ValidationResult(false, fieldName + ": " + result.getErrorMessage());
            }
        }
        
        return new ValidationResult(true, "所有字段验证通过");
    }
}