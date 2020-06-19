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
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Pronouns
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.interactions.Navigation
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.pandautils.views.RecordingMediaType
import com.instructure.student.R
import com.instructure.student.activity.BaseRouterActivity
import com.instructure.student.activity.InternalWebViewActivity
import com.instructure.student.activity.ShareFileSubmissionTarget
import com.instructure.student.fragment.*
import com.instructure.student.mobius.assignmentDetails.AssignmentDetailsEvent
import com.instructure.student.mobius.assignmentDetails.submission.file.ui.UploadStatusSubmissionFragment
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionMode
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.text.ui.TextSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.url.ui.UrlSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsFragment
import com.instructure.student.mobius.common.ui.MobiusView
import com.instructure.student.router.RouteMatcher
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.dialog_submission_picker.*
import kotlinx.android.synthetic.main.dialog_submission_picker_media.*
import kotlinx.android.synthetic.main.fragment_assignment_details.*
import kotlinx.coroutines.Job
import java.net.URLDecoder

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

    private var loadHtmlJob: Job? = null

    init {
        toolbar.setupAsBackButton { (context as? Activity)?.onBackPressed() }
        toolbar.title = context.getString(R.string.assignmentDetails)
        toolbar.subtitle = canvasContext.name
        toolbar.setMenu(R.menu.bookmark_menu) { consumer?.accept(AssignmentDetailsEvent.AddBookmarkClicked) }
        submissionStatusFailedSubtitle.setTextColor(ThemePrefs.buttonColor)
        submissionStatusUploadingSubtitle.setTextColor(ThemePrefs.buttonColor)
        submissionAndRubricLabel.setTextColor(ThemePrefs.buttonColor)
        submitButton.setBackgroundColor(ThemePrefs.buttonColor)
        submitButton.setTextColor(ThemePrefs.buttonTextColor)
    }

    override fun applyTheme() {
        ViewStyler.themeToolbar(context as Activity, toolbar, canvasContext)
    }

    override fun onConnect(output: Consumer<AssignmentDetailsEvent>) {
        submissionStatusFailed.onClick { output.accept(AssignmentDetailsEvent.ViewUploadStatusClicked) }
        submissionStatusUploading.onClick { output.accept(AssignmentDetailsEvent.ViewUploadStatusClicked) }
        submissionRubricButton.onClick { output.accept(AssignmentDetailsEvent.ViewSubmissionClicked) }
        gradeContainer.onClick { output.accept(AssignmentDetailsEvent.ViewSubmissionClicked) }
        submitButton.onClick {
            logEvent(AnalyticsEventConstants.ASSIGNMENT_SUBMIT_SELECTED)
            output.accept(AssignmentDetailsEvent.SubmitAssignmentClicked)
        }
        attachmentIcon.onClick { output.accept(AssignmentDetailsEvent.DiscussionAttachmentClicked) }
        swipeRefreshLayout.setOnRefreshListener { output.accept(AssignmentDetailsEvent.PullToRefresh) }
        setupDescriptionView()
    }

    private fun setupDescriptionView() {
        descriptionWebView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                RouteMatcher.openMedia(context as FragmentActivity, url)
            }
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
            submissionStatusIcon.setVisible(visibilities.submissionStatus)
            submissionStatus.setVisible(visibilities.submissionStatus)
            dueDateContainer.setVisible(visibilities.dueDate)
            submissionTypesContainer.setVisible(visibilities.submissionTypes)
            fileTypesContainer.setVisible(visibilities.fileTypes)
            gradeContainer.setVisible(visibilities.grade)
            lockMessageContainer.setVisible(visibilities.lockedMessage)
            submissionRubricButton.setVisible(visibilities.submissionAndRubricButton)
            lockImageContainer.setVisible(visibilities.lockedImage)
            noDescriptionContainer.setVisible(visibilities.noDescriptionLabel)
            descriptionWebView.setVisible(visibilities.description)
            allowedAttemptsContainer.setVisible(visibilities.allowedAttempts)
            submitButton.isEnabled = visibilities.submitButtonEnabled
            if (visibilities.submitButtonEnabled) {
                submitButton.alpha = 1f
            } else {
                submitButton.alpha = 0.2f
            }
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
        points.contentDescription = state.assignmentPointsA11yText
        submissionStatusIcon.setImageResource(state.submittedStateIcon)
        submissionStatusIcon.imageTintList = ColorStateList.valueOf(state.submittedStateColor)
        submissionStatus.text = state.submittedStateLabel
        submissionStatus.setTextColor(state.submittedStateColor)
        dueDateTextView.text = state.dueDate
        lockMessageTextView.text = state.lockMessage
        submissionTypesTextView.text = state.submissionTypes
        fileTypesTextView.text = state.fileTypes
        allowedAttemptsText.text = state.allowedAttempts.toString()
        usedAttemptsText.text = state.usedAttempts.toString()
        gradeCell.setState(state.gradeState)
        submitButton.text = state.submitButtonText
        if (state.visibilities.description) {
            descriptionLabel.text = state.descriptionLabel
            loadHtmlJob = descriptionWebView.loadHtmlWithIframes(context, false, state.description, ::loadDescriptionHtml,{
                val args = LTIWebViewFragment.makeLTIBundle(
                        URLDecoder.decode(it, "utf-8"), context.getString(R.string.utils_externalToolTitle), true)
                RouteMatcher.route(context, Route(LTIWebViewFragment::class.java, canvasContext, args))
            }, state.assignmentName)
        }
        if(state.visibilities.quizDetails) renderQuizDetails(state.quizDescriptionViewState!!)
        if(state.visibilities.discussionTopicHeader) renderDiscussionTopicHeader(state.discussionHeaderViewState!!)
    }

    private fun loadDescriptionHtml(html: String, contentDescrption: String?) {
        descriptionWebView.loadHtml(html, contentDescrption)
    }

    private fun renderQuizDetails(quizDescriptionViewState: QuizDescriptionViewState) {
        questionCountText.text = quizDescriptionViewState.questionCount
        timeLimitText.text = quizDescriptionViewState.timeLimit
        allowedQuizAttemptsText.text = quizDescriptionViewState.allowedAttempts
    }

    private fun renderDiscussionTopicHeader(discussionHeaderViewState: DiscussionHeaderViewState) {
        if(discussionHeaderViewState is DiscussionHeaderViewState.Loaded) {
            ProfileUtils.loadAvatarForUser(authorAvatar, discussionHeaderViewState.authorName, discussionHeaderViewState.authorAvatarUrl)
            authorAvatar.setupAvatarA11y(discussionHeaderViewState.authorName)
            authorName.text = Pronouns.span(
                discussionHeaderViewState.authorName,
                discussionHeaderViewState.authorPronouns
            )
            authoredDate.text = discussionHeaderViewState.authoredDate
            attachmentIcon.setVisible(discussionHeaderViewState.attachmentIconVisibility)
        } else {
            discussionTopicHeaderContainer.setVisible(false)
        }
    }

    override fun onDispose() {
        loadHtmlJob?.cancel()
        descriptionWebView.stopLoading()
    }

    fun showSubmitDialogView(assignment: Assignment, courseId: Long, visibilities: SubmissionTypesVisibilities, ltiToolUrl: String? = null, ltiToolName: String? = null) {
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
                showMediaRecordingView(assignment)
            }
            setupDialogRow(dialog, dialog.submissionEntryStudio, visibilities.studioUpload) {
                // The LTI info shouldn't be null if we are showing the Studio upload option
                showStudioUploadView(assignment, ltiToolUrl!!, ltiToolName!!)
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

    fun showSubmissionView(assignmentId: Long, course: Course, isObserver: Boolean = false) {
        logEvent(AnalyticsEventConstants.SUBMISSION_CELL_SELECTED)
        RouteMatcher.route(context, SubmissionDetailsFragment.makeRoute(course, assignmentId, isObserver))
    }

    fun showUploadStatusView(submissionId: Long) {
        RouteMatcher.route(context, UploadStatusSubmissionFragment.makeRoute(submissionId))
    }

    fun showOnlineTextEntryView(assignmentId: Long, assignmentName: String?, submittedText: String? = null, isFailure: Boolean = false) {
        logEvent(AnalyticsEventConstants.SUBMIT_TEXTENTRY_SELECTED)
        RouteMatcher.route(context, TextSubmissionUploadFragment.makeRoute(canvasContext, assignmentId, assignmentName, submittedText, isFailure))
    }

    fun showOnlineUrlEntryView(assignmentId: Long, assignmentName: String?, canvasContext: CanvasContext, submittedUrl: String? = null, isFailure: Boolean = false) {
        logEvent(AnalyticsEventConstants.SUBMIT_ONLINEURL_SELECTED)
        RouteMatcher.route(context, UrlSubmissionUploadFragment.makeRoute(canvasContext, assignmentId, assignmentName, submittedUrl, isFailure))
    }

    fun showLTIView(canvasContext: CanvasContext, url: String, title: String) {
        logEvent(AnalyticsEventConstants.ASSIGNMENT_LAUNCHLTI_SELECTED)
        RouteMatcher.route(context, LTIWebViewFragment.makeRoute(canvasContext, url, title, isAssignmentLTI = true))
    }

    fun showQuizStartView(canvasContext: CanvasContext, quiz: Quiz) {
        logEvent(AnalyticsEventConstants.ASSIGNMENT_DETAIL_QUIZLAUNCH)
        if (QuizListFragment.isNativeQuiz(canvasContext, quiz)) {
            RouteMatcher.route(context, QuizStartFragment.makeRoute(canvasContext, quiz))
        } else {
            RouteMatcher.route(context, BasicQuizViewFragment.makeRoute(canvasContext, quiz, quiz.url!!))
        }
    }

    fun showDiscussionDetailView(canvasContext: CanvasContext, discussionTopicHeaderId: Long) {
        logEvent(AnalyticsEventConstants.ASSIGNMENT_DETAIL_DISCUSSIONLAUNCH)
        RouteMatcher.route(context, DiscussionDetailsFragment.makeRoute(canvasContext, discussionTopicHeaderId))
    }

    fun showDiscussionAttachment(canvasContext: CanvasContext, discussionAttachment: Attachment) {
        (context as BaseRouterActivity).openMedia(canvasContext, discussionAttachment.contentType ?: "", discussionAttachment.url ?: "", discussionAttachment.filename ?: "")
    }

    fun showMediaRecordingView(assignment: Assignment) {
        logEvent(AnalyticsEventConstants.SUBMIT_MEDIARECORDING_SELECTED)
        val builder = AlertDialog.Builder(context)
        val dialog = builder.setView(R.layout.dialog_submission_picker_media).create()

        dialog.setOnShowListener {
            setupDialogRow(dialog, dialog.submissionEntryAudio, true) {
                consumer?.accept(AssignmentDetailsEvent.AudioRecordingClicked)
            }
            setupDialogRow(dialog, dialog.submissionEntryVideo, true) {
                consumer?.accept(AssignmentDetailsEvent.VideoRecordingClicked)
            }

            setupDialogRow(dialog, dialog.submissionEntryMediaFile, true) {
                consumer?.accept(AssignmentDetailsEvent.ChooseMediaClicked)
            }
        }
        dialog.show()
    }

    fun showAudioRecordingView() {
        floatingRecordingView.setContentType(RecordingMediaType.Audio)
        floatingRecordingView.setVisible()
        floatingRecordingView.recordingCallback = { file ->
            consumer?.accept(AssignmentDetailsEvent.SendAudioRecordingClicked(file))
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
        logEvent(AnalyticsEventConstants.SUBMIT_FILEUPLOAD_SELECTED)
        RouteMatcher.route(
            context,
            PickerSubmissionUploadFragment.makeRoute(canvasContext, assignment, PickerSubmissionMode.FileSubmission)
        )
    }

    private fun showStudioUploadView(assignment: Assignment, ltiUrl: String, studioLtiToolName: String) {
        logEvent(AnalyticsEventConstants.SUBMIT_STUDIO_SELECTED)
        RouteMatcher.route(context, StudioWebViewFragment.makeRoute(canvasContext, ltiUrl, studioLtiToolName, true, assignment))
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

    fun showBookmarkDialog() = (context as? Navigation)?.addBookmark()
}
