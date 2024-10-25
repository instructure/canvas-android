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
class SubmissionCommentDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var submissionCommentDao: SubmissionCommentDao

    private lateinit var attachmentDao: AttachmentDao
    private lateinit var mediaCommentDao: MediaCommentDao
    private lateinit var authorDao: AuthorDao
    private lateinit var courseDao: CourseDao
    private lateinit var assignmentGroupDao: AssignmentGroupDao
    private lateinit var assignmentDao: AssignmentDao
    private lateinit var submissionDao: SubmissionDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        submissionCommentDao = db.submissionCommentDao()

        attachmentDao = db.attachmentDao()
        mediaCommentDao = db.mediaCommentDao()
        authorDao = db.authorDao()
        courseDao = db.courseDao()
        assignmentGroupDao = db.assignmentGroupDao()
        assignmentDao = db.assignmentDao()
        submissionDao = db.submissionDao()

        runBlocking {
            courseDao.insert(CourseEntity(Course(1L)))
            assignmentGroupDao.insert(AssignmentGroupEntity(AssignmentGroup(1L), 1L))
            assignmentDao.insert(AssignmentEntity(Assignment(1L, assignmentGroupId = 1L), null, null, null, null))
            submissionDao.insert(SubmissionEntity(Submission(1L, attempt = 1L, assignmentId = 1L), null, null))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getSubmissionCommentWithAttachmentsById() = runTest {
        // Setup all DAOs with the data needed for the query
        val id = 1L
        val submissionComment = SubmissionCommentEntity(
            id = id,
            comment = "These are the droids you are looking for",
            authorId = 1,
            mediaCommentId = "66",
            submissionId = 1L,
            attemptId = 1L
        )

        val submissionComment2 = SubmissionCommentEntity(
            id = 2,
            comment = "These are not the droids you are looking for",
            submissionId = 1L,
            attemptId = 1L
        )

        submissionCommentDao.insert(submissionComment)
        submissionCommentDao.insert(submissionComment2)
        authorDao.insert(AuthorEntity(id = 1, displayName = "Obi-Wan"))
        attachmentDao.insert(AttachmentEntity(id = 5, submissionCommentId = 1, filename = "droids.mp4"))
        mediaCommentDao.insert(MediaCommentEntity(MediaComment(mediaId = "66", displayName = "Order 66"), 1, 1))

        // Verify correct query
        val result = submissionCommentDao.findById(id)

        Assert.assertEquals(submissionComment, result!!.submissionComment)
        Assert.assertEquals(1, result.attachments!!.size)
        Assert.assertEquals("droids.mp4", result.attachments!!.first().filename)
        Assert.assertEquals("Obi-Wan", result.author!!.displayName)
        Assert.assertEquals("Order 66", result.mediaComment!!.displayName)
    }

    @Test
    fun testFindBySubmissionId() = runTest {
        val submissionComment = SubmissionCommentEntity(
            id = 1,
            comment = "These are the droids you are looking for",
            authorId = 1,
            mediaCommentId = "66",
            submissionId = 1
        )
        val submissionComment2 = SubmissionCommentEntity(
            id = 2,
            comment = "These are not the droids you are looking for",
            submissionId = 2
        )
        submissionCommentDao.insertAll(listOf(submissionComment, submissionComment2))

        val result = submissionCommentDao.findBySubmissionId(1)

        Assert.assertEquals(1, result.size)
        Assert.assertEquals(submissionComment, result.first().submissionComment)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testSubmissionForeignKey() = runTest {
        val submissionComment = SubmissionCommentEntity(id = 2, comment = "Comment", submissionId = 2, attemptId = 1)

        submissionCommentDao.insert(submissionComment)
    }

    @Test
    fun testSubmissionCascade() = runTest {
        val submissionComment = SubmissionCommentEntity(id = 2, comment = "Comment", submissionId = 1, attemptId = 1)

        submissionCommentDao.insert(submissionComment)

        submissionDao.delete(SubmissionEntity(Submission(1L, attempt = 1L, assignmentId = 1L), null, null))

        val result = submissionCommentDao.findBySubmissionId(1)

        Assert.assertTrue(result.isEmpty())
    }
}