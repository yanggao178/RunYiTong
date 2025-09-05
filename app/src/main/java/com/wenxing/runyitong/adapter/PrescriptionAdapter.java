package com.wenxing.runyitong.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wenxing.runyitong.R;
import com.wenxing.runyitong.model.Prescription;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 处方列表适配器
 * 用于显示处方信息列表
 */
public class PrescriptionAdapter extends RecyclerView.Adapter<PrescriptionAdapter.PrescriptionViewHolder> {
    
    private Context context;
    private List<Prescription> prescriptions;
    private OnPrescriptionClickListener listener;
    private SimpleDateFormat dateFormat;

    /**
     * 处方点击监听器接口
     */
    public interface OnPrescriptionClickListener {
        /**
         * 查看处方详情
         * @param prescription 处方对象
         */
        void onViewDetails(Prescription prescription);
        
        /**
         * 更新处方状态
         * @param prescription 处方对象
         * @param position 位置
         */
        void onUpdateStatus(Prescription prescription, int position);
    }

    /**
     * 构造函数
     * @param context 上下文
     */
    public PrescriptionAdapter(Context context) {
        this.context = context;
        this.prescriptions = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    /**
     * 设置点击监听器
     * @param listener 监听器
     */
    public void setOnPrescriptionClickListener(OnPrescriptionClickListener listener) {
        this.listener = listener;
    }

    /**
     * 更新处方列表数据
     * @param prescriptions 处方列表
     */
    public void updatePrescriptions(List<Prescription> prescriptions) {
        this.prescriptions.clear();
        if (prescriptions != null) {
            this.prescriptions.addAll(prescriptions);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PrescriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_prescription, parent, false);
        return new PrescriptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrescriptionViewHolder holder, int position) {
        Prescription prescription = prescriptions.get(position);
        holder.bind(prescription, position);
    }

    @Override
    public int getItemCount() {
        return prescriptions.size();
    }

    /**
     * 处方ViewHolder
     */
    public class PrescriptionViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvPrescriptionId;
        private TextView tvPrescriptionDate;
        private TextView tvPrescriptionStatus;
        private TextView tvDoctorName;
        private TextView tvHospitalName;
        private TextView tvDiagnosis;
        private TextView tvTotalAmount;
        private Button btnViewDetails;
        private Button btnUpdateStatus;

        /**
         * 构造函数
         * @param itemView 项目视图
         */
        public PrescriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvPrescriptionId = itemView.findViewById(R.id.tv_prescription_id);
            tvPrescriptionDate = itemView.findViewById(R.id.tv_prescription_date);
            tvPrescriptionStatus = itemView.findViewById(R.id.tv_prescription_status);
            tvDoctorName = itemView.findViewById(R.id.tv_doctor_name);
            tvHospitalName = itemView.findViewById(R.id.tv_hospital_name);
            tvDiagnosis = itemView.findViewById(R.id.tv_diagnosis);
            tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
            btnUpdateStatus = itemView.findViewById(R.id.btn_update_status);
        }

        /**
         * 绑定数据
         * @param prescription 处方对象
         * @param position 位置
         */
        public void bind(Prescription prescription, int position) {
            // 设置处方基本信息
            tvPrescriptionId.setText("处方编号: #" + String.format("%03d", prescription.getId()));
            
            if (prescription.getPrescriptionDate() != null) {
                tvPrescriptionDate.setText(dateFormat.format(prescription.getPrescriptionDate()));
            } else {
                tvPrescriptionDate.setText("未知日期");
            }
            
            // 设置状态显示
            tvPrescriptionStatus.setText(prescription.getStatusText());
            setStatusStyle(tvPrescriptionStatus, prescription.getStatus());
            
            // 设置医生和医院信息
            tvDoctorName.setText(prescription.getDoctorName() != null ? 
                prescription.getDoctorName() : "未知医生");
            tvHospitalName.setText(prescription.getHospitalName() != null ? 
                prescription.getHospitalName() : "未知医院");
            
            // 设置诊断信息
            tvDiagnosis.setText(prescription.getDiagnosis() != null ? 
                prescription.getDiagnosis() : "无诊断信息");
            
            // 设置总金额
            tvTotalAmount.setText(String.format("¥%.2f", prescription.getTotalAmount()));
            
            // 设置按钮点击事件
            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetails(prescription);
                }
            });
            
            btnUpdateStatus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUpdateStatus(prescription, position);
                }
            });
            
            // 根据状态设置按钮可见性
            if ("completed".equals(prescription.getStatus()) || 
                "cancelled".equals(prescription.getStatus())) {
                btnUpdateStatus.setVisibility(View.GONE);
            } else {
                btnUpdateStatus.setVisibility(View.VISIBLE);
            }
        }

        /**
         * 设置状态样式
         * @param textView 状态文本视图
         * @param status 状态值
         */
        private void setStatusStyle(TextView textView, String status) {
            switch (status) {
                case "active":
                    textView.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                    textView.setBackgroundColor(context.getResources().getColor(R.color.status_active_bg));
                    break;
                case "completed":
                    textView.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
                    textView.setBackgroundColor(context.getResources().getColor(R.color.status_completed_bg));
                    break;
                case "cancelled":
                    textView.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                    textView.setBackgroundColor(context.getResources().getColor(R.color.status_cancelled_bg));
                    break;
                default:
                    textView.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                    textView.setBackgroundColor(context.getResources().getColor(R.color.status_default_bg));
                    break;
            }
        }
    }
}