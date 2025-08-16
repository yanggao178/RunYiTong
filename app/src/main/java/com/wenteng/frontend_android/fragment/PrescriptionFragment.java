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
import com.wenteng.frontend_android.R;
import com.wenteng.frontend_android.api.ApiClient;
import com.wenteng.frontend_android.api.ApiResponse;
import com.wenteng.frontend_android.api.ApiService;
import com.wenteng.frontend_android.model.SymptomAnalysis;
import com.wenteng.frontend_android.model.OCRResult;
import com.wenteng.frontend_android.model.PrescriptionAnalysis;
import com.wenteng.frontend_android.model.ImageUploadResult;
import com.wenteng.frontend_android.utils.ImageUtils;
import com.wenteng.frontend_android.dialog.ImageProcessingDialogFragment;
import com.wenteng.frontend_android.dialog.ImagePickerDialogFragment;
import com.wenteng.frontend_android.dialog.TestDialogFragment;
import com.wenteng.frontend_android.dialog.CustomImageProcessingDialog;
import okhttp3.MultipartBody;
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
    
    // 图片处理相关
    private Call<ApiResponse<OCRResult>> ocrCall;
    private Call<ApiResponse<PrescriptionAnalysis>> analysisCall;
    private Call<ApiResponse<ImageUploadResult>> uploadCall;
    private Uri selectedImageUri;
    private String imageSource = "unknown"; // 记录图片来源："camera" 或 "gallery"
    
    // 图片选择相关
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private Uri photoUri;
    
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
        if (ocrCall != null && !ocrCall.isCanceled()) {
            ocrCall.cancel();
        }
        if (analysisCall != null && !analysisCall.isCanceled()) {
            analysisCall.cancel();
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
        
        String resultText = result.toString();
        tvAnalysisResult.setText(resultText);
        
        // 保存分析结果状态
        savedAnalysisResult = resultText;
        hasAnalysisResult = true;
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
                public void onOCRSelected() {
                    android.util.Log.d("PrescriptionFragment", "测试对话框 - OCR选项被选中");
                }
                
                @Override
                public void onAnalysisSelected() {
                    android.util.Log.d("PrescriptionFragment", "测试对话框 - 分析选项被选中");
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
                public void onOCRSelected() {
                    android.util.Log.d("PrescriptionFragment", "自定义对话框 - OCR识别被选择");
                    performOCRRecognition();
                }
                
                @Override
                public void onAnalysisSelected() {
                    android.util.Log.d("PrescriptionFragment", "自定义对话框 - 处方分析被选择");
                    performPrescriptionAnalysis();
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
                @Override public void onOCRSelected() { performOCRRecognition(); }
                @Override public void onAnalysisSelected() { performPrescriptionAnalysis(); }
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
        MultipartBody.Part imagePart = ImageUtils.createImagePart(getContext(), selectedImageUri, "file");
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
        MultipartBody.Part imagePart = ImageUtils.createImagePart(getContext(), selectedImageUri, "file");
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
     * 上传图片到服务器
     */
    private void uploadImageToServer() {
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "请先选择图片", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 创建MultipartBody.Part
        MultipartBody.Part imagePart = ImageUtils.createImagePart(getContext(), selectedImageUri, "file");
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
     * 显示处方分析结果
     */
    private void displayPrescriptionAnalysis(PrescriptionAnalysis analysis) {
        if (analysis == null) {
            Toast.makeText(getContext(), "处方分析结果为空", Toast.LENGTH_SHORT).show();
            return;
        }
        
        StringBuilder resultText = new StringBuilder();
        resultText.append("=== 处方智能分析结果 ===\n\n");
        
        if (!TextUtils.isEmpty(analysis.getOcrText())) {
            resultText.append("OCR识别文字:\n").append(analysis.getOcrText()).append("\n\n");
        }
        
        if (!TextUtils.isEmpty(analysis.getAnalysisType())) {
            resultText.append("分析类型: ").append(analysis.getAnalysisType()).append("\n");
        }
        
        if (!TextUtils.isEmpty(analysis.getSyndromeType())) {
            resultText.append("证型: ").append(analysis.getSyndromeType()).append("\n");
        }
        
        if (!TextUtils.isEmpty(analysis.getTreatmentMethod())) {
            resultText.append("治法: ").append(analysis.getTreatmentMethod()).append("\n");
        }
        
        if (!TextUtils.isEmpty(analysis.getMainPrescription())) {
            resultText.append("主方: ").append(analysis.getMainPrescription()).append("\n");
        }
        
        if (analysis.getComposition() != null && !analysis.getComposition().isEmpty()) {
            resultText.append("\n药物组成:\n");
            for (PrescriptionAnalysis.HerbComposition herb : analysis.getComposition()) {
                resultText.append("• ").append(herb.toString()).append("\n");
            }
        }
        
        if (!TextUtils.isEmpty(analysis.getUsage())) {
            resultText.append("\n用法: ").append(analysis.getUsage()).append("\n");
        }
        
        if (!TextUtils.isEmpty(analysis.getContraindications())) {
            resultText.append("禁忌: ").append(analysis.getContraindications()).append("\n");
        }
        
        if (analysis.getDetectedHerbs() != null && !analysis.getDetectedHerbs().isEmpty()) {
            resultText.append("\n检测到的中药: ").append(String.join(", ", analysis.getDetectedHerbs())).append("\n");
        }
        
        if (analysis.getPossibleSymptoms() != null && !analysis.getPossibleSymptoms().isEmpty()) {
            resultText.append("可能症状: ").append(String.join(", ", analysis.getPossibleSymptoms())).append("\n");
        }
        
        if (analysis.getRecommendations() != null && !analysis.getRecommendations().isEmpty()) {
            resultText.append("\n建议:\n");
            for (String recommendation : analysis.getRecommendations()) {
                resultText.append("• ").append(recommendation).append("\n");
            }
        }
        
        if (!TextUtils.isEmpty(analysis.getConfidence())) {
            resultText.append("\n分析置信度: ").append(analysis.getConfidence()).append("\n");
        }
        
        if (!TextUtils.isEmpty(analysis.getMessage())) {
            resultText.append("\n消息: ").append(analysis.getMessage()).append("\n");
        }
        
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
     */
    private void processValidImageResult(Uri imageUri, String source) {
        Log.d("PrescriptionFragment", "处理有效图片结果: " + imageUri + ", 来源: " + source);
        
        // 设置选中的图片URI和来源
        selectedImageUri = imageUri;
        imageSource = source;
        
        // 调用原有的图片处理逻辑
        handleSelectedImage(imageUri);
    }
    
    /**
     * 安全显示Toast，避免Fragment状态异常
     */
    private void showSafeToast(String message) {
        if (getContext() != null && isAdded() && !isDetached() && !isRemoving()) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        } else {
            Log.w("PrescriptionFragment", "无法显示Toast，Fragment状态异常: " + message);
        }
    }
}