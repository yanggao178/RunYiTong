package com.wenxing.xunyitong.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * 微信支付回调Activity
 * 必须放在wxapi包下，且类名必须为WXPayEntryActivity
 */
public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
    private static final String TAG = "WXPayEntryActivity";
    
    private IWXAPI api;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 初始化微信API
        api = WXAPIFactory.createWXAPI(this, getWXAppId());
        api.handleIntent(getIntent(), this);
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }
    
    @Override
    public void onReq(BaseReq req) {
        Log.d(TAG, "onReq: " + req.getType());
    }
    
    @Override
    public void onResp(BaseResp resp) {
        Log.d(TAG, "onResp: errCode = " + resp.errCode);
        
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            // 微信支付回调
            handleWXPayResult(resp);
        }
    }
    
    /**
     * 处理微信支付结果
     */
    private void handleWXPayResult(BaseResp resp) {
        String result = "";
        
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                result = "支付成功";
                Log.d(TAG, "微信支付成功");
                // 支付成功，可以在这里进行后续处理
                // 比如向服务器验证支付结果
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                break;
                
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = "支付取消";
                Log.d(TAG, "用户取消微信支付");
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
                break;
                
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = "支付被拒绝";
                Log.e(TAG, "微信支付被拒绝");
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
                break;
                
            default:
                result = "支付失败";
                Log.e(TAG, "微信支付失败，错误码：" + resp.errCode);
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
                break;
        }
        
        // 关闭当前Activity
        finish();
    }
    
    /**
     * 获取微信AppID
     * 这里应该从配置文件或者BuildConfig中获取
     */
    private String getWXAppId() {
        // 微信AppID，需要与后端配置保持一致
        // 实际使用时需要替换为真实的微信AppID
        return "wx1234567890abcdef";
    }
}