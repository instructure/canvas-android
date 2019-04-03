/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.isRTL
import com.instructure.pandautils.utils.obtainFor
import com.instructure.teacher.R
import com.instructure.teacher.utils.getColorCompat

class LinearProgressBar @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var progress: Float = if (isInEditMode) 0.75f else 0f
        set(value) {
            field = value
            invalidate()
        }

    var barColor: Int = if (isInEditMode) 0xFF00ACEC.toInt() else ThemePrefs.brandColor
        set(value) {
            field = value
            paint.color = value
            invalidate()
        }

    var fillGravity: Int = Gravity.START
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = barColor
    }

    private val progressRect: Rect
        @SuppressLint("RtlHardcoded")
        get() = when {
        // Draw from Top
            fillGravity == Gravity.TOP -> Rect(0, 0, width, (height * progress).toInt())

        // Draw from bottom
            fillGravity == Gravity.BOTTOM -> Rect(0, height - (height * progress).toInt(), width, height)

        // Draw from right
            fillGravity == Gravity.RIGHT
                    || (fillGravity == Gravity.START && isRTL())
                    || (fillGravity == Gravity.END && !isRTL()) -> Rect(width - (width * progress).toInt(), 0, width, height)

        // (Default) draw from left
            else -> Rect(0, 0, (width * progress).toInt(), height)
        }


    init {
        setBackgroundColor(context.getColorCompat(R.color.progressBarBackground))
        attrs?.obtainFor(this, R.styleable.LinearProgressBar) { a, idx ->
            when (idx) {
                R.styleable.LinearProgressBar_lbp_color -> barColor = a.getColor(idx, barColor)
                R.styleable.LinearProgressBar_lbp_fillGravity -> fillGravity = a.getInt(idx, Gravity.START)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(progressRect, paint)
    }
}
