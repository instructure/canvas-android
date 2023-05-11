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

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

sealed class SubmissionFilesViewState {
    object Empty : SubmissionFilesViewState()
    data class FileList(
        val files: List<SubmissionFileData> = emptyList()
    ) : SubmissionFilesViewState()
}

data class SubmissionFileData(
    val id: Long,
    val name: String,
    @DrawableRes val icon: Int,
    val thumbnailUrl: String?,
    val isSelected: Boolean,
    @ColorInt val iconColor: Int,
    @ColorInt val selectionColor: Int
)
