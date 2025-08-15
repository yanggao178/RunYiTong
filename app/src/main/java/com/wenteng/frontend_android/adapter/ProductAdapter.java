package com.wenteng.frontend_android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wenteng.frontend_android.R;
import com.wenteng.frontend_android.model.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.product_item, parent, false);
        return new ProductViewHolder(view);
    }

    private OnItemClickListener onItemClickListener;

    // 点击事件接口
    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    // 设置点击事件监听
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        if (productList == null || position < 0 || position >= productList.size()) {
            return;
        }
        
        Product product = productList.get(position);
        if (product == null) {
            return;
        }
        
        // 安全设置产品名称
        if (holder.productName != null) {
            holder.productName.setText(product.getName() != null ? product.getName() : "未知商品");
        }
        
        // 安全设置价格
        if (holder.productPrice != null) {
            try {
                holder.productPrice.setText(String.format("%.2f", product.getPrice()));
            } catch (Exception e) {
                holder.productPrice.setText("0.00");
            }
        }
        
        // 安全设置描述
        if (holder.productDescription != null) {
            holder.productDescription.setText(product.getDescription() != null ? product.getDescription() : "暂无描述");
        }

        // 暂时使用占位图
        if (holder.productImage != null) {
            holder.productImage.setImageResource(R.drawable.ic_launcher_background);
        }

        // 添加点击事件
        if (holder.itemView != null) {
            holder.itemView.setOnClickListener(v -> {
                if (onItemClickListener != null && product != null) {
                    onItemClickListener.onItemClick(product);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productPrice;
        TextView productDescription;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            productDescription = itemView.findViewById(R.id.product_description);
        }
    }
}