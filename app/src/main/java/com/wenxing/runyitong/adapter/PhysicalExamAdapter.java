package com.wenxing.runyitong.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.wenxing.runyitong.R;
import com.wenxing.runyitong.model.PhysicalExamReport;
import java.util.ArrayList;
import java.util.List;

/**
 * 体检报告适配器
 * 用于在RecyclerView中显示体检报告列表
 */
public class PhysicalExamAdapter extends RecyclerView.Adapter<PhysicalExamAdapter.PhysicalExamViewHolder> {

    private Context context;
    private List<PhysicalExamReport> physicalExamList;
    private OnPhysicalExamClickListener onPhysicalExamClickListener;

    /**
     * 体检报告点击监听器接口
     */
    public interface OnPhysicalExamClickListener {
        void onViewDetails(PhysicalExamReport report);
        void onDeleteReport(PhysicalExamReport report, int position);
    }

    /**
     * 构造函数
     * @param context 上下文
     */
    public PhysicalExamAdapter(Context context) {
        this.context = context;
        this.physicalExamList = new ArrayList<>();
    }

    /**
     * 设置体检报告列表
     * @param physicalExams 体检报告列表
     */
    public void updatePhysicalExams(List<PhysicalExamReport> physicalExams) {
        if (physicalExams != null) {
            this.physicalExamList = physicalExams;
            notifyDataSetChanged();
        }
    }

    /**
     * 设置点击监听器
     * @param listener 点击监听器
     */
    public void setOnPhysicalExamClickListener(OnPhysicalExamClickListener listener) {
        this.onPhysicalExamClickListener = listener;
    }

