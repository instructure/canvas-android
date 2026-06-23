/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.AnalyticsParamConstants
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsBehaviour
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsRouter
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.student.databinding.DialogSubmissionPickerBinding
import com.instructure.student.databinding.DialogSubmissionPickerMediaBinding
import com.instructure.student.fragment.StudioWebViewFragment
import com.instructure.student.mobius.assignmentDetails.launchAudio
import com.instructure.student.router.RouteMatcher
import com.instructure.student.util.getResourceSelectorUrl

class StudentAssignmentDetailsBehaviour(
    private val router: AssignmentDetailsRouter,
) : AssignmentDetailsBehaviour() {
    override val dialogColor: Int = ThemePrefs.textButtonColor

    override fun applyStatusBarTheme(activity: FragmentActivity, course: Course?) {
        course?.let { ViewStyler.setStatusBarDark(activity, it.color) }
    }

    override fun showMediaDialog(
        activity: FragmentActivity,
        onLaunchAudioRecording: () -> Unit,
        startVideoCapture: () -> Unit,
        onLaunchMediaPicker: () -> Unit
    ) {
        val builder = AlertDialog.Builder(activity)
        val dialogBinding = DialogSubmissionPickerMediaBinding.inflate(LayoutInflater.from(activity))
        val dialog = builder.setView(dialogBinding.root).create()

        dialog.setOnShowListener {
            setupDialogRow(dialog, dialogBinding.submissionEntryAudio, true) {
                activity.launchAudio({ Toast.makeText(activity, R.string.permissionDenied, Toast.LENGTH_SHORT).show() }) {
                    onLaunchAudioRecording()
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

    override fun showSubmitDialog(
        activity: FragmentActivity,
        onLaunchAudioRecording: () -> Unit,
        startVideoCapture: () -> Unit,
        onLaunchMediaPicker: () -> Unit,
        assignment: Assignment,
        course: Course,
        isStudioEnabled: Boolean,
        studioLTITool: LTITool?
    ) {
        Analytics.logEvent(AnalyticsEventConstants.ASSIGNMENT_SUBMIT_SELECTED)

        val builder = AlertDialog.Builder(activity)
        val dialogBinding = DialogSubmissionPickerBinding.inflate(LayoutInflater.from(activity))
        val dialog = builder.setView(dialogBinding.root).create()
        val submissionTypes = assignment.getSubmissionTypes()

        dialog.setOnShowListener {
            val nextAttempt = (assignment.submission?.attempt ?: 0) + 1
            setupDialogRow(dialog, dialogBinding.submissionEntryText, submissionTypes.contains(
                Assignment.SubmissionType.ONLINE_TEXT_ENTRY)) {
                Analytics.logEvent(AnalyticsEventConstants.SUBMIT_TEXTENTRY_SELECTED, Bundle().apply {
                    putString(AnalyticsParamConstants.ATTEMPT, nextAttempt.toString())
                })
                router.navigateToTextEntryScreen(
                    activity,
                    course,
                    assignment.id,
                    assignment.name.orEmpty(),
                    attempt = nextAttempt.toLong()
                )
            }
            setupDialogRow(dialog, dialogBinding.submissionEntryWebsite, submissionTypes.contains(
                Assignment.SubmissionType.ONLINE_URL)) {
                Analytics.logEvent(AnalyticsEventConstants.SUBMIT_URL_SELECTED, Bundle().apply {
                    putString(AnalyticsParamConstants.ATTEMPT, nextAttempt.toString())
                })
                router.navigateToUrlSubmissionScreen(
                    activity,
                    course,
                    assignment.id,
                    assignment.name.orEmpty(),
                    null,
                    false,
                    nextAttempt.toLong()
                )
            }
            setupDialogRow(dialog, dialogBinding.submissionEntryFile, submissionTypes.contains(
                Assignment.SubmissionType.ONLINE_UPLOAD)) {
                Analytics.logEvent(AnalyticsEventConstants.SUBMIT_FILEUPLOAD_SELECTED, Bundle().apply {
                    putString(AnalyticsParamConstants.ATTEMPT, nextAttempt.toString())
                })
                router.navigateToUploadScreen(activity, course, assignment, attempt = nextAttempt.toLong())
            }
            setupDialogRow(dialog, dialogBinding.submissionEntryMedia, submissionTypes.contains(
                Assignment.SubmissionType.MEDIA_RECORDING)) {
                Analytics.logEvent(AnalyticsEventConstants.SUBMIT_MEDIARECORDING_SELECTED, Bundle().apply {
                    putString(AnalyticsParamConstants.ATTEMPT, nextAttempt.toString())
                })
                showMediaDialog(activity, onLaunchAudioRecording, startVideoCapture, onLaunchMediaPicker)
            }
            setupDialogRow(
                dialog,
                dialogBinding.submissionEntryStudio,
                isStudioEnabled
            ) {
                Analytics.logEvent(AnalyticsEventConstants.SUBMIT_STUDIO_SELECTED, Bundle().apply {
                    putString(AnalyticsParamConstants.ATTEMPT, nextAttempt.toString())
                })
                navigateToStudioScreen(activity, course, assignment, studioLTITool)
            }
            setupDialogRow(dialog, dialogBinding.submissionEntryStudentAnnotation, submissionTypes.contains(
                Assignment.SubmissionType.STUDENT_ANNOTATION)) {
                Analytics.logEvent(AnalyticsEventConstants.SUBMIT_ANNOTATION_SELECTED, Bundle().apply {
                    putString(AnalyticsParamConstants.ATTEMPT, nextAttempt.toString())
                })
                assignment.submission?.id?.let {
                    router.navigateToAnnotationSubmissionScreen(
                        activity,
                        course,
                        assignment.annotatableAttachmentId,
                        it,
                        assignment.id,
                        assignment.name.orEmpty(),
                        nextAttempt.toLong())
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
        val nextAttempt = (assignment.submission?.attempt ?: 0) + 1
        RouteMatcher.route(
            activity,
            StudioWebViewFragment.makeRoute(
                canvasContext,
                studioLTITool?.getResourceSelectorUrl(canvasContext, assignment).orEmpty(),
                studioLTITool?.name.orEmpty(),
                true,
                assignment,
                nextAttempt.toLong()
            )
        )
    }

    override fun getThemeColor(course: Course): Int {
        return course.color
    }
}
