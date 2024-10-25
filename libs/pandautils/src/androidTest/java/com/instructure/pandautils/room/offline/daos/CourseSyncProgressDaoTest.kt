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
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.TabSyncData
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.CourseSyncProgressEntity
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class CourseSyncProgressDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: OfflineDatabase
    private lateinit var courseSyncProgressDao: CourseSyncProgressDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        courseSyncProgressDao = db.courseSyncProgressDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testInsertError() = runTest {
        val entity = CourseSyncProgressEntity(
            1L,
            "Course 1",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
            progressState = ProgressState.IN_PROGRESS
        )
        courseSyncProgressDao.insert(entity)

        val updatedEntity = entity.copy(progressState = ProgressState.COMPLETED)
        courseSyncProgressDao.insert(updatedEntity)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testInsertAllError() = runTest {
        val entities = listOf(
            CourseSyncProgressEntity(
                1L,
                "Course 1",
                CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
                progressState = ProgressState.IN_PROGRESS
            ),
            CourseSyncProgressEntity(
                2L,
                "Course 2",
                CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
                progressState = ProgressState.IN_PROGRESS
            )
        )

        courseSyncProgressDao.insertAll(entities)

        val updatedEntity = entities.map {
            it.copy(progressState = ProgressState.COMPLETED)
        }
        courseSyncProgressDao.insertAll(updatedEntity)
    }


    @Test
    fun testFindAll() = runTest {
        val entities = listOf(
            CourseSyncProgressEntity(
                1L,
                "Course 1",
                CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
                progressState = ProgressState.IN_PROGRESS
            ),
            CourseSyncProgressEntity(
                2L,
                "Course 2",
                CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
                progressState = ProgressState.IN_PROGRESS
            )
        )

        courseSyncProgressDao.insertAll(entities)

        val result = courseSyncProgressDao.findAll()
        assertEquals(entities, result)
    }

    @Test
    fun testFindByCourseId() = runTest {
        val entities = listOf(
            CourseSyncProgressEntity(
                1L,
                "Course 1",
                CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
                progressState = ProgressState.IN_PROGRESS
            ),
            CourseSyncProgressEntity(
                2L,
                "Course 2",
                CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
                progressState = ProgressState.IN_PROGRESS
            )
        )

        courseSyncProgressDao.insertAll(entities)

        val result = courseSyncProgressDao.findByCourseId(2L)

        assertEquals(entities[1], result)
    }

    @Test
    fun testDeleteAll() = runTest {
        val entities = listOf(
            CourseSyncProgressEntity(
                1L,
                "Course 1",
                CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
                progressState = ProgressState.IN_PROGRESS
            ),
            CourseSyncProgressEntity(
                2L,
                "Course 2",
                CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
                progressState = ProgressState.IN_PROGRESS
            )
        )

        courseSyncProgressDao.insertAll(entities)

        courseSyncProgressDao.deleteAll()

        val result = courseSyncProgressDao.findAll()
        assert(result.isEmpty())
    }

    @Test
    fun testFindAllLiveData() = runTest {
        val entities = listOf(
            CourseSyncProgressEntity(
                1L,
                "Course 1",
                CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
                progressState = ProgressState.IN_PROGRESS
            ),
            CourseSyncProgressEntity(
                2L,
                "Course 2",
                CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
                progressState = ProgressState.IN_PROGRESS
            )
        )

        courseSyncProgressDao.insertAll(entities)

        val result = courseSyncProgressDao.findAllLiveData()
        result.observeForever { }

        assertEquals(entities, result.value)
    }

    @Test
    fun testFindByCourseIdLiveData() = runTest {
        val entities = listOf(
            CourseSyncProgressEntity(
                1L,
                "Course 1",
                CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
                progressState = ProgressState.IN_PROGRESS
            ),
            CourseSyncProgressEntity(
                2L,
                "Course 2",
                CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
                progressState = ProgressState.IN_PROGRESS
            )
        )

        courseSyncProgressDao.insertAll(entities)

        val result = courseSyncProgressDao.findByCourseIdLiveData(entities[1].courseId)
        result.observeForever { }

        assertEquals(entities[1], result.value)
    }

}