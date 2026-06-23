/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.horizon.features.account.manageofflinecontent

import android.content.Context
import android.text.format.Formatter
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import java.io.File
import java.nio.file.Files
import com.instructure.horizon.database.dao.HorizonCourseSyncPlanDao
import com.instructure.horizon.domain.usecase.CourseFileData
import com.instructure.horizon.domain.usecase.CourseWithFilesData
import com.instructure.horizon.domain.usecase.DeviceStorageData
import com.instructure.horizon.domain.usecase.GetCoursesWithFilesUseCase
import com.instructure.horizon.domain.usecase.GetDeviceStorageUseCase
import com.instructure.horizon.domain.usecase.GetSelectedSyncFileIdsUseCase
import com.instructure.horizon.domain.usecase.SaveOfflineSyncPlanUseCase
import com.instructure.horizon.offline.sync.HorizonAggregateProgressObserver
import com.instructure.horizon.offline.sync.HorizonOfflineSyncHelper
import com.instructure.horizon.offline.sync.HorizonSyncProgressData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ManageOfflineContentViewModelTest {

    private val context: Context = mockk(relaxed = true)
    private val getCoursesWithFilesUseCase: GetCoursesWithFilesUseCase = mockk(relaxed = true)
    private val getDeviceStorageUseCase: GetDeviceStorageUseCase = mockk(relaxed = true)
    private val getSelectedSyncFileIdsUseCase: GetSelectedSyncFileIdsUseCase = mockk(relaxed = true)
    private val saveOfflineSyncPlanUseCase: SaveOfflineSyncPlanUseCase = mockk(relaxed = true)
    private val syncHelper: HorizonOfflineSyncHelper = mockk(relaxed = true)
    private val courseSyncPlanDao: HorizonCourseSyncPlanDao = mockk(relaxed = true)
    private val aggregateProgressObserver: HorizonAggregateProgressObserver = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private val progressFlow = MutableStateFlow(HorizonSyncProgressData())
    private val coursePlanFlow = MutableStateFlow<List<com.instructure.horizon.database.entity.HorizonCourseSyncPlanEntity>>(emptyList())

    private lateinit var tempFilesDir: File

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        mockkStatic(Formatter::class)
        every { Formatter.formatShortFileSize(any(), any()) } returns ""
        tempFilesDir = Files.createTempDirectory("manage-offline-test").toFile()
        every { context.filesDir } returns tempFilesDir
        every { aggregateProgressObserver.progressData } returns progressFlow
        every { courseSyncPlanDao.findAllFlow() } returns coursePlanFlow
        coEvery { courseSyncPlanDao.findAll() } returns emptyList()
        coEvery { getDeviceStorageUseCase() } returns DeviceStorageData(totalBytes = 100, availableBytes = 100)
        coEvery { getSelectedSyncFileIdsUseCase() } returns emptySet()
        every { apiPrefs.user } returns User(id = 1L)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
        tempFilesDir.deleteRecursively()
    }

    @Test
    fun `Locked file is rendered as unselected even when previously synced or selected`() {
        coEvery { getSelectedSyncFileIdsUseCase() } returns setOf(2L, 3L)
        coEvery { getCoursesWithFilesUseCase() } returns listOf(
            CourseWithFilesData(
                courseId = 10L,
                courseName = "Course",
                files = listOf(
                    CourseFileData(fileId = 1L, displayName = "a", size = 1L, isSynced = false, isLocked = false),
                    CourseFileData(fileId = 2L, displayName = "b (locked, synced)", size = 1L, isSynced = true, isLocked = true),
                    CourseFileData(fileId = 3L, displayName = "c (locked, selected)", size = 1L, isSynced = false, isLocked = true),
                ),
            )
        )

        val viewModel = getViewModel()
        awaitLoaded(viewModel)
        val files = viewModel.uiState.value.courses.first().files

        assertFalse(files.first { it.fileId == 2L }.isSelected)
        assertFalse(files.first { it.fileId == 3L }.isSelected)
        assertTrue(files.first { it.fileId == 2L }.isLocked)
        assertTrue(files.first { it.fileId == 3L }.isLocked)
    }

    @Test
    fun `updateFileSelection is a no-op on a locked file`() {
        coEvery { getCoursesWithFilesUseCase() } returns listOf(
            CourseWithFilesData(
                courseId = 10L,
                courseName = "Course",
                files = listOf(
                    CourseFileData(fileId = 1L, displayName = "locked", size = 1L, isSynced = false, isLocked = true),
                ),
            )
        )

        val viewModel = getViewModel()
        awaitLoaded(viewModel)
        val lockedFile = viewModel.uiState.value.courses.first().files.first()

        lockedFile.onSelectionChanged(true)

        val after = viewModel.uiState.value.courses.first().files.first()
        assertFalse(after.isSelected)
    }

    @Test
    fun `Course reaches ALL state when every non-locked file is selected`() {
        coEvery { getCoursesWithFilesUseCase() } returns listOf(
            CourseWithFilesData(
                courseId = 10L,
                courseName = "Course",
                files = listOf(
                    CourseFileData(fileId = 1L, displayName = "a", size = 1L, isSynced = false, isLocked = false),
                    CourseFileData(fileId = 2L, displayName = "locked", size = 1L, isSynced = false, isLocked = true),
                ),
            )
        )

        val viewModel = getViewModel()
        awaitLoaded(viewModel)
        val unlocked = viewModel.uiState.value.courses.first().files.first { !it.isLocked }
        unlocked.onSelectionChanged(true)

        assertEquals(
            CourseOfflineState.ALL,
            viewModel.uiState.value.courses.first().offlineState,
        )
    }

    @Test
    fun `onOfflineStateChanged ALL does not select locked files`() {
        coEvery { getCoursesWithFilesUseCase() } returns listOf(
            CourseWithFilesData(
                courseId = 10L,
                courseName = "Course",
                files = listOf(
                    CourseFileData(fileId = 1L, displayName = "a", size = 1L, isSynced = false, isLocked = false),
                    CourseFileData(fileId = 2L, displayName = "locked", size = 1L, isSynced = false, isLocked = true),
                ),
            )
        )

        val viewModel = getViewModel()
        awaitLoaded(viewModel)
        viewModel.uiState.value.courses.first().onOfflineStateChanged(CourseOfflineState.ALL)

        val files = viewModel.uiState.value.courses.first().files
        assertTrue(files.first { it.fileId == 1L }.isSelected)
        assertFalse(files.first { it.fileId == 2L }.isSelected)
    }

    private fun getViewModel(): ManageOfflineContentViewModel {
        return ManageOfflineContentViewModel(
            context,
            getCoursesWithFilesUseCase,
            getDeviceStorageUseCase,
            getSelectedSyncFileIdsUseCase,
            saveOfflineSyncPlanUseCase,
            syncHelper,
            courseSyncPlanDao,
            aggregateProgressObserver,
            apiPrefs,
        )
    }

    private fun awaitLoaded(viewModel: ManageOfflineContentViewModel) {
        val deadline = System.currentTimeMillis() + 5_000
        while (viewModel.uiState.value.loadingState.isLoading) {
            if (System.currentTimeMillis() > deadline) error("Timed out waiting for ViewModel load")
            Thread.sleep(10)
        }
    }
}
