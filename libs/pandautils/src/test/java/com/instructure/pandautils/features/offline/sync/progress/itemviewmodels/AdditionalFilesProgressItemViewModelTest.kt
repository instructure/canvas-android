/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import com.instructure.pandautils.features.offline.sync.progress.AdditionalFilesProgressViewData
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

class AdditionalFilesProgressItemViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val workManager: WorkManager = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    private lateinit var itemViewModel: AdditionalFilesProgressItemViewModel

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
        val courseProgress = CourseProgress(1L, "Course", emptyMap(), additionalFileSyncData = emptyList())
        val courseLiveData = MutableLiveData(createCourseWorkInfo(courseProgress, courseUUID, WorkInfo.State.SUCCEEDED))
        every { workManager.getWorkInfoByIdLiveData(courseUUID) } returns courseLiveData

        itemViewModel = createItemViewModel(courseUUID)

        assertEquals(ProgressState.COMPLETED, itemViewModel.data.state)
    }

    @Test
    fun `Add internal files sizes to total size when progress is starting`() {
        val courseUUID = UUID.randomUUID()
        val additionalFileSyncData = listOf(
            FileSyncData(UUID.randomUUID().toString(), "File 1", 1000),
            FileSyncData(UUID.randomUUID().toString(), "File 2", 2000),
            FileSyncData(UUID.randomUUID().toString(), "File 3", 0)
        )
        val courseProgress = CourseProgress(1L, "Course", emptyMap(), additionalFileSyncData = additionalFileSyncData)
        val courseLiveData = MutableLiveData(createCourseWorkInfo(courseProgress, courseUUID, WorkInfo.State.RUNNING))
        every { workManager.getWorkInfoByIdLiveData(courseUUID) } returns courseLiveData

        itemViewModel = createItemViewModel(courseUUID)

        assertEquals(ProgressState.IN_PROGRESS, itemViewModel.data.state)
        assertEquals("3000 bytes", itemViewModel.data.totalSize)
    }

    @Test
    fun `Update total file size for external files and progress`() {
        val courseUUID = UUID.randomUUID()
        val file1UUID = UUID.randomUUID()
        val file2UUID = UUID.randomUUID()
        val file3UUID = UUID.randomUUID()
        val additionalFileSyncData = listOf(
            FileSyncData(file1UUID.toString(), "File 1", 1000),
            FileSyncData(file2UUID.toString(), "File 2", 2000),
            FileSyncData(file3UUID.toString(), "File 3", 0)
        )
        val courseProgress = CourseProgress(1L, "Course", emptyMap(), additionalFileSyncData = additionalFileSyncData)
        val courseLiveData = MutableLiveData(createCourseWorkInfo(courseProgress, courseUUID, WorkInfo.State.RUNNING))
        every { workManager.getWorkInfoByIdLiveData(courseUUID) } returns courseLiveData

        var fileWorkInfos = listOf(
            createFileWorkInfo(
                FileSyncProgress("File 1", 100, ProgressState.COMPLETED),
                file1UUID,
                WorkInfo.State.SUCCEEDED
            ),
            createFileWorkInfo(
                FileSyncProgress("File 2", 50, ProgressState.IN_PROGRESS),
                file2UUID,
                WorkInfo.State.RUNNING
            ),
            createFileWorkInfo(
                FileSyncProgress("File 3", 0, ProgressState.IN_PROGRESS, totalBytes = 0, externalFile = true),
                file3UUID,
                WorkInfo.State.RUNNING
            )
        )

        val fileLiveData = MutableLiveData(fileWorkInfos)
        every { workManager.getWorkInfosLiveData(any()) } returns fileLiveData

        itemViewModel = createItemViewModel(courseUUID)

        assertEquals(ProgressState.IN_PROGRESS, itemViewModel.data.state)
        assertEquals("3000 bytes", itemViewModel.data.totalSize)

        fileWorkInfos = listOf(
            createFileWorkInfo(
                FileSyncProgress("File 1", 100, ProgressState.COMPLETED),
                file1UUID,
                WorkInfo.State.SUCCEEDED
            ),
            createFileWorkInfo(
                FileSyncProgress("File 2", 100, ProgressState.COMPLETED),
                file2UUID,
                WorkInfo.State.SUCCEEDED
            ),
            createFileWorkInfo(
                FileSyncProgress("File 3", 100, ProgressState.COMPLETED, totalBytes = 3000, externalFile = true),
                file3UUID,
                WorkInfo.State.SUCCEEDED
            )
        )
        fileLiveData.postValue(fileWorkInfos)

        assertEquals(ProgressState.COMPLETED, itemViewModel.data.state)
        assertEquals("6000 bytes", itemViewModel.data.totalSize) // Already counted external files
    }

    private fun createItemViewModel(uuid: UUID): AdditionalFilesProgressItemViewModel {
        return AdditionalFilesProgressItemViewModel(
            AdditionalFilesProgressViewData(courseWorkerId = uuid.toString()),
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