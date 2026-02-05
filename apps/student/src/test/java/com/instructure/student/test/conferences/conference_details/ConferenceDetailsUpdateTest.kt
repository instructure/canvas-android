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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.models.ConferenceRecording
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.PlaybackFormat
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.mobius.conferences.conference_details.ConferenceDetailsEffect
import com.instructure.student.mobius.conferences.conference_details.ConferenceDetailsEvent
import com.instructure.student.mobius.conferences.conference_details.ConferenceDetailsModel
import com.instructure.student.mobius.conferences.conference_details.ConferenceDetailsUpdate
import com.instructure.student.test.util.matchesEffects
import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConferenceDetailsUpdateTest {
    private val initSpec = InitSpec(ConferenceDetailsUpdate()::init)
    private val updateSpec = UpdateSpec(ConferenceDetailsUpdate()::update)

    private val fakeDomain = "https://fake.domain.com"
    private lateinit var initModel: ConferenceDetailsModel
    private lateinit var baseConference: Conference
    private lateinit var canvasContext: CanvasContext

    @Before
    fun setup() {
        canvasContext = Course(id = 123L)
        baseConference = Conference(
            id = 123L,
            title = "Conference 123",
            description = "Description",
            joinUrl = "https://fake.join.url",
            recordings = listOf(
                ConferenceRecording(
                    recordingId = "recording_1",
                    playbackUrl = "https://some.fake.url/recording_1"
                ),
                ConferenceRecording(
                    recordingId = "recording_2",
                    playbackUrl = null,
                    playbackFormats = listOf(
                        PlaybackFormat(
                            length = "",
                            type = "presentation",
                            url = "https://some.fake.url/recording_2"
                        )
                    )
                )
            )
        )
        initModel = ConferenceDetailsModel(canvasContext, baseConference)

        mockkObject(ApiPrefs)
        every { ApiPrefs.fullDomain } returns fakeDomain
    }

    @After
    fun cleanup() {
        unmockkAll()
    }

    @Test
    fun `Initializes without effects`() {
        val expectedModel = initModel
        initSpec
            .whenInit(initModel)
            .then(
                assertThatFirst<ConferenceDetailsModel, ConferenceDetailsEffect>(
                    FirstMatchers.hasModel(expectedModel),
                    FirstMatchers.hasNoEffects()
                )
            )
    }

    @Test
    fun `PullToRefresh event forces network reload`() {
        val expectedModel = initModel.copy(isLoading = true)
        updateSpec
            .given(initModel)
            .whenEvent(ConferenceDetailsEvent.PullToRefresh)
            .then(
                assertThatNext<ConferenceDetailsModel, ConferenceDetailsEffect>(
                    NextMatchers.hasModel(expectedModel),
                    matchesEffects(ConferenceDetailsEffect.RefreshData(canvasContext))
                )
            )
    }

    @Test
    fun `RefreshFinished event updates the model on success`() {
        val newConference = baseConference.copy(title = "Updated Conference Title")
        val successResult = DataResult.Success(listOf(newConference))

        val inputModel = initModel.copy(isLoading = true)
        val expectedModel = initModel.copy(isLoading = false, conference = newConference)
        updateSpec
            .given(inputModel)
            .whenEvent(ConferenceDetailsEvent.RefreshFinished(successResult))
            .then(
                assertThatNext<ConferenceDetailsModel, ConferenceDetailsEffect>(
                    NextMatchers.hasModel(expectedModel),
                    NextMatchers.hasNoEffects()
                )
            )
    }

    @Test
    fun `RefreshFinished event updates the model and produces DisplayRefreshError effect on failure`() {
        val inputModel = initModel.copy(isLoading = true)
        val expectedModel = initModel.copy(isLoading = false)
        updateSpec
            .given(inputModel)
            .whenEvent(ConferenceDetailsEvent.RefreshFinished(DataResult.Fail()))
            .then(
                assertThatNext<ConferenceDetailsModel, ConferenceDetailsEffect>(
                    NextMatchers.hasModel(expectedModel),
                    matchesEffects(ConferenceDetailsEffect.DisplayRefreshError)
                )
            )
    }

    @Test
    fun `JoinConferenceClicked event updates the model and produces JoinConference effect with join url`() {
        val inputModel = initModel
        val expectedModel = inputModel.copy(isJoining = true)
        updateSpec
            .given(inputModel)
            .whenEvent(ConferenceDetailsEvent.JoinConferenceClicked)
            .then(
                assertThatNext<ConferenceDetailsModel, ConferenceDetailsEffect>(
                    NextMatchers.hasModel(expectedModel),
                    matchesEffects(ConferenceDetailsEffect.JoinConference(baseConference.joinUrl!!, false))
                )
            )
    }

    @Test
    fun `JoinConferenceClicked falls back to constructed url if join url is null`() {
        val inputModel = initModel.copy(conference = baseConference.copy(joinUrl = null))
        val expectedModel = inputModel.copy(isJoining = true)
        val expectedJoinUrl = "$fakeDomain${canvasContext.toAPIString()}/conferences/${baseConference.id}/join"
        updateSpec
            .given(inputModel)
            .whenEvent(ConferenceDetailsEvent.JoinConferenceClicked)
            .then(
                assertThatNext<ConferenceDetailsModel, ConferenceDetailsEffect>(
                    NextMatchers.hasModel(expectedModel),
                    matchesEffects(ConferenceDetailsEffect.JoinConference(expectedJoinUrl, true))
                )
            )
    }

    @Test
    fun `JoinConferenceFinished event updates the model `() {
        val inputModel = initModel.copy(isJoining = true)
        val expectedModel = inputModel.copy(isJoining = false)
        updateSpec
            .given(inputModel)
            .whenEvent(ConferenceDetailsEvent.JoinConferenceFinished)
            .then(
                assertThatNext<ConferenceDetailsModel, ConferenceDetailsEffect>(
                    NextMatchers.hasModel(expectedModel),
                    NextMatchers.hasNoEffects()
                )
            )
    }

    @Test
    fun `RecordingClicked event updates the model and produces ShowRecording effect with playback url`() {
        val recording = baseConference.recordings[0]
        val inputModel = initModel
        val expectedModel = inputModel.copy(launchingRecordings = mapOf(recording.recordingId to true))
        updateSpec
            .given(inputModel)
            .whenEvent(ConferenceDetailsEvent.RecordingClicked(recordingId = recording.recordingId))
            .then(
                assertThatNext<ConferenceDetailsModel, ConferenceDetailsEffect>(
                    NextMatchers.hasModel(expectedModel),
                    matchesEffects(ConferenceDetailsEffect.ShowRecording(recording.recordingId, recording.playbackUrl!!))
                )
            )
    }

    @Test
    fun `RecordingClicked falls back to first PlaybackFormat url if playbackUrl is null`() {
        val recording = baseConference.recordings[1]
        val inputModel = initModel
        val expectedModel = inputModel.copy(launchingRecordings = mapOf(recording.recordingId to true))
        updateSpec
            .given(inputModel)
            .whenEvent(ConferenceDetailsEvent.RecordingClicked(recordingId = recording.recordingId))
            .then(
                assertThatNext<ConferenceDetailsModel, ConferenceDetailsEffect>(
                    NextMatchers.hasModel(expectedModel),
                    matchesEffects(ConferenceDetailsEffect.ShowRecording(recording.recordingId, recording.playbackFormats[0].url))
                )
            )
    }

    @Test
    fun `RecordingClicked prioritizes presentation type over notes when playbackUrl is null`() {
        val conferenceWithMultipleFormats = baseConference.copy(
            recordings = listOf(
                ConferenceRecording(
                    recordingId = "recording_3",
                    playbackUrl = null,
                    playbackFormats = listOf(
                        PlaybackFormat(
                            length = null,
                            type = "notes",
                            url = "https://some.fake.url/recording_3/notes"
                        ),
                        PlaybackFormat(
                            length = "1",
                            type = "presentation",
                            url = "https://some.fake.url/recording_3/presentation"
                        )
                    )
                )
            )
        )
        val inputModel = initModel.copy(conference = conferenceWithMultipleFormats)
        val expectedModel = inputModel.copy(launchingRecordings = mapOf("recording_3" to true))
        updateSpec
            .given(inputModel)
            .whenEvent(ConferenceDetailsEvent.RecordingClicked(recordingId = "recording_3"))
            .then(
                assertThatNext(
                    NextMatchers.hasModel(expectedModel),
                    matchesEffects(
                        ConferenceDetailsEffect.ShowRecording(
                            "recording_3",
                            "https://some.fake.url/recording_3/presentation"
                        )
                    )
                )
            )
    }

    @Test
    fun `ShowRecordingFinished event updates the model`() {
        val recording = baseConference.recordings[0]
        val inputModel = initModel.copy(launchingRecordings = mapOf(recording.recordingId to true))
        val expectedModel = inputModel.copy(launchingRecordings = mapOf(recording.recordingId to false))
        updateSpec
            .given(inputModel)
            .whenEvent(ConferenceDetailsEvent.ShowRecordingFinished(recording.recordingId))
            .then(
                assertThatNext<ConferenceDetailsModel, ConferenceDetailsEffect>(
                    NextMatchers.hasModel(expectedModel),
                    NextMatchers.hasNoEffects()
                )
            )
    }

}
