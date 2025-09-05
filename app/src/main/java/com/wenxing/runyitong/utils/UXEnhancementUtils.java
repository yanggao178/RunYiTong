package com.wenxing.runyitong.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.wenxing.runyitong.R;

/**
 * 用户体验增强工具类
 * 提供统一的用户反馈机制，包括错误提示、成功反馈、加载状态等
 */
public class UXEnhancementUtils {
    
    private static final String TAG = "UXEnhancementUtils";
    private static final int FEEDBACK_DURATION_SHORT = 2000;
    private static final int FEEDBACK_DURATION_LONG = 4000;
    
    /**
     * 显示增强的错误提示
     * @param inputLayout 输入框布局
     * @param errorMessage 错误消息
     * @param shakeView 需要震动的视图
     */
    public static void showEnhancedError(TextInputLayout inputLayout, String errorMessage, View shakeView) {
        if (inputLayout == null) return;
        
        // 设置错误消息
        inputLayout.setError(errorMessage);
        
        // 添加错误颜色
        inputLayout.setBoxStrokeErrorColor(ContextCompat.getColorStateList(
            inputLayout.getContext(), R.color.error_red));
        
        // 可访问性错误反馈
        AccessibilityManager.announceError(inputLayout, errorMessage);
        
        // 震动动画
        if (shakeView != null) {
            Animation shakeAnimation = AnimationUtils.loadAnimation(
                shakeView.getContext(), R.anim.error_shake);
            shakeView.startAnimation(shakeAnimation);
        }
        
        // 自动清除错误（可选）
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (inputLayout.getError() != null && inputLayout.getError().toString().equals(errorMessage)) {
                clearError(inputLayout);
            }
        }, 5000);
    }
    
    /**
     * 清除错误提示
     * @param inputLayout 输入框布局
     */
    public static void clearError(TextInputLayout inputLayout) {
        if (inputLayout != null) {
            String originalDescription = inputLayout.getHint() != null ? 
                inputLayout.getHint().toString() : "";
            inputLayout.setError(null);
            inputLayout.setBoxStrokeColor(ContextCompat.getColor(
                inputLayout.getContext(), R.color.input_border));
            
            // 清除可访问性错误反馈
            AccessibilityManager.clearErrorAnnouncement(inputLayout, originalDescription);
        }
    }
    
    /**
     * 显示成功反馈
     * @param inputLayout 输入框布局
     * @param view 需要显示成功动画的视图
     */
    public static void showSuccessFeedback(TextInputLayout inputLayout, View view) {
        if (inputLayout == null) return;
        
        // 设置成功状态颜色
        inputLayout.setBoxStrokeColor(ContextCompat.getColor(
            inputLayout.getContext(), R.color.success_green));
        
        // 成功动画
        if (view != null) {
            Animation successAnimation = AnimationUtils.loadAnimation(
                view.getContext(), R.anim.success_scale);
            view.startAnimation(successAnimation);
        }
        
        // 延迟恢复正常状态
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            inputLayout.setBoxStrokeColor(ContextCompat.getColor(
                inputLayout.getContext(), R.color.input_border));
        }, 2000);
    }
    
    /**
     * 显示增强的Toast消息
     * @param context 上下文
     * @param message 消息内容
     * @param type 消息类型 (success, error, warning, info)
     */
    public static void showEnhancedToast(Context context, String message, String type) {
        if (context == null || message == null) return;
        
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        
        // 根据类型设置不同的样式（这里简化处理）
        switch (type.toLowerCase()) {
            case "success":
                // 可以自定义Toast布局来显示不同颜色
                break;
            case "error":
                toast.setDuration(Toast.LENGTH_LONG);
                break;
            case "warning":
                break;
            default: // info
                break;
        }
        
        toast.show();
    }
    
    /**
     * 显示增强的Snackbar
     * @param view 父视图
     * @param message 消息内容
     * @param type 消息类型
     * @param actionText 操作按钮文本
     * @param actionListener 操作按钮监听器
     */
    public static void showEnhancedSnackbar(View view, String message, String type, 
                                          String actionText, View.OnClickListener actionListener) {
        if (view == null || message == null) return;
        
        int duration = type.equals("error") ? Snackbar.LENGTH_LONG : Snackbar.LENGTH_SHORT;
        Snackbar snackbar = Snackbar.make(view, message, duration);
        
        // 设置不同类型的背景颜色
        switch (type.toLowerCase()) {
            case "success":
                snackbar.setBackgroundTint(ContextCompat.getColor(view.getContext(), R.color.success_green));
                break;
            case "error":
                snackbar.setBackgroundTint(ContextCompat.getColor(view.getContext(), R.color.error_red));
                break;
            case "warning":
                snackbar.setBackgroundTint(ContextCompat.getColor(view.getContext(), R.color.warning_orange));
                break;
            default:
                snackbar.setBackgroundTint(ContextCompat.getColor(view.getContext(), R.color.info_blue));
                break;
        }
        
        // 添加操作按钮
        if (actionText != null && actionListener != null) {
            snackbar.setAction(actionText, actionListener);
            snackbar.setActionTextColor(ContextCompat.getColor(view.getContext(), R.color.white));
        }
        
        snackbar.show();
    }
    
    /**
     * 按钮加载状态动画
     * @param button 按钮视图
     * @param isLoading 是否为加载状态
     * @param originalText 原始文本
     */
    public static void setButtonLoadingState(View button, boolean isLoading, String originalText) {
        if (button == null) return;
        
        button.setEnabled(!isLoading);
        
        if (button instanceof TextView) {
            TextView textView = (TextView) button;
            if (isLoading) {
                textView.setText("加载中...");
                // 可访问性加载状态反馈
                AccessibilityManager.announceLoadingState(button, true, "正在" + originalText + "中");
                // 可以添加加载动画
                startLoadingAnimation(button);
            } else {
                textView.setText(originalText);
                AccessibilityManager.announceLoadingState(button, false, null);
                stopLoadingAnimation(button);
            }
        }
    }
    
    /**
     * 开始加载动画
     * @param view 视图
     */
    private static void startLoadingAnimation(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.5f);
        animator.setDuration(1000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        view.setTag(R.id.loading_animator, animator);
        animator.start();
    }
    
    /**
     * 停止加载动画
     * @param view 视图
     */
    private static void stopLoadingAnimation(View view) {
        ObjectAnimator animator = (ObjectAnimator) view.getTag(R.id.loading_animator);
        if (animator != null) {
            animator.cancel();
            view.setAlpha(1.0f);
            view.setTag(R.id.loading_animator, null);
        }
    }
    
    /**
     * 输入框聚焦动画
     * @param inputLayout 输入框布局
     * @param focused 是否聚焦
     */
    public static void handleInputFocusAnimation(TextInputLayout inputLayout, boolean focused) {
        if (inputLayout == null) return;
        
        float targetElevation = focused ? 8.0f : 2.0f;
        float targetScale = focused ? 1.02f : 1.0f;
        
        ObjectAnimator elevationAnimator = ObjectAnimator.ofFloat(inputLayout, "elevation", targetElevation);
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(inputLayout, "scaleX", targetScale);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(inputLayout, "scaleY", targetScale);
        
        elevationAnimator.setDuration(200);
        scaleXAnimator.setDuration(200);
        scaleYAnimator.setDuration(200);
        
        elevationAnimator.start();
        scaleXAnimator.start();
        scaleYAnimator.start();
    }
    
    /**
     * 设置输入框背景状态
     * @param editText 输入框
     * @param state 状态 (normal, error, success, focused)
     */
    public static void setInputBackgroundState(EditText editText, String state) {
        if (editText == null) return;
        
        Context context = editText.getContext();
        Drawable background = null;
        
        switch (state.toLowerCase()) {
            case "error":
                background = ContextCompat.getDrawable(context, R.drawable.enhanced_input_background);
                break;
            case "success":
                background = ContextCompat.getDrawable(context, R.drawable.enhanced_input_background);
                break;
            case "focused":
                background = ContextCompat.getDrawable(context, R.drawable.enhanced_input_background);
                break;
            default: // normal
                background = ContextCompat.getDrawable(context, R.drawable.enhanced_input_background);
                break;
        }
        
        if (background != null) {
            editText.setBackground(background);
        }
    }
    
    /**
     * 渐入动画
     * @param view 视图
     * @param duration 动画时长
     */
    public static void fadeIn(View view, int duration) {
        if (view == null) return;
        
        view.setAlpha(0.0f);
        view.setVisibility(View.VISIBLE);
        
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0.0f, 1.0f);
        animator.setDuration(duration);
        animator.start();
    }
    
    /**
     * 渐出动画
     * @param view 视图
     * @param duration 动画时长
     */
    public static void fadeOut(View view, int duration) {
        if (view == null) return;
        
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.0f);
        animator.setDuration(duration);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
            }
        });
        animator.start();
    }
}