package com.instructure.pandautils.features.assignments.details

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.RemoteFile

interface AssignmentDetailsRouter {
    fun navigateToAssignmentUploadPicker(context: Context, canvasContext: CanvasContext, assignment: Assignment, mediaUri: Uri)

    fun navigateToLtiScreen(activity: FragmentActivity, canvasContext: CanvasContext?, url: String)

    fun navigateToSubmissionScreen(context: Context, course: CanvasContext, assignmentId: Long, isObserver: Boolean = false, initialSelectedSubmissionAttempt: Long? = null)

    fun navigateToQuizScreen(context: Context, canvasContext: CanvasContext, quiz: Quiz, url: String)

    fun navigateToDiscussionScreen(context: Context, canvasContext: CanvasContext, discussionTopicHeaderId: Long, isAnnouncement: Boolean = false)

    fun navigateToUploadScreen(context: Context, canvasContext: CanvasContext, assignment: Assignment, attemptId: Long? = null)

    fun navigateToTextEntryScreen(context: Context, course: CanvasContext, assignmentId: Long, assignmentName: String? = "", initialText: String? = null, isFailure: Boolean = false)

    fun navigateToUrlSubmissionScreen(context: Context, course: CanvasContext, assignmentId: Long, assignmentName: String? = "", initialUrl: String?, isFailure: Boolean = false)

    fun navigateToAnnotationSubmissionScreen(context: Context, canvasContext: CanvasContext, annotatableAttachmentId: Long, submissionId: Long, assignmentId: Long, assignmentName: String)

    fun navigateToLtiLaunchScreen(context: Context, canvasContext: CanvasContext, url: String, title: String? = null, sessionLessLaunch: Boolean = false, isAssignmentLTI: Boolean = false, ltiTool: LTITool? = null)

    fun navigateToUploadStatusScreen(context: Context, submissionId: Long)

    fun navigateToDiscussionAttachmentScreen(context: Context, attachment: RemoteFile)

    fun navigateToUrl(activity: FragmentActivity, url: String, domain: String, extras: Bundle? = null)

    fun navigateToInternalWebView(context: Context, canvasContext: CanvasContext, url: String, authenticate: Boolean)

    fun openMedia(context: Context, url: String)

    fun showMediaDialog(context: Context)

    fun showSubmitDialog(context: Context, assignment: Assignment, studioLTITool: LTITool?)

    fun showCustomReminderDialog(context: Context)

    fun showDeleteReminderConfirmationDialog(context: Context, onConfirmed: () -> Unit)

    fun showCreateReminderDialog(context: Context)

    fun canRouteInternally(activity: FragmentActivity?, url: String?, domain: String, routeIfPossible: Boolean): Boolean

    fun applyTheme()
}