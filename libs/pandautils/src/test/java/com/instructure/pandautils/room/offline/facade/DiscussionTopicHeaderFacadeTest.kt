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

import androidx.room.withTransaction
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Checkpoint
import com.instructure.canvasapi2.models.DiscussionParticipant
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.DiscussionTopicPermission
import com.instructure.canvasapi2.models.RemoteFile
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.daos.AssignmentDao
import com.instructure.pandautils.room.offline.daos.CheckpointDao
import com.instructure.pandautils.room.offline.daos.DiscussionParticipantDao
import com.instructure.pandautils.room.offline.daos.DiscussionTopicHeaderDao
import com.instructure.pandautils.room.offline.daos.DiscussionTopicPermissionDao
import com.instructure.pandautils.room.offline.daos.DiscussionTopicRemoteFileDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.room.offline.daos.RemoteFileDao
import com.instructure.pandautils.room.offline.entities.AssignmentEntity
import com.instructure.pandautils.room.offline.entities.CheckpointEntity
import com.instructure.pandautils.room.offline.entities.DiscussionParticipantEntity
import com.instructure.pandautils.room.offline.entities.DiscussionTopicHeaderEntity
import com.instructure.pandautils.room.offline.entities.DiscussionTopicPermissionEntity
import com.instructure.pandautils.room.offline.entities.DiscussionTopicRemoteFileEntity
import com.instructure.pandautils.room.offline.entities.LocalFileEntity
import com.instructure.pandautils.room.offline.entities.RemoteFileEntity
import com.instructure.pandautils.utils.orDefault
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.Date

class DiscussionTopicHeaderFacadeTest {

    private val discussionTopicHeaderDao: DiscussionTopicHeaderDao = mockk(relaxed = true)
    private val discussionParticipantDao: DiscussionParticipantDao = mockk(relaxed = true)
    private val discussionTopicPermissionDao: DiscussionTopicPermissionDao = mockk(relaxed = true)
    private val remoteFileDao: RemoteFileDao = mockk(relaxed = true)
    private val localFileDao: LocalFileDao = mockk(relaxed = true)
    private val discussionTopicRemoteFileDao: DiscussionTopicRemoteFileDao = mockk(relaxed = true)
    private val offlineDatabase: OfflineDatabase = mockk(relaxed = true)
    private val assignmentDao: AssignmentDao = mockk(relaxed = true)
    private val checkpointDao: CheckpointDao = mockk(relaxed = true)

