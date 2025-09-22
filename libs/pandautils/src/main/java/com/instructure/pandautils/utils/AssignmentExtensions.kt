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
import android.content.res.Resources
import com.instructure.canvasapi2.CustomGradeStatusesQuery
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Checkpoint
import com.instructure.canvasapi2.models.GradingSchemeRow
import com.instructure.canvasapi2.models.SubAssignmentSubmission
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.convertScoreToLetterGrade
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.pandautils.R
import com.instructure.pandautils.features.grades.SubmissionStateLabel
import com.instructure.pandautils.utils.Const.REPLY_TO_ENTRY
import com.instructure.pandautils.utils.Const.REPLY_TO_TOPIC


private const val NO_GRADE_INDICATOR = "-"

fun Assignment.getAssignmentIcon() = when {
    Assignment.SubmissionType.ONLINE_QUIZ.apiString in submissionTypesRaw -> R.drawable.ic_quiz
    Assignment.SubmissionType.DISCUSSION_TOPIC.apiString in submissionTypesRaw -> R.drawable.ic_discussion
    Assignment.SubmissionType.EXTERNAL_TOOL.apiString in submissionTypesRaw -> ltiToolType().assignmentIconRes
    Assignment.SubmissionType.BASIC_LTI_LAUNCH.apiString in submissionTypesRaw -> ltiToolType().assignmentIconRes
    else -> R.drawable.ic_assignment
}

private fun Assignment.getGrade(
    noSubmission: Boolean,
    submissionGrade: String?,
    submissionScore: Double,
    excused: Boolean,
    possiblePoints: Double,
    resources: Resources,
    restrictQuantitativeData: Boolean,
    gradingScheme: List<GradingSchemeRow>,
    showZeroPossiblePoints: Boolean = false,
    showNotGraded: Boolean = false
): DisplayGrade {
    val pointsPossibleText = NumberHelper.formatDecimal(possiblePoints, 2, true)

    val notGradedDisplayGrade = if ((showZeroPossiblePoints || possiblePoints > 0) && !restrictQuantitativeData) {
        DisplayGrade(
            resources.getString(
                R.string.gradeFormatScoreOutOfPointsPossible,
                NO_GRADE_INDICATOR,
                pointsPossibleText
            ),
            resources.getString(R.string.outOfPointsFormatted, pointsPossibleText)
        )
    } else {
        DisplayGrade(NO_GRADE_INDICATOR, "")
    }

    // No submission
    if (noSubmission) {
        return notGradedDisplayGrade
    }

    // Excused
    if (excused) {
        if (restrictQuantitativeData) {
            return DisplayGrade(resources.getString(R.string.gradeExcused))
        } else {
            return DisplayGrade(
                resources.getString(
                    R.string.gradeFormatScoreOutOfPointsPossible,
                    resources.getString(R.string.excused),
                    pointsPossibleText
                ),
                resources.getString(
                    R.string.contentDescriptionScoreOutOfPointsPossible,
                    resources.getString(R.string.gradeExcused),
                    pointsPossibleText
                )
            )
        }
    }

    val grade = submissionGrade ?: return if (showNotGraded) notGradedDisplayGrade else DisplayGrade()
    val gradeContentDescription = getContentDescriptionForMinusGradeString(grade, resources).validOrNull() ?: grade

    val gradingType = Assignment.getGradingTypeFromAPIString(this.gradingType.orEmpty())

    /*
     * For letter grade or GPA scale grading types, format grade text as "score / pointsPossible (grade)" to
     * more closely match web, e.g. "15 / 20 (2.0)" or "80 / 100 (B-)".
     */
    if (gradingType == Assignment.GradingType.LETTER_GRADE || gradingType == Assignment.GradingType.GPA_SCALE) {
        if (restrictQuantitativeData) {
            return DisplayGrade(grade, gradeContentDescription)
        } else {
            val scoreText = NumberHelper.formatDecimal(submissionScore, 2, true)
            val possiblePointsText = NumberHelper.formatDecimal(possiblePoints, 2, true)
            return DisplayGrade(
                resources.getString(
                    R.string.formattedScoreWithPointsPossibleAndGrade,
                    scoreText,
                    possiblePointsText,
                    grade
                ),
                resources.getString(
                    R.string.contentDescriptionScoreWithPointsPossibleAndGrade,
                    scoreText,
                    possiblePointsText,
                    gradeContentDescription
                )
            )
        }
    }

    if (restrictQuantitativeData && this.isGradingTypeQuantitative) {
        val letterGrade = convertScoreToLetterGrade(submissionScore, possiblePoints, gradingScheme)
        return DisplayGrade(
            letterGrade,
            getContentDescriptionForMinusGradeString(letterGrade, resources).validOrNull() ?: letterGrade
        )
    }

    // Numeric grade
    submissionGrade.toDoubleOrNull()?.let { parsedGrade ->
        if (restrictQuantitativeData) return DisplayGrade()
        val formattedGrade = NumberHelper.formatDecimal(parsedGrade, 2, true)
        return DisplayGrade(
            resources.getString(
                R.string.gradeFormatScoreOutOfPointsPossible,
                formattedGrade,
                pointsPossibleText
            ),
            resources.getString(
                R.string.contentDescriptionScoreOutOfPointsPossible,
                formattedGrade,
                pointsPossibleText
            )
        )
    }

    // Complete/incomplete
    return when (grade) {
        "complete" -> return DisplayGrade(resources.getString(R.string.gradeComplete))
        "incomplete" -> return DisplayGrade(resources.getString(R.string.gradeIncomplete))
        // Other remaining case is where the grade is displayed as a percentage
        else -> if (restrictQuantitativeData) DisplayGrade() else DisplayGrade(grade, gradeContentDescription)
    }
}

