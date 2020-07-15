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
package com.instructure.student.mobius.assignmentDetails.ui.gradeCell

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setTextForVisibility
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import kotlinx.android.synthetic.main.view_student_grade_cell.view.*

class GradeCellView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.view_student_grade_cell, this)
        if (isInEditMode) {
            setState(
                GradeCellViewState.GradeData(
                    graphPercent = 0.91f,
                    score = "91",
                    showPointsLabel = true,
                    outOf = "Out of 100 pts",
                    latePenalty = "Late penalty (-2 pts)",
                    finalGrade = "Final Grade: 89 pts"
                )
            )
        }
    }

    fun setState(state: GradeCellViewState) {
        setVisible()
        when (state) {
            GradeCellViewState.Empty -> setGone()
            GradeCellViewState.Submitted -> {
                submittedState.setVisible()
                gradeState.setGone()
            }
            is GradeCellViewState.GradeData -> {
                submittedState.setGone()
                gradeState.setVisible()
                gradeState.contentDescription = state.gradeCellContentDescription

                // Text and visibility
                score.setTextForVisibility(state.score)
                pointsLabel.setVisible(state.showPointsLabel)
                completeIcon.setVisible(state.showCompleteIcon)
                incompleteIcon.setVisible(state.showIncompleteIcon)
                latePenalty.setTextForVisibility(state.latePenalty)
                finalGrade.setTextForVisibility(state.finalGrade)
                grade.setTextForVisibility(state.grade)
                grade.contentDescription = state.gradeContentDescription
                outOf.setTextForVisibility(state.outOf)
                outOf.contentDescription = state.outOfContentDescription

                // Accent color
                latePenalty.setTextColor(state.accentColor)
                chart.setColor(state.accentColor)
                completeIcon.imageTintList = ColorStateList.valueOf(state.accentColor)
                incompleteIcon.imageTintList = ColorStateList.valueOf(state.accentColor)

                // Percentage
                chart.setPercentage(state.graphPercent, true)

                // Statistics
                statisticsView.setVisible(state.showStatistics)
                statisticsTextContainer.setVisible(state.showStatistics)
                minLabel.setTextForVisibility(state.statisticsMinText)
                maxLabel.setTextForVisibility(state.statisticsMaxText)
                meanLabel.setTextForVisibility(state.statisticsMeanText)
                statisticsView.setState(state)
            }
        }
    }

}

