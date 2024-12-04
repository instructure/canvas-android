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
package com.instructure.pandautils.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import androidx.annotation.ColorInt
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.DP
import com.instructure.pandautils.utils.obtainFor
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CanvasLoadingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    /** Number of steps (frames) in each iteration of the animation  */
    private var stepCount = STEP_COUNT

    /** Distance from view center where circles will 'spawn'  */
    private var spawnPointRadius = 0f

    /** Maximum radius of individual circles - when located at the max ring radius. Actual
     * radius may be larger if the circle extends beyond max rung radius.  */
    private var maxCircleRadius = 0f

    /** Radius of the outermost ring. Should be equal to the smallest view dimension  */
    private var maxRingRadius = 0f

    /** View center, cached for convenience  */
    private val mCenter = PointF()

    /** Circular path used for clipping along the outermost ring  */
    private val clipPath = Path()

    /** Current step (frame) in the animation  */
    private var step = 0

    /** Current iteration  */
    private var iterationCount = 0

    /** Interpolator, to smooth out transition between animations  */
    private val interpolator: Interpolator = AccelerateDecelerateInterpolator()

    /** Paint used to color circles  */
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /** Default circle colors  */
    private var colors = intArrayOf(
        0xFFEB282D.toInt(),
        0xFFC3001D.toInt(),
        0xFF9E1940.toInt(),
        0xFFEB282D.toInt(),
        0xFFEC4013.toInt(),
        0xFFF57F00.toInt(),
        0xFFEE5010.toInt(),
        0xFFEE4831.toInt()
    )

    init {
        attrs?.obtainFor(this, R.styleable.CanvasLoadingView) { a, idx ->
            when (idx) {
                R.styleable.CanvasLoadingView_clv_override_color -> {
                    /* Obtain override color */
                    val overrideColor = a.getColor(idx, Color.GRAY)
                    setOverrideColor(overrideColor)
                }
                R.styleable.CanvasLoadingView_clv_speed_multiplier -> {
                    /* Obtain speed multiplier */
                    stepCount = (STEP_COUNT / a.getFloat(idx, 1f)).toInt()
                }
            }
        }
    }

    fun setOverrideColor(@ColorInt color: Int) {
        for (j in colors.indices) colors[j] = color
    }

    override fun onDraw(canvas: Canvas) {
        /* Set clipping mask */
        canvas.clipPath(clipPath)

        /* Determine raw percentage based on current step and interpolate */
        var percent = step.toFloat() / stepCount
        percent = interpolator.getInterpolation(percent)

        /* Determine raw offset based on current iteration count */
        var offset: Float = if (iterationCount / 2 % 2 == 0) 45f else -45f
        if (iterationCount % 2 == 1) {
            /* Draw rotation animation on odd iterations */
            offset *= (1 - percent)
            drawCircleRing(canvas, offset, FRACTION_INNER_RING_RADIUS)
            if (iterationCount >= 3) drawCircleRing(canvas, 0f, 1f)
        } else {
            /* Draw zoom animation on even iterations */
            val innerRingPercentage = percent * FRACTION_INNER_RING_RADIUS
            val outerRingPercentage = FRACTION_INNER_RING_RADIUS + percent * (1 - FRACTION_INNER_RING_RADIUS)
            val exitingRingPercentage = 1 + percent
            drawCircleRing(canvas, offset, innerRingPercentage)
            if (iterationCount >= 2 || isInEditMode) drawCircleRing(canvas, 0f, outerRingPercentage)
            if (iterationCount >= 4 || isInEditMode) drawCircleRing(canvas, 0f, exitingRingPercentage)
        }

        /* Increment counts, reset current step if needed */
        step++
        if (step > stepCount) {
            step = 0
            iterationCount++
        }
        invalidate()
    }

    /**
     * Draws a ring of 8 circles
     *
     * @param canvas A Canvas object into which the ring will be drawn
     * @param angleOffset Angle rotation offset
     * @param growthPercent (0..1+) How 'grown' the circle is. 0 is no growth with circles of zero
     * size. 1 is full growth with circles at the outer edge.
     */
    private fun drawCircleRing(canvas: Canvas, angleOffset: Float, growthPercent: Float) {
        val radius = growthPercent * maxCircleRadius
        val ringRadius = spawnPointRadius + growthPercent * (maxRingRadius - spawnPointRadius)
        for (i in 0..7) {
            drawCircle(
                canvas,
                colors[i],
                radius,
                Math.toRadians(i * 45f + angleOffset.toDouble()),
                ringRadius
            )
        }
    }

    /**
     * Draws an individual circle
     *
     * @param canvas Canvas object into which the circle will be drawn
     * @param color The circle's color
     * @param radius The radius of the circle
     * @param angleRadians Angle of the circle's center from the view's center, in radians
     * @param distanceFromCenter Distance of the circle's center from the view's center
     */
    private fun drawCircle(canvas: Canvas, color: Int, radius: Float, angleRadians: Double, distanceFromCenter: Float) {
        val x = (mCenter.x + distanceFromCenter * cos(angleRadians)).toFloat()
        val y = (mCenter.y + distanceFromCenter * sin(angleRadians)).toFloat()
        paint.color = color
        canvas.drawCircle(x, y, radius, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        /* Use DEFAULT_DIMEN_DP as width and/or height if not specified */
        var widthSpec = widthMeasureSpec
        var heightSpec = heightMeasureSpec
        if (MeasureSpec.getMode(widthSpec) != MeasureSpec.EXACTLY) {
            widthSpec = MeasureSpec.makeMeasureSpec(context.DP(DEFAULT_DIMEN_DP).toInt(), MeasureSpec.EXACTLY)
        }
        if (MeasureSpec.getMode(heightSpec) != MeasureSpec.EXACTLY) {
            heightSpec = MeasureSpec.makeMeasureSpec(context.DP(DEFAULT_DIMEN_DP).toInt(), MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthSpec, heightSpec)

        /* Set up measurements */
        mCenter[MeasureSpec.getSize(widthSpec) / 2f] = MeasureSpec.getSize(heightSpec) / 2f
        maxRingRadius = min(mCenter.x, mCenter.y)
        spawnPointRadius = maxRingRadius * FRACTION_SPAWN_POINT_RADIUS
        maxCircleRadius = maxRingRadius * FRACTION_MAX_CIRCLE_RADIUS
        clipPath.rewind()
        clipPath.addCircle(mCenter.x, mCenter.y, maxRingRadius, Path.Direction.CW)
    }

    companion object {
        /** Default width and/or height dimension  */
        private const val DEFAULT_DIMEN_DP = 48f

        /** Distance from view center where circles will 'spawn', as a percent of max ring radius  */
        private const val FRACTION_SPAWN_POINT_RADIUS = 0.31f

        /** Maximum circle radius, as a percent of max ring radius */
        private const val FRACTION_MAX_CIRCLE_RADIUS = 0.29f

        /** Radius if the inner ring, as a percent of max ring radius  */
        private const val FRACTION_INNER_RING_RADIUS = 1 / 3f

        /** Default step count  */
        private const val STEP_COUNT = 30
    }
}
