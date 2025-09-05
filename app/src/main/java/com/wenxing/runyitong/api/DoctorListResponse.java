package com.wenxing.runyitong.api;

import com.google.gson.annotations.SerializedName;
import com.wenxing.runyitong.model.Doctor;

import java.io.Serializable;
import java.util.List;

public class DoctorListResponse implements Serializable {
    @SerializedName("doctors")
    private List<Doctor> doctors;

    public DoctorListResponse() {
    }

    public DoctorListResponse(List<Doctor> doctors) {
        this.doctors = doctors;
    }

    public List<Doctor> getDoctors() {
        return doctors;
    }

    public void setDoctors(List<Doctor> doctors) {
        this.doctors = doctors;
    }

    @Override
    public String toString() {
        return "DoctorListResponse{" +
                "doctors=" + doctors +
                '}';
    }
}