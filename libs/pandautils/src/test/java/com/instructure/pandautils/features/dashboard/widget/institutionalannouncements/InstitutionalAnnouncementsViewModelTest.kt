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

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.AccountNotification
import com.instructure.pandautils.domain.models.accountnotification.InstitutionalAnnouncement
import com.instructure.pandautils.domain.usecase.accountnotification.DeleteAccountNotificationUseCase
import com.instructure.pandautils.domain.usecase.accountnotification.LoadInstitutionalAnnouncementsParams
import com.instructure.pandautils.domain.usecase.accountnotification.LoadInstitutionalAnnouncementsUseCase
import com.instructure.pandautils.features.dashboard.widget.GlobalConfig
import com.instructure.pandautils.features.dashboard.widget.usecase.ObserveGlobalConfigUseCase
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.ThemedColor
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
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
    private val deleteAccountNotificationUseCase: DeleteAccountNotificationUseCase = mockk(relaxed = true)
    private val observeGlobalConfigUseCase: ObserveGlobalConfigUseCase = mockk(relaxed = true)
    private val crashlytics: FirebaseCrashlytics = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        coEvery { observeGlobalConfigUseCase(Unit) } returns flowOf(GlobalConfig())
        every { crashlytics.recordException(any()) } just Runs

        mockkObject(ColorKeeper)
        every { ColorKeeper.createThemedColor(any()) } returns ThemedColor(0, 0)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Initial load shows loading state then success`() {
        val announcements = listOf(
            InstitutionalAnnouncement(
                id = 1L,
                subject = "Test Announcement",
                message = "Test Message",
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
                startDate = Date(1000L),
                icon = "info",
                logoUrl = ""
            ),
            InstitutionalAnnouncement(
                id = 2L,
                subject = "Announcement 2",
                message = "Message 2",
                startDate = Date(2000L),
                icon = "warning",
                logoUrl = ""
            ),
            InstitutionalAnnouncement(
                id = 3L,
                subject = "Announcement 3",
                message = "Message 3",
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

    @Test
    fun `observeConfig updates color`() = runTest {
        val testColor = 0xFF00FF00.toInt()
        val themedColor = ThemedColor(testColor, testColor)
        every { ColorKeeper.createThemedColor(testColor) } returns themedColor
        coEvery { observeGlobalConfigUseCase(Unit) } returns flowOf(GlobalConfig(backgroundColor = testColor))
        coEvery { loadInstitutionalAnnouncementsUseCase(LoadInstitutionalAnnouncementsParams(forceRefresh = true)) } returns emptyList()

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(themedColor, state.color)
    }

    private fun createViewModel(): InstitutionalAnnouncementsViewModel {
        return InstitutionalAnnouncementsViewModel(
            loadInstitutionalAnnouncementsUseCase,
            deleteAccountNotificationUseCase,
            observeGlobalConfigUseCase,
            crashlytics
        )
    }

    @Test
    fun `Dismiss removes announcement from list immediately`() {
        val announcements = listOf(
            InstitutionalAnnouncement(
                id = 1L,
                subject = "Announcement 1",
                message = "Message 1",
                startDate = Date(),
                icon = "info",
                logoUrl = ""
            ),
            InstitutionalAnnouncement(
                id = 2L,
                subject = "Announcement 2",
                message = "Message 2",
                startDate = Date(),
                icon = "warning",
                logoUrl = ""
            ),
            InstitutionalAnnouncement(
                id = 3L,
                subject = "Announcement 3",
                message = "Message 3",
                startDate = Date(),
                icon = "calendar",
                logoUrl = ""
            )
        )

        coEvery {
            loadInstitutionalAnnouncementsUseCase(LoadInstitutionalAnnouncementsParams(forceRefresh = true))
        } returns announcements

        coEvery {
            deleteAccountNotificationUseCase(any())
        } returns AccountNotification(id = 2L)

        val viewModel = createViewModel()

        assertEquals(3, viewModel.uiState.value.announcements.size)

        viewModel.uiState.value.onDismiss(2L)

        assertEquals(2, viewModel.uiState.value.announcements.size)
        assertEquals(1L, viewModel.uiState.value.announcements[0].id)
        assertEquals(3L, viewModel.uiState.value.announcements[1].id)
    }

    @Test
    fun `Dismiss calls deleteAccountNotificationUseCase with correct id`() {
        val announcements = listOf(
            InstitutionalAnnouncement(
                id = 1L,
                subject = "Test",
                message = "Message",
                startDate = Date(),
                icon = "info",
                logoUrl = ""
            )
        )

        coEvery {
            loadInstitutionalAnnouncementsUseCase(LoadInstitutionalAnnouncementsParams(forceRefresh = true))
        } returns announcements

        coEvery {
            deleteAccountNotificationUseCase(any())
        } returns AccountNotification(id = 1L)

        val viewModel = createViewModel()

        viewModel.uiState.value.onDismiss(1L)

        coVerify { deleteAccountNotificationUseCase(DeleteAccountNotificationUseCase.Params(1L)) }
    }

    @Test
    fun `Dismiss handles error without affecting UI state`() {
        val announcements = listOf(
            InstitutionalAnnouncement(
                id = 1L,
                subject = "Test",
                message = "Message",
                startDate = Date(),
                icon = "info",
                logoUrl = ""
            )
        )

        coEvery {
            loadInstitutionalAnnouncementsUseCase(LoadInstitutionalAnnouncementsParams(forceRefresh = true))
        } returns announcements

        val exception = Exception("Network error")
        coEvery {
            deleteAccountNotificationUseCase(any())
        } throws exception

        val viewModel = createViewModel()

        viewModel.uiState.value.onDismiss(1L)

        assertTrue(viewModel.uiState.value.announcements.isEmpty())
        every { crashlytics.recordException(exception) } just Runs
    }

    @Test
    fun `Dismiss multiple announcements removes only specified ones`() {
        val announcements = listOf(
            InstitutionalAnnouncement(
                id = 1L,
                subject = "Announcement 1",
                message = "Message 1",
                startDate = Date(),
                icon = "info",
                logoUrl = ""
            ),
            InstitutionalAnnouncement(
                id = 2L,
                subject = "Announcement 2",
                message = "Message 2",
                startDate = Date(),
                icon = "warning",
                logoUrl = ""
            ),
            InstitutionalAnnouncement(
                id = 3L,
                subject = "Announcement 3",
                message = "Message 3",
                startDate = Date(),
                icon = "calendar",
                logoUrl = ""
            )
        )

        coEvery {
            loadInstitutionalAnnouncementsUseCase(LoadInstitutionalAnnouncementsParams(forceRefresh = true))
        } returns announcements

        coEvery {
            deleteAccountNotificationUseCase(any())
        } returns AccountNotification(id = 1L)

        val viewModel = createViewModel()

        assertEquals(3, viewModel.uiState.value.announcements.size)

        viewModel.uiState.value.onDismiss(1L)
        viewModel.uiState.value.onDismiss(3L)

        assertEquals(1, viewModel.uiState.value.announcements.size)
        assertEquals(2L, viewModel.uiState.value.announcements[0].id)
    }
}