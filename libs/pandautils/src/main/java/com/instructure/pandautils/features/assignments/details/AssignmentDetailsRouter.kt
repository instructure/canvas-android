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
package com.instructure.pandautils.features.assignments.details

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.RemoteFile
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions

open class AssignmentDetailsRouter {
    open fun navigateToAssignmentUploadPicker(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        assignment: Assignment,
        mediaUri: Uri,
        attempt: Long = 1L,
        mediaSource: String? = null
    ) = Unit

    open fun navigateToSubmissionScreen(
        activity: FragmentActivity,
        course: CanvasContext,
        assignmentId: Long,
        assignmentUrl: String?,
        isAssignmentEnhancementEnabled: Boolean,
        isObserver: Boolean = false,
        initialSelectedSubmissionAttempt: Long? = null,
        isQuiz: Boolean = false
    ) = Unit

    open fun navigateToQuizScreen(activity: FragmentActivity, canvasContext: CanvasContext, quiz: Quiz, url: String) = Unit

    open fun navigateToDiscussionScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        discussionTopicHeaderId: Long,
        isAnnouncement: Boolean = false
    ) = Unit

    open fun navigateToUploadScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        assignment: Assignment,
        attemptId: Long? = null,
        attempt: Long = 1L
    ) = Unit

    open fun navigateToTextEntryScreen(
        activity: FragmentActivity,
        course: CanvasContext,
        assignmentId: Long,
        assignmentName: String? = "",
        initialText: String? = null,
        isFailure: Boolean = false,
        attempt: Long = 1L
    ) = Unit

    open fun navigateToUrlSubmissionScreen(
        activity: FragmentActivity,
        course: CanvasContext,
        assignmentId: Long,
        assignmentName: String? = "",
        initialUrl: String?,
        isFailure: Boolean = false,
        attempt: Long = 1L
    ) = Unit

    open fun navigateToAnnotationSubmissionScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        annotatableAttachmentId: Long,
        submissionId: Long,
        assignmentId: Long,
        assignmentName: String,
        attempt: Long = 1L
    ) = Unit

    open fun navigateToLtiLaunchScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        url: String,
        title: String? = null,
        sessionLessLaunch: Boolean = false,
        isAssignmentLTI: Boolean = false,
        ltiTool: LTITool? = null,
        openInternally: Boolean = false
    ) = Unit

    open fun navigateToUploadStatusScreen(activity: FragmentActivity, submissionId: Long) = Unit

    open fun navigateToDiscussionAttachmentScreen(activity: FragmentActivity, canvasContext: CanvasContext, attachment: RemoteFile) = Unit

    open fun navigateToUrl(activity: FragmentActivity, url: String, domain: String, extras: Bundle? = null) = Unit

    open fun navigateToSendMessage(activity: FragmentActivity, options: InboxComposeOptions) = Unit
}