package com.fh.theposition.util

import android.os.Build
import android.text.Html
import android.text.Spanned
import java.util.*

object HtmlFormatter {

    fun fromHtml(string: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(formatString(string),Html.FROM_HTML_MODE_LEGACY)
        }else{
            Html.fromHtml(formatString(string))
        }
    }

    private fun formatString(string: String): String {
        return String.format(Locale.ENGLISH,"%s",string)
    }
}