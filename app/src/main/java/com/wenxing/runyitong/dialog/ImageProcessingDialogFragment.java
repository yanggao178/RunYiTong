package com.wenxing.runyitong.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.wenxing.runyitong.R;
import com.wenxing.runyitong.debug.DialogDebugHelper;

/**
 * 图片处理选项对话框Fragment
 * 使用DialogFragment替代AlertDialog，提供更好的生命周期管理
 */
public class ImageProcessingDialogFragment extends DialogFragment {
    
    private static final String TAG = "ImageProcessingDialog";
    
    // 回调接口
    public interface OnProcessingOptionSelectedListener {
        void onXRaySelected();
        void onCTSelected();
        void onUltrasoundSelected();
        void onMRISelected();
        void onPETCTSelected();
        void onUploadSelected();
        void onPreviewSelected();
        void onDialogCancelled();
    }
    
    private OnProcessingOptionSelectedListener listener;
    
    // public static ImageProcessingDialogFragment newInstance() {
    //     ImageProcessingDialogFragment fragment = new ImageProcessingDialogFragment();
    //     Bundle args = new Bundle();
    //     // 可以在这里添加需要传递的参数
    //     // args.putString("key", "value");
    //     fragment.setArguments(args);
    //     return fragment;
    // }
    public static ImageProcessingDialogFragment newInstance(Uri imageUri, String imageSource, boolean enablePreview) {
        ImageProcessingDialogFragment fragment = new ImageProcessingDialogFragment();
        Bundle args = new Bundle();
        
        // 图片相关参数
        if (imageUri != null) {
            args.putString("image_uri", imageUri.toString());
        }
        args.putString("image_source", imageSource); // "camera" 或 "gallery"
        
        // 功能控制参数
        args.putBoolean("enable_preview", enablePreview);
        args.putBoolean("enable_ocr", true);
        args.putBoolean("enable_analysis", true);
        args.putBoolean("enable_upload", true);
        
        // 显示配置参数
        args.putString("dialog_title", "图片处理选项");
        args.putInt("dialog_width_percent", 92); // 对话框宽度百分比
        
        fragment.setArguments(args);
        return fragment;
}
    public void setOnProcessingOptionSelectedListener(OnProcessingOptionSelectedListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "=== 开始创建增强版自定义对话框 ===");
        Log.d(TAG, "Context: " + (getContext() != null) + ", Activity: " + (getActivity() != null));
        
        // 从Bundle中读取参数
        Bundle args = getArguments();
        if (args != null) {
            String imageUriString = args.getString("image_uri");
            String imageSource = args.getString("image_source", "unknown");
            boolean enablePreview = args.getBoolean("enable_preview", true);
            String dialogTitle = args.getString("dialog_title", "图片处理选项");
            int dialogWidthPercent = args.getInt("dialog_width_percent", 92);
            
            Log.d(TAG, "读取参数 - imageUri: " + imageUriString + ", imageSource: " + imageSource + ", enablePreview: " + enablePreview);
        }
        
