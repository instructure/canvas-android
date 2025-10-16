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

import com.instructure.horizon.features.dashboard.DashboardItemState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
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
    private val repository: DashboardAnnouncementBannerRepository = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test initialization loads announcement data`() = runTest {
        val announcements = listOf(
            AnnouncementBannerItem(
                title = "Test Announcement",
                source = "Course 101",
                date = Date(),
                type = AnnouncementType.COURSE,
                route = "test/route"
            )
        )
        coEvery { repository.getUnreadAnnouncements(false) } returns announcements

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.SUCCESS, state.state)
        assertNotNull(state.cardState.announcement)
        assertEquals("Test Announcement", state.cardState.announcement?.title)
        coVerify { repository.getUnreadAnnouncements(false) }
    }

    @Test
    fun `Test shows only first announcement`() = runTest {
        val announcements = listOf(
            AnnouncementBannerItem(
                title = "First Announcement",
                source = "Course 101",
                date = Date(),
                type = AnnouncementType.COURSE,
                route = "test/route1"
            ),
            AnnouncementBannerItem(
                title = "Second Announcement",
                source = "Course 102",
                date = Date(),
                type = AnnouncementType.GLOBAL,
                route = "test/route2"
            )
        )
        coEvery { repository.getUnreadAnnouncements(false) } returns announcements

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("First Announcement", state.cardState.announcement?.title)
    }

    @Test
    fun `Test no announcements returns null`() = runTest {
        coEvery { repository.getUnreadAnnouncements(false) } returns emptyList()

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.SUCCESS, state.state)
        assertNull(state.cardState.announcement)
    }

    @Test
    fun `Test error state when repository throws exception`() = runTest {
        coEvery { repository.getUnreadAnnouncements(false) } throws Exception("Network error")

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DashboardItemState.ERROR, state.state)
    }

    @Test
    fun `Test refresh calls repository with forceNetwork true`() = runTest {
        val announcements = listOf(
            AnnouncementBannerItem(
                title = "Test Announcement",
                source = "Course 101",
                date = Date(),
                type = AnnouncementType.COURSE,
                route = "test/route"
            )
        )
        coEvery { repository.getUnreadAnnouncements(false) } returns announcements
        coEvery { repository.getUnreadAnnouncements(true) } returns announcements

        val viewModel = getViewModel()
        advanceUntilIdle()

        var completed = false
        viewModel.uiState.value.onRefresh { completed = true }
        advanceUntilIdle()

        assertTrue(completed)
        coVerify { repository.getUnreadAnnouncements(true) }
    }

    @Test
    fun `Test refresh updates state to loading then success`() = runTest {
        val announcements = listOf(
            AnnouncementBannerItem(
                title = "Test Announcement",
                source = "Course 101",
                date = Date(),
                type = AnnouncementType.GLOBAL,
                route = "test/route"
            )
        )
        coEvery { repository.getUnreadAnnouncements(any()) } returns announcements

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
        val announcements = listOf(
            AnnouncementBannerItem(
                title = "Test Announcement",
                source = "Course 101",
                date = Date(),
                type = AnnouncementType.COURSE,
                route = "test/route"
            )
        )
        coEvery { repository.getUnreadAnnouncements(false) } returns announcements
        coEvery { repository.getUnreadAnnouncements(true) } throws Exception("Refresh failed")

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
        val announcements = listOf(
            AnnouncementBannerItem(
                title = "Course Announcement",
                source = "Introduction to Kotlin",
                date = Date(),
                type = AnnouncementType.COURSE,
                route = "courses/123/announcements/456"
            )
        )
        coEvery { repository.getUnreadAnnouncements(false) } returns announcements

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AnnouncementType.COURSE, state.cardState.announcement?.type)
        assertEquals("Introduction to Kotlin", state.cardState.announcement?.source)
    }

    @Test
    fun `Test global announcement type`() = runTest {
        val announcements = listOf(
            AnnouncementBannerItem(
                title = "Global Announcement",
                source = "Canvas Career",
                date = Date(),
                type = AnnouncementType.GLOBAL,
                route = "horizon/inbox/account_notification/123"
            )
        )
        coEvery { repository.getUnreadAnnouncements(false) } returns announcements

        val viewModel = getViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AnnouncementType.GLOBAL, state.cardState.announcement?.type)
        assertEquals("Canvas Career", state.cardState.announcement?.source)
    }

    private fun getViewModel(): DashboardAnnouncementBannerViewModel {
        return DashboardAnnouncementBannerViewModel(repository)
    }
}
