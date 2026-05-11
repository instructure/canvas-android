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
package com.instructure.horizon.features.learn.mycontent.saved

import android.content.res.Resources
import com.instructure.canvasapi2.models.journey.learninglibrary.CanvasCourseInfo
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemSortOption
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItemsResponse
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryPageInfo
import com.instructure.horizon.R
import com.instructure.horizon.domain.usecase.GetLastSyncedAtUseCase
import com.instructure.horizon.domain.usecase.GetLearnLearningLibraryItemsUseCase
import com.instructure.horizon.domain.usecase.GetLearnLearningLibraryRecommendationsUseCase
import com.instructure.horizon.domain.usecase.GetNextModuleItemUseCase
import com.instructure.horizon.domain.usecase.OfflineCardStateHelper
import com.instructure.horizon.domain.usecase.ToggleLearnLearningLibraryItemBookmarkUseCase
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
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class LearnMyContentSavedViewModelTest {

    private val resources: Resources = mockk(relaxed = true)
    private val getLearnLearningLibraryItemsUseCase: GetLearnLearningLibraryItemsUseCase = mockk(relaxed = true)
    private val getLearnLearningLibraryRecommendationsUseCase: GetLearnLearningLibraryRecommendationsUseCase = mockk(relaxed = true)
    private val toggleLearnLearningLibraryItemBookmarkUseCase: ToggleLearnLearningLibraryItemBookmarkUseCase = mockk(relaxed = true)
    private val offlineCardStateHelper: OfflineCardStateHelper = mockk(relaxed = true)
    private val getNextModuleItemUseCase: GetNextModuleItemUseCase = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)
    private val getLastSyncedAtUseCase: GetLastSyncedAtUseCase = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val emptyResponse = LearningLibraryCollectionItemsResponse(
        items = emptyList(),
        pageInfo = LearningLibraryPageInfo(null, null, false, false, null, null)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { resources.getString(any()) } returns ""
        every { resources.getString(any(), *anyVararg()) } returns ""
        every { resources.getQuantityString(any(), any(), *anyVararg()) } returns ""
        coEvery { getLearnLearningLibraryItemsUseCase(any()) } returns emptyResponse
        coEvery { getLearnLearningLibraryRecommendationsUseCase(any()) } returns emptyList()
        coEvery { toggleLearnLearningLibraryItemBookmarkUseCase(any()) } returns false
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
    fun `onFiltersChanged calls use case with bookmarkedOnly true`() = runTest {
        val viewModel = getViewModel()

        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        coVerify {
            getLearnLearningLibraryItemsUseCase(match {
                it.cursor == null &&
                it.searchQuery == null &&
                it.sortBy == CollectionItemSortOption.MOST_RECENT &&
                it.typeFilter == null &&
                it.bookmarkedOnly == true
            })
        }
    }

    @Test
    fun `onFiltersChanged also fetches recommendations`() = runTest {
        val viewModel = getViewModel()

        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        coVerify { getLearnLearningLibraryRecommendationsUseCase(match { !it.forceRefresh }) }
    }

    @Test
    fun `onFiltersChanged with Courses typeFilter passes COURSE collection type`() = runTest {
        val viewModel = getViewModel()

        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.Courses)

        coVerify {
            getLearnLearningLibraryItemsUseCase(match {
                it.cursor == null &&
                it.typeFilter == CollectionItemType.COURSE &&
                it.bookmarkedOnly == true
            })
        }
    }

    @Test
    fun `onFiltersChanged with All typeFilter passes null types`() = runTest {
        val viewModel = getViewModel()

        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        coVerify {
            getLearnLearningLibraryItemsUseCase(match { it.typeFilter == null && it.bookmarkedOnly == true })
        }
    }

    @Test
    fun `Successful load populates contentCards`() = runTest {
        coEvery { getLearnLearningLibraryItemsUseCase(any()) } returns LearningLibraryCollectionItemsResponse(
            items = listOf(createTestCollectionItem(id = "item1", name = "Saved Course")),
            pageInfo = LearningLibraryPageInfo(null, null, false, false, 1, null)
        )
        val viewModel = getViewModel()

        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        assertEquals(1, viewModel.uiState.value.contentCards.size)
        assertEquals("item1", viewModel.uiState.value.contentCards[0].id)
    }

    @Test
    fun `Load error sets isError true`() = runTest {
        coEvery { getLearnLearningLibraryItemsUseCase(any()) } throws Exception("Network error")
        val viewModel = getViewModel()

        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        assertTrue(viewModel.uiState.value.loadingState.isError)
    }

    @Test
    fun `showMoreButton is true when pageInfo has next page`() = runTest {
        coEvery { getLearnLearningLibraryItemsUseCase(any()) } returns LearningLibraryCollectionItemsResponse(
            items = listOf(createTestCollectionItem()),
            pageInfo = LearningLibraryPageInfo("cursor1", null, true, false, 10, null)
        )
        val viewModel = getViewModel()

        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        assertTrue(viewModel.uiState.value.showMoreButton)
    }

    @Test
    fun `Refresh re-fetches recommendations`() = runTest {
        val viewModel = getViewModel()
        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        viewModel.uiState.value.loadingState.onRefresh()

        coVerify { getLearnLearningLibraryRecommendationsUseCase(match { !it.forceRefresh }) }
        coVerify { getLearnLearningLibraryRecommendationsUseCase(match { it.forceRefresh }) }
    }

    @Test
    fun `Refresh re-fetches bookmarked items`() = runTest {
        val viewModel = getViewModel()
        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        viewModel.uiState.value.loadingState.onRefresh()

        coVerify { getLearnLearningLibraryItemsUseCase(match { !it.forceRefresh }) }
        coVerify { getLearnLearningLibraryItemsUseCase(match { it.forceRefresh }) }
    }

    @Test
    fun `loadMore fetches with cursor and appends items`() = runTest {
        val firstPage = LearningLibraryCollectionItemsResponse(
            items = listOf(createTestCollectionItem(id = "item1")),
            pageInfo = LearningLibraryPageInfo("cursor1", null, true, false, 2, null)
        )
        val secondPage = LearningLibraryCollectionItemsResponse(
            items = listOf(createTestCollectionItem(id = "item2")),
            pageInfo = LearningLibraryPageInfo(null, null, false, false, 2, null)
        )
        coEvery { getLearnLearningLibraryItemsUseCase(match { it.cursor == null }) } returns firstPage
        coEvery { getLearnLearningLibraryItemsUseCase(match { it.cursor == "cursor1" }) } returns secondPage
        val viewModel = getViewModel()
        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        viewModel.uiState.value.increaseTotalItemCount()

        assertEquals(2, viewModel.uiState.value.contentCards.size)
    }

    @Test
    fun `onBookmarkItem sets bookmarkLoading true then removes item on success`() = runTest {
        coEvery { getLearnLearningLibraryItemsUseCase(any()) } returns LearningLibraryCollectionItemsResponse(
            items = listOf(createTestCollectionItem(id = "item1")),
            pageInfo = LearningLibraryPageInfo(null, null, false, false, 1, null)
        )
        val viewModel = getViewModel()
        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        viewModel.onBookmarkItem("item1")

        assertTrue(viewModel.uiState.value.contentCards.none { it.id == "item1" })
    }

    @Test
    fun `onBookmarkItem calls toggleLearningLibraryItemIsBookmarked`() = runTest {
        coEvery { getLearnLearningLibraryItemsUseCase(any()) } returns LearningLibraryCollectionItemsResponse(
            items = listOf(createTestCollectionItem(id = "item1")),
            pageInfo = LearningLibraryPageInfo(null, null, false, false, 1, null)
        )
        val viewModel = getViewModel()
        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        viewModel.onBookmarkItem("item1")

        coVerify { toggleLearnLearningLibraryItemBookmarkUseCase(match { it.itemId == "item1" }) }
    }

    @Test
    fun `onBookmarkItem error keeps item in list with bookmarkLoading false`() = runTest {
        coEvery { getLearnLearningLibraryItemsUseCase(any()) } returns LearningLibraryCollectionItemsResponse(
            items = listOf(createTestCollectionItem(id = "item1")),
            pageInfo = LearningLibraryPageInfo(null, null, false, false, 1, null)
        )
        coEvery { toggleLearnLearningLibraryItemBookmarkUseCase(match { it.itemId == "item1" }) } throws Exception("Network error")
        val viewModel = getViewModel()
        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        viewModel.onBookmarkItem("item1")

        val item = viewModel.uiState.value.contentCards.find { it.id == "item1" }
        assertNotNull(item)
        assertFalse(item!!.bookmarkLoading)
    }

    @Test
    fun `onBookmarkItem error shows snackbar message`() = runTest {
        coEvery { getLearnLearningLibraryItemsUseCase(any()) } returns LearningLibraryCollectionItemsResponse(
            items = listOf(createTestCollectionItem(id = "item1")),
            pageInfo = LearningLibraryPageInfo(null, null, false, false, 1, null)
        )
        every { resources.getString(R.string.learnMyContentSavedFailedToBookmarkErrorMessage) } returns "Failed to save"
        coEvery { toggleLearnLearningLibraryItemBookmarkUseCase(match { it.itemId == "item1" }) } throws Exception("Network error")
        val viewModel = getViewModel()
        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)

        viewModel.onBookmarkItem("item1")

        assertNotNull(viewModel.uiState.value.loadingState.snackbarMessage)
    }

    @Test
    fun `Refresh error shows snackbar`() = runTest {
        val viewModel = getViewModel()
        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)
        coEvery { getLearnLearningLibraryItemsUseCase(any()) } throws Exception("Network error")

        viewModel.uiState.value.loadingState.onRefresh()

        assertFalse(viewModel.uiState.value.loadingState.isRefreshing)
        assertNotNull(viewModel.uiState.value.loadingState.snackbarMessage)
    }

    @Test
    fun `Dismiss snackbar clears snackbar message`() = runTest {
        val viewModel = getViewModel()
        viewModel.onFiltersChanged("", LearnLearningLibrarySortOption.MostRecent, LearnLearningLibraryTypeFilter.All)
        coEvery { getLearnLearningLibraryItemsUseCase(any()) } throws Exception("Error")
        viewModel.uiState.value.loadingState.onRefresh()

        viewModel.uiState.value.loadingState.onSnackbarDismiss()

        assertNull(viewModel.uiState.value.loadingState.snackbarMessage)
    }

    private fun getViewModel() = LearnMyContentSavedViewModel(
        resources, getLearnLearningLibraryItemsUseCase, getLearnLearningLibraryRecommendationsUseCase,
        toggleLearnLearningLibraryItemBookmarkUseCase, offlineCardStateHelper, getNextModuleItemUseCase, networkStateProvider, featureFlagProvider, getLastSyncedAtUseCase
    )

    private fun createTestCollectionItem(
        id: String = "testItem",
        name: String = "Test Item",
        isBookmarked: Boolean = true,
        itemType: CollectionItemType = CollectionItemType.COURSE,
    ) = LearningLibraryCollectionItem(
        id = id,
        libraryId = "library1",
        itemType = itemType,
        displayOrder = 1.0,
        canvasCourse = CanvasCourseInfo(
            courseId = "1",
            canvasUrl = "https://example.com",
            courseName = name,
            courseImageUrl = null,
            moduleCount = 5.0,
            moduleItemCount = 20.0,
            estimatedDurationMinutes = 60.0,
        ),
        programId = null,
        programCourseId = null,
        createdAt = Date(),
        updatedAt = Date(),
        isBookmarked = isBookmarked,
        completionPercentage = 0.0,
        isEnrolledInCanvas = true,
        moduleInfo = null,
        canvasEnrollmentId = null,
    )
}
