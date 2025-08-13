package com.wenteng.frontend_android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wenteng.frontend_android.R;
import com.wenteng.frontend_android.adapter.BookAdapter;
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
    private BookAdapter chineseMedicineBookAdapter;
    private BookAdapter westernMedicineBookAdapter;
    private List<Book> chineseMedicineBooks;
    private List<Book> westernMedicineBooks;
    private Call<ApiResponse<List<Book>>> booksCall; // 用于取消请求

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_health, container, false);
        initViews(view);
        initData();
        setupRecyclerViews();
        return view;
    }

    private void initViews(View view) {
        chineseMedicineBooksRecyclerView = view.findViewById(R.id.chinese_medicine_books);
        westernMedicineBooksRecyclerView = view.findViewById(R.id.western_medicine_books);
    }

    private void initData() {
        // 初始化空列表
        chineseMedicineBooks = new ArrayList<>();
        westernMedicineBooks = new ArrayList<>();
        
        // 从API加载数据
        loadChineseMedicineBooksFromApi();
        loadWesternMedicineBooksFromApi();
    }
    //private Call<ApiResponse<List<Book>>> booksCall; // 用于取消请求

    private void loadChineseMedicineBooksFromApi() {
        // 显示加载状态
       // showLoading(true);

        // 取消之前的请求（避免重复请求）
        if (booksCall != null && !booksCall.isCanceled()) {
            booksCall.cancel();
        }

        booksCall = ApiClient.getApiService().getChineseMedicineBooks();

        booksCall.enqueue(new Callback<ApiResponse<List<Book>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Book>>> call, Response<ApiResponse<List<Book>>> response) {
               // showLoading(false);

                // 1. 检查Fragment是否已分离
                if (!isFragmentActive()) {
                 //   Log.w("API", "Fragment detached, ignoring response");
                    return;
                }

                // 2. 处理HTTP层面错误
                if (!response.isSuccessful()) {
                    handleHttpError(response);
                    return;
                }

                // 3. 检查响应体
                if (response.body() == null) {
                    showMessage("服务器返回空数据");
                   // Log.e("API", "Empty response body");
                    return;
                }

                ApiResponse<List<Book>> apiResponse = response.body();

                // 4. 处理业务逻辑错误
                if (!apiResponse.isSuccess()) {
                    showMessage(apiResponse.getMessage() != null ?
                            apiResponse.getMessage() : "获取数据失败");
                   // Log.e("API", "API Error: " + apiResponse.getMessage());
                    return;
                }

                // 5. 处理数据
                if (apiResponse.getData() == null || apiResponse.getData().isEmpty()) {
                    showMessage("暂无中医古籍数据");
                    return;
                }

                handleSuccessResponse(apiResponse.getData());
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Book>>> call, Throwable t) {
               // showLoading(false);

                if (!isFragmentActive()) {
                    return;
                }

                // 6. 处理网络失败
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

           // Log.e("API", "HTTP Error: " + response.code() + ", Body: " + errorBody);

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
           // Log.e("API", "Error parsing error body", e);
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

       // Log.e("API", "Network Error: " + t.getClass().getSimpleName(), t);
        showMessage(errorMsg);
    }

    private void handleSuccessResponse(List<Book> books) {
        chineseMedicineBooks.clear();
        chineseMedicineBooks.addAll(books);

        // 初始化或更新Adapter
        if (chineseMedicineBookAdapter == null) {
            setupChineseMedicineRecyclerView();
        } else {
            chineseMedicineBookAdapter.notifyDataSetChanged();
        }

        showMessage(String.format(Locale.getDefault(),
                "已加载 %d 本中医古籍", books.size()));

        // 可选：保存到本地数据库
       // saveBooksToLocal(books);
    }

//    private void showLoading(boolean show) {
//        if (isFragmentActive()) {
//            // 这里替换为你实际的加载UI控制逻辑
//            if (progressBar != null) {
//                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
//            }
//        }
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 取消网络请求
        if (booksCall != null) {
            booksCall.cancel();
        }
    }
