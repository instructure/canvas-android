/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission

import android.content.Context
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.utils.getShortMonthAndDay
import com.instructure.pandautils.utils.isAllowedToSubmitWithOverrides
import com.instructure.pandautils.utils.getTime
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionDetailsEmptyContentViewState
import com.instructure.student.mobius.common.ui.Presenter
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.temporal.ChronoUnit

object SubmissionDetailsEmptyContentPresenter : Presenter<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentViewState> {
    override fun present(model: SubmissionDetailsEmptyContentModel, context: Context): SubmissionDetailsEmptyContentViewState {
        return SubmissionDetailsEmptyContentViewState.Loaded(
            getSubmitButtonTextResource(context, model.assignment),
            getEmptyViewTitleText(model.assignment, context, model.course),
            getEmptyViewSubtitleText(model.assignment, context),
            isSubmitButtonVisible(model.assignment, model.course, model.isObserver)
        )
    }

    private fun getSubmitButtonTextResource(context: Context, assignment: Assignment): String {
        val isExternalToolSubmission = assignment.getSubmissionTypes()
            .any { it == Assignment.SubmissionType.EXTERNAL_TOOL || it == Assignment.SubmissionType.BASIC_LTI_LAUNCH }

        val turnInType = assignment.turnInType

        return context.getString(
            when {
                turnInType == Assignment.TurnInType.QUIZ -> R.string.viewQuiz
                turnInType == Assignment.TurnInType.DISCUSSION -> R.string.viewDiscussion
                isExternalToolSubmission -> R.string.launchExternalTool
                else -> R.string.submitAssignment
            }
        )
    }

    private fun getEmptyViewTitleText(assignment: Assignment, context: Context, course: Course): String {
        return when {
            !isGraded(assignment) -> context.getString(R.string.submissionDetailsNoSubmissionAllowed)
            allowedToSubmit(assignment, course) -> context.getString(R.string.submissionDetailsNoSubmissionYet)
            else -> context.getString(R.string.submissionDetailsAssignmentLocked)
        }
    }

    private fun getEmptyViewSubtitleText(assignment: Assignment, context: Context): String {
        return if (isGraded(assignment)) assignment.getDueString(context) else ""
    }

    private fun isSubmitButtonVisible(assignment: Assignment, course: Course, isObserver: Boolean): Boolean {
        return allowedToSubmit(assignment, course) && !isObserver && isGraded(assignment)
    }

    private fun isGraded(assignment: Assignment): Boolean {
        val gradingType = (assignment.gradingType ?: "")
        return Assignment.getGradingTypeFromAPIString(gradingType) != Assignment.GradingType.NOT_GRADED
    }

    private fun allowedToSubmit(assignment: Assignment, course: Course): Boolean {
        return if (!course.isBetweenValidDateRange()) {
            return false
        } else if (assignment.turnInType == Assignment.TurnInType.ONLINE || assignment.turnInType == Assignment.TurnInType.EXTERNAL_TOOL) {
            assignment.isAllowedToSubmitWithOverrides(course)
        } else {
            true
        }
    }
}

private fun Assignment.getDueString(context: Context): String {
    if (!isAllowedToSubmit)
        return getLockedString(context)

    if (dueAt.isNullOrBlank())
        return context.getString(R.string.submissionDetailsHasNoDueDate)

    // There doesn't appear to be an easy way to convert the UTC time to the user's time, so we do this dance
    val dueDateTime = OffsetDateTime.parse(dueAt).withOffsetSameInstant(OffsetDateTime.now().offset)
    val now = LocalDate.now()

    return when (ChronoUnit.DAYS.between(now, dueDateTime)) {
        -1L -> {
            // Yesterday
            context.getString(R.string.submissionDetailsDueYesterdayAt, dueDateTime.getTime())
        }
        0L -> {
            // Today
            context.getString(R.string.submissionDetailsDueTodayAt, dueDateTime.getTime())
        }
        1L -> {
            // Tomorrow
            context.getString(R.string.submissionDetailsDueTomorrowAt, dueDateTime.getTime())
        }
        else -> {
            // Due sometime in the future
            context.getString(R.string.submissionDetailsDueAt, dueDateTime.getShortMonthAndDay(), dueDateTime.getTime())
        }
    }
}

private fun Assignment.getLockedString(context: Context): String {
    val now = LocalDate.now()

    // Check if the user is locked out by a module
    lockInfo?.let {
        if (!it.lockedModuleName.isNullOrBlank()) {
            return context.getString(R.string.submissionDetailsAssignmentLockedByModuleName, it.lockedModuleName)
        }
        // Check if the user is locked out by module prereqs
        else if (it.modulePrerequisiteNames != null && it.modulePrerequisiteNames!!.size > 0) {
            return context.getString(R.string.submissionDetailsAssignmentLockedByModulePrereqs)
        }
    }

    // Check if we are within the availability window
    unlockAt?.let {
        // There doesn't appear to be an easy way to convert the UTC time to the user's time, so we do this dance
        val unlockDateTime = OffsetDateTime.parse(it).withOffsetSameInstant(OffsetDateTime.now().offset)

        if (now.isBefore(unlockDateTime.toLocalDate())) {
            // Assignment isn't unlocked yet
            return context.getString(R.string.submissionDetailsAssignmentWillUnlockOn, unlockDateTime.getShortMonthAndDay(), unlockDateTime.getTime())
        }
    }

    lockAt?.let {
        // There doesn't appear to be an easy way to convert the UTC time to the user's time, so we do this dance
        val lockDateTime = OffsetDateTime.parse(lockAt).withOffsetSameInstant(OffsetDateTime.now().offset)

        if (now.isAfter(lockDateTime.toLocalDate())) {
            // Assignment was locked
            return context.getString(R.string.submissionDetailsAssignmentWasLockedOn, lockDateTime.getShortMonthAndDay(), lockDateTime.getTime())
        }
    }

    return context.getString(R.string.lockedAssignmentNotModule)
}

