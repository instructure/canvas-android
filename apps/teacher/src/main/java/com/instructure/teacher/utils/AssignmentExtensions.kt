/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
@file:JvmName("AssignmentUtils")

package com.instructure.teacher.utils

import android.content.Context
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentDueDate
import com.instructure.canvasapi2.models.AssignmentOverride
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.postmodels.AssignmentPostBody
import com.instructure.canvasapi2.models.postmodels.OverrideBody
import com.instructure.canvasapi2.models.postmodels.QuizAssignmentPostBody
import com.instructure.canvasapi2.type.SubmissionGradingStatus
import com.instructure.canvasapi2.type.SubmissionType
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.toDate
import com.instructure.pandautils.utils.AssignmentUtils2
import com.instructure.pandautils.utils.AssignmentUtils2.ASSIGNMENT_STATE_DUE
import com.instructure.pandautils.utils.AssignmentUtils2.ASSIGNMENT_STATE_GRADED
import com.instructure.pandautils.utils.AssignmentUtils2.ASSIGNMENT_STATE_GRADED_LATE
import com.instructure.pandautils.utils.AssignmentUtils2.ASSIGNMENT_STATE_GRADED_MISSING
import com.instructure.pandautils.utils.AssignmentUtils2.ASSIGNMENT_STATE_MISSING
import com.instructure.pandautils.utils.AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED
import com.instructure.pandautils.utils.AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED_LATE
import com.instructure.pandautils.utils.AssignmentUtils2.getAssignmentState
import com.instructure.pandautils.utils.DisplayGrade
import com.instructure.pandautils.utils.getContentDescriptionForMinusGradeString
import com.instructure.pandautils.utils.orDefault
import com.instructure.teacher.R
import com.instructure.teacher.models.CoreDates
import com.instructure.teacher.models.DueDateGroup
import java.util.Calendar

fun List<SubmissionType>?.getAssignmentIcon() = when {
    this == null -> R.drawable.ic_assignment
    SubmissionType.online_quiz in this -> R.drawable.ic_quiz
    SubmissionType.discussion_topic in this -> R.drawable.ic_discussion
    else -> R.drawable.ic_assignment
}
//region Grouped due dates

var AssignmentPostBody.coreDates: CoreDates
    get() = CoreDates(
            dueAt.toDate(),
            lockAt.toDate(),
            unlockAt.toDate()
    )
    set(dates) {
        dueAt = dates.dueDate.toApiString()
        lockAt = dates.lockDate.toApiString()
        unlockAt = dates.unlockDate.toApiString()
    }

val Assignment.coreDates: CoreDates
    get() = CoreDates(dueAt.toDate(), lockAt.toDate(), unlockAt.toDate())

var AssignmentDueDate.coreDates: CoreDates
    get() = CoreDates(dueAt.toDate(), lockAt.toDate(), unlockAt.toDate())
    set(dates) {
        dueAt = dates.dueDate.toApiString()
        lockAt = dates.lockDate.toApiString()
        unlockAt = dates.unlockDate.toApiString()
    }

var OverrideBody.coreDates: CoreDates
    get() = CoreDates(dueAt, lockAt, unlockAt)
    set(dates) {
        dueAt = dates.dueDate
        lockAt = dates.lockDate
        unlockAt = dates.unlockDate
    }

typealias EditDateGroups = ArrayList<DueDateGroup>

val Assignment.groupedDueDates: EditDateGroups
    get() {
        val dates = ArrayList(allDates)
        if (!onlyVisibleToOverrides && dates.none { it.isBase }) {
            dates += AssignmentDueDate().apply {
                isBase = true
                coreDates = this@groupedDueDates.coreDates
            }
        }
        return ArrayList(dates.groupBy { it.coreDates }
            .map { (date, simpleDates) ->
                val overrides = simpleDates.filter { it.id > 0 }.map { simpleDate -> overrides?.firstOrNull { it.id == simpleDate.id } ?: AssignmentOverride() }
                DueDateGroup(
                        sectionIds = overrides.map { it.courseSectionId }.filter { it != 0L },
                        groupIds = overrides.map { it.groupId }.filter { it != 0L },
                        studentIds = overrides.flatMap { it.studentIds },
                        isEveryone = simpleDates.any { it.isBase },
                        coreDates = date
                )
            })
    }

fun AssignmentPostBody.setGroupedDueDates(dates: EditDateGroups) {
    val newOverrides: List<OverrideBody> = dates.flatMap { (_, sections, groups, students, coreDate) ->
        val mappedOverrides = arrayListOf<OverrideBody>()
        mappedOverrides += groups.map {
            OverrideBody().apply {
                groupId = it
                coreDates = coreDate
            }
        }
        mappedOverrides += sections.map {
            OverrideBody().apply {
                courseSectionId = it
                coreDates = coreDate
            }
        }
        if (students.isNotEmpty()) {
            mappedOverrides += OverrideBody().apply {
                studentIds = students.toLongArray()
                coreDates = coreDate
            }
        }
        mappedOverrides
    }

    assignmentOverrides = newOverrides

    val baseDate = dates.firstOrNull { it.isEveryone }
    if (baseDate == null) {
        isOnlyVisibleToOverrides = newOverrides.isNotEmpty()
    } else {
        isOnlyVisibleToOverrides = false
        coreDates = baseDate.coreDates
    }
}
//endregion

