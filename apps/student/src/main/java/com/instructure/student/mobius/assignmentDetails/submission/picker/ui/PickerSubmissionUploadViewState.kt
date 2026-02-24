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
package com.instructure.student.mobius.assignmentDetails.submission.picker.ui

sealed class PickerSubmissionUploadViewState(open val visibilities: PickerVisibilities) {
    data class Empty(override val visibilities: PickerVisibilities) : PickerSubmissionUploadViewState(visibilities)
    data class FileList(override val visibilities: PickerVisibilities, val list: List<PickerListItemViewState>) : PickerSubmissionUploadViewState(visibilities)
}

data class PickerVisibilities(
    val loading: Boolean = false,
    val sources: Boolean = false,
    val sourceCamera: Boolean = false,
    val sourceGallery: Boolean = false,
    val sourceFile: Boolean = false,
    val sourceScanner: Boolean = false,
    val submit: Boolean = false
)

data class PickerListItemViewState(
    val position: Int,
    val iconRes: Int,
    val title: String,
    val size: String,
    val canDelete: Boolean = true
)
