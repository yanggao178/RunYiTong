package com.wenteng.frontend_android.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.wenteng.frontend_android.R;
import com.wenteng.frontend_android.adapter.BookPageAdapter;
import com.wenteng.frontend_android.adapter.DownloadedBookAdapter;
import com.wenteng.frontend_android.api.ApiClient;
import com.wenteng.frontend_android.api.ApiResponse;
import com.wenteng.frontend_android.model.Book;
import com.wenteng.frontend_android.model.BookPage;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

// iTextG 5.x imports
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.BaseFont;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.ResponseBody;
import java.io.InputStream;
import java.io.FileOutputStream;
import com.wenteng.frontend_android.api.ApiService;
import com.wenteng.frontend_android.api.ApiClient;

public class BookDetailActivity extends AppCompatActivity {
    private static final String TAG = "BookDetailActivity";
    private static final int PAGES_PER_REQUEST = 10;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1001;
    
    private Book book;
    private List<BookPage> bookPages;
    private BookPageAdapter pageAdapter;
    private List<File> downloadedBooks;
    private DownloadedBookAdapter downloadedBookAdapter;
    private ApiService apiService;
    
    private ImageView bookCoverImageView;
    private TextView bookTitleTextView;
    private TextView bookAuthorTextView;
    private TextView bookDescriptionTextView;
    private RecyclerView pagesRecyclerView;
    private ProgressBar loadingProgressBar;
    private Button loadMoreButton;
    private Button downloadButton;
    private TextView pageInfoTextView;
    
    private int currentPage = 1;
    private int totalPages = 0;
    private boolean isLoading = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        
        Intent intent = getIntent();
        if (intent.hasExtra("book")) {
            book = (Book) intent.getSerializableExtra("book");
        } else if (intent.hasExtra("book_id")) {
            // 从HealthFragment传递的参数构建Book对象
            book = new Book();
            book.setId(intent.getIntExtra("book_id", -1));
            book.setName(intent.getStringExtra("book_name"));
            book.setAuthor(intent.getStringExtra("book_author"));
            book.setDescription(intent.getStringExtra("book_description"));
            book.setCoverUrl(intent.getStringExtra("book_cover_url"));
        } else {
            // 创建测试书籍
            book = new Book();
            book.setId(-1);
            book.setName("测试书籍");
            book.setAuthor("未知作者");
            book.setDescription("这是一本测试书籍");
        }
        
        initViews();
        setupRecyclerView();
        displayBookInfo();
        loadDownloadedBooks();
        
        // 初始化API服务
        apiService = ApiClient.getApiService();
        
