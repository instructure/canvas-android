/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.data.datasource

import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Submission
import com.instructure.horizon.database.dao.HorizonSubmissionDao
import com.instructure.horizon.database.entity.HorizonSubmissionAttachmentEntity
import com.instructure.horizon.database.entity.HorizonSubmissionEntity
import java.util.Date
import javax.inject.Inject

class SubmissionLocalDataSource @Inject constructor(
    private val submissionDao: HorizonSubmissionDao,
) {

    suspend fun getSubmissions(assignmentId: Long): List<Submission> {
        val submissionEntities = submissionDao.getSubmissions(assignmentId)
        if (submissionEntities.isEmpty()) return emptyList()
        val attachmentsByAttempt = submissionDao.getAttachments(assignmentId).groupBy { it.attempt }
        return submissionEntities.map { entity ->
            entity.toSubmission(attachmentsByAttempt[entity.attempt] ?: emptyList())
        }
    }

    suspend fun saveSubmissions(assignmentId: Long, submissions: List<Submission>) {
        val submissionEntities = submissions.map { it.toEntity(assignmentId) }
        val attachmentEntities = submissions.flatMap { submission ->
            submission.attachments.map { it.toEntity(assignmentId, submission.attempt) }
        }
        submissionDao.replaceForAssignment(assignmentId, submissionEntities, attachmentEntities)
    }

    private fun HorizonSubmissionEntity.toSubmission(
        attachments: List<HorizonSubmissionAttachmentEntity>,
    ): Submission {
        return Submission(
            id = submissionId,
            assignmentId = assignmentId,
            attempt = attempt,
            grade = grade,
            score = score,
            submittedAt = submittedAtMs?.let { Date(it) },
            workflowState = workflowState,
            submissionType = submissionType,
            body = body,
            url = url,
            late = late,
            excused = excused,
            missing = missing,
            customGradeStatusId = customGradeStatusId,
            userId = userId,
            attachments = ArrayList(attachments.map { it.toAttachment() }),
        )
    }

    private fun HorizonSubmissionAttachmentEntity.toAttachment(): Attachment {
        return Attachment(
            id = id,
            displayName = displayName,
            url = url,
            contentType = contentType,
            thumbnailUrl = thumbnailUrl,
        )
    }

    private fun Submission.toEntity(assignmentId: Long): HorizonSubmissionEntity {
        return HorizonSubmissionEntity(
            assignmentId = assignmentId,
            attempt = attempt,
            submissionId = id,
            grade = grade,
            score = score,
            submittedAtMs = submittedAt?.time,
            workflowState = workflowState,
            submissionType = submissionType,
            body = body,
            url = url,
            late = late,
            excused = excused,
            missing = missing,
            customGradeStatusId = customGradeStatusId,
            userId = userId,
        )
    }

    private fun Attachment.toEntity(assignmentId: Long, attempt: Long): HorizonSubmissionAttachmentEntity {
        return HorizonSubmissionAttachmentEntity(
            id = id,
            assignmentId = assignmentId,
            attempt = attempt,
            displayName = displayName,
            url = url,
            contentType = contentType,
            thumbnailUrl = thumbnailUrl,
        )
    }
}
