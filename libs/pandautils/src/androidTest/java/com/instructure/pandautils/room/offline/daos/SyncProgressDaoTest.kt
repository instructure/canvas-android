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
import com.instructure.pandautils.room.offline.entities.SyncProgressEntity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SyncProgressDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var syncProgressDao: SyncProgressDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        syncProgressDao = db.syncProgressDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun testFindAll() = runTest {
        val syncProgresses = listOf(
            SyncProgressEntity(UUID.randomUUID().toString(), 1L, "Course 1"),
            SyncProgressEntity(UUID.randomUUID().toString(), 2L, "Course 2"),
            SyncProgressEntity(UUID.randomUUID().toString(), 3L, "Course 3")
        )

        syncProgressDao.insertAll(syncProgresses)

        val result = syncProgressDao.findCourseProgresses()

        assertEquals(syncProgresses, result)
    }

    @Test
    fun testDeleteAll() = runTest {
        val syncProgresses = listOf(
            SyncProgressEntity(UUID.randomUUID().toString(), 1L, "Course 1"),
            SyncProgressEntity(UUID.randomUUID().toString(), 2L, "Course 2"),
            SyncProgressEntity(UUID.randomUUID().toString(), 3L, "Course 3")
        )

        syncProgressDao.insertAll(syncProgresses)

        syncProgressDao.deleteAll()

        val result = syncProgressDao.findCourseProgresses()

        assertEquals(emptyList<SyncProgressEntity>(), result)
    }

    @Test
    fun testClearAndInsert() = runTest {
        val syncProgresses = listOf(
            SyncProgressEntity(UUID.randomUUID().toString(), 1L, "Course 1"),
            SyncProgressEntity(UUID.randomUUID().toString(), 2L, "Course 2"),
            SyncProgressEntity(UUID.randomUUID().toString(), 3L, "Course 3")
        )

        syncProgressDao.insertAll(syncProgresses)

        val updatedSyncProgresses = listOf(
            SyncProgressEntity(UUID.randomUUID().toString(), 4L, "Course 4"),
            SyncProgressEntity(UUID.randomUUID().toString(), 5L, "Course 5"),
            SyncProgressEntity(UUID.randomUUID().toString(), 6L, "Course 6")
        )

        syncProgressDao.clearAndInsert(updatedSyncProgresses)

        val result = syncProgressDao.findCourseProgresses()

        assertEquals(updatedSyncProgresses, result)
    }
}