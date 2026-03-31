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
package com.instructure.horizon.features.learn.filter

import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import com.instructure.horizon.features.learn.LearnEvent
import com.instructure.horizon.features.learn.LearnEventHandler
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryFilterScreenType
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibrarySortOption
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryTypeFilter
import com.instructure.horizon.features.learn.navigation.LearnRoute
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LearnLearningLibraryFilterViewModelTest {

    private val resources: Resources = mockk(relaxed = true)
    private val eventHandler = LearnEventHandler()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { resources.getString(any()) } returns ""
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Browse screenType includes standard item type filters excluding Programs`() = runTest {
        every { resources.getString(LearnLearningLibraryTypeFilter.Programs.labelRes) } returns "Programs"
        val viewModel = getViewModel(screenType = LearnLearningLibraryFilterScreenType.Browse)

        val typeSection = viewModel.uiState.value.sections.last()
        val labels = typeSection.items.map { it.label }

        assertTrue(labels.none { it == "Programs" })
        assertTrue(typeSection.items.size > 2)
    }

    @Test
    fun `MyContent screenType includes only All, Programs and Courses filters`() = runTest {
        every { resources.getString(LearnLearningLibraryTypeFilter.All.labelRes) } returns "All"
        every { resources.getString(LearnLearningLibraryTypeFilter.Programs.labelRes) } returns "Programs"
        every { resources.getString(LearnLearningLibraryTypeFilter.Courses.labelRes) } returns "Courses"
        val viewModel = getViewModel(screenType = LearnLearningLibraryFilterScreenType.MyContent)

        val typeSection = viewModel.uiState.value.sections.last()

        assertEquals(3, typeSection.items.size)
    }

    @Test
    fun `MyContentSaved screenType has same filter count as Browse`() = runTest {
        val browseViewModel = getViewModel(screenType = LearnLearningLibraryFilterScreenType.Browse)
        val savedViewModel = getViewModel(screenType = LearnLearningLibraryFilterScreenType.MyContentSaved)

        val browseCount = browseViewModel.uiState.value.sections.last().items.size
        val savedCount = savedViewModel.uiState.value.sections.last().items.size

        assertEquals(browseCount, savedCount)
    }

    @Test
    fun `Initial state has MostRecent sort option selected`() = runTest {
        val viewModel = getViewModel()

        val sortSection = viewModel.uiState.value.sections.first()
        val mostRecentItem = sortSection.items.first()

        assertTrue(mostRecentItem.isSelected)
    }

    @Test
    fun `Initial state has All type filter selected`() = runTest {
        val viewModel = getViewModel()

        val typeSection = viewModel.uiState.value.sections.last()
        val allItem = typeSection.items.first()

        assertTrue(allItem.isSelected)
    }

    @Test
    fun `Selecting a sort option updates isSelected in state`() = runTest {
        val viewModel = getViewModel()
        val sortSection = viewModel.uiState.value.sections.first()

        sortSection.items[1].onSelected()

        val updatedSortSection = viewModel.uiState.value.sections.first()
        assertFalse(updatedSortSection.items[0].isSelected)
        assertTrue(updatedSortSection.items[1].isSelected)
    }

    @Test
    fun `Selecting a type filter updates isSelected in state`() = runTest {
        val viewModel = getViewModel(screenType = LearnLearningLibraryFilterScreenType.MyContent)
        val typeSection = viewModel.uiState.value.sections.last()

        typeSection.items[1].onSelected()

        val updatedTypeSection = viewModel.uiState.value.sections.last()
        assertFalse(updatedTypeSection.items[0].isSelected)
        assertTrue(updatedTypeSection.items[1].isSelected)
    }

    @Test
    fun `Selecting a type filter emits UpdateLearningLibraryFilter event`() = runTest {
        val viewModel = getViewModel(screenType = LearnLearningLibraryFilterScreenType.MyContent)
        val typeSection = viewModel.uiState.value.sections.last()

        var lastEvent: LearnEvent? = null
        val job = launch(testDispatcher) {
            eventHandler.events.collect { lastEvent = it }
        }

        typeSection.items[2].onSelected()

        val event = lastEvent as? LearnEvent.UpdateLearningLibraryFilter
        assertEquals(LearnLearningLibraryFilterScreenType.MyContent, event?.screenType)
        job.cancel()
    }

    @Test
    fun `Selecting a sort option emits UpdateLearningLibraryFilter event with correct sort`() = runTest {
        val viewModel = getViewModel()
        val sortSection = viewModel.uiState.value.sections.first()

        var lastEvent: LearnEvent? = null
        val job = launch(testDispatcher) {
            eventHandler.events.collect { lastEvent = it }
        }

        sortSection.items.last().onSelected()

        val event = lastEvent as? LearnEvent.UpdateLearningLibraryFilter
        assertEquals(LearnLearningLibrarySortOption.NameDescending, event?.sortOption)
        job.cancel()
    }

    @Test
    fun `clearFilters resets to All type filter and MostRecent sort`() = runTest {
        val viewModel = getViewModel(screenType = LearnLearningLibraryFilterScreenType.MyContent)
        viewModel.uiState.value.sections.last().items[1].onSelected()
        viewModel.uiState.value.sections.first().items[2].onSelected()

        viewModel.uiState.value.onClearFilters()

        val sortSection = viewModel.uiState.value.sections.first()
        val typeSection = viewModel.uiState.value.sections.last()
        assertTrue(sortSection.items[0].isSelected)
        assertTrue(typeSection.items[0].isSelected)
    }

    @Test
    fun `clearFilters emits UpdateLearningLibraryFilter with All and MostRecent`() = runTest {
        val viewModel = getViewModel(screenType = LearnLearningLibraryFilterScreenType.Browse)
        viewModel.uiState.value.sections.last().items[1].onSelected()

        var lastEvent: LearnEvent? = null
        val job = launch(testDispatcher) {
            eventHandler.events.collect { lastEvent = it }
        }

        viewModel.uiState.value.onClearFilters()

        val event = lastEvent as? LearnEvent.UpdateLearningLibraryFilter
        assertEquals(LearnLearningLibraryTypeFilter.All, event?.typeFilter)
        assertEquals(LearnLearningLibrarySortOption.MostRecent, event?.sortOption)
        job.cancel()
    }

    @Test
    fun `SavedStateHandle initializes typeFilter from saved args`() = runTest {
        val viewModel = getViewModel(
            screenType = LearnLearningLibraryFilterScreenType.MyContent,
            initialTypeFilter = LearnLearningLibraryTypeFilter.Courses,
        )

        val typeSection = viewModel.uiState.value.sections.last()
        val coursesItem = typeSection.items.first { it.isSelected }

        assertTrue(coursesItem.isSelected)
    }

    @Test
    fun `SavedStateHandle initializes sortOption from saved args`() = runTest {
        val viewModel = getViewModel(
            initialSortOption = LearnLearningLibrarySortOption.NameAscending,
        )

        val sortSection = viewModel.uiState.value.sections.first()
        val selectedItem = sortSection.items.first { it.isSelected }

        assertTrue(selectedItem.isSelected)
    }

    @Test
    fun `Two sort sections and type sections are always present`() = runTest {
        val viewModel = getViewModel()

        assertEquals(2, viewModel.uiState.value.sections.size)
    }

    private fun getViewModel(
        screenType: LearnLearningLibraryFilterScreenType = LearnLearningLibraryFilterScreenType.Browse,
        initialTypeFilter: LearnLearningLibraryTypeFilter = LearnLearningLibraryTypeFilter.All,
        initialSortOption: LearnLearningLibrarySortOption = LearnLearningLibrarySortOption.MostRecent,
    ): LearnLearningLibraryFilterViewModel {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                LearnRoute.LearnLearningLibraryFilterScreen.screenTypeAttr to screenType.name,
                LearnRoute.LearnLearningLibraryFilterScreen.typeFilterAttr to initialTypeFilter.name,
                LearnRoute.LearnLearningLibraryFilterScreen.sortOptionAttr to initialSortOption.name,
            )
        )
        return LearnLearningLibraryFilterViewModel(resources, eventHandler, savedStateHandle)
    }
}
