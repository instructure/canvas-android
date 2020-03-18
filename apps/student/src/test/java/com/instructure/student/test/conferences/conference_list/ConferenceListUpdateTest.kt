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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.student.mobius.conferences.conference_list.ConferenceListEffect
import com.instructure.student.mobius.conferences.conference_list.ConferenceListEvent
import com.instructure.student.mobius.conferences.conference_list.ConferenceListModel
import com.instructure.student.mobius.conferences.conference_list.ConferenceListUpdate
import com.instructure.student.test.util.matchesEffects
import com.instructure.student.test.util.matchesFirstEffects
import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConferenceListUpdateTest : Assert() {
    private val initSpec = InitSpec(ConferenceListUpdate()::init)
    private val updateSpec = UpdateSpec(ConferenceListUpdate()::update)

    private val fakeDomain = "https://fake.domain.com"
    private lateinit var initModel: ConferenceListModel
    private lateinit var canvasContext: CanvasContext

    @Before
    fun setup() {
        canvasContext = Course(id = 123L)
        initModel = ConferenceListModel(canvasContext)

        mockkStatic(ApiPrefs::class)
        every { ApiPrefs.fullDomain } returns fakeDomain
    }

    @Test
    fun `Initializes to loading state`() {
        val expectedModel = initModel.copy(isLoading = true)
        initSpec
            .whenInit(initModel)
            .then(
                assertThatFirst<ConferenceListModel, ConferenceListEffect>(
                    FirstMatchers.hasModel(expectedModel),
                    matchesFirstEffects(ConferenceListEffect.LoadData(canvasContext, false))
                )
            )
    }

    @Test
    fun `PullToRefresh event forces network reload`() {
        val expectedModel = initModel.copy(isLoading = true)
        updateSpec
            .given(initModel)
            .whenEvent(ConferenceListEvent.PullToRefresh)
            .then(
                assertThatNext<ConferenceListModel, ConferenceListEffect>(
                    NextMatchers.hasModel(expectedModel),
                    matchesEffects(ConferenceListEffect.LoadData(canvasContext, true))
                )
            )
    }

    @Test
    fun `DataLoaded event updates the model`() {
        val inputModel = initModel.copy(isLoading = true)
        val result = DataResult.Success(emptyList<Conference>())
        val expectedModel = initModel.copy(isLoading = false, listResult = result)
        updateSpec
            .given(inputModel)
            .whenEvent(ConferenceListEvent.DataLoaded(result))
            .then(
                assertThatNext<ConferenceListModel, ConferenceListEffect>(
                    NextMatchers.hasModel(expectedModel)
                )
            )
    }

    @Test
    fun `ConferenceClicked event produces ShowConferenceDetails event`() {
        val conference = Conference(id = 345L)
        val result = DataResult.Success(listOf(conference))
        val inputModel = initModel.copy(listResult = result)
        updateSpec
            .given(inputModel)
            .whenEvent(ConferenceListEvent.ConferenceClicked(conferenceId = conference.id))
            .then(
                assertThatNext<ConferenceListModel, ConferenceListEffect>(
                    matchesEffects(ConferenceListEffect.ShowConferenceDetails(conference))
                )
            )
    }

    @Test
    fun `LaunchInBrowser event produces updates model and produces LaunchInBrowser effect`() {
        val inputModel = initModel
        val expectedModel = inputModel.copy(isLaunchingInBrowser = true)
        updateSpec
            .given(inputModel)
            .whenEvent(ConferenceListEvent.LaunchInBrowser)
            .then(
                assertThatNext<ConferenceListModel, ConferenceListEffect>(
                    NextMatchers.hasModel(expectedModel),
                    matchesEffects(ConferenceListEffect.LaunchInBrowser("$fakeDomain/courses/123/conferences"))
                )
            )
    }

    @Test
    fun `LaunchInBrowserFinished event produces updates model`() {
        val inputModel = initModel.copy(isLaunchingInBrowser = true)
        val expectedModel = inputModel.copy(isLaunchingInBrowser = false)
        updateSpec
            .given(inputModel)
            .whenEvent(ConferenceListEvent.LaunchInBrowserFinished)
            .then(
                assertThatNext<ConferenceListModel, ConferenceListEffect>(
                    NextMatchers.hasModel(expectedModel)
                )
            )
    }
}
