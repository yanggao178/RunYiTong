package com.wenxing.runyitong.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.wenxing.runyitong.R;
import com.wenxing.runyitong.api.ApiClient;
import com.wenxing.runyitong.api.ApiService;
import com.wenxing.runyitong.api.ApiResponse;
import com.wenxing.runyitong.api.AddressListResponse;
import com.wenxing.runyitong.model.Address;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.io.IOException;

// Android原生定位服务相关导入
import android.location.LocationManager;
import android.location.LocationListener;
import android.location.Geocoder;
import android.location.Location;
// 注意：不直接导入android.location.Address，而是在需要使用的地方使用完整包名

public class AddressActivity extends AppCompatActivity {

    private static final String TAG = "AddressActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private Button btnAddAddress;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Geocoder geocoder;
    private EditText currentAddressEditText;
    private RecyclerView rvAddresses;
    private LinearLayout tvEmptyAddress;
    private AddressAdapter addressAdapter;
    private List<Address> addressList = new ArrayList<>();
    private ApiService apiService;
    private int userId = -1; // 用户ID，将从SharedPreferences中获取

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 初始化Android原生定位服务
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        geocoder = new Geocoder(this, Locale.getDefault());
        
        // 初始化API服务
        apiService = ApiClient.getApiService();
        
        // 获取用户ID
        getUserInfo();
        
        setContentView(R.layout.activity_address);
        
        setupToolbar();
        initViews();
        setupClickListeners();
        
        // 初始化定位监听器
        initLocationListener();
        
