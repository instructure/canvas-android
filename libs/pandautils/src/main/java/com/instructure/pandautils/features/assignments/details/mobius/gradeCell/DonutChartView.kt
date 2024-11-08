/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.pandautils.features.assignments.details.mobius.gradeCell

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.DP

class DonutChartView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    /** [RectF] used to draw the background and foreground arcs. Its coordinates are updated in [onSizeChanged]. */
    private var arcRect = RectF()

    /** The [Paint] used to draw both arcs. Only thw color is changed between draw calls. */
    private val paint: Paint = Paint().apply {
        isDither = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
        isAntiAlias = true
        strokeWidth = context.DP(3)
    }

    /** The current "progress" of the foreground arc, between 0f and 1f where 1f represents a complete circle. */
    private var progress: Float = 0f

    /** The color of the foreground arc */
    private var color: Int = 0

    /** The color of the background arc */
    private var bgColor: Int = ContextCompat.getColor(
        context,
        R.color.textLight
    )

    /** The animator used to animated the foreground arc from its current progress value to a new progress value. */
    private var animator: ObjectAnimator? = null

    init {
        // Draw at 65% progress in edit mode
        if (isInEditMode) {
            progress = 0.65f
            color = ContextCompat.getColor(
                context,
                R.color.backgroundInfo
            )
        }
    }

    /** Sets the color of this graph */
    fun setColor(@ColorInt selectedColor: Int) {
        color = selectedColor
        invalidate()
    }

    /** Sets the track color of this graph */
    fun setTrackColor(@ColorInt selectedColor: Int) {
        bgColor = selectedColor
        invalidate()
    }

    /** Sets the percentage of this graph. If [animate] is true, the graph will animate to the new value. */
    fun setPercentage(percentage: Float, animate: Boolean) {
        val newProgress = percentage.coerceIn(0f, 1f)
        animator?.cancel()
        if (animate) {
            animator = ObjectAnimator.ofFloat(this, "progress", progress, newProgress).apply {
                duration =
                    ANIM_DURATION
                interpolator = AccelerateDecelerateInterpolator()
            }
            animator?.start()
        } else {
            progress = percentage
            invalidate()
        }
    }

    /** Sets the current progress. Used internally by [animator]. */
    @SuppressLint("AnimatorKeep")
    private fun setProgress(progress: Float) {
        this.progress = progress
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        // Draw background
        paint.color = bgColor
        canvas.drawCircle(arcRect.centerX(), arcRect.centerY(), arcRect.width() / 2, paint)

        // Draw percentage
        paint.color = color
        val sweep = 359.9999f * progress
        canvas.drawArc(arcRect, 270f, sweep, false, paint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // Find the center
        val centerX = w / 2f
        val centerY = w / 2f

        /* Set the radius to half of the smallest dimension, less half of the stroke width to avoid clipping the stroke,
         * an less 0.5f to avoid clipping anti-aliased pixels. */
        val radius = Math.min(centerX, centerY) - (paint.strokeWidth / 2) - 0.5f

        // Update the rect
        arcRect.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
    }

    companion object {
        private const val ANIM_DURATION = 500L
    }

}
