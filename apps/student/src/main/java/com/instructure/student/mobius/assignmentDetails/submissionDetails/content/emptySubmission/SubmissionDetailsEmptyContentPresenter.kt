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
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionDetailsEmptyContentViewState
import com.instructure.student.mobius.common.ui.Presenter
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeFormatterBuilder
import org.threeten.bp.temporal.ChronoField
import org.threeten.bp.temporal.ChronoUnit

class SubmissionDetailsEmptyContentPresenter : Presenter<SubmissionDetailsEmptyContentModel, SubmissionDetailsEmptyContentViewState> {
    override fun present(model: SubmissionDetailsEmptyContentModel, context: Context): SubmissionDetailsEmptyContentViewState {
        model.assignment.isAllowedToSubmit
        return SubmissionDetailsEmptyContentViewState.Loaded(
                model.assignment.isAllowedToSubmit,
                model.assignment.getDueString(context),
                model.assignment.getLockedString(context)
        )
    }
}

fun Assignment.getDueString(context: Context): String {
    if (dueAt.isNullOrBlank()) {
        return context.getString(R.string.submissionDetailsHasNoDueDate)
    }

    // There doesn't appear to be an easy way to convert the UTC time to the user's time, so we do this dance
    val dueDateTime = OffsetDateTime.parse(dueAt).withOffsetSameInstant(OffsetDateTime.now().offset)
    val now = LocalDate.now()

    return when (ChronoUnit.DAYS.between(now, dueDateTime)) {
        -1L -> {
            // Yesterday
            context.getString(R.string.submissionDetailsDueYesterdayAt, dueDateTime.getTime(context))
        }
        0L -> {
            // Today
            context.getString(R.string.submissionDetailsDueTodayAt, dueDateTime.getTime(context))
        }
        1L -> {
            // Tomorrow
            context.getString(R.string.submissionDetailsDueTomorrowAt, dueDateTime.getTime(context))
        }
        else -> {
            // Due sometime in the future
            context.getString(R.string.submissionDetailsDueAt, dueDateTime.getShortMonthAndDay(), dueDateTime.getTime(context))
        }
    }
}

fun Assignment.getLockedString(context: Context): String? {
    if (isAllowedToSubmit) return null

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
            return context.getString(R.string.submissionDetailsAssignmentWillUnlockOn, unlockDateTime.getShortMonthAndDay(), unlockDateTime.getTime(context))
        }
    }

    lockAt?.let {
        // There doesn't appear to be an easy way to convert the UTC time to the user's time, so we do this dance
        val lockDateTime = OffsetDateTime.parse(lockAt).withOffsetSameInstant(OffsetDateTime.now().offset)

        if (now.isAfter(lockDateTime.toLocalDate())) {
            // Assignment was locked
            return context.getString(R.string.submissionDetailsAssignmentWasLockedOn, lockDateTime.getShortMonthAndDay(), lockDateTime.getTime(context))
        }
    }

    return null
}

fun OffsetDateTime.getShortMonthAndDay(): String {
    // Get year if the year of the due date isn't the current year
    val pattern = if (LocalDate.now().year != this.year) DateTimeFormatter.ofPattern("MMM d, Y") else DateTimeFormatter.ofPattern("MMM d")
    return format(pattern)
}

fun OffsetDateTime.getTime(context: Context): String {
    val amPm = hashMapOf(
            0L to context.getString(R.string.lowercaseAM),
            1L to context.getString(R.string.lowercasePM)
    )

    val pattern = DateTimeFormatterBuilder().appendPattern("h:mm").appendText(ChronoField.AMPM_OF_DAY, amPm).toFormatter()
    return format(pattern)
}

