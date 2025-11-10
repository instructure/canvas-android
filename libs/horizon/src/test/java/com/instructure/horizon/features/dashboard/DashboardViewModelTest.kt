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
package com.instructure.horizon.features.dashboard

import com.instructure.canvasapi2.models.UnreadNotificationCount
import com.instructure.pandautils.utils.ThemePrefs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    private val repository: DashboardRepository = mockk(relaxed = true)
    private val themePrefs: ThemePrefs = mockk(relaxed = true)
    private val dashboardEventHandler: DashboardEventHandler = DashboardEventHandler()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val notificationCounts = listOf(
        UnreadNotificationCount(
            type = "Message",
            count = 5,
            unreadCount = 10,
        ),
        UnreadNotificationCount(
            type = "Conversation",
            count = 2,
            unreadCount = 5,
        ),
        UnreadNotificationCount(
            type = "Announcement",
            count = 1,
            unreadCount = 3,
        ),
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.getUnreadCounts(any()) } returns notificationCounts
        coEvery { themePrefs.mobileLogoUrl } returns ""
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test ViewModel successfully loads and filters unread counts`() {
        val viewModel = getViewModel()
        coVerify { repository.getUnreadCounts(true) }

        val state = viewModel.uiState.value
        assertEquals(5, state.unreadCountState.unreadConversations)
        assertEquals(10, state.unreadCountState.unreadNotifications)
    }

    @Test
    fun `Test ViewModel loads logo URL from ThemePrefs`() {
        val logoUrl = "https://example.com/logo.png"
        coEvery { themePrefs.mobileLogoUrl } returns logoUrl

        val viewModel = getViewModel()
        val state = viewModel.uiState.value
        assertEquals(logoUrl, state.logoUrl)
    }

    @Test
    fun `Refresh event triggers refresh and updates unread counts`() = runTest {
        val viewModel = getViewModel()

        val updatedCounts = listOf(
            UnreadNotificationCount(
                type = "Message",
                count = 8,
                unreadCount = 15,
            ),
            UnreadNotificationCount(
                type = "Conversation",
                count = 3,
                unreadCount = 8,
            ),
        )
        coEvery { repository.getUnreadCounts(any()) } returns updatedCounts

        dashboardEventHandler.postEvent(DashboardEvent.DashboardRefresh)
        testScheduler.advanceUntilIdle()

        coVerify(atLeast = 2) { repository.getUnreadCounts(true) }
        val state = viewModel.uiState.value
        assertEquals(8, state.unreadCountState.unreadConversations)
        assertEquals(15, state.unreadCountState.unreadNotifications)
    }

    @Test
    fun `ShowSnackbar event updates snackbar message in UI state`() = runTest {
        val viewModel = getViewModel()

        val testMessage = "Test snackbar message"
        dashboardEventHandler.postEvent(DashboardEvent.ShowSnackbar(testMessage))
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(testMessage, state.snackbarMessage)
    }

    @Test
    fun `Multiple refresh events update state correctly`() = runTest {
        val viewModel = getViewModel()

        dashboardEventHandler.postEvent(DashboardEvent.DashboardRefresh)
        testScheduler.advanceUntilIdle()

        val secondCounts = listOf(
            UnreadNotificationCount(type = "Conversation", count = 10, unreadCount = 20)
        )
        coEvery { repository.getUnreadCounts(any()) } returns secondCounts

        dashboardEventHandler.postEvent(DashboardEvent.DashboardRefresh)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(20, state.unreadCountState.unreadConversations)
    }

    private fun getViewModel(): DashboardViewModel {
        return DashboardViewModel(repository, themePrefs, dashboardEventHandler)
    }
}