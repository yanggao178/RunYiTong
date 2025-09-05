package com.wenxing.runyitong.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import androidx.recyclerview.widget.RecyclerView;

public class BookshelfAnimationUtils {
    
    /**
     * 书籍加载动画 - 从下方滑入并带有缩放效果
     */
    public static void animateBookEntry(View view, int position) {
        view.setTranslationY(200f);
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.setAlpha(0f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        
        ObjectAnimator translateY = ObjectAnimator.ofFloat(view, "translationY", 200f, 0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        
        animatorSet.playTogether(translateY, scaleX, scaleY, alpha);
        animatorSet.setDuration(600);
        animatorSet.setStartDelay(position * 100); // 错开动画时间
        animatorSet.setInterpolator(new OvershootInterpolator(0.8f));
        animatorSet.start();
    }
    
    /**
     * 书籍点击动画 - 轻微的缩放和旋转效果
     */
    public static void animateBookClick(View view) {
        AnimatorSet animatorSet = new AnimatorSet();
        
        ObjectAnimator scaleXDown = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f);
        ObjectAnimator scaleYDown = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f);
        ObjectAnimator rotationDown = ObjectAnimator.ofFloat(view, "rotation", 0f, -1f);
        
        ObjectAnimator scaleXUp = ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f);
        ObjectAnimator scaleYUp = ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f);
        ObjectAnimator rotationUp = ObjectAnimator.ofFloat(view, "rotation", -1f, 0f);
        
        AnimatorSet downSet = new AnimatorSet();
        downSet.playTogether(scaleXDown, scaleYDown, rotationDown);
        downSet.setDuration(100);
        
        AnimatorSet upSet = new AnimatorSet();
        upSet.playTogether(scaleXUp, scaleYUp, rotationUp);
        upSet.setDuration(200);
        upSet.setInterpolator(new OvershootInterpolator(2f));
        
        animatorSet.playSequentially(downSet, upSet);
        animatorSet.start();
    }
    
    /**
     * 书籍悬停动画 - 轻微上浮效果
     */
    public static void animateBookHover(View view, boolean isHovered) {
        float targetY = isHovered ? -8f : 0f;
        float targetElevation = isHovered ? 16f : 12f;
        
        ObjectAnimator translateY = ObjectAnimator.ofFloat(view, "translationY", view.getTranslationY(), targetY);
        ObjectAnimator elevation = ObjectAnimator.ofFloat(view, "elevation", view.getElevation(), targetElevation);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(translateY, elevation);
        animatorSet.setDuration(200);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }
    
    /**
     * RecyclerView整体加载动画
     */
    public static void animateRecyclerViewEntry(RecyclerView recyclerView) {
        recyclerView.setAlpha(0f);
        recyclerView.setTranslationY(50f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        
        ObjectAnimator alpha = ObjectAnimator.ofFloat(recyclerView, "alpha", 0f, 1f);
        ObjectAnimator translateY = ObjectAnimator.ofFloat(recyclerView, "translationY", 50f, 0f);
        
        animatorSet.playTogether(alpha, translateY);
        animatorSet.setDuration(500);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }
}