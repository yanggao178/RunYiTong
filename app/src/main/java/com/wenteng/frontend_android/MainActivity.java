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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 隐藏ActionBar以移除Frontend-Android标题
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 设置默认显示商品Fragment
        replaceFragment(new ProductFragment());

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

        // 替换后
        // 在底部导航的监听器中
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();
            if (id == R.id.nav_product) {
                fragment = new ProductFragment();
            } else if (id == R.id.nav_registration) {
                fragment = new RegistrationFragment();
            } else if (id == R.id.nav_prescription) {
                fragment = new PrescriptionFragment();
            } else if (id == R.id.nav_health) {
                fragment = new HealthFragment();
            } else if (id == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                replaceFragment(fragment);
            }
            return true;
        });
    }

    // 替换Fragment方法
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .commit();
        // .replace(R.id.fragment_container, fragment) //
        // .commit();
    }
}