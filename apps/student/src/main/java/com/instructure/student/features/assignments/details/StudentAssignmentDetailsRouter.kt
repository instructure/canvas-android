package com.instructure.student.features.assignments.details

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.RemoteFile
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.pandautils.databinding.FragmentAssignmentDetailsBinding
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsRouter
import com.instructure.pandautils.features.assignments.details.ReminderChoice
import com.instructure.pandautils.features.assignments.details.reminder.CustomReminderDialog
import com.instructure.pandautils.features.discussion.router.DiscussionRouterFragment
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.showThemed
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.views.RecordingMediaType
import com.instructure.student.R
import com.instructure.student.activity.BaseRouterActivity
import com.instructure.student.databinding.DialogSubmissionPickerBinding
import com.instructure.student.databinding.DialogSubmissionPickerMediaBinding
import com.instructure.student.fragment.BasicQuizViewFragment
import com.instructure.student.fragment.InternalWebviewFragment
import com.instructure.student.fragment.LtiLaunchFragment
import com.instructure.student.fragment.StudioWebViewFragment
import com.instructure.student.mobius.assignmentDetails.launchAudio
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsRepositoryFragment
import com.instructure.student.mobius.assignmentDetails.submissions.annnotation.AnnotationSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submissions.file.ui.UploadStatusSubmissionFragment
import com.instructure.student.mobius.assignmentDetails.submissions.picker.PickerSubmissionMode
import com.instructure.student.mobius.assignmentDetails.submissions.picker.ui.PickerSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submissions.text.ui.TextSubmissionUploadFragment
import com.instructure.student.mobius.assignmentDetails.submissions.url.ui.UrlSubmissionUploadFragment
import com.instructure.student.router.RouteMatcher
import com.instructure.student.util.getResourceSelectorUrl
import java.io.File

class StudentAssignmentDetailsRouter: AssignmentDetailsRouter {
    override fun navigateToAssignmentUploadPicker(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        assignment: Assignment,
        mediaUri: Uri
    ) {
        RouteMatcher.route(
            activity,
            PickerSubmissionUploadFragment.makeRoute(canvasContext, assignment, mediaUri)
        )
    }

