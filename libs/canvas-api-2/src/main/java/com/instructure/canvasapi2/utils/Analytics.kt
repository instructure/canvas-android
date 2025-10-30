
/*
 * Copyright (C) 2019g - present Instructure, Inc.
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
package com.instructure.canvasapi2.utils

import android.os.Bundle
import sdk.pendo.io.Pendo

object Analytics {

    fun logEvent(eventName: String, bundle: Bundle? = null) {
        val map = bundle?.let { bundle ->
            bundle.keySet()
                .filterNotNull()
                .filter { it.isNotBlank() && it.isNotEmpty() }
                .associateWith {
                bundle.getString(it).orEmpty()
            }
        } ?: emptyMap()
        Pendo.track(eventName, map)
    }

    fun logEvent(eventName: String) {
        Pendo.track(eventName, emptyMap())
    }

    fun isSessionActive(): Boolean {
        return !Pendo.getAccountId().isNullOrEmpty()
    }

    fun setUserProperty(propertyName: String, propertyValue: String) {

    }

    fun createOriginBundle(origin: String): Bundle {
        return Bundle().apply {
            putString(AnalyticsParamConstants.SCREEN_OF_ORIGIN, origin)
        }
    }
}

/**
 * The naming scheme for the majority of these is found in a google doc so that we can be consistent
 * across the platforms.
 */
object AnalyticsEventConstants {
    const val AUTHENTICATION_DIALOG = "authentication_dialog"

    /* (STUDENT) ASSIGNMENTS 6.6 */
    const val ASSIGNMENT_DETAIL_ASSIGNMENT = "assignment_detail_assignment"
    const val ASSIGNMENT_DETAIL_QUIZ = "assignment_detail_quiz"
    const val ASSIGNMENT_DETAIL_QUIZLAUNCH = "assignment_detail_quizlaunch"
    const val ASSIGNMENT_DETAIL_DISCUSSION = "assignment_detail_discussion"
    const val ASSIGNMENT_DETAIL_DISCUSSIONLAUNCH = "assignment_detail_discussionlaunch"
    const val ASSIGNMENT_LAUNCHLTI_SELECTED = "assignment_launchlti_selected"
    const val ASSIGNMENT_LIST_SORT_BY_TIME_SELECTED = "assignment_list_sort_by_time_selected"
    const val ASSIGNMENT_LIST_SORT_BY_TYPE_SELECTED = "assignment_list_sort_by_type_selected"
    const val ASSIGNMENT_SUBMIT_SELECTED = "assignment_submit_selected"
    const val SUBMIT_FILEUPLOAD_SELECTED = "submit_fileupload_selected"
    const val SUBMIT_URL_SELECTED = "submit_url_selected"
    const val SUBMIT_TEXTENTRY_SELECTED = "submit_textentry_selected"
    const val SUBMIT_MEDIARECORDING_SELECTED = "submit_mediarecording_selected"
    const val SUBMIT_STUDIO_SELECTED = "submit_studio_selected"
    const val SUBMIT_ANNOTATION_SELECTED = "submit_annotation_selected"
    const val SUBMISSION_CELL_SELECTED = "submission_cell_selected"
    const val SUBMISSION_COMMENTS_SELECTED = "submission_comments_selected"
    const val SUBMISSION_COMMENTS_TEXT_REPLY = "submission_comments_text_reply"
    const val SUBMISSION_COMMENTS_MEDIA_REPLY = "submission_comments_media_reply"
    const val SUBMISSION_RUBRIC_SELECTED = "submission_rubric_selected"
    const val SUBMISSION_FILES_SELECTED = "submission_files_selected"
    const val SUBMISSION_ANNOTATION_SELECTED = "submission_annotation_selected"
    const val SUBMIT_FILEUPLOAD_FAILED = "submit_fileupload_failed"
    const val SUBMIT_FILEUPLOAD_SUCCEEDED = "submit_fileupload_succeeded"
    const val SUBMIT_TEXTENTRY_SUCCEEDED = "submit_textentry_succeeded"
    const val SUBMIT_TEXTENTRY_FAILED = "submit_textentry_failed"
    const val SUBMIT_URL_SUCCEEDED = "submit_url_succeeded"
    const val SUBMIT_URL_FAILED = "submit_url_failed"
    const val SUBMIT_ANNOTATION_PRESENTED = "submit_annotation_presented"
    const val SUBMIT_ANNOTATION_SUCCEEDED = "submit_annotation_succeeded"
    const val SUBMIT_ANNOTATION_FAILED = "submit_annotation_failed"
    const val SUBMIT_STUDIO_SUCCEEDED = "submit_studio_succeeded"
    const val SUBMIT_STUDIO_FAILED = "submit_studio_failed"
    const val SUBMIT_MEDIARECORDING_SUCCEEDED = "submit_mediarecording_succeeded"
    const val SUBMIT_MEDIARECORDING_FAILED = "submit_mediarecording_failed"
    const val UNSUPPORTED_SUBMISSION_CONTENT = "unsupported_submission_content"

    /* Panda Avatar */
    const val PANDA_AVATAR_EDITOR_OPENED = "panda_avatar_editor_opened"
    const val PANDA_AVATAR_SAVED = "panda_avatar_saved"
    const val PANDA_AVATAR_SET_AS_AVATAR = "panda_avatar_set_as_avatar"
    const val PANDA_AVATAR_SHARED = "panda_avatar_shared"

