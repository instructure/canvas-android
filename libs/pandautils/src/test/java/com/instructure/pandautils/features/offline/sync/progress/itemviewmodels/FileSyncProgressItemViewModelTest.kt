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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.progress.FileSyncProgressViewData
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.entities.FileSyncProgressEntity
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class FileSyncProgressItemViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val fileSyncProgressDao: FileSyncProgressDao = mockk(relaxed = true)

    private lateinit var fileSyncProgressItemViewModel: FileSyncProgressItemViewModel

    @Test
    fun `Progress update`() {
        val uuid = UUID.randomUUID()
        var fileProgress = createFileProgress(uuid.toString(), 1L, 0, ProgressState.IN_PROGRESS)
        val fileLiveData = MutableLiveData(fileProgress)

        every { fileSyncProgressDao.findByWorkerIdLiveData(uuid.toString()) } returns fileLiveData

        fileSyncProgressItemViewModel = createItemViewModel(uuid)

        assertEquals(0, fileSyncProgressItemViewModel.data.progress)
        assertEquals(ProgressState.IN_PROGRESS, fileSyncProgressItemViewModel.data.state)

        fileProgress = createFileProgress(uuid.toString(), 1L, 50, ProgressState.IN_PROGRESS)
        fileLiveData.postValue(fileProgress)

        assertEquals(50, fileSyncProgressItemViewModel.data.progress)
        assertEquals(ProgressState.IN_PROGRESS, fileSyncProgressItemViewModel.data.state)

        fileProgress = createFileProgress(uuid.toString(), 1L, 100, ProgressState.COMPLETED)
        fileLiveData.postValue(fileProgress)

        assertEquals(100, fileSyncProgressItemViewModel.data.progress)
        assertEquals(ProgressState.COMPLETED, fileSyncProgressItemViewModel.data.state)
    }

    private fun createItemViewModel(uuid: UUID): FileSyncProgressItemViewModel {
        return FileSyncProgressItemViewModel(
            FileSyncProgressViewData(
                fileName = "File",
                fileSize = "1.0 MB",
                workerId = uuid.toString(),
                progress = 0,
                state = ProgressState.IN_PROGRESS
            ),
            fileSyncProgressDao = fileSyncProgressDao
        )
    }

    private fun createFileProgress(
        workerId: String,
        courseId: Long,
        progress: Int,
        state: ProgressState
    ): FileSyncProgressEntity {
        return FileSyncProgressEntity(
            workerId = workerId,
            courseId = courseId,
            fileName = "File",
            progress = progress,
            fileSize = 1000,
            progressState = state
        )
    }
}