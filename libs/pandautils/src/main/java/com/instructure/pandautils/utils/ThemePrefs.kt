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

import android.content.Context
import android.graphics.Color
import com.instructure.canvasapi2.models.CanvasTheme
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.BooleanPref
import com.instructure.canvasapi2.utils.ColorPref
import com.instructure.canvasapi2.utils.IntPref
import com.instructure.canvasapi2.utils.PrefManager
import com.instructure.canvasapi2.utils.StringPref
import com.instructure.pandautils.R
import dagger.hilt.android.qualifiers.ActivityContext

const val MIN_CONTRAST_FOR_BUTTONS = 3.0
const val MIN_CONTRAST_FOR_TEXT = 4.5

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

    // Used for button background where we have a filled button and a text/icon inside.
    var buttonColor by ColorPref(R.color.backgroundInfo)

    // Button text color for filled button.
    var buttonTextColor by ColorPref(R.color.textLightest)

    // Used for text buttons (for example dialog buttons) and small image buttons.
    var textButtonColor by ColorPref(R.color.textInfo)

    var logoUrl by StringPref()

    var mobileLogoUrl by StringPref()

    var isThemeApplied by BooleanPref()

    var appTheme by IntPref(defaultValue = 2) // Default to system

    private var canvasTheme: CanvasTheme? = null

    override fun keepBaseProps() = listOf(::appTheme)

    override fun onClearPrefs() {
    }

    /**
     * Returns darker version of specified `color`.
     * StatusBar color example would be 0.85F
     */
    fun darker(color: Int, factor: Float = DARK_MULTIPLIER): Int {
        val a = Color.alpha(color)
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.argb(
            a,
            Math.max((r * factor).toInt(), 0),
            Math.max((g * factor).toInt(), 0),
            Math.max((b * factor).toInt(), 0)
        )
    }

    /**
     * Returns darker version of specified `color`.
     * StatusBar color example would be 0.85F
     */
    fun increaseAlpha(color: Int, factor: Int = ALPHA_VALUE): Int {
        val a = factor
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.argb(a, r, g, b)

    }

    // This should not be called with application context.
    fun reapplyCanvasTheme(@ActivityContext context: Context) {
        applyCanvasTheme(canvasTheme ?: return, context)
    }

    // This should not be called with application context.
    fun applyCanvasTheme(theme: CanvasTheme, @ActivityContext context: Context) {
        val tempBrandColor = parseColor(theme.brand, brandColor) // ic-brand-primary - Primary Brand Color
        brandColor = ColorUtils.correctContrastForText(tempBrandColor, context.getColor(R.color.backgroundLightestElevated))

        primaryColor = parseColor(theme.primary, primaryColor)  // ic-brand-global-nav-bgd - Nav Background
        primaryTextColor = parseColor(theme.primaryText, primaryTextColor) // ic-brand-global-nav-menu-item__text-color - Nav Text

        val tempButtonColor = parseColor(theme.button, buttonColor) // ic-brand-button--primary-bgd - Primary Button

        buttonColor = ColorUtils.correctContrastForButtonBackground(
            tempButtonColor,
            context.getColor(R.color.backgroundLightestElevated),
            context.getColor(R.color.textLightest)
        )
        buttonTextColor = context.getColor(R.color.textLightest)
        textButtonColor = ColorUtils.correctContrastForText(tempButtonColor, context.getColor(R.color.backgroundLightestElevated))

        logoUrl = theme.logoUrl
        mobileLogoUrl = when {
            theme.mobileLogoUrl.isNullOrEmpty() -> ""
            theme.mobileLogoUrl?.startsWith("https") == true -> theme.mobileLogoUrl.orEmpty()
            else -> "${ApiPrefs.fullDomain}${theme.mobileLogoUrl.orEmpty()}"
        }
        isThemeApplied = true
        canvasTheme = theme
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
