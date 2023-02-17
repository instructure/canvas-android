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
package com.emeritus.student.mobius.assignmentDetails.submissionDetails.drawer.files

import android.content.Context
import com.instructure.canvasapi2.models.Attachment
import com.instructure.pandautils.utils.textAndIconColor
import com.emeritus.student.R
import com.emeritus.student.mobius.common.ui.Presenter

object SubmissionFilesPresenter : Presenter<SubmissionFilesModel, SubmissionFilesViewState> {
    override fun present(model: SubmissionFilesModel, context: Context): SubmissionFilesViewState {
        return if (model.files.isEmpty()) {
            SubmissionFilesViewState.Empty
        } else {
            val tintColor = model.canvasContext.textAndIconColor
            SubmissionFilesViewState.FileList(model.files.map { mapData(it, model, tintColor) })
        }
    }

    private fun mapData(
        attachment: Attachment,
        model: SubmissionFilesModel,
        tintColor: Int
    ): SubmissionFileData {
        val contentType = attachment.contentType.orEmpty()
        val icon = when {
            contentType.contains("pdf") -> R.drawable.ic_pdf
            contentType.contains("presentation") -> R.drawable.ic_ppt
            contentType.contains("spreadsheet") -> R.drawable.ic_spreadsheet
            contentType.contains("wordprocessing") -> R.drawable.ic_word_doc
            contentType.contains("zip") -> R.drawable.ic_zip
            contentType.contains("image") -> R.drawable.ic_image
            else -> R.drawable.ic_document
        }
        return SubmissionFileData(
            id = attachment.id,
            name = attachment.displayName ?: attachment.filename.orEmpty(),
            icon = icon,
            thumbnailUrl = attachment.thumbnailUrl,
            isSelected = attachment.id == model.selectedFileId,
            iconColor = tintColor,
            selectionColor = tintColor
        )
    }
}
