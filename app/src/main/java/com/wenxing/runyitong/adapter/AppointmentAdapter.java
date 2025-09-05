package com.wenxing.runyitong.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.wenxing.runyitong.R;
import com.wenxing.runyitong.model.Appointment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 预约信息适配器
 * 用于在RecyclerView中显示用户的预约信息列表
 */
public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private Context context;
    private List<Appointment> appointments;
    private OnAppointmentClickListener listener;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    /**
     * 预约点击事件监听器接口
     */
    public interface OnAppointmentClickListener {
        /**
         * 查看预约详情
         * @param appointment 预约信息
         */
        void onViewDetails(Appointment appointment);
        
        /**
         * 取消预约
         * @param appointment 预约信息
         * @param position 位置
         */
        void onCancelAppointment(Appointment appointment, int position);
    }

    /**
     * 构造函数
     * @param context 上下文
     */
    public AppointmentAdapter(Context context) {
        this.context = context;
        this.appointments = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    /**
     * 设置点击监听器
     * @param listener 监听器
     */
    public void setOnAppointmentClickListener(OnAppointmentClickListener listener) {
        this.listener = listener;
    }

    /**
     * 更新预约列表数据
     * @param appointments 预约列表
     */
    public void updateAppointments(List<Appointment> appointments) {
        this.appointments.clear();
        if (appointments != null) {
            this.appointments.addAll(appointments);
        }
        notifyDataSetChanged();
    }

    /**
     * 移除指定位置的预约
     * @param position 位置
     */
    public void removeAppointment(int position) {
        if (position >= 0 && position < appointments.size()) {
            appointments.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, appointments.size());
        }
    }
    
    /**
     * 点击事件监听器接口
     */
    public interface OnItemClickListener {
        void onItemClick(Appointment appointment);
        void onCancelClick(Appointment appointment);
    }
    
    private OnItemClickListener onItemClickListener;
    
    /**
     * 设置点击事件监听器
     * @param listener 监听器
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.bind(appointment, position);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    /**
     * ViewHolder类
     */
    public class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private View statusIndicator;
        private TextView tvDoctorName;
        private TextView tvDepartmentName;
        private TextView tvHospitalName;
        private TextView tvStatus;
        private TextView tvAppointmentDate;
        private TextView tvAppointmentTime;
        private TextView tvPatientName;
        private TextView tvPatientPhone;
        private LinearLayout layoutSymptoms;
        private TextView tvSymptoms;
        private TextView btnViewDetails;
        private TextView btnCancelAppointment;

        /**
         * ViewHolder构造函数
         * @param itemView 视图项
         */
        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
        }

        /**
         * 初始化视图组件
         */
        private void initViews() {
            statusIndicator = itemView.findViewById(R.id.view_status_indicator);
            tvDoctorName = itemView.findViewById(R.id.tv_doctor_name);
            tvDepartmentName = itemView.findViewById(R.id.tv_department_name);
            tvHospitalName = itemView.findViewById(R.id.tv_hospital_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvAppointmentDate = itemView.findViewById(R.id.tv_appointment_date);
            tvAppointmentTime = itemView.findViewById(R.id.tv_appointment_time);
            tvPatientName = itemView.findViewById(R.id.tv_patient_name);
            tvPatientPhone = itemView.findViewById(R.id.tv_patient_phone);
            layoutSymptoms = itemView.findViewById(R.id.layout_symptoms);
            tvSymptoms = itemView.findViewById(R.id.tv_symptoms);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
            btnCancelAppointment = itemView.findViewById(R.id.btn_cancel_appointment);
        }

        /**
         * 绑定数据到视图
         * @param appointment 预约信息
         * @param position 位置
         */
        public void bind(Appointment appointment, int position) {
            // 设置医生和科室信息
            tvDoctorName.setText(appointment.getDoctorName());
            tvDepartmentName.setText(appointment.getDepartmentName());
            tvHospitalName.setText(appointment.getHospitalName());

            // 设置预约时间
            tvAppointmentDate.setText(dateFormat.format(appointment.getAppointmentDate()));
            tvAppointmentTime.setText(appointment.getAppointmentTime());

            // 设置患者信息
            tvPatientName.setText(appointment.getPatientName());
            tvPatientPhone.setText(maskPhoneNumber(appointment.getPatientPhone()));

            // 设置症状描述
            if (appointment.getSymptoms() != null && !appointment.getSymptoms().trim().isEmpty()) {
                layoutSymptoms.setVisibility(View.VISIBLE);
                tvSymptoms.setText(appointment.getSymptoms());
            } else {
                layoutSymptoms.setVisibility(View.GONE);
            }

            // 设置状态
            setAppointmentStatus(appointment.getStatus());

            // 设置点击事件
            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetails(appointment);
                }
            });

            btnCancelAppointment.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelAppointment(appointment, position);
                }
            });

            // 根据状态显示/隐藏取消按钮
            if ("pending".equals(appointment.getStatus()) || "confirmed".equals(appointment.getStatus())) {
                btnCancelAppointment.setVisibility(View.VISIBLE);
            } else {
                btnCancelAppointment.setVisibility(View.GONE);
            }
        }

        /**
         * 设置预约状态显示
         * @param status 状态
         */
        private void setAppointmentStatus(String status) {
            int statusColor;
            int statusBgRes;
            String statusText;

            switch (status) {
                case "pending":
                    statusColor = ContextCompat.getColor(context, R.color.status_pending);
                    statusBgRes = R.drawable.bg_status_pending;
                    statusText = "待确认";
                    break;
                case "confirmed":
                    statusColor = ContextCompat.getColor(context, R.color.status_confirmed);
                    statusBgRes = R.drawable.bg_status_confirmed;
                    statusText = "已确认";
                    break;
                case "completed":
                    statusColor = ContextCompat.getColor(context, R.color.status_completed);
                    statusBgRes = R.drawable.bg_status_completed;
                    statusText = "已完成";
                    break;
                case "cancelled":
                    statusColor = ContextCompat.getColor(context, R.color.status_cancelled);
                    statusBgRes = R.drawable.bg_status_cancelled;
                    statusText = "已取消";
                    break;
                default:
                    statusColor = ContextCompat.getColor(context, R.color.status_pending);
                    statusBgRes = R.drawable.bg_status_pending;
                    statusText = "未知";
                    break;
            }

            statusIndicator.setBackgroundColor(statusColor);
            tvStatus.setText(statusText);
            tvStatus.setBackgroundResource(statusBgRes);
        }

        /**
         * 隐藏手机号中间四位
         * @param phoneNumber 手机号
         * @return 隐藏后的手机号
         */
        private String maskPhoneNumber(String phoneNumber) {
            if (phoneNumber == null || phoneNumber.length() < 11) {
                return phoneNumber;
            }
            return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(7);
        }
    }
}