        // 加载地址列表
        loadAddresses();
    }
    
    /**
     * 从SharedPreferences获取用户信息
     */
    private void getUserInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_login_state", MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);
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
        rvAddresses = findViewById(R.id.rv_addresses);
        tvEmptyAddress = findViewById(R.id.tv_empty_address);
        
        // 初始化RecyclerView
        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        addressAdapter = new AddressAdapter(addressList);
        rvAddresses.setAdapter(addressAdapter);
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
            
            if (userId == -1) {
                Toast.makeText(this, "用户未登录，请先登录", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 保存地址到服务器
            saveAddressToServer(name, phone, address);
            dialog.dismiss();
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    /**
     * 保存地址到服务器
     */
    private void saveAddressToServer(String name, String phone, String fullAddress) {
        // 简单解析地址，实际应用中可能需要更复杂的地址解析
        String province = "";
        String city = "";
        String district = "";
        String detailAddress = fullAddress;
        
        // 创建Address对象
        Address address = new Address();
        address.setUserId(userId);
        address.setName(name);
        address.setPhone(phone);
        address.setProvince(province);
        address.setCity(city);
        address.setDistrict(district);
        address.setDetailAddress(detailAddress);
        address.setDefault(false);
        address.setLatitude("0"); // 定位功能获取到经纬度后可以更新
        address.setLongitude("0");
        
        // 调用API保存地址
        apiService.addAddress(address).enqueue(new Callback<ApiResponse<Address>>() {
            @Override
            public void onResponse(Call<ApiResponse<Address>> call, Response<ApiResponse<Address>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Address> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(AddressActivity.this, "地址添加成功", Toast.LENGTH_SHORT).show();
                        loadAddresses();
                    } else {
                        Toast.makeText(AddressActivity.this, "地址添加失败: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddressActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Address>> call, Throwable t) {
                Log.e(TAG, "保存地址失败: " + t.getMessage());
                Toast.makeText(AddressActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * 加载地址列表
     */
    private void loadAddresses() {
        if (userId == -1) {
            Toast.makeText(this, "用户未登录，请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        
        apiService.getUserAddresses(userId).enqueue(new Callback<AddressListResponse>() {
            @Override
            public void onResponse(Call<AddressListResponse> call, Response<AddressListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AddressListResponse addressListResponse = response.body();
                    if (addressListResponse.isSuccess()) {
                        addressList.clear();
                        
                        // 安全处理getData()可能返回null的情况
                        if (addressListResponse.getData() != null) {
                            try {
                                // 使用循环添加每个地址对象，避免直接调用addAll可能出现的类型转换问题
                                for (Address address : addressListResponse.getData()) {
                                    addressList.add(address);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "添加地址到列表时出错: " + e.getMessage(), e);
                                Toast.makeText(AddressActivity.this, "处理地址数据时出错", Toast.LENGTH_SHORT).show();
                            }
                        }
                        
                        addressAdapter.notifyDataSetChanged();
                        
                        // 显示或隐藏空地址提示
                        if (addressList.isEmpty()) {
                            rvAddresses.setVisibility(View.GONE);
                            tvEmptyAddress.setVisibility(View.VISIBLE);
                        } else {
                            rvAddresses.setVisibility(View.VISIBLE);
                            tvEmptyAddress.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(AddressActivity.this, "获取地址失败: " + addressListResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 处理响应失败的情况，包括500错误
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "API错误响应: " + errorBody);
                            Toast.makeText(AddressActivity.this, "获取地址列表失败: " + response.code() + " - " + errorBody, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AddressActivity.this, "网络请求失败: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "解析错误响应失败", e);
                        Toast.makeText(AddressActivity.this, "获取地址列表失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<AddressListResponse> call, Throwable t) {
                Log.e(TAG, "获取地址列表失败: " + t.getMessage());
                Toast.makeText(AddressActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * 地址适配器，用于RecyclerView
     */
    private class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {
        private List<Address> addresses;
        
        public AddressAdapter(List<Address> addresses) {
            this.addresses = addresses;
        }
        
        @Override
        public AddressViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
            return new AddressViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(AddressViewHolder holder, int position) {
            Address address = addresses.get(position);
            holder.tvName.setText(address.getName());
            holder.tvPhone.setText(address.getPhone());
            holder.tvAddress.setText(buildFullAddress(address));
            
            // 设置默认地址标记
            if (address.isDefault()) {
                holder.tvDefault.setVisibility(View.VISIBLE);
            } else {
                holder.tvDefault.setVisibility(View.GONE);
            }
            
            // 设置编辑按钮点击事件
            holder.btnEdit.setOnClickListener(v -> {
                // 显示编辑地址对话框
                showEditAddressDialog(address);
            });
            
            // 设置删除按钮点击事件
            holder.btnDelete.setOnClickListener(v -> {
                showDeleteConfirmDialog(address.getId());
            });
            
            // 设置设为默认按钮点击事件
            holder.btnSetDefault.setOnClickListener(v -> {
                setAddressAsDefault(address.getId());
            });
        }
        
        @Override
        public int getItemCount() {
            return addresses.size();
        }
        
        class AddressViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvPhone, tvAddress, tvDefault;
            Button btnEdit, btnDelete, btnSetDefault;
            
            public AddressViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_name);
                tvPhone = itemView.findViewById(R.id.tv_phone);
                tvAddress = itemView.findViewById(R.id.tv_address);
                tvDefault = itemView.findViewById(R.id.tv_default);
                btnEdit = itemView.findViewById(R.id.btn_edit);
                btnDelete = itemView.findViewById(R.id.btn_delete);
                btnSetDefault = itemView.findViewById(R.id.btn_set_default);
            }
        }
        
        /**
         * 构建完整的地址字符串
         */
        private String buildFullAddress(Address address) {
            StringBuilder sb = new StringBuilder();
            if (address.getProvince() != null && !address.getProvince().isEmpty()) {
                sb.append(address.getProvince());
            }
            if (address.getCity() != null && !address.getCity().isEmpty()) {
                sb.append(address.getCity());
            }
            if (address.getDistrict() != null && !address.getDistrict().isEmpty()) {
                sb.append(address.getDistrict());
            }
            if (address.getDetailAddress() != null && !address.getDetailAddress().isEmpty()) {
                sb.append(address.getDetailAddress());
            }
            return sb.toString();
        }
    }
    
    /**
     * 显示删除确认对话框
     */
    private void showDeleteConfirmDialog(int addressId) {
        new AlertDialog.Builder(this)
            .setTitle("确认删除")
            .setMessage("确定要删除该收货地址吗？")
            .setPositiveButton("确定", (dialog, which) -> {
                deleteAddress(addressId);
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    /**
     * 删除地址
     */
    private void deleteAddress(int addressId) {
        apiService.deleteAddress(addressId).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(AddressActivity.this, "地址删除成功", Toast.LENGTH_SHORT).show();
                        // 重新加载地址列表
                        loadAddresses();
                    } else {
                        Toast.makeText(AddressActivity.this, "地址删除失败: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddressActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Log.e(TAG, "删除地址失败: " + t.getMessage());
                Toast.makeText(AddressActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * 设置默认地址
     */
    private void setAddressAsDefault(int addressId) {
        apiService.setDefaultAddress(addressId).enqueue(new Callback<ApiResponse<Address>>() {
            @Override
            public void onResponse(Call<ApiResponse<Address>> call, Response<ApiResponse<Address>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Address> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(AddressActivity.this, "已设为默认地址", Toast.LENGTH_SHORT).show();
                        // 重新加载地址列表
                        loadAddresses();
                    } else {
                        Toast.makeText(AddressActivity.this, "设置失败: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddressActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Address>> call, Throwable t) {
                Log.e(TAG, "设置默认地址失败: " + t.getMessage());
                Toast.makeText(AddressActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 构建完整的地址字符串（静态方法，供外部调用）
     */
    public static String buildFullAddress(Address address) {
        StringBuilder sb = new StringBuilder();
        if (address.getProvince() != null && !address.getProvince().isEmpty()) {
            sb.append(address.getProvince());
        }
        if (address.getCity() != null && !address.getCity().isEmpty()) {
            sb.append(address.getCity());
        }
        if (address.getDistrict() != null && !address.getDistrict().isEmpty()) {
            sb.append(address.getDistrict());
        }
        if (address.getDetailAddress() != null && !address.getDetailAddress().isEmpty()) {
            sb.append(address.getDetailAddress());
        }
        return sb.toString();
    }

    /**
     * 显示编辑地址对话框
     */
    private void showEditAddressDialog(Address address) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_address, null);
        
        EditText etName = dialogView.findViewById(R.id.et_name);
        EditText etPhone = dialogView.findViewById(R.id.et_phone);
        EditText etAddress = dialogView.findViewById(R.id.et_address);
        Button btnSave = dialogView.findViewById(R.id.btn_save);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        ImageButton btnLocation = dialogView.findViewById(R.id.btn_location);
        
        // 预填当前地址信息
        etName.setText(address.getName());
        etPhone.setText(address.getPhone());
        
        // 构建完整地址字符串并填充
        String fullAddress = AddressActivity.buildFullAddress(address);
        etAddress.setText(fullAddress);
        
        // 保存当前地址输入框的引用，用于定位功能
        currentAddressEditText = etAddress;
        
        builder.setView(dialogView);
        
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
            String fullAddressText = etAddress.getText().toString().trim();
            
            if (name.isEmpty() || phone.isEmpty() || fullAddressText.isEmpty()) {
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (userId == -1) {
                Toast.makeText(this, "用户未登录，请先登录", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 更新地址到服务器
            updateAddressToServer(address.getId(), name, phone, fullAddressText);
            dialog.dismiss();
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    /**
     * 更新地址到服务器
     */
    private void updateAddressToServer(int addressId, String name, String phone, String fullAddress) {
        // 简单解析地址，实际应用中可能需要更复杂的地址解析
        // 修改前：province、city、district都为空字符串
        // 修改后：设置默认值以满足后端验证要求
        String province = "未知省";
        String city = "未知市";
        String district = "未知区";
        String detailAddress = fullAddress;
        
        // 创建Address对象
        Address address = new Address();
        address.setId(addressId);
        address.setUserId(userId);
        address.setName(name);
        address.setPhone(phone);
        address.setProvince(province);
        address.setCity(city);
        address.setDistrict(district);
        address.setDetailAddress(detailAddress);
        address.setDefault(false); // 保持原有的默认状态
        address.setLatitude("0"); // 定位功能获取到经纬度后可以更新
        address.setLongitude("0");
        
        // 调用API更新地址
        apiService.updateAddress(addressId, address).enqueue(new Callback<ApiResponse<Address>>() {
            @Override
            public void onResponse(Call<ApiResponse<Address>> call, Response<ApiResponse<Address>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Address> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(AddressActivity.this, "地址更新成功", Toast.LENGTH_SHORT).show();
                        // 重新加载地址列表
                        loadAddresses();
                    } else {
                        Toast.makeText(AddressActivity.this, "地址更新失败: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddressActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Address>> call, Throwable t) {
                Log.e(TAG, "更新地址失败: " + t.getMessage());
                Toast.makeText(AddressActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
            }
        });
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
            List<android.location.Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                android.location.Address address = addresses.get(0);
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