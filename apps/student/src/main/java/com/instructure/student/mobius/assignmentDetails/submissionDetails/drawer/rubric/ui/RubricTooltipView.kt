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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.instructure.pandautils.utils.DP
import com.instructure.student.R
import kotlinx.android.synthetic.main.view_rubric_tooltip.view.*

class RubricTooltipView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    /** The size of the triangular tail attaching the tooltip to the anchor point */
    private val tailSize = context.DP(5f)

    /** Rounded corner radius of the tooltip */
    private val cornerRadius = context.DP(5f)

    /** Path which holds the tooltip attachment point / triangle */
    private val tailPath: Path = Path().apply {
        moveTo(-tailSize + 0.5f, 0f)
        lineTo(0f, tailSize - 0.5f)
        lineTo(tailSize - 0.5f, 0f)
        close()
    }

    /** Whether the tail should draw at the top or bottom of this view */
    private var tailBottom = true

    /** The horizontal offset of the point of the tail */
    private var tailOffset = 0f

    /** Paint used to draw the tooltip bubble and tail */
    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.defaultTextDark)
    }

    init {
        View.inflate(context, R.layout.view_rubric_tooltip, this)

        // A11y events will happen on each rating button and not on the tooltip
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS

        // Initial update to ensure padding is set
        update(0, true)
    }

    fun setText(text: CharSequence) {
        tooltipTextView.text = text
    }

    fun update(offset: Int, bottom: Boolean) {
        tailOffset = offset.toFloat()
        tailBottom = bottom
        if (tailBottom) {
            setPadding(0, 0, 0, tailSize.toInt())
        } else {
            setPadding(0, tailSize.toInt(), 0, 0)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.save()

        // Flip vertically if the tail should be on top
        if (!tailBottom) canvas.scale(1f, -1f, 0f, height /  2f)

        // Draw body
        canvas.drawRoundRect(
            tooltipTextView.left.toFloat(),
            tooltipTextView.top.toFloat(),
            tooltipTextView.right.toFloat(),
            tooltipTextView.bottom.toFloat(),
            cornerRadius,
            cornerRadius,
            bubblePaint
        )

        // Draw tail
        val path1 = Path().apply {
            val minOffset = cornerRadius + tailSize
            val minX = tooltipTextView.left.toFloat() + minOffset
            val maxX = tooltipTextView.right.toFloat() - minOffset
            val offsetX = if (maxX < minX) {
                // Width is too small, use the center
                (tooltipTextView.left + tooltipTextView.right) / 2f
            } else {
                tailOffset.coerceIn(minX, maxX)
            }
            val offsetY = height - tailSize -0.5f
            tailPath.offset(offsetX, offsetY, this)
        }
        canvas.drawPath(path1, bubblePaint)

        canvas.restore()

        super.dispatchDraw(canvas)
    }

}
