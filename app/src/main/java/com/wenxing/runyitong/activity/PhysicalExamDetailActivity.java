package com.wenxing.runyitong.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.wenxing.runyitong.R;
import com.wenxing.runyitong.model.PhysicalExamReport;

/**
 * 体检报告详情Activity
 * 用于展示体检报告的详细信息
 */
public class PhysicalExamDetailActivity extends AppCompatActivity {
    
    private static final String TAG = "PhysicalExamDetailActivity";
    
    // UI组件
    private Toolbar toolbar;
    private TextView tvReportName, tvExamDate, tvHospitalName, tvSummary, tvDoctorOpinion;
    private TextView tvKeyFindings, tvNormalItems, tvAbnormalItems, tvRecommendations;
    private Button btnDelete, btnViewReport;
    
    // 数据相关
    private PhysicalExamReport examReport;
    private int reportPosition = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physical_exam_detail);
        
        initViews();
        initData();
        setupClickListeners();
        displayReportDetails();
    }
    
    /**
     * 初始化视图组件
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        
        // 基本信息组件
        tvReportName = findViewById(R.id.tv_report_name);
        tvExamDate = findViewById(R.id.tv_exam_date);
        tvHospitalName = findViewById(R.id.tv_hospital_name);
        
        // 报告内容组件
        tvSummary = findViewById(R.id.tv_summary);
        tvDoctorOpinion = findViewById(R.id.tv_doctor_opinion);
        tvKeyFindings = findViewById(R.id.tv_key_findings);
        tvNormalItems = findViewById(R.id.tv_normal_items);
        tvAbnormalItems = findViewById(R.id.tv_abnormal_items);
        tvRecommendations = findViewById(R.id.tv_recommendations);
        
        // 操作按钮
        btnDelete = findViewById(R.id.btn_delete);
        btnViewReport = findViewById(R.id.btn_view_report);
        
        // 设置工具栏
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("体检报告详情");
        }
    }
    
    /**
     * 初始化数据
     */
    private void initData() {
        // 获取从上一个页面传递过来的体检报告数据
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("physical_exam_report")) {
            examReport = (PhysicalExamReport) intent.getSerializableExtra("physical_exam_report");
        }
        
        // 获取报告位置信息，用于删除操作
        if (intent != null && intent.hasExtra("report_position")) {
            reportPosition = intent.getIntExtra("report_position", -1);
        }
    }
    
    /**
     * 设置点击事件监听器
     */
    private void setupClickListeners() {
        // 删除按钮点击事件
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeleteReport();
            }
        });
        
        // 查看报告按钮点击事件
        btnViewReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewReportDocument();
            }
        });
    }
    
    /**
     * 显示报告详情
     */
    private void displayReportDetails() {
        if (examReport == null) {
            Toast.makeText(this, "未找到体检报告数据", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 显示基本信息
        tvReportName.setText(examReport.getReportName() != null ? examReport.getReportName() : "未命名报告");
        
        // 显示检查日期
        tvExamDate.setText(examReport.getExamDate() != null ? examReport.getExamDate() : "未提供");
        
        // 显示医院名称
        tvHospitalName.setText(examReport.getHospitalName() != null ? examReport.getHospitalName() : "未提供");
        
        // 显示报告内容
        tvSummary.setText(examReport.getSummary() != null ? examReport.getSummary() : "暂无摘要信息");
        tvDoctorOpinion.setText(examReport.getDoctorComments() != null ? examReport.getDoctorComments() : "暂无医生意见");
        tvKeyFindings.setText(examReport.getKeyFindings() != null ? examReport.getKeyFindings().toString() : "暂无关键发现");
        tvNormalItems.setText(examReport.getNormalItems() != null ? examReport.getNormalItems().toString() : "暂无正常项目");
        tvAbnormalItems.setText(examReport.getAbnormalItems() != null ? examReport.getAbnormalItems().toString() : "暂无异常项目");
        tvRecommendations.setText(examReport.getRecommendations() != null ? examReport.getRecommendations() : "暂无建议");
        
        // 根据是否有报告URL决定是否显示查看报告按钮
        if (examReport.getReportUrl() == null || examReport.getReportUrl().isEmpty()) {
            btnViewReport.setVisibility(View.GONE);
        }
    }
    
    /**
     * 确认删除报告
     */
    private void confirmDeleteReport() {
        // 创建自定义风格的删除确认对话框
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomDialogTheme);
        androidx.appcompat.app.AlertDialog dialog = builder
                .setTitle("确认删除")
                .setMessage("确定要删除这份体检报告吗？此操作不可恢复。")
                .setPositiveButton("删除", (dialogInterface, which) -> {
                    deleteReport();
                })
                .setNegativeButton("取消", (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                })
                .create();
        
        // 设置对话框显示和消失的动画
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        
        // 美化按钮样式
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE);
            
            // 设置按钮文本颜色
            positiveButton.setTextColor(getResources().getColor(R.color.error_color));
            negativeButton.setTextColor(getResources().getColor(R.color.text_secondary));
            
            // 设置按钮的圆角和内边距
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    48
            );
            params.setMargins(16, 0, 16, 0);
            positiveButton.setLayoutParams(params);
            negativeButton.setLayoutParams(params);
        });
        
        dialog.show();
    }
    
    /**
     * 删除报告
     */
    private void deleteReport() {
        // 返回上一个页面，并传递删除的报告位置信息
        Intent intent = new Intent();
        intent.putExtra("delete_report", true);
        intent.putExtra("report_position", reportPosition);
        setResult(RESULT_OK, intent);
        finish();
    }
    
    /**
     * 查看报告文档
     */
    private void viewReportDocument() {
        // 跳转到ReportViewerActivity查看完整体检报告
        Intent intent = new Intent(this, ReportViewerActivity.class);
        intent.putExtra(ReportViewerActivity.EXTRA_PHYSICAL_EXAM, examReport);
        startActivity(intent);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}