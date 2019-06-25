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
import com.instructure.pandautils.utils.*
import com.instructure.student.mobius.assignmentDetails.submission.picker.*
import com.instructure.student.mobius.common.ui.MobiusFragment

class PickerSubmissionUploadFragment :
    MobiusFragment<PickerSubmissionUploadModel, PickerSubmissionUploadEvent, PickerSubmissionUploadEffect, PickerSubmissionUploadView, PickerSubmissionUploadViewState>() {

    private val assignment by ParcelableArg<Assignment>(key = Const.ASSIGNMENT)
    private val canvasContext by ParcelableArg<Course>(key = Const.CANVAS_CONTEXT)
    private val isMediaPicker by BooleanArg(key = Const.IS_MEDIA_TYPE)

    override fun makeEffectHandler() = PickerSubmissionUploadEffectHandler(requireContext())

    override fun makeUpdate() = PickerSubmissionUploadUpdate()

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) =
        PickerSubmissionUploadView(inflater, parent)

    override fun makePresenter() = PickerSubmissionUploadPresenter

    override fun makeInitModel() = PickerSubmissionUploadModel(
        canvasContext,
        assignment.id,
        assignment.name ?: "",
        assignment.groupCategoryId,
        assignment.allowedExtensions,
        isMediaPicker
    )

    companion object {
        private fun validRoute(route: Route) = route.canvasContext?.isCourseOrGroup == true
            && route.arguments.containsKey(Const.ASSIGNMENT)
            && route.arguments.containsKey(Const.IS_MEDIA_TYPE)

        fun makeRoute(
            canvasContext: CanvasContext,
            assignment: Assignment,
            isMediaPicker: Boolean
        ): Route {
            val bundle = canvasContext.makeBundle {
                putParcelable(Const.ASSIGNMENT, assignment)
                putBoolean(Const.IS_MEDIA_TYPE, isMediaPicker)
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
                putBoolean(Const.IS_MEDIA_TYPE, true)
                putParcelable(Const.PASSED_URI, mediaUri)
            }

            return Route(PickerSubmissionUploadFragment::class.java, canvasContext, bundle)
        }

        fun newInstance(route: Route): PickerSubmissionUploadFragment? {
            if (!validRoute(route)) return null
            return PickerSubmissionUploadFragment().withArgs(route.arguments)
        }
    }
}
