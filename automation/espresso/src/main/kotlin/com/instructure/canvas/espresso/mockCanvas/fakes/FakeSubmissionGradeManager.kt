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
import com.instructure.canvasapi2.type.GradingType
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
        /*val gradingStandard = SubmissionGradeQuery.GradingStandard(  // TODO: This should be mocked somehow else because Data1 is a generated type and uiState.letterGrades expected GradingSchemeRow list.
            data = listOf(
                SubmissionGradeQuery.Data1(
                    baseValue = 90.0,
                    letterGrade = "A"
                ),
                SubmissionGradeQuery.Data1(
                    baseValue = 80.0,
                    letterGrade = "B"
                ),
                SubmissionGradeQuery.Data1(
                    baseValue = 70.0,
                    letterGrade = "C"
                ),
                SubmissionGradeQuery.Data1(
                    baseValue = 60.0,
                    letterGrade = "D"
                ),
                SubmissionGradeQuery.Data1(
                    baseValue = 50.0,
                    letterGrade = "E"
                ),
                SubmissionGradeQuery.Data1(
                    baseValue = 40.0,
                    letterGrade = "F"
                )
            )
        )*/
        val queryAssignment = SubmissionGradeQuery.Assignment(
            dueAt = assignment?.dueDate,
            gradingType = GradingType.entries.firstOrNull { it.rawValue == assignment?.gradingType },
            pointsPossible = assignment?.pointsPossible ?: 100.0,
            gradingStandard = null, // gradingStandard should be passed once it's mocked out correctly.
            course = null
        )
        val dummySubmission = SubmissionGradeQuery.Submission(
            gradingStatus = SubmissionGradingStatus.needs_grading,
            grade = submission?.grade ?: "100",
            gradeHidden = false,
            _id = submission?.id.toString(),
            submissionStatus = "on_time",
            status = "submitted",
            latePolicyStatus = LatePolicyStatusType.none,
            late = submission?.late ?: false,
            secondsLate = 0.0,
            deductedPoints = 0.0,
            score = submission?.score ?: 100.0,
            excused = submission?.excused ?: false,
            enteredGrade = submission?.enteredGrade ?: "100",
            enteredScore = submission?.score ?: 100.0,
            assignment = queryAssignment,
            hideGradeFromStudent = false,
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