fun Assignment.getGrade(
    submission: Submission?,
    resources: Resources,
    restrictQuantitativeData: Boolean,
    gradingScheme: List<GradingSchemeRow>,
    showZeroPossiblePoints: Boolean = false,
    showNotGraded: Boolean = false
) = getGrade(
    noSubmission = submission == null,
    submissionGrade = submission?.grade,
    submissionScore = submission?.score.orDefault(),
    excused = submission?.excused.orDefault(),
    possiblePoints = pointsPossible.orDefault(),
    resources = resources,
    restrictQuantitativeData = restrictQuantitativeData,
    gradingScheme = gradingScheme,
    showZeroPossiblePoints = showZeroPossiblePoints,
    showNotGraded = showNotGraded
)

fun Assignment.getSubAssignmentSubmissionGrade(
    possiblePoints: Double,
    submission: SubAssignmentSubmission?,
    resources: Resources,
    restrictQuantitativeData: Boolean,
    gradingScheme: List<GradingSchemeRow>,
    showZeroPossiblePoints: Boolean = false,
    showNotGraded: Boolean = false
) = getGrade(
    noSubmission = submission == null,
    submissionGrade = submission?.grade,
    submissionScore = submission?.score.orDefault(),
    excused = submission?.excused.orDefault(),
    possiblePoints = possiblePoints,
    resources = resources,
    restrictQuantitativeData = restrictQuantitativeData,
    gradingScheme = gradingScheme,
    showZeroPossiblePoints = showZeroPossiblePoints,
    showNotGraded = showNotGraded
)

private fun mapSubmissionStateLabel(
    customGradeStatusId: Long?,
    customStatuses: List<CustomGradeStatusesQuery.Node>,
    late: Boolean,
    missing: Boolean,
    graded: Boolean,
    submitted: Boolean,
    notSubmitted: Boolean
): SubmissionStateLabel {
    val matchedCustomStatus = customGradeStatusId?.let { id ->
        customStatuses.find { it._id.toLongOrNull() == id }
    }

    return when {
        matchedCustomStatus != null -> SubmissionStateLabel.Custom(
            R.drawable.ic_flag,
            R.color.textInfo,
            matchedCustomStatus.name
        )

        late -> SubmissionStateLabel.Late
        missing -> SubmissionStateLabel.Missing
        graded -> SubmissionStateLabel.Graded
        submitted -> SubmissionStateLabel.Submitted
        notSubmitted -> SubmissionStateLabel.NotSubmitted
        else -> SubmissionStateLabel.None
    }
}

fun Assignment.getSubmissionStateLabel(
    customStatuses: List<CustomGradeStatusesQuery.Node>
) = mapSubmissionStateLabel(
    submission?.customGradeStatusId,
    customStatuses,
    submission?.late.orDefault(),
    isMissing(),
    isGraded().orDefault(),
    submission?.submittedAt != null,
    !isSubmitted
)

fun Assignment.getSubAssignmentSubmissionStateLabel(
    submission: SubAssignmentSubmission?,
    customStatuses: List<CustomGradeStatusesQuery.Node>
) = mapSubmissionStateLabel(
    submission?.customGradeStatusId,
    customStatuses,
    submission?.late.orDefault(),
    submission?.missing.orDefault(),
    !submission?.grade.isNullOrEmpty(),
    submitted = false, // TODO: Sub-assignments do not have a submittedAt field
    notSubmitted = false // TODO: Sub-assignments do not have a submittedAt field
)

val Assignment.orderedCheckpoints: List<Checkpoint>
    get() = checkpoints.sortedWith(
        compareBy {
            when (it.tag) {
                REPLY_TO_TOPIC -> 0
                REPLY_TO_ENTRY -> 1
                else -> 2
            }
        }
    )
