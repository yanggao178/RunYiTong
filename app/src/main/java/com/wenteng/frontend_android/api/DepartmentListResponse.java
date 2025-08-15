package com.wenteng.frontend_android.api;

import com.google.gson.annotations.SerializedName;
import com.wenteng.frontend_android.model.Department;

import java.io.Serializable;
import java.util.List;

public class DepartmentListResponse implements Serializable {
    @SerializedName("departments")
    private List<Department> departments;

    public DepartmentListResponse() {
    }

    public DepartmentListResponse(List<Department> departments) {
        this.departments = departments;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    @Override
    public String toString() {
        return "DepartmentListResponse{" +
                "departments=" + departments +
                '}';
    }
}