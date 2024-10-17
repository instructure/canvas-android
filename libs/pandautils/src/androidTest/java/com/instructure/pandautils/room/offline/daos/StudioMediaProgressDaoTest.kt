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
 *
 *
 */

package com.instructure.pandautils.room.offline.daos

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.StudioMediaProgressEntity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudioMediaProgressDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: OfflineDatabase
    private lateinit var studioMediaProgressDao: StudioMediaProgressDao

    @Before
    fun setUp() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        studioMediaProgressDao = db.studioMediaProgressDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindById() = runTest {
        val entities = listOf(
            StudioMediaProgressEntity(
                ltiLaunchId = "1",
                progress = 0,
                fileSize = 1000L,
                progressState = ProgressState.IN_PROGRESS
            ),
            StudioMediaProgressEntity(
                ltiLaunchId = "2",
                progress = 0,
                fileSize = 1000L,
                progressState = ProgressState.IN_PROGRESS
            )
        )
        studioMediaProgressDao.insertAll(entities)

        val result = studioMediaProgressDao.findById(1L)

        assertEquals(entities[0].copy(id = 1L), result)
    }

    @Test
    fun testFindAllLiveData() = runTest {
        val entities = listOf(
            StudioMediaProgressEntity(
                ltiLaunchId = "1",
                progress = 0,
                fileSize = 1000L,
                progressState = ProgressState.IN_PROGRESS,
                id = 1
            ),
            StudioMediaProgressEntity(
                ltiLaunchId = "2",
                progress = 0,
                fileSize = 1000L,
                progressState = ProgressState.IN_PROGRESS,
                id = 2
            ),
            StudioMediaProgressEntity(
                ltiLaunchId = "3",
                progress = 0,
                fileSize = 1000L,
                progressState = ProgressState.IN_PROGRESS,
                id = 3
            )
        )
        studioMediaProgressDao.insertAll(entities)

        val result = studioMediaProgressDao.findAllLiveData()
        result.observeForever { }

        assertEquals(entities, result.value)
    }

    @Test
    fun testDeleteAll() = runTest {
        val entities = listOf(
            StudioMediaProgressEntity(
                ltiLaunchId = "1",
                progress = 0,
                fileSize = 1000L,
                progressState = ProgressState.IN_PROGRESS
            ),
            StudioMediaProgressEntity(
                ltiLaunchId = "2",
                progress = 0,
                fileSize = 1000L,
                progressState = ProgressState.IN_PROGRESS
            ),
        )

        studioMediaProgressDao.insertAll(entities)

        studioMediaProgressDao.deleteAll()

        val result = studioMediaProgressDao.findAllLiveData()
        result.observeForever { }

        assert(result.value!!.isEmpty())
    }

    @Test
    fun testFindByRowId() = runTest {
        val entities = listOf(
            StudioMediaProgressEntity(
                ltiLaunchId = "1",
                progress = 0,
                fileSize = 1000L,
                progressState = ProgressState.IN_PROGRESS
            ),
            StudioMediaProgressEntity(
                ltiLaunchId = "2",
                progress = 0,
                fileSize = 1000L,
                progressState = ProgressState.IN_PROGRESS
            ),
        )

        studioMediaProgressDao.insertAll(entities)

        val entity = StudioMediaProgressEntity(
            ltiLaunchId = "3",
            progress = 0,
            fileSize = 1000L,
            progressState = ProgressState.IN_PROGRESS,
            id = 3
        )

        val rowId = studioMediaProgressDao.insert(entity)

        val result = studioMediaProgressDao.findByRowId(rowId)

        assertEquals(entity, result)
    }
}
