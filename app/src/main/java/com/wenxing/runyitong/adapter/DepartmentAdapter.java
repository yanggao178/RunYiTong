package com.wenxing.runyitong.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.wenxing.runyitong.R;
import com.wenxing.runyitong.model.Department;
import java.util.List;

public class DepartmentAdapter extends RecyclerView.Adapter<DepartmentAdapter.DepartmentViewHolder> {
    
    private List<Department> departmentList;
    private OnDepartmentClickListener listener;
    private int selectedPosition = -1;
    
    public interface OnDepartmentClickListener {
        void onDepartmentClick(Department department);
    }
    
    public DepartmentAdapter(List<Department> departmentList, OnDepartmentClickListener listener) {
        this.departmentList = departmentList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public DepartmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_department, parent, false);
        return new DepartmentViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull DepartmentViewHolder holder, int position) {
        Department department = departmentList.get(position);
        holder.bind(department, position == selectedPosition);
        
        final int adapterPosition = position; // 只在方法内部使用传入的position
        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = adapterPosition;
            
            // 更新选中状态
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            
            if (listener != null) {
                listener.onDepartmentClick(department);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return departmentList != null ? departmentList.size() : 0;
    }
    
    static class DepartmentViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView textDepartmentName;
        private TextView textDepartmentDescription;
        
        public DepartmentViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_department);
            textDepartmentName = itemView.findViewById(R.id.text_department_name);
            textDepartmentDescription = itemView.findViewById(R.id.text_department_description);
        }
        
        public void bind(Department department, boolean isSelected) {
            textDepartmentName.setText(department.getName());
            
            if (department.getDescription() != null && !department.getDescription().isEmpty()) {
                textDepartmentDescription.setText(department.getDescription());
                textDepartmentDescription.setVisibility(View.VISIBLE);
            } else {
                textDepartmentDescription.setVisibility(View.GONE);
            }
            
            // 设置选中状态的视觉效果
            if (isSelected) {
                cardView.setCardElevation(8f);
                cardView.setCardBackgroundColor(itemView.getContext().getResources().getColor(R.color.selected_item_background));
            } else {
                cardView.setCardElevation(2f);
                cardView.setCardBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.white));
            }
        }
    }
}