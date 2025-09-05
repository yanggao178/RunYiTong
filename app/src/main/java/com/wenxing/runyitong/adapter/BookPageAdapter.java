package com.wenxing.runyitong.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.wenxing.runyitong.R;
import com.wenxing.runyitong.model.BookPage;

import java.util.ArrayList;
import java.util.List;

public class BookPageAdapter extends RecyclerView.Adapter<BookPageAdapter.PageViewHolder> {
    private static final String TAG = "BookPageAdapter";
    private Context context;
    private List<BookPage> pages;
    
    public BookPageAdapter(Context context, List<BookPage> pages) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.context = context;
        this.pages = pages != null ? pages : new ArrayList<>();
        Log.d(TAG, "BookPageAdapter created with " + this.pages.size() + " pages");
    }
    
    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.book_page_item, parent, false);
        return new PageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        if (pages == null || position < 0 || position >= pages.size()) {
            Log.e(TAG, "Invalid position or null pages list: position=" + position + ", pages size=" + (pages != null ? pages.size() : "null"));
            return;
        }
        
        BookPage page = pages.get(position);
        if (page == null) {
            Log.e(TAG, "Page at position " + position + " is null");
            return;
        }
        
        Log.d(TAG, "Binding page: " + page.getPageNumber() + " at position " + position);
        
        // 设置页码
        holder.pageNumberTextView.setText("第 " + page.getPageNumber() + " 页");
        
        // 设置标题
        if (page.getTitle() != null && !page.getTitle().isEmpty()) {
            holder.pageTitleTextView.setText(page.getTitle());
            holder.pageTitleTextView.setVisibility(View.VISIBLE);
        } else {
            holder.pageTitleTextView.setVisibility(View.GONE);
        }
        
        // 设置内容
        if (page.getContent() != null && !page.getContent().isEmpty()) {
            holder.pageContentTextView.setText(page.getContent());
            holder.pageContentTextView.setVisibility(View.VISIBLE);
        } else {
            holder.pageContentTextView.setText("此页暂无文字内容");
            holder.pageContentTextView.setVisibility(View.VISIBLE);
        }
        
        // 设置图片
        if (page.getImageUrl() != null && !page.getImageUrl().isEmpty()) {
            holder.pageImageView.setVisibility(View.VISIBLE);
            try {
                Glide.with(context)
                        .load(page.getImageUrl())
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                        .into(holder.pageImageView);
            } catch (Exception e) {
                Log.e(TAG, "Error loading image: " + e.getMessage());
                holder.pageImageView.setVisibility(View.GONE);
            }
        } else {
            holder.pageImageView.setVisibility(View.GONE);
            // 清除之前的图片以防止复用问题
            Glide.with(context).clear(holder.pageImageView);
        }
    }
    
    @Override
    public int getItemCount() {
        int count = pages != null ? pages.size() : 0;
        Log.d(TAG, "getItemCount: " + count);
        return count;
    }
    
    public void updatePages(List<BookPage> newPages) {
        Log.d(TAG, "Updating pages list. New size: " + (newPages != null ? newPages.size() : 0));
        this.pages = newPages != null ? newPages : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void addPages(List<BookPage> newPages) {
        if (newPages != null && !newPages.isEmpty()) {
            if (pages == null) {
                pages = new ArrayList<>();
            }
            int startPosition = pages.size();
            pages.addAll(newPages);
            notifyItemRangeInserted(startPosition, newPages.size());
            Log.d(TAG, "Added " + newPages.size() + " pages. Total: " + pages.size());
        } else {
            Log.d(TAG, "No new pages to add");
        }
    }
    
    public void clearPages() {
        if (pages != null) {
            int size = pages.size();
            pages.clear();
            notifyItemRangeRemoved(0, size);
            Log.d(TAG, "Cleared all pages");
        }
    }
    
    public BookPage getPage(int position) {
        if (pages != null && position >= 0 && position < pages.size()) {
            return pages.get(position);
        }
        return null;
    }
    
    public List<BookPage> getPages() {
        return pages != null ? new ArrayList<>(pages) : new ArrayList<>();
    }
    
    public boolean isEmpty() {
        return pages == null || pages.isEmpty();
    }
    
    public void cleanup() {
        if (context != null) {
            // 清理Glide缓存
            try {
                Glide.get(context).clearMemory();
            } catch (Exception e) {
                Log.e(TAG, "Error clearing Glide memory: " + e.getMessage());
            }
        }
        Log.d(TAG, "Adapter cleanup completed");
    }
    
    public static class PageViewHolder extends RecyclerView.ViewHolder {
        TextView pageNumberTextView;
        TextView pageTitleTextView;
        TextView pageContentTextView;
        ImageView pageImageView;
        
        public PageViewHolder(@NonNull View itemView) {
            super(itemView);
            pageNumberTextView = itemView.findViewById(R.id.page_number);
            pageTitleTextView = itemView.findViewById(R.id.page_title);
            pageContentTextView = itemView.findViewById(R.id.page_content);
            pageImageView = itemView.findViewById(R.id.page_image);
        }
    }
}