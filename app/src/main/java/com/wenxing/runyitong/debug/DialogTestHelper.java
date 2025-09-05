package com.wenxing.runyitong.debug;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.wenxing.runyitong.dialog.ImageProcessingDialogFragment;

/**
 * 对话框测试辅助类
 * 用于直接测试对话框的显示和行为
 */
public class DialogTestHelper {
    private static final String TAG = "DialogTestHelper";
    
    /**
     * 直接测试对话框显示
     * @param fragmentManager FragmentManager实例
     * @param context Context实例
     */
    public static void testDialogDisplay(FragmentManager fragmentManager, Context context) {
        Log.d(TAG, "开始测试对话框显示");
        
        try {
            // 创建测试用的URI
            Uri testUri = Uri.parse("content://test/image.jpg");
            
            // 创建对话框实例
            ImageProcessingDialogFragment dialog = ImageProcessingDialogFragment.newInstance(
                testUri, 
                "test", 
                true
            );
            
            Log.d(TAG, "对话框实例创建成功");
            
            // 检查FragmentManager状态
            Log.d(TAG, "FragmentManager状态:");
            Log.d(TAG, "  - isDestroyed: " + fragmentManager.isDestroyed());
            Log.d(TAG, "  - isStateSaved: " + fragmentManager.isStateSaved());
            
            // 显示对话框
            Log.d(TAG, "准备显示对话框");
            dialog.show(fragmentManager, "TestImageProcessingDialog");
            Log.d(TAG, "对话框show()方法调用完成");
            
            // 延迟检查
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    Fragment foundDialog = fragmentManager.findFragmentByTag("TestImageProcessingDialog");
                    if (foundDialog != null && foundDialog.isAdded()) {
                        Log.d(TAG, "✅ 测试对话框显示成功");
                        if (foundDialog instanceof ImageProcessingDialogFragment) {
                            DialogDebugHelper.checkDialogFragmentState(
                                (ImageProcessingDialogFragment) foundDialog, 
                                "TestImageProcessingDialog"
                            );
                        }
                    } else {
                        Log.e(TAG, "❌ 测试对话框显示失败");
                        Log.e(TAG, "失败分析:");
                        Log.e(TAG, "  - foundDialog == null: " + (foundDialog == null));
                        if (foundDialog != null) {
                            Log.e(TAG, "  - foundDialog.isAdded(): " + foundDialog.isAdded());
                            Log.e(TAG, "  - foundDialog.isVisible(): " + foundDialog.isVisible());
                            Log.e(TAG, "  - foundDialog.isResumed(): " + foundDialog.isResumed());
                        }
                    }
                }
            }, 1000);
            
        } catch (Exception e) {
            Log.e(TAG, "测试对话框显示时发生异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 测试简单对话框显示
     * @param fragmentManager FragmentManager实例
     */
    public static void testSimpleDialog(FragmentManager fragmentManager) {
        Log.d(TAG, "开始测试简单对话框显示");
        
        try {
            // 创建不带参数的对话框实例
            ImageProcessingDialogFragment dialog = new ImageProcessingDialogFragment();
            
            Log.d(TAG, "简单对话框实例创建成功");
            
            // 显示对话框
            dialog.show(fragmentManager, "SimpleTestDialog");
            Log.d(TAG, "简单对话框show()方法调用完成");
            
            // 延迟检查
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    Fragment foundDialog = fragmentManager.findFragmentByTag("SimpleTestDialog");
                    if (foundDialog != null && foundDialog.isAdded()) {
                        Log.d(TAG, "✅ 简单测试对话框显示成功");
                    } else {
                        Log.e(TAG, "❌ 简单测试对话框显示失败");
                    }
                }
            }, 1000);
            
        } catch (Exception e) {
            Log.e(TAG, "测试简单对话框显示时发生异常: " + e.getMessage(), e);
        }
    }
}