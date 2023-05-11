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
package com.emeritus.student.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat

object BitmapUtilities {
    @Throws(IllegalArgumentException::class)
    fun getBitmapFromDrawable(context: Context?, @DrawableRes drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context!!, drawableId)
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else if (drawable is VectorDrawableCompat || drawable is VectorDrawable) {
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } else {
            throw IllegalArgumentException("unsupported drawable type")
        }
    }

    fun addPaddingToBitmap(bitmap: Bitmap, padding: Int): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width + padding, bitmap.height + padding, Bitmap.Config.ARGB_8888)
        val can = Canvas(output)
        can.drawBitmap(bitmap, padding / 2.toFloat(), padding / 2.toFloat(), null)
        return output
    }
}
