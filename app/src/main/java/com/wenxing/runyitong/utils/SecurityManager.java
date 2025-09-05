package com.wenxing.runyitong.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.util.concurrent.TimeUnit;

/**
 * 安全性增强管理器
 * 提供登录尝试限制、安全数据存储、密码加密等安全功能
 */
public class SecurityManager {
    
    private static final String TAG = "SecurityManager";
    
    // 安全配置常量
    private static final String PREFS_NAME = "secure_preferences";
    private static final String LOGIN_ATTEMPTS_KEY = "login_attempts";
    private static final String LAST_ATTEMPT_TIME_KEY = "last_attempt_time";
    private static final String ACCOUNT_LOCKED_KEY = "account_locked";
    private static final String LOCK_EXPIRY_TIME_KEY = "lock_expiry_time";
    
    // 安全策略常量
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MINUTES = 15;
    private static final long ATTEMPT_RESET_DURATION_MINUTES = 30;
    
    private Context context;
    private SharedPreferences securePrefs;
    
    public SecurityManager(Context context) {
        this.context = context.getApplicationContext();
        initializeSecurePreferences();
    }
    
    /**
     * 初始化安全存储
     */
    private void initializeSecurePreferences() {
        try {
            // 尝试使用加密存储
            initializeEncryptedPreferences();
            Log.d(TAG, "安全存储初始化成功（加密模式）");
        } catch (Exception e) {
            Log.w(TAG, "加密存储初始化失败，使用普通存储", e);
            // 降级到普通SharedPreferences
            securePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            Log.d(TAG, "安全存储初始化成功（普通模式）");
        }
    }
    
    /**
     * 初始化加密存储（可能抛出异常）
     */
    private void initializeEncryptedPreferences() throws Exception {
        // 创建或获取主密钥
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();
        
        // 创建加密的SharedPreferences
        securePrefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }
    
    /**
     * 登录尝试结果
     */
    public static class LoginAttemptResult {
        private final boolean isAllowed;
        private final String message;
        private final int remainingAttempts;
        private final long lockoutRemainingTime;
        
        public LoginAttemptResult(boolean isAllowed, String message, int remainingAttempts, long lockoutRemainingTime) {
            this.isAllowed = isAllowed;
            this.message = message;
            this.remainingAttempts = remainingAttempts;
            this.lockoutRemainingTime = lockoutRemainingTime;
        }
        
        public boolean isAllowed() { return isAllowed; }
        public String getMessage() { return message; }
        public int getRemainingAttempts() { return remainingAttempts; }
        public long getLockoutRemainingTime() { return lockoutRemainingTime; }
    }
    
    /**
     * 检查是否可以进行登录尝试
     * @return 登录尝试结果
     */
    public LoginAttemptResult checkLoginAttempt() {
        long currentTime = System.currentTimeMillis();
        boolean isLocked = securePrefs.getBoolean(ACCOUNT_LOCKED_KEY, false);
        long lockExpiryTime = securePrefs.getLong(LOCK_EXPIRY_TIME_KEY, 0);
        
        // 检查锁定是否已过期
        if (isLocked && currentTime > lockExpiryTime) {
            unlockAccount();
            isLocked = false;
        }
        
        // 如果账户被锁定
        if (isLocked) {
            long remainingTime = lockExpiryTime - currentTime;
            long remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingTime);
            return new LoginAttemptResult(false, 
                "账户已锁定，请" + (remainingMinutes + 1) + "分钟后再试", 
                0, remainingTime);
        }
        
        // 检查尝试次数
        int attempts = securePrefs.getInt(LOGIN_ATTEMPTS_KEY, 0);
        long lastAttemptTime = securePrefs.getLong(LAST_ATTEMPT_TIME_KEY, 0);
        
