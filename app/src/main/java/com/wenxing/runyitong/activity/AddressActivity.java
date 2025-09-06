package com.wenxing.runyitong.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.viewpager.widget.ViewPager;
import android.graphics.Color;
import com.google.android.material.tabs.TabLayout;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    // 省市区选择相关变量
    private String selectedProvince = "";
    private String selectedCity = "";
    private String selectedDistrict = "";
    private TextView tvLocation;
    private LinearLayout layoutLocationSelector;

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
        
        // 获取所在地选择区域的引用
        layoutLocationSelector = dialogView.findViewById(R.id.layout_location_selector);
        tvLocation = dialogView.findViewById(R.id.tv_location);
        
        // 保存当前地址输入框的引用，用于定位功能
        currentAddressEditText = etAddress;
        
        // 重置省市区选择
        selectedProvince = "";
        selectedCity = "";
        selectedDistrict = "";
        tvLocation.setText("请选择省/市/区");
        
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
        
        // 所在地选择区域点击事件
        layoutLocationSelector.setOnClickListener(v -> {
            // 显示省市区选择器
            showLocationPickerDialog(dialog);
        });
        
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String detailAddress = etAddress.getText().toString().trim();
            
            if (name.isEmpty() || phone.isEmpty() || detailAddress.isEmpty()) {
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (selectedProvince.isEmpty() || selectedCity.isEmpty() || selectedDistrict.isEmpty()) {
                Toast.makeText(this, "请选择省/市/区", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (userId == -1) {
                Toast.makeText(this, "用户未登录，请先登录", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 保存地址到服务器
            saveAddressToServer(name, phone, selectedProvince, selectedCity, selectedDistrict, detailAddress);
            dialog.dismiss();
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    /**
     * 保存地址到服务器
     */
    private void saveAddressToServer(String name, String phone, String province, String city, String district, String detailAddress) {
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
    /**
     * 显示省市区选择对话框 - 修改为三列同时显示
     */
    private void showLocationPickerDialog(AlertDialog parentDialog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择省/市/区");
        
        // 使用新的三列布局
        View view = getLayoutInflater().inflate(R.layout.dialog_location_picker_three_column, null);
        RecyclerView rvProvinces = view.findViewById(R.id.rv_provinces);
        RecyclerView rvCities = view.findViewById(R.id.rv_cities);
        RecyclerView rvDistricts = view.findViewById(R.id.rv_districts);
        
        // 设置RecyclerView的LayoutManager
        rvProvinces.setLayoutManager(new LinearLayoutManager(this));
        rvCities.setLayoutManager(new LinearLayoutManager(this));
        rvDistricts.setLayoutManager(new LinearLayoutManager(this));
        
        // 准备省市区数据
        final Map<String, List<String>> provinceCityMap = new HashMap<>();
        final Map<String, List<String>> cityDistrictMap = new HashMap<>();
        
        // 完整的中国省份、城市、区县数据
        
        // 直辖市
        provinceCityMap.put("北京市", Arrays.asList("北京市"));
        provinceCityMap.put("上海市", Arrays.asList("上海市"));
        provinceCityMap.put("天津市", Arrays.asList("天津市"));
        provinceCityMap.put("重庆市", Arrays.asList("重庆市"));
        
        // 河北省
        provinceCityMap.put("河北省", Arrays.asList("石家庄市", "唐山市", "秦皇岛市", "邯郸市", "邢台市", "保定市", "张家口市", "承德市", "沧州市", "廊坊市", "衡水市"));
        
        // 山西省
        provinceCityMap.put("山西省", Arrays.asList("太原市", "大同市", "阳泉市", "长治市", "晋城市", "朔州市", "晋中市", "运城市", "忻州市", "临汾市", "吕梁市"));
        
        // 辽宁省
        provinceCityMap.put("辽宁省", Arrays.asList("沈阳市", "大连市", "鞍山市", "抚顺市", "本溪市", "丹东市", "锦州市", "营口市", "阜新市", "辽阳市", "盘锦市", "铁岭市", "朝阳市", "葫芦岛市"));
        
        // 吉林省
        provinceCityMap.put("吉林省", Arrays.asList("长春市", "吉林市", "四平市", "辽源市", "通化市", "白山市", "松原市", "白城市", "延边朝鲜族自治州"));
        
        // 黑龙江省
        provinceCityMap.put("黑龙江省", Arrays.asList("哈尔滨市", "齐齐哈尔市", "鸡西市", "鹤岗市", "双鸭山市", "大庆市", "伊春市", "佳木斯市", "七台河市", "牡丹江市", "黑河市", "绥化市", "大兴安岭地区"));
        
        // 江苏省
        provinceCityMap.put("江苏省", Arrays.asList("南京市", "无锡市", "徐州市", "常州市", "苏州市", "南通市", "连云港市", "淮安市", "盐城市", "扬州市", "镇江市", "泰州市", "宿迁市"));
        
        // 浙江省
        provinceCityMap.put("浙江省", Arrays.asList("杭州市", "宁波市", "温州市", "嘉兴市", "湖州市", "绍兴市", "金华市", "衢州市", "舟山市", "台州市", "丽水市"));
        
        // 安徽省
        provinceCityMap.put("安徽省", Arrays.asList("合肥市", "芜湖市", "蚌埠市", "淮南市", "马鞍山市", "淮北市", "铜陵市", "安庆市", "黄山市", "滁州市", "阜阳市", "宿州市", "六安市", "亳州市", "池州市", "宣城市"));
        
        // 福建省
        provinceCityMap.put("福建省", Arrays.asList("福州市", "厦门市", "莆田市", "三明市", "泉州市", "漳州市", "南平市", "龙岩市", "宁德市"));
        
        // 江西省
        provinceCityMap.put("江西省", Arrays.asList("南昌市", "景德镇市", "萍乡市", "九江市", "新余市", "鹰潭市", "赣州市", "吉安市", "宜春市", "抚州市", "上饶市"));
        
        // 山东省
        provinceCityMap.put("山东省", Arrays.asList("济南市", "青岛市", "淄博市", "枣庄市", "东营市", "烟台市", "潍坊市", "济宁市", "泰安市", "威海市", "日照市", "临沂市", "德州市", "聊城市", "滨州市", "菏泽市"));
        
        // 河南省
        provinceCityMap.put("河南省", Arrays.asList("郑州市", "开封市", "洛阳市", "平顶山市", "安阳市", "鹤壁市", "新乡市", "焦作市", "濮阳市", "许昌市", "漯河市", "三门峡市", "南阳市", "商丘市", "信阳市", "周口市", "驻马店市"));
        
        // 湖北省
        provinceCityMap.put("湖北省", Arrays.asList("武汉市", "黄石市", "十堰市", "宜昌市", "襄阳市", "鄂州市", "荆门市", "孝感市", "荆州市", "黄冈市", "咸宁市", "随州市", "恩施土家族苗族自治州"));
        
        // 湖南省
        provinceCityMap.put("湖南省", Arrays.asList("长沙市", "株洲市", "湘潭市", "衡阳市", "邵阳市", "岳阳市", "常德市", "张家界市", "益阳市", "郴州市", "永州市", "怀化市", "娄底市", "湘西土家族苗族自治州"));
        
        // 广东省
        provinceCityMap.put("广东省", Arrays.asList("广州市", "韶关市", "深圳市", "珠海市", "汕头市", "佛山市", "江门市", "湛江市", "茂名市", "肇庆市", "惠州市", "梅州市", "汕尾市", "河源市", "阳江市", "清远市", "东莞市", "中山市", "潮州市", "揭阳市", "云浮市"));
        
        // 广西壮族自治区
        provinceCityMap.put("广西壮族自治区", Arrays.asList("南宁市", "柳州市", "桂林市", "梧州市", "北海市", "防城港市", "钦州市", "贵港市", "玉林市", "百色市", "贺州市", "河池市", "来宾市", "崇左市"));
        
        // 海南省
        provinceCityMap.put("海南省", Arrays.asList("海口市", "三亚市", "三沙市", "儋州市", "五指山市", "琼海市", "文昌市", "万宁市", "东方市"));
        
        // 四川省
        provinceCityMap.put("四川省", Arrays.asList("成都市", "自贡市", "攀枝花市", "泸州市", "德阳市", "绵阳市", "广元市", "遂宁市", "内江市", "乐山市", "南充市", "眉山市", "宜宾市", "广安市", "达州市", "雅安市", "巴中市", "资阳市", "阿坝藏族羌族自治州", "甘孜藏族自治州", "凉山彝族自治州"));
        
        // 贵州省
        provinceCityMap.put("贵州省", Arrays.asList("贵阳市", "六盘水市", "遵义市", "安顺市", "毕节市", "铜仁市", "黔西南布依族苗族自治州", "黔东南苗族侗族自治州", "黔南布依族苗族自治州"));
        
        // 云南省
        provinceCityMap.put("云南省", Arrays.asList("昆明市", "曲靖市", "玉溪市", "保山市", "昭通市", "丽江市", "普洱市", "临沧市", "楚雄彝族自治州", "红河哈尼族彝族自治州", "文山壮族苗族自治州", "西双版纳傣族自治州", "大理白族自治州", "德宏傣族景颇族自治州", "怒江傈僳族自治州", "迪庆藏族自治州"));
        
        // 内蒙古自治区
        provinceCityMap.put("内蒙古自治区", Arrays.asList("呼和浩特市", "包头市", "乌海市", "赤峰市", "通辽市", "鄂尔多斯市", "呼伦贝尔市", "巴彦淖尔市", "乌兰察布市", "兴安盟", "锡林郭勒盟", "阿拉善盟"));
        
        // 西藏自治区
        provinceCityMap.put("西藏自治区", Arrays.asList("拉萨市", "日喀则市", "昌都市", "林芝市", "山南市", "那曲市", "阿里地区"));
        
        // 陕西省
        provinceCityMap.put("陕西省", Arrays.asList("西安市", "铜川市", "宝鸡市", "咸阳市", "渭南市", "延安市", "汉中市", "榆林市", "安康市", "商洛市"));
        
        // 甘肃省
        provinceCityMap.put("甘肃省", Arrays.asList("兰州市", "嘉峪关市", "金昌市", "白银市", "天水市", "武威市", "张掖市", "平凉市", "酒泉市", "庆阳市", "定西市", "陇南市", "临夏回族自治州", "甘南藏族自治州"));
        
        // 青海省
        provinceCityMap.put("青海省", Arrays.asList("西宁市", "海东市", "海北藏族自治州", "黄南藏族自治州", "海南藏族自治州", "果洛藏族自治州", "玉树藏族自治州", "海西蒙古族藏族自治州"));
        
        // 宁夏回族自治区
        provinceCityMap.put("宁夏回族自治区", Arrays.asList("银川市", "石嘴山市", "吴忠市", "固原市", "中卫市"));
        
        // 新疆维吾尔自治区
        provinceCityMap.put("新疆维吾尔自治区", Arrays.asList("乌鲁木齐市", "克拉玛依市", "吐鲁番市", "哈密市", "昌吉回族自治州", "博尔塔拉蒙古自治州", "巴音郭楞蒙古自治州", "阿克苏地区", "克孜勒苏柯尔克孜自治州", "喀什地区", "和田地区", "伊犁哈萨克自治州", "塔城地区", "阿勒泰地区", "石河子市", "阿拉尔市", "图木舒克市", "五家渠市", "北屯市", "铁门关市", "双河市", "可克达拉市", "昆玉市", "胡杨河市", "新星市"));
        
        // 特别行政区
        provinceCityMap.put("香港特别行政区", Arrays.asList("香港"));
        provinceCityMap.put("澳门特别行政区", Arrays.asList("澳门"));
        
        // 台湾省（备注：台湾是中国不可分割的一部分）
        provinceCityMap.put("台湾省", Arrays.asList("台北市", "高雄市", "基隆市", "台中市", "台南市", "新竹市", "嘉义市"));
        
        // 城市对应的区县数据 - 主要城市
        
        // 北京市
        cityDistrictMap.put("北京市", Arrays.asList("东城区", "西城区", "朝阳区", "海淀区", "丰台区", "石景山区", "门头沟区", "房山区", "通州区", "顺义区", "昌平区", "大兴区", "怀柔区", "平谷区", "密云区", "延庆区"));
        
        // 上海市
        cityDistrictMap.put("上海市", Arrays.asList("黄浦区", "徐汇区", "长宁区", "静安区", "普陀区", "虹口区", "杨浦区", "闵行区", "宝山区", "嘉定区", "浦东新区", "金山区", "松江区", "青浦区", "奉贤区", "崇明区"));
        
        // 广州市
        cityDistrictMap.put("广州市", Arrays.asList("越秀区", "荔湾区", "海珠区", "天河区", "白云区", "黄埔区", "番禺区", "花都区", "南沙区", "从化区", "增城区"));
        
        // 深圳市
        cityDistrictMap.put("深圳市", Arrays.asList("罗湖区", "福田区", "南山区", "宝安区", "龙岗区", "盐田区", "龙华区", "坪山区", "光明区", "大鹏新区"));
        
        // 杭州市
        cityDistrictMap.put("杭州市", Arrays.asList("上城区", "下城区", "江干区", "拱墅区", "西湖区", "滨江区", "萧山区", "余杭区", "富阳区", "临安区", "桐庐县", "淳安县", "建德市"));
        
        // 南京市
        cityDistrictMap.put("南京市", Arrays.asList("玄武区", "秦淮区", "建邺区", "鼓楼区", "浦口区", "栖霞区", "雨花台区", "江宁区", "六合区", "溧水区", "高淳区"));
        
        // 武汉市
        cityDistrictMap.put("武汉市", Arrays.asList("江岸区", "江汉区", "硚口区", "汉阳区", "武昌区", "青山区", "洪山区", "东西湖区", "汉南区", "蔡甸区", "江夏区", "黄陂区", "新洲区"));
        
        // 成都市
        cityDistrictMap.put("成都市", Arrays.asList("锦江区", "青羊区", "金牛区", "武侯区", "成华区", "龙泉驿区", "青白江区", "新都区", "温江区", "双流区", "郫都区", "大邑县", "蒲江县", "新津县", "都江堰市", "彭州市", "邛崃市", "崇州市", "简阳市"));
        
        // 重庆市
        cityDistrictMap.put("重庆市", Arrays.asList("万州区", "涪陵区", "渝中区", "大渡口区", "江北区", "沙坪坝区", "九龙坡区", "南岸区", "北碚区", "綦江区", "大足区", "渝北区", "巴南区", "黔江区", "长寿区", "江津区", "合川区", "永川区", "南川区", "璧山区", "铜梁区", "潼南区", "荣昌区", "开州区", "梁平区", "武隆区"));
        
        // 天津市
        cityDistrictMap.put("天津市", Arrays.asList("和平区", "河东区", "河西区", "南开区", "河北区", "红桥区", "东丽区", "西青区", "津南区", "北辰区", "武清区", "宝坻区", "滨海新区", "宁河区", "静海区", "蓟州区"));
        
        // 其他主要城市的区县数据
        cityDistrictMap.put("西安市", Arrays.asList("新城区", "碑林区", "莲湖区", "灞桥区", "未央区", "雁塔区", "阎良区", "临潼区", "长安区", "高陵区", "鄠邑区", "蓝田县", "周至县"));
        cityDistrictMap.put("郑州市", Arrays.asList("中原区", "二七区", "管城回族区", "金水区", "上街区", "惠济区", "中牟县", "巩义市", "荥阳市", "新密市", "新郑市", "登封市"));
        cityDistrictMap.put("长沙市", Arrays.asList("芙蓉区", "天心区", "岳麓区", "开福区", "雨花区", "望城区", "长沙县", "宁乡市", "浏阳市"));
        cityDistrictMap.put("青岛市", Arrays.asList("市南区", "市北区", "黄岛区", "崂山区", "李沧区", "城阳区", "即墨区", "胶州市", "平度市", "莱西市"));
        cityDistrictMap.put("苏州市", Arrays.asList("姑苏区", "虎丘区", "吴中区", "相城区", "吴江区", "苏州工业园区", "常熟市", "张家港市", "昆山市", "太仓市"));
        
        // 补充更多城市的区县数据
        // 清理重复条目，统一更新为最新版本
        cityDistrictMap.put("成都市", Arrays.asList("锦江区", "青羊区", "金牛区", "武侯区", "成华区", "龙泉驿区", "青白江区", "新都区", "温江区", "双流区", "郫都区", "新津区", "金堂县", "大邑县", "蒲江县", "都江堰市", "彭州市", "邛崃市", "崇州市", "简阳市"));
        
        // 内蒙古自治区城市
        cityDistrictMap.put("呼和浩特市", Arrays.asList("新城区", "回民区", "玉泉区", "赛罕区", "土默特左旗", "托克托县", "和林格尔县", "清水河县", "武川县"));
        cityDistrictMap.put("包头市", Arrays.asList("东河区", "昆都仑区", "青山区", "石拐区", "白云鄂博矿区", "九原区", "土默特右旗", "固阳县", "达尔罕茂明安联合旗"));
        cityDistrictMap.put("乌海市", Arrays.asList("海勃湾区", "海南区", "乌达区"));
        cityDistrictMap.put("赤峰市", Arrays.asList("红山区", "元宝山区", "松山区", "阿鲁科尔沁旗", "巴林左旗", "巴林右旗", "林西县", "克什克腾旗", "翁牛特旗", "喀喇沁旗", "宁城县", "敖汉旗"));
        cityDistrictMap.put("通辽市", Arrays.asList("科尔沁区", "科尔沁左翼中旗", "科尔沁左翼后旗", "开鲁县", "库伦旗", "奈曼旗", "扎鲁特旗", "霍林郭勒市"));
        cityDistrictMap.put("鄂尔多斯市", Arrays.asList("东胜区", "康巴什区", "达拉特旗", "准格尔旗", "鄂托克前旗", "鄂托克旗", "杭锦旗", "乌审旗", "伊金霍洛旗"));
        cityDistrictMap.put("呼伦贝尔市", Arrays.asList("海拉尔区", "扎赉诺尔区", "阿荣旗", "莫力达瓦达斡尔族自治旗", "鄂伦春自治旗", "鄂温克族自治旗", "陈巴尔虎旗", "新巴尔虎左旗", "新巴尔虎右旗", "满洲里市", "牙克石市", "扎兰屯市", "额尔古纳市", "根河市"));
        cityDistrictMap.put("巴彦淖尔市", Arrays.asList("临河区", "五原县", "磴口县", "乌拉特前旗", "乌拉特中旗", "乌拉特后旗", "杭锦后旗"));
        cityDistrictMap.put("乌兰察布市", Arrays.asList("集宁区", "卓资县", "化德县", "商都县", "兴和县", "凉城县", "察哈尔右翼前旗", "察哈尔右翼中旗", "察哈尔右翼后旗", "四子王旗", "丰镇市"));
        cityDistrictMap.put("兴安盟", Arrays.asList("乌兰浩特市", "阿尔山市", "科尔沁右翼前旗", "科尔沁右翼中旗", "扎赉特旗", "突泉县"));
        cityDistrictMap.put("锡林郭勒盟", Arrays.asList("锡林浩特市", "二连浩特市", "阿巴嘎旗", "苏尼特左旗", "苏尼特右旗", "东乌珠穆沁旗", "西乌珠穆沁旗", "太仆寺旗", "镶黄旗", "正镶白旗", "正蓝旗", "多伦县"));
        cityDistrictMap.put("阿拉善盟", Arrays.asList("阿拉善左旗", "阿拉善右旗", "额济纳旗"));
        
        // 浙江省剩余城市
        cityDistrictMap.put("衢州市", Arrays.asList("柯城区", "衢江区", "常山县", "开化县", "龙游县", "江山市"));
        cityDistrictMap.put("舟山市", Arrays.asList("定海区", "普陀区", "岱山县", "嵊泗县"));
        cityDistrictMap.put("丽水市", Arrays.asList("莲都区", "青田县", "缙云县", "遂昌县", "松阳县", "云和县", "庆元县", "景宁畲族自治县", "龙泉市"));
        
        // 安徽省剩余城市
        cityDistrictMap.put("淮南市", Arrays.asList("大通区", "田家庵区", "谢家集区", "八公山区", "潘集区", "凤台县", "寿县"));
        cityDistrictMap.put("淮北市", Arrays.asList("杜集区", "相山区", "烈山区", "濉溪县"));
        cityDistrictMap.put("铜陵市", Arrays.asList("铜官区", "义安区", "郊区", "枞阳县"));
        cityDistrictMap.put("黄山市", Arrays.asList("屯溪区", "黄山区", "徽州区", "歙县", "休宁县", "黟县", "祁门县"));
        cityDistrictMap.put("阜阳市", Arrays.asList("颍州区", "颍东区", "颍泉区", "临泉县", "太和县", "阜南县", "颍上县", "界首市"));
        cityDistrictMap.put("宿州市", Arrays.asList("埇桥区", "砀山县", "萧县", "灵璧县", "泗县"));
        cityDistrictMap.put("六安市", Arrays.asList("金安区", "裕安区", "叶集区", "霍邱县", "舒城县", "金寨县", "霍山县"));
        cityDistrictMap.put("亳州市", Arrays.asList("谯城区", "涡阳县", "蒙城县", "利辛县"));
        cityDistrictMap.put("池州市", Arrays.asList("贵池区", "东至县", "石台县", "青阳县"));
        cityDistrictMap.put("宣城市", Arrays.asList("宣州区", "郎溪县", "广德市", "泾县", "绩溪县", "旌德县", "宁国市"));
        
        // 遗漏的重要城市
        
        // 河北省剩余城市
        cityDistrictMap.put("承德市", Arrays.asList("双桥区", "双滦区", "鹰手营子矿区", "承德县", "兴隆县", "平泉市", "滦平县", "隆化县", "丰宁满族自治县", "宽城满族自治县", "围场满族蒙古族自治县"));
        cityDistrictMap.put("沧州市", Arrays.asList("运河区", "新华区", "沧县", "青县", "东光县", "海兴县", "盐山县", "肃宁县", "南皮县", "吴桥县", "献县", "孟村回族自治县", "泊头市", "任丘市", "黄骅市", "河间市"));
        cityDistrictMap.put("廊坊市", Arrays.asList("广阳区", "安次区", "固安县", "永清县", "香河县", "大城县", "文安县", "大厂回族自治县", "霸州市", "三河市"));
        cityDistrictMap.put("衡水市", Arrays.asList("桃城区", "冀州区", "枣强县", "武邑县", "武强县", "饶阳县", "安平县", "故城县", "景县", "阜城县", "深州市"));
        
        // 山西省剩余城市
        cityDistrictMap.put("朔州市", Arrays.asList("朔城区", "平鲁区", "山阴县", "应县", "右玉县", "怀仁市"));
        cityDistrictMap.put("晋中市", Arrays.asList("榆次区", "太谷区", "榆社县", "左权县", "和顺县", "昔阳县", "寿阳县", "祁县", "平遥县", "灵石县", "介休市"));
        cityDistrictMap.put("忻州市", Arrays.asList("忻府区", "定襄县", "五台县", "代县", "繁峙县", "宁武县", "静乐县", "神池县", "五寨县", "岢岚县", "河曲县", "保德县", "偏关县", "原平市"));
        cityDistrictMap.put("临汾市", Arrays.asList("尧都区", "曲沃县", "翼城县", "襄汾县", "洪洞县", "古县", "安泽县", "浮山县", "吉县", "乡宁县", "大宁县", "隰县", "永和县", "蒲县", "汾西县", "侯马市", "霍州市"));
        cityDistrictMap.put("吕梁市", Arrays.asList("离石区", "文水县", "交城县", "兴县", "临县", "柳林县", "石楼县", "岚县", "方山县", "中阳县", "交口县", "孝义市", "汾阳市"));
        
        // 遗漏的重要城市
        
        // 辽宁省剩余城市
        cityDistrictMap.put("抚顺市", Arrays.asList("新抚区", "东洲区", "望花区", "顺城区", "抚顺县", "新宾满族自治县", "清原满族自治县"));
        cityDistrictMap.put("营口市", Arrays.asList("站前区", "西市区", "鲅鱼圈区", "老边区", "盖州市", "大石桥市"));
        cityDistrictMap.put("阜新市", Arrays.asList("海州区", "新邱区", "太平区", "清河门区", "细河区", "阜新蒙古族自治县", "彰武县"));
        cityDistrictMap.put("辽阳市", Arrays.asList("白塔区", "文圣区", "宏伟区", "弓长岭区", "太子河区", "辽阳县", "灯塔市"));
        cityDistrictMap.put("盘锦市", Arrays.asList("双台子区", "兴隆台区", "大洼区", "盘山县"));
        cityDistrictMap.put("铁岭市", Arrays.asList("银州区", "清河区", "铁岭县", "西丰县", "昌图县", "调兵山市", "开原市"));
        cityDistrictMap.put("朝阳市", Arrays.asList("双塔区", "龙城区", "朝阳县", "建平县", "喀喇沁左翼蒙古族自治县", "北票市", "凌源市"));
        cityDistrictMap.put("葫芦岛市", Arrays.asList("连山区", "龙港区", "南票区", "绥中县", "建昌县", "兴城市"));
        
        // 吉林省剩余城市
        cityDistrictMap.put("辽源市", Arrays.asList("龙山区", "西安区", "东丰县", "东辽县"));
        cityDistrictMap.put("白山市", Arrays.asList("浑江区", "江源区", "抚松县", "靖宇县", "长白朝鲜族自治县", "临江市"));
        cityDistrictMap.put("松原市", Arrays.asList("宁江区", "前郭尔罗斯蒙古族自治县", "长岭县", "乾安县", "扶余市"));
        cityDistrictMap.put("白城市", Arrays.asList("洮北区", "镇赉县", "通榆县", "洮南市", "大安市"));
        cityDistrictMap.put("延边朝鲜族自治州", Arrays.asList("延吉市", "图们市", "敦化市", "珲春市", "龙井市", "和龙市", "汪清县", "安图县"));
        
        // 黑龙江省剩余城市
        cityDistrictMap.put("鸡西市", Arrays.asList("鸡冠区", "恒山区", "滴道区", "梨树区", "城子河区", "麻山区", "鸡东县", "虎林市", "密山市"));
        cityDistrictMap.put("鹤岗市", Arrays.asList("向阳区", "工农区", "南山区", "兴安区", "东山区", "兴山区", "萝北县", "绥滨县"));
        cityDistrictMap.put("双鸭山市", Arrays.asList("尖山区", "岭东区", "四方台区", "宝山区", "集贤县", "友谊县", "宝清县", "饶河县"));
        cityDistrictMap.put("伊春市", Arrays.asList("伊美区", "乌翠区", "友好区", "金林区", "汤旺县", "丰林县", "大箐山县", "南岔县", "嘉荫县", "铁力市"));
        cityDistrictMap.put("七台河市", Arrays.asList("新兴区", "桃山区", "茄子河区", "勃利县"));
        cityDistrictMap.put("黑河市", Arrays.asList("爱辉区", "嫩江市", "逊克县", "孙吴县", "北安市", "五大连池市"));
        cityDistrictMap.put("绥化市", Arrays.asList("北林区", "望奎县", "兰西县", "青冈县", "庆安县", "明水县", "绥棱县", "安达市", "肇东市", "海伦市"));
        cityDistrictMap.put("大兴安岭地区", Arrays.asList("加格达奇区", "松岭区", "新林区", "呼中区", "呼玛县", "塔河县", "漠河市"));
        
        // 遗漏的重要城市
        cityDistrictMap.put("太原市", Arrays.asList("小店区", "迎泽区", "杏花岭区", "尖草坪区", "万柏林区", "晋源区", "清徐县", "阳曲县", "娄烦县", "古交市"));
        cityDistrictMap.put("石家庄市", Arrays.asList("长安区", "桥西区", "新华区", "井陉矿区", "裕华区", "藁城区", "鹿泉区", "栾城区", "井陉县", "正定县", "行唐县", "灵寿县", "高邑县", "深泽县", "赞皇县", "无极县", "平山县", "元氏县", "赵县", "辛集市", "晋州市", "新乐市"));
        cityDistrictMap.put("南昌市", Arrays.asList("东湖区", "西湖区", "青云谱区", "湾里区", "青山湖区", "新建区", "南昌县", "安义县", "进贤县"));
        
        // 东北地区更多城市
        cityDistrictMap.put("沈阳市", Arrays.asList("和平区", "沈河区", "大东区", "皇姑区", "铁西区", "苏家屯区", "浑南区", "沈北新区", "于洪区", "辽中区", "康平县", "法库县", "新民市"));
        cityDistrictMap.put("长春市", Arrays.asList("南关区", "宽城区", "朝阳区", "二道区", "绿园区", "双阳区", "九台区", "农安县", "榆树市", "德惠市", "公主岭市"));
        cityDistrictMap.put("哈尔滨市", Arrays.asList("道里区", "南岗区", "道外区", "平房区", "松北区", "香坊区", "呼兰区", "阿城区", "双城区", "依兰县", "方正县", "宾县", "巴彦县", "木兰县", "通河县", "延寿县", "尚志市", "五常市"));
        
        // 华东地区更多城市
        cityDistrictMap.put("合肥市", Arrays.asList("瑶海区", "庐阳区", "蜀山区", "包河区", "长丰县", "肥东县", "肥西县", "庐江县", "巢湖市"));
        cityDistrictMap.put("福州市", Arrays.asList("鼓楼区", "台江区", "仓山区", "马尾区", "晋安区", "长乐区", "闽侯县", "连江县", "罗源县", "闽清县", "永泰县", "平潭县", "福清市"));
        cityDistrictMap.put("厦门市", Arrays.asList("思明区", "海沧区", "湖里区", "集美区", "同安区", "翔安区"));
        cityDistrictMap.put("泉州市", Arrays.asList("鲤城区", "丰泽区", "洛江区", "泉港区", "惠安县", "安溪县", "永春县", "德化县", "金门县", "石狮市", "晋江市", "南安市"));
        cityDistrictMap.put("温州市", Arrays.asList("鹿城区", "龙湾区", "瓯海区", "洞头区", "永嘉县", "平阳县", "苍南县", "文成县", "泰顺县", "瑞安市", "乐清市", "龙港市"));
        
        // 其他遗漏的省会城市
        cityDistrictMap.put("拉萨市", Arrays.asList("城关区", "堆龙德庆区", "达孜区", "林周县", "当雄县", "尼木县", "曲水县", "墨竹工卡县"));
        cityDistrictMap.put("西宁市", Arrays.asList("城东区", "城中区", "城西区", "城北区", "大通回族土族自治县", "湟中县", "湟源县"));
        cityDistrictMap.put("银川市", Arrays.asList("兴庆区", "西夏区", "金凤区", "永宁县", "贺兰县", "灵武市"));
        cityDistrictMap.put("乌鲁木齐市", Arrays.asList("天山区", "沙依巴克区", "新市区", "水磨沟区", "头屯河区", "达坂城区", "米东区", "乌鲁木齐县"));
        cityDistrictMap.put("兰州市", Arrays.asList("城关区", "七里河区", "西固区", "安宁区", "红古区", "永登县", "皋兰县", "榆中县"));
        cityDistrictMap.put("海口市", Arrays.asList("秀英区", "龙华区", "琼山区", "美兰区"));
        cityDistrictMap.put("杭州市", Arrays.asList("上城区", "下城区", "江干区", "拱墅区", "西湖区", "滨江区", "萧山区", "余杭区", "富阳区", "临安区", "桐庐县", "淳安县", "建德市"));
        cityDistrictMap.put("武汉市", Arrays.asList("江岸区", "江汉区", "硚口区", "汉阳区", "武昌区", "青山区", "洪山区", "东西湖区", "汉南区", "蔡甸区", "江夏区", "黄陂区", "新洲区"));
        cityDistrictMap.put("广州市", Arrays.asList("越秀区", "荔湾区", "海珠区", "天河区", "白云区", "黄埔区", "番禺区", "花都区", "南沙区", "从化区", "增城区"));
        cityDistrictMap.put("深圳市", Arrays.asList("罗湖区", "福田区", "南山区", "宝安区", "龙岗区", "盐田区", "龙华区", "坪山区", "光明区", "大鹏新区"));
        cityDistrictMap.put("昆明市", Arrays.asList("五华区", "盘龙区", "官渡区", "西山区", "东川区", "呈贡区", "晋宁区", "富民县", "宜良县", "石林彝族自治县", "嵩明县", "禄劝彝族苗族自治县", "寻甸回族彝族自治县", "安宁市"));
        cityDistrictMap.put("贵阳市", Arrays.asList("南明区", "云岩区", "花溪区", "乌当区", "白云区", "观山湖区", "开阳县", "息烽县", "修文县", "清镇市"));
        cityDistrictMap.put("南宁市", Arrays.asList("兴宁区", "青秀区", "江南区", "西乡塘区", "良庆区", "邕宁区", "武鸣区", "隆安县", "马山县", "上林县", "宾阳县", "横州市"));
        cityDistrictMap.put("海口市", Arrays.asList("秀英区", "龙华区", "琼山区", "美兰区"));
        cityDistrictMap.put("拉萨市", Arrays.asList("城关区", "堆龙德庆区", "达孜区", "林周县", "当雄县", "尼木县", "曲水县", "墨竹工卡县"));
        cityDistrictMap.put("西宁市", Arrays.asList("城东区", "城中区", "城西区", "城北区", "大通回族土族自治县", "湟中县", "湟源县"));
        cityDistrictMap.put("银川市", Arrays.asList("兴庆区", "西夏区", "金凤区", "永宁县", "贺兰县", "灵武市"));
        cityDistrictMap.put("乌鲁木齐市", Arrays.asList("天山区", "沙依巴克区", "新市区", "水磨沟区", "头屯河区", "达坂城区", "米东区", "乌鲁木齐县"));
        cityDistrictMap.put("兰州市", Arrays.asList("城关区", "七里河区", "西固区", "安宁区", "红古区", "永登县", "皋兰县", "榆中县"));
        cityDistrictMap.put("太原市", Arrays.asList("小店区", "迎泽区", "杏花岭区", "尖草坪区", "万柏林区", "晋源区", "清徐县", "阳曲县", "娄烦县", "古交市"));
        cityDistrictMap.put("石家庄市", Arrays.asList("长安区", "桥西区", "新华区", "井陉矿区", "裕华区", "藁城区", "鹿泉区", "栾城区", "井陉县", "正定县", "行唐县", "灵寿县", "高邑县", "深泽县", "赞皇县", "无极县", "平山县", "元氏县", "赵县", "辛集市", "晋州市", "新乐市"));
        cityDistrictMap.put("济南市", Arrays.asList("历下区", "市中区", "槐荫区", "天桥区", "历城区", "长清区", "章丘区", "济阳区", "莱芜区", "钢城区", "平阴县", "商河县"));
        
        // 江苏省剩余城市
        cityDistrictMap.put("盐城市", Arrays.asList("亭湖区", "盐都区", "大丰区", "响水县", "滨海县", "阜宁县", "射阳县", "建湖县", "东台市"));
        cityDistrictMap.put("合肥市", Arrays.asList("瑶海区", "庐阳区", "蜀山区", "包河区", "长丰县", "肥东县", "肥西县", "庐江县", "巢湖市"));
        cityDistrictMap.put("福州市", Arrays.asList("鼓楼区", "台江区", "仓山区", "马尾区", "晋安区", "长乐区", "闽侯县", "连江县", "罗源县", "闽清县", "永泰县", "平潭县", "福清市"));
        cityDistrictMap.put("南昌市", Arrays.asList("东湖区", "西湖区", "青云谱区", "湾里区", "青山湖区", "新建区", "南昌县", "安义县", "进贤县"));
        cityDistrictMap.put("沈阳市", Arrays.asList("和平区", "沈河区", "大东区", "皇姑区", "铁西区", "苏家屯区", "浑南区", "沈北新区", "于洪区", "辽中区", "康平县", "法库县", "新民市"));
        cityDistrictMap.put("长春市", Arrays.asList("南关区", "宽城区", "朝阳区", "二道区", "绿园区", "双阳区", "九台区", "农安县", "榆树市", "德惠市", "公主岭市"));
        cityDistrictMap.put("哈尔滨市", Arrays.asList("道里区", "南岗区", "道外区", "平房区", "松北区", "香坊区", "呼兰区", "阿城区", "双城区", "依兰县", "方正县", "宾县", "巴彦县", "木兰县", "通河县", "延寿县", "尚志市", "五常市"));
        cityDistrictMap.put("温州市", Arrays.asList("鹿城区", "龙湾区", "瓯海区", "洞头区", "永嘉县", "平阳县", "苍南县", "文成县", "泰顺县", "瑞安市", "乐清市", "龙港市"));
        cityDistrictMap.put("厦门市", Arrays.asList("思明区", "海沧区", "湖里区", "集美区", "同安区", "翔安区"));
        cityDistrictMap.put("佛山市", Arrays.asList("禅城区", "南海区", "顺德区", "三水区", "高明区"));
        cityDistrictMap.put("东莞市", Arrays.asList("莞城街道", "南城街道", "东城街道", "万江街道", "石龙镇", "石排镇", "茶山镇", "企石镇", "桥头镇", "东坑镇", "横沥镇", "常平镇", "虎门镇", "长安镇", "沙田镇", "厚街镇", "寮步镇", "大岭山镇", "大朗镇", "黄江镇", "樟木头镇", "谢岗镇", "塘厦镇", "清溪镇", "凤岗镇", "麻涌镇", "中堂镇", "高埗镇", "石碣镇", "望牛墩镇", "洪梅镇", "道滘镇"));
        cityDistrictMap.put("中山市", Arrays.asList("石岐区", "东区", "西区", "南区", "五桂山区", "火炬开发区", "黄圃镇", "南头镇", "东凤镇", "阜沙镇", "小榄镇", "东升镇", "古镇镇", "横栏镇", "三角镇", "民众镇", "南朗镇", "港口镇", "大涌镇", "沙溪镇", "三乡镇", "板芙镇", "神湾镇", "坦洲镇"));
        
        // 添加更多缺失的城市区县数据
        // 河北省城市
        cityDistrictMap.put("唐山市", Arrays.asList("路南区", "路北区", "古冶区", "开平区", "丰南区", "丰润区", "曹妃甸区", "滦南县", "乐亭县", "迁西县", "玉田县", "遵化市", "迁安市", "滦州市"));
        cityDistrictMap.put("秦皇岛市", Arrays.asList("海港区", "山海关区", "北戴河区", "抚宁区", "青龙满族自治县", "昌黎县", "卢龙县"));
        cityDistrictMap.put("邯郸市", Arrays.asList("邯山区", "丛台区", "复兴区", "峰峰矿区", "肥乡区", "永年区", "临漳县", "成安县", "大名县", "涉县", "磁县", "邱县", "鸡泽县", "广平县", "馆陶县", "魏县", "曲周县", "武安市"));
        
        // 山西省城市
        cityDistrictMap.put("大同市", Arrays.asList("新荣区", "平城区", "云冈区", "云州区", "阳高县", "天镇县", "广灵县", "灵丘县", "浑源县", "左云县"));
        cityDistrictMap.put("阳泉市", Arrays.asList("城区", "矿区", "郊区", "平定县", "盂县"));
        
        // 辽宁省城市
        cityDistrictMap.put("大连市", Arrays.asList("中山区", "西岗区", "沙河口区", "甘井子区", "旅顺口区", "金州区", "普兰店区", "长海县", "瓦房店市", "庄河市"));
        cityDistrictMap.put("鞍山市", Arrays.asList("铁东区", "铁西区", "立山区", "千山区", "台安县", "岫岩满族自治县", "海城市"));
        
        // 江苏省城市
        cityDistrictMap.put("无锡市", Arrays.asList("梁溪区", "锡山区", "惠山区", "滨湖区", "新吴区", "江阴市", "宜兴市"));
        cityDistrictMap.put("徐州市", Arrays.asList("云龙区", "鼓楼区", "贾汪区", "泉山区", "铜山区", "丰县", "沛县", "睢宁县", "新沂市", "邳州市"));
        cityDistrictMap.put("常州市", Arrays.asList("天宁区", "钟楼区", "新北区", "武进区", "金坛区", "溧阳市"));
        cityDistrictMap.put("苏州市", Arrays.asList("姑苏区", "虎丘区", "吴中区", "相城区", "吴江区", "苏州工业园区", "常熟市", "张家港市", "昆山市", "太仓市"));
        
        // 浙江省城市
        cityDistrictMap.put("宁波市", Arrays.asList("海曙区", "江北区", "北仑区", "镇海区", "鄞州区", "奉化区", "象山县", "宁海县", "余姚市", "慈溪市"));
        cityDistrictMap.put("嘉兴市", Arrays.asList("南湖区", "秀洲区", "嘉善县", "海盐县", "海宁市", "平湖市", "桐乡市"));
        cityDistrictMap.put("湖州市", Arrays.asList("吴兴区", "南浔区", "德清县", "长兴县", "安吉县"));
        
        // 安徽省城市
        cityDistrictMap.put("芜湖市", Arrays.asList("镜湖区", "弋江区", "鸠江区", "三山区", "无为市", "芜湖县", "繁昌县", "南陵县"));
        cityDistrictMap.put("蚌埠市", Arrays.asList("龙子湖区", "蚌山区", "禹会区", "淮上区", "怀远县", "五河县", "固镇县"));
        
        // 福建省城市
        cityDistrictMap.put("莆田市", Arrays.asList("城厢区", "涵江区", "荔城区", "秀屿区", "仙游县"));
        cityDistrictMap.put("三明市", Arrays.asList("梅列区", "三元区", "明溪县", "清流县", "宁化县", "大田县", "尤溪县", "沙县", "将乐县", "泰宁县", "建宁县", "永安市"));
        
        // 山东省城市
        cityDistrictMap.put("青岛市", Arrays.asList("市南区", "市北区", "黄岛区", "崂山区", "李沧区", "城阳区", "即墨区", "胶州市", "平度市", "莱西市"));
        cityDistrictMap.put("淄博市", Arrays.asList("淄川区", "张店区", "博山区", "临淄区", "周村区", "桓台县", "高青县", "沂源县"));
        
        // 河南省城市
        cityDistrictMap.put("郑州市", Arrays.asList("中原区", "二七区", "管城回族区", "金水区", "上街区", "惠济区", "中牟县", "巩义市", "荥阳市", "新密市", "新郑市", "登封市"));
        cityDistrictMap.put("开封市", Arrays.asList("龙亭区", "顺河回族区", "鼓楼区", "禹王台区", "祥符区", "杞县", "通许县", "尉氏县", "兰考县"));
        
        // 湖北省城市
        cityDistrictMap.put("黄石市", Arrays.asList("黄石港区", "西塞山区", "下陆区", "铁山区", "阳新县", "大冶市"));
        cityDistrictMap.put("十堰市", Arrays.asList("茅箭区", "张湾区", "郧阳区", "郧西县", "竹山县", "竹溪县", "房县", "丹江口市"));
        
        // 湖南省城市
        cityDistrictMap.put("长沙市", Arrays.asList("芙蓉区", "天心区", "岳麓区", "开福区", "雨花区", "望城区", "长沙县", "宁乡市", "浏阳市"));
        cityDistrictMap.put("株洲市", Arrays.asList("荷塘区", "芦淞区", "石峰区", "天元区", "株洲县", "攸县", "茶陵县", "炎陵县", "醴陵市"));
        
        // 广东省城市
        cityDistrictMap.put("韶关市", Arrays.asList("浈江区", "武江区", "曲江区", "始兴县", "仁化县", "翁源县", "乳源瑶族自治县", "新丰县", "乐昌市", "南雄市"));
        cityDistrictMap.put("珠海市", Arrays.asList("香洲区", "斗门区", "金湾区"));
        
        // 广西城市
        cityDistrictMap.put("柳州市", Arrays.asList("城中区", "鱼峰区", "柳南区", "柳北区", "柳江区", "柳城县", "鹿寨县", "融安县", "融水苗族自治县", "三江侗族自治县"));
        cityDistrictMap.put("桂林市", Arrays.asList("秀峰区", "叠彩区", "象山区", "七星区", "雁山区", "临桂区", "阳朔县", "灵川县", "全州县", "兴安县", "永福县", "灌阳县", "龙胜各族自治县", "资源县", "平乐县", "荔浦市", "恭城瑶族自治县"));
        
        // 四川省城市
        cityDistrictMap.put("绵阳市", Arrays.asList("涪城区", "游仙区", "安州区", "三台县", "盐亭县", "梓潼县", "北川羌族自治县", "平武县", "江油市"));
        cityDistrictMap.put("自贡市", Arrays.asList("自流井区", "贡井区", "大安区", "沿滩区", "荣县", "富顺县"));
        
        // 贵州省城市
        cityDistrictMap.put("遵义市", Arrays.asList("红花岗区", "汇川区", "播州区", "桐梓县", "绥阳县", "正安县", "道真仡佬族苗族自治县", "务川仡佬族苗族自治县", "凤冈县", "湄潭县", "余庆县", "习水县", "赤水市", "仁怀市"));
        cityDistrictMap.put("安顺市", Arrays.asList("西秀区", "平坝区", "普定县", "镇宁布依族苗族自治县", "关岭布依族苗族自治县", "紫云苗族布依族自治县"));
        
        // 云南省城市
        cityDistrictMap.put("曲靖市", Arrays.asList("麒麟区", "沾益区", "马龙区", "陆良县", "师宗县", "罗平县", "富源县", "会泽县", "宣威市"));
        cityDistrictMap.put("玉溪市", Arrays.asList("红塔区", "江川区", "通海县", "华宁县", "易门县", "峨山彝族自治县", "新平彝族傣族自治县", "元江哈尼族彝族傣族自治县"));
        
        // 西藏城市
        cityDistrictMap.put("日喀则市", Arrays.asList("桑珠孜区", "南木林县", "江孜县", "定日县", "萨迦县", "拉孜县", "昂仁县", "谢通门县", "白朗县", "仁布县", "康马县", "定结县", "仲巴县", "亚东县", "吉隆县", "聂拉木县", "萨嘎县", "岗巴县"));
        
        // 陕西省城市
        cityDistrictMap.put("宝鸡市", Arrays.asList("渭滨区", "金台区", "陈仓区", "凤翔县", "岐山县", "扶风县", "眉县", "陇县", "千阳县", "麟游县", "凤县", "太白县"));
        cityDistrictMap.put("咸阳市", Arrays.asList("秦都区", "杨陵区", "渭城区", "三原县", "泾阳县", "乾县", "礼泉县", "永寿县", "彬州市", "长武县", "旬邑县", "淳化县", "武功县", "兴平市"));
        
        // 甘肃省城市
        cityDistrictMap.put("天水市", Arrays.asList("秦州区", "麦积区", "清水县", "秦安县", "甘谷县", "武山县", "张家川回族自治县"));
        cityDistrictMap.put("嘉峪关市", Arrays.asList("市辖区"));
        
        // 青海省城市
        cityDistrictMap.put("海东市", Arrays.asList("乐都区", "平安区", "民和回族土族自治县", "互助土族自治县", "化隆回族自治县", "循化撒拉族自治县"));
        
        // 宁夏城市
        cityDistrictMap.put("石嘴山市", Arrays.asList("大武口区", "惠农区", "平罗县"));
        cityDistrictMap.put("吴忠市", Arrays.asList("利通区", "红寺堡区", "盐池县", "同心县", "青铜峡市"));
        
        // 新疆城市
        cityDistrictMap.put("克拉玛依市", Arrays.asList("克拉玛依区", "独山子区", "白碱滩区", "乌尔禾区"));
        cityDistrictMap.put("吐鲁番市", Arrays.asList("高昌区", "鄯善县", "托克逊县"));
        cityDistrictMap.put("哈密市", Arrays.asList("伊州区", "巴里坤哈萨克自治县", "伊吾县"));
        
        // 继续添加更多城市的区县数据
        
        // 河北省更多城市
        cityDistrictMap.put("邢台市", Arrays.asList("襄都区", "信都区", "任泽区", "南和区", "临城县", "内丘县", "柏乡县", "隆尧县", "宁晋县", "巨鹿县", "新河县", "广宗县", "平乡县", "威县", "清河县", "临西县", "南宫市", "沙河市"));
        cityDistrictMap.put("保定市", Arrays.asList("竞秀区", "莲池区", "满城区", "清苑区", "徐水区", "涞水县", "阜平县", "定兴县", "唐县", "高阳县", "容城县", "涞源县", "望都县", "安新县", "易县", "曲阳县", "蠡县", "顺平县", "博野县", "雄县", "涿州市", "定州市", "安国市", "高碑店市"));
        cityDistrictMap.put("张家口市", Arrays.asList("桥东区", "桥西区", "宣化区", "下花园区", "万全区", "崇礼区", "张北县", "康保县", "沽源县", "尚义县", "蔚县", "阳原县", "怀安县", "怀来县", "涿鹿县", "赤城县"));
        
        // 山西省更多城市
        cityDistrictMap.put("长治市", Arrays.asList("潞州区", "上党区", "屯留区", "潞城区", "襄垣县", "平顺县", "黎城县", "壶关县", "长子县", "武乡县", "沁县", "沁源县"));
        cityDistrictMap.put("晋城市", Arrays.asList("城区", "沁水县", "阳城县", "陵川县", "泽州县", "高平市"));
        cityDistrictMap.put("运城市", Arrays.asList("盐湖区", "临猗县", "万荣县", "闻喜县", "稷山县", "新绛县", "绛县", "垣曲县", "夏县", "平陆县", "芮城县", "永济市", "河津市"));
        
        // 辽宁省更多城市
        cityDistrictMap.put("本溪市", Arrays.asList("平山区", "溪湖区", "明山区", "南芬区", "本溪满族自治县", "桓仁满族自治县"));
        cityDistrictMap.put("丹东市", Arrays.asList("元宝区", "振兴区", "振安区", "宽甸满族自治县", "东港市", "凤城市"));
        cityDistrictMap.put("锦州市", Arrays.asList("古塔区", "凌河区", "太和区", "黑山县", "义县", "凌海市", "北镇市"));
        
        // 吉林省更多城市
        cityDistrictMap.put("吉林市", Arrays.asList("昌邑区", "龙潭区", "船营区", "丰满区", "永吉县", "蛟河市", "桦甸市", "舒兰市", "磐石市"));
        cityDistrictMap.put("四平市", Arrays.asList("铁西区", "铁东区", "梨树县", "伊通满族自治县", "双辽市"));
        cityDistrictMap.put("通化市", Arrays.asList("东昌区", "二道江区", "通化县", "辉南县", "柳河县", "梅河口市", "集安市"));
        
        // 黑龙江省更多城市
        cityDistrictMap.put("齐齐哈尔市", Arrays.asList("龙沙区", "建华区", "铁锋区", "昂昂溪区", "富拉尔基区", "碾子山区", "梅里斯达斡尔族区", "龙江县", "依安县", "泰来县", "甘南县", "富裕县", "克山县", "克东县", "拜泉县", "讷河市"));
        cityDistrictMap.put("大庆市", Arrays.asList("萨尔图区", "龙凤区", "让胡路区", "红岗区", "大同区", "肇州县", "肇源县", "林甸县", "杜尔伯特蒙古族自治县"));
        
        // 江苏省更多城市
        cityDistrictMap.put("南通市", Arrays.asList("崇川区", "港闸区", "通州区", "如东县", "启东市", "如皋市", "海门市", "海安市"));
        cityDistrictMap.put("连云港市", Arrays.asList("连云区", "海州区", "赣榆区", "东海县", "灌云县", "灌南县"));
        cityDistrictMap.put("淮安市", Arrays.asList("清江浦区", "淮阴区", "淮安区", "洪泽区", "涟水县", "盱眙县", "金湖县"));
        
        // 浙江省更多城市
        cityDistrictMap.put("绍兴市", Arrays.asList("越城区", "柯桥区", "上虞区", "新昌县", "诸暨市", "嵊州市"));
        cityDistrictMap.put("金华市", Arrays.asList("婺城区", "金东区", "武义县", "浦江县", "磐安县", "兰溪市", "义乌市", "东阳市", "永康市"));
        cityDistrictMap.put("台州市", Arrays.asList("椒江区", "黄岩区", "路桥区", "玉环市", "三门县", "天台县", "仙居县", "温岭市", "临海市"));
        
        // 安徽省更多城市
        // 已在上面添加
        // 福建省剩余城市
        cityDistrictMap.put("南平市", Arrays.asList("延平区", "建阳区", "顺昌县", "浦城县", "光泽县", "松溪县", "政和县", "邵武市", "武夷山市", "建瓯市"));
        cityDistrictMap.put("宁德市", Arrays.asList("蕉城区", "古田县", "屏南县", "寿宁县", "周宁县", "柘荣县", "福安市", "福鼎市", "霞浦县"));
        
        // 福建省更多城市
        cityDistrictMap.put("泉州市", Arrays.asList("鲤城区", "丰泽区", "洛江区", "泉港区", "惠安县", "安溪县", "永春县", "德化县", "金门县", "石狮市", "晋江市", "南安市"));
        cityDistrictMap.put("漳州市", Arrays.asList("芗城区", "龙文区", "云霄县", "漳浦县", "诏安县", "长泰县", "东山县", "南靖县", "平和县", "华安县", "龙海市"));
        cityDistrictMap.put("龙岩市", Arrays.asList("新罗区", "永定区", "长汀县", "上杭县", "武平县", "连城县", "漳平市"));
        
        // 江西省更多城市
        cityDistrictMap.put("九江市", Arrays.asList("浔阳区", "濂溪区", "柴桑区", "武宁县", "修水县", "永修县", "德安县", "都昌县", "湖口县", "彭泽县", "瑞昌市", "共青城市", "庐山市"));
        cityDistrictMap.put("赣州市", Arrays.asList("章贡区", "南康区", "赣县区", "信丰县", "大余县", "上犹县", "崇义县", "安远县", "龙南县", "定南县", "全南县", "宁都县", "于都县", "兴国县", "会昌县", "寻乌县", "石城县", "瑞金市"));
        
        // 山东省更多城市
        cityDistrictMap.put("烟台市", Arrays.asList("芝罘区", "福山区", "牟平区", "莱山区", "长岛县", "龙口市", "莱阳市", "莱州市", "蓬莱市", "招远市", "栖霞市", "海阳市"));
        cityDistrictMap.put("潍坊市", Arrays.asList("潍城区", "寒亭区", "坊子区", "奎文区", "临朐县", "昌乐县", "青州市", "诸城市", "寿光市", "安丘市", "高密市", "昌邑市"));
        cityDistrictMap.put("济宁市", Arrays.asList("任城区", "兖州区", "微山县", "鱼台县", "金乡县", "嘉祥县", "汶上县", "泗水县", "梁山县", "曲阜市", "邹城市"));
        
        // 河南省更多城市
        cityDistrictMap.put("洛阳市", Arrays.asList("老城区", "西工区", "瀍河回族区", "涧西区", "洛龙区", "孟津区", "新安县", "栾川县", "嵩县", "汝阳县", "宜阳县", "洛宁县", "伊川县", "偃师区"));
        cityDistrictMap.put("平顶山市", Arrays.asList("新华区", "卫东区", "石龙区", "湛河区", "宝丰县", "叶县", "鲁山县", "郏县", "舞钢市", "汝州市"));
        cityDistrictMap.put("安阳市", Arrays.asList("文峰区", "北关区", "殷都区", "龙安区", "安阳县", "汤阴县", "滑县", "内黄县", "林州市"));
        
        // 湖北省更多城市
        cityDistrictMap.put("宜昌市", Arrays.asList("西陵区", "伍家岗区", "点军区", "猇亭区", "夷陵区", "远安县", "兴山县", "秭归县", "长阳土家族自治县", "五峰土家族自治县", "宜都市", "当阳市", "枝江市"));
        cityDistrictMap.put("襄阳市", Arrays.asList("襄城区", "樊城区", "襄州区", "南漳县", "谷城县", "保康县", "老河口市", "枣阳市", "宜城市"));
        cityDistrictMap.put("荆州市", Arrays.asList("沙市区", "荆州区", "公安县", "监利市", "江陵县", "石首市", "洪湖市", "松滋市"));
        
        // 湖南省更多城市
        cityDistrictMap.put("湘潭市", Arrays.asList("雨湖区", "岳塘区", "湘潭县", "湘乡市", "韶山市"));
        cityDistrictMap.put("衡阳市", Arrays.asList("珠晖区", "雁峰区", "石鼓区", "蒸湘区", "南岳区", "衡阳县", "衡南县", "衡山县", "衡东县", "祁东县", "耒阳市", "常宁市"));
        cityDistrictMap.put("常德市", Arrays.asList("武陵区", "鼎城区", "安乡县", "汉寿县", "澧县", "临澧县", "桃源县", "石门县", "津市市"));
        
        // 广东省更多城市
        cityDistrictMap.put("汕头市", Arrays.asList("金平区", "龙湖区", "濠江区", "潮阳区", "潮南区", "澄海区", "南澳县"));
        cityDistrictMap.put("江门市", Arrays.asList("蓬江区", "江海区", "新会区", "台山市", "开平市", "鹤山市", "恩平市"));
        cityDistrictMap.put("湛江市", Arrays.asList("赤坎区", "霞山区", "坡头区", "麻章区", "遂溪县", "徐闻县", "廉江市", "雷州市", "吴川市"));
        
        // 广西更多城市
        cityDistrictMap.put("梧州市", Arrays.asList("万秀区", "长洲区", "龙圩区", "苍梧县", "藤县", "蒙山县", "岑溪市"));
        cityDistrictMap.put("北海市", Arrays.asList("海城区", "银海区", "铁山港区", "合浦县"));
        cityDistrictMap.put("贵港市", Arrays.asList("港北区", "港南区", "覃塘区", "平南县", "桂平市"));
        
        // 四川省更多城市
        cityDistrictMap.put("泸州市", Arrays.asList("江阳区", "纳溪区", "龙马潭区", "泸县", "合江县", "叙永县", "古蔺县"));
        cityDistrictMap.put("德阳市", Arrays.asList("旌阳区", "罗江区", "中江县", "广汉市", "什邡市", "绵竹市"));
        cityDistrictMap.put("乐山市", Arrays.asList("市中区", "沙湾区", "五通桥区", "金口河区", "犍为县", "井研县", "夹江县", "沐川县", "峨边彝族自治县", "马边彝族自治县", "峨眉山市"));
        
        // 贵州省更多城市
        cityDistrictMap.put("六盘水市", Arrays.asList("钟山区", "水城区", "盘州市", "六枝特区"));
        cityDistrictMap.put("毕节市", Arrays.asList("七星关区", "大方县", "黔西县", "金沙县", "织金县", "纳雍县", "威宁彝族回族苗族自治县", "赫章县"));
        
        // 云南省更多城市
        cityDistrictMap.put("保山市", Arrays.asList("隆阳区", "施甸县", "龙陵县", "昌宁县", "腾冲市"));
        cityDistrictMap.put("丽江市", Arrays.asList("古城区", "玉龙纳西族自治县", "永胜县", "华坪县", "宁蒗彝族自治县"));
        cityDistrictMap.put("普洱市", Arrays.asList("思茅区", "宁洱哈尼族彝族自治县", "墨江哈尼族自治县", "景东彝族自治县", "景谷傣族彝族自治县", "镇沅彝族哈尼族拉祜族自治县", "江城哈尼族彝族自治县", "孟连傣族拉祜族佤族自治县", "澜沧拉祜族自治县", "西盟佤族自治县"));
        
        // 陕西省更多城市
        cityDistrictMap.put("延安市", Arrays.asList("宝塔区", "安塞区", "延长县", "延川县", "子长市", "志丹县", "吴起县", "甘泉县", "富县", "洛川县", "宜川县", "黄龙县", "黄陵县"));
        cityDistrictMap.put("汉中市", Arrays.asList("汉台区", "南郑区", "城固县", "洋县", "西乡县", "勉县", "宁强县", "略阳县", "镇巴县", "留坝县", "佛坪县"));
        
        // 甘肃省更多城市
        cityDistrictMap.put("白银市", Arrays.asList("白银区", "平川区", "靖远县", "会宁县", "景泰县"));
        cityDistrictMap.put("武威市", Arrays.asList("凉州区", "民勤县", "古浪县", "天祝藏族自治县"));
        cityDistrictMap.put("张掖市", Arrays.asList("甘州区", "肃南裕固族自治县", "民乐县", "临泽县", "高台县", "山丹县"));
        cityDistrictMap.put("平凉市", Arrays.asList("崆峒区", "泾川县", "灵台县", "崇信县", "华亭市", "庄浪县", "静宁县"));
        cityDistrictMap.put("酒泉市", Arrays.asList("肃州区", "金塔县", "瓜州县", "肃北蒙古族自治县", "阿克塞哈萨克族自治县", "玉门市", "敦煌市"));
        
        // 内蒙古自治区城市
        cityDistrictMap.put("呼和浩特市", Arrays.asList("新城区", "回民区", "玉泉区", "赛罕区", "土默特左旗", "托克托县", "和林格尔县", "清水河县", "武川县"));
        cityDistrictMap.put("包头市", Arrays.asList("东河区", "昆都仑区", "青山区", "石拐区", "白云鄂博矿区", "九原区", "土默特右旗", "固阳县", "达尔罕茂明安联合旗"));
        cityDistrictMap.put("赤峰市", Arrays.asList("红山区", "元宝山区", "松山区", "阿鲁科尔沁旗", "巴林左旗", "巴林右旗", "林西县", "克什克腾旗", "翁牛特旗", "喀喇沁旗", "宁城县", "敖汉旗"));
        
        // 福建省剩余城市
        cityDistrictMap.put("南平市", Arrays.asList("延平区", "建阳区", "顺昌县", "浦城县", "光泽县", "松溪县", "政和县", "邵武市", "武夷山市", "建瓯市"));
        cityDistrictMap.put("宁德市", Arrays.asList("蕉城区", "霞浦县", "古田县", "屏南县", "寿宁县", "周宁县", "柘荣县", "福安市", "福鼎市"));
        
        // 江西省剩余城市
        cityDistrictMap.put("景德镇市", Arrays.asList("昌江区", "珠山区", "浮梁县", "乐平市"));
        cityDistrictMap.put("萍乡市", Arrays.asList("安源区", "湘东区", "莲花县", "上栗县", "芦溪县"));
        cityDistrictMap.put("新余市", Arrays.asList("渝水区", "分宜县"));
        cityDistrictMap.put("鹰潭市", Arrays.asList("月湖区", "余江区", "贵溪市"));
        cityDistrictMap.put("吉安市", Arrays.asList("吉州区", "青原区", "吉安县", "吉水县", "峡江县", "新干县", "永丰县", "泰和县", "遂川县", "万安县", "安福县", "永新县", "井冈山市"));
        cityDistrictMap.put("宜春市", Arrays.asList("袁州区", "奉新县", "万载县", "上高县", "宜丰县", "靖安县", "铜鼓县", "丰城市", "樟树市", "高安市"));
        
        // 山东省剩余城市
        cityDistrictMap.put("枣庄市", Arrays.asList("市中区", "薛城区", "峄城区", "台儿庄区", "山亭区", "滕州市"));
        cityDistrictMap.put("东营市", Arrays.asList("东营区", "河口区", "垦利区", "利津县", "广饶县"));
        cityDistrictMap.put("泰安市", Arrays.asList("泰山区", "岱岳区", "宁阳县", "东平县", "新泰市", "肥城市"));
        cityDistrictMap.put("威海市", Arrays.asList("环翠区", "文登区", "荣成市", "乳山市"));
        
        // 河南省剩余城市
        cityDistrictMap.put("鹤壁市", Arrays.asList("鹤山区", "山城区", "淇滨区", "浚县", "淇县"));
        cityDistrictMap.put("新乡市", Arrays.asList("红旗区", "卫滨区", "凤泉区", "牧野区", "新乡县", "获嘉县", "原阳县", "延津县", "封丘县", "卫辉市", "辉县市", "长垣市"));
        cityDistrictMap.put("焦作市", Arrays.asList("解放区", "中站区", "马村区", "山阳区", "修武县", "博爱县", "武陟县", "温县", "沁阳市", "孟州市"));
        
        // 湖北省剩余城市
        cityDistrictMap.put("鄂州市", Arrays.asList("梁子湖区", "华容区", "鄂城区"));
        cityDistrictMap.put("荆门市", Arrays.asList("东宝区", "掇刀区", "京山市", "沙洋县", "钟祥市"));
        cityDistrictMap.put("孝感市", Arrays.asList("孝南区", "孝昌县", "大悟县", "云梦县", "应城市", "安陆市", "汉川市"));
        
        // 湖南省剩余城市
        cityDistrictMap.put("邵阳市", Arrays.asList("双清区", "大祥区", "北塔区", "邵东县", "新邵县", "邵阳县", "隆回县", "洞口县", "绥宁县", "新宁县", "城步苗族自治县", "武冈市"));
        cityDistrictMap.put("岳阳市", Arrays.asList("岳阳楼区", "云溪区", "君山区", "岳阳县", "华容县", "湘阴县", "平江县", "汨罗市", "临湘市"));
        
        // 湖南省剩余城市
        cityDistrictMap.put("张家界市", Arrays.asList("永定区", "武陵源区", "慈利县", "桑植县"));
        cityDistrictMap.put("益阳市", Arrays.asList("资阳区", "赫山区", "南县", "桃江县", "安化县", "沅江市"));
        cityDistrictMap.put("郴州市", Arrays.asList("北湖区", "苏仙区", "桂阳县", "宜章县", "永兴县", "嘉禾县", "临武县", "汝城县", "桂东县", "安仁县", "资兴市"));
        cityDistrictMap.put("永州市", Arrays.asList("零陵区", "冷水滩区", "祁阳县", "东安县", "双牌县", "道县", "江永县", "宁远县", "蓝山县", "新田县", "江华瑶族自治县"));
        cityDistrictMap.put("怀化市", Arrays.asList("鹤城区", "中方县", "沅陵县", "辰溪县", "溆浦县", "会同县", "麻阳苗族自治县", "新晃侗族自治县", "芷江侗族自治县", "靖州苗族侗族自治县", "通道侗族自治县", "洪江市"));
        cityDistrictMap.put("娄底市", Arrays.asList("娄星区", "双峰县", "新化县", "冷水江市", "涟源市"));
        cityDistrictMap.put("湘西土家族苗族自治州", Arrays.asList("吉首市", "泸溪县", "凤凰县", "花垣县", "保靖县", "古丈县", "永顺县", "龙山县"));
        
        // 广东省剩余城市
        cityDistrictMap.put("茂名市", Arrays.asList("茂南区", "电白区", "高州市", "化州市", "信宜市"));
        cityDistrictMap.put("肇庆市", Arrays.asList("端州区", "鼎湖区", "高要区", "广宁县", "怀集县", "封开县", "德庆县", "四会市"));
        cityDistrictMap.put("惠州市", Arrays.asList("惠城区", "惠阳区", "博罗县", "惠东县", "龙门县"));
        
        // 广西剩余城市
        cityDistrictMap.put("防城港市", Arrays.asList("港口区", "防城区", "上思县", "东兴市"));
        cityDistrictMap.put("钦州市", Arrays.asList("钦南区", "钦北区", "灵山县", "浦北县"));
        cityDistrictMap.put("玉林市", Arrays.asList("玉州区", "福绵区", "容县", "陆川县", "博白县", "兴业县", "北流市"));
        
        // 四川省剩余城市
        cityDistrictMap.put("自贡市", Arrays.asList("自流井区", "贡井区", "大安区", "沿滩区", "荣县", "富顺县"));
        cityDistrictMap.put("攀枝花市", Arrays.asList("东区", "西区", "仁和区", "米易县", "盐边县"));
        cityDistrictMap.put("广元市", Arrays.asList("利州区", "昭化区", "朝天区", "旺苍县", "青川县", "剑阁县", "苍溪县"));
        
        // 贵州省剩余城市
        cityDistrictMap.put("黔西南布依族苗族自治州", Arrays.asList("兴义市", "兴仁市", "普安县", "晴隆县", "贞丰县", "望谟县", "册亨县", "安龙县"));
        cityDistrictMap.put("黔东南苗族侗族自治州", Arrays.asList("凯里市", "黄平县", "施秉县", "三穗县", "镇远县", "岑巩县", "天柱县", "锦屏县", "剑河县", "台江县", "黎平县", "榕江县", "从江县", "雷山县", "麻江县", "丹寨县"));
        
        // 云南省剩余城市
        cityDistrictMap.put("昭通市", Arrays.asList("昭阳区", "鲁甸县", "巧家县", "盐津县", "大关县", "永善县", "绥江县", "镇雄县", "彝良县", "威信县", "水富市"));
        cityDistrictMap.put("临沧市", Arrays.asList("临翔区", "凤庆县", "云县", "永德县", "镇康县", "双江拉祜族佤族布朗族傣族自治县", "耿马傣族佤族自治县", "沧源佤族自治县"));
        
        // 陕西省剩余城市
        cityDistrictMap.put("铜川市", Arrays.asList("王益区", "印台区", "耀州区", "宜君县"));
        cityDistrictMap.put("榆林市", Arrays.asList("榆阳区", "横山区", "府谷县", "靖边县", "定边县", "绥德县", "米脂县", "佳县", "吴堡县", "清涧县", "子洲县", "神木市"));
        
        // 继续添加更多城市的区县数据
        
        // 台湾省（特别行政区）城市
        cityDistrictMap.put("台北市", Arrays.asList("中正区", "大同区", "中山区", "松山区", "大安区", "万华区", "信义区", "士林区", "北投区", "内湖区", "南港区", "文山区"));
        cityDistrictMap.put("高雄市", Arrays.asList("新兴区", "前金区", "苓雅区", "盐埕区", "鼓山区", "旗津区", "前镇区", "三民区", "左营区", "楠梓区", "小港区", "凤山区", "林园区", "大寮区", "大树区", "大社区", "仁武区", "鸟松区", "冈山区", "桥头区", "燕巢区", "田寮区", "阿莲区", "路竹区", "湖内区", "茄萣区", "永安乡", "弥陀区", "梓官区", "旗山区", "美浓区", "六龟区", "甲仙区", "杉林区", "内门区", "茂林区", "桃源区", "那玛夏区"));
        cityDistrictMap.put("台中市", Arrays.asList("中区", "东区", "南区", "西区", "北区", "北屯区", "西屯区", "南屯区", "太平区", "大里区", "雾峰区", "乌日区", "丰原区", "后里区", "石冈区", "东势区", "新社区", "潭子区", "大雅区", "神冈区", "大肚区", "龙井区", "沙鹿区", "梧栖区", "清水区", "大甲区", "外埔区", "后里区"));
        cityDistrictMap.put("台南市", Arrays.asList("中西区", "东区", "南区", "北区", "安平区", "安南区", "永康区", "归仁区", "新化区", "左镇区", "玉井区", "楠西区", "南化区", "仁德区", "关庙区", "龙崎区", "官田区", "麻豆区", "佳里区", "西港区", "七股区", "将军区", "学甲区", "北门区", "新营区", "后壁区", "白河区", "东山区", "六甲区", "下营区", "柳营区", "盐水区", "善化区", "大内区", "山上区", "新市"));
        cityDistrictMap.put("桃园市", Arrays.asList("桃园区", "中坜区", "平镇区", "八德区", "杨梅区", "芦竹区", "大溪区", "龙潭区", "龟山区", "大园区", "观音区", "新屋区", "复兴区"));
        cityDistrictMap.put("新竹市", Arrays.asList("东区", "北区", "香山区"));
        
        // 澳门特别行政区
        cityDistrictMap.put("澳门", Arrays.asList("花地玛堂区", "圣安多尼堂区", "大堂区", "望德堂区", "风顺堂区", "嘉模堂区", "圣方济各堂区", "路氹填海区"));
        
        // 香港特别行政区
        cityDistrictMap.put("香港", Arrays.asList("中西区", "湾仔区", "东区", "南区", "油尖旺区", "深水埗区", "九龙城区", "黄大仙区", "观塘区", "北区", "大埔区", "沙田区", "西贡区", "荃湾区", "屯门区", "元朗区", "葵青区", "离岛区"));
        
        // 黑龙江省更多城市
        cityDistrictMap.put("佳木斯市", Arrays.asList("向阳区", "前进区", "东风区", "郊区", "桦南县", "桦川县", "汤原县", "同江市", "富锦市", "抚远市"));
        cityDistrictMap.put("牡丹江市", Arrays.asList("东安区", "阳明区", "爱民区", "西安区", "林口县", "绥芬河市", "海林市", "宁安市", "穆棱市", "东宁市"));
        
        // 江苏省更多城市
        cityDistrictMap.put("扬州市", Arrays.asList("广陵区", "邗江区", "江都区", "宝应县", "仪征市", "高邮市"));
        cityDistrictMap.put("镇江市", Arrays.asList("京口区", "润州区", "丹徒区", "丹阳市", "扬中市", "句容市"));
        cityDistrictMap.put("泰州市", Arrays.asList("海陵区", "高港区", "姜堰区", "兴化市", "靖江市", "泰兴市"));
        cityDistrictMap.put("宿迁市", Arrays.asList("宿城区", "宿豫区", "沭阳县", "泗阳县", "泗洪县"));
        
        // 山东省更多城市
        cityDistrictMap.put("日照市", Arrays.asList("东港区", "岚山区", "五莲县", "莒县"));
        cityDistrictMap.put("临沂市", Arrays.asList("兰山区", "罗庄区", "河东区", "沂南县", "郯城县", "沂水县", "兰陵县", "费县", "平邑县", "莒南县", "蒙阴县", "临沭县"));
        cityDistrictMap.put("德州市", Arrays.asList("德城区", "陵城区", "宁津县", "庆云县", "临邑县", "齐河县", "平原县", "夏津县", "武城县", "乐陵市", "禹城市"));
        cityDistrictMap.put("聊城市", Arrays.asList("东昌府区", "茌平区", "阳谷县", "莘县", "东阿县", "冠县", "高唐县", "临清市"));
        cityDistrictMap.put("滨州市", Arrays.asList("滨城区", "沾化区", "惠民县", "阳信县", "无棣县", "博兴县", "邹平市"));
        cityDistrictMap.put("菏泽市", Arrays.asList("牡丹区", "定陶区", "曹县", "单县", "成武县", "巨野县", "郓城县", "鄄城县", "东明县"));
        
        // 广东省更多城市
        cityDistrictMap.put("梅州市", Arrays.asList("梅江区", "梅县区", "大埔县", "丰顺县", "五华县", "平远县", "蕉岭县", "兴宁市"));
        cityDistrictMap.put("汕尾市", Arrays.asList("城区", "海丰县", "陆河县", "陆丰市"));
        cityDistrictMap.put("河源市", Arrays.asList("源城区", "紫金县", "龙川县", "连平县", "和平县", "东源县"));
        cityDistrictMap.put("阳江市", Arrays.asList("江城区", "阳东区", "阳西县", "阳春市"));
        cityDistrictMap.put("清远市", Arrays.asList("清城区", "清新区", "佛冈县", "阳山县", "连山壮族瑶族自治县", "连南瑶族自治县", "英德市", "连州市"));
        cityDistrictMap.put("东莞市", Arrays.asList("莞城街道", "南城街道", "东城街道", "万江街道", "石龙镇", "石排镇", "茶山镇", "企石镇", "桥头镇", "东坑镇", "横沥镇", "常平镇", "虎门镇", "长安镇", "沙田镇", "厚街镇", "寮步镇", "大岭山镇", "大朗镇", "黄江镇", "樟木头镇", "谢岗镇", "塘厦镇", "清溪镇", "凤岗镇", "麻涌镇", "中堂镇", "高埗镇", "石碣镇", "望牛墩镇", "洪梅镇", "道滘镇"));
        cityDistrictMap.put("中山市", Arrays.asList("石岐区", "东区", "西区", "南区", "五桂山区", "火炬开发区", "黄圃镇", "南头镇", "东凤镇", "阜沙镇", "小榄镇", "东升镇", "古镇镇", "横栏镇", "三角镇", "民众镇", "南朗镇", "港口镇", "大涌镇", "沙溪镇", "三乡镇", "板芙镇", "神湾镇", "坦洲镇"));
        
        // 清理重复的城市条目
        // 完善已有的数据
        cityDistrictMap.put("杭州市", Arrays.asList("上城区", "下城区", "江干区", "拱墅区", "西湖区", "滨江区", "萧山区", "余杭区", "富阳区", "临安区", "桐庐县", "淳安县", "建德市"));
        cityDistrictMap.put("南京市", Arrays.asList("玄武区", "秦淮区", "建邺区", "鼓楼区", "浦口区", "栖霞区", "雨花台区", "江宁区", "六合区", "溧水区", "高淳区"));
        cityDistrictMap.put("重庆市", Arrays.asList("万州区", "涪陵区", "渝中区", "大渡口区", "江北区", "沙坪坝区", "九龙坡区", "南岸区", "北碚区", "綦江区", "大足区", "渝北区", "巴南区", "黔江区", "长寿区", "江津区", "合川区", "永川区", "南川区", "璧山区", "铜梁区", "潼南区", "荣昌区", "开州区", "梁平区", "武隆区", "城口县", "丰都县", "垫江县", "忠县", "云阳县", "奉节县", "巫山县", "巫溪县", "石柱土家族自治县", "秀山土家族苗族自治县", "酉阳土家族苗族自治县", "彭水苗族土家族自治县"));
        
        // 江西省剩余城市 - 补充完整
        cityDistrictMap.put("景德镇市", Arrays.asList("昌江区", "珠山区", "浮梁县", "乐平市"));
        cityDistrictMap.put("萍乡市", Arrays.asList("安源区", "湘东区", "莲花县", "上栗县", "芦溪县"));
        cityDistrictMap.put("新余市", Arrays.asList("渝水区", "分宜县"));
        cityDistrictMap.put("鹰潭市", Arrays.asList("月湖区", "余江区", "贵溪市"));
        cityDistrictMap.put("吉安市", Arrays.asList("吉州区", "青原区", "吉安县", "吉水县", "峡江县", "新干县", "永丰县", "泰和县", "遂川县", "万安县", "安福县", "永新县", "井冈山市"));
        cityDistrictMap.put("宜春市", Arrays.asList("袁州区", "奉新县", "万载县", "上高县", "宜丰县", "靖安县", "铜鼓县", "丰城市", "樟树市", "高安市"));
        
        // 山东省剩余城市 - 补充完整
        cityDistrictMap.put("枣庄市", Arrays.asList("市中区", "薛城区", "峄城区", "台儿庄区", "山亭区", "滕州市"));
        cityDistrictMap.put("东营市", Arrays.asList("东营区", "河口区", "垦利区", "利津县", "广饶县"));
        cityDistrictMap.put("泰安市", Arrays.asList("泰山区", "岱岳区", "宁阳县", "东平县", "新泰市", "肥城市"));
        cityDistrictMap.put("威海市", Arrays.asList("环翠区", "文登区", "荣成市", "乳山市"));
        cityDistrictMap.put("日照市", Arrays.asList("东港区", "岚山区", "五莲县", "莒县"));
        cityDistrictMap.put("临沂市", Arrays.asList("兰山区", "罗庄区", "河东区", "沂南县", "郯城县", "沂水县", "兰陵县", "费县", "平邑县", "莒南县", "蒙阴县", "临沭县"));
        cityDistrictMap.put("德州市", Arrays.asList("德城区", "陵城区", "宁津县", "庆云县", "临邑县", "齐河县", "平原县", "夏津县", "武城县", "乐陵市", "禹城市"));
        cityDistrictMap.put("聊城市", Arrays.asList("东昌府区", "茌平区", "阳谷县", "莘县", "东阿县", "冠县", "高唐县", "临清市"));
        cityDistrictMap.put("滨州市", Arrays.asList("滨城区", "沾化区", "惠民县", "阳信县", "无棣县", "博兴县", "邹平市"));
        cityDistrictMap.put("菏泽市", Arrays.asList("牡丹区", "定陶区", "曹县", "单县", "成武县", "巨野县", "郓城县", "鄄城县", "东明县"));
        
        // 河南省剩余城市 - 补充完整
        cityDistrictMap.put("鹤壁市", Arrays.asList("鹤山区", "山城区", "淇滨区", "浚县", "淇县"));
        cityDistrictMap.put("新乡市", Arrays.asList("红旗区", "卫滨区", "凤泉区", "牧野区", "新乡县", "获嘉县", "原阳县", "延津县", "封丘县", "卫辉市", "辉县市", "长垣市"));
        cityDistrictMap.put("焦作市", Arrays.asList("解放区", "中站区", "马村区", "山阳区", "修武县", "博爱县", "武陟县", "温县", "沁阳市", "孟州市"));
        cityDistrictMap.put("濮阳市", Arrays.asList("华龙区", "清丰县", "南乐县", "范县", "台前县", "濮阳县"));
        cityDistrictMap.put("许昌市", Arrays.asList("魏都区", "建安区", "鄢陵县", "襄城县", "禹州市", "长葛市"));
        cityDistrictMap.put("漯河市", Arrays.asList("源汇区", "郾城区", "召陵区", "舞阳县", "临颍县"));
        cityDistrictMap.put("三门峡市", Arrays.asList("湖滨区", "陕州区", "渑池县", "卢氏县", "义马市", "灵宝市"));
        cityDistrictMap.put("南阳市", Arrays.asList("宛城区", "卧龙区", "南召县", "方城县", "西峡县", "镇平县", "内乡县", "淅川县", "社旗县", "唐河县", "新野县", "桐柏县", "邓州市"));
        cityDistrictMap.put("商丘市", Arrays.asList("梁园区", "睢阳区", "民权县", "睢县", "宁陵县", "柘城县", "虞城县", "夏邑县", "永城市"));
        cityDistrictMap.put("信阳市", Arrays.asList("浉河区", "平桥区", "罗山县", "光山县", "新县", "商城县", "固始县", "潢川县", "淮滨县", "息县"));
        cityDistrictMap.put("周口市", Arrays.asList("川汇区", "淮阳区", "扶沟县", "西华县", "商水县", "沈丘县", "郸城县", "太康县", "鹿邑县", "项城市"));
        cityDistrictMap.put("驻马店市", Arrays.asList("驿城区", "西平县", "上蔡县", "平舆县", "正阳县", "确山县", "泌阳县", "汝南县", "遂平县", "新蔡县"));
        
        // 湖北省剩余城市 - 补充完整
        cityDistrictMap.put("鄂州市", Arrays.asList("梁子湖区", "华容区", "鄂城区"));
        cityDistrictMap.put("荆门市", Arrays.asList("东宝区", "掇刀区", "京山市", "沙洋县", "钟祥市"));
        cityDistrictMap.put("孝感市", Arrays.asList("孝南区", "孝昌县", "大悟县", "云梦县", "应城市", "安陆市", "汉川市"));
        cityDistrictMap.put("黄冈市", Arrays.asList("黄州区", "团风县", "红安县", "罗田县", "英山县", "浠水县", "蕲春县", "黄梅县", "麻城市", "武穴市"));
        cityDistrictMap.put("咸宁市", Arrays.asList("咸安区", "嘉鱼县", "通城县", "崇阳县", "通山县", "赤壁市"));
        cityDistrictMap.put("随州市", Arrays.asList("曾都区", "随县", "广水市"));
        cityDistrictMap.put("恩施土家族苗族自治州", Arrays.asList("恩施市", "利川市", "建始县", "巴东县", "宣恩县", "咸丰县", "来凤县", "鹤峰县"));
        
        // 清理重复的城市条目
        // 对于已经存在的城市，后面的条目会覆盖前面的，所以无需额外处理
        
        // 确保为所有城市添加区县数据
        // 当区县数据缺失时，自动添加默认值"市辖区"
        // 在选择城市时会自动处理
        
        // 广西壮族自治区剩余城市
        cityDistrictMap.put("南宁市", Arrays.asList("兴宁区", "青秀区", "江南区", "西乡塘区", "良庆区", "邕宁区", "武鸣区", "隆安县", "马山县", "上林县", "宾阳县", "横州市"));
        cityDistrictMap.put("柳州市", Arrays.asList("城中区", "鱼峰区", "柳南区", "柳北区", "柳江区", "柳城县", "鹿寨县", "融安县", "融水苗族自治县", "三江侗族自治县"));
        cityDistrictMap.put("桂林市", Arrays.asList("秀峰区", "叠彩区", "象山区", "七星区", "雁山区", "临桂区", "阳朔县", "灵川县", "全州县", "兴安县", "永福县", "灌阳县", "龙胜各族自治县", "资源县", "平乐县", "恭城瑶族自治县"));
        cityDistrictMap.put("防城港市", Arrays.asList("港口区", "防城区", "上思县", "东兴市"));
        cityDistrictMap.put("钦州市", Arrays.asList("钦南区", "钦北区", "灵山县", "浦北县"));
        cityDistrictMap.put("玉林市", Arrays.asList("玉州区", "福绵区", "容县", "陆川县", "博白县", "兴业县", "北流市"));
        cityDistrictMap.put("百色市", Arrays.asList("右江区", "田阳区", "田东县", "平果市", "德保县", "靖西市", "那坡县", "凌云县", "乐业县", "田林县", "西林县", "隆林各族自治县"));
        cityDistrictMap.put("贺州市", Arrays.asList("八步区", "平桂区", "昭平县", "钟山县", "富川瑶族自治县"));
        cityDistrictMap.put("河池市", Arrays.asList("宜州区", "金城江区", "南丹县", "天峨县", "凤山县", "东兰县", "罗城仫佬族自治县", "环江毛南族自治县", "巴马瑶族自治县", "都安瑶族自治县", "大化瑶族自治县"));
        cityDistrictMap.put("来宾市", Arrays.asList("兴宾区", "忻城县", "象州县", "武宣县", "金秀瑶族自治县", "合山市"));
        cityDistrictMap.put("崇左市", Arrays.asList("江州区", "扶绥县", "宁明县", "龙州县", "大新县", "天等县", "凭祥市"));
        
        // 海南省城市
        cityDistrictMap.put("海口市", Arrays.asList("秀英区", "龙华区", "琼山区", "美兰区"));
        cityDistrictMap.put("三亚市", Arrays.asList("海棠区", "吉阳区", "天涯区", "崖州区"));
        cityDistrictMap.put("三沙市", Arrays.asList("西沙区", "南沙区"));
        cityDistrictMap.put("儋州市", Arrays.asList("那大镇", "和庆镇", "南丰镇", "大成镇", "雅星镇", "兰洋镇", "光村镇", "木棠镇", "海头镇", "峨蔓镇", "王五镇", "白马井镇", "中和镇", "排浦镇", "东成镇"));
        cityDistrictMap.put("五指山市", Arrays.asList("通什镇", "南圣镇", "毛阳镇", "番阳镇", "水满乡", "畅好乡", "毛道乡"));
        cityDistrictMap.put("琼海市", Arrays.asList("嘉积镇", "中原镇", "博鳌镇", "潭门镇", "长坡镇", "塔洋镇", "大路镇", "万泉镇", "石壁镇", "会山镇", "龙江镇", "阳江镇", "中原镇"));
        cityDistrictMap.put("文昌市", Arrays.asList("文城镇", "重兴镇", "蓬莱镇", "会文镇", "东路镇", "潭牛镇", "东阁镇", "文教镇", "东郊镇", "龙楼镇", "昌洒镇", "翁田镇", "抱罗镇", "冯坡镇", "锦山镇", "铺前镇"));
        cityDistrictMap.put("万宁市", Arrays.asList("万城镇", "龙滚镇", "和乐镇", "后安镇", "大茂镇", "东澳镇", "礼纪镇", "长丰镇", "山根镇", "北大镇", "南桥镇", "三更罗镇"));
        cityDistrictMap.put("东方市", Arrays.asList("八所镇", "东河镇", "大田镇", "感城镇", "板桥镇", "三家镇", "四更镇", "新龙镇", "天安乡", "江边乡"));
        
        // 贵州省剩余城市
        cityDistrictMap.put("贵阳市", Arrays.asList("南明区", "云岩区", "花溪区", "乌当区", "白云区", "观山湖区", "开阳县", "息烽县", "修文县", "清镇市"));
        cityDistrictMap.put("遵义市", Arrays.asList("红花岗区", "汇川区", "播州区", "桐梓县", "绥阳县", "正安县", "道真仡佬族苗族自治县", "务川仡佬族苗族自治县", "凤冈县", "湄潭县", "余庆县", "习水县", "赤水市", "仁怀市"));
        cityDistrictMap.put("安顺市", Arrays.asList("西秀区", "平坝区", "普定县", "镇宁布依族苗族自治县", "关岭布依族苗族自治县", "紫云苗族布依族自治县"));
        cityDistrictMap.put("铜仁市", Arrays.asList("碧江区", "万山区", "江口县", "玉屏侗族自治县", "石阡县", "思南县", "印江土家族苗族自治县", "德江县", "沿河土家族自治县", "松桃苗族自治县"));
        cityDistrictMap.put("黔西南布依族苗族自治州", Arrays.asList("兴义市", "兴仁市", "普安县", "晴隆县", "贞丰县", "望谟县", "册亨县", "安龙县"));
        cityDistrictMap.put("黔东南苗族侗族自治州", Arrays.asList("凯里市", "黄平县", "施秉县", "三穗县", "镇远县", "岑巩县", "天柱县", "锦屏县", "剑河县", "台江县", "黎平县", "榕江县", "从江县", "雷山县", "麻江县", "丹寨县"));
        cityDistrictMap.put("黔南布依族苗族自治州", Arrays.asList("都匀市", "福泉市", "荔波县", "贵定县", "瓮安县", "独山县", "平塘县", "罗甸县", "长顺县", "龙里县", "惠水县", "三都水族自治县"));
        
        // 西藏自治区城市
        cityDistrictMap.put("拉萨市", Arrays.asList("城关区", "堆龙德庆区", "达孜区", "林周县", "当雄县", "尼木县", "曲水县", "墨竹工卡县"));
        cityDistrictMap.put("日喀则市", Arrays.asList("桑珠孜区", "南木林县", "江孜县", "定日县", "萨迦县", "拉孜县", "昂仁县", "谢通门县", "白朗县", "仁布县", "康马县", "定结县", "仲巴县", "亚东县", "吉隆县", "聂拉木县", "萨嘎县", "岗巴县"));
        cityDistrictMap.put("昌都市", Arrays.asList("卡若区", "江达县", "贡觉县", "类乌齐县", "丁青县", "察雅县", "八宿县", "左贡县", "芒康县", "洛隆县", "边坝县"));
        cityDistrictMap.put("林芝市", Arrays.asList("巴宜区", "工布江达县", "米林县", "墨脱县", "波密县", "察隅县", "朗县"));
        cityDistrictMap.put("山南市", Arrays.asList("乃东区", "扎囊县", "贡嘎县", "桑日县", "琼结县", "曲松县", "措美县", "洛扎县", "加查县", "隆子县", "错那县", "浪卡子县"));
        cityDistrictMap.put("那曲市", Arrays.asList("色尼区", "嘉黎县", "比如县", "聂荣县", "安多县", "申扎县", "索县", "班戈县", "巴青县", "尼玛县", "双湖县"));
        cityDistrictMap.put("阿里地区", Arrays.asList("噶尔县", "普兰县", "札达县", "日土县", "革吉县", "改则县", "措勤县"));
        
        // 甘肃省剩余城市
        cityDistrictMap.put("兰州市", Arrays.asList("城关区", "七里河区", "西固区", "安宁区", "红古区", "永登县", "皋兰县", "榆中县"));
        cityDistrictMap.put("嘉峪关市", Arrays.asList("雄关区", "长城区", "镜铁区"));
        cityDistrictMap.put("金昌市", Arrays.asList("金川区", "永昌县"));
        cityDistrictMap.put("天水市", Arrays.asList("秦州区", "麦积区", "清水县", "秦安县", "甘谷县", "武山县", "张家川回族自治县"));
        cityDistrictMap.put("庆阳市", Arrays.asList("西峰区", "庆城县", "环县", "华池县", "合水县", "正宁县", "宁县", "镇原县"));
        cityDistrictMap.put("定西市", Arrays.asList("安定区", "通渭县", "陇西县", "渭源县", "临洮县", "漳县", "岷县"));
        cityDistrictMap.put("陇南市", Arrays.asList("武都区", "成县", "文县", "宕昌县", "康县", "西和县", "礼县", "徽县", "两当县"));
        cityDistrictMap.put("临夏回族自治州", Arrays.asList("临夏市", "临夏县", "康乐县", "永靖县", "广河县", "和政县", "东乡族自治县", "积石山保安族东乡族撒拉族自治县"));
        cityDistrictMap.put("甘南藏族自治州", Arrays.asList("合作市", "临潭县", "卓尼县", "舟曲县", "迭部县", "玛曲县", "碌曲县", "夏河县"));
        
        // 青海省城市
        cityDistrictMap.put("西宁市", Arrays.asList("城东区", "城中区", "城西区", "城北区", "大通回族土族自治县", "湟中县", "湟源县"));
        cityDistrictMap.put("海东市", Arrays.asList("乐都区", "平安区", "民和回族土族自治县", "互助土族自治县", "化隆回族自治县", "循化撒拉族自治县"));
        cityDistrictMap.put("海北藏族自治州", Arrays.asList("海晏县", "祁连县", "刚察县", "门源回族自治县"));
        cityDistrictMap.put("黄南藏族自治州", Arrays.asList("同仁市", "尖扎县", "泽库县", "河南蒙古族自治县"));
        cityDistrictMap.put("海南藏族自治州", Arrays.asList("共和县", "同德县", "贵德县", "兴海县", "贵南县"));
        cityDistrictMap.put("果洛藏族自治州", Arrays.asList("玛沁县", "班玛县", "甘德县", "达日县", "久治县", "玛多县"));
        cityDistrictMap.put("玉树藏族自治州", Arrays.asList("玉树市", "杂多县", "称多县", "治多县", "囊谦县", "曲麻莱县"));
        cityDistrictMap.put("海西蒙古族藏族自治州", Arrays.asList("格尔木市", "德令哈市", "茫崖市", "乌兰县", "都兰县", "天峻县", "大柴旦行政区"));
        
        // 宁夏回族自治区剩余城市
        cityDistrictMap.put("银川市", Arrays.asList("兴庆区", "西夏区", "金凤区", "永宁县", "贺兰县", "灵武市"));
        cityDistrictMap.put("固原市", Arrays.asList("原州区", "西吉县", "隆德县", "泾源县", "彭阳县"));
        cityDistrictMap.put("中卫市", Arrays.asList("沙坡头区", "中宁县", "海原县"));
        
        // 新疆维吾尔自治区主要城市
        cityDistrictMap.put("乌鲁木齐市", Arrays.asList("天山区", "沙依巴克区", "新市区", "水磨沟区", "头屯河区", "达坂城区", "米东区", "乌鲁木齐县"));
        cityDistrictMap.put("克拉玛依市", Arrays.asList("独山子区", "克拉玛依区", "白碱滩区", "乌尔禾区"));
        cityDistrictMap.put("吐鲁番市", Arrays.asList("高昌区", "鄯善县", "托克逊县"));
        cityDistrictMap.put("哈密市", Arrays.asList("伊州区", "巴里坤哈萨克自治县", "伊吾县"));
        cityDistrictMap.put("昌吉回族自治州", Arrays.asList("昌吉市", "阜康市", "呼图壁县", "玛纳斯县", "奇台县", "吉木萨尔县", "木垒哈萨克自治县"));
        cityDistrictMap.put("博尔塔拉蒙古自治州", Arrays.asList("博乐市", "阿拉山口市", "精河县", "温泉县"));
        cityDistrictMap.put("巴音郭楞蒙古自治州", Arrays.asList("库尔勒市", "轮台县", "尉犁县", "若羌县", "且末县", "焉耆回族自治县", "和静县", "和硕县", "博湖县"));
        cityDistrictMap.put("阿克苏地区", Arrays.asList("阿克苏市", "温宿县", "库车市", "沙雅县", "新和县", "拜城县", "乌什县", "阿瓦提县", "柯坪县"));
        cityDistrictMap.put("克孜勒苏柯尔克孜自治州", Arrays.asList("阿图什市", "阿克陶县", "阿合奇县", "乌恰县"));
        cityDistrictMap.put("喀什地区", Arrays.asList("喀什市", "疏附县", "疏勒县", "英吉沙县", "泽普县", "莎车县", "叶城县", "麦盖提县", "岳普湖县", "伽师县", "巴楚县", "塔什库尔干塔吉克自治县"));
        cityDistrictMap.put("和田地区", Arrays.asList("和田市", "和田县", "墨玉县", "皮山县", "洛浦县", "策勒县", "于田县", "民丰县"));
        cityDistrictMap.put("伊犁哈萨克自治州", Arrays.asList("伊宁市", "奎屯市", "霍尔果斯市", "伊宁县", "察布查尔锡伯自治县", "霍城县", "巩留县", "新源县", "昭苏县", "特克斯县", "尼勒克县"));
        cityDistrictMap.put("塔城地区", Arrays.asList("塔城市", "乌苏市", "沙湾市", "额敏县", "托里县", "裕民县", "和布克赛尔蒙古自治县"));
        cityDistrictMap.put("阿勒泰地区", Arrays.asList("阿勒泰市", "布尔津县", "富蕴县", "福海县", "哈巴河县", "青河县", "吉木乃县"));
        
        // 广东省剩余城市 - 补充完整
        cityDistrictMap.put("梅州市", Arrays.asList("梅江区", "梅县区", "大埔县", "丰顺县", "五华县", "平远县", "蕉岭县", "兴宁市"));
        cityDistrictMap.put("汕尾市", Arrays.asList("城区", "海丰县", "陆河县", "陆丰市"));
        cityDistrictMap.put("河源市", Arrays.asList("源城区", "紫金县", "龙川县", "连平县", "和平县", "东源县"));
        cityDistrictMap.put("阳江市", Arrays.asList("江城区", "阳东区", "阳西县", "阳春市"));
        cityDistrictMap.put("清远市", Arrays.asList("清城区", "清新区", "佛冈县", "阳山县", "连山壮族瑶族自治县", "连南瑶族自治县", "英德市", "连州市"));
        cityDistrictMap.put("东莞市", Arrays.asList("莞城街道", "南城街道", "东城街道", "万江街道", "石龙镇", "石排镇", "茶山镇", "企石镇", "桥头镇", "东坑镇", "横沥镇", "常平镇", "虎门镇", "长安镇", "沙田镇", "厚街镇", "寮步镇", "大岭山镇", "大朗镇", "黄江镇", "樟木头镇", "谢岗镇", "塘厦镇", "清溪镇", "凤岗镇", "麻涌镇", "中堂镇", "高埗镇", "石碣镇", "望牛墩镇", "洪梅镇", "道滘镇"));
        cityDistrictMap.put("中山市", Arrays.asList("石岐区", "东区", "西区", "南区", "五桂山区", "火炬开发区", "黄圃镇", "南头镇", "东凤镇", "阜沙镇", "小榄镇", "东升镇", "古镇镇", "横栏镇", "三角镇", "民众镇", "南朗镇", "港口镇", "大涌镇", "沙溪镇", "三乡镇", "板芙镇", "神湾镇", "坦洲镇"));
        cityDistrictMap.put("潮州市", Arrays.asList("湘桥区", "潮安区", "饶平县"));
        cityDistrictMap.put("揭阳市", Arrays.asList("榕城区", "揭东区", "揭西县", "惠来县", "普宁市"));
        cityDistrictMap.put("云浮市", Arrays.asList("云城区", "云安区", "新兴县", "郁南县", "罗定市"));
        
        // 四川省剩余城市 - 补充完整
        cityDistrictMap.put("自贡市", Arrays.asList("自流井区", "贡井区", "大安区", "沿滩区", "荣县", "富顺县"));
        cityDistrictMap.put("攀枝花市", Arrays.asList("东区", "西区", "仁和区", "米易县", "盐边县"));
        cityDistrictMap.put("泸州市", Arrays.asList("江阳区", "纳溪区", "龙马潭区", "泸县", "合江县", "叙永县", "古蔺县"));
        cityDistrictMap.put("德阳市", Arrays.asList("旌阳区", "罗江区", "中江县", "广汉市", "什邡市", "绵竹市"));
        cityDistrictMap.put("广元市", Arrays.asList("利州区", "昭化区", "朝天区", "旺苍县", "青川县", "剑阁县", "苍溪县"));
        cityDistrictMap.put("遂宁市", Arrays.asList("船山区", "安居区", "蓬溪县", "射洪市", "大英县"));
        cityDistrictMap.put("内江市", Arrays.asList("市中区", "东兴区", "威远县", "资中县", "隆昌市"));
        cityDistrictMap.put("乐山市", Arrays.asList("市中区", "沙湾区", "五通桥区", "金口河区", "犍为县", "井研县", "夹江县", "沐川县", "峨边彝族自治县", "马边彝族自治县", "峨眉山市"));
        cityDistrictMap.put("南充市", Arrays.asList("顺庆区", "高坪区", "嘉陵区", "南部县", "营山县", "蓬安县", "仪陇县", "西充县", "阆中市"));
        cityDistrictMap.put("眉山市", Arrays.asList("东坡区", "彭山区", "仁寿县", "洪雅县", "丹棱县", "青神县"));
        cityDistrictMap.put("宜宾市", Arrays.asList("翠屏区", "南溪区", "叙州区", "江安县", "长宁县", "高县", "珙县", "筠连县", "兴文县", "屏山县"));
        cityDistrictMap.put("广安市", Arrays.asList("广安区", "前锋区", "岳池县", "武胜县", "邻水县", "华蓥市"));
        cityDistrictMap.put("达州市", Arrays.asList("通川区", "达川区", "大竹县", "渠县", "开江县", "宣汉县", "万源市"));
        cityDistrictMap.put("雅安市", Arrays.asList("雨城区", "名山区", "荥经县", "汉源县", "石棉县", "天全县", "芦山县", "宝兴县"));
        cityDistrictMap.put("巴中市", Arrays.asList("巴州区", "恩阳区", "通江县", "南江县", "平昌县"));
        cityDistrictMap.put("资阳市", Arrays.asList("雁江区", "安岳县", "乐至县"));
        cityDistrictMap.put("阿坝藏族羌族自治州", Arrays.asList("马尔康市", "汶川县", "理县", "茂县", "松潘县", "九寨沟县", "金川县", "小金县", "黑水县", "壤塘县", "阿坝县", "若尔盖县", "红原县"));
        cityDistrictMap.put("甘孜藏族自治州", Arrays.asList("康定市", "泸定县", "丹巴县", "九龙县", "雅江县", "道孚县", "炉霍县", "甘孜县", "新龙县", "德格县", "白玉县", "石渠县", "色达县", "理塘县", "巴塘县", "乡城县", "稻城县", "得荣县"));
        cityDistrictMap.put("凉山彝族自治州", Arrays.asList("西昌市", "木里藏族自治县", "盐源县", "德昌县", "会理市", "会东县", "宁南县", "普格县", "布拖县", "金阳县", "昭觉县", "喜德县", "冕宁县", "越西县", "甘洛县", "美姑县", "雷波县"));
        
        // 云南省剩余城市 - 补充完整
        cityDistrictMap.put("保山市", Arrays.asList("隆阳区", "施甸县", "龙陵县", "昌宁县", "腾冲市"));
        cityDistrictMap.put("昭通市", Arrays.asList("昭阳区", "鲁甸县", "巧家县", "盐津县", "大关县", "永善县", "绥江县", "镇雄县", "彝良县", "威信县", "水富市"));
        cityDistrictMap.put("丽江市", Arrays.asList("古城区", "玉龙纳西族自治县", "永胜县", "华坪县", "宁蒗彝族自治县"));
        cityDistrictMap.put("普洱市", Arrays.asList("思茅区", "宁洱哈尼族彝族自治县", "墨江哈尼族自治县", "景东彝族自治县", "景谷傣族彝族自治县", "镇沅彝族哈尼族拉祜族自治县", "江城哈尼族彝族自治县", "孟连傣族拉祜族佤族自治县", "澜沧拉祜族自治县", "西盟佤族自治县"));
        cityDistrictMap.put("临沧市", Arrays.asList("临翔区", "凤庆县", "云县", "永德县", "镇康县", "双江拉祜族佤族布朗族傣族自治县", "耿马傣族佤族自治县", "沧源佤族自治县"));
        cityDistrictMap.put("楚雄彝族自治州", Arrays.asList("楚雄市", "双柏县", "牟定县", "南华县", "姚安县", "大姚县", "永仁县", "元谋县", "武定县", "禄丰县"));
        cityDistrictMap.put("红河哈尼族彝族自治州", Arrays.asList("蒙自市", "个旧市", "开远市", "弥勒市", "建水县", "石屏县", "泸西县", "元阳县", "红河县", "金平苗族瑶族傣族自治县", "绿春县", "屏边苗族自治县", "河口瑶族自治县"));
        cityDistrictMap.put("文山壮族苗族自治州", Arrays.asList("文山市", "砚山县", "西畴县", "麻栗坡县", "马关县", "丘北县", "广南县", "富宁县"));
        cityDistrictMap.put("西双版纳傣族自治州", Arrays.asList("景洪市", "勐海县", "勐腊县"));
        cityDistrictMap.put("大理白族自治州", Arrays.asList("大理市", "漾濞彝族自治县", "祥云县", "宾川县", "弥渡县", "南涧彝族自治县", "巍山彝族回族自治县", "永平县", "云龙县", "洱源县", "剑川县", "鹤庆县"));
        cityDistrictMap.put("德宏傣族景颇族自治州", Arrays.asList("芒市", "瑞丽市", "梁河县", "盈江县", "陇川县"));
        cityDistrictMap.put("怒江傈僳族自治州", Arrays.asList("泸水市", "福贡县", "贡山独龙族怒族自治县", "兰坪白族普米族自治县"));
        cityDistrictMap.put("迪庆藏族自治州", Arrays.asList("香格里拉市", "德钦县", "维西傈僳族自治县"));
        
        // 陕西省剩余城市 - 补充完整
        cityDistrictMap.put("铜川市", Arrays.asList("王益区", "印台区", "耀州区", "宜君县"));
        cityDistrictMap.put("宝鸡市", Arrays.asList("渭滨区", "金台区", "陈仓区", "凤翔县", "岐山县", "扶风县", "眉县", "陇县", "千阳县", "麟游县", "凤县", "太白县"));
        cityDistrictMap.put("咸阳市", Arrays.asList("秦都区", "杨陵区", "渭城区", "三原县", "泾阳县", "乾县", "礼泉县", "永寿县", "彬州市", "长武县", "旬邑县", "淳化县", "武功县", "兴平市"));
        cityDistrictMap.put("渭南市", Arrays.asList("临渭区", "华州区", "潼关县", "大荔县", "合阳县", "澄城县", "蒲城县", "白水县", "富平县", "韩城市", "华阴市"));
        cityDistrictMap.put("延安市", Arrays.asList("宝塔区", "安塞区", "延长县", "延川县", "子长市", "志丹县", "吴起县", "甘泉县", "富县", "洛川县", "宜川县", "黄龙县", "黄陵县"));
        cityDistrictMap.put("汉中市", Arrays.asList("汉台区", "南郑区", "城固县", "洋县", "西乡县", "勉县", "宁强县", "略阳县", "镇巴县", "留坝县", "佛坪县"));
        cityDistrictMap.put("榆林市", Arrays.asList("榆阳区", "横山区", "府谷县", "靖边县", "定边县", "绥德县", "米脂县", "佳县", "吴堡县", "清涧县", "子洲县", "神木市"));
        cityDistrictMap.put("安康市", Arrays.asList("汉滨区", "汉阴县", "石泉县", "宁陕县", "紫阳县", "岚皋县", "平利县", "镇坪县", "旬阳县", "白河县"));
        cityDistrictMap.put("商洛市", Arrays.asList("商州区", "洛南县", "丹凤县", "商南县", "山阳县", "镇安县", "柞水县"));
        
        // 确保为所有城市添加区县数据
        // 当区县数据缺失时，自动添加默认值"市辖区"
        // 在选择城市时会自动处理
        
        // 先声明dialog变量
        final AlertDialog[] dialogHolder = new AlertDialog[1];
        
        // 创建三个适配器变量来保存引用
        final ThreeColumnLocationAdapter[] provinceAdapterHolder = new ThreeColumnLocationAdapter[1];
        final ThreeColumnLocationAdapter[] cityAdapterHolder = new ThreeColumnLocationAdapter[1];
        final ThreeColumnLocationAdapter[] districtAdapterHolder = new ThreeColumnLocationAdapter[1];
        
        // 创建监听器对象数组，避免初始化错误
        final OnLocationSelectedListener[] locationListenerHolder = new OnLocationSelectedListener[1];
        
        // 创建监听器对象
        OnLocationSelectedListener locationListener = new OnLocationSelectedListener() {
            @Override
            public void onProvinceSelected(String province) {
                selectedProvince = province;
                selectedCity = "";
                selectedDistrict = "";
                
                // 更新城市列表
                ArrayList<String> cities = new ArrayList<>();
                if (provinceCityMap.containsKey(province)) {
                    List<String> cityList = provinceCityMap.get(province);
                    if (cityList != null) {
                        cities.addAll(cityList);
                    }
                }
                
                // 创建城市适配器
                ThreeColumnLocationAdapter cityAdapter = new ThreeColumnLocationAdapter(cities, new LocationItemClickListener() {
                    @Override
                    public void onItemClick(String location) {
                        if (locationListenerHolder[0] != null) {
                            locationListenerHolder[0].onCitySelected(location);
                        }
                    }
                });
                cityAdapterHolder[0] = cityAdapter;
                rvCities.setAdapter(cityAdapter);
                
                // 清空区县列表
                districtAdapterHolder[0] = new ThreeColumnLocationAdapter(new ArrayList<>(), null);
                rvDistricts.setAdapter(districtAdapterHolder[0]);
                
                // 更新省份选择状态
                if (provinceAdapterHolder[0] != null) {
                    provinceAdapterHolder[0].setSelectedItem(province);
                }
            }
            
            @Override
            public void onCitySelected(String city) {
                selectedCity = city;
                selectedDistrict = "";
                
                // 更新区县列表
                ArrayList<String> districts = new ArrayList<>();
                if (cityDistrictMap.containsKey(city)) {
                    List<String> districtList = cityDistrictMap.get(city);
                    if (districtList != null) {
                        // 确保区县列表不为空，如果为空则添加一个默认值
                        if (districtList.isEmpty()) {
                            districts.add("市辖区");
                        } else {
                            districts.addAll(districtList);
                        }
                    }
                }
                
                // 创建区县适配器
                ThreeColumnLocationAdapter districtAdapter = new ThreeColumnLocationAdapter(districts, new LocationItemClickListener() {
                    @Override
                    public void onItemClick(String location) {
                        if (locationListenerHolder[0] != null) {
                            locationListenerHolder[0].onDistrictSelected(location);
                        }
                    }
                });
                districtAdapterHolder[0] = districtAdapter;
                rvDistricts.setAdapter(districtAdapter);
                
                // 更新城市选择状态
                if (cityAdapterHolder[0] != null) {
                    cityAdapterHolder[0].setSelectedItem(city);
                }
            }
            
            @Override
            public void onDistrictSelected(String district) {
                selectedDistrict = district;
                
                // 更新区县选择状态
                if (districtAdapterHolder[0] != null) {
                    districtAdapterHolder[0].setSelectedItem(district);
                }
                
                // 更新所在地显示
                if (tvLocation != null) {
                    tvLocation.setText(selectedProvince + " " + selectedCity + " " + selectedDistrict);
                    tvLocation.setTextColor(getResources().getColor(R.color.text_primary));
                }
                
                // 关闭省市区选择对话框
                if (dialogHolder[0] != null) {
                    dialogHolder[0].dismiss();
                }
            }
        };
        
        // 保存监听器引用
        locationListenerHolder[0] = locationListener;
        
        // 初始化省份列表
        ArrayList<String> provinces = new ArrayList<>(provinceCityMap.keySet());
        ThreeColumnLocationAdapter provinceAdapter = new ThreeColumnLocationAdapter(provinces, new LocationItemClickListener() {
            @Override
            public void onItemClick(String location) {
                if (locationListenerHolder[0] != null) {
                    locationListenerHolder[0].onProvinceSelected(location);
                }
            }
        });
        provinceAdapterHolder[0] = provinceAdapter;
        rvProvinces.setAdapter(provinceAdapter);
        
        // 初始化空的城市和区县列表
        cityAdapterHolder[0] = new ThreeColumnLocationAdapter(new ArrayList<>(), null);
        districtAdapterHolder[0] = new ThreeColumnLocationAdapter(new ArrayList<>(), null);
        rvCities.setAdapter(cityAdapterHolder[0]);
        rvDistricts.setAdapter(districtAdapterHolder[0]);
        
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialogHolder[0] = dialog;
        
        // 当对话框显示时，如果有预选值则恢复状态
        dialog.setOnShowListener(dialogInterface -> {
            if (!selectedProvince.isEmpty()) {
                // 恢复省份选择
                if (locationListenerHolder[0] != null) {
                    locationListenerHolder[0].onProvinceSelected(selectedProvince);
                    
                    if (!selectedCity.isEmpty()) {
                        // 恢复城市选择
                        locationListenerHolder[0].onCitySelected(selectedCity);
                        
                        if (!selectedDistrict.isEmpty()) {
                            // 恢复区县选择状态显示
                            if (districtAdapterHolder[0] != null) {
                                districtAdapterHolder[0].setSelectedItem(selectedDistrict);
                            }
                        }
                    }
                }
            }
        });
        
        dialog.show();
    }
    
    /**
     * 禁用Tab的辅助方法
     */
    private void disableTab(TabLayout tabLayout, int position) {
        if (position >= 0 && position < tabLayout.getTabCount()) {
            TabLayout.Tab tab = tabLayout.getTabAt(position);
            if (tab != null && tab.view != null) {
                tab.view.setEnabled(false);
                TextView textView = tab.view.findViewById(android.R.id.text1);
                if (textView != null) {
                    textView.setTextColor(Color.parseColor("#CCCCCC"));
                }
            }
        }
    }
    
    /**
     * 启用Tab的辅助方法
     */
    private void enableTab(TabLayout tabLayout, int position) {
        if (position >= 0 && position < tabLayout.getTabCount()) {
            TabLayout.Tab tab = tabLayout.getTabAt(position);
            if (tab != null && tab.view != null) {
                tab.view.setEnabled(true);
                TextView textView = tab.view.findViewById(android.R.id.text1);
                if (textView != null) {
                    textView.setTextColor(Color.parseColor("#3F51B5")); // primary color
                }
            }
        }
    }
    
    /**
     * 省市区选择监听器接口
     */
    private interface OnLocationSelectedListener {
        void onProvinceSelected(String province);
        void onCitySelected(String city);
        void onDistrictSelected(String district);
    }
    
    /**
     * 省市区选择器ViewPager适配器
     */
    private class LocationPagerAdapter extends androidx.viewpager.widget.PagerAdapter {
        private ArrayList<String> provinces;
        private Map<String, List<String>> provinceCityMap;
        private Map<String, List<String>> cityDistrictMap;
        private OnLocationSelectedListener listener;
        // 新增：保存当前选择的省市区数据，避免引用外部变量导致的数据同步问题
        private String currentProvince = "";
        private String currentCity = "";
        
        public LocationPagerAdapter(ArrayList<String> provinces,
                                  Map<String, List<String>> provinceCityMap,
                                  Map<String, List<String>> cityDistrictMap,
                                  OnLocationSelectedListener listener) {
            this.provinces = provinces;
            this.provinceCityMap = provinceCityMap;
            this.cityDistrictMap = cityDistrictMap;
            this.listener = listener;
        }
        
        // 新增：更新当前选择的省份
        public void updateCurrentProvince(String province) {
            this.currentProvince = province;
        }
        
        // 新增：更新当前选择的城市
        public void updateCurrentCity(String city) {
            this.currentCity = city;
        }
        
        @Override
        public int getCount() {
            return 3; // 省、市、区三个Tab
        }
        
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
        
        @Override
        public int getItemPosition(Object object) {
            // 强制刷新所有页面
            return POSITION_NONE;
        }
        
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // 创建RecyclerView用于显示列表
            RecyclerView recyclerView = new RecyclerView(container.getContext());
            recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
            
            // 根据Tab位置设置不同的适配器
            if (position == 0) {
                // 省份列表
                LocationListAdapter adapter = new LocationListAdapter(provinces, new LocationItemClickListener() {
                         @Override
                         public void onItemClick(String location) {
                        if (listener != null) {
                            listener.onProvinceSelected(location);
                        }
                    }
                });
                recyclerView.setAdapter(adapter);
            } else if (position == 1) {
                // 城市列表 - 修复：使用适配器内部的currentProvince变量
                ArrayList<String> cities = new ArrayList<>();
                if (!currentProvince.isEmpty() && provinceCityMap.containsKey(currentProvince)) {
                    List<String> cityList = provinceCityMap.get(currentProvince);
                    if (cityList != null) {
                        cities.addAll(cityList);
                    }
                }
                LocationListAdapter adapter = new LocationListAdapter(cities, new LocationItemClickListener() {
                    @Override
                    public void onItemClick(String location) {
                        if (listener != null) {
                            listener.onCitySelected(location);
                        }
                    }
                });
                recyclerView.setAdapter(adapter);
            } else if (position == 2) {
                // 区县列表 - 修复：使用适配器内部的currentCity变量
                ArrayList<String> districts = new ArrayList<>();
                if (!currentCity.isEmpty() && cityDistrictMap.containsKey(currentCity)) {
                    List<String> districtList = cityDistrictMap.get(currentCity);
                    if (districtList != null) {
                        districts.addAll(districtList);
                    }
                }
                // 如果没有找到区县数据，添加一个默认值
                if (districts.isEmpty()) {
                    districts.add("市辖区");
                }
                
                // 记录已处理的城市，用于调试
                Log.d("AddressActivity", "处理城市: " + currentCity + ", 区县数量: " + districts.size());
                
                LocationListAdapter adapter = new LocationListAdapter(districts, new LocationItemClickListener() {
                    @Override
                    public void onItemClick(String location) {
                        if (listener != null) {
                            listener.onDistrictSelected(location);
                        }
                    }
                });
                recyclerView.setAdapter(adapter);
            }
            
            container.addView(recyclerView);
            return recyclerView;
        }
        
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
    
    /**
     * 三列显示的省市区列表适配器
     */
    private class ThreeColumnLocationAdapter extends RecyclerView.Adapter<ThreeColumnLocationAdapter.LocationViewHolder> {
        private ArrayList<String> locations;
        private LocationItemClickListener listener;
        private String selectedItem = "";
        
        public ThreeColumnLocationAdapter(ArrayList<String> locations, LocationItemClickListener listener) {
            this.locations = locations;
            this.listener = listener;
        }
        
        public void setSelectedItem(String item) {
            this.selectedItem = item;
            notifyDataSetChanged();
        }
        
        @Override
        public LocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location_three_column, parent, false);
            return new LocationViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(LocationViewHolder holder, int position) {
            final String location = locations.get(position);
            holder.tvLocation.setText(location);
            
            // 设置选中状态
            if (location.equals(selectedItem)) {
                holder.itemView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                holder.tvLocation.setTextColor(getResources().getColor(android.R.color.white));
            } else {
                holder.itemView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                holder.tvLocation.setTextColor(getResources().getColor(R.color.text_primary));
            }
            
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        selectedItem = location;
                        notifyDataSetChanged();
                        listener.onItemClick(location);
                    }
                }
            });
        }
        
        @Override
        public int getItemCount() {
            return locations.size();
        }
        
        class LocationViewHolder extends RecyclerView.ViewHolder {
            TextView tvLocation;
            
            public LocationViewHolder(View itemView) {
                super(itemView);
                tvLocation = itemView.findViewById(R.id.tv_location);
            }
        }
    }
    
    /**
      * 省市区列表适配器的点击监听器接口
      */
     private interface LocationItemClickListener {
         void onItemClick(String location);
     }
     
     /**
      * 省市区列表适配器
      */
     private class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.LocationViewHolder> {
         private ArrayList<String> locations;
         private LocationItemClickListener listener;
          
         public LocationListAdapter(ArrayList<String> locations, LocationItemClickListener listener) {
             this.locations = locations;
             this.listener = listener;
         }
        
        @Override
        public LocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location, parent, false);
            return new LocationViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(LocationViewHolder holder, int position) {
            final String location = locations.get(position);
            holder.tvLocation.setText(location);
            
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(location);
                    }
                }
            });
        }
        
        @Override
        public int getItemCount() {
            return locations.size();
        }
        
        class LocationViewHolder extends RecyclerView.ViewHolder {
            TextView tvLocation;
            
            public LocationViewHolder(View itemView) {
                super(itemView);
                tvLocation = itemView.findViewById(R.id.tv_location);
            }
        }
    }
    
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
        
        // 获取所在地选择区域的引用
        layoutLocationSelector = dialogView.findViewById(R.id.layout_location_selector);
        tvLocation = dialogView.findViewById(R.id.tv_location);
        
        // 预填当前地址信息
        etName.setText(address.getName());
        etPhone.setText(address.getPhone());
        
        // 设置省市区信息
        selectedProvince = address.getProvince() != null ? address.getProvince() : "";
        selectedCity = address.getCity() != null ? address.getCity() : "";
        selectedDistrict = address.getDistrict() != null ? address.getDistrict() : "";
        
        // 显示省市区信息
        if (!selectedProvince.isEmpty() && !selectedCity.isEmpty() && !selectedDistrict.isEmpty()) {
            tvLocation.setText(selectedProvince + " " + selectedCity + " " + selectedDistrict);
            tvLocation.setTextColor(getResources().getColor(R.color.text_primary));
        } else {
            tvLocation.setText("请选择省/市/区");
            tvLocation.setTextColor(getResources().getColor(R.color.text_hint));
        }
        
        // 设置详细地址
        etAddress.setText(address.getDetailAddress() != null ? address.getDetailAddress() : "");
        
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
        
        // 所在地选择区域点击事件
        layoutLocationSelector.setOnClickListener(v -> {
            // 显示省市区选择器
            showLocationPickerDialog(dialog);
        });
        
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String detailAddress = etAddress.getText().toString().trim();
            
            if (name.isEmpty() || phone.isEmpty() || detailAddress.isEmpty()) {
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (selectedProvince.isEmpty() || selectedCity.isEmpty() || selectedDistrict.isEmpty()) {
                Toast.makeText(this, "请选择省/市/区", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (userId == -1) {
                Toast.makeText(this, "用户未登录，请先登录", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 更新地址到服务器
            updateAddressToServer(address.getId(), name, phone, selectedProvince, selectedCity, selectedDistrict, detailAddress);
            dialog.dismiss();
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    /**
     * 更新地址到服务器
     */
    private void updateAddressToServer(int addressId, String name, String phone, String province, String city, String district, String detailAddress) {
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