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
import androidx.annotation.DrawableRes
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.toDate
import com.instructure.pandautils.R

fun PlannerItem.todoHtmlUrl(apiPrefs: ApiPrefs): String {
    return "${apiPrefs.fullDomain}/todos/${this.plannable.id}"
}

fun PlannerItem.getUrl(apiPrefs: ApiPrefs): String {
    val url = when (plannableType) {
        PlannableType.CALENDAR_EVENT -> {
            "/${canvasContext.type.apiString}/${canvasContext.id}/calendar_events/${plannable.id}"
        }

        PlannableType.PLANNER_NOTE -> {
            "/todos/${plannable.id}"
        }

        else -> {
            htmlUrl.orEmpty()
        }
    }

    return if (url.startsWith("/")) {
        apiPrefs.fullDomain + url
    } else {
        url
    }
}

@DrawableRes
fun PlannerItem.getIconForPlannerItem(): Int {
    return when (this.plannableType) {
        PlannableType.ASSIGNMENT -> R.drawable.ic_assignment
        PlannableType.QUIZ -> R.drawable.ic_quiz
        PlannableType.CALENDAR_EVENT -> R.drawable.ic_calendar
        PlannableType.DISCUSSION_TOPIC, PlannableType.SUB_ASSIGNMENT -> R.drawable.ic_discussion
        PlannableType.PLANNER_NOTE -> R.drawable.ic_todo
        else -> R.drawable.ic_calendar
    }
}

fun PlannerItem.getDateTextForPlannerItem(context: Context): String? {
    return when (plannableType) {
        PlannableType.PLANNER_NOTE -> {
            plannable.todoDate.toDate()?.let {
                DateHelper.getFormattedTime(context, it)
            }
        }

        PlannableType.CALENDAR_EVENT -> {
            val startDate = plannable.startAt
            val endDate = plannable.endAt
            if (startDate != null && endDate != null) {
                val startText = DateHelper.getFormattedTime(context, startDate).orEmpty()
                val endText = DateHelper.getFormattedTime(context, endDate).orEmpty()

                when {
                    plannable.allDay == true -> context.getString(R.string.widgetAllDay)
                    startDate == endDate -> startText
                    else -> context.getString(R.string.widgetFromTo, startText, endText)
                }
            } else null
        }

        else -> {
            plannable.dueAt?.let {
                val timeText = DateHelper.getFormattedTime(context, it).orEmpty()
                context.getString(R.string.widgetDueDate, timeText)
            }
        }
    }
}

fun PlannerItem.getContextNameForPlannerItem(context: Context, courses: Collection<Course>): String {
    val course = courses.find { it.id == canvasContext.id }
    val hasNickname = course?.originalName != null
    val courseTitle = if (hasNickname) course.name else course?.courseCode
    return when (plannableType) {
        PlannableType.PLANNER_NOTE -> {
            if (contextName.isNullOrEmpty()) {
                context.getString(R.string.userCalendarToDo)
            } else {
                context.getString(R.string.courseToDo, courseTitle ?: contextName)
            }
        }

        else -> {
            if (canvasContext is Course) {
                courseTitle.orEmpty()
            } else {
                contextName.orEmpty()
            }
        }
    }
}

fun PlannerItem.getTagForPlannerItem(context: Context): String? {
    return if (plannable.subAssignmentTag == Const.REPLY_TO_TOPIC) {
        context.getString(R.string.reply_to_topic)
    } else if (plannable.subAssignmentTag == Const.REPLY_TO_ENTRY && plannableItemDetails?.replyRequiredCount != null) {
        context.getString(
            R.string.additional_replies,
            plannableItemDetails?.replyRequiredCount
        )
    } else {
        null
    }
}

fun PlannerItem.isComplete(): Boolean {
    return plannerOverride?.markedComplete ?: if (plannableType == PlannableType.ASSIGNMENT
        || plannableType == PlannableType.DISCUSSION_TOPIC
        || plannableType == PlannableType.SUB_ASSIGNMENT
    ) {
        submissionState?.submitted == true
    } else {
        false
    }
}
