package com.wenteng.frontend_android.fragment;

import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.wenteng.frontend_android.R;
import androidx.cardview.widget.CardView;
import com.wenteng.frontend_android.R;
import com.wenteng.frontend_android.activity.LoginActivity;
import com.wenteng.frontend_android.activity.OrderActivity;
import com.wenteng.frontend_android.activity.SettingsActivity;

public class ProfileFragment extends Fragment {

    private Button btnExpressOrders, btnLogin;
    private CardView cardSettings;
    private TextView tvUsername, tvWelcome;
    private boolean isLoggedIn = false; // 简单的登录状态管理

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
        cardSettings = view.findViewById(R.id.card_settings);
        tvUsername = view.findViewById(R.id.tv_username);
        tvWelcome = view.findViewById(R.id.tv_welcome);
        
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
        
        updateLoginStatus();
    }
    
    private void setupClickListeners() {
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                try {
                    // 检查Fragment是否已添加到Activity并且Activity不为空
                    if (!isAdded() || getActivity() == null || getActivity().isFinishing()) {
                        android.util.Log.w("ProfileFragment", "Fragment未正确附加到Activity或Activity正在结束");
                        return;
                    }
                    
                    // 检查Context是否可用
                    if (getContext() == null) {
                        android.util.Log.w("ProfileFragment", "Context为空，无法启动Activity");
                        return;
                    }
                    
                    if (!isLoggedIn) {
                        // 跳转到登录页面
                        // 使用完整的类路径创建Intent
                        Intent intent = new Intent();
                        intent.setClassName(getActivity().getPackageName(), "com.wenteng.frontend_android.activity.LoginActivity");
                        
                        // 添加Intent标志以确保正确启动
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        
                        // 验证Intent是否可以被解析
                        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivity(intent);
                            android.util.Log.d("ProfileFragment", "成功启动LoginActivity");
                        } else {
                            android.util.Log.e("ProfileFragment", "无法解析LoginActivity Intent");
                            Toast.makeText(getContext(), "登录页面暂时无法打开，请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // 退出登录
                        isLoggedIn = false;
                        updateLoginStatus();
                        Toast.makeText(getActivity(), "已退出登录", Toast.LENGTH_SHORT).show();
                    }
                    
                } catch (android.content.ActivityNotFoundException e) {
                    android.util.Log.e("ProfileFragment", "LoginActivity未找到", e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "登录页面未找到，请检查应用配置", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    android.util.Log.e("ProfileFragment", "启动LoginActivity时发生未知错误", e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "页面跳转失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        
        if (btnExpressOrders != null) {
            btnExpressOrders.setOnClickListener(v -> {
            if (getActivity() == null) {
                android.util.Log.e("ProfileFragment", "Activity is null, cannot perform action");
                return;
            }
            
            if (!isLoggedIn) {
                Toast.makeText(getActivity(), "请先登录", Toast.LENGTH_SHORT).show();
                try {
                    Intent intent = new Intent(getActivity(), com.wenteng.frontend_android.activity.LoginActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    android.util.Log.e("ProfileFragment", "Error starting LoginActivity from orders: " + e.getMessage(), e);
                    Toast.makeText(getActivity(), "无法打开登录页面: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                return;
            }
            
            try {
                Toast.makeText(getActivity(), "正在打开订单页面...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), OrderActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "打开订单页面失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            });
        }
        
        if (cardSettings != null) {
            cardSettings.setOnClickListener(v -> {
            if (getActivity() == null) {
                android.util.Log.e("ProfileFragment", "Activity is null, cannot open settings");
                return;
            }
            
            try {
                Toast.makeText(getActivity(), "正在打开设置页面...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "打开设置页面失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            });
        }
    }
    
    private void updateLoginStatus() {
        if (isLoggedIn) {
            tvUsername.setText("admin");
            tvWelcome.setText("欢迎回来！");
            btnLogin.setText("退出登录");
        } else {
            tvUsername.setText("未登录");
            tvWelcome.setText("请登录以使用完整功能");
            btnLogin.setText("登录/注册");
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 这里可以检查登录状态，简单演示设为已登录
        // 在实际应用中，应该从SharedPreferences或其他存储中读取登录状态
        updateLoginStatus();
    }
}