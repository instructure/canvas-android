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
package com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui

import android.animation.FloatEvaluator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.StateSet
import android.view.Gravity
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.instructure.pandautils.utils.DP
import com.instructure.pandautils.utils.ViewStyler
import com.emeritus.student.R
import com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.RatingData

@SuppressLint("ViewConstructor")
class CriterionRatingButton(context: Context, data: RatingData, val tint: Int) : TextView(context) {

    /** Horizontal padding */
    private val horizontalPadding = context.DP(12f).toInt()

    /** Minimum button width and height */
    private val minSize = context.DP(52f).toInt()

    /** Button text size */
    private val fontSize = if (data.useSmallText) 16f else 20f

    init {
        // Set text appearance to Medium
        @Suppress("DEPRECATION")
        setTextAppearance(context, R.style.TextFont_Medium)

        // Assign sizes/dimensions
        textSize = fontSize
        minWidth = minSize
        minHeight = minSize
        gravity = Gravity.CENTER
        setPadding(horizontalPadding, 0, horizontalPadding, 0)

        // Set the background to handle selected and unselected states
        background = RatingButtonDrawable(context, tint)

        // Set text color to handle selected and unselected states
        setTextColor(
            ViewStyler.generateColorStateList(
                intArrayOf(android.R.attr.state_activated) to Color.WHITE,
                intArrayOf(android.R.attr.state_selected) to tint,
                intArrayOf(android.R.attr.state_pressed) to tint,
                intArrayOf() to ContextCompat.getColor(context, R.color.textDark)
            )
        )

        // Populate data
        contentDescription = data.text
        text = data.text
        isActivated = data.isAssessed
        isSelected = data.isSelected
    }

}

/**
 * A tinted, pill-shaped drawable that animates between activated/selected/normal states with an overshoot interpolator.
 *
 * Default state uses a transparent background and a gray stroke. Has a 2dp margin.
 * Pressed state uses a transparent background and a tinted stroke. Has a 2dp margin.
 * Activated state uses an opaque tinted background and a tinted stroke. Has a 2dp margin.
 * Selected state uses a mostly-transparent tinted background and a tinted stroke. Has no margin.
 * Activated-Selected state uses an opaque tinted background and a tinted stroke. Has no margin.
 */
class RatingButtonDrawable(context: Context, val tint: Int) : Drawable() {

    /** Radius of the corners. We use a large value like 1000dp to keep the drawable pill-shaped */
    private val radius = context.DP(1000)

    /** Width of the stroke */
    private val strokeSize = context.DP(1f)

    /** Paint object used for drawing the background */
    private val bgPaint = Paint()

    /** Paint object used for drawing the stroke */
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = strokeSize
        style = Paint.Style.STROKE
    }

    /** Map of state specs to animation states */
    private val specMap = listOf(
        /** Activated and Selected state */
        intArrayOf(android.R.attr.state_activated, android.R.attr.state_selected) to AnimState(
            padding = 0f,
            backgroundColor = tint,
            strokeColor = tint
        ),

        /** Selected State */
        intArrayOf(android.R.attr.state_selected) to AnimState(
            padding = 0f,
            backgroundColor = ColorUtils.setAlphaComponent(tint, 13),
            strokeColor = tint
        ),

        /** Activated state */
        intArrayOf(android.R.attr.state_activated) to  AnimState(
            padding = context.DP(2),
            backgroundColor = tint,
            strokeColor = tint
        ),

        /** Pressed state*/
        intArrayOf(android.R.attr.state_pressed) to AnimState(
            padding = context.DP(2),
            backgroundColor = Color.TRANSPARENT,
            strokeColor = tint
        ),

        /** Default state */
        intArrayOf() to AnimState(
            padding = context.DP(2),
            backgroundColor = Color.TRANSPARENT,
            strokeColor = ContextCompat.getColor(context, R.color.backgroundMedium)
        )
    )

    /** States tracked for animation, initialized using default state from specMap */
    private var startAnimState = specMap.last().second
    private var currentAnimState = specMap.last().second
    private var targetAnimState = specMap.last().second

    /** Float evaluator for calculating animation progress from start state to target state */
    private val floatEval = FloatEvaluator()

    /** The last spec-AnimState pair tracked by onStateChange */
    private var lastState = specMap.last()

    /** The Animator used to perform state change animations */
    private val animator = ObjectAnimator.ofFloat(this, "animProgress", 0f, 1f).apply {
        interpolator = OvershootInterpolator(4f)
        duration = 200
    }

    @Suppress("unused")
    @SuppressLint("AnimatorKeep")
    private fun setAnimProgress(newProgress: Float) {
        currentAnimState = AnimState(
            padding = floatEval.evaluate(newProgress, startAnimState.padding, targetAnimState.padding),
            backgroundColor = targetAnimState.backgroundColor,
            strokeColor = targetAnimState.strokeColor
        )
        invalidateSelf()
    }

    override fun isStateful() = true

    override fun onStateChange(stateSet: IntArray): Boolean {
        val newState = specMap.find { StateSet.stateSetMatches(it.first, stateSet) } ?: specMap.last()
        if (lastState != newState) {
            lastState = newState
            animator.cancel()
            startAnimState = currentAnimState
            targetAnimState = newState.second
            animator.start()
            return true
        }
        return false
    }

    override fun draw(canvas: Canvas) {
        bgPaint.color = currentAnimState.backgroundColor
        strokePaint.color = currentAnimState.strokeColor
        val padding = currentAnimState.padding + strokeSize / 2f + 0.5f
        val rect = RectF(
            padding,
            padding,
            canvas.clipBounds.width() - padding,
            canvas.clipBounds.height() - padding
        )
        canvas.drawRoundRect(rect, radius, radius, bgPaint)
        canvas.drawRoundRect(rect, radius, radius, strokePaint)
    }


    override fun setAlpha(alpha: Int) = Unit

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) = Unit

    private data class AnimState(
        val padding: Float,
        val backgroundColor: Int,
        val strokeColor: Int
    )

}
