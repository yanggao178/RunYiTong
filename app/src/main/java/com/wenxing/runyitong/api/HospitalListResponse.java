package com.wenxing.runyitong.api;

import com.google.gson.annotations.SerializedName;
import com.wenxing.runyitong.model.Hospital;

import java.io.Serializable;
import java.util.List;

public class HospitalListResponse implements Serializable {
    @SerializedName("hospitals")
    private List<Hospital> hospitals;

    public HospitalListResponse() {
    }

    public HospitalListResponse(List<Hospital> hospitals) {
        this.hospitals = hospitals;
    }

    public List<Hospital> getHospitals() {
        return hospitals;
    }

    public void setHospitals(List<Hospital> hospitals) {
        this.hospitals = hospitals;
    }

    @Override
    public String toString() {
        return "HospitalListResponse{" +
                "hospitals=" + hospitals +
                '}';
    }
}