package com.fh.theposition.util

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.RadioButton
import androidx.annotation.RequiresApi
import com.google.android.material.animation.ArgbEvaluatorCompat

object AnimationUtil {

    public class AnimUtils {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        fun animateBackground(endColor: Int, view: View) {
            view.clearAnimation()
            val valueAnimator = ValueAnimator.ofObject(
                ArgbEvaluatorCompat(),
                view.backgroundTintList!!.defaultColor,
                endColor
            )
            valueAnimator.duration = 300L
            valueAnimator.interpolator = DecelerateInterpolator(1.5f)
            valueAnimator.addUpdateListener { animation: ValueAnimator ->
                view.backgroundTintList = ColorStateList.valueOf(animation.animatedValue as Int)
            }
            valueAnimator.start()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun animateBackground(endColor: Int, view: ViewGroup) {
        view.clearAnimation()
        val valueAnimator = ValueAnimator.ofObject(
            ArgbEvaluatorCompat(),
            view.backgroundTintList!!.defaultColor,
            endColor
        )
        valueAnimator.duration = 300L
        valueAnimator.interpolator = DecelerateInterpolator(1.5f)
        valueAnimator.addUpdateListener { animation: ValueAnimator ->
            view.backgroundTintList = ColorStateList.valueOf(animation.animatedValue as Int)
        }
        valueAnimator.start()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun animateTint(endColor: Int, view: RadioButton) {
        view.clearAnimation()
        val valueAnimator = ValueAnimator.ofObject(
            ArgbEvaluatorCompat(),
            view.buttonTintList!!.defaultColor,
            endColor
        )
        valueAnimator.duration = 300L
        valueAnimator.interpolator = DecelerateInterpolator(1.5f)
        valueAnimator.addUpdateListener { animation: ValueAnimator ->
            view.buttonTintList = ColorStateList.valueOf(animation.animatedValue as Int)
        }
        valueAnimator.start()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun animateElevation(elevation: Float?, button: RadioButton) {
        button.clearAnimation()
        val valueAnimator = ValueAnimator.ofFloat(button.elevation, elevation!!)
        valueAnimator.duration = 300L
        valueAnimator.interpolator = DecelerateInterpolator(1.5f)
        valueAnimator.addUpdateListener { animation: ValueAnimator ->
            button.elevation = (animation.animatedValue as Float)
        }
        valueAnimator.start()
    }
}