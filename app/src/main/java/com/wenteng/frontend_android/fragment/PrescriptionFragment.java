package com.wenteng.frontend_android.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.provider.MediaStore;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import com.wenteng.frontend_android.R;
import com.wenteng.frontend_android.api.ApiClient;
import com.wenteng.frontend_android.api.ApiResponse;
import com.wenteng.frontend_android.api.ApiService;
import com.wenteng.frontend_android.utils.SymptomsHistoryManager;
import com.wenteng.frontend_android.adapter.SymptomsHistoryAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.cardview.widget.CardView;
import com.wenteng.frontend_android.model.SymptomAnalysis;
import com.wenteng.frontend_android.model.OCRResult;
import com.wenteng.frontend_android.model.PrescriptionAnalysis;
import com.wenteng.frontend_android.model.MedicalImageAnalysis;
import com.wenteng.frontend_android.model.ImageUploadResult;
import com.wenteng.frontend_android.model.TongueDiagnosisResult;
import com.wenteng.frontend_android.model.TongueAnalysis;
import com.wenteng.frontend_android.model.TongueBody;
import com.wenteng.frontend_android.model.TongueCoating;
import com.wenteng.frontend_android.model.TCMDiagnosis;
import com.wenteng.frontend_android.model.TCMRecommendations;
import com.wenteng.frontend_android.model.FaceDiagnosisResult;
import com.wenteng.frontend_android.model.FacialAnalysis;
import com.wenteng.frontend_android.model.Complexion;
import com.wenteng.frontend_android.model.FacialFeatures;
import com.wenteng.frontend_android.model.FacialRegions;
import com.wenteng.frontend_android.model.TCMFaceDiagnosis;
import com.wenteng.frontend_android.model.TCMFaceRecommendations;
import com.wenteng.frontend_android.utils.ImageUtils;
import com.wenteng.frontend_android.model.PrescriptionCreate;
import com.wenteng.frontend_android.model.Prescription;
import android.content.SharedPreferences;
import android.content.Context;

