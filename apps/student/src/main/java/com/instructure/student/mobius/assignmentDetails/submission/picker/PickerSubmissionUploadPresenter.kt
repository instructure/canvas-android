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
package com.instructure.student.mobius.assignmentDetails.submission.picker

import android.content.Context
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerListItemViewState
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerSubmissionUploadViewState
import com.instructure.student.mobius.assignmentDetails.submission.picker.ui.PickerVisibilities
import com.instructure.student.mobius.common.ui.Presenter
import com.instructure.student.util.FileUtils

object PickerSubmissionUploadPresenter : Presenter<PickerSubmissionUploadModel, PickerSubmissionUploadViewState> {
    override fun present(
        model: PickerSubmissionUploadModel,
        context: Context
    ): PickerSubmissionUploadViewState {
        return if (model.files.isEmpty() && !model.isLoadingFile) {
            presentEmptyState(model)
        } else {
            presentListState(model, context)
        }
    }

    private fun presentEmptyState(model: PickerSubmissionUploadModel) : PickerSubmissionUploadViewState {
        return PickerSubmissionUploadViewState.Empty(getVisibilities(model))
    }

    private fun presentListState(model: PickerSubmissionUploadModel, context: Context) : PickerSubmissionUploadViewState {
        val visibilities = getVisibilities(model)

        val fileStates = model.files.mapIndexed { index, file ->
            PickerListItemViewState(
                position = index,
                iconRes = FileUtils.getFileIcon(file.name, file.contentType),
                title = file.name,
                size = NumberHelper.readableFileSize(context, file.size),
                canDelete = !model.mode.isMediaSubmission
            )
        }

        return PickerSubmissionUploadViewState.FileList(visibilities, fileStates)
    }

    private fun getVisibilities(model: PickerSubmissionUploadModel) = PickerVisibilities(
        loading = model.isLoadingFile,
        sources = !model.mode.isMediaSubmission,
        submit = model.files.isNotEmpty(),
        sourceFile = !model.mode.isMediaSubmission,
        sourceCamera = !model.mode.isMediaSubmission && allowsCameraImages(model.allowedExtensions),
        sourceGallery = !model.mode.isMediaSubmission && allowsGalleryImages(model.allowedExtensions),
        sourceScanner = !model.mode.isMediaSubmission && model.scannerAvailable && allowsScannedDocuments(model.allowedExtensions)
    )

    private fun allowsCameraImages(allowedExtensions: List<String>): Boolean {
        return allowedExtensions.isEmpty() || allowedExtensions.any { it in listOf("jpg") } // We save camera images as jpg
    }

    private fun allowsGalleryImages(allowedExtensions: List<String>): Boolean {
        return allowedExtensions.isEmpty() || allowedExtensions.any { it in listOf("png", "jpg", "jpeg", "mp4", "mov", "gif", "tiff", "bmp") }
    }

    private fun allowsScannedDocuments(allowedExtensions: List<String>): Boolean {
        return allowedExtensions.isEmpty() || allowedExtensions.any { it in listOf("pdf") }
    }
}
