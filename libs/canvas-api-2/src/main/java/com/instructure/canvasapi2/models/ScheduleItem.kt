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

import android.content.Context
import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.R
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.toDate
import com.instructure.canvasapi2.utils.toSimpleDate
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class ScheduleItem(
        @SerializedName("id")
        var itemId: String = "", // Can be different values - check the getId() override method below
        val title: String? = null,
        var description: String? = null,
        @SerializedName("start_at")
        val startAt: String? = null,
        @SerializedName("end_at")
        val endAt: String? = null,
        @SerializedName("all_day")
        val isAllDay: Boolean = false,
        @SerializedName("all_day_date")
        val allDayAt: String? = null,
        @SerializedName("location_address")
        val locationAddress: String? = null,
        @SerializedName("location_name")
        val locationName: String? = null,
        @SerializedName("html_url")
        val htmlUrl: String? = null,
        @SerializedName("context_code")
        val contextCode: String? = null,
        @SerializedName("effective_context_code")
        val effectiveContextCode: String? = null,
        @SerializedName("hidden")
        val isHidden: Boolean = false,
        @SerializedName("assignment_overrides")
        val assignmentOverrides: List<AssignmentOverride?>? = arrayListOf(),
        @SerializedName("important_dates")
        val importantDates: Boolean = false,
        val type: String = "",

        // Not API related - Included here so they get parcelized
        var submissionTypes: List<Assignment.SubmissionType> = ArrayList(),
        var pointsPossible: Double = 0.0,
        var quizId: Long = 0,
        var discussionTopicHeader: DiscussionTopicHeader? = null,
        var lockedModuleName: String? = null,
        var assignment: Assignment? = null,

        var userName: String? = null,
        var itemType: Type? = Type.TYPE_CALENDAR
) : CanvasModel<ScheduleItem>() {
    override val comparisonDate get() = startDate
    override val comparisonString get() = title

    val endDate get() = endAt.toDate()
    val allDayDate get() = allDayAt.toSimpleDate()

    @IgnoredOnParcel
    var contextType: CanvasContext.Type? = null
        get() {
            if (contextCode == null) {
                field = CanvasContext.Type.USER;
            } else if (field == null) {
                parseContextCode()
            }

            return field
        }

    @IgnoredOnParcel
    var userId: Long = -1
    get() {
        if (field < 0) {
            parseContextCode()
        }
        return field
    }

    @IgnoredOnParcel
    var courseId: Long = -1
        get() {
            if (field < 0) {
                parseContextCode()
            }
            return field
        }

    @IgnoredOnParcel
    var groupId: Long = -1
        get() {
            if (field < 0) {
                parseContextCode()
            }
            return field
        }

    @IgnoredOnParcel
    val startDate: Date? get() = startAt?.toDate()

    val contextId: Long
        get() {
            return when (contextType) {
                CanvasContext.Type.COURSE -> courseId
                CanvasContext.Type.GROUP -> groupId
                CanvasContext.Type.USER -> userId
                else -> -1
            }
        }


    enum class Type {
        TYPE_ASSIGNMENT, TYPE_CALENDAR, TYPE_SYLLABUS
    }

    override val id get(): Long {
        // id can either be a regular long, or it could be prefixed by "assignment_".
        // for more info check out the upcoming_events api documentation
        try {
            return itemId.toLong()
        } catch (e: NumberFormatException) {
            return if (assignmentOverrides != null && assignmentOverrides.isNotEmpty()) {
                assignmentOverrides[0]!!.id
            } else {
                // It's a string with assignment_ as a prefix...hopefully
                try {
                    val tempId = itemId.replace("assignment_", "")
                    itemId = tempId
                    itemId.toLong()
                } catch (e1: Exception) {
                    itemId = (-1L).toString()
                    -1L
                }

            }
        } catch (e: Exception) {
            itemId = (-1L).toString()
            return -1L
        }

    }

    fun getStartString(context: Context): String? = when {
        isAllDay -> context.getString(R.string.allDayEvent)
        else -> if (startDate != null) {
            DateHelper.createPrefixedDateString(context, R.string.Starts, startDate)
        } else ""
    }

    fun getStartDateString(context: Context): String? = when {
        isAllDay && allDayDate != null -> DateHelper.getFormattedDate(context, allDayDate)
        else -> if (startDate != null) {
            DateHelper.getFormattedDate(context, startDate)
        } else ""
    }

    fun getStartToEndString(context: Context): String? = when {
        isAllDay -> context.getString(R.string.allDayEvent)
        else -> if (startDate != null) {
            if (endDate != null && startDate != endDate) {
                DateHelper.getFormattedTime(context, startDate) + " " + context.resources.getString(R.string.to) + " " + DateHelper.getFormattedTime(context, endDate)
            } else DateHelper.getFormattedTime(context, startDate)
        } else ""
    }

    fun hasAssignmentOverrides(): Boolean = assignmentOverrides != null && !assignmentOverrides.isEmpty()

    private fun parseContextCode() {
        if (effectiveContextCode != null) {
            parseContextCode(effectiveContextCode)
        } else {
            parseContextCode(contextCode!!)
        }
    }

    private fun parseContextCode(contextCode: String) {
        when {
            contextCode.startsWith("user_") -> {
                contextType = CanvasContext.Type.USER
                val id = contextCode.replace("user_", "")
                userId = id.toLong()
            }
            contextCode.startsWith("course_") -> {
                contextType = CanvasContext.Type.COURSE
                val id = contextCode.replace("course_", "")
                courseId = id.toLong()
            }
            contextCode.startsWith("group_") -> {
                contextType = CanvasContext.Type.GROUP
                val id = contextCode.replace("group_", "")
                groupId = id.toLong()
            }
        }
    }

    companion object {
        fun createSyllabus(title: String?, description: String?): ScheduleItem =
                ScheduleItem(
                        itemType = Type.TYPE_SYLLABUS,
                        title = title,
                        description = description,
                        itemId = Long.MIN_VALUE.toString()
                )

        const val TYPE_EVENT = "event"
    }
}
