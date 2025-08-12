package com.wenteng.frontend_android;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wenteng.frontend_android.R;
import com.wenteng.frontend_android.model.Product;

import java.text.SimpleDateFormat;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView productImage;
    private TextView productTitle;
    private TextView productPrice;
    private TextView productSpecification;
    private TextView productManufacturer;
    private TextView productPurchaseCount;
    private TextView productDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initViews();
        initData();
    }

    private void initViews() {
        productImage = findViewById(R.id.product_image);
        productTitle = findViewById(R.id.product_title);
        productPrice = findViewById(R.id.product_price);
        productSpecification = findViewById(R.id.product_specification);
        productManufacturer = findViewById(R.id.product_manufacturer);
        productPurchaseCount = findViewById(R.id.product_purchase_count);
        productDetail = findViewById(R.id.product_detail);

        // 分享按钮点击事件
        findViewById(R.id.share_button).setOnClickListener(v -> {
            Toast.makeText(ProductDetailActivity.this, "分享功能开发中", Toast.LENGTH_SHORT).show();
        });

        // 购买按钮点击事件
        findViewById(R.id.buy_button).setOnClickListener(v -> {
            Toast.makeText(ProductDetailActivity.this, "购买功能开发中", Toast.LENGTH_SHORT).show();
        });
    }

    private void initData() {
        // 获取从上一个页面传递的商品对象
        Product product = (Product) getIntent().getSerializableExtra("product");
        if (product != null) {
            productTitle.setText(product.getName());
            productPrice.setText(String.format("¥%.2f", product.getPrice()));
            
            // 使用从后端获取的实际数据
            productSpecification.setText(product.getSpecification() != null ? product.getSpecification() : "标准规格");
            productManufacturer.setText(product.getManufacturer() != null ? product.getManufacturer() : "健康医药有限公司");
            productPurchaseCount.setText(product.getPurchaseCount() + "+ 人已购");
            productDetail.setText(product.getDescription() + "\n\n这是商品的详细描述信息，包含了商品的使用方法、注意事项等。");

            // 这里可以使用图片加载库加载网络图片，例如Glide或Picasso
            // Glide.with(this).load(product.getImageUrl()).into(productImage);
        }
    }
}