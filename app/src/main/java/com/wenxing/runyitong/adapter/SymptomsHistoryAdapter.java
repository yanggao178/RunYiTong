package com.wenxing.runyitong.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.wenxing.runyitong.R;
import java.util.List;

/**
 * 症状历史记录适配器
 * 用于在RecyclerView中显示症状历史记录列表
 */
public class SymptomsHistoryAdapter extends RecyclerView.Adapter<SymptomsHistoryAdapter.ViewHolder> {
    
    private List<String> historyList;
    private OnItemClickListener onItemClickListener;
    private OnItemDeleteListener onItemDeleteListener;
    
    /**
     * 构造函数
     * @param historyList 历史记录列表
     */
    public SymptomsHistoryAdapter(List<String> historyList) {
        this.historyList = historyList;
    }
    
    /**
     * 项目点击监听器接口
     */
    public interface OnItemClickListener {
        void onItemClick(String symptom);
    }
    
    /**
     * 项目删除监听器接口
     */
    public interface OnItemDeleteListener {
        void onItemDelete(String symptom, int position);
    }
    
    /**
     * 设置项目点击监听器
     * @param listener 监听器
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    
    /**
     * 设置项目删除监听器
     * @param listener 监听器
     */
    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.onItemDeleteListener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_symptoms_history, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String symptom = historyList.get(position);
        holder.bind(symptom, position);
    }
    
    @Override
    public int getItemCount() {
        return historyList.size();
    }
    
    /**
     * 更新历史记录列表
     * @param newHistoryList 新的历史记录列表
     */
    public void updateHistory(List<String> newHistoryList) {
        this.historyList = newHistoryList;
        notifyDataSetChanged();
    }
    
    /**
     * 删除指定位置的项目
     * @param position 位置
     */
    public void removeItem(int position) {
        if (position >= 0 && position < historyList.size()) {
            historyList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, historyList.size());
        }
    }
    
    /**
     * ViewHolder类
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSymptom;
        private ImageView ivDelete;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSymptom = itemView.findViewById(R.id.tv_symptom);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }
        
        /**
         * 绑定数据
         * @param symptom 症状描述
         * @param position 位置
         */
        public void bind(String symptom, int position) {
            tvSymptom.setText(symptom);
            
            // 设置点击事件
            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(symptom);
                }
            });
            
            // 设置删除按钮点击事件
            ivDelete.setOnClickListener(v -> {
                if (onItemDeleteListener != null) {
                    onItemDeleteListener.onItemDelete(symptom, position);
                }
            });
        }
    }
}