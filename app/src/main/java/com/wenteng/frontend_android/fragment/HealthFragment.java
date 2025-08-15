package com.wenteng.frontend_android.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wenteng.frontend_android.R;
import com.wenteng.frontend_android.adapter.BookshelfAdapter;
import com.wenteng.frontend_android.model.Book;
import com.wenteng.frontend_android.api.ApiClient;
import com.wenteng.frontend_android.api.ApiResponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.Toast;
import java.net.SocketTimeoutException;
import java.net.ConnectException;
import java.io.IOException;
import javax.net.ssl.SSLHandshakeException;
import java.util.Locale;

public class HealthFragment extends Fragment {

    private RecyclerView chineseMedicineBooksRecyclerView;
    private RecyclerView westernMedicineBooksRecyclerView;
    private BookshelfAdapter chineseMedicineBookAdapter;
    private BookshelfAdapter westernMedicineBookAdapter;
    private List<Book> chineseMedicineBooks;
    private List<Book> westernMedicineBooks;
    private Call<ApiResponse<List<Book>>> chineseBooksCall; // 用于取消请求
    private Call<ApiResponse<List<Book>>> westernBooksCall; // 用于取消请求
    private boolean isDataLoaded = false; // 标记数据是否已加载

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_health, container, false);
        initViews(view);
        initData();
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d("HealthFragment", "onResume called");
        // 在Fragment恢复时检查是否需要加载数据
        if (!isDataLoaded) {
            loadBooksData();
        }
    }
    
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d("HealthFragment", "onHiddenChanged called, hidden=" + hidden);
        if (!hidden && !isDataLoaded) {
            // Fragment变为可见且数据未加载时，加载数据
            loadBooksData();
        }
    }
    
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d("HealthFragment", "setUserVisibleHint called, isVisibleToUser=" + isVisibleToUser);
        if (isVisibleToUser && !isDataLoaded) {
            // Fragment变为可见且数据未加载时，加载数据
            loadBooksData();
        }
    }

    private void initViews(View view) {
        chineseMedicineBooksRecyclerView = view.findViewById(R.id.chinese_medicine_books);
        westernMedicineBooksRecyclerView = view.findViewById(R.id.western_medicine_books);
        
        // 设置书架网格布局管理器
        if (chineseMedicineBooksRecyclerView != null) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3); // 3列网格
            chineseMedicineBooksRecyclerView.setLayoutManager(gridLayoutManager);
        }
        if (westernMedicineBooksRecyclerView != null) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3); // 3列网格
            westernMedicineBooksRecyclerView.setLayoutManager(gridLayoutManager);
        }
    }

    private void initData() {
        // 初始化空列表
        chineseMedicineBooks = new ArrayList<>();
        westernMedicineBooks = new ArrayList<>();
        
        // 立即创建书架适配器
        if (getContext() != null) {
            chineseMedicineBookAdapter = new BookshelfAdapter(getContext(), chineseMedicineBooks);
            westernMedicineBookAdapter = new BookshelfAdapter(getContext(), westernMedicineBooks);
            
            // 设置适配器
            if (chineseMedicineBooksRecyclerView != null) {
                chineseMedicineBooksRecyclerView.setAdapter(chineseMedicineBookAdapter);
            }
            if (westernMedicineBooksRecyclerView != null) {
                westernMedicineBooksRecyclerView.setAdapter(westernMedicineBookAdapter);
            }
        }
    }
    
    private void loadBooksData() {
        if (isDataLoaded) {
            Log.d("HealthFragment", "数据已加载，跳过重复加载");
            return;
        }
        
        Log.d("HealthFragment", "开始加载书籍数据");
        loadChineseMedicineBooksFromApi();
        loadWesternMedicineBooksFromApi();
    }

    private void loadChineseMedicineBooksFromApi() {
        Log.d("HealthFragment", "开始请求中医书籍数据");

        // 取消之前的请求（避免重复请求）
        if (chineseBooksCall != null && !chineseBooksCall.isCanceled()) {
            chineseBooksCall.cancel();
        }

        chineseBooksCall = ApiClient.getApiService().getChineseMedicineBooks();
        Log.d("HealthFragment", "请求URL: " + chineseBooksCall.request().url());

        chineseBooksCall.enqueue(new Callback<ApiResponse<List<Book>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Book>>> call, Response<ApiResponse<List<Book>>> response) {
                Log.d("HealthFragment", "中医书籍API响应: " + response.code());

                // 1. 检查Fragment是否已分离
                if (!isFragmentActive()) {
                    Log.w("HealthFragment", "Fragment detached, ignoring response");
                    return;
                }

                // 2. 处理HTTP层面错误
                if (!response.isSuccessful()) {
                    handleHttpError(response);
                    return;
                }

                // 3. 检查响应体
                if (response.body() == null) {
                    Log.e("HealthFragment", "Empty response body");
                    showMessage("服务器返回空数据");
                    return;
                }

                ApiResponse<List<Book>> apiResponse = response.body();

                // 4. 处理业务逻辑错误
                if (!apiResponse.isSuccess()) {
                    Log.e("HealthFragment", "API Error: " + apiResponse.getMessage());
                    showMessage(apiResponse.getMessage() != null ?
                            apiResponse.getMessage() : "获取数据失败");
                    return;
                }

                // 5. 处理数据
                if (apiResponse.getData() == null || apiResponse.getData().isEmpty()) {
                    Log.w("HealthFragment", "No Chinese medicine books data");
                    showMessage("暂无中医古籍数据");
                    return;
                }

                handleChineseMedicineSuccessResponse(apiResponse.getData());
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Book>>> call, Throwable t) {
                Log.e("HealthFragment", "中医书籍API请求失败", t);

                if (!isFragmentActive()) {
                    return;
                }

                // 处理网络失败
                handleNetworkError(t);
            }
        });
    }
    
    private void loadWesternMedicineBooksFromApi() {
        Log.d("HealthFragment", "开始请求西医书籍数据");

        // 取消之前的请求（避免重复请求）
        if (westernBooksCall != null && !westernBooksCall.isCanceled()) {
            westernBooksCall.cancel();
        }

        westernBooksCall = ApiClient.getApiService().getWesternMedicineBooks();
        Log.d("HealthFragment", "请求URL: " + westernBooksCall.request().url());

        westernBooksCall.enqueue(new Callback<ApiResponse<List<Book>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Book>>> call, Response<ApiResponse<List<Book>>> response) {
                Log.d("HealthFragment", "西医书籍API响应: " + response.code());

                if (!isFragmentActive()) {
                    Log.w("HealthFragment", "Fragment detached, ignoring response");
                    return;
                }

                if (!response.isSuccessful()) {
                    handleHttpError(response);
                    return;
                }

                if (response.body() == null) {
                    Log.e("HealthFragment", "Empty response body");
                    showMessage("服务器返回空数据");
                    return;
                }

                ApiResponse<List<Book>> apiResponse = response.body();

                if (!apiResponse.isSuccess()) {
                    Log.e("HealthFragment", "API Error: " + apiResponse.getMessage());
                    showMessage(apiResponse.getMessage() != null ?
                            apiResponse.getMessage() : "获取数据失败");
                    return;
                }

                if (apiResponse.getData() == null || apiResponse.getData().isEmpty()) {
                    Log.w("HealthFragment", "No Western medicine books data");
                    showMessage("暂无西医经典数据");
                    return;
                }

                handleWesternMedicineSuccessResponse(apiResponse.getData());
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Book>>> call, Throwable t) {
                Log.e("HealthFragment", "西医书籍API请求失败", t);

                if (!isFragmentActive()) {
                    return;
                }

                handleNetworkError(t);
            }
        });
    }

