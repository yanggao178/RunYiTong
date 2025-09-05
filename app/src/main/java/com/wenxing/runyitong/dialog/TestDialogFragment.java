package com.wenxing.runyitong.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * 测试用的简单DialogFragment
 * 用于验证基本的DialogFragment功能是否正常
 */
public class TestDialogFragment extends DialogFragment {
    
    private static final String TAG = "TestDialogFragment";
    
    public static TestDialogFragment newInstance() {
        android.util.Log.d(TAG, "创建TestDialogFragment实例");
        return new TestDialogFragment();
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d(TAG, "onCreate调用");
        
        // 设置对话框样式
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Material_Dialog);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        android.util.Log.d(TAG, "onCreateView调用");
        
        try {
            // 创建简单的布局
            TextView textView = new TextView(requireContext());
            textView.setText("这是一个测试对话框\n\n如果你能看到这个对话框，说明DialogFragment基本功能正常。\n\n点击外部区域关闭对话框。");
            textView.setPadding(60, 60, 60, 60);
            textView.setTextSize(16);
            textView.setBackgroundColor(0xFFFFFFFF);
            
            android.util.Log.d(TAG, "测试对话框视图创建成功");
            return textView;
            
        } catch (Exception e) {
            android.util.Log.e(TAG, "创建对话框视图时发生异常: " + e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public void onStart() {
        super.onStart();
        android.util.Log.d(TAG, "onStart调用");
        
        try {
            Dialog dialog = getDialog();
            if (dialog != null && dialog.getWindow() != null) {
                // 设置对话框大小
                dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.8),
                    ViewGroup.LayoutParams.WRAP_CONTENT
                );
                android.util.Log.d(TAG, "对话框窗口属性设置成功");
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "设置对话框属性时发生异常: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        android.util.Log.d(TAG, "onResume调用 - 对话框应该已经显示");
    }
    
    @Override
    public void onPause() {
        super.onPause();
        android.util.Log.d(TAG, "onPause调用");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        android.util.Log.d(TAG, "onDestroy调用");
    }
}