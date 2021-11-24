package com.fh.theposition.util

import android.content.Context
import com.fh.theposition.R

object MoonAngle {
    fun getMoonPhase(context: Context, moonAngle: Double): String {
        return if (moonAngle > -180.0 && moonAngle < -135.0) {
            context.getString(R.string.new_moon)
        } else if (moonAngle > -135 && moonAngle < -90) {
            context.getString(R.string.waxing_crescent)
        } else if (moonAngle > -90 && moonAngle < -45) {
            context.getString(R.string.first_quarter)
        } else if (moonAngle > -45 && moonAngle < 0) {
            context.getString(R.string.waxing_gibbous)
        } else if (moonAngle > 0 && moonAngle < 45) {
            context.getString(R.string.full_moon)
        } else if (moonAngle > 45 && moonAngle < 90) {
            context.getString(R.string.waning_gibbous)
        } else if (moonAngle > 90 && moonAngle < 135) {
            context.getString(R.string.third_quarter)
        } else if (moonAngle > 135 && moonAngle < 180) {
            context.getString(R.string.waning_crescent)
        } else {
            context.getString(R.string.not_available)
        }
    }

}