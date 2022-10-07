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

import android.graphics.*
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat

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

    @JvmStatic
    @JvmOverloads
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
}
