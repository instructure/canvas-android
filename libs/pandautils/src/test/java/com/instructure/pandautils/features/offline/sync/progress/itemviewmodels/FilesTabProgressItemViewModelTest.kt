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
import com.instructure.pandautils.features.offline.sync.progress.FileTabProgressViewData
import com.instructure.pandautils.utils.toJson
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class FilesTabProgressItemViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val workManager: WorkManager = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    private lateinit var filesTabProgressItemViewModel: FilesTabProgressItemViewModel

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
    fun `Success if no files`() {
        val courseUUID = UUID.randomUUID()
        val courseProgress = CourseProgress(1L, "Course", emptyMap(), emptyList())
        val courseLiveData = MutableLiveData(createCourseWorkInfo(courseProgress, courseUUID, WorkInfo.State.SUCCEEDED))
        every { workManager.getWorkInfoByIdLiveData(courseUUID) } returns courseLiveData

        filesTabProgressItemViewModel = createItemViewModel(courseUUID)

        assertEquals(ProgressState.COMPLETED, filesTabProgressItemViewModel.data.state)
    }

    @Test
    fun `Create file items`() {
        val courseUUID = UUID.randomUUID()
        val fileSyncData = listOf(
            FileSyncData(UUID.randomUUID().toString(), "File 1", 1000),
            FileSyncData(UUID.randomUUID().toString(), "File 2", 2000),
            FileSyncData(UUID.randomUUID().toString(), "File 3", 3000)
        )
        val courseProgress = CourseProgress(1L, "Course", emptyMap(), fileSyncData)
        val courseLiveData = MutableLiveData(createCourseWorkInfo(courseProgress, courseUUID, WorkInfo.State.RUNNING))
        every { workManager.getWorkInfoByIdLiveData(courseUUID) } returns courseLiveData

        filesTabProgressItemViewModel = createItemViewModel(courseUUID)

        assertEquals(ProgressState.IN_PROGRESS, filesTabProgressItemViewModel.data.state)
        assertEquals(3, filesTabProgressItemViewModel.data.items.size)
        assertEquals("6000 bytes", filesTabProgressItemViewModel.data.totalSize)
        assertEquals("File 1", filesTabProgressItemViewModel.data.items[0].data.fileName)
        assertEquals("File 2", filesTabProgressItemViewModel.data.items[1].data.fileName)
        assertEquals("File 3", filesTabProgressItemViewModel.data.items[2].data.fileName)
        assert(filesTabProgressItemViewModel.data.toggleable)
    }


    @Test
    fun `Progress updates`() {
        val courseUUID = UUID.randomUUID()
        val fileSyncData = listOf(
            FileSyncData(UUID.randomUUID().toString(), "File 1", 1000),
            FileSyncData(UUID.randomUUID().toString(), "File 2", 2000),
            FileSyncData(UUID.randomUUID().toString(), "File 3", 3000)
        )
        val courseProgress = CourseProgress(1L, "Course", emptyMap(), fileSyncData)
        val courseLiveData = MutableLiveData(createCourseWorkInfo(courseProgress, courseUUID, WorkInfo.State.RUNNING))
        every { workManager.getWorkInfoByIdLiveData(courseUUID) } returns courseLiveData

        var fileWorkInfos = listOf(
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
        val fileLiveData = MutableLiveData(fileWorkInfos)
        every { workManager.getWorkInfosLiveData(any()) } returns fileLiveData

        filesTabProgressItemViewModel = createItemViewModel(courseUUID)

        assertEquals(ProgressState.IN_PROGRESS, filesTabProgressItemViewModel.data.state)
        assertEquals(50, filesTabProgressItemViewModel.data.progress)

        fileWorkInfos = listOf(
            createFileWorkInfo(
                FileSyncProgress("File 1", 50, ProgressState.IN_PROGRESS),
                UUID.randomUUID(),
                WorkInfo.State.RUNNING
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
            )
        )
        fileLiveData.postValue(fileWorkInfos)

        assertEquals(ProgressState.IN_PROGRESS, filesTabProgressItemViewModel.data.state)
        assertEquals(83, filesTabProgressItemViewModel.data.progress)

        fileWorkInfos = listOf(
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
            )
        )
        fileLiveData.postValue(fileWorkInfos)

        assertEquals(ProgressState.COMPLETED, filesTabProgressItemViewModel.data.state)
        assertEquals(100, filesTabProgressItemViewModel.data.progress)
    }


    private fun createItemViewModel(uuid: UUID): FilesTabProgressItemViewModel {
        return FilesTabProgressItemViewModel(
            FileTabProgressViewData(
                courseWorkerId = uuid.toString(),
                emptyList(),
            ),
            workManager,
            context
        )
    }

    private fun createCourseWorkInfo(courseProgress: CourseProgress, uuid: UUID, state: WorkInfo.State): WorkInfo {
        return WorkInfo(
            uuid,
            state,
            if (state == WorkInfo.State.SUCCEEDED) workDataOf(CourseSyncWorker.OUTPUT to courseProgress.toJson()) else workDataOf(),
            listOf(CourseSyncWorker.TAG),
            workDataOf(
                CourseSyncWorker.COURSE_PROGRESS to courseProgress.toJson()
            ),
            0,
            0
        )
    }

    private fun createFileWorkInfo(fileSyncProgress: FileSyncProgress, uuid: UUID, state: WorkInfo.State): WorkInfo {
        return WorkInfo(
            uuid,
            state,
            if (state == WorkInfo.State.SUCCEEDED) workDataOf(FileSyncWorker.OUTPUT to fileSyncProgress.toJson()) else workDataOf(),
            listOf(FileSyncWorker.TAG),
            workDataOf(
                FileSyncWorker.PROGRESS to fileSyncProgress.toJson()
            ),
            0,
            0
        )
    }
}