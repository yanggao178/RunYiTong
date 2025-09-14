package com.wenxing.runyitong;

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

import com.wenxing.runyitong.api.ApiClient;
import com.wenxing.runyitong.api.ApiResponse;
import com.wenxing.runyitong.model.PaymentOrderRequest;
import com.wenxing.runyitong.model.PaymentOrderResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.appcompat.app.AppCompatActivity;

import com.alipay.sdk.app.PayTask;
import com.wenxing.runyitong.R;
import com.wenxing.runyitong.model.Product;

// 微信支付SDK导入
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    private static final int SDK_PAY_FLAG = 1;
    private static final int WECHAT_PAY_REQUEST_CODE = 2;
    
    // 微信支付API
    private IWXAPI wxApi;
    
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
                    String memo = payResult.getMemo();
                    
                    android.util.Log.d("AlipayResult", "支付结果: " + payResult.toString());
                    
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        onPaymentSuccess("支付宝支付");
                    } else {
                        // 显示详细的错误信息
                        String errorMessage = "支付失败";
                        if (!TextUtils.isEmpty(memo)) {
                            errorMessage = memo;
                        } else if (TextUtils.equals(resultStatus, "4000")) {
                            errorMessage = "订单支付失败";
                        } else if (TextUtils.equals(resultStatus, "6001")) {
                            errorMessage = "用户中途取消";
                        } else if (TextUtils.equals(resultStatus, "6002")) {
                            errorMessage = "网络连接出错";
                        } else if (TextUtils.equals(resultStatus, "8000")) {
                            errorMessage = "支付结果因为支付渠道原因或者系统原因还在处理中";
                        }
                        
                        Toast.makeText(ProductDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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
        
        // 初始化微信API
        initWechatAPI();
        
        initViews();
        initData();
    }
    
    /**
     * 初始化微信API
     */
    private void initWechatAPI() {
        // 微信AppID，需要与后端配置和WXPayEntryActivity保持一致
        String wxAppId = "wx1234567890abcdef";
        wxApi = WXAPIFactory.createWXAPI(this, wxAppId, true);
        wxApi.registerApp(wxAppId);
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
            // Glide.with(this).load(currentProduct.getFeaturedImageFile()).into(productImage);
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
       * 微信支付订单信息回调接口
       */
      private interface WechatOrderInfoCallback {
          void onSuccess(PaymentOrderResponse.OrderInfo orderInfo);
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
         Toast.makeText(this, "正在启动微信支付...", Toast.LENGTH_SHORT).show();
         
         // 显示加载对话框
         android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
         progressDialog.setMessage("正在创建微信订单...");
         progressDialog.setCancelable(true);
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
         
         // 从服务器获取微信订单信息
         getWechatOrderInfoFromServer(1, new WechatOrderInfoCallback() {
             @Override
             public void onSuccess(PaymentOrderResponse.OrderInfo orderInfo) {
                 // 取消超时处理
                 timeoutHandler.removeCallbacks(timeoutRunnable);
                 // 关闭加载对话框
                 if (progressDialog.isShowing()) {
                     progressDialog.dismiss();
                 }
                 
                 // 启动微信支付
                 startWechatPay(orderInfo);
             }
             
             @Override
             public void onError(String errorMessage) {
                 // 取消超时处理
                 timeoutHandler.removeCallbacks(timeoutRunnable);
                 // 关闭加载对话框
                 if (progressDialog.isShowing()) {
                     progressDialog.dismiss();
                 }
                 
                 Toast.makeText(ProductDetailActivity.this, "创建微信订单失败: " + errorMessage, Toast.LENGTH_LONG).show();
             }
         });
     }
     
     /**
      * 从服务器获取微信订单信息
      */
     private void getWechatOrderInfoFromServer(int quantity, WechatOrderInfoCallback callback) {
         PaymentOrderRequest request = new PaymentOrderRequest();
         request.setProductId(currentProduct.getId());
         request.setQuantity(quantity);
         
         Call<ApiResponse<PaymentOrderResponse>> call = ApiClient.getPaymentService().createWechatOrder(request);
         call.enqueue(new Callback<ApiResponse<PaymentOrderResponse>>() {
             @Override
             public void onResponse(Call<ApiResponse<PaymentOrderResponse>> call, Response<ApiResponse<PaymentOrderResponse>> response) {
                 if (response.isSuccessful() && response.body() != null) {
                     ApiResponse<PaymentOrderResponse> apiResponse = response.body();
                     if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                         PaymentOrderResponse.OrderInfo orderInfo = apiResponse.getData().getOrderInfo();
                         if (orderInfo != null) {
                             callback.onSuccess(orderInfo);
                         } else {
                             callback.onError("订单信息为空");
                         }
                     } else {
                         String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "创建订单失败";
                         callback.onError(errorMsg);
                     }
                 } else {
                     callback.onError("网络请求失败: " + response.code());
                 }
             }
             
             @Override
             public void onFailure(Call<ApiResponse<PaymentOrderResponse>> call, Throwable t) {
                 callback.onError("网络连接失败: " + t.getMessage());
             }
         });
     }
     
     /**
      * 启动微信支付
      */
     private void startWechatPay(PaymentOrderResponse.OrderInfo orderInfo) {
         if (!wxApi.isWXAppInstalled()) {
             Toast.makeText(this, "未安装微信客户端", Toast.LENGTH_SHORT).show();
             return;
         }
         
         if (wxApi.getWXAppSupportAPI() < 0x21020001) {
            Toast.makeText(this, "微信客户端版本不支持", Toast.LENGTH_SHORT).show();
            return;
        }
         
         // 获取APP支付参数
         Map<String, String> appPayParams = orderInfo.getAppPayParams();
         if (appPayParams == null) {
             Toast.makeText(this, "支付参数错误", Toast.LENGTH_SHORT).show();
             return;
         }
         
         // 构建微信支付请求
         PayReq payReq = new PayReq();
         payReq.appId = appPayParams.get("appid");
         payReq.partnerId = appPayParams.get("partnerid");
         payReq.prepayId = appPayParams.get("prepayid");
         payReq.packageValue = appPayParams.get("package");
         payReq.nonceStr = appPayParams.get("noncestr");
         payReq.timeStamp = appPayParams.get("timestamp");
         payReq.sign = appPayParams.get("sign");
         
         // 发起微信支付
         boolean result = wxApi.sendReq(payReq);
         if (!result) {
             Toast.makeText(this, "启动微信支付失败", Toast.LENGTH_SHORT).show();
         }
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         
         if (requestCode == WECHAT_PAY_REQUEST_CODE) {
             if (resultCode == RESULT_OK) {
                 // 微信支付成功
                 onPaymentSuccess("微信支付");
             } else {
                 // 微信支付失败或取消
                 Toast.makeText(this, "微信支付已取消", Toast.LENGTH_SHORT).show();
             }
         }
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