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
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        handleSelectedImage(selectedImageUri);
                    }
                }
            }
        );
        
        // 拍照
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK) {
                    if (photoUri != null) {
                        handleSelectedImage(photoUri);
                    }
                }
            }
        );
        
        // 相机权限请求
        cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(getContext(), "需要相机权限才能拍照", Toast.LENGTH_SHORT).show();
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
            // Fragment变为可见时，恢复状态
            restoreState();
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
        // 恢复状态
        restoreState();
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
                        openGallery();
                        break;
                    case 1: // 拍照
                        checkCameraPermissionAndOpen();
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
    
    /**
     * 打开相册选择图片
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
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
                cameraLauncher.launch(takePictureIntent);
            }
        } else {
            Toast.makeText(getContext(), "没有找到相机应用", Toast.LENGTH_SHORT).show();
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
        if (imageUri == null) {
            Toast.makeText(getContext(), "图片选择失败", Toast.LENGTH_SHORT).show();
            return;
        }
        
        selectedImageUri = imageUri;
        
        // 显示图片处理选项对话框
        showImageProcessingDialog();
    }
    
    /**
     * 显示图片处理选项对话框
     */
    private void showImageProcessingDialog() {
        // 创建自定义对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_image_processing_options, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        // 设置点击事件
        dialogView.findViewById(R.id.card_ocr).setOnClickListener(v -> {
            dialog.dismiss();
            performOCRRecognition();
        });
        
        dialogView.findViewById(R.id.card_analysis).setOnClickListener(v -> {
            dialog.dismiss();
            performPrescriptionAnalysis();
        });
        
        dialogView.findViewById(R.id.card_upload).setOnClickListener(v -> {
            dialog.dismiss();
            uploadImageToServer();
        });
        
        dialogView.findViewById(R.id.card_preview).setOnClickListener(v -> {
            dialog.dismiss();
            previewImage();
        });
        
        dialogView.findViewById(R.id.iv_close).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
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
}