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
import com.instructure.testutils.ViewModelTestRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val repository: DashboardRepository = mockk(relaxed = true)
    private val themePrefs: ThemePrefs = mockk(relaxed = true)

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
        coEvery { repository.getUnreadCounts(any()) } returns notificationCounts
        coEvery { themePrefs.mobileLogoUrl } returns ""
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

    private fun getViewModel(): DashboardViewModel {
        return DashboardViewModel(repository, themePrefs)
    }
}
