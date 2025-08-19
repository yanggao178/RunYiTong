package com.wenteng.frontend_android;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.wenteng.frontend_android.fragment.HealthFragment;
import com.wenteng.frontend_android.fragment.PrescriptionFragment;
import com.wenteng.frontend_android.fragment.ProductFragment;
import com.wenteng.frontend_android.fragment.ProfileFragment;
import com.wenteng.frontend_android.fragment.RegistrationFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.wenteng.frontend_android.R;
import com.wenteng.frontend_android.utils.OverlayPermissionManager;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    // SharedPreferences相关常量
    private static final String PREFS_NAME = "app_state";
    private static final String KEY_LAST_FRAGMENT = "last_fragment";
    private static final String FRAGMENT_PRODUCT = "product";
    private static final String FRAGMENT_REGISTRATION = "registration";
    private static final String FRAGMENT_PRESCRIPTION = "prescription";
    private static final String FRAGMENT_HEALTH = "health";
    private static final String FRAGMENT_PROFILE = "profile";

    private BottomNavigationView bottomNavigationView;
    private ProductFragment productFragment;
    private RegistrationFragment registrationFragment;
    private PrescriptionFragment prescriptionFragment;
    private HealthFragment healthFragment;
    private ProfileFragment profileFragment;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            // 监控内存使用情况
            logMemoryUsage("onCreate start");
            
            android.util.Log.d("MainActivity", "onCreate started");
            
            // 确保应用始终以纵向模式显示
            setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            
            setContentView(R.layout.activity_main);
            android.util.Log.d("MainActivity", "Layout set successfully");
            
            // 隐藏ActionBar以移除Frontend-Android标题
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }

            bottomNavigationView = findViewById(R.id.bottom_navigation);
            if (bottomNavigationView == null) {
                android.util.Log.e("MainActivity", "BottomNavigationView not found!");
                return;
            }
            android.util.Log.d("MainActivity", "BottomNavigationView initialized");

            // 初始化所有Fragment实例
            initFragments();
            android.util.Log.d("MainActivity", "Fragments initialized");
            
            // 恢复上次选中的Fragment，如果没有则显示商品Fragment
            restoreLastFragment();
            android.util.Log.d("MainActivity", "Fragment restored");
            
            // 设置底部导航点击事件
            setupBottomNavigation();
            
            logMemoryUsage("onCreate end");
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error in onCreate", e);
            // 可以在这里添加崩溃报告或用户友好的错误提示
        }

        // 底部导航点击事件
        // 替换前
        // bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
        // Fragment fragment = null;
        // int itemId = item.getItemId();
        //
        // if (itemId == R.id.nav_product) {
        // fragment = new ProductFragment();
        // } else if (itemId == R.id.nav_registration) {
        // fragment = new RegistrationFragment();
        // } else if (itemId == R.id.nav_prescription) {
        // fragment = new PrescriptionFragment();
        // } else if (itemId == R.id.nav_health) {
        // fragment = new HealthFragment();
        // } else if (itemId == R.id.nav_profile) {
        // fragment = new ProfileFragment();
        // }
        //
        // if (fragment != null) {
        // replaceFragment(fragment);
        // return true;
        // }
        // return false;
        // });


    }
    
    /**
     * 设置底部导航点击事件
     */
    private void setupBottomNavigation() {
        // 底部导航点击事件
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();
            if (id == R.id.nav_product) {
                fragment = productFragment;
            } else if (id == R.id.nav_registration) {
                fragment = registrationFragment;
            } else if (id == R.id.nav_prescription) {
                fragment = prescriptionFragment;
            } else if (id == R.id.nav_health) {
                fragment = healthFragment;
            } else if (id == R.id.nav_profile) {
                fragment = profileFragment;
            }

            if (fragment != null) {
                showFragment(fragment);
            }
            return true;
        });
    }

    /**
     * 初始化所有Fragment实例
     */
    private void initFragments() {
        try {
            android.util.Log.d("MainActivity", "Creating fragment instances");
            productFragment = new ProductFragment();
            registrationFragment = new RegistrationFragment();
            prescriptionFragment = new PrescriptionFragment();
            healthFragment = new HealthFragment();
            profileFragment = new ProfileFragment();
            android.util.Log.d("MainActivity", "All fragments created successfully");
            
            // 添加所有Fragment到容器中，但先隐藏它们
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.frame_layout, productFragment, "product");
            transaction.add(R.id.frame_layout, registrationFragment, "registration");
            transaction.add(R.id.frame_layout, prescriptionFragment, "prescription");
            transaction.add(R.id.frame_layout, healthFragment, "health");
            transaction.add(R.id.frame_layout, profileFragment, "profile");
            android.util.Log.d("MainActivity", "All fragments added to transaction");
            
            // 隐藏所有Fragment
            transaction.hide(productFragment);
            transaction.hide(registrationFragment);
            transaction.hide(prescriptionFragment);
            transaction.hide(healthFragment);
            transaction.hide(profileFragment);
            android.util.Log.d("MainActivity", "All fragments hidden");
            
            // 使用commitNow()确保fragment立即被添加，然后再显示默认fragment
            transaction.commitNow();
            android.util.Log.d("MainActivity", "Fragment transaction committed immediately");
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error initializing fragments", e);
            throw e; // 重新抛出异常，让调用者知道初始化失败
        }
    }
    
    /**
     * 显示指定Fragment，隐藏其他Fragment
     */
    private void showFragment(Fragment fragment) {
        android.util.Log.d("MainActivity", "showFragment called, target: " + fragment.getClass().getSimpleName());
        
        // 检查Activity状态
        if (isFinishing() || isDestroyed()) {
            android.util.Log.w("MainActivity", "Activity is finishing or destroyed, cannot show fragment");
            return;
        }
        
        // 检查Fragment状态
        if (fragment == null) {
            android.util.Log.e("MainActivity", "Target fragment is null");
            return;
        }
        
        if (currentFragment == fragment) {
            android.util.Log.d("MainActivity", "Same fragment, no switch needed");
            return; // 如果是当前Fragment，不需要切换
        }
        
        // 检查内存状态
        logMemoryUsage("before fragment switch");
        
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        
        // 隐藏当前Fragment
        if (currentFragment != null && currentFragment.isAdded()) {
            android.util.Log.d("MainActivity", "Hiding current fragment: " + currentFragment.getClass().getSimpleName());
            transaction.hide(currentFragment);
        }
        
        // 显示目标Fragment
        if (fragment.isAdded()) {
            android.util.Log.d("MainActivity", "Showing target fragment: " + fragment.getClass().getSimpleName());
            transaction.show(fragment);
        } else {
            android.util.Log.w("MainActivity", "Fragment not added, adding and showing: " + fragment.getClass().getSimpleName());
            // 如果fragment没有被添加，先添加再显示
            transaction.add(R.id.frame_layout, fragment);
            transaction.show(fragment);
        }
        
        // 使用commitAllowingStateLoss避免状态丢失异常
        transaction.commitAllowingStateLoss();
        
        currentFragment = fragment;
        
        // 保存当前Fragment状态
        saveCurrentFragment(fragment);
        
        // 更新底部导航栏选中状态
        updateBottomNavigationSelection(fragment);
        
        // 监控内存使用
        logMemoryUsage("after showing " + fragment.getClass().getSimpleName());
        
        android.util.Log.d("MainActivity", "Fragment switch completed");
    }
    
    /**
     * 保存当前Fragment状态到SharedPreferences
     */
    private void saveCurrentFragment(Fragment fragment) {
        String fragmentName = getFragmentName(fragment);
        if (fragmentName != null) {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_LAST_FRAGMENT, fragmentName)
                .apply();
            android.util.Log.d("MainActivity", "Saved fragment state: " + fragmentName);
        }
    }
    
    /**
     * 恢复上次选中的Fragment
     */
    private void restoreLastFragment() {
        String lastFragmentName = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getString(KEY_LAST_FRAGMENT, FRAGMENT_PRODUCT);
        
        Fragment fragmentToShow = getFragmentByName(lastFragmentName);
        if (fragmentToShow != null) {
            showFragment(fragmentToShow);
            android.util.Log.d("MainActivity", "Restored last fragment: " + lastFragmentName);
        } else {
            // 如果找不到对应Fragment，显示默认的商品Fragment
            showFragment(productFragment);
            android.util.Log.d("MainActivity", "Fallback to default fragment");
        }
    }
    
    /**
     * 根据Fragment获取对应的名称
     */
    private String getFragmentName(Fragment fragment) {
        if (fragment == productFragment) {
            return FRAGMENT_PRODUCT;
        } else if (fragment == registrationFragment) {
            return FRAGMENT_REGISTRATION;
        } else if (fragment == prescriptionFragment) {
            return FRAGMENT_PRESCRIPTION;
        } else if (fragment == healthFragment) {
            return FRAGMENT_HEALTH;
        } else if (fragment == profileFragment) {
            return FRAGMENT_PROFILE;
        }
        return null;
    }
    
    /**
     * 根据名称获取对应的Fragment
     */
    private Fragment getFragmentByName(String fragmentName) {
        switch (fragmentName) {
            case FRAGMENT_PRODUCT:
                return productFragment;
            case FRAGMENT_REGISTRATION:
                return registrationFragment;
            case FRAGMENT_PRESCRIPTION:
                return prescriptionFragment;
            case FRAGMENT_HEALTH:
                return healthFragment;
            case FRAGMENT_PROFILE:
                return profileFragment;
            default:
                return null;
        }
    }
    
    /**
     * 更新底部导航栏选中状态
     */
    private void updateBottomNavigationSelection(Fragment fragment) {
        if (bottomNavigationView == null) return;
        
        int menuItemId = R.id.nav_product; // 默认值
        
        if (fragment == productFragment) {
            menuItemId = R.id.nav_product;
        } else if (fragment == registrationFragment) {
            menuItemId = R.id.nav_registration;
        } else if (fragment == prescriptionFragment) {
            menuItemId = R.id.nav_prescription;
        } else if (fragment == healthFragment) {
            menuItemId = R.id.nav_health;
        } else if (fragment == profileFragment) {
            menuItemId = R.id.nav_profile;
        }
        
        bottomNavigationView.setSelectedItemId(menuItemId);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        android.util.Log.d("MainActivity", "onResume called - 应用回到前台");
        logMemoryUsage("onResume");
        
        // 延迟重置对话框状态，防止从微信返回时立即重复显示对话框
        // 但允许用户在一段时间后重新触发
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            // 通过反射调用HospitalAdapter的resetDialogState方法
            try {
                Class<?> adapterClass = Class.forName("com.wenteng.frontend_android.adapter.HospitalAdapter");
                java.lang.reflect.Method resetMethod = adapterClass.getDeclaredMethod("resetDialogState");
                resetMethod.setAccessible(true);
                resetMethod.invoke(null);
                android.util.Log.d("MainActivity", "对话框状态已延迟重置");
            } catch (Exception e) {
                android.util.Log.w("MainActivity", "重置对话框状态失败: " + e.getMessage());
            }
        }, 5000); // 5秒后重置状态
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        logMemoryUsage("onPause");
    }
    
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        android.util.Log.w("MainActivity", "Low memory warning received");
        logMemoryUsage("onLowMemory");
        // 强制垃圾回收
        System.gc();
    }
    
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        android.util.Log.w("MainActivity", "Memory trim requested, level: " + level);
        logMemoryUsage("onTrimMemory level " + level);
        
        // 根据不同级别采取不同的内存清理策略
        if (level >= TRIM_MEMORY_MODERATE) {
            // 清理一些缓存
            System.gc();
        }
    }
    
    private void logMemoryUsage(String context) {
        try {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            long maxMemory = runtime.maxMemory();
            
            android.util.Log.d("MainActivity", String.format(
                "Memory [%s]: Used=%dMB, Free=%dMB, Total=%dMB, Max=%dMB, Usage=%.1f%%",
                context,
                usedMemory / (1024 * 1024),
                freeMemory / (1024 * 1024),
                totalMemory / (1024 * 1024),
                maxMemory / (1024 * 1024),
                (usedMemory * 100.0 / maxMemory)
            ));
            
            // 如果内存使用超过75%，发出警告
            if (usedMemory * 100 / maxMemory > 75) {
                android.util.Log.w("MainActivity", "High memory usage detected: " + (usedMemory * 100 / maxMemory) + "%");
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error logging memory usage", e);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // 处理悬浮窗权限返回结果
        OverlayPermissionManager.handlePermissionResult(this, requestCode);
    }
}