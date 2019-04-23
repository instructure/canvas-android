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
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.activity.InternalWebViewActivity
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.SubmissionDetailsEmptyContentEvent
import com.instructure.student.mobius.assignmentDetails.submissionDetails.content.emptySubmission.ui.SubmissionDetailsEmptyContentViewState.Loaded
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsFragment
import com.instructure.student.mobius.assignmentDetails.ui.SubmissionTypesVisibilities
import com.instructure.student.mobius.common.ui.MobiusView
import com.instructure.student.router.RouteMatcher
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.dialog_submission_picker.*
import kotlinx.android.synthetic.main.fragment_submission_details_empty_content.*

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
        dialog.setOnShowListener {
            setupDialogRow(dialog, dialog.submissionEntryText, visibilities.textEntry) {
                showOnlineTextEntryView(assignment.id, courseId)
            }
            setupDialogRow(dialog, dialog.submissionEntryWebsite, visibilities.urlEntry) {
                showOnlineUrlEntryView(assignment.id, courseId)
            }
            setupDialogRow(dialog, dialog.submissionEntryFile, visibilities.fileUpload) {
                showFileUploadView(assignment, courseId)
            }
            setupDialogRow(dialog, dialog.submissionEntryMedia, visibilities.mediaRecording) {
                showMediaRecordingView(assignment, courseId)
            }
            setupDialogRow(dialog, dialog.submissionEntryArc, visibilities.arcUpload) {
                showArcUploadView(assignment, courseId)
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

    fun showSubmissionView(assignmentId: Long, course: Course) {
        RouteMatcher.route(context, SubmissionDetailsFragment.makeRoute(course, assignmentId))
    }

    fun showUploadStatusView(assignmentId: Long, course: Course) {
        // TODO
        context.toast("Route to status page")
    }

    fun showOnlineTextEntryView(assignmentId: Long, courseId: Long) {
        // TODO
        context.toast("Route to text entry page")
    }

    fun showOnlineUrlEntryView(assignmentId: Long, courseId: Long) {
        // TODO
        context.toast("Route to url page")
    }

    fun showMediaRecordingView(assignment: Assignment, courseId: Long) {
        // TODO
        context.toast("Route to media page")
    }

    fun showFileUploadView(assignment: Assignment, courseId: Long) {
        // TODO
        context.toast("Route to file upload page")
    }

    fun showArcUploadView(assignment: Assignment, courseId: Long) {
        // TODO
        context.toast("Route to arc upload page")
    }

    fun showQuizOrDiscussionView(url: String) {
        if (!RouteMatcher.canRouteInternally(context, url, ApiPrefs.domain, true)) {
            val intent = Intent(context, InternalWebViewActivity::class.java)
            context.startActivity(intent)
        }
    }
}