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
import androidx.lifecycle.MutableLiveData
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.progress.AdditionalFilesProgressViewData
import com.instructure.pandautils.room.offline.daos.CourseSyncProgressDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.entities.CourseSyncProgressEntity
import com.instructure.pandautils.room.offline.entities.FileSyncProgressEntity
import io.mockk.*
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import com.instructure.testutils.ViewModelTestRule
import org.junit.Test
import java.util.*

class AdditionalFilesProgressItemViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

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

    @Test
    fun `Success if no files`() {
        val courseProgress = CourseSyncProgressEntity(
            1L,
            "Course",
            emptyMap(),
            progressState = ProgressState.COMPLETED,
            additionalFilesStarted = true
        )
        val courseLiveData = MutableLiveData(courseProgress)

        every { courseSyncProgressDao.findByCourseIdLiveData(1L) } returns courseLiveData
        every { fileSyncProgressDao.findAdditionalFilesByCourseIdLiveData(1L) } returns MutableLiveData(emptyList())

        itemViewModel = createItemViewModel(1L)

        assertEquals(ProgressState.COMPLETED, itemViewModel.data.state)
    }

    @Test
    fun `Add internal files sizes to total size when progress is starting`() {
        val additionalFileSyncData = listOf(
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 1",
                progress = 0,
                fileSize = 1000,
                additionalFile = true,
                progressState = ProgressState.IN_PROGRESS, fileId = 1L
            ),
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 2",
                progress = 0,
                fileSize = 2000,
                additionalFile = true,
                progressState = ProgressState.IN_PROGRESS, fileId = 2L
            ),
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 3",
                progress = 0,
                fileSize = 0,
                additionalFile = true,
                progressState = ProgressState.IN_PROGRESS, fileId = 3L
            )
        )
        val fileLiveData = MutableLiveData(additionalFileSyncData)

        val courseProgress =
            CourseSyncProgressEntity(1L, "Course", emptyMap(), additionalFilesStarted = true)
        val courseLiveData = MutableLiveData(courseProgress)

        every { courseSyncProgressDao.findByCourseIdLiveData(1L) } returns courseLiveData
        every { fileSyncProgressDao.findAdditionalFilesByCourseIdLiveData(1L) } returns fileLiveData

        itemViewModel = createItemViewModel(1L)

        assertEquals(ProgressState.IN_PROGRESS, itemViewModel.data.state)
        assertEquals("3000 bytes", itemViewModel.data.totalSize)
    }

    @Test
    fun `Update total file size for external files and progress`() {
        val additionalFileSyncData = listOf(
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 1",
                progress = 0,
                fileSize = 1000,
                additionalFile = true,
                progressState = ProgressState.IN_PROGRESS,
                fileId = 1L
            ),
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 2",
                progress = 0,
                fileSize = 2000,
                additionalFile = true,
                progressState = ProgressState.IN_PROGRESS,
                fileId = 2L
            ),
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 3",
                progress = 0,
                fileSize = 0,
                additionalFile = true,
                progressState = ProgressState.IN_PROGRESS,
                fileId = 3L
            ),
        )

        val fileLiveData = MutableLiveData(additionalFileSyncData)
        every { fileSyncProgressDao.findAdditionalFilesByCourseIdLiveData(1L) } returns fileLiveData

        val courseProgress = CourseSyncProgressEntity(
            1L,
            "Course",
            emptyMap(),
            additionalFilesStarted = true,
            progressState = ProgressState.IN_PROGRESS
        )
        val courseLiveData = MutableLiveData(courseProgress)
        every { courseSyncProgressDao.findByCourseIdLiveData(1L) } returns courseLiveData

        itemViewModel = createItemViewModel(1L)

        assertEquals(ProgressState.IN_PROGRESS, itemViewModel.data.state)
        assertEquals("3000 bytes", itemViewModel.data.totalSize)

        fileLiveData.postValue(additionalFileSyncData.map {
            it.copy(
                progress = 100,
                progressState = ProgressState.COMPLETED,
                fileSize = if (it.fileSize == 0L) 3000 else it.fileSize
            )
        })

        assertEquals(ProgressState.COMPLETED, itemViewModel.data.state)
        assertEquals("6000 bytes", itemViewModel.data.totalSize) // Already counted external files
    }

    @Test
    fun `Error state`() {
        val additionalFileSyncData = listOf(
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 1",
                progress = 0,
                fileSize = 1000,
                additionalFile = true,
                progressState = ProgressState.IN_PROGRESS,
                fileId = 1L
            ),
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 2",
                progress = 0,
                fileSize = 2000,
                additionalFile = true,
                progressState = ProgressState.IN_PROGRESS,
                fileId = 2L
            ),
            FileSyncProgressEntity(
                courseId = 1L,
                fileName = "File 3",
                progress = 0,
                fileSize = 0,
                additionalFile = true,
                progressState = ProgressState.ERROR, fileId = 3L
            ),
        )

        val fileLiveData = MutableLiveData(additionalFileSyncData)
        every { fileSyncProgressDao.findAdditionalFilesByCourseIdLiveData(1L) } returns fileLiveData

        val courseProgress = CourseSyncProgressEntity(
            1L,
            "Course",
            emptyMap(),
            additionalFilesStarted = true,
            progressState = ProgressState.IN_PROGRESS
        )
        val courseLiveData = MutableLiveData(courseProgress)
        every { courseSyncProgressDao.findByCourseIdLiveData(1L) } returns courseLiveData

        itemViewModel = createItemViewModel(1L)

        assertEquals(ProgressState.ERROR, itemViewModel.data.state)
    }

    private fun createItemViewModel(courseId: Long): AdditionalFilesProgressItemViewModel {
        return AdditionalFilesProgressItemViewModel(
            data = AdditionalFilesProgressViewData(courseId = courseId),
            context = context,
            fileSyncProgressDao = fileSyncProgressDao,
            courseSyncProgressDao = courseSyncProgressDao
        )
    }
}
