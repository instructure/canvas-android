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

package com.instructure.pandautils.views

import android.content.Context
import android.graphics.*
import androidx.annotation.ColorInt
import android.util.AttributeSet
import android.view.View
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.DP
import com.instructure.pandautils.utils.specMode
import com.instructure.pandautils.utils.specSize

class ProgressiveCanvasLoadingView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /** Distance from view center where circles will 'spawn'  */
    private var mSpawnPointRadius = 0f

    /** Maximum radius of individual circles - when located at the max ring radius. Actual
     * radius may be larger if the circle extends beyond max rung radius.  */
    private var mMaxCircleRadius = 0f

    /** Radius of the outermost ring. Should be equal to the smallest view dimension  */
    private var mMaxRingRadius = 0f

    /** View center, cached for convenience  */
    private val mCenter = PointF()

    /** Circular path used for clipping along the outermost ring  */
    private val mClipPath = Path()

    /** Whether or not to draw this view in an 'indeterminate' state. Calling [setProgress] will disable this state */
    var isIndeterminate = false
        set(value) {
            field = value
            invalidate()
        }

    /** Current progress - between 0 and 1, or between 0 and 2 for indeterminate state */
    private var progress = 0f

    /** Paint used to color circles  */
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /** Sets the current progress. Value will be coerced between 0 and 1. Calling this will set [isIndeterminate] to false */
    fun setProgress(newProgress: Float) {
        progress = newProgress.coerceIn(0f, 1f)
        isIndeterminate = false
    }

    init {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.ProgressiveCanvasLoadingView)
            setColor(a.getColor(R.styleable.ProgressiveCanvasLoadingView_pclv_override_color, Color.GRAY))
            progress = a.getFloat(R.styleable.ProgressiveCanvasLoadingView_pclv_progress, 0.75f)
            isIndeterminate = a.getBoolean(R.styleable.ProgressiveCanvasLoadingView_pclv_indeterminate, false)
            a.recycle()
        }
    }

    fun setColor(@ColorInt color: Int) {
        mPaint.color = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {

        /* Set clipping mask */
        canvas.clipPath(mClipPath)

        if (isIndeterminate) {
            /* Draw the circles */
            for (i in 0 until CIRCLE_COUNT) {
                var angleOffset = 360f / CIRCLE_COUNT * Math.sin(Math.PI * progress).toFloat()
                if (i % 2 == 0) angleOffset = -angleOffset
                drawCircle(canvas, i, -90f + angleOffset, 1f)
            }
            progress += 0.01f
            invalidate()

        } else {
            /* Determine index of 'progressing' circle */
            val circleIdx = (16 * progress).toInt()
            val roughProgress = circleIdx * CIRCLE_PROGRESSION_PERCENTAGE
            val circleProgress = (progress - roughProgress) / CIRCLE_PROGRESSION_PERCENTAGE

            /* Draw the circles */
            for (i in 0..circleIdx) {
                drawCircle(canvas, i, -90f, if (i < circleIdx) 1f else circleProgress)
            }
        }
    }

    private fun drawCircle(canvas: Canvas, idx: Int, angleOffset: Float, rawProgress: Float) {
        val progress = if (idx % 2 == 0) rawProgress else (rawProgress * FRACTION_INNER_RING_RADIUS)
        val radius = progress * mMaxCircleRadius
        val ringRadius = mSpawnPointRadius + progress * (mMaxRingRadius - mSpawnPointRadius)
        val angle = 360f / (CIRCLE_COUNT / 2) * (idx / 2) + angleOffset
        val angleRadians = Math.toRadians((angle).toDouble())
        val x = (mCenter.x + ringRadius * Math.cos(angleRadians)).toFloat()
        val y = (mCenter.y + ringRadius * Math.sin(angleRadians)).toFloat()
        canvas.drawCircle(x, y, radius, mPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var newWidthSpec = widthMeasureSpec
        var newHeightSpec = heightMeasureSpec

        /* Use DEFAULT_DIMEN_DP as width and/or height if not specified */
        if (newWidthSpec.specMode != View.MeasureSpec.EXACTLY) {
            newWidthSpec = MeasureSpec.makeMeasureSpec(context.DP(DEFAULT_DIMEN_DP).toInt(), View.MeasureSpec.EXACTLY)
        }
        if (newHeightSpec.specMode != View.MeasureSpec.EXACTLY) {
            newHeightSpec = MeasureSpec.makeMeasureSpec(context.DP(DEFAULT_DIMEN_DP).toInt(), View.MeasureSpec.EXACTLY)
        }
        super.onMeasure(newWidthSpec, newHeightSpec)

        /* Set up measurements */
        mCenter.set(newWidthSpec.specSize / 2f, newHeightSpec.specSize / 2f)
        mMaxRingRadius = Math.min(mCenter.x, mCenter.y)
        mSpawnPointRadius = mMaxRingRadius * FRACTION_SPAWN_POINT_RADIUS
        mMaxCircleRadius = mMaxRingRadius * FRACTION_MAX_CIRCLE_RADIUS
        mClipPath.rewind()
        mClipPath.addCircle(mCenter.x, mCenter.y, mMaxRingRadius, Path.Direction.CW)

    }

    companion object {
        /** Default width and/or height dimension  */
        private val DEFAULT_DIMEN_DP = 48f

        /** Distance from view center where circles will 'spawn', as a percent of max ring radius  */
        private val FRACTION_SPAWN_POINT_RADIUS = 0.31f

        /** Maximum circle radius, as a percent of max ring radius */
        private val FRACTION_MAX_CIRCLE_RADIUS = 0.29f

        /** Radius if the inner ring, as a percent of max ring radius  */
        private val FRACTION_INNER_RING_RADIUS = 1 / 3f

        /** Number of circles in the design */
        private val CIRCLE_COUNT = 16

        /** Progression percent allotted to each circle animation */
        private val CIRCLE_PROGRESSION_PERCENTAGE = 1f / CIRCLE_COUNT

    }
}
