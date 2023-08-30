/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.util

import android.content.Context
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.GradingSchemeRow
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.convertScoreToLetterGrade
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.pandautils.utils.*
import com.instructure.student.R

object BinderUtils {
    private const val NO_GRADE_INDICATOR = "-"

    @Suppress("DEPRECATION")
    fun getHtmlAsText(html: String?) = html?.validOrNull()?.let { StringUtilities.simplifyHTML(Html.fromHtml(it)) }

    fun getGrade(assignment: Assignment, submission: Submission?, context: Context, restrictQuantitativeData: Boolean, gradingScheme: List<GradingSchemeRow>): DisplayGrade {
        val possiblePoints = assignment.pointsPossible
        val pointsPossibleText = NumberHelper.formatDecimal(possiblePoints, 2, true)

        // No submission
        if (submission == null) {
            return if (possiblePoints > 0 && !restrictQuantitativeData) {
                DisplayGrade(
                    context.getString(
                        R.string.gradeFormatScoreOutOfPointsPossible,
                        NO_GRADE_INDICATOR,
                        pointsPossibleText
                    ),
                    context.getString(R.string.outOfPointsFormatted, pointsPossibleText)
                )
            } else {
                DisplayGrade(NO_GRADE_INDICATOR, "")
            }
        }

        // Excused
        if (submission.excused) {
            if (restrictQuantitativeData) {
                return DisplayGrade(context.getString(R.string.gradeExcused))
            } else {
                return DisplayGrade(
                    context.getString(
                        R.string.gradeFormatScoreOutOfPointsPossible,
                        context.getString(R.string.excused),
                        pointsPossibleText
                    ),
                    context.getString(
                        R.string.contentDescriptionScoreOutOfPointsPossible,
                        context.getString(R.string.gradeExcused),
                        pointsPossibleText
                    )
                )
            }
        }

        val grade = submission.grade ?: return DisplayGrade()
        val gradeContentDescription = getContentDescriptionForMinusGradeString(grade, context).validOrNull() ?: grade

        val gradingType = Assignment.getGradingTypeFromAPIString(assignment.gradingType.orEmpty())

        /*
         * For letter grade or GPA scale grading types, format grade text as "score / pointsPossible (grade)" to
         * more closely match web, e.g. "15 / 20 (2.0)" or "80 / 100 (B-)".
         */
        if (gradingType == Assignment.GradingType.LETTER_GRADE || gradingType == Assignment.GradingType.GPA_SCALE) {
            if (restrictQuantitativeData) {
                return DisplayGrade(grade, gradeContentDescription)
            } else {
                val scoreText = NumberHelper.formatDecimal(submission.score, 2, true)
                val possiblePointsText = NumberHelper.formatDecimal(possiblePoints, 2, true)
                return DisplayGrade(
                    context.getString(
                        R.string.formattedScoreWithPointsPossibleAndGrade,
                        scoreText,
                        possiblePointsText,
                        grade
                    ),
                    context.getString(
                        R.string.contentDescriptionScoreWithPointsPossibleAndGrade,
                        scoreText,
                        possiblePointsText,
                        gradeContentDescription
                    )
                )
            }
        }

        if (restrictQuantitativeData && assignment.isGradingTypeQuantitative) {
            val letterGrade = convertScoreToLetterGrade(submission.score, assignment.pointsPossible, gradingScheme)
            return DisplayGrade(letterGrade, getContentDescriptionForMinusGradeString(letterGrade, context).validOrNull() ?: letterGrade)
        }

        // Numeric grade
        submission.grade?.toDoubleOrNull()?.let { parsedGrade ->
            if (restrictQuantitativeData) return DisplayGrade()
            val formattedGrade = NumberHelper.formatDecimal(parsedGrade, 2, true)
            return DisplayGrade(
                context.getString(
                    R.string.gradeFormatScoreOutOfPointsPossible,
                    formattedGrade,
                    pointsPossibleText
                ),
                context.getString(
                    R.string.contentDescriptionScoreOutOfPointsPossible,
                    formattedGrade,
                    pointsPossibleText
                )
            )
        }

        // Complete/incomplete
        return when (grade) {
            "complete" -> return DisplayGrade(context.getString(R.string.gradeComplete))
            "incomplete" -> return DisplayGrade(context.getString(R.string.gradeIncomplete))
            // Other remaining case is where the grade is displayed as a percentage
            else -> if (restrictQuantitativeData) DisplayGrade() else DisplayGrade(grade, gradeContentDescription)
        }
    }

    fun setupGradeText(
        context: Context,
        textView: TextView,
        assignment: Assignment,
        submission: Submission,
        color: Int,
        restrictQuantitativeData: Boolean,
        gradingScheme: List<GradingSchemeRow>
    ) {
        val (grade, contentDescription) = getGrade(assignment, submission, context, restrictQuantitativeData, gradingScheme)
        if (!submission.excused && grade.isValid()) {
            textView.text = grade
            textView.contentDescription = contentDescription
            textView.setTextAppearance(R.style.TextStyle_Grade)
            textView.background =
                ColorUtils.colorIt(color, ContextCompat.getDrawable(context, R.drawable.grade_background)!!)
        } else {
            textView.text = grade
            textView.setTextAppearance(R.style.TextStyle_NoGrade)
            textView.background = null
            textView.contentDescription = grade
        }
    }

    fun getAssignmentIcon(assignment: Assignment?): Int {
        if (assignment == null) return 0

        return when {
            assignment.getSubmissionTypes().contains(Assignment.SubmissionType.ONLINE_QUIZ) -> R.drawable.ic_quiz
            assignment.getSubmissionTypes()
                .contains(Assignment.SubmissionType.DISCUSSION_TOPIC) -> R.drawable.ic_discussion
            else -> R.drawable.ic_assignment
        }
    }

    fun updateShadows(isFirstItem: Boolean, isLastItem: Boolean, top: View, bottom: View) {
        if (isFirstItem) top.setVisible() else top.setInvisible()
        if (isLastItem) bottom.setVisible() else bottom.setInvisible()
    }
}
