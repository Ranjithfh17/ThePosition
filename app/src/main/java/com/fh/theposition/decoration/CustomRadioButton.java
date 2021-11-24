package com.fh.theposition.decoration;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatRadioButton;

import com.fh.theposition.R;
import com.fh.theposition.util.AnimationUtil;
import com.fh.theposition.util.ColorUtils;

public class CustomRadioButton extends AppCompatRadioButton {


    public CustomRadioButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRadioButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("ResourceType")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setChecked(boolean checked) {
        if (checked){
            AnimationUtil.INSTANCE.animateTint(ColorUtils.INSTANCE.resolveAttrColor(getContext(), R.color.blue), this);
        }else {
            AnimationUtil.INSTANCE.animateTint(ColorUtils.INSTANCE.resolveAttrColor(getContext(), R.color.teal_700),this);
        }
        super.setChecked(checked);
    }
}
