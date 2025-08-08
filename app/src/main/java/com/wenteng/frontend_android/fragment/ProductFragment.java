package com.wenteng.frontend_android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wenteng.frontend_android.R;
import com.wenteng.frontend_android.adapter.ProductAdapter;
import com.wenteng.frontend_android.model.Product;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProductFragment extends Fragment {

    private RecyclerView productRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product, container, false);
        initViews(view);
        initData();
        setupRecyclerView();
        return view;
    }

    private void initViews(View view) {
        productRecyclerView = view.findViewById(R.id.product_recycler_view);
    }

    private void initData() {
        productList = new ArrayList<>();
        // 添加测试数据
        productList.add(new Product(1, "养生茶", 98.0, "纯天然草本配方，滋阴补肾", "https://example.com/tea.jpg","保健品",100,new Date(),new Date()));
        productList.add(new Product(2, "艾灸贴", 68.0, "缓解疲劳，促进血液循环", "https://example.com/moxibustion.jpg","保健品",100,new Date(),new Date()));
        productList.add(new Product(3, "按摩仪", 199.0, "智能按摩，舒缓肌肉紧张", "https://example.com/massager.jpg","保健品",100,new Date(),new Date()));
        productList.add(new Product(4, "中药饮片", 128.0, "精选中药材，调理身体","https://example.com/herb.jpg","保健品",100,new Date(),new Date()));
        productList.add(new Product(5, "血压计", 299.0, "家用智能血压监测仪", "https://example.com/blood_pressure.jpg","保健品",100,new Date(),new Date()));
    }

    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(getContext(), productList);
        productRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        productRecyclerView.setAdapter(productAdapter);
    }
}