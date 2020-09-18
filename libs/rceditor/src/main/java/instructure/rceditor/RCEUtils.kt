/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package instructure.rceditor

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.widget.ImageButton
import android.widget.ImageView
import androidx.annotation.ColorInt
import java.util.regex.Matcher
import java.util.regex.Pattern

internal object RCEUtils {
    //<span style="color: rgb(139, 150, 158);">Gray</span>
    private const val RBG_REGEX = "rgb\\((\\d+),\\s*(\\d+),\\s*(\\d+)\\);"

    //<span style=";"></span>
    private const val SPAN_REGEX = "<span\\s*style=\"\\s*;\">\\s*</span>"

    //<span style="color: #1482c8;"></span>
    private const val HEX_COMMA_REGEX = "#([A-Fa-f0-9]{3,8});"

    fun colorIt(color: Int, view: ImageView) {
        val drawable = view.drawable ?: return
        drawable.mutate().colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        view.setImageDrawable(drawable)
    }

    fun colorIt(color: Int, view: ImageButton) {
        val drawable = view.drawable ?: return
        drawable.mutate().colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        view.setImageDrawable(drawable)
    }

    private fun workaroundRbg2Hex(html: String): String? {
        return try {
            val pattern = Pattern.compile(RBG_REGEX)
            val action = pattern.matcher(html)
            val sb = StringBuffer(html.length)
            while (action.find()) {
                val values = action.group().replace("rgb(", "") // removes prefix
                    .replace(");", "") // removes suffix
                    .replace("\\s".toRegex(), "") // Clean up whitespace
                    .split(",".toRegex()).toList()
                val hex = String.format(
                    "#%02x%02x%02x",
                    Integer.valueOf(values[0]),
                    Integer.valueOf(values[1]),
                    Integer.valueOf(values[2])
                )
                action.appendReplacement(sb, Matcher.quoteReplacement(hex))
            }
            action.appendTail(sb)
            sb.toString()
        } catch (e: Exception) {
            null
        }
    }

    private fun workaroundInvalidSpan(html: String): String? {
        return try {
            val pattern = Pattern.compile(SPAN_REGEX)
            val action = pattern.matcher(html)
            val sb = StringBuffer(html.length)
            while (action.find()) {
                action.appendReplacement(sb, Matcher.quoteReplacement(""))
            }
            action.appendTail(sb)
            sb.toString()
        } catch (e: Exception) {
            null
        }
    }

    private fun workaroundInvalidHex(html: String): String? {
        return try {
            val pattern = Pattern.compile(HEX_COMMA_REGEX)
            val action = pattern.matcher(html)
            val sb = StringBuffer(html.length)
            while (action.find()) {
                val cleanValue = action.group().replace(";", "")
                action.appendReplacement(sb, Matcher.quoteReplacement(cleanValue))
            }
            action.appendTail(sb)
            sb.toString()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * When we do a PUT on the HTML there is often small things that'll cause the HTML to be invalid by the server and
     * replaced by "&nbsp;". In order to avoid content getting nuked we use these workarounds. It is possible that
     * the actual error is due to improper encoding on our end.
     * @param html A valid HTML string
     * @return A String or null value that has been sanitized.
     */
    fun sanitizeHTML(html: String?): String? {
        html ?: return null
        var validated: String = workaroundRbg2Hex(html) ?: return null
        validated = workaroundInvalidSpan(validated) ?: return null
        validated = workaroundInvalidHex(validated) ?: return null
        return validated
    }

    fun makeEditTextColorStateList(@ColorInt defaultColor: Int, @ColorInt tintColor: Int): ColorStateList {
        val states = arrayOf(
            intArrayOf(-android.R.attr.state_enabled),
            intArrayOf(android.R.attr.state_focused, -android.R.attr.state_pressed),
            intArrayOf(android.R.attr.state_focused, android.R.attr.state_pressed),
            intArrayOf(-android.R.attr.state_focused, android.R.attr.state_pressed),
            intArrayOf(android.R.attr.state_checked),
            intArrayOf()
        )
        val colors = intArrayOf(defaultColor, tintColor, tintColor, tintColor, tintColor, defaultColor)
        return ColorStateList(states, colors)
    }

    fun increaseAlpha(@ColorInt color: Int): Int {
        val a = 0x32
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.argb(a, r, g, b)
    }
}
