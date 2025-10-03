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

package com.instructure.pandautils.features.offline.sync.progress

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkManager
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.features.offline.sync.AggregateProgressObserver
import com.instructure.pandautils.features.offline.sync.AggregateProgressViewData
import com.instructure.pandautils.features.offline.sync.OfflineSyncHelper
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.AdditionalFilesProgressItemViewModel
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.CourseProgressItemViewModel
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.FilesTabProgressItemViewModel
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.StudioMediaProgressItemViewModel
import com.instructure.pandautils.room.offline.daos.CourseSyncProgressDao
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.daos.StudioMediaProgressDao
import com.instructure.pandautils.room.offline.entities.CourseSyncProgressEntity
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.room.offline.model.CourseSyncSettingsWithFiles
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import com.instructure.testutils.ViewModelTestRule
import com.instructure.testutils.LifecycleTestOwner
import org.junit.Test
import java.util.UUID

@ExperimentalCoroutinesApi
class SyncProgressViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val lifecycleTestOwner = LifecycleTestOwner()

    private val context: Context = mockk(relaxed = true)
    private val workManager: WorkManager = mockk(relaxed = true)
    private val courseSyncSettingsDao: CourseSyncSettingsDao = mockk(relaxed = true)
    private val offlineSyncHelper: OfflineSyncHelper = mockk(relaxed = true)
    private val aggregateProgressObserver: AggregateProgressObserver = mockk(relaxed = true)
    private val courseSyncProgressDao: CourseSyncProgressDao = mockk(relaxed = true)
    private val fileSyncProgressDao: FileSyncProgressDao = mockk(relaxed = true)
    private val studioMediaProgressDao: StudioMediaProgressDao = mockk(relaxed = true)

    private lateinit var viewModel: SyncProgressViewModel

    @Before
    fun setup() {

        mockkObject(NumberHelper)
        val captor = slot<Long>()
        every { NumberHelper.readableFileSize(any<Context>(), capture(captor)) } answers {
            "${captor.captured} bytes"
        }
    }

    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `Move back if sync is not running`() = runTest {
        coEvery { courseSyncProgressDao.findAll() } returns emptyList()

        viewModel = createViewModel()

        assertEquals(SyncProgressAction.Back, viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Init state`() = runTest {
        val courseProgresses = listOf(
            CourseSyncProgressEntity(1L, "Course 1", emptyMap()),
            CourseSyncProgressEntity(2L, "Course 2", emptyMap())
        )

        coEvery { courseSyncProgressDao.findAll() } returns courseProgresses

        coEvery { courseSyncSettingsDao.findWithFilesById(1L) } returns CourseSyncSettingsWithFiles(
            courseSyncSettings = CourseSyncSettingsEntity(1L, "Course 1", true, fullFileSync = true),
            files = emptyList()
        )

        coEvery { courseSyncSettingsDao.findWithFilesById(2L) } returns CourseSyncSettingsWithFiles(
            courseSyncSettings = CourseSyncSettingsEntity(2L, "Course 2", true, fullFileSync = false),
            files = emptyList()
        )

        viewModel = createViewModel()

        val expected = listOf(
            CourseProgressItemViewModel(
                data = CourseProgressViewData(
                    courseName = "Course 1",
                    courseId = 1L,
                    files =
                    FilesTabProgressItemViewModel(
                        data = FileTabProgressViewData(
                            courseId = 1L,
                            items = emptyList(),
                        ),
                        context = context,
                        courseSyncProgressDao = courseSyncProgressDao,
                        fileSyncProgressDao = fileSyncProgressDao
                    ),
                    additionalFiles = AdditionalFilesProgressItemViewModel(
                        data = AdditionalFilesProgressViewData(1L),
                        context = context,
                        courseSyncProgressDao = courseSyncProgressDao,
                        fileSyncProgressDao = fileSyncProgressDao
                    ),
                ),
                context = context,
                courseSyncProgressDao = courseSyncProgressDao,
                fileSyncProgressDao = fileSyncProgressDao
            ),
            CourseProgressItemViewModel(
                data = CourseProgressViewData(
                    courseName = "Course 2",
                    files = null,
                    additionalFiles = AdditionalFilesProgressItemViewModel(
                        data = AdditionalFilesProgressViewData(2L),
                        context = context,
                        courseSyncProgressDao = courseSyncProgressDao,
                        fileSyncProgressDao = fileSyncProgressDao
                    ),
                    courseId = 2L
                ),
                context = context,
                courseSyncProgressDao = courseSyncProgressDao,
                fileSyncProgressDao = fileSyncProgressDao
            ),
            StudioMediaProgressItemViewModel(StudioMediaProgressViewData(), studioMediaProgressDao, context)
        )

        assertEquals(expected, viewModel.data.value?.items)
    }

    @Test
    fun `Retry`() = runTest {
        val courseProgress = CourseSyncProgressEntity(1L, "Course 1", emptyMap(), progressState = ProgressState.ERROR)

        coEvery { courseSyncProgressDao.findAll() } returns listOf(courseProgress)

        coEvery { courseSyncSettingsDao.findWithFilesById(1L) } returns CourseSyncSettingsWithFiles(
            courseSyncSettings = CourseSyncSettingsEntity(1L, "Course 1", true, fullFileSync = true),
            files = emptyList()
        )

        every { aggregateProgressObserver.progressData } returns MutableLiveData(
            AggregateProgressViewData(
                title = "Course 1",
                progressState = ProgressState.ERROR,
                progress = 0,
                totalSize = "0 bytes"
            )
        )

        viewModel = createViewModel()

        viewModel.onActionClicked()

        coVerify {
            courseSyncProgressDao.deleteAll()
            fileSyncProgressDao.deleteAll()
            studioMediaProgressDao.deleteAll()
            offlineSyncHelper.syncOnce(listOf(1L))
        }

        assertEquals(SyncProgressAction.Back, viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Cancel`() = runTest {
        val syncProgress =
            CourseSyncProgressEntity(1L, "Course 1", emptyMap(), progressState = ProgressState.IN_PROGRESS)

        coEvery { courseSyncProgressDao.findAll() } returns listOf(syncProgress)

        coEvery { courseSyncSettingsDao.findWithFilesById(1L) } returns CourseSyncSettingsWithFiles(
            courseSyncSettings = CourseSyncSettingsEntity(1L, "Course 1", true, fullFileSync = true),
            files = emptyList()
        )

        every { aggregateProgressObserver.progressData } returns MutableLiveData(
            AggregateProgressViewData(
                title = "Course 1",
                progressState = ProgressState.IN_PROGRESS,
                progress = 0,
                totalSize = "0 bytes"
            )
        )

        viewModel = createViewModel()

        viewModel.onActionClicked()

        assertEquals(SyncProgressAction.CancelConfirmation, viewModel.events.value?.getContentIfNotHandled())

        viewModel.cancel()

        coVerify {
            offlineSyncHelper.cancelRunningWorkers()
            courseSyncProgressDao.deleteAll()
            fileSyncProgressDao.deleteAll()
            studioMediaProgressDao.deleteAll()
        }

        assertEquals(SyncProgressAction.Back, viewModel.events.value?.getContentIfNotHandled())
    }

    private fun createViewModel(): SyncProgressViewModel {
        return SyncProgressViewModel(
            context,
            workManager,
            courseSyncSettingsDao,
            offlineSyncHelper,
            aggregateProgressObserver,
            courseSyncProgressDao,
            fileSyncProgressDao,
            studioMediaProgressDao
        )
    }

}
