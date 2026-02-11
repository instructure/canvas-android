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
import com.instructure.canvasapi2.SubmissionContentQuery
import com.instructure.canvasapi2.fragment.SubmissionFields
import com.instructure.canvasapi2.managers.graphql.SubmissionContentManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.type.MediaType
import com.instructure.canvasapi2.type.SubmissionState
import com.instructure.canvasapi2.type.SubmissionStatusTagType
import com.instructure.canvasapi2.type.SubmissionType

class FakeSubmissionContentManager : SubmissionContentManager {
    override suspend fun getSubmissionContent(
        userId: Long,
        assignmentId: Long,
        domain: String?
    ): SubmissionContentQuery.Data {
        val assignment = MockCanvas.data.assignments[assignmentId]
        val userRootSubmission = MockCanvas.data.submissions[assignmentId]?.firstOrNull { it.userId == userId }
        val submissionHistory = userRootSubmission?.submissionHistory ?: emptyList()
        val submission = submissionHistory.firstOrNull()
        val student = MockCanvas.data.students.find { it.id == userId } ?: MockCanvas.data.students[0]
        val user = SubmissionFields.User(student.avatarUrl, student.name, student.shortName, student.sortableName)
        val submissionTypes = assignment?.getSubmissionTypes()?.map { type ->
            when (type) {
                Assignment.SubmissionType.ONLINE_TEXT_ENTRY -> SubmissionType.online_text_entry
                Assignment.SubmissionType.ONLINE_URL -> SubmissionType.online_url
                Assignment.SubmissionType.ONLINE_UPLOAD -> SubmissionType.online_upload
                Assignment.SubmissionType.MEDIA_RECORDING -> SubmissionType.media_recording
                Assignment.SubmissionType.ONLINE_QUIZ -> SubmissionType.online_quiz
                Assignment.SubmissionType.DISCUSSION_TOPIC -> SubmissionType.discussion_topic
                Assignment.SubmissionType.EXTERNAL_TOOL -> SubmissionType.basic_lti_launch
                Assignment.SubmissionType.ON_PAPER -> SubmissionType.on_paper
                Assignment.SubmissionType.NONE -> SubmissionType.none
                Assignment.SubmissionType.STUDENT_ANNOTATION -> SubmissionType.student_annotation
                else -> SubmissionType.online_text_entry
            }
        } ?: listOf(SubmissionType.online_text_entry)

        val submissionAttachments = submission?.attachments?.map { attachment ->
            SubmissionFields.Attachment(
                _id = attachment.id.toString(),
                contentType = attachment.contentType,
                mimeClass = attachment.mimeClass,
                createdAt = attachment.createdAt,
                displayName = attachment.displayName,
                id = attachment.id.toString(),
                size = attachment.size.toString(),
                thumbnailUrl = attachment.thumbnailUrl,
                title = attachment.filename,
                type = attachment.type,
                updatedAt = attachment.updatedAt,
                url = attachment.url,
                submissionPreviewUrl = attachment.submissionPreviewUrl
            )
        }

        val firstAttachment = submission?.attachments?.getOrNull(0)

        val fragmentAssignment = SubmissionFields.Assignment(
            submissionTypes,
            assignment?.htmlUrl,
            assignment?.anonymousGrading,
            assignment?.id.toString(),
            assignment?.courseId.toString(),
            dueAt = assignment?.dueDate,
            groupSet = null,
            gradeGroupStudentsIndividually = false
        )

        val mediaObject = if (submission?.submissionType == "media_recording" && firstAttachment != null) {
            val mediaType = when {
                firstAttachment.contentType?.startsWith("video/") == true -> MediaType.video
                firstAttachment.contentType?.startsWith("audio/") == true -> MediaType.audio
                else -> MediaType.video
            }
            SubmissionFields.MediaObject(
                mediaType = mediaType,
                mediaDownloadUrl = firstAttachment.url,
                title = firstAttachment.displayName,
                thumbnailUrl = firstAttachment.thumbnailUrl,
                mediaSources = listOf(SubmissionFields.MediaSource(url = firstAttachment.url))
            )
        } else null

        val dummySubmissionFields = SubmissionFields(
            groupId = "group-1",
            state = when (submission?.workflowState) {
                "pending_review" -> SubmissionState.pending_review
                "graded" -> SubmissionState.graded
                "submitted" -> SubmissionState.submitted
                null -> SubmissionState.unsubmitted
                else -> if (submission != null) SubmissionState.submitted else SubmissionState.unsubmitted
            },
            attempt = (submission?.attempt ?: 0).toInt(),
            body = submission?.body,
            url = submission?.url,
            previewUrl = submission?.url,
            submissionType = submission?.submissionType?.let { typeString ->
                when (typeString) {
                    "online_text_entry" -> SubmissionType.online_text_entry
                    "online_url" -> SubmissionType.online_url
                    "online_upload" -> SubmissionType.online_upload
                    "media_recording" -> SubmissionType.media_recording
                    "online_quiz" -> SubmissionType.online_quiz
                    "discussion_topic" -> SubmissionType.discussion_topic
                    "basic_lti_launch" -> SubmissionType.basic_lti_launch
                    "on_paper" -> SubmissionType.on_paper
                    "none" -> SubmissionType.none
                    "student_annotation" -> SubmissionType.student_annotation
                    else -> SubmissionType.online_text_entry
                }
            },
            statusTag = SubmissionStatusTagType.none,
            status = if (submission != null) "submitted" else "unsubmitted",
            submissionStatus = "on_time",
            cachedDueDate = assignment?.dueDate,
            submittedAt = submission?.submittedAt,
            attachments = submissionAttachments,
            mediaObject = mediaObject,
            user = user,
            assignment = fragmentAssignment,
            customGradeStatus = null
        )

        val edges = if (submissionHistory.isNotEmpty()) {
            submissionHistory.mapNotNull { attemptSubmission ->
                attemptSubmission?.let {
                    val attemptAttachments = it.attachments?.map { attachment ->
                        SubmissionFields.Attachment(
                            _id = attachment.id.toString(),
                            contentType = attachment.contentType,
                            mimeClass = attachment.mimeClass,
                            createdAt = attachment.createdAt,
                            displayName = attachment.displayName,
                            id = attachment.id.toString(),
                            size = attachment.size.toString(),
                            thumbnailUrl = attachment.thumbnailUrl,
                            title = attachment.filename,
                            type = attachment.type,
                            updatedAt = attachment.updatedAt,
                            url = attachment.url,
                            submissionPreviewUrl = attachment.submissionPreviewUrl
                        )
                    }

                    val attemptFirstAttachment = it.attachments?.getOrNull(0)

                    val attemptMediaObject = if (it.submissionType == "media_recording" && attemptFirstAttachment != null) {
                        val mediaType = when {
                            attemptFirstAttachment.contentType?.startsWith("video/") == true -> MediaType.video
                            attemptFirstAttachment.contentType?.startsWith("audio/") == true -> MediaType.audio
                            else -> MediaType.video
                        }
                        SubmissionFields.MediaObject(
                            mediaType = mediaType,
                            mediaDownloadUrl = attemptFirstAttachment.url,
                            title = attemptFirstAttachment.displayName,
                            thumbnailUrl = attemptFirstAttachment.thumbnailUrl,
                            mediaSources = listOf(SubmissionFields.MediaSource(url = attemptFirstAttachment.url))
                        )
                    } else null

                    val fields = dummySubmissionFields.copy(
                        attempt = it.attempt.toInt(),
                        body = it.body,
                        url = it.url,
                        previewUrl = it.url,
                        attachments = attemptAttachments,
                        mediaObject = attemptMediaObject
                    )
                    val node = SubmissionContentQuery.Node(__typename = "Submission", submissionFields = fields)
                    SubmissionContentQuery.Edge(node = node)
                }
            }
        } else {
            // When there are no submissions, still create one edge with unsubmitted state
            // so the ViewModel can access the assignment information
            val node = SubmissionContentQuery.Node(__typename = "Submission", submissionFields = dummySubmissionFields)
            listOf(SubmissionContentQuery.Edge(node = node))
        }

        val dummyHistoriesConnection = SubmissionContentQuery.SubmissionHistoriesConnection(
            edges = edges,
            pageInfo = SubmissionContentQuery.PageInfo(
                hasNextPage = false,
                hasPreviousPage = false,
                startCursor = "start-cursor",
                endCursor = "end-cursor"
            )
        )
        val dummySubmission = SubmissionContentQuery.Submission(
            __typename = "Submission",
            _id = submission?.id?.toString() ?: "0",
            userId = userId.toString(),
            submissionHistoriesConnection = dummyHistoriesConnection,
            submissionFields = dummySubmissionFields
        )
        return SubmissionContentQuery.Data(submission = dummySubmission)
    }
}