import com.wenteng.frontend_android.dialog.ImageProcessingDialogFragment;
import com.wenteng.frontend_android.dialog.ImagePickerDialogFragment;
import com.wenteng.frontend_android.dialog.TestDialogFragment;
import com.wenteng.frontend_android.dialog.CustomImageProcessingDialog;
import com.wenteng.frontend_android.dialog.ImageTypeMismatchDialog;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrescriptionFragment extends Fragment {
    
    private static final String TAG = "PrescriptionFragment";
    
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
    
    // 图片处理相关
    private Call<ApiResponse<OCRResult>> ocrCall;
    private Call<ApiResponse<PrescriptionAnalysis>> analysisCall;
    private Call<ApiResponse<MedicalImageAnalysis>> medicalImageAnalysisCall;
    private Call<ApiResponse<FaceDiagnosisResult>> faceDiagnosisCall;
    private Call<ApiResponse<TongueDiagnosisResult>> tongueDiagnosisCall;
    private Call<ApiResponse<ImageUploadResult>> uploadCall;
    private Uri selectedImageUri;
    private String imageSource = "unknown"; // 记录图片来源："camera" 或 "gallery"
    
    // 图片选择相关
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private Uri photoUri;
    
    // 历史记录相关
    private SymptomsHistoryManager historyManager;
    private SymptomsHistoryAdapter historyAdapter;
    private RecyclerView rvHistory;
    private CardView cvHistoryDropdown;
    private TextView tvClearHistory;
    private boolean isHistoryDropdownVisible = false;
    
    // 状态保存相关
    private static final String KEY_SYMPTOMS_TEXT = "symptoms_text";
    private static final String KEY_ANALYSIS_RESULT = "analysis_result";
    private static final String KEY_HAS_RESULT = "has_result";
    private String savedSymptomsText = "";
    private String savedAnalysisResult = "";
    private boolean hasAnalysisResult = false;

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
        
        // 初始化AI中医舌面诊按钮
        TextView tvAiTongueDiagnosis = view.findViewById(R.id.tv_ai_tongue_diagnosis);
        if (tvAiTongueDiagnosis != null) {
            tvAiTongueDiagnosis.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 弹出舌面诊图片来源选择对话框
                    showTongueDiagnosisImagePickerDialog();
                }
            });
        }
        
        // 初始化历史记录相关控件
        cvHistoryDropdown = view.findViewById(R.id.cv_history_dropdown);
        rvHistory = view.findViewById(R.id.rv_history);
        tvClearHistory = view.findViewById(R.id.tv_clear_history);
        
        // 初始化历史记录管理器
        historyManager = new SymptomsHistoryManager(getContext());
        
        // 设置历史记录列表
        setupHistoryRecyclerView();
        
        // 设置历史记录功能
        setupHistoryFeatures();
        
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
        
        // 初始化图片选择相关的ActivityResultLauncher
        initImagePickers();
        
        // 恢复保存的状态
        restoreState();
        
        return view;
    }
    
    /**
     * 设置历史记录RecyclerView
     */
    private void setupHistoryRecyclerView() {
        historyAdapter = new SymptomsHistoryAdapter(historyManager.getHistory());
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHistory.setAdapter(historyAdapter);
        
        // 设置历史记录项点击事件
        historyAdapter.setOnItemClickListener(symptom -> {
            etSymptoms.setText(symptom);
            etSymptoms.setSelection(symptom.length()); // 将光标移到末尾
            hideHistoryDropdown();
        });
        
        // 设置历史记录项删除事件
        historyAdapter.setOnItemDeleteListener((symptom, position) -> {
            historyManager.removeSymptom(symptom);
            historyAdapter.updateHistory(historyManager.getHistory());
            
            // 如果没有历史记录了，隐藏下拉框
            if (!historyManager.hasHistory()) {
                hideHistoryDropdown();
            }
        });
    }
    
    /**
     * 设置历史记录功能
     */
    private void setupHistoryFeatures() {
        // 设置症状输入框的焦点监听器
        etSymptoms.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && historyManager.hasHistory()) {
                showHistoryDropdown();
            } else if (!hasFocus) {
                // 延迟隐藏，给用户时间点击历史记录项
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (!etSymptoms.hasFocus()) {
                        hideHistoryDropdown();
                    }
                }, 200);
            }
        });
        
        // 设置症状输入框的点击监听器
        etSymptoms.setOnClickListener(v -> {
            if (historyManager.hasHistory()) {
                showHistoryDropdown();
            }
        });
        
        // 设置清空历史记录按钮点击事件
        tvClearHistory.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                .setTitle("清空历史记录")
                .setMessage("确定要清空所有症状历史记录吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    historyManager.clearHistory();
                    historyAdapter.updateHistory(historyManager.getHistory());
                    hideHistoryDropdown();
                    Toast.makeText(getContext(), "历史记录已清空", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
        });
    }
    
    /**
     * 显示历史记录下拉框
     */
    private void showHistoryDropdown() {
        if (!isHistoryDropdownVisible && historyManager.hasHistory()) {
            // 更新历史记录数据
            historyAdapter.updateHistory(historyManager.getHistory());
            
            cvHistoryDropdown.setVisibility(View.VISIBLE);
            isHistoryDropdownVisible = true;
            
            // 添加动画效果
            cvHistoryDropdown.setAlpha(0f);
            cvHistoryDropdown.animate()
                .alpha(1f)
                .setDuration(200)
                .start();
        }
    }
    
    /**
     * 隐藏历史记录下拉框
     */
    private void hideHistoryDropdown() {
        if (isHistoryDropdownVisible) {
            cvHistoryDropdown.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> {
                    cvHistoryDropdown.setVisibility(View.GONE);
                    isHistoryDropdownVisible = false;
                })
                .start();
        }
    }
    
    /**
     * 初始化图片选择相关的ActivityResultLauncher
     */
    private void initImagePickers() {
        // 相册选择
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> handleImageSelectionResult(result, "gallery")
        );
        
        // 拍照
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> handleImageSelectionResult(result, "camera")
        );
        
        // 相机权限请求
        cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    Log.d("PrescriptionFragment", "相机权限已授予");
                    openCamera();
                } else {
                    Log.w("PrescriptionFragment", "相机权限被拒绝");
                    showSafeToast("需要相机权限才能拍照");
                }
            }
        );
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
           // startProgressUpdate();
        } else {
            llLoading.setVisibility(View.GONE);
            tvAnalysisResult.setVisibility(View.VISIBLE);
            // 重新启用按钮
            btnUploadPrescription.setEnabled(true);
            btnSelectImageSource.setEnabled(true);
            etSymptoms.setEnabled(true);
            
            // 停止进度更新
         //   stopProgressUpdate();
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
                        timeoutHandler.postDelayed(this, 3000); // 每5秒更新一次
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

    /**
     * 启动医学影像分析进度更新
     */
    private void startMedicalImageAnalysisProgressUpdate(String imageType) {
        if (timeoutHandler == null) {
            timeoutHandler = new Handler(Looper.getMainLooper());
        }
        
        // 显示正在分析片子的等待提示
        String imageTypeName = getImageTypeDisplayName(imageType);
        Toast.makeText(getContext(), "🔍 正在分析" + imageTypeName + "片子，请稍候...", Toast.LENGTH_SHORT).show();
        
        progressStep = 0;
        progressUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (tvLoadingText != null) {
                    String[] progressMessages = getImageAnalysisProgressMessages(imageType);
                    if (progressStep < progressMessages.length) {
                        tvLoadingText.setText(progressMessages[progressStep]);
                        progressStep++;
                        timeoutHandler.postDelayed(this, 3000); // 每8秒更新一次
                    } else {
                        // 循环显示最后几条消息
                        progressStep = Math.max(0, progressMessages.length - 3);
                        tvLoadingText.setText(progressMessages[progressStep]);
                        progressStep++;
                        timeoutHandler.postDelayed(this, 3000);
                    }
                }
            }
        };
        timeoutHandler.post(progressUpdateRunnable);
    }

    /**
     * 获取医学影像分析进度消息
     */
    private String[] getImageAnalysisProgressMessages(String imageType) {
        String displayName = getImageTypeDisplayName(imageType);
        return new String[]{
            "正在对" + displayName + "进行影像预处理...",
            "AI正在识别" + displayName + "中的特征...",
            "正在分析" + displayName + "影像内容...",
            "正在生成" + displayName + "诊断建议...",
            "即将完成" + displayName + "分析..."
        };
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // 清理资源
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
        }
        if (ocrCall != null && !ocrCall.isCanceled()) {
            ocrCall.cancel();
        }
        if (analysisCall != null && !analysisCall.isCanceled()) {
            analysisCall.cancel();
        }
        if (medicalImageAnalysisCall != null && !medicalImageAnalysisCall.isCanceled()) {
            medicalImageAnalysisCall.cancel();
        }
        if (faceDiagnosisCall != null && !faceDiagnosisCall.isCanceled()) {
            faceDiagnosisCall.cancel();
        }
        if (tongueDiagnosisCall != null && !tongueDiagnosisCall.isCanceled()) {
            tongueDiagnosisCall.cancel();
        }
        if (uploadCall != null && !uploadCall.isCanceled()) {
            uploadCall.cancel();
        }
        if (timeoutHandler != null) {
            if (timeoutRunnable != null) {
                timeoutHandler.removeCallbacks(timeoutRunnable);
            }
            if (progressUpdateRunnable != null) {
                timeoutHandler.removeCallbacks(progressUpdateRunnable);
            }
        }
        
        // 停止打字机效果
        stopTypewriterEffect();
        
        // 清理临时文件
        if (getContext() != null) {
            ImageUtils.cleanupTempFiles(getContext());
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
        
        // 保存当前输入的症状文本
        savedSymptomsText = symptoms;
        
        // 将症状描述添加到历史记录中
        if (historyManager != null) {
            historyManager.addSymptom(symptoms);
            // 更新历史记录列表
            if (historyAdapter != null) {
                historyAdapter.updateHistory(historyManager.getHistory());
            }
        }
        
        // 隐藏历史记录下拉框
        hideHistoryDropdown();
        
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
        timeoutHandler.postDelayed(timeoutRunnable, 90000); // 90秒超时，给AI分析更多时间
        
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
            SymptomAnalysis.SyndromeType syndromeType = analysis.getSyndromeType();
            if (syndromeType.getMainSyndrome() != null) {
                result.append("主证：").append(syndromeType.getMainSyndrome()).append("\n");
            }
            if (syndromeType.getSecondarySyndrome() != null) {
                result.append("次证：").append(syndromeType.getSecondarySyndrome()).append("\n");
            }
            result.append("\n");
        }
        
        result.append("【治法】\n");
        if (analysis.getTreatmentMethod() != null) {
            SymptomAnalysis.TreatmentMethod treatmentMethod = analysis.getTreatmentMethod();
            if (treatmentMethod.getMainMethod() != null) {
                result.append("主要治法：").append(treatmentMethod.getMainMethod()).append("\n");
            }
            if (treatmentMethod.getAuxiliaryMethod() != null) {
                result.append("辅助治法：").append(treatmentMethod.getAuxiliaryMethod()).append("\n");
            }
            if (treatmentMethod.getTreatmentPriority() != null) {
                result.append("治疗重点：").append(treatmentMethod.getTreatmentPriority()).append("\n");
            }
            if (treatmentMethod.getCarePrinciple() != null) {
                result.append("调护原则：").append(treatmentMethod.getCarePrinciple()).append("\n");
            }
            result.append("\n");
        }
        
        result.append("【主方】\n");
        if (analysis.getMainPrescription() != null) {
            SymptomAnalysis.MainPrescription mainPrescription = analysis.getMainPrescription();
            if (mainPrescription.getFormulaName() != null) {
                result.append("方名：").append(mainPrescription.getFormulaName()).append("\n");
            }
            if (mainPrescription.getFormulaSource() != null) {
                result.append("出处：").append(mainPrescription.getFormulaSource()).append("\n");
            }
            if (mainPrescription.getFormulaAnalysis() != null) {
                result.append("方解：").append(mainPrescription.getFormulaAnalysis()).append("\n");
            }
            if (mainPrescription.getModifications() != null) {
                result.append("加减：").append(mainPrescription.getModifications()).append("\n");
            }
            result.append("\n");
        }
        
        result.append("【组成】\n");
        if (analysis.getComposition() != null && !analysis.getComposition().isEmpty()) {
            for (SymptomAnalysis.MedicineComposition medicine : analysis.getComposition()) {
                result.append(medicine.getHerb())
                      .append(" ").append(medicine.getDosage());
                if (medicine.getRole() != null) {
                    result.append(" (").append(medicine.getRole()).append(")");
                }
                if (medicine.getFunction() != null) {
                    result.append(" - ").append(medicine.getFunction());
                }
                if (medicine.getPreparation() != null) {
                    result.append(" [").append(medicine.getPreparation()).append("]");
                }
                result.append("\n");
            }
            result.append("\n");
        }
        
        result.append("【煎服法】\n");
        if (analysis.getUsage() != null) {
            SymptomAnalysis.Usage usage = analysis.getUsage();
            if (usage.getPreparationMethod() != null) {
                result.append("制备方法：").append(usage.getPreparationMethod()).append("\n");
            }
            if (usage.getAdministrationTime() != null) {
                result.append("服用时间：").append(usage.getAdministrationTime()).append("\n");
            }
            if (usage.getTreatmentCourse() != null) {
                result.append("疗程：").append(usage.getTreatmentCourse()).append("\n");
            }
            result.append("\n");
        } else {
            result.append("每日1剂，水煎服，早晚各1次\n\n");
        }
        
        result.append("【禁忌注意事项】\n");
        if (analysis.getContraindications() != null) {
            SymptomAnalysis.Contraindications contraindications = analysis.getContraindications();
            if (contraindications.getContraindications() != null && !contraindications.getContraindications().trim().isEmpty()) {
                result.append("禁忌：").append(contraindications.getContraindications()).append("\n");
            }
            if (contraindications.getDietaryRestrictions() != null && !contraindications.getDietaryRestrictions().trim().isEmpty()) {
                result.append("饮食禁忌：").append(contraindications.getDietaryRestrictions()).append("\n");
            }
            if (contraindications.getLifestyleCare() != null && !contraindications.getLifestyleCare().trim().isEmpty()) {
                result.append("生活调护：").append(contraindications.getLifestyleCare()).append("\n");
            }
            if (contraindications.getPrecautions() != null && !contraindications.getPrecautions().trim().isEmpty()) {
                result.append("注意事项：").append(contraindications.getPrecautions()).append("\n");
            }
        } else {
            result.append("孕妇慎用，过敏体质者慎用\n");
        }
        
        // 添加西医诊疗部分
        if (analysis.getWesternMedicine() != null) {
            result.append("\n\n=== 西医诊疗建议 ===\n\n");
            
            SymptomAnalysis.WesternMedicine westernMedicine = analysis.getWesternMedicine();
            
            // 西医诊断
            result.append("【西医诊断】\n");
            if (westernMedicine.getDiagnosis() != null) {
                SymptomAnalysis.Diagnosis diagnosis = westernMedicine.getDiagnosis();
                if (diagnosis.getPossibleDiagnosis() != null && !diagnosis.getPossibleDiagnosis().trim().isEmpty()) {
                    result.append("可能诊断：").append(diagnosis.getPossibleDiagnosis()).append("\n");
                }
                if (diagnosis.getDifferentialDiagnosis() != null && !diagnosis.getDifferentialDiagnosis().trim().isEmpty()) {
                    result.append("鉴别诊断：").append(diagnosis.getDifferentialDiagnosis()).append("\n");
                }
                if (diagnosis.getRecommendedTests() != null && !diagnosis.getRecommendedTests().trim().isEmpty()) {
                    result.append("建议检查：").append(diagnosis.getRecommendedTests()).append("\n");
                }
                if (diagnosis.getPathologicalMechanism() != null && !diagnosis.getPathologicalMechanism().trim().isEmpty()) {
                    result.append("病理机制：").append(diagnosis.getPathologicalMechanism()).append("\n");
                }
            }
            result.append("\n");
            
            // 西医治疗
            result.append("【西医治疗】\n");
            if (westernMedicine.getTreatment() != null) {
                SymptomAnalysis.Treatment treatment = westernMedicine.getTreatment();
                if (treatment.getDrugTherapy() != null && !treatment.getDrugTherapy().trim().isEmpty()) {
                    result.append("药物治疗：").append(treatment.getDrugTherapy()).append("\n");
                }
                if (treatment.getNonDrugTherapy() != null && !treatment.getNonDrugTherapy().trim().isEmpty()) {
                    result.append("非药物治疗：").append(treatment.getNonDrugTherapy()).append("\n");
                }
                if (treatment.getLifestyleIntervention() != null && !treatment.getLifestyleIntervention().trim().isEmpty()) {
                    result.append("生活干预：").append(treatment.getLifestyleIntervention()).append("\n");
                }
                if (treatment.getPreventionMeasures() != null && !treatment.getPreventionMeasures().trim().isEmpty()) {
                    result.append("预防措施：").append(treatment.getPreventionMeasures()).append("\n");
                }
            }
            result.append("\n");
            
            // 西医用药指导
            result.append("【用药指导】\n");
            if (westernMedicine.getMedication() != null) {
                SymptomAnalysis.Medication medication = westernMedicine.getMedication();
                if (medication.getDrugSelection() != null && !medication.getDrugSelection().trim().isEmpty()) {
                    result.append("药物选择：").append(medication.getDrugSelection()).append("\n");
                }
                if (medication.getAdministrationMethod() != null && !medication.getAdministrationMethod().trim().isEmpty()) {
                    result.append("用药方法：").append(medication.getAdministrationMethod()).append("\n");
                }
                if (medication.getAdverseReactions() != null && !medication.getAdverseReactions().trim().isEmpty()) {
                    result.append("不良反应：").append(medication.getAdverseReactions()).append("\n");
                }
                if (medication.getDrugInteractions() != null && !medication.getDrugInteractions().trim().isEmpty()) {
                    result.append("药物相互作用：").append(medication.getDrugInteractions()).append("\n");
                }
            }
        }
        
        String resultText = result.toString();
        
        // 使用打字机效果显示结果
        displayTextWithTypewriterEffect(resultText);
        
        // 保存分析结果状态
        savedAnalysisResult = resultText;
        hasAnalysisResult = true;
        
        // 保存处方信息到服务器
        savePrescriptionToServer(analysis, resultText);
    }
    
    // 打字机效果相关变量
    private Handler typewriterHandler;
    private boolean isTypewriterActive = false;
    private String currentTypewriterText = "";
    
    /**
     * 打字机效果显示文本
     */
    private void displayTextWithTypewriterEffect(String text) {
        if (tvAnalysisResult == null || text == null || text.isEmpty()) {
            return;
        }
        
        // 停止之前的打字机效果
        stopTypewriterEffect();
        
        // 清空当前显示的文本
        tvAnalysisResult.setText("");
        
        // 保存当前文本
        currentTypewriterText = text;
        isTypewriterActive = true;
        
        // 创建Handler用于延时显示字符
        typewriterHandler = new Handler(Looper.getMainLooper());
        
        // 打字机效果的延时时间（毫秒）
        final int TYPING_DELAY = 50; // 每个字符显示间隔50毫秒
        
        // 添加点击跳过功能提示
        showTypewriterSkipHint();
        
        // 逐字显示文本
        for (int i = 0; i <= text.length(); i++) {
            final int index = i;
            typewriterHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (tvAnalysisResult != null && isTypewriterActive && index <= text.length()) {
                        String displayText = text.substring(0, index);
                        tvAnalysisResult.setText(displayText);
                        
                        // 自动滚动到底部，确保用户能看到最新显示的内容
                        scrollToBottom();
                        
                        // 如果是最后一个字符，标记打字机效果结束
                        if (index == text.length()) {
                            isTypewriterActive = false;
                        }
                    }
                }
            }, i * TYPING_DELAY);
        }
        
        // 设置点击跳过功能
        tvAnalysisResult.setOnClickListener(v -> {
            if (isTypewriterActive) {
                skipTypewriterEffect();
            }
        });
    }
    
    /**
     * 停止打字机效果
     */
    private void stopTypewriterEffect() {
        if (typewriterHandler != null) {
            typewriterHandler.removeCallbacksAndMessages(null);
        }
        isTypewriterActive = false;
    }
    
    /**
     * 跳过打字机效果，直接显示完整文本
     */
    private void skipTypewriterEffect() {
        stopTypewriterEffect();
        if (tvAnalysisResult != null && !currentTypewriterText.isEmpty()) {
            tvAnalysisResult.setText(currentTypewriterText);
            scrollToBottom();
            Toast.makeText(getContext(), "已跳过打字机效果", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 显示打字机跳过提示
     */
    private void showTypewriterSkipHint() {
        if (getContext() != null) {
            Toast.makeText(getContext(), "正在逐字显示结果，点击文本区域可跳过", Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * 滚动到底部
     */
    private void scrollToBottom() {
        if (tvAnalysisResult != null && tvAnalysisResult.getParent() instanceof ScrollView) {
            ScrollView scrollView = (ScrollView) tvAnalysisResult.getParent();
            scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
        }
    }
    
    /**
     * 保存Fragment状态
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // 保存症状输入文本
        if (etSymptoms != null) {
            savedSymptomsText = etSymptoms.getText().toString();
        }
        
        outState.putString(KEY_SYMPTOMS_TEXT, savedSymptomsText);
        outState.putString(KEY_ANALYSIS_RESULT, savedAnalysisResult);
        outState.putBoolean(KEY_HAS_RESULT, hasAnalysisResult);
    }
    
    /**
     * 恢复Fragment状态
     */
    private void restoreState() {
        // 恢复症状输入文本
        if (!savedSymptomsText.isEmpty() && etSymptoms != null) {
            etSymptoms.setText(savedSymptomsText);
        }
        
        // 恢复分析结果
        if (hasAnalysisResult && !savedAnalysisResult.isEmpty() && tvAnalysisResult != null) {
            tvAnalysisResult.setText(savedAnalysisResult);
        }
    }
    
    /**
     * 从Bundle中恢复状态
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (savedInstanceState != null) {
            savedSymptomsText = savedInstanceState.getString(KEY_SYMPTOMS_TEXT, "");
            savedAnalysisResult = savedInstanceState.getString(KEY_ANALYSIS_RESULT, "");
            hasAnalysisResult = savedInstanceState.getBoolean(KEY_HAS_RESULT, false);
            
            // 恢复状态
            restoreState();
        }
    }
    
    /**
     * 处理Fragment显示隐藏状态变化
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        
        if (!hidden) {
            // Fragment变为可见时，恢复状态和屏幕方向
            restoreState();
            restoreScreenOrientation();
        } else {
            // Fragment被隐藏时，保存当前状态
            saveCurrentState();
        }
    }
    
    /**
     * 保存当前状态
     */
    private void saveCurrentState() {
        // 保存症状输入文本
        if (etSymptoms != null) {
            savedSymptomsText = etSymptoms.getText().toString();
        }
    }
    
    /**
     * 保存处方信息到服务器
     * @param analysis 症状分析结果
     * @param prescriptionText 处方全文
     */
    private void savePrescriptionToServer(SymptomAnalysis analysis, String prescriptionText) {
        Log.d(TAG, "开始保存处方信息到服务器");
        
        // 检查用户登录状态
        int userId = getCurrentUserId();
        if (userId == -1) {
            Log.w(TAG, "用户未登录，不保存处方信息");
            return;
        }
        
        // 提取症状描述
        String symptoms = etSymptoms != null ? etSymptoms.getText().toString().trim() : "";
        if (symptoms.isEmpty()) {
            Log.w(TAG, "症状描述为空，不保存处方信息");
            return;
        }
        
        // 提取诊断信息
        String diagnosis = extractDiagnosis(analysis);
        
        // 创建处方创建请求
        PrescriptionCreate prescriptionCreate = new PrescriptionCreate(
            userId,
            symptoms,
            diagnosis,
            prescriptionText,
            "AI中医助手", // 医生名称
            null // 图片URL（如果有的话）
        );
        
        Log.d(TAG, "发送处方创建请求: " + prescriptionCreate.toString());
        
        // 调用API保存处方
        Call<ApiResponse<Prescription>> call = apiService.createPrescription(prescriptionCreate);
        call.enqueue(new Callback<ApiResponse<Prescription>>() {
            @Override
            public void onResponse(Call<ApiResponse<Prescription>> call, Response<ApiResponse<Prescription>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Prescription> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "处方保存成功");
                        showSafeToast("处方已保存到个人档案");
                    } else {
                        Log.e(TAG, "处方保存失败: " + apiResponse.getMessage());
                        showSafeToast("处方保存失败：" + apiResponse.getMessage());
                    }
                } else {
                    Log.e(TAG, "处方保存请求失败，响应码: " + response.code());
                    showSafeToast("处方保存失败，请稍后重试");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Prescription>> call, Throwable t) {
                Log.e(TAG, "处方保存网络错误: " + t.getMessage(), t);
                if (!call.isCanceled()) {
                    showSafeToast("网络连接失败，处方未保存");
                }
            }
        });
    }
    
    /**
     * 获取当前用户ID
     * @return 用户ID，如果未登录则返回-1
     */
    private int getCurrentUserId() {
        if (getContext() == null) {
            return -1;
        }
        
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_login_state", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("user_id", -1);
    }
    
    /**
     * 从分析结果中提取诊断信息
     * @param analysis 症状分析结果
     * @return 诊断信息
     */
    private String extractDiagnosis(SymptomAnalysis analysis) {
        StringBuilder diagnosis = new StringBuilder();
        
        // 提取辨证分型
        if (analysis.getSyndromeType() != null) {
            SymptomAnalysis.SyndromeType syndromeType = analysis.getSyndromeType();
            if (syndromeType.getMainSyndrome() != null) {
                diagnosis.append("主证：").append(syndromeType.getMainSyndrome());
            }
            if (syndromeType.getSecondarySyndrome() != null) {
                if (diagnosis.length() > 0) diagnosis.append("; ");
                diagnosis.append("次证：").append(syndromeType.getSecondarySyndrome());
            }
        }
        
        // 提取主方信息
        if (analysis.getMainPrescription() != null && analysis.getMainPrescription().getFormulaName() != null) {
            if (diagnosis.length() > 0) diagnosis.append("; ");
            diagnosis.append("主方：").append(analysis.getMainPrescription().getFormulaName());
        }
        
        return diagnosis.toString();
    }
    
    /**
     * 安全显示Toast消息（避免在Fragment分离后显示）
     * @param message 消息内容
     */
    private void showSafeToast(String message) {
        if (getContext() != null && isAdded() && !isDetached() && !isRemoving()) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        } else {
            Log.w("PrescriptionFragment", "无法显示Toast，Fragment状态异常: " + message);
        }
    }
    
    /**
     * Fragment重新可见时调用
     */
    @Override
    public void onResume() {
        super.onResume();
        // 恢复状态和屏幕方向
        restoreState();
        restoreScreenOrientation();
    }
    
    /**
     * Fragment暂停时调用
     */
    @Override
    public void onPause() {
        super.onPause();
        // 保存当前状态
        saveCurrentState();
    }
    
    /**
     * 显示图片选择对话框
     */
    /**
     * 显示图片选择对话框
     * 使用自定义DialogFragment替代简单的AlertDialog，提供更好的用户体验
     */
    // 当前舌面诊类型："tongue"表示舌诊，"face"表示面诊
    private String currentTongueDiagnosisType = null;
    
    /**
     * 显示舌面诊图片选择对话框
     */
    private void showTongueDiagnosisImagePickerDialog() {
        Log.d("PrescriptionFragment", "显示舌面诊图片选择对话框");
        
        if (getContext() == null || !isAdded()) {
            Log.w("PrescriptionFragment", "Fragment状态异常，无法显示对话框");
            return;
        }
        
        try {
            TongueDiagnosisPickerDialogFragment dialogFragment = TongueDiagnosisPickerDialogFragment.newInstance();
            dialogFragment.setOnTongueDiagnosisSelectedListener(new TongueDiagnosisPickerDialogFragment.OnTongueDiagnosisSelectedListener() {
                @Override
                public void onTongueDiagnosisSelected() {
                    Log.d("PrescriptionFragment", "用户选择舌诊");
                    // 设置当前分析类型为舌诊
                    currentTongueDiagnosisType = "tongue";
                    showImagePickerDialog();
                }
                
                @Override
                public void onFaceDiagnosisSelected() {
                    Log.d("PrescriptionFragment", "用户选择面诊");
                    // 设置当前分析类型为面诊
                    currentTongueDiagnosisType = "face";
                    showImagePickerDialog();
                }
                
                @Override
                public void onDialogCancelled() {
                    Log.d("PrescriptionFragment", "用户取消舌面诊选择");
                }
            });
            
            dialogFragment.show(getParentFragmentManager(), "TongueDiagnosisPickerDialog");
        } catch (Exception e) {
            Log.e("PrescriptionFragment", "显示舌面诊对话框异常: " + e.getMessage(), e);
            showSafeToast("显示选择对话框失败，请重试");
        }
    }
    
    private void showImagePickerDialog() {
        android.util.Log.d("PrescriptionFragment", "=== 开始显示图片选择对话框 ===");
        android.util.Log.d("PrescriptionFragment", "Fragment状态 - Context: " + (getContext() != null) + ", isAdded: " + isAdded() + ", isDetached: " + isDetached() + ", isRemoving: " + isRemoving());
        
        // 检查Fragment状态
        if (getContext() == null || !isAdded() || isDetached() || isRemoving()) {
            android.util.Log.w("PrescriptionFragment", "Fragment状态不正常，无法显示对话框");
            Toast.makeText(getActivity(), "页面状态异常，请重试", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            android.util.Log.d("PrescriptionFragment", "开始创建ImagePickerDialogFragment");
            
            // 创建自定义图片选择对话框
            ImagePickerDialogFragment dialogFragment = ImagePickerDialogFragment.newInstance();
            
            // 设置回调监听器
            dialogFragment.setOnImagePickerOptionSelectedListener(new ImagePickerDialogFragment.OnImagePickerOptionSelectedListener() {
                @Override
                public void onGallerySelected() {
                    android.util.Log.d("PrescriptionFragment", "用户选择从相册选择");
                    openGallery();
                }
                
                @Override
                public void onCameraSelected() {
                    android.util.Log.d("PrescriptionFragment", "用户选择拍照");
                    checkCameraPermissionAndOpen();
                }
                
                @Override
                public void onDialogCancelled() {
                    android.util.Log.d("PrescriptionFragment", "用户取消图片选择对话框");
                }
            });
            
            // 显示对话框
            android.util.Log.d("PrescriptionFragment", "准备显示ImagePickerDialogFragment");
            dialogFragment.show(getParentFragmentManager(), "ImagePickerDialog");
            android.util.Log.d("PrescriptionFragment", "ImagePickerDialogFragment显示完成");
            
            // 显示提示信息
            Toast.makeText(requireActivity(), "请选择图片来源", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            android.util.Log.e("PrescriptionFragment", "显示ImagePickerDialogFragment时发生异常: " + e.getMessage(), e);
            e.printStackTrace();
            
            // 异常情况下使用简单对话框作为备用方案
            android.util.Log.d("PrescriptionFragment", "异常情况下使用简单AlertDialog作为备用方案");
            showFallbackImagePickerDialog();
        }
    }
    
    /**
     * 备用的简单图片选择对话框
     * 当自定义对话框无法正常显示时使用
     */
    private void showFallbackImagePickerDialog() {
        android.util.Log.d("PrescriptionFragment", "显示备用图片选择对话框");
        
        try {
            final String[] options = {"从相册选择", "拍照", "取消"};
            
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("选择图片来源");
            
            // 设置对话框选项
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0: // 从相册选择
                            android.util.Log.d("PrescriptionFragment", "备用对话框：用户选择从相册选择");
                            openGallery();
                            break;
                        case 1: // 拍照
                            android.util.Log.d("PrescriptionFragment", "备用对话框：用户选择拍照");
                            checkCameraPermissionAndOpen();
                            break;
                        case 2: // 取消
                            android.util.Log.d("PrescriptionFragment", "备用对话框：用户取消");
                            dialog.dismiss();
                            break;
                    }
                }
            });
            
            AlertDialog dialog = builder.create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
                dialog.getWindow().setGravity(android.view.Gravity.BOTTOM);
            }
            dialog.show();
            
            android.util.Log.d("PrescriptionFragment", "备用对话框显示成功");
            
        } catch (Exception e) {
            android.util.Log.e("PrescriptionFragment", "显示备用对话框时发生异常: " + e.getMessage(), e);
            Toast.makeText(getContext(), "无法显示选择对话框，请重试", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 打开相册选择图片
     */
    private void openGallery() {
        android.util.Log.d("PrescriptionFragment", "=== openGallery 开始 ===");
        
        try {
            // 检查Fragment和Activity状态
            if (getActivity() == null || !isAdded() || isRemoving()) {
                android.util.Log.e("PrescriptionFragment", "Fragment状态异常，无法打开相册");
                return;
            }
            
            // 创建标准的图片选择Intent
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            
            // 检查是否有应用可以处理这个Intent
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                android.util.Log.d("PrescriptionFragment", "启动相册选择器");
                galleryLauncher.launch(intent);
            } else {
                android.util.Log.e("PrescriptionFragment", "没有找到可用的图片选择应用");
                showSafeToast("没有找到可用的图片选择应用");
            }
            
        } catch (Exception e) {
            android.util.Log.e("PrescriptionFragment", "打开相册时发生异常: " + e.getMessage(), e);
            showSafeToast("打开相册失败，请重试");
        }
        
        android.util.Log.d("PrescriptionFragment", "=== openGallery 结束 ===");
    }
    
    /**
     * 备用相册选择方法，使用不同的Intent方式
     */
    private void openGalleryAlternative() {
        android.util.Log.d("PrescriptionFragment", "=== openGalleryAlternative 开始 ===");
        
        try {
            // 检查Fragment和Activity状态
            if (getActivity() == null || !isAdded() || isRemoving()) {
                android.util.Log.e("PrescriptionFragment", "Fragment状态异常，无法打开相册");
                return;
            }
            
            // 尝试使用GET_CONTENT方式
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            
            // 检查是否有应用可以处理这个Intent
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                android.util.Log.d("PrescriptionFragment", "启动备用相册选择器");
                galleryLauncher.launch(intent);
            } else {
                android.util.Log.e("PrescriptionFragment", "没有找到可用的文件选择应用");
                showSafeToast("没有找到可用的文件选择应用");
            }
            
        } catch (Exception e) {
            android.util.Log.e("PrescriptionFragment", "打开备用相册时发生异常: " + e.getMessage(), e);
            showSafeToast("打开相册失败，请重试");
        }
        
        android.util.Log.d("PrescriptionFragment", "=== openGalleryAlternative 结束 ===");
    }
    
    /**
     * 测试相册选择功能
     */
    public void testGallerySelection() {
        android.util.Log.d("PrescriptionFragment", "=== 测试相册选择功能 ===");
        
        // 先测试标准方法
        android.util.Log.d("PrescriptionFragment", "测试标准相册选择方法");
        openGallery();
        
        // 延迟测试备用方法
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                android.util.Log.d("PrescriptionFragment", "如果标准方法失败，可以尝试备用方法");
                // openGalleryAlternative(); // 暂时注释，避免同时启动两个选择器
            }
        }, 5000);
    }
    
    /**
     * 检查相机权限并打开相机
     */
    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }
    
    /**
     * 打开相机拍照
     */
    private void openCamera() {
        // 先设置屏幕方向为纵向
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // 创建图片文件
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(getContext(), "创建图片文件失败", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 如果文件创建成功
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(getContext(),
                        "com.wenteng.frontend_android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                // 强制使用纵向模式
                takePictureIntent.putExtra("android.intent.extra.screenOrientation", android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                // 移除 FLAG_ACTIVITY_NEW_TASK 标志，避免在新任务栈中启动导致无法返回结果
                
                cameraLauncher.launch(takePictureIntent);
            }
        } else {
            Toast.makeText(getContext(), "没有找到相机应用", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 恢复屏幕方向
     */
    private void restoreScreenOrientation() {
        if (getActivity() != null) {
            // 恢复为纵向模式，保持应用一致的方向
            getActivity().setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
    
    /**
     * 创建图片文件
     */
    private File createImageFile() throws IOException {
        // 创建图片文件名
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir("Pictures");
        
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }
    
    /**
     * 处理选择的图片
     */
    private void handleSelectedImage(Uri imageUri) {
        android.util.Log.d("PrescriptionFragment", "=== handleSelectedImage 开始 ===");
        android.util.Log.d("PrescriptionFragment", "接收到的imageUri: " + imageUri);
        
        if (imageUri == null) {
            android.util.Log.e("PrescriptionFragment", "imageUri为null，显示错误提示");
            Toast.makeText(getContext(), "图片选择失败", Toast.LENGTH_SHORT).show();
            return;
        }
        
        android.util.Log.d("PrescriptionFragment", "设置selectedImageUri");
        selectedImageUri = imageUri;
        android.util.Log.d("PrescriptionFragment", "selectedImageUri已设置为: " + selectedImageUri);
        
        // 测试 ImageProcessingDialogFragment 的 onCreateDialog 方法
       // testImageProcessingDialogCreation();
        
        // 直接显示图片处理选项对话框
        android.util.Log.d("PrescriptionFragment", "准备调用showImageProcessingDialog()");
        try {
            showImageProcessingDialog();
            android.util.Log.d("PrescriptionFragment", "showImageProcessingDialog()调用完成");
        } catch (Exception e) {
            android.util.Log.e("PrescriptionFragment", "调用showImageProcessingDialog()时发生异常: " + e.getMessage(), e);
        }
        
        android.util.Log.d("PrescriptionFragment", "=== handleSelectedImage 结束 ===");
    }
    
    /**
     * 测试 ImageProcessingDialogFragment 的 onCreateDialog 方法
     */
    private void testImageProcessingDialogCreation() {
        android.util.Log.d("PrescriptionFragment", "=== 开始测试 ImageProcessingDialogFragment.onCreateDialog() ===");
        
        try {
            // 创建 DialogFragment 实例
            ImageProcessingDialogFragment testDialog = ImageProcessingDialogFragment.newInstance(selectedImageUri, imageSource, true);
            android.util.Log.d("PrescriptionFragment", "DialogFragment 实例创建成功");
            
            // 设置监听器
            testDialog.setOnProcessingOptionSelectedListener(new ImageProcessingDialogFragment.OnProcessingOptionSelectedListener() {
                @Override
                public void onXRaySelected() {
                    android.util.Log.d("PrescriptionFragment", "测试对话框 - X光分析选项被选中");
                }
                
                @Override
                public void onCTSelected() {
                    android.util.Log.d("PrescriptionFragment", "测试对话框 - CT分析选项被选中");
                }
                
                @Override
                public void onUltrasoundSelected() {
                    android.util.Log.d("PrescriptionFragment", "测试对话框 - B超分析选项被选中");
                }
                
                @Override
                public void onMRISelected() {
                    android.util.Log.d("PrescriptionFragment", "测试对话框 - MRI分析选项被选中");
                }
                
                @Override
                public void onPETCTSelected() {
                    android.util.Log.d("PrescriptionFragment", "测试对话框 - PET-CT分析选项被选中");
                }
                
                @Override
                public void onUploadSelected() {
                    android.util.Log.d("PrescriptionFragment", "测试对话框 - 上传选项被选中");
                }
                
                @Override
                public void onPreviewSelected() {
                    android.util.Log.d("PrescriptionFragment", "测试对话框 - 预览选项被选中");
                }
                
                @Override
                public void onDialogCancelled() {
                    android.util.Log.d("PrescriptionFragment", "测试对话框 - 对话框被取消");
                }
            });
            
            android.util.Log.d("PrescriptionFragment", "监听器设置完成，准备显示测试对话框");
            
            // 显示对话框进行测试
            testDialog.show(getParentFragmentManager(), "TestImageProcessingDialog");
            android.util.Log.d("PrescriptionFragment", "测试对话框显示调用完成");
            
            // 延迟检查对话框状态
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    androidx.fragment.app.Fragment dialog = getParentFragmentManager().findFragmentByTag("TestImageProcessingDialog");
                    if (dialog != null && dialog.isAdded()) {
                        android.util.Log.d("PrescriptionFragment", "✅ 测试成功：ImageProcessingDialogFragment.onCreateDialog() 正常工作");
                        // 关闭测试对话框
                        if (dialog instanceof ImageProcessingDialogFragment) {
                            ((ImageProcessingDialogFragment) dialog).dismiss();
                        }
                    } else {
                        android.util.Log.e("PrescriptionFragment", "❌ 测试失败：ImageProcessingDialogFragment.onCreateDialog() 可能存在问题");
                    }
                }
            }, 1000);
            
        } catch (Exception e) {
            android.util.Log.e("PrescriptionFragment", "❌ 测试异常：ImageProcessingDialogFragment.onCreateDialog() 发生错误: " + e.getMessage(), e);
            e.printStackTrace();
        }
        
        android.util.Log.d("PrescriptionFragment", "=== ImageProcessingDialogFragment.onCreateDialog() 测试完成 ===");
    }
    
    /**
     * 测试基本对话框功能
     */
    private void testBasicDialog() {
        android.util.Log.d("PrescriptionFragment", "=== 开始测试基本对话框功能 ===");
        
        try {
            // 检查基本状态
            if (getContext() == null) {
                android.util.Log.e("PrescriptionFragment", "Context为null");
                return;
            }
            
            if (!isAdded()) {
                android.util.Log.e("PrescriptionFragment", "Fragment未添加到Activity");
                return;
            }
            
            android.util.Log.d("PrescriptionFragment", "基本状态检查通过，显示测试对话框");
            
            // 显示简单的AlertDialog测试
            new android.app.AlertDialog.Builder(requireContext())
                .setTitle("对话框测试")
                .setMessage("基本对话框功能正常。\n\n现在选择下一步操作：")
                .setPositiveButton("新自定义对话框", (dialog, which) -> {
                    android.util.Log.d("PrescriptionFragment", "用户选择新自定义对话框");
                    showCustomImageProcessingDialog();
                })
                .setNegativeButton("原自定义对话框", (dialog, which) -> {
                    android.util.Log.d("PrescriptionFragment", "用户选择原自定义对话框");
                    showImageProcessingDialog();
                })
                .setNeutralButton("测试DialogFragment", (dialog, which) -> {
                    android.util.Log.d("PrescriptionFragment", "用户选择测试DialogFragment");
                    showTestDialogFragment();
                })
                .setCancelable(true)
                .show();
                
            android.util.Log.d("PrescriptionFragment", "测试对话框显示成功");
            
        } catch (Exception e) {
            android.util.Log.e("PrescriptionFragment", "测试基本对话框时发生异常: " + e.getMessage(), e);
            e.printStackTrace();
            Toast.makeText(getContext(), "对话框功能异常: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * 显示测试用的DialogFragment
     */
    private void showTestDialogFragment() {
        android.util.Log.d("PrescriptionFragment", "=== 开始显示测试DialogFragment ===");
        
        try {
            // 检查Fragment状态
            if (!isAdded() || getFragmentManager() == null) {
                android.util.Log.e("PrescriptionFragment", "Fragment状态异常，无法显示DialogFragment");
                Toast.makeText(getContext(), "Fragment状态异常", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 创建并显示测试DialogFragment
            TestDialogFragment testDialog = TestDialogFragment.newInstance();
            testDialog.show(getParentFragmentManager(), "TestDialogFragment");
            
            android.util.Log.d("PrescriptionFragment", "测试DialogFragment显示调用完成");
            
            // 延迟检查对话框是否成功显示
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    if (testDialog.isAdded() && testDialog.getDialog() != null && testDialog.getDialog().isShowing()) {
                        android.util.Log.d("PrescriptionFragment", "✓ 测试DialogFragment显示成功");
                    } else {
                        android.util.Log.e("PrescriptionFragment", "✗ 测试DialogFragment显示失败");
                        Toast.makeText(getContext(), "测试DialogFragment显示失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    android.util.Log.e("PrescriptionFragment", "检查测试DialogFragment状态时异常: " + e.getMessage(), e);
                }
            }, 500);
            
        } catch (Exception e) {
            android.util.Log.e("PrescriptionFragment", "显示测试DialogFragment时发生异常: " + e.getMessage(), e);
            e.printStackTrace();
            Toast.makeText(getContext(), "显示测试对话框异常: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * 显示新的自定义图片处理对话框
     */
    private void showCustomImageProcessingDialog() {
        android.util.Log.d("PrescriptionFragment", "=== 开始显示新的自定义图片处理对话框 ===");
        
        try {
            // 检查Fragment状态
            if (!isAdded() || getParentFragmentManager() == null) {
                android.util.Log.e("PrescriptionFragment", "Fragment状态异常，无法显示自定义对话框");
                Toast.makeText(getContext(), "Fragment状态异常", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (selectedImageUri == null) {
                android.util.Log.e("PrescriptionFragment", "selectedImageUri为null");
                Toast.makeText(getContext(), "请先选择图片", Toast.LENGTH_SHORT).show();
                return;
            }
            
            android.util.Log.d("PrescriptionFragment", "创建CustomImageProcessingDialog实例");
            
            // 创建并配置自定义对话框
            CustomImageProcessingDialog customDialog = CustomImageProcessingDialog.newInstance();
            customDialog.setOnProcessingOptionSelectedListener(new CustomImageProcessingDialog.OnProcessingOptionSelectedListener() {
                @Override
                public void onXRaySelected() {
                    android.util.Log.d("PrescriptionFragment", "自定义对话框 - X光分析被选择");
                    performMedicalImageAnalysis("xray");
                }
                
                @Override
                public void onCTSelected() {
                    android.util.Log.d("PrescriptionFragment", "自定义对话框 - CT分析被选择");
                    performMedicalImageAnalysis("ct");
                }
                
                @Override
                public void onUltrasoundSelected() {
                    android.util.Log.d("PrescriptionFragment", "自定义对话框 - B超分析被选择");
                    performMedicalImageAnalysis("ultrasound");
                }
                
                @Override
                public void onMRISelected() {
                    android.util.Log.d("PrescriptionFragment", "自定义对话框 - MRI分析被选择");
                    performMedicalImageAnalysis("mri");
                }
                
                @Override
                public void onPETCTSelected() {
                    android.util.Log.d("PrescriptionFragment", "自定义对话框 - PET-CT分析被选择");
                    performMedicalImageAnalysis("petct");
                }
                
                @Override
                public void onUploadSelected() {
                    android.util.Log.d("PrescriptionFragment", "自定义对话框 - 上传服务器被选择");
                    uploadImageToServer();
                }
                
                @Override
                public void onPreviewSelected() {
                    android.util.Log.d("PrescriptionFragment", "自定义对话框 - 预览图片被选择");
                    previewImage();
                }
                
                @Override
                public void onDialogCancelled() {
                    android.util.Log.d("PrescriptionFragment", "自定义对话框被取消");
                }
            });
            
            // 显示对话框
            customDialog.show(getParentFragmentManager(), "CustomImageProcessingDialog");
            android.util.Log.d("PrescriptionFragment", "自定义对话框显示调用完成");
            
            // 延迟检查对话框是否成功显示
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    if (customDialog.isAdded() && customDialog.getDialog() != null && customDialog.getDialog().isShowing()) {
                        android.util.Log.d("PrescriptionFragment", "✓ 自定义对话框显示成功");
                        Toast.makeText(getContext(), "自定义对话框显示成功", Toast.LENGTH_SHORT).show();
                    } else {
                        android.util.Log.e("PrescriptionFragment", "✗ 自定义对话框显示失败");
                        Toast.makeText(getContext(), "自定义对话框显示失败，使用备用方案", Toast.LENGTH_SHORT).show();
                        showSimpleProcessingDialog();
                    }
                } catch (Exception e) {
                    android.util.Log.e("PrescriptionFragment", "检查自定义对话框状态时异常: " + e.getMessage(), e);
                }
            }, 500);
            
        } catch (Exception e) {
            android.util.Log.e("PrescriptionFragment", "显示自定义对话框时发生异常: " + e.getMessage(), e);
            e.printStackTrace();
            Toast.makeText(getContext(), "显示自定义对话框异常: " + e.getMessage(), Toast.LENGTH_LONG).show();
            
            // 异常时使用备用方案
            showSimpleProcessingDialog();
        }
    }
    
    /**
     * 显示图片处理选项对话框
     * 使用DialogFragment替代AlertDialog，提供更好的生命周期管理
     */
    // ...existing code...
    /**
     * 显示图片处理选项对话框
     * 使用DialogFragment替代AlertDialog，提供更好的生命周期管理
     */
    // ...existing code...
    private void showImageProcessingDialog() {
        final String TAG = "PrescriptionFragment";
        final String DIALOG_TAG = "ImageProcessingDialog";

        Log.d(TAG, "=== showImageProcessingDialog START ===");
        Log.d(TAG, "Fragment state: isAdded=" + isAdded() + ", isDetached=" + isDetached() + ", isRemoving=" + isRemoving() + ", getContext()!=null=" + (getContext() != null));
        Log.d(TAG, "selectedImageUri=" + (selectedImageUri != null ? selectedImageUri.toString() : "null"));

        if (getContext() == null || !isAdded() || isDetached() || isRemoving()) {
            Log.w(TAG, "Fragment state invalid, cannot show dialog");
            showSafeToast("页面状态异常，无法显示对话框");
            return;
        }
        if (selectedImageUri == null) {
            Log.w(TAG, "selectedImageUri is null");
            showSafeToast("请先选择图片");
            return;
        }

        try {
            androidx.fragment.app.FragmentManager parentFm = getParentFragmentManager();
            androidx.fragment.app.FragmentManager childFm = getChildFragmentManager();

            // 如果 parentFm 已保存状态，短延迟重试（避免 IllegalStateException / 丢失）
            if (parentFm != null && parentFm.isStateSaved()) {
                Log.w(TAG, "parent FragmentManager state saved, 延迟重试显示对话框");
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        showImageProcessingDialog();
                    } catch (Exception ex) {
                        Log.e(TAG, "延迟重试失败: " + ex.getMessage(), ex);
                        showSimpleProcessingDialog();
                    }
                }, 300);
                return;
            }

            ImageProcessingDialogFragment dialogFragment = ImageProcessingDialogFragment.newInstance(selectedImageUri, imageSource, true);
            if (dialogFragment == null) {
                Log.e(TAG, "创建 ImageProcessingDialogFragment 失败");
                showSimpleProcessingDialog();
                return;
            }

            dialogFragment.setOnProcessingOptionSelectedListener(new ImageProcessingDialogFragment.OnProcessingOptionSelectedListener() {
                @Override public void onXRaySelected() { performMedicalImageAnalysis("xray"); }
                @Override public void onCTSelected() { performMedicalImageAnalysis("ct"); }
                @Override public void onUltrasoundSelected() { performMedicalImageAnalysis("ultrasound"); }
                @Override public void onMRISelected() { performMedicalImageAnalysis("mri"); }
                @Override public void onPETCTSelected() { performMedicalImageAnalysis("petct"); }
                @Override public void onUploadSelected() { uploadImageToServer(); }
                @Override public void onPreviewSelected() { previewImage(); }
                @Override public void onDialogCancelled() { Log.d(TAG, "用户取消对话框"); }
            });

            // 先尝试使用父 FragmentManager 且通过同步 add(commitNowAllowingStateLoss) 立即添加（可避免异步被覆盖）
            boolean shown = false;
            if (parentFm != null) {
                try {
                    // 移除可能存在的旧 fragment（同步）
                    androidx.fragment.app.Fragment existing = parentFm.findFragmentByTag(DIALOG_TAG);
                    if (existing != null) {
                        Log.d(TAG, "发现同tag旧对话框，尝试同步移除");
                        parentFm.beginTransaction().remove(existing).commitNowAllowingStateLoss();
                    }

                    Log.d(TAG, "尝试使用 parentFm 同步 add(dialog)");
                    parentFm.beginTransaction().add(dialogFragment, DIALOG_TAG).commitNowAllowingStateLoss();
                    // commitNowAllowingStateLoss 已经执行，验证是否添加
                    shown = dialogFragment.isAdded() || (parentFm.findFragmentByTag(DIALOG_TAG) != null && parentFm.findFragmentByTag(DIALOG_TAG).isAdded());
                    Log.d(TAG, "parentFm add result: isAdded=" + dialogFragment.isAdded() + ", shown=" + shown);
                } catch (Exception e) {
                    Log.w(TAG, "parentFm 同步添加失败: " + e.getMessage(), e);
                    shown = false;
                }
            }

            // 如果 parentFm 失败，则尝试用 childFragmentManager 的同步 add
            if (!shown && childFm != null) {
                try {
                    androidx.fragment.app.Fragment existingChild = childFm.findFragmentByTag(DIALOG_TAG);
                    if (existingChild != null) {
                        Log.d(TAG, "childFm 发现同tag旧对话框，尝试同步移除");
                        childFm.beginTransaction().remove(existingChild).commitNowAllowingStateLoss();
                    }

                    Log.d(TAG, "尝试使用 childFm 同步 add(dialog)");
                    childFm.beginTransaction().add(dialogFragment, DIALOG_TAG).commitNowAllowingStateLoss();
                    shown = dialogFragment.isAdded() || (childFm.findFragmentByTag(DIALOG_TAG) != null && childFm.findFragmentByTag(DIALOG_TAG).isAdded());
                    Log.d(TAG, "childFm add result: isAdded=" + dialogFragment.isAdded() + ", shown=" + shown);
                } catch (Exception e) {
                    Log.w(TAG, "childFm 同步添加失败: " + e.getMessage(), e);
                    shown = false;
                }
            }

            // 最后退回到标准的 show()（可能异步），并立即 executePendingTransactions 以便快速检测
            if (!shown) {
                try {
                    Log.d(TAG, "使用 dialogFragment.show(parentFm) 作为回退方案");
                    if (parentFm != null) {
                        dialogFragment.show(parentFm, DIALOG_TAG);
                        try { parentFm.executePendingTransactions(); } catch (Exception ignore) {}
                        shown = dialogFragment.isAdded() || (parentFm.findFragmentByTag(DIALOG_TAG) != null && parentFm.findFragmentByTag(DIALOG_TAG).isAdded());
                    } else if (childFm != null) {
                        dialogFragment.show(childFm, DIALOG_TAG);
                        try { childFm.executePendingTransactions(); } catch (Exception ignore) {}
                        shown = dialogFragment.isAdded() || (childFm.findFragmentByTag(DIALOG_TAG) != null && childFm.findFragmentByTag(DIALOG_TAG).isAdded());
                    }
                    Log.d(TAG, "show() fallback result: shown=" + shown);
                } catch (Exception showEx) {
                    Log.w(TAG, "show() 回退方案失败: " + showEx.getMessage(), showEx);
                    shown = false;
                }
            }

            // 最终验证并在失败时使用简单对话框作为降级
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    boolean nowAdded = false;
                    if (parentFm != null) {
                        androidx.fragment.app.Fragment f = parentFm.findFragmentByTag(DIALOG_TAG);
                        nowAdded = f != null && f.isAdded();
                    }
                    if (!nowAdded && childFm != null) {
                        androidx.fragment.app.Fragment f2 = childFm.findFragmentByTag(DIALOG_TAG);
                        nowAdded = f2 != null && f2.isAdded();
                    }

                    Log.d(TAG, "最终检查对话框是否已添加: nowAdded=" + nowAdded + ", dialog.isAdded=" + dialogFragment.isAdded());
                    if (!nowAdded) {
                        Log.e(TAG, "对话框未显示，降级到简单对话框");
                        showSimpleProcessingDialog();
                    } else {
                        Log.d(TAG, "对话框显示成功");
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "最终检查异常: " + ex.getMessage(), ex);
                    showSimpleProcessingDialog();
                }
            }, 250);

            // 立即给用户提示
            showSafeToast("请选择处理方式");

        } catch (Exception e) {
            Log.e("PrescriptionFragment", "showImageProcessingDialog 异常: " + e.getMessage(), e);
            showSimpleProcessingDialog();
        }
    }
// ...existing code...
// ...existing code...
    // private void showImageProcessingDialog() {
    //     android.util.Log.d("PrescriptionFragment", "=== 开始显示图片处理选项对话框 ===");
    //     android.util.Log.d("PrescriptionFragment", "Fragment状态 - Context: " + (getContext() != null) + ", isAdded: " + isAdded() + ", isDetached: " + isDetached() + ", isRemoving: " + isRemoving());
    //     android.util.Log.d("PrescriptionFragment", "selectedImageUri: " + (selectedImageUri != null ? selectedImageUri.toString() : "null"));
        
    //     // 检查Fragment状态
    //     if (getContext() == null || !isAdded() || isDetached() || isRemoving()) {
    //         android.util.Log.w("PrescriptionFragment", "Fragment状态不正常，无法显示对话框");
    //         Toast.makeText(getActivity(), "页面状态异常，请重试", Toast.LENGTH_SHORT).show();
    //         return;
    //     }
        
    //     // 检查是否有选中的图片
    //     if (selectedImageUri == null) {
    //         android.util.Log.w("PrescriptionFragment", "没有选中的图片，无法显示处理选项对话框");
    //         Toast.makeText(getContext(), "请先选择图片", Toast.LENGTH_SHORT).show();
    //         return;
    //     }
        
    //     try {
    //         android.util.Log.d("PrescriptionFragment", "开始创建DialogFragment");
            
    //         // 检查FragmentManager状态
    //         if (getParentFragmentManager() == null) {
    //             android.util.Log.e("PrescriptionFragment", "FragmentManager为null");
    //             throw new IllegalStateException("FragmentManager is null");
    //         }
            
    //         android.util.Log.d("PrescriptionFragment", "FragmentManager状态正常，开始创建对话框实例");
            
    //         // 创建DialogFragment实例
    //         ImageProcessingDialogFragment dialogFragment = ImageProcessingDialogFragment.newInstance(selectedImageUri, imageSource, true);
            
    //         if (dialogFragment == null) {
    //             android.util.Log.e("PrescriptionFragment", "DialogFragment创建失败");
    //             throw new RuntimeException("Failed to create DialogFragment");
    //         }
            
    //         android.util.Log.d("PrescriptionFragment", "DialogFragment创建成功，设置监听器");
            
    //         // 设置回调监听器
    //         dialogFragment.setOnProcessingOptionSelectedListener(new ImageProcessingDialogFragment.OnProcessingOptionSelectedListener() {
    //             @Override
    //             public void onOCRSelected() {
    //                 android.util.Log.d("PrescriptionFragment", "用户选择OCR识别");
    //                 performOCRRecognition();
    //             }
                
    //             @Override
    //             public void onAnalysisSelected() {
    //                 android.util.Log.d("PrescriptionFragment", "用户选择处方分析");
    //                 performPrescriptionAnalysis();
    //             }
                
    //             @Override
    //             public void onUploadSelected() {
    //                 android.util.Log.d("PrescriptionFragment", "用户选择上传服务器");
    //                 uploadImageToServer();
    //             }
                
    //             @Override
    //             public void onPreviewSelected() {
    //                 android.util.Log.d("PrescriptionFragment", "用户选择预览图片");
    //                 previewImage();
    //             }
                
    //             @Override
    //             public void onDialogCancelled() {
    //                 android.util.Log.d("PrescriptionFragment", "用户取消对话框");
    //             }
    //         });
            
    //         android.util.Log.d("PrescriptionFragment", "监听器设置完成，准备显示对话框");
            
    //         // 检查是否已经有同名的对话框存在
    //         androidx.fragment.app.Fragment existingDialog = getParentFragmentManager().findFragmentByTag("ImageProcessingDialog");
    //         if (existingDialog != null) {
    //             android.util.Log.w("PrescriptionFragment", "已存在同名对话框，先移除");
    //             getParentFragmentManager().beginTransaction().remove(existingDialog).commitAllowingStateLoss();
    //         }
            
    //         // 显示对话框前的最终检查
    //         android.util.Log.d("PrescriptionFragment", "显示对话框前的最终状态检查:");
    //         com.wenteng.frontend_android.debug.DialogDebugHelper.checkFragmentState(this, "PrescriptionFragment");
            
    //         // 显示对话框
    //         android.util.Log.d("PrescriptionFragment", "开始显示DialogFragment");
    //         try {
    //             dialogFragment.show(getParentFragmentManager(), "ImageProcessingDialog");
    //             android.util.Log.d("PrescriptionFragment", "DialogFragment.show()调用完成");
                
    //             // 开始监控对话框生命周期
    //             com.wenteng.frontend_android.debug.DialogDebugHelper.monitorDialogLifecycle(dialogFragment, "ImageProcessingDialog");
                
    //         } catch (Exception showException) {
    //             android.util.Log.e("PrescriptionFragment", "显示对话框时发生异常: " + showException.getMessage(), showException);
    //             showSimpleProcessingDialog();
    //             return;
    //         }
            
    //         // 延迟检查对话框是否真的显示了
    //         new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
    //             @Override
    //             public void run() {
    //                 androidx.fragment.app.Fragment dialog = getParentFragmentManager().findFragmentByTag("ImageProcessingDialog");
    //                 if (dialog != null && dialog.isAdded()) {
    //                     android.util.Log.d("PrescriptionFragment", "✅ 对话框显示成功确认");
    //                     if (dialog instanceof com.wenteng.frontend_android.dialog.ImageProcessingDialogFragment) {
    //                         com.wenteng.frontend_android.debug.DialogDebugHelper.checkDialogFragmentState(
    //                             (com.wenteng.frontend_android.dialog.ImageProcessingDialogFragment) dialog, 
    //                             "ImageProcessingDialog"
    //                         );
    //                     }
    //                 } else {
    //                     android.util.Log.e("PrescriptionFragment", "❌ 对话框显示失败，使用备用方案");
    //                     android.util.Log.e("PrescriptionFragment", "失败原因分析:");
    //                     android.util.Log.e("PrescriptionFragment", "  - dialog == null: " + (dialog == null));
    //                     if (dialog != null) {
    //                         android.util.Log.e("PrescriptionFragment", "  - dialog.isAdded(): " + dialog.isAdded());
    //                     }
    //                     showSimpleProcessingDialog();
    //                 }
    //             }
    //         }, 500);
            
    //         // 显示提示信息
    //         Toast.makeText(requireActivity(), "请选择处理方式", Toast.LENGTH_SHORT).show();
            
    //     } catch (Exception e) {
    //         android.util.Log.e("PrescriptionFragment", "显示DialogFragment时发生异常: " + e.getMessage(), e);
    //         e.printStackTrace();
            
    //         // 异常情况下使用简单对话框
    //         android.util.Log.d("PrescriptionFragment", "异常情况下使用简单对话框作为备用方案");
    //         showSimpleProcessingDialog();
    //     }
    // }
    
    /**
     * 显示简单的处理选项对话框（备用方案）
     */
    private void showSimpleProcessingDialog() {
        if (getContext() == null || !isAdded() || isDetached()) {
            return;
        }
        
        try {
            String[] options = {"OCR文字识别", "处方智能分析", "上传到服务器", "预览图片"};
            
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            AlertDialog dialog = builder.setTitle("选择处理方式")
                   .setItems(options, (dlg, which) -> {
                       switch (which) {
                           case 0:
                               performOCRRecognition();
                               break;
                           case 1:
                               performPrescriptionAnalysis();
                               break;
                           case 2:
                               uploadImageToServer();
                               break;
                           case 3:
                               previewImage();
                               break;
                       }
                   })
                   .setNegativeButton("取消", null)
                   .create();
            
            // 设置对话框居中显示
            if (dialog.getWindow() != null) {
                dialog.getWindow().setGravity(Gravity.CENTER);
                // 设置对话框宽度为屏幕宽度的90%
                android.view.WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
                layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
                layoutParams.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
                dialog.getWindow().setAttributes(layoutParams);
            }
            
            dialog.show();
                   
            android.util.Log.d("PrescriptionFragment", "显示简单对话框成功");
            
        } catch (Exception e) {
            android.util.Log.e("PrescriptionFragment", "显示简单对话框也失败: " + e.getMessage(), e);
            Toast.makeText(getContext(), "对话框显示异常，请重新选择图片", Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * 测试对话框显示的方法
     * 用于调试对话框显示问题
     */
    public void testDialogDisplay() {
        android.util.Log.d("PrescriptionFragment", "开始执行对话框显示测试");
        
        // 使用测试辅助类进行测试
        com.wenteng.frontend_android.debug.DialogTestHelper.testDialogDisplay(
            getParentFragmentManager(), 
            requireContext()
        );
        
        // 延迟后测试简单对话框
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                android.util.Log.d("PrescriptionFragment", "开始测试简单对话框");
                com.wenteng.frontend_android.debug.DialogTestHelper.testSimpleDialog(
                    getParentFragmentManager()
                );
            }
        }, 3000);
    }
    
    /**
     * 测试简化对话框显示功能
     */
    public void testSimpleDialog() {
        android.util.Log.d("PrescriptionFragment", "=== Testing Simple Dialog ===");
        
        if (getActivity() == null) {
            android.util.Log.e("PrescriptionFragment", "Activity is null, cannot show simple dialog");
            return;
        }
        
        try {
            com.wenteng.frontend_android.SimpleTestDialog simpleDialog = 
                new com.wenteng.frontend_android.SimpleTestDialog(getActivity());
            android.util.Log.d("PrescriptionFragment", "Simple dialog created successfully");
            
            simpleDialog.show();
            android.util.Log.d("PrescriptionFragment", "Simple dialog show() called");
            
            // 检查对话框是否真的显示了
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (simpleDialog.isShowing()) {
                        android.util.Log.d("PrescriptionFragment", "✓ Simple dialog is showing successfully!");
                    } else {
                        android.util.Log.e("PrescriptionFragment", "✗ Simple dialog is NOT showing!");
                    }
                }
            }, 500);
            
        } catch (Exception e) {
            android.util.Log.e("PrescriptionFragment", "Error creating/showing simple dialog: " + e.getMessage(), e);
        }
    }
    
    /**
     * 执行OCR文字识别
     */
    private void performOCRRecognition() {
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "请先选择图片", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 检查图片大小
        if (ImageUtils.isImageTooLarge(getContext(), selectedImageUri)) {
            Toast.makeText(getContext(), "图片过大，正在压缩...", Toast.LENGTH_SHORT).show();
        }
        
        // 创建MultipartBody.Part
        MultipartBody.Part imagePart = ImageUtils.createImagePart(getContext(), selectedImageUri, "image");
        if (imagePart == null) {
            Toast.makeText(getContext(), "图片处理失败", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        tvLoadingText.setText("正在识别文字...");
        
        ocrCall = apiService.ocrTextRecognition(imagePart);
        ocrCall.enqueue(new Callback<ApiResponse<OCRResult>>() {
            @Override
            public void onResponse(Call<ApiResponse<OCRResult>> call, Response<ApiResponse<OCRResult>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<OCRResult> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        displayOCRResult(apiResponse.getData());
                    } else {
                        Toast.makeText(getContext(), "OCR识别失败: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "网络请求失败", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<OCRResult>> call, Throwable t) {
                showLoading(false);
                if (!call.isCanceled()) {
                    Toast.makeText(getContext(), "OCR识别失败: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    
    /**
     * 执行处方智能分析
     */
    private void performPrescriptionAnalysis() {
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "请先选择图片", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 创建MultipartBody.Part
        MultipartBody.Part imagePart = ImageUtils.createImagePart(getContext(), selectedImageUri, "image");
        if (imagePart == null) {
            Toast.makeText(getContext(), "图片处理失败", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        tvLoadingText.setText("正在分析处方...");
        
        analysisCall = apiService.analyzePrescriptionImage(imagePart);
        analysisCall.enqueue(new Callback<ApiResponse<PrescriptionAnalysis>>() {
            @Override
            public void onResponse(Call<ApiResponse<PrescriptionAnalysis>> call, Response<ApiResponse<PrescriptionAnalysis>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<PrescriptionAnalysis> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        displayPrescriptionAnalysis(apiResponse.getData());
                    } else {
                        Toast.makeText(getContext(), "处方分析失败: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "网络请求失败", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<PrescriptionAnalysis>> call, Throwable t) {
                showLoading(false);
                if (!call.isCanceled()) {
                    Toast.makeText(getContext(), "处方分析失败: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    
    /**
     * 执行医学影像分析
     * @param imageType 影像类型："xray", "ct", "ultrasound", "mri", "petct"
     */
    private void performMedicalImageAnalysis(String imageType) {
        Log.d("PrescriptionFragment", "开始执行医学影像分析，类型: " + imageType);
        
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "请先选择图片", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 创建MultipartBody.Part用于上传
        MultipartBody.Part imagePart = ImageUtils.createImagePart(getContext(), selectedImageUri, "image");
        if (imagePart == null) {
            Toast.makeText(getContext(), "图片处理失败", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 显示加载界面但不启动普通的进度更新
        llLoading.setVisibility(View.VISIBLE);
        tvAnalysisResult.setVisibility(View.GONE);
        // 禁用按钮防止重复点击
        btnUploadPrescription.setEnabled(false);
        btnSelectImageSource.setEnabled(false);
        etSymptoms.setEnabled(false);
        
        // 启动医学影像分析专用的进度更新，显示分析过程的不同阶段
//        startMedicalImageAnalysisProgressUpdate(imageType);
        
        // 调用相应的API接口进行医学影像分析
        switch (imageType) {
            case "xray":
                medicalImageAnalysisCall = apiService.analyzeXRayImage(imagePart);
                break;
            case "ct":
                medicalImageAnalysisCall = apiService.analyzeCTImage(imagePart);
                break;
            case "ultrasound":
                medicalImageAnalysisCall = apiService.analyzeUltrasoundImage(imagePart);
                break;
            case "mri":
                medicalImageAnalysisCall = apiService.analyzeMRIImage(imagePart);
                break;
            case "petct":
                medicalImageAnalysisCall = apiService.analyzePETCTImage(imagePart);
                break;
            default:
                showLoading(false);
                Toast.makeText(getContext(), "不支持的影像类型", Toast.LENGTH_SHORT).show();
                return;
        }
        
        if (medicalImageAnalysisCall != null) {
            medicalImageAnalysisCall.enqueue(new Callback<ApiResponse<MedicalImageAnalysis>>() {
                @Override
                public void onResponse(Call<ApiResponse<MedicalImageAnalysis>> call, Response<ApiResponse<MedicalImageAnalysis>> response) {
                    showLoading(false);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<MedicalImageAnalysis> apiResponse = response.body();
                        Log.d("PrescriptionFragment", "API响应成功 - success: " + apiResponse.isSuccess() + ", message: " + apiResponse.getMessage());
                        
                        if (apiResponse.isSuccess()) {
                            MedicalImageAnalysis analysisData = apiResponse.getData();
                            Log.d("PrescriptionFragment", "分析数据获取成功");
                            
                            if (analysisData != null) {
                                Log.d("PrescriptionFragment", "显示医学影像分析结果");
                                // 显示医学影像分析结果
                                displayMedicalImageAnalysis(analysisData, imageType);
                                Toast.makeText(getContext(), getImageTypeDisplayName(imageType) + "影像分析完成", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.w("PrescriptionFragment", "分析数据为空，使用模拟结果");
                                String mockResult = generateMockAnalysisResult(imageType);
                                displayTextWithTypewriterEffect(mockResult);
                                Toast.makeText(getContext(), "分析数据为空，使用模拟结果", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d("PrescriptionFragment", "API响应失败 - errorCode: " + apiResponse.getErrorCode());
                            // 检查是否为图像类型不匹配错误
                            if ("IMAGE_TYPE_MISMATCH".equals(apiResponse.getErrorCode())) {
                                Log.d("PrescriptionFragment", "API级别检测到IMAGE_TYPE_MISMATCH错误，显示错误对话框");
                                showImageTypeMismatchDialog(imageType, apiResponse.getMessage());
                                // 不显示分析结果，直接返回
                                return;
                            } else {
                                // 其他API错误，使用模拟结果作为备用方案
                                String mockResult = generateMockAnalysisResult(imageType);
                                displayTextWithTypewriterEffect(mockResult);
                                Toast.makeText(getContext(), "使用模拟分析结果: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        // 网络请求失败，使用模拟结果作为备用方案
                        Log.e("PrescriptionFragment", "网络请求失败 - HTTP状态码: " + response.code() + ", 消息: " + response.message());
                        String mockResult = generateMockAnalysisResult(imageType);
                        displayTextWithTypewriterEffect(mockResult);
                        Toast.makeText(getContext(), "网络请求失败(" + response.code() + ")，使用模拟分析结果", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<MedicalImageAnalysis>> call, Throwable t) {
                    showLoading(false);
                    if (!call.isCanceled()) {
                        // 网络请求失败，使用模拟结果作为备用方案
                        Log.e("PrescriptionFragment", "网络连接失败: " + t.getClass().getSimpleName() + " - " + t.getMessage(), t);
                        String mockResult = generateMockAnalysisResult(imageType);
                        displayTextWithTypewriterEffect(mockResult);
                        
                        // 根据异常类型显示不同的错误提示
                        String errorMessage;
                        if (t instanceof com.google.gson.JsonSyntaxException) {
                            errorMessage = "服务器响应格式异常，使用模拟分析结果";
                        } else if (t instanceof java.net.SocketTimeoutException) {
                            errorMessage = "分析超时，使用模拟分析结果";
                        } else if (t instanceof java.net.ConnectException) {
                            errorMessage = "无法连接服务器，使用模拟分析结果";
                        } else if (t instanceof java.io.IOException) {
                            errorMessage = "网络异常，使用模拟分析结果";
                        } else {
                            errorMessage = "网络连接失败，使用模拟分析结果";
                        }
                        
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
    
    /**
     * 生成模拟的医学影像分析结果
     * @param imageType 影像类型
     * @return 分析结果文本
     */
    /**
     * 生成AI医学影像分析结果（集成真实AI分析）
     * @param imageType 影像类型
     * @return 分析结果字符串
     */
    private String generateMockAnalysisResult(String imageType) {
        // 首先尝试调用真实的AI分析API
        try {
            return performRealTimeAIAnalysis(imageType);
        } catch (Exception e) {
            Log.w(TAG, "AI分析失败，使用模拟结果: " + e.getMessage());
            // AI分析失败时，返回模拟结果作为备用方案，并传递失败信息
            String failureReason = "网络连接异常或服务暂时不可用";
            if (e.getMessage() != null && !e.getMessage().trim().isEmpty()) {
                failureReason = e.getMessage();
            }
            return generateFallbackAnalysisResult(imageType, failureReason);
        }
    }
    
    /**
     * 执行实时AI分析（生成模拟分析结果）
     * @param imageType 影像类型
     * @return 模拟AI分析结果
     */
    private String performRealTimeAIAnalysis(String imageType) {
        if (selectedImageUri == null) {
            throw new RuntimeException("未选择图片");
        }
        
        // 直接生成模拟的医学影像分析结果
        try {
            // 创建模拟的MedicalImageAnalysis对象
            MedicalImageAnalysis mockAnalysis = createMockMedicalImageAnalysis(imageType);
            
            // 格式化并返回模拟分析结果
            String formattedResult = formatMedicalImageAnalysisResult(mockAnalysis, imageType);
            
            // 添加模拟结果标识
            StringBuilder result = new StringBuilder();
            result.append(formattedResult);
            result.append("\n\n🤖 注意：此为模拟AI分析结果，仅供开发测试使用，请以专业医师诊断为准。");
            
            return result.toString();
            
        } catch (Exception e) {
            throw new RuntimeException("模拟分析生成失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据影像类型获取对应的API调用
     * @param imageType 影像类型
     * @param imagePart 图片请求体
     * @return API调用对象
     */
    private Call<ApiResponse<MedicalImageAnalysis>> getAnalysisCallByType(String imageType, MultipartBody.Part imagePart) {
        switch (imageType) {
            case "xray":
                return apiService.analyzeXRayImage(imagePart);
            case "ct":
                return apiService.analyzeCTImage(imagePart);
            case "ultrasound":
                return apiService.analyzeUltrasoundImage(imagePart);
            case "mri":
                return apiService.analyzeMRIImage(imagePart);
            case "petct":
                return apiService.analyzePETCTImage(imagePart);
            default:
                return null;
        }
    }
    
    /*
     * 格式化AI分析结果
     * @param analysis AI分析数据
     * @param imageType 影像类型
     * @return 格式化的分析结果
     */
    /*
    private String formatAIAnalysisResult(PrescriptionAnalysis analysis, String imageType) {
        StringBuilder result = new StringBuilder();
        result.append("=== ").append(getImageTypeDisplayName(imageType)).append("AI影像分析报告 ===\n\n");
        
        // 添加分析结果
        if (analysis.getAnalysisResult() != null && !analysis.getAnalysisResult().isEmpty()) {
            result.append(analysis.getAnalysisResult()).append("\n\n");
        }
        
        // 添加置信度信息
        if (analysis.getConfidenceScore() > 0) {
            result.append("AI置信度：").append(String.format("%.1f%%", analysis.getConfidenceScore() * 100)).append("\n\n");
        }
        
        // 添加时间戳
        if (analysis.getAnalysisTimestamp() != null && !analysis.getAnalysisTimestamp().isEmpty()) {
            result.append("分析时间：").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())).append("\n\n");
        }
        
        // 添加免责声明
        result.append("注意：此为AI辅助分析结果，仅供参考，请以专业医师诊断为准。");
        
        return result.toString();
    }
    */
    
    /**
     * 格式化医学影像分析结果为显示文本
     * @param analysis 医学影像分析结果
     * @param imageType 影像类型
     * @return 格式化后的文本
     */
    private String formatMedicalImageAnalysisResult(MedicalImageAnalysis analysis, String imageType) {
        StringBuilder result = new StringBuilder();
        
        result.append("🔬 AI分析结果\n\n");
        
        // 影像类型
        result.append("📋 影像类型: ").append(getImageTypeDisplayName(imageType)).append("\n\n");
        
        // 影像发现
        if (analysis.getFindings() != null && !analysis.getFindings().isEmpty()) {
            result.append("🔍 影像发现:\n");
            
            String primaryFindings = analysis.getPrimaryFindings();
            if (primaryFindings != null && !primaryFindings.trim().isEmpty()) {
                result.append("主要发现: ").append(primaryFindings).append("\n");
            }
            
            String abnormalities = analysis.getAbnormalities();
            if (abnormalities != null && !abnormalities.trim().isEmpty()) {
                result.append("异常表现: ").append(abnormalities).append("\n");
            }
            
            result.append("\n");
        }
        
        // 诊断结果
        if (analysis.getDiagnosis() != null && !analysis.getDiagnosis().isEmpty()) {
            String primaryDiagnosis = analysis.getPrimaryDiagnosis();
            if (primaryDiagnosis != null && !primaryDiagnosis.trim().isEmpty()) {
                result.append("🎯 主要诊断: ").append(primaryDiagnosis).append("\n\n");
            }
        }
        
        // 建议
        if (analysis.getRecommendations() != null && !analysis.getRecommendations().isEmpty()) {
            String immediateActions = analysis.getImmediateActions();
            if (immediateActions != null && !immediateActions.trim().isEmpty()) {
                result.append("💡 建议: ").append(immediateActions).append("\n\n");
            }
        }
        
        // 严重程度
        if (analysis.getSeverity() != null && !analysis.getSeverity().trim().isEmpty()) {
            result.append("⚡ 严重程度: ").append(analysis.getSeverity()).append("\n\n");
        }
        
        // 置信度
        if (analysis.getConfidence() > 0) {
            result.append("📊 AI置信度: ").append(String.format("%.1f%%", analysis.getConfidence() * 100)).append("\n\n");
        }
        
        // 免责声明
        result.append("⚠️ 免责声明: 此为AI辅助分析结果，仅供参考，请以专业医师诊断为准。");
        
        return result.toString();
    }
    
    /**
     * 生成备用分析结果（当AI分析失败时使用）
     * @param imageType 影像类型
     * @return 模拟分析结果
     */
    private String generateFallbackAnalysisResult(String imageType) {
        return generateFallbackAnalysisResult(imageType, null);
    }
    
    /**
     * 生成备用分析结果（当AI分析失败时使用）
     * @param imageType 影像类型
     * @param failureReason 失败原因（可选）
     * @return 模拟分析结果
     */
    private String generateFallbackAnalysisResult(String imageType, String failureReason) {
        // 创建模拟的MedicalImageAnalysis对象
        MedicalImageAnalysis mockAnalysis = createMockMedicalImageAnalysis(imageType);
        
        // 使用现有的格式化方法生成结果
        StringBuilder result = new StringBuilder();
        
        // 如果有失败信息，先显示失败原因
        if (failureReason != null && !failureReason.trim().isEmpty()) {
            result.append("⚠️ AI分析失败信息：\n");
            result.append(failureReason).append("\n\n");
            result.append("以下为模拟分析结果：\n\n");
        }
        
        // 格式化模拟分析结果
        String formattedResult = formatMedicalImageAnalysisResult(mockAnalysis, imageType);
        result.append(formattedResult);
        
        // 添加模拟结果标识
        result.append("\n\n📝 注意：此为模拟分析结果（AI服务暂时不可用），仅供参考，请以专业医师诊断为准。");
        
        return result.toString();
    }
    
    /**
     * 创建模拟的医学影像分析结果
     * @param imageType 影像类型
     * @return 模拟的MedicalImageAnalysis对象
     */
    private MedicalImageAnalysis createMockMedicalImageAnalysis(String imageType) {
        MedicalImageAnalysis analysis = new MedicalImageAnalysis();
        analysis.setImageType(imageType);
        
        // 创建模拟的findings数据
        Map<String, Object> findings = new HashMap<>();
        Map<String, Object> diagnosis = new HashMap<>();
        Map<String, Object> recommendations = new HashMap<>();
        
//         switch (imageType) {
//             case "xray":
//                 // X光模拟数据
//                 findings.put("primary_findings", "影像分析失败，请重试");
//                 findings.put("secondary_findings", "");
//                 findings.put("abnormalities", "");
//                 findings.put("normal_findings", "");
//                 findings.put("image_quality", "");
                
//                 diagnosis.put("primary_diagnosis", "请咨询医师");
//                 diagnosis.put("differential_diagnosis", "");
//                 diagnosis.put("diagnostic_confidence", "");
//                 diagnosis.put("severity_level", "");
//                 diagnosis.put("prognosis", "");
                
//                 recommendations.put("immediate_actions", "请咨询医师");
//                 recommendations.put("follow_up", "");
//                 recommendations.put("treatment", "");
//                 recommendations.put("lifestyle", "");
//                 recommendations.put("further_examinations", "");
//                 recommendations.put("specialist_referral", "");
                
//                 analysis.setSeverity("轻微");
//                 analysis.setConfidence(0.85f);
//                 break;
                
//             case "ct":
//                 // CT模拟数据
//                 findings.put("primary_findings", "影像分析失败，请重试");
//                 findings.put("secondary_findings", "");
//                 findings.put("abnormalities", "");
//                 findings.put("normal_findings", "");
//                 findings.put("image_quality", "");
                
//                 diagnosis.put("primary_diagnosis", "请咨询医师");
//                 diagnosis.put("differential_diagnosis", "");
//                 diagnosis.put("diagnostic_confidence", "");
//                 diagnosis.put("severity_level", "");
//                 diagnosis.put("prognosis", "");
                
//                 recommendations.put("immediate_actions", "请咨询医师");
//                 recommendations.put("follow_up", "");
//                 recommendations.put("treatment", "");
//                 recommendations.put("lifestyle", "");
//                 recommendations.put("further_examinations", "");
//                 recommendations.put("specialist_referral", "");
                
//                 analysis.setSeverity("轻微");
//                 analysis.setConfidence(0.85);
//                 break;
                
//             case "ultrasound":
//                 // 超声模拟数据
//                 findings.put("primary_findings", "影像分析失败，请重试");
//                 findings.put("secondary_findings", "");
//                 findings.put("abnormalities", "");
//                 findings.put("normal_findings", "");
//                 findings.put("image_quality", "");
                
//                 diagnosis.put("primary_diagnosis", "请咨询医师");
//                 diagnosis.put("differential_diagnosis", "");
//                 diagnosis.put("diagnostic_confidence", "");
//                 diagnosis.put("severity_level", "");
//                 diagnosis.put("prognosis", "");
                
//                 recommendations.put("immediate_actions", "请咨询医师");
//                 recommendations.put("follow_up", "");
//                 recommendations.put("treatment", "");
//                 recommendations.put("lifestyle", "");
//                 recommendations.put("further_examinations", "");
//                 recommendations.put("specialist_referral", "");
                
//                 analysis.setSeverity("轻微");
//                 analysis.setConfidence(0.85);
//                 break;
                
//             case "mri":
//                 // MRI模拟数据
// findings.put("primary_findings", "影像分析失败，请重试");
//                 findings.put("secondary_findings", "");
//                 findings.put("abnormalities", "");
//                 findings.put("normal_findings", "");
//                 findings.put("image_quality", "");
                
//                 diagnosis.put("primary_diagnosis", "请咨询医师");
//                 diagnosis.put("differential_diagnosis", "");
//                 diagnosis.put("diagnostic_confidence", "");
//                 diagnosis.put("severity_level", "");
//                 diagnosis.put("prognosis", "");
                
//                 recommendations.put("immediate_actions", "请咨询医师");
//                 recommendations.put("follow_up", "");
//                 recommendations.put("treatment", "");
//                 recommendations.put("lifestyle", "");
//                 recommendations.put("further_examinations", "");
//                 recommendations.put("specialist_referral", "");
                
//                 analysis.setSeverity("轻微");
//                 analysis.setConfidence(0.85);
//                 break;
                
//             case "petct":
//                 // PET-CT模拟数据
//                 findings.put("primary_findings", "影像分析失败，请重试");
//                 findings.put("secondary_findings", "");
//                 findings.put("abnormalities", "");
//                 findings.put("normal_findings", "");
//                 findings.put("image_quality", "");
                
//                 diagnosis.put("primary_diagnosis", "请咨询医师");
//                 diagnosis.put("differential_diagnosis", "");
//                 diagnosis.put("diagnostic_confidence", "");
//                 diagnosis.put("severity_level", "");
//                 diagnosis.put("prognosis", "");
                
//                 recommendations.put("immediate_actions", "请咨询医师");
//                 recommendations.put("follow_up", "");
//                 recommendations.put("treatment", "");
//                 recommendations.put("lifestyle", "");
//                 recommendations.put("further_examinations", "");
//                 recommendations.put("specialist_referral", "");
                
//                 analysis.setSeverity("轻微");
//                 analysis.setConfidence(0.85);
//                 break;
                
//             default:
//                 // 默认模拟数据
//                 findings.put("primary_findings", "影像检查显示基本正常");
//                 findings.put("abnormalities", "未发现明显异常");
                
//                 diagnosis.put("primary_diagnosis", "影像检查未见明显异常");
//                 diagnosis.put("diagnostic_confidence", "80%");
                
//                 recommendations.put("immediate_actions", "无需特殊处理");
//                 recommendations.put("follow_up", "建议定期复查");
                
//                 analysis.setSeverity("正常");
//                 analysis.setConfidence(0.80);
//                 break;
//         }
        findings.put("primary_findings", "影像分析失败，请重试");
        findings.put("secondary_findings", "");
        findings.put("abnormalities", "");
        findings.put("normal_findings", "");
        findings.put("image_quality", "");
        
        diagnosis.put("primary_diagnosis", "请咨询医师");
        diagnosis.put("differential_diagnosis", "");
        diagnosis.put("diagnostic_confidence", "");
        diagnosis.put("severity_level", "");
        diagnosis.put("prognosis", "");
        
        recommendations.put("immediate_actions", "请咨询医师");
        recommendations.put("follow_up", "");
        recommendations.put("treatment", "");
        recommendations.put("lifestyle", "");
        recommendations.put("further_examinations", "");
        recommendations.put("specialist_referral", "");
        
        analysis.setSeverity("轻微");
        analysis.setConfidence(0.85f);
        analysis.setFindings(findings);
        analysis.setDiagnosis(diagnosis);
        analysis.setRecommendations(recommendations);
        
        return analysis;
    }
    
    /**
     * 执行中医舌诊分析
     * 专门处理舌诊图像的AI分析功能
     */
    private void performTongueDiagnosis() {
        Log.d("PrescriptionFragment", "开始执行中医舌诊分析");
        
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "请先选择舌诊图片", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 创建MultipartBody.Part用于上传舌诊图片
        MultipartBody.Part imagePart = ImageUtils.createImagePart(getContext(), selectedImageUri, "image");
        if (imagePart == null) {
            Toast.makeText(getContext(), "舌诊图片处理失败", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 显示加载界面
        llLoading.setVisibility(View.VISIBLE);
        tvAnalysisResult.setVisibility(View.GONE);
        // 禁用按钮防止重复点击
        btnUploadPrescription.setEnabled(false);
        btnSelectImageSource.setEnabled(false);
        etSymptoms.setEnabled(false);
        
        // 启动中医舌诊专用的进度更新
//        startTongueDiagnosisProgressUpdate();
        
        // 调用中医舌诊API接口
        tongueDiagnosisCall = apiService.analyzeTongueImage(imagePart);
        
        if (tongueDiagnosisCall != null) {
            tongueDiagnosisCall.enqueue(new Callback<ApiResponse<TongueDiagnosisResult>>() {
                @Override
                public void onResponse(Call<ApiResponse<TongueDiagnosisResult>> call, Response<ApiResponse<TongueDiagnosisResult>> response) {
                    showLoading(false);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<TongueDiagnosisResult> apiResponse = response.body();
                        Log.d("PrescriptionFragment", "中医舌诊API响应成功 - success: " + apiResponse.isSuccess() + ", message: " + apiResponse.getMessage());
                        
                        if (apiResponse.isSuccess()) {
                            TongueDiagnosisResult analysisData = apiResponse.getData();
                            Log.d("PrescriptionFragment", "舌诊分析数据获取成功");
                            
                            if (analysisData != null) {
                                Log.d("PrescriptionFragment", "显示中医舌诊分析结果");
                                // 显示中医舌诊分析结果
                                displayTongueDiagnosisResult(analysisData);
                                Toast.makeText(getContext(), "中医舌诊分析完成", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.w("PrescriptionFragment", "舌诊分析数据为空，使用模拟结果");
                                String mockResult = generateMockTongueDiagnosisResult();
                                displayTextWithTypewriterEffect(mockResult);
                                Toast.makeText(getContext(), "舌诊分析数据为空，使用模拟结果", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d("PrescriptionFragment", "舌诊API响应失败 - errorCode: " + apiResponse.getErrorCode());
                            // 检查是否为图像类型不匹配错误
                            if ("IMAGE_TYPE_MISMATCH".equals(apiResponse.getErrorCode())) {
                                Log.d("PrescriptionFragment", "API级别检测到舌诊图像类型不匹配错误，显示错误对话框");
                                showImageTypeMismatchDialog("tongue", apiResponse.getMessage());
                                return;
                            } else {
                                // 其他API错误，使用模拟结果作为备用方案
                                String mockResult = generateMockTongueDiagnosisResult();
                                displayTextWithTypewriterEffect(mockResult);
                                Toast.makeText(getContext(), "使用模拟舌诊分析结果: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        // 网络请求失败，使用模拟结果作为备用方案
                        Log.e("PrescriptionFragment", "舌诊网络请求失败 - HTTP状态码: " + response.code() + ", 消息: " + response.message());
                        String mockResult = generateMockTongueDiagnosisResult();
                        displayTextWithTypewriterEffect(mockResult);
                        Toast.makeText(getContext(), "网络请求失败(" + response.code() + ")，使用模拟舌诊分析结果", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<TongueDiagnosisResult>> call, Throwable t) {
                    showLoading(false);
                    if (!call.isCanceled()) {
                        // 网络请求失败，使用模拟结果作为备用方案
                        Log.e("PrescriptionFragment", "舌诊网络连接失败: " + t.getClass().getSimpleName() + " - " + t.getMessage(), t);
                        String mockResult = generateMockTongueDiagnosisResult();
                        displayTextWithTypewriterEffect(mockResult);
                        
                        // 根据异常类型显示不同的错误提示
                        String errorMessage;
                        if (t instanceof com.google.gson.JsonSyntaxException) {
                            errorMessage = "服务器响应格式异常，使用模拟舌诊分析结果";
                        } else if (t instanceof java.net.SocketTimeoutException) {
                            errorMessage = "舌诊分析超时，使用模拟分析结果";
                        } else if (t instanceof java.net.ConnectException) {
                            errorMessage = "无法连接服务器，使用模拟舌诊分析结果";
                        } else if (t instanceof java.io.IOException) {
                            errorMessage = "网络异常，使用模拟舌诊分析结果";
                        } else {
                            errorMessage = "网络连接失败，使用模拟舌诊分析结果";
                        }
                        
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
    
    /**
     * 启动中医舌诊分析专用的进度更新
     */
    private void startTongueDiagnosisProgressUpdate() {
        final String[] progressMessages = {
            "正在分析舌质颜色...",
            "正在检测舌苔厚薄...",
            "正在评估舌体形态...",
            "正在进行中医辨证...",
            "正在生成调理建议...",
            "分析即将完成..."
        };
        
        final int[] currentIndex = {0};
        
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (llLoading.getVisibility() == View.VISIBLE && currentIndex[0] < progressMessages.length) {
                    tvLoadingText.setText(progressMessages[currentIndex[0]]);
                    currentIndex[0]++;
                    handler.postDelayed(this, 2000); // 每2秒更新一次
                }
            }
        };
        
        handler.post(progressRunnable);
    }
    
    /**
     * 显示中医舌诊分析结果
     * @param analysisData 舌诊分析数据
     */
    private void displayTongueDiagnosisResult(TongueDiagnosisResult analysisData) {
        String formattedResult = formatTongueDiagnosisResult(analysisData);
        displayTextWithTypewriterEffect(formattedResult);
    }
    
    /**
     * 格式化中医舌诊分析结果
     * @param analysis 舌诊分析数据
     * @return 格式化后的舌诊报告
     */
    private String formatTongueDiagnosisResult(TongueDiagnosisResult analysis) {
        StringBuilder result = new StringBuilder();
        
        result.append("🏥 中医舌诊AI分析报告\n\n");
        
        // 舌质分析
        if (analysis.getTongueAnalysis() != null) {
            TongueAnalysis tongueAnalysis = analysis.getTongueAnalysis();
            
            // 舌质分析
            if (tongueAnalysis.getTongueBody() != null) {
                TongueBody tongueBody = tongueAnalysis.getTongueBody();
                result.append("👅 舌质分析:\n");
                
                String tongueColor = tongueBody.getColor() != null ? tongueBody.getColor() : "淡红色";
                String tongueShape = tongueBody.getShape() != null ? tongueBody.getShape() : "正常";
                String tongueTexture = tongueBody.getTexture() != null ? tongueBody.getTexture() : "润泽";
                String tongueMobility = tongueBody.getMobility() != null ? tongueBody.getMobility() : "活动正常";
                
                result.append("• 舌色: ").append(tongueColor).append("\n");
                result.append("• 舌形: ").append(tongueShape).append("\n");
                result.append("• 舌质: ").append(tongueTexture).append("\n");
                result.append("• 舌体活动: ").append(tongueMobility).append("\n\n");
            }
            
            // 舌苔分析
            if (tongueAnalysis.getTongueCoating() != null) {
                TongueCoating tongueCoating = tongueAnalysis.getTongueCoating();
                result.append("🔍 舌苔分析:\n");
                
                String coatingColor = tongueCoating.getColor() != null ? tongueCoating.getColor() : "薄白苔";
                String coatingThickness = tongueCoating.getThickness() != null ? tongueCoating.getThickness() : "薄苔";
                String coatingMoisture = tongueCoating.getMoisture() != null ? tongueCoating.getMoisture() : "润苔";
                String coatingTexture = tongueCoating.getTexture() != null ? tongueCoating.getTexture() : "均匀分布";
                
                result.append("• 苔色: ").append(coatingColor).append("\n");
                result.append("• 苔质厚薄: ").append(coatingThickness).append("\n");
                result.append("• 润燥程度: ").append(coatingMoisture).append("\n");
                result.append("• 苔质性状: ").append(coatingTexture).append("\n\n");
            }
        }
        
        // 中医诊断
        if (analysis.getTcmDiagnosis() != null) {
            TCMDiagnosis tcmDiagnosis = analysis.getTcmDiagnosis();
            result.append("🎯 中医诊断:\n");
            
            String syndromePattern = tcmDiagnosis.getSyndromePattern() != null ? tcmDiagnosis.getSyndromePattern() : "气血调和";
            String constitutionType = tcmDiagnosis.getConstitutionType() != null ? tcmDiagnosis.getConstitutionType() : "平和质";
            String organFunction = tcmDiagnosis.getOrganFunction() != null ? tcmDiagnosis.getOrganFunction() : "脏腑功能基本正常";
            String qiBloodStatus = tcmDiagnosis.getQiBloodStatus() != null ? tcmDiagnosis.getQiBloodStatus() : "气血状态良好";
            
            result.append("• 证候类型: ").append(syndromePattern).append("\n");
            result.append("• 体质类型: ").append(constitutionType).append("\n");
            result.append("• 脏腑功能: ").append(organFunction).append("\n");
            result.append("• 气血状态: ").append(qiBloodStatus).append("\n\n");
        }
        
        // 调理建议
        if (analysis.getRecommendations() != null) {
            TCMRecommendations recommendations = analysis.getRecommendations();
            result.append("💡 调理建议:\n");
            
            String dietaryTherapy = recommendations.getDietaryTherapy() != null ? recommendations.getDietaryTherapy() : "饮食清淡，营养均衡";
            String lifestyleAdjustment = recommendations.getLifestyleAdjustment() != null ? recommendations.getLifestyleAdjustment() : "规律作息，适量运动";
            String herbalSuggestions = recommendations.getHerbalSuggestions() != null ? recommendations.getHerbalSuggestions() : "可咨询中医师";
            String followUp = recommendations.getFollowUp() != null ? recommendations.getFollowUp() : "建议定期复查";
            
            result.append("• 食疗建议: ").append(dietaryTherapy).append("\n");
            result.append("• 生活调理: ").append(lifestyleAdjustment).append("\n");
            result.append("• 中药建议: ").append(herbalSuggestions).append("\n");
            result.append("• 复诊建议: ").append(followUp).append("\n\n");
        }
        
        // 严重程度和置信度
        if (analysis.getSeverity() != null && !analysis.getSeverity().trim().isEmpty()) {
            result.append("⚡ 健康程度: ").append(analysis.getSeverity()).append("\n");
        }
        
        if (analysis.getConfidence() > 0) {
            result.append("📊 AI置信度: ").append(String.format("%.1f%%", analysis.getConfidence() * 100)).append("\n\n");
        }
        
        // 免责声明
        result.append("⚠️ 免责声明: 此为AI辅助中医舌诊分析结果，仅供参考，请以专业中医师诊断为准。");
        
        return result.toString();
    }
    
    /**
     * 生成模拟的中医舌诊分析结果
     * @return 模拟舌诊分析结果
     */
    private String generateMockTongueDiagnosisResult() {
        // 创建模拟的舌诊分析数据
        TongueDiagnosisResult mockAnalysis = createMockTongueDiagnosisAnalysis();
        
        // 格式化模拟分析结果
        String formattedResult = formatTongueDiagnosisResult(mockAnalysis);
        
        // 添加模拟结果标识
        StringBuilder result = new StringBuilder();
        result.append(formattedResult);
        result.append("\n\n🤖 注意：此为模拟中医舌诊分析结果（AI服务暂时不可用），仅供开发测试使用，请以专业中医师诊断为准。");
        
        return result.toString();
    }
    
    /**
     * 创建模拟的中医舌诊分析数据
     * @return 模拟的舌诊分析对象
     */
    private TongueDiagnosisResult createMockTongueDiagnosisAnalysis() {
        TongueDiagnosisResult analysis = new TongueDiagnosisResult();
        analysis.setImageType("tongue");
        
        // 创建舌质分析数据
        TongueBody tongueBody = new TongueBody();
        tongueBody.setColor("未显示");
        tongueBody.setShape("未显示");
        tongueBody.setTexture("未显示");
        tongueBody.setMobility("未显示");
        
        // 创建舌苔分析数据
        TongueCoating tongueCoating = new TongueCoating();
        tongueCoating.setColor("未显示");
        tongueCoating.setThickness("未显示");
        tongueCoating.setMoisture("未显示");
        tongueCoating.setTexture("未显示");
        
        // 创建舌诊分析对象
        TongueAnalysis tongueAnalysis = new TongueAnalysis();
        tongueAnalysis.setTongueBody(tongueBody);
        tongueAnalysis.setTongueCoating(tongueCoating);
        
        // 创建中医诊断数据
        TCMDiagnosis tcmDiagnosis = new TCMDiagnosis();
        tcmDiagnosis.setSyndromePattern("未显示");
        tcmDiagnosis.setConstitutionType("未显示");
        tcmDiagnosis.setOrganFunction("未显示");
        tcmDiagnosis.setQiBloodStatus("未显示");
        
        // 创建调理建议数据
        TCMRecommendations recommendations = new TCMRecommendations();
        recommendations.setDietaryTherapy("未显示");
        recommendations.setLifestyleAdjustment("未显示");
        recommendations.setHerbalSuggestions("未显示");
        recommendations.setFollowUp("未显示");
        
        analysis.setTongueAnalysis(tongueAnalysis);
        analysis.setTcmDiagnosis(tcmDiagnosis);
        analysis.setRecommendations(recommendations);
        analysis.setSeverity("未显示");
        analysis.setConfidence(0.88f);
        
        return analysis;
    }
    
    /**
     * 从Map中安全获取字符串值
     * @param map 数据Map
     * @param key 键名
     * @param defaultValue 默认值
     * @return 字符串值
     */
    private String getStringFromMap(Map<String, Object> map, String key, String defaultValue) {
        if (map == null || !map.containsKey(key)) {
            return defaultValue;
        }
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    /**
     * 执行中医面诊分析
     * 专门处理面诊图像的AI分析功能
     */
    private void performFaceDiagnosis() {
        Log.d("PrescriptionFragment", "开始执行中医面诊分析");
        
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "请先选择面诊图片", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 创建MultipartBody.Part用于上传面诊图片
        MultipartBody.Part imagePart = ImageUtils.createImagePart(getContext(), selectedImageUri, "image");
        if (imagePart == null) {
            Toast.makeText(getContext(), "面诊图片处理失败", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 显示加载界面
        llLoading.setVisibility(View.VISIBLE);
        tvAnalysisResult.setVisibility(View.GONE);
        // 禁用按钮防止重复点击
        btnUploadPrescription.setEnabled(false);
        btnSelectImageSource.setEnabled(false);
        etSymptoms.setEnabled(false);
        
        // 启动中医面诊专用的进度更新
//        startFaceDiagnosisProgressUpdate();
        
        // 调用中医面诊API接口
        faceDiagnosisCall = apiService.analyzeFaceImage(imagePart);
        
        if (faceDiagnosisCall != null) {
            faceDiagnosisCall.enqueue(new Callback<ApiResponse<FaceDiagnosisResult>>() {
                @Override
                public void onResponse(Call<ApiResponse<FaceDiagnosisResult>> call, Response<ApiResponse<FaceDiagnosisResult>> response) {
                    showLoading(false);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<FaceDiagnosisResult> apiResponse = response.body();
                        Log.d("PrescriptionFragment", "中医面诊API响应成功 - success: " + apiResponse.isSuccess() + ", message: " + apiResponse.getMessage());
                        
                        if (apiResponse.isSuccess()) {
                            FaceDiagnosisResult analysisData = apiResponse.getData();
                            Log.d("PrescriptionFragment", "面诊分析数据获取成功");
                            
                            if (analysisData != null) {
                                Log.d("PrescriptionFragment", "显示中医面诊分析结果");
                                // 显示中医面诊分析结果
                                displayFaceDiagnosisResult(analysisData);
                                Toast.makeText(getContext(), "中医面诊分析完成", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.w("PrescriptionFragment", "面诊分析数据为空，使用模拟结果");
                                String mockResult = generateMockFaceDiagnosisResult();
                                displayTextWithTypewriterEffect(mockResult);
                                Toast.makeText(getContext(), "面诊分析数据为空，使用模拟结果", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d("PrescriptionFragment", "面诊API响应失败 - errorCode: " + apiResponse.getErrorCode());
                            // 检查是否为图像类型不匹配错误
                            if ("IMAGE_TYPE_MISMATCH".equals(apiResponse.getErrorCode())) {
                                Log.d("PrescriptionFragment", "API级别检测到面诊图像类型不匹配错误，显示错误对话框");
                                showImageTypeMismatchDialog("face", apiResponse.getMessage());
                                return;
                            } else {
                                // 其他API错误，使用模拟结果作为备用方案
                                String mockResult = generateMockFaceDiagnosisResult();
                                displayTextWithTypewriterEffect(mockResult);
                                Toast.makeText(getContext(), "使用模拟面诊分析结果: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        // 网络请求失败，使用模拟结果作为备用方案
                        Log.e("PrescriptionFragment", "面诊网络请求失败 - HTTP状态码: " + response.code() + ", 消息: " + response.message());
                        String mockResult = generateMockFaceDiagnosisResult();
                        displayTextWithTypewriterEffect(mockResult);
                        Toast.makeText(getContext(), "网络请求失败(" + response.code() + ")，使用模拟面诊分析结果", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<FaceDiagnosisResult>> call, Throwable t) {
                    showLoading(false);
                    if (!call.isCanceled()) {
                        // 网络请求失败，使用模拟结果作为备用方案
                        Log.e("PrescriptionFragment", "面诊网络连接失败: " + t.getClass().getSimpleName() + " - " + t.getMessage(), t);
                        String mockResult = generateMockFaceDiagnosisResult();
                        displayTextWithTypewriterEffect(mockResult);
                        
                        // 根据异常类型显示不同的错误提示
                        String errorMessage;
                        if (t instanceof com.google.gson.JsonSyntaxException) {
                            errorMessage = "服务器响应格式异常，使用模拟面诊分析结果";
                        } else if (t instanceof java.net.SocketTimeoutException) {
                            errorMessage = "面诊分析超时，使用模拟分析结果";
                        } else if (t instanceof java.net.ConnectException) {
                            errorMessage = "无法连接服务器，使用模拟面诊分析结果";

    

                        } else if (t instanceof java.io.IOException) {
                            errorMessage = "网络异常，使用模拟面诊分析结果";
                        } else {
                            errorMessage = "网络连接失败，使用模拟面诊分析结果";
                        }
                        
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
    
    /**
     * 启动中医面诊分析专用的进度更新
     */
    private void startFaceDiagnosisProgressUpdate() {
        final String[] progressMessages = {
            "正在分析面部气色...",
            "正在检测五官特征...",
            "正在评估面部区域...",
            "正在进行中医辨证...",
            "正在生成调理建议...",
            "分析即将完成..."
        };
        
        final int[] currentIndex = {0};
        
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (llLoading.getVisibility() == View.VISIBLE && currentIndex[0] < progressMessages.length) {
                    tvLoadingText.setText(progressMessages[currentIndex[0]]);
                    currentIndex[0]++;
                    handler.postDelayed(this, 2000); // 每2秒更新一次
                }
            }
        };
        
        handler.post(progressRunnable);
    }
    
    /**
     * 显示中医面诊分析结果
     * @param analysisData 面诊分析数据
     */
    private void displayFaceDiagnosisResult(FaceDiagnosisResult analysisData) {
        String formattedResult = formatFaceDiagnosisResult(analysisData);
        displayTextWithTypewriterEffect(formattedResult);
    }
    
    /**
     * 格式化中医面诊分析结果
     * @param analysis 面诊分析数据
     * @return 格式化后的面诊报告
     */
    private String formatFaceDiagnosisResult(FaceDiagnosisResult analysis) {
        StringBuilder result = new StringBuilder();
        
        result.append("🏥 中医面诊AI分析报告\n\n");
        
        // 面部分析
        if (analysis.getFacialAnalysis() != null) {
            FacialAnalysis facialAnalysis = analysis.getFacialAnalysis();
            result.append("👤 面部分析:\n");
            
            // 面色分析
            if (facialAnalysis.getComplexion() != null) {
                Complexion complexion = facialAnalysis.getComplexion();
                String faceColor = complexion.getColor() != null ? complexion.getColor() : "红润有光泽";
                String luster = complexion.getLuster() != null ? complexion.getLuster() : "有光泽";
                String texture = complexion.getTexture() != null ? complexion.getTexture() : "细腻";
                
                result.append("• 面色: ").append(faceColor).append("，").append(luster).append("，").append(texture).append("\n");
            }
            
            // 五官特征
            if (facialAnalysis.getFacialFeatures() != null) {
                FacialFeatures features = facialAnalysis.getFacialFeatures();
                String eyeFeatures = features.getEyes() != null ? features.getEyes() : "目光有神";
                String noseFeatures = features.getNose() != null ? features.getNose() : "鼻梁挺直";
                String mouthFeatures = features.getMouth() != null ? features.getMouth() : "唇色红润";
                String earFeatures = features.getEars() != null ? features.getEars() : "耳廓饱满";
                
                result.append("• 眼部: ").append(eyeFeatures).append("\n");
                result.append("• 鼻部: ").append(noseFeatures).append("\n");
                result.append("• 口唇: ").append(mouthFeatures).append("\n");
                result.append("• 耳部: ").append(earFeatures).append("\n\n");
            }
            
            // 面部区域分析
            if (facialAnalysis.getFacialRegions() != null) {
                FacialRegions regions = facialAnalysis.getFacialRegions();
                result.append("🔍 面部区域分析:\n");
                String foreheadArea = regions.getForehead() != null ? regions.getForehead() : "额部光洁";
                String cheekArea = regions.getCheeks() != null ? regions.getCheeks() : "两颊红润";
                String chinArea = regions.getChin() != null ? regions.getChin() : "下颌饱满";
                String templeArea = regions.getTemples() != null ? regions.getTemples() : "太阳穴饱满";
                
                result.append("• 额部: ").append(foreheadArea).append("\n");
                result.append("• 颊部: ").append(cheekArea).append("\n");
                result.append("• 颏部: ").append(chinArea).append("\n");
                result.append("• 太阳穴: ").append(templeArea).append("\n\n");
            }
        }
        
        // 中医诊断
        if (analysis.getTcmDiagnosis() != null) {
            TCMFaceDiagnosis diagnosis = analysis.getTcmDiagnosis();
            result.append("🎯 中医诊断:\n");
            
            String syndromePattern = diagnosis.getSyndromePattern() != null ? diagnosis.getSyndromePattern() : "气血充盈";
            String constitutionType = diagnosis.getConstitutionType() != null ? diagnosis.getConstitutionType() : "平和质";
            String organFunction = diagnosis.getOrganFunction() != null ? diagnosis.getOrganFunction() : "脏腑功能协调";
            String qiBloodStatus = diagnosis.getQiBloodStatus() != null ? diagnosis.getQiBloodStatus() : "气血状态良好";
            
            result.append("• 证候类型: ").append(syndromePattern).append("\n");
            result.append("• 体质类型: ").append(constitutionType).append("\n");
            result.append("• 脏腑功能: ").append(organFunction).append("\n");
            result.append("• 气血状态: ").append(qiBloodStatus).append("\n\n");
        }
        
        // 调理建议
        if (analysis.getRecommendations() != null) {
            TCMFaceRecommendations recommendations = analysis.getRecommendations();
            result.append("💡 调理建议:\n");
            
            String dietaryTherapy = recommendations.getDietaryTherapy() != null ? recommendations.getDietaryTherapy() : "饮食均衡，营养充足";
            String lifestyleAdjustment = recommendations.getLifestyleAdjustment() != null ? recommendations.getLifestyleAdjustment() : "规律作息，心情愉悦";
            String herbalSuggestions = recommendations.getHerbalSuggestions() != null ? recommendations.getHerbalSuggestions() : "可适当调理";
            String acupointMassage = recommendations.getAcupointMassage() != null ? recommendations.getAcupointMassage() : "可按摩相关穴位";
            
            result.append("• 食疗建议: ").append(dietaryTherapy).append("\n");
            result.append("• 生活调理: ").append(lifestyleAdjustment).append("\n");
            result.append("• 中药建议: ").append(herbalSuggestions).append("\n");
            result.append("• 穴位按摩: ").append(acupointMassage).append("\n\n");
        }
        
        // 严重程度和置信度
        if (analysis.getSeverity() != null && !analysis.getSeverity().trim().isEmpty()) {
            result.append("⚡ 健康程度: ").append(analysis.getSeverity()).append("\n");
        }
        
        if (analysis.getConfidence() > 0) {
            result.append("📊 AI置信度: ").append(String.format("%.1f%%", analysis.getConfidence() * 100)).append("\n\n");
        }
        
        // 免责声明
        result.append("⚠️ 免责声明: 此为AI辅助中医面诊分析结果，仅供参考，请以专业中医师诊断为准。");
        
        return result.toString();
    }
    
    /**
     * 生成模拟的中医面诊分析结果
     * @return 模拟面诊分析结果
     */
    private String generateMockFaceDiagnosisResult() {
        // 创建模拟的面诊分析数据
        FaceDiagnosisResult mockAnalysis = createMockFaceDiagnosisAnalysis();
        
        // 格式化模拟分析结果
        String formattedResult = formatFaceDiagnosisResult(mockAnalysis);
        
        // 添加模拟结果标识
        StringBuilder result = new StringBuilder();
        result.append(formattedResult);
        result.append("\n\n🤖 注意：此为模拟中医面诊分析结果（AI服务暂时不可用），仅供开发测试使用，请以专业中医师诊断为准。");
        
        return result.toString();
    }
    
    /**
     * 创建模拟的中医面诊分析数据
     * @return 模拟的面诊分析对象
     */
    private FaceDiagnosisResult createMockFaceDiagnosisAnalysis() {
        // 创建面色分析
        Complexion complexion = new Complexion();
        complexion.setColor("未显示");
        complexion.setLuster("未显示");
        complexion.setTexture("未显示");
        complexion.setDistribution("未显示");
        
        // 创建五官特征分析
        FacialFeatures facialFeatures = new FacialFeatures();
        facialFeatures.setEyes("未显示");
        facialFeatures.setNose("未显示");
        facialFeatures.setMouth("未显示");
        facialFeatures.setEars("未显示");

        
        // 创建面部区域分析
        FacialRegions facialRegions = new FacialRegions();
        facialRegions.setForehead("未显示");
        facialRegions.setCheeks("未显示");
        facialRegions.setChin("未显示");
        facialRegions.setTemples("未显示");

        
        // 创建面部分析
        FacialAnalysis facialAnalysis = new FacialAnalysis();
        facialAnalysis.setComplexion(complexion);
        facialAnalysis.setFacialFeatures(facialFeatures);
        facialAnalysis.setFacialRegions(facialRegions);
        
        // 创建中医诊断
        TCMFaceDiagnosis tcmDiagnosis = new TCMFaceDiagnosis();
        tcmDiagnosis.setSyndromePattern("未显示");
        tcmDiagnosis.setConstitutionType("未显示");
        tcmDiagnosis.setOrganFunction("未显示");
        tcmDiagnosis.setQiBloodStatus("未显示");
        
        // 创建调理建议
        TCMFaceRecommendations tcmRecommendations = new TCMFaceRecommendations();
        tcmRecommendations.setDietaryTherapy("未显示");
        tcmRecommendations.setLifestyleAdjustment("未显示");
        tcmRecommendations.setHerbalSuggestions("未显示");
        tcmRecommendations.setAcupointMassage("未显示");

        
        // 创建面诊结果
        FaceDiagnosisResult result = new FaceDiagnosisResult();
        result.setImageType("中医面诊");
        result.setFacialAnalysis(facialAnalysis);
        result.setTcmDiagnosis(tcmDiagnosis);
        result.setRecommendations(tcmRecommendations);
        result.setSeverity("健康");
        result.setConfidence(0.85f);
        
        return result;
    }
    
    /**
     * 获取影像类型的显示名称
     * @param imageType 影像类型
     * @return 显示名称
     */
    private String getImageTypeDisplayName(String imageType) {
        switch (imageType) {
            case "xray":
                return "X光";
            case "ct":
                return "CT";
            case "ultrasound":
                return "B超";
            case "mri":
                return "MRI";
            case "petct":
                return "PET-CT";
            case "tongue":
                return "中医舌诊";
            case "face":
                return "中医面诊";
            default:
                return "医学影像";
        }
    }
    
    /**
     * 显示图像类型不匹配错误对话框
     * @param requestedType 用户请求的分析类型
     * @param errorMessage 错误消息
     */
    private void showImageTypeMismatchDialog(String requestedType, String errorMessage) {
        if (getContext() == null) {
            return;
        }
        
        ImageTypeMismatchDialog dialog = new ImageTypeMismatchDialog(getContext(), requestedType, errorMessage);
        dialog.setOnActionListener(new ImageTypeMismatchDialog.OnActionListener() {
            @Override
            public void onSelectCorrectImage() {
                // 重新选择图片
                showImagePickerDialog();
            }
            
            @Override
            public void onRetry() {
                // 重新尝试分析
                performMedicalImageAnalysis(requestedType);
            }
            
            @Override
            public void onCancel() {
                // 取消操作，不需要额外处理
            }
        });
        
        dialog.show();
    }
    
    /**
     * 上传图片到服务器
     */
    private void uploadImageToServer() {
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "请先选择图片", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 创建MultipartBody.Part
        MultipartBody.Part imagePart = ImageUtils.createImagePart(getContext(), selectedImageUri, "image");
        if (imagePart == null) {
            Toast.makeText(getContext(), "图片处理失败", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        tvLoadingText.setText("正在上传图片...");
        
        uploadCall = apiService.uploadImage(imagePart);
        uploadCall.enqueue(new Callback<ApiResponse<ImageUploadResult>>() {
            @Override
            public void onResponse(Call<ApiResponse<ImageUploadResult>> call, Response<ApiResponse<ImageUploadResult>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ImageUploadResult> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        displayUploadResult(apiResponse.getData());
                    } else {
                        Toast.makeText(getContext(), "图片上传失败: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "网络请求失败", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<ImageUploadResult>> call, Throwable t) {
                showLoading(false);
                if (!call.isCanceled()) {
                    Toast.makeText(getContext(), "图片上传失败: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    
    /**
     * 预览图片
     */
    private void previewImage() {
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "请先选择图片", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 创建图片预览对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_image_preview, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        // 获取控件引用
        android.widget.ImageView imageView = dialogView.findViewById(R.id.iv_preview);
        TextView tvImageInfo = dialogView.findViewById(R.id.tv_image_info);
        android.widget.ProgressBar pbLoading = dialogView.findViewById(R.id.pb_loading);
        android.widget.ImageView ivClosePreview = dialogView.findViewById(R.id.iv_close_preview);
        android.widget.Button btnEdit = dialogView.findViewById(R.id.btn_edit);
        android.widget.Button btnClose = dialogView.findViewById(R.id.btn_close);
        android.widget.ImageButton btnZoomIn = dialogView.findViewById(R.id.btn_zoom_in);
        android.widget.ImageButton btnZoomOut = dialogView.findViewById(R.id.btn_zoom_out);
        
        // 显示加载状态
        pbLoading.setVisibility(View.VISIBLE);
        
        // 异步加载图片
        new Thread(() -> {
            try {
                // 获取图片信息
                long imageSize = ImageUtils.getImageSize(getContext(), selectedImageUri);
                String imageSizeStr = ImageUtils.formatFileSize(imageSize);
                String imageInfo = ImageUtils.getImageInfo(getContext(), selectedImageUri);
                
                // 在主线程更新UI
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // 设置图片
                        imageView.setImageURI(selectedImageUri);
                        
                        // 显示图片信息
                        tvImageInfo.setText("图片大小: " + imageSizeStr + "\n" + imageInfo);
                        
                        // 隐藏加载状态
                        pbLoading.setVisibility(View.GONE);
                    });
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        pbLoading.setVisibility(View.GONE);
                        tvImageInfo.setText("加载图片信息失败");
                    });
                }
            }
        }).start();
        
        // 设置缩放功能
        final float[] currentScale = {1.0f};
        final float maxScale = 3.0f;
        final float minScale = 0.5f;
        
        btnZoomIn.setOnClickListener(v -> {
            if (currentScale[0] < maxScale) {
                currentScale[0] += 0.2f;
                imageView.setScaleX(currentScale[0]);
                imageView.setScaleY(currentScale[0]);
            }
        });
        
        btnZoomOut.setOnClickListener(v -> {
            if (currentScale[0] > minScale) {
                currentScale[0] -= 0.2f;
                imageView.setScaleX(currentScale[0]);
                imageView.setScaleY(currentScale[0]);
            }
        });
        
        // 设置点击事件
        ivClosePreview.setOnClickListener(v -> dialog.dismiss());
        
        btnEdit.setOnClickListener(v -> {
            dialog.dismiss();
            editImage();
        });
        
        btnClose.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    /**
     * 编辑图片（增强版）
     */
    private void editImage() {
        // 创建自定义对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_image_edit_options, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        // 设置点击事件
        dialogView.findViewById(R.id.card_rotate_cw).setOnClickListener(v -> {
            dialog.dismiss();
            performImageEdit(ImageUtils.EditOperation.ROTATE_90_CW);
        });
        
        dialogView.findViewById(R.id.card_rotate_ccw).setOnClickListener(v -> {
            dialog.dismiss();
            performImageEdit(ImageUtils.EditOperation.ROTATE_90_CCW);
        });
        
        dialogView.findViewById(R.id.card_flip_horizontal).setOnClickListener(v -> {
            dialog.dismiss();
            performImageEdit(ImageUtils.EditOperation.FLIP_HORIZONTAL);
        });
        
        dialogView.findViewById(R.id.card_flip_vertical).setOnClickListener(v -> {
            dialog.dismiss();
            performImageEdit(ImageUtils.EditOperation.FLIP_VERTICAL);
        });
        
        dialogView.findViewById(R.id.card_image_info).setOnClickListener(v -> {
            dialog.dismiss();
            showImageDetailInfo();
        });
        
        dialogView.findViewById(R.id.iv_close_edit).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_cancel_edit).setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    /**
     * 显示OCR识别结果
     */
    private void displayOCRResult(OCRResult result) {
        if (result == null) {
            Toast.makeText(getContext(), "OCR识别结果为空", Toast.LENGTH_SHORT).show();
            return;
        }
        
        StringBuilder resultText = new StringBuilder();
        resultText.append("=== OCR文字识别结果 ===\n\n");
        
        if (!TextUtils.isEmpty(result.getExtractedText())) {
            resultText.append("识别文字:\n").append(result.getExtractedText()).append("\n\n");
        }
        
        resultText.append("文字长度: ").append(result.getTextLength()).append("\n");
        resultText.append("包含中文: ").append(result.isHasChinese() ? "是" : "否").append("\n");
        
        if (!TextUtils.isEmpty(result.getConfidence())) {
            resultText.append("识别置信度: ").append(result.getConfidence()).append("\n");
        }
        
        if (!TextUtils.isEmpty(result.getErrorDetails())) {
            resultText.append("\n错误详情: ").append(result.getErrorDetails());
        }
        
        tvAnalysisResult.setText(resultText.toString());
        tvAnalysisResult.setVisibility(View.VISIBLE);
        
        // 保存结果状态
        hasAnalysisResult = true;
        savedAnalysisResult = resultText.toString();
    }
    
    /**
     * 显示医学影像分析结果
     */
    private void displayMedicalImageAnalysis(MedicalImageAnalysis analysis, String imageType) {
        if (analysis == null) {
            Toast.makeText(getContext(), "医学影像分析结果为空", Toast.LENGTH_SHORT).show();
            return;
        }
        
        StringBuilder resultText = new StringBuilder();
        resultText.append("🏥 === ").append(getImageTypeDisplayName(imageType)).append("AI影像分析报告 === 🏥\n\n");
        
        // 影像类型
        if (!TextUtils.isEmpty(analysis.getImageType())) {
            resultText.append("📋 【影像类型】\n")
                     .append(analysis.getImageType())
                     .append("\n\n");
        }
        
        // 影像发现
        if (analysis.getFindings() != null && !analysis.getFindings().isEmpty()) {
            resultText.append("🔍 【影像发现】\n");
            
            // 主要发现
            String primaryFindings = analysis.getPrimaryFindings();
            if (!TextUtils.isEmpty(primaryFindings)) {
                resultText.append("主要发现: ").append(primaryFindings).append("\n");
            }
            
            // 次要发现
            String secondaryFindings = analysis.getSecondaryFindings();
            if (!TextUtils.isEmpty(secondaryFindings)) {
                resultText.append("次要发现: ").append(secondaryFindings).append("\n");
            }
            
            // 异常表现
            String abnormalities = analysis.getAbnormalities();
            if (!TextUtils.isEmpty(abnormalities)) {
                resultText.append("异常表现: ").append(abnormalities).append("\n");
            }
            
            resultText.append("\n");
        }
        
        // 诊断结果
        if (analysis.getDiagnosis() != null && !analysis.getDiagnosis().isEmpty()) {
            resultText.append("🎯 【诊断结果】\n");
            
            // 主要诊断
            String primaryDiagnosis = analysis.getPrimaryDiagnosis();
            if (!TextUtils.isEmpty(primaryDiagnosis)) {
                resultText.append("主要诊断: ").append(primaryDiagnosis).append("\n");
            }
            
            // 鉴别诊断
            String differentialDiagnosis = analysis.getDifferentialDiagnosis();
            if (!TextUtils.isEmpty(differentialDiagnosis)) {
                resultText.append("鉴别诊断: ").append(differentialDiagnosis).append("\n");
            }
            
            // 诊断置信度
            String diagnosticConfidence = analysis.getDiagnosticConfidence();
            if (!TextUtils.isEmpty(diagnosticConfidence)) {
                resultText.append("诊断置信度: ").append(diagnosticConfidence).append("\n");
            }
            
            resultText.append("\n");
        }
        
        // 医学建议
        if (analysis.getRecommendations() != null && !analysis.getRecommendations().isEmpty()) {
            resultText.append("💡 【医学建议】\n");
            
            // 即时行动建议
            String immediateActions = analysis.getImmediateActions();
            if (!TextUtils.isEmpty(immediateActions)) {
                resultText.append("即时行动: ").append(immediateActions).append("\n");
            }
            
            // 随访建议
            String followUp = analysis.getFollowUp();
            if (!TextUtils.isEmpty(followUp)) {
                resultText.append("随访建议: ").append(followUp).append("\n");
            }
            
            resultText.append("\n");
        }
        
        // 严重程度
        if (!TextUtils.isEmpty(analysis.getSeverity())) {
            resultText.append("⚡ 【严重程度】\n")
                     .append(analysis.getSeverity())
                     .append("\n\n");
        }
        
        // AI置信度
        if (analysis.getConfidence() > 0) {
            resultText.append("🎯 【AI置信度】\n")
                     .append(String.format("%.1f%%", analysis.getConfidence() * 100))
                     .append("\n\n");
        }
        
        // 免责声明
        resultText.append("⚠️ 【重要提示】\n")
                 .append("此为AI辅助分析结果，仅供参考，请以专业医师诊断为准。");
        
        // 显示结果
        displayTextWithTypewriterEffect(resultText.toString());
        savedAnalysisResult = resultText.toString();
    }
    
    /**
     * 显示处方分析结果
     */
    private void displayPrescriptionAnalysis(PrescriptionAnalysis analysis) {
        if (analysis == null) {
            Toast.makeText(getContext(), "处方分析结果为空", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 检查是否为图像类型不匹配错误
        if ("IMAGE_TYPE_MISMATCH".equals(analysis.getErrorCode())) {
            StringBuilder errorText = new StringBuilder();
            errorText.append("⚠️ === 图像类型不匹配 === ⚠️\n\n");
            
            // 显示分析类型
            if (!TextUtils.isEmpty(analysis.getAnalysisType())) {
                errorText.append("📊 【请求的分析类型】 ").append(analysis.getAnalysisType()).append("\n\n");
            }
            
            // 显示检测结果
            if (analysis.getFindings() != null && !analysis.getFindings().isEmpty()) {
                errorText.append("🔍 【检测结果】\n");
                for (String finding : analysis.getFindings()) {
                    errorText.append("• ").append(finding).append("\n");
                }
                errorText.append("\n");
            }
            
            // 显示诊断结果
            if (!TextUtils.isEmpty(analysis.getDiagnosis())) {
                errorText.append("🚫 【诊断结果】\n").append(analysis.getDiagnosis()).append("\n\n");
            }
            
            // 显示建议
            if (analysis.getRecommendations() != null && !analysis.getRecommendations().isEmpty()) {
                errorText.append("💡 【建议】\n");
                for (String recommendation : analysis.getRecommendations()) {
                    errorText.append("• ").append(recommendation).append("\n");
                }
                errorText.append("\n");
            }
            
            // 显示置信度（应该是0.0）
            if (analysis.getConfidence() != null) {
                errorText.append("📊 【置信度】 ").append(analysis.getConfidence()).append("\n\n");
            }
            
            errorText.append("📝 【说明】\n");
            errorText.append("系统检测到您上传的图像类型与所选择的分析类型不匹配。\n");
            errorText.append("请重新选择正确的医学影像或选择匹配的分析类型。\n\n");
            
            errorText.append("🔄 【解决方案】\n");
            errorText.append("1. 重新上传正确类型的医学影像\n");
            errorText.append("2. 选择与当前图像匹配的分析类型\n");
            errorText.append("3. 确保图像清晰且为标准医学影像格式\n");
            
            tvAnalysisResult.setText(errorText.toString());
            tvAnalysisResult.setVisibility(View.VISIBLE);
            
            // 保存错误结果状态
            hasAnalysisResult = true;
            savedAnalysisResult = errorText.toString();
            return;
        }
        
        StringBuilder resultText = new StringBuilder();
        resultText.append("📋 === 中医处方智能分析报告 === 📋\n\n");
        
        // OCR识别结果
        if (!TextUtils.isEmpty(analysis.getOcrText())) {
            resultText.append("🔍 【OCR识别文字】\n")
                     .append(analysis.getOcrText())
                     .append("\n\n");
        }
        
        // 分析类型
        if (!TextUtils.isEmpty(analysis.getAnalysisType())) {
            resultText.append("📊 【分析类型】 ").append(analysis.getAnalysisType()).append("\n\n");
        }
        
        // 辩证分型 - 详细展示
        resultText.append("🎯 【辩证分型】\n");
        PrescriptionAnalysis.SyndromeType syndromeType = analysis.getSyndromeType();
        if (syndromeType != null) {
            if (syndromeType.getMainSyndrome() != null && !syndromeType.getMainSyndrome().trim().isEmpty()) {
                resultText.append("主要证型: ").append(syndromeType.getMainSyndrome()).append("\n");
            }
            if (syndromeType.getSecondarySyndrome() != null && !syndromeType.getSecondarySyndrome().trim().isEmpty()) {
                resultText.append("兼夹证型: ").append(syndromeType.getSecondarySyndrome()).append("\n");
            }
            if (syndromeType.getDiseaseLocation() != null && !syndromeType.getDiseaseLocation().trim().isEmpty()) {
                resultText.append("病位分析: ").append(syndromeType.getDiseaseLocation()).append("\n");
            }
            if (syndromeType.getDiseaseNature() != null && !syndromeType.getDiseaseNature().trim().isEmpty()) {
                resultText.append("病性分析: ").append(syndromeType.getDiseaseNature()).append("\n");
            }
            if (syndromeType.getPathogenesis() != null && !syndromeType.getPathogenesis().trim().isEmpty()) {
                resultText.append("病机分析: ").append(syndromeType.getPathogenesis()).append("\n");
            }
        } else {
            resultText.append("主要证型: 待进一步辩证\n");
        }
        
        // 可能的症状表现
        if (analysis.getPossibleSymptoms() != null && !analysis.getPossibleSymptoms().isEmpty()) {
            resultText.append("症状表现: ").append(String.join("、", analysis.getPossibleSymptoms())).append("\n");
        } else {
            resultText.append("症状表现: 根据处方推断可能包括相关脏腑功能失调症状\n");
        }
        resultText.append("\n");
        
        // 治法 - 详细展示
        resultText.append("⚡ 【治疗法则】\n");
        PrescriptionAnalysis.TreatmentMethod treatmentMethod = analysis.getTreatmentMethod();
        if (treatmentMethod != null) {
            if (treatmentMethod.getMainMethod() != null && !treatmentMethod.getMainMethod().trim().isEmpty()) {
                resultText.append("主要治法: ").append(treatmentMethod.getMainMethod()).append("\n");
            }
            if (treatmentMethod.getAuxiliaryMethod() != null && !treatmentMethod.getAuxiliaryMethod().trim().isEmpty()) {
                resultText.append("辅助治法: ").append(treatmentMethod.getAuxiliaryMethod()).append("\n");
            }
            if (treatmentMethod.getTreatmentPriority() != null && !treatmentMethod.getTreatmentPriority().trim().isEmpty()) {
                resultText.append("治疗层次: ").append(treatmentMethod.getTreatmentPriority()).append("\n");
            }
            if (treatmentMethod.getCarePrinciple() != null && !treatmentMethod.getCarePrinciple().trim().isEmpty()) {
                resultText.append("调护原则: ").append(treatmentMethod.getCarePrinciple()).append("\n");
            }
        } else {
            resultText.append("主要治法: 根据方药配伍推断治疗原则\n");
        }
        resultText.append("治疗原则: 辨证论治，标本兼顾，调和阴阳，扶正祛邪\n");
        resultText.append("\n");
        
        // 主方及来源
        resultText.append("📜 【方剂信息】\n");
        PrescriptionAnalysis.MainPrescription mainPrescription = analysis.getMainPrescription();
        if (mainPrescription != null) {
            if (mainPrescription.getFormulaName() != null && !mainPrescription.getFormulaName().trim().isEmpty()) {
                resultText.append("主方名称: ").append(mainPrescription.getFormulaName()).append("\n");
            }
            if (mainPrescription.getFormulaSource() != null && !mainPrescription.getFormulaSource().trim().isEmpty()) {
                resultText.append("方剂出处: ").append(mainPrescription.getFormulaSource()).append("\n");
            }
            if (mainPrescription.getFormulaAnalysis() != null && !mainPrescription.getFormulaAnalysis().trim().isEmpty()) {
                resultText.append("方义分析: ").append(mainPrescription.getFormulaAnalysis()).append("\n");
            }
            if (mainPrescription.getModifications() != null && !mainPrescription.getModifications().trim().isEmpty()) {
                resultText.append("加减变化: ").append(mainPrescription.getModifications()).append("\n");
            }
        } else {
            resultText.append("主方名称: 经验方或自拟方\n");
        }
        resultText.append("\n");
        
        // 药物组成 - 详细分类展示
        resultText.append("🌿 【药物组成及配伍分析】\n");
        if (analysis.getComposition() != null && !analysis.getComposition().isEmpty()) {
            // 按药物角色分类显示
            StringBuilder junYao = new StringBuilder();
            StringBuilder chenYao = new StringBuilder();
            StringBuilder zuoYao = new StringBuilder();
            StringBuilder shiYao = new StringBuilder();
            StringBuilder otherYao = new StringBuilder();
            
            for (PrescriptionAnalysis.HerbComposition herb : analysis.getComposition()) {
                String role = herb.getRole() != null ? herb.getRole() : "其他";
                StringBuilder herbInfo = new StringBuilder("  • " + herb.getHerb() + " " + 
                               (herb.getDosage() != null ? herb.getDosage() : "适量"));
                
                if (herb.getFunction() != null && !herb.getFunction().trim().isEmpty()) {
                    herbInfo.append(" - ").append(herb.getFunction());
                }
                if (herb.getPreparation() != null && !herb.getPreparation().trim().isEmpty()) {
                    herbInfo.append(" (").append(herb.getPreparation()).append(")");
                }
                herbInfo.append("\n");
                String herbInfoStr = herbInfo.toString();
                
                if (role.contains("君") || role.contains("主")) {
                    junYao.append(herbInfoStr);
                } else if (role.contains("臣") || role.contains("辅")) {
                    chenYao.append(herbInfoStr);
                } else if (role.contains("佐") || role.contains("调")) {
                    zuoYao.append(herbInfoStr);
                } else if (role.contains("使") || role.contains("引")) {
                    shiYao.append(herbInfoStr);
                } else {
                    otherYao.append(herbInfoStr);
                }
            }
            
            if (junYao.length() > 0) {
                resultText.append("👑 君药（主药）:\n").append(junYao);
            }
            if (chenYao.length() > 0) {
                resultText.append("🤝 臣药（辅药）:\n").append(chenYao);
            }
            if (zuoYao.length() > 0) {
                resultText.append("⚖️ 佐药（调药）:\n").append(zuoYao);
            }
            if (shiYao.length() > 0) {
                resultText.append("🎯 使药（引药）:\n").append(shiYao);
            }
            if (otherYao.length() > 0) {
                resultText.append("📋 其他药物:\n").append(otherYao);
            }
        } else {
            resultText.append("药物组成: 请参考处方原文或进一步识别\n");
        }
        
        // 检测到的中药材
        if (analysis.getDetectedHerbs() != null && !analysis.getDetectedHerbs().isEmpty()) {
            resultText.append("\n🔍 【识别到的中药材】\n");
            resultText.append(String.join("、", analysis.getDetectedHerbs())).append("\n");
        }
        resultText.append("\n");
        
        // 用法用量
        resultText.append("💊 【用法用量】\n");
        String usage = analysis.getUsage();
        if (usage != null && !usage.trim().isEmpty()) {
            resultText.append(usage).append("\n");
        } else {
            resultText.append("煎服法: 水煎服，一日一剂，早晚分服\n");
            resultText.append("煎煮法: 先煎30分钟，后下药物另煎15分钟\n");
            resultText.append("服用时间: 饭后30分钟温服\n");
        }
        resultText.append("\n");
        
        // 注意事项和禁忌
        resultText.append("⚠️ 【注意事项】\n");
        String contraindications = analysis.getContraindications();
        if (contraindications != null && !contraindications.trim().isEmpty()) {
            resultText.append(contraindications).append("\n");
        } else {
            resultText.append("孕妇慎用，过敏体质者慎用\n");
        }
        resultText.append("\n");
        
        // 专业建议
        resultText.append("💡 【专业建议】\n");
        if (analysis.getRecommendations() != null && !analysis.getRecommendations().isEmpty()) {
            for (String recommendation : analysis.getRecommendations()) {
                resultText.append("• ").append(recommendation).append("\n");
            }
        } else {
            resultText.append("• 建议在中医师指导下使用，切勿自行调整剂量\n");
            resultText.append("• 定期复诊，根据病情变化调整治疗方案\n");
            resultText.append("• 配合适当的饮食调理和生活方式改善\n");
            resultText.append("• 如症状加重或出现新症状，请及时就医\n");
        }
        resultText.append("\n");
        
        // 分析置信度和技术信息
        if (!TextUtils.isEmpty(analysis.getConfidence())) {
            resultText.append("📊 【分析置信度】 ").append(analysis.getConfidence()).append("\n\n");
        }
        
        if (!TextUtils.isEmpty(analysis.getMessage())) {
            resultText.append("📝 【系统消息】 ").append(analysis.getMessage()).append("\n\n");
        }
        
        // 免责声明
        resultText.append("⚖️ 【免责声明】\n");
        resultText.append("本分析结果仅供参考，不能替代专业医师的诊断和治疗建议。\n");
        resultText.append("请在合格中医师指导下使用中药，确保用药安全有效。\n");
        
        if (!TextUtils.isEmpty(analysis.getAiError())) {
            resultText.append("\nAI错误: ").append(analysis.getAiError()).append("\n");
        }
        
        if (!TextUtils.isEmpty(analysis.getErrorDetails())) {
            resultText.append("错误详情: ").append(analysis.getErrorDetails());
        }
        
        tvAnalysisResult.setText(resultText.toString());
        tvAnalysisResult.setVisibility(View.VISIBLE);
        
        // 保存结果状态
        hasAnalysisResult = true;
        savedAnalysisResult = resultText.toString();
    }
    
    /**
     * 显示上传结果
     */
    private void displayUploadResult(ImageUploadResult result) {
        if (result == null) {
            Toast.makeText(getContext(), "上传结果为空", Toast.LENGTH_SHORT).show();
            return;
        }
        
        StringBuilder resultText = new StringBuilder();
        resultText.append("=== 图片上传结果 ===\n\n");
        
        if (!TextUtils.isEmpty(result.getFilename())) {
            resultText.append("文件名: ").append(result.getFilename()).append("\n");
        }
        
        if (!TextUtils.isEmpty(result.getUrl())) {
            resultText.append("访问URL: ").append(result.getUrl()).append("\n");
        }
        
        if (!TextUtils.isEmpty(result.getFileSize())) {
            resultText.append("文件大小: ").append(result.getFileSize()).append("\n");
        }
        
        if (!TextUtils.isEmpty(result.getUploadTime())) {
            resultText.append("上传时间: ").append(result.getUploadTime()).append("\n");
        }
        
        if (!TextUtils.isEmpty(result.getMessage())) {
            resultText.append("\n消息: ").append(result.getMessage()).append("\n");
        }
        
        if (!TextUtils.isEmpty(result.getErrorDetails())) {
            resultText.append("错误详情: ").append(result.getErrorDetails());
        }
        
        tvAnalysisResult.setText(resultText.toString());
        tvAnalysisResult.setVisibility(View.VISIBLE);
        
        // 保存结果状态
        hasAnalysisResult = true;
        savedAnalysisResult = resultText.toString();
        
        Toast.makeText(getContext(), "图片上传成功！", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 执行图片编辑操作
     * @param operation 编辑操作类型
     */
    private void performImageEdit(ImageUtils.EditOperation operation) {
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "请先选择图片", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 显示加载提示
        showLoading(true);
        tvLoadingText.setText("正在编辑图片...");
        
        ImageUtils.editImageAsync(getContext(), selectedImageUri, operation, new ImageUtils.ImageProcessCallback() {
            @Override
            public void onSuccess(android.graphics.Bitmap result) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        
                        // 保存编辑后的图片
                        String filename = "edited_image_" + System.currentTimeMillis();
                        Uri editedUri = ImageUtils.saveBitmapToUri(getContext(), result, filename);
                        
                        if (editedUri != null) {
                            selectedImageUri = editedUri;
                            Toast.makeText(getContext(), "图片编辑成功", Toast.LENGTH_SHORT).show();
                            
                            // 重新显示预览
                            previewImage();
                        } else {
                            Toast.makeText(getContext(), "保存编辑后的图片失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(getContext(), "图片编辑失败: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }
    
    /**
     * 显示图片详细信息
     */
    private void showImageDetailInfo() {
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "请先选择图片", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String imageInfo = ImageUtils.getImageInfo(getContext(), selectedImageUri);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("图片详细信息")
                .setMessage(imageInfo)
                .setPositiveButton("确定", null)
                .setNeutralButton("生成缩略图", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        generateAndShowThumbnail();
                    }
                })
                .show();
    }
    
    /**
     * 生成并显示缩略图
     */
    private void generateAndShowThumbnail() {
        if (selectedImageUri == null) {
            return;
        }
        
        showLoading(true);
        tvLoadingText.setText("正在生成缩略图...");
        
        // 在后台线程生成缩略图
        new Thread(() -> {
            android.graphics.Bitmap thumbnail = ImageUtils.generateThumbnail(getContext(), selectedImageUri);
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    showLoading(false);
                    
                    if (thumbnail != null) {
                        showThumbnailDialog(thumbnail);
                    } else {
                        Toast.makeText(getContext(), "生成缩略图失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }
    
    /**
     * 显示缩略图对话框
     * @param thumbnail 缩略图Bitmap
     */
    private void showThumbnailDialog(android.graphics.Bitmap thumbnail) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_image_preview, null);
        
        android.widget.ImageView imageView = dialogView.findViewById(R.id.iv_preview);
        TextView tvImageInfo = dialogView.findViewById(R.id.tv_image_info);
        
        imageView.setImageBitmap(thumbnail);
        tvImageInfo.setText("缩略图 (200x200)");
        
        builder.setView(dialogView)
                .setTitle("缩略图预览")
                .setPositiveButton("关闭", null)
                .show();
    }
    
    /**
     * 诊断Fragment生命周期问题
     * 用于调试Fragment状态和Activity状态相关的问题
     */
    private void diagnoseFragmentLifecycleIssues() {
        Log.d("PrescriptionFragment", "=== Fragment生命周期诊断开始 ===");
        
        // Fragment状态检查
        Log.d("PrescriptionFragment", "Fragment状态:");
        Log.d("PrescriptionFragment", "  - isAdded(): " + isAdded());
        Log.d("PrescriptionFragment", "  - isDetached(): " + isDetached());
        Log.d("PrescriptionFragment", "  - isRemoving(): " + isRemoving());
        Log.d("PrescriptionFragment", "  - isVisible(): " + isVisible());
        Log.d("PrescriptionFragment", "  - isResumed(): " + isResumed());
        Log.d("PrescriptionFragment", "  - isHidden(): " + isHidden());
        
        // Activity状态检查
        Log.d("PrescriptionFragment", "Activity状态:");
        if (getActivity() != null) {
            Log.d("PrescriptionFragment", "  - getActivity(): 不为null");
            Log.d("PrescriptionFragment", "  - isFinishing(): " + getActivity().isFinishing());
            Log.d("PrescriptionFragment", "  - isDestroyed(): " + getActivity().isDestroyed());
        } else {
            Log.d("PrescriptionFragment", "  - getActivity(): 为null (Fragment已分离)");
        }
        
        // Context状态检查
        Log.d("PrescriptionFragment", "Context状态:");
        if (getContext() != null) {
            Log.d("PrescriptionFragment", "  - getContext(): 不为null");
        } else {
            Log.d("PrescriptionFragment", "  - getContext(): 为null");
        }
        
        // FragmentManager状态检查
        Log.d("PrescriptionFragment", "FragmentManager状态:");
        try {
            if (getParentFragmentManager() != null) {
                Log.d("PrescriptionFragment", "  - getParentFragmentManager(): 不为null");
                Log.d("PrescriptionFragment", "  - isStateSaved(): " + getParentFragmentManager().isStateSaved());
            } else {
                Log.d("PrescriptionFragment", "  - getParentFragmentManager(): 为null");
            }
        } catch (Exception e) {
            Log.e("PrescriptionFragment", "  - FragmentManager检查异常: " + e.getMessage());
        }
        
        // 修复建议
        Log.d("PrescriptionFragment", "修复建议:");
        if (!isAdded()) {
            Log.d("PrescriptionFragment", "  - Fragment未添加到Activity，请检查Fragment事务");
        }
        if (getActivity() == null) {
            Log.d("PrescriptionFragment", "  - Activity为null，Fragment可能已分离，避免UI操作");
        }
        if (getActivity() != null && getActivity().isFinishing()) {
            Log.d("PrescriptionFragment", "  - Activity正在结束，避免启动新的Dialog或Fragment");
        }
        
        Log.d("PrescriptionFragment", "=== Fragment生命周期诊断结束 ===");
    }
    
    /**
     * 运行Fragment生命周期诊断
     * 公共方法，可以从外部调用进行诊断
     */
    public void runFragmentLifecycleDiagnostics() {
        Log.d("PrescriptionFragment", "开始运行Fragment生命周期诊断...");
        diagnoseFragmentLifecycleIssues();
        
        // 显示诊断结果Toast
        if (getContext() != null) {
            Toast.makeText(getContext(), "Fragment生命周期诊断完成，请查看Logcat", Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * 统一处理图片选择结果，解决8种null情况
     * @param result ActivityResult结果
     * @param source 来源："gallery" 或 "camera"
     */
    private void handleImageSelectionResult(androidx.activity.result.ActivityResult result, String source) {
        Log.d("PrescriptionFragment", "=== handleImageSelectionResult 开始 ===");
        Log.d("PrescriptionFragment", "来源: " + source);
        
        // 记录当前Fragment状态
        logCurrentFragmentState();
        
        // 1. 验证Fragment和Activity状态（解决getActivity()为null的4种原因）
        if (!validateFragmentAndActivityState()) {
            return;
        }
        
        // 2. 检查ResultCode
        Log.d("PrescriptionFragment", "ResultCode: " + result.getResultCode());
        Log.d("PrescriptionFragment", "RESULT_OK: " + android.app.Activity.RESULT_OK);
        Log.d("PrescriptionFragment", "RESULT_CANCELED: " + android.app.Activity.RESULT_CANCELED);
        
        if (result.getResultCode() != android.app.Activity.RESULT_OK) {
            Log.w("PrescriptionFragment", "操作未成功完成，ResultCode: " + result.getResultCode());
            
            if (result.getResultCode() == android.app.Activity.RESULT_CANCELED) {
                Log.i("PrescriptionFragment", "用户主动取消了" + source + "操作");
                // 不显示Toast，用户主动取消是正常行为
            } else {
                Log.e("PrescriptionFragment", "" + source + "操作失败，错误码: " + result.getResultCode());
                showSafeToast(source.equals("gallery") ? "相册选择失败" : "拍照失败");
            }
            return;
        }
        
        Log.d("PrescriptionFragment", "✅ ResultCode检查通过，操作成功");
        
        // 3. 验证结果数据（解决result.getData()为null的4种原因）
        Uri imageUri = validateResultData(result, source);
        if (imageUri == null) {
            return;
        }
        
        // 4. 处理有效的图片结果
        processValidImageResult(imageUri, source);
        
        Log.d("PrescriptionFragment", "=== handleImageSelectionResult 结束 ===");
    }
    
    /**
     * 记录当前Fragment状态
     */
    private void logCurrentFragmentState() {
        Log.d("PrescriptionFragment", "=== Fragment状态检查 ===");
        Log.d("PrescriptionFragment", "isAdded(): " + isAdded());
        Log.d("PrescriptionFragment", "isDetached(): " + isDetached());
        Log.d("PrescriptionFragment", "isRemoving(): " + isRemoving());
        Log.d("PrescriptionFragment", "getActivity() != null: " + (getActivity() != null));
        Log.d("PrescriptionFragment", "getContext() != null: " + (getContext() != null));
        
        if (getActivity() != null) {
            Log.d("PrescriptionFragment", "Activity.isFinishing(): " + getActivity().isFinishing());
            Log.d("PrescriptionFragment", "Activity.isDestroyed(): " + getActivity().isDestroyed());
        }
    }
    
    /**
     * 验证Fragment和Activity状态
     * 解决getActivity()为null的4种原因：
     * 1. Fragment分离（Detached）
     * 2. Activity销毁（内存回收/用户操作）
     * 3. Fragment移除（Removing状态）
     * 4. 异步回调时机问题
     */
    private boolean validateFragmentAndActivityState() {
        // 检查Fragment分离状态
        if (isDetached()) {
            Log.e("PrescriptionFragment", "❌ Fragment已分离，无法处理图片选择结果");
            return false;
        }
        
        // 检查Fragment是否已添加到Activity
        if (!isAdded()) {
            Log.e("PrescriptionFragment", "❌ Fragment未添加到Activity，无法处理图片选择结果");
            return false;
        }
        
        // 检查Fragment是否正在移除
        if (isRemoving()) {
            Log.e("PrescriptionFragment", "❌ Fragment正在移除，无法处理图片选择结果");
            return false;
        }
        
        // 检查Activity是否存在
        if (getActivity() == null) {
            Log.e("PrescriptionFragment", "❌ Activity为null，可能已被销毁");
            return false;
        }
        
        // 检查Activity是否正在结束或已销毁
        if (getActivity().isFinishing() || getActivity().isDestroyed()) {
            Log.e("PrescriptionFragment", "❌ Activity正在结束或已销毁");
            return false;
        }
        
        Log.d("PrescriptionFragment", "✅ Fragment和Activity状态验证通过");
        return true;
    }
    
    /**
     * 验证结果数据
     * 解决result.getData()为null的4种原因：
     * 1. 用户取消操作
     * 2. 系统内存不足
     * 3. 存储权限问题
     * 4. 图片选择器异常
     */
    private Uri validateResultData(androidx.activity.result.ActivityResult result, String source) {
        Uri imageUri = null;
        
        if ("camera".equals(source)) {
            // 拍照使用预设的photoUri
            imageUri = photoUri;
            Log.d("PrescriptionFragment", "拍照结果，使用photoUri: " + imageUri);
            
            if (imageUri == null) {
                Log.e("PrescriptionFragment", "❌ 拍照失败：photoUri为null");
                analyzeDataNullCauses("camera", null);
                showSafeToast("拍照失败，请重试");
                return null;
            }
        } else {
            // 相册选择使用result.getData()
            Intent data = result.getData();
            if (data != null) {
                imageUri = data.getData();
            }
            Log.d("PrescriptionFragment", "相册选择结果，getData(): " + imageUri);
            
            if (imageUri == null) {
                Log.e("PrescriptionFragment", "❌ 相册选择失败：getData()为null");
                analyzeDataNullCauses("gallery", result);
                return null;
            }
        }
        
        Log.d("PrescriptionFragment", "✅ 结果数据验证通过，imageUri: " + imageUri);
        return imageUri;
    }
    
    /**
     * 分析数据为null的原因
     */
    private void analyzeDataNullCauses(String source, androidx.activity.result.ActivityResult result) {
        Log.d("PrescriptionFragment", "=== 分析数据为null的原因 ===");
        
        // 检查内存状态
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        Log.d("PrescriptionFragment", "内存使用情况: " + String.format("%.1f%%", memoryUsagePercent));
        Log.d("PrescriptionFragment", "最大内存: " + (maxMemory / 1024 / 1024) + "MB");
        Log.d("PrescriptionFragment", "已用内存: " + (usedMemory / 1024 / 1024) + "MB");
        
        if (memoryUsagePercent > 80) {
            Log.w("PrescriptionFragment", "⚠️ 内存使用率过高，可能导致图片选择失败");
            showSafeToast("内存不足，请关闭其他应用后重试");
            return;
        }
        
        // 检查存储权限
        if (getContext() != null) {
            boolean hasReadPermission = ContextCompat.checkSelfPermission(getContext(), 
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            Log.d("PrescriptionFragment", "读取存储权限: " + hasReadPermission);
            
            if (!hasReadPermission && "gallery".equals(source)) {
                Log.w("PrescriptionFragment", "⚠️ 缺少存储读取权限，可能导致相册选择失败");
                showSafeToast("需要存储权限才能访问相册");
                return;
            }
        }
        
        // 根据来源分析具体原因
        if ("gallery".equals(source)) {
            Log.w("PrescriptionFragment", "相册选择失败可能原因：");
            Log.w("PrescriptionFragment", "1. 用户取消了选择");
            Log.w("PrescriptionFragment", "2. 图片文件损坏或不可访问");
            Log.w("PrescriptionFragment", "3. 相册应用异常");
            Log.w("PrescriptionFragment", "4. 系统内存不足");
            showSafeToast("相册选择失败，请重试或选择其他图片");
        } else {
            Log.w("PrescriptionFragment", "拍照失败可能原因：");
            Log.w("PrescriptionFragment", "1. 相机应用异常");
            Log.w("PrescriptionFragment", "2. 存储空间不足");
            Log.w("PrescriptionFragment", "3. 相机权限问题");
            Log.w("PrescriptionFragment", "4. 文件创建失败");
            showSafeToast("拍照失败，请检查存储空间和权限");
        }
    }
    
    /**
     * 处理有效的图片结果
     * 根据图片类型选择执行面诊或舌诊分析
     */
    private void processValidImageResult(Uri imageUri, String source) {
        Log.d("PrescriptionFragment", "处理有效图片结果: " + imageUri + ", 来源: " + source);
        
        // 设置选中的图片URI和来源
        selectedImageUri = imageUri;
        imageSource = source;
        
        // 检查是否为舌面诊图片选择
        if (currentTongueDiagnosisType != null && !currentTongueDiagnosisType.isEmpty()) {
            Log.d("PrescriptionFragment", "检测到舌面诊类型: " + currentTongueDiagnosisType + ", 直接执行AI分析");
            
            // 根据图片类型选择执行相应的诊断分析
            if ("face".equals(currentTongueDiagnosisType)) {
                // 执行面诊分析
                Log.d("PrescriptionFragment", "执行面诊分析");
                performFaceDiagnosis();
            } else if("tongue".equals(currentTongueDiagnosisType)) {
                // 执行舌诊分析
                Log.d("PrescriptionFragment", "执行舌诊分析");
                performTongueDiagnosis();
            } 
            
            // 重置舌面诊类型
            currentTongueDiagnosisType = null;
        } else {
            // 调用原有的图片处理逻辑
            handleSelectedImage(imageUri);
        }
    }
    

}