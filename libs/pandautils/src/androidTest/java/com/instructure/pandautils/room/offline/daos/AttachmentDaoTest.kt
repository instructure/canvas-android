/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.pandautils.room.offline.daos

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.*
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class AttachmentDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var courseDao: CourseDao
    private lateinit var assignmentGroupDao: AssignmentGroupDao
    private lateinit var assignmentDao: AssignmentDao
    private lateinit var submissionDao: SubmissionDao
    private lateinit var submissionCommentDao: SubmissionCommentDao
    private lateinit var attachmentDao: AttachmentDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        courseDao = db.courseDao()
        assignmentGroupDao = db.assignmentGroupDao()
        assignmentDao = db.assignmentDao()
        submissionDao = db.submissionDao()
        submissionCommentDao = db.submissionCommentDao()
        attachmentDao = db.attachmentDao()

        runBlocking {
            courseDao.insert(CourseEntity(Course(1L)))
            assignmentGroupDao.insert(AssignmentGroupEntity(AssignmentGroup(1L), 1L))
            assignmentDao.insert(AssignmentEntity(Assignment(1L, assignmentGroupId = 1L), null, null, null, null))
            submissionDao.insert(SubmissionEntity(Submission(1L, attempt = 1L, assignmentId = 1L), null, null))
            submissionCommentDao.insert(SubmissionCommentEntity(SubmissionComment(1L), 1L, 1L))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndFindingByParentId() = runTest {
        val attachmentEntity = AttachmentEntity(
            id = 1, contentType = "image/jpg", filename = "image.jpg", displayName = "File",
            url = "file.com", createdAt = Date(), size = 10000, workerId = "123", submissionCommentId = 1L
        )

        val attachmentEntity2 = attachmentEntity.copy(id = 2, workerId = "124", filename = "image2.jpg")

        attachmentDao.insertAll(listOf(attachmentEntity, attachmentEntity2))
        val result = attachmentDao.findByParentId("123")
        Assert.assertEquals(1, result!!.size)
        Assert.assertEquals(attachmentEntity, result.first())
    }

    @Test
    fun dontReturnAnyItemIfEntitiesAreDeleted() = runTest {
        val attachmentEntity = AttachmentEntity(
            id = 1, contentType = "image/jpg", filename = "image.jpg", displayName = "File",
            url = "file.com", createdAt = Date(), size = 10000, workerId = "123", submissionCommentId = 1L
        )

        val attachmentEntity2 = attachmentEntity.copy(id = 2, workerId = "124", filename = "image2.jpg")

        attachmentDao.insertAll(listOf(attachmentEntity, attachmentEntity2))
        attachmentDao.deleteAll(listOf(attachmentEntity, attachmentEntity2))
        val result = attachmentDao.findByParentId("123")

        Assert.assertEquals(0, result!!.size)
    }

    @Test
    fun testFindBySubmissionId() = runTest {
        val attachmentEntity = AttachmentEntity(
            id = 1, contentType = "image/jpg", filename = "image.jpg", displayName = "File", url = "file.com",
            createdAt = Date(), size = 10000, workerId = "123", submissionCommentId = 1, submissionId = 1
        )
        val attachmentEntity2 = attachmentEntity.copy(id = 2, workerId = "124", filename = "image2.jpg", submissionId = 2)
        attachmentDao.insertAll(listOf(attachmentEntity, attachmentEntity2))

        val result = attachmentDao.findBySubmissionId(1)

        Assert.assertEquals(listOf(attachmentEntity), result)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testSubmissionCommentForeignKey() = runTest {
        val attachmentEntity = AttachmentEntity(
            id = 1, contentType = "image/jpg", filename = "image.jpg", displayName = "File",
            url = "file.com", createdAt = Date(), size = 10000, workerId = "123", submissionCommentId = 2L
        )

        attachmentDao.insert(attachmentEntity)
    }

    @Test
    fun testSubmissionCommentCascade() = runTest {
        val attachmentEntity = AttachmentEntity(
            id = 1, contentType = "image/jpg", filename = "image.jpg", displayName = "File", url = "file.com",
            createdAt = Date(), size = 10000, workerId = "123", submissionCommentId = 1, submissionId = 1L
        )

        attachmentDao.insert(attachmentEntity)

        submissionCommentDao.delete(SubmissionCommentEntity(SubmissionComment(1L), 1L, 1L))

        val result = attachmentDao.findBySubmissionId(1L)

        Assert.assertTrue(result.isEmpty())
    }
}