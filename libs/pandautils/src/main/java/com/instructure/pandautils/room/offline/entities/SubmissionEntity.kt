/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 */

package com.instructure.pandautils.room.offline.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import com.instructure.canvasapi2.models.*
import com.instructure.pandautils.utils.orDefault
import java.util.*

@Entity(
    primaryKeys = ["id", "attempt"],
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AssignmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["assignmentId"],
            onDelete = ForeignKey.CASCADE,
            deferred = true
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class SubmissionEntity(
    val id: Long,
    val grade: String?,
    val score: Double,
    val attempt: Long,
    val submittedAt: Date?,
    val commentCreated: Date?,
    val mediaContentType: String?,
    val mediaCommentUrl: String?,
    val mediaCommentDisplay: String?,
    val body: String?,
    val isGradeMatchesCurrentSubmission: Boolean,
    val workflowState: String?,
    val submissionType: String?,
    val previewUrl: String?,
    val url: String?,
    val late: Boolean,
    val excused: Boolean,
    val missing: Boolean,
    val mediaCommentId: String?,
    val assignmentId: Long,
    val userId: Long?,
    val graderId: Long?,
    val groupId: Long?,
    val pointsDeducted: Double?,
    val enteredScore: Double,
    val enteredGrade: String?,
    val postedAt: Date?,
    val gradingPeriodId: Long?
) {
    constructor(submission: Submission, groupId: Long?, mediaCommentId: String?) : this(
        id = submission.id,
        grade = submission.grade,
        score = submission.score,
        attempt = submission.attempt,
        submittedAt = submission.submittedAt,
        commentCreated = submission.commentCreated,
        mediaContentType = submission.mediaContentType,
        mediaCommentUrl = submission.mediaCommentUrl,
        mediaCommentDisplay = submission.mediaCommentDisplay,
        body = submission.body,
        isGradeMatchesCurrentSubmission = submission.isGradeMatchesCurrentSubmission,
        workflowState = submission.workflowState,
        submissionType = submission.submissionType,
        previewUrl = submission.previewUrl,
        url = submission.url,
        late = submission.late,
        excused = submission.excused,
        missing = submission.missing,
        mediaCommentId = mediaCommentId,
        assignmentId = submission.assignmentId,
        userId = if (submission.userId == 0L) null else submission.userId,
        graderId = if (submission.graderId == 0L) null else submission.graderId,
        groupId = groupId,
        pointsDeducted = submission.pointsDeducted,
        enteredScore = submission.enteredScore,
        enteredGrade = submission.enteredGrade,
        postedAt = submission.postedAt,
        gradingPeriodId = submission.gradingPeriodId
    )

    fun toApiModel(
        submissionHistory: List<Submission> = emptyList(),
        submissionComments: List<SubmissionComment> = emptyList(),
        attachments: List<Attachment> = emptyList(),
        rubricAssessment: HashMap<String, RubricCriterionAssessment> = hashMapOf(),
        mediaComment: MediaComment? = null,
        assignment: Assignment? = null,
        user: User? = null,
        group: Group? = null
    ) = Submission(
        id = id,
        grade = grade,
        score = score,
        attempt = attempt,
        submittedAt = submittedAt,
        submissionComments = submissionComments,
        commentCreated = commentCreated,
        mediaContentType = mediaContentType,
        mediaCommentUrl = mediaCommentUrl,
        mediaCommentDisplay = mediaCommentDisplay,
        submissionHistory = submissionHistory,
        attachments = ArrayList(attachments),
        body = body,
        rubricAssessment = rubricAssessment,
        isGradeMatchesCurrentSubmission = isGradeMatchesCurrentSubmission,
        workflowState = workflowState,
        submissionType = submissionType,
        previewUrl = previewUrl,
        url = url,
        late = late,
        excused = excused,
        missing = missing,
        mediaComment = mediaComment,
        assignmentId = assignmentId,
        assignment = assignment,
        userId = userId.orDefault(),
        graderId = graderId.orDefault(),
        user = user,
        //TODO
        discussionEntries = arrayListOf(),
        group = group,
        pointsDeducted = pointsDeducted,
        enteredScore = enteredScore,
        enteredGrade = enteredGrade,
        postedAt = postedAt,
        gradingPeriodId = gradingPeriodId
    )
}