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

package com.instructure.pandautils.features.dashboard.widget.conferences

import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.domain.usecase.conference.LoadLiveConferencesUseCase
import com.instructure.pandautils.domain.usecase.session.GetAuthenticatedSessionUseCase
import com.instructure.pandautils.models.ConferenceDashboardBlacklist
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ConferencesViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)

    private val testDispatcher = UnconfinedTestDispatcher()
    private val loadLiveConferencesUseCase: LoadLiveConferencesUseCase = mockk(relaxed = true)
    private val getAuthenticatedSessionUseCase: GetAuthenticatedSessionUseCase = mockk(relaxed = true)
    private val conferenceDashboardBlacklist: ConferenceDashboardBlacklist = mockk(relaxed = true)
    private val conferencesWidgetRouter: ConferencesWidgetRouter = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)

    private lateinit var viewModel: ConferencesViewModel

    @Before
    fun setUp() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)
        setupStrings()
        setupApiPrefs()

        every { conferenceDashboardBlacklist.conferenceDashboardBlacklist } returns emptySet()
    }

    private fun setupStrings() {
        every { resources.getString(R.string.conferencesWidgetDismissed) } returns "Conference dismissed"
    }

    private fun setupApiPrefs() {
        every { apiPrefs.fullDomain } returns "https://canvas.instructure.com"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `init loads conferences with force refresh`() = runTest {
        val conferences = listOf(
            ConferenceItem(1L, "Biology 101", "https://join.url", Course(id = 1, name = "Biology 101")),
            ConferenceItem(2L, "Chemistry 201", null, Course(id = 2, name = "Chemistry 201"))
        )
        coEvery { loadLiveConferencesUseCase(LoadLiveConferencesUseCase.Params(forceRefresh = true)) } returns conferences

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertFalse(state.error)
        assertEquals(2, state.conferences.size)
        assertEquals("Biology 101", state.conferences[0].subtitle)
        assertEquals("Chemistry 201", state.conferences[1].subtitle)
    }

    @Test
    fun `init shows error state when loading fails`() = runTest {
        coEvery { loadLiveConferencesUseCase(LoadLiveConferencesUseCase.Params(forceRefresh = true)) } throws Exception("Network error")

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertTrue(state.error)
        assertTrue(state.conferences.isEmpty())
    }

    @Test
    fun `onRefresh reloads conferences`() = runTest {
        val initialConferences = listOf(
            ConferenceItem(1L, "Biology 101", null, Course(id = 1))
        )
        val refreshedConferences = listOf(
            ConferenceItem(1L, "Biology 101", null, Course(id = 1)),
            ConferenceItem(2L, "Chemistry 201", null, Course(id = 2))
        )
        coEvery { loadLiveConferencesUseCase(LoadLiveConferencesUseCase.Params(forceRefresh = true)) } returns initialConferences andThen refreshedConferences

        viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.conferences.size)

        viewModel.uiState.value.onRefresh()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.conferences.size)
        coVerify(exactly = 2) { loadLiveConferencesUseCase(LoadLiveConferencesUseCase.Params(forceRefresh = true)) }
    }

    @Test
    fun `onJoinConference calls router with authenticated URL`() = runTest {
        val course = Course(id = 1, name = "Biology 101")
        val conference = ConferenceItem(
            id = 1L,
            subtitle = "Biology 101",
            joinUrl = "https://canvas.instructure.com/courses/1/conferences/1/join",
            canvasContext = course
        )
        val conferences = listOf(conference)
        val activity: FragmentActivity = mockk(relaxed = true)

        coEvery { loadLiveConferencesUseCase(any()) } returns conferences
        coEvery { getAuthenticatedSessionUseCase(any()) } returns "https://authenticated.session.url"

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.value.onJoinConference(activity, conference)
        advanceUntilIdle()

        verify { conferencesWidgetRouter.launchConference(activity, course, "https://authenticated.session.url") }
    }

    @Test
    fun `onJoinConference uses original URL when authentication fails`() = runTest {
        val course = Course(id = 1, name = "Biology 101")
        val conference = ConferenceItem(
            id = 1L,
            subtitle = "Biology 101",
            joinUrl = "https://external.conference.url/join",
            canvasContext = course
        )
        val conferences = listOf(conference)
        val activity: FragmentActivity = mockk(relaxed = true)

        coEvery { loadLiveConferencesUseCase(any()) } returns conferences
        coEvery { getAuthenticatedSessionUseCase(any()) } throws Exception("Auth failed")

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.value.onJoinConference(activity, conference)
        advanceUntilIdle()

        verify { conferencesWidgetRouter.launchConference(activity, course, "https://external.conference.url/join") }
    }

    @Test
    fun `onDismissConference removes conference and shows snackbar`() = runTest {
        val conferences = listOf(
            ConferenceItem(1L, "Biology 101", null, Course(id = 1)),
            ConferenceItem(2L, "Chemistry 201", null, Course(id = 2))
        )
        coEvery { loadLiveConferencesUseCase(any()) } returns conferences

        viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.conferences.size)

        viewModel.uiState.value.onDismissConference(conferences[0])
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.conferences.size)
        assertEquals("Chemistry 201", state.conferences[0].subtitle)
        assertNotNull(state.snackbarMessage)
        assertEquals("Conference dismissed", state.snackbarMessage?.message)
        verify { conferenceDashboardBlacklist.conferenceDashboardBlacklist = setOf("1") }
    }

    @Test
    fun `onClearSnackbar clears snackbar message`() = runTest {
        val conferences = listOf(
            ConferenceItem(1L, "Biology 101", null, Course(id = 1))
        )
        coEvery { loadLiveConferencesUseCase(any()) } returns conferences

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.value.onDismissConference(conferences[0])
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.snackbarMessage)

        viewModel.uiState.value.onClearSnackbar()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.snackbarMessage)
    }

    @Test
    fun `joining state is set during join and cleared after delay`() = runTest {
        val course = Course(id = 1, name = "Biology 101")
        val conference = ConferenceItem(1L, "Biology 101", "https://join.url", course)
        val activity: FragmentActivity = mockk(relaxed = true)

        coEvery { loadLiveConferencesUseCase(any()) } returns listOf(conference)
        coEvery { getAuthenticatedSessionUseCase(any()) } returns "https://auth.url"

        viewModel = createViewModel()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.joiningConferenceId)

        viewModel.uiState.value.onJoinConference(activity, conference)

        assertEquals(1L, viewModel.uiState.value.joiningConferenceId)

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.joiningConferenceId)
    }

    private fun createViewModel(): ConferencesViewModel {
        return ConferencesViewModel(
            loadLiveConferencesUseCase,
            getAuthenticatedSessionUseCase,
            conferenceDashboardBlacklist,
            conferencesWidgetRouter,
            apiPrefs,
            resources
        )
    }
}