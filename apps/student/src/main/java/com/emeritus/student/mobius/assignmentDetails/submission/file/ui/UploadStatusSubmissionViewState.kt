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
package com.emeritus.student.mobius.assignmentDetails.submission.file.ui

sealed class UploadStatusSubmissionViewState(val visibilities: UploadVisibilities) {
    object Loading : UploadStatusSubmissionViewState(UploadVisibilities(loading = true))

    data class Succeeded(val title: String = "", val message: String = "") :
        UploadStatusSubmissionViewState(UploadVisibilities(succeeded = true))

    data class InProgress(
        val title: String = "",
        val sizeMessage: String = "",
        val percentageString: String = "",
        val percentage: Double = 0.0,
        val list: List<UploadListItemViewState>
    ) : UploadStatusSubmissionViewState(UploadVisibilities(inProgress = true, cancelable = true))

    data class Failed(
        val title: String = "",
        val message: String = "",
        val list: List<UploadListItemViewState>
    ) : UploadStatusSubmissionViewState(UploadVisibilities(failed = true, cancelable = true))
}

data class UploadVisibilities(
    val loading: Boolean = false,
    val inProgress: Boolean = false,
    val failed: Boolean = false,
    val succeeded: Boolean = false,
    val cancelable: Boolean = false
)

data class UploadListItemViewState(
    val position: Int,
    val iconRes: Int,
    val iconColor: Int,
    val title: String,
    val size: String,
    val canDelete: Boolean = true,
    val errorMessage: String?
)
