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

package com.instructure.pandautils.features.dashboard.notifications.itemviewmodels

import android.content.res.Resources
import com.instructure.pandautils.R
import com.instructure.pandautils.features.dashboard.notifications.SyncProgressViewData
import com.instructure.pandautils.features.offline.sync.AggregateProgressViewData
import com.instructure.pandautils.features.offline.sync.ProgressState
import com.instructure.testutils.ViewModelTestRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SyncProgressItemViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val resources: Resources = mockk(relaxed = true)

    private lateinit var itemViewModel: SyncProgressItemViewModel

    @Before
    fun setup() {
        setupStrings()
        itemViewModel = SyncProgressItemViewModel(SyncProgressViewData(), mockk(), mockk(), resources)
    }

    private fun setupStrings() {
        every { resources.getString(R.string.syncProgress_syncingOfflineContent) } returns "Syncing Offline Content"

        val captor = slot<Int>()
        every {
            resources.getQuantityString(
                R.plurals.syncProgress_itemCount,
                capture(captor),
                any()
            )
        } answers { "${captor.captured} items are syncing" }

        every { resources.getString(R.string.syncProgress_offlineContentSyncFailed) } returns "Offline Content Sync Failed"
        every { resources.getString(R.string.syncProgress_syncErrorSubtitle) } returns "Sync Error Subtitle"
        every { resources.getString(R.string.syncProgress_offlineContentSyncCompleted) } returns "Offline Content Sync Completed"
        every { resources.getString(R.string.syncProgress_downloadStarting) } returns "Download Starting"
        every { resources.getString(R.string.syncProgress_syncQueued) } returns "Queued"
    }

    @Test
    fun `Update data`() {
        val progressData = AggregateProgressViewData(
            title = "title",
            totalSize = "10000 B",
            progress = 50,
            progressState = ProgressState.IN_PROGRESS,
            itemCount = 1
        )

        itemViewModel.update(progressData)

        val expected = SyncProgressViewData(
            title = "Syncing Offline Content",
            subtitle = "1 items are syncing",
            progress = 50,
            progressState = ProgressState.IN_PROGRESS
        )

        assertEquals(expected, itemViewModel.data)
    }

    @Test
    fun `Update error`() {
        val progressData = AggregateProgressViewData(
            title = "title",
            totalSize = "10000 B",
            progress = 50,
            progressState = ProgressState.ERROR,
            itemCount = 1
        )

        itemViewModel.update(progressData)

        val expected = SyncProgressViewData(
            title = "Offline Content Sync Failed",
            subtitle = "Sync Error Subtitle",
            progress = 50,
            progressState = ProgressState.ERROR
        )

        assertEquals(expected, itemViewModel.data)
    }

    @Test
    fun `Update starting`() {
        val progressData = AggregateProgressViewData(
            title = "title",
            totalSize = "10000 B",
            progress = 50,
            progressState = ProgressState.STARTING,
            itemCount = 1
        )

        itemViewModel.update(progressData)

        val expected = SyncProgressViewData(
            title = "Download Starting",
            subtitle = "Queued",
            progress = 50,
            progressState = ProgressState.STARTING
        )

        assertEquals(expected, itemViewModel.data)
    }

    @Test
    fun `Update completed`() {
        val progressData = AggregateProgressViewData(
            title = "title",
            totalSize = "10000 B",
            progress = 50,
            progressState = ProgressState.COMPLETED,
            itemCount = 1
        )

        itemViewModel.update(progressData)

        val expected = SyncProgressViewData(
            title = "Offline Content Sync Completed",
            subtitle = "",
            progress = 50,
            progressState = ProgressState.COMPLETED
        )

        assertEquals(expected, itemViewModel.data)
    }
}
