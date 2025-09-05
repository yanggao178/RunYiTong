package com.wenxing.runyitong.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * PDF书签管理器
 * 负责书签的增删改查和持久化存储
 */
public class BookmarkManager {
    private static final String TAG = "BookmarkManager";
    private static final String PREF_NAME = "pdf_bookmarks";
    private static final String KEY_BOOKMARKS = "bookmarks_";
    
    private Context context;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    
    /**
     * 书签数据模型
     */
    public static class Bookmark {
        private String pdfPath;
        private int pageNumber;
        private String title;
        private String note;
        private long timestamp;
        
        public Bookmark() {}
        
        public Bookmark(String pdfPath, int pageNumber, String title, String note) {
            this.pdfPath = pdfPath;
            this.pageNumber = pageNumber;
            this.title = title;
            this.note = note;
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters and Setters
        public String getPdfPath() { return pdfPath; }
        public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }
        
        public int getPageNumber() { return pageNumber; }
        public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Bookmark bookmark = (Bookmark) obj;
            return pageNumber == bookmark.pageNumber && 
                   pdfPath != null && pdfPath.equals(bookmark.pdfPath);
        }
        
        @Override
        public int hashCode() {
            return (pdfPath != null ? pdfPath.hashCode() : 0) * 31 + pageNumber;
        }
    }
    
    public BookmarkManager(Context context) {
        this.context = context.getApplicationContext();
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }
    
    /**
     * 添加书签
     */
    public boolean addBookmark(String pdfPath, int pageNumber, String title, String note) {
        try {
            List<Bookmark> bookmarks = getBookmarks(pdfPath);
            
            // 检查是否已存在相同页面的书签
            for (Bookmark bookmark : bookmarks) {
                if (bookmark.getPageNumber() == pageNumber) {
                    Log.d(TAG, "书签已存在，页面: " + pageNumber);
                    return false;
                }
            }
            
            Bookmark newBookmark = new Bookmark(pdfPath, pageNumber, title, note);
            bookmarks.add(newBookmark);
            
            // 按页码排序
            Collections.sort(bookmarks, Comparator.comparingInt(Bookmark::getPageNumber));
            
            saveBookmarks(pdfPath, bookmarks);
            Log.d(TAG, "添加书签成功，页面: " + pageNumber);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "添加书签失败", e);
            return false;
        }
    }
    
    /**
     * 删除书签
     */
    public boolean removeBookmark(String pdfPath, int pageNumber) {
        try {
            List<Bookmark> bookmarks = getBookmarks(pdfPath);
            boolean removed = bookmarks.removeIf(bookmark -> bookmark.getPageNumber() == pageNumber);
            
            if (removed) {
                saveBookmarks(pdfPath, bookmarks);
                Log.d(TAG, "删除书签成功，页面: " + pageNumber);
            } else {
                Log.d(TAG, "书签不存在，页面: " + pageNumber);
            }
            
            return removed;
        } catch (Exception e) {
            Log.e(TAG, "删除书签失败", e);
            return false;
        }
    }
    
    /**
     * 检查页面是否有书签
     */
    public boolean hasBookmark(String pdfPath, int pageNumber) {
        try {
            List<Bookmark> bookmarks = getBookmarks(pdfPath);
            return bookmarks.stream().anyMatch(bookmark -> bookmark.getPageNumber() == pageNumber);
        } catch (Exception e) {
            Log.e(TAG, "检查书签失败", e);
            return false;
        }
    }
    
    /**
     * 获取指定PDF的所有书签
     */
    public List<Bookmark> getBookmarks(String pdfPath) {
        try {
            String key = KEY_BOOKMARKS + pdfPath.hashCode();
            String json = sharedPreferences.getString(key, null);
            
            if (json == null || json.isEmpty()) {
                return new ArrayList<>();
            }
            
            Type listType = new TypeToken<List<Bookmark>>(){}.getType();
            List<Bookmark> bookmarks = gson.fromJson(json, listType);
            return bookmarks != null ? bookmarks : new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "获取书签列表失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取书签详情
     */
    public Bookmark getBookmark(String pdfPath, int pageNumber) {
        try {
            List<Bookmark> bookmarks = getBookmarks(pdfPath);
            return bookmarks.stream()
                    .filter(bookmark -> bookmark.getPageNumber() == pageNumber)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            Log.e(TAG, "获取书签详情失败", e);
            return null;
        }
    }
    
    /**
     * 更新书签信息
     */
    public boolean updateBookmark(String pdfPath, int pageNumber, String title, String note) {
        try {
            List<Bookmark> bookmarks = getBookmarks(pdfPath);
            
            for (Bookmark bookmark : bookmarks) {
                if (bookmark.getPageNumber() == pageNumber) {
                    bookmark.setTitle(title);
                    bookmark.setNote(note);
                    bookmark.setTimestamp(System.currentTimeMillis());
                    
                    saveBookmarks(pdfPath, bookmarks);
                    Log.d(TAG, "更新书签成功，页面: " + pageNumber);
                    return true;
                }
            }
            
            Log.d(TAG, "书签不存在，无法更新，页面: " + pageNumber);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "更新书签失败", e);
            return false;
        }
    }
    
    /**
     * 清空指定PDF的所有书签
     */
    public boolean clearBookmarks(String pdfPath) {
        try {
            String key = KEY_BOOKMARKS + pdfPath.hashCode();
            sharedPreferences.edit().remove(key).apply();
            Log.d(TAG, "清空书签成功: " + pdfPath);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "清空书签失败", e);
            return false;
        }
    }
    
    /**
     * 获取书签数量
     */
    public int getBookmarkCount(String pdfPath) {
        return getBookmarks(pdfPath).size();
    }
    
    /**
     * 保存书签列表到SharedPreferences
     */
    private void saveBookmarks(String pdfPath, List<Bookmark> bookmarks) {
        try {
            String key = KEY_BOOKMARKS + pdfPath.hashCode();
            String json = gson.toJson(bookmarks);
            sharedPreferences.edit().putString(key, json).apply();
        } catch (Exception e) {
            Log.e(TAG, "保存书签失败", e);
            throw e;
        }
    }
    
    /**
     * 导出书签数据（用于备份）
     */
    public String exportBookmarks(String pdfPath) {
        try {
            List<Bookmark> bookmarks = getBookmarks(pdfPath);
            return gson.toJson(bookmarks);
        } catch (Exception e) {
            Log.e(TAG, "导出书签失败", e);
            return null;
        }
    }
    
    /**
     * 导入书签数据（用于恢复）
     */
    public boolean importBookmarks(String pdfPath, String json) {
        try {
            Type listType = new TypeToken<List<Bookmark>>(){}.getType();
            List<Bookmark> bookmarks = gson.fromJson(json, listType);
            
            if (bookmarks != null) {
                saveBookmarks(pdfPath, bookmarks);
                Log.d(TAG, "导入书签成功，数量: " + bookmarks.size());
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "导入书签失败", e);
            return false;
        }
    }
}