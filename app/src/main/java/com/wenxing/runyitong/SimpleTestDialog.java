package com.wenxing.runyitong;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * 简化的测试对话框，用于验证对话框显示功能
 */
public class SimpleTestDialog extends Dialog {
    private static final String TAG = "SimpleTestDialog";
    
    public SimpleTestDialog(Context context) {
        super(context, R.style.CustomDialogTheme);
        Log.d(TAG, "SimpleTestDialog constructor called");
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        
        // 创建简单的布局
        setContentView(R.layout.dialog_simple_test);
        
        TextView titleText = findViewById(R.id.title_text);
        Button closeButton = findViewById(R.id.close_button);
        
        if (titleText != null) {
            titleText.setText("测试对话框");
        }
        
        if (closeButton != null) {
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Close button clicked");
                    dismiss();
                }
            });
        }
        
        // 设置对话框属性
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        
        Log.d(TAG, "Dialog setup completed");
    }
    
    @Override
    public void show() {
        Log.d(TAG, "show() called");
        try {
            super.show();
            Log.d(TAG, "Dialog shown successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error showing dialog: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void dismiss() {
        Log.d(TAG, "dismiss() called");
        try {
            super.dismiss();
            Log.d(TAG, "Dialog dismissed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error dismissing dialog: " + e.getMessage(), e);
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }
}