    private val facade = DiscussionTopicHeaderFacade(
        discussionTopicHeaderDao,
        discussionParticipantDao,
        discussionTopicPermissionDao,
        remoteFileDao,
        localFileDao,
        discussionTopicRemoteFileDao,
        offlineDatabase,
        assignmentDao,
        checkpointDao
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        mockkStatic(
            "androidx.room.RoomDatabaseKt"
        )

        val transactionLambda = slot<suspend () -> Unit>()
        coEvery { offlineDatabase.withTransaction(capture(transactionLambda)) } coAnswers {
            transactionLambda.captured.invoke()
        }

        coEvery { assignmentDao.findById(0) } returns null
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Calling insertDiscussion should insert discussion topic header and related entities`() = runTest {
        val discussionParticipant = DiscussionParticipant(id = 1L)
        val discussionTopicPermission = DiscussionTopicPermission()
        val discussionTopicHeader = DiscussionTopicHeader(author = discussionParticipant, permissions = discussionTopicPermission, attachments = mutableListOf(RemoteFile(1L)))

        coEvery { discussionParticipantDao.insert(any()) } returns 1L
        coEvery { discussionTopicHeaderDao.insert(any()) } returns 1L
        coEvery { discussionTopicPermissionDao.insert(any()) } returns 1L

        facade.insertDiscussion(discussionTopicHeader, 1)

        coVerify { discussionParticipantDao.insert(DiscussionParticipantEntity(discussionParticipant)) }
        coVerify { discussionTopicHeaderDao.insert(DiscussionTopicHeaderEntity(discussionTopicHeader, 1L, null)) }
        coVerify { discussionTopicPermissionDao.insert(DiscussionTopicPermissionEntity(discussionTopicPermission.copy(), 1L)) }
        coVerify { discussionTopicHeaderDao.update(DiscussionTopicHeaderEntity(discussionTopicHeader.copy(id = 1), 1L, 1L)) }
        coVerify { remoteFileDao.insertAll(listOf(RemoteFileEntity(discussionTopicHeader.attachments[0]))) }
        coVerify { discussionTopicRemoteFileDao.insertAll(listOf(DiscussionTopicRemoteFileEntity(1, 1)))}
    }

    @Test
    fun `insertDiscussions should insert discussion topic headers and related entities`() = runTest {
        val discussionParticipant = DiscussionParticipant(id = 1L)
        val discussionParticipant2 = DiscussionParticipant(id = 2L)
        val discussionTopicHeader = DiscussionTopicHeader(id = 1, author = discussionParticipant, attachments = mutableListOf(RemoteFile(1L)))
        val discussionTopicHeader2 = DiscussionTopicHeader(id = 2, author = discussionParticipant2, attachments = mutableListOf(RemoteFile(2L)))

        facade.insertDiscussions(listOf(discussionTopicHeader, discussionTopicHeader2), 1, false)

        coVerify {
            discussionParticipantDao.upsertAll(
                listOf(
                    DiscussionParticipantEntity(discussionParticipant),
                    DiscussionParticipantEntity(discussionParticipant2)
                )
            )
        }
        coVerify {
            discussionTopicHeaderDao.insertAll(
                listOf(
                    DiscussionTopicHeaderEntity(discussionTopicHeader, 1),
                    DiscussionTopicHeaderEntity(discussionTopicHeader2, 1)
                )
            )
        }

        coVerify {
            remoteFileDao.insertAll(
                listOf(
                    RemoteFileEntity(discussionTopicHeader.attachments[0]),
                    RemoteFileEntity(discussionTopicHeader2.attachments[0])
                )
            )
        }
        coVerify {
            discussionTopicRemoteFileDao.insertAll(
                listOf(
                    DiscussionTopicRemoteFileEntity(1, 1),
                    DiscussionTopicRemoteFileEntity(2, 2)
                )
            )
        }
    }

    @Test
    fun `Calling getDiscussionTopicHeaderById should return the discussion topic header with the specified ID`() = runTest {
        val discussionTopicHeaderId = 1L
        val discussionParticipant = DiscussionParticipant(id = 1L, displayName = "displayName")
        val discussionPermission = DiscussionTopicPermission()
        val discussionTopicHeader = DiscussionTopicHeader(id = discussionTopicHeaderId, author = discussionParticipant, permissions = discussionPermission, title = "Title", attachments = mutableListOf(RemoteFile(1L, url = "path")))

        coEvery { discussionParticipantDao.findById(any()) } returns DiscussionParticipantEntity(discussionParticipant)
        coEvery { discussionTopicHeaderDao.findById(any()) } returns DiscussionTopicHeaderEntity(discussionTopicHeader, 1)
        coEvery { discussionTopicPermissionDao.findByDiscussionTopicHeaderId(any()) } returns DiscussionTopicPermissionEntity(discussionPermission, discussionTopicHeaderId)
        coEvery { remoteFileDao.findById(any()) } returns RemoteFileEntity(discussionTopicHeader.attachments[0])
        coEvery { discussionTopicRemoteFileDao.findByDiscussionId(any()) } returns listOf(DiscussionTopicRemoteFileEntity(discussionTopicHeaderId, 1))
        coEvery { localFileDao.findById(any()) } returns LocalFileEntity(id = 1, path = "path", courseId = 1L, createdDate = Date())

        val result = facade.getDiscussionTopicHeaderById(discussionTopicHeaderId)!!

        Assert.assertEquals(discussionParticipant, result.author)
        Assert.assertEquals(discussionTopicHeader, result)
    }

    @Test
    fun `getDiscussionsForCourse should return the discussion topic headers for that course`() = runTest {
        val discussionParticipant = DiscussionParticipant(id = 1L, displayName = "displayName")
        val discussionTopicPermission = DiscussionTopicPermission()
        val discussionTopicHeader = DiscussionTopicHeader(id = 1, author = discussionParticipant, permissions = discussionTopicPermission, title = "Title")
        val discussionTopicHeader2 = DiscussionTopicHeader(id = 2, author = discussionParticipant, permissions = discussionTopicPermission, title = "Title")

        coEvery { discussionParticipantDao.findById(any()) } returns DiscussionParticipantEntity(discussionParticipant)
        coEvery { discussionTopicPermissionDao.findByDiscussionTopicHeaderId(any()) } returns DiscussionTopicPermissionEntity(discussionTopicPermission, 1L)
        coEvery { discussionTopicHeaderDao.findAllDiscussionsForCourse(any()) } returns listOf(
            DiscussionTopicHeaderEntity(discussionTopicHeader, 1), DiscussionTopicHeaderEntity(discussionTopicHeader2, 1)
        )

        val result = facade.getDiscussionsForCourse(1)

        Assert.assertEquals(2, result.size)
        Assert.assertEquals(discussionParticipant, result[0].author)
        Assert.assertEquals(discussionParticipant, result[1].author)
        Assert.assertEquals(discussionTopicHeader, result[0])
        Assert.assertEquals(discussionTopicHeader2, result[1])
    }

    @Test
    fun `getDiscussionTopicHeaderById should return discussion with assignment when assignmentId is set`() = runTest {
        val discussionTopicHeaderId = 1L
        val assignmentId = 10L
        val discussionParticipant = DiscussionParticipant(id = 1L, displayName = "displayName")
        val discussionPermission = DiscussionTopicPermission()
        val assignment = Assignment(id = assignmentId, name = "Test Assignment", pointsPossible = 15.0)
        val discussionTopicHeader = DiscussionTopicHeader(
            id = discussionTopicHeaderId,
            author = discussionParticipant,
            permissions = discussionPermission,
            title = "Title",
            assignmentId = assignmentId,
            assignment = assignment
        )

        coEvery { discussionParticipantDao.findById(1L) } returns DiscussionParticipantEntity(discussionParticipant)
        coEvery { discussionTopicHeaderDao.findById(discussionTopicHeaderId) } returns DiscussionTopicHeaderEntity(
            discussionTopicHeader,
            1
        )
        coEvery { discussionTopicPermissionDao.findByDiscussionTopicHeaderId(discussionTopicHeaderId) } returns DiscussionTopicPermissionEntity(
            discussionPermission,
            discussionTopicHeaderId
        )
        coEvery { discussionTopicRemoteFileDao.findByDiscussionId(discussionTopicHeaderId) } returns emptyList()
        coEvery { assignmentDao.findById(assignmentId) } returns AssignmentEntity(assignment, null, null, 1L, null)
        coEvery { checkpointDao.findByAssignmentId(assignmentId) } returns emptyList()

        val result = facade.getDiscussionTopicHeaderById(discussionTopicHeaderId)!!

        Assert.assertEquals(discussionParticipant, result.author)
        Assert.assertNotNull(result.assignment)
        Assert.assertEquals(assignmentId, result.assignment?.id)
        Assert.assertEquals(15.0, result.assignment?.pointsPossible.orDefault(), 0.01)

        coVerify { assignmentDao.findById(assignmentId) }
        coVerify { checkpointDao.findByAssignmentId(assignmentId) }
    }

    @Test
    fun `getDiscussionTopicHeaderById should return discussion with assignment and checkpoints`() = runTest {
        val discussionTopicHeaderId = 1L
        val assignmentId = 10L
        val discussionParticipant = DiscussionParticipant(id = 1L, displayName = "displayName")
        val discussionPermission = DiscussionTopicPermission()
        val checkpoint1 = Checkpoint(
            tag = "reply_to_topic",
            name = "Reply to Topic",
            pointsPossible = 5.0,
            dueAt = "2023-01-29T23:59:00Z"
        )
        val checkpoint2 = Checkpoint(
            tag = "reply_to_entry",
            name = "Reply to Entry",
            pointsPossible = 5.0,
            dueAt = "2023-01-31T23:59:00Z"
        )
        val assignment = Assignment(
            id = assignmentId,
            name = "Test Assignment",
            pointsPossible = 10.0,
            checkpoints = listOf(checkpoint1, checkpoint2)
        )
        val discussionTopicHeader = DiscussionTopicHeader(
            id = discussionTopicHeaderId,
            author = discussionParticipant,
            permissions = discussionPermission,
            title = "Title",
            assignmentId = assignmentId,
            assignment = assignment
        )

        coEvery { discussionParticipantDao.findById(1L) } returns DiscussionParticipantEntity(discussionParticipant)
        coEvery { discussionTopicHeaderDao.findById(discussionTopicHeaderId) } returns DiscussionTopicHeaderEntity(
            discussionTopicHeader,
            1
        )
        coEvery { discussionTopicPermissionDao.findByDiscussionTopicHeaderId(discussionTopicHeaderId) } returns DiscussionTopicPermissionEntity(
            discussionPermission,
            discussionTopicHeaderId
        )
        coEvery { discussionTopicRemoteFileDao.findByDiscussionId(discussionTopicHeaderId) } returns emptyList()
        coEvery { assignmentDao.findById(assignmentId) } returns AssignmentEntity(assignment, null, null, 1L, null)
        coEvery { checkpointDao.findByAssignmentId(assignmentId) } returns listOf(
            CheckpointEntity(checkpoint1, assignmentId),
            CheckpointEntity(checkpoint2, assignmentId)
        )

        val result = facade.getDiscussionTopicHeaderById(discussionTopicHeaderId)!!

        Assert.assertEquals(discussionParticipant, result.author)
        Assert.assertNotNull(result.assignment)
        Assert.assertEquals(assignmentId, result.assignment?.id)
        Assert.assertEquals(10.0, result.assignment?.pointsPossible.orDefault(), 0.01)
        Assert.assertEquals(2, result.assignment?.checkpoints?.size)
        Assert.assertEquals("Reply to Topic", result.assignment?.checkpoints?.get(0)?.name)
        Assert.assertEquals("Reply to Entry", result.assignment?.checkpoints?.get(1)?.name)

        coVerify { assignmentDao.findById(assignmentId) }
        coVerify { checkpointDao.findByAssignmentId(assignmentId) }
    }

    @Test
    fun `getDiscussionTopicHeaderById should return discussion without assignment when assignmentId is 0`() = runTest {
        val discussionTopicHeaderId = 1L
        val discussionParticipant = DiscussionParticipant(id = 1L, displayName = "displayName")
        val discussionPermission = DiscussionTopicPermission()
        val discussionTopicHeader = DiscussionTopicHeader(
            id = discussionTopicHeaderId,
            author = discussionParticipant,
            permissions = discussionPermission,
            title = "Title",
            assignmentId = 0
        )

        coEvery { discussionParticipantDao.findById(1L) } returns DiscussionParticipantEntity(discussionParticipant)
        coEvery { discussionTopicHeaderDao.findById(discussionTopicHeaderId) } returns DiscussionTopicHeaderEntity(
            discussionTopicHeader,
            1
        )
        coEvery { discussionTopicPermissionDao.findByDiscussionTopicHeaderId(discussionTopicHeaderId) } returns DiscussionTopicPermissionEntity(
            discussionPermission,
            discussionTopicHeaderId
        )
        coEvery { discussionTopicRemoteFileDao.findByDiscussionId(discussionTopicHeaderId) } returns emptyList()

        val result = facade.getDiscussionTopicHeaderById(discussionTopicHeaderId)!!

        Assert.assertEquals(discussionParticipant, result.author)
        Assert.assertNull(result.assignment)
    }
}