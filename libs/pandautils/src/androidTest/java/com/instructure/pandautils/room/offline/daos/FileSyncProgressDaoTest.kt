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
 *
 */

package com.instructure.pandautils.room.offline.daos

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.CourseSyncProgressEntity
import com.instructure.pandautils.room.offline.entities.FileSyncProgressEntity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class FileSyncProgressDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: OfflineDatabase
    private lateinit var courseSyncProgressDao: CourseSyncProgressDao
    private lateinit var fileSyncProgressDao: FileSyncProgressDao

    @Before
    fun setUp() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        courseSyncProgressDao = db.courseSyncProgressDao()
        fileSyncProgressDao = db.fileSyncProgressDao()

        courseSyncProgressDao.insert(
            CourseSyncProgressEntity(
                workerId = "workerId",
                courseId = 1L,
                courseName = "Course 1"
            )
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testInsertError() = runTest {
        val entity = FileSyncProgressEntity(
            workerId = "workerId",
            courseId = 1L,
            fileName = "File 1",
            progress = 0,
            fileSize = 1000L,
            progressState = ProgressState.IN_PROGRESS
        )
        fileSyncProgressDao.insert(entity)

        val updatedEntity = entity.copy(progressState = ProgressState.COMPLETED)
        fileSyncProgressDao.insert(updatedEntity)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testInsertAllError() = runTest {
        val entity = FileSyncProgressEntity(
            workerId = "workerId",
            courseId = 1L,
            fileName = "File 1",
            progress = 0,
            fileSize = 1000L,
            progressState = ProgressState.IN_PROGRESS
        )
        fileSyncProgressDao.insertAll(listOf(entity))

        val updatedEntity = entity.copy(progressState = ProgressState.COMPLETED)
        fileSyncProgressDao.insertAll(listOf(updatedEntity))
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testForeignKeyInsert() = runTest {
        courseSyncProgressDao.deleteAll()

        val entity = FileSyncProgressEntity(
            workerId = "workerId",
            courseId = 1L,
            fileName = "File 1",
            progress = 0,
            fileSize = 1000L,
            progressState = ProgressState.IN_PROGRESS
        )
        fileSyncProgressDao.insert(entity)
    }

    @Test
    fun testForeignKeyDelete() = runTest {
        val entity = FileSyncProgressEntity(
            workerId = "workerId",
            courseId = 1L,
            fileName = "File 1",
            progress = 0,
            fileSize = 1000L,
            progressState = ProgressState.IN_PROGRESS
        )
        fileSyncProgressDao.insert(entity)

        courseSyncProgressDao.deleteAll()

        val result = fileSyncProgressDao.findByCourseId(1L)

        assert(result.isEmpty())
    }

    @Test
    fun testFindByWorkerId() = runTest {
        val entities = listOf(
            FileSyncProgressEntity(
                workerId = "workerId",
                courseId = 1L,
                fileName = "File 1",
                progress = 0,
                fileSize = 1000L,
                progressState = ProgressState.IN_PROGRESS
            ),
            FileSyncProgressEntity(
                workerId = "workerId2",
                courseId = 1L,
                fileName = "File 2",
                progress = 0,
                fileSize = 1000L,
                progressState = ProgressState.IN_PROGRESS
            )
        )
        fileSyncProgressDao.insertAll(entities)

        val result = fileSyncProgressDao.findByWorkerId("workerId")

        assertEquals(entities[0], result)
    }

    @Test
    fun testFindByWorkerIdLiveData() = runTest {
        val entities = listOf(
            FileSyncProgressEntity(
                workerId = "workerId",
                courseId = 1L,
                fileName = "File 1",
                progress = 0,
                fileSize = 1000L,
                progressState = ProgressState.IN_PROGRESS
            ),
            FileSyncProgressEntity(
                workerId = "workerId2",
                courseId = 1L,
                fileName = "File 2",
                progress = 0,
                fileSize = 1000L,
                progressState = ProgressState.IN_PROGRESS
            )
        )
        fileSyncProgressDao.insertAll(entities)

        val result = fileSyncProgressDao.findByWorkerIdLiveData("workerId")
        result.observeForever { }

        assertEquals(entities[0], result.value)
    }

    @Test
    fun testFindByCourseIdLiveData() = runTest {
        courseSyncProgressDao.insert(CourseSyncProgressEntity(2L, "workerId2", "Course 2"))
        val entities = listOf(
            FileSyncProgressEntity(
                workerId = "workerId",
                courseId = 1L,
                fileName = "File 1",
                progress = 0,
                fileSize = 1000L,
                progressState = ProgressState.IN_PROGRESS
            ),
            FileSyncProgressEntity(
                workerId = "workerId2",
                courseId = 1L,
                fileName = "File 2",
                progress = 0,
                fileSize = 1000L,
                progressState = ProgressState.IN_PROGRESS
            ),
            FileSyncProgressEntity(
                workerId = "workerId3",
                courseId = 2L,
                fileName = "File 3",
                progress = 0,
                fileSize = 1000L,
                progressState = ProgressState.IN_PROGRESS
            )
        )
        fileSyncProgressDao.insertAll(entities)

        val result = fileSyncProgressDao.findByCourseIdLiveData(1L)
        result.observeForever { }

        assertEquals(entities.subList(0, 2), result.value)
    }

    @Test
    fun testFindAllLiveData() = runTest {
        courseSyncProgressDao.insert(CourseSyncProgressEntity(2L, "workerId2", "Course 2"))
        val entities = listOf(
            FileSyncProgressEntity(
                workerId = "workerId",
                courseId = 1L,
                fileName = "File 1",
                progress = 0,
                fileSize = 1000L,
                progressState = ProgressState.IN_PROGRESS
            ),
            FileSyncProgressEntity(
                workerId = "workerId2",
                courseId = 1L,
                fileName = "File 2",
                progress = 0,
                fileSize = 1000L,
                progressState = ProgressState.IN_PROGRESS
            ),
            FileSyncProgressEntity(
                workerId = "workerId3",
                courseId = 2L,
                fileName = "File 3",
                progress = 0,
                fileSize = 1000L,
                progressState = ProgressState.IN_PROGRESS
            )
        )
        fileSyncProgressDao.insertAll(entities)

        val result = fileSyncProgressDao.findAllLiveData()
        result.observeForever { }

        assertEquals(entities, result.value)
    }

    @Test
    fun testDeleteAll() = runTest {
        val entities = listOf(
            FileSyncProgressEntity(
                workerId = "workerId",
                courseId = 1L,
                fileName = "File 1",
                progress = 0,
                fileSize = 1000L,
                progressState = ProgressState.IN_PROGRESS
            ),
            FileSyncProgressEntity(
                workerId = "workerId2",
                courseId = 1L,
                fileName = "File 2",
                progress = 0,
                fileSize = 1000L,
                progressState = ProgressState.IN_PROGRESS
            )
        )

        fileSyncProgressDao.insertAll(entities)

        fileSyncProgressDao.deleteAll()

        val result = fileSyncProgressDao.findAllLiveData()
        result.observeForever { }

        assert(result.value!!.isEmpty())
    }

}