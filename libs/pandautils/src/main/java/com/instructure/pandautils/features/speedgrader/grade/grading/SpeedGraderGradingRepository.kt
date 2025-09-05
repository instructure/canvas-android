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
import com.instructure.canvasapi2.UpdateSubmissionStatusMutation
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.graphql.SubmissionGradeManager
import com.instructure.canvasapi2.models.Submission

class SpeedGraderGradingRepository(
    private val submissionGradeManager: SubmissionGradeManager,
    private val submissionApi: SubmissionAPI.SubmissionInterface
) {

    suspend fun getSubmissionGrade(
        assignmentId: Long,
        studentId: Long,
        forceNetwork: Boolean
    ): SubmissionGradeQuery.Data {
        return submissionGradeManager.getSubmissionGrade(assignmentId, studentId, forceNetwork)
    }

    suspend fun updateSubmissionGrade(
        score: String,
        userId: Long,
        assignmentId: Long,
        courseId: Long,
        excused: Boolean,
        subAssignmentTag: String? = null,
    ): Submission {
        return submissionApi.postSubmissionGrade(
            contextId = courseId,
            assignmentId = assignmentId,
            userId = userId,
            assignmentScore = score,
            isExcused = excused,
            subAssignmentTag = subAssignmentTag,
            restParams = RestParams()
        ).dataOrThrow
    }

    suspend fun excuseSubmission(
        userId: Long,
        assignmentId: Long,
        courseId: Long,
        subAssignmentTag: String? = null,
    ): Submission {
        return submissionApi.postSubmissionExcusedStatus(
            contextId = courseId,
            assignmentId = assignmentId,
            userId = userId,
            isExcused = true,
            subAssignmentTag = subAssignmentTag,
            restParams = RestParams()
        ).dataOrThrow
    }


    suspend fun updateSubmissionStatus(
        submissionId: Long,
        customStatusId: String? = null,
        latePolicyStatus: String? = null,
        checkpointTag: String? = null
    ): UpdateSubmissionStatusMutation.Data {
        return submissionGradeManager.updateSubmissionStatus(
            submissionId,
            customStatusId,
            latePolicyStatus,
            checkpointTag
        )
    }

    suspend fun updateLateSecondsOverride(
        userId: Long,
        assignmentId: Long,
        courseId: Long,
        lateSeconds: Int
    ): Submission {
        return submissionApi.postSubmissionLateSecondsOverride(
            courseId,
            assignmentId,
            userId,
            lateSeconds,
            RestParams(isForceReadFromNetwork = true)
        ).dataOrThrow
    }
}