/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.progress

import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.instructure.pandautils.R
import com.instructure.pandautils.features.offline.sync.AggregateProgressObserver
import com.instructure.pandautils.features.offline.sync.AggregateProgressViewData
import com.instructure.pandautils.features.offline.sync.ProgressState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveSyncProgressUseCaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val aggregateProgressObserver: AggregateProgressObserver = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val progressLiveData = MutableLiveData<AggregateProgressViewData?>()
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var useCase: ObserveSyncProgressUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { aggregateProgressObserver.progressData } returns progressLiveData
        every { resources.getString(R.string.syncProgress_syncingOfflineContent) } returns "Syncing offline content"
        every { resources.getQuantityString(R.plurals.syncProgress_itemCount, any(), any()) } answers {
            val count = arg<Int>(1)
            "$count items"
        }
        every { resources.getString(R.string.syncProgress_offlineContentSyncFailed) } returns "Sync failed"
        every { resources.getString(R.string.syncProgress_syncErrorSubtitle) } returns "Tap to retry"
        every { resources.getString(R.string.syncProgress_offlineContentSyncCompleted) } returns "Sync completed"
        every { resources.getString(R.string.syncProgress_downloadStarting) } returns "Download starting"
        every { resources.getString(R.string.syncProgress_syncQueued) } returns "Sync queued"

        useCase = ObserveSyncProgressUseCase(aggregateProgressObserver, resources)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Returns null when progress data is null`() = runTest {
        progressLiveData.value = null

        val result = useCase().first()

        assertNull(result)
    }

    @Test
    fun `Returns null when progress state is COMPLETED`() = runTest {
        progressLiveData.value = AggregateProgressViewData(
            title = "Completed",
            progressState = ProgressState.COMPLETED,
            progress = 100,
            itemCount = 5
        )

        val result = useCase().first()

        assertNull(result)
    }

    @Test
    fun `Returns SyncProgressItem for IN_PROGRESS state`() = runTest {
        progressLiveData.value = AggregateProgressViewData(
            title = "In Progress",
            progressState = ProgressState.IN_PROGRESS,
            progress = 50,
            itemCount = 3
        )

        val result = useCase().first()

        assertEquals("Syncing offline content", result?.title)
        assertEquals("3 items", result?.subtitle)
        assertEquals(50, result?.progress)
        assertEquals(ProgressState.IN_PROGRESS, result?.state)
        assertEquals(3, result?.itemCount)
    }

    @Test
    fun `Returns SyncProgressItem for ERROR state`() = runTest {
        progressLiveData.value = AggregateProgressViewData(
            title = "Error",
            progressState = ProgressState.ERROR,
            progress = 0,
            itemCount = 2
        )

        val result = useCase().first()

        assertEquals("Sync failed", result?.title)
        assertEquals("Tap to retry", result?.subtitle)
        assertEquals(ProgressState.ERROR, result?.state)
    }

    @Test
    fun `Returns SyncProgressItem for STARTING state`() = runTest {
        progressLiveData.value = AggregateProgressViewData(
            title = "Starting",
            progressState = ProgressState.STARTING,
            progress = 0,
            itemCount = 4
        )

        val result = useCase().first()

        assertEquals("Download starting", result?.title)
        assertEquals("Sync queued", result?.subtitle)
        assertEquals(ProgressState.STARTING, result?.state)
    }
}