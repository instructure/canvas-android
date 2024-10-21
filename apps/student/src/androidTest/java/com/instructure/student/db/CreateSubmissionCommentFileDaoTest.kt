/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.student.db

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.student.room.StudentDb
import com.instructure.student.room.entities.CreatePendingSubmissionCommentEntity
import com.instructure.student.room.entities.CreateSubmissionCommentFileEntity
import com.instructure.student.room.entities.daos.CreatePendingSubmissionCommentDao
import com.instructure.student.room.entities.daos.CreateSubmissionCommentFileDao
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class CreateSubmissionCommentFileDaoTest {

    private lateinit var db: StudentDb
    private lateinit var dao: CreateSubmissionCommentFileDao
    private lateinit var commentDao: CreatePendingSubmissionCommentDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            StudentDb::class.java
        ).build()
        dao = db.submissionCommentFileDao()
        commentDao = db.pendingSubmissionCommentDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testSubmissionCommentForeignKey() = runTest {
        val entity = CreateSubmissionCommentFileEntity(
            pendingCommentId = 1,
            attachmentId = 1,
            name = "file",
            size = 100,
            contentType = "text/plain",
            fullPath = "/path/to/file"
        )

        dao.insert(entity)
    }

    @Test
    fun testFindFilesForPendingComment() = runTest {
        val files = listOf(
            CreateSubmissionCommentFileEntity(
                id = 1L,
                pendingCommentId = 1L,
                attachmentId = 1,
                name = "file1",
                size = 100,
                contentType = "text/plain",
                fullPath = "/path/to/file1"
            ),
            CreateSubmissionCommentFileEntity(
                id = 2L,
                pendingCommentId = 1L,
                attachmentId = 2,
                name = "file2",
                size = 200,
                contentType = "text/plain",
                fullPath = "/path/to/file2"
            ),
            CreateSubmissionCommentFileEntity(
                id = 3L,
                pendingCommentId = 2L,
                attachmentId = 3,
                name = "file3",
                size = 300,
                contentType = "text/plain",
                fullPath = "/path/to/file3"
            )
        )

        addPendingComment(1L)
        addPendingComment(2L)

        files.forEach { dao.insert(it) }

        val result = dao.findFilesForPendingComment(1L)

        assertEquals(2, result.size)
        assertEquals(files.subList(0, 2), result)
    }

    @Test
    fun testSetFileAttachmentId() = runTest {
        val file = CreateSubmissionCommentFileEntity(
            id = 1L,
            pendingCommentId = 1L,
            name = "file",
            size = 100,
            contentType = "text/plain",
            fullPath = "/path/to/file"
        )

        addPendingComment(1L)

        dao.insert(file)

        dao.setFileAttachmentId(1L, 1L)

        val result = dao.findFilesForPendingComment(1L)

        assertEquals(1L, result[0].attachmentId)
    }

    @Test
    fun testDeleteFilesForCommentId() = runTest {
        val files = listOf(
            CreateSubmissionCommentFileEntity(
                id = 1L,
                pendingCommentId = 1L,
                attachmentId = 1,
                name = "file1",
                size = 100,
                contentType = "text/plain",
                fullPath = "/path/to/file1"
            ),
            CreateSubmissionCommentFileEntity(
                id = 2L,
                pendingCommentId = 1L,
                attachmentId = 2,
                name = "file2",
                size = 200,
                contentType = "text/plain",
                fullPath = "/path/to/file2"
            ),
            CreateSubmissionCommentFileEntity(
                id = 3L,
                pendingCommentId = 2L,
                attachmentId = 3,
                name = "file3",
                size = 300,
                contentType = "text/plain",
                fullPath = "/path/to/file3"
            )
        )

        addPendingComment(1L)
        addPendingComment(2L)

        files.forEach { dao.insert(it) }

        dao.deleteFilesForCommentId(1L)

        val result = dao.findFilesForPendingComment(1L)

        assertEquals(0, result.size)
    }

    private suspend fun addPendingComment(id: Long) {
        commentDao.insert(
            CreatePendingSubmissionCommentEntity(
                id = id,
                accountDomain = "domain",
                canvasContext = CanvasContext.defaultCanvasContext(),
                assignmentName = "assignment",
                assignmentId = 1,
                lastActivityDate = Date(),
                isGroupMessage = false
            )
        )
    }
}