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
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.APIHelper
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
import com.instructure.pandautils.compose.composables.ComposeEmbeddedWebViewCallbacks
import com.instructure.pandautils.compose.composables.ComposeWebViewCallbacks
import com.instructure.pandautils.features.assignments.details.composables.AssignmentDetailsScreen
import com.instructure.pandautils.features.shareextension.ShareFileSubmissionTarget
import com.instructure.pandautils.navigation.WebViewRouter
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.needsPermissions
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.utils.withArgs
import com.instructure.pandautils.views.FloatingRecordingView
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

    private val viewModel: AssignmentDetailsViewModel by viewModels()

    private var captureVideoUri: Uri? = null
    private val captureVideoContract = registerForActivityResult(ActivityResultContracts.CaptureVideo()) {
        val assignment = viewModel.assignment
        val course = viewModel.currentCourse
        if (assignment != null && captureVideoUri != null && it && course != null) {
            val nextAttempt = (assignment.submission?.attempt ?: 0) + 1
            assignmentDetailsRouter.navigateToAssignmentUploadPicker(requireActivity(), course, assignment, captureVideoUri!!, nextAttempt.toLong(), "camera")
        } else {
            toast(R.string.videoRecordingError)
        }
    }

    private val mediaPickerContract = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        val assignment = viewModel.assignment
        val course = viewModel.currentCourse
        if (assignment != null && it != null && course != null) {
            val nextAttempt = (assignment.submission?.attempt ?: 0) + 1
            assignmentDetailsRouter.navigateToAssignmentUploadPicker(requireActivity(), course, assignment, it, nextAttempt.toLong(), "library")
        } else {
            toast(R.string.unexpectedErrorOpeningFile)
        }
    }

    private val notificationsPermissionContract = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override val bookmark: Bookmarker by lazy { viewModel.bookmarker }

    override fun applyTheme() = Unit

    override fun getFragment(): Fragment = this

    override fun title() = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireActivity()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                val initialToolbarColor = remember { assignmentDetailsBehaviour.getThemeColor(Course(id = courseId)) }
                val toolbarColor = uiState.courseColor?.color() ?: initialToolbarColor

                LaunchedEffect(uiState.courseColor) {
                    viewModel.currentCourse?.let { course ->
                        viewModel.updateReminderColor(assignmentDetailsBehaviour.getThemeColor(course))
                        assignmentDetailsBehaviour.applyStatusBarTheme(requireActivity(), course)
                    }
                }

                LaunchedEffect(uiState.navigation) {
                    uiState.navigation?.let { navigation ->
                        handleNavigation(navigation)
                        uiState.onNavigationHandled()
                    }
                }

                LaunchedEffect(uiState.toastMessage) {
                    uiState.toastMessage?.let { message ->
                        toast(message)
                        uiState.onToastShown()
                    }
                }

                LaunchedEffect(uiState.mediaDialogAssignment) {
                    if (uiState.mediaDialogAssignment != null) {
                        assignmentDetailsBehaviour.showMediaDialog(
                            requireActivity(),
                            { viewModel.onAudioRecordingRequested() },
                            ::startVideoCapture,
                            { mediaPickerContract.launch(arrayOf("video/*", "audio/*")) }
                        )
                        uiState.onMediaDialogShown()
                    }
                }

                LaunchedEffect(uiState.submitDialog) {
                    uiState.submitDialog?.let { dialog ->
                        viewModel.currentCourse?.let { course ->
                            assignmentDetailsBehaviour.showSubmitDialog(
                                requireActivity(),
                                { viewModel.onAudioRecordingRequested() },
                                ::startVideoCapture,
                                { mediaPickerContract.launch(arrayOf("video/*", "audio/*")) },
                                dialog.assignment,
                                course,
                                viewModel.isStudioAccepted(),
                                dialog.studioLTITool
                            )
                        }
                        uiState.onSubmitDialogShown()
                    }
                }

                LaunchedEffect(uiState.createReminder) {
                    uiState.createReminder?.let { reminder ->
                        checkAlarmPermission(reminder.tag)
                        uiState.onCreateReminderShown()
                    }
                }

                LaunchedEffect(uiState.deleteReminderId) {
                    uiState.deleteReminderId?.let { reminderId ->
                        viewModel.showDeleteReminderConfirmationDialog(requireContext(), reminderId, assignmentDetailsBehaviour.dialogColor)
                        uiState.onDeleteReminderShown()
                    }
                }

                val navigationHost = activity as? Navigation
                val bookmarkable = navigationHost?.canBookmark().orDefault() && viewModel.bookmarker.canBookmark
                val onAddBookmark: (() -> Unit)? = if (bookmarkable) {
                    {
                        if (APIHelper.hasNetworkConnection()) {
                            navigationHost?.addBookmark()
                        } else {
                            Toast.makeText(requireContext(), getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    null
                }

                val messageFab = remember(uiState.screenState) {
                    assignmentDetailsBehaviour.getMessageFabConfig(viewModel.currentCourse, viewModel.assignment)?.let { config ->
                        MessageFabUiState(
                            iconRes = config.iconRes,
                            contentDescription = config.contentDescription,
                            onClick = { assignmentDetailsRouter.navigateToSendMessage(requireActivity(), config.options) }
                        )
                    }
                }

                AssignmentDetailsScreen(
                    title = getString(R.string.assignmentDetails),
                    subtitle = viewModel.currentCourse?.name.orEmpty(),
                    uiState = uiState,
                    toolbarColor = toolbarColor,
                    navigationActionClick = { activity?.onBackPressed() },
                    webViewCallbacks = descriptionWebViewCallbacks(),
                    embeddedWebViewCallbacks = descriptionEmbeddedWebViewCallbacks(),
                    applyOnWebView = {
                        addVideoClient(requireActivity())
                        focusable = View.NOT_FOCUSABLE
                        isFocusableInTouchMode = false
                    },
                    onAddBookmark = onAddBookmark,
                    messageFab = messageFab,
                    recordingOverlay = {
                        if (uiState.showAudioRecording) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AndroidView(
                                    modifier = Modifier.align(Alignment.BottomStart),
                                    factory = { context ->
                                        FloatingRecordingView(context).apply {
                                            setContentType(RecordingMediaType.Audio)
                                            stoppedCallback = { viewModel.onAudioRecordingFinished() }
                                            recordingCallback = { file ->
                                                viewModel.uploadAudioSubmission(context, file)
                                                viewModel.onAudioRecordingFinished()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkAlarmPermissionResult()
    }

    private fun descriptionWebViewCallbacks() = ComposeWebViewCallbacks(
        openMedia = { _, url, _ -> webViewRouter.openMedia(url) },
        canRouteInternally = { url -> webViewRouter.canRouteInternally(url, false) },
        routeInternally = { url ->
            val extras = viewModel.assignment?.let { assignment ->
                viewModel.currentCourse?.let { course ->
                    Bundle().apply { putParcelable(Const.SUBMISSION_TARGET, ShareFileSubmissionTarget(course, assignment)) }
                }
            }
            webViewRouter.routeInternally(url, extras)
        }
    )

    private fun descriptionEmbeddedWebViewCallbacks() = ComposeEmbeddedWebViewCallbacks(
        shouldLaunchInternalWebViewFragment = { true },
        launchInternalWebViewFragment = { url ->
            viewModel.currentCourse?.let {
                webViewRouter.launchInternalWebViewFragment(url, it)
            }
        }
    )

    private fun handleNavigation(navigation: AssignmentDetailsNavigation) {
        val canvasContext: CanvasContext = viewModel.currentCourse ?: run {
            toast(R.string.generalUnexpectedError)
            return
        }

        when (navigation) {
            is AssignmentDetailsNavigation.LtiScreen -> {
                webViewRouter.openLtiScreen(viewModel.currentCourse, navigation.url)
            }
            is AssignmentDetailsNavigation.SubmissionScreen -> {
                assignmentDetailsRouter.navigateToSubmissionScreen(
                    requireActivity(),
                    canvasContext,
                    assignmentId,
                    navigation.assignmentUrl,
                    navigation.isAssignmentEnhancementEnabled,
                    navigation.isObserver,
                    navigation.selectedSubmissionAttempt,
                    navigation.isQuiz
                )
            }
            is AssignmentDetailsNavigation.QuizScreen -> {
                assignmentDetailsRouter.navigateToQuizScreen(requireActivity(), canvasContext, navigation.quiz, navigation.quiz.url.orEmpty())
            }
            is AssignmentDetailsNavigation.DiscussionScreen -> {
                assignmentDetailsRouter.navigateToDiscussionScreen(requireActivity(), canvasContext, navigation.discussionTopicHeaderId)
            }
            is AssignmentDetailsNavigation.UploadScreen -> {
                assignmentDetailsRouter.navigateToUploadScreen(requireActivity(), canvasContext, navigation.assignment)
            }
            is AssignmentDetailsNavigation.TextEntryScreen -> {
                assignmentDetailsRouter.navigateToTextEntryScreen(requireActivity(), canvasContext, assignmentId, navigation.assignmentName, navigation.submittedText, navigation.isFailure)
            }
            is AssignmentDetailsNavigation.UrlSubmissionScreen -> {
                assignmentDetailsRouter.navigateToUrlSubmissionScreen(requireActivity(), canvasContext, assignmentId, navigation.assignmentName, navigation.submittedUrl, navigation.isFailure)
            }
            is AssignmentDetailsNavigation.AnnotationSubmissionScreen -> {
                navigation.assignment.submission?.id?.let { submissionId ->
                    assignmentDetailsRouter.navigateToAnnotationSubmissionScreen(
                        requireActivity(),
                        canvasContext,
                        navigation.assignment.annotatableAttachmentId,
                        submissionId,
                        navigation.assignment.id,
                        navigation.assignment.name.orEmpty()
                    )
                }
            }
            is AssignmentDetailsNavigation.LtiLaunchScreen -> {
                assignmentDetailsRouter.navigateToLtiLaunchScreen(
                    requireActivity(),
                    canvasContext,
                    navigation.ltiTool?.url.orEmpty(),
                    navigation.title,
                    isAssignmentLTI = true,
                    ltiTool = navigation.ltiTool,
                    openInternally = navigation.openInternally
                )
            }
            is AssignmentDetailsNavigation.UploadStatusScreen -> {
                assignmentDetailsRouter.navigateToUploadStatusScreen(requireActivity(), navigation.submissionId)
            }
            is AssignmentDetailsNavigation.DiscussionAttachment -> {
                navigation.attachments.firstOrNull()?.let {
                    assignmentDetailsRouter.navigateToDiscussionAttachmentScreen(requireActivity(), canvasContext, it)
                }
            }
            is AssignmentDetailsNavigation.SendMessage -> {
                assignmentDetailsRouter.navigateToSendMessage(requireActivity(), navigation.options)
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
