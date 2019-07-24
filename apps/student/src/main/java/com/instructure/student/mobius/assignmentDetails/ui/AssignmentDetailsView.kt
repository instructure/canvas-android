/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.mobius.assignmentDetails.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.instructure.canvasapi2.models.*
import android.widget.Toast
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.student.R
import com.instructure.student.activity.BaseRouterActivity
import com.instructure.student.activity.InternalWebViewActivity
import com.instructure.student.activity.ShareFileSubmissionTarget
import com.instructure.student.fragment.DiscussionDetailsFragment
import com.instructure.student.fragment.InternalWebviewFragment
import com.instructure.student.fragment.LTIWebViewFragment
import com.instructure.student.fragment.QuizStartFragment
import com.instructure.student.mobius.assignmentDetails.AssignmentDetailsEvent
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.text.ui.TextSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.url.ui.UrlSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsFragment
import com.instructure.student.mobius.common.ui.MobiusView
import com.instructure.student.router.RouteMatcher
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.dialog_submission_picker.*
import kotlinx.android.synthetic.main.fragment_assignment_details.*

class AssignmentDetailsView(
    val canvasContext: CanvasContext,
    inflater: LayoutInflater,
    parent: ViewGroup
) :
    MobiusView<AssignmentDetailsViewState, AssignmentDetailsEvent>(
        R.layout.fragment_assignment_details,
        inflater,
        parent
    ) {

    init {
        toolbar.setupAsBackButton { (context as? Activity)?.onBackPressed() }
        toolbar.title = context.getString(R.string.assignmentDetails)
        toolbar.subtitle = canvasContext.name
        submissionStatusFailedSubtitle.setTextColor(ThemePrefs.buttonColor)
        submissionStatusUploadingSubtitle.setTextColor(ThemePrefs.buttonColor)
        submissionAndRubricLabel.setTextColor(ThemePrefs.buttonColor)
        submitButton.backgroundTintList = ColorStateList.valueOf(ThemePrefs.buttonColor)
        submitButton.setTextColor(ThemePrefs.buttonTextColor)
    }

    override fun applyTheme() {
        ViewStyler.themeToolbar(context as Activity, toolbar, canvasContext)
    }

    override fun onConnect(output: Consumer<AssignmentDetailsEvent>) {
        submissionStatusFailed.onClick { output.accept(AssignmentDetailsEvent.ViewUploadStatusClicked) }
        submissionStatusUploading.onClick { output.accept(AssignmentDetailsEvent.ViewUploadStatusClicked) }
        submissionRubricButton.onClick { output.accept(AssignmentDetailsEvent.ViewSubmissionClicked) }
        submitButton.onClick { output.accept(AssignmentDetailsEvent.SubmitAssignmentClicked) }
        attachmentIcon.onClick { output.accept(AssignmentDetailsEvent.DiscussionAttachmentClicked) }
        swipeRefreshLayout.setOnRefreshListener { output.accept(AssignmentDetailsEvent.PullToRefresh) }
        setupDescriptionView()
    }

    private fun setupDescriptionView() {
        descriptionWebView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {}
            override fun onPageFinishedCallback(webView: WebView, url: String) {}
            override fun onPageStartedCallback(webView: WebView, url: String) {}
            override fun canRouteInternallyDelegate(url: String): Boolean {
                return RouteMatcher.canRouteInternally(context, url, ApiPrefs.domain, false)
            }

            override fun routeInternallyCallback(url: String) {
                consumer?.accept(AssignmentDetailsEvent.InternalRouteRequested(url))
            }
        }

        descriptionWebView.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
            override fun launchInternalWebViewFragment(url: String) {
                InternalWebviewFragment.loadInternalWebView(
                    context,
                    InternalWebviewFragment.makeRoute(canvasContext, url, false)
                )
            }

            override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
        }
    }

    override fun render(state: AssignmentDetailsViewState) {
        with (state) {
            swipeRefreshLayout.isRefreshing = visibilities.loading
            errorContainer.setVisible(visibilities.errorMessage)
            titleContainer.setVisible(visibilities.title)
            dueDateContainer.setVisible(visibilities.dueDate)
            submissionTypesContainer.setVisible(visibilities.submissionTypes)
            fileTypesContainer.setVisible(visibilities.fileTypes)
            gradeContainer.setVisible(visibilities.grade)
            lockMessageContainer.setVisible(visibilities.lockedMessage)
            submissionRubricButton.setVisible(visibilities.submissionAndRubricButton)
            lockImageContainer.setVisible(visibilities.lockedImage)
            noDescriptionContainer.setVisible(visibilities.noDescriptionLabel)
            descriptionWebView.setVisible(visibilities.description)
            submitButton.setVisible(visibilities.submitButton)
            submissionUploadStatusContainer.setVisible(visibilities.submissionUploadStatusInProgress || visibilities.submissionUploadStatusFailed)
            submissionStatusUploading.setVisible(visibilities.submissionUploadStatusInProgress)
            submissionStatusFailed.setVisible(visibilities.submissionUploadStatusFailed)
            descriptionContainer.setVisible(visibilities.description || visibilities.noDescriptionLabel)
            quizDetails.setVisible(visibilities.quizDetails)
            discussionTopicHeaderContainer.setVisible(visibilities.discussionTopicHeader)
        }

        when (state) {
            AssignmentDetailsViewState.Loading -> Unit
            AssignmentDetailsViewState.Error -> Unit
            is AssignmentDetailsViewState.Loaded -> renderLoadedState(state)
        }.exhaustive
    }

    private fun renderLoadedState(state: AssignmentDetailsViewState.Loaded) {
        assignmentName.text = state.assignmentName
        points.text = state.assignmentPoints
        submissionStatusIcon.setImageResource(state.submittedStateIcon)
        submissionStatusIcon.imageTintList = ColorStateList.valueOf(state.submittedStateColor)
        submissionStatus.text = state.submittedStateLabel
        submissionStatus.setTextColor(state.submittedStateColor)
        dueDateTextView.text = state.dueDate
        lockMessageTextView.text = state.lockMessage
        submissionTypesTextView.text = state.submissionTypes
        fileTypesTextView.text = state.fileTypes
        gradeCell.setState(state.gradeState)
        submitButton.text = state.submitButtonText
        if (state.visibilities.description) {
            descriptionLabel.text = state.descriptionLabel
            descriptionWebView.formatHTML(state.description, state.assignmentName)
        }
        if(state.visibilities.quizDetails) renderQuizDetails(state.quizDescriptionViewState!!)
        if(state.visibilities.discussionTopicHeader) renderDiscussionTopicHeader(state.discussionHeaderViewState!!)
    }

    private fun renderQuizDetails(quizDescriptionViewState: QuizDescriptionViewState) {
        questionCountText.text = quizDescriptionViewState.questionCount
        timeLimitText.text = quizDescriptionViewState.timeLimit
        allowedAttemptsText.text = quizDescriptionViewState.allowedAttempts
    }

    private fun renderDiscussionTopicHeader(discussionHeaderViewState: DiscussionHeaderViewState) {
        ProfileUtils.loadAvatarForUser(authorAvatar, discussionHeaderViewState.authorName, discussionHeaderViewState.authorAvatarUrl)
        authorAvatar.setupAvatarA11y(discussionHeaderViewState.authorName)
        authorName.text = discussionHeaderViewState.authorName
        authoredDate.text = discussionHeaderViewState.authoredDate
        attachmentIcon.setVisible(discussionHeaderViewState.attachmentIconVisibility)
    }

    override fun onDispose() {
        descriptionWebView.stopLoading()
    }

    fun showSubmitDialogView(assignment: Assignment, courseId: Long, visibilities: SubmissionTypesVisibilities) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.setView(R.layout.dialog_submission_picker).create()

        dialog.setOnShowListener {
            setupDialogRow(dialog, dialog.submissionEntryText, visibilities.textEntry) {
                showOnlineTextEntryView(assignment.id, assignment.name, assignment.submission?.body)
            }
            setupDialogRow(dialog, dialog.submissionEntryWebsite, visibilities.urlEntry) {
                showOnlineUrlEntryView(assignment.id, assignment.name, canvasContext, assignment.submission?.url)
            }
            setupDialogRow(dialog, dialog.submissionEntryFile, visibilities.fileUpload) {
                showFileUploadView(assignment)
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

    fun routeInternally(url: String, domain: String, course: Course, assignment: Assignment) {
        val extras = Bundle().apply {
            putParcelable(Const.SUBMISSION_TARGET, ShareFileSubmissionTarget(course, assignment))
        }
        RouteMatcher.routeUrl(context, url, domain, extras)
    }

    fun showSubmissionView(assignmentId: Long, course: Course) {
        RouteMatcher.route(context, SubmissionDetailsFragment.makeRoute(course, assignmentId))
    }

    fun showUploadStatusView(submissionId: Long) {
        Toast.makeText(context, "Route to upload status", Toast.LENGTH_SHORT).show()
    }

    fun showOnlineTextEntryView(assignmentId: Long, assignmentName: String?, submittedText: String? = null) {
        RouteMatcher.route(context, TextSubmissionUploadFragment.makeRoute(canvasContext, assignmentId, assignmentName, submittedText))
    }

    fun showOnlineUrlEntryView(assignmentId: Long, assignmentName: String?, canvasContext: CanvasContext, submittedUrl: String? = null) {
        RouteMatcher.route(context, UrlSubmissionUploadFragment.makeRoute(canvasContext, assignmentId, assignmentName, submittedUrl))
    }

    fun showLTIView(canvasContext: CanvasContext, url: String, title: String) {
        RouteMatcher.route(context, LTIWebViewFragment.makeRoute(canvasContext, url, title, isAssignmentLTI = true))
    }

    fun showQuizStartView(canvasContext: CanvasContext, quiz: Quiz) {
        RouteMatcher.route(context, QuizStartFragment.makeRoute(canvasContext, quiz))
    }

    fun showDiscussionDetailView(canvasContext: CanvasContext, discussionTopicHeaderId: Long) {
        RouteMatcher.route(context, DiscussionDetailsFragment.makeRoute(canvasContext, discussionTopicHeaderId))
    }

    fun showDiscussionAttachment(canvasContext: CanvasContext, discussionAttachment: Attachment) {
        (context as BaseRouterActivity).openMedia(canvasContext, discussionAttachment.contentType ?: "", discussionAttachment.url ?: "", discussionAttachment.filename ?: "")
    }

    fun showMediaRecordingView(assignment: Assignment, courseId: Long) {
        // TODO
        context.toast("Show media recording dialog")
    }

    fun showFileUploadView(assignment: Assignment) {
        RouteMatcher.route(context, PickerSubmissionUploadFragment.makeRoute(canvasContext, assignment, false))
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
