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
import android.view.View
import android.view.ViewTreeObserver
import android.widget.EditText
import androidx.core.graphics.drawable.DrawableCompat
import com.instructure.canvasapi2.models.CanvasTheme
import com.instructure.canvasapi2.utils.BooleanPref
import com.instructure.canvasapi2.utils.ColorPref
import com.instructure.canvasapi2.utils.IntPref
import com.instructure.canvasapi2.utils.PrefManager
import com.instructure.canvasapi2.utils.StringPref
import com.instructure.pandautils.R
import androidx.core.graphics.ColorUtils as AndroidColorUtils

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
    var buttonTextColor by ColorPref(R.color.white)

    // Used for text buttons (for example dialog buttons) and small image buttons.
    var textButtonColor by ColorPref(R.color.textInfo)

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

    fun applyCanvasTheme(theme: CanvasTheme, context: Context) {
        val tempBrandColor = parseColor(theme.brand, brandColor) // ic-brand-primary - Primary Brand Color
        brandColor = correctContrastForText(tempBrandColor, context.getColor(R.color.backgroundLightestElevated))

        primaryColor = parseColor(theme.primary, primaryColor)  // ic-brand-global-nav-bgd - Nav Background
        primaryTextColor = parseColor(theme.primaryText, primaryTextColor) // ic-brand-global-nav-menu-item__text-color - Nav Text

        val tempButtonColor = parseColor(theme.button, buttonColor) // ic-brand-button--primary-bgd - Primary Button

        buttonColor = correctContrastForButtonBackground(tempButtonColor, context.getColor(R.color.backgroundLightest), context.getColor(R.color.white))
        buttonTextColor = context.getColor(R.color.white)
        textButtonColor = correctContrastForText(tempButtonColor, context.getColor(R.color.backgroundLightestElevated))

        logoUrl = theme.logoUrl
        isThemeApplied = true
    }

    fun correctContrastForButtonBackground(buttonColor: Int, backgroundColor: Int, insideTextColor: Int): Int {
        var backgroundContrast = AndroidColorUtils.calculateContrast(buttonColor, backgroundColor)
        val textContrast = AndroidColorUtils.calculateContrast(insideTextColor, buttonColor)

        if (backgroundContrast >= MIN_CONTRAST_FOR_BUTTONS && textContrast >= MIN_CONTRAST_FOR_TEXT) return buttonColor

        var newColor = buttonColor

        if (textContrast < MIN_CONTRAST_FOR_TEXT) {
            newColor = correctContrastForText(buttonColor, insideTextColor)
        }

        backgroundContrast = AndroidColorUtils.calculateContrast(newColor, backgroundColor)

        if (backgroundContrast < MIN_CONTRAST_FOR_BUTTONS) {
            // Correct contrast for background, but don't go under 4.5:1 for text contrast
            val backgroundLuminance = AndroidColorUtils.calculateLuminance(backgroundColor)
            val delta = if (backgroundLuminance > 0.5) -0.01f else 0.01f

            var hslArray = FloatArray(3)
            AndroidColorUtils.colorToHSL(buttonColor, hslArray)

            var saturation = hslArray[1]
            var lightness = hslArray[2]

            // The main logic is the same as for the text, but we need an extra exit condition if the contrast with the text would go too low.
            while (AndroidColorUtils.calculateContrast(newColor, backgroundColor) < MIN_CONTRAST_FOR_BUTTONS && saturation >= 0) {
                if (lightness < 1 || lightness > 0) {
                    lightness += delta
                } else if (delta == 0.01f) {
                    saturation -= 0.01f
                }
                val tempNewColor = AndroidColorUtils.HSLToColor(floatArrayOf(hslArray[0], saturation, lightness))
                if (AndroidColorUtils.calculateContrast(insideTextColor, tempNewColor) > MIN_CONTRAST_FOR_TEXT) {
                    newColor = tempNewColor
                } else {
                    break
                }
            }
        }

        return newColor
    }

    fun correctContrastForText(color: Int, colorAgainst: Int): Int {
        val contrast = AndroidColorUtils.calculateContrast(color, colorAgainst)

        if (contrast >= MIN_CONTRAST_FOR_TEXT) return color

        var newColor = color

        var hslArray = FloatArray(3)
        AndroidColorUtils.colorToHSL(color, hslArray)

        // If the background is light we need to decrease the lightness and increase if the background is dark
        val colorAgainstLuminance = AndroidColorUtils.calculateLuminance(colorAgainst)
        val delta = if (colorAgainstLuminance > 0.5) -0.01f else 0.01f

        var saturation = hslArray[1]
        var lightness = hslArray[2]
        while (AndroidColorUtils.calculateContrast(newColor, colorAgainst) < MIN_CONTRAST_FOR_TEXT && saturation >= 0) {
            if (lightness < 1 || lightness > 0) {
                lightness += delta
            } else if (delta == 0.01f) {
                // if the color is not light enough and we are going for lighter color we desaturate it.
                // Adding saturation for darkening is not necessary, because if lightness is 0, color is already black.
                saturation -= 0.01f
            }
            newColor = AndroidColorUtils.HSLToColor(floatArrayOf(hslArray[0], saturation, lightness))
        }
        return newColor
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
