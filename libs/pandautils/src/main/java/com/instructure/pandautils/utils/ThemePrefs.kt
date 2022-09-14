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

package com.instructure.pandautils.utils

import android.graphics.Color
import androidx.core.graphics.drawable.DrawableCompat
import android.view.View
import android.view.ViewTreeObserver
import android.widget.EditText
import com.instructure.canvasapi2.models.CanvasTheme
import com.instructure.canvasapi2.utils.*
import com.instructure.pandautils.R

object ThemePrefs : PrefManager("CanvasTheme") {

    const val DARK_MULTIPLIER = 0.85f
    const val ALPHA_VALUE = 0x32

    var brandColor by ColorPref(R.color.textInfo)

    // Used for Toolbar background color
    var primaryColor by ColorPref(R.color.licorice)

    val darkPrimaryColor: Int
        get() = darker(primaryColor, DARK_MULTIPLIER)

    // Used for text color in Toolbars
    var primaryTextColor by ColorPref(R.color.white)

    var accentColor by ColorPref(R.color.textInfo)

    var buttonColor by ColorPref(R.color.backgroundInfo)

    var buttonTextColor by ColorPref(R.color.white)

    var logoUrl by StringPref()

    var isThemeApplied by BooleanPref()

    var appTheme by IntPref(defaultValue = 0)

    var themeSelectionShown by BooleanPref()

    override fun keepBaseProps() = listOf(::appTheme, ::themeSelectionShown)

    override fun onClearPrefs() {
    }

    /**
     * Returns darker version of specified `color`.
     * StatusBar color example would be 0.85F
     */
    @JvmOverloads
    fun darker(color: Int, factor: Float = DARK_MULTIPLIER): Int {
        val a = Color.alpha(color)
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.argb(
                a,
                Math.max((r * factor).toInt(), 0),
                Math.max((g * factor).toInt(), 0),
                Math.max((b * factor).toInt(), 0))
    }

    /**
     * Returns darker version of specified `color`.
     * StatusBar color example would be 0.85F
     */
    @JvmOverloads
    fun increaseAlpha(color: Int, factor: Int = ALPHA_VALUE): Int {
        val a = factor
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.argb(a, r, g, b)

    }

    fun themeViewBackground(view: View, color: Int) {
        val viewTreeObserver = view.viewTreeObserver
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val wrappedDrawable = DrawableCompat.wrap(view.background)
                if (wrappedDrawable != null) {
                    DrawableCompat.setTint(wrappedDrawable.mutate(), color)
                    view.background = wrappedDrawable
                }
            }
        })
    }

    fun themeEditTextBackground(editText: EditText, color: Int) {
        editText.setTextColor(color)
        themeViewBackground(editText, color)
    }

    fun applyCanvasTheme(theme: CanvasTheme) {
        brandColor = parseColor(theme.brand, brandColor)
        primaryColor = parseColor(theme.primary, primaryColor)
        primaryTextColor = parseColor(theme.primaryText, primaryTextColor)
        accentColor = parseColor(theme.accent, accentColor)
        buttonColor = parseColor(theme.button, buttonColor)
        buttonTextColor = parseColor(theme.buttonText, buttonTextColor)
        logoUrl = theme.logoUrl
        isThemeApplied = true
    }

    private fun parseColor(hexColor: String, defaultColor: Int): Int {
        try {
            val trimmedColorCode = getTrimmedColorCode(hexColor)
            return ColorUtils.parseColor(trimmedColorCode, defaultColor = defaultColor)
        } catch (e: IllegalArgumentException) {
            return defaultColor
        }
    }

    // There might be cases where the color codes from the response contain whitespaces.
    private fun getTrimmedColorCode(colorCode: String): String {
        return if (colorCode.contains("#")) {
            "#${colorCode.trimMargin("#")}"
        } else {
            colorCode
        }
    }

}
