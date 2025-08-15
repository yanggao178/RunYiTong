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

public class MainActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_main);
        
        // 隐藏ActionBar以移除Frontend-Android标题
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 初始化所有Fragment实例
        initFragments();
        
        // 设置默认显示商品Fragment
        showFragment(productFragment);

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
        productFragment = new ProductFragment();
        registrationFragment = new RegistrationFragment();
        prescriptionFragment = new PrescriptionFragment();
        healthFragment = new HealthFragment();
        profileFragment = new ProfileFragment();
        
        // 添加所有Fragment到容器中，但先隐藏它们
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.frame_layout, productFragment, "product");
        transaction.add(R.id.frame_layout, registrationFragment, "registration");
        transaction.add(R.id.frame_layout, prescriptionFragment, "prescription");
        transaction.add(R.id.frame_layout, healthFragment, "health");
        transaction.add(R.id.frame_layout, profileFragment, "profile");
        
        // 隐藏所有Fragment
        transaction.hide(productFragment);
        transaction.hide(registrationFragment);
        transaction.hide(prescriptionFragment);
        transaction.hide(healthFragment);
        transaction.hide(profileFragment);
        
        transaction.commit();
    }
    
    /**
     * 显示指定Fragment，隐藏其他Fragment
     */
    private void showFragment(Fragment fragment) {
        android.util.Log.d("MainActivity", "showFragment called, target: " + fragment.getClass().getSimpleName());
        if (currentFragment == fragment) {
            android.util.Log.d("MainActivity", "Same fragment, no switch needed");
            return; // 如果是当前Fragment，不需要切换
        }
        
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        
        // 隐藏当前Fragment
        if (currentFragment != null) {
            android.util.Log.d("MainActivity", "Hiding current fragment: " + currentFragment.getClass().getSimpleName());
            transaction.hide(currentFragment);
        }
        
        // 显示目标Fragment
        android.util.Log.d("MainActivity", "Showing target fragment: " + fragment.getClass().getSimpleName());
        transaction.show(fragment);
        transaction.commit();
        
        currentFragment = fragment;
        android.util.Log.d("MainActivity", "Fragment switch completed");
    }
}