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
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import androidx.work.workDataOf
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.features.offline.sync.AggregateProgressObserver
import com.instructure.pandautils.features.offline.sync.CourseProgress
import com.instructure.pandautils.features.offline.sync.CourseSyncWorker
import com.instructure.pandautils.features.offline.sync.FileSyncData
import com.instructure.pandautils.features.offline.sync.FileSyncProgress
import com.instructure.pandautils.features.offline.sync.FileSyncWorker
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.TabSyncData
import com.instructure.pandautils.room.offline.daos.SyncProgressDao
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.room.offline.entities.SyncProgressEntity
import com.instructure.pandautils.utils.toJson
import io.mockk.coEvery
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

class AggregateProgressObserverTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val workManager: WorkManager = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val syncProgressDao: SyncProgressDao = mockk(relaxed = true)

    private lateinit var aggregateProgressObserver: AggregateProgressObserver

    @Before
    fun setup() {
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
    fun `Course update aggregate progress`() {
        val course1UUID = UUID.randomUUID().toString()
        val syncProgress = SyncProgressEntity(course1UUID, 1L, "Course 1")

        coEvery { syncProgressDao.findCourseProgressesLiveData() } returns MutableLiveData(listOf(syncProgress))

        var courseProgress = CourseProgress(
            1L,
            "Course 1",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
            emptyList()
        )

        var courseWorkInfo = createCourseWorkInfo(courseProgress, UUID.fromString(course1UUID), WorkInfo.State.RUNNING)

        val courseLiveData = MutableLiveData(
            listOf(
                courseWorkInfo
            )
        )

        every { workManager.getWorkInfosLiveData(any()) } returns courseLiveData

        aggregateProgressObserver = createObserver()

        assertEquals(0, aggregateProgressObserver.progressData.value?.progress)
        assertEquals(
            "${CourseSyncSettingsEntity.TABS.size * 100 * 1000} bytes",
            aggregateProgressObserver.progressData.value?.totalSize
        )
        assertEquals(ProgressState.IN_PROGRESS, aggregateProgressObserver.progressData.value?.progressState)

        courseProgress = courseProgress.copy(
            tabs = CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.COMPLETED) }
        )

        courseWorkInfo = createCourseWorkInfo(courseProgress, UUID.fromString(course1UUID), WorkInfo.State.SUCCEEDED)

        courseLiveData.postValue(listOf(courseWorkInfo))

        assertEquals(100, aggregateProgressObserver.progressData.value?.progress)
        assertEquals(ProgressState.COMPLETED, aggregateProgressObserver.progressData.value?.progressState)
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

        coEvery { syncProgressDao.findCourseProgressesLiveData() } returns MutableLiveData(syncProgress)

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

        var course1WorkInfo = createCourseWorkInfo(course1Progress, course1Id, WorkInfo.State.RUNNING)
        var course2WorkInfo = createCourseWorkInfo(course2Progress, course2Id, WorkInfo.State.RUNNING)

        val courseLiveData = MutableLiveData(
            listOf(
                course1WorkInfo,
                course2WorkInfo
            )
        )

        var file1Progress = FileSyncProgress("File 1", 0, ProgressState.IN_PROGRESS)
        var file1WorkInfo = createFileWorkInfo(file1Progress, file1Id, WorkInfo.State.RUNNING)

        var file2Progress = FileSyncProgress("File 2", 0, ProgressState.IN_PROGRESS)
        var file2WorkInfo = createFileWorkInfo(file2Progress, file2Id, WorkInfo.State.RUNNING)

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

        aggregateProgressObserver = createObserver()

        assertEquals(0, aggregateProgressObserver.progressData.value?.progress)
        assertEquals(
            "${2 * 1000000 + 1000 + 2000} bytes",
            aggregateProgressObserver.progressData.value?.totalSize
        )

        file1Progress = file1Progress.copy(
            progress = 100,
            progressState = ProgressState.COMPLETED
        )
        file1WorkInfo = createFileWorkInfo(file1Progress, file1Id, WorkInfo.State.SUCCEEDED)
        course1Progress = course1Progress.copy(
            tabs = CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.COMPLETED) },
        )
        course1WorkInfo = createCourseWorkInfo(course1Progress, course1Id, WorkInfo.State.SUCCEEDED)

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

        assertEquals(50, aggregateProgressObserver.progressData.value?.progress)

        file2Progress = file2Progress.copy(
            progress = 100,
            progressState = ProgressState.COMPLETED
        )
        file2WorkInfo = createFileWorkInfo(file2Progress, file2Id, WorkInfo.State.SUCCEEDED)

        course2Progress = course2Progress.copy(
            tabs = CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.COMPLETED) },
        )
        course2WorkInfo = createCourseWorkInfo(course2Progress, course2Id, WorkInfo.State.SUCCEEDED)

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

        assertEquals(100, aggregateProgressObserver.progressData.value?.progress)
        assertEquals(ProgressState.COMPLETED, aggregateProgressObserver.progressData.value?.progressState)
    }

    @Test
    fun `Update total size and progress with additional files`() {
        val course1Id = UUID.randomUUID()

        val file1Id = UUID.randomUUID()
        val file2Id = UUID.randomUUID()
        val file3Id = UUID.randomUUID()

        val syncProgress = listOf(SyncProgressEntity(course1Id.toString(), 1L, "Course 1"))

        coEvery { syncProgressDao.findCourseProgressesLiveData() } returns MutableLiveData(syncProgress)

        var course1Progress = CourseProgress(
            1L,
            "Course 1",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
            fileSyncData = listOf(
                FileSyncData(file1Id.toString(), "File 1", 1000)),
            additionalFileSyncData = listOf(
                FileSyncData(file2Id.toString(), "Additional internal file", 2000),
                FileSyncData(file3Id.toString(), "Additional external file", 0)
            )
        )

        var course1WorkInfo = createCourseWorkInfo(course1Progress, course1Id, WorkInfo.State.RUNNING)

        val courseLiveData = MutableLiveData(listOf(course1WorkInfo))

        var file1Progress = FileSyncProgress("File 1", 0, ProgressState.IN_PROGRESS)
        var file1WorkInfo = createFileWorkInfo(file1Progress, file1Id, WorkInfo.State.RUNNING)

        var file2Progress = FileSyncProgress("Additional internal file", 0, ProgressState.IN_PROGRESS)
        var file2WorkInfo = createFileWorkInfo(file2Progress, file2Id, WorkInfo.State.RUNNING)

        var file3Progress = FileSyncProgress("Additional external file", 0, ProgressState.IN_PROGRESS, totalBytes = 0, externalFile = true)
        var file3WorkInfo = createFileWorkInfo(file3Progress, file3Id, WorkInfo.State.RUNNING)

        val aggregateWorkInfo = listOf(
            course1WorkInfo,
            file1WorkInfo,
            file2WorkInfo,
            file3WorkInfo
        )

        val aggregateLiveData = MutableLiveData(aggregateWorkInfo)

        val queryCaptor = slot<WorkQuery>()
        every { workManager.getWorkInfosLiveData(capture(queryCaptor)) } answers {
            if (queryCaptor.captured.ids.size == 1) {
                courseLiveData
            } else {
                aggregateLiveData
            }
        }

        aggregateProgressObserver = createObserver()

        assertEquals(0, aggregateProgressObserver.progressData.value?.progress)
        assertEquals(
            "${1000000 + 1000 + 2000} bytes",
            aggregateProgressObserver.progressData.value?.totalSize
        )

        file1Progress = file1Progress.copy(
            progress = 100,
            progressState = ProgressState.COMPLETED
        )
        file1WorkInfo = createFileWorkInfo(file1Progress, file1Id, WorkInfo.State.SUCCEEDED)
        course1Progress = course1Progress.copy(
            tabs = CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.COMPLETED) },
        )
        course1WorkInfo = createCourseWorkInfo(course1Progress, course1Id, WorkInfo.State.RUNNING)

        courseLiveData.postValue(listOf(course1WorkInfo))
        aggregateLiveData.postValue(
            listOf(
                course1WorkInfo,
                file1WorkInfo,
                file2WorkInfo,
                file3WorkInfo
            )
        )

        // Course tabs and files are completed, but additional files are still in progress
        assertEquals(99, aggregateProgressObserver.progressData.value?.progress)

        file2Progress = file2Progress.copy(progress = 100, progressState = ProgressState.COMPLETED)
        file2WorkInfo = createFileWorkInfo(file2Progress, file2Id, WorkInfo.State.SUCCEEDED)

        file3Progress = FileSyncProgress("Additional external file", 0, ProgressState.IN_PROGRESS, totalBytes = 3000, externalFile = true)
        file3WorkInfo = createFileWorkInfo(file3Progress, file3Id, WorkInfo.State.RUNNING)

        aggregateLiveData.postValue(
            listOf(
                course1WorkInfo,
                file1WorkInfo,
                file2WorkInfo,
                file3WorkInfo
            )
        )

        // Total size is updated with the external file
        assertEquals("${1000000 + 1000 + 2000 + 3000} bytes", aggregateProgressObserver.progressData.value?.totalSize)

        file3Progress = FileSyncProgress("Additional external file", 100, ProgressState.COMPLETED, totalBytes = 3000, externalFile = true)
        file3WorkInfo = createFileWorkInfo(file3Progress, file3Id, WorkInfo.State.SUCCEEDED)

        course1WorkInfo = createCourseWorkInfo(course1Progress, course1Id, WorkInfo.State.SUCCEEDED)

        aggregateLiveData.postValue(
            listOf(
                course1WorkInfo,
                file1WorkInfo,
                file2WorkInfo,
                file3WorkInfo
            )
        )

        // External files are downloaded, progress should be 100%
        assertEquals(100, aggregateProgressObserver.progressData.value?.progress)
        assertEquals(ProgressState.COMPLETED, aggregateProgressObserver.progressData.value?.progressState)
    }

    @Test
    fun `Error state`() {
        val course1UUID = UUID.randomUUID().toString()
        val syncProgress = SyncProgressEntity(course1UUID, 1L, "Course 1")

        coEvery { syncProgressDao.findCourseProgressesLiveData() } returns MutableLiveData(listOf(syncProgress))

        val courseProgress = CourseProgress(
            1L,
            "Course 1",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
            emptyList()
        )

        val courseWorkInfo = createCourseWorkInfo(courseProgress, UUID.fromString(course1UUID), WorkInfo.State.RUNNING)

        val courseLiveData = MutableLiveData(
            listOf(
                courseWorkInfo
            )
        )

        every { workManager.getWorkInfosLiveData(any()) } returns courseLiveData

        aggregateProgressObserver = createObserver()

        courseLiveData.postValue(
            listOf(
                createCourseWorkInfo(courseProgress, UUID.fromString(course1UUID), WorkInfo.State.FAILED)
            )
        )

        assertEquals(ProgressState.ERROR, aggregateProgressObserver.progressData.value?.progressState)
    }

    @Test
    fun `Progress tracking starts when course files are ready`() {
        val course1UUID = UUID.randomUUID().toString()
        val syncProgress = SyncProgressEntity(course1UUID, 1L, "Course 1")

        coEvery { syncProgressDao.findCourseProgressesLiveData() } returns MutableLiveData(listOf(syncProgress))

        var courseProgress = CourseProgress(
            1L,
            "Course 1",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
            null
        )

        var courseWorkInfo = createCourseWorkInfo(courseProgress, UUID.fromString(course1UUID), WorkInfo.State.RUNNING)

        val courseLiveData = MutableLiveData(
            listOf(
                courseWorkInfo
            )
        )

        every { workManager.getWorkInfosLiveData(any()) } returns courseLiveData

        aggregateProgressObserver = createObserver()

        assertEquals(ProgressState.STARTING, aggregateProgressObserver.progressData.value?.progressState)

        courseProgress = courseProgress.copy(
            fileSyncData = emptyList()
        )

        courseWorkInfo = createCourseWorkInfo(courseProgress, UUID.randomUUID(), WorkInfo.State.RUNNING)

        courseLiveData.postValue(listOf(courseWorkInfo))

        assertEquals(0, aggregateProgressObserver.progressData.value?.progress)
    }

    private fun createObserver(): AggregateProgressObserver {
        return AggregateProgressObserver(workManager, context, syncProgressDao)
    }

    private fun createCourseWorkInfo(courseProgress: CourseProgress, uuid: UUID, state: WorkInfo.State): WorkInfo {
        return WorkInfo(
            uuid,
            state,
            if (state.isFinished) workDataOf(CourseSyncWorker.OUTPUT to courseProgress.toJson()) else workDataOf(),
            listOf(CourseSyncWorker.TAG),
            workDataOf(
                CourseSyncWorker.COURSE_PROGRESS to courseProgress.toJson()
            ),
            0,
            0
        )
    }

    private fun createFileWorkInfo(fileSyncProgress: FileSyncProgress, uuid: UUID, state: WorkInfo.State): WorkInfo {
        return WorkInfo(
            uuid,
            state,
            if (state == WorkInfo.State.SUCCEEDED) workDataOf(FileSyncWorker.OUTPUT to fileSyncProgress.toJson()) else workDataOf(),
            listOf(FileSyncWorker.TAG),
            workDataOf(
                FileSyncWorker.PROGRESS to fileSyncProgress.toJson()
            ),
            0,
            0
        )
    }
}