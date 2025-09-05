package com.wenxing.runyitong.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import com.wenxing.runyitong.R;

/**
 * 自定义图片处理选项对话框
 * 使用程序化布局创建，避免XML布局加载问题
 */
public class CustomImageProcessingDialog extends DialogFragment {
    
    private static final String TAG = "CustomImageProcessingDialog";
    
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
    
    public static CustomImageProcessingDialog newInstance() {
        Log.d(TAG, "创建CustomImageProcessingDialog实例");
        return new CustomImageProcessingDialog();
    }
    
    public void setOnProcessingOptionSelectedListener(OnProcessingOptionSelectedListener listener) {
        this.listener = listener;
        Log.d(TAG, "设置处理选项监听器: " + (listener != null));
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate调用");
        
        // 设置对话框样式为无标题
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView调用 - 开始创建自定义布局");
        
        try {
            Context context = requireContext();
            
            // 创建主容器
            LinearLayout mainContainer = new LinearLayout(context);
            mainContainer.setOrientation(LinearLayout.VERTICAL);
            mainContainer.setBackgroundResource(R.drawable.dialog_background);
            mainContainer.setPadding(32, 32, 32, 32);
            
            // 创建标题栏
            LinearLayout titleBar = createTitleBar(context);
            mainContainer.addView(titleBar);
            
            // 创建选项列表
            LinearLayout optionsContainer = createOptionsContainer(context);
            mainContainer.addView(optionsContainer);
            
            // 创建取消按钮
            TextView cancelButton = createCancelButton(context);
            mainContainer.addView(cancelButton);
            
            Log.d(TAG, "自定义布局创建成功");
            return mainContainer;
            
        } catch (Exception e) {
            Log.e(TAG, "创建自定义布局时发生异常: " + e.getMessage(), e);
            return createFallbackView();
        }
    }
    
    /**
     * 创建标题栏
     */
    private LinearLayout createTitleBar(Context context) {
        LinearLayout titleBar = new LinearLayout(context);
        titleBar.setOrientation(LinearLayout.HORIZONTAL);
        titleBar.setGravity(android.view.Gravity.CENTER_VERTICAL);
        titleBar.setPadding(0, 0, 0, 24);
        
        // 标题图标
        ImageView titleIcon = new ImageView(context);
        titleIcon.setImageResource(R.drawable.ic_image_processing);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(72, 72);
        iconParams.setMargins(0, 0, 16, 0);
        titleIcon.setLayoutParams(iconParams);
        titleBar.addView(titleIcon);
        
        // 标题文字
        TextView titleText = new TextView(context);
        titleText.setText("选择处理方式");
        titleText.setTextSize(18);
        titleText.setTextColor(Color.parseColor("#333333"));
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        titleText.setLayoutParams(textParams);
        titleBar.addView(titleText);
        
        // 关闭按钮
        ImageView closeButton = new ImageView(context);
        closeButton.setImageResource(R.drawable.ic_close);
        closeButton.setBackgroundResource(android.R.drawable.btn_default);
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(48, 48);
        closeButton.setLayoutParams(closeParams);
        closeButton.setOnClickListener(v -> {
            Log.d(TAG, "用户点击关闭按钮");
            if (listener != null) {
                listener.onDialogCancelled();
            }
            dismiss();
        });
        titleBar.addView(closeButton);
        
        return titleBar;
    }
    
    /**
     * 创建选项容器
     */
    private LinearLayout createOptionsContainer(Context context) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(0, 0, 0, 24);
        
        // 创建选项卡片
        container.addView(createOptionCard(context, "X光智能分析", "分析X光影像的病理特征", R.drawable.ic_xray, R.drawable.circle_background_light, () -> {
            Log.d(TAG, "用户选择X光分析");
            if (listener != null) {
                listener.onXRaySelected();
            }
            dismiss();
        }));
        
        container.addView(createOptionCard(context, "CT智能分析", "分析CT影像的病理特征", R.drawable.ic_ct, R.drawable.circle_background_accent, () -> {
            Log.d(TAG, "用户选择CT分析");
            if (listener != null) {
                listener.onCTSelected();
            }
            dismiss();
        }));
        
        container.addView(createOptionCard(context, "B超智能分析", "分析B超影像的病理特征", R.drawable.ic_ultrasound, R.drawable.circle_background_success, () -> {
            Log.d(TAG, "用户选择B超分析");
            if (listener != null) {
                listener.onUltrasoundSelected();
            }
            dismiss();
        }));
        
        container.addView(createOptionCard(context, "MRI智能分析", "分析MRI影像的病理特征", R.drawable.ic_mri, R.drawable.circle_background_warning, () -> {
            Log.d(TAG, "用户选择MRI分析");
            if (listener != null) {
                listener.onMRISelected();
            }
            dismiss();
        }));
        
        container.addView(createOptionCard(context, "PET-CT智能分析", "分析PET-CT影像的病理特征", R.drawable.ic_petct, R.drawable.circle_background_light, () -> {
            Log.d(TAG, "用户选择PET-CT分析");
            if (listener != null) {
                listener.onPETCTSelected();
            }
            dismiss();
        }));
        
