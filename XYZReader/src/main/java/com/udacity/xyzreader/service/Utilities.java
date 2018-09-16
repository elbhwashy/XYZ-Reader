package com.udacity.xyzreader.service;

import android.animation.ValueAnimator;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.FrameLayout;

public class Utilities {
    /*
     * This function is the product of gar at https://stackoverflow.com/a/4009133
     * suggested to use by Udacity to implement network connection check
     * */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm != null ? cm.getActiveNetworkInfo() : null;
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Creates an animator that smoothly animates the passed view height from startHeight to
     * endHeight.
     *
     * @param view        The view that needs to be animated.
     * @param startHeight Starting height of the view.
     * @param endHeight   Final height of the view.
     * @param duration    Duration of the animation.
     * @return ValueAnimator
     */
    public static ValueAnimator getToggleHeightAnimator(final android.view.View view, int startHeight, int endHeight, long duration) {
        ValueAnimator animator = ValueAnimator.ofInt(startHeight, endHeight);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int val = (Integer) animation.getAnimatedValue();
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
                params.height = val;
                view.setLayoutParams(params);
            }
        });
        animator.setDuration(duration);
        return animator;
    }
}