    @NonNull
    @Override
    public PhysicalExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_physical_exam, parent, false);
        return new PhysicalExamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhysicalExamViewHolder holder, int position) {
        PhysicalExamReport report = physicalExamList.get(position);
        if (report == null) {
            android.util.Log.w("PhysicalExamAdapter", "Position " + position + " report is null, 设置默认值");
            
            // 设置默认值防止空指针
            holder.tvReportName.setText("数据错误");
            holder.tvExamDate.setText("--");
            holder.tvHospitalName.setText("--");
            holder.tvSummary.setText("数据加载失败");
            return;
        }

        // 记录绑定数据的详细信息 - 增强版本
        android.util.Log.d("PhysicalExamAdapter", "Binding report at position " + position + ":");
        android.util.Log.d("PhysicalExamAdapter", "  ID: " + report.getId());
        android.util.Log.d("PhysicalExamAdapter", "  Name: '" + (report.getReportName() != null ? report.getReportName() : "[null]") + "'");
        android.util.Log.d("PhysicalExamAdapter", "  Date: '" + (report.getExamDate() != null ? report.getExamDate() : "[null]") + "'");
        android.util.Log.d("PhysicalExamAdapter", "  Hospital: '" + (report.getHospitalName() != null ? report.getHospitalName() : "[null]") + "'");
        android.util.Log.d("PhysicalExamAdapter", "  Summary: '" + (report.getSummary() != null ? report.getSummary() : "[null]") + "'");
        android.util.Log.d("PhysicalExamAdapter", "  Doctor Comments: '" + (report.getDoctorComments() != null ? report.getDoctorComments() : "[null]") + "'");
        android.util.Log.d("PhysicalExamAdapter", "  Recommendations: '" + (report.getRecommendations() != null ? report.getRecommendations() : "[null]") + "'");
        
        // 设置报告信息，并添加空值检查 - 增强版本
        try {
            // 报告名称 - 更智能的默认值处理
            String reportName = report.getReportName();
            if (android.text.TextUtils.isEmpty(reportName)) {
                android.util.Log.w("PhysicalExamAdapter", "Report name is empty for position " + position + ", 使用默认名称");
                // 根据其他可用信息生成更有意义的默认名称
                String defaultName = "体检报告";
                if (!android.text.TextUtils.isEmpty(report.getExamDate())) {
                    defaultName += "_" + report.getExamDate();
                } else if (report.getId() > 0) {
                    defaultName += "_" + report.getId();
                } else {
                    defaultName += "_" + (position + 1);
                }
                holder.tvReportName.setText(defaultName);
                android.util.Log.d("PhysicalExamAdapter", "  生成默认报告名称: " + defaultName);
            } else {
                holder.tvReportName.setText(reportName);
            }
            
            // 检查日期 - 增强日期处理
            String examDate = report.getExamDate();
            if (android.text.TextUtils.isEmpty(examDate)) {
                android.util.Log.w("PhysicalExamAdapter", "Exam date is empty for position " + position + ", 使用默认日期");
                holder.tvExamDate.setText("日期未知");
            } else {
                // 尝试格式化日期显示
                try {
                    // 如果是标准日期格式，尝试格式化
                    if (examDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                        java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("yyyy年MM月dd日", java.util.Locale.getDefault());
                        java.util.Date date = inputFormat.parse(examDate);
                        if (date != null) {
                            holder.tvExamDate.setText(outputFormat.format(date));
                        } else {
                            holder.tvExamDate.setText(examDate);
                        }
                    } else {
                        holder.tvExamDate.setText(examDate);
                    }
                } catch (Exception e) {
                    android.util.Log.w("PhysicalExamAdapter", "Date formatting failed, using original: " + examDate);
                    holder.tvExamDate.setText(examDate);
                }
            }
            
            // 医院名称 - 更智能的默认值
            String hospitalName = report.getHospitalName();
            if (android.text.TextUtils.isEmpty(hospitalName)) {
                android.util.Log.w("PhysicalExamAdapter", "Hospital name is empty for position " + position + ", 使用默认医院");
                holder.tvHospitalName.setText("医院信息未知");
            } else {
                holder.tvHospitalName.setText(hospitalName);
            }
            
            // 报告摘要 - 更好的默认值和备选方案
            String summary = report.getSummary();
            String doctorComments = report.getDoctorComments();
            String recommendations = report.getRecommendations();
            
            String displaySummary = "";
            
            if (!android.text.TextUtils.isEmpty(summary)) {
                displaySummary = summary;
                android.util.Log.d("PhysicalExamAdapter", "  使用摘要信息");
            } else if (!android.text.TextUtils.isEmpty(doctorComments)) {
                displaySummary = "医生评论: " + doctorComments;
                android.util.Log.d("PhysicalExamAdapter", "  使用医生评论作为摘要");
            } else if (!android.text.TextUtils.isEmpty(recommendations)) {
                displaySummary = "建议: " + recommendations;
                android.util.Log.d("PhysicalExamAdapter", "  使用建议作为摘要");
            } else {
                displaySummary = "暂无详细信息，点击查看更多";
                android.util.Log.w("PhysicalExamAdapter", "Summary, doctor comments, and recommendations are all empty for position " + position);
            }
            
            holder.tvSummary.setText(displaySummary);
            
            // 数据完整性统计
            boolean hasBasicInfo = !android.text.TextUtils.isEmpty(report.getReportName()) && 
                                  !android.text.TextUtils.isEmpty(report.getExamDate()) && 
                                  !android.text.TextUtils.isEmpty(report.getHospitalName());
            boolean hasDetailInfo = !android.text.TextUtils.isEmpty(summary) || 
                                   !android.text.TextUtils.isEmpty(doctorComments) || 
                                   !android.text.TextUtils.isEmpty(recommendations);
            
            android.util.Log.d("PhysicalExamAdapter", "  数据完整性: 基本信息=" + hasBasicInfo + ", 详细信息=" + hasDetailInfo);
            
            android.util.Log.d("PhysicalExamAdapter", "Successfully bound report data for position " + position);
            
        } catch (Exception e) {
            android.util.Log.e("PhysicalExamAdapter", "Error binding report data for position " + position, e);
            // 设置默认值防止崩溃 - 增强版错误处理
            try {
                holder.tvReportName.setText("数据错误_" + (position + 1));
                holder.tvExamDate.setText("--");
                holder.tvHospitalName.setText("--");
                holder.tvSummary.setText("数据加载失败，请刷新页面");
                android.util.Log.d("PhysicalExamAdapter", "Error recovery completed for position " + position);
            } catch (Exception recoveryError) {
                android.util.Log.e("PhysicalExamAdapter", "Error recovery also failed for position " + position, recoveryError);
            }
        }

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (onPhysicalExamClickListener != null) {
                android.util.Log.d("PhysicalExamAdapter", "Item clicked at position " + position);
                onPhysicalExamClickListener.onViewDetails(report);
            }
        });

        // 设置删除按钮点击事件
        holder.btnDelete.setOnClickListener(v -> {
            if (onPhysicalExamClickListener != null) {
                android.util.Log.d("PhysicalExamAdapter", "Delete clicked at position " + position);
                onPhysicalExamClickListener.onDeleteReport(report, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return physicalExamList != null ? physicalExamList.size() : 0;
    }

    /**
     * 体检报告视图持有者
     */
    public static class PhysicalExamViewHolder extends RecyclerView.ViewHolder {
        TextView tvReportName;
        TextView tvExamDate;
        TextView tvHospitalName;
        TextView tvSummary;
        TextView btnDelete;

        public PhysicalExamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReportName = itemView.findViewById(R.id.tv_report_name);
            tvExamDate = itemView.findViewById(R.id.tv_exam_date);
            tvHospitalName = itemView.findViewById(R.id.tv_hospital_name);
            tvSummary = itemView.findViewById(R.id.tv_summary);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}