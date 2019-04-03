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
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Alert(
        @SerializedName("id")
        val stringId: String = "",
        @SerializedName("marked_read")
        val markedRead: Boolean = false,
        val dismissed: Boolean = false,
        @SerializedName("alert_type")
        val alertType: AlertType? = null,
        val title: String? = null,
        @SerializedName("action_date")
        val actionAt: String? = null,
        @SerializedName("creation_date")
        val createdAt: String? = null,
        @SerializedName("observer_id")
        val observerId: String? = null,
        @SerializedName("student_id")
        val studentId: String? = null,
        @SerializedName("course_id")
        val courseId: String? = null,
        @SerializedName("alert_criteria_id")
        val alertCriteriaId: String? = null,
        @SerializedName("asset_url")
        val assetUrl: String? = null
) : CanvasModel<Alert>() {

    val actionDate get() = actionAt.toDate()
    val creationDate get() = createdAt.toDate()

    enum class AlertType(val apiString: String?) {
        @SerializedName("course_announcement") COURSE_ANNOUNCEMENT("course_announcement"),
        @SerializedName("institution_announcement") INSTITUTION_ANNOUNCEMENT("institution_announcement"),
        @SerializedName("assignment_grade_high") ASSIGNMENT_GRADE_HIGH("assignment_grade_high"),
        @SerializedName("assignment_grade_low") ASSIGNMENT_GRADE_LOW("assignment_grade_low"),
        @SerializedName("assignment_missing") ASSIGNMENT_MISSING("assignment_missing"),
        @SerializedName("course_grade_high") COURSE_GRADE_HIGH("course_grade_high"),
        @SerializedName("course_grade_low") COURSE_GRADE_LOW("course_grade_low"),
        NONE(null)
    }

    override val id get() = stringId.hashCode().toLong()

    companion object {
        fun getAlertTypeFromString(alertType: String?): AlertType? = when (alertType) {
            AlertType.COURSE_ANNOUNCEMENT.apiString -> AlertType.COURSE_ANNOUNCEMENT
            AlertType.INSTITUTION_ANNOUNCEMENT.apiString -> AlertType.INSTITUTION_ANNOUNCEMENT
            AlertType.ASSIGNMENT_GRADE_HIGH.apiString -> AlertType.ASSIGNMENT_GRADE_HIGH
            AlertType.ASSIGNMENT_GRADE_LOW.apiString -> AlertType.ASSIGNMENT_GRADE_LOW
            AlertType.ASSIGNMENT_MISSING.apiString -> AlertType.ASSIGNMENT_MISSING
            AlertType.COURSE_GRADE_HIGH.apiString -> AlertType.COURSE_GRADE_HIGH
            AlertType.COURSE_GRADE_LOW.apiString -> AlertType.COURSE_GRADE_LOW
            else -> null
        }

        fun alertTypeToAPIString(alertType: AlertType?): String? = when (alertType) {
            Alert.AlertType.COURSE_ANNOUNCEMENT -> AlertType.COURSE_ANNOUNCEMENT.apiString
            Alert.AlertType.INSTITUTION_ANNOUNCEMENT -> AlertType.INSTITUTION_ANNOUNCEMENT.apiString
            Alert.AlertType.ASSIGNMENT_GRADE_HIGH -> AlertType.ASSIGNMENT_GRADE_HIGH.apiString
            Alert.AlertType.ASSIGNMENT_GRADE_LOW -> AlertType.ASSIGNMENT_GRADE_LOW.apiString
            Alert.AlertType.ASSIGNMENT_MISSING -> AlertType.ASSIGNMENT_MISSING.apiString
            Alert.AlertType.COURSE_GRADE_HIGH -> AlertType.COURSE_GRADE_HIGH.apiString
            Alert.AlertType.COURSE_GRADE_LOW -> AlertType.COURSE_GRADE_LOW.apiString
            else -> null
        }
    }
}