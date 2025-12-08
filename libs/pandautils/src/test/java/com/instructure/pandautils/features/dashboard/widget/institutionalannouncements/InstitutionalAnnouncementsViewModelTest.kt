/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
 */
package com.instructure.pandautils.features.dashboard.widget.institutionalannouncements

import com.instructure.pandautils.domain.models.accountnotification.InstitutionalAnnouncement
import com.instructure.pandautils.domain.usecase.accountnotification.LoadInstitutionalAnnouncementsParams
import com.instructure.pandautils.domain.usecase.accountnotification.LoadInstitutionalAnnouncementsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class InstitutionalAnnouncementsViewModelTest {

    private val loadInstitutionalAnnouncementsUseCase: LoadInstitutionalAnnouncementsUseCase = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Initial load shows loading state then success`() {
        val announcements = listOf(
            InstitutionalAnnouncement(
                id = 1L,
                subject = "Test Announcement",
                message = "Test Message",
                institutionName = "",
                startDate = Date(),
                icon = "info",
                logoUrl = ""
            )
        )

        coEvery {
            loadInstitutionalAnnouncementsUseCase(LoadInstitutionalAnnouncementsParams(forceRefresh = true))
        } returns announcements

        val viewModel = createViewModel()

        assertFalse(viewModel.uiState.value.loading)
        assertFalse(viewModel.uiState.value.error)
        assertEquals(1, viewModel.uiState.value.announcements.size)
        assertEquals("Test Announcement", viewModel.uiState.value.announcements[0].subject)
    }

    @Test
    fun `Load error shows error state`() {
        coEvery {
            loadInstitutionalAnnouncementsUseCase(any())
        } throws Exception("Network error")

        val viewModel = createViewModel()

        assertFalse(viewModel.uiState.value.loading)
        assertTrue(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.announcements.isEmpty())
    }

    @Test
    fun `Refresh loads announcements with forceRefresh`() {
        val initialAnnouncements = listOf(
            InstitutionalAnnouncement(
                id = 1L,
                subject = "Initial",
                message = "Message",
                institutionName = "",
                startDate = Date(),
                icon = "info",
                logoUrl = ""
            )
        )

        val refreshedAnnouncements = listOf(
            InstitutionalAnnouncement(
                id = 2L,
                subject = "Refreshed",
                message = "Message",
                institutionName = "",
                startDate = Date(),
                icon = "warning",
                logoUrl = ""
            )
        )

        coEvery {
            loadInstitutionalAnnouncementsUseCase(LoadInstitutionalAnnouncementsParams(forceRefresh = true))
        } returns initialAnnouncements andThen refreshedAnnouncements

        val viewModel = createViewModel()

        assertEquals("Initial", viewModel.uiState.value.announcements[0].subject)

        viewModel.uiState.value.onRefresh()

        assertEquals("Refreshed", viewModel.uiState.value.announcements[0].subject)
    }

    @Test
    fun `Empty announcements list returns empty state`() {
        coEvery {
            loadInstitutionalAnnouncementsUseCase(LoadInstitutionalAnnouncementsParams(forceRefresh = true))
        } returns emptyList()

        val viewModel = createViewModel()

        assertFalse(viewModel.uiState.value.loading)
        assertFalse(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.announcements.isEmpty())
    }

    @Test
    fun `Multiple announcements are loaded correctly`() {
        val announcements = listOf(
            InstitutionalAnnouncement(
                id = 1L,
                subject = "Announcement 1",
                message = "Message 1",
                institutionName = "",
                startDate = Date(1000L),
                icon = "info",
                logoUrl = ""
            ),
            InstitutionalAnnouncement(
                id = 2L,
                subject = "Announcement 2",
                message = "Message 2",
                institutionName = "",
                startDate = Date(2000L),
                icon = "warning",
                logoUrl = ""
            ),
            InstitutionalAnnouncement(
                id = 3L,
                subject = "Announcement 3",
                message = "Message 3",
                institutionName = "",
                startDate = Date(3000L),
                icon = "calendar",
                logoUrl = ""
            )
        )

        coEvery {
            loadInstitutionalAnnouncementsUseCase(LoadInstitutionalAnnouncementsParams(forceRefresh = true))
        } returns announcements

        val viewModel = createViewModel()

        assertFalse(viewModel.uiState.value.loading)
        assertFalse(viewModel.uiState.value.error)
        assertEquals(3, viewModel.uiState.value.announcements.size)
        assertEquals("Announcement 1", viewModel.uiState.value.announcements[0].subject)
        assertEquals("Announcement 2", viewModel.uiState.value.announcements[1].subject)
        assertEquals("Announcement 3", viewModel.uiState.value.announcements[2].subject)
    }

    @Test
    fun `Refresh after error recovers to success state`() {
        coEvery {
            loadInstitutionalAnnouncementsUseCase(any())
        } throws Exception("Network error")

        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.announcements.isEmpty())

        val announcements = listOf(
            InstitutionalAnnouncement(
                id = 1L,
                subject = "Recovered",
                message = "Message",
                institutionName = "",
                startDate = Date(),
                icon = "info",
                logoUrl = ""
            )
        )

        coEvery {
            loadInstitutionalAnnouncementsUseCase(LoadInstitutionalAnnouncementsParams(forceRefresh = true))
        } returns announcements

        viewModel.uiState.value.onRefresh()

        assertFalse(viewModel.uiState.value.loading)
        assertFalse(viewModel.uiState.value.error)
        assertEquals(1, viewModel.uiState.value.announcements.size)
        assertEquals("Recovered", viewModel.uiState.value.announcements[0].subject)
    }

    private fun createViewModel(): InstitutionalAnnouncementsViewModel {
        return InstitutionalAnnouncementsViewModel(loadInstitutionalAnnouncementsUseCase)
    }
}