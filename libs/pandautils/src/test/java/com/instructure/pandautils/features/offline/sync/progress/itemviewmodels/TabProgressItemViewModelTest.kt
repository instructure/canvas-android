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
import com.instructure.canvasapi2.models.Tab
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.pandautils.features.offline.sync.TabSyncData
import com.instructure.pandautils.features.offline.sync.progress.TabProgressViewData
import com.instructure.pandautils.room.offline.daos.CourseSyncProgressDao
import com.instructure.pandautils.room.offline.entities.CourseSyncProgressEntity
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import com.instructure.testutils.ViewModelTestRule
import org.junit.Test
import java.util.UUID

class TabProgressItemViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val courseSyncProgressDao: CourseSyncProgressDao = mockk(relaxed = true)

    private lateinit var tabProgressItemViewModel: TabProgressItemViewModel

    @Test
    fun `Progress updates`() {
        var courseProgress = CourseSyncProgressEntity(
            1L,
            "Course",
            CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.IN_PROGRESS) },
        )
        val courseLiveData = MutableLiveData(courseProgress)

        every { courseSyncProgressDao.findByCourseIdLiveData(1L) } returns courseLiveData

        tabProgressItemViewModel = createItemViewModel(1L)

        assertEquals(ProgressState.IN_PROGRESS, tabProgressItemViewModel.data.state)

        courseProgress = courseProgress.copy(
            tabs = CourseSyncSettingsEntity.TABS.associateWith { TabSyncData(it, ProgressState.COMPLETED) }
        )

        courseLiveData.postValue(courseProgress)

        assertEquals(ProgressState.COMPLETED, tabProgressItemViewModel.data.state)
    }

    private fun createItemViewModel(courseId: Long): TabProgressItemViewModel {
        return TabProgressItemViewModel(
            TabProgressViewData(
                Tab.ASSIGNMENTS_ID,
                "Assignments",
                courseId,
                ProgressState.IN_PROGRESS
            ),
            courseSyncProgressDao
        )
    }
}