        try {
            // 检查Context
            if (getContext() == null) {
                Log.e(TAG, "Context为null，无法创建对话框");
                throw new IllegalStateException("Context is null");
            }
            
            Log.d(TAG, "开始创建Dialog实例");
            Dialog dialog = new Dialog(requireContext(), R.style.CustomDialogTheme);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            Log.d(TAG, "Dialog实例创建成功");
            
            // 设置布局
            Log.d(TAG, "开始加载布局文件: dialog_image_processing_options");
            LayoutInflater inflater = LayoutInflater.from(getContext());
            if (inflater == null) {
                Log.e(TAG, "LayoutInflater为null");
                throw new RuntimeException("LayoutInflater is null");
            }
            
            View view = inflater.inflate(R.layout.dialog_image_processing_options, null);
            if (view == null) {
                Log.e(TAG, "无法加载对话框布局文件");
                throw new RuntimeException("Dialog layout inflation failed");
            }
            Log.d(TAG, "布局文件加载成功");
            
            dialog.setContentView(view);
            Log.d(TAG, "对话框内容视图设置完成");
            
            // 配置窗口属性
            Window window = dialog.getWindow();
            if (window != null) {
                Log.d(TAG, "开始配置对话框窗口属性");
                
                // 设置背景透明
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                
                // 设置窗口大小和位置
                WindowManager.LayoutParams layoutParams = window.getAttributes();
                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                layoutParams.width = (int) (screenWidth * 0.92);
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                Log.d(TAG, "窗口尺寸设置: width=" + layoutParams.width + ", height=WRAP_CONTENT");
                
                // 设置窗口动画
                layoutParams.windowAnimations = R.style.DialogAnimation;
                
                // 设置窗口标志
                window.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                        WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                window.setDimAmount(0.6f);
                
                window.setAttributes(layoutParams);
                Log.d(TAG, "窗口属性配置完成");
            } else {
                Log.w(TAG, "无法获取对话框窗口对象");
            }
            
            // 设置点击事件
            Log.d(TAG, "开始设置点击事件监听器");
            setupClickListeners(view, dialog);
            Log.d(TAG, "点击事件监听器设置完成");
            
            // 设置对话框属性
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            Log.d(TAG, "对话框基本属性设置完成");
            
            // 添加进入动画
            Log.d(TAG, "添加进入动画");
            addEnterAnimation(view);
            
            Log.d(TAG, "=== 增强版对话框创建完成 ===");
            return dialog;
            
        } catch (Exception e) {
            Log.e(TAG, "创建对话框时发生异常: " + e.getMessage(), e);
            e.printStackTrace();
            
            // 创建简单的备用对话框
            Log.d(TAG, "尝试创建备用对话框");
            try {
                return createFallbackDialog();
            } catch (Exception fallbackException) {
                Log.e(TAG, "创建备用对话框也失败: " + fallbackException.getMessage(), fallbackException);
                // 返回最简单的对话框
                return new android.app.AlertDialog.Builder(requireContext())
                    .setTitle("图片处理选项")
                    .setMessage("对话框加载失败，请重试")
                    .setPositiveButton("确定", null)
                    .create();
            }
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 当使用onCreateDialog时，这个方法不会被调用
        return null;
    }
    
