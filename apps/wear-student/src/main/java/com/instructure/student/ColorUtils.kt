/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 */

package com.instructure.student

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.instructure.candroid.R
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.Logger

const val MIN_CONTRAST_FOR_BUTTONS = 3.0
const val MIN_CONTRAST_FOR_TEXT = 4.5
const val K5_DEFAULT_COLOR = "#394B58"

object ColorUtils {

    private val defaultColor = ContextCompat.getColor(ContextKeeper.appContext, R.color.textDarkest)
    fun generateColor(canvasContext: CanvasContext, darkTheme: Boolean): ThemedColor {
        if (canvasContext.type == CanvasContext.Type.USER || canvasContext.name.isNullOrBlank()) {
            return ThemedColor(defaultColor, isDark = darkTheme) // defaultColor is already themed so we don't need 3 different colors
        }

        val colorRes = when (Math.abs((canvasContext.name?.hashCode() ?: "Null Name".hashCode()) % 13)) {
            0 -> R.color.colorCottonCandy
            1 -> R.color.colorBarbie
            2 -> R.color.colorBarneyPurple
            3 -> R.color.colorEggplant
            4 -> R.color.colorUltramarine
            5 -> R.color.colorOcean11
            6 -> R.color.colorCyan
            7 -> R.color.colorAquaMarine
            8 -> R.color.colorEmeraldGreen
            9 -> R.color.colorFreshCutLawn
            10 -> R.color.colorChartreuse
            11 -> R.color.colorSunFlower
            12 -> R.color.colorTangerine
            13 -> R.color.colorBloodOrange
            else -> R.color.colorSriracha
        }

        val color = ContextCompat.getColor(ContextKeeper.appContext, colorRes)
        val themedColor = createThemedColor(color, darkTheme)
        return themedColor
    }

    fun createThemedColor(@ColorInt color: Int, darkTheme: Boolean): ThemedColor {
        val light = correctContrastForText(color, ContextKeeper.appContext.getColor(R.color.white))
        val darkBackgroundColor = correctContrastForButtonBackground(color, ContextKeeper.appContext.getColor(R.color.backgroundDarkMode), ContextKeeper.appContext.getColor(R.color.white))
        val darkTextAndIconColor = correctContrastForText(color, ContextKeeper.appContext.getColor(R.color.elevatedDarkColor))

        return ThemedColor(light, darkBackgroundColor, darkTextAndIconColor, darkTheme)
    }

    fun correctContrastForButtonBackground(buttonColor: Int, backgroundColor: Int, insideTextColor: Int): Int {
        try {
            var backgroundContrast = androidx.core.graphics.ColorUtils.calculateContrast(buttonColor, backgroundColor)
            val textContrast = androidx.core.graphics.ColorUtils.calculateContrast(insideTextColor, buttonColor)

            if (backgroundContrast >= MIN_CONTRAST_FOR_BUTTONS && textContrast >= MIN_CONTRAST_FOR_TEXT) return buttonColor

            var newColor = buttonColor

            if (textContrast < MIN_CONTRAST_FOR_TEXT) {
                newColor = correctContrastForText(buttonColor, insideTextColor)
            }

            backgroundContrast = androidx.core.graphics.ColorUtils.calculateContrast(newColor, backgroundColor)

            if (backgroundContrast < MIN_CONTRAST_FOR_BUTTONS) {
                // Correct contrast for background, but don't go under 4.5:1 for text contrast
                val backgroundLuminance = androidx.core.graphics.ColorUtils.calculateLuminance(backgroundColor)
                val delta = if (backgroundLuminance > 0.5) -0.01f else 0.01f

                var hslArray = FloatArray(3)
                androidx.core.graphics.ColorUtils.colorToHSL(buttonColor, hslArray)

                var saturation = hslArray[1]
                var lightness = hslArray[2]

                // The main logic is the same as for the text, but we need an extra exit condition if the contrast with the text would go too low.
                while (androidx.core.graphics.ColorUtils.calculateContrast(newColor, backgroundColor) < MIN_CONTRAST_FOR_BUTTONS && saturation >= 0) {
                    if (lightness < 1 || lightness > 0) {
                        lightness += delta
                    } else if (delta == 0.01f) {
                        saturation -= 0.01f
                    }
                    val tempNewColor = androidx.core.graphics.ColorUtils.HSLToColor(floatArrayOf(hslArray[0], saturation, lightness))
                    if (androidx.core.graphics.ColorUtils.calculateContrast(insideTextColor, tempNewColor) > MIN_CONTRAST_FOR_TEXT) {
                        newColor = tempNewColor
                    } else {
                        break
                    }
                }
            }

            return newColor
        } catch (e: Exception) {
            Logger.e("Failed to fix contrast for #${Integer.toHexString(buttonColor)}: ${e.message}")
            return buttonColor
        }
    }

