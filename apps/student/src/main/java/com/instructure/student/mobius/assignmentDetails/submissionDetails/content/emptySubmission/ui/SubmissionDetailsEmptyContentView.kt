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

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.discussion.router.DiscussionRouterFragment
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.onClickWithRequireNetwork
import com.instructure.pandautils.utils.setHidden
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.views.RecordingMediaType
import com.instructure.student.activity.InternalWebViewActivity
import com.instructure.student.databinding.DialogSubmissionPickerBinding
import com.instructure.student.databinding.DialogSubmissionPickerMediaBinding
import com.instructure.student.databinding.FragmentSubmissionDetailsEmptyContentBinding
import com.instructure.student.fragment.BasicQuizViewFragment
import com.instructure.student.fragment.LtiLaunchFragment
import com.instructure.student.fragment.StudioWebViewFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionDetailsEmptyContentViewState.Loaded
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionTypesVisibilities
import com.instructure.student.mobius.assignmentDetails.submission.annnotation.AnnotationSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionMode
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.text.ui.TextSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.url.ui.UrlSubmissionUploadFragment
import com.instructure.student.mobius.common.ui.MobiusView
import com.instructure.student.router.RouteMatcher
import com.spotify.mobius.functions.Consumer

class SubmissionDetailsEmptyContentView(
    val canvasContext: CanvasContext,
    inflater: LayoutInflater,
    parent: ViewGroup
) : MobiusView<SubmissionDetailsEmptyContentViewState, SubmissionDetailsEmptyContentEvent, FragmentSubmissionDetailsEmptyContentBinding>(
    inflater,
    FragmentSubmissionDetailsEmptyContentBinding::inflate,
    parent
) {

    init {
        binding.submitButton.backgroundTintList = ColorStateList.valueOf(ThemePrefs.buttonColor)
        binding.submitButton.setTextColor(ThemePrefs.buttonTextColor)
    }

    override fun onConnect(output: Consumer<SubmissionDetailsEmptyContentEvent>) {
        binding.submitButton.onClickWithRequireNetwork { output.accept(SubmissionDetailsEmptyContentEvent.SubmitAssignmentClicked) }
    }

    override fun render(state: SubmissionDetailsEmptyContentViewState) {
        when(state) {
            is Loaded -> {
                binding.title.text = state.emptyViewTitleText
                binding.message.text = state.emptyViewSubtitleText
                binding.submitButton.apply {
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
        val binding = DialogSubmissionPickerBinding.inflate(LayoutInflater.from(context), null, false)
        val dialog = builder.setView(binding.root).create()

        dialog.setOnShowListener {
            setupDialogRow(dialog, binding.submissionEntryText, visibilities.textEntry) {
                showOnlineTextEntryView(assignment.id, assignment.name)
            }
            setupDialogRow(dialog, binding.submissionEntryWebsite, visibilities.urlEntry) {
                showOnlineUrlEntryView(assignment.id, assignment.name, canvasContext)
            }
            setupDialogRow(dialog, binding.submissionEntryFile, visibilities.fileUpload) {
                showFileUploadView(assignment)
            }
            setupDialogRow(dialog, binding.submissionEntryMedia, visibilities.mediaRecording) {
                showMediaRecordingView()
            }
            setupDialogRow(dialog, binding.submissionEntryStudio, visibilities.studioUpload) {
                // The LTI info shouldn't be null if we are showing the Studio upload option
                showStudioUploadView(assignment, ltiToolUrl!!, ltiToolName!!)
            }
            setupDialogRow(dialog, binding.submissionEntryStudentAnnotation, visibilities.studentAnnotation) {
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
        RouteMatcher.route(activity as FragmentActivity, TextSubmissionUploadFragment.makeRoute(canvasContext, assignmentId, assignmentName, submittedText, isFailure))
    }

    fun showOnlineUrlEntryView(assignmentId: Long, assignmentName: String?, canvasContext: CanvasContext, submittedUrl: String? = null) {
        logEventWithOrigin(AnalyticsEventConstants.SUBMIT_ONLINEURL_SELECTED)
        RouteMatcher.route(activity as FragmentActivity, UrlSubmissionUploadFragment.makeRoute(canvasContext, assignmentId, assignmentName, submittedUrl))
    }

    fun showLTIView(canvasContext: CanvasContext, title: String, ltiTool: LTITool? = null) {
        logEventWithOrigin(AnalyticsEventConstants.ASSIGNMENT_LAUNCHLTI_SELECTED)
        RouteMatcher.route(activity as FragmentActivity, LtiLaunchFragment.makeRoute(
            canvasContext,
            ltiTool?.url ?: "",
            title,
            isAssignmentLTI = true,
            ltiTool = ltiTool
        ))
    }

    fun showQuizStartView(canvasContext: CanvasContext, quiz: Quiz) {
        logEventWithOrigin(AnalyticsEventConstants.ASSIGNMENT_DETAIL_QUIZLAUNCH)
        RouteMatcher.route(activity as FragmentActivity, BasicQuizViewFragment.makeRoute(canvasContext, quiz, quiz.url!!))
    }

    fun showDiscussionDetailView(canvasContext: CanvasContext, discussionTopicHeaderId: Long) {
        logEventWithOrigin(AnalyticsEventConstants.ASSIGNMENT_DETAIL_DISCUSSIONLAUNCH)
        RouteMatcher.route(activity as FragmentActivity, DiscussionRouterFragment.makeRoute(canvasContext, discussionTopicHeaderId))
    }

    fun showMediaRecordingView() {
        logEventWithOrigin(AnalyticsEventConstants.SUBMIT_MEDIARECORDING_SELECTED)
        val builder = AlertDialog.Builder(context)
        val binding = DialogSubmissionPickerMediaBinding.inflate(LayoutInflater.from(context), null, false)
        val dialog = builder.setView(binding.root).create()

        dialog.setOnShowListener {
            setupDialogRow(dialog, binding.submissionEntryAudio, true) {
                consumer?.accept(SubmissionDetailsEmptyContentEvent.AudioRecordingClicked)
            }
            setupDialogRow(dialog, binding.submissionEntryVideo, true) {
                consumer?.accept(SubmissionDetailsEmptyContentEvent.VideoRecordingClicked)
            }

            setupDialogRow(dialog, binding.submissionEntryMediaFile, true) {
                consumer?.accept(SubmissionDetailsEmptyContentEvent.ChooseMediaClicked)
            }
        }
        dialog.show()
    }

    private fun showStudioUploadView(assignment: Assignment, ltiUrl: String, studioLtiToolName: String) {
        logEventWithOrigin(AnalyticsEventConstants.SUBMIT_STUDIO_SELECTED)
        RouteMatcher.route(activity as FragmentActivity, StudioWebViewFragment.makeRoute(canvasContext, ltiUrl, studioLtiToolName, true, assignment))
    }

    fun showAudioRecordingView() {
        binding.floatingRecordingView.apply {
            setContentType(RecordingMediaType.Audio)
            setVisible()
            recordingCallback = { file ->
                consumer?.accept(SubmissionDetailsEmptyContentEvent.SendAudioRecordingClicked(file))
            }
            stoppedCallback = {}
        }
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
        RouteMatcher.route(activity as FragmentActivity, PickerSubmissionUploadFragment.makeRoute(canvasContext, assignment, PickerSubmissionMode.FileSubmission))
    }

    fun showQuizOrDiscussionView(url: String) {
        if (!RouteMatcher.canRouteInternally(activity as FragmentActivity, url, ApiPrefs.domain, true)) {
            val intent = Intent(context, InternalWebViewActivity::class.java)
            context.startActivity(intent)
        }
    }

    fun launchFilePickerView(uri: Uri, course: Course, assignment: Assignment) {
        RouteMatcher.route(activity as FragmentActivity, PickerSubmissionUploadFragment.makeRoute(course, assignment, uri))
    }

    fun returnToAssignmentDetails() {
        // Not run on main thread of fragment host by default, so force it to run on UI thread
        activity.runOnUiThread { activity.onBackPressed() }
    }

    fun showStudentAnnotationView(assignment: Assignment) {
        logEvent(AnalyticsEventConstants.SUBMIT_STUDENT_ANNOTATION_SELECTED)

        val submissionId = assignment.submission?.id
        if (submissionId != null) {
            RouteMatcher.route(
                activity as FragmentActivity,
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
