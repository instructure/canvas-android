/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.features.speedgrader.grade.comments

import com.instructure.canvasapi2.CreateSubmissionCommentMutation
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.graphql.SubmissionCommentsManager
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.SubmissionCommentsResponseWrapper

class SpeedGraderCommentsRepository(
    private val submissionCommentsManager: SubmissionCommentsManager,
    private val submissionApi: SubmissionAPI.SubmissionInterface
) {
    suspend fun getSubmissionComments(userId: Long, assignmentId: Long): SubmissionCommentsResponseWrapper {
        return submissionCommentsManager.getSubmissionComments(userId, assignmentId)
    }

    suspend fun createSubmissionComment(
        submissionId: Long,
        comment: String,
        attempt: Int? = null,
        isGroupComment: Boolean = false
    ): CreateSubmissionCommentMutation.Data {
        return submissionCommentsManager.createSubmissionComment(submissionId, comment, attempt, isGroupComment)
    }

    suspend fun getSingleSubmission(courseId: Long, assignmentId: Long, studentId: Long): Submission? {
        return submissionApi.getSingleSubmission(courseId, assignmentId, studentId, RestParams()).dataOrNull
    }
}
