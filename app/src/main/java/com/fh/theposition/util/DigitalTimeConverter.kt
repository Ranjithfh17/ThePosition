package com.fh.theposition.util

import android.os.Build
import android.text.SpannableString
import androidx.annotation.RequiresApi
import com.fh.theposition.util.SpannableStringBuilder.buildSpannableString
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object DigitalTimeConverter {

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTime(zonedDateTime: ZonedDateTime): SpannableString {
        return buildSpannableString(zonedDateTime.format(DateTimeFormatter.ofPattern("hh:mm a")))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTimeWithSeconds(zonedDateTime: ZonedDateTime): SpannableString {
        return buildSpannableString(zonedDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")))
    }
}