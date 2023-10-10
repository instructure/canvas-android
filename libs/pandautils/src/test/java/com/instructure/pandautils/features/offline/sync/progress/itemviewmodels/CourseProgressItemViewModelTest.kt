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

package com.instructure.pandautils.features.offline.sync.progress.itemviewmodels

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkInfo
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.TabSyncData
import com.instructure.pandautils.features.offline.sync.progress.AdditionalFilesProgressViewData
import com.instructure.pandautils.features.offline.sync.progress.CourseProgressViewData
import com.instructure.pandautils.room.offline.daos.CourseSyncProgressDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.entities.CourseSyncProgressEntity
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.FileSyncProgressEntity
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class CourseProgressItemViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val courseSyncProgressDao: CourseSyncProgressDao = mockk(relaxed = true)
    private val fileSyncProgressDao: FileSyncProgressDao = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    private lateinit var courseProgressItemViewModel: CourseProgressItemViewModel

    @Before
    fun setup() {
        mockkObject(NumberHelper)
        val captor = slot<Long>()
        every { NumberHelper.readableFileSize(any<Context>(), capture(captor)) } answers { "${captor.captured} bytes" }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Create tab items`() {
        val uuid = UUID.randomUUID()
        val courseProgress = CourseSyncProgressEntity(
            1L,
            uuid.toString(),
            "Course",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
            ProgressState.IN_PROGRESS
        )

        val courseLiveData = MutableLiveData(courseProgress)
        every { courseSyncProgressDao.findByWorkerIdLiveData(uuid.toString()) } returns courseLiveData
        every { fileSyncProgressDao.findByCourseIdLiveData(1L) } returns MutableLiveData(emptyList())

        courseProgressItemViewModel = createItemViewModel(uuid)

        assertEquals("1000000 bytes", courseProgressItemViewModel.data.size)

        courseProgress.tabs.values.forEachIndexed { index, tabSyncData ->
            assertEquals(tabSyncData.state, courseProgressItemViewModel.data.tabs?.get(index)?.data?.state)
            assertEquals(tabSyncData.tabName, courseProgressItemViewModel.data.tabs?.get(index)?.data?.tabName)
        }
    }

    @Test
    fun `Failed course sync`() {
        val uuid = UUID.randomUUID()
        val courseProgress = CourseSyncProgressEntity(
            1L,
            uuid.toString(),
            "Course",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.ERROR) },
            progressState = ProgressState.ERROR
        )

        val courseLiveData = MutableLiveData(courseProgress)
        every { courseSyncProgressDao.findByWorkerIdLiveData(uuid.toString()) } returns courseLiveData
        every { fileSyncProgressDao.findByCourseIdLiveData(1L) } returns MutableLiveData(emptyList())

        courseProgressItemViewModel = createItemViewModel(uuid)

        assertEquals(WorkInfo.State.FAILED, courseProgressItemViewModel.data.state)
    }

    @Test
    fun `Failed file sync`() {
        val uuid = UUID.randomUUID()
        val courseProgress = CourseSyncProgressEntity(
            1L,
            uuid.toString(),
            "Course",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.COMPLETED) },
            progressState = ProgressState.COMPLETED
        )

        var fileProgresses = listOf(
            FileSyncProgressEntity(UUID.randomUUID().toString(), 1L,"File 1", 0, 1000, ProgressState.IN_PROGRESS),
            FileSyncProgressEntity(UUID.randomUUID().toString(), 1L,"File 2", 0, 2000, ProgressState.IN_PROGRESS),
            FileSyncProgressEntity(UUID.randomUUID().toString(), 1L,"File 3", 0, 3000, ProgressState.IN_PROGRESS),
        )

        val courseLiveData = MutableLiveData(courseProgress)
        val fileLiveData = MutableLiveData(fileProgresses)
        every { courseSyncProgressDao.findByWorkerIdLiveData(uuid.toString()) } returns courseLiveData
        every { fileSyncProgressDao.findByCourseIdLiveData(1L) } returns fileLiveData

        fileProgresses = listOf(
            FileSyncProgressEntity(UUID.randomUUID().toString(), 1L,"File 1", 0, 1000, ProgressState.IN_PROGRESS),
            FileSyncProgressEntity(UUID.randomUUID().toString(), 1L,"File 2", 50, 2000, ProgressState.IN_PROGRESS),
            FileSyncProgressEntity(UUID.randomUUID().toString(), 1L,"File 3", 100, 3000, ProgressState.COMPLETED),
        )
        fileLiveData.postValue(fileProgresses)

        courseProgressItemViewModel = createItemViewModel(uuid)
        assertEquals(WorkInfo.State.RUNNING, courseProgressItemViewModel.data.state)

        fileProgresses = listOf(
            FileSyncProgressEntity(UUID.randomUUID().toString(), 1L,"File 1", 0, 1000, ProgressState.IN_PROGRESS),
            FileSyncProgressEntity(UUID.randomUUID().toString(), 1L,"File 2", 50, 2000, ProgressState.ERROR),
            FileSyncProgressEntity(UUID.randomUUID().toString(), 1L,"File 3", 100, 3000, ProgressState.COMPLETED),
        )
        fileLiveData.postValue(fileProgresses)

        assertEquals(WorkInfo.State.FAILED, courseProgressItemViewModel.data.state)
        assert(courseProgressItemViewModel.data.failed)
    }

    @Test
    fun `Sync success`() {
        val uuid = UUID.randomUUID()
        val courseProgress = CourseSyncProgressEntity(
            1L,
            uuid.toString(),
            "Course",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.COMPLETED) },
            progressState = ProgressState.COMPLETED
        )
        val fileProgresses = listOf(
            FileSyncProgressEntity(UUID.randomUUID().toString(), 1L,"File 1", 100, 1000, ProgressState.COMPLETED),
            FileSyncProgressEntity(UUID.randomUUID().toString(), 1L,"File 2", 100, 2000, ProgressState.COMPLETED),
            FileSyncProgressEntity(UUID.randomUUID().toString(), 1L,"File 3", 100, 3000, ProgressState.COMPLETED),
        )

        val courseLiveData = MutableLiveData(courseProgress)
        val fileLiveData = MutableLiveData(fileProgresses)

        every { courseSyncProgressDao.findByWorkerIdLiveData(uuid.toString()) } returns courseLiveData
        every { fileSyncProgressDao.findByCourseIdLiveData(1L) } returns fileLiveData

        courseProgressItemViewModel = createItemViewModel(uuid)

        assertEquals(WorkInfo.State.SUCCEEDED, courseProgressItemViewModel.data.state)
        assertFalse(courseProgressItemViewModel.data.failed)
    }

    @Test
    fun `Update total size with additional files`() {
        val uuid = UUID.randomUUID()
        val courseProgress = CourseProgress(
            1L,
            "Course",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
            fileSyncData = listOf(
                FileSyncData(UUID.randomUUID().toString(), "File 1", 1000),
                FileSyncData(UUID.randomUUID().toString(), "File 2", 2000),
            ),
            additionalFileSyncData = listOf(
                FileSyncData(UUID.randomUUID().toString(), "File 3", 3000), // Internal file, we know the size in advancr
                FileSyncData(UUID.randomUUID().toString(), "File 4", 0)) // External file, we don't know the size
        )

        val courseLiveData = MutableLiveData(createCourseWorkInfo(courseProgress, uuid, WorkInfo.State.RUNNING))
        every { workManager.getWorkInfoByIdLiveData(uuid) } returns courseLiveData

        val aggregateLiveData = MutableLiveData<List<WorkInfo>>()
        every { workManager.getWorkInfosLiveData(any()) } returns aggregateLiveData

        aggregateLiveData.postValue(
            listOf(
                createCourseWorkInfo(courseProgress, uuid, WorkInfo.State.RUNNING),
                createFileWorkInfo(
                    FileSyncProgress("File 1", 0, ProgressState.IN_PROGRESS),
                    UUID.randomUUID(),
                    WorkInfo.State.RUNNING
                ),
                createFileWorkInfo(
                    FileSyncProgress("File 2", 50, ProgressState.IN_PROGRESS),
                    UUID.randomUUID(),
                    WorkInfo.State.RUNNING
                ),
                createFileWorkInfo(
                    FileSyncProgress("File 3", 100, ProgressState.COMPLETED),
                    UUID.randomUUID(),
                    WorkInfo.State.SUCCEEDED
                ),
                createFileWorkInfo(
                    FileSyncProgress("File 4", 0, ProgressState.IN_PROGRESS, externalFile = true),
                    UUID.randomUUID(),
                    WorkInfo.State.RUNNING
                )
            )
        )

        courseProgressItemViewModel = createItemViewModel(uuid)
        assertEquals(WorkInfo.State.RUNNING, courseProgressItemViewModel.data.state)

        // We don't know the size of the external file yet, so we won't add this to the total size
        assertEquals("${1000000 + 1000 + 2000 + 3000} bytes", courseProgressItemViewModel.data.size)

        aggregateLiveData.postValue(
            listOf(
                createCourseWorkInfo(courseProgress, uuid, WorkInfo.State.SUCCEEDED),
                createFileWorkInfo(
                    FileSyncProgress("File 1", 100, ProgressState.COMPLETED),
                    UUID.randomUUID(),
                    WorkInfo.State.SUCCEEDED
                ),
                createFileWorkInfo(
                    FileSyncProgress("File 2", 100, ProgressState.COMPLETED),
                    UUID.randomUUID(),
                    WorkInfo.State.SUCCEEDED
                ),
                createFileWorkInfo(
                    FileSyncProgress("File 3", 100, ProgressState.COMPLETED),
                    UUID.randomUUID(),
                    WorkInfo.State.SUCCEEDED
                ),
                createFileWorkInfo(
                    FileSyncProgress("File 4", 100, ProgressState.COMPLETED, totalBytes = 4000, externalFile = true),
                    UUID.randomUUID(),
                    WorkInfo.State.SUCCEEDED
                )
            )
        )

        // We already know the size of the external file
        assertEquals("${1000000 + 1000 + 2000 + 3000 + 4000} bytes", courseProgressItemViewModel.data.size)
        assertEquals(WorkInfo.State.SUCCEEDED, courseProgressItemViewModel.data.state)
        assertFalse(courseProgressItemViewModel.data.failed)
    }

    private fun createItemViewModel(uuid: UUID): CourseProgressItemViewModel {
        return CourseProgressItemViewModel(
            data = CourseProgressViewData(
                courseName = "Course",
                courseId = 1L,
                workerId = uuid.toString(),
                files = null,
                additionalFiles = AdditionalFilesProgressItemViewModel(AdditionalFilesProgressViewData(uuid.toString()), workManager, context),
                size = "Queued"
            ),
            context = context,
            courseSyncProgressDao = courseSyncProgressDao,
            fileSyncProgressDao = fileSyncProgressDao
        )
    }
}