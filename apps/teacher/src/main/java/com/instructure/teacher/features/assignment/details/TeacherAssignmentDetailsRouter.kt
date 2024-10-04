package com.instructure.teacher.features.assignment.details

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
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

class TeacherAssignmentDetailsRouter: AssignmentDetailsRouter {
    override fun navigateToAssignmentUploadPicker(
        activity: FragmentActivity,
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
        activity: FragmentActivity,
        course: CanvasContext,
        assignmentId: Long,
        isObserver: Boolean,
        initialSelectedSubmissionAttempt: Long?
    ) {
        TODO("Not yet implemented")
    }

    override fun navigateToQuizScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        quiz: Quiz,
        url: String
    ) {
        TODO("Not yet implemented")
    }

    override fun navigateToDiscussionScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        discussionTopicHeaderId: Long,
        isAnnouncement: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun navigateToUploadScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        assignment: Assignment,
        attemptId: Long?
    ) {
        TODO("Not yet implemented")
    }

    override fun navigateToTextEntryScreen(
        activity: FragmentActivity,
        course: CanvasContext,
        assignmentId: Long,
        assignmentName: String?,
        initialText: String?,
        isFailure: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun navigateToUrlSubmissionScreen(
        activity: FragmentActivity,
        course: CanvasContext,
        assignmentId: Long,
        assignmentName: String?,
        initialUrl: String?,
        isFailure: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun navigateToAnnotationSubmissionScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        annotatableAttachmentId: Long,
        submissionId: Long,
        assignmentId: Long,
        assignmentName: String
    ) {
        TODO("Not yet implemented")
    }

    override fun navigateToLtiLaunchScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        url: String,
        title: String?,
        sessionLessLaunch: Boolean,
        isAssignmentLTI: Boolean,
        ltiTool: LTITool?
    ) {
        TODO("Not yet implemented")
    }

    override fun navigateToUploadStatusScreen(activity: FragmentActivity, submissionId: Long) {
        TODO("Not yet implemented")
    }

    override fun navigateToDiscussionAttachmentScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        attachment: RemoteFile
    ) {
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
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        url: String,
        authenticate: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun openMedia(activity: FragmentActivity, url: String) {
        TODO("Not yet implemented")
    }

    override fun showMediaDialog(
        activity: FragmentActivity,
        binding: FragmentAssignmentDetailsBinding?,
        recordCallback: (File?) -> Unit,
        startVideoCapture: () -> Unit,
        onLaunchMediaPicker: () -> Unit
    ) {
        TODO("Not yet implemented")
    }

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
    ) {
        TODO("Not yet implemented")
    }

    override fun showCustomReminderDialog(fragment: Fragment) {
        TODO("Not yet implemented")
    }

    override fun showDeleteReminderConfirmationDialog(context: Context, onConfirmed: () -> Unit) {
        TODO("Not yet implemented")
    }

    override fun showCreateReminderDialog(
        context: Context,
        onReminderSelected: (ReminderChoice) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun canRouteInternally(
        activity: FragmentActivity?,
        url: String,
        domain: String,
        routeIfPossible: Boolean
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun applyTheme(
        activity: FragmentActivity,
        binding: FragmentAssignmentDetailsBinding?,
        bookmark: Bookmarker,
        toolbar: Toolbar,
        course: Course?
    ) {
        TODO("Not yet implemented")
    }

    override fun onOptionsItemSelected(activity: FragmentActivity, item: MenuItem): Boolean {
        TODO("Not yet implemented")
    }
}