// === 辅助方法 ===

    private boolean isFragmentActive() {
        return isAdded() && getActivity() != null && !getActivity().isFinishing();
    }

    private void handleHttpError(Response<ApiResponse<List<Book>>> response) {
        try {
            String errorBody = response.errorBody() != null ?
                    response.errorBody().string() : "无错误详情";

            Log.e("HealthFragment", "HTTP Error: " + response.code() + ", Body: " + errorBody);

            switch (response.code()) {
                case 401:
                    showMessage("认证失败，请重新登录");
                    break;
                case 403:
                    showMessage("无权访问此资源");
                    break;
                case 404:
                    showMessage("资源不存在");
                    break;
                case 500:
                    showMessage("服务器内部错误");
                    break;
                default:
                    showMessage("请求失败，状态码: " + response.code());
            }
        } catch (IOException e) {
            Log.e("HealthFragment", "Error parsing error body", e);
            showMessage("网络请求失败");
        }
    }

    private void handleNetworkError(Throwable t) {
        String errorMsg = "加载失败，请检查网络";

        if (t instanceof SocketTimeoutException) {
            errorMsg = "请求超时，请重试";
        } else if (t instanceof ConnectException) {
            errorMsg = "无法连接服务器";
        } else if (t instanceof SSLHandshakeException) {
            errorMsg = "安全连接失败";
        } else if (t instanceof IOException) {
            errorMsg = "网络异常";
        }

        Log.e("HealthFragment", "Network Error: " + t.getClass().getSimpleName(), t);
        showMessage(errorMsg);
    }

    private void handleChineseMedicineSuccessResponse(List<Book> books) {
        Log.d("HealthFragment", "收到中医书籍数据: " + books.size() + " 本");
        for (int i = 0; i < Math.min(books.size(), 3); i++) {
            Book book = books.get(i);
            Log.d("HealthFragment", "书籍 " + i + ": " + book.getName() + " - " + book.getAuthor());
        }
        
        chineseMedicineBooks.clear();
        chineseMedicineBooks.addAll(books);
        Log.d("HealthFragment", "中医书籍列表大小: " + chineseMedicineBooks.size());

        // 更新UI
        if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().runOnUiThread(() -> {
                if (chineseMedicineBookAdapter != null) {
                    chineseMedicineBookAdapter.notifyDataSetChanged();
                    Log.d("HealthFragment", "中医书籍适配器数据已更新");
                }
            });
        }

        showMessage(String.format(Locale.getDefault(), "已加载 %d 本中医古籍", books.size()));
    }
    
    private void handleWesternMedicineSuccessResponse(List<Book> books) {
        Log.d("HealthFragment", "收到西医书籍数据: " + books.size() + " 本");
        for (int i = 0; i < Math.min(books.size(), 3); i++) {
            Book book = books.get(i);
            Log.d("HealthFragment", "书籍 " + i + ": " + book.getName() + " - " + book.getAuthor());
        }
        
        westernMedicineBooks.clear();
        westernMedicineBooks.addAll(books);
        Log.d("HealthFragment", "西医书籍列表大小: " + westernMedicineBooks.size());

        // 更新UI
        if (getActivity() != null && !getActivity().isFinishing()) {
            getActivity().runOnUiThread(() -> {
                if (westernMedicineBookAdapter != null) {
                    westernMedicineBookAdapter.notifyDataSetChanged();
                    Log.d("HealthFragment", "西医书籍适配器数据已更新");
                }
            });
        }

        showMessage(String.format(Locale.getDefault(), "已加载 %d 本西医经典", books.size()));
        
        // 标记数据已加载
        isDataLoaded = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 取消网络请求
        if (chineseBooksCall != null) {
            chineseBooksCall.cancel();
        }
        if (westernBooksCall != null) {
            westernBooksCall.cancel();
        }
        // 重置数据加载状态
        isDataLoaded = false;
    }
    
    private void showMessage(String message) {
        Log.d("HealthFragment", message);
        if (getActivity() != null && !getActivity().isFinishing() && isAdded()) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            });
        }
    }
}