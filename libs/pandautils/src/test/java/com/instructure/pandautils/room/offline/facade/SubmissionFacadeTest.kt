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

import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.MediaComment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.room.offline.daos.AttachmentDao
import com.instructure.pandautils.room.offline.daos.AuthorDao
import com.instructure.pandautils.room.offline.daos.GroupDao
import com.instructure.pandautils.room.offline.daos.MediaCommentDao
import com.instructure.pandautils.room.offline.daos.RubricCriterionAssessmentDao
import com.instructure.pandautils.room.offline.daos.SubAssignmentSubmissionDao
import com.instructure.pandautils.room.offline.daos.SubmissionCommentDao
import com.instructure.pandautils.room.offline.daos.SubmissionDao
import com.instructure.pandautils.room.offline.daos.UserDao
import com.instructure.pandautils.room.offline.entities.GroupEntity
import com.instructure.pandautils.room.offline.entities.MediaCommentEntity
import com.instructure.pandautils.room.offline.entities.SubmissionEntity
import com.instructure.pandautils.room.offline.entities.UserEntity
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class SubmissionFacadeTest {

    private val submissionDao: SubmissionDao = mockk(relaxed = true)
    private val groupDao: GroupDao = mockk(relaxed = true)
    private val mediaCommentDao: MediaCommentDao = mockk(relaxed = true)
    private val userDao: UserDao = mockk(relaxed = true)
    private val submissionCommentDao: SubmissionCommentDao = mockk(relaxed = true)
    private val attachmentDao: AttachmentDao = mockk(relaxed = true)
    private val authorDao: AuthorDao = mockk(relaxed = true)
    private val rubricCriterionAssessmentDao: RubricCriterionAssessmentDao = mockk(relaxed = true)
    private val subAssignmentSubmissionDao: SubAssignmentSubmissionDao = mockk(relaxed = true)

    private val facade = SubmissionFacade(
        submissionDao, groupDao, mediaCommentDao, userDao,
        submissionCommentDao, attachmentDao, authorDao, rubricCriterionAssessmentDao, subAssignmentSubmissionDao
    )

    @Test
    fun `Calling insertSubmission should insert submission and related entities`() = runTest {
        val group = Group(id = 1L)
        val mediaComment = MediaComment(mediaId = "mediaId")
        val user = User(id = 1L)
        val submissionHistory = listOf(Submission(id = 2L), Submission(id = 3L))
        val submission = Submission(
            id = 1L,
            group = group,
            mediaComment = mediaComment,
            user = user,
            userId = user.id,
            graderId = user.id,
            submissionHistory = submissionHistory
        )

        coEvery { submissionDao.insert(any()) } returns 1L
        coEvery { subAssignmentSubmissionDao.insertAll(any()) } just Runs

        facade.insertSubmission(submission)

        coVerify { groupDao.insertOrUpdate(GroupEntity(group)) }
        coVerify { mediaCommentDao.insert(MediaCommentEntity(mediaComment, 1L, 0)) }
        coVerify { userDao.insertOrUpdate(UserEntity(user)) }
        coVerify { submissionDao.insertOrUpdate(SubmissionEntity(submission, group.id, mediaComment.mediaId)) }
        coVerify { subAssignmentSubmissionDao.insertAll(emptyList()) }
    }

    @Test
    fun `Calling getSubmissionById should return the submission with the specified ID`() = runTest {
        val submissionId = 1L
        val group = Group(id = 1, name = "name")
        val mediaComment = MediaComment(mediaId = "mediaId")
        val user = User(id = 1L)
        val submission = Submission(id = submissionId, attempt = 3, group = group, mediaComment = mediaComment, userId = user.id, user = user)
        val submissionHistory = listOf(Submission(id = submissionId, attempt = 1), Submission(id = submissionId, attempt = 2), submission)

        coEvery { groupDao.findById(any()) } returns GroupEntity(group)
        coEvery { mediaCommentDao.findById(any()) } returns MediaCommentEntity(mediaComment, 1L, 0)
        coEvery { userDao.findById(any()) } returns UserEntity(user)
        coEvery { submissionDao.findById(any()) } returns submissionHistory.map { SubmissionEntity(it, group.id, mediaComment.mediaId) }
        coEvery { subAssignmentSubmissionDao.findBySubmissionIdAndAttempt(any(), any()) } returns emptyList()

        val result = facade.getSubmissionById(submissionId)!!

        Assert.assertEquals(submissionId, result.id)
        Assert.assertEquals(group, result.group)
        Assert.assertEquals(mediaComment, result.mediaComment)
        Assert.assertEquals(user, result.user)
        Assert.assertEquals(submissionHistory.size, result.submissionHistory.size)
    }

    @Test
    fun `Calling findByAssignmentIds should return submissions by the specified assignment ID`() = runTest {
        val assignmentId = 1L
        val submissionId = 1L
        val group = Group(id = 1, name = "name")
        val mediaComment = MediaComment(mediaId = "mediaId")
        val user = User(id = 1L)
        val submission = Submission(
            id = submissionId,
            attempt = 3,
            group = group,
            mediaComment = mediaComment,
            userId = user.id,
            user = user,
            assignmentId = assignmentId
        )
        val submissionHistory = listOf(Submission(id = submissionId, attempt = 1), Submission(id = submissionId, attempt = 2), submission)

        coEvery { groupDao.findById(group.id) } returns GroupEntity(group)
        coEvery { mediaCommentDao.findById(mediaComment.mediaId) } returns MediaCommentEntity(mediaComment, 1L, 0)
        coEvery { userDao.findById(user.id) } returns UserEntity(user)
        coEvery { submissionDao.findByAssignmentIds(listOf(assignmentId)) } returns submissionHistory.map {
            SubmissionEntity(
                it,
                group.id,
                mediaComment.mediaId
            )
        }
        coEvery { submissionDao.findById(submissionId) } returns submissionHistory.map { SubmissionEntity(it, group.id, mediaComment.mediaId) }
        coEvery { subAssignmentSubmissionDao.findBySubmissionIdAndAttempt(any(), any()) } returns emptyList()

        val result = facade.findByAssignmentIds(listOf(assignmentId))

        Assert.assertEquals(submissionId, result.first().id)
        Assert.assertEquals(group, result.first().group)
        Assert.assertEquals(mediaComment, result.first().mediaComment)
        Assert.assertEquals(user, result.first().user)
        Assert.assertEquals(submissionHistory.size, result.first().submissionHistory.size)
    }

    @Test
    fun `Calling findByAssignmentId should return submission by the specified assignment ID`() = runTest {
        val assignmentId = 1L
        val submissionId = 1L
        val group = Group(id = 1, name = "name")
        val mediaComment = MediaComment(mediaId = "mediaId")
        val user = User(id = 1L)
        val submission = Submission(
            id = submissionId,
            attempt = 3,
            group = group,
            mediaComment = mediaComment,
            userId = user.id,
            user = user,
            assignmentId = assignmentId
        )
        val submissionHistory = listOf(Submission(id = submissionId, attempt = 1), Submission(id = submissionId, attempt = 2), submission)

        coEvery { groupDao.findById(group.id) } returns GroupEntity(group)
        coEvery { mediaCommentDao.findById(mediaComment.mediaId) } returns MediaCommentEntity(mediaComment, 1L, 0)
        coEvery { userDao.findById(user.id) } returns UserEntity(user)
        coEvery { submissionDao.findByAssignmentId(assignmentId) } returns SubmissionEntity(submission, group.id, mediaComment.mediaId)
        coEvery { submissionDao.findById(submissionId) } returns submissionHistory.map { SubmissionEntity(it, group.id, mediaComment.mediaId) }

        val result = facade.findByAssignmentId(assignmentId)!!

        Assert.assertEquals(submissionId, result.id)
        Assert.assertEquals(group, result.group)
        Assert.assertEquals(mediaComment, result.mediaComment)
        Assert.assertEquals(user, result.user)
        Assert.assertEquals(submissionHistory.size, result.submissionHistory.size)
    }
}