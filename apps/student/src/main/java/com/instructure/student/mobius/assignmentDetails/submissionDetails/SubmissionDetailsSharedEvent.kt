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
package com.instructure.student.mobius.assignmentDetails.submissionDetails

import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionComment

sealed class SubmissionDetailsSharedEvent {
    data class FileSelected(val file: Attachment) : SubmissionDetailsSharedEvent()
    data class SubmissionClicked(val submission: Submission) : SubmissionDetailsSharedEvent()
    data class SubmissionAttachmentClicked(
        val submission: Submission,
        val attachment: Attachment
    ) : SubmissionDetailsSharedEvent()
    object AudioRecordingViewLaunched : SubmissionDetailsSharedEvent()
    object VideoRecordingViewLaunched : SubmissionDetailsSharedEvent()
    data class SubmissionCommentsUpdated(val submissionComments: List<SubmissionComment>) : SubmissionDetailsSharedEvent()
}
