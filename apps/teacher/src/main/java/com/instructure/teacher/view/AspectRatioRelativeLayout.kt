/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */

package com.instructure.teacher.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.instructure.teacher.R


class AspectRatioRelativeLayout @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var aspectRatio: Float = 1f

    init {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.AspectRatioRelativeLayout)
            aspectRatio = a.getFloat(R.styleable.AspectRatioRelativeLayout_aspectRatio, aspectRatio)
            a.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val (width, widthMode) = widthMeasureSpec.deconstructSpec()
        val (height, heightMode) = heightMeasureSpec.deconstructSpec()

        when {
            widthMode == MeasureSpec.EXACTLY -> measureRatio(width, widthMode, height, heightMode, false)
            heightMode == MeasureSpec.EXACTLY -> measureRatio(height, heightMode, width, widthMode, true)
            widthMode == MeasureSpec.AT_MOST -> measureRatio(width, widthMode, height, heightMode, false)
            heightMode == MeasureSpec.AT_MOST -> measureRatio(height, heightMode, width, widthMode, true)
            else -> super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    @SuppressLint("WrongCall")
    private fun measureRatio(x: Int, xMode: Int, y: Int, yMode: Int, invert: Boolean) {
        val desiredRatio = if (invert) 1 / aspectRatio else aspectRatio
        val measureRatio = x.toFloat() / y

        var (newX: Int, newY: Int) = when (yMode) {
            MeasureSpec.EXACTLY,
            MeasureSpec.AT_MOST -> {
                if (measureRatio > desiredRatio) {
                    Pair((y * desiredRatio).toInt(), y)
                } else {
                    Pair(x, (x / desiredRatio).toInt())
                }
            }
            else -> Pair(x, (x / desiredRatio).toInt())
        }

        if (invert) {
            val tmp = newX
            newX = newY
            newY = tmp
        }

        super.onMeasure(MeasureSpec.makeMeasureSpec(newX, xMode), MeasureSpec.makeMeasureSpec(newY, xMode))
    }

    private fun Int.deconstructSpec(): Pair<Int, Int> = Pair(MeasureSpec.getSize(this), MeasureSpec.getMode(this))

}
