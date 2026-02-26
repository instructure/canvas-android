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

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.Logger

object ColorUtils {
    fun tintIt(color: Int, drawable: Drawable): Drawable {
        return DrawableCompat.wrap(drawable).also { DrawableCompat.setTint(it, color) }
    }

    fun colorIt(color: Int, drawable: Drawable): Drawable {
        return drawable.mutate().apply { colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP) }
    }

    fun colorIt(color: Int, imageView: ImageView) {
        val drawable = imageView.drawable ?: return
        drawable.mutate().colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        imageView.setImageDrawable(drawable)
    }

    fun colorIt(color: Int, map: Bitmap): Bitmap {
        val mutableBitmap = map.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply { colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP) }
        canvas.drawBitmap(mutableBitmap, 0f, 0f, paint)
        return mutableBitmap
    }

    /**
     * Do not use this directly for parsing course colors. Use [CanvasContext.color] or [CanvasContext.color].
     */
    fun parseColor(colorCode: String?, @ColorInt defaultColor: Int? = null): Int {
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
                Color.parseColor(ColorApiHelper.K5_DEFAULT_COLOR)
            }
        }
    }

    fun correctContrastForButtonBackground(buttonColor: Int, backgroundColor: Int, insideTextColor: Int): Int {
        try {
            var backgroundContrast = ColorUtils.calculateContrast(buttonColor, backgroundColor)
            val textContrast = ColorUtils.calculateContrast(insideTextColor, buttonColor)

            if (backgroundContrast >= MIN_CONTRAST_FOR_BUTTONS && textContrast >= MIN_CONTRAST_FOR_TEXT) return buttonColor

            var newColor = buttonColor

            if (textContrast < MIN_CONTRAST_FOR_TEXT) {
                newColor = correctContrastForText(buttonColor, insideTextColor)
            }

            backgroundContrast = ColorUtils.calculateContrast(newColor, backgroundColor)

            if (backgroundContrast < MIN_CONTRAST_FOR_BUTTONS) {
                // Correct contrast for background, but don't go under 4.5:1 for text contrast
                val backgroundLuminance = ColorUtils.calculateLuminance(backgroundColor)
                val delta = if (backgroundLuminance > 0.5) -0.01f else 0.01f

                var hslArray = FloatArray(3)
                ColorUtils.colorToHSL(buttonColor, hslArray)

                var saturation = hslArray[1]
                var lightness = hslArray[2]

                // The main logic is the same as for the text, but we need an extra exit condition if the contrast with the text would go too low.
                while (ColorUtils.calculateContrast(newColor, backgroundColor) < MIN_CONTRAST_FOR_BUTTONS && saturation >= 0) {
                    if (lightness < 1 || lightness > 0) {
                        lightness += delta
                    } else if (delta == 0.01f) {
                        saturation -= 0.01f
                    }
                    val tempNewColor = ColorUtils.HSLToColor(floatArrayOf(hslArray[0], saturation, lightness))
                    if (ColorUtils.calculateContrast(insideTextColor, tempNewColor) > MIN_CONTRAST_FOR_TEXT) {
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
            val contrast = ColorUtils.calculateContrast(color, colorAgainst)

            if (contrast >= MIN_CONTRAST_FOR_TEXT) return color

            var newColor = color

            var hslArray = FloatArray(3)
            ColorUtils.colorToHSL(color, hslArray)

            // If the background is light we need to decrease the lightness and increase if the background is dark
            val colorAgainstLuminance = ColorUtils.calculateLuminance(colorAgainst)
            val delta = if (colorAgainstLuminance > 0.5) -0.01f else 0.01f

            var saturation = hslArray[1]
            var lightness = hslArray[2]
            while (ColorUtils.calculateContrast(newColor, colorAgainst) < MIN_CONTRAST_FOR_TEXT && saturation >= 0) {
                if (lightness < 1 || lightness > 0) {
                    lightness += delta
                } else if (delta == 0.01f) {
                    // if the color is not light enough and we are going for lighter color we desaturate it.
                    // Adding saturation for darkening is not necessary, because if lightness is 0, color is already black.
                    saturation -= 0.01f
                }
                newColor = ColorUtils.HSLToColor(floatArrayOf(hslArray[0], saturation, lightness))
            }
            return newColor
        } catch (e: Exception) {
            Logger.e("Failed to fix contrast for #${Integer.toHexString(color)}: ${e.message}")
            return color
        }
    }

    fun Int.toApiHexString(): String {
        var hexColor = Integer.toHexString(this)
        // Remove alpha if present
        if (hexColor.length > 6) {
            hexColor = hexColor.substring(hexColor.length - 6)
        }
        // Remove # if present
        if (hexColor.startsWith("#")) {
            hexColor = hexColor.replace("#", "")
        }
        return hexColor
    }
}
