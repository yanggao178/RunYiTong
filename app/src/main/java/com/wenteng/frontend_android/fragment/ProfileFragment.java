package com.wenteng.frontend_android.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.wenteng.frontend_android.R;
import com.wenteng.frontend_android.activity.LoginActivity;
import com.wenteng.frontend_android.utils.DiagnosticUtils;
import com.wenteng.frontend_android.activity.OrderActivity;
import com.wenteng.frontend_android.activity.SettingsActivity;
import com.wenteng.frontend_android.activity.MyAppointmentsActivity;
import com.wenteng.frontend_android.activity.MyPrescriptionsActivity;

public class ProfileFragment extends Fragment {

    private Button btnExpressOrders, btnLogin, btnLogout;
    private CardView cardSettings, cardMyAppointments, cardMyPrescriptions;
    private TextView tvUsername, tvWelcome;
    private SharedPreferences sharedPreferences;
    
    // SharedPreferences相关常量
    private static final String PREFS_NAME = "user_login_state";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USERNAME = "username";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        initViews(view);
        setupClickListeners();
        
        return view;
    }
    
    private void initViews(View view) {
        btnExpressOrders = view.findViewById(R.id.btn_express_orders);
        btnLogin = view.findViewById(R.id.btn_login);
        btnLogout = view.findViewById(R.id.btn_logout);
        cardSettings = view.findViewById(R.id.card_settings);
        tvUsername = view.findViewById(R.id.tv_username);
        tvWelcome = view.findViewById(R.id.tv_welcome);
        cardMyAppointments = view.findViewById(R.id.card_my_appointments);
        cardMyPrescriptions = view.findViewById(R.id.card_my_prescriptions);
        
        // 初始化SharedPreferences
        if (getActivity() != null) {
            sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, getActivity().MODE_PRIVATE);
        }
        
        // 检查关键视图是否找到
        if (btnLogin == null) {
            android.util.Log.e("ProfileFragment", "btnLogin not found in layout");
        }
        if (btnExpressOrders == null) {
            android.util.Log.e("ProfileFragment", "btnExpressOrders not found in layout");
        }
        if (cardSettings == null) {
            android.util.Log.e("ProfileFragment", "cardSettings not found in layout");
        }
        if (cardMyAppointments == null) {
            android.util.Log.e("ProfileFragment", "cardMyAppointments not found in layout");
        }
        if (cardMyPrescriptions == null) {
            android.util.Log.e("ProfileFragment", "cardMyPrescriptions not found in layout");
        }
        
        updateLoginStatus();
    }
    
    private void setupClickListeners() {
        // 登录按钮 - 仅用于跳转到登录页面
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                try {
                    android.util.Log.d("ProfileFragment", "Login button clicked");
                    
                    // 简化的启动逻辑，移除过度的诊断检查
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    
                    // 检查Activity是否可用
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        startActivity(intent);
                        android.util.Log.d("ProfileFragment", "LoginActivity started successfully");
                    } else {
                        android.util.Log.w("ProfileFragment", "Activity not available for starting LoginActivity");
                        Toast.makeText(getContext(), "无法启动登录页面，请重试", Toast.LENGTH_SHORT).show();
                    }
                    
                } catch (ActivityNotFoundException e) {
                    android.util.Log.e("ProfileFragment", "LoginActivity not found", e);
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "登录页面未找到", Toast.LENGTH_SHORT).show();
                    }
                } catch (SecurityException e) {
                    android.util.Log.e("ProfileFragment", "Security exception when starting LoginActivity", e);
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "权限不足，无法启动登录页面", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    android.util.Log.e("ProfileFragment", "Unexpected error when starting LoginActivity", e);
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "启动登录页面时发生错误：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        
        // 退出登录按钮 - 仅用于退出登录
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                if (getActivity() != null && !getActivity().isFinishing()) {
                    try {
                        performLogout();
                    } catch (Exception e) {
                        android.util.Log.e("ProfileFragment", "退出登录失败: " + e.getMessage(), e);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "退出登录失败，请重试", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
        
        if (btnExpressOrders != null) {
            btnExpressOrders.setOnClickListener(v -> {
                if (getActivity() == null || getActivity().isFinishing()) {
                    return;
                }
                
                if (!getLoginState()) {
                    // 显示登录对话框
                    showLoginDialog();
                    return;
                }
                
                try {
                    Intent intent = new Intent(getActivity(), OrderActivity.class);
                    startActivity(intent);
                    android.util.Log.d("ProfileFragment", "启动OrderActivity");
                } catch (Exception e) {
                    android.util.Log.e("ProfileFragment", "启动OrderActivity失败: " + e.getMessage(), e);
                    Toast.makeText(getActivity(), "无法打开订单页面，请重试", Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        if (cardSettings != null) {
            cardSettings.setOnClickListener(v -> {
                if (getActivity() != null && !getActivity().isFinishing()) {
                    try {
                        Intent intent = new Intent(getActivity(), SettingsActivity.class);
                        startActivity(intent);
                        android.util.Log.d("ProfileFragment", "启动SettingsActivity");
                    } catch (Exception e) {
                        android.util.Log.e("ProfileFragment", "启动SettingsActivity失败: " + e.getMessage(), e);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "无法打开设置页面，请重试", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
        
        // 我的预约点击事件
        if (cardMyAppointments != null) {
            cardMyAppointments.setOnClickListener(v -> {
                if (getActivity() == null || getActivity().isFinishing()) {
                    return;
                }
                
                if (!getLoginState()) {
                    // 显示登录对话框
                    showLoginDialog();
                    return;
                }
                
                try {
                    Intent intent = new Intent(getActivity(), MyAppointmentsActivity.class);
                    startActivity(intent);
                    android.util.Log.d("ProfileFragment", "启动MyAppointmentsActivity");
                } catch (Exception e) {
                    android.util.Log.e("ProfileFragment", "启动MyAppointmentsActivity失败: " + e.getMessage(), e);
                    Toast.makeText(getActivity(), "无法打开预约页面，请重试", Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        // 我的处方点击事件
        if (cardMyPrescriptions != null) {
            cardMyPrescriptions.setOnClickListener(v -> {
                if (getActivity() == null || getActivity().isFinishing()) {
                    return;
                }
                
                if (!getLoginState()) {
                    // 显示登录对话框
                    showLoginDialog();
                    return;
                }
                
                try {
                    Intent intent = new Intent(getActivity(), MyPrescriptionsActivity.class);
                    startActivity(intent);
                    android.util.Log.d("ProfileFragment", "启动MyPrescriptionsActivity");
                    Toast.makeText(getActivity(), "启动我的处方成功", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    android.util.Log.e("ProfileFragment", "启动MyPrescriptionsActivity失败: " + e.getMessage(), e);
                    Toast.makeText(getActivity(), "无法打开处方页面，请重试", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    /**
     * 显示登录对话框
     */
    private void showLoginDialog() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        
        new AlertDialog.Builder(getActivity())
                .setTitle("提示")
                .setMessage("请先登录")
                .setPositiveButton("去登录", (dialog, which) -> {
                    try {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        android.util.Log.d("ProfileFragment", "用户选择去登录");
                    } catch (Exception e) {
                        android.util.Log.e("ProfileFragment", "启动LoginActivity失败: " + e.getMessage(), e);
                        Toast.makeText(getActivity(), "无法打开登录页面，请重试", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    dialog.dismiss();
                    android.util.Log.d("ProfileFragment", "用户取消登录");
                })
                .setCancelable(true)
                .show();
    }
    
    /**
     * 执行退出登录操作
     */
    private void performLogout() {
        // 清除登录状态
        saveLoginState(false, "");
        updateLoginStatus();
        
        if (getActivity() != null) {
            Toast.makeText(getActivity(), "已成功退出登录", Toast.LENGTH_SHORT).show();
        }
        
        android.util.Log.d("ProfileFragment", "用户已退出登录");
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
            android.util.Log.d("ProfileFragment", "登录状态已保存: " + isLoggedIn + ", 用户名: " + username);
        }
    }
    
    /**
     * 从SharedPreferences读取登录状态
     */
    private boolean getLoginState() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        }
        return false;
    }
    
    /**
     * 从SharedPreferences读取用户名
     */
    private String getSavedUsername() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(KEY_USERNAME, "未登录");
        }
        return "未登录";
    }
    
    /**
     * 更新登录状态UI
     */
    private void updateLoginStatus() {
        boolean isLoggedIn = getLoginState();
        String username = getSavedUsername();
        
        if (isLoggedIn) {
            // 已登录状态
            tvUsername.setText(username);
            tvWelcome.setText("欢迎回来！");
            
            // 显示退出登录按钮，隐藏登录按钮
            if (btnLogin != null) {
                btnLogin.setVisibility(View.GONE);
            }
            if (btnLogout != null) {
                btnLogout.setVisibility(View.VISIBLE);
            }
        } else {
            // 未登录状态
            tvUsername.setText("未登录");
            tvWelcome.setText("请登录以使用完整功能");
            
            // 显示登录按钮，隐藏退出登录按钮
            if (btnLogin != null) {
                btnLogin.setVisibility(View.VISIBLE);
            }
            if (btnLogout != null) {
                btnLogout.setVisibility(View.GONE);
            }
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 刷新登录状态UI
        updateLoginStatus();
    }
    
    /**
     * 模拟登录成功，保存登录状态
     * 在实际应用中，这个方法应该在LoginActivity登录成功后调用
     */
    public void simulateLoginSuccess(String username) {
        saveLoginState(true, username);
        updateLoginStatus();
        android.util.Log.d("ProfileFragment", "模拟登录成功: " + username);
    }
}