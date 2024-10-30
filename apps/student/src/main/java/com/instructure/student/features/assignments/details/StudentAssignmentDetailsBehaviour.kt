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
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.interactions.Navigation
import com.instructure.interactions.bookmarks.Bookmarker
import com.instructure.pandautils.databinding.FragmentAssignmentDetailsBinding
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsBehaviour
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsRouter
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.views.RecordingMediaType
import com.instructure.student.R
import com.instructure.student.databinding.DialogSubmissionPickerBinding
import com.instructure.student.databinding.DialogSubmissionPickerMediaBinding
import com.instructure.student.fragment.StudioWebViewFragment
import com.instructure.student.mobius.assignmentDetails.launchAudio
import com.instructure.student.router.RouteMatcher
import com.instructure.student.util.getResourceSelectorUrl
import java.io.File

class StudentAssignmentDetailsBehaviour (
    private val router: AssignmentDetailsRouter,
): AssignmentDetailsBehaviour() {
    override val dialogColor: Int = ThemePrefs.textButtonColor

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
            setupDialogRow(dialog, dialogBinding.submissionEntryText, submissionTypes.contains(
                Assignment.SubmissionType.ONLINE_TEXT_ENTRY)) {
                router.navigateToTextEntryScreen(
                    activity,
                    course,
                    assignment.id,
                    assignment.name.orEmpty(),
                )
            }
            setupDialogRow(dialog, dialogBinding.submissionEntryWebsite, submissionTypes.contains(
                Assignment.SubmissionType.ONLINE_URL)) {
                router.navigateToUrlSubmissionScreen(
                    activity,
                    course,
                    assignment.id,
                    assignment.name.orEmpty(),
                    null,
                    false
                )
            }
            setupDialogRow(dialog, dialogBinding.submissionEntryFile, submissionTypes.contains(
                Assignment.SubmissionType.ONLINE_UPLOAD)) {
                router.navigateToUploadScreen(activity, course, assignment)
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
                    router.navigateToAnnotationSubmissionScreen(
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

    override fun applyTheme(
        activity: FragmentActivity,
        binding: FragmentAssignmentDetailsBinding?,
        bookmark: Bookmarker,
        toolbar: Toolbar,
        course: LiveData<Course>,
        assignment: Assignment?,
        routeToCompose: ((InboxComposeOptions) -> Unit)?
    ) {
        binding?.toolbar?.apply {
            setupAsBackButton {
                activity.onBackPressed()
            }

            title = activity.getString(R.string.assignmentDetails)
            subtitle = course.value?.name

            setupToolbarMenu(activity, bookmark, toolbar)

            ViewStyler.themeToolbarColored(activity, this, course.value)
        }
    }

    private fun setupToolbarMenu(activity: FragmentActivity, bookmark: Bookmarker, toolbar: Toolbar) {
        addBookmarkMenuIfAllowed(activity, bookmark, toolbar)
        addOnMenuItemClickListener(activity, toolbar)
    }

    private fun addBookmarkMenuIfAllowed(activity: FragmentActivity, bookmark: Bookmarker, toolbar: Toolbar) {
        val navigation = activity as? Navigation
        val bookmarkFeatureAllowed = navigation?.canBookmark() ?: false
        if (bookmarkFeatureAllowed && bookmark.canBookmark && toolbar.menu.findItem(
                R.id.bookmark) == null) {
            toolbar.inflateMenu(R.menu.bookmark_menu)
        }
    }

    private fun addOnMenuItemClickListener(activity: FragmentActivity, toolbar: Toolbar) {
        toolbar.setOnMenuItemClickListener { item -> onOptionsItemSelected(activity, item) }
    }

    override fun onOptionsItemSelected(activity: FragmentActivity, item: MenuItem): Boolean {
        if (item.itemId == R.id.bookmark) {
            if (APIHelper.hasNetworkConnection()) {
                (activity as? Navigation)?.addBookmark()
            } else {
                Toast.makeText(activity, activity.getString(com.instructure.pandautils.R.string.notAvailableOffline), Toast.LENGTH_SHORT).show()
            }
            return true
        }
        return false
    }
}