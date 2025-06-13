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
 */package com.instructure.pandautils.features.speedgrader.grade.grading

import com.instructure.canvasapi2.SubmissionGradeQuery
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.graphql.SubmissionGradeManager
import com.instructure.canvasapi2.models.Submission

class SpeedGraderGradingRepository(
    private val submissionGradeManager: SubmissionGradeManager,
    private val submissionApi: SubmissionAPI.SubmissionInterface
) {

    suspend fun getSubmissionGrade(assignmentId: Long, studentId: Long): SubmissionGradeQuery.Data {
        return submissionGradeManager.getSubmissionGrade(assignmentId, studentId)
    }

    suspend fun updateSubmissionGrade(score: Double, userId: Long, assignmentId: Long, courseId: Long): Submission {
        return submissionApi.postSubmissionGrade(
            contextId = courseId,
            assignmentId = assignmentId,
            userId = userId,
            assignmentScore = score.toString(),
            isExcused = false,
            restParams = RestParams()
        ).dataOrThrow
    }
}