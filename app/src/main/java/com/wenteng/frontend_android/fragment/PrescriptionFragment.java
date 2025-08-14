package com.wenteng.frontend_android.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.wenteng.frontend_android.R;
import com.wenteng.frontend_android.api.ApiClient;
import com.wenteng.frontend_android.api.ApiResponse;
import com.wenteng.frontend_android.api.ApiService;
import com.wenteng.frontend_android.model.SymptomAnalysis;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrescriptionFragment extends Fragment {
    
    private EditText etSymptoms;
    private TextView tvAnalysisResult;
    private LinearLayout llLoading;
    private ProgressBar progressBar;
    private TextView tvLoadingText;
    private ImageButton btnUploadPrescription;
    private ImageButton btnSelectImageSource;
    private ApiService apiService;
    private Handler timeoutHandler;
    private Runnable timeoutRunnable;
    private Runnable progressUpdateRunnable;
    private Call<ApiResponse<SymptomAnalysis>> currentCall;
    private int progressStep = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_prescription, container, false);
        
        // 初始化API服务
        apiService = ApiClient.getApiService();
        timeoutHandler = new Handler(Looper.getMainLooper());
        
        // 初始化控件
        etSymptoms = view.findViewById(R.id.et_symptoms);
        tvAnalysisResult = view.findViewById(R.id.tv_analysis_result);
        llLoading = view.findViewById(R.id.ll_loading);
        progressBar = view.findViewById(R.id.progress_bar);
        tvLoadingText = view.findViewById(R.id.tv_loading_text);
        btnSelectImageSource = view.findViewById(R.id.btn_select_image_source);
        btnUploadPrescription = view.findViewById(R.id.btn_upload_prescription);
        
        // 设置症状输入框的监听器
        etSymptoms.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                analyzeSymptoms();
                return true;
            }
            return false;
        });
        
        // 初始化选择图片来源按钮并设置点击事件
        btnSelectImageSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickerDialog();
            }
        });
        
        // 初始化上传按钮并设置点击事件
        btnUploadPrescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 检查是否有症状输入，如果有则先分析症状
                String symptoms = etSymptoms.getText().toString().trim();
                if (!TextUtils.isEmpty(symptoms)) {
                    analyzeSymptoms();
                } else {
                    Toast.makeText(getContext(), "请输入症状描述或选择图片来源", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        return view;
    }
    
    /**
     * 显示/隐藏加载状态
     */
    private void showLoading(boolean show) {
        if (show) {
            llLoading.setVisibility(View.VISIBLE);
            tvAnalysisResult.setVisibility(View.GONE);
            // 禁用按钮防止重复点击
            btnUploadPrescription.setEnabled(false);
            btnSelectImageSource.setEnabled(false);
            etSymptoms.setEnabled(false);
            
            // 开始动态更新进度提示
            progressStep = 0;
            startProgressUpdate();
        } else {
            llLoading.setVisibility(View.GONE);
            tvAnalysisResult.setVisibility(View.VISIBLE);
            // 重新启用按钮
            btnUploadPrescription.setEnabled(true);
            btnSelectImageSource.setEnabled(true);
            etSymptoms.setEnabled(true);
            
            // 停止进度更新
            stopProgressUpdate();
        }
    }
    
    /**
     * 开始进度更新
     */
    private void startProgressUpdate() {
        progressUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (llLoading.getVisibility() == View.VISIBLE) {
                    String[] messages = {
                        "AI正在分析您的症状\n预计需要10-30秒，请耐心等待",
                        "正在理解症状描述\n分析中...",
                        "正在匹配中医理论\n请稍候...",
                        "正在生成处方建议\n即将完成..."
                    };
                    
                    if (progressStep < messages.length) {
                        tvLoadingText.setText(messages[progressStep]);
                        progressStep++;
                        timeoutHandler.postDelayed(this, 5000); // 每5秒更新一次
                    }
                }
            }
        };
        timeoutHandler.post(progressUpdateRunnable);
    }
    
    /**
     * 停止进度更新
     */
    private void stopProgressUpdate() {
        if (progressUpdateRunnable != null) {
            timeoutHandler.removeCallbacks(progressUpdateRunnable);
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // 清理资源
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
        }
        if (timeoutHandler != null) {
            if (timeoutRunnable != null) {
                timeoutHandler.removeCallbacks(timeoutRunnable);
            }
            if (progressUpdateRunnable != null) {
                timeoutHandler.removeCallbacks(progressUpdateRunnable);
            }
        }
    }
    
    /**
     * 分析症状
     */
    private void analyzeSymptoms() {
        String symptoms = etSymptoms.getText().toString().trim();
        
        if (TextUtils.isEmpty(symptoms)) {
            Toast.makeText(getContext(), "请输入症状描述", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 显示加载状态
        showLoading(true);
        
        // 设置超时处理（30秒）
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentCall != null && !currentCall.isCanceled()) {
                    currentCall.cancel();
                    showLoading(false);
                    tvAnalysisResult.setText("请求超时，请检查网络连接后重试");
                    Toast.makeText(getContext(), "分析超时，请重试", Toast.LENGTH_SHORT).show();
                }
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, 30000); // 30秒超时
        
        // 调用API分析症状
        currentCall = apiService.analyzeSymptoms(symptoms);
        currentCall.enqueue(new Callback<ApiResponse<SymptomAnalysis>>() {
            @Override
            public void onResponse(Call<ApiResponse<SymptomAnalysis>> call, Response<ApiResponse<SymptomAnalysis>> response) {
                // 取消超时处理
                if (timeoutRunnable != null) {
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                }
                
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<SymptomAnalysis> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        displayAnalysisResult(apiResponse.getData());
                        Toast.makeText(getContext(), "分析完成", Toast.LENGTH_SHORT).show();
                    } else {
                        tvAnalysisResult.setText("分析失败: " + apiResponse.getMessage());
                        Toast.makeText(getContext(), "分析失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    tvAnalysisResult.setText("网络请求失败，请检查网络连接");
                    Toast.makeText(getContext(), "网络请求失败", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<SymptomAnalysis>> call, Throwable t) {
                // 取消超时处理
                if (timeoutRunnable != null) {
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                }
                
                showLoading(false);
                
                if (!call.isCanceled()) {
                    tvAnalysisResult.setText("网络错误: " + t.getMessage());
                    Toast.makeText(getContext(), "网络连接失败，请稍后重试", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    /**
     * 显示分析结果
     */
    private void displayAnalysisResult(SymptomAnalysis analysis) {
        StringBuilder result = new StringBuilder();
        
        result.append("【症状分析】\n");
        if (analysis.getAnalysis() != null) {
            result.append(analysis.getAnalysis()).append("\n\n");
        }
        
        result.append("【辨证分型】\n");
        if (analysis.getSyndromeType() != null) {
            result.append(analysis.getSyndromeType()).append("\n\n");
        }
        
        result.append("【治法】\n");
        if (analysis.getTreatmentMethod() != null) {
            result.append(analysis.getTreatmentMethod()).append("\n\n");
        }
        
        result.append("【主方】\n");
        if (analysis.getMainPrescription() != null) {
            result.append(analysis.getMainPrescription()).append("\n\n");
        }
        
        result.append("【组成】\n");
        if (analysis.getComposition() != null && !analysis.getComposition().isEmpty()) {
            for (SymptomAnalysis.MedicineComposition medicine : analysis.getComposition()) {
                result.append(medicine.getMedicine())
                      .append(" ").append(medicine.getDosage())
                      .append(" (").append(medicine.getRole()).append(")\n");
            }
            result.append("\n");
        }
        
        result.append("【煎服法】\n");
        if (analysis.getUsage() != null) {
            result.append(analysis.getUsage()).append("\n\n");
        }
        
        result.append("【禁忌】\n");
        if (analysis.getContraindications() != null) {
            result.append(analysis.getContraindications());
        }
        
        tvAnalysisResult.setText(result.toString());
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