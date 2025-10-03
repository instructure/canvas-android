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
import androidx.lifecycle.MutableLiveData
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.progress.FileTabProgressViewData
import com.instructure.pandautils.room.offline.daos.CourseSyncProgressDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.entities.CourseSyncProgressEntity
import com.instructure.pandautils.room.offline.entities.FileSyncProgressEntity
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import com.instructure.testutils.ViewModelTestRule
import org.junit.Test

class FilesTabProgressItemViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val courseSyncProgressDao: CourseSyncProgressDao = mockk(relaxed = true)
    private val fileSyncProgressDao: FileSyncProgressDao = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    private lateinit var filesTabProgressItemViewModel: FilesTabProgressItemViewModel

    @Before
    fun setup() {
        mockkObject(NumberHelper)
        val captor = slot<Long>()
        every { NumberHelper.readableFileSize(any<Context>(), capture(captor)) } answers { "${captor.captured} bytes" }
    }

    @Test
    fun `Success if no files`() {
        val courseProgress =
            CourseSyncProgressEntity(
                1L,
                "Course",
                emptyMap(),
                additionalFilesStarted = true,
                progressState = ProgressState.COMPLETED
            )
        val courseLiveData = MutableLiveData(courseProgress)

        every { courseSyncProgressDao.findByCourseIdLiveData(1L) } returns courseLiveData
        every { fileSyncProgressDao.findCourseFilesByCourseIdLiveData(1L) } returns MutableLiveData(emptyList())

        filesTabProgressItemViewModel = createItemViewModel(1L)

        assertEquals(ProgressState.COMPLETED, filesTabProgressItemViewModel.data.state)
    }

    @Test
    fun `Create file items`() {
        val fileSyncProgresses = listOf(
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

        val courseProgress = CourseSyncProgressEntity(
            1L,
            "Course",
            emptyMap(),
            additionalFilesStarted = true,
            progressState = ProgressState.IN_PROGRESS
        )
        val courseLiveData = MutableLiveData(courseProgress)
        val fileLiveData = MutableLiveData(fileSyncProgresses)

        every { courseSyncProgressDao.findByCourseIdLiveData(1L) } returns courseLiveData
        every { fileSyncProgressDao.findCourseFilesByCourseIdLiveData(1L) } returns fileLiveData

        filesTabProgressItemViewModel = createItemViewModel(1L)

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
        var fileSyncData = listOf(
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
        val courseProgress = CourseSyncProgressEntity(
            1L,
            "Course",
            emptyMap(),
            progressState = ProgressState.COMPLETED
        )
        val courseLiveData = MutableLiveData(courseProgress)
        val fileLiveData = MutableLiveData(fileSyncData)

        every { courseSyncProgressDao.findByCourseIdLiveData(1L) } returns courseLiveData
        every { fileSyncProgressDao.findCourseFilesByCourseIdLiveData(1L) } returns fileLiveData

        filesTabProgressItemViewModel = createItemViewModel(1L)

        assertEquals(ProgressState.IN_PROGRESS, filesTabProgressItemViewModel.data.state)
        assertEquals(50, filesTabProgressItemViewModel.data.progress)

        fileSyncData = listOf(
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 1",
                progress = 50,
                fileSize = 1000,
                progressState = ProgressState.IN_PROGRESS,
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
        fileLiveData.postValue(fileSyncData)

        assertEquals(ProgressState.IN_PROGRESS, filesTabProgressItemViewModel.data.state)
        assertEquals(83, filesTabProgressItemViewModel.data.progress)

        fileSyncData = listOf(
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
        fileLiveData.postValue(fileSyncData)

        assertEquals(ProgressState.COMPLETED, filesTabProgressItemViewModel.data.state)
        assertEquals(100, filesTabProgressItemViewModel.data.progress)
    }

    private fun createItemViewModel(courseId: Long): FilesTabProgressItemViewModel {
        return FilesTabProgressItemViewModel(
            FileTabProgressViewData(
                courseId,
                emptyList(),
            ),
            context,
            courseSyncProgressDao,
            fileSyncProgressDao
        )
    }
}
