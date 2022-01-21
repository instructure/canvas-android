package com.instructure.teacher.features.speedgrader.commentlibrary

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan

object Highlighter {

    @JvmStatic
    fun createHighlightedText(data: HighlightedSuggestionViewData): Spannable {
        return SpannableString(data.suggestion).apply {
            setSpan(StyleSpan(Typeface.BOLD), data.highlightStart, data.highlightEnd, 0)
        }
    }
}