fun Assignment.getDisplayGrade(
    submission: Submission?,
    context: Context,
    includePointsPossible: Boolean = true,
    includeLatePenalty: Boolean = false
): DisplayGrade {
    // If the submission doesn't exist, so we return an empty string
    if(submission == null) return DisplayGrade()

    // Cover the first edge case: excused assignment
    if(submission.excused) {
        return DisplayGrade(context.getString(R.string.excused))
    }

    // Cover the second edge case: NOT_GRADED type and no grade
    if(Assignment.getGradingTypeFromAPIString(this.gradingType ?: "") == Assignment.GradingType.NOT_GRADED) {
        return DisplayGrade(context.getString(R.string.not_graded))
    }

    // First lets see if the assignment is graded
    if(submission.grade != null && submission.grade != "null") {
        return when(Assignment.getGradingTypeFromAPIString(this.gradingType ?: "")) {
            Assignment.GradingType.POINTS ->
                if(includeLatePenalty) {
                    getPointsFraction(context, submission.enteredScore, this.pointsPossible)
                } else {
                    getPointsFraction(context, submission.score.orDefault(), this.pointsPossible)
                }
            //edge case, NOT_GRADED type with grade, it COULD happen
            Assignment.GradingType.NOT_GRADED -> DisplayGrade(context.getString(R.string.not_graded))
            else ->{
                var grade = submission.grade.takeUnless { it == "null" }.orEmpty()
                if (this.gradingType == Assignment.PERCENT_TYPE) {
                    try {
                        val value: Double = if(includeLatePenalty) submission.enteredGrade?.removeSuffix("%")?.toDouble() as Double else submission.grade?.removeSuffix("%")?.toDouble() as Double
                        grade = NumberHelper.doubleToPercentage(value, 2)
                    } catch (e: NumberFormatException) { }
                }
                when(submission.grade) {
                    "complete" ->
                        grade = context.getString(R.string.complete_grade)
                    "incomplete" ->
                        grade = context.getString(R.string.incomplete_grade)
                }
                if (includePointsPossible) {
                    if(includeLatePenalty) {
                        getPointsFractionWithGrade(context, submission.enteredScore, this.pointsPossible, grade)
                    } else {
                        getPointsFractionWithGrade(context, submission.score.orDefault(), this.pointsPossible, grade)
                    }
                } else {
                    DisplayGrade(grade)
                }
            }

        }
    } else {
        //return empty string for "empty" state
        return DisplayGrade()
    }
}

fun getDisplayGrade(
    context: Context,
    gradingStatus: SubmissionGradingStatus?,
    gradingType: String,
    grade: String?,
    enteredGrade: String?,
    score: Double?,
    enteredScore: Double?,
    pointsPossible: Double,
    includePointsPossible: Boolean = true,
    includeLatePenalty: Boolean = false
): DisplayGrade {
    if (gradingStatus == null) return DisplayGrade()

    // Cover the first edge case: excused assignment
    if (gradingStatus == SubmissionGradingStatus.excused) return DisplayGrade(context.getString(R.string.excused))


    // Cover the second edge case: NOT_GRADED type and no grade
    if (Assignment.getGradingTypeFromAPIString(gradingType) == Assignment.GradingType.NOT_GRADED) {
        return DisplayGrade(context.getString(R.string.not_graded))
    }

    // First let's see if the assignment is graded
    if (gradingStatus == SubmissionGradingStatus.graded && score != null && enteredScore != null) {
        return when (Assignment.getGradingTypeFromAPIString(gradingType)) {
            Assignment.GradingType.POINTS ->
                if (includeLatePenalty) {
                    getPointsFraction(context, enteredScore, pointsPossible)
                } else {
                    getPointsFraction(context, score, pointsPossible)
                }
        // Edge case, NOT_GRADED type with grade, it COULD happen
            Assignment.GradingType.NOT_GRADED -> DisplayGrade(context.getString(R.string.not_graded))
            else -> {
                var formattedGrade = grade.takeUnless { it == "null" }.orEmpty()
                if (gradingType == Assignment.PERCENT_TYPE) {
                    try {
                        val value: Double = if (includeLatePenalty) {
                            enteredGrade?.removeSuffix("%")?.toDouble() as Double
                        } else {
                            grade?.removeSuffix( "%")?.toDouble() as Double
                        }
                        formattedGrade = NumberHelper.doubleToPercentage(value, 2)
                    } catch (ignored: NumberFormatException) { }
                }
                when (grade) {
                    "complete" ->
                        formattedGrade = context.getString(R.string.complete_grade)
                    "incomplete" ->
                        formattedGrade = context.getString(R.string.incomplete_grade)
                }
                if (includePointsPossible) {
                    if (includeLatePenalty) {
                        getPointsFractionWithGrade(context, enteredScore, pointsPossible, formattedGrade)
                    } else {
                        getPointsFractionWithGrade(context, score, pointsPossible, formattedGrade)
                    }
                } else {
                    DisplayGrade(
                        formattedGrade,
                        getContentDescriptionForMinusGradeString(formattedGrade, context)
                    )
                }
            }

        }
    } else {
        // Return empty string for "empty" state
        return DisplayGrade()
    }
}

