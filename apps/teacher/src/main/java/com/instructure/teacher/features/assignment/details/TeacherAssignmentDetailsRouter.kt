package com.instructure.teacher.features.assignment.details

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.RemoteFile
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsRouter

class TeacherAssignmentDetailsRouter: AssignmentDetailsRouter {
    override fun navigateToAssignmentUploadPicker(
        context: Context,
        canvasContext: CanvasContext,
        assignment: Assignment,
        mediaUri: Uri
    ) {
        TODO("Not yet implemented")
    }

    override fun navigateToLtiScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext?,
        url: String
    ) {
        TODO("Not yet implemented")
    }

    override fun navigateToSubmissionScreen(
        context: Context,
        course: CanvasContext,
        assignmentId: Long,
        isObserver: Boolean,
        initialSelectedSubmissionAttempt: Long?
    ) {
        TODO("Not yet implemented")
    }

    override fun navigateToQuizScreen(
        context: Context,
        canvasContext: CanvasContext,
        quiz: Quiz,
        url: String
    ) {
        TODO("Not yet implemented")
    }

    override fun navigateToDiscussionScreen(
        context: Context,
        canvasContext: CanvasContext,
        discussionTopicHeaderId: Long,
        isAnnouncement: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun navigateToUploadScreen(
        context: Context,
        canvasContext: CanvasContext,
        assignment: Assignment,
        attemptId: Long?
    ) {
        TODO("Not yet implemented")
    }

    override fun navigateToTextEntryScreen(
        context: Context,
        course: CanvasContext,
        assignmentId: Long,
        assignmentName: String?,
        initialText: String?,
        isFailure: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun navigateToUrlSubmissionScreen(
        context: Context,
        course: CanvasContext,
        assignmentId: Long,
        assignmentName: String?,
        initialUrl: String?,
        isFailure: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun navigateToAnnotationSubmissionScreen(
        context: Context,
        canvasContext: CanvasContext,
        annotatableAttachmentId: Long,
        submissionId: Long,
        assignmentId: Long,
        assignmentName: String
    ) {
        TODO("Not yet implemented")
    }

    override fun navigateToLtiLaunchScreen(
        context: Context,
        canvasContext: CanvasContext,
        url: String,
        title: String?,
        sessionLessLaunch: Boolean,
        isAssignmentLTI: Boolean,
        ltiTool: LTITool?
    ) {
        TODO("Not yet implemented")
    }

    override fun navigateToUploadStatusScreen(context: Context, submissionId: Long) {
        TODO("Not yet implemented")
    }

    override fun navigateToDiscussionAttachmentScreen(context: Context, attachment: RemoteFile) {
        TODO("Not yet implemented")
    }

    override fun navigateToUrl(
        activity: FragmentActivity,
        url: String,
        domain: String,
        extras: Bundle?
    ) {
        TODO("Not yet implemented")
    }

    override fun navigateToInternalWebView(
        context: Context,
        canvasContext: CanvasContext,
        url: String,
        authenticate: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun openMedia(context: Context, url: String) {
        TODO("Not yet implemented")
    }

    override fun showMediaDialog(context: Context) {
        TODO("Not yet implemented")
    }

    override fun showSubmitDialog(
        context: Context,
        assignment: Assignment,
        studioLTITool: LTITool?
    ) {
        TODO("Not yet implemented")
    }

    override fun showCustomReminderDialog(context: Context) {
        TODO("Not yet implemented")
    }

    override fun showDeleteReminderConfirmationDialog(context: Context, onConfirmed: () -> Unit) {
        TODO("Not yet implemented")
    }

    override fun showCreateReminderDialog(context: Context) {
        TODO("Not yet implemented")
    }

    override fun canRouteInternally(
        activity: FragmentActivity?,
        url: String?,
        domain: String,
        routeIfPossible: Boolean
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun applyTheme() {
        TODO("Not yet implemented")
    }
}