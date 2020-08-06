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

import com.instructure.canvas.espresso.mockCanvas.Endpoint
import com.instructure.canvas.espresso.mockCanvas.utils.LongId
import com.instructure.canvas.espresso.mockCanvas.utils.PathVars
import com.instructure.canvas.espresso.mockCanvas.utils.Segment
import com.instructure.canvas.espresso.mockCanvas.utils.successResponse
import com.instructure.canvas.espresso.mockCanvas.utils.unauthorizedResponse
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.GradeableStudent
import com.instructure.canvasapi2.models.SubmissionSummary
import okio.Buffer
import org.json.JSONObject

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
    Segment("gradeable_students") to GradeableStudentsEndpoint,
    response = {
        GET {
            val assignment = data.assignments[pathVars.assignmentId]

            if (assignment != null) {
                request.successResponse(assignment)
            } else {
                request.unauthorizedResponse()
            }

        }

        PUT {
            val assignment = data.assignments[pathVars.assignmentId]
            if(assignment != null) {

                // Sigh... Need to extract the json object from the body
                val buffer = Buffer()
                request.body()?.writeTo(buffer)
                val stringOutput = buffer.readUtf8()
                val jsonObject = JSONObject(stringOutput)

                // Then extract the modified assignment object from the json object
                val assignmentObject = jsonObject.getJSONObject("assignment")

                // Now see if we have either a name or points_possible object to apply to a modified assignment
                // TODO: Support additional fields being changed?
                val newName = assignmentObject?.getString("name")
                val newPoints = assignmentObject?.getDouble("points_possible")
                if(newName != null || newPoints != null) {
                    val name = newName ?: assignment.name
                    val points = newPoints ?: assignment.pointsPossible
                    val modifiedAssignment = assignment.copy(
                            name = name,
                            pointsPossible = points
                    )
                    data.assignments.put(pathVars.assignmentId, modifiedAssignment)
                }

                // May or may not have been modified
                val finalAssignment = data.assignments[pathVars.assignmentId]
                request.successResponse(finalAssignment!!)
            }
            else {
                request.unauthorizedResponse()
            }
        }
    }
)

/**
 * Endpoint that returns gradeable students for an assignment
 */
object GradeableStudentsEndpoint : Endpoint ( response = {
    GET {
        val assignment = data.assignments[pathVars.assignmentId]
        val courseId = pathVars.courseId
        val gradeableStudents = data.enrollments.values
                        .filter {e -> e.courseId == courseId && e.isStudent}
                        .map {e -> GradeableStudent(id = e.userId, displayName = e.user?.shortName ?: "", pronouns = e.user?.pronouns)}
        request.successResponse(gradeableStudents)
    }
})
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
