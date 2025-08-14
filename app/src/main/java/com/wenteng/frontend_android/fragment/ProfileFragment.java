package com.wenteng.frontend_android.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.wenteng.frontend_android.R;
import com.wenteng.frontend_android.activity.OrderActivity;

public class ProfileFragment extends Fragment {

    private Button btnExpressOrders;

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
    }
}