package com.wenxing.runyitong.api;

import com.google.gson.annotations.SerializedName;
import com.wenxing.runyitong.model.Product;
import java.util.List;

public class ProductListResponse {
    @SerializedName("items")
    private List<Product> items;
    
    @SerializedName("total")
    private int total;
    
    @SerializedName("skip")
    private int skip;
    
    @SerializedName("limit")
    private int limit;
    
    public ProductListResponse() {}
    
    public List<Product> getItems() {
        return items;
    }
    
    public void setItems(List<Product> items) {
        this.items = items;
    }
    
    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
    
    public int getSkip() {
        return skip;
    }
    
    public void setSkip(int skip) {
        this.skip = skip;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public void setLimit(int limit) {
        this.limit = limit;
    }
}