package com.wenxing.runyitong.utils;

import android.content.Context;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ResponsiveGridLayoutManager extends GridLayoutManager {
    private int itemWidth;
    private boolean isInitialized = false;
    
    public ResponsiveGridLayoutManager(Context context, int itemWidthDp) {
        super(context, 1);
        this.itemWidth = (int) (itemWidthDp * context.getResources().getDisplayMetrics().density);
    }
    
    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (!isInitialized && getWidth() > 0) {
            int totalWidth = getWidth() - getPaddingLeft() - getPaddingRight();
            int spanCount = Math.max(1, totalWidth / itemWidth);
            setSpanCount(spanCount);
            isInitialized = true;
        }
        super.onLayoutChildren(recycler, state);
    }
    
    public void resetInitialization() {
        isInitialized = false;
    }
}