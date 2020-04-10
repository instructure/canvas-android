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

import com.instructure.canvasapi2.managers.ConferenceManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.student.mobius.conferences.conference_details.ConferenceDetailsEffect
import com.instructure.student.mobius.conferences.conference_details.ConferenceDetailsEffectHandler
import com.instructure.student.mobius.conferences.conference_details.ConferenceDetailsEvent
import com.instructure.student.mobius.conferences.conference_details.ui.ConferenceDetailsView
import com.spotify.mobius.functions.Consumer
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ConferenceDetailsEffectHandlerTest : Assert() {
    private val testDispatcher = TestCoroutineDispatcher()
    private val view: ConferenceDetailsView = mockk(relaxed = true)
    private val effectHandler =
        ConferenceDetailsEffectHandler().apply { view = this@ConferenceDetailsEffectHandlerTest.view }
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
        testDispatcher.cleanupTestCoroutines()
        clearAllMocks()
    }

    fun test(block: suspend TestCoroutineScope.() -> Unit) = testDispatcher.runBlockingTest(block)

    @Test
    fun `ShowRecording calls launchUrl on view, waits 3000ms, and produces ShowRecordingFinished event`() = test {
        val recordingId = "recording_123"
        val url = "url"

        connection.accept(ConferenceDetailsEffect.ShowRecording(recordingId, url))

        // Should call launchUrl on view
        verify { view.launchUrl(url) }

        // Advance the clock to skip delay
        val timeSkipped = advanceUntilIdle()

        // Time skipped should be 3000ms
        assertEquals(timeSkipped, 3000)

        // Should produce ShowRecordingFinished event
        verify { eventConsumer.accept(ConferenceDetailsEvent.ShowRecordingFinished(recordingId)) }

        confirmVerified(view)
        confirmVerified(eventConsumer)
    }

    @Test
    fun `JoinConference calls launchUrl, waits 3000ms, and produces JoinConferenceFinished event`() = test {
        val url = "url"
        val authenticate = false

        connection.accept(ConferenceDetailsEffect.JoinConference(url, authenticate))

        // Should call launchUrl on view
        verify { view.launchUrl(url) }

        // Advance the clock to skip delay
        val timeSkipped = advanceUntilIdle()

        // Time skipped should be 3000ms
        assertEquals(timeSkipped, 3000)

        // Should produce JoinConferenceFinished event
        verify { eventConsumer.accept(ConferenceDetailsEvent.JoinConferenceFinished) }

        confirmVerified(view)
        confirmVerified(eventConsumer)
    }

    @Test
    fun `JoinConference calls API when authenticate is true`() = test {
        val url = "url"
        val sessionUrl = "session-url"
        val authenticate = true

        // Mock API
        mockkStatic("com.instructure.canvasapi2.utils.weave.AwaitApiKt")
        coEvery { awaitApi<AuthenticatedSession>(any()) } returns AuthenticatedSession(sessionUrl)

        connection.accept(ConferenceDetailsEffect.JoinConference(url, authenticate))

        // Should call launchUrl on view
        verify { view.launchUrl(sessionUrl) }

        // Should call API
        coVerify { awaitApi<AuthenticatedSession>(any()) }

        // Advance the clock to skip delay
        val timeSkipped = advanceUntilIdle()

        // Time skipped should be 3000ms
        assertEquals(timeSkipped, 3000)

        // Should produce JoinConferenceFinished event
        verify { eventConsumer.accept(ConferenceDetailsEvent.JoinConferenceFinished) }

        confirmVerified(view)
        confirmVerified(eventConsumer)
    }

    @Suppress("DeferredResultUnused")
    @Test
    fun `RefreshData calls API and produces RefreshFinished event`() = test {
        val canvasContext: CanvasContext = Course(id = 123L)
        val apiResult = DataResult.Success(emptyList<Conference>())

        // Mock API
        mockkObject(ConferenceManager)
        every { ConferenceManager.getConferencesForContextAsync(any(), any()) } returns mockk {
            coEvery { await() } returns apiResult
        }

        connection.accept(ConferenceDetailsEffect.RefreshData(canvasContext))

        verify { ConferenceManager.getConferencesForContextAsync(canvasContext, true) }
        verify { eventConsumer.accept(ConferenceDetailsEvent.RefreshFinished(apiResult)) }

        confirmVerified(ConferenceManager)
        confirmVerified(eventConsumer)
    }

    @Test
    fun `DisplayRefreshError calls displayRefreshError on view`() {
        connection.accept(ConferenceDetailsEffect.DisplayRefreshError)
        verify { view.displayRefreshError() }
        confirmVerified(view)
    }
}
