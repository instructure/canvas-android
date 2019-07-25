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
package com.instructure.student.mobius.assignmentDetails.submission.file

import com.instructure.canvasapi2.models.postmodels.FileSubmitObject

sealed class UploadStatusSubmissionEvent {
    data class OnFilesRefreshed(
        val failed: Boolean,
        val submissionId: Long,
        val files: List<FileSubmitObject>
    ) : UploadStatusSubmissionEvent()

    data class OnPersistedSubmissionLoaded(val failed: Boolean, val files: List<FileSubmitObject>) :
        UploadStatusSubmissionEvent()

    data class OnUploadProgressChanged(
        val fileIndex: Int,
        val submissionId: Long,
        val uploaded: Long
    ) : UploadStatusSubmissionEvent()
}

sealed class UploadStatusSubmissionEffect {
    data class LoadPersistedFiles(val submissionId: Long) : UploadStatusSubmissionEffect()
}

data class UploadStatusSubmissionModel(
    val submissionId: Long,
    val assignmentName: String? = null,
    val isLoading: Boolean = false,
    val isFailed: Boolean = false,
    val uploadedBytes: Int? = null,
    val files: List<FileSubmitObject> = emptyList()
)
