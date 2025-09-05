package com.wenxing.runyitong.debug;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * 对话框调试辅助类
 * 用于分析对话框显示和消失的问题
 */
public class DialogDebugHelper {
    
    private static final String TAG = "DialogDebugHelper";
    
    /**
     * 检查Fragment状态
     */
    public static boolean checkFragmentState(Fragment fragment, String fragmentName) {
        Log.d(TAG, "=== 检查Fragment状态: " + fragmentName + " ===");
        
        if (fragment == null) {
            Log.e(TAG, "Fragment为null");
            return false;
        }
        
        Log.d(TAG, "Fragment基本状态:");
        Log.d(TAG, "  - isAdded(): " + fragment.isAdded());
        Log.d(TAG, "  - isDetached(): " + fragment.isDetached());
        Log.d(TAG, "  - isRemoving(): " + fragment.isRemoving());
        Log.d(TAG, "  - isResumed(): " + fragment.isResumed());
        Log.d(TAG, "  - isVisible(): " + fragment.isVisible());
        Log.d(TAG, "  - getContext(): " + (fragment.getContext() != null));
        Log.d(TAG, "  - getActivity(): " + (fragment.getActivity() != null));
        
        if (fragment.getActivity() != null) {
            Log.d(TAG, "Activity状态:");
            Log.d(TAG, "  - isFinishing(): " + fragment.getActivity().isFinishing());
            Log.d(TAG, "  - isDestroyed(): " + fragment.getActivity().isDestroyed());
        }
        
        FragmentManager fm = fragment.getParentFragmentManager();
        if (fm != null) {
            Log.d(TAG, "FragmentManager状态:");
            Log.d(TAG, "  - isStateSaved(): " + fm.isStateSaved());
            Log.d(TAG, "  - isDestroyed(): " + fm.isDestroyed());
        } else {
            Log.e(TAG, "FragmentManager为null");
        }
        
        boolean isValid = fragment.getContext() != null && 
                         fragment.isAdded() && 
                         !fragment.isDetached() && 
                         !fragment.isRemoving();
        
        Log.d(TAG, "Fragment状态检查结果: " + (isValid ? "有效" : "无效"));
        return isValid;
    }
    
    /**
     * 检查DialogFragment显示状态
     */
    public static void checkDialogFragmentState(DialogFragment dialogFragment, String dialogName) {
        Log.d(TAG, "=== 检查DialogFragment状态: " + dialogName + " ===");
        
        if (dialogFragment == null) {
            Log.e(TAG, "DialogFragment为null");
            return;
        }
        
        checkFragmentState(dialogFragment, dialogName);
        
        Dialog dialog = dialogFragment.getDialog();
        if (dialog != null) {
            Log.d(TAG, "Dialog状态:");
            Log.d(TAG, "  - isShowing(): " + dialog.isShowing());
            Log.d(TAG, "  - getWindow(): " + (dialog.getWindow() != null));
            
            if (dialog.getWindow() != null) {
                Log.d(TAG, "  - Window attributes: " + dialog.getWindow().getAttributes());
            }
        } else {
            Log.e(TAG, "Dialog为null");
        }
    }
    
    /**
     * 监控DialogFragment的生命周期
     */
    public static void monitorDialogLifecycle(DialogFragment dialogFragment, String dialogName) {
        Log.d(TAG, "开始监控DialogFragment生命周期: " + dialogName);
        
        // 延迟检查对话框状态
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        
        // 100ms后检查
        handler.postDelayed(() -> {
            Log.d(TAG, "[100ms后] 检查对话框状态");
            checkDialogFragmentState(dialogFragment, dialogName);
        }, 100);
        
        // 500ms后检查
        handler.postDelayed(() -> {
            Log.d(TAG, "[500ms后] 检查对话框状态");
            checkDialogFragmentState(dialogFragment, dialogName);
        }, 500);
        
        // 1000ms后检查
        handler.postDelayed(() -> {
            Log.d(TAG, "[1000ms后] 检查对话框状态");
            checkDialogFragmentState(dialogFragment, dialogName);
        }, 1000);
    }
    
    /**
     * 检查Context状态
     */
    public static boolean checkContextState(Context context, String contextName) {
        Log.d(TAG, "=== 检查Context状态: " + contextName + " ===");
        
        if (context == null) {
            Log.e(TAG, "Context为null");
            return false;
        }
        
        Log.d(TAG, "Context类型: " + context.getClass().getSimpleName());
        
        if (context instanceof android.app.Activity) {
            android.app.Activity activity = (android.app.Activity) context;
            Log.d(TAG, "Activity状态:");
            Log.d(TAG, "  - isFinishing(): " + activity.isFinishing());
            Log.d(TAG, "  - isDestroyed(): " + activity.isDestroyed());
            
            return !activity.isFinishing() && !activity.isDestroyed();
        }
        
        return true;
    }
}