    /* Refresh Token Events */
    const val REFRESH_TOKEN = "refresh_token"
    const val FOREVER_TOKEN = "forever_token"
    const val LOGIN_FAILURE = "login_failure"
    const val LOGIN_SUCCESS = "login_success"
    const val TOKEN_REFRESH_FAILURE = "token_refresh_failure"
    const val TOKEN_REFRESH_FAILURE_TOKEN_NOT_VALID = "token_refresh_failure_token_not_valid"
    const val TOKEN_REFRESH_FAILURE_NO_SECRET = "token_refresh_failure_no_secret"

    /* Misc Events */
    const val REFRESH_PAIRING_CODE = "refresh_pairing_code"
    const val STUDENT_VIEW_TAPPED = "student_view_clicked"

    const val WHAT_IF_GRADES = "what_if_grades_used"

    const val CHANGED_C4E_MODE = "c4e_changed"

    /* QR Code Login */
    const val QR_CODE_LOGIN_CLICKED = "qr_code_login_clicked"
    const val QR_CODE_LOGIN_SUCCESS = "qr_code_login_success"
    const val QR_CODE_LOGIN_FAILURE = "qr_code_login_failure"

    /* User Properties */
    const val USER_PROPERTY_BUILD_TYPE = "build_type"
    const val USER_PROPERTY_OS_VERSION = "os_version"

    /* Offline usage properties */
    const val OFFLINE_SYNC_BUTTON_TAPPED = "offline_sync_button_tapped"
    const val OFFLINE_AUTO_SYNC_TURNED_ON = "offline_auto_sync_turned_on"
    const val OFFLINE_AUTO_SYNC_TURNED_OFF = "offline_auto_sync_turned_off"
    const val OFFLINE_COURSE_OPENED_OFFLINE_ENABLED = "offline_course_opened_offline_enabled"
    const val OFFLINE_COURSE_OPENED_OFFLINE_NOT_ENABLED = "offline_course_opened_offline_not_enabled"
    const val OFFLINE_DURATION_OFFLINE_ENABLED = "offline_duration_offline_enabled"
    const val OFFLINE_DURATION_OFFLINE_NOT_ENABLED = "offline_duration_offline_not_enabled"

    /* Parent */
    const val ADD_STUDENT_DASHBOARD = "add_student_dashboard"
    const val ADD_STUDENT_FAILURE = "add_student_failure"
    const val ADD_STUDENT_MANAGE_STUDENTS = "add_student_manage_students"
    const val ADD_STUDENT_SUCCESS = "add_student_success"
    const val DARK_MODE_OFF = "dark_mode_off"
    const val DARK_MODE_ON = "dark_mode_on"
    const val DARK_MODE_SYSTEM = "dark_mode_system"
    const val LOGOUT = "logout"
    const val SWITCH_USERS = "switch_users"
    const val RATING_DIALOG = "rating_dialog"
    const val RATING_DIALOG_SHOW = "rating_dialog_show"
    const val RATING_DIALOG_DONT_SHOW_AGAIN = "rating_dialog_dont_show_again"
    const val REMINDER_ASSIGNMENT_CREATE = "reminder_assignment"
    const val REMINDER_EVENT_CREATE = "reminder_event"
    const val SUBMISSION_AND_RUBRIC_INTERACTION = "submission_and_rubric_interaction"

    /* Widget */
    const val WIDGET_TODO_WIDGET_ADDED = "widget_todo_added"
    const val WIDGET_TODO_WIDGET_DELETED = "widget_todo_deleted"
    const val WIDGET_TODO_CREATE_ACTION = "widget_todo_create_action"
    const val WIDGET_TODO_OPEN_ITEM_ACTION = "widget_todo_open_item_action"
    const val WIDGET_TODO_OPEN_TODOS_ACTION = "widget_todo_open_todos_action"
    const val WIDGET_TODO_REFRESH_ACTION = "widget_todo_refresh_action"
    const val WIDGET_GRADES_WIDGET_ADDED = "widget_grades_added"
    const val WIDGET_GRADES_WIDGET_DELETED = "widget_grades_deleted"
    const val WIDGET_GRADES_OPEN_ITEM_ACTION = "widget_grades_open_item_action"
    const val WIDGET_GRADES_OPEN_APP_ACTION = "widget_grades_open_app_action"
    const val WIDGET_SINGLE_GRADE_WIDGET_ADDED = "widget_single_grade_added"
    const val WIDGET_SINGLE_GRADE_WIDGET_DELETED = "widget_single_grade_deleted"
    const val WIDGET_SINGLE_GRADE_OPEN_ITEM_ACTION = "widget_single_grade_open_item_action"
    const val WIDGET_SINGLE_GRADE_OPEN_APP_ACTION = "widget_single_grade_open_app_action"
}

/**
 * If other analytics platforms support custom params we can use these with any name, if not we can have a similiar soultion as before.
 */
object AnalyticsParamConstants {
    const val DOMAIN_PARAM = ""
    const val USER_CONTEXT_ID = ""
    const val CANVAS_CONTEXT_ID = ""
    const val ASSIGNMENT_ID = ""
    const val SCREEN_OF_ORIGIN = ""

    //custom
    const val MANUAL_C4E_STATE = "manual_c4e_state"
    const val DURATION = "duration"
    const val STAR_RATING = "star_rating"
    const val MEDIA_SOURCE = "media_source"
    const val MEDIA_TYPE = "media_type"
}
