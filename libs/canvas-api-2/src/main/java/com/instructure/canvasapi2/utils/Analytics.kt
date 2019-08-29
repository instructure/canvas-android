
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
    fun createAnalyticsBundle(
            domain: String,
            userId: String,
            canvasContextId: String
    ) : Bundle {
        return Bundle().apply {
            putString(AnalyticsParamConstants.DOMAIN_PARAM, domain)
            putString(AnalyticsParamConstants.USER_CONTEXT_ID, userId)
            putString(AnalyticsParamConstants.CANVAS_CONTEXT_ID, canvasContextId)
        }
    }

    @JvmStatic
    fun createAssignmentAnalyticsBundle(
            domain: String,
            userId: String,
            canvasContextId: String,
            assignmentId: Long
    ): Bundle {
        return createAnalyticsBundle(domain, userId, canvasContextId).apply {
            putLong(AnalyticsParamConstants.ASSIGNMENT_ID, assignmentId)
        }
    }

}

/**
 * The naming scheme for the majority of these is found in a google doc so that we can be consistent
 * across the platforms.
 */
object AnalyticsEventConstants {
    const val AUTHENTICATION_DIALOG = "authentication_dialog"

    /* ASSIGNMENTS 6.6 */
    const val ASSIGNMENT_DETAIL_ASSIGNMENT = "assignment_detail_assignment"
    const val ASSIGNMENT_DETAIL_QUIZ = "assignment_detail_quiz"
    const val ASSIGNMENT_DETAIL_QUIZLAUNCH = "assignment_detail_quizlaunch"
    const val ASSIGNMENT_DETAIL_DISCUSSION = "assignment_detail_discussion"
    const val ASSIGNMENT_DETAIL_DISCUSSIONLAUNCH = "assignment_detail_discussionlaunch"
    const val ASSIGNMENT_SUBMIT_SELECTED = "assignment_submit_selected"
    const val ASSIGNMENT_LAUNCHLTI_SELECTED = "assignment_launchlti_selected"
    const val SUBMIT_FILEUPLOAD_SELECTED = "submit_fileupload_selected"
    const val SUBMIT_TEXTENTRY_SELECTED = "submit_textentry_selected"
    const val SUBMIT_MEDIARECORDING_SELECTED = "submit_mediarecording_selected"
    const val SUBMIT_ARC_SELECTED = "submit_arc_selected"
    const val SUBMISSION_CELL_SELECTED = "submission_cell_selected"
    const val SUBMISSION_COMMENTS_SELECTED = "assignment_submit_selected"
    const val SUBMISSION_COMMENTS_TEXT_REPLY = "submission_comments_text_reply"
    const val SUBMISSION_COMMENTS_MEDIA_REPLY = "submission_comments_media_reply"
    const val SUBMISSION_RUBRIC_SELECTED = "submission_rubric_selected"
    const val SUBMISSION_FILES_SELECTED = "submission_files_selected"
    const val SUBMISSION_ANNOTATION_SELECTED = "SUBMISSION_RUBRIC_SELECTED"
    const val SUBMIT_FILEUPLOAD_FAILED = "submit_fileupload_failed"
    const val SUBMIT_FILEUPLOAD_SUCCEEDED = "submit_fileupload_succeeded"
    const val UNSUPPORTED_SUBMISSION_CONTENT = "unsupported_submission_content"

}

object AnalyticsParamConstants {
    const val DOMAIN_PARAM = "domain"
    const val USER_CONTEXT_ID = "user_context_id"
    const val CANVAS_CONTEXT_ID = "canvas_context_id"
    const val ASSIGNMENT_ID = "assignment_id"
}