        container.addView(createOptionCard(context, "上传到服务器", "将图片保存到云端服务器", R.drawable.ic_cloud_upload, R.drawable.circle_background_success, () -> {
            Log.d(TAG, "用户选择上传服务器");
            if (listener != null) {
                listener.onUploadSelected();
            }
            dismiss();
        }));
        
        container.addView(createOptionCard(context, "预览图片", "查看和编辑选中的图片", R.drawable.ic_preview, R.drawable.circle_background_warning, () -> {
            Log.d(TAG, "用户选择预览图片");
            if (listener != null) {
                listener.onPreviewSelected();
            }
            dismiss();
        }));
        
        return container;
    }
    
    /**
     * 创建选项卡片
     */
    private CardView createOptionCard(Context context, String title, String description, int iconRes, int backgroundRes, Runnable onClickAction) {
        CardView cardView = new CardView(context);
        cardView.setCardElevation(4);
        cardView.setRadius(12);
        cardView.setUseCompatPadding(true);
        
        LinearLayout cardContent = new LinearLayout(context);
        cardContent.setOrientation(LinearLayout.HORIZONTAL);
        cardContent.setPadding(20, 16, 20, 16);
        cardContent.setGravity(android.view.Gravity.CENTER_VERTICAL);
        
        // 图标容器
        LinearLayout iconContainer = new LinearLayout(context);
        iconContainer.setBackgroundResource(backgroundRes);
        iconContainer.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams iconContainerParams = new LinearLayout.LayoutParams(56, 56);
        iconContainerParams.setMargins(0, 0, 16, 0);
        iconContainer.setLayoutParams(iconContainerParams);
        
        ImageView icon = new ImageView(context);
        icon.setImageResource(iconRes);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(32, 32);
        icon.setLayoutParams(iconParams);
        iconContainer.addView(icon);
        
        cardContent.addView(iconContainer);
        
        // 文字容器
        LinearLayout textContainer = new LinearLayout(context);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textContainerParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        textContainer.setLayoutParams(textContainerParams);
        
        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextSize(16);
        titleView.setTextColor(Color.parseColor("#333333"));
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        textContainer.addView(titleView);
        
        TextView descView = new TextView(context);
        descView.setText(description);
        descView.setTextSize(14);
        descView.setTextColor(Color.parseColor("#666666"));
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        descParams.setMargins(0, 4, 0, 0);
        descView.setLayoutParams(descParams);
        textContainer.addView(descView);
        
        cardContent.addView(textContainer);
        cardView.addView(cardContent);
        
        // 设置点击事件
        cardView.setOnClickListener(v -> {
            Log.d(TAG, "选项卡片被点击: " + title);
            onClickAction.run();
        });
        
        return cardView;
    }
    
    /**
     * 创建取消按钮
     */
    private TextView createCancelButton(Context context) {
        TextView cancelButton = new TextView(context);
        cancelButton.setText("取消");
        cancelButton.setTextSize(16);
        cancelButton.setTextColor(Color.parseColor("#666666"));
        cancelButton.setGravity(android.view.Gravity.CENTER);
        cancelButton.setPadding(0, 16, 0, 16);
        cancelButton.setBackgroundResource(R.drawable.button_outline);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 0);
        cancelButton.setLayoutParams(params);
        
        cancelButton.setOnClickListener(v -> {
            Log.d(TAG, "用户点击取消按钮");
            if (listener != null) {
                listener.onDialogCancelled();
            }
            dismiss();
        });
        
        return cancelButton;
    }
    
    /**
     * 创建备用视图
     */
    private View createFallbackView() {
        Log.w(TAG, "创建备用简单视图");
        
        Context context = requireContext();
        TextView textView = new TextView(context);
        textView.setText("图片处理选项\n\n1. OCR文字识别\n2. 处方智能分析\n3. 上传到服务器\n4. 预览图片\n\n点击外部区域关闭");
        textView.setPadding(40, 40, 40, 40);
        textView.setTextSize(16);
        textView.setBackgroundColor(Color.WHITE);
        textView.setTextColor(Color.BLACK);
        
        return textView;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart调用");
        
        try {
            Dialog dialog = getDialog();
            if (dialog != null && dialog.getWindow() != null) {
                Window window = dialog.getWindow();
                
                // 设置背景透明
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                
                // 设置窗口大小
                WindowManager.LayoutParams params = window.getAttributes();
                params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                window.setAttributes(params);
                
                Log.d(TAG, "对话框窗口属性设置成功");
            }
        } catch (Exception e) {
            Log.e(TAG, "设置对话框属性时发生异常: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume调用 - 对话框应该已经显示");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy调用");
    }
    
    @Override
    public void onCancel(@NonNull android.content.DialogInterface dialog) {
        super.onCancel(dialog);
        Log.d(TAG, "对话框被取消");
        if (listener != null) {
            listener.onDialogCancelled();
        }
    }
}