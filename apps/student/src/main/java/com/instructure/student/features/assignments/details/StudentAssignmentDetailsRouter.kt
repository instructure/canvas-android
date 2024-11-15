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
package com.instructure.student.features.assignments.details

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.RemoteFile
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsRouter
import com.instructure.pandautils.features.discussion.router.DiscussionRouterFragment
import com.instructure.pandautils.features.lti.LtiLaunchFragment
import com.instructure.student.activity.BaseRouterActivity
import com.instructure.student.fragment.BasicQuizViewFragment
import com.instructure.student.mobius.assignmentDetails.submission.annnotation.AnnotationSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.file.ui.UploadStatusSubmissionFragment
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionMode
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.text.ui.TextSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.url.ui.UrlSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsRepositoryFragment
import com.instructure.student.router.RouteMatcher

class StudentAssignmentDetailsRouter: AssignmentDetailsRouter() {
    override fun navigateToAssignmentUploadPicker(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        assignment: Assignment,
        mediaUri: Uri
    ) {
        RouteMatcher.route(
            activity,
            PickerSubmissionUploadFragment.makeRoute(canvasContext, assignment, mediaUri)
        )
    }

    override fun navigateToSubmissionScreen(
        activity: FragmentActivity,
        course: CanvasContext,
        assignmentId: Long,
        isObserver: Boolean,
        initialSelectedSubmissionAttempt: Long?
    ) {
        RouteMatcher.route(
            activity,
            SubmissionDetailsRepositoryFragment.makeRoute(course, assignmentId, isObserver, initialSelectedSubmissionAttempt)
        )
    }

    override fun navigateToQuizScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        quiz: Quiz,
        url: String
    ) {
        RouteMatcher.route(activity, BasicQuizViewFragment.makeRoute(canvasContext, quiz, url))
    }

    override fun navigateToDiscussionScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        discussionTopicHeaderId: Long,
        isAnnouncement: Boolean
    ) {
        RouteMatcher.route(activity, DiscussionRouterFragment.makeRoute(canvasContext, discussionTopicHeaderId, isAnnouncement))
    }

    override fun navigateToUploadScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        assignment: Assignment,
        attemptId: Long?
    ) {
        Analytics.logEvent(AnalyticsEventConstants.SUBMIT_FILEUPLOAD_SELECTED)
        RouteMatcher.route(
            activity,
            PickerSubmissionUploadFragment.makeRoute(canvasContext, assignment, PickerSubmissionMode.FileSubmission)
        )
    }

    override fun navigateToTextEntryScreen(
        activity: FragmentActivity,
        course: CanvasContext,
        assignmentId: Long,
        assignmentName: String?,
        initialText: String?,
        isFailure: Boolean
    ) {
        Analytics.logEvent(AnalyticsEventConstants.SUBMIT_TEXTENTRY_SELECTED)
        RouteMatcher.route(
            activity,
            TextSubmissionUploadFragment.makeRoute(course, assignmentId, assignmentName, initialText, isFailure)
        )
    }

    override fun navigateToUrlSubmissionScreen(
        activity: FragmentActivity,
        course: CanvasContext,
        assignmentId: Long,
        assignmentName: String?,
        initialUrl: String?,
        isFailure: Boolean
    ) {
        Analytics.logEvent(AnalyticsEventConstants.SUBMIT_ONLINEURL_SELECTED)
        RouteMatcher.route(
            activity,
            UrlSubmissionUploadFragment.makeRoute(course, assignmentId, assignmentName, initialUrl, isFailure)
        )
    }

    override fun navigateToAnnotationSubmissionScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        annotatableAttachmentId: Long,
        submissionId: Long,
        assignmentId: Long,
        assignmentName: String
    ) {
        Analytics.logEvent(AnalyticsEventConstants.SUBMIT_STUDENT_ANNOTATION_SELECTED)
        RouteMatcher.route(
            activity,
            AnnotationSubmissionUploadFragment.makeRoute(
                canvasContext,
                annotatableAttachmentId,
                submissionId,
                assignmentId,
                assignmentName
            )
        )
    }

    override fun navigateToLtiLaunchScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        url: String,
        title: String?,
        sessionLessLaunch: Boolean,
        isAssignmentLTI: Boolean,
        ltiTool: LTITool?,
        openInternally: Boolean
    ) {
        RouteMatcher.route(
            activity,
            LtiLaunchFragment.makeRoute(
                canvasContext,
                url,
                title,
                sessionLessLaunch = sessionLessLaunch,
                assignmentLti = isAssignmentLTI,
                ltiTool = ltiTool,
                openInternally = openInternally
            )
        )
    }

    override fun navigateToUploadStatusScreen(activity: FragmentActivity, submissionId: Long) {
        RouteMatcher.route(activity, UploadStatusSubmissionFragment.makeRoute(submissionId))
    }

    override fun navigateToDiscussionAttachmentScreen(activity: FragmentActivity, canvasContext: CanvasContext, attachment: RemoteFile) {
        (activity as? BaseRouterActivity)?.openMedia(
            canvasContext,
            attachment.contentType.orEmpty(),
            attachment.url.orEmpty(),
            attachment.fileName.orEmpty()
        )
    }

    override fun navigateToUrl(
        activity: FragmentActivity,
        url: String,
        domain: String,
        extras: Bundle?
    ) {
        RouteMatcher.routeUrl(activity, url, domain, extras)
    }
}