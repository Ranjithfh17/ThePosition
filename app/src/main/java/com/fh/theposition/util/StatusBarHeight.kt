package com.fh.theposition.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import android.view.Window
import com.fh.theposition.R

object StatusBarHeight {

    //get statusBar height using window object
    fun getStatusBarHeight(window: Window): Int {
        val rect = Rect()
        window.decorView.getWindowVisibleDisplayFrame(rect)
        return rect.top - window.findViewById<View>(Window.ID_ANDROID_CONTENT).top
    }


    // get statusBar height using system framework
    fun getStatusBarHeight(resources: Resources): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")

        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result

    }

    //get toolbar height using  context resources
    fun getToolBarHeight(context: Context): Int {
        var height = 0
        val typedValue = TypedValue()
        if (context.theme.resolveAttribute(R.attr.actionBarSize, typedValue, true)) {
            height = TypedValue.complexToDimensionPixelSize(
                typedValue.data,
                context.resources.displayMetrics
            )
        }
        return height

    }


    //get navigationBarHeight using system framework
    fun getNavigationBarHeight(resources: Resources): Int {
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            if (isEdgeToEdgeEnabled(resources) == 2) {
                return 0
            } else {
                resources.getDimensionPixelSize(resourceId)
            }
        }

        return 0

    }


    /**
     * Checks if the current device has gesture mode turned on
     *
     * @param resources of the current context environment
     * @return 0 : Navigation is displaying with 3 buttons
     * 1 : Navigation is displaying with 2 button(Android P navigation mode)
     * 2 : Full screen gesture(Gesture on android Q)
     */
    fun isEdgeToEdgeEnabled(resources: Resources): Int {
        val resourceId =
            resources.getIdentifier("config_navBarInteractionMode", "integer", "android")
        return if (resourceId > 0) {
            resources.getInteger(resourceId)
        } else 0
    }
}