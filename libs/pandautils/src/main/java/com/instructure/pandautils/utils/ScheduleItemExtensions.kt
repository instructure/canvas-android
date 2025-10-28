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
import android.net.Uri
import com.instructure.canvasapi2.models.Assignment.SubmissionType
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.toSimpleDate
import com.instructure.pandares.R
import java.util.Date

val ScheduleItem.iconRes: Int
    get() {
        val isClassicQuiz = this.assignment?.getSubmissionTypes()?.contains(SubmissionType.ONLINE_QUIZ).orDefault()
        val isNewQuiz = this.assignment?.getSubmissionTypes()?.contains(SubmissionType.EXTERNAL_TOOL).orDefault() &&
            this.assignment?.externalToolAttributes?.url?.contains("quiz-lti").orDefault()

        return when {
            this.type == "event" -> R.drawable.ic_calendar
            isClassicQuiz && this.assignment?.isLocked.orDefault() -> R.drawable.ic_lock  // For classic quizzes, use isLocked instead of lockedForUser (classic quizzes always return lockedForUser=true from API)
            isClassicQuiz -> R.drawable.ic_quiz
            this.assignment?.lockedForUser.orDefault() -> R.drawable.ic_lock
            isNewQuiz -> R.drawable.ic_quiz
            this.assignment?.getSubmissionTypes()?.contains(SubmissionType.DISCUSSION_TOPIC).orDefault() -> R.drawable.ic_discussion
            else -> R.drawable.ic_assignment
        }
    }

val ScheduleItem.contentDescriptionRes: Int
    get() {
        val isClassicQuiz = this.assignment?.getSubmissionTypes()?.contains(SubmissionType.ONLINE_QUIZ).orDefault()
        val isNewQuiz = this.assignment?.getSubmissionTypes()?.contains(SubmissionType.EXTERNAL_TOOL).orDefault() &&
            this.assignment?.externalToolAttributes?.url?.contains("quiz-lti").orDefault()

        return when {
            this.type == "event" -> R.string.a11y_summaryEventContentDescription
            isClassicQuiz && this.assignment?.isLocked.orDefault() -> R.string.a11y_summaryLockedContentDescription // For classic quizzes, use isLocked instead of lockedForUser (classic quizzes always return lockedForUser=true from API)
            isClassicQuiz -> R.string.a11y_summaryQuizContentDescription
            this.assignment?.lockedForUser.orDefault() -> R.string.a11y_summaryLockedContentDescription
            isNewQuiz -> R.string.a11y_summaryQuizContentDescription
            this.assignment?.getSubmissionTypes()?.contains(SubmissionType.DISCUSSION_TOPIC).orDefault() -> R.string.a11y_summaryDiscussionContentDescription
            else -> R.string.a11y_summaryAssignmentContentDescription
        }
    }

fun ScheduleItem.getDisplayDate(context: Context): String {
    val date: Date? = if (this.isAllDay) {
        this.allDayDate ?: this.startAt.toSimpleDate()
    } else {
        this.startAt.toSimpleDate() ?: this.allDayDate
    }

    return date?.toFormattedString() ?: context.getString(com.instructure.pandautils.R.string.scheduleItemNoDueDate)
}

val ScheduleItem.dueAt: Date?
    get() {
        return if (this.isAllDay) {
            this.allDayDate ?: this.startAt.toSimpleDate()
        } else {
            this.startAt.toSimpleDate() ?: this.allDayDate
        }
    }

val ScheduleItem.eventHtmlUrl: String?
    get() {
        if (this.htmlUrl == null) return null

        val htmlUri = Uri.parse(this.htmlUrl)
        val eventId = htmlUri.getQueryParameter("event_id")

        return "${htmlUri.scheme}://${htmlUri.host}/${this.contextType?.apiString}/${this.contextId}/calendar_events/${eventId}"
    }