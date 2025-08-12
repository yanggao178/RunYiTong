package com.wenteng.frontend_android.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.wenteng.frontend_android.R;

public class PrescriptionFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_prescription, container, false);
        
        // 初始化上传按钮并设置点击事件
        ImageButton btnUploadPrescription = view.findViewById(R.id.btn_upload_prescription);
        btnUploadPrescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickerDialog();
            }
        });
        
        return view;
    }
    
    /**
     * 显示图片选择对话框
     */
    private void showImagePickerDialog() {
        final String[] options = {"从相册选择", "拍照", "取消"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("选择图片来源");
        
        // 设置对话框从底部弹出
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // 从相册选择
                        Toast.makeText(getContext(), "从相册选择", Toast.LENGTH_SHORT).show();
                        // TODO: 实现从相册选择图片的功能
                        break;
                    case 1: // 拍照
                        Toast.makeText(getContext(), "拍照", Toast.LENGTH_SHORT).show();
                        // TODO: 实现拍照功能
                        break;
                    case 2: // 取消
                        dialog.dismiss();
                        break;
                }
            }
        });
        
        AlertDialog dialog = builder.create();
        dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
        dialog.getWindow().setGravity(android.view.Gravity.BOTTOM);
        dialog.show();
    }
}