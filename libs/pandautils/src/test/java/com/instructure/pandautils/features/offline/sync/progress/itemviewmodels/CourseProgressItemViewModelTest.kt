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
import io.mockk.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

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
            progressState = ProgressState.IN_PROGRESS
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
            additionalFilesStarted = true,
            progressState = ProgressState.ERROR
        )

        val courseLiveData = MutableLiveData(courseProgress)
        every { courseSyncProgressDao.findByWorkerIdLiveData(uuid.toString()) } returns courseLiveData
        every { fileSyncProgressDao.findByCourseIdLiveData(1L) } returns MutableLiveData(emptyList())

        courseProgressItemViewModel = createItemViewModel(uuid)

        assertEquals(ProgressState.ERROR, courseProgressItemViewModel.data.state)
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
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 1",
                progress = 0,
                fileSize = 1000,
                progressState = ProgressState.IN_PROGRESS,
                fileId = 1L
            ),
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 2",
                progress = 0,
                fileSize = 2000,
                progressState = ProgressState.IN_PROGRESS,
                fileId = 2L
            ),
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 3",
                progress = 0,
                fileSize = 3000,
                progressState = ProgressState.IN_PROGRESS,
                fileId = 3L
            ),
        )

        val courseLiveData = MutableLiveData(courseProgress)
        val fileLiveData = MutableLiveData(fileProgresses)
        every { courseSyncProgressDao.findByWorkerIdLiveData(uuid.toString()) } returns courseLiveData
        every { fileSyncProgressDao.findByCourseIdLiveData(1L) } returns fileLiveData

        fileProgresses = listOf(
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 1",
                progress = 0,
                fileSize = 1000,
                progressState = ProgressState.IN_PROGRESS,
                fileId = 1L
            ),
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 2",
                progress = 50,
                fileSize = 2000,
                progressState = ProgressState.IN_PROGRESS,
                fileId = 2L
            ),
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 3",
                progress = 100,
                fileSize = 3000,
                progressState = ProgressState.COMPLETED,
                fileId = 3L
            ),
        )
        fileLiveData.postValue(fileProgresses)

        courseProgressItemViewModel = createItemViewModel(uuid)
        assertEquals(ProgressState.IN_PROGRESS, courseProgressItemViewModel.data.state)

        fileProgresses = listOf(
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 1",
                progress = 0,
                fileSize = 1000,
                progressState = ProgressState.IN_PROGRESS,
                fileId = 1L
            ),
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 2",
                progress = 50,
                fileSize = 2000,
                progressState = ProgressState.ERROR,
                fileId = 2L
            ),
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 3",
                progress = 100,
                fileSize = 3000,
                progressState = ProgressState.COMPLETED,
                fileId = 3L
            ),
        )
        fileLiveData.postValue(fileProgresses)

        assertEquals(ProgressState.ERROR, courseProgressItemViewModel.data.state)
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
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 1",
                progress = 100,
                fileSize = 1000,
                progressState = ProgressState.COMPLETED,
                fileId = 1L
            ),
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 2",
                progress = 100,
                fileSize = 2000,
                progressState = ProgressState.COMPLETED,
                fileId = 2L
            ),
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 3",
                progress = 100,
                fileSize = 3000,
                progressState = ProgressState.COMPLETED,
                fileId = 3L
            ),
        )

        val courseLiveData = MutableLiveData(courseProgress)
        val fileLiveData = MutableLiveData(fileProgresses)

        every { courseSyncProgressDao.findByWorkerIdLiveData(uuid.toString()) } returns courseLiveData
        every { fileSyncProgressDao.findByCourseIdLiveData(1L) } returns fileLiveData

        courseProgressItemViewModel = createItemViewModel(uuid)

        assertEquals(ProgressState.COMPLETED, courseProgressItemViewModel.data.state)
        assertFalse(courseProgressItemViewModel.data.failed)
    }

    @Test
    fun `Update total size with additional files`() {
        val uuid = UUID.randomUUID()
        val courseProgress = CourseSyncProgressEntity(
            1L,
            uuid.toString(),
            "Course",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.COMPLETED) },
            additionalFilesStarted = true,
            progressState = ProgressState.COMPLETED
        )

        val courseLiveData = MutableLiveData(courseProgress)
        every { courseSyncProgressDao.findByWorkerIdLiveData(uuid.toString()) } returns courseLiveData

        val files = listOf(
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 1",
                progress = 0,
                fileSize = 1000,
                progressState = ProgressState.IN_PROGRESS,
                fileId = 1L
            ),
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 2",
                progress = 50,
                fileSize = 2000,
                progressState = ProgressState.IN_PROGRESS,
                fileId = 2L
            ),
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 3",
                progress = 100,
                fileSize = 3000,
                additionalFile = true,
                progressState = ProgressState.COMPLETED,
                fileId = 3L
            ),
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 4",
                progress = 0,
                fileSize = 0,
                additionalFile = true,
                progressState = ProgressState.IN_PROGRESS,
                fileId = 4L
            ),
        )

        val filesLiveData = MutableLiveData(files)
        every { fileSyncProgressDao.findByCourseIdLiveData(1L) } returns filesLiveData

        courseProgressItemViewModel = createItemViewModel(uuid)
        assertEquals(ProgressState.IN_PROGRESS, courseProgressItemViewModel.data.state)

        // We don't know the size of the external file yet, so we won't add this to the total size
        assertEquals("${1000000 + 1000 + 2000 + 3000} bytes", courseProgressItemViewModel.data.size)

        filesLiveData.postValue(
            files.map {
                it.copy(
                    progress = 100,
                    progressState = ProgressState.COMPLETED,
                    fileSize = if (it.fileSize == 0L) 4000 else it.fileSize
                )
            }
        )

        // We already know the size of the external file
        assertEquals("${1000000 + 1000 + 2000 + 3000 + 4000} bytes", courseProgressItemViewModel.data.size)
        assertEquals(ProgressState.COMPLETED, courseProgressItemViewModel.data.state)
        assertFalse(courseProgressItemViewModel.data.failed)
    }

    private fun createItemViewModel(uuid: UUID): CourseProgressItemViewModel {
        return CourseProgressItemViewModel(
            data = CourseProgressViewData(
                courseName = "Course",
                courseId = 1L,
                workerId = uuid.toString(),
                files = null,
                additionalFiles = AdditionalFilesProgressItemViewModel(
                    data = AdditionalFilesProgressViewData(uuid.toString()),
                    context = context,
                    fileSyncProgressDao = fileSyncProgressDao,
                    courseSyncProgressDao = courseSyncProgressDao
                ),
                size = "Queued"
            ),
            context = context,
            courseSyncProgressDao = courseSyncProgressDao,
            fileSyncProgressDao = fileSyncProgressDao
        )
    }
}