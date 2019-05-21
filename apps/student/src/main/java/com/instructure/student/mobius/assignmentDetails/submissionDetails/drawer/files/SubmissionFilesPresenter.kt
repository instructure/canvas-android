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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.files

import android.content.Context
import com.instructure.canvasapi2.models.Attachment
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.color
import com.instructure.student.R
import com.instructure.student.mobius.common.ui.Presenter

object SubmissionFilesPresenter : Presenter<SubmissionFilesModel, SubmissionFilesViewState> {
    override fun present(model: SubmissionFilesModel, context: Context): SubmissionFilesViewState {
        return if (model.files.isEmpty()) {
            SubmissionFilesViewState.Empty
        } else {
            val iconColor = model.canvasContext.color
            val selectionColor = ThemePrefs.brandColor
            SubmissionFilesViewState.FileList(model.files.map { mapData(it, model, iconColor, selectionColor) })
        }
    }

    private fun mapData(
        attachment: Attachment,
        model: SubmissionFilesModel,
        iconColor: Int,
        selectionColor: Int
    ): SubmissionFileData {
        val contentType = attachment.contentType.orEmpty()
        val icon = when {
            contentType.contains("pdf") -> R.drawable.vd_pdf
            contentType.contains("presentation") -> R.drawable.vd_ppt
            contentType.contains("spreadsheet") -> R.drawable.vd_spreadsheet
            contentType.contains("wordprocessing") -> R.drawable.vd_word_doc
            contentType.contains("zip") -> R.drawable.vd_zip
            contentType.contains("image") -> R.drawable.vd_image
            else -> R.drawable.vd_document
        }
        return SubmissionFileData(
            id = attachment.id,
            name = attachment.displayName ?: attachment.filename.orEmpty(),
            icon = icon,
            thumbnailUrl = attachment.thumbnailUrl,
            isSelected = attachment.id == model.selectedFileId,
            iconColor = iconColor,
            selectionColor = selectionColor
        )
    }
}
