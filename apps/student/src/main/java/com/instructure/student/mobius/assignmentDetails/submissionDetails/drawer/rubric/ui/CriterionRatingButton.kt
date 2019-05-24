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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.StateListDrawable
import android.view.Gravity
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.instructure.pandautils.utils.DP
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.getDrawableCompat
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.RatingData

@SuppressLint("ViewConstructor")
class CriterionRatingButton(context: Context, data: RatingData) : TextView(context) {

    /** Horizontal padding */
    private val horizontalPadding = context.DP(12f).toInt()

    /** Minimum button width and height */
    private val minSize = context.DP(48f).toInt()

    /** Button text size */
    private val fontSize = 20f

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
        background = makeBackground()

        // Set text color to handle selected and unselected states
        val brandColor: Int = if (isInEditMode) 0xFF34444F.toInt() else ThemePrefs.brandColor
        setTextColor(
            ViewStyler.generateColorStateList(
                intArrayOf(android.R.attr.state_selected) to Color.WHITE,
                intArrayOf(android.R.attr.state_pressed) to brandColor,
                intArrayOf() to ContextCompat.getColor(context, R.color.defaultTextGray)
            )
        )

        // Tooltip setup
        SubmissionRubricTooltipHandler.setTooltipText(this, data.description.orEmpty())

        // Populate data
        contentDescription = data.description.orEmpty()
        text = data.points
        isSelected = data.isSelected
    }

    /** Generates a background drawable with selected and unselected states */
    private fun makeBackground() = StateListDrawable().apply {
        val selectedState = context.getDrawableCompat(R.drawable.bg_criterion_button_selected)
        val pressedState = context.getDrawableCompat(R.drawable.bg_criterion_button_unselected)
        val defaultState = context.getDrawableCompat(R.drawable.bg_criterion_button_unselected)
        if (isInEditMode) {
            val color = ContextCompat.getColor(context, R.color.canvasDefaultPrimary)
            selectedState.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            pressedState.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        } else {
            selectedState.setColorFilter(ThemePrefs.brandColor, PorterDuff.Mode.SRC_ATOP)
            pressedState.setColorFilter(ThemePrefs.brandColor, PorterDuff.Mode.SRC_ATOP)
        }
        addState(intArrayOf(android.R.attr.state_selected), selectedState)
        addState(intArrayOf(android.R.attr.state_pressed), pressedState)
        addState(intArrayOf(), defaultState)
    }

}
