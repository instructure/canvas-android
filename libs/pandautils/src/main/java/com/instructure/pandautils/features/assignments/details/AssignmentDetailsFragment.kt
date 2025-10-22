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

package com.instructure.pandautils.features.assignments.details

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.interactions.bookmarks.Bookmarkable
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouterParams
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_ASSIGNMENT_DETAILS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.databinding.FragmentAssignmentDetailsBinding
import com.instructure.pandautils.features.assignments.details.composables.DiscussionCheckpointLayout
import com.instructure.pandautils.features.assignments.details.composables.DueDateReminderLayout
import com.instructure.pandautils.features.shareextension.ShareFileSubmissionTarget
import com.instructure.pandautils.navigation.WebViewRouter
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.needsPermissions
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.utils.withArgs
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.pandautils.views.RecordingMediaType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@ScreenView(SCREEN_VIEW_ASSIGNMENT_DETAILS)
@PageView(url = "courses/{courseId}/assignments/{assignmentId}")
@AndroidEntryPoint
class AssignmentDetailsFragment : BaseCanvasFragment(), FragmentInteractions, Bookmarkable {

    @Inject
    lateinit var assignmentDetailsRouter: AssignmentDetailsRouter

    @Inject
    lateinit var webViewRouter: WebViewRouter

    @Inject
    lateinit var assignmentDetailsBehaviour: AssignmentDetailsBehaviour

    override val navigation: Navigation? = null

    @get:PageViewUrlParam(name = "assignmentId")
    val assignmentId by LongArg(key = Const.ASSIGNMENT_ID)

    @get:PageViewUrlParam(name = "courseId")
    val courseId by LongArg(key = Const.COURSE_ID, default = 0)

    val submissionId by LongArg(key = Const.SUBMISSION_ID, default = -1)

    private var binding: FragmentAssignmentDetailsBinding? = null
    private val viewModel: AssignmentDetailsViewModel by viewModels()

    private var captureVideoUri: Uri? = null
    private val captureVideoContract = registerForActivityResult(ActivityResultContracts.CaptureVideo()) {
        val assignment = viewModel.assignment
        val course = viewModel.course.value
        if (assignment != null && captureVideoUri != null && it && course != null) {
            assignmentDetailsRouter.navigateToAssignmentUploadPicker(requireActivity(), course, assignment, captureVideoUri!!)
        } else {
            toast(R.string.videoRecordingError)
        }
    }

