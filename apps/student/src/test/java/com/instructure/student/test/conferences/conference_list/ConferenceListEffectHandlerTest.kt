/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.student.test.conferences.conference_list

import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.mobius.conferences.conference_list.ConferenceListEffect
import com.instructure.student.mobius.conferences.conference_list.ConferenceListEffectHandler
import com.instructure.student.mobius.conferences.conference_list.ConferenceListEvent
import com.instructure.student.mobius.conferences.conference_list.ConferenceListRepository
import com.instructure.student.mobius.conferences.conference_list.ui.ConferenceListView
import com.spotify.mobius.functions.Consumer
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ConferenceListEffectHandlerTest : Assert() {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val view: ConferenceListView = mockk(relaxed = true)
    private val repository: ConferenceListRepository = mockk(relaxed = true)
    private val effectHandler = ConferenceListEffectHandler(repository).apply {
        view = this@ConferenceListEffectHandlerTest.view
    }
    private val eventConsumer: Consumer<ConferenceListEvent> = mockk(relaxed = true)
    private val connection = effectHandler.connect(eventConsumer)

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun cleanUp() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Suppress("DeferredResultUnused")
    @Test
    fun `LoadData calls API and returns DataLoaded event`() {
        val canvasContext: CanvasContext = Course(id = 123L)
        val refresh = true
        val apiResult = DataResult.Success<List<Conference>>(emptyList())

        // Mock API
        coEvery { repository.getConferencesForContext(any(), any()) } returns apiResult

        connection.accept(ConferenceListEffect.LoadData(canvasContext, refresh))

        coVerify { repository.getConferencesForContext(canvasContext, refresh) }
        verify { eventConsumer.accept(ConferenceListEvent.DataLoaded(apiResult)) }

        confirmVerified(repository)
        confirmVerified(eventConsumer)
    }

    @Test
    fun `LaunchInBrowser calls API, calls launchUrl and produces LaunchInBrowserFinished`() = runTest {
        val url = "url"
        val sessionUrl = "session-url"

        // Mock API
        coEvery { repository.getAuthenticatedSession(any()) } returns AuthenticatedSession(sessionUrl)

        connection.accept(ConferenceListEffect.LaunchInBrowser(url))

        // Should call launchUrl on view
        verify { view.launchUrl(sessionUrl) }

        // Should call API
        coVerify { repository.getAuthenticatedSession(url) }

        // Advance the clock to skip delay
        advanceUntilIdle()

        // Should produce LaunchInBrowserFinished event
        verify { eventConsumer.accept(ConferenceListEvent.LaunchInBrowserFinished) }

        confirmVerified(view)
        confirmVerified(eventConsumer)
    }

    @Test
    fun `ShowConferenceDetails calls showConferenceDetails on view`() {
        val conference = Conference(id = 123L)
        connection.accept(ConferenceListEffect.ShowConferenceDetails(conference))
        verify { view.showConferenceDetails(conference)}
        confirmVerified(view)
    }
}
