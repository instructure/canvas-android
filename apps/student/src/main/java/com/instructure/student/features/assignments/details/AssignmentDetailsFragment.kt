/*
 * Copyright (C) 2022 - present Instructure, Inc.
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

package com.instructure.student.features.assignments.details

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.models.Assignment.SubmissionType
import com.instructure.canvasapi2.utils.*
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.analytics.SCREEN_VIEW_ASSIGNMENT_DETAILS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.features.discussion.router.DiscussionRouterFragment
import com.instructure.pandautils.features.shareextension.ShareFileSubmissionTarget
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.pandautils.views.RecordingMediaType
import com.instructure.student.R
import com.instructure.student.activity.BaseRouterActivity
import com.instructure.student.databinding.DialogSubmissionPickerBinding
import com.instructure.student.databinding.DialogSubmissionPickerMediaBinding
import com.instructure.student.databinding.FragmentAssignmentDetailsBinding
import com.instructure.student.fragment.*
import com.instructure.student.mobius.assignmentDetails.getVideoUri
import com.instructure.student.mobius.assignmentDetails.launchAudio
import com.instructure.student.mobius.assignmentDetails.needsPermissions
import com.instructure.student.mobius.assignmentDetails.submission.annnotation.AnnotationSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.file.ui.UploadStatusSubmissionFragment
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionMode
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.text.ui.TextSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submission.url.ui.UrlSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsRepositoryFragment
import com.instructure.student.router.RouteMatcher
import com.instructure.student.util.getResourceSelectorUrl
import dagger.hilt.android.AndroidEntryPoint

@ScreenView(SCREEN_VIEW_ASSIGNMENT_DETAILS)
@PageView(url = "{canvasContext}/assignments/{assignmentId}")
@AndroidEntryPoint
class AssignmentDetailsFragment : ParentFragment(), Bookmarkable {

    @get:PageViewUrlParam(name = "assignmentId")
    val assignmentId by LongArg(key = Const.ASSIGNMENT_ID)
    val canvasContext by ParcelableArg<Course>(key = Const.CANVAS_CONTEXT)

    private var binding: FragmentAssignmentDetailsBinding? = null
    private val viewModel: AssignmentDetailsViewModel by viewModels()

    private var captureVideoUri: Uri? = null
    private val captureVideoContract = registerForActivityResult(ActivityResultContracts.CaptureVideo()) {
        val assignment = viewModel.assignment
        if (assignment != null && captureVideoUri != null && it) {
            RouteMatcher.route(requireContext(), PickerSubmissionUploadFragment.makeRoute(canvasContext, assignment, captureVideoUri!!))
        } else {
            toast(R.string.videoRecordingError)
        }
    }

    private val mediaPickerContract = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        val assignment = viewModel.assignment
        if (assignment != null && it != null) {
            RouteMatcher.route(requireContext(), PickerSubmissionUploadFragment.makeRoute(canvasContext, assignment, it))
        } else {
            toast(R.string.unexpectedErrorOpeningFile)
        }
    }

    override val bookmark: Bookmarker by lazy { viewModel.bookmarker }

    override fun applyTheme() {
        binding?.toolbar?.apply {
            setupAsBackButton {
                activity?.onBackPressed()
            }

            title = context?.getString(R.string.assignmentDetails)
            subtitle = viewModel.course?.name

            setupToolbarMenu(this)

            ViewStyler.themeToolbarColored(requireActivity(), this, viewModel.course)
        }
    }

    override fun title() = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAssignmentDetailsBinding.inflate(inflater, container, false)
        binding?.lifecycleOwner = this
        binding?.viewModel = viewModel
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyTheme()
        setupDescriptionView()

        viewModel.events.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        }
    }

    private fun handleAction(action: AssignmentDetailAction) {
        val canvasContext = canvasContext as? CanvasContext ?: run {
            toast(R.string.generalUnexpectedError)
            return
        }

        when (action) {
            is AssignmentDetailAction.ShowToast -> {
                toast(action.message)
            }
            is AssignmentDetailAction.NavigateToLtiScreen -> {
                LtiLaunchFragment.routeLtiLaunchFragment(requireActivity(), viewModel.course, action.url)
            }
            is AssignmentDetailAction.NavigateToSubmissionScreen -> {
                RouteMatcher.route(
                    requireActivity(),
                    SubmissionDetailsRepositoryFragment.makeRoute(canvasContext, assignmentId, action.isObserver, action.selectedSubmissionAttempt)
                )
            }
            is AssignmentDetailAction.NavigateToQuizScreen -> {
                RouteMatcher.route(requireActivity(), BasicQuizViewFragment.makeRoute(canvasContext, action.quiz, action.quiz.url.orEmpty()))
            }
            is AssignmentDetailAction.NavigateToDiscussionScreen -> {
                RouteMatcher.route(requireActivity(), DiscussionRouterFragment.makeRoute(canvasContext, action.discussionTopicHeaderId))
            }
            is AssignmentDetailAction.NavigateToUploadScreen -> navigateToUploadScreen(action.assignment)
            is AssignmentDetailAction.NavigateToTextEntryScreen -> navigateToTextEntryScreen(
                action.assignmentName,
                action.submittedText,
                action.isFailure
            )
            is AssignmentDetailAction.NavigateToUrlSubmissionScreen -> navigateToUrlSubmissionScreen(
                action.assignmentName,
                action.submittedUrl,
                action.isFailure
            )
            is AssignmentDetailAction.NavigateToAnnotationSubmissionScreen -> navigateToAnnotationSubmissionScreen(action.assignment)
            is AssignmentDetailAction.NavigateToLtiLaunchScreen -> {
                RouteMatcher.route(
                    requireActivity(), LtiLaunchFragment.makeRoute(
                        canvasContext,
                        action.ltiTool?.url.orEmpty(),
                        action.title,
                        isAssignmentLTI = true,
                        ltiTool = action.ltiTool
                    )
                )
            }
            is AssignmentDetailAction.ShowMediaDialog -> {
                showMediaDialog()
            }
            is AssignmentDetailAction.ShowSubmitDialog -> {
                showSubmitDialogView(action.assignment, action.studioLTITool)
            }
            is AssignmentDetailAction.NavigateToUploadStatusScreen -> {
                RouteMatcher.route(requireActivity(), UploadStatusSubmissionFragment.makeRoute(action.submissionId))
            }
            is AssignmentDetailAction.OnDiscussionHeaderAttachmentClicked -> {
                showDiscussionAttachments(action.attachments)
            }
        }
    }

    private fun navigateToTextEntryScreen(assignmentName: String?, submittedText: String? = null, isFailure: Boolean = false) {
        Analytics.logEvent(AnalyticsEventConstants.SUBMIT_TEXTENTRY_SELECTED)
        RouteMatcher.route(
            requireActivity(),
            TextSubmissionUploadFragment.makeRoute(canvasContext, assignmentId, assignmentName, submittedText, isFailure)
        )
    }

    private fun navigateToUrlSubmissionScreen(assignmentName: String?, submittedUrl: String? = null, isFailure: Boolean = false) {
        Analytics.logEvent(AnalyticsEventConstants.SUBMIT_ONLINEURL_SELECTED)
        RouteMatcher.route(
            requireActivity(),
            UrlSubmissionUploadFragment.makeRoute(canvasContext, assignmentId, assignmentName, submittedUrl, isFailure)
        )
    }

    private fun navigateToUploadScreen(assignment: Assignment) {
        Analytics.logEvent(AnalyticsEventConstants.SUBMIT_FILEUPLOAD_SELECTED)
        RouteMatcher.route(
            requireActivity(),
            PickerSubmissionUploadFragment.makeRoute(canvasContext, assignment, PickerSubmissionMode.FileSubmission)
        )
    }

    private fun navigateToAnnotationSubmissionScreen(assignment: Assignment) {
        assignment.submission?.id?.let {
            Analytics.logEvent(AnalyticsEventConstants.SUBMIT_STUDENT_ANNOTATION_SELECTED)
            RouteMatcher.route(
                requireActivity(),
                AnnotationSubmissionUploadFragment.makeRoute(
                    canvasContext,
                    assignment.annotatableAttachmentId,
                    it,
                    assignment.id,
                    assignment.name.orEmpty()
                )
            )
        } ?: toast(R.string.generalUnexpectedError)
    }

    private fun navigateToStudioScreen(assignment: Assignment, studioLTITool: LTITool?) {
        Analytics.logEvent(AnalyticsEventConstants.SUBMIT_STUDIO_SELECTED)
        RouteMatcher.route(
            requireActivity(),
            StudioWebViewFragment.makeRoute(
                canvasContext,
                studioLTITool?.getResourceSelectorUrl(canvasContext, assignment).orEmpty(),
                studioLTITool?.name.orEmpty(),
                true,
                assignment
            )
        )
    }

    private fun setupDialogRow(dialog: Dialog, view: View, visibility: Boolean, onClick: () -> Unit) {
        view.setVisible(visibility)
        view.setOnClickListener {
            onClick()
            dialog.cancel()
        }
    }

    private fun showSubmitDialogView(assignment: Assignment, studioLTITool: LTITool?) {
        val builder = AlertDialog.Builder(requireContext())
        val dialogBinding = DialogSubmissionPickerBinding.inflate(layoutInflater)
        val dialog = builder.setView(dialogBinding.root).create()
        val submissionTypes = assignment.getSubmissionTypes()

        dialog.setOnShowListener {
            setupDialogRow(dialog, dialogBinding.submissionEntryText, submissionTypes.contains(SubmissionType.ONLINE_TEXT_ENTRY)) {
                navigateToTextEntryScreen(assignment.name)
            }
            setupDialogRow(dialog, dialogBinding.submissionEntryWebsite, submissionTypes.contains(SubmissionType.ONLINE_URL)) {
                navigateToUrlSubmissionScreen(assignment.name)
            }
            setupDialogRow(dialog, dialogBinding.submissionEntryFile, submissionTypes.contains(SubmissionType.ONLINE_UPLOAD)) {
                navigateToUploadScreen(assignment)
            }
            setupDialogRow(dialog, dialogBinding.submissionEntryMedia, submissionTypes.contains(SubmissionType.MEDIA_RECORDING)) {
                showMediaDialog()
            }
            setupDialogRow(
                dialog,
                dialogBinding.submissionEntryStudio,
                (submissionTypes.contains(SubmissionType.ONLINE_UPLOAD) && assignment.isStudioEnabled)
            ) {
                navigateToStudioScreen(assignment, studioLTITool)
            }
            setupDialogRow(dialog, dialogBinding.submissionEntryStudentAnnotation, submissionTypes.contains(SubmissionType.STUDENT_ANNOTATION)) {
                navigateToAnnotationSubmissionScreen(assignment)
            }
        }
        dialog.show()
    }

    private fun showMediaDialog() {
        Analytics.logEvent(AnalyticsEventConstants.SUBMIT_MEDIARECORDING_SELECTED)
        val builder = AlertDialog.Builder(requireContext())
        val dialogBinding = DialogSubmissionPickerMediaBinding.inflate(layoutInflater)
        val dialog = builder.setView(dialogBinding.root).create()

        dialog.setOnShowListener {
            setupDialogRow(dialog, dialogBinding.submissionEntryAudio, true) {
                activity?.launchAudio({ toast(R.string.permissionDenied) }, ::showAudioRecordingView)
            }
            setupDialogRow(dialog, dialogBinding.submissionEntryVideo, true) {
                startVideoCapture()
            }
            setupDialogRow(dialog, dialogBinding.submissionEntryMediaFile, true) {
                mediaPickerContract.launch(arrayOf("video/*", "audio/*"))
            }
        }
        dialog.show()
    }

    private fun startVideoCapture() {
        if (activity?.needsPermissions({
                startVideoCapture()
            }, {
                toast(R.string.permissionDenied)
            },
                PermissionUtils.CAMERA,
                PermissionUtils.RECORD_AUDIO
            ).orDefault()
        ) {
            return
        }

        captureVideoUri = requireActivity().getVideoUri()
        captureVideoContract.launch(captureVideoUri)
    }

    private fun showAudioRecordingView() {
        binding?.floatingRecordingView?.apply {
            setContentType(RecordingMediaType.Audio)
            setVisible()
            stoppedCallback = {}
            recordingCallback = {
                viewModel.uploadAudioSubmission(context, it)
            }
        }
    }

    private fun setupDescriptionView() {
        binding?.descriptionWebViewWrapper?.webView?.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                RouteMatcher.openMedia(requireActivity(), url)
            }

            override fun onPageFinishedCallback(webView: WebView, url: String) {}
            override fun onPageStartedCallback(webView: WebView, url: String) {}
            override fun canRouteInternallyDelegate(url: String): Boolean {
                return RouteMatcher.canRouteInternally(requireActivity(), url, ApiPrefs.domain, false)
            }

            override fun routeInternallyCallback(url: String) {
                viewModel.assignment?.let {
                    val extras = Bundle().apply { putParcelable(Const.SUBMISSION_TARGET, ShareFileSubmissionTarget(canvasContext, it)) }
                    RouteMatcher.routeUrl(requireActivity(), url, ApiPrefs.domain, extras)
                }
            }
        }

        binding?.descriptionWebViewWrapper?.webView?.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
            override fun launchInternalWebViewFragment(url: String) {
                InternalWebviewFragment.loadInternalWebView(
                    context,
                    InternalWebviewFragment.makeRoute(canvasContext, url, false)
                )
            }

            override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
        }

        binding?.descriptionWebViewWrapper?.webView?.apply {
            focusable = View.NOT_FOCUSABLE
            isFocusableInTouchMode = false
        }
    }

    private fun showDiscussionAttachments(attachments: List<RemoteFile>) {
        val discussionAttachment = attachments.firstOrNull() ?: return
        (requireActivity() as BaseRouterActivity).openMedia(
            canvasContext,
            discussionAttachment.contentType.orEmpty(),
            discussionAttachment.url.orEmpty(),
            discussionAttachment.fileName.orEmpty()
        )
    }

    companion object {
        fun makeRoute(course: CanvasContext, assignmentId: Long): Route {
            val bundle = course.makeBundle { putLong(Const.ASSIGNMENT_ID, assignmentId) }
            return Route(null, AssignmentDetailsFragment::class.java, course, bundle)
        }

        private fun validRoute(route: Route): Boolean {
            return route.canvasContext is Course && (route.arguments.containsKey(Const.ASSIGNMENT_ID) || route.paramsHash.containsKey(RouterParams.ASSIGNMENT_ID))
        }

        fun newInstance(route: Route): AssignmentDetailsFragment? {
            if (!validRoute(route)) return null

            // If routed from a URL, set the bundle's assignment ID from the url value
            if (route.paramsHash.containsKey(RouterParams.ASSIGNMENT_ID)) {
                val assignmentId = route.paramsHash[RouterParams.ASSIGNMENT_ID]?.toLong().orDefault()
                route.arguments.putLong(Const.ASSIGNMENT_ID, assignmentId)
                // Clear API cache when routing from a URL so we fetch fresh data from the network
                CanvasRestAdapter.clearCacheUrls("assignments/$assignmentId")
            }

            if (route.paramsHash.containsKey(RouterParams.SUBMISSION_ID)) {
                // Indicate that we want to route to the Submission Details page - this will give us a small backstack, allowing the user to hit back and go to Assignment Details instead
                // of closing the app (in the case of when the app isn't running and the user hits a push notification that takes them to Submission Details)
                route.arguments.putString(Const.SUBMISSION_ID, route.paramsHash[RouterParams.SUBMISSION_ID])
            }

            return AssignmentDetailsFragment().withArgs(route.arguments)
        }
    }
}
