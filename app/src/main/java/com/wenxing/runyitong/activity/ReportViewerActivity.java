package com.wenxing.runyitong.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.wenxing.runyitong.R;
import com.wenxing.runyitong.model.PhysicalExamReport;

public class ReportViewerActivity extends AppCompatActivity {

    private static final String TAG = "ReportViewerActivity";
    public static final String EXTRA_PHYSICAL_EXAM = "extra_physical_exam";
    
    private WebView webView;
    private PhysicalExamReport physicalExamReport;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_viewer);
        
        // 初始化Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("完整体检报告");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // 获取传入的体检报告数据
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_PHYSICAL_EXAM)) {
            physicalExamReport = (PhysicalExamReport) intent.getSerializableExtra(EXTRA_PHYSICAL_EXAM);
        }
        
        if (physicalExamReport == null) {
            Toast.makeText(this, "无法加载报告数据", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // 初始化WebView
        webView = findViewById(R.id.web_view);
        initWebView();
        
        // 加载报告内容
        loadReportContent();
    }
    
    private void initWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // 页面加载完成
            }
            
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(ReportViewerActivity.this, "加载报告失败: " + description, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadReportContent() {
        // 由于没有实际的报告URL，我们创建一个HTML内容来显示报告信息
        String reportContent = generateReportHtml();
        webView.loadDataWithBaseURL(null, reportContent, "text/html", "UTF-8", null);
    }
    
    private String generateReportHtml() {
        StringBuilder htmlBuilder = new StringBuilder();
        
        htmlBuilder.append("<!DOCTYPE html>");
        htmlBuilder.append("<html lang='zh-CN'>");
        htmlBuilder.append("<head>");
        htmlBuilder.append("<meta charset='UTF-8'>");
        htmlBuilder.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        htmlBuilder.append("<title>").append(physicalExamReport.getReportName()).append("</title>");
        htmlBuilder.append("<style>");
        htmlBuilder.append("body { font-family: Arial, sans-serif; margin: 20px; padding: 0; }");
        htmlBuilder.append("h1, h2 { color: #2196F3; }");
        htmlBuilder.append(".section { margin-bottom: 20px; padding: 15px; border: 1px solid #E0E0E0; border-radius: 5px; }");
        htmlBuilder.append(".label { font-weight: bold; color: #666; }");
        htmlBuilder.append("</style>");
        htmlBuilder.append("</head>");
        htmlBuilder.append("<body>");
        htmlBuilder.append("<h1>").append(physicalExamReport.getReportName()).append("</h1>");
        
        // 基本信息
        htmlBuilder.append("<div class='section'>");
        htmlBuilder.append("<h2>基本信息</h2>");
        htmlBuilder.append("<p><span class='label'>检查日期：</span>").append(physicalExamReport.getExamDate()).append("</p>");
        htmlBuilder.append("<p><span class='label'>医院名称：</span>").append(physicalExamReport.getHospitalName()).append("</p>");
        htmlBuilder.append("</div>");
        
        // 报告摘要
        if (physicalExamReport.getSummary() != null && !physicalExamReport.getSummary().isEmpty()) {
            htmlBuilder.append("<div class='section'>");
            htmlBuilder.append("<h2>报告摘要</h2>");
            htmlBuilder.append("<p>").append(physicalExamReport.getSummary()).append("</p>");
            htmlBuilder.append("</div>");
        }
        
        // 医生意见
        if (physicalExamReport.getDoctorComments() != null && !physicalExamReport.getDoctorComments().isEmpty()) {
            htmlBuilder.append("<div class='section'>");
            htmlBuilder.append("<h2>医生意见</h2>");
            htmlBuilder.append("<p>").append(physicalExamReport.getDoctorComments()).append("</p>");
            htmlBuilder.append("</div>");
        }
        
        // 关键发现
        if (physicalExamReport.getKeyFindings() != null && !physicalExamReport.getKeyFindings().isEmpty()) {
            htmlBuilder.append("<div class='section'>");
            htmlBuilder.append("<h2>关键发现</h2>");
            htmlBuilder.append("<p>").append(physicalExamReport.getKeyFindings().toString()).append("</p>");
            htmlBuilder.append("</div>");
        }
        
        // 正常项目
        if (physicalExamReport.getNormalItems() != null && !physicalExamReport.getNormalItems().isEmpty()) {
            htmlBuilder.append("<div class='section'>");
            htmlBuilder.append("<h2>正常项目</h2>");
            htmlBuilder.append("<p>").append(physicalExamReport.getNormalItems().toString()).append("</p>");
            htmlBuilder.append("</div>");
        }
        
        // 异常项目
        if (physicalExamReport.getAbnormalItems() != null && !physicalExamReport.getAbnormalItems().isEmpty()) {
            htmlBuilder.append("<div class='section'>");
            htmlBuilder.append("<h2>异常项目</h2>");
            htmlBuilder.append("<p>").append(physicalExamReport.getAbnormalItems().toString()).append("</p>");
            htmlBuilder.append("</div>");
        }
        
        // 建议
        if (physicalExamReport.getRecommendations() != null && !physicalExamReport.getRecommendations().isEmpty()) {
            htmlBuilder.append("<div class='section'>");
            htmlBuilder.append("<h2>建议</h2>");
            htmlBuilder.append("<p>").append(physicalExamReport.getRecommendations()).append("</p>");
            htmlBuilder.append("</div>");
        }
        
        htmlBuilder.append("</body>");
        htmlBuilder.append("</html>");
        
        return htmlBuilder.toString();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}