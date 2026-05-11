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
package com.instructure.horizon.features.learn.mycontent.inprogress

import android.content.res.Resources
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemSortOption
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryPageInfo
import com.instructure.canvasapi2.models.journey.mycontent.CourseEnrollmentItem
import com.instructure.canvasapi2.models.journey.mycontent.LearnItemType
import com.instructure.canvasapi2.models.journey.mycontent.LearnItemsResponse
import com.instructure.canvasapi2.models.journey.mycontent.ProgramEnrollmentItem
import com.instructure.horizon.domain.usecase.GetLastSyncedAtUseCase
import com.instructure.horizon.domain.usecase.GetLearnMyContentInProgressItemsUseCase
import com.instructure.horizon.domain.usecase.GetNextModuleItemUseCase
import com.instructure.horizon.domain.usecase.OfflineCardStateHelper
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
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class LearnMyContentInProgressViewModelTest {

    private val resources: Resources = mockk(relaxed = true)
    private val getLearnMyContentInProgressItemsUseCase: GetLearnMyContentInProgressItemsUseCase = mockk(relaxed = true)
    private val offlineCardStateHelper: OfflineCardStateHelper = mockk(relaxed = true)
    private val getNextModuleItemUseCase: GetNextModuleItemUseCase = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)
    private val getLastSyncedAtUseCase: GetLastSyncedAtUseCase = mockk(relaxed = true)
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
        coEvery { getLearnMyContentInProgressItemsUseCase(any()) } returns emptyResponse
        coEvery { featureFlagProvider.offlineEnabled() } returns false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Initial state has empty content cards`() = runTest {
        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.contentCards.isEmpty())
    }

    @Test
    fun `Initial state is not loading`() = runTest {
        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.loadingState.isLoading)
    }

    @Test
    fun `Initial state has no error`() = runTest {
        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.loadingState.isError)
    }

    @Test
    fun `Initial state showMoreButton is false`() = runTest {
        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.showMoreButton)
    }

    @Test
    fun `Initial state isMoreLoading is false`() = runTest {
        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.isMoreLoading)
    }

    @Test
    fun `onFiltersChanged triggers load`() = runTest {
        val viewModel = getViewModel()

        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        coVerify {
            getLearnMyContentInProgressItemsUseCase(match {
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
            getLearnMyContentInProgressItemsUseCase(match {
                it.cursor == null &&
                it.searchQuery == null &&
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
            getLearnMyContentInProgressItemsUseCase(match {
                it.cursor == null &&
                it.searchQuery == null &&
                it.sortBy == CollectionItemSortOption.MOST_RECENT &&
                it.itemTypes == listOf(LearnItemType.COURSE)
            })
        }
    }

    @Test
    fun `onFiltersChanged with All typeFilter passes null item types`() = runTest {
        val viewModel = getViewModel()

        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        coVerify {
            getLearnMyContentInProgressItemsUseCase(match { it.itemTypes == null })
        }
    }

    @Test
    fun `onFiltersChanged with NameAscending sort passes NAME_A_Z sort option`() = runTest {
        val viewModel = getViewModel()

        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.NameAscending, LearnLearningLibraryTypeFilter.All)

        coVerify {
            getLearnMyContentInProgressItemsUseCase(match {
                it.cursor == null &&
                it.sortBy == CollectionItemSortOption.NAME_A_Z &&
                it.itemTypes == null
            })
        }
    }

    @Test
    fun `Successful load populates contentCards`() = runTest {
        val programs = listOf(createTestProgramItem(id = "p1", name = "Program A"))
        coEvery { getLearnMyContentInProgressItemsUseCase(any()) } returns LearnItemsResponse(
            items = programs,
            pageInfo = LearningLibraryPageInfo(null, null, false, false, 1, null)
        )
        val viewModel = getViewModel()

        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        assertEquals(1, viewModel.uiState.value.contentCards.size)
        assertEquals("Program A", viewModel.uiState.value.contentCards[0].name)
    }

    @Test
    fun `Successful load sets totalItemCount from pageInfo`() = runTest {
        coEvery { getLearnMyContentInProgressItemsUseCase(any()) } returns LearnItemsResponse(
            items = listOf(createTestProgramItem()),
            pageInfo = LearningLibraryPageInfo(null, null, false, false, 42, null)
        )
        val viewModel = getViewModel()

        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        assertEquals(42, viewModel.uiState.value.totalItemCount)
    }

    @Test
    fun `showMoreButton is true when pageInfo has next page`() = runTest {
        coEvery { getLearnMyContentInProgressItemsUseCase(any()) } returns LearnItemsResponse(
            items = listOf(createTestProgramItem()),
            pageInfo = LearningLibraryPageInfo("cursor1", null, true, false, 10, null)
        )
        val viewModel = getViewModel()

        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        assertTrue(viewModel.uiState.value.showMoreButton)
    }

    @Test
    fun `showMoreButton is false when pageInfo has no next page`() = runTest {
        coEvery { getLearnMyContentInProgressItemsUseCase(any()) } returns LearnItemsResponse(
            items = listOf(createTestProgramItem()),
            pageInfo = LearningLibraryPageInfo(null, null, false, false, 1, null)
        )
        val viewModel = getViewModel()

        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        assertFalse(viewModel.uiState.value.showMoreButton)
    }

    @Test
    fun `Load error sets isError true`() = runTest {
        coEvery { getLearnMyContentInProgressItemsUseCase(any()) } throws Exception("Network error")
        val viewModel = getViewModel()

        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        assertTrue(viewModel.uiState.value.loadingState.isError)
        assertFalse(viewModel.uiState.value.loadingState.isLoading)
    }

    @Test
    fun `Filter change replaces existing items`() = runTest {
        coEvery { getLearnMyContentInProgressItemsUseCase(any()) } returns LearnItemsResponse(
            items = listOf(createTestProgramItem(name = "First")),
            pageInfo = LearningLibraryPageInfo(null, null, false, false, 1, null)
        )
        val viewModel = getViewModel()
        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        coEvery { getLearnMyContentInProgressItemsUseCase(any()) } returns LearnItemsResponse(
            items = listOf(createTestProgramItem(name = "Second")),
            pageInfo = LearningLibraryPageInfo(null, null, false, false, 1, null)
        )
        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.Programs)

        assertEquals(1, viewModel.uiState.value.contentCards.size)
        assertEquals("Second", viewModel.uiState.value.contentCards[0].name)
    }

    @Test
    fun `Refresh re-fetches items`() = runTest {
        val viewModel = getViewModel()
        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        viewModel.uiState.value.loadingState.onRefresh()

        coVerify { getLearnMyContentInProgressItemsUseCase(match { !it.forceRefresh }) }
        coVerify { getLearnMyContentInProgressItemsUseCase(match { it.forceRefresh }) }
    }

    @Test
    fun `Refresh success clears error and updates content`() = runTest {
        coEvery { getLearnMyContentInProgressItemsUseCase(any()) } throws Exception("Error")
        val viewModel = getViewModel()
        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        coEvery { getLearnMyContentInProgressItemsUseCase(any()) } returns LearnItemsResponse(
            items = listOf(createTestProgramItem(name = "Refreshed")),
            pageInfo = LearningLibraryPageInfo(null, null, false, false, 1, null)
        )
        viewModel.uiState.value.loadingState.onRefresh()

        assertFalse(viewModel.uiState.value.loadingState.isError)
        assertFalse(viewModel.uiState.value.loadingState.isRefreshing)
        assertEquals("Refreshed", viewModel.uiState.value.contentCards[0].name)
    }

    @Test
    fun `Refresh error shows snackbar message`() = runTest {
        val viewModel = getViewModel()
        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)
        coEvery { getLearnMyContentInProgressItemsUseCase(any()) } throws Exception("Network error")

        viewModel.uiState.value.loadingState.onRefresh()

        assertFalse(viewModel.uiState.value.loadingState.isRefreshing)
        assertNotNull(viewModel.uiState.value.loadingState.snackbarMessage)
    }

    @Test
    fun `Dismiss snackbar clears snackbar message`() = runTest {
        val viewModel = getViewModel()
        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)
        coEvery { getLearnMyContentInProgressItemsUseCase(any()) } throws Exception("Network error")
        viewModel.uiState.value.loadingState.onRefresh()

        viewModel.uiState.value.loadingState.onSnackbarDismiss()

        assertNull(viewModel.uiState.value.loadingState.snackbarMessage)
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
        coEvery { getLearnMyContentInProgressItemsUseCase(match { it.cursor == null }) } returns firstPage
        coEvery { getLearnMyContentInProgressItemsUseCase(match { it.cursor == "cursor1" }) } returns secondPage
        val viewModel = getViewModel()
        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        viewModel.uiState.value.increaseTotalItemCount()

        assertEquals(2, viewModel.uiState.value.contentCards.size)
        assertEquals("First", viewModel.uiState.value.contentCards[0].name)
        assertEquals("Second", viewModel.uiState.value.contentCards[1].name)
    }

    @Test
    fun `loadMore error shows snackbar and clears isMoreLoading`() = runTest {
        coEvery { getLearnMyContentInProgressItemsUseCase(match { it.cursor == null }) } returns LearnItemsResponse(
            items = listOf(createTestProgramItem()),
            pageInfo = LearningLibraryPageInfo("cursor1", null, true, false, 2, null)
        )
        val viewModel = getViewModel()
        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)
        coEvery { getLearnMyContentInProgressItemsUseCase(match { it.cursor == "cursor1" }) } throws Exception("Network error")

        viewModel.uiState.value.increaseTotalItemCount()

        assertFalse(viewModel.uiState.value.isMoreLoading)
        assertNotNull(viewModel.uiState.value.loadingState.snackbarMessage)
    }

    @Test
    fun `Search query triggers load with searchQuery after debounce`() = runTest {
        val viewModel = getViewModel()

        viewModel.onFiltersChanged("kotlin", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)
        advanceTimeBy(350)

        coVerify {
            getLearnMyContentInProgressItemsUseCase(match { it.cursor == null && it.searchQuery == "kotlin" })
        }
    }

    @Test
    fun `Empty search query passes null searchQuery to use case`() = runTest {
        val viewModel = getViewModel()

        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        coVerify {
            getLearnMyContentInProgressItemsUseCase(match { it.cursor == null && it.searchQuery == null })
        }
    }

    private fun getViewModel() = LearnMyContentInProgressViewModel(
        resources, getLearnMyContentInProgressItemsUseCase, offlineCardStateHelper, getNextModuleItemUseCase, networkStateProvider, featureFlagProvider, getLastSyncedAtUseCase
    )

    private fun createTestProgramItem(
        id: String = "program1",
        name: String = "Test Program",
    ) = ProgramEnrollmentItem(
        id = id,
        name = name,
        position = 1,
        enrolledAt = Date(),
        completionPercentage = 50.0,
        startDate = null,
        endDate = null,
        status = "active",
        description = null,
        variant = "standard",
        estimatedDurationMinutes = null,
        courseCount = 2,
    )

    private fun createTestCourseItem(
        id: String = "course1",
        name: String = "Test Course",
        completionPercentage: Double? = 50.0,
    ) = CourseEnrollmentItem(
        id = id,
        name = name,
        position = 1,
        enrolledAt = Date(),
        completionPercentage = completionPercentage,
        startAt = null,
        endAt = null,
        requirementCount = 10,
        requirementCompletedCount = 5,
        completedAt = null,
        grade = null,
        imageUrl = null,
        workflowState = "available",
        lastActivityAt = null,
    )
}