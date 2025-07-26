/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.canvas.espresso.mockCanvas.fakes

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvasapi2.SubmissionGradeQuery
import com.instructure.canvasapi2.UpdateSubmissionGradeMutation
import com.instructure.canvasapi2.UpdateSubmissionStatusMutation
import com.instructure.canvasapi2.managers.graphql.SubmissionGradeManager
import com.instructure.canvasapi2.type.LatePolicyStatusType
import com.instructure.canvasapi2.type.SubmissionGradingStatus

class FakeSubmissionGradeManager : SubmissionGradeManager {
    override suspend fun getSubmissionGrade(
        assignmentId: Long,
        studentId: Long,
        forceNetwork: Boolean
    ): SubmissionGradeQuery.Data {
        val assignment = MockCanvas.data.assignments[assignmentId]
        val course = MockCanvas.data.courses[assignment?.courseId]
        val submission = MockCanvas.data.submissions[assignmentId]?.get(0)
        val dummySubmission = SubmissionGradeQuery.Submission(
            gradingStatus = SubmissionGradingStatus.needs_grading,
            grade = submission?.grade ?: "A",
            gradeHidden = false,
            _id = submission?.id.toString(),
            submissionStatus = "on_time",
            status = "submitted",
            latePolicyStatus = LatePolicyStatusType.none,
            late = false,
            secondsLate = 0.0,
            deductedPoints = 0.0,
            score = submission?.score ?: 100.0,
            excused = submission?.excused ?: false,
            enteredGrade = submission?.enteredGrade ?: "A",
            enteredScore = submission?.score ?: 100.0,
            assignment = null,
        )
        return SubmissionGradeQuery.Data(submission = dummySubmission)
    }

    override suspend fun updateSubmissionGrade(
        score: Double,
        submissionId: Long
    ): UpdateSubmissionGradeMutation.Data {
        TODO("Not yet implemented")
    }

    override suspend fun updateSubmissionStatus(
        submissionId: Long,
        customGradeStatusId: String?,
        latePolicyStatus: String?
    ): UpdateSubmissionStatusMutation.Data {
        TODO("Not yet implemented")
    }
}