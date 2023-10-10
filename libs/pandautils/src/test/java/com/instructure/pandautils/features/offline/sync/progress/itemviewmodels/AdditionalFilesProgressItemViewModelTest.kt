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
import com.instructure.pandautils.features.offline.sync.CourseSyncWorker
import com.instructure.pandautils.features.offline.sync.FileSyncWorker
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.progress.AdditionalFilesProgressViewData
import com.instructure.pandautils.room.offline.daos.CourseSyncProgressDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.entities.CourseSyncProgressEntity
import com.instructure.pandautils.room.offline.entities.FileSyncProgressEntity
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

    private val courseSyncProgressDao: CourseSyncProgressDao = mockk(relaxed = true)
    private val fileSyncProgressDao: FileSyncProgressDao = mockk(relaxed = true)
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
        val courseProgress = CourseSyncProgressEntity(1L, courseUUID.toString(), "Course", emptyMap(), progressState = ProgressState.COMPLETED, additionalFilesStarted = true)
        val courseLiveData = MutableLiveData(courseProgress)

        every { courseSyncProgressDao.findByWorkerIdLiveData(courseUUID.toString()) } returns courseLiveData
        every { fileSyncProgressDao.findAdditionalFilesByCourseIdLiveData(1L) } returns MutableLiveData(emptyList())

        itemViewModel = createItemViewModel(courseUUID)

        assertEquals(ProgressState.COMPLETED, itemViewModel.data.state)
    }

    @Test
    fun `Add internal files sizes to total size when progress is starting`() {
        val courseUUID = UUID.randomUUID()
        val additionalFileSyncData = listOf(
            FileSyncProgressEntity(UUID.randomUUID().toString(), 1L, "File 1", 0, 1000, true, false, ProgressState.IN_PROGRESS),
            FileSyncProgressEntity(UUID.randomUUID().toString(), 1L, "File 2", 0, 2000, true, false, ProgressState.IN_PROGRESS),
            FileSyncProgressEntity(UUID.randomUUID().toString(), 1L, "File 3", 0, 0, true, true, ProgressState.IN_PROGRESS)
        )
        val fileLiveData = MutableLiveData(additionalFileSyncData)

        val courseProgress = CourseSyncProgressEntity(1L, courseUUID.toString(), "Course", emptyMap(), additionalFilesStarted = true)
        val courseLiveData = MutableLiveData(courseProgress)

        every { courseSyncProgressDao.findByWorkerIdLiveData(courseUUID.toString()) } returns courseLiveData
        every { fileSyncProgressDao.findAdditionalFilesByCourseIdLiveData(1L) } returns fileLiveData

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
            FileSyncProgressEntity(file1UUID.toString(), 1L, "File 1", 0, 1000, true, false, ProgressState.IN_PROGRESS),
            FileSyncProgressEntity(file2UUID.toString(), 1L, "File 2", 0, 2000, true, false, ProgressState.IN_PROGRESS),
            FileSyncProgressEntity(file3UUID.toString(), 1L, "File 3", 0, 0, true, true, ProgressState.IN_PROGRESS),
        )

        val fileLiveData = MutableLiveData(additionalFileSyncData)
        every { fileSyncProgressDao.findAdditionalFilesByCourseIdLiveData(1L) } returns fileLiveData

        val courseProgress = CourseSyncProgressEntity(1L, courseUUID.toString(), "Course", emptyMap(), additionalFilesStarted = true, progressState = ProgressState.IN_PROGRESS)
        val courseLiveData = MutableLiveData(courseProgress)
        every { courseSyncProgressDao.findByWorkerIdLiveData(courseUUID.toString()) } returns courseLiveData

        itemViewModel = createItemViewModel(courseUUID)

        assertEquals(ProgressState.IN_PROGRESS, itemViewModel.data.state)
        assertEquals("3000 bytes", itemViewModel.data.totalSize)


        fileLiveData.postValue(additionalFileSyncData.map { it.copy(
            progress = 100,
            progressState = ProgressState.COMPLETED,
            fileSize = if (it.fileSize == 0L) 3000 else it.fileSize
        ) })

        assertEquals(ProgressState.COMPLETED, itemViewModel.data.state)
        assertEquals("6000 bytes", itemViewModel.data.totalSize) // Already counted external files
    }

    private fun createItemViewModel(uuid: UUID): AdditionalFilesProgressItemViewModel {
        return AdditionalFilesProgressItemViewModel(
            data = AdditionalFilesProgressViewData(courseWorkerId = uuid.toString()),
            context = context,
            fileSyncProgressDao = fileSyncProgressDao,
            courseSyncProgressDao = courseSyncProgressDao
        )
    }
}