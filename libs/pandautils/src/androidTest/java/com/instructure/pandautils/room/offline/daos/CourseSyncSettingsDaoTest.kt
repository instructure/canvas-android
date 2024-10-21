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
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.FileSyncSettingsEntity
import com.instructure.pandautils.room.offline.model.CourseSyncSettingsWithFiles
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CourseSyncSettingsSD {

    private lateinit var db: OfflineDatabase
    private lateinit var courseSyncSettingsDao: CourseSyncSettingsDao
    private lateinit var fileSyncSettingsDao: FileSyncSettingsDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        courseSyncSettingsDao = db.courseSyncSettingsDao()
        fileSyncSettingsDao = db.fileSyncSettingsDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindAllEntities() = runTest {
        val courseSyncSettingsEntity = CourseSyncSettingsEntity(
            courseId = 1L,
            courseName = "Course 1",
            fullContentSync = false
        )
        val courseSyncSettingsEntity2 = CourseSyncSettingsEntity(
            courseId = 2L,
            courseName = "Course 2",
            fullContentSync = false
        )
        courseSyncSettingsDao.insert(courseSyncSettingsEntity)
        courseSyncSettingsDao.insert(courseSyncSettingsEntity2)

        val result = courseSyncSettingsDao.findAll()

        assertEquals(listOf(courseSyncSettingsEntity, courseSyncSettingsEntity2), result)
    }

    @Test
    fun testInsertReplace() = runTest {
        val courseSyncSettingsEntity = CourseSyncSettingsEntity(
            courseId = 1L,
            courseName = "Course 1",
            fullContentSync = false
        )

        courseSyncSettingsDao.insert(courseSyncSettingsEntity)

        val updated = courseSyncSettingsEntity.copy(fullContentSync = true)
        courseSyncSettingsDao.insert(updated)

        val result = courseSyncSettingsDao.findById(1L)

        assertEquals(updated, result)
    }

    @Test
    fun testFindById() = runTest {
        val courseSyncSettingsEntity = CourseSyncSettingsEntity(
            courseId = 1L,
            courseName = "Course 1",
            fullContentSync = false
        )
        val courseSyncSettingsEntity2 = CourseSyncSettingsEntity(
            courseId = 2L,
            courseName = "Course 2",
            fullContentSync = false
        )
        courseSyncSettingsDao.insert(courseSyncSettingsEntity)
        courseSyncSettingsDao.insert(courseSyncSettingsEntity2)

        val result = courseSyncSettingsDao.findById(2L)

        assertEquals(courseSyncSettingsEntity2, result)
    }

    @Test
    fun testFindByIdList() = runTest {
        val courseSyncSettingsEntity = CourseSyncSettingsEntity(
            courseId = 1L,
            courseName = "Course 1",
            fullContentSync = false
        )
        val courseSyncSettingsEntity2 = CourseSyncSettingsEntity(
            courseId = 2L,
            courseName = "Course 2",
            fullContentSync = false
        )

        val courseSyncSettingsEntity3 = CourseSyncSettingsEntity(
            courseId = 3L,
            courseName = "Course 3",
            fullContentSync = false
        )
        courseSyncSettingsDao.insert(courseSyncSettingsEntity)
        courseSyncSettingsDao.insert(courseSyncSettingsEntity2)
        courseSyncSettingsDao.insert(courseSyncSettingsEntity3)

        val result = courseSyncSettingsDao.findByIds(listOf(1L, 3L))

        assertEquals(listOf(courseSyncSettingsEntity, courseSyncSettingsEntity3), result)
    }

    @Test
    fun findWithFiles() = runTest {
        val courseSyncSettingsEntity = CourseSyncSettingsEntity(
            courseId = 1L,
            courseName = "Course 1",
            fullContentSync = false
        )
        val courseSyncSettingsEntity2 = CourseSyncSettingsEntity(
            courseId = 2L,
            courseName = "Course 2",
            fullContentSync = false
        )
        courseSyncSettingsDao.insert(courseSyncSettingsEntity)
        courseSyncSettingsDao.insert(courseSyncSettingsEntity2)

        val fileSyncSettingsEntity = FileSyncSettingsEntity(1L, "", 1L, null)
        val fileSyncSettingsEntity2 = FileSyncSettingsEntity(2L, "", 1L, null)
        val fileSyncSettingsEntity3 = FileSyncSettingsEntity(3L, "", 2L, null)
        val fileSyncSettingsEntity4 = FileSyncSettingsEntity(4L, "", 2L, null)
        fileSyncSettingsDao.insert(fileSyncSettingsEntity)
        fileSyncSettingsDao.insert(fileSyncSettingsEntity2)
        fileSyncSettingsDao.insert(fileSyncSettingsEntity3)
        fileSyncSettingsDao.insert(fileSyncSettingsEntity4)

        val result1 = courseSyncSettingsDao.findWithFilesById(1L)
        assertEquals(
            CourseSyncSettingsWithFiles(
                courseSyncSettingsEntity,
                listOf(fileSyncSettingsEntity, fileSyncSettingsEntity2)
            ), result1
        )

        val result2 = courseSyncSettingsDao.findWithFilesById(2L)
        assertEquals(
            CourseSyncSettingsWithFiles(
                courseSyncSettingsEntity2,
                listOf(fileSyncSettingsEntity3, fileSyncSettingsEntity4)
            ), result2
        )
    }
}