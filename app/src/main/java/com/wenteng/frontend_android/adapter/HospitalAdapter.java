package com.wenteng.frontend_android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.wenteng.frontend_android.R;
import com.wenteng.frontend_android.model.Hospital;
import java.util.List;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder> {
    
    private List<Hospital> hospitalList;
    private OnHospitalClickListener listener;
    private int selectedPosition = -1;
    
    public interface OnHospitalClickListener {
        void onHospitalClick(Hospital hospital);
    }
    
    public HospitalAdapter(List<Hospital> hospitalList, OnHospitalClickListener listener) {
        this.hospitalList = hospitalList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public HospitalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hospital, parent, false);
        return new HospitalViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull HospitalViewHolder holder, int position) {
        Hospital hospital = hospitalList.get(position);
        holder.bind(hospital, position == selectedPosition);
        
        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = position;
            
            // 更新选中状态
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            
            if (listener != null) {
                listener.onHospitalClick(hospital);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return hospitalList != null ? hospitalList.size() : 0;
    }
    
    static class HospitalViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView textHospitalName;
        private TextView textHospitalLevel;
        private TextView textHospitalAddress;
        private TextView textHospitalPhone;
        
        public HospitalViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_hospital);
            textHospitalName = itemView.findViewById(R.id.text_hospital_name);
            textHospitalLevel = itemView.findViewById(R.id.text_hospital_level);
            textHospitalAddress = itemView.findViewById(R.id.text_hospital_address);
            textHospitalPhone = itemView.findViewById(R.id.text_hospital_phone);
        }
        
        public void bind(Hospital hospital, boolean isSelected) {
            textHospitalName.setText(hospital.getName());
            textHospitalLevel.setText(hospital.getLevel());
            textHospitalAddress.setText(hospital.getAddress());
            textHospitalPhone.setText(hospital.getPhone());
            
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