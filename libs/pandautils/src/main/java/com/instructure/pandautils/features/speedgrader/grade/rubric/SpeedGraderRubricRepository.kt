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
package com.instructure.pandautils.features.speedgrader.grade.rubric

import com.instructure.canvasapi2.SubmissionRubricQuery
import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.SubmissionRubricManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Submission

class SpeedGraderRubricRepository(
    private val submissionRubricManager: SubmissionRubricManager,
    private val assignmentApi: AssignmentAPI.AssignmentInterface,
    private val submissionApi: SubmissionAPI.SubmissionInterface
) {

    suspend fun getRubrics(assignmentId: Long, userId: Long): SubmissionRubricQuery.Data {
        return submissionRubricManager.getRubrics(assignmentId, userId)
    }

    suspend fun getAssignmentRubric(courseId: Long, assignmentId: Long): Assignment {
        return assignmentApi.getAssignment(courseId, assignmentId, RestParams()).dataOrThrow
    }

    suspend fun postSubmissionRubricAssessment(
        courseId: Long,
        assignmentId: Long,
        userId: Long,
        rubricAssessmentMap: Map<String, String>
    ): Submission {
        return submissionApi.postSubmissionRubricAssessmentMap(
            courseId,
            assignmentId,
            userId,
            rubricAssessmentMap,
            RestParams()
        ).dataOrThrow
    }
}