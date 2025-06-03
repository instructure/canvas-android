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
package com.instructure.pandautils.room.studentdb

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.room.studentdb.entities.CreateFileSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.daos.CreateFileSubmissionDao
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateFileSubmissionDaoTest {

    private lateinit var db: StudentDb
    private lateinit var dao: CreateFileSubmissionDao
    private lateinit var submissionDao: CreateSubmissionDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            StudentDb::class.java
        ).build()
        dao = db.fileSubmissionDao()
        submissionDao = db.submissionDao()
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testSubmissionForeignKey() = runTest {
        val fileSubmission = CreateFileSubmissionEntity(
            id = 1L,
            dbSubmissionId = 1L
        )

        dao.insert(fileSubmission)
    }

    @Test
    fun testFindFilesForSubmissionId() = runTest {
        val files = listOf(
            CreateFileSubmissionEntity(
                id = 1L,
                dbSubmissionId = 1L
            ),
            CreateFileSubmissionEntity(
                id = 2L,
                dbSubmissionId = 1L
            ),
            CreateFileSubmissionEntity(
                id = 3L,
                dbSubmissionId = 2L
            )
        )
        addSubmission(1L)
        addSubmission(2L)

        files.forEach { dao.insert(it) }

        val result = dao.findFilesForSubmissionId(1L)

        assertEquals(files.subList(0, 2), result)
    }

    @Test
    fun testFindFileForSubmissionId() = runTest {
        val files = listOf(
            CreateFileSubmissionEntity(
                id = 1L,
                dbSubmissionId = 1L
            ),
            CreateFileSubmissionEntity(
                id = 2L,
                dbSubmissionId = 1L
            ),
            CreateFileSubmissionEntity(
                id = 3L,
                dbSubmissionId = 2L
            )
        )
        addSubmission(1L)
        addSubmission(2L)

        files.forEach { dao.insert(it) }

        val result = dao.findFileForSubmissionId(1L)

        assertEquals(files[0], result)
    }

    @Test
    fun testFindFilesForPath() = runTest {
        val files = listOf(
            CreateFileSubmissionEntity(
                id = 1L,
                dbSubmissionId = 1L,
                fullPath = "/path/to/file1"
            ),
            CreateFileSubmissionEntity(
                id = 2L,
                dbSubmissionId = 1L,
                fullPath = "/path/to/file1"
            ),
            CreateFileSubmissionEntity(
                id = 3L,
                dbSubmissionId = 2L,
                fullPath = "/path/to/file1"
            )
        )

        addSubmission(1L)
        addSubmission(2L)

        files.forEach { dao.insert(it) }

        val result = dao.findFilesForPath(3L, "/path/to/file1")

        assertEquals(files.subList(0, 2), result)
    }

    @Test
    fun testDeleteFilesForSubmissionId() = runTest {
        val files = listOf(
            CreateFileSubmissionEntity(
                id = 1L,
                dbSubmissionId = 1L
            ),
            CreateFileSubmissionEntity(
                id = 2L,
                dbSubmissionId = 1L
            ),
            CreateFileSubmissionEntity(
                id = 3L,
                dbSubmissionId = 2L
            )
        )

        addSubmission(1L)
        addSubmission(2L)

        files.forEach { dao.insert(it) }

        dao.deleteFilesForSubmissionId(1L)

        val result = dao.findFileForSubmissionId(1L)

        assertNull(result)
    }

    @Test
    fun testSetFileAttachmentIdAndError() = runTest {
        val file = CreateFileSubmissionEntity(
            id = 1L,
            dbSubmissionId = 1L
        )

        addSubmission(1L)

        dao.insert(file)

        dao.setFileAttachmentIdAndError(1L,true, "error message", 1L)

        val result = dao.findFileForSubmissionId(1L)

        assertEquals(1L, result?.attachmentId)
        assertEquals(true, result?.errorFlag)
        assertEquals("error message", result?.error)
    }

    @Test
    fun setFileError() = runTest {
        val file = CreateFileSubmissionEntity(
            id = 1L,
            dbSubmissionId = 1L
        )

        addSubmission(1L)

        dao.insert(file)

        dao.setFileError(true, "error message", 1L)

        val result = dao.findFileForSubmissionId(1L)

        assertEquals(true, result?.errorFlag)
        assertEquals("error message", result?.error)
    }

    @Test
    fun testDeleteFileById() = runTest {
        val files = listOf(
            CreateFileSubmissionEntity(
                id = 1L,
                dbSubmissionId = 1L
            ),
            CreateFileSubmissionEntity(
                id = 2L,
                dbSubmissionId = 2L
            )
        )

        addSubmission(1L)
        addSubmission(2L)

        files.forEach { dao.insert(it) }

        dao.deleteFileById(1L)

        val result = dao.findFileForSubmissionId(1L)

        assertNull(result)
    }

    private suspend fun addSubmission(id: Long) {
        submissionDao.insert(
            CreateSubmissionEntity(
                id = id,
                submissionType = "online_text_entry",
                userId = 1L,
                canvasContext = CanvasContext.defaultCanvasContext(),
                assignmentId = 1L
            )
        )
    }
}