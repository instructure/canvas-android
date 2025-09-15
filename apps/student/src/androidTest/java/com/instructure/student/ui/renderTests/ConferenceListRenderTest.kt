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
package com.instructure.student.ui.renderTests

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.student.mobius.conferences.conference_list.ui.ConferenceListItemViewState
import com.instructure.student.mobius.conferences.conference_list.ui.ConferenceListRepositoryFragment
import com.instructure.student.mobius.conferences.conference_list.ui.ConferenceListViewState
import com.instructure.student.ui.utils.StudentRenderTest
import com.spotify.mobius.runners.WorkRunner
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ConferenceListRenderTest : StudentRenderTest() {
    private val canvasContext: CanvasContext = Course(id = 123L, name = "Test Course")
    private val canvasContextGroup: CanvasContext = Group(id = 1337L, name = "Test Group")

    @Test
    fun displaysToolbarItems() {
        val state = ConferenceListViewState.Loaded(isLaunchingInBrowser = false, itemStates =  emptyList())
        loadPageWithViewState(state, canvasContext)

        conferenceListRenderPage.assertDisplaysToolbarTitle("Conferences")
        conferenceListRenderPage.assertDisplaysToolbarSubtitle(canvasContext.name!!)
        conferenceListRenderPage.assertDisplaysLaunching(false)
    }

    @Test
    fun displaysLoadingState() {
        val state = ConferenceListViewState.Loading(isLaunchingInBrowser = false)
        loadPageWithViewState(state, canvasContext)

        conferenceListRenderPage.assertLoading(true)
    }

    @Test
    fun displaysLaunchingInBrowserState() {
        val state = ConferenceListViewState.Loaded(isLaunchingInBrowser = true, itemStates =  emptyList())
        loadPageWithViewState(state, canvasContext)

        conferenceListRenderPage.assertDisplaysLaunching(true)
    }

    @Test
    fun displaysErrorState() {
        val state = ConferenceListViewState.Loaded(false, listOf(ConferenceListItemViewState.Error))
        loadPageWithViewState(state, canvasContext)

        conferenceListRenderPage.assertDisplaysError()
    }

    @Test
    fun displaysEmptyState() {
        val state = ConferenceListViewState.Loaded(false, listOf(ConferenceListItemViewState.Empty))
        loadPageWithViewState(state, canvasContext)

        conferenceListRenderPage.assertDisplaysEmptyView()
    }

    @Test
    fun displaysListItems() {
        val tint = Color.BLUE
        val itemStates = listOf(
            ConferenceListItemViewState.ConferenceHeader("Header 1"),
            ConferenceListItemViewState.ConferenceItem(
                tint = tint,
                title = "Conference 1",
                subtitle = "Conference 1 Description",
                label = "In Progress",
                labelTint = tint,
                conferenceId = 0,
                isJoinable = false
            ),
            ConferenceListItemViewState.ConferenceItem(
                tint = tint,
                title = "Conference 2",
                subtitle = "Conference 2 Description",
                label = "Not Started",
                labelTint = tint,
                conferenceId = 0,
                isJoinable = false
            ),
            ConferenceListItemViewState.ConferenceHeader("Header 2"),
            ConferenceListItemViewState.ConferenceItem(
                tint = tint,
                title = "Conference 3",
                subtitle = "Conference 3 Description",
                label = "Concluded",
                labelTint = tint,
                conferenceId = 0,
                isJoinable = false
            )
        )
        val state = ConferenceListViewState.Loaded(false, itemStates)
        loadPageWithViewState(state, canvasContext)

        conferenceListRenderPage.assertDisplaysListItems(itemStates)
    }


    @Test
    fun displaysToolbarItemsWithGroup() {
        val state = ConferenceListViewState.Loaded(isLaunchingInBrowser = false, itemStates =  emptyList())
        loadPageWithViewState(state, canvasContextGroup)
        conferenceListRenderPage.assertDisplaysToolbarTitle("Conferences")
        conferenceListRenderPage.assertDisplaysToolbarSubtitle(canvasContextGroup.name!!)
        conferenceListRenderPage.assertDisplaysLaunching(false)
    }

    private fun loadPageWithViewState(state: ConferenceListViewState, canvasContext: CanvasContext) {
        val emptyEffectRunner = object : WorkRunner {
            override fun dispose() = Unit
            override fun post(runnable: Runnable) = Unit
        }
        val route = ConferenceListRepositoryFragment.makeRoute(canvasContext)
        val fragment = ConferenceListRepositoryFragment.newInstance(route)!!.apply {
            overrideInitViewState = state
            loopMod = { it.effectRunner { emptyEffectRunner } }
        }
        activityRule.activity.loadFragment(fragment)
    }
}
