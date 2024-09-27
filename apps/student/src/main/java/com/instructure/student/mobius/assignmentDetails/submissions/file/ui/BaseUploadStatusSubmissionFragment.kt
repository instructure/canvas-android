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
package com.instructure.student.mobius.assignmentDetails.submissions.file.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.pandautils.analytics.SCREEN_VIEW_UPLOAD_STATUS_SUBMISSION
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LongArg
import com.instructure.student.databinding.FragmentUploadStatusSubmissionBinding
import com.instructure.student.mobius.assignmentDetails.submissions.file.UploadStatusSubmissionEffect
import com.instructure.student.mobius.assignmentDetails.submissions.file.UploadStatusSubmissionEvent
import com.instructure.student.mobius.assignmentDetails.submissions.file.UploadStatusSubmissionModel
import com.instructure.student.mobius.assignmentDetails.submissions.file.UploadStatusSubmissionPresenter
import com.instructure.student.mobius.assignmentDetails.submissions.file.UploadStatusSubmissionUpdate
import com.instructure.student.mobius.common.ui.MobiusFragment

@ScreenView(SCREEN_VIEW_UPLOAD_STATUS_SUBMISSION)
abstract class BaseUploadStatusSubmissionFragment :
    MobiusFragment<UploadStatusSubmissionModel, UploadStatusSubmissionEvent, UploadStatusSubmissionEffect, UploadStatusSubmissionView, UploadStatusSubmissionViewState, FragmentUploadStatusSubmissionBinding>() {

    protected val submissionId by LongArg(key = Const.SUBMISSION_ID)

    override fun makeUpdate() = UploadStatusSubmissionUpdate()

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) =
        UploadStatusSubmissionView(inflater, parent)

    override fun makePresenter() = UploadStatusSubmissionPresenter

    override fun makeInitModel() = UploadStatusSubmissionModel(submissionId)
}