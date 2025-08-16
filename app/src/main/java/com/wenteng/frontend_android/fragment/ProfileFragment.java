package com.wenteng.frontend_android.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import com.wenteng.frontend_android.R;
import com.wenteng.frontend_android.activity.OrderActivity;
import com.wenteng.frontend_android.activity.SettingsActivity;

public class ProfileFragment extends Fragment {

    private Button btnExpressOrders;
    private CardView cardSettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        initViews(view);
        setupClickListeners();
        
        return view;
    }
    
    private void initViews(View view) {
        btnExpressOrders = view.findViewById(R.id.btn_express_orders);
        cardSettings = view.findViewById(R.id.card_settings);
    }
    
    private void setupClickListeners() {
        btnExpressOrders.setOnClickListener(v -> {
            try {
                Toast.makeText(getActivity(), "正在打开订单页面...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), OrderActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "打开订单页面失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        
        cardSettings.setOnClickListener(v -> {
            try {
                Toast.makeText(getActivity(), "正在打开设置页面...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "打开设置页面失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}