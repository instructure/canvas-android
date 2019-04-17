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
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionDetailsEmptyViewState
import com.instructure.student.mobius.common.ui.Presenter
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeFormatterBuilder
import org.threeten.bp.format.ResolverStyle
import org.threeten.bp.format.SignStyle
import org.threeten.bp.temporal.ChronoField
import org.threeten.bp.temporal.ChronoUnit

class SubmissionDetailsEmptyPresenter : Presenter<SubmissionDetailsEmptyModel, SubmissionDetailsEmptyViewState> {
    override fun present(model: SubmissionDetailsEmptyModel, context: Context): SubmissionDetailsEmptyViewState {
        model.assignment.isAllowedToSubmit
        return SubmissionDetailsEmptyViewState.Loaded(
                model.assignment.isAllowedToSubmit,
                model.assignment.getDueString(context)
        )
    }
}

fun Assignment.getDueString(context: Context): String {
    if (this.dueAt.isNullOrBlank()) {
        return context.getString(R.string.submissionDetailsHasNoDueDate)
    }

    val dueDateTime = OffsetDateTime.parse(this.dueAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    val now = LocalDate.now()

    return when (ChronoUnit.DAYS.between(now, dueDateTime).toInt()) {
        -1 -> {
            // Yesterday
            context.getString(R.string.submissionDetailsDueYesterdayAt, dueDateTime.getTime(context))
        }
        0 -> {
            // Today
            context.getString(R.string.submissionDetailsDueTodayAt, dueDateTime.getTime(context))
        }
        1 -> {
            // Tomorrow
            context.getString(R.string.submissionDetailsDueTomorrowAt, dueDateTime.getTime(context))
        }
        else -> {
            // Due sometime in the future
            context.getString(R.string.submissionDetailsDueAt, dueDateTime.getShortMonthAndDay(context), dueDateTime.getTime(context))
        }
    }
}

fun OffsetDateTime.getShortMonthAndDay(context: Context): String {
    val monthsOfTheYear = hashMapOf(
            1L to context.getString(R.string.monthShortJanuary),
            2L to context.getString(R.string.monthShortFebruary),
            3L to context.getString(R.string.monthShortMarch),
            4L to context.getString(R.string.monthShortApril),
            5L to context.getString(R.string.monthShortMay),
            6L to context.getString(R.string.monthShortJune),
            7L to context.getString(R.string.monthShortJuly),
            8L to context.getString(R.string.monthShortAugust),
            9L to context.getString(R.string.monthShortSeptember),
            10L to context.getString(R.string.monthShortOctober),
            11L to context.getString(R.string.monthShortNovember),
            12L to context.getString(R.string.monthShortDecember)
    )

    return format(DateTimeFormatterBuilder().parseCaseInsensitive().parseLenient().appendText(ChronoField.MONTH_OF_YEAR, monthsOfTheYear).appendLiteral(' ').appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE).toFormatter().withResolverStyle(ResolverStyle.SMART))
}

fun OffsetDateTime.getTime(context: Context): String {
    val amPm = hashMapOf(
            0L to context.getString(R.string.lowercaseAM),
            1L to context.getString(R.string.lowercasePM)
    )
    return format(DateTimeFormatterBuilder().parseCaseInsensitive().parseLenient().appendValue(ChronoField.CLOCK_HOUR_OF_AMPM, 1, 2, SignStyle.NEVER).appendLiteral(':').appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendText(ChronoField.AMPM_OF_DAY, amPm).toFormatter().withResolverStyle(ResolverStyle.SMART))
}

