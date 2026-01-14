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

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateSubmissionDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: StudentDb
    private lateinit var dao: CreateSubmissionDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, StudentDb::class.java).build()
        dao = db.submissionDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testInsertDuplicate() = runTest {
        val entity = CreateSubmissionEntity(
            id = 1,
            assignmentId = 1,
            userId = 1,
            errorFlag = false,
            submissionType = "online_text_entry",
            canvasContext = CanvasContext.defaultCanvasContext(),
        )

        dao.insert(entity)
        dao.insert(entity)
    }

    @Test
    fun testFindSubmissionById() = runTest {
        val entity = CreateSubmissionEntity(
            id = 1,
            assignmentId = 1,
            userId = 1,
            errorFlag = false,
            submissionType = "online_text_entry",
            canvasContext = CanvasContext.defaultCanvasContext(),
        )

        dao.insert(entity)

        val result = dao.findSubmissionById(1)
        assertEquals(entity, result)
    }

    @Test
    fun testSetSubmissionError() = runTest {
        val entity = CreateSubmissionEntity(
            id = 1,
            assignmentId = 1,
            userId = 1,
            errorFlag = false,
            submissionType = "online_text_entry",
            canvasContext = CanvasContext.defaultCanvasContext(),
        )

        dao.insert(entity)

        dao.setSubmissionError(true, 1)

        val result = dao.findSubmissionById(1)
        assertEquals(true, result?.errorFlag)
    }

    @Test
    fun testFindSubmissionsByAssignmentId() = runTest {
        val entities = listOf(
            CreateSubmissionEntity(
                id = 1,
                assignmentId = 1,
                userId = 1,
                errorFlag = false,
                submissionType = "online_text_entry",
                canvasContext = CanvasContext.defaultCanvasContext(),
            ),
            CreateSubmissionEntity(
                id = 2,
                assignmentId = 1,
                userId = 1,
                errorFlag = false,
                submissionType = "online_text_entry",
                canvasContext = CanvasContext.defaultCanvasContext(),
            ),
            CreateSubmissionEntity(
                id = 3,
                assignmentId = 2,
                userId = 1,
                errorFlag = false,
                submissionType = "online_text_entry",
                canvasContext = CanvasContext.defaultCanvasContext(),
            )
        )

        entities.forEach { dao.insert(it) }

        val result = dao.findSubmissionsByAssignmentId(1, 1)
        assertEquals(entities.subList(0, 2), result)
    }

    @Test
    fun testDeleteSubmissionsForAssignmentId() = runTest {
        val entities = listOf(
            CreateSubmissionEntity(
                id = 1,
                assignmentId = 1,
                userId = 1,
                errorFlag = false,
                submissionType = "online_text_entry",
                canvasContext = CanvasContext.defaultCanvasContext(),
            ),
            CreateSubmissionEntity(
                id = 2,
                assignmentId = 1,
                userId = 1,
                errorFlag = false,
                submissionType = "online_text_entry",
                canvasContext = CanvasContext.defaultCanvasContext(),
            ),
            CreateSubmissionEntity(
                id = 3,
                assignmentId = 2,
                userId = 1,
                errorFlag = false,
                submissionType = "online_text_entry",
                canvasContext = CanvasContext.defaultCanvasContext(),
            )
        )

        entities.forEach { dao.insert(it) }

        dao.deleteSubmissionsForAssignmentId(1L, 1L)

        val result = dao.findSubmissionById(3L)
        assertEquals(entities[2], result)
    }

    @Test
    fun testUpdateProgress() = runTest {
        val entity = CreateSubmissionEntity(
            id = 1,
            assignmentId = 1,
            userId = 1,
            errorFlag = false,
            submissionType = "online_text_entry",
            canvasContext = CanvasContext.defaultCanvasContext(),
        )

        dao.insert(entity)

        dao.updateProgress(1, 1, 0.5, 1)

        val result = dao.findSubmissionById(1)
        assertEquals(1L, result?.currentFile)
        assertEquals(1, result?.fileCount)
        assertEquals(0.5f, result?.progress)
    }

    @Test
    fun testFindSubmissionsByRowId() = runTest {
        val entity = CreateSubmissionEntity(
            id = 1,
            assignmentId = 1,
            userId = 1,
            errorFlag = false,
            submissionType = "online_text_entry",
            canvasContext = CanvasContext.defaultCanvasContext(),
        )

        val rowId = dao.insert(entity)

        val result = dao.findSubmissionByRowId(rowId)
        assertEquals(entity, result)
    }

    @Test
    fun testDeleteDraftByAssignmentId() = runTest {
        val entities = listOf(
            CreateSubmissionEntity(
                id = 1,
                assignmentId = 1,
                userId = 1,
                errorFlag = false,
                submissionType = "online_text_entry",
                canvasContext = CanvasContext.defaultCanvasContext(),
                isDraft = true
            ),
            CreateSubmissionEntity(
                id = 2,
                assignmentId = 1,
                userId = 1,
                errorFlag = false,
                submissionType = "online_text_entry",
                canvasContext = CanvasContext.defaultCanvasContext(),
                isDraft = false
            ),
            CreateSubmissionEntity(
                id = 3,
                assignmentId = 2,
                userId = 1,
                errorFlag = false,
                submissionType = "online_text_entry",
                canvasContext = CanvasContext.defaultCanvasContext(),
                isDraft = true
            )
        )

        entities.forEach { dao.insert(it) }

        dao.deleteDraftByAssignmentId(1L, 1L)

        val result = dao.findSubmissionById(1L)
        assertNull(result)

        val result2 = dao.findSubmissionById(3L)
        assertEquals(entities[2], result2)
    }

    @Test
    fun testUpdateSubmissionState() = runTest {
        val entity = CreateSubmissionEntity(
            id = 1,
            assignmentId = 1,
            userId = 1,
            errorFlag = false,
            submissionType = "online_text_entry",
            canvasContext = CanvasContext.defaultCanvasContext(),
        )

        dao.insert(entity)

        dao.updateSubmissionState(1, com.instructure.pandautils.room.studentdb.entities.SubmissionState.FAILED)

        val result = dao.findSubmissionById(1)
        assertEquals(com.instructure.pandautils.room.studentdb.entities.SubmissionState.FAILED, result?.submissionState)
    }
}