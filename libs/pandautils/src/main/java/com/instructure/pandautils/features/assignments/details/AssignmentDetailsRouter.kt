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
import java.io.File

interface AssignmentDetailsRouter {
    fun navigateToAssignmentUploadPicker(activity: FragmentActivity, canvasContext: CanvasContext, assignment: Assignment, mediaUri: Uri)

    fun navigateToLtiScreen(activity: FragmentActivity, canvasContext: CanvasContext?, url: String)

    fun navigateToSubmissionScreen(activity: FragmentActivity, course: CanvasContext, assignmentId: Long, isObserver: Boolean = false, initialSelectedSubmissionAttempt: Long? = null)

    fun navigateToQuizScreen(activity: FragmentActivity, canvasContext: CanvasContext, quiz: Quiz, url: String)

    fun navigateToDiscussionScreen(activity: FragmentActivity, canvasContext: CanvasContext, discussionTopicHeaderId: Long, isAnnouncement: Boolean = false)

    fun navigateToUploadScreen(activity: FragmentActivity, canvasContext: CanvasContext, assignment: Assignment, attemptId: Long? = null)

    fun navigateToTextEntryScreen(activity: FragmentActivity, course: CanvasContext, assignmentId: Long, assignmentName: String? = "", initialText: String? = null, isFailure: Boolean = false)

    fun navigateToUrlSubmissionScreen(activity: FragmentActivity, course: CanvasContext, assignmentId: Long, assignmentName: String? = "", initialUrl: String?, isFailure: Boolean = false)

    fun navigateToAnnotationSubmissionScreen(activity: FragmentActivity, canvasContext: CanvasContext, annotatableAttachmentId: Long, submissionId: Long, assignmentId: Long, assignmentName: String)

    fun navigateToLtiLaunchScreen(activity: FragmentActivity, canvasContext: CanvasContext, url: String, title: String? = null, sessionLessLaunch: Boolean = false, isAssignmentLTI: Boolean = false, ltiTool: LTITool? = null)

    fun navigateToUploadStatusScreen(activity: FragmentActivity, submissionId: Long)

    fun navigateToDiscussionAttachmentScreen(activity: FragmentActivity, canvasContext: CanvasContext, attachment: RemoteFile)

    fun navigateToUrl(activity: FragmentActivity, url: String, domain: String, extras: Bundle? = null)

    fun navigateToInternalWebView(activity: FragmentActivity, canvasContext: CanvasContext, url: String, authenticate: Boolean)

    fun openMedia(activity: FragmentActivity, url: String)

    fun showMediaDialog(activity: FragmentActivity, binding: FragmentAssignmentDetailsBinding?, recordCallback: (File?) -> Unit, startVideoCapture: () -> Unit, onLaunchMediaPicker: () -> Unit,)

    fun showSubmitDialog(activity: FragmentActivity, binding: FragmentAssignmentDetailsBinding?, recordCallback: (File?) -> Unit, startVideoCapture: () -> Unit, onLaunchMediaPicker: () -> Unit, assignment: Assignment, course: Course, isStudioEnabled: Boolean, studioLTITool: LTITool?)

    fun showCustomReminderDialog(fragment: Fragment)

    fun showDeleteReminderConfirmationDialog(context: Context, onConfirmed: () -> Unit)

    fun showCreateReminderDialog(context: Context, onReminderSelected: (ReminderChoice) -> Unit)

    fun canRouteInternally(activity: FragmentActivity?, url: String, domain: String, routeIfPossible: Boolean): Boolean

    fun applyTheme(activity: FragmentActivity, binding: FragmentAssignmentDetailsBinding?, bookmark: Bookmarker, toolbar: Toolbar, course: Course?)

    fun onOptionsItemSelected(activity: FragmentActivity, item: MenuItem): Boolean
}