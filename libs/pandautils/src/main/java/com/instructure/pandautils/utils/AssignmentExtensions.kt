/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.pandautils.utils

import android.content.Context
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.GradingSchemeRow
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.convertScoreToLetterGrade
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.pandautils.R


private const val NO_GRADE_INDICATOR = "-"

fun Assignment.getAssignmentIcon() = when {
    Assignment.SubmissionType.ONLINE_QUIZ.apiString in submissionTypesRaw -> R.drawable.ic_quiz
    Assignment.SubmissionType.DISCUSSION_TOPIC.apiString in submissionTypesRaw -> R.drawable.ic_discussion
    isInternalLti() -> R.drawable.ic_quiz
    else -> R.drawable.ic_assignment
}

fun Assignment.getGrade(
    submission: Submission?,
    context: Context,
    restrictQuantitativeData: Boolean,
    gradingScheme: List<GradingSchemeRow>,
    showZeroPossiblePoints: Boolean = false,
    showNotGraded: Boolean = false
): DisplayGrade {
    val possiblePoints = this.pointsPossible
    val pointsPossibleText = NumberHelper.formatDecimal(possiblePoints, 2, true)

    val notGradedDisplayGrade = if ((showZeroPossiblePoints || possiblePoints > 0) && !restrictQuantitativeData) {
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

    // No submission
    if (submission == null) {
        return notGradedDisplayGrade
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

    val grade = submission.grade ?: return if (showNotGraded) notGradedDisplayGrade else DisplayGrade()
    val gradeContentDescription = getContentDescriptionForMinusGradeString(grade, context).validOrNull() ?: grade

    val gradingType = Assignment.getGradingTypeFromAPIString(this.gradingType.orEmpty())

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

    if (restrictQuantitativeData && this.isGradingTypeQuantitative) {
        val letterGrade = convertScoreToLetterGrade(submission.score, this.pointsPossible, gradingScheme)
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
