package com.wenxing.runyitong.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 症状历史记录管理器
 * 负责存储和检索用户输入的症状描述历史记录
 */
public class SymptomsHistoryManager {
    private static final String PREFS_NAME = "symptoms_history";
    private static final String KEY_HISTORY = "history_list";
    private static final int MAX_HISTORY_SIZE = 10; // 最多保存10条历史记录
    
    private SharedPreferences sharedPreferences;
    private Gson gson;
    
    /**
     * 构造函数
     * @param context 上下文对象
     */
    public SymptomsHistoryManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    /**
     * 添加症状描述到历史记录
     * @param symptom 症状描述文本
     */
    public void addSymptom(String symptom) {
        if (symptom == null || symptom.trim().isEmpty()) {
            return;
        }
        
        symptom = symptom.trim();
        List<String> history = getHistory();
        
        // 如果已存在相同的症状，先移除
        history.remove(symptom);
        
        // 添加到列表开头
        history.add(0, symptom);
        
        // 限制历史记录数量
        if (history.size() > MAX_HISTORY_SIZE) {
            history = history.subList(0, MAX_HISTORY_SIZE);
        }
        
        saveHistory(history);
    }
    
    /**
     * 获取症状历史记录列表
     * @return 历史记录列表
     */
    public List<String> getHistory() {
        String json = sharedPreferences.getString(KEY_HISTORY, null);
        if (json == null) {
            return new ArrayList<>();
        }
        
        try {
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> history = gson.fromJson(json, listType);
            return history != null ? history : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * 清空历史记录
     */
    public void clearHistory() {
        sharedPreferences.edit().remove(KEY_HISTORY).apply();
    }
    
    /**
     * 删除指定的历史记录
     * @param symptom 要删除的症状描述
     */
    public void removeSymptom(String symptom) {
        List<String> history = getHistory();
        history.remove(symptom);
        saveHistory(history);
    }
    
    /**
     * 保存历史记录到SharedPreferences
     * @param history 历史记录列表
     */
    private void saveHistory(List<String> history) {
        String json = gson.toJson(history);
        sharedPreferences.edit().putString(KEY_HISTORY, json).apply();
    }
    
    /**
     * 检查是否有历史记录
     * @return 如果有历史记录返回true，否则返回false
     */
    public boolean hasHistory() {
        return !getHistory().isEmpty();
    }
    
    /**
     * 获取历史记录数量
     * @return 历史记录数量
     */
    public int getHistoryCount() {
        return getHistory().size();
    }
}