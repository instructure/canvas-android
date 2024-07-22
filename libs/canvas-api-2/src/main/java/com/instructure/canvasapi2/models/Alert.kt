/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Alert(
    val id: Long,
    @SerializedName("observer_alert_threshold_id")
    val observerAlertThresholdId: Long,
    @SerializedName("context_type")
    val contextType: String,
    @SerializedName("context_id")
    val contextId: Long,
    @SerializedName("alert_type")
    val alertType: AlertType,
    @SerializedName("workflow_state")
    val workflowState: AlertWorkflowState,
    @SerializedName("action_date")
    val actionDate: Date?,
    val title: String,
    @SerializedName("user_id")
    val userId: Long,
    @SerializedName("observer_id")
    val observerId: Long,
    @SerializedName("html_url")
    val htmlUrl: String?,
    @SerializedName("locked_for_user")
    val lockedForUser: Boolean
)

enum class AlertType {
    @SerializedName("assignment_missing")
    ASSIGNMENT_MISSING,
    @SerializedName("assignment_grade_high")
    ASSIGNMENT_GRADE_HIGH,
    @SerializedName("assignment_grade_low")
    ASSIGNMENT_GRADE_LOW,
    @SerializedName("course_grade_high")
    COURSE_GRADE_HIGH,
    @SerializedName("course_grade_low")
    COURSE_GRADE_LOW,
    @SerializedName("course_announcement")
    COURSE_ANNOUNCEMENT,
    @SerializedName("institution_announcement")
    INSTITUTION_ANNOUNCEMENT;

    fun isAlertInfo() = listOf(INSTITUTION_ANNOUNCEMENT, COURSE_ANNOUNCEMENT).contains(this)

    fun isAlertPositive() = listOf(COURSE_GRADE_HIGH, ASSIGNMENT_GRADE_HIGH).contains(this)

    fun isAlertNegative() = listOf(ASSIGNMENT_MISSING, ASSIGNMENT_GRADE_LOW, COURSE_GRADE_LOW).contains(this)
}

enum class AlertWorkflowState {
    @SerializedName("unread")
    UNREAD,
    @SerializedName("read")
    READ,
    @SerializedName("deleted")
    DELETED,
    @SerializedName("dismissed")
    DISMISSED
}