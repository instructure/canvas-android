package com.instructure.pandautils.utils

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan

object Highlighter {

    @JvmStatic
    fun createHighlightedText(data: HighlightedTextData): Spannable {
        return SpannableString(data.text).apply {
            setSpan(StyleSpan(Typeface.BOLD), data.highlightStart, data.highlightEnd, 0)
        }
    }
}

data class HighlightedTextData(val text: String, val highlightStart: Int, val highlightEnd: Int)