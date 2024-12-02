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
import com.instructure.canvasapi2.models.Assignment.SubmissionType
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.toSimpleDate
import com.instructure.pandares.R
import java.util.Date

val ScheduleItem.iconRes: Int
    get() {
        return when {
            this.type == "event" -> R.drawable.ic_calendar
            this.assignment?.lockedForUser.orDefault() -> R.drawable.ic_lock
            this.assignment?.getSubmissionTypes()?.contains(SubmissionType.ONLINE_QUIZ).orDefault() -> R.drawable.ic_quiz
            this.assignment?.getSubmissionTypes()?.contains(SubmissionType.DISCUSSION_TOPIC).orDefault() -> R.drawable.ic_discussion
            else -> R.drawable.ic_assignment
        }
    }

fun ScheduleItem.getDisplayDate(context: Context): String {
        val date: Date? = if (this.isAllDay) {
            this.allDayDate ?: this.startAt.toSimpleDate()
        } else {
            this.startAt.toSimpleDate() ?: this.allDayDate
        }

        return date?.toFormattedString()
            ?: context.getString(com.instructure.pandautils.R.string.scheduleItemNoDueDate)
    }