
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
import com.google.firebase.analytics.FirebaseAnalytics
import com.instructure.canvasapi2.utils.AnalyticsParamConstants.ASSIGNMENT_ID
import com.instructure.canvasapi2.utils.AnalyticsParamConstants.CANVAS_CONTEXT_ID
import com.instructure.canvasapi2.utils.AnalyticsParamConstants.DOMAIN_PARAM
import com.instructure.canvasapi2.utils.AnalyticsParamConstants.SCREEN_OF_ORIGIN
import com.instructure.canvasapi2.utils.AnalyticsParamConstants.USER_CONTEXT_ID

object Analytics {

    lateinit var firebase: FirebaseAnalytics

    @JvmStatic
    fun logEvent(eventName: String, bundle: Bundle? = null) {
        firebase.logEvent(eventName, bundle)
    }

    @JvmStatic
    fun logEvent(eventName: String) {
        firebase.logEvent(eventName, null)
    }

    @JvmStatic
    fun setUserProperty(propertyName: String, propertyValue: String) {
        firebase.setUserProperty(propertyName, propertyValue)
    }

    @JvmStatic
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
    const val ASSIGNMENT_SUBMIT_SELECTED = "assignment_submit_selected"
    const val ASSIGNMENT_LAUNCHLTI_SELECTED = "assignment_launchlti_selected"
    const val SUBMIT_FILEUPLOAD_SELECTED = "submit_fileupload_selected"
    const val SUBMIT_ONLINEURL_SELECTED = "submit_onlineurl_selected"
    const val SUBMIT_TEXTENTRY_SELECTED = "submit_textentry_selected"
    const val SUBMIT_MEDIARECORDING_SELECTED = "submit_mediarecording_selected"
    const val SUBMIT_STUDIO_SELECTED = "submit_studio_selected"
    const val SUBMISSION_CELL_SELECTED = "submission_cell_selected"
    const val SUBMISSION_COMMENTS_SELECTED = "submission_comments_selected"
    const val SUBMISSION_COMMENTS_TEXT_REPLY = "submission_comments_text_reply"
    const val SUBMISSION_COMMENTS_MEDIA_REPLY = "submission_comments_media_reply"
    const val SUBMISSION_RUBRIC_SELECTED = "submission_rubric_selected"
    const val SUBMISSION_FILES_SELECTED = "submission_files_selected"
    const val SUBMISSION_ANNOTATION_SELECTED = "submission_annotation_selected"
    const val SUBMIT_FILEUPLOAD_FAILED = "submit_fileupload_failed"
    const val SUBMIT_FILEUPLOAD_SUCCEEDED = "submit_fileupload_succeeded"
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

    const val WHAT_IF_GRADES = "what_if_grades_used"

    /* QR Code Login */
    const val QR_CODE_LOGIN_CLICKED = "qr_code_login_clicked"
    const val QR_CODE_LOGIN_SUCCESS = "qr_code_login_success"
    const val QR_CODE_LOGIN_FAILURE = "qr_code_login_failure"

    /* User Properties */
    const val USER_PROPERTY_BUILD_TYPE = "build_type"
    const val USER_PROPERTY_OS_VERSION = "os_version"
}

/**
 * PARAMS
 * Due to the limits on custom params, we will mostly be using a mapping of the pre-defined params,
 * mappings will be recorded below. Make sure we are only using params where the data is relevant.
 *
 * [DOMAIN_PARAM] -> AFFILIATION
 * [USER_CONTEXT_ID] -> CHARACTER
 * [CANVAS_CONTEXT_ID] -> GROUP_ID
 * [ASSIGNMENT_ID]/DISCUSSION/ETC ID -> ITEM_ID
 * There is also ITEM_CATEGORY if the event is vague regarding the type of item
 * [SCREEN_OF_ORIGIN] -> ORIGIN
 * Used when events can originate from multiple locations
 *
 */
object AnalyticsParamConstants {
    const val DOMAIN_PARAM = FirebaseAnalytics.Param.AFFILIATION
    const val USER_CONTEXT_ID = FirebaseAnalytics.Param.CHARACTER
    const val CANVAS_CONTEXT_ID = FirebaseAnalytics.Param.GROUP_ID
    const val ASSIGNMENT_ID = FirebaseAnalytics.Param.ITEM_ID
    const val SCREEN_OF_ORIGIN = FirebaseAnalytics.Param.ORIGIN
}
