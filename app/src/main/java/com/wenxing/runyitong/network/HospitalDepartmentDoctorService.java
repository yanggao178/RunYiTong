package com.wenxing.runyitong.network;

import com.wenxing.runyitong.api.ApiResponse;
import com.wenxing.runyitong.api.ApiService;
import com.wenxing.runyitong.model.Department;
import com.wenxing.runyitong.model.Doctor;
import com.wenxing.runyitong.model.Hospital;
import com.wenxing.runyitong.api.DepartmentListResponse;
import com.wenxing.runyitong.api.DoctorListResponse;
import com.wenxing.runyitong.api.HospitalListResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 医院科室医生服务类
 * 用于实现动态获取医院、科室及医生的网络功能
 */
public class HospitalDepartmentDoctorService {

    private ApiService apiService;

    /**
     * 构造函数
     * @param apiService Retrofit API服务实例
     */
    public HospitalDepartmentDoctorService(ApiService apiService) {
        this.apiService = apiService;
    }

    /**
     * 获取医院列表的回调接口
     */
    public interface GetHospitalsCallback {
        void onSuccess(List<Hospital> hospitals);
        void onFailure(String errorMessage);
    }

    /**
     * 获取科室列表的回调接口
     */
    public interface GetDepartmentsCallback {
        void onSuccess(List<Department> departments);
        void onFailure(String errorMessage);
    }

    /**
     * 获取医生列表的回调接口
     */
    public interface GetDoctorsCallback {
        void onSuccess(List<Doctor> doctors);
        void onFailure(String errorMessage);
    }

    /**
     * 获取所有医院列表
     * @param callback 回调接口
     */
    public void getHospitals(final GetHospitalsCallback callback) {
        Call<ApiResponse<HospitalListResponse>> call = apiService.getHospitals();
        call.enqueue(new Callback<ApiResponse<HospitalListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<HospitalListResponse>> call, Response<ApiResponse<HospitalListResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    callback.onSuccess(response.body().getData().getHospitals());
                } else {
                    callback.onFailure("获取医院列表失败");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<HospitalListResponse>> call, Throwable t) {
                callback.onFailure("网络请求失败：" + t.getMessage());
            }
        });
    }

    /**
     * 获取指定医院的科室列表
     * @param hospitalId 医院ID
     * @param callback 回调接口
     */
    public void getDepartmentsByHospital(int hospitalId, final GetDepartmentsCallback callback) {
        // 使用返回ApiResponse<DepartmentListResponse>的接口
        Call<ApiResponse<DepartmentListResponse>> call = apiService.getHospitalDepartments(hospitalId);
        call.enqueue(new Callback<ApiResponse<DepartmentListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<DepartmentListResponse>> call, Response<ApiResponse<DepartmentListResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    callback.onSuccess(response.body().getData().getDepartments());
                } else {
                    // 尝试使用备用接口
                    getDepartmentsByHospitalAlternative(hospitalId, callback);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DepartmentListResponse>> call, Throwable t) {
                // 尝试使用备用接口
                getDepartmentsByHospitalAlternative(hospitalId, callback);
            }
        });
    }

    /**
     * 使用备用接口获取指定医院的科室列表
     * @param hospitalId 医院ID
     * @param callback 回调接口
     */
    private void getDepartmentsByHospitalAlternative(int hospitalId, final GetDepartmentsCallback callback) {
        // 使用返回List<Department>的备用接口
        Call<List<Department>> call = apiService.getDepartmentsByHospital(hospitalId);
        call.enqueue(new Callback<List<Department>>() {
            @Override
            public void onResponse(Call<List<Department>> call, Response<List<Department>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("获取科室列表失败");
                }
            }

            @Override
            public void onFailure(Call<List<Department>> call, Throwable t) {
                callback.onFailure("网络请求失败：" + t.getMessage());
            }
        });
    }

    /**
     * 获取指定科室的医生列表
     * @param departmentId 科室ID
     * @param hospitalId 医院ID（可选，可为null）
     * @param callback 回调接口
     */
    public void getDoctorsByDepartment(Integer departmentId, Integer hospitalId, final GetDoctorsCallback callback) {
        Call<ApiResponse<DoctorListResponse>> call = apiService.getDoctors(departmentId, hospitalId);
        call.enqueue(new Callback<ApiResponse<DoctorListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<DoctorListResponse>> call, Response<ApiResponse<DoctorListResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    callback.onSuccess(response.body().getData().getDoctors());
                } else {
                    callback.onFailure("获取医生列表失败");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DoctorListResponse>> call, Throwable t) {
                callback.onFailure("网络请求失败：" + t.getMessage());
            }
        });
    }

    /**
     * 获取指定医院和科室的医生列表
     * @param hospitalId 医院ID
     * @param departmentId 科室ID
     * @param callback 回调接口
     */
    public void getDoctorsByHospitalAndDepartment(int hospitalId, int departmentId, final GetDoctorsCallback callback) {
        getDoctorsByDepartment(departmentId, hospitalId, callback);
    }

    /**
     * 获取指定医院的所有医生
     * @param hospitalId 医院ID
     * @param callback 回调接口
     */
    public void getDoctorsByHospital(int hospitalId, final GetDoctorsCallback callback) {
        getDoctorsByDepartment(null, hospitalId, callback);
    }

    /**
     * 获取所有科室列表
     * @param callback 回调接口
     */
    public void getAllDepartments(final GetDepartmentsCallback callback) {
        Call<ApiResponse<DepartmentListResponse>> call = apiService.getDepartments();
        call.enqueue(new Callback<ApiResponse<DepartmentListResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<DepartmentListResponse>> call, Response<ApiResponse<DepartmentListResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    callback.onSuccess(response.body().getData().getDepartments());
                } else {
                    callback.onFailure("获取科室列表失败");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DepartmentListResponse>> call, Throwable t) {
                callback.onFailure("网络请求失败：" + t.getMessage());
            }
        });
    }
}