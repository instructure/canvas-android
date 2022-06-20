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
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.instructure.teacher.R
import com.instructure.teacher.utils.getColorCompat


class TutorialSwipeView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val QUARTER_TRANSPARENT = 0x40FFFFFF
    private val HALF_TRANSPARENT = 0x88FFFFFF.toInt()
    private val TRANSPARENT = 0x00FFFFFF
    private val SHADOW_RADIUS = resources.getDimension(R.dimen.speedGraderTutorialShadowRadius)
    private val STROKE_WIDTH = resources.getDimension(R.dimen.speedGraderTutorialStrokeWidth)

    var mColor: Int = context.getColorCompat(R.color.borderSuccess)

    var showTrail = true

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        attrs?.let { context.obtainStyledAttributes(it, R.styleable.TutorialSwipeView) }?.apply {
            mColor = getColor(R.styleable.TutorialSwipeView_tsv_color, mColor)
            showTrail = getBoolean(R.styleable.TutorialSwipeView_tvs_showTrail, showTrail)
        }?.recycle()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val centerY = height / 2f
        val centerX = width / 2f
        val radius = centerY - SHADOW_RADIUS
        val gradientInset = SHADOW_RADIUS - STROKE_WIDTH / 2

        val circlePath = Path()
        circlePath.addCircle(centerX, centerY, radius, Path.Direction.CW)

        canvas.save()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            canvas.clipOutPath(circlePath)
        } else {
            @Suppress("DEPRECATION")
            canvas.clipPath(circlePath, Region.Op.DIFFERENCE)
        }

        with(Paint(Paint.ANTI_ALIAS_FLAG)) {
            color = mColor
            setShadowLayer(SHADOW_RADIUS, 0f, 0f, mColor and HALF_TRANSPARENT)
            canvas.drawPath(circlePath, this)
        }

        if (showTrail) with(Paint()) {
            shader = LinearGradient(centerX, 0f, width.toFloat(), 0f, mColor and QUARTER_TRANSPARENT, mColor and TRANSPARENT, Shader.TileMode.CLAMP)
            canvas.drawRect(centerX, gradientInset, width.toFloat(), height.toFloat() - gradientInset, this)
            canvas.drawPath(circlePath, this)
        }

        canvas.restore()

        with(Paint(Paint.ANTI_ALIAS_FLAG)) {
            color = mColor
            style = Paint.Style.STROKE
            strokeWidth = STROKE_WIDTH
            canvas.drawPath(circlePath, this)
        }
    }

}