private fun getPointsFraction(context: Context, points: Double, pointsPossible: Double): DisplayGrade {
    val pointsText = NumberHelper.formatDecimal(points, 2, true)
    val possibleText = NumberHelper.formatDecimal(pointsPossible, 2, true)
    val text = context.getString(R.string.gradeFormatScoreOutOfPointsPossible, pointsText, possibleText)
    val contentDescription = context.getString(R.string.contentDescriptionScoreOutOfPointsPossible, pointsText, possibleText)
    return  DisplayGrade(text, contentDescription)
}

fun getPointsFractionWithGrade(context: Context, points: Double, pointsPossible: Double, grade: String?): DisplayGrade {
    val pointsText = NumberHelper.formatDecimal(points, 2, true)
    val possibleText = NumberHelper.formatDecimal(pointsPossible, 2, true)
    val text = context.getString(R.string.formattedScoreWithPointsPossibleAndGrade, pointsText, possibleText, grade)
    val contentDescription = context.getString(
        R.string.contentDescriptionScoreWithPointsPossibleAndGrade,
        pointsText,
        possibleText,
        getContentDescriptionForMinusGradeString(grade.orEmpty(), context)
    )
    return DisplayGrade(text, contentDescription)
}

fun Assignment?.getState(submission: Submission?, isTeacher: Boolean = false) = AssignmentUtils2.getAssignmentState(this, submission, isTeacher)

/**
 *
 * @return Pair(stringRes: Int, colorRes: Int)
 */
fun Assignment.getResForSubmission(submission: Submission?): Pair<Int, Int> {
    when(getAssignmentState(this, submission)) {
        ASSIGNMENT_STATE_MISSING -> {
            // If they haven't turned it in but there is no due date, we just want to show it as "Not Submitted"
            return if(this.dueAt == null) {
                Pair(R.string.submission_status_not_submitted, R.color.textDark)
            } else {
                Pair(R.string.submission_status_missing, R.color.textDanger)
            }
        }

        ASSIGNMENT_STATE_GRADED, ASSIGNMENT_STATE_GRADED_MISSING -> {
            return when {
                submission != null && (submission.attempt > 0 || Assignment.SubmissionType.ON_PAPER.apiString in submissionTypesRaw) -> // User has made attempts, so it has been submitted, or there is a submission and it was on paper
                    Pair(R.string.submission_status_submitted, R.color.textSuccess)
                this.dueAt == null -> // No Due date + no submission + graded == Not Submitted
                    Pair(R.string.submission_status_not_submitted, R.color.textDark)
                (this.dueAt.toDate()?.time ?: 0) >= Calendar.getInstance().timeInMillis -> // Not past due date + no submission + grade == Not submitted yet
                    Pair(R.string.submission_status_not_submitted, R.color.textDark)
                else -> // Past due + no submission + grade == Missing
                    Pair(R.string.submission_status_missing, R.color.textDanger)
            }
        }

        ASSIGNMENT_STATE_SUBMITTED_LATE, ASSIGNMENT_STATE_GRADED_LATE ->
            return Pair(R.string.submission_status_late, R.color.textWarning)

        ASSIGNMENT_STATE_SUBMITTED ->
            return Pair(R.string.submission_status_submitted, R.color.textSuccess)

        ASSIGNMENT_STATE_DUE ->
            return Pair(R.string.submission_status_not_submitted, R.color.textDark)

        else -> return Pair(-1, -1)
    }
}

fun getResForSubmission(submissionStatus: String?): Pair<Int, Int> {
    return when(submissionStatus) {
        "missing" -> Pair(R.string.submission_status_missing, R.color.textDanger)
        "late" -> Pair(R.string.submission_status_late, R.color.textWarning)
        "submitted" -> Pair(R.string.submission_status_submitted, R.color.textSuccess)
        "unsubmitted" -> Pair(R.string.submission_status_not_submitted, R.color.textDark)
        else -> Pair(-1, -1)
    }
}

fun AssignmentPostBody.toQuizAssignmentPostBody() = QuizAssignmentPostBody(
    dueAt, notifyOfUpdate, unlockAt, lockAt, published, assignmentOverrides, isOnlyVisibleToOverrides
)