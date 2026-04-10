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
package com.instructure.horizon.features.learn.learninglibrary.list

import android.content.res.Resources
import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.canvasapi2.models.journey.learninglibrary.CanvasCourseInfo
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemSortOption
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItemsResponse
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryPageInfo
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.LearnEvent
import com.instructure.horizon.features.learn.LearnEventHandler
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryFilterScreenType
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibrarySortOption
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryTypeFilter
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.ThemePrefs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
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
class LearnLearningLibraryListViewModelTest {
    private val eventHandler = LearnEventHandler()
    private val resources: Resources = mockk(relaxed = true)
    private val repository: LearnLearningLibraryListRepository = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val emptyItemsResponse = LearningLibraryCollectionItemsResponse(
        items = emptyList(),
        pageInfo = LearningLibraryPageInfo(null, null, false, false, null, null)
    )

    private var testCollections: List<EnrolledLearningLibraryCollection> = listOf(
        createTestCollection(
            id = "collection1",
            name = "Introduction to Programming",
            items = listOf(
                createTestCollectionItem(
                    id = "item1",
                    courseId = "1",
                    courseName = "Python Basics",
                    completionPercentage = 0.0,
                    isBookmarked = false,
                    isEnrolledInCanvas = true
                )
            )
        ),
        createTestCollection(
            id = "collection2",
            name = "Advanced Web Development",
            items = listOf(
                createTestCollectionItem(
                    id = "item2",
                    courseId = "2",
                    courseName = "React Advanced",
                    completionPercentage = 50.0,
                    isBookmarked = true,
                    isEnrolledInCanvas = true
                )
            )
        ),
        createTestCollection(
            id = "collection3",
            name = "Data Science Fundamentals",
            items = listOf(
                createTestCollectionItem(
                    id = "item3",
                    courseId = "3",
                    courseName = "Machine Learning",
                    completionPercentage = 100.0,
                    isBookmarked = false,
                    isEnrolledInCanvas = true
                )
            )
        )
    )