    /**
     * 创建备用对话框（当主对话框创建失败时使用）
     */
    private Dialog createFallbackDialog() {
        Log.d(TAG, "创建备用对话框");
        
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // 创建简单的线性布局
        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        layout.setBackgroundColor(Color.WHITE);
        
        // 添加标题
        android.widget.TextView title = new android.widget.TextView(getContext());
        title.setText("选择处理方式");
        title.setTextSize(18);
        title.setTextColor(Color.BLACK);
        title.setPadding(0, 0, 0, 24);
        layout.addView(title);
        
        // 添加选项按钮
        String[] options = {"OCR文字识别", "处方智能分析", "上传到服务器", "预览图片"};
        for (int i = 0; i < options.length; i++) {
            android.widget.Button button = new android.widget.Button(getContext());
            button.setText(options[i]);
            button.setPadding(16, 16, 16, 16);
            
            final int index = i;
            button.setOnClickListener(v -> {
                handleOptionSelected(index);
                dismiss();
            });
            
            layout.addView(button);
        }
        
        // 添加取消按钮
        android.widget.Button cancelButton = new android.widget.Button(getContext());
        cancelButton.setText("取消");
        cancelButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDialogCancelled();
            }
            dismiss();
        });
        layout.addView(cancelButton);
        
        dialog.setContentView(layout);
        
        // 配置窗口
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);
        }
        
        return dialog;
    }
    
    /**
     * 处理选项选择
     */
    private void handleOptionSelected(int index) {
        if (listener == null) return;
        
        switch (index) {
            case 0:
                listener.onXRaySelected();
                break;
            case 1:
                listener.onCTSelected();
                break;
            case 2:
                listener.onUltrasoundSelected();
                break;
            case 3:
                listener.onMRISelected();
                break;
            case 4:
                listener.onPETCTSelected();
                break;
            case 5:
                listener.onUploadSelected();
                break;
            case 6:
                listener.onPreviewSelected();
                break;
        }
    }
    
    /**
     * 添加进入动画
     */
    private void addEnterAnimation(View view) {
        try {
            // 设置初始状态
            view.setAlpha(0f);
            view.setScaleX(0.8f);
            view.setScaleY(0.8f);
            
            // 执行动画
            view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
                
        } catch (Exception e) {
            Log.w(TAG, "添加进入动画失败: " + e.getMessage());
            // 如果动画失败，确保视图可见
            view.setAlpha(1f);
            view.setScaleX(1f);
            view.setScaleY(1f);
        }
    }
    
    private void setupClickListeners(View view, Dialog dialog) {
        Log.d(TAG, "设置增强版点击事件监听器");
        
        try {
            // X光分析
            View xrayCard = view.findViewById(R.id.card_xray);
            if (xrayCard != null) {
                xrayCard.setOnClickListener(v -> {
                    Log.d(TAG, "用户选择X光分析");
                    addClickAnimation(v);
                    performDelayedAction(() -> {
                        if (listener != null) {
                            listener.onXRaySelected();
                        }
                        dismissWithAnimation();
                    });
                });
                Log.d(TAG, "X光卡片点击事件设置成功");
            } else {
                Log.e(TAG, "找不到X光卡片视图 (R.id.card_xray)");
            }
            
            // CT分析
            View ctCard = view.findViewById(R.id.card_ct);
            if (ctCard != null) {
                ctCard.setOnClickListener(v -> {
                    Log.d(TAG, "用户选择CT分析");
                    addClickAnimation(v);
                    performDelayedAction(() -> {
                        if (listener != null) {
                            listener.onCTSelected();
                        }
                        dismissWithAnimation();
                    });
                });
                Log.d(TAG, "CT卡片点击事件设置成功");
            } else {
                Log.e(TAG, "找不到CT卡片视图 (R.id.card_ct)");
            }
            
            // B超分析
            View ultrasoundCard = view.findViewById(R.id.card_ultrasound);
            if (ultrasoundCard != null) {
                ultrasoundCard.setOnClickListener(v -> {
                    Log.d(TAG, "用户选择B超分析");
                    addClickAnimation(v);
                    performDelayedAction(() -> {
                        if (listener != null) {
                            listener.onUltrasoundSelected();
                        }
                        dismissWithAnimation();
                    });
                });
                Log.d(TAG, "B超卡片点击事件设置成功");
            } else {
                Log.e(TAG, "找不到B超卡片视图 (R.id.card_ultrasound)");
            }
            
            // MRI分析
            View mriCard = view.findViewById(R.id.card_mri);
            if (mriCard != null) {
                mriCard.setOnClickListener(v -> {
                    Log.d(TAG, "用户选择MRI分析");
                    addClickAnimation(v);
                    performDelayedAction(() -> {
                        if (listener != null) {
                            listener.onMRISelected();
                        }
                        dismissWithAnimation();
                    });
                });
                Log.d(TAG, "MRI卡片点击事件设置成功");
            } else {
                Log.e(TAG, "找不到MRI卡片视图 (R.id.card_mri)");
            }
            
            // PET-CT分析
            View petctCard = view.findViewById(R.id.card_petct);
            if (petctCard != null) {
                petctCard.setOnClickListener(v -> {
                    Log.d(TAG, "用户选择PET-CT分析");
                    addClickAnimation(v);
                    performDelayedAction(() -> {
                        if (listener != null) {
                            listener.onPETCTSelected();
                        }
                        dismissWithAnimation();
                    });
                });
                Log.d(TAG, "PET-CT卡片点击事件设置成功");
            } else {
                Log.e(TAG, "找不到PET-CT卡片视图 (R.id.card_petct)");
            }
            
            // 上传到服务器
            View uploadCard = view.findViewById(R.id.card_upload);
            if (uploadCard != null) {
                uploadCard.setOnClickListener(v -> {
                    Log.d(TAG, "用户选择上传服务器");
                    addClickAnimation(v);
                    performDelayedAction(() -> {
                        if (listener != null) {
                            listener.onUploadSelected();
                        }
                        dismissWithAnimation();
                    });
                });
                Log.d(TAG, "上传卡片点击事件设置成功");
            } else {
                Log.e(TAG, "找不到上传卡片视图 (R.id.card_upload)");
            }
            
            // 预览图片
            View previewCard = view.findViewById(R.id.card_preview);
            if (previewCard != null) {
                previewCard.setOnClickListener(v -> {
                    Log.d(TAG, "用户选择预览图片");
                    addClickAnimation(v);
                    performDelayedAction(() -> {
                        if (listener != null) {
                            listener.onPreviewSelected();
                        }
                        dismissWithAnimation();
                    });
                });
                Log.d(TAG, "预览卡片点击事件设置成功");
            } else {
                Log.e(TAG, "找不到预览卡片视图 (R.id.card_preview)");
            }
            
            // 关闭按钮
            View closeButton = view.findViewById(R.id.iv_close);
            if (closeButton != null) {
                closeButton.setOnClickListener(v -> {
                    Log.d(TAG, "用户点击关闭按钮");
                    addClickAnimation(v);
                    performDelayedAction(() -> {
                        if (listener != null) {
                            listener.onDialogCancelled();
                        }
                        dismissWithAnimation();
                    });
                });
                Log.d(TAG, "关闭按钮点击事件设置成功");
            } else {
                Log.e(TAG, "找不到关闭按钮 (R.id.iv_close)");
            }
            
            // 取消按钮
            View cancelButton = view.findViewById(R.id.btn_cancel);
            if (cancelButton != null) {
                cancelButton.setOnClickListener(v -> {
                    Log.d(TAG, "用户点击取消按钮");
                    addClickAnimation(v);
                    performDelayedAction(() -> {
                        if (listener != null) {
                            listener.onDialogCancelled();
                        }
                        dismissWithAnimation();
                    });
                });
                Log.d(TAG, "取消按钮点击事件设置成功");
            } else {
                Log.e(TAG, "找不到取消按钮 (R.id.btn_cancel)");
            }
            
            Log.d(TAG, "所有点击事件监听器设置完成");
            
        } catch (Exception e) {
            Log.e(TAG, "设置点击事件监听器时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 添加点击动画效果
     */
    private void addClickAnimation(View view) {
        try {
            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start();
                })
                .start();
        } catch (Exception e) {
            Log.w(TAG, "添加点击动画失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行延迟操作
     */
    private void performDelayedAction(Runnable action) {
        try {
            new android.os.Handler(android.os.Looper.getMainLooper())
                .postDelayed(action, 150);
        } catch (Exception e) {
            Log.w(TAG, "执行延迟操作失败: " + e.getMessage());
            // 如果延迟执行失败，立即执行
            action.run();
        }
    }
    
    /**
     * 带动画的关闭对话框
     */
    private void dismissWithAnimation() {
        try {
            Dialog dialog = getDialog();
            if (dialog != null && dialog.isShowing()) {
                View view = dialog.findViewById(android.R.id.content);
                if (view != null) {
                    view.animate()
                        .alpha(0f)
                        .scaleX(0.8f)
                        .scaleY(0.8f)
                        .setDuration(200)
                        .withEndAction(this::dismiss)
                        .start();
                } else {
                    dismiss();
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "关闭动画失败: " + e.getMessage());
            dismiss();
        }
    }
    
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "对话框开始显示");
        DialogDebugHelper.checkDialogFragmentState(this, "ImageProcessingDialog");
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "对话框恢复显示");
        DialogDebugHelper.checkDialogFragmentState(this, "ImageProcessingDialog");
    }
    
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "对话框暂停显示 - 可能的原因：Activity切换、系统对话框覆盖、内存不足");
        DialogDebugHelper.checkDialogFragmentState(this, "ImageProcessingDialog");
    }
    
    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "对话框停止显示 - 可能的原因：Activity不可见、Fragment被移除");
        DialogDebugHelper.checkDialogFragmentState(this, "ImageProcessingDialog");
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "对话框视图销毁 - 可能的原因：配置变更、Fragment被移除、内存回收");
        DialogDebugHelper.checkDialogFragmentState(this, "ImageProcessingDialog");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "对话框销毁 - Fragment生命周期结束");
        listener = null;
    }
    
    @Override
    public void onCancel(@NonNull android.content.DialogInterface dialog) {
        super.onCancel(dialog);
        Log.d(TAG, "对话框被取消 - 用户点击外部区域或按返回键");
        if (listener != null) {
            listener.onDialogCancelled();
        }
    }
    
    @Override
    public void onDismiss(@NonNull android.content.DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(TAG, "对话框被关闭 - dismiss()被调用或系统自动关闭");
    }
}