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
package com.instructure.horizon.features.learn.mycontent.completed

import android.content.res.Resources
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemSortOption
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryPageInfo
import com.instructure.canvasapi2.models.journey.mycontent.LearnItemType
import com.instructure.canvasapi2.models.journey.mycontent.LearnItemsResponse
import com.instructure.canvasapi2.models.journey.mycontent.ProgramEnrollmentItem
import com.instructure.horizon.domain.usecase.GetLearnMyContentCompletedItemsUseCase
import com.instructure.horizon.domain.usecase.GetNextModuleItemUseCase
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibrarySortOption
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryTypeFilter
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class LearnMyContentCompletedViewModelTest {

    private val resources: Resources = mockk(relaxed = true)
    private val getLearnMyContentCompletedItemsUseCase: GetLearnMyContentCompletedItemsUseCase = mockk(relaxed = true)
    private val getNextModuleItemUseCase: GetNextModuleItemUseCase = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val emptyResponse = LearnItemsResponse(
        items = emptyList(),
        pageInfo = LearningLibraryPageInfo(null, null, false, false, null, null)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { resources.getString(any()) } returns ""
        every { resources.getString(any(), *anyVararg()) } returns ""
        every { resources.getQuantityString(any(), any(), *anyVararg()) } returns ""
        coEvery { getLearnMyContentCompletedItemsUseCase(any()) } returns emptyResponse
        coEvery { featureFlagProvider.offlineEnabled() } returns false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onFiltersChanged triggers load`() = runTest {
        val viewModel = getViewModel()

        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        coVerify {
            getLearnMyContentCompletedItemsUseCase(match {
                it.cursor == null &&
                it.searchQuery == null &&
                it.sortBy == CollectionItemSortOption.MOST_RECENT &&
                it.itemTypes == null
            })
        }
    }

    @Test
    fun `onFiltersChanged with Programs typeFilter passes PROGRAM item type`() = runTest {
        val viewModel = getViewModel()

        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.Programs)

        coVerify {
            getLearnMyContentCompletedItemsUseCase(match {
                it.cursor == null &&
                it.sortBy == CollectionItemSortOption.MOST_RECENT &&
                it.itemTypes == listOf(LearnItemType.PROGRAM)
            })
        }
    }

    @Test
    fun `onFiltersChanged with Courses typeFilter passes COURSE item type`() = runTest {
        val viewModel = getViewModel()

        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.Courses)

        coVerify {
            getLearnMyContentCompletedItemsUseCase(match {
                it.cursor == null &&
                it.sortBy == CollectionItemSortOption.MOST_RECENT &&
                it.itemTypes == listOf(LearnItemType.COURSE)
            })
        }
    }

    @Test
    fun `Successful load populates contentCards`() = runTest {
        coEvery { getLearnMyContentCompletedItemsUseCase(any()) } returns LearnItemsResponse(
            items = listOf(createTestProgramItem(name = "Completed Program")),
            pageInfo = LearningLibraryPageInfo(null, null, false, false, 1, null)
        )
        val viewModel = getViewModel()

        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        assertEquals(1, viewModel.uiState.value.contentCards.size)
        assertEquals("Completed Program", viewModel.uiState.value.contentCards[0].name)
    }

    @Test
    fun `Load error sets isError true`() = runTest {
        coEvery { getLearnMyContentCompletedItemsUseCase(any()) } throws Exception("Network error")
        val viewModel = getViewModel()

        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        assertTrue(viewModel.uiState.value.loadingState.isError)
    }

    @Test
    fun `showMoreButton is true when pageInfo has next page`() = runTest {
        coEvery { getLearnMyContentCompletedItemsUseCase(any()) } returns LearnItemsResponse(
            items = listOf(createTestProgramItem()),
            pageInfo = LearningLibraryPageInfo("cursor1", null, true, false, 10, null)
        )
        val viewModel = getViewModel()

        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        assertTrue(viewModel.uiState.value.showMoreButton)
    }

    @Test
    fun `Refresh re-fetches items`() = runTest {
        val viewModel = getViewModel()
        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        viewModel.uiState.value.loadingState.onRefresh()

        coVerify { getLearnMyContentCompletedItemsUseCase(match { !it.forceRefresh }) }
        coVerify { getLearnMyContentCompletedItemsUseCase(match { it.forceRefresh }) }
    }

    @Test
    fun `Refresh error shows snackbar message`() = runTest {
        val viewModel = getViewModel()
        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)
        coEvery { getLearnMyContentCompletedItemsUseCase(any()) } throws Exception("Network error")

        viewModel.uiState.value.loadingState.onRefresh()

        assertFalse(viewModel.uiState.value.loadingState.isRefreshing)
        assertNotNull(viewModel.uiState.value.loadingState.snackbarMessage)
    }

    @Test
    fun `loadMore fetches with nextCursor and appends items`() = runTest {
        val firstPage = LearnItemsResponse(
            items = listOf(createTestProgramItem(id = "p1", name = "First")),
            pageInfo = LearningLibraryPageInfo("cursor1", null, true, false, 2, null)
        )
        val secondPage = LearnItemsResponse(
            items = listOf(createTestProgramItem(id = "p2", name = "Second")),
            pageInfo = LearningLibraryPageInfo(null, null, false, false, 2, null)
        )
        coEvery { getLearnMyContentCompletedItemsUseCase(match { it.cursor == null }) } returns firstPage
        coEvery { getLearnMyContentCompletedItemsUseCase(match { it.cursor == "cursor1" }) } returns secondPage
        val viewModel = getViewModel()
        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        viewModel.uiState.value.increaseTotalItemCount()

        assertEquals(2, viewModel.uiState.value.contentCards.size)
        assertEquals("First", viewModel.uiState.value.contentCards[0].name)
        assertEquals("Second", viewModel.uiState.value.contentCards[1].name)
    }

    private fun getViewModel() = LearnMyContentCompletedViewModel(
        resources, getLearnMyContentCompletedItemsUseCase, getNextModuleItemUseCase, networkStateProvider, featureFlagProvider
    )

    private fun createTestProgramItem(
        id: String = "program1",
        name: String = "Test Program",
    ) = ProgramEnrollmentItem(
        id = id,
        name = name,
        position = 1,
        enrolledAt = Date(),
        completionPercentage = 100.0,
        startDate = null,
        endDate = null,
        status = "completed",
        description = null,
        variant = "standard",
        estimatedDurationMinutes = null,
        courseCount = 2,
    )
}