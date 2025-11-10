/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.file.upload

import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.pandautils.features.file.upload.itemviewmodels.FileItemViewModel
import java.util.*

data class FileUploadDialogViewData(
        val allowedExtensions: String?,
        val files: List<FileItemViewModel>
)

data class FileItemViewData(
        val fileName: String,
        val fileSize: String,
        val fullPath: String
)

sealed class FileUploadAction {
    object TakePhoto : FileUploadAction()
    object PickImage : FileUploadAction()
    object PickFile : FileUploadAction()
    object PickMultipleImage : FileUploadAction()
    object PickMultipleFile : FileUploadAction()
    object UploadStarted : FileUploadAction()
    data class ShowToast(val toast: String) : FileUploadAction()
    data class AttachmentSelectedAction(val event: Int, val attachment: FileSubmitObject?) : FileUploadAction()
    data class UploadStartedAction(val id: UUID, val liveData: LiveData<WorkInfo?>, val selectedUris: List<String>) : FileUploadAction()
}

