/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.learn

import androidx.lifecycle.SavedStateHandle
import com.instructure.horizon.features.learn.navigation.LearnRoute
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LearnViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Initial state has COURSES tab selected by default`() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = LearnViewModel(savedStateHandle)

        val state = viewModel.state.value
        assertEquals(LearnTab.COURSES, state.selectedTab)
        assertEquals(LearnTab.entries, state.tabs)
    }

    @Test
    fun `Initial state restores selected tab from SavedStateHandle when provided`() {
        val savedStateHandle = SavedStateHandle(mapOf(
            LearnRoute.LearnScreen.selectedTabAttr to "programs"
        ))
        val viewModel = LearnViewModel(savedStateHandle)

        val state = viewModel.state.value
        assertEquals(LearnTab.PROGRAMS, state.selectedTab)
    }

    @Test
    fun `Initial state ignores invalid tab string from SavedStateHandle`() {
        val savedStateHandle = SavedStateHandle(mapOf(
            LearnRoute.LearnScreen.selectedTabAttr to "invalid_tab"
        ))
        val viewModel = LearnViewModel(savedStateHandle)

        val state = viewModel.state.value
        assertEquals(LearnTab.COURSES, state.selectedTab)
    }

    @Test
    fun `updateSelectedTabIndex updates selected tab`() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = LearnViewModel(savedStateHandle)

        viewModel.state.value.updateSelectedTabIndex(1)

        val state = viewModel.state.value
        assertEquals(LearnTab.PROGRAMS, state.selectedTab)
    }

    @Test
    fun `updateSelectedTabIndex with 0 selects COURSES tab`() {
        val savedStateHandle = SavedStateHandle(mapOf(
            LearnRoute.LearnScreen.selectedTabAttr to "programs"
        ))
        val viewModel = LearnViewModel(savedStateHandle)

        viewModel.state.value.updateSelectedTabIndex(0)

        val state = viewModel.state.value
        assertEquals(LearnTab.COURSES, state.selectedTab)
    }

    @Test
    fun `updateSelectedTab updates selected tab by string value`() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = LearnViewModel(savedStateHandle)

        viewModel.state.value.updateSelectedTab("programs")

        val state = viewModel.state.value
        assertEquals(LearnTab.PROGRAMS, state.selectedTab)
    }

    @Test
    fun `updateSelectedTab with courses string selects COURSES tab`() {
        val savedStateHandle = SavedStateHandle(mapOf(
            LearnRoute.LearnScreen.selectedTabAttr to "programs"
        ))
        val viewModel = LearnViewModel(savedStateHandle)

        viewModel.state.value.updateSelectedTab("courses")

        val state = viewModel.state.value
        assertEquals(LearnTab.COURSES, state.selectedTab)
    }

    @Test
    fun `updateSelectedTab with invalid string does not change tab`() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = LearnViewModel(savedStateHandle)
        val initialTab = viewModel.state.value.selectedTab

        viewModel.state.value.updateSelectedTab("invalid_value")

        val state = viewModel.state.value
        assertEquals(initialTab, state.selectedTab)
    }

    @Test
    fun `State callbacks are properly initialized`() {
        val savedStateHandle = SavedStateHandle()
        val viewModel = LearnViewModel(savedStateHandle)

        val state = viewModel.state.value
        assertEquals(LearnTab.entries, state.tabs)
        assertEquals(LearnTab.COURSES, state.selectedTab)
    }
}
