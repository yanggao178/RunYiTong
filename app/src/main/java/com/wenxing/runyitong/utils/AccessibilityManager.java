package com.wenxing.runyitong.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import com.google.android.material.textfield.TextInputLayout;

/**
 * 可访问性增强管理器
 * 提供无障碍功能支持、键盘导航优化、屏幕阅读器支持等
 */
public class AccessibilityManager {
    
    private static final String TAG = "AccessibilityManager";
    
    /**
     * 为视图设置可访问性属性
     * @param view 视图
     * @param contentDescription 内容描述
     * @param hint 提示信息
     */
    public static void setupAccessibility(View view, String contentDescription, String hint) {
        if (view == null) return;
        
        // 设置内容描述
        if (contentDescription != null && !contentDescription.isEmpty()) {
            view.setContentDescription(contentDescription);
        }
        
        // 设置可聚焦
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        
        // 设置可访问性代理
        ViewCompat.setAccessibilityDelegate(view, new androidx.core.view.AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                
                if (hint != null && !hint.isEmpty()) {
                    info.setHintText(hint);
                }
                
                // 设置操作类型
                if (view instanceof Button) {
                    info.setClassName(Button.class.getName());
                    info.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK);
                } else if (view instanceof EditText) {
                    info.setClassName(EditText.class.getName());
                    info.setEditable(true);
                    info.addAction(AccessibilityNodeInfoCompat.ACTION_SET_TEXT);
                    info.addAction(AccessibilityNodeInfoCompat.ACTION_FOCUS);
                }
            }
        });
    }
    
    /**
     * 设置登录表单的可访问性
     * @param context 上下文
     * @param rootView 根视图
     */
    public static void setupLoginFormAccessibility(Context context, ViewGroup rootView) {
        if (rootView == null) return;
        
        // 遍历所有子视图设置可访问性
        setupViewGroupAccessibility(rootView);
    }
    
    /**
     * 递归设置视图组的可访问性
     * @param viewGroup 视图组
     */
    private static void setupViewGroupAccessibility(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            
            if (child instanceof ViewGroup) {
                setupViewGroupAccessibility((ViewGroup) child);
            } else {
                setupIndividualViewAccessibility(child);
            }
        }
    }
    
    /**
     * 设置单个视图的可访问性
     * @param view 视图
     */
    private static void setupIndividualViewAccessibility(View view) {
        if (view == null) return;
        
        String resourceName = "";
        try {
            resourceName = view.getResources().getResourceEntryName(view.getId());
        } catch (Exception e) {
            // 忽略资源名称获取失败
        }
        
        // 根据视图类型和ID设置可访问性
        if (view instanceof EditText) {
            setupEditTextAccessibility((EditText) view, resourceName);
        } else if (view instanceof Button) {
            setupButtonAccessibility((Button) view, resourceName);
        } else if (view instanceof TextView) {
            setupTextViewAccessibility((TextView) view, resourceName);
        } else if (view instanceof ImageView) {
            setupImageViewAccessibility((ImageView) view, resourceName);
        }
    }
    
    /**
     * 设置输入框的可访问性
     * @param editText 输入框
     * @param resourceName 资源名称
     */
    private static void setupEditTextAccessibility(EditText editText, String resourceName) {
        String contentDescription = "";
        String hint = "";
        
        switch (resourceName) {
            case "et_username":
                contentDescription = "用户名输入框";
                hint = "请输入您的用户名";
                break;
            case "et_password":
                contentDescription = "密码输入框";
                hint = "请输入您的密码";
                break;
            case "et_phone":
                contentDescription = "手机号输入框";
                hint = "请输入11位手机号";
                break;
            case "et_verification_code":
                contentDescription = "验证码输入框";
                hint = "请输入6位验证码";
                break;
            case "et_email":
                contentDescription = "邮箱输入框";
                hint = "请输入您的邮箱地址";
                break;
            case "et_confirm_password":
                contentDescription = "确认密码输入框";
                hint = "请再次输入密码";
                break;
            default:
                contentDescription = "文本输入框";
                hint = "请输入内容";
                break;
        }
        
        setupAccessibility(editText, contentDescription, hint);
        
        // 设置输入模式说明
        if (resourceName.contains("password")) {
            ViewCompat.setAccessibilityDelegate(editText, new androidx.core.view.AccessibilityDelegateCompat() {
                @Override
                public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
                    super.onInitializeAccessibilityNodeInfo(host, info);
                    info.setPassword(true);
                    info.setHintText("密码输入，内容将被隐藏");
                }
            });
        }
    }
    
    /**
     * 设置按钮的可访问性
     * @param button 按钮
     * @param resourceName 资源名称
     */
    private static void setupButtonAccessibility(Button button, String resourceName) {
        String contentDescription = "";
        String hint = "";
        
        switch (resourceName) {
            case "btn_login":
                contentDescription = "登录按钮";
                hint = "点击进行账户登录";
                break;
            case "btn_sms_login":
                contentDescription = "手机验证码登录按钮";
                hint = "点击使用手机验证码登录";
                break;
            case "btn_register":
                contentDescription = "注册按钮";
                hint = "点击注册新账户";
                break;
            case "btn_send_code":
                contentDescription = "发送验证码按钮";
                hint = "点击发送手机验证码";
                break;
            case "btn_back_to_login":
                contentDescription = "返回登录按钮";
                hint = "点击返回登录页面";
                break;
            default:
                contentDescription = button.getText() != null ? button.getText().toString() + "按钮" : "按钮";
                hint = "点击执行操作";
                break;
        }
        
        setupAccessibility(button, contentDescription, hint);
        
        // 设置按钮状态说明
        final String finalHint = hint; // 创建final变量供内部类使用
        ViewCompat.setAccessibilityDelegate(button, new androidx.core.view.AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                
                if (!button.isEnabled()) {
                    info.setHintText(finalHint + "（当前不可用）");
                } else {
                    info.setHintText(finalHint);
                }
                
                info.setClickable(button.isClickable());
                info.setEnabled(button.isEnabled());
            }
        });
    }
    
    /**
     * 设置文本视图的可访问性
     * @param textView 文本视图
     * @param resourceName 资源名称
     */
    private static void setupTextViewAccessibility(TextView textView, String resourceName) {
        String contentDescription = "";
        
        switch (resourceName) {
            case "tv_forgot_password":
                contentDescription = "忘记密码链接";
                setupAccessibility(textView, contentDescription, "点击找回密码");
                break;
            case "tv_title":
                contentDescription = "应用标题";
                setupAccessibility(textView, contentDescription, null);
                break;
            default:
                if (textView.getText() != null && !textView.getText().toString().isEmpty()) {
                    contentDescription = textView.getText().toString();
                    setupAccessibility(textView, contentDescription, null);
                }
                break;
        }
    }
    
    /**
     * 设置图像视图的可访问性
     * @param imageView 图像视图
     * @param resourceName 资源名称
     */
    private static void setupImageViewAccessibility(ImageView imageView, String resourceName) {
        String contentDescription = "";
        
        switch (resourceName) {
            case "logo_image":
                contentDescription = "应用标志";
                break;
            case "ic_profile":
                contentDescription = "用户头像图标";
                break;
            case "ic_phone":
                contentDescription = "手机图标";
                break;
            case "ic_lock":
                contentDescription = "密码锁图标";
                break;
            default:
                contentDescription = "图片";
                break;
        }
        
        setupAccessibility(imageView, contentDescription, null);
    }
    
    /**
     * 设置TextInputLayout的可访问性
     * @param textInputLayout TextInputLayout
     * @param label 标签
     * @param hint 提示
     */
    public static void setupTextInputLayoutAccessibility(TextInputLayout textInputLayout, 
                                                        String label, String hint) {
        if (textInputLayout == null) return;
        
        // 设置提示文本
        if (hint != null && !hint.isEmpty()) {
            textInputLayout.setHint(hint);
        }
        
        // 设置可访问性标签
        ViewCompat.setAccessibilityDelegate(textInputLayout, new androidx.core.view.AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                
                if (label != null) {
                    info.setContentDescription(label);
                }
                
                // 如果有错误信息，包含在描述中
                CharSequence error = textInputLayout.getError();
                if (error != null && !error.toString().isEmpty()) {
                    String errorDescription = label + "，错误：" + error.toString();
                    info.setContentDescription(errorDescription);
                    info.setError(error);
                }
            }
        });
    }
    
    /**
     * 设置错误状态的可访问性反馈
     * @param view 视图
     * @param errorMessage 错误消息
     */
    public static void announceError(View view, String errorMessage) {
        if (view == null || errorMessage == null) return;
        
        // 发送可访问性事件
        view.sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent.TYPE_VIEW_FOCUSED);
        
        // 设置内容描述包含错误信息
        String originalDescription = view.getContentDescription() != null ? 
            view.getContentDescription().toString() : "";
        view.setContentDescription(originalDescription + "，错误：" + errorMessage);
        
        // 发送错误通知
        view.announceForAccessibility("输入错误：" + errorMessage);
    }
    
    /**
     * 清除错误状态的可访问性反馈
     * @param view 视图
     * @param originalDescription 原始描述
     */
    public static void clearErrorAnnouncement(View view, String originalDescription) {
        if (view == null) return;
        
        view.setContentDescription(originalDescription);
        view.announceForAccessibility("输入正确");
    }
    
    /**
     * 设置键盘导航顺序
     * @param views 视图数组，按导航顺序排列
     */
    public static void setupKeyboardNavigation(View... views) {
        if (views == null || views.length == 0) return;
        
        for (int i = 0; i < views.length; i++) {
            View currentView = views[i];
            if (currentView == null) continue;
            
            // 设置可聚焦
            currentView.setFocusable(true);
            currentView.setFocusableInTouchMode(true);
            
            // 设置下一个焦点
            if (i < views.length - 1) {
                View nextView = views[i + 1];
                if (nextView != null) {
                    currentView.setNextFocusDownId(nextView.getId());
                    currentView.setNextFocusForwardId(nextView.getId());
                }
            }
            
            // 设置上一个焦点
            if (i > 0) {
                View prevView = views[i - 1];
                if (prevView != null) {
                    currentView.setNextFocusUpId(prevView.getId());
                }
            }
        }
    }
    
    /**
     * 为加载状态设置可访问性反馈
     * @param view 视图
     * @param isLoading 是否加载中
     * @param loadingMessage 加载消息
     */
    public static void announceLoadingState(View view, boolean isLoading, String loadingMessage) {
        if (view == null) return;
        
        if (isLoading) {
            String message = loadingMessage != null ? loadingMessage : "正在加载";
            view.announceForAccessibility(message);
            
            // 设置加载状态描述
            ViewCompat.setAccessibilityDelegate(view, new androidx.core.view.AccessibilityDelegateCompat() {
                @Override
                public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
                    super.onInitializeAccessibilityNodeInfo(host, info);
                    info.setContentDescription(message);
                    info.setEnabled(false);
                }
            });
        } else {
            view.announceForAccessibility("加载完成");
        }
    }
    
    /**
     * 检查是否启用了无障碍服务
     * @param context 上下文
     * @return 是否启用
     */
    public static boolean isAccessibilityEnabled(Context context) {
        try {
            android.view.accessibility.AccessibilityManager accessibilityManager = 
                (android.view.accessibility.AccessibilityManager) 
                context.getSystemService(Context.ACCESSIBILITY_SERVICE);
            
            return accessibilityManager != null && accessibilityManager.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查是否启用了触摸探索模式
     * @param context 上下文
     * @return 是否启用
     */
    public static boolean isTouchExplorationEnabled(Context context) {
        try {
            android.view.accessibility.AccessibilityManager accessibilityManager = 
                (android.view.accessibility.AccessibilityManager) 
                context.getSystemService(Context.ACCESSIBILITY_SERVICE);
            
            return accessibilityManager != null && accessibilityManager.isTouchExplorationEnabled();
        } catch (Exception e) {
            return false;
        }
    }
}