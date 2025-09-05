package com.wenxing.runyitong.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.WindowManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.graphics.PixelFormat;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.LinearLayout;
import android.util.TypedValue;
import android.view.Gravity;
import android.provider.Settings;

import java.util.List;

public class WeChatAccessibilityService extends AccessibilityService {
    private static final String TAG = "WeChatAccessibilityService";
    private static final String WECHAT_PACKAGE = "com.tencent.mm";
    
    private static WeChatAccessibilityService instance;
    private String searchKeyword;
    private boolean isSearching = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    
    // 悬浮窗相关
    private WindowManager windowManager;
    private View floatingView;
    private boolean isFloatingViewShowing = false;
    
    public static WeChatAccessibilityService getInstance() {
        return instance;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Log.d(TAG, "无障碍服务已创建");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        hideFloatingGuide();
        instance = null;
        Log.d(TAG, "无障碍服务已销毁");
    }
    
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "无障碍服务已连接");
        
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | 
                         AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        info.packageNames = new String[]{WECHAT_PACKAGE};
        setServiceInfo(info);
    }
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!isSearching || searchKeyword == null) {
            return;
        }
        
        String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "";
        if (!WECHAT_PACKAGE.equals(packageName)) {
            return;
        }
        
        Log.d(TAG, "微信窗口事件: " + event.getEventType() + ", 类名: " + event.getClassName());
        
        // 延迟执行搜索操作，确保界面完全加载
        handler.postDelayed(() -> {
            performWeChatSearch();
        }, 1000);
    }
    
    @Override
    public void onInterrupt() {
        Log.d(TAG, "无障碍服务被中断");
    }
    
    /**
     * 开始微信搜索
     */
    public void startWeChatSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            Log.w(TAG, "搜索关键词为空");
            return;
        }
        
        this.searchKeyword = keyword.trim();
        this.isSearching = true;
        
        Log.d(TAG, "开始微信搜索: " + searchKeyword);
        
        // 启动微信
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(WECHAT_PACKAGE);
        if (launchIntent != null) {
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launchIntent);
            Log.d(TAG, "已启动微信应用");
        } else {
            Log.e(TAG, "无法启动微信应用");
            stopSearch();
        }
    }
    
    /**
     * 直接启动微信搜索（强化版）
     * 此方法提供更直接、更高效的微信搜索自动化体验
     */
    public void startWeChatSearchDirectly(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            Log.w(TAG, "搜索关键词为空");
            return;
        }
        
        this.searchKeyword = keyword.trim();
        this.isSearching = true;
        
        Log.d(TAG, "开始直接微信搜索: " + searchKeyword);
        
        // 启动微信
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(WECHAT_PACKAGE);
        if (launchIntent != null) {
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(launchIntent);
            Log.d(TAG, "已启动微信应用");
        } else {
            Log.e(TAG, "无法启动微信应用");
            stopSearch();
        }
    }
    
    /**
     * 执行微信搜索操作
     */
    public void performWeChatSearch() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            Log.w(TAG, "无法获取根节点");
            return;
        }
        
        try {
            // 查找搜索按钮或搜索框
            boolean searchStarted = findAndClickSearch(rootNode);
            
            if (searchStarted) {
                // 延迟输入搜索内容
                handler.postDelayed(() -> {
                    inputSearchKeyword();
                }, 1500);
            }
        } catch (Exception e) {
            Log.e(TAG, "执行搜索操作时出错: " + e.getMessage());
        } finally {
            rootNode.recycle();
        }
    }
    
    /**
     * 查找并点击搜索按钮
     */
    private boolean findAndClickSearch(AccessibilityNodeInfo node) {
        if (node == null) return false;
        
        // 查找搜索相关的节点
        List<AccessibilityNodeInfo> searchNodes = node.findAccessibilityNodeInfosByText("搜索");
        if (!searchNodes.isEmpty()) {
            for (AccessibilityNodeInfo searchNode : searchNodes) {
                if (searchNode.isClickable()) {
                    Log.d(TAG, "找到搜索按钮，正在点击");
                    boolean clicked = searchNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    searchNode.recycle();
                    if (clicked) {
                        return true;
                    }
                }
            }
        }
        
        // 查找搜索框 - 遍历所有子节点寻找EditText
        if (findEditTextAndClick(node)) {
            return true;
        }
        
        // 尝试通过ID查找搜索相关元素
        AccessibilityNodeInfo searchById = node.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cd7").isEmpty() ? 
            null : node.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cd7").get(0);
        if (searchById != null && searchById.isClickable()) {
            Log.d(TAG, "通过ID找到搜索元素，正在点击");
            boolean clicked = searchById.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            searchById.recycle();
            if (clicked) {
                return true;
            }
        }
        
        Log.w(TAG, "未找到可点击的搜索元素");
        return false;
    }
    
    /**
     * 输入搜索关键词
     */
    private void inputSearchKeyword() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            Log.w(TAG, "无法获取根节点进行输入");
            return;
        }
        
        try {
            // 查找输入框并输入内容
            if (findEditTextAndInput(rootNode)) {
                // 延迟后点击搜索按钮
                handler.postDelayed(() -> {
                    clickSearchButton();
                }, 1000);
            }
        } catch (Exception e) {
            Log.e(TAG, "输入搜索关键词时出错: " + e.getMessage());
        } finally {
            rootNode.recycle();
        }
    }
    
    /**
     * 点击搜索按钮
     */
    private void clickSearchButton() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            Log.w(TAG, "无法获取根节点进行搜索");
            stopSearch();
            return;
        }
        
        try {
            // 查找搜索按钮
            List<AccessibilityNodeInfo> searchButtons = rootNode.findAccessibilityNodeInfosByText("搜索");
            
            for (AccessibilityNodeInfo button : searchButtons) {
                if (button.isClickable()) {
                    Log.d(TAG, "点击搜索按钮");
                    button.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    button.recycle();
                    
                    // 延迟后点击公众号标签
                    handler.postDelayed(() -> {
                        clickPublicAccountTab();
                    }, 2000);
                    return;
                }
            }
            
            Log.w(TAG, "未找到搜索按钮");
        } catch (Exception e) {
            Log.e(TAG, "点击搜索按钮时出错: " + e.getMessage());
        } finally {
            rootNode.recycle();
            stopSearch();
        }
    }
    
    /**
     * 点击公众号标签
     */
    private void clickPublicAccountTab() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            Log.w(TAG, "无法获取根节点进行公众号标签点击");
            stopSearch();
            return;
        }
        
        try {
            // 查找公众号标签
            List<AccessibilityNodeInfo> publicAccountNodes = rootNode.findAccessibilityNodeInfosByText("公众号");
            
            for (AccessibilityNodeInfo node : publicAccountNodes) {
                if (node.isClickable()) {
                    Log.d(TAG, "点击公众号标签");
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    node.recycle();
                    
                    Log.d(TAG, "微信搜索自动化完成");
                    stopSearch();
                    return;
                }
            }
            
            Log.d(TAG, "未找到公众号标签，搜索完成");
        } catch (Exception e) {
            Log.e(TAG, "点击公众号标签时出错: " + e.getMessage());
        } finally {
            rootNode.recycle();
            stopSearch();
        }
    }
    
    /**
     * 递归查找EditText并点击
     */
    private boolean findEditTextAndClick(AccessibilityNodeInfo node) {
        if (node == null) return false;
        String strClassName = null;
        if (node.getClassName() != null) {
            strClassName = node.getClassName().toString();
        }
        
        // 检查当前节点是否是EditText
        if ("android.widget.EditText".equals(strClassName)) {
            if (node.isClickable() || node.isFocusable()) {
                Log.d(TAG, "找到搜索框，正在点击");
                boolean clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                if (!clicked) {
                    clicked = node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                }
                if (clicked) {
                    return true;
                }
            }
        }
        
        // 递归检查子节点
        try {
            int childCount = node.getChildCount();
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo child = null;
                try {
                    child = node.getChild(i);
                    if (child != null) {
                        boolean found = findEditTextAndClick(child);
                        child.recycle();
                        if (found) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "获取或访问子节点时出错: " + e.getMessage());
                    if (child != null) {
                        try {
                            child.recycle();
                        } catch (Exception re) {
                            // 忽略回收时的异常
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取子节点数量时出错: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * 递归查找EditText并输入内容
     */
    private boolean findEditTextAndInput(AccessibilityNodeInfo node) {
        if (node == null) return false;
        
        // 检查当前节点是否是EditText
        if ("android.widget.EditText".equals(node.getClassName())) {
            if (node.isFocused() || node.isFocusable()) {
                Log.d(TAG, "找到输入框，正在输入: " + searchKeyword);
                
                // 清空现有内容
                Bundle clearArgs = new Bundle();
                clearArgs.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, 0);
                clearArgs.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, 
                    node.getText() != null ? node.getText().length() : 0);
                node.performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION, clearArgs);
                
                // 输入搜索关键词
                Bundle inputArgs = new Bundle();
                inputArgs.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, searchKeyword);
                boolean inputSuccess = node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, inputArgs);
                
                if (inputSuccess) {
                    Log.d(TAG, "输入成功");
                    return true;
                } else {
                    Log.w(TAG, "输入失败");
                }
            }
        }
        
        // 递归检查子节点
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                if (findEditTextAndInput(child)) {
                    child.recycle();
                    return true;
                }
                child.recycle();
            }
        }
        
        return false;
    }
    
    /**
     * 停止搜索
     */
    private void stopSearch() {
        isSearching = false;
        searchKeyword = null;
        Log.d(TAG, "停止搜索");
    }
    
    /**
     * 显示悬浮指引窗口
     */
    public void showFloatingGuide(String hospitalName) {
        if (isFloatingViewShowing || !canDrawOverlays()) {
            return;
        }
        
        try {
            // 创建悬浮窗布局
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(40, 30, 40, 30);
            
            // 设置背景
            GradientDrawable background = new GradientDrawable();
            background.setColor(Color.parseColor("#F8F9FA"));
            background.setCornerRadius(20);
            background.setStroke(2, Color.parseColor("#E9ECEF"));
            layout.setBackground(background);
            
            // 标题
            TextView titleView = new TextView(this);
            titleView.setText("微信搜索指引");
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            titleView.setTextColor(Color.parseColor("#212529"));
            titleView.setGravity(Gravity.CENTER);
            titleView.setPadding(0, 0, 0, 20);
            layout.addView(titleView);
            
            // 内容
            TextView contentView = new TextView(this);
            String message = "已打开微信，请按以下步骤搜索：" + System.lineSeparator() + System.lineSeparator() +
                    "1. 点击顶部搜索框" + System.lineSeparator() +
                    "2. 输入：" + hospitalName + System.lineSeparator() +
                    "3. 点击搜索按钮" + System.lineSeparator() +
                    "4. 选择'公众号'标签" + System.lineSeparator() +
                    "5. 点击对应的医院公众号";
            contentView.setText(message);
            contentView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            contentView.setTextColor(Color.parseColor("#495057"));
            contentView.setLineSpacing(8, 1.2f);
            contentView.setPadding(0, 0, 0, 20);
            layout.addView(contentView);
            
            // 关闭按钮
            Button closeButton = new Button(this);
            closeButton.setText("我知道了");
            closeButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            closeButton.setTextColor(Color.WHITE);
            
            GradientDrawable buttonBackground = new GradientDrawable();
            buttonBackground.setColor(Color.parseColor("#007BFF"));
            buttonBackground.setCornerRadius(25);
            closeButton.setBackground(buttonBackground);
            
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            buttonParams.topMargin = 10;
            closeButton.setLayoutParams(buttonParams);
            
            closeButton.setOnClickListener(v -> hideFloatingGuide());
            layout.addView(closeButton);
            
            // 设置悬浮窗参数 - 使用SYSTEM_ALERT_WINDOW权限对应的窗口类型
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.TRANSLUCENT
            );
            
            params.gravity = Gravity.CENTER;
            params.x = 0;
            params.y = 0;
            
            floatingView = layout;
            windowManager.addView(floatingView, params);
            isFloatingViewShowing = true;
            
            Log.d(TAG, "悬浮指引窗口已显示");
            
        } catch (Exception e) {
            Log.e(TAG, "显示悬浮窗失败: " + e.getMessage());
        }
    }
    
    /**
     * 隐藏悬浮指引窗口
     */
    public void hideFloatingGuide() {
        if (isFloatingViewShowing && floatingView != null && windowManager != null) {
            try {
                windowManager.removeView(floatingView);
                floatingView = null;
                isFloatingViewShowing = false;
                Log.d(TAG, "悬浮指引窗口已隐藏");
            } catch (Exception e) {
                Log.e(TAG, "隐藏悬浮窗失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 检查是否有悬浮窗权限
     */
    private boolean canDrawOverlays() {
        return Settings.canDrawOverlays(this);
    }
}