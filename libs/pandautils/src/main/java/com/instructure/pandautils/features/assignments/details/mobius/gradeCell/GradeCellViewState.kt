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

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.getContentDescriptionForMinusGradeString
import com.instructure.pandautils.utils.orDefault

sealed class GradeCellViewState {
    object Empty : GradeCellViewState()
    object Submitted : GradeCellViewState()
    data class GradeData(
        val showCompleteIcon: Boolean = false,
        val showIncompleteIcon: Boolean = false,
        val showPointsLabel: Boolean = false,
        @ColorInt val accentColor: Int = Color.GRAY,
        val graphPercent: Float = 0f,
        val score: String = "",
        val grade: String = "",
        val gradeContentDescription: String = "",
        val gradeCellContentDescription: String = "",
        val outOf: String = "",
        val outOfContentDescription: String = "",
        val yourGrade: String = "",
        val latePenalty: String = "",
        val finalGrade: String = "",
        val stats: GradeStats? = null
    ) : GradeCellViewState()

    data class GradeStats(
        val score: Double = 0.0,
        val outOf: Double = 0.0,
        val min: Double? = null,
        val max: Double? = null,
        val mean: Double? = null,
        val minText: String = "",
        val maxText: String = "",
        val meanText: String = ""
    )

    companion object {
        /**
         * Generates a [GradeCellViewState] from the provided [submission] and [assignment]. The [submission] must be
         * a root submission, meaning it should be the parent submission object rather than an object from the
         * submission history.
         */
        fun fromSubmission(
            context: Context,
            assignment: Assignment,
            submission: Submission?,
            restrictQuantitativeData: Boolean = false
        ): GradeCellViewState {
            // Return empty state if unsubmitted and ungraded, or "Not Graded" grading type or quantitative data is restricted
            val hideGrades = restrictQuantitativeData && assignment.isGradingTypeQuantitative && submission?.excused != true
            if ((submission?.submittedAt == null && submission?.isGraded != true) || assignment.gradingType == Assignment.NOT_GRADED_TYPE || hideGrades) {
                return Empty
            }

            // Return the Submitted state if the submission has not been graded
            if (submission.submittedAt != null && !submission.isGraded) return Submitted

            /* The accent color, which determines the graph color and color of the late penalty text, should match
             * the course color. The only exception to this is when the submission for a Complete/Incomplete assignment
             * is graded as Incomplete, in which case the accent color should match the gray text color */
            val accentColor = CanvasContext.emptyCourseContext(assignment.courseId).color

            /* The 'Out of' text abbreviates the word "points" to "pts" which is read as "P T S" by screen readers, so
             * we use a second string with the full word "points" as a content description. */
            val pointsPossibleText = NumberHelper.formatDecimal(assignment.pointsPossible, 2, true)
            val outOfText = if (restrictQuantitativeData) "" else context.getString(R.string.outOfPointsAbbreviatedFormatted, pointsPossibleText)
            val outOfContentDescriptionText = if (restrictQuantitativeData) "" else context.getString(
                R.string.outOfPointsFormatted, pointsPossibleText)

            // Excused
            if (submission.excused) {
                return GradeData(
                    graphPercent = 1.0f,
                    showCompleteIcon = true,
                    accentColor = accentColor,
                    grade = context.getString(R.string.gradeExcused),
                    outOf = outOfText,
                    outOfContentDescription = outOfContentDescriptionText
                )
            }

            // Complete/Incomplete
            if (assignment.gradingType == Assignment.PASS_FAIL_TYPE) {
                val isComplete = (submission.grade == "complete")
                return GradeData(
                    showCompleteIcon = isComplete,
                    showIncompleteIcon = !isComplete,
                    grade = context.getString(if (isComplete) R.string.gradeComplete else R.string.gradeIncomplete),
                    accentColor = if (isComplete) accentColor else ContextCompat.getColor(context, R.color.textDark),
                    outOf = outOfText,
                    outOfContentDescription = outOfContentDescriptionText,
                    graphPercent = 1.0f
                )
            }

            if (restrictQuantitativeData) {
                val grade = submission.grade.orEmpty()
                val accessibleGradeString = getContentDescriptionForMinusGradeString(grade, context)
                val gradeCellContentDescription = context.getString(R.string.a11y_gradeCellContentDescriptionLetterGradeOnly, accessibleGradeString)

                return GradeData(
                    showCompleteIcon = true,
                    graphPercent = 1.0f,
                    accentColor = accentColor,
                    grade = grade,
                    gradeContentDescription = accessibleGradeString,
                    gradeCellContentDescription = gradeCellContentDescription
                )
            }

            val score = NumberHelper.formatDecimal(submission.score.orDefault(), 2, true)
            val graphPercent = (submission.score.orDefault() / assignment.pointsPossible).coerceIn(0.0, 1.0).toFloat()

            // If grading type is Points, don't show the grade since we're already showing it as the score
            var grade = if (assignment.gradingType != Assignment.POINTS_TYPE) submission.grade.orEmpty() else ""
            // Google talkback fails hard on "minus", so we need to remove the dash and replace it with the word
            val accessibleGradeString = getContentDescriptionForMinusGradeString(grade, context)
            // We also need the entire grade cell to be read in a reasonable fashion
            val gradeCellContentDescription = when {
                accessibleGradeString.isNotEmpty() -> context.getString(R.string.a11y_gradeCellContentDescriptionWithLetterGrade, score, outOfContentDescriptionText, accessibleGradeString)
                grade.isNotEmpty() -> context.getString(R.string.a11y_gradeCellContentDescriptionWithLetterGrade, score, outOfContentDescriptionText, grade)
                else -> context.getString(R.string.a11y_gradeCellContentDescription, score, outOfContentDescriptionText)
            }

            var latePenalty = ""
            var finalGrade = ""
            var yourGrade = ""

            // Adjust for late penalty, if any
            if ((submission.pointsDeducted ?: 0.0) > 0.0) {
                grade = "" // Grade will be shown in the 'final grade' text
                val pointsDeducted = NumberHelper.formatDecimal(submission.pointsDeducted!!, 2, true)
                val achievedScore = NumberHelper.formatDecimal(submission.enteredScore, 2, true)
                yourGrade = context.getString(R.string.yourGrade, achievedScore)
                latePenalty = context.getString(R.string.latePenaltyUpdated, pointsDeducted)
                finalGrade = context.getString(R.string.finalGradeFormatted, submission.grade)
            }

            // Grade statistics
            val stats = assignment.scoreStatistics?.let { stats ->
                GradeStats(
                    score = submission.score.orDefault(),
                    outOf = assignment.pointsPossible,
                    min = stats.min,
                    max = stats.max,
                    mean = stats.mean,
                    minText = context.getString(
                        R.string.scoreStatisticsLow,
                        NumberHelper.formatDecimal(stats.min, 1, true)
                    ),
                    maxText = context.getString(
                        R.string.scoreStatisticsHigh,
                        NumberHelper.formatDecimal(stats.max, 1, true)
                    ),
                    meanText = context.getString(
                        R.string.scoreStatisticsMean,
                        NumberHelper.formatDecimal(stats.mean, 1, true)
                    )
                )
            }

            return GradeData(
                graphPercent = graphPercent,
                accentColor = accentColor,
                score = score,
                showPointsLabel = true,
                outOf = outOfText,
                outOfContentDescription = outOfContentDescriptionText,
                grade = grade,
                gradeContentDescription = accessibleGradeString,
                gradeCellContentDescription = gradeCellContentDescription,
                yourGrade = yourGrade,
                latePenalty = latePenalty,
                finalGrade = finalGrade,
                stats = stats
            )
        }
    }
}
