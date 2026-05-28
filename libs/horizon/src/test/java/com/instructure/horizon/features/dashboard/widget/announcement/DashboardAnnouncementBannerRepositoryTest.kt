/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.features.dashboard.widget.announcement

import android.content.Context
import com.instructure.horizon.domain.usecase.GetLastSyncedAtUseCase
import com.instructure.horizon.domain.usecase.GetUnreadAnnouncementsUseCase
import com.instructure.horizon.features.dashboard.DashboardEventHandler
import com.instructure.horizon.model.HorizonAnnouncement
import com.instructure.horizon.model.HorizonAnnouncementKind
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

/**
 * The old DashboardAnnouncementBannerRepository was a thin wrapper around
 * GetUnreadAnnouncementsUseCase that mapped HorizonAnnouncement to AnnouncementBannerItem.
 * The repository is gone; the same mapping happens inside DashboardAnnouncementBannerViewModel.
 * These tests now verify the mapping by driving the view model.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardAnnouncementBannerRepositoryTest {
    private val getUnreadAnnouncementsUseCase: GetUnreadAnnouncementsUseCase = mockk(relaxed = true)
    private val dashboardEventHandler: DashboardEventHandler = DashboardEventHandler()
    private val context: Context = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)
    private val getLastSyncedAtUseCase: GetLastSyncedAtUseCase = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { networkStateProvider.isOnline() } returns true
        coEvery { featureFlagProvider.offlineEnabled() } returns false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Maps course announcements to banner items`() = runTest {
        coEvery { getUnreadAnnouncementsUseCase(any()) } returns listOf(
            announcement(id = 1, kind = HorizonAnnouncementKind.COURSE, courseId = 100L, title = "Course A1", courseName = "Course 1"),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        val items = viewModel.uiState.value.cardState.items
        assertEquals(1, items.size)
        assertEquals("Course A1", items[0].title)
        assertEquals("Course 1", items[0].source)
    }

    @Test
    fun `Maps account announcements to banner items`() = runTest {
        coEvery { getUnreadAnnouncementsUseCase(any()) } returns listOf(
            announcement(id = 2, kind = HorizonAnnouncementKind.ACCOUNT, courseId = null, title = "Global", courseName = null),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        val items = viewModel.uiState.value.cardState.items
        assertEquals(1, items.size)
        assertEquals("Global", items[0].title)
        assertEquals(null, items[0].source)
    }

    @Test
    fun `Returns empty when use case returns empty`() = runTest {
        coEvery { getUnreadAnnouncementsUseCase(any()) } returns emptyList()

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.cardState.items.size)
    }

    private fun announcement(
        id: Long,
        kind: HorizonAnnouncementKind,
        courseId: Long?,
        title: String,
        courseName: String?,
    ) = HorizonAnnouncement(
        id = id,
        kind = kind,
        courseId = courseId,
        courseName = courseName,
        title = title,
        message = "",
        authorDisplayName = null,
        postedAt = Date(),
        isUnread = true,
        isClosed = false,
    )

    private fun createViewModel(): DashboardAnnouncementBannerViewModel {
        return DashboardAnnouncementBannerViewModel(
            getUnreadAnnouncementsUseCase,
            dashboardEventHandler,
            context,
            networkStateProvider,
            featureFlagProvider,
            getLastSyncedAtUseCase,
        )
    }
}
