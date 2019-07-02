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

import com.instructure.student.mobius.common.ui.UpdateInit
import com.spotify.mobius.First
import com.spotify.mobius.Next

class UploadStatusSubmissionUpdate :
    UpdateInit<UploadStatusSubmissionModel, UploadStatusSubmissionEvent, UploadStatusSubmissionEffect>() {
    override fun performInit(model: UploadStatusSubmissionModel): First<UploadStatusSubmissionModel, UploadStatusSubmissionEffect> {
        return First.first(
            model.copy(isLoading = true),
            setOf(UploadStatusSubmissionEffect.LoadPersistedFiles(model.submissionId))
        )
    }

    override fun update(
        model: UploadStatusSubmissionModel,
        event: UploadStatusSubmissionEvent
    ): Next<UploadStatusSubmissionModel, UploadStatusSubmissionEffect> = when (event) {
        is UploadStatusSubmissionEvent.OnPersistedSubmissionLoaded -> {
            Next.next(model.copy(isLoading = false, files = event.files, isFailed = event.failed))
        }
        is UploadStatusSubmissionEvent.OnFilesRefreshed -> {
            Next.next(
                model.copy(files = event.files, isFailed = event.failed, uploadedBytes = null)
            )
        }
        is UploadStatusSubmissionEvent.OnUploadProgressChanged -> {
            val uploadedFileSize = model.files.take(event.fileIndex).fold(0) { sum, file ->
                sum + file.size.toInt()
            }
            Next.next(model.copy(uploadedBytes = uploadedFileSize + event.uploaded.toInt()))
        }
    }
}
