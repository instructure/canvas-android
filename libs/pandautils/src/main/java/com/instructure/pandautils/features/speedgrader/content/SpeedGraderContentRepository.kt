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
package com.instructure.pandautils.features.speedgrader.content

import com.instructure.canvasapi2.SubmissionContentQuery
import com.instructure.canvasapi2.apis.CanvaDocsAPI
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.graphql.SubmissionContentManager
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.canvadocs.CanvaDocSessionRequestBody
import com.instructure.canvasapi2.models.canvadocs.CanvaDocSessionResponseBody

class SpeedGraderContentRepository(
    private val submissionContentManager: SubmissionContentManager,
    private val submissionApi: SubmissionAPI.SubmissionInterface,
    private val canvaDocsApi: CanvaDocsAPI.CanvaDocsInterFace
) {

    suspend fun getSubmission(assignmentId: Long, studentId: Long): SubmissionContentQuery.Data {
        return submissionContentManager.getSubmissionContent(studentId, assignmentId)
    }

    suspend fun getSingleSubmission(courseId: Long,assignmentId: Long, studentId: Long): Submission? {
        return submissionApi.getSingleSubmission(courseId, assignmentId, studentId, RestParams()).dataOrNull
    }

    suspend fun createCanvaDocSession(
        submissionId: String,
        attempt: String
    ): CanvaDocSessionResponseBody {
        return canvaDocsApi.createCanvaDocSession(CanvaDocSessionRequestBody(submissionId, attempt), RestParams(isForceReadFromNetwork = true)).dataOrThrow
    }
}