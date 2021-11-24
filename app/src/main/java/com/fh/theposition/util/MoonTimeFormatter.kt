package com.fh.theposition.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object MoonTimeFormatter {

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatMoonDate(zonedDateTime: ZonedDateTime): String {
        return try {
            zonedDateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm:ss a"))
        } catch (e: Exception) {
            "N/A"
        }
    }
}