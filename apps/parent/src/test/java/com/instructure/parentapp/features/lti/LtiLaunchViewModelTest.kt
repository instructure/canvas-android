/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.features.lti

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.LTITool
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LtiLaunchViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val repository: LtiLaunchRepository = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: LtiLaunchViewModel

    @Before
    fun setup() {
        every { savedStateHandle.get<String>(LtiLaunchFragment.LTI_URL) } returns "url"
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Launch custom tab when lti tool url is successful`() = runTest {
        val ltiTool = LTITool(url = "url")
        coEvery { repository.getLtiFromAuthenticationUrl("url") } returns ltiTool

        viewModel = LtiLaunchViewModel(savedStateHandle, repository)

        val events = mutableListOf<LtiLaunchAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(events[0], LtiLaunchAction.LaunchCustomTab("url"))
    }

    @Test
    fun `Show error when lti tool url is null`() = runTest {
        val ltiTool = LTITool(url = null)
        coEvery { repository.getLtiFromAuthenticationUrl("url") } returns ltiTool

        viewModel = LtiLaunchViewModel(savedStateHandle, repository)

        val events = mutableListOf<LtiLaunchAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(events[0], LtiLaunchAction.ShowError)
    }

    @Test
    fun `Show error when lti request fails`() = runTest {
        coEvery { repository.getLtiFromAuthenticationUrl("url") } throws Exception()

        viewModel = LtiLaunchViewModel(savedStateHandle, repository)

        val events = mutableListOf<LtiLaunchAction>()

        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        assertEquals(events[0], LtiLaunchAction.ShowError)
    }
}