    override fun navigateToLtiScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext?,
        url: String
    ) {
        LtiLaunchFragment.routeLtiLaunchFragment(activity, canvasContext, url)
    }

    override fun navigateToSubmissionScreen(
        activity: FragmentActivity,
        course: CanvasContext,
        assignmentId: Long,
        isObserver: Boolean,
        initialSelectedSubmissionAttempt: Long?
    ) {
        RouteMatcher.route(
            activity,
            SubmissionDetailsRepositoryFragment.makeRoute(course, assignmentId, isObserver, initialSelectedSubmissionAttempt)
        )
    }

    override fun navigateToQuizScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        quiz: Quiz,
        url: String
    ) {
        RouteMatcher.route(activity, BasicQuizViewFragment.makeRoute(canvasContext, quiz, url))
    }

    override fun navigateToDiscussionScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        discussionTopicHeaderId: Long,
        isAnnouncement: Boolean
    ) {
        RouteMatcher.route(activity, DiscussionRouterFragment.makeRoute(canvasContext, discussionTopicHeaderId, isAnnouncement))
    }

    override fun navigateToUploadScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        assignment: Assignment,
        attemptId: Long?
    ) {
        Analytics.logEvent(AnalyticsEventConstants.SUBMIT_FILEUPLOAD_SELECTED)
        RouteMatcher.route(
            activity,
            PickerSubmissionUploadFragment.makeRoute(canvasContext, assignment, PickerSubmissionMode.FileSubmission)
        )
    }

    override fun navigateToTextEntryScreen(
        activity: FragmentActivity,
        course: CanvasContext,
        assignmentId: Long,
        assignmentName: String?,
        initialText: String?,
        isFailure: Boolean
    ) {
        Analytics.logEvent(AnalyticsEventConstants.SUBMIT_TEXTENTRY_SELECTED)
        RouteMatcher.route(
            activity,
            TextSubmissionUploadFragment.makeRoute(course, assignmentId, assignmentName, initialText, isFailure)
        )
    }

    override fun navigateToUrlSubmissionScreen(
        activity: FragmentActivity,
        course: CanvasContext,
        assignmentId: Long,
        assignmentName: String?,
        initialUrl: String?,
        isFailure: Boolean
    ) {
        Analytics.logEvent(AnalyticsEventConstants.SUBMIT_ONLINEURL_SELECTED)
        RouteMatcher.route(
            activity,
            UrlSubmissionUploadFragment.makeRoute(course, assignmentId, assignmentName, initialUrl, isFailure)
        )
    }

    override fun navigateToAnnotationSubmissionScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        annotatableAttachmentId: Long,
        submissionId: Long,
        assignmentId: Long,
        assignmentName: String
    ) {
        Analytics.logEvent(AnalyticsEventConstants.SUBMIT_STUDENT_ANNOTATION_SELECTED)
        RouteMatcher.route(
            activity,
            AnnotationSubmissionUploadFragment.makeRoute(
                canvasContext,
                annotatableAttachmentId,
                submissionId,
                assignmentId,
                assignmentName
            )
        )
    }

    override fun navigateToLtiLaunchScreen(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        url: String,
        title: String?,
        sessionLessLaunch: Boolean,
        isAssignmentLTI: Boolean,
        ltiTool: LTITool?
    ) {
        RouteMatcher.route(
            activity,
            LtiLaunchFragment.makeRoute(
                canvasContext,
                url,
                title,
                sessionLessLaunch = sessionLessLaunch,
                isAssignmentLTI = isAssignmentLTI,
                ltiTool = ltiTool
            )
        )
    }

    override fun navigateToUploadStatusScreen(activity: FragmentActivity, submissionId: Long) {
        RouteMatcher.route(activity, UploadStatusSubmissionFragment.makeRoute(submissionId))
    }

    override fun navigateToDiscussionAttachmentScreen(activity: FragmentActivity, canvasContext: CanvasContext, attachment: RemoteFile) {
        (activity as? BaseRouterActivity)?.openMedia(
            canvasContext,
            attachment.contentType.orEmpty(),
            attachment.url.orEmpty(),
            attachment.fileName.orEmpty()
        )
    }

    override fun navigateToUrl(
        activity: FragmentActivity,
        url: String,
        domain: String,
        extras: Bundle?
    ) {
        RouteMatcher.routeUrl(activity, url, domain, extras)
    }

    override fun navigateToInternalWebView(
        activity: FragmentActivity,
        canvasContext: CanvasContext,
        url: String,
        authenticate: Boolean
    ) {
        InternalWebviewFragment.loadInternalWebView(
            activity,
            InternalWebviewFragment.makeRoute(canvasContext, url, authenticate)
        )
    }

    override fun openMedia(activity: FragmentActivity, url: String) {
        RouteMatcher.openMedia(activity, url)
    }

    override fun showMediaDialog(
        activity: FragmentActivity,
        binding: FragmentAssignmentDetailsBinding?,
        recordCallback: (File?) -> Unit,
        startVideoCapture: () -> Unit,
        onLaunchMediaPicker: () -> Unit,
    ) {
        Analytics.logEvent(AnalyticsEventConstants.SUBMIT_MEDIARECORDING_SELECTED)
        val builder = AlertDialog.Builder(activity)
        val dialogBinding = DialogSubmissionPickerMediaBinding.inflate(LayoutInflater.from(activity))
        val dialog = builder.setView(dialogBinding.root).create()

        dialog.setOnShowListener {
            setupDialogRow(dialog, dialogBinding.submissionEntryAudio, true) {
                activity.launchAudio({ activity.toast(R.string.permissionDenied) }) {
                    showAudioRecordingView(binding, recordCallback)
                }
            }
            setupDialogRow(dialog, dialogBinding.submissionEntryVideo, true) {
                startVideoCapture()
            }
            setupDialogRow(dialog, dialogBinding.submissionEntryMediaFile, true) {
                onLaunchMediaPicker()
            }
        }
        dialog.show()
    }


    private fun showAudioRecordingView(binding: FragmentAssignmentDetailsBinding?, recordCallback: (File?) -> Unit) {
        binding?.floatingRecordingView?.apply {
            setContentType(RecordingMediaType.Audio)
            setVisible()
            stoppedCallback = {}
            recordingCallback = {
                recordCallback(it)
            }
        }
    }

    override fun showSubmitDialog(
        activity: FragmentActivity,
        binding: FragmentAssignmentDetailsBinding?,
        recordCallback: (File?) -> Unit,
        startVideoCapture: () -> Unit,
        onLaunchMediaPicker: () -> Unit,
        assignment: Assignment,
        course: Course,
        isStudioEnabled: Boolean,
        studioLTITool: LTITool?
    ) {
        val builder = AlertDialog.Builder(activity)
        val dialogBinding = DialogSubmissionPickerBinding.inflate(LayoutInflater.from(activity))
        val dialog = builder.setView(dialogBinding.root).create()
        val submissionTypes = assignment.getSubmissionTypes()

        dialog.setOnShowListener {
            setupDialogRow(dialog, dialogBinding.submissionEntryText, submissionTypes.contains(Assignment.SubmissionType.ONLINE_TEXT_ENTRY)) {
                navigateToTextEntryScreen(
                    activity,
                    course,
                    assignment.id,
                    assignment.name.orEmpty(),
                )
            }
            setupDialogRow(dialog, dialogBinding.submissionEntryWebsite, submissionTypes.contains(Assignment.SubmissionType.ONLINE_URL)) {
                navigateToUrlSubmissionScreen(
                    activity,
                    course,
                    assignment.id,
                    assignment.name.orEmpty(),
                    null,
                    false
                )
            }
            setupDialogRow(dialog, dialogBinding.submissionEntryFile, submissionTypes.contains(Assignment.SubmissionType.ONLINE_UPLOAD)) {
                navigateToUploadScreen(activity, course, assignment)
            }
            setupDialogRow(dialog, dialogBinding.submissionEntryMedia, submissionTypes.contains(
                Assignment.SubmissionType.MEDIA_RECORDING)) {
                showMediaDialog(activity, binding, recordCallback, startVideoCapture, onLaunchMediaPicker)
            }
            setupDialogRow(
                dialog,
                dialogBinding.submissionEntryStudio,
                isStudioEnabled
            ) {
                navigateToStudioScreen(activity, course, assignment, studioLTITool)
            }
            setupDialogRow(dialog, dialogBinding.submissionEntryStudentAnnotation, submissionTypes.contains(
                Assignment.SubmissionType.STUDENT_ANNOTATION)) {
                assignment.submission?.id?.let{
                    navigateToAnnotationSubmissionScreen(
                        activity,
                        course,
                        assignment.annotatableAttachmentId,
                        it,
                        assignment.id,
                        assignment.name.orEmpty())
                }
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

    private fun navigateToStudioScreen(activity: FragmentActivity, canvasContext: CanvasContext, assignment: Assignment, studioLTITool: LTITool?) {
        Analytics.logEvent(AnalyticsEventConstants.SUBMIT_STUDIO_SELECTED)
        RouteMatcher.route(
            activity,
            StudioWebViewFragment.makeRoute(
                canvasContext,
                studioLTITool?.getResourceSelectorUrl(canvasContext, assignment).orEmpty(),
                studioLTITool?.name.orEmpty(),
                true,
                assignment
            )
        )
    }

    override fun showCustomReminderDialog(activity: FragmentActivity) {
        CustomReminderDialog.newInstance().show(activity.supportFragmentManager, null)
    }

    override fun showDeleteReminderConfirmationDialog(context: Context, onConfirmed: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle(R.string.deleteReminderTitle)
            .setMessage(R.string.deleteReminderMessage)
            .setNegativeButton(R.string.no, null)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                onConfirmed()
                dialog.dismiss()
            }
            .showThemed()
    }

    override fun showCreateReminderDialog(context: Context, onReminderSelected: (ReminderChoice) -> Unit) {
        val choices = listOf(
            ReminderChoice.Minute(5),
            ReminderChoice.Minute(15),
            ReminderChoice.Minute(30),
            ReminderChoice.Hour(1),
            ReminderChoice.Day(1),
            ReminderChoice.Week(1),
            ReminderChoice.Custom,
        )

        AlertDialog.Builder(context)
            .setTitle(R.string.reminderTitle)
            .setNegativeButton(R.string.cancel, null)
            .setSingleChoiceItems(
                choices.map {
                    if (it is ReminderChoice.Custom) {
                        it.getText(context.resources)
                    } else {
                        context.getString(R.string.reminderBefore, it.getText(context.resources))
                    }
                }.toTypedArray(), -1
            ) { dialog, which ->
                onReminderSelected(choices[which])
                dialog.dismiss()
            }
            .showThemed()
    }

    override fun canRouteInternally(
        activity: FragmentActivity?,
        url: String,
        domain: String,
        routeIfPossible: Boolean
    ): Boolean {
        return RouteMatcher.canRouteInternally(activity, url, domain, routeIfPossible)
    }

    override fun applyTheme(activity: FragmentActivity, binding: FragmentAssignmentDetailsBinding?, course: Course?) {
        binding?.toolbar?.apply {
            setupAsBackButton {
                activity.onBackPressed()
            }

            title = activity.getString(R.string.assignmentDetails)
            subtitle = course?.name

            // TODO: Bookmark is missing

            ViewStyler.themeToolbarColored(activity, this, course)
        }
    }

}