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
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.analytics.SCREEN_VIEW_SUBMISSION_UPLOAD_PICKER
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.SerializableArg
import com.instructure.student.databinding.FragmentPickerSubmissionUploadBinding
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionMode
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadEffect
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadEvent
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadModel
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadPresenter
import com.instructure.student.mobius.assignmentDetails.submission.picker.PickerSubmissionUploadUpdate
import com.instructure.pandautils.utils.NullableStringArg
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerSubmissionUploadFragment.Companion.INVALID_ATTEMPT
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerSubmissionUploadFragment.Companion.MEDIA_SOURCE
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerSubmissionUploadFragment.Companion.PICKER_MODE
import com.instructure.student.mobius.common.ui.MobiusFragment

@ScreenView(SCREEN_VIEW_SUBMISSION_UPLOAD_PICKER)
abstract class BasePickerSubmissionUploadFragment :
    MobiusFragment<PickerSubmissionUploadModel, PickerSubmissionUploadEvent, PickerSubmissionUploadEffect, PickerSubmissionUploadView, PickerSubmissionUploadViewState, FragmentPickerSubmissionUploadBinding>() {

    private val assignment by ParcelableArg<Assignment>(key = Const.ASSIGNMENT)
    private val canvasContext by ParcelableArg<Course>(key = Const.CANVAS_CONTEXT)
    private val mode by SerializableArg(key = PICKER_MODE, default = PickerSubmissionMode.FileSubmission)
    private val mediaUri by NullableParcelableArg<Uri>(key = Const.PASSED_URI, default = null)
    private var attemptId by LongArg(key = Const.SUBMISSION_ATTEMPT)
    private val initialMediaSource by NullableStringArg(key = MEDIA_SOURCE)

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
        attemptId = attemptId.takeIf { it != INVALID_ATTEMPT },
        mediaSource = initialMediaSource
    )
}
