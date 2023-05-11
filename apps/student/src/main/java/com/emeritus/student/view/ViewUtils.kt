/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.emeritus.student.view

import android.net.Uri
import android.text.SpannableString
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import com.instructure.canvasapi2.utils.ApiPrefs.domain
import com.emeritus.student.activity.InternalWebViewActivity
import com.emeritus.student.activity.InterwebsToApplication.Companion.createIntent
import com.emeritus.student.router.RouteMatcher

object ViewUtils {
    /**
     * Parse the links to make them clickable and override what they do (so we don't just open a different app)
     */
    fun linkifyTextView(tv: TextView) {
        val current = tv.text as SpannableString
        val spans = current.getSpans(0, current.length, URLSpan::class.java)
        for (span in spans) {
            val start = current.getSpanStart(span)
            val end = current.getSpanEnd(span)
            current.removeSpan(span)
            current.setSpan(DefensiveURLSpan(span.url), start, end, 0)
        }
    }

    private class DefensiveURLSpan(private val url: String) : URLSpan(url) {
        override fun onClick(widget: View) {
            if (RouteMatcher.getInternalRoute(url, domain) != null) {
                // Normally we would do the normal routing, but we need an activity for that, which we don't have. So we'll use the more generic routing for the app
                widget.context.startActivity(createIntent(widget.context, Uri.parse(url)))
            } else {
                val intent = InternalWebViewActivity.createIntent(widget.context, url, "", false)
                widget.context.startActivity(intent)
            }
        }

    }
}
