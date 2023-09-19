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

package com.instructure.pandautils.room.offline.facade

import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.room.offline.daos.*
import com.instructure.pandautils.room.offline.entities.*

class SubmissionFacade(
    private val submissionDao: SubmissionDao,
    private val groupDao: GroupDao,
    private val mediaCommentDao: MediaCommentDao,
    private val userDao: UserDao,
    private val userApi: UserAPI.UsersInterface,
    private val submissionCommentDao: SubmissionCommentDao,
    private val attachmentDao: AttachmentDao,
    private val authorDao: AuthorDao,
    private val rubricCriterionAssessmentDao: RubricCriterionAssessmentDao
) {
    private val fetchedUsers = mutableMapOf<Long, User?>()

    suspend fun insertSubmission(submission: Submission) {
        val groupId = submission.group?.let { group -> groupDao.insert(GroupEntity(group)) }

        submissionDao.insertOrUpdate(SubmissionEntity(submission, groupId, submission.mediaComment?.mediaId))

        submission.mediaComment?.let { mediaComment ->
            mediaCommentDao.insert(MediaCommentEntity(mediaComment, submission.id, submission.attempt))
        }

        if (submission.userId != 0L) {
            val user = submission.user
                ?: fetchedUsers[submission.userId]
                ?: userApi.getUser(
                    submission.userId,
                    RestParams(isForceReadFromNetwork = true)
                ).dataOrNull

            fetchedUsers[submission.userId] = user
            if (user != null) {
                userDao.insertOrUpdate(UserEntity(user))
            }
        }

        if (submission.graderId != 0L) {
            val grader = fetchedUsers[submission.graderId]
                ?: userApi.getUser(
                    submission.graderId,
                    RestParams(isForceReadFromNetwork = true)
                ).dataOrNull

            if (grader != null) {
                fetchedUsers[grader.id] = grader
                userDao.insertOrUpdate(UserEntity(grader))
            }
        }

        submission.submissionComments.forEach { submissionComment ->
            submissionCommentDao.insert(SubmissionCommentEntity(submissionComment, submission.id, submission.attempt))

            submissionComment.mediaComment?.let {
                mediaCommentDao.insert(MediaCommentEntity(it, submission.id, submission.attempt))
            }

            submissionComment.attachments.map {
                attachmentDao.insert(AttachmentEntity(it, submissionId = submission.id, submissionCommentId = submissionComment.id))
            }

            submissionComment.author?.let {
                authorDao.insert(AuthorEntity(it))
            }
        }

        attachmentDao.insertAll(submission.attachments.map {
            AttachmentEntity(it, submissionId = submission.id, attempt = submission.attempt)
        })

        rubricCriterionAssessmentDao.insertAll(submission.rubricAssessment.map {
            RubricCriterionAssessmentEntity(it.value, it.key, submission.assignmentId)
        })

        submission.submissionHistory.forEach { submissionHistoryItem ->
            submissionHistoryItem?.let { insertSubmission(it) }
        }
    }

    suspend fun getSubmissionById(id: Long): Submission? {
        val submissionHistoryEntities = submissionDao.findById(id)
        return submissionHistoryEntities.lastOrNull()?.let { submissionEntity ->
            createApiModelFromEntity(submissionEntity).copy(submissionHistory = submissionHistoryEntities.map {
                createApiModelFromEntity(it)
            })
        }
    }

    private suspend fun createApiModelFromEntity(submissionEntity: SubmissionEntity): Submission {
        val mediaCommentEntity = mediaCommentDao.findById(submissionEntity.mediaCommentId)
        val userEntity = submissionEntity.userId?.let { userDao.findById(it) }
        val groupEntity = submissionEntity.groupId?.let { groupDao.findById(it) }
        val submissionCommentEntities = submissionCommentDao.findBySubmissionId(submissionEntity.id)
        val attachmentEntities = attachmentDao.findBySubmissionId(submissionEntity.id)
        val rubricCriterionAssessmentEntities = rubricCriterionAssessmentDao.findByAssignmentId(submissionEntity.assignmentId)

        return submissionEntity.toApiModel(
            mediaComment = mediaCommentEntity?.toApiModel(),
            user = userEntity?.toApiModel(),
            group = groupEntity?.toApiModel(),
            submissionComments = submissionCommentEntities.map { it.toApiModel() },
            attachments = attachmentEntities.filter { it.attempt == submissionEntity.attempt }.map { it.toApiModel() },
            rubricAssessment = HashMap(rubricCriterionAssessmentEntities.associateBy({ it.id }, { it.toApiModel() }))
        )
    }

    suspend fun findByAssignmentIds(assignmentIds: List<Long>): List<Submission> {
        val submissionsByAssignmentIds = submissionDao.findByAssignmentIds(assignmentIds)
        return submissionsByAssignmentIds.mapNotNull { getSubmissionById(it.id) }
    }

    suspend fun findByAssignmentId(assignmentId: Long): Submission? {
        return submissionDao.findByAssignmentId(assignmentId)?.let {
            getSubmissionById(it.id)
        }
    }
}