    private val mediaPickerContract = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        val assignment = viewModel.assignment
        val course = viewModel.course.value
        if (assignment != null && it != null && course != null) {
            assignmentDetailsRouter.navigateToAssignmentUploadPicker(requireActivity(), course, assignment, it)
        } else {
            toast(R.string.unexpectedErrorOpeningFile)
        }
    }

    private val notificationsPermissionContract = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override val bookmark: Bookmarker by lazy { viewModel.bookmarker }

    override fun applyTheme() {
        binding?.toolbar?.apply {
            setupAsBackButton {
                activity?.onBackPressed()
            }

            title = context?.getString(R.string.assignmentDetails)

            assignmentDetailsBehaviour.applyTheme(requireActivity(), binding, bookmark, viewModel.course.value, this)
        }
    }

    override fun getFragment(): Fragment = this

    override fun title() = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAssignmentDetailsBinding.inflate(inflater, container, false)
        binding?.lifecycleOwner = this
        binding?.viewModel = viewModel
        viewModel.course.value?.let {
            viewModel.updateReminderColor(assignmentDetailsBehaviour.getThemeColor(it))
        }

        binding?.dueComposeView?.setContent {
            val states = viewModel.dueDateReminderViewStates
            DueDateReminderLayout(
                states,
                onAddClick = { tag -> checkAlarmPermission(tag) },
                onRemoveClick = { reminderId ->
                    viewModel.showDeleteReminderConfirmationDialog(
                        requireContext(),
                        reminderId,
                        assignmentDetailsBehaviour.dialogColor
                    )
                }
            )
        }

        binding?.checkpointGradesComposeView?.setContent {
            val checkpoints = viewModel.discussionCheckpoints
            DiscussionCheckpointLayout(checkpoints)
        }

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

        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state.isSuccessState()) {
                assignmentDetailsBehaviour.setupAppSpecificViews(
                    requireActivity(),
                    binding,
                    viewModel.course.value!!,
                    viewModel.assignment
                ) { options ->
                    assignmentDetailsRouter.navigateToSendMessage(requireActivity(), options)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkAlarmPermissionResult()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return assignmentDetailsBehaviour.onOptionsItemSelected(requireActivity(), item)
    }

    private fun handleAction(action: AssignmentDetailAction) {
        val canvasContext: CanvasContext = viewModel.course.value ?: run {
            toast(R.string.generalUnexpectedError)
            return
        }

        when (action) {
            is AssignmentDetailAction.ShowToast -> {
                toast(action.message)
            }
            is AssignmentDetailAction.NavigateToLtiScreen -> {
                webViewRouter.openLtiScreen(viewModel.course.value, action.url)
            }
            is AssignmentDetailAction.NavigateToSubmissionScreen -> {
                assignmentDetailsRouter.navigateToSubmissionScreen(
                    requireActivity(),
                    canvasContext,
                    assignmentId,
                    action.assignmentUrl,
                    action.isAssignmentEnhancementEnabled,
                    action.isObserver,
                    action.selectedSubmissionAttempt,
                    action.isQuiz
                )
            }
            is AssignmentDetailAction.NavigateToQuizScreen -> {
                assignmentDetailsRouter.navigateToQuizScreen(requireActivity(), canvasContext, action.quiz, action.quiz.url.orEmpty())
            }
            is AssignmentDetailAction.NavigateToDiscussionScreen -> {
                assignmentDetailsRouter.navigateToDiscussionScreen(requireActivity(), canvasContext, action.discussionTopicHeaderId)
            }
            is AssignmentDetailAction.NavigateToUploadScreen -> {
                assignmentDetailsRouter.navigateToUploadScreen(requireActivity(), canvasContext, action.assignment)
            }
            is AssignmentDetailAction.NavigateToTextEntryScreen -> {
                assignmentDetailsRouter.navigateToTextEntryScreen(requireActivity(), canvasContext, assignmentId, action.assignmentName, action.submittedText, action.isFailure)
            }
            is AssignmentDetailAction.NavigateToUrlSubmissionScreen -> {
                assignmentDetailsRouter.navigateToUrlSubmissionScreen(requireActivity(), canvasContext, assignmentId, action.assignmentName, action.submittedUrl, action.isFailure)
            }
            is AssignmentDetailAction.NavigateToAnnotationSubmissionScreen -> {
                action.assignment.submission?.id?.let { submissionId ->
                    assignmentDetailsRouter.navigateToAnnotationSubmissionScreen(
                        requireActivity(),
                        canvasContext,
                        action.assignment.annotatableAttachmentId,
                        submissionId,
                        action.assignment.id,
                        action.assignment.name.orEmpty()
                    )
                }
            }
            is AssignmentDetailAction.NavigateToLtiLaunchScreen -> {
                assignmentDetailsRouter.navigateToLtiLaunchScreen(
                    requireActivity(),
                    canvasContext,
                    action.ltiTool?.url.orEmpty(),
                    action.title,
                    isAssignmentLTI = true,
                    ltiTool = action.ltiTool,
                    openInternally = action.openInternally
                )
            }
            is AssignmentDetailAction.ShowMediaDialog -> {
                assignmentDetailsBehaviour.showMediaDialog(
                    requireActivity(),
                    binding,
                    { viewModel.uploadAudioSubmission(context, it) },
                    ::startVideoCapture,
                    { mediaPickerContract.launch(arrayOf("video/*", "audio/*")) }
                )
            }
            is AssignmentDetailAction.ShowSubmitDialog -> {
                viewModel.course.value?.let {
                    assignmentDetailsBehaviour.showSubmitDialog(
                        requireActivity(),
                        binding,
                        { viewModel.uploadAudioSubmission(context, it) },
                        ::startVideoCapture,
                        { mediaPickerContract.launch(arrayOf("video/*", "audio/*")) },
                        action.assignment,
                        it,
                        viewModel.isStudioAccepted(),
                        action.studioLTITool
                    )
                }
            }
            is AssignmentDetailAction.NavigateToUploadStatusScreen -> {
                assignmentDetailsRouter.navigateToUploadStatusScreen(requireActivity(), action.submissionId)
            }
            is AssignmentDetailAction.OnDiscussionHeaderAttachmentClicked -> {
                action.attachments.firstOrNull()?.let {
                    assignmentDetailsRouter.navigateToDiscussionAttachmentScreen(requireActivity(), canvasContext, it)
                }
            }
            is AssignmentDetailAction.ShowReminderDialog -> {
                checkAlarmPermission()
            }
            is AssignmentDetailAction.ShowDeleteReminderConfirmationDialog -> {
                viewModel.showDeleteReminderConfirmationDialog(requireContext(), action.reminderId, assignmentDetailsBehaviour.dialogColor)
            }
            is AssignmentDetailAction.NavigateToSendMessage -> {
                assignmentDetailsRouter.navigateToSendMessage(requireActivity(), action.options)
            }
        }
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

        captureVideoUri = viewModel.getVideoUri(requireActivity())
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
        binding?.descriptionWebViewWrapper?.webView?.addVideoClient(requireActivity())
        binding?.descriptionWebViewWrapper?.webView?.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                webViewRouter.openMedia(url)
            }

            override fun onPageFinishedCallback(webView: WebView, url: String) {}
            override fun onPageStartedCallback(webView: WebView, url: String) {}
            override fun canRouteInternallyDelegate(url: String): Boolean {
                return webViewRouter.canRouteInternally(url, false)
            }

            override fun routeInternallyCallback(url: String) {
                val extras = viewModel.assignment?.let { assignment ->
                    viewModel.course.value?.let { course ->
                        Bundle().apply { putParcelable(Const.SUBMISSION_TARGET, ShareFileSubmissionTarget(course, assignment)) }
                    }
                }
                webViewRouter.routeInternally(url, extras)
            }
        }

        binding?.descriptionWebViewWrapper?.webView?.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
            override fun launchInternalWebViewFragment(url: String) {
                viewModel.course.value?.let {
                    webViewRouter.launchInternalWebViewFragment(url, it)
                }
            }

            override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
        }

        binding?.descriptionWebViewWrapper?.webView?.apply {
            focusable = View.NOT_FOCUSABLE
            isFocusableInTouchMode = false
        }
    }

    private fun checkAlarmPermission(tag: String? = null) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && requireActivity().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            viewModel.checkingNotificationPermission = true
            notificationsPermissionContract.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                viewModel.showCreateReminderDialog(requireActivity(), assignmentDetailsBehaviour.dialogColor, tag)
            } else {
                viewModel.checkingReminderPermission = true
                startActivity(
                    Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:" + requireContext().packageName)
                    )
                )
            }
        } else {
            viewModel.showCreateReminderDialog(requireActivity(), assignmentDetailsBehaviour.dialogColor, tag)
        }
    }

    private fun checkAlarmPermissionResult() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && viewModel.checkingNotificationPermission) {
            if (requireActivity().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                checkAlarmPermission()
            } else {
                Snackbar.make(requireView(), getString(R.string.notificationPermissionNotGrantedError), Snackbar.LENGTH_LONG).show()
            }
            viewModel.checkingNotificationPermission = false
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && viewModel.checkingReminderPermission) {
            if ((requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()) {
                viewModel.showCreateReminderDialog(requireActivity(), assignmentDetailsBehaviour.dialogColor)
            } else {
                Snackbar.make(requireView(), getString(R.string.reminderPermissionNotGrantedError), Snackbar.LENGTH_LONG).show()
            }
            viewModel.checkingReminderPermission = false
        }
    }

    companion object {
        fun makeRoute(course: CanvasContext, assignmentId: Long): Route {
            val bundle = course.makeBundle {
                putLong(Const.ASSIGNMENT_ID, assignmentId)
                putLong(Const.COURSE_ID, course.id)
            }
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
                route.arguments.putLong(Const.SUBMISSION_ID, route.paramsHash[RouterParams.SUBMISSION_ID]?.toLong().orDefault())
            }

            if (route.paramsHash.containsKey(RouterParams.COURSE_ID)) {
                route.arguments.putLong(Const.COURSE_ID, route.paramsHash[RouterParams.COURSE_ID]?.toLong().orDefault())
            }

            return AssignmentDetailsFragment().withArgs(route.arguments)
        }
    }
}
