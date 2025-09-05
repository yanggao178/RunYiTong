package com.wenxing.runyitong.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wenxing.runyitong.R;
import com.wenxing.runyitong.model.PDFPageModel;
import com.wenxing.runyitong.utils.PDFSearchUtils;
import com.wenxing.runyitong.utils.PDFZoomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PDFPageAdapter extends RecyclerView.Adapter<PDFPageAdapter.PDFPageViewHolder> {
    private static final String TAG = "PDFPageAdapter";
    private static final int BITMAP_QUALITY = 85;
    private static final float DEFAULT_ZOOM = 1.0f;
    private static final int ANIMATION_DURATION = 300;
    
    private Context context;
    private List<PDFPageModel> pages;
    private PdfRenderer pdfRenderer;
    private ExecutorService executorService;
    private Handler mainHandler;
    private OnPageClickListener onPageClickListener;
    
    // 缩放和搜索相关
    private float zoomLevel = PDFZoomUtils.DEFAULT_ZOOM;
    private List<PDFSearchUtils.SearchResult> searchResults;
    private int highlightedPageIndex = -1;
    private OnPageLongClickListener onPageLongClickListener;
    private float currentZoom = DEFAULT_ZOOM;
    private boolean isZoomEnabled = true;
    
    // 接口定义
    public interface OnPageClickListener {
        void onPageClick(int pageNumber, PDFPageModel page);
    }
    
    public interface OnPageLongClickListener {
        void onPageLongClick(int pageNumber, PDFPageModel page);
    }
    
    public PDFPageAdapter(Context context, PdfRenderer pdfRenderer) {
        if (context == null) {
            throw new IllegalArgumentException("Context不能为空");
        }
        
        this.context = context;
        this.pdfRenderer = pdfRenderer;
        this.pages = new ArrayList<>();
        this.executorService = Executors.newFixedThreadPool(3); // 限制并发线程数
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.searchResults = new ArrayList<>();
        
        // 如果pdfRenderer不为空，立即初始化页面
        if (pdfRenderer != null) {
            initializePages();
        }
        
        Log.d(TAG, "PDFPageAdapter 构造完成，页面数: " + pages.size());
    }
    
    // 新增构造函数，接受页面列表
    public PDFPageAdapter(Context context, List<PDFPageModel> pages) {
        if (context == null) {
            throw new IllegalArgumentException("Context不能为空");
        }
        
        this.context = context;
        this.pdfRenderer = null; // 稍后通过setPdfRenderer设置
        this.pages = pages != null ? new ArrayList<>(pages) : new ArrayList<>();
        this.executorService = Executors.newFixedThreadPool(3);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.searchResults = new ArrayList<>();
        
        Log.d(TAG, "PDFPageAdapter 构造完成，页面数: " + this.pages.size());
    }
    
    private void initializePages() {
        if (pdfRenderer == null) {
            Log.w(TAG, "PdfRenderer 为空，无法初始化页面");
            return;
        }
        
        try {
            int pageCount = pdfRenderer.getPageCount();
            if (pageCount <= 0) {
                Log.w(TAG, "PDF页面数无效: " + pageCount);
                return;
            }
            
            // 清理旧页面
            if (pages != null) {
                for (PDFPageModel page : pages) {
                    if (page != null) {
                        page.cleanup();
                    }
                }
                pages.clear();
            } else {
                pages = new ArrayList<>();
            }
            
            // 创建新页面
            for (int i = 0; i < pageCount; i++) {
                PDFPageModel page = new PDFPageModel(i, "第 " + (i + 1) + " 页");
                pages.add(page);
            }
            
            Log.d(TAG, "初始化 " + pageCount + " 页PDF");
        } catch (Exception e) {
            Log.e(TAG, "初始化页面时发生错误", e);
            if (pages == null) {
                pages = new ArrayList<>();
            }
        }
    }
    
    @NonNull
    @Override
    public PDFPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pdf_page_item, parent, false);
        return new PDFPageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PDFPageViewHolder holder, int position) {
        if (pages == null || position < 0 || position >= pages.size()) {
            Log.w(TAG, "无效的位置或页面列表为空: position=" + position + ", size=" + (pages != null ? pages.size() : "null"));
            return;
        }
        
        PDFPageModel page = pages.get(position);
        if (page == null) {
            Log.w(TAG, "页面为空: position=" + position);
            return;
        }
        
        holder.bind(page, position);
    }
    
    @Override
    public int getItemCount() {
        return pages != null ? pages.size() : 0;
    }
    
    @Override
    public void onViewRecycled(@NonNull PDFPageViewHolder holder) {
        super.onViewRecycled(holder);
        holder.cleanup();
    }
    
    // ViewHolder 类
    public class PDFPageViewHolder extends RecyclerView.ViewHolder {
        private TextView pageHeaderNumber;
        private TextView pageHeaderSize;
        private ImageView pageImageView;
        private ProgressBar loadingProgress;
        private View loadingContainer;
        private View errorContainer;
        private TextView errorMessage;
        private TextView retryButton;
        private View pageOverlay;
        
        private PDFPageModel currentPage;
        private int currentPosition;
        
        public PDFPageViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
            setupClickListeners();
        }
        
        private void initViews() {
            pageHeaderNumber = itemView.findViewById(R.id.page_number);
            pageHeaderSize = itemView.findViewById(R.id.page_size_info);
            pageImageView = itemView.findViewById(R.id.pdf_page_image);
            loadingProgress = itemView.findViewById(R.id.loading_progress);
            loadingContainer = itemView.findViewById(R.id.loading_container);
            errorContainer = itemView.findViewById(R.id.error_container);
            errorMessage = itemView.findViewById(R.id.error_message);
            retryButton = itemView.findViewById(R.id.retry_page_button);
            pageOverlay = itemView.findViewById(R.id.page_overlay);
        }
        
        private void setupClickListeners() {
            // 页面点击
            itemView.setOnClickListener(v -> {
                if (onPageClickListener != null && currentPage != null) {
                    onPageClickListener.onPageClick(currentPosition, currentPage);
                }
            });
            
            // 页面长按
            itemView.setOnLongClickListener(v -> {
                if (onPageLongClickListener != null && currentPage != null) {
                    onPageLongClickListener.onPageLongClick(currentPosition, currentPage);
                    return true;
                }
                return false;
            });
            
            // 重试按钮
            retryButton.setOnClickListener(v -> {
                if (currentPage != null) {
                    loadPageBitmap(currentPage, currentPosition);
                }
            });
        }
        
        public void bind(PDFPageModel page, int position) {
            if (page == null) {
                Log.w(TAG, "尝试绑定空页面，position: " + position);
                return;
            }
            
            this.currentPage = page;
            this.currentPosition = position;
            
            try {
                // 设置页面头部信息
                if (pageHeaderNumber != null) {
                    pageHeaderNumber.setText(page.getPageTitle());
                }
                
                // 根据加载状态更新UI
                updateUIForLoadingState(page.getLoadingState());
                
                // 如果页面未加载，开始加载
                if (!page.isLoaded() && !page.isLoading()) {
                    loadPageBitmap(page, position);
                } else if (page.isLoaded()) {
                    displayPageBitmap(page);
                }
                
                // 更新页脚信息
                updateFooterInfo(page);
            } catch (Exception e) {
                Log.e(TAG, "绑定页面时发生错误，position: " + position, e);
            }
        }
        
        private void updateUIForLoadingState(PDFPageModel.LoadingState state) {
            switch (state) {
                case PENDING:
                case LOADING:
                    showLoading();
                    break;
                case LOADED:
                    showContent();
                    break;
                case ERROR:
                    showError();
                    break;
            }
        }
        
        private void showLoading() {
            loadingContainer.setVisibility(View.VISIBLE);
            errorContainer.setVisibility(View.GONE);
            pageImageView.setVisibility(View.GONE);
            pageOverlay.setVisibility(View.GONE);
            
            // 启动加载动画
            if (loadingProgress.getVisibility() != View.VISIBLE) {
                loadingProgress.setVisibility(View.VISIBLE);
                ObjectAnimator.ofFloat(loadingProgress, "rotation", 0f, 360f)
                        .setDuration(1000)
                        .start();
            }
        }
        
        private void showContent() {
            loadingContainer.setVisibility(View.GONE);
            errorContainer.setVisibility(View.GONE);
            pageImageView.setVisibility(View.VISIBLE);
            
            // 添加淡入动画
            pageImageView.setAlpha(0f);
            ObjectAnimator animator = ObjectAnimator.ofFloat(pageImageView, "alpha", 0f, 1f);
            animator.setDuration(ANIMATION_DURATION);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.start();
        }
        
        private void showError() {
            loadingContainer.setVisibility(View.GONE);
            errorContainer.setVisibility(View.VISIBLE);
            pageImageView.setVisibility(View.GONE);
            pageOverlay.setVisibility(View.GONE);
            
            if (currentPage != null && currentPage.getErrorMessage() != null) {
                errorMessage.setText(currentPage.getErrorMessage());
            } else {
                errorMessage.setText("加载失败");
            }
        }
        
        private void displayPageBitmap(PDFPageModel page) {
            try {
                if (page == null) {
                    Log.w(TAG, "页面为空，无法显示bitmap");
                    showError();
                    return;
                }
                
                Bitmap bitmap = page.getPageBitmap();
                if (bitmap != null && !bitmap.isRecycled()) {
                    // 验证bitmap尺寸
                    if (bitmap.getWidth() > 0 && bitmap.getHeight() > 0) {
                        pageImageView.setImageBitmap(bitmap);
                        
                        // 更新页面大小信息
                        if (pageHeaderSize != null) {
                            String sizeInfo = page.getOriginalWidth() + "×" + page.getOriginalHeight();
                            if (page.getZoomLevel() != 1.0f) {
                                sizeInfo += String.format(" (%.1fx)", page.getZoomLevel());
                            }
                            pageHeaderSize.setText(sizeInfo);
                            pageHeaderSize.setVisibility(View.VISIBLE);
                        }
                        
                        // 显示内容
                        showContent();
                        
                        Log.d(TAG, "页面 " + (page.getPageNumber() + 1) + " bitmap显示成功");
                    } else {
                        Log.w(TAG, "页面 " + (page.getPageNumber() + 1) + " bitmap尺寸无效: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                        page.setErrorMessage("图像尺寸无效");
                        page.setLoadingState(PDFPageModel.LoadingState.ERROR);
                        showError();
                    }
                } else {
                    Log.w(TAG, "页面 " + (page.getPageNumber() + 1) + " bitmap为空或已回收");
                    page.setErrorMessage("图像数据无效");
                    page.setLoadingState(PDFPageModel.LoadingState.ERROR);
                    showError();
                }
            } catch (Exception e) {
                Log.e(TAG, "显示页面bitmap时发生错误", e);
                if (page != null) {
                    page.setErrorMessage("显示失败: " + e.getMessage());
                    page.setLoadingState(PDFPageModel.LoadingState.ERROR);
                }
                showError();
            }
        }
        
        private void updateFooterInfo(PDFPageModel page) {
            StringBuilder info = new StringBuilder();
            
            if (page.getLoadDuration() > 0) {
                info.append("加载时间: ").append(page.getLoadDurationString());
            }
            
            if (page.getZoomLevel() != 1.0f) {
                if (info.length() > 0) info.append(" | ");
                info.append(String.format("缩放: %.1fx", page.getZoomLevel()));
            }
            
            if (page.isBookmarked()) {
                if (info.length() > 0) info.append(" | ");
                info.append("★ 已收藏");
            }
            
            // Footer info removed - using page_info_text instead
            TextView pageInfoText = itemView.findViewById(R.id.page_info_text);
            if (pageInfoText != null) {
                pageInfoText.setText(info.toString());
                pageInfoText.setVisibility(info.length() > 0 ? View.VISIBLE : View.GONE);
            }
        }
        
        public void cleanup() {
            try {
                // 清理ImageView
                if (pageImageView != null) {
                    Drawable drawable = pageImageView.getDrawable();
                    if (drawable instanceof BitmapDrawable) {
                        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                        if (bitmap != null && !bitmap.isRecycled()) {
                            // 不在这里回收bitmap，因为它可能被PDFPageModel管理
                            Log.d(TAG, "清理ViewHolder中的bitmap引用");
                        }
                    }
                    pageImageView.setImageBitmap(null);
                }
                
                // 停止所有动画
                if (loadingProgress != null) {
                    loadingProgress.clearAnimation();
                }
                
                // 清理当前页面引用
                currentPage = null;
                
                Log.d(TAG, "ViewHolder清理完成");
            } catch (Exception e) {
                Log.e(TAG, "清理ViewHolder时发生错误", e);
            }
        }
    }
    
    // 加载页面位图
    private void loadPageBitmap(PDFPageModel page, int position) {
        if (page == null) {
            Log.w(TAG, "页面为空，无法加载");
            return;
        }
        
        if (pdfRenderer == null) {
            Log.w(TAG, "PdfRenderer为空，无法加载页面 " + (page.getPageNumber() + 1));
            page.setErrorMessage("PDF渲染器未初始化");
            page.setLoadingState(PDFPageModel.LoadingState.ERROR);
            notifyItemChanged(position);
            return;
        }
        
        // 如果页面已经加载或正在加载，跳过
        if (page.isLoaded() || page.isLoading()) {
            return;
        }
        
        // 检查executorService是否可用
        if (executorService == null || executorService.isShutdown()) {
            Log.w(TAG, "ExecutorService不可用，无法加载页面");
            page.setErrorMessage("服务不可用");
            page.setLoadingState(PDFPageModel.LoadingState.ERROR);
            notifyItemChanged(position);
            return;
        }
        
        page.setLoadingState(PDFPageModel.LoadingState.LOADING);
        notifyItemChanged(position);
        
        executorService.execute(() -> {
            PdfRenderer.Page pdfPage = null;
            Bitmap bitmap = null;
            try {
                // 检查页面索引是否有效
                if (page.getPageNumber() < 0 || page.getPageNumber() >= pdfRenderer.getPageCount()) {
                    throw new IllegalArgumentException("页面索引超出范围: " + page.getPageNumber());
                }
                
                pdfPage = pdfRenderer.openPage(page.getPageNumber());
                
                // 先获取原始尺寸，避免在close后访问
                int originalWidth = pdfPage.getWidth();
                int originalHeight = pdfPage.getHeight();
                
                // 验证原始尺寸
                if (originalWidth <= 0 || originalHeight <= 0) {
                    throw new IllegalStateException("页面尺寸无效: " + originalWidth + "x" + originalHeight);
                }
                
                // 使用新的缩放级别计算尺寸
                float effectiveZoom = Math.max(0.1f, Math.min(5.0f, Math.max(zoomLevel, currentZoom)));
                int width = Math.max(1, (int) (originalWidth * effectiveZoom));
                int height = Math.max(1, (int) (originalHeight * effectiveZoom));
                
                // 限制最大尺寸以避免内存问题
                final int MAX_SIZE = 2048;
                if (width > MAX_SIZE || height > MAX_SIZE) {
                    float scale = Math.min((float) MAX_SIZE / width, (float) MAX_SIZE / height);
                    width = Math.max(1, (int) (width * scale));
                    height = Math.max(1, (int) (height * scale));
                }
                
                // 检查内存使用情况
                long requiredMemory = (long) width * height * 4; // ARGB_8888 = 4 bytes per pixel
                Runtime runtime = Runtime.getRuntime();
                long availableMemory = runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory());
                
                if (requiredMemory > availableMemory * 0.8) { // 使用80%的可用内存作为阈值
                    throw new OutOfMemoryError("内存不足，无法创建bitmap: 需要 " + (requiredMemory / 1024 / 1024) + "MB");
                }
                
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                pdfPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                
                // 检查是否有搜索结果需要高亮
                if (searchResults != null) {
                    for (PDFSearchUtils.SearchResult result : searchResults) {
                        if (result.getPageNumber() == page.getPageNumber()) {
                            page.setBookmarked(true); // 临时用书签状态表示搜索高亮
                            break;
                        }
                    }
                }
                
                final Bitmap finalBitmap = bitmap;
                final int finalOriginalWidth = originalWidth;
                final int finalOriginalHeight = originalHeight;
                final float finalEffectiveZoom = effectiveZoom;
                
                // 在主线程更新UI
                mainHandler.post(() -> {
                    try {
                        if (finalBitmap != null && !finalBitmap.isRecycled()) {
                            page.setPageBitmap(finalBitmap);
                            page.setOriginalWidth(finalOriginalWidth);
                            page.setOriginalHeight(finalOriginalHeight);
                            page.setZoomLevel(finalEffectiveZoom);
                            page.setLoadingState(PDFPageModel.LoadingState.LOADED);
                            notifyItemChanged(position);
                            
                            Log.d(TAG, "页面 " + (page.getPageNumber() + 1) + " 加载完成，耗时: " + page.getLoadDurationString());
                        } else {
                            throw new IllegalStateException("Bitmap无效或已回收");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "UI更新失败", e);
                        page.setErrorMessage("UI更新失败: " + e.getMessage());
                        page.setLoadingState(PDFPageModel.LoadingState.ERROR);
                        notifyItemChanged(position);
                        // 回收bitmap
                        if (finalBitmap != null && !finalBitmap.isRecycled()) {
                            finalBitmap.recycle();
                        }
                    }
                });
                
            } catch (OutOfMemoryError e) {
                Log.e(TAG, "内存不足，无法加载页面 " + (page.getPageNumber() + 1), e);
                
                mainHandler.post(() -> {
                    page.setErrorMessage("内存不足");
                    page.setLoadingState(PDFPageModel.LoadingState.ERROR);
                    notifyItemChanged(position);
                });
                
                // 回收bitmap
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            } catch (Exception e) {
                Log.e(TAG, "加载页面 " + (page.getPageNumber() + 1) + " 失败", e);
                
                mainHandler.post(() -> {
                    page.setErrorMessage("加载失败: " + e.getMessage());
                    page.setLoadingState(PDFPageModel.LoadingState.ERROR);
                    notifyItemChanged(position);
                });
                
                // 回收bitmap
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            } finally {
                if (pdfPage != null) {
                    try {
                        pdfPage.close();
                    } catch (Exception e) {
                        Log.w(TAG, "关闭PDF页面时发生错误", e);
                    }
                }
            }
        });
    }
    
    // 公共方法
    public void setZoom(float zoom) {
        if (zoom <= 0) {
            Log.w(TAG, "无效的缩放值: " + zoom + "，使用默认值1.0");
            zoom = 1.0f;
        }
        
        // 限制缩放范围
        zoom = Math.max(0.1f, Math.min(5.0f, zoom));
        
        if (zoom != currentZoom) {
            this.currentZoom = zoom;
            this.zoomLevel = zoom;
            
            Log.d(TAG, "设置缩放级别: " + zoom);
            
            // 清理所有已加载的页面，强制重新加载
            if (pages != null) {
                for (PDFPageModel page : pages) {
                    if (page != null) {
                        page.cleanup();
                        page.setLoadingState(PDFPageModel.LoadingState.PENDING);
                    }
                }
            }
            
            notifyDataSetChanged();
        }
    }
    
    public float getCurrentZoom() {
        return currentZoom;
    }
    
    public void setZoomEnabled(boolean enabled) {
        this.isZoomEnabled = enabled;
    }
    
    public boolean isZoomEnabled() {
        return isZoomEnabled;
    }
    
    public void refreshPage(int pageNumber) {
        if (pageNumber >= 0 && pageNumber < pages.size()) {
            PDFPageModel page = pages.get(pageNumber);
            page.cleanup();
            page.setLoadingState(PDFPageModel.LoadingState.PENDING);
            notifyItemChanged(pageNumber);
        }
    }
    
    public void refreshAllPages() {
        if (pages != null) {
            for (PDFPageModel page : pages) {
                if (page != null) {
                    page.cleanup();
                    page.setLoadingState(PDFPageModel.LoadingState.PENDING);
                }
            }
        }
        notifyDataSetChanged();
    }
    
    // 更新页面列表
    public void updatePages(List<PDFPageModel> newPages) {
        if (pages != null) {
            // 清理旧页面
            for (PDFPageModel page : pages) {
                if (page != null) {
                    page.cleanup();
                }
            }
            pages.clear();
        } else {
            pages = new ArrayList<>();
        }
        
        if (newPages != null) {
            pages.addAll(newPages);
        }
        
        notifyDataSetChanged();
         Log.d(TAG, "页面列表已更新，新页面数: " + pages.size());
     }
    
    public PDFPageModel getPage(int position) {
        if (position >= 0 && position < pages.size()) {
            return pages.get(position);
        }
        return null;
    }
    
    public void toggleBookmark(int pageNumber) {
        if (pageNumber >= 0 && pageNumber < pages.size()) {
            PDFPageModel page = pages.get(pageNumber);
            page.setBookmarked(!page.isBookmarked());
            notifyItemChanged(pageNumber);
            
            String message = page.isBookmarked() ? "已添加书签" : "已移除书签";
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
    
    // 设置监听器
    public void setOnPageClickListener(OnPageClickListener listener) {
        this.onPageClickListener = listener;
    }
    
    public void setZoomLevel(float zoomLevel) {
        this.zoomLevel = PDFZoomUtils.clampZoomLevel(zoomLevel);
    }
    
    public void setSearchResults(List<PDFSearchUtils.SearchResult> searchResults) {
        this.searchResults = searchResults;
    }
    
    public void setHighlightedPage(int pageIndex) {
        this.highlightedPageIndex = pageIndex;
    }
    
    public void setOnPageLongClickListener(OnPageLongClickListener listener) {
        this.onPageLongClickListener = listener;
    }
    
    public void setPdfRenderer(PdfRenderer pdfRenderer) {
        this.pdfRenderer = pdfRenderer;
        if (pdfRenderer != null) {
            initializePages();
            Log.d(TAG, "PdfRenderer 已设置，页面已初始化");
        } else {
            pages.clear();
            Log.w(TAG, "PdfRenderer 设置为空，清空页面");
        }
    }
    
    // 清理资源
    public void cleanup() {
        Log.d(TAG, "开始清理PDFPageAdapter资源");
        
        // 清理所有页面资源
        if (pages != null) {
            for (PDFPageModel page : pages) {
                if (page != null) {
                    page.cleanup();
                }
            }
            pages.clear();
        }
        
        // 关闭线程池
        if (executorService != null && !executorService.isShutdown()) {
            try {
                executorService.shutdown();
                // 等待正在执行的任务完成
                if (!executorService.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // 清理其他引用
        pdfRenderer = null;
        searchResults = null;
        onPageClickListener = null;
        onPageLongClickListener = null;
        
        Log.d(TAG, "PDFPageAdapter资源清理完成");
    }
}