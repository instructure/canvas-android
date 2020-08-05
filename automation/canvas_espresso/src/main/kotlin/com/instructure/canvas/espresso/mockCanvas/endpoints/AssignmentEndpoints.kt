/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.canvas.espresso.mockCanvas.endpoints

import android.util.Log
import com.instructure.canvas.espresso.mockCanvas.Endpoint
import com.instructure.canvas.espresso.mockCanvas.utils.*
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.SubmissionSummary

/**
 * Endpoint for assignment index, for a course
 *
 * ROUTES:
 * - `{assignmentId}` -> [Assignment]
 */
object AssignmentIndexEndpoint : Endpoint(
    LongId(PathVars::assignmentId) to AssignmentEndpoint
)

/**
 * Endpoint that returns a specific assignment for a course. If the requested assignment does not
 * exist in the assignment list, an unauth'd response is returned.
 *
 * ROUTES:
 * - `submissions` -> [SubmissionIndexEndpoint]
 */
object AssignmentEndpoint : Endpoint(
    Segment("submissions") to SubmissionIndexEndpoint,
    Segment("submission_summary") to SubmissionSummaryEndpoint,
    response = {
        GET {
            val assignment = data.assignments[pathVars.assignmentId]

            if (assignment != null) {
                request.successResponse(assignment)
            } else {
                request.unauthorizedResponse()
            }

        }
    }
)

/**
 * Endpoint that returns a submission summary for a specified assignment
 */
object SubmissionSummaryEndpoint : Endpoint( response = {
    GET {
        val assignment = data.assignments[pathVars.assignmentId]
        val courseId = pathVars.courseId
        val studentCount = data.enrollments.values.filter {e -> e.courseId == courseId && e.isStudent}.size
        val submissionCount = data.submissions[assignment?.id]?.size ?: 0
        val gradedCount = data.submissions[assignment?.id]?.filter {submission -> submission.isGraded}?.size ?: 0
        request.successResponse(
                SubmissionSummary(
                        notSubmitted = studentCount - submissionCount,
                        graded = gradedCount,
                        ungraded = submissionCount - gradedCount
                )
        )
    }
})

/**
 * Endpoint for assignment groups for a course
 */
object AssignmentGroupListEndpoint : Endpoint(
    LongId(PathVars::assignmentId) to AssignmentEndpoint,
    response = {
        GET {
            request.successResponse(data.assignmentGroups[pathVars.courseId] ?: listOf(AssignmentGroup()))
        }
    }
)
