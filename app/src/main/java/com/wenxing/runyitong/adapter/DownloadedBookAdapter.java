package com.wenxing.runyitong.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wenxing.runyitong.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DownloadedBookAdapter extends RecyclerView.Adapter<DownloadedBookAdapter.DownloadedBookViewHolder> {
    private Context context;
    private List<File> downloadedBooks;
    private OnBookClickListener onBookClickListener;

    public interface OnBookClickListener {
        void onBookClick(File bookFile);
        void onBookDelete(File bookFile, int position);
    }

    public DownloadedBookAdapter(Context context, List<File> downloadedBooks) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.context = context;
        this.downloadedBooks = downloadedBooks != null ? downloadedBooks : new java.util.ArrayList<>();
    }

    public void setOnBookClickListener(OnBookClickListener listener) {
        this.onBookClickListener = listener;
    }
    
    // 删除书籍的方法
    public void removeBook(int position) {
        if (downloadedBooks != null && position >= 0 && position < downloadedBooks.size()) {
            downloadedBooks.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, downloadedBooks.size());
        }
    }
    
    // 更新书籍列表
    public void updateBooks(List<File> newBooks) {
        if (newBooks == null) {
            this.downloadedBooks = new java.util.ArrayList<>();
        } else {
            this.downloadedBooks = newBooks;
        }
        notifyDataSetChanged();
    }
    
    // 添加单本书籍
    public void addBook(File bookFile) {
        if (downloadedBooks != null && bookFile != null && bookFile.exists()) {
            downloadedBooks.add(bookFile);
            notifyItemInserted(downloadedBooks.size() - 1);
        }
    }
    
    // 清空所有书籍
    public void clearBooks() {
        if (downloadedBooks != null) {
            int size = downloadedBooks.size();
            downloadedBooks.clear();
            notifyItemRangeRemoved(0, size);
        }
    }
    
    // 获取指定位置的书籍
    public File getBook(int position) {
        if (downloadedBooks != null && position >= 0 && position < downloadedBooks.size()) {
            return downloadedBooks.get(position);
        }
        return null;
    }
    
    // 检查是否为空
    public boolean isEmpty() {
        return downloadedBooks == null || downloadedBooks.isEmpty();
    }

    @NonNull
    @Override
    public DownloadedBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.downloaded_book_item, parent, false);
        return new DownloadedBookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadedBookViewHolder holder, int position) {
        try {
            // 检查数据有效性
            if (downloadedBooks == null || position < 0 || position >= downloadedBooks.size()) {
                android.util.Log.e("DownloadedBookAdapter", "Invalid position or null data: position=" + position + ", size=" + (downloadedBooks != null ? downloadedBooks.size() : "null"));
                return;
            }
            
            File bookFile = downloadedBooks.get(position);
            if (bookFile == null || !bookFile.exists()) {
                android.util.Log.e("DownloadedBookAdapter", "Book file is null or does not exist at position " + position);
                return;
            }
            android.util.Log.d("DownloadedBookAdapter", "Binding book at position " + position + ": " + bookFile.getName());
            
            // 检查ViewHolder的TextView是否为null
            if (holder.bookNameTextView == null) {
                android.util.Log.e("DownloadedBookAdapter", "bookNameTextView is null!");
                return;
            }
            if (holder.fileSizeTextView == null) {
                android.util.Log.e("DownloadedBookAdapter", "fileSizeTextView is null!");
                return;
            }
            if (holder.downloadTimeTextView == null) {
                android.util.Log.e("DownloadedBookAdapter", "downloadTimeTextView is null!");
                return;
            }
            
            // 设置书籍名称（去掉.pdf扩展名）
            String fileName = bookFile.getName();
            if (fileName.endsWith(".pdf")) {
                fileName = fileName.substring(0, fileName.length() - 4);
            }
            holder.bookNameTextView.setText(fileName);
            android.util.Log.d("DownloadedBookAdapter", "Set book name: " + fileName);
            
            // 设置文件大小
            long fileSize = bookFile.length();
            String fileSizeText = formatFileSize(fileSize);
            holder.fileSizeTextView.setText(fileSizeText);
            android.util.Log.d("DownloadedBookAdapter", "Set file size: " + fileSizeText);
            
            // 设置下载时间（文件修改时间）
            long lastModified = bookFile.lastModified();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String downloadTime = sdf.format(new Date(lastModified));
            holder.downloadTimeTextView.setText("下载时间: " + downloadTime);
            android.util.Log.d("DownloadedBookAdapter", "Set download time: " + downloadTime);
            
            // 确保卡片可见
            holder.itemView.setVisibility(android.view.View.VISIBLE);
            
            // 设置打开按钮点击事件
            if (holder.openButton != null) {
                holder.openButton.setOnClickListener(v -> {
                    if (onBookClickListener != null && bookFile != null && bookFile.exists()) {
                        onBookClickListener.onBookClick(bookFile);
                    }
                });
            }
            
            // 设置删除按钮点击事件
            if (holder.deleteButton != null) {
                holder.deleteButton.setOnClickListener(v -> {
                    int currentPosition = holder.getAdapterPosition();
                    if (onBookClickListener != null && currentPosition != RecyclerView.NO_POSITION && 
                        currentPosition >= 0 && currentPosition < downloadedBooks.size()) {
                        onBookClickListener.onBookDelete(bookFile, currentPosition);
                    }
                });
            }
            
            // 保留整个item的点击事件（用于打开书籍）
            holder.itemView.setOnClickListener(v -> {
                if (onBookClickListener != null && bookFile != null && bookFile.exists()) {
                    onBookClickListener.onBookClick(bookFile);
                }
            });
            
            android.util.Log.d("DownloadedBookAdapter", "Successfully bound book at position " + position);
            
        } catch (Exception e) {
            android.util.Log.e("DownloadedBookAdapter", "Error binding book at position " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        return downloadedBooks != null ? downloadedBooks.size() : 0;
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0);
        } else {
            return String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }

    public static class DownloadedBookViewHolder extends RecyclerView.ViewHolder {
        ImageView bookIconImageView;
        TextView bookNameTextView;
        TextView fileSizeTextView;
        TextView downloadTimeTextView;
        ImageView deleteButton;
        ImageView openButton;

        public DownloadedBookViewHolder(@NonNull View itemView) {
            super(itemView);
            android.util.Log.d("DownloadedBookAdapter", "Creating ViewHolder");
            
            bookIconImageView = itemView.findViewById(R.id.book_icon);
            bookNameTextView = itemView.findViewById(R.id.book_name);
            fileSizeTextView = itemView.findViewById(R.id.file_size);
            downloadTimeTextView = itemView.findViewById(R.id.download_time);
            deleteButton = itemView.findViewById(R.id.delete_button);
            openButton = itemView.findViewById(R.id.open_button);
            
            // 检查所有View是否找到
            android.util.Log.d("DownloadedBookAdapter", "bookIconImageView: " + (bookIconImageView != null ? "found" : "NULL"));
            android.util.Log.d("DownloadedBookAdapter", "bookNameTextView: " + (bookNameTextView != null ? "found" : "NULL"));
            android.util.Log.d("DownloadedBookAdapter", "fileSizeTextView: " + (fileSizeTextView != null ? "found" : "NULL"));
            android.util.Log.d("DownloadedBookAdapter", "downloadTimeTextView: " + (downloadTimeTextView != null ? "found" : "NULL"));
            android.util.Log.d("DownloadedBookAdapter", "deleteButton: " + (deleteButton != null ? "found" : "NULL"));
            android.util.Log.d("DownloadedBookAdapter", "openButton: " + (openButton != null ? "found" : "NULL"));
            
            if (bookNameTextView == null || fileSizeTextView == null || downloadTimeTextView == null) {
                android.util.Log.e("DownloadedBookAdapter", "Some TextViews are null! This will cause display issues.");
            }
        }
    }
}