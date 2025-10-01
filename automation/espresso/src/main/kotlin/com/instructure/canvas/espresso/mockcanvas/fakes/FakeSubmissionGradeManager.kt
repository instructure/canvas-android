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
package com.instructure.canvas.espresso.mockcanvas.fakes

import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvasapi2.SubmissionGradeQuery
import com.instructure.canvasapi2.UpdateSubmissionGradeMutation
import com.instructure.canvasapi2.UpdateSubmissionStatusMutation
import com.instructure.canvasapi2.managers.graphql.SubmissionGradeManager
import com.instructure.canvasapi2.type.GradingType
import com.instructure.canvasapi2.type.LatePolicyStatusType
import com.instructure.canvasapi2.type.SubmissionGradingStatus
import com.instructure.pandautils.utils.orDefault
import java.util.Date

class FakeSubmissionGradeManager : SubmissionGradeManager {
    override suspend fun getSubmissionGrade(
        assignmentId: Long,
        studentId: Long,
        forceNetwork: Boolean
    ): SubmissionGradeQuery.Data {
        val assignment = MockCanvas.data.assignments[assignmentId]
        val course = MockCanvas.data.courses[assignment?.courseId]
        val submission = MockCanvas.data.submissions[assignmentId]?.get(0)
        val gradingStandard = SubmissionGradeQuery.GradingStandard1(
            data = listOf(
                SubmissionGradeQuery.Data2(
                    baseValue = 0.9,
                    letterGrade = "A"
                ),
                SubmissionGradeQuery.Data2(
                    baseValue = 0.8,
                    letterGrade = "B"
                ),
                SubmissionGradeQuery.Data2(
                    baseValue = 0.7,
                    letterGrade = "C"
                ),
                SubmissionGradeQuery.Data2(
                    baseValue = 0.6,
                    letterGrade = "D"
                ),
                SubmissionGradeQuery.Data2(
                    baseValue = 0.5,
                    letterGrade = "E"
                ),
                SubmissionGradeQuery.Data2(
                    baseValue = 0.4,
                    letterGrade = "F"
                )
            )
        )
        val queryAssignment = SubmissionGradeQuery.Assignment(
            dueAt = assignment?.dueDate,
            gradingType = GradingType.entries.firstOrNull { it.rawValue == assignment?.gradingType },
            pointsPossible = assignment?.pointsPossible ?: 100.0,
            gradingStandard = null,
            course = SubmissionGradeQuery.Course(null, gradingStandard, emptyList())
        )
        val dummySubmission = SubmissionGradeQuery.Submission(
            gradingStatus = SubmissionGradingStatus.needs_grading,
            grade = submission?.grade ?: "100",
            gradeHidden = false,
            _id = submission?.id.toString(),
            submissionStatus = "on_time",
            status = submission?.status ?: "submitted",
            latePolicyStatus = LatePolicyStatusType.none,
            late = submission?.late ?: false,
            secondsLate = if (submission?.late.orDefault()) 86400.0 else 0.0,
            deductedPoints = 0.0,
            score = submission?.score ?: 100.0,
            excused = submission?.excused ?: false,
            enteredGrade = submission?.enteredGrade ?: "100",
            enteredScore = submission?.score ?: 100.0,
            assignment = queryAssignment,
            hideGradeFromStudent = false,
            submittedAt = Date()
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