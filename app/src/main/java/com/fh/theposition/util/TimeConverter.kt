package com.fh.theposition.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.ZonedDateTime

object TimeConverter {

    @RequiresApi(Build.VERSION_CODES.O)
    fun getSecondsInDegreeWithDecimalPrecision(zonedDateTime: ZonedDateTime): Float {
        return (zonedDateTime.second + zonedDateTime.nano / 1000000000f) * 6f
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getSecondsInDegree(zonedDateTime: ZonedDateTime): Float {
        return zonedDateTime.second * 6f
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMinutesInDegree(zonedDateTime: ZonedDateTime): Float {
        return (zonedDateTime.minute + zonedDateTime.second / 60f) * 6f
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getHoursInDegree(zonedDateTime: ZonedDateTime): Float {
        return 0.5f * (60f * zonedDateTime.hour + zonedDateTime.minute)
    }
}