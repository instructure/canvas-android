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
package com.instructure.parentapp.features.assignment.details

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
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
import com.instructure.pandautils.features.assignments.details.reminder.CustomReminderDialog
import com.instructure.pandautils.utils.showThemed
import com.instructure.parentapp.R
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

    // TODO: Navigate to internal webview
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

    override fun showCustomReminderDialog(fragment: Fragment) {
        CustomReminderDialog.newInstance().show(fragment.childFragmentManager, null)
    }

    override fun showDeleteReminderConfirmationDialog(context: Context, onConfirmed: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle(R.string.deleteReminderTitle)
            .setMessage(R.string.deleteReminderMessage)
            .setNegativeButton(R.string.no, null)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                onConfirmed()
                dialog.dismiss()
            }
            .showThemed()
    }

    override fun showCreateReminderDialog(context: Context, onReminderSelected: (ReminderChoice) -> Unit) {
        val choices = listOf(
            ReminderChoice.Minute(5),
            ReminderChoice.Minute(15),
            ReminderChoice.Minute(30),
            ReminderChoice.Hour(1),
            ReminderChoice.Day(1),
            ReminderChoice.Week(1),
            ReminderChoice.Custom,
        )

        AlertDialog.Builder(context)
            .setTitle(R.string.reminderTitle)
            .setNegativeButton(R.string.cancel, null)
            .setSingleChoiceItems(
                choices.map {
                    if (it is ReminderChoice.Custom) {
                        it.getText(context.resources)
                    } else {
                        context.getString(R.string.reminderBefore, it.getText(context.resources))
                    }
                }.toTypedArray(), -1
            ) { dialog, which ->
                onReminderSelected(choices[which])
                dialog.dismiss()
            }
            .showThemed()
    }

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