package com.wenteng.frontend_android.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.wenteng.frontend_android.R;

// Android原生定位服务相关导入
import android.location.LocationManager;
import android.location.LocationListener;
import android.location.Geocoder;
import android.location.Address;
import android.location.Location;
import java.util.List;
import java.util.Locale;
import java.io.IOException;

public class AddressActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private Button btnAddAddress;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Geocoder geocoder;
    private EditText currentAddressEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 初始化Android原生定位服务
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        geocoder = new Geocoder(this, Locale.getDefault());
        
        setContentView(R.layout.activity_address);
        
        setupToolbar();
        initViews();
        setupClickListeners();
        
        // 初始化定位监听器
        initLocationListener();
    }
    
    /**
     * 初始化定位监听器
     */
    private void initLocationListener() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // 处理位置信息
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                
                // 根据经纬度获取地址信息
                getAddressFromLocation(latitude, longitude);
                
                // 停止定位更新
                locationManager.removeUpdates(locationListener);
            }
            
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            
            @Override
            public void onProviderEnabled(String provider) {}
            
            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(AddressActivity.this, "定位服务已关闭", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("收货地址");
        }
    }

    private void initViews() {
        btnAddAddress = findViewById(R.id.btn_add_address);
    }

    private void setupClickListeners() {
        btnAddAddress.setOnClickListener(v -> showAddAddressDialog());
    }

    private void showAddAddressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_address, null);
        
        EditText etName = dialogView.findViewById(R.id.et_name);
        EditText etPhone = dialogView.findViewById(R.id.et_phone);
        EditText etAddress = dialogView.findViewById(R.id.et_address);
        Button btnSave = dialogView.findViewById(R.id.btn_save);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        ImageButton btnLocation = dialogView.findViewById(R.id.btn_location);
        
        // 保存当前地址输入框的引用，用于定位功能
        currentAddressEditText = etAddress;
        
        builder.setView(dialogView);
        // 移除默认标题，使用自定义布局中的标题
        
        AlertDialog dialog = builder.create();
        
        // 设置对话框窗口属性
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        // 定位按钮点击事件
        btnLocation.setOnClickListener(v -> {
            requestLocationPermissionAndGetLocation();
        });
        
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            
            if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // TODO: 保存地址到数据库或服务器
            Toast.makeText(this, "地址添加成功", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    /**
     * 请求定位权限并获取位置
     */
    private void requestLocationPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            // 请求权限
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // 已有权限，直接获取位置
            startLocationRequest();
        }
    }

    /**
     * 开始定位请求
     */
    private void startLocationRequest() {
        Toast.makeText(this, "正在获取位置信息...", Toast.LENGTH_SHORT).show();
        
        try {
            // 检查GPS是否可用
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000, // 最小时间间隔（毫秒）
                    10,    // 最小距离间隔（米）
                    locationListener
                );
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                // 如果GPS不可用，使用网络定位
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    10000,
                    10,
                    locationListener
                );
            } else {
                Toast.makeText(this, "请开启定位服务", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "定位权限不足", Toast.LENGTH_SHORT).show();
        }
    }


    
    /**
     * 根据经纬度获取地址信息
     */
    private void getAddressFromLocation(double latitude, double longitude) {
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder addressText = new StringBuilder();
                
                // 构建详细地址
                if (address.getAdminArea() != null) {
                    addressText.append(address.getAdminArea());
                }
                if (address.getLocality() != null) {
                    addressText.append(address.getLocality());
                }
                if (address.getSubLocality() != null) {
                    addressText.append(address.getSubLocality());
                }
                if (address.getThoroughfare() != null) {
                    addressText.append(address.getThoroughfare());
                }
                if (address.getSubThoroughfare() != null) {
                    addressText.append(address.getSubThoroughfare());
                }
                
                String finalAddress = addressText.toString();
                if (!finalAddress.isEmpty()) {
                    runOnUiThread(() -> {
                        if (currentAddressEditText != null) {
                            currentAddressEditText.setText(finalAddress);
                        }
                        Toast.makeText(AddressActivity.this, "定位成功", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(AddressActivity.this, "无法获取详细地址信息", Toast.LENGTH_SHORT).show();
                    });
                }
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(AddressActivity.this, "无法解析地址信息", Toast.LENGTH_SHORT).show();
                });
            }
        } catch (IOException e) {
            runOnUiThread(() -> {
                Toast.makeText(AddressActivity.this, "地址解析失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被授予，获取位置
                startLocationRequest();
            } else {
                Toast.makeText(this, "需要位置权限才能使用定位功能", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}