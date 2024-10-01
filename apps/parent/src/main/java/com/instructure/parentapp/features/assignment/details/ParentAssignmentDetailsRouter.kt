package com.instructure.parentapp.features.assignment.details

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.RemoteFile
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.pandautils.databinding.FragmentAssignmentDetailsBinding
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsRouter
import com.instructure.pandautils.features.assignments.details.ReminderChoice
import java.io.File

class ParentAssignmentDetailsRouter: AssignmentDetailsRouter {
    override fun navigateToAssignmentUploadPicker(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        assignment: Assignment,
        mediaUri: Uri
    ) = Unit

    override fun navigateToLtiScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext?,
        url: String
    ) = Unit

    override fun navigateToSubmissionScreen(
        activity: FragmentActivity,
        course: CanvasContext,
        assignmentId: Long,
        isObserver: Boolean,
        initialSelectedSubmissionAttempt: Long?
    ) = Unit

    override fun navigateToQuizScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        quiz: Quiz,
        url: String
    ) = Unit

    override fun navigateToDiscussionScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        discussionTopicHeaderId: Long,
        isAnnouncement: Boolean
    ) = Unit

    override fun navigateToUploadScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        assignment: Assignment,
        attemptId: Long?
    ) = Unit

    override fun navigateToTextEntryScreen(
        activity: FragmentActivity,
        course: CanvasContext,
        assignmentId: Long,
        assignmentName: String?,
        initialText: String?,
        isFailure: Boolean
    ) = Unit

    override fun navigateToUrlSubmissionScreen(
        activity: FragmentActivity,
        course: CanvasContext,
        assignmentId: Long,
        assignmentName: String?,
        initialUrl: String?,
        isFailure: Boolean
    ) = Unit

    override fun navigateToAnnotationSubmissionScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        annotatableAttachmentId: Long,
        submissionId: Long,
        assignmentId: Long,
        assignmentName: String
    ) = Unit

    override fun navigateToLtiLaunchScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        url: String,
        title: String?,
        sessionLessLaunch: Boolean,
        isAssignmentLTI: Boolean,
        ltiTool: LTITool?
    ) = Unit

    override fun navigateToUploadStatusScreen(activity: FragmentActivity, submissionId: Long) = Unit

    override fun navigateToDiscussionAttachmentScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        attachment: RemoteFile
    ) = Unit

    override fun navigateToUrl(
        activity: FragmentActivity,
        url: String,
        domain: String,
        extras: Bundle?
    ) = Unit

    override fun navigateToInternalWebView(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        url: String,
        authenticate: Boolean
    ) = Unit

    override fun openMedia(activity: FragmentActivity, url: String) = Unit

    override fun showMediaDialog(
        activity: FragmentActivity,
        binding: FragmentAssignmentDetailsBinding?,
        recordCallback: (File?) -> Unit,
        startVideoCapture: () -> Unit,
        onLaunchMediaPicker: () -> Unit
    ) = Unit

    override fun showSubmitDialog(
        activity: FragmentActivity,
        binding: FragmentAssignmentDetailsBinding?,
        recordCallback: (File?) -> Unit,
        startVideoCapture: () -> Unit,
        onLaunchMediaPicker: () -> Unit,
        assignment: Assignment,
        course: Course,
        isStudioEnabled: Boolean,
        studioLTITool: LTITool?
    ) = Unit

    override fun showCustomReminderDialog(activity: FragmentActivity) = Unit

    override fun showDeleteReminderConfirmationDialog(context: Context, onConfirmed: () -> Unit) = Unit

    override fun showCreateReminderDialog(
        context: Context,
        onReminderSelected: (ReminderChoice) -> Unit
    ) = Unit

    override fun canRouteInternally(
        activity: FragmentActivity?,
        url: String,
        domain: String,
        routeIfPossible: Boolean
    ): Boolean = false

    override fun applyTheme(
        activity: FragmentActivity,
        binding: FragmentAssignmentDetailsBinding?,
        bookmark: Bookmarker,
        toolbar: Toolbar,
        course: Course?
    ) = Unit

    override fun onOptionsItemSelected(activity: FragmentActivity, item: MenuItem): Boolean = false
}