package com.fh.theposition.decoration;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class CustomCoordinateLayout extends CoordinatorLayout {
    View proxyView;

    public CustomCoordinateLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomCoordinateLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isPointInChildBounds(@NonNull View child, int x, int y) {
        if (super.isPointInChildBounds(child, x, y)){
            return true;
        }

        if (proxyView != null){
            return super.isPointInChildBounds(child, x, y);
        }

        return false;


    }

    public void setProxyView(View view){
        this.proxyView=view;
    }
}
