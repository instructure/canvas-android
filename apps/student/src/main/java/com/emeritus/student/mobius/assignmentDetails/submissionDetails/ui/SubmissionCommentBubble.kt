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
package com.emeritus.student.mobius.assignmentDetails.submissionDetails.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.FrameLayout
import com.instructure.pandautils.utils.DP
import com.instructure.pandautils.utils.isRTL
import com.instructure.pandautils.utils.obtainFor
import com.emeritus.student.R

class SubmissionCommentBubble @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    /** The radius of the three normal corners */
    var cornerRadius: Float = context.DP(12f)
        set(value) {
            field = value
            invalidate()
        }

    /** Paint used to draw the bubble background */
    private var bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.LTGRAY }

    /** Path that defines the area of the bubble background */
    private val bubblePath = Path()

    /** Radius of the tail's curve*/
    private var tailRadius = context.DP(20)

    /** Distance between this view and the avatar view around which the tail curls */
    private var targetMargin = context.DP(4)

    init {
        attrs?.obtainFor(this, R.styleable.SubmissionCommentBubble) { a, idx ->
            when (idx) {
                R.styleable.SubmissionCommentBubble_ctv_color -> bubblePaint.color = a.getColor(idx, Color.LTGRAY)
                R.styleable.SubmissionCommentBubble_ctv_tailRadius -> tailRadius = a.getDimension(idx, tailRadius)
                R.styleable.SubmissionCommentBubble_ctv_targetMargin -> targetMargin =
                    a.getDimension(idx, targetMargin)
            }
        }
        setPadding(
            paddingLeft,
            paddingTop + getTailHeight().toInt(),
            paddingRight,
            paddingBottom
        )

    }

    private fun getTailHeight(): Float {
        val outerRadius = tailRadius + targetMargin
        val h = outerRadius - Math.sqrt(Math.pow(outerRadius.toDouble(), 2.0) - Math.pow(tailRadius.toDouble(), 2.0))
        return h.toFloat()
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, left, top, right, bottom)

        val outerRadius = tailRadius + targetMargin

        val tailHeight = getTailHeight()

        val tailArcRect = RectF().apply {
            right = outerRadius * 2
            bottom = outerRadius * 2
            offsetTo(tailRadius - outerRadius, tailHeight + -outerRadius * 2)
        }

        val cornerDiameter = cornerRadius * 2

        val cornerRect = RectF().apply {
            right = cornerDiameter
            bottom = cornerDiameter
        }

        bubblePath.apply {
            rewind()
            moveTo(0f, 0f)
            val startAngle = 180 - Math.toDegrees(Math.acos(tailRadius / outerRadius.toDouble())).toFloat()
            arcTo(tailArcRect, startAngle, -startAngle + 90, false)
            lineTo(width - cornerRadius, tailHeight)
            cornerRect.offsetTo(width - cornerDiameter, tailHeight)
            arcTo(cornerRect, -90f, 90f, false)
            lineTo(width.toFloat(), height - cornerRadius)
            cornerRect.offsetTo(width - cornerDiameter, height - cornerDiameter)
            arcTo(cornerRect, 0f, 90f, false)
            lineTo(cornerRadius, height.toFloat())
            cornerRect.offsetTo(0f, height - cornerDiameter)
            arcTo(cornerRect, 90f, 90f, false)
            close()
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        // reverse for RTL and 'outgoing' direction
        if (isRTL()) {
            canvas.save()
            canvas.scale(-1f, 1f, width / 2f, 0f)
            canvas.drawPath(bubblePath, bubblePaint)
        }
        canvas.drawPath(bubblePath, bubblePaint)
        if (isRTL()) canvas.restore()
        super.dispatchDraw(canvas)
    }

}
