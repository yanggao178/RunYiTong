package com.wenxing.runyitong.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.wenxing.runyitong.R;
import com.wenxing.runyitong.model.Hospital;
import com.wenxing.runyitong.service.WeChatAccessibilityService;
import android.provider.Settings;
import android.text.TextUtils;

import java.util.List;

import androidx.appcompat.app.AlertDialog;
import android.widget.Button;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.LinearLayout;
import android.util.TypedValue;
import android.view.Gravity;
import com.wenxing.runyitong.utils.OverlayPermissionManager;
import android.app.Activity;
import android.widget.ImageView;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.Spannable;
import android.graphics.Typeface;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder> {
    
    private List<Hospital> hospitalList;
    private OnHospitalClickListener listener;
    private int selectedPosition = -1;
    private Context context;
    
    // 防止重复显示对话框的状态标记
    private static boolean isDialogShown = false;
    private static long lastDialogTime = 0;
    private static final long DIALOG_COOLDOWN = 10000; // 10秒冷却时间
    
    // 注：现在从医院对象中直接获取微信公众号信息，不再使用硬编码映射
    

    // 微信包名和协议
    private static final String WECHAT_PACKAGE = "com.tencent.mm";
    private static final String WECHAT_SEARCH_SCHEME = "weixin://dl/search?query=";
    private static final String WECHAT_GENERAL_SCHEME = "weixin://";
    public interface OnHospitalClickListener {
        void onHospitalClick(Hospital hospital);
    }
    
    public HospitalAdapter(List<Hospital> hospitalList, OnHospitalClickListener listener) {
        this.hospitalList = hospitalList;
        this.listener = listener;
    }
    
    public HospitalAdapter(Context context, List<Hospital> hospitalList, OnHospitalClickListener listener) {
        this.context = context;
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
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) {
                return; // 位置无效，忽略点击事件
            }
            
            int previousPosition = selectedPosition;
            selectedPosition = adapterPosition;
            
            // 更新选中状态
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            
            // 重新获取医院对象，确保使用最新的位置
            Hospital clickedHospital = hospitalList.get(adapterPosition);
            
            // 打开微信公众号 - 传递完整的医院对象
            // openWeChatPublicAccount(clickedHospital);
            
            // // 跳转到无障碍设置页面
            // if (context instanceof Activity) {
            //     Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            //     intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //     context.startActivity(intent);
            // }
            
            if (listener != null) {
                listener.onHospitalClick(clickedHospital);
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
        private TextView textHospitalDepartments;
        
        public HospitalViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_hospital);
            textHospitalName = itemView.findViewById(R.id.text_hospital_name);
            textHospitalLevel = itemView.findViewById(R.id.text_hospital_level);
            textHospitalAddress = itemView.findViewById(R.id.text_hospital_address);
            textHospitalPhone = itemView.findViewById(R.id.text_hospital_phone);
            textHospitalDepartments = itemView.findViewById(R.id.text_hospital_departments);
        }
        
        public void bind(Hospital hospital, boolean isSelected) {
            textHospitalName.setText(hospital.getName());
            textHospitalLevel.setText(hospital.getLevel());
            textHospitalAddress.setText(hospital.getAddress());
            textHospitalPhone.setText(hospital.getPhone());
            
            // 显示科室信息
            List<String> departments = hospital.getDepartments();
            if (departments != null && !departments.isEmpty()) {
                StringBuilder departmentsStr = new StringBuilder();
                for (int i = 0; i < departments.size(); i++) {
                    departmentsStr.append(departments.get(i));
                    if (i < departments.size() - 1) {
                        departmentsStr.append("、");
                    }
                }
                textHospitalDepartments.setText(departmentsStr.toString());
                textHospitalDepartments.setVisibility(View.VISIBLE);
            } else {
                // 科室信息为空时隐藏该行，不显示'暂无科室信息'
                textHospitalDepartments.setVisibility(View.GONE);
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
     

     
     /**
     * 打开微信公众号 - 从医院对象直接获取信息版本
     */
    private void openWeChatPublicAccount(Hospital hospital) {
        if (context == null) {
            android.util.Log.e("HospitalAdapter", "Context is null");
            return;
        }
        
        // 预检查微信是否安装
        if (!isWeChatInstalled()) {
            android.util.Log.w("HospitalAdapter", "微信未安装");
            Toast.makeText(context, "请先安装微信客户端", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String hospitalName = hospital.getName();
        
        if (hospitalName != null && !hospitalName.isEmpty()) {
            android.util.Log.d("HospitalAdapter", "找到微信公众号搜索关键词: " + hospitalName);
//            jumpToOfficialAccountByUsername(hospital.getWechatId());
//
            // 显示确认对话框，然后使用更完善的启动微信搜索
            // 功能
            showWeChatGuideDialogWithConfirmation(hospitalName);
        } else {
            android.util.Log.w("HospitalAdapter", "未找到该医院的微信公众号信息");
            
            // 提供更友好的提示，允许用户手动搜索
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("公众号信息")
                   .setMessage("未找到该医院的微信公众号预设信息，是否仍要打开微信进行手动搜索？\n\n搜索关键词建议: " + hospitalName)
                   .setPositiveButton("前往微信", (dialog, which) -> {
                       // 使用医院名称作为默认搜索关键词
                       startWeChatSearch(hospitalName);
                   })
                   .setNegativeButton("取消", null)
                   .show();
        }
    }
    

    
    /**
     * 显示美化的微信搜索指引对话框，用户确认后启动微信
     */
    private void showWeChatGuideDialogWithConfirmation(String hospitalName) {
        // 检查是否在冷却时间内，防止重复显示对话框
        long currentTime = System.currentTimeMillis();
        if (isDialogShown && (currentTime - lastDialogTime) < DIALOG_COOLDOWN) {
            android.util.Log.d("HospitalAdapter", "对话框在冷却时间内，跳过显示");
            return;
        }
        
        // 标记对话框已显示
        isDialogShown = true;
        lastDialogTime = currentTime;
        
        // 创建自定义布局
        LinearLayout dialogLayout = new LinearLayout(context);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(60, 40, 60, 20);
        dialogLayout.setBackgroundColor(Color.WHITE);
        
        // 添加微信图标和标题
        LinearLayout headerLayout = new LinearLayout(context);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setGravity(Gravity.CENTER_VERTICAL);
        headerLayout.setPadding(0, 0, 0, 30);
        
        // 微信图标（使用系统图标或自定义颜色圆圈）
        ImageView iconView = new ImageView(context);
        GradientDrawable iconBackground = new GradientDrawable();
        iconBackground.setShape(GradientDrawable.OVAL);
        iconBackground.setColor(Color.parseColor("#07C160")); // 微信绿色
        iconView.setBackground(iconBackground);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(80, 80);
        iconParams.setMargins(0, 0, 20, 0);
        iconView.setLayoutParams(iconParams);
        
        // 标题文本
        TextView titleView = new TextView(context);
        titleView.setText("微信搜索指引");
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        titleView.setTextColor(Color.parseColor("#333333"));
        titleView.setTypeface(null, Typeface.BOLD);
        
        headerLayout.addView(iconView);
        headerLayout.addView(titleView);
        
        // 主要内容文本
        TextView contentView = new TextView(context);
        String content = "即将为您打开微信搜索 \"" + hospitalName + "\"\n\n" +
                "📱 搜索步骤：\n\n" +
                "1️⃣ 点击微信顶部搜索框\n\n" +
                "2️⃣ 输入：" + hospitalName + "\n\n" +
                "3️⃣ 选择公众号进行关注\n\n" +
                "4️⃣ 点击对应的医院公众号";
        
        // 创建带颜色的文本
        SpannableString spannableContent = new SpannableString(content);
        // 高亮搜索关键词
        int keywordStart = content.indexOf(hospitalName);
        if (keywordStart != -1) {
            spannableContent.setSpan(new ForegroundColorSpan(Color.parseColor("#07C160")), 
                    keywordStart, keywordStart + hospitalName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        contentView.setText(spannableContent);
        contentView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        contentView.setTextColor(Color.parseColor("#666666"));
        contentView.setLineSpacing(8, 1.2f);
        contentView.setPadding(0, 0, 0, 40);
        
        // 按钮布局
        LinearLayout buttonLayout = new LinearLayout(context);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.CENTER);
        
        // 取消按钮
        Button cancelButton = new Button(context);
        cancelButton.setText("取消");
        cancelButton.setTextColor(Color.parseColor("#999999"));
        cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        cancelButton.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        cancelParams.setMargins(0, 0, 20, 0);
        cancelButton.setLayoutParams(cancelParams);
        
        // 确认按钮
        Button confirmButton = new Button(context);
        confirmButton.setText("我知道了");
        confirmButton.setTextColor(Color.WHITE);
        confirmButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        confirmButton.setTypeface(null, Typeface.BOLD);
        
        // 创建圆角背景
        GradientDrawable confirmBackground = new GradientDrawable();
        confirmBackground.setShape(GradientDrawable.RECTANGLE);
        confirmBackground.setColor(Color.parseColor("#07C160"));
        confirmBackground.setCornerRadius(25);
        confirmButton.setBackground(confirmBackground);
        
        LinearLayout.LayoutParams confirmParams = new LinearLayout.LayoutParams(
                0, 100, 2);
        confirmButton.setLayoutParams(confirmParams);
        
        buttonLayout.addView(cancelButton);
        buttonLayout.addView(confirmButton);
        
        // 添加所有视图到主布局
        dialogLayout.addView(headerLayout);
        dialogLayout.addView(contentView);
        dialogLayout.addView(buttonLayout);
        
        // 创建对话框
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogLayout)
                .setCancelable(true)
                .create();
        
        // 设置按钮点击事件
        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
            // 用户取消时重置状态，允许重新显示
            resetDialogState();
        });
        confirmButton.setOnClickListener(v -> {
            dialog.dismiss();
            startWeChatSearchDirectly(hospitalName);
            // 用户确认后不立即重置状态，防止从微信返回时重复显示
        });
        
        // 显示对话框
        dialog.show();
        
        // 设置对话框窗口属性
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // 设置圆角背景
            GradientDrawable dialogBackground = new GradientDrawable();
            dialogBackground.setShape(GradientDrawable.RECTANGLE);
            dialogBackground.setColor(Color.WHITE);
            dialogBackground.setCornerRadius(30);
            dialogLayout.setBackground(dialogBackground);
        }
    }
    
    /**
     * 重置对话框状态，允许重新显示
     */
    private static void resetDialogState() {
        isDialogShown = false;
        lastDialogTime = 0;
        android.util.Log.d("HospitalAdapter", "对话框状态已重置");
    }

    public void jumpToOfficialAccountByUsername(String userName) {
        String scheme = "weixin://dl/officialaccounts?username=" + userName;

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(scheme));
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "跳转失败", Toast.LENGTH_SHORT).show();
        }
    }

        /*
         * 直接启动微信搜索功能（不显示fallback对话框）
         */
        private void startWeChatSearchDirectly (String searchKeyword){
            android.util.Log.d("HospitalAdapter", "用户确认后开始启动微信搜索：" + searchKeyword);
            boolean success = false;
            String errorMessage = "";

//            if(searchKeyword != null)
//            {
//                jumpToOfficialAccountByUsername(searchKeyword);
//                success = true;
//            }

            // 方法1：使用无障碍服务自动化搜索（推荐方式）
            WeChatAccessibilityService accessibilityService = WeChatAccessibilityService.getInstance();
            if (accessibilityService != null && isAccessibilityServiceEnabled()) {
                try {
                    android.util.Log.d("HospitalAdapter", "方法1：使用无障碍服务自动搜索");
                    accessibilityService.startWeChatSearch(searchKeyword);;
                    Toast.makeText(context, "正在自动打开微信搜索：" + searchKeyword, Toast.LENGTH_SHORT).show();
                    success = true;
                } catch (Exception e) {
                    android.util.Log.e("HospitalAdapter", "方法1异常：" + e.getMessage());
                    errorMessage += "方法1异常：" + e.getMessage() + "; ";
                    Toast.makeText(context, errorMessage + searchKeyword, Toast.LENGTH_SHORT).show();
                }
            } else {
                android.util.Log.w("HospitalAdapter", "方法1失败：无障碍服务未启用或不可用");
                errorMessage += "方法1失败：无障碍服务未启用; ";
                Toast.makeText(context, errorMessage + searchKeyword, Toast.LENGTH_SHORT).show();
            }

            // 方法2：直接启动微信应用（备用方式）
            if (!success) {
                try {
                    PackageManager pm = context.getPackageManager();
                    Intent launchIntent = pm.getLaunchIntentForPackage(WECHAT_PACKAGE);
                    if (launchIntent != null) {
                        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(launchIntent);
                        android.util.Log.d("HospitalAdapter", "方法2成功：直接启动微信应用");

                        // 检查悬浮窗权限并显示悬浮指引窗口（不显示fallback对话框）
                        if (context instanceof Activity) {
                            Activity activity = (Activity) context;
                            OverlayPermissionManager.checkAndRequestOverlayPermission(activity, new OverlayPermissionManager.PermissionCallback() {
                                @Override
                                public void onPermissionGranted() {
                                    // 权限获取成功，使用无障碍服务显示悬浮指引窗口
                                    WeChatAccessibilityService service = WeChatAccessibilityService.getInstance();
                                    if (service != null) {
                                        // 延迟5秒显示悬浮窗，确保微信已完全启动
                                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                            service.showFloatingGuide(searchKeyword);
                                        }, 5000);
                                    }
                                }

                                @Override
                                public void onPermissionDenied() {
                                    // 权限获取失败，不显示任何对话框，避免重复
                                    android.util.Log.d("HospitalAdapter", "悬浮窗权限被拒绝，不显示fallback对话框");
                                }
                            });
                        }
                        success = true;
                    } else {
                        android.util.Log.w("HospitalAdapter", "方法2失败：无法获取微信启动Intent");
                        errorMessage += "方法2失败：无法获取微信启动Intent; ";
                    }
                } catch (Exception e) {
                    android.util.Log.e("HospitalAdapter", "方法2异常：" + e.getMessage());
                    errorMessage += "方法2异常：" + e.getMessage() + "; ";
                }
            }

            // 如果所有方法都失败，显示错误信息但不显示对话框
            if (!success) {
                android.util.Log.e("HospitalAdapter", "所有启动微信的方法都失败了: " + errorMessage);
                Toast.makeText(context, "无法启动微信，请手动打开微信搜索：" + searchKeyword, Toast.LENGTH_LONG).show();
            }
        }

        /**
         * 启动微信搜索功能（带fallback对话框）
         */
        private void startWeChatSearch (String searchKeyword){
            android.util.Log.d("HospitalAdapter", "用户确认后开始启动微信搜索：" + searchKeyword);
            boolean success = false;
            String errorMessage = "";

            // 方法1：使用无障碍服务自动化搜索（推荐方式）
            WeChatAccessibilityService accessibilityService = WeChatAccessibilityService.getInstance();
            if (accessibilityService != null && isAccessibilityServiceEnabled()) {
                try {
                    android.util.Log.d("HospitalAdapter", "方法1：使用无障碍服务自动搜索");
                    accessibilityService.performWeChatSearch();
                    Toast.makeText(context, "正在自动打开微信搜索：" + searchKeyword, Toast.LENGTH_SHORT).show();
                    success = true;
                } catch (Exception e) {
                    android.util.Log.e("HospitalAdapter", "方法1异常：" + e.getMessage());
                    errorMessage += "方法1异常：" + e.getMessage() + "; ";
                }
            } else {
                android.util.Log.w("HospitalAdapter", "方法1失败：无障碍服务未启用或不可用");
                errorMessage += "方法1失败：无障碍服务未启用; ";
            }

            // 方法2：直接启动微信应用（备用方式）
            if (!success) {
                try {
                    PackageManager pm = context.getPackageManager();
                    Intent launchIntent = pm.getLaunchIntentForPackage("com.tencent.mm");
                    if (launchIntent != null) {
                        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(launchIntent);
                        android.util.Log.d("HospitalAdapter", "方法2成功：直接启动微信应用");

                        // 检查悬浮窗权限并显示指引窗口
                        if (context instanceof Activity) {
                            Activity activity = (Activity) context;
                            OverlayPermissionManager.checkAndRequestOverlayPermission(activity, new OverlayPermissionManager.PermissionCallback() {
                                @Override
                                public void onPermissionGranted() {
                                    // 权限获取成功，使用无障碍服务显示悬浮指引窗口
                                    WeChatAccessibilityService service = WeChatAccessibilityService.getInstance();
                                    if (service != null) {
                                        // 延迟5秒显示悬浮窗，确保微信已完全启动
                                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                            service.showFloatingGuide(searchKeyword);
                                        }, 5000);
                                    } else {
                                        // 如果无障碍服务不可用，回退到普通对话框
                                        showFallbackDialog(searchKeyword);
                                    }
                                }

                                @Override
                                public void onPermissionDenied() {
                                    // 权限获取失败，显示普通对话框
                                    showFallbackDialog(searchKeyword);
                                }
                            });
                        } else {
                            // 如果context不是Activity，直接显示普通对话框
                            showFallbackDialog(searchKeyword);
                        }
                        success = true;
                    } else {
                        android.util.Log.w("HospitalAdapter", "方法2失败：无法获取微信启动Intent");
                        errorMessage += "方法2失败：无法获取微信启动Intent; ";
                    }
                } catch (Exception e) {
                    android.util.Log.e("HospitalAdapter", "方法2异常：" + e.getMessage());
                    errorMessage += "方法2异常：" + e.getMessage() + "; ";
                }
            }

            // 方法3：如果前面的方法都失败，尝试其他方式
            if (!success) {
                try {
                    PackageManager pm = context.getPackageManager();
                    Intent launchIntent = pm.getLaunchIntentForPackage("com.tencent.mm");
                    if (launchIntent != null) {
                        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(launchIntent);
                        android.util.Log.d("HospitalAdapter", "方法2成功：直接启动微信应用");

                        // 启动微信后，尝试延迟发送搜索协议
                        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                        handler.postDelayed(() -> {
                            try {
                                Intent searchIntent = new Intent(Intent.ACTION_VIEW);
                                String searchUrl = "weixin://dl/search?query=" + Uri.encode(searchKeyword);
                                searchIntent.setData(Uri.parse(searchUrl));
                                searchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                if (searchIntent.resolveActivity(pm) != null) {
                                    context.startActivity(searchIntent);
                                    android.util.Log.d("HospitalAdapter", "延迟搜索成功：" + searchUrl);
                                } else {
                                    android.util.Log.w("HospitalAdapter", "延迟搜索失败：无法解析搜索协议");
                                }
                            } catch (Exception ex) {
                                android.util.Log.e("HospitalAdapter", "延迟搜索异常：" + ex.getMessage());
                            }
                        }, 1500); // 延迟1.5秒等待微信启动

                        Toast.makeText(context, "已打开微信，正在尝试自动搜索：" + searchKeyword, Toast.LENGTH_LONG).show();
                        success = true;
                    } else {
                        android.util.Log.w("HospitalAdapter", "方法2失败：无法获取微信启动Intent");
                        errorMessage += "方法2失败：无法获取微信启动Intent; ";
                    }
                } catch (Exception e) {
                    android.util.Log.e("HospitalAdapter", "方法2异常：" + e.getMessage());
                    errorMessage += "方法2异常：" + e.getMessage() + "; ";
                }
            }

            // 方法3：如果搜索协议失败，尝试通用微信协议启动
            if (!success) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("weixin://"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PackageManager pm = context.getPackageManager();
                    if (intent.resolveActivity(pm) != null) {
                        context.startActivity(intent);
                        android.util.Log.d("HospitalAdapter", "方法3成功：通用微信协议");
                        Toast.makeText(context, "已打开微信，请在微信中搜索：" + searchKeyword, Toast.LENGTH_LONG).show();
                        success = true;
                    } else {
                        android.util.Log.w("HospitalAdapter", "方法3失败：无法解析通用微信协议");
                        errorMessage += "方法3失败：无法解析通用微信协议; ";
                    }
                } catch (Exception e) {
                    android.util.Log.e("HospitalAdapter", "方法3异常：" + e.getMessage());
                    errorMessage += "方法3异常：" + e.getMessage() + "; ";
                }
            }

            // 方法4：尝试通过应用商店链接打开微信
            if (!success) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=com.tencent.mm"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    PackageManager pm = context.getPackageManager();
                    if (intent.resolveActivity(pm) != null) {
                        // 不直接打开应用商店，而是先检查微信是否真的未安装
                        if (isWeChatInstalled()) {
                            android.util.Log.w("HospitalAdapter", "微信已安装但无法启动，提示手动搜索");
                            Toast.makeText(context, "微信已安装但无法自动打开，请手动在微信中搜索：" + searchKeyword, Toast.LENGTH_LONG).show();
                        } else {
                            android.util.Log.w("HospitalAdapter", "微信未安装，提示安装");
                            Toast.makeText(context, "请先安装微信客户端，然后搜索：" + searchKeyword, Toast.LENGTH_LONG).show();
                        }
                        success = true;
                    } else {
                        android.util.Log.w("HospitalAdapter", "方法4失败：无法打开应用商店");
                        errorMessage += "方法4失败：无法打开应用商店; ";
                    }
                } catch (Exception e) {
                    android.util.Log.e("HospitalAdapter", "方法4异常：" + e.getMessage());
                    errorMessage += "方法4异常：" + e.getMessage() + "; ";
                }
            }

            // 方法5：如果以上都失败，显示详细错误信息和手动搜索提示
            if (!success) {
                android.util.Log.e("HospitalAdapter", "所有方法都失败了。错误信息：" + errorMessage);
                Toast.makeText(context, "无法自动打开微信，请手动在微信中搜索：" + searchKeyword, Toast.LENGTH_LONG).show();
            }
        }

        /**
         * 检查微信是否安装
         */
        private boolean isWeChatInstalled () {
            PackageManager pm = context.getPackageManager();

            // 方法1：检查包名
            try {
                pm.getPackageInfo("com.tencent.mm", 0);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                // 继续尝试其他方法
            }

            // 方法2：检查Intent是否可以处理微信协议
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("weixin://"));
                if (intent.resolveActivity(pm) != null) {
                    return true;
                }
            } catch (Exception e) {
                // 继续尝试其他方法
            }

            // 方法3：检查微信的启动Activity
            try {
                Intent launchIntent = pm.getLaunchIntentForPackage("com.tencent.mm");
                if (launchIntent != null) {
                    return true;
                }
            } catch (Exception e) {
                // 继续尝试其他方法
            }

            return false;
        }

        /**
         * 检查无障碍服务是否启用
         */
        private boolean isAccessibilityServiceEnabled () {
            int accessibilityEnabled = 0;
            final String service = context.getPackageName() + "/" + WeChatAccessibilityService.class.getCanonicalName();

            try {
                accessibilityEnabled = Settings.Secure.getInt(
                        context.getContentResolver(),
                        Settings.Secure.ACCESSIBILITY_ENABLED);
            } catch (Settings.SettingNotFoundException e) {
                android.util.Log.e("HospitalAdapter", "无障碍设置未找到: " + e.getMessage());
            }

            TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

            if (accessibilityEnabled == 1) {
                String settingValue = Settings.Secure.getString(
                        context.getContentResolver(),
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
                if (settingValue != null) {
                    mStringColonSplitter.setString(settingValue);
                    while (mStringColonSplitter.hasNext()) {
                        String accessibilityService = mStringColonSplitter.next();
                        if (accessibilityService.equalsIgnoreCase(service)) {
                            android.util.Log.d("HospitalAdapter", "无障碍服务已启用");
                            return true;
                        }
                    }
                }
            }

            android.util.Log.w("HospitalAdapter", "无障碍服务未启用");
            return false;
        }

        /**
         * 尝试其他方式打开微信
         */
        private void tryAlternativeWeChatOpen (String hospitalName){
            try {
                // 尝试通过Intent查询可以处理weixin协议的应用
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("weixin://"));
                PackageManager pm = context.getPackageManager();
                if (intent.resolveActivity(pm) != null) {
                    // 如果有应用可以处理weixin协议，说明微信已安装
                    Toast.makeText(context, "正在尝试打开微信公众号...", Toast.LENGTH_SHORT).show();
                    context.startActivity(intent);
                } else {
                    // 没有应用可以处理，提示手动搜索
                    Toast.makeText(context, "无法自动打开微信公众号，请在微信中手动搜索：" + hospitalName, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(context, "请在微信中手动搜索：" + hospitalName, Toast.LENGTH_LONG).show();
            }
        }

        /**
         * 显示美化的微信搜索指引对话框
         */
        /**
         * 显示降级对话框（当权限被拒绝或无障碍服务不可用时）
         * @param searchKeyword 搜索关键词
         */
        private void showFallbackDialog (String searchKeyword){
            String message = "已打开微信\n\n请按以下步骤搜索：\n" +
                    "1. 点击微信顶部搜索框\n" +
                    "2. 输入：" + searchKeyword + "\n" +
                    "3. 选择'公众号'标签\n" +
                    "4. 点击对应的医院公众号";
            showWeChatGuideDialog(message);
        }

        private void showWeChatGuideDialog (String message){
            if (context == null) {
                return;
            }

            // 创建自定义布局
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(60, 40, 60, 20);

            // 创建标题
            TextView titleView = new TextView(context);
            titleView.setText("微信搜索指引");
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            titleView.setTextColor(Color.parseColor("#2E7D32"));
            titleView.setGravity(Gravity.CENTER);
            titleView.setPadding(0, 0, 0, 30);
            titleView.setTypeface(null, android.graphics.Typeface.BOLD);
            layout.addView(titleView);

            // 创建消息内容
            TextView messageView = new TextView(context);
            messageView.setText(message);
            messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            messageView.setTextColor(Color.parseColor("#424242"));
            messageView.setLineSpacing(8, 1.2f);
            messageView.setPadding(20, 0, 20, 30);
            layout.addView(messageView);

            // 创建对话框
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(layout);

            // 创建确定按钮
            builder.setPositiveButton("我知道了", (dialog, which) -> {
                dialog.dismiss();
            });

            AlertDialog dialog = builder.create();

            // 设置对话框样式
            dialog.show();

            // 美化确定按钮
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (positiveButton != null) {
                positiveButton.setTextColor(Color.parseColor("#2E7D32"));
                positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                positiveButton.setTypeface(null, android.graphics.Typeface.BOLD);

                // 创建圆角背景
                GradientDrawable drawable = new GradientDrawable();
                drawable.setShape(GradientDrawable.RECTANGLE);
                drawable.setCornerRadius(25);
                drawable.setColor(Color.parseColor("#E8F5E8"));
                drawable.setStroke(2, Color.parseColor("#2E7D32"));
                positiveButton.setBackground(drawable);
                positiveButton.setPadding(40, 20, 40, 20);
            }

            // 设置对话框窗口样式
            if (dialog.getWindow() != null) {
                GradientDrawable windowDrawable = new GradientDrawable();
                windowDrawable.setShape(GradientDrawable.RECTANGLE);
                windowDrawable.setCornerRadius(20);
                windowDrawable.setColor(Color.WHITE);
                dialog.getWindow().setBackgroundDrawable(windowDrawable);
            }
        }
    }