    @Before
    fun setup() {
        mockkObject(ThemePrefs)
        every { ThemePrefs.brandColor } returns 0xFF0000FF.toInt()
        every { ThemePrefs.mobileLogoUrl } returns "https://example.com/logo.png"
        Dispatchers.setMain(testDispatcher)
        every { resources.getString(any()) } returns ""
        every { resources.getString(any(), *anyVararg()) } returns ""
        every { resources.getQuantityString(any(), any()) } returns "2 items"
        every { resources.getQuantityString(any(), any(), *anyVararg()) } returns "2 items"

        coEvery { repository.getEnrolledLearningLibraries(any()) } returns testCollections
        coEvery { repository.getLearningLibraryItems(any(), any(), any(), any(), any(), any(), any(), any()) } returns emptyItemsResponse
        coEvery { featureFlagProvider.offlineEnabled() } returns false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Initial state loads collections successfully`() = runTest {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.collectionState.loadingState.isLoading)
        assertFalse(state.collectionState.loadingState.isError)
        assertEquals(3, state.collectionState.collections.size)
        coVerify { repository.getEnrolledLearningLibraries(false) }
    }

    @Test
    fun `Initial state has empty search query`() = runTest {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery.text)
    }

    @Test
    fun `Initial state sets visible item count to pageSize`() = runTest {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertEquals(3, state.collectionState.itemsToDisplay)
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
    fun `Initial state has MostRecent sort option`() = runTest {
        val viewModel = getViewModel()

        assertEquals(LearnLearningLibrarySortOption.MostRecent, viewModel.uiState.value.sortOption)
    }

    @Test
    fun `Loading state shows error when repository fails`() = runTest {
        coEvery { repository.getEnrolledLearningLibraries(any()) } throws Exception("Network error")
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.collectionState.loadingState.isError)
        assertFalse(state.collectionState.loadingState.isLoading)
    }

    @Test
    fun `Empty collections list loads successfully`() = runTest {
        coEvery { repository.getEnrolledLearningLibraries(any()) } returns emptyList()
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.collectionState.loadingState.isLoading)
        assertFalse(state.collectionState.loadingState.isError)
        assertEquals(0, state.collectionState.collections.size)
    }

    @Test
    fun `increaseItemsToDisplay increases count by pageSize`() = runTest {
        val viewModel = getViewModel()
        val initialCount = viewModel.uiState.value.collectionState.itemsToDisplay

        viewModel.uiState.value.collectionState.increaseItemsToDisplay()

        val state = viewModel.uiState.value
        assertEquals(initialCount + 3, state.collectionState.itemsToDisplay)
    }

    @Test
    fun `Multiple increaseItemsToDisplay calls accumulate`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.collectionState.increaseItemsToDisplay()
        viewModel.uiState.value.collectionState.increaseItemsToDisplay()

        val state = viewModel.uiState.value
        assertEquals(9, state.collectionState.itemsToDisplay)
    }

    @Test
    fun `Collections are mapped correctly to LearnLearningLibraryCollectionState`() = runTest {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        val firstCollection = state.collectionState.collections[0]
        assertEquals("collection1", firstCollection.id)
        assertEquals("Introduction to Programming", firstCollection.name)
        assertEquals(1, firstCollection.itemCount)
        assertEquals(1, firstCollection.items.size)
    }

    @Test
    fun `Collection items are mapped correctly`() = runTest {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        val firstItem = state.collectionState.collections[0].items[0]
        assertEquals("item1", firstItem.id)
        assertEquals("Python Basics", firstItem.name)
        assertFalse(firstItem.isBookmarked)
    }

    @Test
    fun `Course item not enrolled in Canvas can enroll`() = runTest {
        val collections = listOf(
            createTestCollection(
                id = "collection1",
                name = "Test Collection",
                items = listOf(
                    createTestCollectionItem(
                        id = "item1",
                        courseId = "1",
                        courseName = "Test Course",
                        itemType = CollectionItemType.COURSE,
                        isEnrolledInCanvas = false
                    )
                )
            )
        )
        coEvery { repository.getEnrolledLearningLibraries(any()) } returns collections
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        val firstItem = state.collectionState.collections[0].items[0]
        assertTrue(firstItem.canEnroll)
    }

    @Test
    fun `Course item already enrolled in Canvas cannot enroll`() = runTest {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        val firstItem = state.collectionState.collections[0].items[0]
        assertFalse(firstItem.canEnroll)
    }

    @Test
    fun `Non-course item cannot enroll`() = runTest {
        val collections = listOf(
            createTestCollection(
                id = "collection1",
                name = "Test Collection",
                items = listOf(
                    createTestCollectionItem(
                        id = "item1",
                        courseId = "1",
                        courseName = "Test Page",
                        itemType = CollectionItemType.PAGE,
                        isEnrolledInCanvas = false
                    )
                )
            )
        )
        coEvery { repository.getEnrolledLearningLibraries(any()) } returns collections
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        val firstItem = state.collectionState.collections[0].items[0]
        assertFalse(firstItem.canEnroll)
    }

    @Test
    fun `Refresh re-fetches collections`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.collectionState.loadingState.onRefresh()

        coVerify { repository.getEnrolledLearningLibraries(false) }
        coVerify { repository.getEnrolledLearningLibraries(true) }
    }

    @Test
    fun `Refresh updates collections list`() = runTest {
        val viewModel = getViewModel()
        val updatedCollections = listOf(
            createTestCollection(
                id = "collection4",
                name = "New Collection",
                items = listOf(
                    createTestCollectionItem(
                        id = "item4",
                        courseId = "4",
                        courseName = "New Course",
                        completionPercentage = 0.0,
                        isBookmarked = false,
                        isEnrolledInCanvas = true
                    )
                )
            )
        )
        coEvery { repository.getEnrolledLearningLibraries(any()) } returns updatedCollections

        viewModel.uiState.value.collectionState.loadingState.onRefresh()

        val state = viewModel.uiState.value
        assertFalse(state.collectionState.loadingState.isRefreshing)
        assertEquals(1, state.collectionState.collections.size)
        assertEquals("New Collection", state.collectionState.collections[0].name)
    }

    @Test
    fun `Refresh on error shows snackbar message`() = runTest {
        val viewModel = getViewModel()
        coEvery { repository.getEnrolledLearningLibraries(any()) } throws Exception("Network error")

        viewModel.uiState.value.collectionState.loadingState.onRefresh()

        val state = viewModel.uiState.value
        assertFalse(state.collectionState.loadingState.isRefreshing)
        assertTrue(state.collectionState.loadingState.snackbarMessage != null)
    }

    @Test
    fun `Dismiss snackbar clears both collection and item snackbar messages`() = runTest {
        val viewModel = getViewModel()
        coEvery { repository.getEnrolledLearningLibraries(any()) } throws Exception("Network error")
        viewModel.uiState.value.collectionState.loadingState.onRefresh()

        viewModel.uiState.value.collectionState.loadingState.onSnackbarDismiss()

        val state = viewModel.uiState.value
        assertNull(state.collectionState.loadingState.snackbarMessage)
        assertNull(state.itemState.loadingState.snackbarMessage)
    }

    @Test
    fun `onCollectionBookmarkClicked sets loading state and updates bookmark`() = runTest {
        val viewModel = getViewModel()
        coEvery { repository.toggleLearningLibraryItemIsBookmarked("item1") } returns true

        viewModel.uiState.value.collectionState.onBookmarkClicked("item1")

        val state = viewModel.uiState.value
        val firstItem = state.collectionState.collections[0].items[0]
        assertTrue(firstItem.isBookmarked)
        assertFalse(firstItem.bookmarkLoading)
        coVerify { repository.toggleLearningLibraryItemIsBookmarked("item1") }
    }

    @Test
    fun `onCollectionBookmarkClicked handles errors and shows error message`() = runTest {
        val viewModel = getViewModel()
        every { resources.getString(R.string.learnLearningLibraryFailedToUpdateBookmarkMessage) } returns "Failed to update bookmark"
        coEvery { repository.toggleLearningLibraryItemIsBookmarked("item1") } throws Exception("Network error")

        viewModel.uiState.value.collectionState.onBookmarkClicked("item1")

        val state = viewModel.uiState.value
        val firstItem = state.collectionState.collections[0].items[0]
        assertFalse(firstItem.bookmarkLoading)
        assertTrue(state.collectionState.loadingState.snackbarMessage != null)
    }

    @Test
    fun `isEmptyFilter returns true when no filters are applied`() = runTest {
        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.isEmptyFilter())
    }

    @Test
    fun `isEmptyFilter returns false when search query is set`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSearchQuery(TextFieldValue("python"))

        assertFalse(viewModel.uiState.value.isEmptyFilter())
    }

    @Test
    fun `isEmptyFilter returns false when activeFilterCount is non-zero`() = runTest {
        val viewModel = getViewModel()

        eventHandler.postEvent(LearnEvent.UpdateLearningLibraryFilter(
            screenType = LearnLearningLibraryFilterScreenType.Browse,
            typeFilter = LearnLearningLibraryTypeFilter.Pages,
            sortOption = LearnLearningLibrarySortOption.MostRecent
        ))

        assertFalse(viewModel.uiState.value.isEmptyFilter())
    }

    @Test
    fun `UpdateLearningLibraryFilter event updates typeFilter in state`() = runTest {
        val viewModel = getViewModel()

        eventHandler.postEvent(LearnEvent.UpdateLearningLibraryFilter(
            screenType = LearnLearningLibraryFilterScreenType.Browse,
            typeFilter = LearnLearningLibraryTypeFilter.Courses,
            sortOption = LearnLearningLibrarySortOption.MostRecent
        ))

        assertEquals(LearnLearningLibraryTypeFilter.Courses, viewModel.uiState.value.typeFilter)
    }

    @Test
    fun `UpdateLearningLibraryFilter event updates sortOption in state`() = runTest {
        val viewModel = getViewModel()

        eventHandler.postEvent(LearnEvent.UpdateLearningLibraryFilter(
            screenType = LearnLearningLibraryFilterScreenType.Browse,
            typeFilter = LearnLearningLibraryTypeFilter.All,
            sortOption = LearnLearningLibrarySortOption.NameAscending
        ))

        assertEquals(LearnLearningLibrarySortOption.NameAscending, viewModel.uiState.value.sortOption)
    }

    @Test
    fun `UpdateLearningLibraryFilter event increments activeFilterCount when typeFilter is non-All`() = runTest {
        val viewModel = getViewModel()

        eventHandler.postEvent(LearnEvent.UpdateLearningLibraryFilter(
            screenType = LearnLearningLibraryFilterScreenType.Browse,
            typeFilter = LearnLearningLibraryTypeFilter.Pages,
            sortOption = LearnLearningLibrarySortOption.MostRecent
        ))

        assertEquals(1, viewModel.uiState.value.activeFilterCount)
    }

    @Test
    fun `UpdateLearningLibraryFilter event resets activeFilterCount to zero when typeFilter is All`() = runTest {
        val viewModel = getViewModel()
        eventHandler.postEvent(LearnEvent.UpdateLearningLibraryFilter(
            screenType = LearnLearningLibraryFilterScreenType.Browse,
            typeFilter = LearnLearningLibraryTypeFilter.Pages,
            sortOption = LearnLearningLibrarySortOption.MostRecent
        ))

        eventHandler.postEvent(LearnEvent.UpdateLearningLibraryFilter(
            screenType = LearnLearningLibraryFilterScreenType.Browse,
            typeFilter = LearnLearningLibraryTypeFilter.All,
            sortOption = LearnLearningLibrarySortOption.MostRecent
        ))

        assertEquals(0, viewModel.uiState.value.activeFilterCount)
    }

    @Test
    fun `UpdateLearningLibraryFilter event with MyContent screenType is ignored by Browse VM`() = runTest {
        val viewModel = getViewModel()

        eventHandler.postEvent(LearnEvent.UpdateLearningLibraryFilter(
            screenType = LearnLearningLibraryFilterScreenType.MyContent,
            typeFilter = LearnLearningLibraryTypeFilter.Pages,
            sortOption = LearnLearningLibrarySortOption.MostRecent
        ))

        assertEquals(LearnLearningLibraryTypeFilter.All, viewModel.uiState.value.typeFilter)
        assertEquals(0, viewModel.uiState.value.activeFilterCount)
    }

    @Test
    fun `UpdateLearningLibraryFilter event with MyContentSaved screenType is ignored by Browse VM`() = runTest {
        val viewModel = getViewModel()

        eventHandler.postEvent(LearnEvent.UpdateLearningLibraryFilter(
            screenType = LearnLearningLibraryFilterScreenType.MyContentSaved,
            typeFilter = LearnLearningLibraryTypeFilter.Pages,
            sortOption = LearnLearningLibrarySortOption.MostRecent
        ))

        assertEquals(LearnLearningLibraryTypeFilter.All, viewModel.uiState.value.typeFilter)
        assertEquals(0, viewModel.uiState.value.activeFilterCount)
    }

    @Test
    fun `UpdateLearningLibraryFilter event passes typeFilter to repository`() = runTest {
        val viewModel = getViewModel()

        eventHandler.postEvent(LearnEvent.UpdateLearningLibraryFilter(
            screenType = LearnLearningLibraryFilterScreenType.Browse,
            typeFilter = LearnLearningLibraryTypeFilter.Pages,
            sortOption = LearnLearningLibrarySortOption.MostRecent
        ))

        coVerify { repository.getLearningLibraryItems(any(), any(), any(), typeFilter = CollectionItemType.PAGE, any(), any(), any(), any()) }
    }

    @Test
    fun `UpdateLearningLibraryFilter event passes sortOption to repository`() = runTest {
        val viewModel = getViewModel()

        eventHandler.postEvent(LearnEvent.UpdateLearningLibraryFilter(
            screenType = LearnLearningLibraryFilterScreenType.Browse,
            typeFilter = LearnLearningLibraryTypeFilter.All,
            sortOption = LearnLearningLibrarySortOption.NameAscending
        ))

        coVerify { repository.getLearningLibraryItems(any(), any(), any(), any(), any(), any(), sortBy = CollectionItemSortOption.NAME_A_Z, any()) }
    }

    @Test
    fun `Item loading populates items from repository`() = runTest {
        val testItems = listOf(
            createTestCollectionItem(id = "item1", courseId = "1", courseName = "Python Basics")
        )
        coEvery { repository.getLearningLibraryItems(any(), any(), any(), any(), any(), any(), any(), any()) } returns LearningLibraryCollectionItemsResponse(
            items = testItems,
            pageInfo = LearningLibraryPageInfo(null, null, false, false, null, null)
        )
        val viewModel = getViewModel()

        eventHandler.postEvent(LearnEvent.UpdateLearningLibraryFilter(
            screenType = LearnLearningLibraryFilterScreenType.Browse,
            typeFilter = LearnLearningLibraryTypeFilter.Pages,
            sortOption = LearnLearningLibrarySortOption.MostRecent
        ))

        val state = viewModel.uiState.value
        assertEquals(1, state.itemState.items.size)
        assertEquals("Python Basics", state.itemState.items[0].name)
    }

    @Test
    fun `Item loading shows error state when repository fails`() = runTest {
        val viewModel = getViewModel()
        coEvery { repository.getLearningLibraryItems(any(), any(), any(), any(), any(), any(), any(), any()) } throws Exception("Network error")

        eventHandler.postEvent(LearnEvent.UpdateLearningLibraryFilter(
            screenType = LearnLearningLibraryFilterScreenType.Browse,
            typeFilter = LearnLearningLibraryTypeFilter.Pages,
            sortOption = LearnLearningLibrarySortOption.MostRecent
        ))

        val state = viewModel.uiState.value
        assertTrue(state.itemState.loadingState.isError)
    }

    @Test
    fun `Search query triggers item loading after debounce`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSearchQuery(TextFieldValue("python"))
        advanceTimeBy(350)

        coVerify { repository.getLearningLibraryItems(null, any(), "python", any(), any(), any(), any(), any()) }
    }

    @Test
    fun `Search query updates searchQuery in state immediately`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSearchQuery(TextFieldValue("python"))

        assertEquals("python", viewModel.uiState.value.searchQuery.text)
    }

    @Test
    fun `Search query change replaces item list when no cursor`() = runTest {
        val firstResponse = LearningLibraryCollectionItemsResponse(
            items = listOf(createTestCollectionItem(id = "item1", courseName = "Python Basics")),
            pageInfo = LearningLibraryPageInfo(null, null, false, false, null, null)
        )
        coEvery { repository.getLearningLibraryItems(any(), any(), any(), any(), any(), any(), any(), any()) } returns firstResponse
        val viewModel = getViewModel()

        eventHandler.postEvent(LearnEvent.UpdateLearningLibraryFilter(
            screenType = LearnLearningLibraryFilterScreenType.Browse,
            typeFilter = LearnLearningLibraryTypeFilter.Pages,
            sortOption = LearnLearningLibrarySortOption.MostRecent
        ))
        val secondResponse = LearningLibraryCollectionItemsResponse(
            items = listOf(createTestCollectionItem(id = "item2", courseName = "React Advanced")),
            pageInfo = LearningLibraryPageInfo(null, null, false, false, null, null)
        )
        coEvery { repository.getLearningLibraryItems(any(), any(), any(), any(), any(), any(), any(), any()) } returns secondResponse

        eventHandler.postEvent(LearnEvent.UpdateLearningLibraryFilter(
            screenType = LearnLearningLibraryFilterScreenType.Browse,
            typeFilter = LearnLearningLibraryTypeFilter.Courses,
            sortOption = LearnLearningLibrarySortOption.MostRecent
        ))

        assertEquals(1, viewModel.uiState.value.itemState.items.size)
        assertEquals("React Advanced", viewModel.uiState.value.itemState.items[0].name)
    }

    @Test
    fun `showMoreButton is set when API has next page`() = runTest {
        coEvery { repository.getLearningLibraryItems(any(), any(), any(), any(), any(), any(), any(), any()) } returns LearningLibraryCollectionItemsResponse(
            items = listOf(createTestCollectionItem(id = "item1")),
            pageInfo = LearningLibraryPageInfo(nextCursor = "cursor1", previousCursor = null, hasNextPage = true, hasPreviousPage = false, totalCount = null, pageCursors = null)
        )
        val viewModel = getViewModel()

        eventHandler.postEvent(LearnEvent.UpdateLearningLibraryFilter(
            screenType = LearnLearningLibraryFilterScreenType.Browse,
            typeFilter = LearnLearningLibraryTypeFilter.Pages,
            sortOption = LearnLearningLibrarySortOption.MostRecent
        ))

        assertTrue(viewModel.uiState.value.itemState.showMoreButton)
    }

    @Test
    fun `onShowMoreClicked fetches next page and appends items`() = runTest {
        val firstPage = LearningLibraryCollectionItemsResponse(
            items = listOf(createTestCollectionItem(id = "item1", courseName = "First Course")),
            pageInfo = LearningLibraryPageInfo(nextCursor = "cursor1", previousCursor = null, hasNextPage = true, hasPreviousPage = false, totalCount = null, pageCursors = null)
        )
        val secondPage = LearningLibraryCollectionItemsResponse(
            items = listOf(createTestCollectionItem(id = "item2", courseName = "Second Course")),
            pageInfo = LearningLibraryPageInfo(null, null, false, false, null, null)
        )
        coEvery { repository.getLearningLibraryItems(null, any(), any(), any(), any(), any(), any(), any()) } returns firstPage
        coEvery { repository.getLearningLibraryItems("cursor1", any(), any(), any(), any(), any(), any(), any()) } returns secondPage
        val viewModel = getViewModel()
        eventHandler.postEvent(LearnEvent.UpdateLearningLibraryFilter(
            screenType = LearnLearningLibraryFilterScreenType.Browse,
            typeFilter = LearnLearningLibraryTypeFilter.Pages,
            sortOption = LearnLearningLibrarySortOption.MostRecent
        ))

        viewModel.uiState.value.itemState.onShowMoreClicked()

        val items = viewModel.uiState.value.itemState.items
        assertEquals(2, items.size)
        assertEquals("First Course", items[0].name)
        assertEquals("Second Course", items[1].name)
    }

    @Test
    fun `onShowMoreClicked sets isMoreButtonLoading during load`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.itemState.onShowMoreClicked()

        assertFalse(viewModel.uiState.value.itemState.isMoreButtonLoading)
    }

    @Test
    fun `Items refresh re-fetches items`() = runTest {
        val viewModel = getViewModel()
        eventHandler.postEvent(LearnEvent.UpdateLearningLibraryFilter(
            screenType = LearnLearningLibraryFilterScreenType.Browse,
            typeFilter = LearnLearningLibraryTypeFilter.Pages,
            sortOption = LearnLearningLibrarySortOption.MostRecent
        ))

        viewModel.uiState.value.itemState.loadingState.onRefresh()

        coVerify { repository.getLearningLibraryItems(null, any(), any(), any(), any(), any(), any(), false) }
        coVerify { repository.getLearningLibraryItems(null, any(), any(), any(), any(), any(), any(), true) }
    }

    @Test
    fun `Items refresh updates items list`() = runTest {
        val viewModel = getViewModel()
        val refreshedItems = listOf(
            createTestCollectionItem(id = "item1", courseName = "Refreshed Course")
        )
        coEvery { repository.getLearningLibraryItems(null, any(), any(), any(), any(), any(), any(), any()) } returns LearningLibraryCollectionItemsResponse(
            items = refreshedItems,
            pageInfo = LearningLibraryPageInfo(null, null, false, false, null, null)
        )

        viewModel.uiState.value.itemState.loadingState.onRefresh()

        val state = viewModel.uiState.value
        assertFalse(state.itemState.loadingState.isRefreshing)
        assertEquals(1, state.itemState.items.size)
        assertEquals("Refreshed Course", state.itemState.items[0].name)
    }

    @Test
    fun `Items refresh on error shows snackbar message`() = runTest {
        val viewModel = getViewModel()
        coEvery { repository.getLearningLibraryItems(null, any(), any(), any(), any(), any(), any(), any()) } throws Exception("Network error")

        viewModel.uiState.value.itemState.loadingState.onRefresh()

        val state = viewModel.uiState.value
        assertFalse(state.itemState.loadingState.isRefreshing)
        assertTrue(state.itemState.loadingState.snackbarMessage != null)
    }

    @Test
    fun `onItemBookmarkClicked updates bookmark in item state`() = runTest {
        val testItems = listOf(
            createTestCollectionItem(id = "item1", courseName = "Python Basics", isBookmarked = false)
        )
        coEvery { repository.getLearningLibraryItems(any(), any(), any(), any(), any(), any(), any(), any()) } returns LearningLibraryCollectionItemsResponse(
            items = testItems,
            pageInfo = LearningLibraryPageInfo(null, null, false, false, null, null)
        )
        coEvery { repository.toggleLearningLibraryItemIsBookmarked("item1") } returns true
        val viewModel = getViewModel()
        eventHandler.postEvent(LearnEvent.UpdateLearningLibraryFilter(
            screenType = LearnLearningLibraryFilterScreenType.Browse,
            typeFilter = LearnLearningLibraryTypeFilter.Pages,
            sortOption = LearnLearningLibrarySortOption.MostRecent
        ))

        viewModel.uiState.value.itemState.onBookmarkClicked("item1")

        val updatedItem = viewModel.uiState.value.itemState.items.find { it.id == "item1" }
        assertTrue(updatedItem!!.isBookmarked)
        assertFalse(updatedItem.bookmarkLoading)
    }

    @Test
    fun `onItemBookmarkClicked also updates collection state bookmark`() = runTest {
        val testItems = listOf(
            createTestCollectionItem(id = "item1", courseName = "Python Basics", isBookmarked = false)
        )
        coEvery { repository.getLearningLibraryItems(any(), any(), any(), any(), any(), any(), any(), any()) } returns LearningLibraryCollectionItemsResponse(
            items = testItems,
            pageInfo = LearningLibraryPageInfo(null, null, false, false, null, null)
        )
        coEvery { repository.toggleLearningLibraryItemIsBookmarked("item1") } returns true
        val viewModel = getViewModel()
        eventHandler.postEvent(LearnEvent.UpdateLearningLibraryFilter(
            screenType = LearnLearningLibraryFilterScreenType.Browse,
            typeFilter = LearnLearningLibraryTypeFilter.Pages,
            sortOption = LearnLearningLibrarySortOption.MostRecent
        ))

        viewModel.uiState.value.itemState.onBookmarkClicked("item1")

        val collectionItem = viewModel.uiState.value.collectionState.collections
            .flatMap { it.items }
            .find { it.id == "item1" }
        assertTrue(collectionItem!!.isBookmarked)
    }

    @Test
    fun `onItemBookmarkClicked error shows error in item state`() = runTest {
        val testItems = listOf(
            createTestCollectionItem(id = "item1", courseName = "Python Basics", isBookmarked = false)
        )
        coEvery { repository.getLearningLibraryItems(any(), any(), any(), any(), any(), any(), any(), any()) } returns LearningLibraryCollectionItemsResponse(
            items = testItems,
            pageInfo = LearningLibraryPageInfo(null, null, false, false, null, null)
        )
        every { resources.getString(R.string.learnLearningLibraryFailedToUpdateBookmarkMessage) } returns "Failed to update bookmark"
        coEvery { repository.toggleLearningLibraryItemIsBookmarked("item1") } throws Exception("Network error")
        val viewModel = getViewModel()
        eventHandler.postEvent(LearnEvent.UpdateLearningLibraryFilter(
            screenType = LearnLearningLibraryFilterScreenType.Browse,
            typeFilter = LearnLearningLibraryTypeFilter.Pages,
            sortOption = LearnLearningLibrarySortOption.MostRecent
        ))

        viewModel.uiState.value.itemState.onBookmarkClicked("item1")

        val state = viewModel.uiState.value
        assertFalse(state.itemState.items.find { it.id == "item1" }!!.bookmarkLoading)
        assertTrue(state.itemState.loadingState.snackbarMessage != null)
    }

    private fun getViewModel(): LearnLearningLibraryListViewModel {
        return LearnLearningLibraryListViewModel(resources, repository, eventHandler, apiPrefs, networkStateProvider, featureFlagProvider)
    }

    private fun createTestCollection(
        id: String = "testCollection",
        name: String = "Test Collection",
        items: List<LearningLibraryCollectionItem> = emptyList()
    ): EnrolledLearningLibraryCollection = EnrolledLearningLibraryCollection(
        id = id,
        name = name,
        publicName = name,
        description = "Test description",
        createdAt = Date(),
        updatedAt = Date(),
        items = items,
        totalItemCount = items.size
    )

    private fun createTestCollectionItem(
        id: String = "testItem",
        courseId: String = "1",
        courseName: String = "Test Course",
        completionPercentage: Double? = 0.0,
        isBookmarked: Boolean = false,
        isEnrolledInCanvas: Boolean? = true,
        itemType: CollectionItemType = CollectionItemType.COURSE
    ): LearningLibraryCollectionItem = LearningLibraryCollectionItem(
        id = id,
        libraryId = "library1",
        itemType = itemType,
        displayOrder = 1.0,
        canvasCourse = CanvasCourseInfo(
            courseId = courseId,
            canvasUrl = "https://example.com",
            courseName = courseName,
            courseImageUrl = "https://example.com/image.png",
            moduleCount = 5.0,
            moduleItemCount = 20.0,
            estimatedDurationMinutes = 120.0
        ),
        programId = null,
        programCourseId = null,
        createdAt = Date(),
        updatedAt = Date(),
        isBookmarked = isBookmarked,
        completionPercentage = completionPercentage,
        isEnrolledInCanvas = isEnrolledInCanvas,
        moduleInfo = null,
        canvasEnrollmentId = null
    )
}
