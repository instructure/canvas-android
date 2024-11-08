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
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.CourseEntity
import com.instructure.pandautils.room.offline.entities.LocalFileEntity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Date

class LocalFileDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var localFileDao: LocalFileDao
    private lateinit var courseDao: CourseDao

    @Before
    fun setUp() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        localFileDao = db.localFileDao()
        courseDao = db.courseDao()

        courseDao.insert(CourseEntity(Course(1L)))
    }

    @Test
    fun testInsertReplace() = runTest {
        val file = LocalFileEntity(1L, 1L, Date(), "")
        val updated = LocalFileEntity(1L, 1L, Date(), "updated")

        localFileDao.insert(file)
        localFileDao.insert(updated)

        val result = localFileDao.findById(1L)

        assertEquals(updated, result)
    }

    @Test
    fun testFindById() = runTest {
        val files = listOf(
            LocalFileEntity(1L, 1L, Date(), ""),
            LocalFileEntity(2L, 1L, Date(), "")
        )

        files.forEach {
            localFileDao.insert(it)
        }

        val result = localFileDao.findById(1L)

        assertEquals(files[0], result)
    }

    @Test
    fun testFindByIds() = runTest {
        val files = listOf(
            LocalFileEntity(1L, 1L, Date(), ""),
            LocalFileEntity(2L, 1L, Date(), "")
        )

        files.forEach {
            localFileDao.insert(it)
        }

        val result = localFileDao.findByIds(listOf(1L, 2L))

        assertEquals(files, result)
    }

    @Test
    fun testFindRemovedFiles() = runTest {
        val course2 = CourseEntity(Course(2L))

        courseDao.insert(course2)

        val files = listOf(
            LocalFileEntity(1L, 1L, Date(), ""),
            LocalFileEntity(2L, 1L, Date(), ""),
            LocalFileEntity(3L, 1L, Date(), ""),
            LocalFileEntity(4L, 2L, Date(), "")
        )

        files.forEach {
            localFileDao.insert(it)
        }

        val result = localFileDao.findRemovedFiles(1L, listOf(1L, 3L, 4L))

        assertEquals(listOf(files[1]), result)
    }

    @Test
    fun testFindByCourseId() = runTest {
        val files = listOf(
            LocalFileEntity(3L, 1L, Date(), ""),
            LocalFileEntity(4L, 2L, Date(), "")
        )

        files.forEach {
            localFileDao.insert(it)
        }

        val result = localFileDao.findByCourseId(1L)

        assertEquals(files.take(1), result)
    }

    @Test
    fun testExistsById() = runTest {
        localFileDao.insert(LocalFileEntity(1L, 1L, Date(), ""))

        val existsResult = localFileDao.existsById(1L)
        val notExistsResult = localFileDao.existsById(2L)

        assertEquals(true, existsResult)
        assertEquals(false, notExistsResult)
    }
}
