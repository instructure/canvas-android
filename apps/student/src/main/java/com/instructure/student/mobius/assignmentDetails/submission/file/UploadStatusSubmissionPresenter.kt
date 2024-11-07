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

import android.content.Context
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.student.R
import com.instructure.student.mobius.assignmentDetails.submission.file.ui.UploadListItemViewState
import com.instructure.student.mobius.assignmentDetails.submission.file.ui.UploadStatusSubmissionViewState
import com.instructure.student.mobius.common.ui.Presenter
import com.instructure.student.util.FileUtils

object UploadStatusSubmissionPresenter :
    Presenter<UploadStatusSubmissionModel, UploadStatusSubmissionViewState> {

    override fun present(
        model: UploadStatusSubmissionModel,
        context: Context
    ): UploadStatusSubmissionViewState {
        return when {
            model.isFailed -> presentFailed(
                model,
                context
            )
            model.isLoading -> UploadStatusSubmissionViewState.Loading
            model.files.isEmpty() -> presentSuccess(
                context
            )
            else -> presentInProgress(
                model,
                context
            )
        }
    }

    private fun presentSuccess(
        context: Context
    ): UploadStatusSubmissionViewState {
        return UploadStatusSubmissionViewState.Succeeded(
            context.getString(R.string.submissionSuccessTitle),
            context.getString(R.string.submissionSuccessMessage)
        )
    }

    private fun presentFailed(
        model: UploadStatusSubmissionModel,
        context: Context
    ): UploadStatusSubmissionViewState {
        return UploadStatusSubmissionViewState.Failed(
            context.getString(R.string.submissionStatusFailedTitle),
            context.getString(R.string.submissionUploadFailedMessage),
            presentListItems(
                model,
                context,
                R.drawable.ic_warning,
                true
            )
        )
    }

    private fun presentInProgress(
        model: UploadStatusSubmissionModel,
        context: Context
    ): UploadStatusSubmissionViewState {

        val bytes = model.uploadedBytes ?: 0
        val totalBytes: Long = model.files.fold(0L) { size, file -> size + (file.size ?: 0) }
        val percent = (bytes / totalBytes.toDouble()) * 100

        val bytesString = NumberHelper.readableFileSize(context, bytes)
        val totalBytesString = NumberHelper.readableFileSize(context, totalBytes)

        val title = context.getString(R.string.assignmentSubmissionUpload, model.assignmentName)
        val size = context.getString(R.string.submissionUploadByteProgress, bytesString, totalBytesString)

        return UploadStatusSubmissionViewState.InProgress(
            title,
            size,
            NumberHelper.doubleToPercentage(percent),
            percent,
            presentListItems(
                model,
                context,
                null,
                false
            )
        )
    }

    private fun presentListItems(
        model: UploadStatusSubmissionModel,
        context: Context,
        failedIcon: Int?,
        deletableIfError: Boolean
    ): List<UploadListItemViewState> {
        var icon: Int
        var color: Int = R.color.textInfo
        var canDelete = false

        return model.files.mapIndexed { index, file ->
            if (file.errorFlag && failedIcon != null) {
                icon = failedIcon
                color = R.color.textDanger
                canDelete = deletableIfError
            } else {
                icon = FileUtils.getFileIcon(file.name ?: "", file.contentType ?: "")
            }

             UploadListItemViewState(
                index,
                icon,
                ContextCompat.getColor(context, color),
                file.name ?: "",
                NumberHelper.readableFileSize(context, file.size ?: 0),
                canDelete,
                file.error
             )
        }
    }
}
