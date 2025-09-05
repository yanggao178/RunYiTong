package com.wenxing.runyitong.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wenxing.runyitong.R;

/**
 * 美化的图像类型不匹配对话框
 */
public class ImageTypeMismatchDialog extends Dialog {
    
    private String requestedType;
    private String errorMessage;
    private OnActionListener listener;
    
    public interface OnActionListener {
        void onSelectCorrectImage();
        void onRetry();
        void onCancel();
    }
    
    public ImageTypeMismatchDialog(@NonNull Context context, String requestedType, String errorMessage) {
        super(context, R.style.CustomDialogTheme);
        this.requestedType = requestedType;
        this.errorMessage = errorMessage;
    }
    
    public void setOnActionListener(OnActionListener listener) {
        this.listener = listener;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 移除默认标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.dialog_image_type_mismatch);
        
        // 设置对话框属性
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        
        initViews();
        setupClickListeners();
    }
    
    private void initViews() {
        TextView tvRequestedType = findViewById(R.id.tv_requested_type);
        TextView tvErrorMessage = findViewById(R.id.tv_error_message);
        
        // 设置请求的分析类型
        if (requestedType != null) {
            String displayName = getImageTypeDisplayName(requestedType);
            tvRequestedType.setText(displayName);
        }
        
        // 设置错误信息
        if (errorMessage != null && !errorMessage.isEmpty()) {
            tvErrorMessage.setText(errorMessage);
        } else {
            String defaultMessage = getContext().getString(R.string.image_type_mismatch_message, 
                    getImageTypeDisplayName(requestedType));
            tvErrorMessage.setText(defaultMessage);
        }
    }
    
    private void setupClickListeners() {
        Button btnCancel = findViewById(R.id.btn_cancel);
        Button btnRetry = findViewById(R.id.btn_retry);
        Button btnSelectCorrectImage = findViewById(R.id.btn_select_correct_image);
        
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onCancel();
                }
                dismiss();
            }
        });
        
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onRetry();
                }
                dismiss();
            }
        });
        
        btnSelectCorrectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onSelectCorrectImage();
                }
                dismiss();
            }
        });
    }
    
    private String getImageTypeDisplayName(String imageType) {
        if (imageType == null) {
            return getContext().getString(R.string.unknown_image_type);
        }
        
        switch (imageType.toLowerCase()) {
            case "xray":
                return getContext().getString(R.string.xray_type_name);
            case "ct":
                return getContext().getString(R.string.ct_type_name);
            case "ultrasound":
                return getContext().getString(R.string.ultrasound_type_name);
            case "mri":
                return getContext().getString(R.string.mri_type_name);
            case "petct":
                return getContext().getString(R.string.petct_type_name);
            default:
                return getContext().getString(R.string.unknown_image_type);
        }
    }
}