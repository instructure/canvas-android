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

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.activity.InternalWebViewActivity
import com.instructure.student.mobius.assignmentDetails.submission.text.ui.TextSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.url.ui.UrlSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionDetailsEmptyContentViewState.Loaded
import com.instructure.student.mobius.assignmentDetails.ui.SubmissionTypesVisibilities
import com.instructure.student.mobius.common.ui.MobiusView
import com.instructure.student.router.RouteMatcher
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.dialog_submission_picker.*
import kotlinx.android.synthetic.main.fragment_submission_details_empty_content.*
import kotlinx.android.synthetic.main.fragment_submission_details_empty_content.submitButton

class SubmissionDetailsEmptyContentView(
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
                title.text = if (state.isAllowedToSubmit) context.getString(R.string.submissionDetailsNoSubmissionYet) else context.getString(R.string.submissionDetailsAssignmentLocked)
                message.text = state.dueDateText
                submitButton.setHidden(state.isAllowedToSubmit)
            }
        }
    }

    override fun onDispose() {}
    override fun applyTheme() {}

    fun showSubmitDialogView(assignment: Assignment, courseId: Long, visibilities: SubmissionTypesVisibilities) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.setView(R.layout.dialog_submission_picker)
                .create()
        val course = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, courseId)
        dialog.setOnShowListener {
            setupDialogRow(dialog, dialog.submissionEntryText, visibilities.textEntry) {
                showOnlineTextEntryView(assignment.id, assignment.name, course)
            }
            setupDialogRow(dialog, dialog.submissionEntryWebsite, visibilities.urlEntry) {
                showOnlineUrlEntryView(assignment.id, assignment.name, course)
            }
            setupDialogRow(dialog, dialog.submissionEntryFile, visibilities.fileUpload) {
                showFileUploadView(assignment, courseId)
            }
            setupDialogRow(dialog, dialog.submissionEntryMedia, visibilities.mediaRecording) {
                showMediaRecordingView(assignment, courseId)
            }
            setupDialogRow(dialog, dialog.submissionEntryStudio, visibilities.studioUpload) {
                showStudioUploadView(assignment, courseId)
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

    fun showOnlineTextEntryView(assignmentId: Long, assignmentName: String?, canvasContext: CanvasContext, submittedText: String? = null) {
        RouteMatcher.route(context, TextSubmissionUploadFragment.makeRoute(canvasContext, assignmentId, assignmentName, submittedText))
    }

    fun showOnlineUrlEntryView(assignmentId: Long, assignmentName: String?, canvasContext: CanvasContext, submittedUrl: String? = null) {
        RouteMatcher.route(context, UrlSubmissionUploadFragment.makeRoute(canvasContext, assignmentId, assignmentName, submittedUrl))
    }

    fun showMediaRecordingView(assignment: Assignment, courseId: Long) {
        // TODO
        context.toast("Route to media page")
    }

    fun showFileUploadView(assignment: Assignment, courseId: Long) {
        // TODO
        context.toast("Route to file upload page")
    }

    fun showStudioUploadView(assignment: Assignment, courseId: Long) {
        // TODO
        context.toast("Route to Studio upload page")
    }

    fun showQuizOrDiscussionView(url: String) {
        if (!RouteMatcher.canRouteInternally(context, url, ApiPrefs.domain, true)) {
            val intent = Intent(context, InternalWebViewActivity::class.java)
            context.startActivity(intent)
        }
    }
}