    fun correctContrastForText(color: Int, colorAgainst: Int): Int {
        try {
            val contrast = androidx.core.graphics.ColorUtils.calculateContrast(color, colorAgainst)

            if (contrast >= MIN_CONTRAST_FOR_TEXT) return color

            var newColor = color

            var hslArray = FloatArray(3)
            androidx.core.graphics.ColorUtils.colorToHSL(color, hslArray)

            // If the background is light we need to decrease the lightness and increase if the background is dark
            val colorAgainstLuminance = androidx.core.graphics.ColorUtils.calculateLuminance(colorAgainst)
            val delta = if (colorAgainstLuminance > 0.5) -0.01f else 0.01f

            var saturation = hslArray[1]
            var lightness = hslArray[2]
            while (androidx.core.graphics.ColorUtils.calculateContrast(newColor, colorAgainst) < MIN_CONTRAST_FOR_TEXT && saturation >= 0) {
                if (lightness < 1 || lightness > 0) {
                    lightness += delta
                } else if (delta == 0.01f) {
                    // if the color is not light enough and we are going for lighter color we desaturate it.
                    // Adding saturation for darkening is not necessary, because if lightness is 0, color is already black.
                    saturation -= 0.01f
                }
                newColor = androidx.core.graphics.ColorUtils.HSLToColor(floatArrayOf(hslArray[0], saturation, lightness))
            }
            return newColor
        } catch (e: Exception) {
            Logger.e("Failed to fix contrast for #${Integer.toHexString(color)}: ${e.message}")
            return color
        }
    }

    fun parseColor(hexColor: String): Int = try {
        val trimmedColorCode = getTrimmedColorCode(hexColor)
        parseColor(trimmedColorCode, defaultColor = defaultColor)
    } catch (e: IllegalArgumentException) {
        defaultColor
    }

    private fun parseColor(colorCode: String?, @ColorInt defaultColor: Int? = null): Int {
        return try {
            val fullColorCode = if (colorCode?.length == 4 && colorCode[0].toString() == "#") {
                "#${colorCode[1]}${colorCode[1]}${colorCode[2]}${colorCode[2]}${colorCode[3]}${colorCode[3]}"
            } else {
                colorCode
            }
            Color.parseColor(fullColorCode)
        } catch (e: Exception) {
            if (defaultColor != null) {
                defaultColor
            } else {
                Color.parseColor(K5_DEFAULT_COLOR)
            }
        }
    }

    private fun getTrimmedColorCode(colorCode: String): String {
        return if (colorCode.contains("#")) {
            "#${colorCode.trimMargin("#")}"
        } else {
            colorCode
        }
    }

}

data class ThemedColor(
    @ColorInt val light: Int,
    @ColorInt val darkBackgroundColor: Int = light,
    @ColorInt val darkTextAndIconColor: Int = light,
    val isDark: Boolean
) {
    @ColorInt
    fun textAndIconColor() = if (isDark) darkTextAndIconColor else light

    @ColorInt
    fun backgroundColor() = if (isDark) darkBackgroundColor else light
}