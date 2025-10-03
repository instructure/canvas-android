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

import androidx.lifecycle.MutableLiveData
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.progress.FileSyncProgressViewData
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.entities.FileSyncProgressEntity
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import com.instructure.testutils.ViewModelTestRule
import org.junit.Test
import java.util.*

class FileSyncProgressItemViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val fileSyncProgressDao: FileSyncProgressDao = mockk(relaxed = true)

    private lateinit var fileSyncProgressItemViewModel: FileSyncProgressItemViewModel

    @Test
    fun `Progress update`() {
        var fileProgress = createFileProgress(1L, 1L, 0, ProgressState.IN_PROGRESS)
        val fileLiveData = MutableLiveData(fileProgress)

        every { fileSyncProgressDao.findByFileIdLiveData(1L) } returns fileLiveData

        fileSyncProgressItemViewModel = createItemViewModel(1L)

        assertEquals(0, fileSyncProgressItemViewModel.data.progress)
        assertEquals(ProgressState.IN_PROGRESS, fileSyncProgressItemViewModel.data.state)

        fileProgress = createFileProgress(1L, 1L, 50, ProgressState.IN_PROGRESS)
        fileLiveData.postValue(fileProgress)

        assertEquals(50, fileSyncProgressItemViewModel.data.progress)
        assertEquals(ProgressState.IN_PROGRESS, fileSyncProgressItemViewModel.data.state)

        fileProgress = createFileProgress(1L, 1L, 100, ProgressState.COMPLETED)
        fileLiveData.postValue(fileProgress)

        assertEquals(100, fileSyncProgressItemViewModel.data.progress)
        assertEquals(ProgressState.COMPLETED, fileSyncProgressItemViewModel.data.state)
    }

    private fun createItemViewModel(id: Long): FileSyncProgressItemViewModel {
        return FileSyncProgressItemViewModel(
            FileSyncProgressViewData(
                fileName = "File",
                fileSize = "1.0 MB",
                fileId = id,
                progress = 0,
                state = ProgressState.IN_PROGRESS
            ),
            fileSyncProgressDao = fileSyncProgressDao
        )
    }

    private fun createFileProgress(
        fileId: Long,
        courseId: Long,
        progress: Int,
        state: ProgressState
    ): FileSyncProgressEntity {
        return FileSyncProgressEntity(
            courseId = courseId,
            fileName = "File",
            progress = progress,
            fileSize = 1000,
            progressState = state,
            fileId = fileId
        )
    }
}
