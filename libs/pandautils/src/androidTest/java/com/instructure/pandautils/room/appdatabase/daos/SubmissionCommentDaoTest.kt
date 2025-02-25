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
package com.instructure.pandautils.room.appdatabase.daos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.room.appdatabase.AppDatabase
import com.instructure.pandautils.room.appdatabase.entities.AttachmentEntity
import com.instructure.pandautils.room.appdatabase.entities.AuthorEntity
import com.instructure.pandautils.room.appdatabase.entities.MediaCommentEntity
import com.instructure.pandautils.room.appdatabase.entities.SubmissionCommentEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SubmissionCommentDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var submissionCommentDao: SubmissionCommentDao

    private lateinit var attachmentDao: AttachmentDao
    private lateinit var mediaCommentDao: MediaCommentDao
    private lateinit var authorDao: AuthorDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        submissionCommentDao = db.submissionCommentDao()

        attachmentDao = db.attachmentDao()
        mediaCommentDao = db.mediaCommentDao()
        authorDao = db.authorDao()
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
            mediaCommentId = "66"
        )

        val submissionComment2 = SubmissionCommentEntity(
            id = 2,
            comment = "These are not the droids you are looking for",
        )

        submissionCommentDao.insert(submissionComment)
        submissionCommentDao.insert(submissionComment2)
        authorDao.insert(AuthorEntity(id = 1, displayName = "Obi-Wan"))
        attachmentDao.insert(AttachmentEntity(id = 5, submissionCommentId = 1, filename = "droids.mp4"))
        mediaCommentDao.insert(MediaCommentEntity(mediaId = "66", displayName = "Order 66"))

        // Verify correct query
        val result = submissionCommentDao.findById(id)

        Assert.assertEquals(submissionComment, result!!.submissionComment)
        Assert.assertEquals(1, result.attachments!!.size)
        Assert.assertEquals("droids.mp4", result.attachments!!.first().filename)
        Assert.assertEquals("Obi-Wan", result.author!!.displayName)
        Assert.assertEquals("Order 66", result.mediaComment!!.displayName)
    }
}