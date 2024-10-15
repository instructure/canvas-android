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
            Next.next(
                model.copy(
                    isLoading = false,
                    assignmentName = event.assignmentName,
                    files = event.files,
                    isFailed = event.failed
                )
            )
        }
        is UploadStatusSubmissionEvent.OnFilesRefreshed -> {
            Next.next(
                model.copy(files = event.files, isFailed = event.failed, uploadedBytes = null)
            )
        }
        is UploadStatusSubmissionEvent.OnUploadProgressChanged -> {
            val uploadedFileSize = model.files.take(event.fileIndex).fold(0) { sum, file ->
                sum + (file.size ?: 0).toInt()
            }
            var currentFileProgress = 0.0
            if (model.files.isNotEmpty())
                currentFileProgress = model.files[event.fileIndex].size?.times(event.uploaded) ?: 0.0
            Next.next(model.copy(uploadedBytes = uploadedFileSize + currentFileProgress.toLong()))
        }
        UploadStatusSubmissionEvent.OnRequestCancelClicked -> {
            Next.dispatch(setOf(UploadStatusSubmissionEffect.ShowCancelDialog))
        }
        UploadStatusSubmissionEvent.OnCancelClicked -> {
            Next.dispatch(setOf(UploadStatusSubmissionEffect.OnDeleteSubmission(model.submissionId)))
        }
        UploadStatusSubmissionEvent.OnRetryClicked -> {
            Next.dispatch(setOf(UploadStatusSubmissionEffect.RetrySubmission(model.submissionId)))
        }
        is UploadStatusSubmissionEvent.OnDeleteFile -> {
            // If we're deleting the last file in the list, just delete the whole submission
            if (model.files.size == 1) {
                Next.dispatch<UploadStatusSubmissionModel, UploadStatusSubmissionEffect>(setOf(UploadStatusSubmissionEffect.OnDeleteSubmission(model.submissionId)))
            } else {
                val deletedFile = model.files[event.position]
                Next.next<UploadStatusSubmissionModel, UploadStatusSubmissionEffect>(
                    model.copy(files = model.files.minus(deletedFile)),
                    setOf(UploadStatusSubmissionEffect.OnDeleteFileFromSubmission(deletedFile.id))
                )
            }
        }
        UploadStatusSubmissionEvent.RequestLoad -> {
            Next.dispatch(setOf(UploadStatusSubmissionEffect.LoadPersistedFiles(model.submissionId)))
        }
    }
}