        // 如果距离上次尝试超过重置时间，重置计数
        if (currentTime - lastAttemptTime > TimeUnit.MINUTES.toMillis(ATTEMPT_RESET_DURATION_MINUTES)) {
            resetLoginAttempts();
            attempts = 0;
        }
        
        int remainingAttempts = MAX_LOGIN_ATTEMPTS - attempts;
        
        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            lockAccount();
            return new LoginAttemptResult(false, 
                "登录尝试次数过多，账户已锁定" + LOCKOUT_DURATION_MINUTES + "分钟", 
                0, TimeUnit.MINUTES.toMillis(LOCKOUT_DURATION_MINUTES));
        }
        
        return new LoginAttemptResult(true, "可以登录", remainingAttempts, 0);
    }
    
    /**
     * 记录登录失败
     */
    public void recordLoginFailure() {
        int attempts = securePrefs.getInt(LOGIN_ATTEMPTS_KEY, 0) + 1;
        long currentTime = System.currentTimeMillis();
        
        securePrefs.edit()
                .putInt(LOGIN_ATTEMPTS_KEY, attempts)
                .putLong(LAST_ATTEMPT_TIME_KEY, currentTime)
                .apply();
        
        Log.d(TAG, "记录登录失败，当前尝试次数: " + attempts);
        
        // 如果达到最大尝试次数，锁定账户
        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            lockAccount();
        }
    }
    
    /**
     * 记录登录成功
     */
    public void recordLoginSuccess() {
        resetLoginAttempts();
        unlockAccount();
        Log.d(TAG, "登录成功，重置安全状态");
    }
    
    /**
     * 锁定账户
     */
    private void lockAccount() {
        long lockExpiryTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(LOCKOUT_DURATION_MINUTES);
        
        securePrefs.edit()
                .putBoolean(ACCOUNT_LOCKED_KEY, true)
                .putLong(LOCK_EXPIRY_TIME_KEY, lockExpiryTime)
                .apply();
        
        Log.d(TAG, "账户已锁定" + LOCKOUT_DURATION_MINUTES + "分钟");
    }
    
    /**
     * 解锁账户
     */
    private void unlockAccount() {
        securePrefs.edit()
                .putBoolean(ACCOUNT_LOCKED_KEY, false)
                .putLong(LOCK_EXPIRY_TIME_KEY, 0)
                .apply();
        
        Log.d(TAG, "账户已解锁");
    }
    
    /**
     * 重置登录尝试计数
     */
    private void resetLoginAttempts() {
        securePrefs.edit()
                .putInt(LOGIN_ATTEMPTS_KEY, 0)
                .putLong(LAST_ATTEMPT_TIME_KEY, 0)
                .apply();
        
        Log.d(TAG, "登录尝试计数已重置");
    }
    
    /**
     * 获取当前登录尝试次数
     * @return 尝试次数
     */
    public int getCurrentAttempts() {
        return securePrefs.getInt(LOGIN_ATTEMPTS_KEY, 0);
    }
    
    /**
     * 安全存储用户凭据
     * @param key 键
     * @param value 值
     */
    public void secureStore(String key, String value) {
        try {
            securePrefs.edit().putString(key, value).apply();
            Log.d(TAG, "安全存储数据: " + key);
        } catch (Exception e) {
            Log.e(TAG, "安全存储失败: " + key, e);
        }
    }
    
    /**
     * 安全获取存储的数据
     * @param key 键
     * @param defaultValue 默认值
     * @return 存储的值
     */
    public String secureRetrieve(String key, String defaultValue) {
        try {
            return securePrefs.getString(key, defaultValue);
        } catch (Exception e) {
            Log.e(TAG, "安全获取数据失败: " + key, e);
            return defaultValue;
        }
    }
    
    /**
     * 安全存储布尔值
     * @param key 键
     * @param value 值
     */
    public void secureStoreBoolean(String key, boolean value) {
        try {
            securePrefs.edit().putBoolean(key, value).apply();
            Log.d(TAG, "安全存储布尔值: " + key);
        } catch (Exception e) {
            Log.e(TAG, "安全存储布尔值失败: " + key, e);
        }
    }
    
    /**
     * 安全获取布尔值
     * @param key 键
     * @param defaultValue 默认值
     * @return 存储的值
     */
    public boolean secureRetrieveBoolean(String key, boolean defaultValue) {
        try {
            return securePrefs.getBoolean(key, defaultValue);
        } catch (Exception e) {
            Log.e(TAG, "安全获取布尔值失败: " + key, e);
            return defaultValue;
        }
    }
    
    /**
     * 清除所有安全存储的数据
     */
    public void clearAllSecureData() {
        try {
            securePrefs.edit().clear().apply();
            Log.d(TAG, "已清除所有安全数据");
        } catch (Exception e) {
            Log.e(TAG, "清除安全数据失败", e);
        }
    }
    
    /**
     * 检查密码是否满足安全要求
     * @param password 密码
     * @return 检查结果
     */
    public static class PasswordSecurityResult {
        private final boolean isSecure;
        private final String[] issues;
        private final String[] suggestions;
        
        public PasswordSecurityResult(boolean isSecure, String[] issues, String[] suggestions) {
            this.isSecure = isSecure;
            this.issues = issues;
            this.suggestions = suggestions;
        }
        
        public boolean isSecure() { return isSecure; }
        public String[] getIssues() { return issues; }
        public String[] getSuggestions() { return suggestions; }
    }
    
    /**
     * 检查密码安全性
     * @param password 密码
     * @return 安全性检查结果
     */
    public static PasswordSecurityResult checkPasswordSecurity(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordSecurityResult(false, 
                new String[]{"密码为空"}, 
                new String[]{"请输入密码"});
        }
        
        java.util.List<String> issues = new java.util.ArrayList<>();
        java.util.List<String> suggestions = new java.util.ArrayList<>();
        
        // 检查长度
        if (password.length() < 8) {
            issues.add("密码太短");
            suggestions.add("使用至少8个字符");
        }
        
        // 检查字符多样性
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\\\\|,.<>/?].*");
        
        int diversity = 0;
        if (hasLower) diversity++;
        if (hasUpper) diversity++;
        if (hasDigit) diversity++;
        if (hasSpecial) diversity++;
        
        if (diversity < 3) {
            issues.add("字符类型不够多样");
            if (!hasLower) suggestions.add("添加小写字母");
            if (!hasUpper) suggestions.add("添加大写字母");
            if (!hasDigit) suggestions.add("添加数字");
            if (!hasSpecial) suggestions.add("添加特殊字符");
        }
        
        // 检查常见弱密码
        String[] commonPasswords = {
            "123456", "password", "123456789", "12345678", "12345",
            "qwerty", "abc123", "admin", "letmein", "welcome"
        };
        
        for (String common : commonPasswords) {
            if (password.toLowerCase().contains(common.toLowerCase())) {
                issues.add("包含常见弱密码模式");
                suggestions.add("避免使用常见密码模式");
                break;
            }
        }
        
        boolean isSecure = issues.isEmpty();
        return new PasswordSecurityResult(isSecure, 
            issues.toArray(new String[0]), 
            suggestions.toArray(new String[0]));
    }
    
    /**
     * 生成安全的随机密码
     * @param length 密码长度
     * @return 随机密码
     */
    public static String generateSecurePassword(int length) {
        if (length < 8) length = 8;
        
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String digits = "0123456789";
        String special = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        String allChars = lowercase + uppercase + digits + special;
        
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder password = new StringBuilder();
        
        // 确保包含每种类型的字符
        password.append(lowercase.charAt(random.nextInt(lowercase.length())));
        password.append(uppercase.charAt(random.nextInt(uppercase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));
        
        // 填充剩余长度
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        // 打乱字符顺序
        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        
        return new String(chars);
    }
}