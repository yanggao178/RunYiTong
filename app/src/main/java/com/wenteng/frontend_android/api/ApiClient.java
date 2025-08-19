package com.wenteng.frontend_android.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Locale;

public class ApiClient {
    // 根据运行环境选择合适的服务器地址
    private static final String BASE_URL = getBaseUrl();
    
    private static String getBaseUrl() {
        // 优先使用localhost进行本地测试
        //
        
        // 如果需要根据环境切换，可以使用以下代码：
        
        String fingerprint = android.os.Build.FINGERPRINT;
        if (fingerprint != null && (fingerprint.startsWith("generic") 
                || fingerprint.startsWith("unknown") 
                || fingerprint.contains("emu64"))) {
            // 模拟器环境
            return "http://10.0.2.15:8000/";
        } else {
            // 真实设备环境，使用局域网IP地址
           return "http://192.168.0.8:8000/";
          ///  return "http://8.141.2.166:8000";
        }
        
    }
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;
    
    /**
     * 获取Retrofit实例
     */
    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // 创建日志拦截器
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            // 创建OkHttpClient
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
            
            // 创建自定义Gson实例处理日期格式
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
                    .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> {
                        try {
                            String dateString = json.getAsString();
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault());
                            return format.parse(dateString);
                        } catch (ParseException e) {
                            try {
                                // 尝试另一种格式
                                String dateString = json.getAsString();
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
                                return format.parse(dateString);
                            } catch (ParseException ex) {
                                return null;
                            }
                        }
                    })
                    .create();
            
            // 创建Retrofit实例
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
    
    /**
     * 获取ApiClient实例
     */
    public static ApiClient getInstance() {
        return new ApiClient();
    }
    
    /**
     * 获取API服务实例
     */
    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofitInstance().create(ApiService.class);
        }
        return apiService;
    }
}