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
import com.instructure.canvasapi2.SubmissionRubricQuery
import com.instructure.canvasapi2.managers.SubmissionRubricManager

class FakeSubmissionRubricManager : SubmissionRubricManager {
    override suspend fun getRubrics(assignmentId: Long, userId: Long): SubmissionRubricQuery.Data {

        val assignment = MockCanvas.data.assignments[assignmentId]
        val course = MockCanvas.data.courses[assignment?.courseId]
        val submission = MockCanvas.data.submissions[assignmentId]?.get(0)

        // Dummy Rubric Rating
        val dummyRating = SubmissionRubricQuery.Rating(
            _id = "rating-1",
            description = "Excellent work",
            longDescription = "Detailed feedback for excellent work.",
            points = 5.0,
            rubricId = "rubric-1"
        )
        // Dummy Rubric Criterion
        val dummyCriterion = SubmissionRubricQuery.Criterium(
            _id = "criterion-1",
            description = "Criterion description",
            longDescription = "Long description of criterion",
            points = 5.0,
            ratings = listOf(dummyRating)
        )
        // Dummy Rubric
        val dummyRubric = SubmissionRubricQuery.Rubric(
            _id = "rubric-1",
            buttonDisplay = "always",
            criteriaCount = 1,
            criteria = listOf(dummyCriterion)
        )
        // Dummy Assignment
        val dummyAssignment = SubmissionRubricQuery.Assignment(
            rubric = dummyRubric
        )
        // Dummy PageInfo
        val dummyPageInfo = SubmissionRubricQuery.PageInfo(
            endCursor = "end-cursor",
            startCursor = "start-cursor",
            hasNextPage = false,
            hasPreviousPage = false
        )
        // Dummy Assessment Rating
        val dummyAssessmentRating = SubmissionRubricQuery.AssessmentRating(
            comments = "Good job!",
            commentsEnabled = true,
            commentsHtml = "<p>Good job!</p>",
            criterion = null,
            description = "Assessment rating description",
            _id = "assessment-rating-1",
            points = 5.0
        )
        // Dummy Node
        val dummyNode = SubmissionRubricQuery.Node(
            _id = "node-1",
            score = 5.0,
            assessmentRatings = listOf(dummyAssessmentRating),
            artifactAttempt = 1
        )
        // Dummy Edge
        val dummyEdge = SubmissionRubricQuery.Edge(
            node = dummyNode
        )
        // Dummy RubricAssessmentsConnection
        val dummyRubricAssessmentsConnection = SubmissionRubricQuery.RubricAssessmentsConnection(
            pageInfo = dummyPageInfo,
            edges = listOf(dummyEdge)
        )
        // Dummy Submission
        val dummySubmission = SubmissionRubricQuery.Submission(
            assignment = dummyAssignment,
            rubricAssessmentsConnection = dummyRubricAssessmentsConnection
        )
        return SubmissionRubricQuery.Data(submission = dummySubmission)
    }
}