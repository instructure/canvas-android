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

package com.instructure.student.mobius.assignmentDetails.submissionDetails.ui

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Submission

sealed class SubmissionDetailsViewState {
    object Error : SubmissionDetailsViewState()
    object Loading : SubmissionDetailsViewState()
    data class Loaded(
        val showVersionsSpinner: Boolean,
        val selectedVersionSpinnerIndex: Int,
        val submissionVersions: List<Pair<Long, String>>,
        val tabData : List<SubmissionDetailsTabData>
    ): SubmissionDetailsViewState()
}

sealed class SubmissionDetailsTabData(val tabName: String) {
    data class CommentData(
        val name: String,
        val assignment: Assignment,
        val submission: Submission,
        val attemptId: Long?
    ) : SubmissionDetailsTabData(name)
    data class FileData(
        val name: String,
        val files: List<Attachment>,
        val selectedFileId: Long,
        val canvasContext: CanvasContext
    ) : SubmissionDetailsTabData(name)
    data class RubricData(
        val name: String,
        val assignment: Assignment,
        val submission: Submission
    ) : SubmissionDetailsTabData(name)
}

data class SubmissionTypesVisibilities(
    var textEntry: Boolean = false,
    var urlEntry: Boolean = false,
    var fileUpload: Boolean = false,
    var mediaRecording: Boolean = false,
    var studioUpload: Boolean = false,
    var studentAnnotation: Boolean = false
)
