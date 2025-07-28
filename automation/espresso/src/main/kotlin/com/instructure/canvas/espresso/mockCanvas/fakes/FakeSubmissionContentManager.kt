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
import com.instructure.canvasapi2.SubmissionContentQuery
import com.instructure.canvasapi2.fragment.SubmissionFields
import com.instructure.canvasapi2.managers.graphql.SubmissionContentManager
import com.instructure.canvasapi2.type.SubmissionState
import com.instructure.canvasapi2.type.SubmissionStatusTagType
import com.instructure.canvasapi2.type.SubmissionType

class FakeSubmissionContentManager : SubmissionContentManager {
    override suspend fun getSubmissionContent(
        userId: Long,
        assignmentId: Long
    ): SubmissionContentQuery.Data {
        val assignment = MockCanvas.data.assignments[assignmentId]
        val course = MockCanvas.data.courses[assignment?.courseId]
        val submission = MockCanvas.data.submissions[assignmentId]?.get(0)
        val student = MockCanvas.data.students[0]
        val user = SubmissionFields.User(student.avatarUrl, student.name, student.shortName, student.sortableName)
        val submissionTypes = listOf(SubmissionType.online_text_entry) //TODO

        val attachment = submission?.attachments?.getOrNull(0)
        val submissionAttachment = if (attachment != null) SubmissionFields.Attachment(
            _id = attachment.id.toString(),
            contentType = attachment.contentType,
            mimeClass = attachment.mimeClass,
            createdAt = attachment.createdAt,
            displayName = attachment.displayName,
            id = attachment.id.toString(),
            size = attachment.size?.toString(),
            thumbnailUrl = attachment.thumbnailUrl,
            title = attachment.filename,
            type = attachment.type,
            updatedAt = attachment.updatedAt,
            url = attachment.url,
            submissionPreviewUrl = attachment.submissionPreviewUrl
        ) else null

        val fragmentAssignment = SubmissionFields.Assignment(
            submissionTypes,
            assignment?.htmlUrl,
            assignment?.anonymousGrading,
            assignment?.id.toString(),
            assignment?.courseId.toString(),
            dueAt = assignment?.dueDate
        )
        val dummySubmissionFields = SubmissionFields(
            groupId = "group-1",
            state = SubmissionState.submitted,
            attempt = (submission?.attempt ?: 1).toInt(),
            body = submission?.body,
            url = "https://dummy.url/submission",
            previewUrl = "https://dummy.url/preview",
            submissionType = SubmissionType.online_text_entry,
            statusTag = SubmissionStatusTagType.none,
            status = "submitted",
            submissionStatus = "on_time",
            cachedDueDate = assignment?.dueDate,
            submittedAt = submission?.submittedAt,
            attachments = if (submissionAttachment == null) null else listOf(submissionAttachment),
            mediaObject = null,
            user = user,
            assignment = fragmentAssignment ?: null
        )

        val dummyNode = SubmissionContentQuery.Node(
            __typename = "Submission",
            submissionFields = dummySubmissionFields
        )
        val dummyEdge = SubmissionContentQuery.Edge(node = dummyNode)
        val dummyHistoriesConnection = SubmissionContentQuery.SubmissionHistoriesConnection(
            edges = listOf(dummyEdge),
            pageInfo = SubmissionContentQuery.PageInfo(
                hasNextPage = false,
                hasPreviousPage = false,
                startCursor = "start-cursor",
                endCursor = "end-cursor"
            )
        )
        val dummySubmission = if (submission == null) null else SubmissionContentQuery.Submission(
            __typename = "Submission",
            _id = submission.id.toString(),
            userId = userId.toString(),
            submissionHistoriesConnection = dummyHistoriesConnection,
            submissionFields = dummySubmissionFields
        )
        return SubmissionContentQuery.Data(submission = dummySubmission)
    }
}