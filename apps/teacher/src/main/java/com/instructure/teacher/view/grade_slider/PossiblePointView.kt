/*
 * Copyright (C) 2021 - present  Instructure, Inc.
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
package com.instructure.teacher.view.grade_slider

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.instructure.pandautils.utils.SP
import com.instructure.pandautils.utils.toPx
import com.instructure.teacher.R
import com.instructure.teacher.utils.getColorCompat

class PossiblePointView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var anchorRect = Rect()

    private var label: String = ""

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = context.SP(16f)
        textAlign = Paint.Align.CENTER
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColorCompat(R.color.textLightest)
        strokeWidth = 4.toPx.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawLine(anchorRect.left.toFloat(), anchorRect.bottom.toFloat() - 16.toPx, anchorRect.left.toFloat(), anchorRect.bottom.toFloat() - 8.toPx, linePaint)
        canvas.drawText(label, anchorRect.left.toFloat(), anchorRect.bottom.toFloat() + 16.toPx, textPaint)
    }

    fun showPossiblePoint(anchorRect: Rect, label: String) {
        this.anchorRect = anchorRect
        this.label = label
        invalidate()
    }
}