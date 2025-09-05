package com.wenxing.runyitong.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import com.wenxing.runyitong.R;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Switch switchNotifications;
    private Switch switchDarkMode;
    private Switch switchAutoUpdate;
    private CardView cardAbout;
    private CardView cardPrivacy;
    private CardView cardHelp;
    private LinearLayout layoutAddress, layoutSecurity, layoutIdentity, layoutPayment;
    private TextView tvVersionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        initViews();
        setupToolbar();
        setupClickListeners();
        loadSettings();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        switchNotifications = findViewById(R.id.switch_notifications);
        switchDarkMode = findViewById(R.id.switch_dark_mode);
        switchAutoUpdate = findViewById(R.id.switch_auto_update);
        cardAbout = findViewById(R.id.card_about);
        cardPrivacy = findViewById(R.id.card_privacy);
        cardHelp = findViewById(R.id.card_help);
        layoutAddress = findViewById(R.id.layout_address);
        layoutSecurity = findViewById(R.id.layout_security);
        layoutIdentity = findViewById(R.id.layout_identity);
        layoutPayment = findViewById(R.id.layout_payment);
        tvVersionName = findViewById(R.id.tv_version_name);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("设置");
        }
    }
    
    private void setupClickListeners() {
        // 通知设置
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 保存通知设置
            saveNotificationSetting(isChecked);
        });
        
        // 深色模式设置
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 保存深色模式设置
            saveDarkModeSetting(isChecked);
        });
        
        // 自动更新设置
        switchAutoUpdate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 保存自动更新设置
            saveAutoUpdateSetting(isChecked);
        });
        
        // 关于我们
        cardAbout.setOnClickListener(v -> {
            // 打开关于页面
            showAboutDialog();
        });
        
        // 隐私政策
        cardPrivacy.setOnClickListener(v -> {
            // 打开隐私政策页面
            showPrivacyPolicy();
        });
        
        // 帮助与反馈
        cardHelp.setOnClickListener(v -> {
            // 打开帮助页面
            showHelpDialog();
        });
        
        // 账户管理点击事件
        layoutAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddressActivity.class);
            startActivity(intent);
        });
        
        layoutSecurity.setOnClickListener(v -> {
            // 跳转到账号与安全页面
            Intent intent = new Intent(this, SecuritySettingsActivity.class);
            startActivity(intent);
        });
        
        // 认证与支付点击事件
        layoutIdentity.setOnClickListener(v -> {
            Intent intent = new Intent(this, IdentityVerificationActivity.class);
            startActivity(intent);
        });
        
        layoutPayment.setOnClickListener(v -> {
            Intent intent = new Intent(this, PaymentSettingsActivity.class);
            startActivity(intent);
        });
    }
    

    
    private void loadSettings() {
        // 从SharedPreferences加载设置
        android.content.SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        
        switchNotifications.setChecked(prefs.getBoolean("notifications_enabled", true));
        switchDarkMode.setChecked(prefs.getBoolean("dark_mode_enabled", false));
        switchAutoUpdate.setChecked(prefs.getBoolean("auto_update_enabled", true));
        
        // 设置版本信息
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            tvVersionName.setText("版本 " + versionName);
        } catch (Exception e) {
            tvVersionName.setText("版本 1.0.0");
        }
    }
    
    private void saveNotificationSetting(boolean enabled) {
        android.content.SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        prefs.edit().putBoolean("notifications_enabled", enabled).apply();
    }
    
    private void saveDarkModeSetting(boolean enabled) {
        android.content.SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        prefs.edit().putBoolean("dark_mode_enabled", enabled).apply();
        // 这里可以添加切换主题的逻辑
    }
    
    private void saveAutoUpdateSetting(boolean enabled) {
        android.content.SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        prefs.edit().putBoolean("auto_update_enabled", enabled).apply();
    }
    
    private void showAboutDialog() {
        // 创建自定义视图
        android.view.LayoutInflater inflater = getLayoutInflater();
        
        // 创建主容器
        android.widget.LinearLayout mainLayout = new android.widget.LinearLayout(this);
        mainLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        mainLayout.setPadding(60, 40, 60, 40);
        mainLayout.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
        
        // 添加AI医疗助手图标
        android.widget.ImageView logoIcon = new android.widget.ImageView(this);
        logoIcon.setImageResource(android.R.drawable.ic_dialog_info); // 使用系统图标
        android.widget.LinearLayout.LayoutParams logoParams = new android.widget.LinearLayout.LayoutParams(
            120, 120);
        logoParams.gravity = android.view.Gravity.CENTER;
        logoParams.setMargins(0, 0, 0, 24);
        logoIcon.setLayoutParams(logoParams);
        logoIcon.setColorFilter(android.graphics.Color.parseColor("#2196F3"));
        mainLayout.addView(logoIcon);
        
        // 标题
        android.widget.TextView titleView = new android.widget.TextView(this);
        titleView.setText("🤖 AI医疗助手");
        titleView.setTextSize(22);
        titleView.setTextColor(android.graphics.Color.parseColor("#2196F3"));
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setGravity(android.view.Gravity.CENTER);
        android.widget.LinearLayout.LayoutParams titleParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, 24);
        titleView.setLayoutParams(titleParams);
        mainLayout.addView(titleView);
        
        // 版本信息
        android.widget.TextView versionView = new android.widget.TextView(this);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            versionView.setText("版本 " + versionName);
        } catch (Exception e) {
            versionView.setText("版本 1.0.0");
        }
        versionView.setTextSize(14);
        versionView.setTextColor(android.graphics.Color.parseColor("#666666"));
        versionView.setGravity(android.view.Gravity.CENTER);
        android.widget.LinearLayout.LayoutParams versionParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        versionParams.setMargins(0, 0, 0, 32);
        versionView.setLayoutParams(versionParams);
        mainLayout.addView(versionView);
        
        // 分隔线
        android.view.View divider = new android.view.View(this);
        divider.setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"));
        android.widget.LinearLayout.LayoutParams dividerParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 2);
        dividerParams.setMargins(0, 0, 0, 24);
        divider.setLayoutParams(dividerParams);
        mainLayout.addView(divider);
        
        // 应用描述
        android.widget.TextView descView = new android.widget.TextView(this);
        descView.setText("💊 智能医疗服务应用\n\n✨ 主要功能：\n• 🏥 预约挂号服务\n• 📋 AI处方分析\n• 📊 健康数据管理\n• 🔍 医学影像识别\n• 💬 在线医疗咨询");
        descView.setTextSize(15);
        descView.setTextColor(android.graphics.Color.parseColor("#333333"));
        descView.setLineSpacing(8, 1.2f);
        android.widget.LinearLayout.LayoutParams descParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        descParams.setMargins(0, 0, 0, 24);
        descView.setLayoutParams(descParams);
        mainLayout.addView(descView);
        
        // 开发团队信息
        android.widget.TextView teamView = new android.widget.TextView(this);
        teamView.setText("👨‍💻 开发团队：稳行科技\n📧 联系邮箱：support@wenxing.com\n🌐 官方网站：www.wenxing.com");
        teamView.setTextSize(13);
        teamView.setTextColor(android.graphics.Color.parseColor("#666666"));
        teamView.setLineSpacing(6, 1.1f);
        android.widget.LinearLayout.LayoutParams teamParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        teamView.setLayoutParams(teamParams);
        mainLayout.addView(teamView);
        
        // 创建美化的AlertDialog
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(mainLayout)
            .setPositiveButton("✨ 太棒了", (dialogInterface, which) -> {
                dialogInterface.dismiss();
            })
            .setNeutralButton("💌 联系我们", (dialogInterface, which) -> {
                // 打开邮件应用
                android.content.Intent emailIntent = new android.content.Intent(android.content.Intent.ACTION_SENDTO);
                emailIntent.setData(android.net.Uri.parse("mailto:support@wenxing.com"));
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "AI医疗助手 - 用户反馈");
                try {
                    startActivity(emailIntent);
                } catch (Exception e) {
                    android.widget.Toast.makeText(this, "未找到邮件应用", android.widget.Toast.LENGTH_SHORT).show();
                }
            })
            .setCancelable(true)
            .create();
            
        // 显示对话框
        dialog.show();
        
        // 美化按钮样式
        android.widget.Button positiveButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
        android.widget.Button neutralButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL);
        
        if (positiveButton != null) {
            positiveButton.setTextColor(android.graphics.Color.parseColor("#2196F3"));
            positiveButton.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        
        if (neutralButton != null) {
            neutralButton.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
        }
        
        // 设置对话框窗口样式
        if (dialog.getWindow() != null) {
            android.view.Window window = dialog.getWindow();
            window.setBackgroundDrawableResource(android.R.drawable.dialog_holo_light_frame);
            // 添加进入动画
            window.getAttributes().windowAnimations = android.R.style.Animation_Dialog;
        }
    }
    
    private void showPrivacyPolicy() {
        // 创建自定义视图
        android.widget.LinearLayout mainLayout = new android.widget.LinearLayout(this);
        mainLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        mainLayout.setPadding(60, 40, 60, 40);
        mainLayout.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
        
        // 添加隐私保护图标
        android.widget.ImageView privacyIcon = new android.widget.ImageView(this);
        privacyIcon.setImageResource(android.R.drawable.ic_secure); // 使用系统安全图标
        android.widget.LinearLayout.LayoutParams iconParams = new android.widget.LinearLayout.LayoutParams(
            120, 120);
        iconParams.gravity = android.view.Gravity.CENTER;
        iconParams.setMargins(0, 0, 0, 24);
        privacyIcon.setLayoutParams(iconParams);
        privacyIcon.setColorFilter(android.graphics.Color.parseColor("#4CAF50"));
        mainLayout.addView(privacyIcon);
        
        // 标题
        android.widget.TextView titleView = new android.widget.TextView(this);
        titleView.setText("🔒 隐私政策");
        titleView.setTextSize(22);
        titleView.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setGravity(android.view.Gravity.CENTER);
        android.widget.LinearLayout.LayoutParams titleParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, 24);
        titleView.setLayoutParams(titleParams);
        mainLayout.addView(titleView);
        
        // 分隔线
        android.view.View divider = new android.view.View(this);
        divider.setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"));
        android.widget.LinearLayout.LayoutParams dividerParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 2);
        dividerParams.setMargins(0, 0, 0, 24);
        divider.setLayoutParams(dividerParams);
        mainLayout.addView(divider);
        
        // 隐私承诺
        android.widget.TextView commitmentView = new android.widget.TextView(this);
        commitmentView.setText("🛡️ 我们的隐私承诺\n\n我们深知隐私保护的重要性，承诺为您提供最安全的医疗服务体验。");
        commitmentView.setTextSize(15);
        commitmentView.setTextColor(android.graphics.Color.parseColor("#2196F3"));
        commitmentView.setLineSpacing(6, 1.2f);
        commitmentView.setTypeface(null, android.graphics.Typeface.BOLD);
        android.widget.LinearLayout.LayoutParams commitmentParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        commitmentParams.setMargins(0, 0, 0, 20);
        commitmentView.setLayoutParams(commitmentParams);
        mainLayout.addView(commitmentView);
        
        // 数据收集说明
        android.widget.TextView dataCollectionView = new android.widget.TextView(this);
        dataCollectionView.setText("📊 数据收集与使用\n\n• 🏥 医疗信息：仅用于提供诊断和治疗建议\n• 👤 个人资料：用于身份验证和账户管理\n• 📱 设备信息：用于应用性能优化\n• 💊 处方记录：用于健康档案管理");
        dataCollectionView.setTextSize(14);
        dataCollectionView.setTextColor(android.graphics.Color.parseColor("#333333"));
        dataCollectionView.setLineSpacing(8, 1.2f);
        android.widget.LinearLayout.LayoutParams dataParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        dataParams.setMargins(0, 0, 0, 20);
        dataCollectionView.setLayoutParams(dataParams);
        mainLayout.addView(dataCollectionView);
        
        // 安全保障
        android.widget.TextView securityView = new android.widget.TextView(this);
        securityView.setText("🔐 安全保障措施\n\n• 🔒 端到端加密传输\n• 🏛️ 符合国家医疗数据安全标准\n• 🚫 绝不向第三方泄露个人信息\n• 🗑️ 支持数据删除和导出");
        securityView.setTextSize(14);
        securityView.setTextColor(android.graphics.Color.parseColor("#333333"));
        securityView.setLineSpacing(8, 1.2f);
        android.widget.LinearLayout.LayoutParams securityParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        securityParams.setMargins(0, 0, 0, 20);
        securityView.setLayoutParams(securityParams);
        mainLayout.addView(securityView);
        
        // 联系信息
        android.widget.TextView contactView = new android.widget.TextView(this);
        contactView.setText("📞 如有隐私相关问题，请联系：\n\n📧 隐私专员：privacy@wenxing.com\n⏰ 工作时间：周一至周五 9:00-18:00");
        contactView.setTextSize(13);
        contactView.setTextColor(android.graphics.Color.parseColor("#666666"));
        contactView.setLineSpacing(6, 1.1f);
        android.widget.LinearLayout.LayoutParams contactParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        contactView.setLayoutParams(contactParams);
        mainLayout.addView(contactView);
        
        // 创建美化的AlertDialog
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(mainLayout)
            .setPositiveButton("✅ 我已了解", (dialogInterface, which) -> {
                dialogInterface.dismiss();
            })
            .setNeutralButton("📧 联系隐私专员", (dialogInterface, which) -> {
                // 打开邮件应用
                android.content.Intent emailIntent = new android.content.Intent(android.content.Intent.ACTION_SENDTO);
                emailIntent.setData(android.net.Uri.parse("mailto:privacy@wenxing.com"));
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "AI医疗助手 - 隐私政策咨询");
                try {
                    startActivity(emailIntent);
                } catch (Exception e) {
                    android.widget.Toast.makeText(this, "未找到邮件应用", android.widget.Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("📖 查看完整版", (dialogInterface, which) -> {
                // 打开完整隐私政策网页
                android.content.Intent webIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                webIntent.setData(android.net.Uri.parse("https://www.wenxing.com/privacy"));
                try {
                    startActivity(webIntent);
                } catch (Exception e) {
                    android.widget.Toast.makeText(this, "无法打开网页", android.widget.Toast.LENGTH_SHORT).show();
                }
            })
            .setCancelable(true)
            .create();
            
        // 显示对话框
        dialog.show();
        
        // 美化按钮样式
        android.widget.Button positiveButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
        android.widget.Button neutralButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL);
        android.widget.Button negativeButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE);
        
        if (positiveButton != null) {
            positiveButton.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            positiveButton.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        
        if (neutralButton != null) {
            neutralButton.setTextColor(android.graphics.Color.parseColor("#FF9800"));
        }
        
        if (negativeButton != null) {
            negativeButton.setTextColor(android.graphics.Color.parseColor("#2196F3"));
        }
        
        // 设置对话框窗口样式
        if (dialog.getWindow() != null) {
            android.view.Window window = dialog.getWindow();
            window.setBackgroundDrawableResource(android.R.drawable.dialog_holo_light_frame);
            // 添加进入动画
            window.getAttributes().windowAnimations = android.R.style.Animation_Dialog;
        }
    }
    
    private void showHelpDialog() {
        // 创建自定义视图
        android.widget.LinearLayout mainLayout = new android.widget.LinearLayout(this);
        mainLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        mainLayout.setPadding(60, 40, 60, 40);
        mainLayout.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
        
        // 添加帮助图标
        android.widget.ImageView helpIcon = new android.widget.ImageView(this);
        helpIcon.setImageResource(android.R.drawable.ic_dialog_info);
        android.widget.LinearLayout.LayoutParams iconParams = new android.widget.LinearLayout.LayoutParams(
            120, 120);
        iconParams.gravity = android.view.Gravity.CENTER;
        iconParams.setMargins(0, 0, 0, 24);
        helpIcon.setLayoutParams(iconParams);
        helpIcon.setColorFilter(android.graphics.Color.parseColor("#FF9800"));
        mainLayout.addView(helpIcon);
        
        // 标题
        android.widget.TextView titleView = new android.widget.TextView(this);
        titleView.setText("🤝 帮助与反馈");
        titleView.setTextSize(22);
        titleView.setTextColor(android.graphics.Color.parseColor("#FF9800"));
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setGravity(android.view.Gravity.CENTER);
        android.widget.LinearLayout.LayoutParams titleParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, 24);
        titleView.setLayoutParams(titleParams);
        mainLayout.addView(titleView);
        
        // 分隔线
        android.view.View divider = new android.view.View(this);
        divider.setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"));
        android.widget.LinearLayout.LayoutParams dividerParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 2);
        dividerParams.setMargins(0, 0, 0, 24);
        divider.setLayoutParams(dividerParams);
        mainLayout.addView(divider);
        
        // 欢迎信息
        android.widget.TextView welcomeView = new android.widget.TextView(this);
        welcomeView.setText("💝 我们随时为您提供帮助\n\n遇到问题不要担心，我们的专业团队会及时为您解决各种使用问题。");
        welcomeView.setTextSize(15);
        welcomeView.setTextColor(android.graphics.Color.parseColor("#FF9800"));
        welcomeView.setLineSpacing(6, 1.2f);
        welcomeView.setTypeface(null, android.graphics.Typeface.BOLD);
        android.widget.LinearLayout.LayoutParams welcomeParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        welcomeParams.setMargins(0, 0, 0, 20);
        welcomeView.setLayoutParams(welcomeParams);
        mainLayout.addView(welcomeView);
        
        // 联系方式
        android.widget.TextView contactMethodsView = new android.widget.TextView(this);
        contactMethodsView.setText("📞 联系方式\n\n🔥 客服热线：400-123-4567\n   • 专业医疗咨询支持\n   • 技术问题快速解决\n   • 24小时紧急服务\n\n📧 客服邮箱：help@wenteng.com\n   • 详细问题描述\n   • 功能建议反馈\n   • 投诉与建议");
        contactMethodsView.setTextSize(14);
        contactMethodsView.setTextColor(android.graphics.Color.parseColor("#333333"));
        contactMethodsView.setLineSpacing(8, 1.2f);
        android.widget.LinearLayout.LayoutParams methodsParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        methodsParams.setMargins(0, 0, 0, 20);
        contactMethodsView.setLayoutParams(methodsParams);
        mainLayout.addView(contactMethodsView);
        
        // 服务时间
        android.widget.TextView serviceTimeView = new android.widget.TextView(this);
        serviceTimeView.setText("⏰ 服务时间\n\n🌅 周一至周五：9:00 - 18:00\n🌙 周六至周日：10:00 - 16:00\n\n💡 温馨提示：\n• 工作时间内回复更快\n• 紧急问题请拨打热线\n• 节假日可能延迟回复");
        serviceTimeView.setTextSize(14);
        serviceTimeView.setTextColor(android.graphics.Color.parseColor("#333333"));
        serviceTimeView.setLineSpacing(8, 1.2f);
        android.widget.LinearLayout.LayoutParams timeParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        timeParams.setMargins(0, 0, 0, 20);
        serviceTimeView.setLayoutParams(timeParams);
        mainLayout.addView(serviceTimeView);
        
        // 常见问题提示
        android.widget.TextView faqView = new android.widget.TextView(this);
        faqView.setText("❓ 常见问题\n\n• 登录问题：检查网络连接\n• 支付问题：确认账户余额\n• 功能异常：重启应用\n• 更多FAQ请查看官网");
        faqView.setTextSize(13);
        faqView.setTextColor(android.graphics.Color.parseColor("#666666"));
        faqView.setLineSpacing(6, 1.1f);
        android.widget.LinearLayout.LayoutParams faqParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        faqView.setLayoutParams(faqParams);
        mainLayout.addView(faqView);
        
        // 创建美化的AlertDialog
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(mainLayout)
            .setPositiveButton("🎯 立即联系", (dialogInterface, which) -> {
                // 显示联系方式选择
                showContactOptionsDialog();
            })
            .setNeutralButton("📧 发送邮件", (dialogInterface, which) -> {
                // 打开邮件应用
                android.content.Intent emailIntent = new android.content.Intent(android.content.Intent.ACTION_SENDTO);
                emailIntent.setData(android.net.Uri.parse("mailto:help@wenteng.com"));
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "AI医疗助手 - 用户反馈");
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "请在此描述您遇到的问题或建议...\n\n=== 设备信息 ===\n系统版本：Android\n应用版本：1.0.0\n");
                try {
                    startActivity(emailIntent);
                } catch (Exception e) {
                    android.widget.Toast.makeText(this, "未找到邮件应用", android.widget.Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("📱 拨打热线", (dialogInterface, which) -> {
                // 拨打客服电话
                android.content.Intent callIntent = new android.content.Intent(android.content.Intent.ACTION_DIAL);
                callIntent.setData(android.net.Uri.parse("tel:400-123-4567"));
                try {
                    startActivity(callIntent);
                } catch (Exception e) {
                    android.widget.Toast.makeText(this, "无法拨打电话", android.widget.Toast.LENGTH_SHORT).show();
                }
            })
            .setCancelable(true)
            .create();
            
        // 显示对话框
        dialog.show();
        
        // 美化按钮样式
        android.widget.Button positiveButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
        android.widget.Button neutralButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL);
        android.widget.Button negativeButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE);
        
        if (positiveButton != null) {
            positiveButton.setTextColor(android.graphics.Color.parseColor("#FF9800"));
            positiveButton.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        
        if (neutralButton != null) {
            neutralButton.setTextColor(android.graphics.Color.parseColor("#2196F3"));
        }
        
        if (negativeButton != null) {
            negativeButton.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
        }
        
        // 设置对话框窗口样式
        if (dialog.getWindow() != null) {
            android.view.Window window = dialog.getWindow();
            window.setBackgroundDrawableResource(android.R.drawable.dialog_holo_light_frame);
            // 添加进入动画
            window.getAttributes().windowAnimations = android.R.style.Animation_Dialog;
        }
    }
    
    /**
     * 显示联系方式选择对话框
     */
    private void showContactOptionsDialog() {
        String[] options = {"📱 拨打客服热线", "📧 发送邮件反馈", "🌐 访问官方网站", "💬 在线客服(即将开放)"};
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("选择联系方式")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // 拨打电话
                        android.content.Intent callIntent = new android.content.Intent(android.content.Intent.ACTION_DIAL);
                        callIntent.setData(android.net.Uri.parse("tel:400-123-4567"));
                        try {
                            startActivity(callIntent);
                        } catch (Exception e) {
                            android.widget.Toast.makeText(this, "无法拨打电话", android.widget.Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 1: // 发送邮件
                        android.content.Intent emailIntent = new android.content.Intent(android.content.Intent.ACTION_SENDTO);
                        emailIntent.setData(android.net.Uri.parse("mailto:help@wenteng.com"));
                        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "AI医疗助手 - 用户反馈");
                        try {
                            startActivity(emailIntent);
                        } catch (Exception e) {
                            android.widget.Toast.makeText(this, "未找到邮件应用", android.widget.Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 2: // 访问网站
                        android.content.Intent webIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                        webIntent.setData(android.net.Uri.parse("https://www.wenteng.com/help"));
                        try {
                            startActivity(webIntent);
                        } catch (Exception e) {
                            android.widget.Toast.makeText(this, "无法打开网页", android.widget.Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 3: // 在线客服
                        android.widget.Toast.makeText(this, "在线客服功能即将开放，敬请期待！", android.widget.Toast.LENGTH_LONG).show();
                        break;
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}