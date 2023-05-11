/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.emeritus.student.test.conferences.conference_details

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.models.ConferenceRecording
import com.instructure.canvasapi2.models.Course
import com.emeritus.student.R
import com.emeritus.student.mobius.conferences.conference_details.ConferenceDetailsModel
import com.emeritus.student.mobius.conferences.conference_details.ConferenceDetailsPresenter
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class ConferenceDetailsPresenterTest : Assert() {

    private lateinit var context: Context
    private lateinit var baseModel: ConferenceDetailsModel
    private lateinit var baseConference: Conference
    private lateinit var baseCanvasContext: CanvasContext

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        baseCanvasContext = Course(id = 123L)
        baseConference = Conference(
            id = 123L,
            startedAt = null,
            endedAt = null,
            title = "Conference Title",
            description = "This is the conference description",
            recordings = emptyList()
        )
        baseModel = ConferenceDetailsModel(
            canvasContext = baseCanvasContext,
            conference = baseConference,
            isLoading = false,
            isJoining = false,
            launchingRecordings = emptyMap()
        )
    }

    @Test
    fun `State is loading when refreshing`() {
        // Set model to loading
        val model = baseModel.copy(isLoading = true)

        // Generate state
        val state = ConferenceDetailsPresenter.present(model, context)

        // State isLoading should be true
        assertEquals(state.isLoading, true)
    }

    @Test
    fun `State is joining when joining a conference`() {
        // Set model to joining
        val model = baseModel.copy(isJoining = true)

        // Generate state
        val state = ConferenceDetailsPresenter.present(model, context)

        // State isJoining should be true
        assertEquals(state.isJoining, true)
    }

    @Test
    fun `Returns correct title `() {
        // Set conference title in model
        val title = "This is the conference title"
        val model = baseModel.copy(conference = baseConference.copy(title = title))

        // Generate state
        val state = ConferenceDetailsPresenter.present(model, context)

        // State should have correct title
        assertEquals(state.title, title)
    }

    @Test
    fun `Returns correct description`() {
        // Set conference description in model
        val description = "This is the conference description"
        val model = baseModel.copy(conference = baseConference.copy(description = description))

        // Generate state
        val state = ConferenceDetailsPresenter.present(model, context)

        // State should have correct description
        assertEquals(state.description, baseConference.description)
    }

    @Test
    fun `Returns no-description text if there is no description`() {
        // Set conference description to null
        val model = baseModel.copy(conference = baseConference.copy(description = null))

        // Generate state
        val state = ConferenceDetailsPresenter.present(model, context)

        // State description should match R.string.noConferenceDescription
        assertEquals(state.description, context.getString(R.string.noConferenceDescription))
    }

    @Test
    fun `State showJoinContainer is true if conference is started but not ended`() {
        // Set start date bu no end date
        val model = baseModel.copy(conference = baseConference.copy(startedAt = Date(), endedAt = null))

        // Generate state
        val state = ConferenceDetailsPresenter.present(model, context)

        // State showJoinContainer should be true
        assertEquals(state.showJoinContainer, true)
    }

    @Test
    fun `State showJoinContainer is false if conference is started and ended`() {
        // Set both start date and end date
        val model = baseModel.copy(conference = baseConference.copy(startedAt = Date(), endedAt = Date()))

        // Generate state
        val state = ConferenceDetailsPresenter.present(model, context)

        // State showJoinContainer should be false
        assertEquals(state.showJoinContainer, false)
    }

    @Test
    fun `State showJoinContainer is false if conference not started or ended`() {
        // Set neither the start date nor end date
        val model = baseModel.copy(conference = baseConference.copy(startedAt = null, endedAt = null))

        // Generate state
        val state = ConferenceDetailsPresenter.present(model, context)

        // State showJoinContainer should be false
        assertEquals(state.showJoinContainer, false)
    }

    @Test
    fun `Returns correct status for not-started conference`() {
        // Set neither the start date nor end date
        val model = baseModel.copy(conference = baseConference.copy(startedAt = null, endedAt = null))

        // Generate state
        val state = ConferenceDetailsPresenter.present(model, context)

        assertEquals(state.status, "Not Started")
    }

    @Test
    fun `Returns correct status for in-progress conference`() {
        // Set start date but no end date
        val startedAt = Calendar.getInstance().apply { set(2000, 0, 1, 8, 57) }.time
        val model = baseModel.copy(conference = baseConference.copy(startedAt = startedAt, endedAt = null))

        // Generate state
        val state = ConferenceDetailsPresenter.present(model, context)

        assertEquals(state.status, "Started Jan 1 at 8:57 AM")
    }

    @Test
    fun `Returns correct status for concluded conference`() {
        val startedAt = Calendar.getInstance().apply { set(2000, 0, 1, 8, 57) }.time
        val endedAt = Calendar.getInstance().apply { set(2000, 0, 1, 10, 21) }.time
        val model = baseModel.copy(conference = baseConference.copy(startedAt = startedAt, endedAt = endedAt))

        // Generate state
        val state = ConferenceDetailsPresenter.present(model, context)

        assertEquals(state.status, "Concluded Jan 1 at 10:21 AM")
    }

    @Test
    fun `showRecordingSection is false if there are no recordings`() {
        // Set recordings to empty list
        val model = baseModel.copy(conference = baseConference.copy(recordings = emptyList()))

        // Generate state
        val state = ConferenceDetailsPresenter.present(model, context)

        // showRecordingSection should be false
        assertEquals(state.showRecordingSection, false)
    }

    @Test
    fun `showRecordingSection is true if there are recordings`() {
        // Add a recording to the conference
        val recording = ConferenceRecording()
        val model = baseModel.copy(conference = baseConference.copy(recordings = listOf(recording)))

        // Generate state
        val state = ConferenceDetailsPresenter.present(model, context)

        // showRecordingSection should be false
        assertEquals(state.showRecordingSection, true)
    }

    @Test
    fun `maps recordings list to recording states`() {
        // Add a recording to the conference
        val recording = ConferenceRecording()
        val model = baseModel.copy(conference = baseConference.copy(recordings = listOf(recording)))

        // Generate state
        val state = ConferenceDetailsPresenter.present(model, context)
        val recordingState = ConferenceDetailsPresenter.mapRecordingStates(recording, context, emptyMap())

        // showRecordingSection should be false
        assertEquals(state.recordings.size, 1)

        // State should match output from mapRecordingStates
        assertEquals(state.recordings[0], recordingState)
    }

    @Test
    fun `mapRecordingStates returns correct recording id`() {
        // Set up recording id
        val recordingId = "conference_recording_123"
        val recording = ConferenceRecording(recordingId = recordingId)

        // Generate state
        val state = ConferenceDetailsPresenter.mapRecordingStates(recording, context, emptyMap())

        // State recordingId should match recording's id
        assertEquals(state.recordingId, recording.recordingId)
    }

    @Test
    fun `mapRecordingStates returns correct title`() {
        // Set up recording title
        val title = "Recording for Conference 123"
        val recording = ConferenceRecording(title = title)

        // Generate state
        val state = ConferenceDetailsPresenter.mapRecordingStates(recording, context, emptyMap())

        // Titles should match
        assertEquals(state.title, recording.title)
    }

    @Test
    fun `mapRecordingStates formats duration as minutes`() {
        // Set up recording duration
        val durationMinutes = 13L
        val recording = ConferenceRecording(durationMinutes = durationMinutes)

        // Generate state
        val state = ConferenceDetailsPresenter.mapRecordingStates(recording, context, emptyMap())

        // Should have formatted duration string
        val expected = context.resources.getQuantityString(R.plurals.minutes, durationMinutes.toInt(), durationMinutes)
        assertEquals(state.duration, expected)
    }

    @Test
    fun `mapRecordingStates formats date`() {
        // Set up recording date
        val createdAtMillis = Calendar.getInstance().apply { set(2000, 0, 1, 8, 57) }.timeInMillis
        val recording = ConferenceRecording(createdAtMillis = createdAtMillis)

        // Generate state
        val state = ConferenceDetailsPresenter.mapRecordingStates(recording, context, emptyMap())

        // Should have formatted date string
        assertEquals(state.date, "Jan 1 at 8:57 AM")
    }

    @Test
    fun `mapRecordingStates returns true for isLaunching if recording id is true in launch map`() {
        // Set up launch map with recording id set to true
        val recordingId = "conference_recording_123"
        val launchMap: Map<String, Boolean> = mapOf(recordingId to true)
        val recording = ConferenceRecording(recordingId = recordingId)

        // Generate state
        val state = ConferenceDetailsPresenter.mapRecordingStates(recording, context, launchMap)

        // isLaunching should be true
        assertEquals(state.isLaunching, true)
    }

    @Test
    fun `mapRecordingStates returns false for isLaunching if recording id is false in launch map`() {
        // Set up launch map with recording id set to false
        val recordingId = "conference_recording_123"
        val launchMap: Map<String, Boolean> = mapOf(recordingId to false)
        val recording = ConferenceRecording(recordingId = recordingId)

        // Generate state
        val state = ConferenceDetailsPresenter.mapRecordingStates(recording, context, launchMap)

        // isLaunching should be false
        assertEquals(state.isLaunching, false)
    }

    @Test
    fun `mapRecordingStates returns false for isLaunching if recording id is not found in launch map`() {
        // Set up an empty launch map
        val recordingId = "conference_recording_123"
        val launchMap: Map<String, Boolean> = emptyMap()
        val recording = ConferenceRecording(recordingId = recordingId)

        // Generate state
        val state = ConferenceDetailsPresenter.mapRecordingStates(recording, context, launchMap)

        // isLaunching should be false
        assertEquals(state.isLaunching, false)
    }
}
