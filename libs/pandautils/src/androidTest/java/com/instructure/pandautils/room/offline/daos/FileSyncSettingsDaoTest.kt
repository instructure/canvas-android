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
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileSyncSettingsDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var fileSyncSettingsDao: FileSyncSettingsDao

    @Before
    fun setUp() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        fileSyncSettingsDao = db.fileSyncSettingsDao()

        val courseSyncSettingsDao = db.courseSyncSettingsDao()
        courseSyncSettingsDao.insert(
            CourseSyncSettingsEntity(
                courseId = 1L,
                courseName = "Course 1",
                fullContentSync = false
            )
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertReplaces() = runTest {
        val fileSyncSettingsEntity = FileSyncSettingsEntity(1L, "", 1L, null)

        fileSyncSettingsDao.insert(fileSyncSettingsEntity)

        val updated = fileSyncSettingsEntity.copy(url = "https://instructure.com")

        fileSyncSettingsDao.insert(updated)

        val result = fileSyncSettingsDao.findById(1L)

        assertEquals(updated, result)
    }

    @Test
    fun testFindById() = runTest {
        val fileSyncSettingsEntity = FileSyncSettingsEntity(1L, "", 1L, null)
        val fileSyncSettingsEntity2 = FileSyncSettingsEntity(2L, "", 1L, null)

        fileSyncSettingsDao.insert(fileSyncSettingsEntity)
        fileSyncSettingsDao.insert(fileSyncSettingsEntity2)

        val result = fileSyncSettingsDao.findById(2L)

        assertEquals(fileSyncSettingsEntity2, result)
    }

    @Test
    fun testFindAll() = runTest {
        val fileSyncSettingsEntity = FileSyncSettingsEntity(1L, "", 1L, null)
        val fileSyncSettingsEntity2 = FileSyncSettingsEntity(2L, "", 1L, null)

        fileSyncSettingsDao.insert(fileSyncSettingsEntity)
        fileSyncSettingsDao.insert(fileSyncSettingsEntity2)

        val result = fileSyncSettingsDao.findAll()

        assertEquals(listOf(fileSyncSettingsEntity, fileSyncSettingsEntity2), result)
    }

    @Test
    fun testDeleteById() = runTest {
        val fileSyncSettingsEntity = FileSyncSettingsEntity(1L, "", 1L, null)
        val fileSyncSettingsEntity2 = FileSyncSettingsEntity(2L, "", 1L, null)

        fileSyncSettingsDao.insert(fileSyncSettingsEntity)
        fileSyncSettingsDao.insert(fileSyncSettingsEntity2)

        val result1 = fileSyncSettingsDao.findById(1L)
        assertEquals(fileSyncSettingsEntity, result1)

        fileSyncSettingsDao.deleteById(1L)

        val result2 = fileSyncSettingsDao.findById(1L)
        assertNull(result2)
    }

    @Test
    fun testDeleteByIds() = runTest {
        val fileSyncSettingsEntity = FileSyncSettingsEntity(1L, "", 1L, null)
        val fileSyncSettingsEntity2 = FileSyncSettingsEntity(2L, "", 1L, null)
        val fileSyncSettingsEntity3 = FileSyncSettingsEntity(3L, "", 1L, null)

        fileSyncSettingsDao.insert(fileSyncSettingsEntity)
        fileSyncSettingsDao.insert(fileSyncSettingsEntity2)
        fileSyncSettingsDao.insert(fileSyncSettingsEntity3)

        val result1 = fileSyncSettingsDao.findById(1L)
        assertEquals(fileSyncSettingsEntity, result1)
        val result2 = fileSyncSettingsDao.findById(3L)
        assertEquals(fileSyncSettingsEntity3, result2)

        fileSyncSettingsDao.deleteByIds(listOf(1L, 3L))

        val result3 = fileSyncSettingsDao.findById(1L)
        assertNull(result3)
        val result4 = fileSyncSettingsDao.findById(3L)
        assertNull(result4)
    }
}