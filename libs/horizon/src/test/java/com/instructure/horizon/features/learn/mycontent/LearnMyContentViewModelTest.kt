/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.features.learn.mycontent

import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.horizon.features.learn.LearnEvent
import com.instructure.horizon.features.learn.LearnEventHandler
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryFilterScreenType
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibrarySortOption
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryTypeFilter
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LearnMyContentViewModelTest {

    private val eventHandler = LearnEventHandler()
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
    fun `Initial state has InProgress selected tab`() = runTest {
        val viewModel = getViewModel()

        assertEquals(LearnMyContentTab.InProgress, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun `Initial state has MostRecent sort option`() = runTest {
        val viewModel = getViewModel()

        assertEquals(LearnLearningLibrarySortOption.MostRecent, viewModel.uiState.value.sortByOption)
    }

    @Test
    fun `Initial state has All type filter`() = runTest {
        val viewModel = getViewModel()

        assertEquals(LearnLearningLibraryTypeFilter.All, viewModel.uiState.value.typeFilter)
    }

    @Test
    fun `Initial state has zero active filter count`() = runTest {
        val viewModel = getViewModel()

        assertEquals(0, viewModel.uiState.value.activeFilterCount)
    }

    @Test
    fun `Initial state has empty search query`() = runTest {
        val viewModel = getViewModel()

        assertEquals("", viewModel.uiState.value.searchQuery.text)
    }

    @Test
    fun `updateSearchQuery updates searchQuery in state`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSearchQuery(TextFieldValue("kotlin"))

        assertEquals("kotlin", viewModel.uiState.value.searchQuery.text)
    }

    @Test
    fun `onTabSelected updates selected tab`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onTabSelected(LearnMyContentTab.Completed)

        assertEquals(LearnMyContentTab.Completed, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun `onTabSelected resets sortByOption to MostRecent`() = runTest {
        val viewModel = getViewModel()
        eventHandler.postEvent(
            LearnEvent.UpdateLearningLibraryFilter(
                screenType = LearnLearningLibraryFilterScreenType.MyContent,
                typeFilter = LearnLearningLibraryTypeFilter.All,
                sortOption = LearnLearningLibrarySortOption.NameAscending,
            )
        )

        viewModel.uiState.value.onTabSelected(LearnMyContentTab.Saved)

        assertEquals(LearnLearningLibrarySortOption.MostRecent, viewModel.uiState.value.sortByOption)
    }

    @Test
    fun `onTabSelected resets typeFilter to All`() = runTest {
        val viewModel = getViewModel()
        eventHandler.postEvent(
            LearnEvent.UpdateLearningLibraryFilter(
                screenType = LearnLearningLibraryFilterScreenType.MyContent,
                typeFilter = LearnLearningLibraryTypeFilter.Programs,
                sortOption = LearnLearningLibrarySortOption.MostRecent,
            )
        )

        viewModel.uiState.value.onTabSelected(LearnMyContentTab.Completed)

        assertEquals(LearnLearningLibraryTypeFilter.All, viewModel.uiState.value.typeFilter)
    }

    @Test
    fun `onTabSelected resets activeFilterCount to zero`() = runTest {
        val viewModel = getViewModel()
        eventHandler.postEvent(
            LearnEvent.UpdateLearningLibraryFilter(
                screenType = LearnLearningLibraryFilterScreenType.MyContent,
                typeFilter = LearnLearningLibraryTypeFilter.Courses,
                sortOption = LearnLearningLibrarySortOption.MostRecent,
            )
        )

        viewModel.uiState.value.onTabSelected(LearnMyContentTab.InProgress)

        assertEquals(0, viewModel.uiState.value.activeFilterCount)
    }

    @Test
    fun `UpdateLearningLibraryFilter with MyContent screenType updates sortByOption`() = runTest {
        val viewModel = getViewModel()

        eventHandler.postEvent(
            LearnEvent.UpdateLearningLibraryFilter(
                screenType = LearnLearningLibraryFilterScreenType.MyContent,
                typeFilter = LearnLearningLibraryTypeFilter.All,
                sortOption = LearnLearningLibrarySortOption.NameDescending,
            )
        )

        assertEquals(LearnLearningLibrarySortOption.NameDescending, viewModel.uiState.value.sortByOption)
    }

    @Test
    fun `UpdateLearningLibraryFilter with MyContent screenType updates typeFilter`() = runTest {
        val viewModel = getViewModel()

        eventHandler.postEvent(
            LearnEvent.UpdateLearningLibraryFilter(
                screenType = LearnLearningLibraryFilterScreenType.MyContent,
                typeFilter = LearnLearningLibraryTypeFilter.Programs,
                sortOption = LearnLearningLibrarySortOption.MostRecent,
            )
        )

        assertEquals(LearnLearningLibraryTypeFilter.Programs, viewModel.uiState.value.typeFilter)
    }

    @Test
    fun `UpdateLearningLibraryFilter with MyContent screenType increments activeFilterCount for non-All filter`() = runTest {
        val viewModel = getViewModel()

        eventHandler.postEvent(
            LearnEvent.UpdateLearningLibraryFilter(
                screenType = LearnLearningLibraryFilterScreenType.MyContent,
                typeFilter = LearnLearningLibraryTypeFilter.Courses,
                sortOption = LearnLearningLibrarySortOption.MostRecent,
            )
        )

        assertEquals(1, viewModel.uiState.value.activeFilterCount)
    }

    @Test
    fun `UpdateLearningLibraryFilter with MyContent screenType resets activeFilterCount for All filter`() = runTest {
        val viewModel = getViewModel()
        eventHandler.postEvent(
            LearnEvent.UpdateLearningLibraryFilter(
                screenType = LearnLearningLibraryFilterScreenType.MyContent,
                typeFilter = LearnLearningLibraryTypeFilter.Programs,
                sortOption = LearnLearningLibrarySortOption.MostRecent,
            )
        )

        eventHandler.postEvent(
            LearnEvent.UpdateLearningLibraryFilter(
                screenType = LearnLearningLibraryFilterScreenType.MyContent,
                typeFilter = LearnLearningLibraryTypeFilter.All,
                sortOption = LearnLearningLibrarySortOption.MostRecent,
            )
        )

        assertEquals(0, viewModel.uiState.value.activeFilterCount)
    }

    @Test
    fun `UpdateLearningLibraryFilter with MyContentSaved screenType updates state`() = runTest {
        val viewModel = getViewModel()

        eventHandler.postEvent(
            LearnEvent.UpdateLearningLibraryFilter(
                screenType = LearnLearningLibraryFilterScreenType.MyContentSaved,
                typeFilter = LearnLearningLibraryTypeFilter.Courses,
                sortOption = LearnLearningLibrarySortOption.LeastRecent,
            )
        )

        assertEquals(LearnLearningLibraryTypeFilter.Courses, viewModel.uiState.value.typeFilter)
        assertEquals(LearnLearningLibrarySortOption.LeastRecent, viewModel.uiState.value.sortByOption)
    }

    @Test
    fun `UpdateLearningLibraryFilter with Browse screenType is ignored`() = runTest {
        val viewModel = getViewModel()

        eventHandler.postEvent(
            LearnEvent.UpdateLearningLibraryFilter(
                screenType = LearnLearningLibraryFilterScreenType.Browse,
                typeFilter = LearnLearningLibraryTypeFilter.Programs,
                sortOption = LearnLearningLibrarySortOption.NameAscending,
            )
        )

        assertEquals(LearnLearningLibraryTypeFilter.All, viewModel.uiState.value.typeFilter)
        assertEquals(LearnLearningLibrarySortOption.MostRecent, viewModel.uiState.value.sortByOption)
        assertEquals(0, viewModel.uiState.value.activeFilterCount)
    }

    private fun getViewModel() = LearnMyContentViewModel(eventHandler)
}
