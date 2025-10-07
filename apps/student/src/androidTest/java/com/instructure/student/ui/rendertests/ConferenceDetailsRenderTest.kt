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
package com.instructure.student.ui.rendertests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.student.mobius.conferences.conference_details.ui.ConferenceDetailsRepositoryFragment
import com.instructure.student.mobius.conferences.conference_details.ui.ConferenceDetailsViewState
import com.instructure.student.mobius.conferences.conference_details.ui.ConferenceRecordingViewState
import com.instructure.student.ui.utils.StudentRenderTest
import com.spotify.mobius.runners.WorkRunner
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ConferenceDetailsRenderTest : StudentRenderTest() {
    private val canvasContext: CanvasContext = Course(id = 123L, name = "Test Course")
    private val canvasContextGroup: CanvasContext = Group(id = 1337L, name = "Test Group")

    private lateinit var baseState: ConferenceDetailsViewState
    private lateinit var baseRecordingState: ConferenceRecordingViewState

    @Before
    fun setup() {
        baseRecordingState = ConferenceRecordingViewState(
            recordingId = "recording_1",
            title = "Recording 1 Title",
            date = "Jan 1 at 11:55 AM",
            duration = "13 Minutes",
            isLaunching = false
        )
        baseState = ConferenceDetailsViewState(
            isLoading = false,
            isJoining = false,
            title = "Conference Title",
            status = "Conference Status",
            description = "Conference Description",
            showJoinContainer = true,
            showRecordingSection = true,
            recordings = listOf(baseRecordingState)
        )
    }

    @Test
    fun displaysToolbarItems() {
        val state = baseState
        loadPageWithViewState(state, canvasContext)

        conferenceDetailsRenderPage.assertDisplaysToolbarTitle("Conference Details")
        conferenceDetailsRenderPage.assertDisplaysToolbarSubtitle(canvasContext.name!!)
    }

    @Test
    fun displaysRefreshingState() {
        val state = baseState.copy(isLoading = true)
        loadPageWithViewState(state, canvasContext)

        conferenceDetailsRenderPage.assertRefreshing(true)
    }

    @Test
    fun displaysJoinableState() {
        val state = baseState.copy(
            isJoining = false,
            showJoinContainer = true
        )
        loadPageWithViewState(state, canvasContext)

        conferenceDetailsRenderPage.assertDisplaysJoinable()
    }

    @Test
    fun displaysJoiningState() {
        val state = baseState.copy(
            isJoining = true,
            showJoinContainer = true
        )
        loadPageWithViewState(state, canvasContext)

        conferenceDetailsRenderPage.assertDisplaysJoining()
    }

    @Test
    fun displaysInProgressIndicator() {
        val state = baseState.copy(
            isJoining = false,
            showJoinContainer = true
        )
        loadPageWithViewState(state, canvasContext)

        conferenceDetailsRenderPage.assertDisplaysInProgressIndicator(shouldDisplay = true)
    }

    @Test
    fun hidesInProgressIndicator() {
        val state = baseState.copy(
            isJoining = false,
            showJoinContainer = false
        )
        loadPageWithViewState(state, canvasContext)

        conferenceDetailsRenderPage.assertDisplaysInProgressIndicator(shouldDisplay = false)
    }

    @Test
    fun displaysConferenceTitle() {
        val state = baseState.copy(title = "Test Title")
        loadPageWithViewState(state, canvasContext)

        conferenceDetailsRenderPage.assertDisplaysTitle(state.title)
    }

    @Test
    fun displaysConferenceStatus() {
        val state = baseState.copy(status = "Test Status")
        loadPageWithViewState(state, canvasContext)

        conferenceDetailsRenderPage.assertDisplaysStatus(state.status)
    }

    @Test
    fun displaysConferenceDescription() {
        val state = baseState.copy(description = "Test Description")
        loadPageWithViewState(state, canvasContext)

        conferenceDetailsRenderPage.assertDisplaysDescription(state.description)
    }

    @Test
    fun displaysRecordingSection() {
        loadPageWithViewState(baseState, canvasContext)
        conferenceDetailsRenderPage.assertDisplaysRecordingSection(shouldDisplay = true)
    }

    @Test
    fun hidesRecordingSection() {
        val state = baseState.copy(
            showRecordingSection = false,
            recordings = emptyList()
        )
        loadPageWithViewState(state, canvasContext)
        conferenceDetailsRenderPage.assertDisplaysRecordingSection(shouldDisplay = false)
    }

    @Test
    fun displaysRecording() {
        loadPageWithViewState(baseState, canvasContext)
        conferenceDetailsRenderPage.assertDisplaysRecording(baseRecordingState)
    }

    @Test
    fun displaysLaunchingRecording() {
        val recordingState = baseRecordingState.copy(
            isLaunching = true
        )
        val state = baseState.copy(
            recordings = listOf(recordingState)
        )
        loadPageWithViewState(state, canvasContext)
        conferenceDetailsRenderPage.assertDisplaysRecording(recordingState)
    }


    @Test
    fun displaysToolbarItemsWithGroup() {
        val state = baseState
        loadPageWithViewState(state, canvasContextGroup)

        conferenceDetailsRenderPage.assertDisplaysToolbarTitle("Conference Details")
        conferenceDetailsRenderPage.assertDisplaysToolbarSubtitle(canvasContextGroup.name!!)
    }

    private fun loadPageWithViewState(state: ConferenceDetailsViewState, canvasContext: CanvasContext) {
        val emptyEffectRunner = object : WorkRunner {
            override fun dispose() = Unit
            override fun post(runnable: Runnable) = Unit
        }
        val route = ConferenceDetailsRepositoryFragment.makeRoute(canvasContext, Conference())
        val fragment = ConferenceDetailsRepositoryFragment.newInstance(route)!!.apply {
            overrideInitViewState = state
            loopMod = { it.effectRunner { emptyEffectRunner } }
        }
        activityRule.activity.loadFragment(fragment)
    }
}
