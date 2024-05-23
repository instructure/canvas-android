/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 *
 */

package com.instructure.canvasapi2.models

import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.utils.toDate
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.util.*

@Parcelize
data class ToDo(
        @SerializedName("start_date")
        val startAt: String? = "",
        val type: Type? = null,
        @SerializedName("needs_grading_count")
        val needsGradingCount: Int = 0,
        val ignore: String? = null,
        @SerializedName("ignore_permanently")
        val ignorePermanently: String? = null,
        @SerializedName("html_url")
        val htmlUrl: String? = null,
        @SerializedName("course_id")
        val courseId: Long = 0,
        @SerializedName("group_id")
        val groupId: Long = 0,

        // Helper variables - Here so they get parcelized
        var startDate: Date? = null,
        var canvasContext: CanvasContext? = null,
        var assignment: Assignment? = null,
        var quiz: Quiz? = null,
        var scheduleItem: ScheduleItem? = null,
        var isChecked: Boolean = false
) : CanvasComparable<ToDo>() {

    // Due date is more important if we have one
    override val comparisonDate: Date?
        get() {
            var date: Date? = Date(java.lang.Long.MAX_VALUE)
            if (startDate != null) {
                date = startDate
            }
            if (dueDate != null) {
                date = dueDate
            }
            return date
        }

    override val comparisonString get() = title
    override val id: Long get() = assignment?.id ?: quiz?.id ?: scheduleItem?.id ?: -1

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    val title: String?
        get() {
            if (assignment != null && assignment!!.name != null) {
                return assignment!!.name
            } else if (scheduleItem != null && scheduleItem!!.title != null) {
                return scheduleItem!!.title
            } else if (quiz != null && quiz!!.title != null) {
                return quiz!!.title
            }
            return ""
        }

    val dueDate: Date? get() = assignment?.dueDate ?: scheduleItem?.endDate ?: quiz?.dueDate

    enum class Type : Serializable {
        @SerializedName("submitting") Submitting,
        @SerializedName("grading") Grading,
        @SerializedName("upcoming_event") UpcomingEvent,
        UPCOMING_ASSIGNMENT
    }

    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

    fun startDate(): Date? {
        startDate = startAt.toDate() ?: startDate
        return startDate
    }

    // Upcoming assignment type doesn't come back and we have to check the assignment field to know
    // if that's the type or not - default to upcoming event
    val todoType: Type get() = when {
        type == Type.Submitting || type == Type.Grading -> type
        assignment != null -> Type.UPCOMING_ASSIGNMENT
        else -> Type.UpcomingEvent
    }

    companion object {
        fun setContextInfo(toDo: ToDo, courses: Map<Long, Course>, groups: Map<Long, Group>) {
            if (toDo.groupId > 0) {
                toDo.canvasContext = groups[toDo.groupId]
            } else {
                toDo.canvasContext = courses[toDo.courseId]
            }
        }

        fun toDoWithScheduleItem(scheduleItem: ScheduleItem): ToDo {
            val assignment = scheduleItem.assignment
            val type = if (assignment == null) Type.UpcomingEvent else Type.UPCOMING_ASSIGNMENT
            val courseId = if (scheduleItem.contextType == CanvasContext.Type.COURSE) scheduleItem.courseId else -1
            val groupId = if (scheduleItem.contextType == CanvasContext.Type.GROUP) scheduleItem.groupId else -1
            return ToDo(
                    assignment = assignment,
                    type = type,
                    scheduleItem = scheduleItem,
                    courseId = courseId,
                    groupId = groupId)
        }
    }
}
