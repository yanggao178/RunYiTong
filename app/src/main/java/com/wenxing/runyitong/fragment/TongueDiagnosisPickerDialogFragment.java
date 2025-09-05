package com.wenxing.runyitong.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import com.wenxing.runyitong.R;

/**
 * 中医舌面诊选择对话框Fragment
 * 用于让用户选择舌诊或面诊功能
 */
public class TongueDiagnosisPickerDialogFragment extends DialogFragment {
    
    private static final String TAG = "TongueDiagnosisPickerDialogFragment";
    
    /**
     * 舌面诊选择监听器接口
 */
    public interface OnTongueDiagnosisSelectedListener {
        /**
         * 用户选择舌诊
         */
        void onTongueDiagnosisSelected();
        
        /**
         * 用户选择面诊
         */
        void onFaceDiagnosisSelected();
        
        /**
         * 用户取消选择
         */
        void onDialogCancelled();
    }
    
    private OnTongueDiagnosisSelectedListener listener;
    
    /**
     * 创建新实例
     * @return TongueDiagnosisPickerDialogFragment实例
     */
    public static TongueDiagnosisPickerDialogFragment newInstance() {
        return new TongueDiagnosisPickerDialogFragment();
    }
    
    /**
     * 设置选择监听器
     * @param listener 监听器
     */
    public void setOnTongueDiagnosisSelectedListener(OnTongueDiagnosisSelectedListener listener) {
        this.listener = listener;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "创建舌面诊选择对话框");
        
        // 加载自定义布局
        View view = inflater.inflate(R.layout.dialog_tongue_diagnosis_picker, container, false);
        
        // 设置点击事件
        setupClickListeners(view);
        
        return view;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            // 设置对话框样式
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setBackgroundDrawableResource(android.R.color.transparent);
            }
        }
    }
    
    /**
     * 设置点击事件监听器
     * @param view 根视图
     */
    private void setupClickListeners(View view) {
        // 舌诊卡片点击事件
        CardView cardTongueDiagnosis = view.findViewById(R.id.card_tongue_diagnosis);
        cardTongueDiagnosis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "用户选择舌诊");
                if (listener != null) {
                    listener.onTongueDiagnosisSelected();
                }
                dismiss();
            }
        });
        
        // 面诊卡片点击事件
        CardView cardFaceDiagnosis = view.findViewById(R.id.card_face_diagnosis);
        cardFaceDiagnosis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "用户选择面诊");
                if (listener != null) {
                    listener.onFaceDiagnosisSelected();
                }
                dismiss();
            }
        });
        
        // 取消按钮点击事件
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "用户取消选择");
                if (listener != null) {
                    listener.onDialogCancelled();
                }
                dismiss();
            }
        });
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