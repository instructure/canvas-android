/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.canvasapi2.utils

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.SpannedString
import android.text.style.StyleSpan
import androidx.annotation.StringRes
import androidx.core.text.TextUtilsCompat

object Pronouns {
    /**
     * Returns a [Spanned] that contains the provided [name] and [pronouns], where [pronouns] will be italicized and
     * wrapped in parentheses. If [pronouns] is not a valid string then the return value will only contain the [name].
     */
    fun span(name: String?, pronouns: String?) : Spanned {
        val span = SpannableStringBuilder(name.orEmpty())
        if (pronouns.isValid()) {
            span.append(" ($pronouns)", StyleSpan(Typeface.ITALIC), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return SpannedString(span)
    }

    /**
     * Returns a [Spanned] built from the provided String resource and [formatArgs], where the specified [pronouns] will
     * be italicized. Exactly one of the [formatArgs] should be the value returned from calling [span] using the same
     * [pronouns] specified here.
     *
     * For example, given the following:
     * ```
     * <string name="exampleString">%1$s and %2$s others liked this comment.</string>
     *
     * ...
     *
     * val user = User(name: "User 1", pronouns: "He/Him")
     * val userCount = 4
     *
     * ...
     *
     * Pronouns.resource(
     *     context,
     *     R.string.exampleString,
     *     user.pronouns,
     *     Pronouns.span(user.name, user.pronouns),
     *     userCount
     * )
     * ```
     * The output would be:
     * "User 1 *(He/Him)* and 4 others liked this comment."
     *
     * If [pronouns] is not a valid string then no additional styling will be applied.
     */
    fun resource(context: Context, @StringRes resId: Int, pronouns: String?, vararg formatArgs: Any) : Spanned {
        var text: CharSequence = context.getString(resId, *formatArgs)
        if (pronouns.isValid()) {
            val target = "($pronouns)"
            val index = text.indexOf(target)
            if (index != -1) {
                text = SpannableString(text).apply {
                    setSpan(StyleSpan(Typeface.ITALIC), index, index + target.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
        return SpannedString(text)
    }

    /**
     * Returns a String that contains the provided [name] and [pronouns], where [pronouns] will be wrapped in parentheses
     * and HTML italics tags. If [pronouns] is not a valid string then [name] will be returned unmodified.
     * Whenever possible, prefer calling [span] over this function in order to add visual emphasis to the
     * user's chosen pronouns.
     *
     * The [name] and [pronouns] will be HTML encoded to avoid potential XSS issues.
     */
    fun html(name: String?, pronouns: String?) : String {
        val encodedName = name?.let { TextUtilsCompat.htmlEncode(it) }.orEmpty()
        val encodedPronouns = pronouns?.let { TextUtilsCompat.htmlEncode(it) }?.validOrNull() ?: return encodedName
        return """$encodedName <i>($encodedPronouns)</i>"""
    }
}
