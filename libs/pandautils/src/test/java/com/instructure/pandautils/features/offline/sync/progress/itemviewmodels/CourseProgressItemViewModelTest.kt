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
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.features.offline.sync.CourseProgress
import com.instructure.pandautils.features.offline.sync.CourseSyncWorker
import com.instructure.pandautils.features.offline.sync.FileSyncData
import com.instructure.pandautils.features.offline.sync.FileSyncProgress
import com.instructure.pandautils.features.offline.sync.FileSyncWorker
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.TabSyncData
import com.instructure.pandautils.features.offline.sync.progress.AdditionalFilesProgressViewData
import com.instructure.pandautils.features.offline.sync.progress.CourseProgressViewData
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.utils.toJson
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

    private val workManager: WorkManager = mockk(relaxed = true)
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

        val courseLiveData = MutableLiveData(
            WorkInfo(
                uuid,
                WorkInfo.State.RUNNING,
                workDataOf(),
                listOf(CourseSyncWorker.TAG),
                workDataOf(),
                0,
                0
            )
        )
        every { workManager.getWorkInfoByIdLiveData(uuid) } returns courseLiveData

        courseProgressItemViewModel = createItemViewModel(uuid)

        assertEquals("Queued", courseProgressItemViewModel.data.size)
        assertEquals(WorkInfo.State.ENQUEUED, courseProgressItemViewModel.data.state)

        val courseProgress = CourseProgress(
            1L,
            "Course",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
            fileSyncData = emptyList()
        )

        courseLiveData.postValue(createCourseWorkInfo(courseProgress, uuid, WorkInfo.State.RUNNING))

        assertEquals("1000000 bytes", courseProgressItemViewModel.data.size)
        courseProgress.tabs.values.forEachIndexed { index, tabSyncData ->
            assertEquals(tabSyncData.state, courseProgressItemViewModel.data.tabs?.get(index)?.data?.state)
            assertEquals(tabSyncData.tabName, courseProgressItemViewModel.data.tabs?.get(index)?.data?.tabName)
        }
    }

    @Test
    fun `Failed course sync`() {
        val uuid = UUID.randomUUID()
        val courseProgress = CourseProgress(
            1L,
            "Course",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.ERROR) },
            emptyList()
        )

        val courseLiveData = MutableLiveData(createCourseWorkInfo(courseProgress, uuid, WorkInfo.State.FAILED))
        every { workManager.getWorkInfoByIdLiveData(uuid) } returns courseLiveData
        every { workManager.getWorkInfosLiveData(any()) } returns MutableLiveData(
            listOf(
                createCourseWorkInfo(
                    courseProgress,
                    uuid,
                    WorkInfo.State.FAILED
                )
            )
        )

        courseProgressItemViewModel = createItemViewModel(uuid)

        assertEquals(WorkInfo.State.FAILED, courseProgressItemViewModel.data.state)
    }

    @Test
    fun `Failed file sync`() {
        val uuid = UUID.randomUUID()
        val courseProgress = CourseProgress(
            1L,
            "Course",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
            listOf(
                FileSyncData(UUID.randomUUID().toString(), "File 1", 1000),
                FileSyncData(UUID.randomUUID().toString(), "File 2", 2000),
                FileSyncData(UUID.randomUUID().toString(), "File 3", 3000)
            )
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
                )
            )
        )

        courseProgressItemViewModel = createItemViewModel(uuid)
        assertEquals(WorkInfo.State.RUNNING, courseProgressItemViewModel.data.state)

        aggregateLiveData.postValue(
            listOf(
                createCourseWorkInfo(courseProgress, uuid, WorkInfo.State.RUNNING),
                createFileWorkInfo(
                    FileSyncProgress("File 1", 0, ProgressState.IN_PROGRESS),
                    UUID.randomUUID(),
                    WorkInfo.State.RUNNING
                ),
                createFileWorkInfo(
                    FileSyncProgress("File 2", 50, ProgressState.ERROR),
                    UUID.randomUUID(),
                    WorkInfo.State.FAILED
                ),
                createFileWorkInfo(
                    FileSyncProgress("File 3", 100, ProgressState.COMPLETED),
                    UUID.randomUUID(),
                    WorkInfo.State.SUCCEEDED
                )
            )
        )

        assertEquals(WorkInfo.State.FAILED, courseProgressItemViewModel.data.state)
        assert(courseProgressItemViewModel.data.failed)
    }

    @Test
    fun `Sync success`() {
        val uuid = UUID.randomUUID()
        val courseProgress = CourseProgress(
            1L,
            "Course",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
            listOf(
                FileSyncData(UUID.randomUUID().toString(), "File 1", 1000),
                FileSyncData(UUID.randomUUID().toString(), "File 2", 2000),
                FileSyncData(UUID.randomUUID().toString(), "File 3", 3000)
            )
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
                )
            )
        )

        courseProgressItemViewModel = createItemViewModel(uuid)
        assertEquals(WorkInfo.State.RUNNING, courseProgressItemViewModel.data.state)

        aggregateLiveData.postValue(
            listOf(
                createCourseWorkInfo(courseProgress, uuid, WorkInfo.State.SUCCEEDED),
                createFileWorkInfo(
                    FileSyncProgress("File 1", 0, ProgressState.COMPLETED),
                    UUID.randomUUID(),
                    WorkInfo.State.SUCCEEDED
                ),
                createFileWorkInfo(
                    FileSyncProgress("File 2", 50, ProgressState.COMPLETED),
                    UUID.randomUUID(),
                    WorkInfo.State.SUCCEEDED
                ),
                createFileWorkInfo(
                    FileSyncProgress("File 3", 100, ProgressState.COMPLETED),
                    UUID.randomUUID(),
                    WorkInfo.State.SUCCEEDED
                )
            )
        )

        assertEquals(WorkInfo.State.SUCCEEDED, courseProgressItemViewModel.data.state)
        assertFalse(courseProgressItemViewModel.data.failed)
    }

    private fun createItemViewModel(uuid: UUID): CourseProgressItemViewModel {
        return CourseProgressItemViewModel(
            data = CourseProgressViewData(
                courseName = "Course",
                workerId = uuid.toString(),
                files = null,
                additionalFiles = AdditionalFilesProgressItemViewModel(AdditionalFilesProgressViewData(uuid.toString()), workManager, context),
                size = "Queued"
            ),
            workManager = workManager,
            context = context
        )
    }

    private fun createCourseWorkInfo(courseProgress: CourseProgress, uuid: UUID, state: WorkInfo.State): WorkInfo {
        return WorkInfo(
            uuid,
            state,
            if (state.isFinished) workDataOf(CourseSyncWorker.OUTPUT to courseProgress.toJson()) else workDataOf(),
            listOf(CourseSyncWorker.TAG),
            workDataOf(
                CourseSyncWorker.COURSE_PROGRESS to courseProgress.toJson()
            ),
            0,
            0
        )
    }

    private fun createFileWorkInfo(
        fileSyncProgress: FileSyncProgress,
        uuid: UUID,
        state: WorkInfo.State
    ): WorkInfo {
        return WorkInfo(
            uuid,
            state,
            if (state.isFinished) workDataOf(FileSyncWorker.OUTPUT to fileSyncProgress.toJson()) else workDataOf(),
            listOf(FileSyncWorker.TAG),
            workDataOf(
                FileSyncWorker.PROGRESS to fileSyncProgress.toJson()
            ),
            0,
            0
        )
    }
}