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
import androidx.lifecycle.MutableLiveData
import com.instructure.horizon.R
import com.instructure.horizon.domain.usecase.GetLastSyncedAtUseCase
import com.instructure.horizon.domain.usecase.GetUnreadAnnouncementsUseCase
import com.instructure.horizon.features.dashboard.DashboardEventHandler
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.model.HorizonAnnouncement
import com.instructure.horizon.model.HorizonAnnouncementKind
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.ThemePrefs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
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

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardAnnouncementBannerViewModelTest {
    private val getUnreadAnnouncementsUseCase: GetUnreadAnnouncementsUseCase = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val eventHandler = DashboardEventHandler()
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)
    private val getLastSyncedAtUseCase: GetLastSyncedAtUseCase = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        mockkObject(ThemePrefs)
        every { ThemePrefs.brandColor } returns 1
        every { context.getString(R.string.notificationsAnnouncementCategoryLabel) } returns "Announcement"
        every { networkStateProvider.isOnlineLiveData } returns MutableLiveData(true)
        every { networkStateProvider.isOnline() } returns true
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun courseAnnouncement(title: String, courseId: Long, courseName: String): HorizonAnnouncement =
        HorizonAnnouncement(
            id = 1L,
            kind = HorizonAnnouncementKind.COURSE,
            courseId = courseId,
            courseName = courseName,
            title = title,
            message = "",
            authorDisplayName = null,
            postedAt = Date(),
            isUnread = true,
            isClosed = false,
        )

    private fun accountAnnouncement(title: String): HorizonAnnouncement =
        HorizonAnnouncement(
            id = 2L,
            kind = HorizonAnnouncementKind.ACCOUNT,
            courseId = null,
            courseName = null,
            title = title,
            message = "",
            authorDisplayName = null,
            postedAt = Date(),
            isUnread = true,
            isClosed = false,
        )

    @Test
    fun `Test initialization loads announcement data`() = runTest {
        coEvery { getUnreadAnnouncementsUseCase(match { !it.forceRefresh }) } returns listOf(
            courseAnnouncement("Test Announcement", 101L, "Course 101")
        )

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.SUCCESS, state.state)
        assertEquals(1, state.cardState.items.size)
        assertEquals("Test Announcement", state.cardState.items[0].title)
        coVerify { getUnreadAnnouncementsUseCase(match { !it.forceRefresh }) }
    }

    @Test
    fun `Test shows all announcements`() = runTest {
        coEvery { getUnreadAnnouncementsUseCase(match { !it.forceRefresh }) } returns listOf(
            courseAnnouncement("First Announcement", 101L, "Course 101"),
            accountAnnouncement("Second Announcement"),
        )

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.cardState.items.size)
        assertEquals("First Announcement", state.cardState.items[0].title)
        assertEquals("Second Announcement", state.cardState.items[1].title)
    }

    @Test
    fun `Test no announcements returns empty list`() = runTest {
        coEvery { getUnreadAnnouncementsUseCase(any()) } returns emptyList()

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.SUCCESS, state.state)
        assertTrue(state.cardState.items.isEmpty())
    }

    @Test
    fun `Test error state when use case throws exception`() = runTest {
        coEvery { getUnreadAnnouncementsUseCase(any()) } throws Exception("Network error")

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.ERROR, state.state)
    }

    @Test
    fun `Test refresh calls use case with forceNetwork true`() = runTest {
        coEvery { getUnreadAnnouncementsUseCase(any()) } returns listOf(
            courseAnnouncement("Test Announcement", 101L, "Course 101")
        )

        val viewModel = getViewModel()
        advanceUntilIdle()

        var completed = false
        viewModel.uiState.value.onRefresh { completed = true }
        advanceUntilIdle()

        assertTrue(completed)
        coVerify { getUnreadAnnouncementsUseCase(match { it.forceRefresh }) }
    }

    @Test
    fun `Test refresh updates state to loading then success`() = runTest {
        coEvery { getUnreadAnnouncementsUseCase(any()) } returns listOf(
            accountAnnouncement("Test Announcement")
        )

        val viewModel = getViewModel()
        advanceUntilIdle()

        var completed = false
        viewModel.uiState.value.onRefresh { completed = true }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.SUCCESS, state.state)
        assertTrue(completed)
    }

    @Test
    fun `Test refresh with error sets error state`() = runTest {
        coEvery { getUnreadAnnouncementsUseCase(match { !it.forceRefresh }) } returns listOf(
            courseAnnouncement("Test Announcement", 101L, "Course 101")
        )
        coEvery { getUnreadAnnouncementsUseCase(match { it.forceRefresh }) } throws Exception("Refresh failed")

        val viewModel = getViewModel()
        advanceUntilIdle()

        var completed = false
        viewModel.uiState.value.onRefresh { completed = true }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.ERROR, state.state)
        assertTrue(completed)
    }

    @Test
    fun `Test course announcement type`() = runTest {
        coEvery { getUnreadAnnouncementsUseCase(any()) } returns listOf(
            courseAnnouncement("Course Announcement", 123L, "Introduction to Kotlin")
        )

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.cardState.items.size)
        assertEquals("Introduction to Kotlin", state.cardState.items[0].source)
    }

    @Test
    fun `Test global announcement type`() = runTest {
        coEvery { getUnreadAnnouncementsUseCase(any()) } returns listOf(
            accountAnnouncement("Global Announcement")
        )

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.cardState.items.size)
        assertEquals(null, state.cardState.items[0].source)
    }

    private fun getViewModel(): DashboardAnnouncementBannerViewModel {
        return DashboardAnnouncementBannerViewModel(
            getUnreadAnnouncementsUseCase,
            eventHandler,
            context,
            networkStateProvider,
            featureFlagProvider,
            getLastSyncedAtUseCase,
        )
    }
}
