package com.wenteng.frontend_android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wenteng.frontend_android.api.ApiClient;
import com.wenteng.frontend_android.api.ApiResponse;
import com.wenteng.frontend_android.model.PaymentOrderRequest;
import com.wenteng.frontend_android.model.PaymentOrderResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.appcompat.app.AppCompatActivity;

import com.alipay.sdk.app.PayTask;
import com.wenteng.frontend_android.R;
import com.wenteng.frontend_android.model.Product;

import java.text.SimpleDateFormat;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    private static final int SDK_PAY_FLAG = 1;
    
    private ImageView productImage;
    private TextView productTitle;
    private TextView productPrice;
    private TextView productSpecification;
    private TextView productManufacturer;
    private TextView productPurchaseCount;
    private TextView productDetail;
    private Product currentProduct;
    
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    @SuppressWarnings("unchecked")
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    /**
                     * 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        onPaymentSuccess("支付宝支付");
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        Toast.makeText(ProductDetailActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                default:
                    break;
            }
        };
    };

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
        findViewById(R.id.btn_share).setOnClickListener(v -> {
            shareProduct();
        });

        // 购买按钮点击事件
        findViewById(R.id.btn_buy).setOnClickListener(v -> {
            purchaseProduct();
        });
    }

    private void initData() {
        // 获取从上一个页面传递的商品对象
        currentProduct = (Product) getIntent().getSerializableExtra("product");
        if (currentProduct != null) {
            productTitle.setText(currentProduct.getName());
            productPrice.setText(String.format("¥%.2f", currentProduct.getPrice()));
            
            // 使用从后端获取的实际数据
            productSpecification.setText(currentProduct.getSpecification() != null ? currentProduct.getSpecification() : "标准规格");
            productManufacturer.setText(currentProduct.getManufacturer() != null ? currentProduct.getManufacturer() : "健康医药有限公司");
            productPurchaseCount.setText(currentProduct.getPurchaseCount() + "+ 人已购");
            productDetail.setText(currentProduct.getDescription() + "\n\n这是商品的详细描述信息，包含了商品的使用方法、注意事项等。");

            // 这里可以使用图片加载库加载网络图片，例如Glide或Picasso
            // Glide.with(this).load(currentProduct.getImageUrl()).into(productImage);
        }
    }

    /**
     * 分享商品功能
     */
    private void shareProduct() {
        if (currentProduct == null) {
            Toast.makeText(this, "商品信息不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        // 构建分享内容
        String shareTitle = "推荐一个好商品";
        String shareContent = String.format(
            "商品名称：%s\n" +
            "价格：¥%.2f\n" +
            "规格：%s\n" +
            "厂商：%s\n" +
            "描述：%s\n\n" +
            "来自AI医疗助手，专业的医疗健康平台",
            currentProduct.getName(),
            currentProduct.getPrice(),
            currentProduct.getSpecification() != null ? currentProduct.getSpecification() : "标准规格",
            currentProduct.getManufacturer() != null ? currentProduct.getManufacturer() : "健康医药有限公司",
            currentProduct.getDescription()
        );

        // 创建分享Intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareTitle);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);

        // 创建选择器
        Intent chooser = Intent.createChooser(shareIntent, "分享商品到");
        
        // 检查是否有应用可以处理分享Intent
        if (shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        } else {
             Toast.makeText(this, "没有找到可用的分享应用", Toast.LENGTH_SHORT).show();
         }
     }

     /**
      * 购买商品功能
      */
     private void purchaseProduct() {
         if (currentProduct == null) {
             Toast.makeText(this, "商品信息不存在", Toast.LENGTH_SHORT).show();
             return;
         }

         // 显示支付方式选择对话框
         showPaymentMethodDialog();
     }

     /**
     * 显示支付方式选择对话框
     */
    private void showPaymentMethodDialog() {
        // 创建自定义对话框
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        
        // 加载自定义布局
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_payment_method, null);
        builder.setView(dialogView);
        
        // 设置商品信息
        TextView tvProductName = dialogView.findViewById(R.id.tv_product_name);
        TextView tvProductPrice = dialogView.findViewById(R.id.tv_product_price);
        
        tvProductName.setText("商品：" + currentProduct.getName());
        tvProductPrice.setText(String.format("¥%.2f", currentProduct.getPrice()));
        
        // 创建对话框
        android.app.AlertDialog dialog = builder.create();
        
        // 设置对话框背景透明，让自定义圆角生效
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        // 支付宝支付点击事件
        LinearLayout layoutAlipay = dialogView.findViewById(R.id.layout_alipay);
        layoutAlipay.setOnClickListener(v -> {
            dialog.dismiss();
            confirmPurchase("支付宝支付");
        });
        
        // 微信支付点击事件
        LinearLayout layoutWechat = dialogView.findViewById(R.id.layout_wechat);
        layoutWechat.setOnClickListener(v -> {
            dialog.dismiss();
            confirmPurchase("微信支付");
        });
        
        // 取消按钮点击事件
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

     /**
      * 确认购买对话框
      */
     private void confirmPurchase(String paymentMethod) {
         android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
         builder.setTitle("确认购买");
         builder.setMessage(String.format(
             "商品：%s\n价格：¥%.2f\n支付方式：%s\n\n确定要购买这个商品吗？",
             currentProduct.getName(),
             currentProduct.getPrice(),
             paymentMethod
         ));

         builder.setPositiveButton("确认支付", (dialog, which) -> {
             // 执行支付逻辑
             processPurchase(paymentMethod);
         });

         builder.setNegativeButton("取消", (dialog, which) -> {
             dialog.dismiss();
         });

         builder.show();
     }

     /**
      * 处理购买逻辑
      */
     private void processPurchase(String paymentMethod) {
         // 显示支付处理中的提示
         Toast.makeText(this, "正在跳转到" + paymentMethod + "...", Toast.LENGTH_SHORT).show();

         // 根据支付方式处理不同的支付逻辑
         if ("支付宝支付".equals(paymentMethod)) {
             processAlipayPayment();
         } else if ("微信支付".equals(paymentMethod)) {
             processWechatPayment();
         }
     }

     /**
      * 处理支付宝支付
      */
     private void processAlipayPayment() {
         Toast.makeText(this, "正在启动支付宝支付...", Toast.LENGTH_SHORT).show();
         
         // 显示加载对话框
         android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
         progressDialog.setMessage("正在创建订单...");
         progressDialog.setCancelable(true); // 允许用户取消
         progressDialog.setOnCancelListener(dialog -> {
             Toast.makeText(this, "订单创建已取消", Toast.LENGTH_SHORT).show();
         });
         progressDialog.show();
         
         // 设置超时处理
         android.os.Handler timeoutHandler = new android.os.Handler();
         Runnable timeoutRunnable = () -> {
             if (progressDialog.isShowing()) {
                 progressDialog.dismiss();
                 Toast.makeText(this, "订单创建超时，请重试", Toast.LENGTH_LONG).show();
             }
         };
         timeoutHandler.postDelayed(timeoutRunnable, 30000); // 30秒超时
         
         // 从服务器获取订单信息
         getOrderInfoFromServer(1, new OrderInfoCallback() { // 默认数量为1
             @Override
             public void onSuccess(String orderString, PaymentOrderResponse.OrderInfo orderInfo) {
                 // 取消超时处理
                 timeoutHandler.removeCallbacks(timeoutRunnable);
                 // 关闭加载对话框
                 if (progressDialog.isShowing()) {
                     progressDialog.dismiss();
                 }
                 
                 // 执行支付宝支付
                 Runnable payRunnable = new Runnable() {
                     @Override
                     public void run() {
                         PayTask alipay = new PayTask(ProductDetailActivity.this);
                         Map<String, String> result = alipay.payV2(orderString, true);
                         
                         Message msg = new Message();
                         msg.what = SDK_PAY_FLAG;
                         msg.obj = result;
                         mHandler.sendMessage(msg);
                     }
                 };
                 
                 // 必须异步调用
                 Thread payThread = new Thread(payRunnable);
                 payThread.start();
             }
             
             @Override
             public void onError(String errorMessage) {
                 // 取消超时处理
                 timeoutHandler.removeCallbacks(timeoutRunnable);
                 // 关闭加载对话框
                 if (progressDialog.isShowing()) {
                     progressDialog.dismiss();
                 }
                 
                 // 显示错误信息
                 runOnUiThread(() -> {
                     Toast.makeText(ProductDetailActivity.this, "订单创建失败: " + errorMessage, Toast.LENGTH_LONG).show();
                 });
             }
         });
     }

     /**
       * 从服务器获取支付订单信息
       */
      private void getOrderInfoFromServer(int quantity, OrderInfoCallback callback) {
          // 创建请求对象
          PaymentOrderRequest request = new PaymentOrderRequest(
              currentProduct.getId(),
              quantity,
              "30m"
          );
          
          android.util.Log.d("ProductDetail", "开始创建订单，商品ID: " + currentProduct.getId() + ", 数量: " + quantity);
          
          // 调用服务器API创建订单
          Call<ApiResponse<PaymentOrderResponse>> call = ApiClient.getApiService().createAlipayOrder(request);
          
          call.enqueue(new Callback<ApiResponse<PaymentOrderResponse>>() {
              @Override
              public void onResponse(Call<ApiResponse<PaymentOrderResponse>> call, Response<ApiResponse<PaymentOrderResponse>> response) {
                  android.util.Log.d("ProductDetail", "收到服务器响应，状态码: " + response.code());
                  if (response.isSuccessful() && response.body() != null) {
                      ApiResponse<PaymentOrderResponse> apiResponse = response.body();
                      if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                          PaymentOrderResponse orderResponse = apiResponse.getData();
                          if (orderResponse.isSuccess() && orderResponse.getOrderString() != null) {
                              android.util.Log.d("ProductDetail", "订单创建成功");
                              callback.onSuccess(orderResponse.getOrderString(), orderResponse.getOrderInfo());
                          } else {
                              android.util.Log.e("ProductDetail", "订单创建失败: " + orderResponse.getMessage());
                              callback.onError("订单创建失败: " + orderResponse.getMessage());
                          }
                      } else {
                          android.util.Log.e("ProductDetail", "API调用失败: " + apiResponse.getMessage());
                          callback.onError("API调用失败: " + apiResponse.getMessage());
                      }
                  } else {
                      android.util.Log.e("ProductDetail", "网络请求失败: " + response.message());
                      callback.onError("网络请求失败: " + response.message());
                  }
              }
              
              @Override
              public void onFailure(Call<ApiResponse<PaymentOrderResponse>> call, Throwable t) {
                  android.util.Log.e("ProductDetail", "网络连接失败: " + t.getMessage(), t);
                  callback.onError("网络连接失败: " + t.getMessage());
              }
          });
      }
      
      /**
       * 订单信息回调接口
       */
      private interface OrderInfoCallback {
          void onSuccess(String orderString, PaymentOrderResponse.OrderInfo orderInfo);
          void onError(String errorMessage);
      }

     /**
      * 支付结果处理类
      */
     public static class PayResult {
         private String resultStatus;
         private String result;
         private String memo;

         public PayResult(Map<String, String> rawResult) {
             if (rawResult == null) {
                 return;
             }

             for (String key : rawResult.keySet()) {
                 if (TextUtils.equals(key, "resultStatus")) {
                     resultStatus = rawResult.get(key);
                 } else if (TextUtils.equals(key, "result")) {
                     result = rawResult.get(key);
                 } else if (TextUtils.equals(key, "memo")) {
                     memo = rawResult.get(key);
                 }
             }
         }

         @Override
         public String toString() {
             return "resultStatus={" + resultStatus + "};memo={" + memo
                     + "};result={" + result + "}";
         }

         /**
          * @return the resultStatus
          */
         public String getResultStatus() {
             return resultStatus;
         }

         /**
          * @return the memo
          */
         public String getMemo() {
             return memo;
         }

         /**
          * @return the result
          */
         public String getResult() {
             return result;
         }
     }

     /**
      * 处理微信支付
      */
     private void processWechatPayment() {
         // 这里可以集成微信支付SDK进行实际支付
         // 目前模拟支付流程
         
         // 模拟微信支付处理时间
         new android.os.Handler().postDelayed(() -> {
             // 模拟支付成功
             onPaymentSuccess("微信支付");
         }, 2000); // 模拟2秒的支付处理时间
     }

     /**
      * 支付成功回调
      */
     private void onPaymentSuccess(String paymentMethod) {
         // 更新购买数量显示
         int newPurchaseCount = currentProduct.getPurchaseCount() + 1;
         currentProduct.setPurchaseCount(newPurchaseCount);
         productPurchaseCount.setText(newPurchaseCount + "+ 人已购");

         // 显示支付成功提示
         Toast.makeText(this, paymentMethod + "支付成功！感谢您的购买", Toast.LENGTH_LONG).show();

         // 这里可以添加实际的后续操作：
         // 1. 调用后端API记录订单
         // 2. 更新库存
         // 3. 发送购买成功通知
         // 4. 跳转到订单详情页面
         // Intent intent = new Intent(this, OrderActivity.class);
         // intent.putExtra("order_id", orderId);
         // intent.putExtra("payment_method", paymentMethod);
         // startActivity(intent);
     }
}