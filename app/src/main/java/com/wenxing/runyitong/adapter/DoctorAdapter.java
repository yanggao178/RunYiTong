package com.wenxing.runyitong.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.wenxing.runyitong.R;
import com.wenxing.runyitong.model.Doctor;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {
    
    private List<Doctor> doctorList;
    private OnDoctorClickListener listener;
    private int selectedPosition = -1;
    
    public interface OnDoctorClickListener {
        void onDoctorClick(Doctor doctor);
    }
    
    public DoctorAdapter(List<Doctor> doctorList, OnDoctorClickListener listener) {
        this.doctorList = doctorList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor, parent, false);
        return new DoctorViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        int adapterPosition = holder.getAdapterPosition();
        Doctor doctor = doctorList.get(adapterPosition);
        android.util.Log.d("DoctorAdapter", "绑定医生数据，位置: " + adapterPosition + ", 医生: " + doctor.getName());
        holder.bind(doctor, adapterPosition == selectedPosition);
        
        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = adapterPosition;
            
            // 更新选中状态
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            
            if (listener != null) {
                listener.onDoctorClick(doctor);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        int count = doctorList != null ? doctorList.size() : 0;
        android.util.Log.d("DoctorAdapter", "getItemCount返回: " + count);
        return count;
    }
    
    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView textDoctorName;
        private TextView textDoctorTitle;
        private TextView textDoctorDepartment;
        private TextView textDoctorHospital;
        private TextView textDoctorExperience;
        private TextView textDoctorSpecialties;
        
        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_doctor);
            textDoctorName = itemView.findViewById(R.id.text_doctor_name);
            textDoctorTitle = itemView.findViewById(R.id.text_doctor_title);
            textDoctorDepartment = itemView.findViewById(R.id.text_doctor_department);
            textDoctorHospital = itemView.findViewById(R.id.text_doctor_hospital);
            textDoctorExperience = itemView.findViewById(R.id.text_doctor_experience);
            textDoctorSpecialties = itemView.findViewById(R.id.text_doctor_specialties);
        }
        
        public void bind(Doctor doctor, boolean isSelected) {
            textDoctorName.setText(doctor.getName());
            textDoctorTitle.setText(doctor.getTitle());
            textDoctorDepartment.setText(doctor.getDepartmentName());
            textDoctorHospital.setText(doctor.getHospitalName());
            textDoctorExperience.setText(doctor.getExperienceYears() + "年经验");
            
            // 显示专业特长
            if (doctor.getSpecialties() != null && !doctor.getSpecialties().isEmpty()) {
                StringBuilder specialties = new StringBuilder("专长：");
                for (int i = 0; i < doctor.getSpecialties().size(); i++) {
                    if (i > 0) specialties.append("、");
                    specialties.append(doctor.getSpecialties().get(i));
                }
                textDoctorSpecialties.setText(specialties.toString());
                textDoctorSpecialties.setVisibility(View.VISIBLE);
            } else {
                textDoctorSpecialties.setVisibility(View.GONE);
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