        // 如果没有已下载的书籍，创建示例书籍
        if (downloadedBooks.isEmpty()) {
            copyExampleBooks();
        }
    }
    
    private void initViews() {
        bookCoverImageView = findViewById(R.id.book_cover_detail);
        bookTitleTextView = findViewById(R.id.book_title_detail);
        bookAuthorTextView = findViewById(R.id.book_author_detail);
        bookDescriptionTextView = findViewById(R.id.book_description_detail);
        pagesRecyclerView = findViewById(R.id.pages_recycler_view);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        loadMoreButton = findViewById(R.id.load_more_button);
        downloadButton = findViewById(R.id.download_button);
        pageInfoTextView = findViewById(R.id.page_info_text);
        
        loadMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNextPage();
            }
        });
        
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload();
            }
        });
    }
    
    private void setupRecyclerView() {
        bookPages = new ArrayList<>();
        pageAdapter = new BookPageAdapter(this, bookPages);
        
        downloadedBooks = new ArrayList<>();
        downloadedBookAdapter = new DownloadedBookAdapter(this, downloadedBooks);
        
        // 设置下载书籍的点击监听器
        downloadedBookAdapter.setOnBookClickListener(new DownloadedBookAdapter.OnBookClickListener() {
            @Override
            public void onBookClick(File bookFile) {
                try {
                    if (bookFile == null) {
                        Log.e(TAG, "Book file is null");
                        Toast.makeText(BookDetailActivity.this, "书籍文件无效", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    Log.d(TAG, "Book clicked: " + bookFile.getAbsolutePath());
                    
                    // 点击已下载的书籍时，打开PDF阅读器
                    String fileName = bookFile.getName();
                    String bookTitle = fileName;
                    if (fileName.endsWith(".pdf")) {
                        bookTitle = fileName.substring(0, fileName.length() - 4);
                    }
                    
                    Log.d(TAG, "Opening PDF with title: " + bookTitle);
                    openPDFReader(bookFile.getAbsolutePath(), bookTitle);
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error handling book click", e);
                    Toast.makeText(BookDetailActivity.this, "打开书籍时出错：" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onBookDelete(File bookFile, int position) {
                try {
                    if (bookFile == null) {
                        Log.e(TAG, "Cannot delete null book file");
                        Toast.makeText(BookDetailActivity.this, "无法删除无效的书籍文件", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    Log.d(TAG, "Delete requested for book: " + bookFile.getName());
                    showDeleteConfirmDialog(bookFile, position);
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error handling book delete", e);
                    Toast.makeText(BookDetailActivity.this, "删除书籍时出错：" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        
        pagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pagesRecyclerView.setAdapter(downloadedBookAdapter);
    }
    
    private void displayBookInfo() {
        if (book != null) {
            // 设置书籍标题
            String title = book.getName();
            if (title == null || title.trim().isEmpty()) {
                title = "未知书籍";
            }
            bookTitleTextView.setText(title);
            
            // 设置作者信息
            String author = book.getAuthor();
            if (author == null || author.trim().isEmpty()) {
                author = "未知作者";
            }
            bookAuthorTextView.setText("作者：" + author);
            
            // 设置书籍描述
            String description = book.getDescription();
            if (description == null || description.trim().isEmpty()) {
                description = "暂无描述信息";
            }
            bookDescriptionTextView.setText(description);
            
            // 加载书籍封面
            if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
                Glide.with(this)
                    .load(book.getCoverUrl())
                    .placeholder(R.drawable.ic_book_placeholder)
                    .error(R.drawable.ic_book_placeholder)
                    .into(bookCoverImageView);
            } else {
                // 设置默认封面
                bookCoverImageView.setImageResource(R.drawable.ic_book_placeholder);
            }
        }
    }
    
    private void loadNextPage() {
        if (!isLoading) {
            loadBookPages(currentPage);
        }
    }
    
    private void loadBookPages(int page) {
        // 简化的页面加载逻辑
        Toast.makeText(this, "页面加载功能暂未实现", Toast.LENGTH_SHORT).show();
    }
    
    private void startDownload() {
        Log.d(TAG, "startDownload method called");
        if (book == null) {
            Toast.makeText(this, "书籍信息不完整", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 检查存储权限
        if (!checkStoragePermission()) {
            requestStoragePermission();
            return;
        }
        
        performDownload();
    }
    
    private boolean checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    private void requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                Toast.makeText(this, "请授予文件管理权限后重试", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
        }
    }
    
    private void performDownload() {
        // 创建并显示美化的下载进度对话框
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_download_progress, null);
        
        ProgressBar progressBar = dialogView.findViewById(R.id.progress_bar);
        TextView progressText = dialogView.findViewById(R.id.progress_text);
        TextView statusMessage = dialogView.findViewById(R.id.status_message);
        Button cancelButton = dialogView.findViewById(R.id.btn_cancel);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);
        
        final AlertDialog progressDialog = builder.create();
        
        // 设置取消按钮点击事件
        final boolean[] isCancelled = {false};
        cancelButton.setOnClickListener(v -> {
            isCancelled[0] = true;
            progressDialog.dismiss();
            downloadButton.setEnabled(true);
            downloadButton.setText("下载并阅读PDF");
            Toast.makeText(BookDetailActivity.this, "下载已取消", Toast.LENGTH_SHORT).show();
        });
        
        progressDialog.show();
        
        // 禁用下载按钮
        downloadButton.setEnabled(false);
        downloadButton.setText("下载中...");
        
        // 执行下载过程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 更新对话框状态
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!isCancelled[0]) {
                                statusMessage.setText("正在创建下载目录...");
                                progressBar.setProgress(10);
                                progressText.setText("10%");
                            }
                        }
                    });
                    
                    Thread.sleep(500); // 模拟处理时间
                    
                    // 创建PDF文件
                    final String fileName;
                    if (book.getName().startsWith("未知")) {
                        fileName = "书籍_" + System.currentTimeMillis() + ".pdf";
                    } else {
                        fileName = book.getName() + ".pdf";
                    }
                    
                    File downloadDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "books");
                    if (!downloadDir.exists()) {
                        downloadDir.mkdirs();
                    }
                    
                    File pdfFile = new File(downloadDir, fileName);
                    
                    // 更新对话框状态
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!isCancelled[0]) {
                                statusMessage.setText("正在从服务器下载PDF文件...");
                                progressBar.setProgress(30);
                                progressText.setText("30%");
                            }
                        }
                    });
                    
                    // 首先尝试从服务器下载PDF文件
                    boolean downloadSuccess = false;
                    boolean resourceNotFound = false;
                    try {
                        Call<ResponseBody> call = apiService.downloadBookPdf(book.getId());
                        Response<ResponseBody> response = call.execute();
                        
                        if (response.isSuccessful() && response.body() != null) {
                            // 从服务器下载PDF文件
                            InputStream inputStream = response.body().byteStream();
                            FileOutputStream outputStream = new FileOutputStream(pdfFile);
                            
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                            
                            outputStream.close();
                            inputStream.close();
                            downloadSuccess = true;
                            Log.d(TAG, "Successfully downloaded PDF from server: " + fileName);
                        } else if (response.code() == 404) {
                            // 服务器返回404，资源不存在
                            resourceNotFound = true;
                            Log.w(TAG, "PDF resource not found on server (404): " + fileName);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to download PDF from server, will create local PDF: " + e.getMessage());
                    }
                    
                    // 如果资源不存在（404错误），显示对话框并退出
                    if (resourceNotFound) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!isCancelled[0]) {
                                    progressDialog.dismiss();
                                    downloadButton.setEnabled(true);
                                    showResourceNotFoundDialog();
                                }
                            }
                        });
                        return;
                    }
                    
                    // 如果服务器下载失败（非404错误），则创建包含完整内容的本地PDF
                    if (!downloadSuccess) {
                        // 更新对话框状态
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!isCancelled[0]) {
                                    statusMessage.setText("服务器下载失败，正在创建本地PDF文件...");
                                    progressBar.setProgress(50);
                                    progressText.setText("50%");
                                }
                            }
                        });
                        
                        Log.d(TAG, "Starting local PDF creation for: " + fileName);
                        Document document = new Document();
                        PdfWriter writer = null;
                        FileOutputStream fos = null;
                        
                        try {
                            fos = new FileOutputStream(pdfFile);
                            writer = PdfWriter.getInstance(document, fos);
                            document.open();
                            Log.d(TAG, "PDF document opened successfully");
                            
                            // 创建字体
                            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
                            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
                            Font authorFont = new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL);
                            Log.d(TAG, "Fonts created successfully");
                            
                            // 添加标题
                            Paragraph title = new Paragraph(book.getName(), titleFont);
                            title.setAlignment(Element.ALIGN_CENTER);
                            document.add(title);
                            document.add(new Paragraph("\n"));
                            Log.d(TAG, "Title added to PDF");
                            
                            // 添加作者信息
                            Paragraph author = new Paragraph("作者: " + book.getAuthor(), authorFont);
                            document.add(author);
                            document.add(new Paragraph("\n"));
                            Log.d(TAG, "Author added to PDF");
                            
                            // 根据书名添加具体内容
                            String fullContent = getBookContent(book.getName());
                            Log.d(TAG, "Got book content, length: " + fullContent.length());
                            Paragraph content = new Paragraph(fullContent, normalFont);
                            document.add(content);
                            Log.d(TAG, "Content added to PDF");
                            
                        } catch (DocumentException e) {
                            Log.e(TAG, "PDF DocumentException", e);
                            throw new IOException("PDF creation failed: " + e.getMessage(), e);
                        } catch (Exception e) {
                            Log.e(TAG, "PDF creation general exception", e);
                            throw new IOException("PDF creation failed: " + e.getMessage(), e);
                        } finally {
                            try {
                                if (document != null && document.isOpen()) {
                                    document.close();
                                    Log.d(TAG, "PDF document closed");
                                }
                                if (fos != null) {
                                    fos.close();
                                    Log.d(TAG, "FileOutputStream closed");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error closing PDF resources", e);
                            }
                        }
                        Log.d(TAG, "Created local PDF with full content: " + fileName + ", size: " + pdfFile.length() + " bytes");
                    }
                    
                    // 更新UI
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!isCancelled[0]) {
                                // 更新对话框状态
                                statusMessage.setText("下载完成，正在打开PDF阅读器...");
                                progressBar.setProgress(100);
                                progressText.setText("100%");
                                
                                // 延迟一下让用户看到完成消息
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 关闭进度对话框
                                        if (progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                        }
                                    
                                    downloadButton.setEnabled(true);
                                    downloadButton.setText("下载并阅读PDF");
                                    
                                    Toast.makeText(BookDetailActivity.this, "下载完成：" + fileName, Toast.LENGTH_LONG).show();
                                    
                                    // 重新加载已下载书籍列表
                                    loadDownloadedBooks();
                                    
                                    // 自动打开PDF阅读器
                                    openPDFReader(pdfFile.getAbsolutePath(), book.getName());
                                    }
                                }, 1000); // 延迟1秒
                            }
                        }
                    });
                    
                } catch (Exception e) {
                    Log.e(TAG, "Download failed", e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!isCancelled[0]) {
                                // 关闭进度对话框
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                
                                downloadButton.setEnabled(true);
                                downloadButton.setText("下载并阅读PDF");
                                Toast.makeText(BookDetailActivity.this, "下载失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        }).start();
    }
    
    private void openPDFReader(String pdfFilePath, String bookTitle) {
        // 验证PDF文件是否存在和有效
        if (!isValidPDFFile(pdfFilePath)) {
            showResourceNotFoundDialog();
            return;
        }
        
        try {
            File pdfFile = new File(pdfFilePath);
            
            // 使用FileProvider获取安全的URI
            Uri pdfUri = androidx.core.content.FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                pdfFile
            );
            
            // 创建Intent使用系统默认PDF阅读器
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            // 检查是否有应用可以处理PDF文件
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
                Log.d(TAG, "Opening PDF with system reader: " + pdfFilePath + ", title: " + bookTitle);
            } else {
                // 如果没有PDF阅读器，提示用户安装
                Toast.makeText(this, "未找到PDF阅读器，请安装PDF阅读应用", Toast.LENGTH_LONG).show();
                Log.w(TAG, "No PDF reader app found on device");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to open PDF with system reader", e);
            Toast.makeText(this, "无法打开PDF文件：" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private String getBookContent(String bookName) {
        switch (bookName) {
            case "伤寒杂病论":
                return "《伤寒杂病论》\n\n" +
                        "作者：张仲景\n\n" +
                        "序言\n" +
                        "《伤寒杂病论》是东汉末年张仲景所著的中医经典著作，被誉为'医圣之书'。本书系统地阐述了外感热病和内科杂病的辨证论治规律，奠定了中医临床医学的基础。\n\n" +
                        "第一章 伤寒论概述\n" +
                        "伤寒者，外感风寒之邪，内伤脏腑之病也。其病机变化复杂，症状表现多样。张仲景根据病邪传变规律，将伤寒病分为六经辨证：太阳病、阳明病、少阳病、太阴病、少阴病、厥阴病。\n\n" +
                        "太阳病篇\n" +
                        "太阳之为病，脉浮，头项强痛而恶寒。太阳病，发热，汗出，恶风，脉缓者，名为中风。太阳病，或已发热，或未发热，必恶寒，体痛，呕逆，脉阴阳俱紧者，名为伤寒。\n\n" +
                        "桂枝汤方：桂枝三两，芍药三两，甘草二两，生姜三两，大枣十二枚。上五味，以水七升，微火煮取三升，去滓，适寒温，服一升。服已须臾，啜热稀粥一升余，以助药力，温覆令一时许，遍身漐漐微似有汗者益佳。\n\n" +
                        "麻黄汤方：麻黄三两，桂枝二两，甘草一两，杏仁七十个。上四味，以水九升，先煮麻黄，减二升，去上沫，内诸药，煮取二升半，去滓，温服八合。覆取微似汗，不须啜粥。\n\n" +
                        "阳明病篇\n" +
                        "阳明之为病，胃家实是也。阳明病，脉迟，汗出多，微恶寒者，表未解也，可发汗，宜桂枝汤。阳明病，脉浮，无汗而喘者，发汗则愈，宜麻黄汤。\n\n" +
                        "白虎汤方：知母六两，石膏一斤，甘草二两，粳米六合。上四味，以水一斗，煮米熟汤成，去滓，温服一升，日三服。\n\n" +
                        "承气汤类方：\n" +
                        "大承气汤：大黄四两，厚朴半斤，枳实五枚，芒硝三合。\n" +
                        "小承气汤：大黄四两，厚朴二两，枳实三枚。\n" +
                        "调胃承气汤：大黄四两，甘草二两，芒硝半升。\n\n" +
                        "少阳病篇\n" +
                        "少阳之为病，口苦，咽干，目眩也。少阳病，往来寒热，胸胁苦满，嘿嘿不欲饮食，心烦喜呕，或胸中烦而不呕，或渴，或腹中痛，或胁下痞硬，或心下悸、小便不利，或不渴、身有微热，或咳者，小柴胡汤主之。\n\n" +
                        "小柴胡汤方：柴胡半斤，黄芩三两，人参三两，半夏半升，甘草三两，生姜三两，大枣十二枚。上七味，以水一斗二升，煮取六升，去滓，再煎取三升，温服一升，日三服。\n\n" +
                        "三阴病篇\n" +
                        "太阴之为病，腹满而吐，食不下，自利益甚，时腹自痛。若下之，必胸下结硬。\n" +
                        "少阴之为病，脉微细，但欲寐也。\n" +
                        "厥阴之为病，消渴，气上撞心，心中疼热，饥而不欲食，食则吐蛔，下之利不止。\n\n" +
                        "第二章 杂病论要点\n" +
                        "杂病者，内伤七情，外感六淫，饮食劳倦，房室金刃，虫兽所伤等诸病也。其治疗当辨证求因，审因论治。\n\n" +
                        "脏腑辨证\n" +
                        "心病：心悸怔忡，失眠多梦，神志不安。治宜养心安神，方用甘麦大枣汤、炙甘草汤等。\n" +
                        "肺病：咳嗽气喘，痰多胸闷。治宜宣肺化痰，方用麻杏石甘汤、小青龙汤等。\n" +
                        "脾胃病：腹痛腹泻，纳呆呕吐。治宜健脾和胃，方用理中汤、四君子汤等。\n" +
                        "肝病：胁痛易怒，头晕目眩。治宜疏肝理气，方用四逆散、逍遥散等。\n" +
                        "肾病：腰膝酸软，阳痿遗精。治宜补肾壮阳，方用肾气丸、右归丸等。\n\n" +
                        "妇科病证\n" +
                        "月经不调：经期紊乱，量多量少。治宜调经理血，方用四物汤、当归芍药散等。\n" +
                        "妊娠病：妊娠恶阻，胎动不安。治宜安胎止呕，方用当归散、胶艾汤等。\n" +
                        "产后病：产后血虚，恶露不绝。治宜补血化瘀，方用生化汤、四物汤等。\n\n" +
                        "结语\n" +
                        "《伤寒杂病论》不仅是一部医学经典，更是中医辨证论治思想的集大成者。其六经辨证体系和方药配伍规律，至今仍指导着中医临床实践，被历代医家奉为圭臬。学习本书，当深入理解其辨证思路，熟练掌握其方药运用，方能在临床中灵活应用，造福患者。\n\n" +
                        "附录：常用方剂索引\n" +
                        "解表剂：桂枝汤、麻黄汤、小青龙汤、大青龙汤\n" +
                        "清热剂：白虎汤、竹叶石膏汤、黄连解毒汤\n" +
                        "攻下剂：大承气汤、小承气汤、调胃承气汤\n" +
                        "和解剂：小柴胡汤、大柴胡汤、柴胡桂枝汤\n" +
                        "温里剂：理中汤、四逆汤、吴茱萸汤\n" +
                        "补益剂：炙甘草汤、肾气丸、当归芍药散\n" +
                        "安神剂：甘麦大枣汤、酸枣仁汤\n" +
                        "理血剂：四物汤、生化汤、桃核承气汤";
            
            case "黄帝内经":
                return "《黄帝内经》\n\n" +
                        "《黄帝内经》是中国最早的医学典籍，奠定了中医学的理论基础。全书分为《素问》和《灵枢》两部分，系统阐述了中医的基本理论，包括阴阳五行、脏腑经络、病因病机、诊法治则等内容。\n\n" +
                        "第一篇 上古天真论\n" +
                        "昔在黄帝，生而神灵，弱而能言，幼而徇齐，长而敦敏，成而登天。乃问于天师曰：余闻上古之人，春秋皆度百岁，而动作不衰；今时之人，年半百而动作皆衰者，时世异耶，人将失之耶？\n\n" +
                        "岐伯对曰：上古之人，其知道者，法于阴阳，和于术数，食饮有节，起居有常，不妄作劳，故能形与神俱，而尽终其天年，度百岁乃去。今时之人不然也，以酒为浆，以妄为常，醉以入房，以欲竭其精，以耗散其真，不知持满，不时御神，务快其心，逆于生乐，起居无节，故半百而衰也。\n\n" +
                        "第二篇 四气调神大论\n" +
                        "春三月，此谓发陈，天地俱生，万物以荣，夜卧早起，广步于庭，被发缓形，以使志生，生而勿杀，予而勿夺，赏而勿罚，此春气之应，养生之道也。逆之则伤肝，夏为寒变，奉长者少。\n\n" +
                        "夏三月，此谓蕃秀，天地气交，万物华实，夜卧早起，无厌于日，使志无怒，使华英成秀，使气得泄，若所爱在外，此夏气之应，养长之道也。逆之则伤心，秋为痎疟，奉收者少，冬至重病。\n\n" +
                        "第三篇 生气通天论\n" +
                        "黄帝曰：夫自古通天者，生之本，本于阴阳。天地之间，六合之内，其气九州、九窍、五脏十二节，皆通乎天气。其生五，其气三，数犯此者，则邪气伤人，此寿命之本也。\n\n" +
                        "阳气者，若天与日，失其所则折寿而不彰。故天运当以日光明，是故阳因而上，卫外者也。因于寒，欲如运枢，起居如惊，神气乃浮。因于暑，汗，烦则喘喝，静则多言，体若燔炭，汗出而散。\n\n" +
                        "脏腑理论\n" +
                        "心者，君主之官也，神明出焉。肺者，相傅之官，治节出焉。肝者，将军之官，谋虑出焉。胆者，中正之官，决断出焉。膻中者，臣使之官，喜乐出焉。脾胃者，仓廪之官，五味出焉。大肠者，传道之官，变化出焉。小肠者，受盛之官，化物出焉。肾者，作强之官，伎巧出焉。三焦者，决渎之官，水道出焉。膀胱者，州都之官，津液藏焉，气化则能出矣。\n\n" +
                        "经络学说\n" +
                        "经脉者，所以能决死生，处百病，调虚实，不可不通。手太阴肺经、手阳明大肠经、足阳明胃经、足太阴脾经、手少阴心经、手太阳小肠经、足太阳膀胱经、足少阴肾经、手厥阴心包经、手少阳三焦经、足少阳胆经、足厥阴肝经，此十二经脉者，人之所以生，病之所以成，人之所以治，病之所以起。\n\n" +
                        "诊断方法\n" +
                        "望而知之谓之神，闻而知之谓之圣，问而知之谓之工，切而知之谓之巧。夫色脉与尺之相应也，如桴鼓影响之相应也，不得相失也，此亦本末根叶之出入也，故根死则叶枯矣。";
            
            case "神农本草经":
                return "《神农本草经》\n\n" +
                        "《神农本草经》是中国现存最早的药学专著，记载了365种药物，分为上、中、下三品。本书奠定了中药学的基础，其药物分类和性味理论至今仍在指导临床用药。\n\n" +
                        "序言\n" +
                        "神农尝百草，始有医药。本草者，药之根本也。上品药性，应天，无毒，多服久服不伤人，欲轻身益气，不老延年者，本上经。中品药性，应人，无毒有毒，斟酌其宜，欲遏病补虚者，本中经。下品药性，应地，多毒，不可久服，欲除寒热邪气，破积聚者，本下经。\n\n" +
                        "上品药物（120种）\n" +
                        "人参：味甘，微寒。主补五脏，安精神，定魂魄，止惊悸，除邪气，明目，开心益智。久服轻身延年。\n" +
                        "甘草：味甘，平。主五脏六腑寒热邪气，坚筋骨，长肌肉，倍力，金疮尰，解毒。久服轻身延年。\n" +
                        "地黄：味甘，寒。主折跌绝筋，伤中，逐血痹，填骨髓，长肌肉。作汤除寒热积聚，除痹。生者尤良。久服轻身不老。\n" +
                        "麦门冬：味甘，平。主心腹结气，伤中伤饱，胃络脉绝，羸瘦短气。久服轻身，不老不饥。\n" +
                        "茯苓：味甘，平。主胸胁逆气，忧恚惊邪恐悸，心下结痛，寒热烦满，咳逆，口焦舌干，利小便。久服安魂养神，不饥延年。\n\n" +
                        "中品药物（120种）\n" +
                        "当归：味甘，温。主咳逆上气，温疟寒热洗洗在皮肤中，妇人漏下绝子，诸恶疮疡金疮，煮饮之。\n" +
                        "芍药：味苦，平。主邪气腹痛，除血痹，破坚积，寒热疝瘕，止痛，利小便，益气。\n" +
                        "川芎：味辛，温。主中风入脑头痛，寒痹，筋挛缓急，金疮，妇人血闭无子。\n" +
                        "白术：味甘，温。主风寒湿痹，死肌，痉，疸，止汗，除热消食。作煎饵久服轻身延年不饥。\n" +
                        "桂枝：味辛，温。主上气咳逆，结气喉痹吐吸，利关节，补中益气。久服通神明，轻身不老。\n\n" +
                        "下品药物（125种）\n" +
                        "大黄：味苦，寒。主下瘀血，血闭，寒热，破症瘕积聚，留饮宿食，荡涤肠胃，推陈致新，通利水谷道，调中化食，安和五脏。\n" +
                        "附子：味辛，温。主风寒咳逆邪气，温中，金疮，破症坚积聚，血瘕，寒湿踒躄，拘挛膝痛，不能行步。\n" +
                        "半夏：味辛，平。主伤寒寒热，心下坚，下气，喉咽肿痛，头眩胸胀，咳逆，肠鸣，止汗。\n" +
                        "麻黄：味苦，温。主中风伤寒头痛，温疟，发表出汗，去邪热气，止咳逆上气，除寒热，破症坚积聚。\n\n" +
                        "药性理论\n" +
                        "四气：寒、热、温、凉。寒凉药性能清热泻火，温热药性能温阳散寒。\n" +
                        "五味：酸、苦、甘、辛、咸。酸能收敛，苦能泄下，甘能补中，辛能发散，咸能软坚。\n" +
                        "升降浮沉：药物作用的趋向性。升浮药性向上向外，沉降药性向下向内。\n" +
                        "归经：药物作用的脏腑定位。不同药物归属不同脏腑经络，产生相应治疗作用。\n\n" +
                        "配伍理论\n" +
                        "七情：单行、相须、相使、相畏、相杀、相恶、相反。\n" +
                        "君臣佐使：方剂配伍的基本原则。君药主治，臣药辅助，佐药调和，使药引经。";
            
            default:
                return "本书是一本关于中医的专业书籍，包含了丰富的理论知识和实践经验。\n\n" +
                        "主要内容包括：\n" +
                        "• 中医基础理论\n" +
                        "• 诊断方法\n" +
                        "• 治疗原则\n" +
                        "• 药物应用\n" +
                        "• 临床实践\n\n" +
                        "本书内容详实，理论与实践相结合，是学习中医的重要参考资料。通过系统学习，可以深入了解中医的精髓，掌握中医诊疗的基本方法和技能。\n\n" +
                        "下载时间: " + new java.util.Date().toString();
        }
    }
    
    private boolean isValidPDFFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            Log.e(TAG, "PDF file path is null or empty");
            return false;
        }
        
        File file = new File(filePath);
        if (!file.exists()) {
            Log.e(TAG, "PDF file does not exist: " + filePath);
            return false;
        }
        
        if (!file.isFile()) {
            Log.e(TAG, "Path is not a file: " + filePath);
            return false;
        }
        
        if (file.length() == 0) {
            Log.e(TAG, "PDF file is empty: " + filePath);
            return false;
        }
        
        if (!file.canRead()) {
            Log.e(TAG, "Cannot read PDF file: " + filePath);
            return false;
        }
        
        // 检查文件扩展名
        if (!filePath.toLowerCase().endsWith(".pdf")) {
            Log.e(TAG, "File is not a PDF: " + filePath);
            return false;
        }
        
        Log.d(TAG, "PDF file validation passed: " + filePath);
        return true;
    }
    
    private void showResourceNotFoundDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_resource_not_found, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        Button btnOk = dialogView.findViewById(R.id.btn_ok);
        Button btnRetry = dialogView.findViewById(R.id.btn_retry);
        
        btnOk.setOnClickListener(v -> dialog.dismiss());
        
        btnRetry.setOnClickListener(v -> {
            dialog.dismiss();
            // 重新触发下载
            performDownload();
        });
        
        dialog.show();
    }
    
    private void loadDownloadedBooks() {
        Log.d(TAG, "Loading downloaded books");
        
        try {
            // 获取下载目录
            File downloadDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "books");
            
            Log.d(TAG, "Using download directory path: " + downloadDir.getAbsolutePath());
            
            // 确保目录存在
            if (!downloadDir.exists()) {
                Log.d(TAG, "Creating download directory");
                if (!downloadDir.mkdirs()) {
                    Log.e(TAG, "Failed to create download directory");
                    Toast.makeText(this, "无法创建下载目录", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // 检查目录权限
            if (!downloadDir.canRead()) {
                Log.e(TAG, "Cannot read download directory");
                Toast.makeText(this, "无法读取下载目录，请检查权限", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 清空现有数据
            downloadedBooks.clear();
            
            // 扫描PDF文件
            File[] allFiles = downloadDir.listFiles();
            if (allFiles != null) {
                for (File file : allFiles) {
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".pdf")) {
                        // 验证PDF文件有效性
                        if (isValidPDFFile(file.getAbsolutePath())) {
                            downloadedBooks.add(file);
                            Log.d(TAG, "Added valid book: " + file.getName() + " (" + file.length() + " bytes)");
                        } else {
                            Log.w(TAG, "Skipped invalid PDF file: " + file.getName());
                        }
                    }
                }
            } else {
                Log.w(TAG, "No files found in download directory or directory is not accessible");
            }
            
            Log.d(TAG, "Found " + downloadedBooks.size() + " valid books");
            
            // 更新UI
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    downloadedBookAdapter.notifyDataSetChanged();
                    
                    if (downloadedBooks.size() > 0) {
                        pageInfoTextView.setText(String.format("共找到 %d 本已下载的书籍", downloadedBooks.size()));
                    } else {
                        pageInfoTextView.setText("暂无已下载的书籍");
                    }
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading downloaded books", e);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(BookDetailActivity.this, "加载已下载书籍时出错：" + e.getMessage(), Toast.LENGTH_LONG).show();
                    pageInfoTextView.setText("加载书籍列表失败");
                }
            });
        }
    }
    
    private void copyExampleBooks() {
        Log.d(TAG, "Creating example books");
        
        File downloadDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "books");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        
        String[] bookNames = {
            "中医基础理论",
            "黄帝内经",
            "伤寒论",
            "本草纲目"
        };
        
        for (String bookName : bookNames) {
            String fileName = bookName + ".pdf";
            File bookFile = new File(downloadDir, fileName);
            if (!bookFile.exists()) {
                try {
                    // 创建真正的PDF文件
                    Document document = new Document();
                    PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(bookFile));
                    document.open();
                    
                    try {
                        // 创建字体
                        Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
                        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
                        
                        // 添加标题
                        Paragraph title = new Paragraph(bookName, titleFont);
                        title.setAlignment(Element.ALIGN_CENTER);
                        document.add(title);
                        
                        // 添加空行
                        document.add(new Paragraph("\n\n"));
                        
                        // 添加内容
                        String contentText = "这是 " + bookName + " 的示例内容。\n\n" +
                                "本书是一本关于中医的专业书籍，包含了丰富的理论知识和实践经验。\n\n" +
                                "主要内容包括：\n" +
                                "• 中医基础理论\n" +
                                "• 诊断方法\n" +
                                "• 治疗原则\n" +
                                "• 药物应用\n" +
                                "• 临床实践\n\n" +
                                "创建时间: " + new java.util.Date().toString();
                        Paragraph content = new Paragraph(contentText, normalFont);
                        document.add(content);
                        
                    } catch (DocumentException e) {
                        Log.e(TAG, "PDF creation failed for " + bookName, e);
                        throw new IOException("PDF creation failed", e);
                    } finally {
                        document.close();
                    }
                    
                    Log.d(TAG, "Created example book: " + fileName);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to create example book: " + fileName, e);
                }
            }
        }
        
        // 重新加载已下载的书籍
        loadDownloadedBooks();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performDownload();
            } else {
                Toast.makeText(this, "需要存储权限才能下载文件", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void showDeleteConfirmDialog(File bookFile, int position) {
        // 获取文件名（去掉.pdf后缀）
        String fileName = bookFile.getName();
        if (fileName.endsWith(".pdf")) {
            fileName = fileName.substring(0, fileName.length() - 4);
        }
        
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("📚 删除书籍")
                .setMessage("您确定要删除《" + fileName + "》吗？\n\n⚠️ 此操作无法撤销，文件将被永久删除。")
                .setPositiveButton("🗑️ 删除", (dialogInterface, which) -> {
                    deleteBook(bookFile, position);
                })
                .setNegativeButton("❌ 取消", (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                })
                .setCancelable(true)
                .create();
        
        // 美化对话框样式
        dialog.show();
        
        // 设置按钮颜色
        if (dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
        if (dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        }
    }
    
    private void deleteBook(File bookFile, int position) {
        try {
            if (bookFile.delete()) {
                downloadedBooks.remove(position);
                downloadedBookAdapter.notifyItemRemoved(position);
                downloadedBookAdapter.notifyItemRangeChanged(position, downloadedBooks.size());
                
                // 更新页面信息
                if (downloadedBooks.size() > 0) {
                    pageInfoTextView.setText(String.format("共找到 %d 本已下载的书籍", downloadedBooks.size()));
                } else {
                    pageInfoTextView.setText("暂无已下载的书籍");
                }
                
                Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Delete book failed", e);
            Toast.makeText(this, "删除失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}