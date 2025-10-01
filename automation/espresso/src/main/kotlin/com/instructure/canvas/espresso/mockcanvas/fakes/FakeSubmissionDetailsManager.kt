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
import com.instructure.canvasapi2.SubmissionDetailsQuery
import com.instructure.canvasapi2.managers.graphql.SubmissionDetailsManager
import com.instructure.canvasapi2.type.SubmissionType

class FakeSubmissionDetailsManager : SubmissionDetailsManager {
    override suspend fun getSubmissionDetails(userId: Long, assignmentId: Long): SubmissionDetailsQuery.Data {

        val assignment = MockCanvas.data.assignments[assignmentId]
        val course = MockCanvas.data.courses[assignment?.courseId]
        val submission = MockCanvas.data.submissions[assignmentId]?.get(0)

        val dummyNode = SubmissionDetailsQuery.Node(
            attempt = submission?.attempt?.toInt() ?: 1,
            wordCount = 123.0,
            submissionType = SubmissionType.online_text_entry,
        )
        val dummyEdge = SubmissionDetailsQuery.Edge(
            node = dummyNode
        )
        val dummyHistoriesConnection = SubmissionDetailsQuery.SubmissionHistoriesConnection(
            edges = listOf(dummyEdge)
        )
        val dummySubmission = SubmissionDetailsQuery.Submission(
            submissionHistoriesConnection = dummyHistoriesConnection
        )
        return SubmissionDetailsQuery.Data(submission = dummySubmission)
    }
}
