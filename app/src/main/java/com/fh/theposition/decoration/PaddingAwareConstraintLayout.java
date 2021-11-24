package com.fh.theposition.decoration;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.fh.theposition.util.StatusBarHeight;

public class PaddingAwareConstraintLayout extends ConstraintLayout {

    public PaddingAwareConstraintLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public PaddingAwareConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PaddingAwareConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public PaddingAwareConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        setPadding(getPaddingLeft(),
                StatusBarHeight.INSTANCE.getStatusBarHeight(getResources()) + getPaddingTop(),
                getPaddingRight(),
                StatusBarHeight.INSTANCE.getNavigationBarHeight(getResources()) + getPaddingBottom());
    }
}
