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

package com.instructure.pandautils.features.offline.sync.progress

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.features.offline.sync.AggregateProgressObserver
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.TabSyncData
import com.instructure.pandautils.room.offline.daos.CourseSyncProgressDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.entities.CourseSyncProgressEntity
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.FileSyncProgressEntity
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class AggregateProgressObserverTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val context: Context = mockk(relaxed = true)
    private val courseSyncProgressDao: CourseSyncProgressDao = mockk(relaxed = true)
    private val fileSyncProgressDao: FileSyncProgressDao = mockk(relaxed = true)

    private lateinit var aggregateProgressObserver: AggregateProgressObserver

    @Before
    fun setup() {
        mockkObject(NumberHelper)
        val captor = slot<Long>()
        every { NumberHelper.readableFileSize(any<Context>(), capture(captor)) } answers {
            "${captor.captured} bytes"
        }
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `Course update aggregate progress`() {
        val course1UUID = UUID.randomUUID().toString()
        var courseProgress = CourseSyncProgressEntity(
            1L,
            course1UUID,
            "Course 1",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
            progressState = ProgressState.IN_PROGRESS
        )

        val courseProgressLiveData = MutableLiveData(listOf(courseProgress))

        every { courseSyncProgressDao.findAllLiveData() } returns courseProgressLiveData

        aggregateProgressObserver = createObserver()

        assertEquals(0, aggregateProgressObserver.progressData.value?.progress)
        assertEquals(
            "${CourseSyncSettingsEntity.TABS.size * 100 * 1000} bytes",
            aggregateProgressObserver.progressData.value?.totalSize
        )
        assertEquals(ProgressState.IN_PROGRESS, aggregateProgressObserver.progressData.value?.progressState)

        courseProgress = courseProgress.copy(
            tabs = CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.COMPLETED) },
            progressState = ProgressState.COMPLETED
        )

        courseProgressLiveData.postValue(listOf(courseProgress))

        assertEquals(100, aggregateProgressObserver.progressData.value?.progress)
        assertEquals(ProgressState.COMPLETED, aggregateProgressObserver.progressData.value?.progressState)
    }

    @Test
    fun `Aggregate progress updates`() {
        val course1Id = UUID.randomUUID()
        val course2Id = UUID.randomUUID()

        val file1Id = UUID.randomUUID()
        val file2Id = UUID.randomUUID()

        var course1 = CourseSyncProgressEntity(
            1L,
            course1Id.toString(),
            "Course 1",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
            progressState = ProgressState.IN_PROGRESS
        )
        var course2 = CourseSyncProgressEntity(
            2L,
            course2Id.toString(),
            "Course 2",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
            progressState = ProgressState.IN_PROGRESS
        )

        var file1 =
            FileSyncProgressEntity(file1Id.toString(), 1L, "File 1", 0, 1000, progressState = ProgressState.IN_PROGRESS)
        var file2 =
            FileSyncProgressEntity(file2Id.toString(), 2L, "File 1", 0, 2000, progressState = ProgressState.IN_PROGRESS)

        val courseLiveData = MutableLiveData(listOf(course1, course2))
        val fileLiveData = MutableLiveData(listOf(file1, file2))

        every { courseSyncProgressDao.findAllLiveData() } returns courseLiveData
        every { fileSyncProgressDao.findAllLiveData() } returns fileLiveData

        aggregateProgressObserver = createObserver()

        assertEquals(0, aggregateProgressObserver.progressData.value?.progress)
        assertEquals(
            "${2 * 1000000 + 1000 + 2000} bytes",
            aggregateProgressObserver.progressData.value?.totalSize
        )

        file1 = file1.copy(
            progress = 100,
            progressState = ProgressState.COMPLETED
        )

        course1 = course1.copy(
            tabs = CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.COMPLETED) },
            progressState = ProgressState.COMPLETED
        )

        courseLiveData.postValue(listOf(course1, course2))
        fileLiveData.postValue(listOf(file1, file2))

        assertEquals(49, aggregateProgressObserver.progressData.value?.progress)

        file2 = file2.copy(
            progress = 100,
            progressState = ProgressState.COMPLETED
        )

        course2 = course2.copy(
            tabs = CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.COMPLETED) },
            progressState = ProgressState.COMPLETED
        )

        courseLiveData.postValue(listOf(course1, course2))
        fileLiveData.postValue(listOf(file1, file2))

        assertEquals(100, aggregateProgressObserver.progressData.value?.progress)
        assertEquals(ProgressState.COMPLETED, aggregateProgressObserver.progressData.value?.progressState)
    }

    @Test
    fun `Update total size and progress with additional files`() {
        val course1Id = UUID.randomUUID()

        val file1Id = UUID.randomUUID()
        val file2Id = UUID.randomUUID()
        val file3Id = UUID.randomUUID()

        var course1Progress = CourseSyncProgressEntity(
            1L,
            course1Id.toString(),
            "Course 1",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
            additionalFilesStarted = true,
            progressState = ProgressState.IN_PROGRESS
        )

        val courseLiveData = MutableLiveData(listOf(course1Progress))

        var file1Progress =
            FileSyncProgressEntity(file1Id.toString(), 1L, "File 1", 0, 1000, false, false, ProgressState.IN_PROGRESS)
        var file2Progress = FileSyncProgressEntity(
            file2Id.toString(),
            1L,
            "Additional internal file",
            0,
            2000,
            true,
            false,
            ProgressState.IN_PROGRESS
        )
        var file3Progress = FileSyncProgressEntity(
            file3Id.toString(),
            1L,
            "Additional external file",
            0,
            0,
            true,
            true,
            ProgressState.IN_PROGRESS
        )

        val fileLiveData = MutableLiveData(listOf(file1Progress, file2Progress, file3Progress))

        every { courseSyncProgressDao.findAllLiveData() } returns courseLiveData
        every { fileSyncProgressDao.findAllLiveData() } returns fileLiveData

        aggregateProgressObserver = createObserver()

        assertEquals(0, aggregateProgressObserver.progressData.value?.progress)
        assertEquals(
            "${1000000 + 1000 + 2000} bytes",
            aggregateProgressObserver.progressData.value?.totalSize
        )

        file1Progress = file1Progress.copy(
            progress = 100,
            progressState = ProgressState.COMPLETED
        )
        course1Progress = course1Progress.copy(
            tabs = CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.COMPLETED) },
        )

        courseLiveData.postValue(listOf(course1Progress))
        fileLiveData.postValue(listOf(file1Progress, file2Progress, file3Progress))

        // Course tabs and files are completed, but additional files are still in progress
        assertEquals(99, aggregateProgressObserver.progressData.value?.progress)

        file2Progress = file2Progress.copy(progress = 100, progressState = ProgressState.COMPLETED)

        file3Progress = FileSyncProgressEntity(
            file3Id.toString(),
            1L,
            "Additional external file",
            0,
            3000,
            true,
            true,
            ProgressState.IN_PROGRESS
        )

        fileLiveData.postValue(listOf(file1Progress, file2Progress, file3Progress))

        // Total size is updated with the external file
        assertEquals("${1000000 + 1000 + 2000 + 3000} bytes", aggregateProgressObserver.progressData.value?.totalSize)

        file3Progress = FileSyncProgressEntity(
            file3Id.toString(),
            1L,
            "Additional external file",
            100,
            3000,
            true,
            true,
            ProgressState.COMPLETED
        )

        course1Progress = course1Progress.copy(
            progressState = ProgressState.COMPLETED
        )
        courseLiveData.postValue(listOf(course1Progress))
        fileLiveData.postValue(
            listOf(file1Progress, file2Progress, file3Progress)
        )

        // External files are downloaded, progress should be 100%
        assertEquals(
            100, aggregateProgressObserver.progressData.value?.progress
        )
        assertEquals(ProgressState.COMPLETED, aggregateProgressObserver.progressData.value?.progressState)
    }

    @Test
    fun `Error state`() {
        val course1UUID = UUID.randomUUID().toString()

        var course1 = CourseSyncProgressEntity(
            1L,
            course1UUID,
            "Course 1",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
            progressState = ProgressState.IN_PROGRESS
        )

        val courseLiveData = MutableLiveData(listOf(course1))

        every { courseSyncProgressDao.findAllLiveData() } returns courseLiveData

        aggregateProgressObserver = createObserver()

        course1 = course1.copy(
            tabs = CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.COMPLETED) },
            progressState = ProgressState.ERROR
        )

        courseLiveData.postValue(listOf(course1))

        assertEquals(ProgressState.ERROR, aggregateProgressObserver.progressData.value?.progressState)
    }

    private fun createObserver(): AggregateProgressObserver {
        return AggregateProgressObserver(context, courseSyncProgressDao, fileSyncProgressDao)
    }
}