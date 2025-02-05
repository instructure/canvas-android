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

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CanvasContextPermission(
        @SerializedName(CREATE_DISCUSSION_TOPIC)
        val canCreateDiscussionTopic: Boolean = false,
        @SerializedName(MANAGE_GRADES)
        val manage_grades: Boolean = false,
        @SerializedName(SEND_MESSAGES)
        val send_messages: Boolean = false,
        @SerializedName(SEND_MESSAGES_ALL)
        val send_messages_all: Boolean = false,
        @SerializedName(VIEW_ALL_GRADES)
        val view_all_grades: Boolean = false,
        @SerializedName(VIEW_ANALYTICS)
        val view_analytics: Boolean = false,
        @SerializedName(BECOME_USER)
        val become_user: Boolean = false,
        @SerializedName(CAN_UPDATE_NAME)
        val canUpdateName: Boolean = false,
        @SerializedName(CAN_UPDATE_AVATAR)
        val canUpdateAvatar: Boolean = false,
        @SerializedName(CREATE_ANNOUNCEMENT)
        val canCreateAnnouncement: Boolean = false,
        @SerializedName(STUDENT_VIEW)
        val canUseStudentView: Boolean = false,
        @SerializedName(MANAGE_CONTENT)
        val canManageContent: Boolean = false,
        @SerializedName(MANAGE_COURSE_CONTENT_EDIT)
        val canEditCourseContent: Boolean = false,
        @SerializedName(MANAGE_CALENDAR)
        val manageCalendar: Boolean = false,
        @SerializedName(LIMIT_PARENT_APP_WEB_ACCESS)
        val limitParentAppWebAccess: Boolean = false
) : Parcelable {
    companion object {
        const val BECOME_USER = "become_user"
        const val CAN_UPDATE_AVATAR = "can_update_avatar"
        const val CAN_UPDATE_NAME = "can_update_name"
        const val CREATE_ANNOUNCEMENT = "create_announcement"
        const val CREATE_DISCUSSION_TOPIC = "create_discussion_topic"
        const val DELETE = "delete"
        const val MANAGE_GRADES = "manage_grades"
        const val SEND_MESSAGES = "send_messages"
        const val SEND_MESSAGES_ALL = "send_messages_all"
        const val VIEW_ALL_GRADES = "view_all_grades"
        const val VIEW_ANALYTICS = "view_analytics"
        const val STUDENT_VIEW = "use_student_view"
        const val MANAGE_CONTENT = "manage_content"
        const val MANAGE_COURSE_CONTENT_EDIT = "manage_course_content_edit"
        const val MANAGE_CALENDAR = "manage_calendar"
        const val LIMIT_PARENT_APP_WEB_ACCESS = "limit_parent_app_web_access"
    }
}
