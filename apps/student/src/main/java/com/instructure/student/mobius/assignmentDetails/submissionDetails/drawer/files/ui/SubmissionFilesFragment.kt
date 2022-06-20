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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.files.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.analytics.SCREEN_VIEW_SUBMISSION_FILES
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.ParcelableListArg
import com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.files.*
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionDetailsTabData
import com.instructure.student.mobius.common.ui.MobiusFragment

@ScreenView(SCREEN_VIEW_SUBMISSION_FILES)
class SubmissionFilesFragment :
    MobiusFragment<SubmissionFilesModel, SubmissionFilesEvent, SubmissionFilesEffect, SubmissionFilesView, SubmissionFilesViewState>() {

    private var files: List<Attachment> by ParcelableListArg()
    private var selectedFileId: Long by LongArg()
    private var canvasContext: CanvasContext by ParcelableArg()

    override fun makeEffectHandler() = SubmissionFilesEffectHandler()

    override fun makeUpdate() = SubmissionFilesUpdate()

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) = SubmissionFilesView(inflater, parent)

    override fun makePresenter() = SubmissionFilesPresenter

    override fun makeInitModel() = SubmissionFilesModel(canvasContext, files, selectedFileId)

    companion object {
        fun newInstance(data: SubmissionDetailsTabData.FileData) = SubmissionFilesFragment().apply {
            files = data.files
            selectedFileId = data.selectedFileId
            canvasContext = data.canvasContext
        }
    }
}
