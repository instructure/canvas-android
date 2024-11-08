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
import com.instructure.canvasapi2.models.ObserveeAssignment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.ObserveeAssignmentGroup
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
    LongId(PathVars::assignmentId) to AssignmentEndpoint,
    response = {
        GET {
            val courseId = pathVars.courseId
            val assignments = data.assignments.values.filter { assignment -> assignment.courseId == courseId }
            request.successResponse(assignments)
        }
    }
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
                if (request.url.queryParameterValues("include[]").contains("observed_users")) {
                    request.successResponse(assignment.toObserveeAssignment())
                } else {
                    request.successResponse(assignment)
                }
            } else {
                request.unauthorizedResponse()
            }

        }

        PUT {
            val assignment = data.assignments[pathVars.assignmentId]
            if (assignment != null) {

                // Sigh... Need to extract the json object from the body
                val buffer = Buffer()
                request.body?.writeTo(buffer)
                val stringOutput = buffer.readUtf8()
                val jsonObject = JSONObject(stringOutput)

                // Then extract the modified assignment object from the json object
                val assignmentObject = jsonObject.getJSONObject("assignment")!!

                // Right now, we are only cognizant of changes to the name and points_possible fields,
                // because that's all that is required.  In the future, we might need to be cognizant of
                // additional fields.
                // TODO: Support additional fields being changed?
                val newName = assignmentObject.optString("name", null) ?: assignment.name
                val newPoints =
                    if (assignmentObject.has("points_possible")) assignmentObject.getDouble("points_possible") else assignment.pointsPossible
                val modifiedAssignment = assignment.copy(
                    name = newName,
                    pointsPossible = newPoints
                )
                data.assignments.put(pathVars.assignmentId, modifiedAssignment)
                request.successResponse(modifiedAssignment)
            } else {
                request.unauthorizedResponse()
            }
        }
    }
)

/**
 * Endpoint that returns gradeable students for an assignment
 */
object GradeableStudentsEndpoint : Endpoint(response = {
    GET {
        val assignment = data.assignments[pathVars.assignmentId]
        val courseId = pathVars.courseId
        val gradeableStudents = data.enrollments.values
            .filter { e -> e.courseId == courseId && e.isStudent }
            .map { e -> GradeableStudent(id = e.userId, displayName = e.user?.shortName ?: "", pronouns = e.user?.pronouns) }
        request.successResponse(gradeableStudents)
    }
})

/**
 * Endpoint that returns a submission summary for a specified assignment
 */
object SubmissionSummaryEndpoint : Endpoint(response = {
    GET {
        val assignment = data.assignments[pathVars.assignmentId]
        val courseId = pathVars.courseId
        val studentCount = data.enrollments.values.filter { e -> e.courseId == courseId && e.isStudent }.size
        val submissionCount = data.submissions[assignment?.id]?.size ?: 0
        val gradedCount = data.submissions[assignment?.id]?.filter { submission -> submission.isGraded }?.size ?: 0
        val summary = SubmissionSummary(
            notSubmitted = studentCount - submissionCount,
            graded = gradedCount,
            ungraded = submissionCount - gradedCount
        )

        request.successResponse(
            summary
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
            if (request.url.queryParameterValues("include[]").contains("observed_users")) {
                val gradingPeriodId = request.url.queryParameterValues("grading_period_id").firstOrNull()?.toLongOrNull()
                val assignmentGroups = data.assignmentGroups[pathVars.courseId].orEmpty().map {
                    it.toObserveeAssignmentGroup()
                }
                // Invalid grading period ID
                if (gradingPeriodId == -1L) {
                    request.successResponse(emptyList<ObserveeAssignmentGroup>())
                } else {
                    request.successResponse(assignmentGroups)
                }
            } else {
                request.successResponse(data.assignmentGroups[pathVars.courseId] ?: listOf(AssignmentGroup()))
            }
        }
    }
)

private fun AssignmentGroup.toObserveeAssignmentGroup() = ObserveeAssignmentGroup(
    id = id,
    name = name,
    position = position,
    groupWeight = groupWeight,
    assignments = assignments.map { it.toObserveeAssignment() },
    rules = rules
)

private fun Assignment.toObserveeAssignment() = ObserveeAssignment(
    id = id,
    name = name,
    description = description,
    submissionTypesRaw = submissionTypesRaw,
    dueAt = dueAt,
    pointsPossible = pointsPossible,
    courseId = courseId,
    isGradeGroupsIndividually = isGradeGroupsIndividually,
    gradingType = gradingType,
    needsGradingCount = needsGradingCount,
    htmlUrl = htmlUrl,
    url = url,
    quizId = quizId,
    rubric = rubric,
    isUseRubricForGrading = isUseRubricForGrading,
    rubricSettings = rubricSettings,
    allowedExtensions = allowedExtensions,
    submissionList = listOfNotNull(submission),
    assignmentGroupId = assignmentGroupId,
    position = position,
    isPeerReviews = isPeerReviews,
    lockInfo = lockInfo,
    lockedForUser = lockedForUser,
    lockAt = lockAt,
    unlockAt = unlockAt,
    lockExplanation = lockExplanation,
    discussionTopicHeader = discussionTopicHeader,
    needsGradingCountBySection = needsGradingCountBySection,
    freeFormCriterionComments = freeFormCriterionComments,
    published = published,
    groupCategoryId = groupCategoryId,
    allDates = allDates,
    userSubmitted = userSubmitted,
    unpublishable = unpublishable,
    overrides = overrides,
    onlyVisibleToOverrides = onlyVisibleToOverrides,
    anonymousPeerReviews = anonymousPeerReviews,
    moderatedGrading = moderatedGrading,
    anonymousGrading = anonymousGrading,
    allowedAttempts = allowedAttempts,
    isStudioEnabled = isStudioEnabled
)