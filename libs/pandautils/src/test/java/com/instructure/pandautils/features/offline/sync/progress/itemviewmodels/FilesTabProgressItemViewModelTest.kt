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
import com.instructure.pandautils.features.offline.sync.progress.FileTabProgressViewData
import com.instructure.pandautils.room.offline.daos.CourseSyncProgressDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.entities.CourseSyncProgressEntity
import com.instructure.pandautils.room.offline.entities.FileSyncProgressEntity
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

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Success if no files`() {
        val courseUUID = UUID.randomUUID()
        val courseProgress =
            CourseSyncProgressEntity(
                1L,
                courseUUID.toString(),
                "Course",
                emptyMap(),
                additionalFilesStarted = true,
                progressState = ProgressState.COMPLETED
            )
        val courseLiveData = MutableLiveData(courseProgress)

        every { courseSyncProgressDao.findByWorkerIdLiveData(courseUUID.toString()) } returns courseLiveData
        every { fileSyncProgressDao.findCourseFilesByCourseIdLiveData(1L) } returns MutableLiveData(emptyList())

        filesTabProgressItemViewModel = createItemViewModel(courseUUID)

        assertEquals(ProgressState.COMPLETED, filesTabProgressItemViewModel.data.state)
    }

    @Test
    fun `Create file items`() {
        val courseUUID = UUID.randomUUID()
        val fileSyncProgresses = listOf(
            FileSyncProgressEntity(
                UUID.randomUUID().toString(),
                1L,
                "File 1",
                0,
                1000,
                progressState = ProgressState.IN_PROGRESS
            ),
            FileSyncProgressEntity(
                UUID.randomUUID().toString(),
                1L,
                "File 2",
                0,
                2000,
                progressState = ProgressState.IN_PROGRESS
            ),
            FileSyncProgressEntity(
                UUID.randomUUID().toString(),
                1L,
                "File 3",
                0,
                3000,
                progressState = ProgressState.IN_PROGRESS
            ),
        )

        val courseProgress = CourseSyncProgressEntity(
            1L,
            courseUUID.toString(),
            "Course",
            emptyMap(),
            additionalFilesStarted = true,
            progressState = ProgressState.IN_PROGRESS
        )
        val courseLiveData = MutableLiveData(courseProgress)
        val fileLiveData = MutableLiveData(fileSyncProgresses)

        every { courseSyncProgressDao.findByWorkerIdLiveData(courseUUID.toString()) } returns courseLiveData
        every { fileSyncProgressDao.findCourseFilesByCourseIdLiveData(1L) } returns fileLiveData

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
        var fileSyncData = listOf(
            FileSyncProgressEntity(
                UUID.randomUUID().toString(),
                1L,
                "File 1",
                0,
                1000,
                progressState = ProgressState.IN_PROGRESS
            ),
            FileSyncProgressEntity(
                UUID.randomUUID().toString(),
                1L,
                "File 2",
                50,
                2000,
                progressState = ProgressState.IN_PROGRESS
            ),
            FileSyncProgressEntity(
                UUID.randomUUID().toString(),
                1L,
                "File 3",
                100,
                3000,
                progressState = ProgressState.COMPLETED
            ),
        )
        val courseProgress = CourseSyncProgressEntity(
            1L,
            courseUUID.toString(),
            "Course",
            emptyMap(),
            progressState = ProgressState.COMPLETED
        )
        val courseLiveData = MutableLiveData(courseProgress)
        val fileLiveData = MutableLiveData(fileSyncData)

        every { courseSyncProgressDao.findByWorkerIdLiveData(courseUUID.toString()) } returns courseLiveData
        every { fileSyncProgressDao.findCourseFilesByCourseIdLiveData(1L) } returns fileLiveData

        filesTabProgressItemViewModel = createItemViewModel(courseUUID)

        assertEquals(ProgressState.IN_PROGRESS, filesTabProgressItemViewModel.data.state)
        assertEquals(50, filesTabProgressItemViewModel.data.progress)

        fileSyncData = listOf(
            FileSyncProgressEntity(
                UUID.randomUUID().toString(),
                1L,
                "File 1",
                50,
                1000,
                progressState = ProgressState.IN_PROGRESS
            ),
            FileSyncProgressEntity(
                UUID.randomUUID().toString(),
                1L,
                "File 2",
                100,
                2000,
                progressState = ProgressState.COMPLETED
            ),
            FileSyncProgressEntity(
                UUID.randomUUID().toString(),
                1L,
                "File 3",
                100,
                3000,
                progressState = ProgressState.COMPLETED
            ),
        )
        fileLiveData.postValue(fileSyncData)

        assertEquals(ProgressState.IN_PROGRESS, filesTabProgressItemViewModel.data.state)
        assertEquals(83, filesTabProgressItemViewModel.data.progress)

        fileSyncData = listOf(
            FileSyncProgressEntity(
                UUID.randomUUID().toString(),
                1L,
                "File 1",
                100,
                1000,
                progressState = ProgressState.COMPLETED
            ),
            FileSyncProgressEntity(
                UUID.randomUUID().toString(),
                1L,
                "File 2",
                100,
                2000,
                progressState = ProgressState.COMPLETED
            ),
            FileSyncProgressEntity(
                UUID.randomUUID().toString(),
                1L,
                "File 3",
                100,
                3000,
                progressState = ProgressState.COMPLETED
            ),
        )
        fileLiveData.postValue(fileSyncData)

        assertEquals(ProgressState.COMPLETED, filesTabProgressItemViewModel.data.state)
        assertEquals(100, filesTabProgressItemViewModel.data.progress)
    }


    private fun createItemViewModel(uuid: UUID): FilesTabProgressItemViewModel {
        return FilesTabProgressItemViewModel(
            FileTabProgressViewData(
                courseWorkerId = uuid.toString(),
                emptyList(),
            ),
            context,
            courseSyncProgressDao,
            fileSyncProgressDao
        )
    }
}