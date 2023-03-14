/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.discussion.router.DiscussionRouterFragment
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setHidden
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.views.RecordingMediaType
import com.instructure.student.R
import com.instructure.student.activity.InternalWebViewActivity
import com.instructure.student.fragment.BasicQuizViewFragment
import com.instructure.student.fragment.LtiLaunchFragment
import com.instructure.student.fragment.StudioWebViewFragment
import com.instructure.student.mobius.assignmentDetails.submission.annnotation.AnnotationSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionMode
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.text.ui.TextSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.url.ui.UrlSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionDetailsEmptyContentViewState.Loaded
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionTypesVisibilities
import com.instructure.student.mobius.common.ui.MobiusView
import com.instructure.student.router.RouteMatcher
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.dialog_submission_picker.*
import kotlinx.android.synthetic.main.dialog_submission_picker_media.*
import kotlinx.android.synthetic.main.fragment_submission_details_empty_content.*

class SubmissionDetailsEmptyContentView(
    val canvasContext: CanvasContext,
    inflater: LayoutInflater,
    parent: ViewGroup
) : MobiusView<SubmissionDetailsEmptyContentViewState, SubmissionDetailsEmptyContentEvent>(
    R.layout.fragment_submission_details_empty_content,
    inflater,
    parent
) {

    init {
        submitButton.backgroundTintList = ColorStateList.valueOf(ThemePrefs.buttonColor)
        submitButton.setTextColor(ThemePrefs.buttonTextColor)
    }

    override fun onConnect(output: Consumer<SubmissionDetailsEmptyContentEvent>) {
        submitButton.onClick { output.accept(SubmissionDetailsEmptyContentEvent.SubmitAssignmentClicked) }
    }

    override fun render(state: SubmissionDetailsEmptyContentViewState) {
        when(state) {
            is Loaded -> {
                title.text = state.emptyViewTitleText
                message.text = state.emptyViewSubtitleText
                submitButton.apply {
                    setHidden(!state.submitButtonVisible)
                    text = state.submitButtonText
                }
            }
        }
    }

    override fun onDispose() {}
    override fun applyTheme() {}

    fun showSubmitDialogView(assignment: Assignment, visibilities: SubmissionTypesVisibilities, ltiToolUrl: String? = null, ltiToolName: String? = null) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.setView(R.layout.dialog_submission_picker).create()

        dialog.setOnShowListener {
            setupDialogRow(dialog, dialog.submissionEntryText, visibilities.textEntry) {
                showOnlineTextEntryView(assignment.id, assignment.name)
            }
            setupDialogRow(dialog, dialog.submissionEntryWebsite, visibilities.urlEntry) {
                showOnlineUrlEntryView(assignment.id, assignment.name, canvasContext)
            }
            setupDialogRow(dialog, dialog.submissionEntryFile, visibilities.fileUpload) {
                showFileUploadView(assignment)
            }
            setupDialogRow(dialog, dialog.submissionEntryMedia, visibilities.mediaRecording) {
                showMediaRecordingView()
            }
            setupDialogRow(dialog, dialog.submissionEntryStudio, visibilities.studioUpload) {
                // The LTI info shouldn't be null if we are showing the Studio upload option
                showStudioUploadView(assignment, ltiToolUrl!!, ltiToolName!!)
            }
            setupDialogRow(dialog, dialog.submissionEntryStudentAnnotation, visibilities.studentAnnotation) {
                showStudentAnnotationView(assignment)
            }
        }
        dialog.show()
    }

    private fun setupDialogRow(dialog: Dialog, view: View, visibility: Boolean, onClick: () -> Unit) {
        view.setVisible(visibility)
        view.setOnClickListener {
            onClick()
            dialog.cancel()
        }
    }

    fun showOnlineTextEntryView(assignmentId: Long, assignmentName: String?, submittedText: String? = null, isFailure: Boolean = false) {
        logEventWithOrigin(AnalyticsEventConstants.SUBMIT_TEXTENTRY_SELECTED)
        RouteMatcher.route(context, TextSubmissionUploadFragment.makeRoute(canvasContext, assignmentId, assignmentName, submittedText, isFailure))
    }

    fun showOnlineUrlEntryView(assignmentId: Long, assignmentName: String?, canvasContext: CanvasContext, submittedUrl: String? = null) {
        logEventWithOrigin(AnalyticsEventConstants.SUBMIT_ONLINEURL_SELECTED)
        RouteMatcher.route(context, UrlSubmissionUploadFragment.makeRoute(canvasContext, assignmentId, assignmentName, submittedUrl))
    }

    fun showLTIView(canvasContext: CanvasContext, title: String, ltiTool: LTITool? = null) {
        logEventWithOrigin(AnalyticsEventConstants.ASSIGNMENT_LAUNCHLTI_SELECTED)
        RouteMatcher.route(context, LtiLaunchFragment.makeRoute(
            canvasContext,
            ltiTool?.url ?: "",
            title,
            isAssignmentLTI = true,
            ltiTool = ltiTool
        ))
    }

    fun showQuizStartView(canvasContext: CanvasContext, quiz: Quiz) {
        logEventWithOrigin(AnalyticsEventConstants.ASSIGNMENT_DETAIL_QUIZLAUNCH)
        RouteMatcher.route(context, BasicQuizViewFragment.makeRoute(canvasContext, quiz, quiz.url!!))
    }

    fun showDiscussionDetailView(canvasContext: CanvasContext, discussionTopicHeaderId: Long) {
        logEventWithOrigin(AnalyticsEventConstants.ASSIGNMENT_DETAIL_DISCUSSIONLAUNCH)
        RouteMatcher.route(context, DiscussionRouterFragment.makeRoute(canvasContext, discussionTopicHeaderId))
    }

    fun showMediaRecordingView() {
        logEventWithOrigin(AnalyticsEventConstants.SUBMIT_MEDIARECORDING_SELECTED)
        val builder = AlertDialog.Builder(context)
        val dialog = builder.setView(R.layout.dialog_submission_picker_media).create()

        dialog.setOnShowListener {
            setupDialogRow(dialog, dialog.submissionEntryAudio, true) {
                consumer?.accept(SubmissionDetailsEmptyContentEvent.AudioRecordingClicked)
            }
            setupDialogRow(dialog, dialog.submissionEntryVideo, true) {
                consumer?.accept(SubmissionDetailsEmptyContentEvent.VideoRecordingClicked)
            }

            setupDialogRow(dialog, dialog.submissionEntryMediaFile, true) {
                consumer?.accept(SubmissionDetailsEmptyContentEvent.ChooseMediaClicked)
            }
        }
        dialog.show()
    }

    private fun showStudioUploadView(assignment: Assignment, ltiUrl: String, studioLtiToolName: String) {
        logEventWithOrigin(AnalyticsEventConstants.SUBMIT_STUDIO_SELECTED)
        RouteMatcher.route(context, StudioWebViewFragment.makeRoute(canvasContext, ltiUrl, studioLtiToolName, true, assignment))
    }

    fun showAudioRecordingView() {
        floatingRecordingView.setContentType(RecordingMediaType.Audio)
        floatingRecordingView.setVisible()
        floatingRecordingView.recordingCallback = { file ->
            consumer?.accept(SubmissionDetailsEmptyContentEvent.SendAudioRecordingClicked(file))
        }
        floatingRecordingView.stoppedCallback = {}
    }

    fun showPermissionDeniedToast() {
        Toast.makeText(context, com.instructure.pandautils.R.string.permissionDenied, Toast.LENGTH_LONG).show()
    }

    fun showAudioRecordingError() {
        Toast.makeText(context, com.instructure.pandautils.R.string.audioRecordingError, Toast.LENGTH_LONG).show()
    }

    fun showVideoRecordingError() {
        Toast.makeText(context, com.instructure.pandautils.R.string.videoRecordingError, Toast.LENGTH_LONG).show()
    }

    fun showMediaPickingError() {
        Toast.makeText(context, com.instructure.pandautils.R.string.unexpectedErrorOpeningFile, Toast.LENGTH_LONG).show()
    }

    fun showFileUploadView(assignment: Assignment) {
        logEventWithOrigin(AnalyticsEventConstants.SUBMIT_FILEUPLOAD_SELECTED)
        RouteMatcher.route(context, PickerSubmissionUploadFragment.makeRoute(canvasContext, assignment, PickerSubmissionMode.FileSubmission))
    }

    fun showQuizOrDiscussionView(url: String) {
        if (!RouteMatcher.canRouteInternally(context, url, ApiPrefs.domain, true)) {
            val intent = Intent(context, InternalWebViewActivity::class.java)
            context.startActivity(intent)
        }
    }

    fun launchFilePickerView(uri: Uri, course: Course, assignment: Assignment) {
        RouteMatcher.route(context, PickerSubmissionUploadFragment.makeRoute(course, assignment, uri))
    }

    fun returnToAssignmentDetails() {
        // Not run on main thread of fragment host by default, so force it to run on UI thread
        (context as Activity).runOnUiThread { (context as Activity).onBackPressed() }
    }

    fun showStudentAnnotationView(assignment: Assignment) {
        logEvent(AnalyticsEventConstants.SUBMIT_STUDENT_ANNOTATION_SELECTED)

        val submissionId = assignment.submission?.id
        if (submissionId != null) {
            RouteMatcher.route(
                context,
                AnnotationSubmissionUploadFragment.makeRoute(
                    canvasContext,
                    assignment.annotatableAttachmentId,
                    submissionId,
                    assignment.id,
                    assignment.name ?: ""
                )
            )
        }
    }
}
