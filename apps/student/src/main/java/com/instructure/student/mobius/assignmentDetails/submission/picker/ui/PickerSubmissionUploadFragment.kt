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
package com.instructure.student.mobius.assignmentDetails.submission.picker.ui

import android.net.Uri
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.isCourseOrGroup
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.withArgs
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionMode
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadEffectHandler
import com.instructure.student.mobius.common.ui.SubmissionHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PickerSubmissionUploadFragment : BasePickerSubmissionUploadFragment() {

    @Inject
    lateinit var submissionHelper: SubmissionHelper

    override fun makeEffectHandler() = PickerSubmissionUploadEffectHandler(requireContext(), submissionHelper)

    companion object {

        const val PICKER_MODE = "pickerMode"
        const val INVALID_ATTEMPT = -1L
        const val MEDIA_SOURCE = "mediaSource"

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
            mediaUri: Uri,
            attemptId: Long = 1L,
            mediaSource: String? = null
        ): Route {
            val bundle = canvasContext.makeBundle {
                putParcelable(Const.ASSIGNMENT, assignment)
                putParcelable(Const.PASSED_URI, mediaUri)
                putSerializable(PICKER_MODE, PickerSubmissionMode.MediaSubmission)
                putLong(Const.SUBMISSION_ATTEMPT, attemptId)
                mediaSource?.let { putString(MEDIA_SOURCE, it) }
            }

            return Route(PickerSubmissionUploadFragment::class.java, canvasContext, bundle)
        }

        fun newInstance(route: Route): PickerSubmissionUploadFragment? {
            if (!validRoute(route)) return null
            return PickerSubmissionUploadFragment().withArgs(route.arguments)
        }
    }
}