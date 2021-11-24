package com.fh.theposition.util

import android.graphics.Color
import android.text.SpannableString
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan

object SpannableStringBuilder {

    fun buildSpannableString(string: String): SpannableString {
        val spannableString = SpannableString(string)
        spannableString.setSpan(RelativeSizeSpan(0.5f),5,string.length,0)
        spannableString.setSpan(ForegroundColorSpan(Color.GRAY),5,string.length,0)
        return spannableString
    }




}