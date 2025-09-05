package com.wenxing.runyitong.utils;

import android.graphics.Rect;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BookshelfItemDecoration extends RecyclerView.ItemDecoration {
    private final int spacing;
    
    public BookshelfItemDecoration(int spacingDp) {
        this.spacing = spacingDp;
    }
    
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, 
                              @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int spanCount = getSpanCount(parent);
        int column = position % spanCount;
        
        // 左右间距
        outRect.left = spacing - column * spacing / spanCount;
        outRect.right = (column + 1) * spacing / spanCount;
        
        // 上下间距
        if (position < spanCount) {
            outRect.top = spacing;
        }
        outRect.bottom = spacing;
    }
    
    private int getSpanCount(RecyclerView parent) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof ResponsiveGridLayoutManager) {
            return ((ResponsiveGridLayoutManager) layoutManager).getSpanCount();
        }
        return 1;
    }
}