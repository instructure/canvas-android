/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.canvasapi2.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class PlannerItem (
    @SerializedName("course_id")
    val courseId: Long?,

    @SerializedName("group_id")
    val groupId: Long?,

    @SerializedName("user_id")
    val userId: Long?,

    @SerializedName("context_type")
    val contextType: String?,

    @SerializedName("context_name")
    val contextName: String?,

    @SerializedName("plannable_type")
    val plannableType: PlannableType,

    val plannable: Plannable,

    @SerializedName("plannable_date")
    val plannableDate: Date,

    @SerializedName("html_url")
    val htmlUrl: String?,

    @SerializedName("submissions")
    val submissionState: SubmissionState?,

    @SerializedName("new_activity")
    val newActivity: Boolean?,

    @SerializedName("planner_override")
    var plannerOverride: PlannerOverride? = null,

    @SerializedName("details")
    val plannableItemDetails: PlannerItemDetails? = null,

    var isChecked: Boolean = false
): Parcelable, CanvasComparable<PlannerItem>() {

    val canvasContext: CanvasContext
        get() {
            courseId?.let { return Course(id = it) }
            groupId?.let { return Group(id = it) }
            userId?.let { return User(id = it) }
            plannable.courseId?.let { return Course(id = it) }
            plannable.groupId?.let { return Group(id = it) }
            plannable.userId?.let { return User(id = it) }
            return CanvasContext.defaultCanvasContext()
        }

    override val id: Long
        get() = plannable.id
    override val comparisonDate: Date
        get() = plannableDate
    override val comparisonString: String
        get() = "${plannable.title}${plannable.subAssignmentTag}"

}

enum class PlannableType {
    @SerializedName("announcement")
    ANNOUNCEMENT,
    @SerializedName("assignment")
    ASSIGNMENT,
    @SerializedName("sub_assignment")
    SUB_ASSIGNMENT,
    @SerializedName("discussion_topic")
    DISCUSSION_TOPIC,
    @SerializedName("quiz")
    QUIZ,
    @SerializedName("wiki_page")
    WIKI_PAGE,
    @SerializedName("planner_note")
    PLANNER_NOTE,
    @SerializedName("calendar_event")
    CALENDAR_EVENT,
    @SerializedName("todo")
    TODO,
    @SerializedName("assessment_request")
    ASSESSMENT_REQUEST
}

@Parcelize
data class PlannerItemDetails(
    @SerializedName("reply_to_entry_required_count")
    val replyRequiredCount: Int?
): Parcelable
