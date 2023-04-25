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
package com.instructure.student.mobius.assignmentDetails.submission.picker.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_SUBMISSION_UPLOAD_PICKER
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.instructure.student.databinding.FragmentPickerSubmissionUploadBinding
import com.instructure.student.mobius.assignmentDetails.submission.picker.*
import com.instructure.student.mobius.common.ui.MobiusFragment

@ScreenView(SCREEN_VIEW_SUBMISSION_UPLOAD_PICKER)
class PickerSubmissionUploadFragment :
    MobiusFragment<PickerSubmissionUploadModel, PickerSubmissionUploadEvent, PickerSubmissionUploadEffect, PickerSubmissionUploadView, PickerSubmissionUploadViewState, FragmentPickerSubmissionUploadBinding>() {

    private val assignment by ParcelableArg<Assignment>(key = Const.ASSIGNMENT)
    private val canvasContext by ParcelableArg<Course>(key = Const.CANVAS_CONTEXT)
    private val mode by SerializableArg(key = PICKER_MODE, default = PickerSubmissionMode.FileSubmission)
    private val mediaUri by NullableParcelableArg<Uri>(key = Const.PASSED_URI, default = null)
    private var attemptId by LongArg(key = Const.SUBMISSION_ATTEMPT)

    override fun makeEffectHandler() = PickerSubmissionUploadEffectHandler(requireContext())

    override fun makeUpdate() = PickerSubmissionUploadUpdate()

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) =
        PickerSubmissionUploadView(inflater, parent, mode)

    override fun makePresenter() = PickerSubmissionUploadPresenter

    override fun makeInitModel() = PickerSubmissionUploadModel(
        canvasContext,
        assignment.id,
        assignment.name ?: "",
        assignment.groupCategoryId,
        if (mode.isForComment || mode.isMediaSubmission) emptyList() else assignment.allowedExtensions,
        mode,
        mediaUri,
        attemptId = attemptId.takeIf { it != INVALID_ATTEMPT }
    )

    companion object {

        private const val PICKER_MODE = "pickerMode"
        private const val INVALID_ATTEMPT = -1L

        private fun validRoute(route: Route) = route.canvasContext?.isCourseOrGroup == true
            && route.arguments.containsKey(Const.ASSIGNMENT)
            && route.arguments.containsKey(PICKER_MODE)

        fun makeRoute(
            canvasContext: CanvasContext,
            assignment: Assignment,
            mode: PickerSubmissionMode,
            attemptId: Long? = null
        ): Route {
            val bundle = canvasContext.makeBundle {
                putParcelable(Const.ASSIGNMENT, assignment)
                putSerializable(PICKER_MODE, mode)
                putLong(Const.SUBMISSION_ATTEMPT, attemptId ?: INVALID_ATTEMPT)
            }

            return Route(PickerSubmissionUploadFragment::class.java, canvasContext, bundle)
        }

        // For use with file/gallery picker of media recording submissions to allow confirmation before hitting submit
        fun makeRoute(
            canvasContext: CanvasContext,
            assignment: Assignment,
            mediaUri: Uri
        ): Route {
            val bundle = canvasContext.makeBundle {
                putParcelable(Const.ASSIGNMENT, assignment)
                putParcelable(Const.PASSED_URI, mediaUri)
                putSerializable(PICKER_MODE, PickerSubmissionMode.MediaSubmission)
            }

            return Route(PickerSubmissionUploadFragment::class.java, canvasContext, bundle)
        }

        fun newInstance(route: Route): PickerSubmissionUploadFragment? {
            if (!validRoute(route)) return null
            return PickerSubmissionUploadFragment().withArgs(route.arguments)
        }
    }
}
