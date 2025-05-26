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

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.room.studentdb.entities.CreatePendingSubmissionCommentEntity
import com.instructure.pandautils.room.studentdb.entities.daos.CreatePendingSubmissionCommentDao
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class CreatePendingSubmissionCommentDaoTest {

    private lateinit var db: StudentDb
    private lateinit var dao: CreatePendingSubmissionCommentDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            StudentDb::class.java
        ).build()
        dao = db.pendingSubmissionCommentDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun testFindAll() = runTest {
        val comments = listOf(
            CreatePendingSubmissionCommentEntity(
                id = 1L,
                accountDomain = "test",
                canvasContext = CanvasContext.defaultCanvasContext(),
                assignmentName = "test",
                assignmentId = 1L,
                lastActivityDate = Date(),
                isGroupMessage = false
            ),
            CreatePendingSubmissionCommentEntity(
                id = 2L,
                accountDomain = "test",
                canvasContext = CanvasContext.defaultCanvasContext(),
                assignmentName = "test",
                assignmentId = 2L,
                lastActivityDate = Date(),
                isGroupMessage = false
            )
        )

        comments.forEach { dao.insert(it) }

        val result = dao.findAll()
        assertEquals(comments, result)
    }

    @Test
    fun testFindCommentById() = runTest {
        val comments = listOf(
            CreatePendingSubmissionCommentEntity(
                id = 1L,
                accountDomain = "test",
                canvasContext = CanvasContext.defaultCanvasContext(),
                assignmentName = "test",
                assignmentId = 1L,
                lastActivityDate = Date(),
                isGroupMessage = false
            ),
            CreatePendingSubmissionCommentEntity(
                id = 2L,
                accountDomain = "test",
                canvasContext = CanvasContext.defaultCanvasContext(),
                assignmentName = "test",
                assignmentId = 2L,
                lastActivityDate = Date(),
                isGroupMessage = false
            )
        )

        comments.forEach { dao.insert(it) }

        val result = dao.findCommentById(1L)

        assertEquals(comments[0], result)
    }

    @Test
    fun testSetCommentError() = runTest {
        val comment = CreatePendingSubmissionCommentEntity(
            id = 1L,
            accountDomain = "test",
            canvasContext = CanvasContext.defaultCanvasContext(),
            assignmentName = "test",
            assignmentId = 1L,
            lastActivityDate = Date(),
            isGroupMessage = false
        )

        dao.insert(comment)

        dao.setCommentError(true, 1L)

        val result = dao.findCommentById(1L)

        assertEquals(true, result?.errorFlag)
    }

    @Test
    fun testUpdateCommentProgress() = runTest {
        val comment = CreatePendingSubmissionCommentEntity(
            id = 1L,
            accountDomain = "test",
            canvasContext = CanvasContext.defaultCanvasContext(),
            assignmentName = "test",
            assignmentId = 1L,
            lastActivityDate = Date(),
            isGroupMessage = false,
        )

        dao.insert(comment)

        dao.updateCommentProgress(1L, 4L, 5L, 0.8)

        val result = dao.findCommentById(1L)

        assertEquals(0.8f, result?.progress)
        assertEquals(4, result?.currentFile)
        assertEquals(5, result?.fileCount)
    }

    @Test
    fun testFindIdByRowId() = runTest {
        val comment = CreatePendingSubmissionCommentEntity(
            id = 1L,
            accountDomain = "test",
            canvasContext = CanvasContext.defaultCanvasContext(),
            assignmentName = "test",
            assignmentId = 1L,
            lastActivityDate = Date(),
            isGroupMessage = false,
        )

        val rowId = dao.insert(comment)

        val result = dao.findIdByRowId(rowId)

        assertEquals(1L, result)
    }

    @Test
    fun testFindCommentsByAccountAndAssignmentId() = runTest {
        val comments = listOf(
            CreatePendingSubmissionCommentEntity(
                id = 1L,
                accountDomain = "test",
                canvasContext = CanvasContext.defaultCanvasContext(),
                assignmentName = "test",
                assignmentId = 1L,
                lastActivityDate = Date(),
                isGroupMessage = false
            ),
            CreatePendingSubmissionCommentEntity(
                id = 2L,
                accountDomain = "test",
                canvasContext = CanvasContext.defaultCanvasContext(),
                assignmentName = "test",
                assignmentId = 2L,
                lastActivityDate = Date(),
                isGroupMessage = false
            )
        )

        comments.forEach { dao.insert(it) }

        val result = dao.findCommentsByAccountAndAssignmentId("test", 1L)

        assertEquals(1, result.size)
        assertEquals(comments[0], result[0])
    }
}