/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.student.features.documentscanning

import android.graphics.*

fun Bitmap.toGrayscale(): Bitmap {
    val width = this.width
    val height = this.height

    val grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(grayscaleBitmap)
    val paint = Paint()
    val colorMatrix = ColorMatrix()
    colorMatrix.setSaturation(0f)
    val colorMatrixFilter = ColorMatrixColorFilter(colorMatrix)
    paint.colorFilter = colorMatrixFilter
    canvas.drawBitmap(this, 0f, 0f, paint)
    return grayscaleBitmap
}

fun Bitmap.toMonochrome(): Bitmap {
    val width = this.width
    val height = this.height

    val monochromeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    val hsv = FloatArray(3)
    for (column in 0 until width) {
        for (row in 0 until height) {
            Color.colorToHSV(this.getPixel(column, row), hsv)
            if (hsv[2] > 0.5f) {
                monochromeBitmap.setPixel(column, row, Color.WHITE)
            } else {
                monochromeBitmap.setPixel(column, row, Color.BLACK)
            }
        }
    }

    return monochromeBitmap
}