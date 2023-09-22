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
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State
import androidx.work.WorkManager
import androidx.work.WorkQuery
import androidx.work.workDataOf
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.features.offline.sync.CourseProgress
import com.instructure.pandautils.features.offline.sync.CourseSyncWorker
import com.instructure.pandautils.features.offline.sync.FileSyncData
import com.instructure.pandautils.features.offline.sync.FileSyncProgress
import com.instructure.pandautils.features.offline.sync.FileSyncWorker
import com.instructure.pandautils.features.offline.sync.OfflineSyncHelper
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.TabSyncData
import com.instructure.pandautils.features.offline.sync.progress.SyncProgressAction
import com.instructure.pandautils.features.offline.sync.progress.SyncProgressViewModel
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.CourseProgressItemViewModel
import com.instructure.pandautils.features.offline.sync.progress.itemviewmodels.FilesTabProgressItemViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.SyncProgressDao
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.SyncProgressEntity
import com.instructure.pandautils.room.offline.model.CourseSyncSettingsWithFiles
import com.instructure.pandautils.utils.toJson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

@ExperimentalCoroutinesApi
class SyncProgressViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val context: Context = mockk(relaxed = true)
    private val workManager: WorkManager = mockk(relaxed = true)
    private val syncProgressDao: SyncProgressDao = mockk(relaxed = true)
    private val courseSyncSettingsDao: CourseSyncSettingsDao = mockk(relaxed = true)
    private val offlineSyncHelper: OfflineSyncHelper = mockk(relaxed = true)

    private lateinit var viewModel: SyncProgressViewModel

    @Before
    fun setup() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        mockkObject(NumberHelper)
        val captor = slot<Long>()
        every { NumberHelper.readableFileSize(any<Context>(), capture(captor)) } answers {
            "${captor.captured} bytes"
        }
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `Move back if sync is not running`() = runTest {
        coEvery { syncProgressDao.findCourseProgresses() } returns emptyList()

        viewModel = createViewModel()

        assertEquals(SyncProgressAction.Back, viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Init state`() = runTest {
        val course1UUID = UUID.randomUUID().toString()
        val course2UUID = UUID.randomUUID().toString()
        val syncProgressEntities = listOf(
            SyncProgressEntity(course1UUID, 1L, "Course 1"),
            SyncProgressEntity(course2UUID, 2L, "Course 2"),
        )
        coEvery { syncProgressDao.findCourseProgresses() } returns syncProgressEntities

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
                    files = listOf(
                        FilesTabProgressItemViewModel(
                            data = FileTabProgressViewData(
                                courseWorkerId = course1UUID,
                                items = emptyList(),
                            ),
                            workManager = workManager,
                            context = context,
                        )
                    ),
                    workerId = course1UUID
                ),
                workManager = workManager,
                context = context
            ),
            CourseProgressItemViewModel(
                data = CourseProgressViewData(
                    courseName = "Course 2",
                    files = emptyList(),
                    workerId = course2UUID
                ),
                workManager = workManager,
                context = context
            )
        )

        assertEquals(expected, viewModel.data.value?.items)
    }

    @Test
    fun `Retry`() = runTest {
        val course1UUID = UUID.randomUUID().toString()
        val syncProgress = SyncProgressEntity(course1UUID, 1L, "Course 1")

        coEvery { syncProgressDao.findCourseProgresses() } returns listOf(syncProgress)

        coEvery { courseSyncSettingsDao.findWithFilesById(1L) } returns CourseSyncSettingsWithFiles(
            courseSyncSettings = CourseSyncSettingsEntity(1L, "Course 1", true, fullFileSync = true),
            files = emptyList()
        )

        every { workManager.getWorkInfosLiveData(any()) } returns MutableLiveData(
            listOf(
                WorkInfo(
                    UUID.fromString(
                        course1UUID
                    ),
                    State.FAILED,
                    workDataOf(),
                    listOf(CourseSyncWorker.TAG),
                    workDataOf(),
                    0,
                    0
                )
            )
        )

        viewModel = createViewModel()

        viewModel.onActionClicked()

        coVerify {
            syncProgressDao.deleteAll()
            offlineSyncHelper.syncOnce(listOf(1L))
        }

        assertEquals(SyncProgressAction.Back, viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Cancel`() = runTest {
        val course1UUID = UUID.randomUUID().toString()
        val syncProgress = SyncProgressEntity(course1UUID, 1L, "Course 1")

        coEvery { syncProgressDao.findCourseProgresses() } returns listOf(syncProgress)

        coEvery { courseSyncSettingsDao.findWithFilesById(1L) } returns CourseSyncSettingsWithFiles(
            courseSyncSettings = CourseSyncSettingsEntity(1L, "Course 1", true, fullFileSync = true),
            files = emptyList()
        )

        every { workManager.getWorkInfosLiveData(any()) } returns MutableLiveData(
            listOf(
                WorkInfo(
                    UUID.fromString(
                        course1UUID
                    ),
                    State.RUNNING,
                    workDataOf(),
                    listOf(CourseSyncWorker.TAG),
                    workDataOf(
                        CourseSyncWorker.COURSE_PROGRESS to CourseProgress(
                            1L,
                            "Course 1",
                            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
                            emptyList()
                        ).toJson()
                    ),
                    0,
                    0
                )
            )
        )

        viewModel = createViewModel()

        viewModel.onActionClicked()

        assertEquals(SyncProgressAction.CancelConfirmation, viewModel.events.value?.getContentIfNotHandled())

        viewModel.cancel()

        coVerify {
            workManager.cancelAllWorkByTag(CourseSyncWorker.TAG)
            workManager.cancelAllWorkByTag(FileSyncWorker.TAG)
            syncProgressDao.deleteAll()
        }

        assertEquals(SyncProgressAction.Back, viewModel.events.value?.getContentIfNotHandled())
    }

    @Test
    fun `Course update aggregate progress`() {
        val course1UUID = UUID.randomUUID().toString()
        val syncProgress = SyncProgressEntity(course1UUID, 1L, "Course 1")

        coEvery { syncProgressDao.findCourseProgresses() } returns listOf(syncProgress)

        coEvery { courseSyncSettingsDao.findWithFilesById(1L) } returns CourseSyncSettingsWithFiles(
            courseSyncSettings = CourseSyncSettingsEntity(1L, "Course 1", true, fullFileSync = true),
            files = emptyList()
        )

        var courseProgress = CourseProgress(
            1L,
            "Course 1",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
            emptyList()
        )

        var courseWorkInfo = createCourseWorkInfo(courseProgress, UUID.fromString(course1UUID), State.RUNNING)

        val courseLiveData = MutableLiveData(
            listOf(
                courseWorkInfo
            )
        )

        every { workManager.getWorkInfosLiveData(any()) } returns courseLiveData

        viewModel = createViewModel()

        assertEquals(0, viewModel.progressData.value?.progress)
        assertEquals(
            "${CourseSyncSettingsEntity.TABS.size * 100 * 1000} bytes",
            viewModel.progressData.value?.totalSize
        )

        courseProgress = courseProgress.copy(
            tabs = CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.COMPLETED) }
        )

        courseWorkInfo = createCourseWorkInfo(courseProgress, UUID.fromString(course1UUID), State.RUNNING)

        courseLiveData.postValue(listOf(courseWorkInfo))

        assertEquals(100, viewModel.progressData.value?.progress)
    }

    @Test
    fun `Progress tracking starts when course files are ready`() {
        val course1UUID = UUID.randomUUID().toString()
        val syncProgress = SyncProgressEntity(course1UUID, 1L, "Course 1")

        coEvery { syncProgressDao.findCourseProgresses() } returns listOf(syncProgress)

        coEvery { courseSyncSettingsDao.findWithFilesById(1L) } returns CourseSyncSettingsWithFiles(
            courseSyncSettings = CourseSyncSettingsEntity(1L, "Course 1", true, fullFileSync = true),
            files = emptyList()
        )

        var courseProgress = CourseProgress(
            1L,
            "Course 1",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
            null
        )

        var courseWorkInfo = createCourseWorkInfo(courseProgress, UUID.fromString(course1UUID), State.RUNNING)

        val courseLiveData = MutableLiveData(
            listOf(
                courseWorkInfo
            )
        )

        every { workManager.getWorkInfosLiveData(any()) } returns courseLiveData

        viewModel = createViewModel()

        assertNull(viewModel.progressData.value)

        courseProgress = courseProgress.copy(
            fileSyncData = emptyList()
        )

        courseWorkInfo = createCourseWorkInfo(courseProgress, UUID.randomUUID(), State.RUNNING)

        courseLiveData.postValue(listOf(courseWorkInfo))

        assertEquals(0, viewModel.progressData.value?.progress)
    }

    @Test
    fun `Aggregate progress updates`() {
        val course1Id = UUID.randomUUID()
        val course2Id = UUID.randomUUID()

        val file1Id = UUID.randomUUID()
        val file2Id = UUID.randomUUID()

        val syncProgress = listOf(
            SyncProgressEntity(course1Id.toString(), 1L, "Course 1"),
            SyncProgressEntity(course2Id.toString(), 2L, "Course 2")
        )

        coEvery { syncProgressDao.findCourseProgresses() } returns syncProgress

        var course1Progress = CourseProgress(
            1L,
            "Course 1",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
            listOf(
                FileSyncData(file1Id.toString(), "File 1", 1000),
                FileSyncData(file2Id.toString(), "File 2", 2000)
            )
        )
        var course2Progress = CourseProgress(
            2L,
            "Course 2",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
            emptyList()
        )

        var course1WorkInfo = createCourseWorkInfo(course1Progress, course1Id, State.RUNNING)
        var course2WorkInfo = createCourseWorkInfo(course2Progress, course2Id, State.RUNNING)

        val courseLiveData = MutableLiveData(
            listOf(
                course1WorkInfo,
                course2WorkInfo
            )
        )

        var file1Progress = FileSyncProgress("File 1", 0, ProgressState.IN_PROGRESS)
        var file1WorkInfo = createFileWorkInfo(file1Progress, file1Id, State.RUNNING)

        var file2Progress = FileSyncProgress("File 2", 0, ProgressState.IN_PROGRESS)
        var file2WorkInfo = createFileWorkInfo(file2Progress, file2Id, State.RUNNING)

        val aggregateWorkInfo = listOf(
            course1WorkInfo,
            course2WorkInfo,
            file1WorkInfo,
            file2WorkInfo
        )

        val aggregateLiveData = MutableLiveData(aggregateWorkInfo)

        val queryCaptor = slot<WorkQuery>()
        every { workManager.getWorkInfosLiveData(capture(queryCaptor)) } answers {
            if (queryCaptor.captured.ids.size == 2) {
                courseLiveData
            } else {
                aggregateLiveData
            }
        }

        viewModel = createViewModel()

        assertEquals(0, viewModel.progressData.value?.progress)
        assertEquals(
            "${2 * 1000000 + 1000 + 2000} bytes",
            viewModel.progressData.value?.totalSize
        )

        file1Progress = file1Progress.copy(
            progress = 100,
            progressState = ProgressState.COMPLETED
        )
        file1WorkInfo = createFileWorkInfo(file1Progress, file1Id, State.SUCCEEDED)
        course1Progress = course1Progress.copy(
            tabs = CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.COMPLETED) },
        )
        course1WorkInfo = createCourseWorkInfo(course1Progress, course1Id, State.SUCCEEDED)

        courseLiveData.postValue(
            listOf(
                course1WorkInfo,
                course2WorkInfo
            )
        )
        aggregateLiveData.postValue(
            listOf(
                course1WorkInfo,
                course2WorkInfo,
                file1WorkInfo,
                file2WorkInfo
            )
        )

        assertEquals(50, viewModel.progressData.value?.progress)

        file2Progress = file2Progress.copy(
            progress = 100,
            progressState = ProgressState.COMPLETED
        )
        file2WorkInfo = createFileWorkInfo(file2Progress, file2Id, State.SUCCEEDED)

        course2Progress = course2Progress.copy(
            tabs = CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.COMPLETED) },
        )
        course2WorkInfo = createCourseWorkInfo(course2Progress, course2Id, State.SUCCEEDED)

        courseLiveData.postValue(
            listOf(
                course1WorkInfo,
                course2WorkInfo
            )
        )
        aggregateLiveData.postValue(
            listOf(
                course1WorkInfo,
                course2WorkInfo,
                file1WorkInfo,
                file2WorkInfo
            )
        )

        assertEquals(100, viewModel.progressData.value?.progress)
        assertEquals(ViewState.Success, viewModel.state.value)
    }

    @Test
    fun `Error state`() {
        val course1UUID = UUID.randomUUID().toString()
        val syncProgress = SyncProgressEntity(course1UUID, 1L, "Course 1")

        coEvery { syncProgressDao.findCourseProgresses() } returns listOf(syncProgress)

        coEvery { courseSyncSettingsDao.findWithFilesById(1L) } returns CourseSyncSettingsWithFiles(
            courseSyncSettings = CourseSyncSettingsEntity(1L, "Course 1", true, fullFileSync = true),
            files = emptyList()
        )

        val courseProgress = CourseProgress(
            1L,
            "Course 1",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
            emptyList()
        )

        val courseWorkInfo = createCourseWorkInfo(courseProgress, UUID.fromString(course1UUID), State.FAILED)

        val courseLiveData = MutableLiveData(
            listOf(
                courseWorkInfo
            )
        )

        every { workManager.getWorkInfosLiveData(any()) } returns courseLiveData

        viewModel = createViewModel()

        assertEquals(ViewState.Error(), viewModel.state.value)
    }

    private fun createCourseWorkInfo(courseProgress: CourseProgress, uuid: UUID, state: State): WorkInfo {
        return WorkInfo(
            uuid,
            state,
            if (state == State.SUCCEEDED) workDataOf(CourseSyncWorker.OUTPUT to courseProgress.toJson()) else workDataOf(),
            listOf(CourseSyncWorker.TAG),
            workDataOf(
                CourseSyncWorker.COURSE_PROGRESS to courseProgress.toJson()
            ),
            0,
            0
        )
    }

    private fun createFileWorkInfo(fileSyncProgress: FileSyncProgress, uuid: UUID, state: State): WorkInfo {
        return WorkInfo(
            uuid,
            state,
            if (state == State.SUCCEEDED) workDataOf(FileSyncWorker.OUTPUT to fileSyncProgress.toJson()) else workDataOf(),
            listOf(FileSyncWorker.TAG),
            workDataOf(
                FileSyncWorker.PROGRESS to fileSyncProgress.toJson()
            ),
            0,
            0
        )
    }

    private fun createViewModel(): SyncProgressViewModel {
        return SyncProgressViewModel(context, workManager, syncProgressDao, courseSyncSettingsDao, offlineSyncHelper)
    }

}