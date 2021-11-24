package com.fh.theposition.util

import android.content.Context
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.fh.theposition.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

object AsyncImageLoader {

    fun loadImage(value: Int, imageView: ImageView, context: Context, delay: Int) {
        CoroutineScope(Dispatchers.Default).launch {
            val drawable = if (value != 0) context.resources?.let {
                ResourcesCompat.getDrawable(it, value, context.theme)
            }!! else null


            withContext(Dispatchers.Main) {
                val animOut: Animation = AnimationUtils.loadAnimation(context, R.anim.image_out)
                val animIn: Animation = AnimationUtils.loadAnimation(context, R.anim.image_in)

                animOut.startOffset = delay.toLong()
                animIn.startOffset = delay.toLong()


                animOut.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        imageView.setImageDrawable(drawable)

                        animIn.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(animation: Animation?) {}

                            override fun onAnimationEnd(animation: Animation?) {}

                            override fun onAnimationRepeat(animation: Animation?) {}

                        })

                        imageView.startAnimation(animIn)

                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })

                imageView.startAnimation(animOut)
            }


        }
    }


    fun loadImageWithoutAnimation(value: Int, imageView: ImageView, context: Context) {
        CoroutineScope(Dispatchers.Default).launch {
            val drawable = if (value != 0) context.resources?.let {
                ResourcesCompat.getDrawable(it, value, context.theme)
            }!! else null

            withContext(Dispatchers.Main) {
                try {
                    imageView.setImageDrawable(drawable)
                }catch (exception:Exception){
                    exception.printStackTrace()
                }
            }
        }
    }
}