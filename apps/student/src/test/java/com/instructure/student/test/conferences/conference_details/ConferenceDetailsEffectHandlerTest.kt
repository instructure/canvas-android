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
package com.instructure.student.test.conferences.conference_details

import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.mobius.conferences.conference_details.ConferenceDetailsEffect
import com.instructure.student.mobius.conferences.conference_details.ConferenceDetailsEffectHandler
import com.instructure.student.mobius.conferences.conference_details.ConferenceDetailsEvent
import com.instructure.student.mobius.conferences.conference_details.ConferenceDetailsRepository
import com.instructure.student.mobius.conferences.conference_details.ui.ConferenceDetailsView
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
class ConferenceDetailsEffectHandlerTest : Assert() {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val view: ConferenceDetailsView = mockk(relaxed = true)
    private val repository: ConferenceDetailsRepository = mockk(relaxed = true)
    private val effectHandler = ConferenceDetailsEffectHandler(repository).apply {
        view = this@ConferenceDetailsEffectHandlerTest.view
    }
    private val eventConsumer: Consumer<ConferenceDetailsEvent> = mockk(relaxed = true)
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

    @Test
    fun `ShowRecording calls launchUrl on view and produces ShowRecordingFinished event`() = runTest {
        val recordingId = "recording_123"
        val url = "url"

        connection.accept(ConferenceDetailsEffect.ShowRecording(recordingId, url))

        // Should call launchUrl on view
        verify { view.launchUrl(url) }

        // Advance the clock to skip delay
        advanceUntilIdle()

        // Should produce ShowRecordingFinished event
        verify { eventConsumer.accept(ConferenceDetailsEvent.ShowRecordingFinished(recordingId)) }

        confirmVerified(view)
        confirmVerified(eventConsumer)
    }

    @Test
    fun `JoinConference calls launchUrl and produces JoinConferenceFinished event`() = runTest {
        val url = "url"
        val authenticate = false

        connection.accept(ConferenceDetailsEffect.JoinConference(url, authenticate))

        // Should call launchUrl on view
        verify { view.launchUrl(url) }

        // Advance the clock to skip delay
        advanceUntilIdle()

        // Should produce JoinConferenceFinished event
        verify { eventConsumer.accept(ConferenceDetailsEvent.JoinConferenceFinished) }

        confirmVerified(view)
        confirmVerified(eventConsumer)
    }

    @Test
    fun `JoinConference calls API when authenticate is true`() = runTest {
        val url = "url"
        val sessionUrl = "session-url"
        val authenticate = true

        // Mock API
        coEvery { repository.getAuthenticatedSession(any()) } returns AuthenticatedSession(sessionUrl)

        connection.accept(ConferenceDetailsEffect.JoinConference(url, authenticate))

        // Should call launchUrl on view
        verify { view.launchUrl(sessionUrl) }

        // Should call API
        coVerify { repository.getAuthenticatedSession(url) }

        // Advance the clock to skip delay
        advanceUntilIdle()

        // Should produce JoinConferenceFinished event
        verify { eventConsumer.accept(ConferenceDetailsEvent.JoinConferenceFinished) }

        confirmVerified(view)
        confirmVerified(eventConsumer)
    }

    @Suppress("DeferredResultUnused")
    @Test
    fun `RefreshData calls API and produces RefreshFinished event`() = runTest {
        val canvasContext: CanvasContext = Course(id = 123L)
        val apiResult = DataResult.Success(emptyList<Conference>())

        // Mock API
        coEvery { repository.getConferencesForContext(any(), any()) } returns apiResult

        connection.accept(ConferenceDetailsEffect.RefreshData(canvasContext))

        coVerify { repository.getConferencesForContext(canvasContext, true) }
        verify { eventConsumer.accept(ConferenceDetailsEvent.RefreshFinished(apiResult)) }

        confirmVerified(repository)
        confirmVerified(eventConsumer)
    }

    @Test
    fun `DisplayRefreshError calls displayRefreshError on view`() {
        connection.accept(ConferenceDetailsEffect.DisplayRefreshError)
        verify { view.displayRefreshError() }
        confirmVerified(view)
    }
}
