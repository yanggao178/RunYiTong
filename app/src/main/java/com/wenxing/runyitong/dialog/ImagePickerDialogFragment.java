package com.wenxing.runyitong.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
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

/**
 * 图片选择对话框Fragment
 * 提供从相册选择和拍照两种选项的自定义对话框
 */
public class ImagePickerDialogFragment extends DialogFragment {
    
    private static final String TAG = "ImagePickerDialog";
    
    /**
     * 图片选择选项监听器接口
     */
    public interface OnImagePickerOptionSelectedListener {
        /**
         * 选择从相册选择
         */
        void onGallerySelected();
        
        /**
         * 选择拍照
         */
        void onCameraSelected();
        
        /**
         * 对话框被取消
         */
        void onDialogCancelled();
    }
    
    private OnImagePickerOptionSelectedListener listener;
    
    /**
     * 创建新实例
     */
    public static ImagePickerDialogFragment newInstance() {
        Log.d(TAG, "创建新的ImagePickerDialogFragment实例");
        return new ImagePickerDialogFragment();
    }
    
    /**
     * 设置选项选择监听器
     */
    public void setOnImagePickerOptionSelectedListener(OnImagePickerOptionSelectedListener listener) {
        this.listener = listener;
        Log.d(TAG, "设置选项选择监听器: " + (listener != null ? "已设置" : "null"));
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "开始创建自定义图片选择对话框");
        
        try {
            // 创建对话框
            Dialog dialog = new Dialog(requireContext(), R.style.CustomDialogTheme);
            
            // 加载布局
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.dialog_image_picker, null);
            
            if (view == null) {
                Log.e(TAG, "无法加载对话框布局文件");
                return createFallbackDialog();
            }
            
            dialog.setContentView(view);
            
            // 设置窗口属性
            Window window = dialog.getWindow();
            if (window != null) {
                // 设置透明背景
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                
                // 设置窗口大小
                WindowManager.LayoutParams params = window.getAttributes();
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                params.width = (int) (displayMetrics.widthPixels * 0.90); // 90%屏幕宽度
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                
                // 设置窗口动画
                window.setWindowAnimations(R.style.DialogAnimation);
                
                // 设置调光效果
                window.setDimAmount(0.6f);
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                
                window.setAttributes(params);
                Log.d(TAG, "窗口属性设置完成");
            }
            
            // 设置点击事件监听器
            setupClickListeners(view, dialog);
            
            // 添加进入动画
            addEnterAnimation(view);
            
            Log.d(TAG, "自定义对话框创建成功");
            return dialog;
            
        } catch (Exception e) {
            Log.e(TAG, "创建对话框时发生异常: " + e.getMessage(), e);
            return createFallbackDialog();
        }
    }
    
    /**
     * 创建备用简单对话框
     */
    private Dialog createFallbackDialog() {
        Log.w(TAG, "使用备用简单对话框");
        
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("选择图片来源")
               .setItems(new String[]{"从相册选择", "拍照", "取消"}, (dialog, which) -> {
                   switch (which) {
                       case 0:
                           if (listener != null) listener.onGallerySelected();
                           break;
                       case 1:
                           if (listener != null) listener.onCameraSelected();
                           break;
                       case 2:
                           if (listener != null) listener.onDialogCancelled();
                           break;
                   }
                   dismiss();
               });
        
        return builder.create();
    }
    
    /**
     * 添加进入动画
     */
    private void addEnterAnimation(View view) {
        try {
            view.setAlpha(0f);
            view.setScaleX(0.8f);
            view.setScaleY(0.8f);
            
            view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .start();
                
            Log.d(TAG, "进入动画设置完成");
        } catch (Exception e) {
            Log.w(TAG, "设置进入动画失败: " + e.getMessage());
        }
    }
    
    /**
     * 设置点击事件监听器
     */
    private void setupClickListeners(View view, Dialog dialog) {
        Log.d(TAG, "设置增强版点击事件监听器");
        
        try {
            // 从相册选择选项
            View galleryOption = view.findViewById(R.id.option_gallery);
            if (galleryOption != null) {
                galleryOption.setOnClickListener(v -> {
                    Log.d(TAG, "用户选择从相册选择");
                    addClickAnimation(v);
                    performDelayedAction(() -> {
                        if (listener != null) {
                            listener.onGallerySelected();
                        }
                        dismissWithAnimation();
                    });
                });
                Log.d(TAG, "相册选择选项点击事件设置成功");
            } else {
                Log.e(TAG, "找不到相册选择选项 (R.id.option_gallery)");
            }
            
            // 拍照选项
            View cameraOption = view.findViewById(R.id.option_camera);
            if (cameraOption != null) {
                cameraOption.setOnClickListener(v -> {
                    Log.d(TAG, "用户选择拍照");
                    addClickAnimation(v);
                    performDelayedAction(() -> {
                        if (listener != null) {
                            listener.onCameraSelected();
                        }
                        dismissWithAnimation();
                    });
                });
                Log.d(TAG, "拍照选项点击事件设置成功");
            } else {
                Log.e(TAG, "找不到拍照选项 (R.id.option_camera)");
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
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        Log.d(TAG, "对话框被取消");
        if (listener != null) {
            listener.onDialogCancelled();
        }
    }
}