//    private void loadChineseMedicineBooksFromApi() {
//        Call<ApiResponse<List<Book>>> call = ApiClient.getApiService().getChineseMedicineBooks();
//
//        call.enqueue(new Callback<ApiResponse<List<Book>>>() {
//            @Override
//            public void onResponse(Call<ApiResponse<List<Book>>> call, Response<ApiResponse<List<Book>>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    ApiResponse<List<Book>> apiResponse = response.body();
//                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
//                        List<Book> books = apiResponse.getData();
//                        chineseMedicineBooks.clear();
//                        chineseMedicineBooks.addAll(books);
//
//                        // 更新UI - 确保Fragment仍然活跃
//                        if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
//                            if (chineseMedicineBookAdapter != null) {
//                                chineseMedicineBookAdapter.notifyDataSetChanged();
//                            } else {
//                                // 如果adapter还未初始化，重新设置RecyclerView
//                                setupChineseMedicineRecyclerView();
//                            }
//
//                            showMessage("成功加载 " + books.size() + " 本中医古籍");
//                        }
//                    } else {
//                        if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
//                            showMessage("获取中医古籍失败: " + apiResponse.getMessage());
//                        }
//                    }
//                } else {
//                    if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
//                        showMessage("网络请求失败，状态码: " + response.code());
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ApiResponse<List<Book>>> call, Throwable t) {
//                if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
//                    showMessage("加载中医古籍失败: " + t.getMessage());
//                }
//                t.printStackTrace();
//            }
//        });
//    }
    
    private void loadWesternMedicineBooksFromApi() {
        Call<ApiResponse<List<Book>>> call = ApiClient.getApiService().getWesternMedicineBooks();
        
        call.enqueue(new Callback<ApiResponse<List<Book>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Book>>> call, Response<ApiResponse<List<Book>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Book>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Book> books = apiResponse.getData();
                        westernMedicineBooks.clear();
                        westernMedicineBooks.addAll(books);
                        
                        // 更新UI - 确保Fragment仍然活跃
                        if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
                            if (westernMedicineBookAdapter != null) {
                                westernMedicineBookAdapter.notifyDataSetChanged();
                            } else {
                                // 如果adapter还未初始化，重新设置RecyclerView
                                setupWesternMedicineRecyclerView();
                            }
                            
                            showMessage("成功加载 " + books.size() + " 本西医经典");
                        }
                    } else {
                        if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
                            showMessage("获取西医经典失败: " + apiResponse.getMessage());
                        }
                    }
                } else {
                    if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
                        showMessage("网络请求失败，状态码: " + response.code());
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<Book>>> call, Throwable t) {
                if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
                    showMessage("加载西医经典失败: " + t.getMessage());
                }
                t.printStackTrace();
            }
        });
    }
    
    private void showMessage(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerViews() {
        // 初始设置RecyclerView，使用空数据
        setupChineseMedicineRecyclerView();
        setupWesternMedicineRecyclerView();
    }
    
    private void setupChineseMedicineRecyclerView() {
        if (isAdded() && getContext() != null && chineseMedicineBooksRecyclerView != null && 
            getActivity() != null && !getActivity().isFinishing()) {
            try {
                chineseMedicineBookAdapter = new BookAdapter(getContext(), chineseMedicineBooks);
                chineseMedicineBooksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                chineseMedicineBooksRecyclerView.setAdapter(chineseMedicineBookAdapter);
            } catch (IllegalStateException e) {
                e.printStackTrace();
                // Fragment状态异常，延迟重试
                if (getView() != null) {
                    getView().post(() -> setupChineseMedicineRecyclerView());
                }
            }
        }
    }
    
    private void setupWesternMedicineRecyclerView() {
        if (isAdded() && getContext() != null && westernMedicineBooksRecyclerView != null && 
            getActivity() != null && !getActivity().isFinishing()) {
            try {
                westernMedicineBookAdapter = new BookAdapter(getContext(), westernMedicineBooks);
                westernMedicineBooksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                westernMedicineBooksRecyclerView.setAdapter(westernMedicineBookAdapter);
            } catch (IllegalStateException e) {
                e.printStackTrace();
                // Fragment状态异常，延迟重试
                if (getView() != null) {
                    getView().post(() -> setupWesternMedicineRecyclerView());
                }
            }
        }
    }
}