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
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.instructure.canvasapi2.models.Tab
import com.instructure.pandautils.features.offline.sync.CourseProgress
import com.instructure.pandautils.features.offline.sync.CourseSyncWorker
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.TabSyncData
import com.instructure.pandautils.features.offline.sync.progress.TabProgressViewData
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.utils.toJson
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class TabProgressItemViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val workManager: WorkManager = mockk(relaxed = true)

    private lateinit var tabProgressItemViewModel: TabProgressItemViewModel

    @Test
    fun `Progress updates`() {
        val uuid = UUID.randomUUID()
        var courseProgress = CourseProgress(
            1L,
            "Course",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
            emptyList()
        )
        val courseLiveData = MutableLiveData(createCourseWorkInfo(courseProgress, uuid, WorkInfo.State.RUNNING))

        every { workManager.getWorkInfoByIdLiveData(uuid) } returns courseLiveData

        tabProgressItemViewModel = createItemViewModel(uuid)

        assertEquals(ProgressState.IN_PROGRESS, tabProgressItemViewModel.data.state)

        courseProgress = CourseProgress(
            1L,
            "Course",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.COMPLETED) },
            emptyList()
        )

        courseLiveData.postValue(createCourseWorkInfo(courseProgress, uuid, WorkInfo.State.SUCCEEDED))

        assertEquals(ProgressState.COMPLETED, tabProgressItemViewModel.data.state)
    }

    private fun createItemViewModel(uuid: UUID): TabProgressItemViewModel {
        return TabProgressItemViewModel(
            TabProgressViewData(
                Tab.ASSIGNMENTS_ID,
                "Assignments",
                uuid.toString(),
                ProgressState.IN_PROGRESS
            ),
            workManager
        )
    }

    private fun createCourseWorkInfo(courseProgress: CourseProgress, uuid: UUID, state: WorkInfo.State): WorkInfo {
        return WorkInfo(
            uuid,
            state,
            if (state == WorkInfo.State.SUCCEEDED) workDataOf(CourseSyncWorker.OUTPUT to courseProgress.toJson()) else workDataOf(),
            listOf(CourseSyncWorker.TAG),
            workDataOf(
                CourseSyncWorker.COURSE_PROGRESS to courseProgress.toJson()
            ),
            0,
            0
        )
    }
}