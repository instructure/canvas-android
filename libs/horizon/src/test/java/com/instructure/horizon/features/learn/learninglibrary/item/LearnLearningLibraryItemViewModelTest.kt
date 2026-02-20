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
package com.instructure.horizon.features.learn.learninglibrary.item

import android.content.res.Resources
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.journey.learninglibrary.CanvasCourseInfo
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItemsResponse
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryPageInfo
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryTypeFilter
import com.instructure.horizon.features.learn.navigation.LearnRoute
import com.instructure.pandautils.utils.ThemePrefs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class LearnLearningLibraryItemViewModelTest {
    private val resources: Resources = mockk(relaxed = true)
    private val repository: LearnLearningLibraryItemRepository = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testItems = listOf(
        createTestCollectionItem(id = "item1", courseName = "Python Basics", isBookmarked = false, isEnrolledInCanvas = true),
        createTestCollectionItem(id = "item2", courseName = "React Advanced", isBookmarked = true, isEnrolledInCanvas = true),
        createTestCollectionItem(id = "item3", courseName = "Machine Learning", completionPercentage = 100.0, isEnrolledInCanvas = true)
    )

    @Before
    fun setup() {
        mockkObject(ThemePrefs)
        every { ThemePrefs.brandColor } returns 0xFF0000FF.toInt()
        every { ThemePrefs.mobileLogoUrl } returns "https://example.com/logo.png"
        Dispatchers.setMain(testDispatcher)
        every { resources.getString(any()) } returns ""
        every { resources.getString(any(), *anyVararg()) } returns ""
        every { resources.getQuantityString(any(), any()) } returns ""
        every { resources.getQuantityString(any(), any(), *anyVararg()) } returns ""

        coEvery { repository.getLearningLibraryItems(any(), any(), any(), any(), any(), any(), any()) } returns createTestResponse(testItems)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `bookmarkOnly is true when saved state type is bookmark`() {
        val viewModel = getViewModel(type = "bookmark")

        assertTrue(viewModel.bookmarkOnly)
    }

    @Test
    fun `completedOnly is true when saved state type is completed`() {
        val viewModel = getViewModel(type = "completed")

        assertTrue(viewModel.completedOnly)
    }

    @Test
    fun `bookmarkOnly is false when saved state type is completed`() {
        val viewModel = getViewModel(type = "completed")

        assertFalse(viewModel.bookmarkOnly)
    }

    @Test
    fun `completedOnly is false when saved state type is bookmark`() {
        val viewModel = getViewModel(type = "bookmark")

        assertFalse(viewModel.completedOnly)
    }

    @Test
    fun `both flags are false when saved state type is unrecognized`() {
        val viewModel = getViewModel(type = "other")

        assertFalse(viewModel.bookmarkOnly)
        assertFalse(viewModel.completedOnly)
    }

    @Test
    fun `title is set to bookmarks title when in bookmark mode`() {
        every { resources.getString(R.string.learnLearningLibraryBookmarksTitle) } returns "Bookmarks"
        val viewModel = getViewModel(type = "bookmark")

        assertEquals("Bookmarks", viewModel.uiState.value.title)
    }

    @Test
    fun `title is set to completed title when in completed mode`() {
        every { resources.getString(R.string.learnLearningLibraryCompletedTitle) } returns "Completed"
        val viewModel = getViewModel(type = "completed")

        assertEquals("Completed", viewModel.uiState.value.title)
    }

    @Test
    fun `initial load populates items successfully`() {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.loadingState.isLoading)
        assertFalse(state.loadingState.isError)
        assertEquals(3, state.items.size)
    }

    @Test
    fun `initial load calls repository with bookmarkedOnly flag in bookmark mode`() {
        getViewModel(type = "bookmark")

        coVerify {
            repository.getLearningLibraryItems(
                afterCursor = null,
                limit = any(),
                searchQuery = any(),
                typeFilter = any(),
                bookmarkedOnly = true,
                completedOnly = false,
                forceNetwork = false
            )
        }
    }

    @Test
    fun `initial load calls repository with completedOnly flag in completed mode`() {
        getViewModel(type = "completed")

        coVerify {
            repository.getLearningLibraryItems(
                afterCursor = null,
                limit = any(),
                searchQuery = any(),
                typeFilter = any(),
                bookmarkedOnly = false,
                completedOnly = true,
                forceNetwork = false
            )
        }
    }

    @Test
    fun `initial load shows error state when repository fails`() {
        coEvery { repository.getLearningLibraryItems(any(), any(), any(), any(), any(), any(), any()) } throws Exception("Network error")
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.loadingState.isError)
        assertFalse(state.loadingState.isLoading)
    }

    @Test
    fun `showMoreButton is true when response has next page`() {
        coEvery { repository.getLearningLibraryItems(any(), any(), any(), any(), any(), any(), any()) } returns
            createTestResponse(testItems, hasNextPage = true, nextCursor = "nextCursor123")
        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.showMoreButton)
    }

    @Test
    fun `showMoreButton is false when response has no next page`() {
        coEvery { repository.getLearningLibraryItems(any(), any(), any(), any(), any(), any(), any()) } returns
            createTestResponse(testItems, hasNextPage = false)
        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.showMoreButton)
    }

    @Test
    fun `onShowMoreClicked appends items to existing list`() {
        val firstPageItems = listOf(createTestCollectionItem(id = "item1", courseName = "Course 1"))
        val secondPageItems = listOf(createTestCollectionItem(id = "item2", courseName = "Course 2"))
        coEvery {
            repository.getLearningLibraryItems(isNull(), any(), any(), any(), any(), any(), any())
        } returns createTestResponse(firstPageItems, hasNextPage = true, nextCursor = "cursor2")
        coEvery {
            repository.getLearningLibraryItems(eq("cursor2"), any(), any(), any(), any(), any(), any())
        } returns createTestResponse(secondPageItems, hasNextPage = false)

        val viewModel = getViewModel()
        viewModel.uiState.value.onShowMoreClicked()

        val state = viewModel.uiState.value
        assertEquals(2, state.items.size)
        assertEquals("item1", state.items[0].id)
        assertEquals("item2", state.items[1].id)
    }

    @Test
    fun `onShowMoreClicked shows snackbar on failure`() {
        val firstPageItems = listOf(createTestCollectionItem(id = "item1", courseName = "Course 1"))
        coEvery {
            repository.getLearningLibraryItems(isNull(), any(), any(), any(), any(), any(), any())
        } returns createTestResponse(firstPageItems, hasNextPage = true, nextCursor = "cursor2")
        coEvery {
            repository.getLearningLibraryItems(eq("cursor2"), any(), any(), any(), any(), any(), any())
        } throws Exception("Network error")
        every { resources.getString(R.string.learnLearningLibraryItemFailedToLoadMessage) } returns "Failed to load"

        val viewModel = getViewModel()
        viewModel.uiState.value.onShowMoreClicked()

        val state = viewModel.uiState.value
        assertFalse(state.isMoreButtonLoading)
        assertNotNull(state.loadingState.snackbarMessage)
    }

    @Test
    fun `onUpdateSearchQuery updates searchQuery in state`() {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSearchQuery(TextFieldValue("python"))

        assertEquals("python", viewModel.uiState.value.searchQuery.text)
    }

    @Test
    fun `onUpdateSearchQuery triggers data reload after debounce`() {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSearchQuery(TextFieldValue("python"))
        testDispatcher.scheduler.advanceTimeBy(300)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            repository.getLearningLibraryItems(
                afterCursor = null,
                limit = any(),
                searchQuery = eq("python"),
                typeFilter = any(),
                bookmarkedOnly = any(),
                completedOnly = any(),
                forceNetwork = any()
            )
        }
    }

    @Test
    fun `onUpdateSearchQuery resets items when triggering new load`() {
        val initialItems = listOf(createTestCollectionItem(id = "item1", courseName = "Old Item"))
        val searchItems = listOf(createTestCollectionItem(id = "item2", courseName = "Python Basics"))
        coEvery {
            repository.getLearningLibraryItems(any(), any(), eq(""), any(), any(), any(), any())
        } returns createTestResponse(initialItems)
        coEvery {
            repository.getLearningLibraryItems(any(), any(), eq("python"), any(), any(), any(), any())
        } returns createTestResponse(searchItems)

        val viewModel = getViewModel()
        viewModel.uiState.value.updateSearchQuery(TextFieldValue("python"))
        testDispatcher.scheduler.advanceTimeBy(300)
        testDispatcher.scheduler.advanceUntilIdle()

        val items = viewModel.uiState.value.items
        assertEquals(1, items.size)
        assertEquals("item2", items[0].id)
    }

    @Test
    fun `onUpdateTypeFilter updates typeFilter in state`() {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateTypeFilter(LearnLearningLibraryTypeFilter.Pages)

        assertEquals(LearnLearningLibraryTypeFilter.Pages, viewModel.uiState.value.typeFilter)
    }

    @Test
    fun `onUpdateTypeFilter triggers immediate data reload with null cursor`() {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateTypeFilter(LearnLearningLibraryTypeFilter.Files)

        coVerify {
            repository.getLearningLibraryItems(
                afterCursor = null,
                limit = any(),
                searchQuery = any(),
                typeFilter = CollectionItemType.FILE,
                bookmarkedOnly = any(),
                completedOnly = any(),
                forceNetwork = any()
            )
        }
    }

    @Test
    fun `refreshData calls repository with forceNetwork true and null cursor`() {
        val viewModel = getViewModel()

        viewModel.uiState.value.loadingState.onRefresh()

        coVerify {
            repository.getLearningLibraryItems(
                afterCursor = null,
                limit = any(),
                searchQuery = any(),
                typeFilter = any(),
                bookmarkedOnly = any(),
                completedOnly = any(),
                forceNetwork = true
            )
        }
    }

    @Test
    fun `refreshData clears isRefreshing after success`() {
        val viewModel = getViewModel()

        viewModel.uiState.value.loadingState.onRefresh()

        assertFalse(viewModel.uiState.value.loadingState.isRefreshing)
    }

    @Test
    fun `refreshData shows snackbar on failure`() {
        val viewModel = getViewModel()
        coEvery {
            repository.getLearningLibraryItems(any(), any(), any(), any(), any(), any(), eq(true))
        } throws Exception("Network error")
        every { resources.getString(R.string.learnLearningLibraryItemFailedToLoadMessage) } returns "Failed to refresh"

        viewModel.uiState.value.loadingState.onRefresh()

        val state = viewModel.uiState.value
        assertFalse(state.loadingState.isRefreshing)
        assertNotNull(state.loadingState.snackbarMessage)
    }

    @Test
    fun `onDismissSnackbar clears snackbar message`() {
        val viewModel = getViewModel()
        coEvery {
            repository.getLearningLibraryItems(any(), any(), any(), any(), any(), any(), eq(true))
        } throws Exception("Network error")
        viewModel.uiState.value.loadingState.onRefresh()

        viewModel.uiState.value.loadingState.onSnackbarDismiss()

        assertNull(viewModel.uiState.value.loadingState.snackbarMessage)
    }

    @Test
    fun `toggleItemBookmarked sets isBookmarked to true and clears loading`() {
        coEvery { repository.toggleLearningLibraryItemIsBookmarked("item1") } returns true
        val viewModel = getViewModel()

        viewModel.uiState.value.onBookmarkClicked("item1")

        val item = viewModel.uiState.value.items.first { it.id == "item1" }
        assertTrue(item.isBookmarked)
        assertFalse(item.bookmarkLoading)
        coVerify { repository.toggleLearningLibraryItemIsBookmarked("item1") }
    }

    @Test
    fun `toggleItemBookmarked sets isBookmarked to false when unbookmarking`() = runTest {
        coEvery { repository.toggleLearningLibraryItemIsBookmarked("item2") } returns false
        val viewModel = getViewModel()

        viewModel.uiState.value.onBookmarkClicked("item2")

        val item = viewModel.uiState.value.items.first { it.id == "item2" }
        assertFalse(item.isBookmarked)
        assertFalse(item.bookmarkLoading)
    }

    @Test
    fun `toggleItemBookmarked only modifies the targeted item`() = runTest {
        coEvery { repository.toggleLearningLibraryItemIsBookmarked("item1") } returns true
        val viewModel = getViewModel()

        viewModel.uiState.value.onBookmarkClicked("item1")

        advanceUntilIdle()

        val otherItem = viewModel.uiState.value.items.first { it.id == "item2" }
        assertFalse(otherItem.bookmarkLoading)
    }

    @Test
    fun `toggleItemBookmarked shows error message and resets loading on failure`() {
        coEvery { repository.toggleLearningLibraryItemIsBookmarked("item1") } throws Exception("Network error")
        every { resources.getString(R.string.learnLearningLibraryFailedToUpdateBookmarkMessage) } returns "Failed to update bookmark"
        val viewModel = getViewModel()

        viewModel.uiState.value.onBookmarkClicked("item1")

        val state = viewModel.uiState.value
        val item = state.items.first { it.id == "item1" }
        assertFalse(item.bookmarkLoading)
        assertNotNull(state.loadingState.errorMessage)
    }

    @Test
    fun `onEnrollItem updates item from repository and clears enrollLoading`() {
        val enrolledItem = createTestCollectionItem(id = "item1", courseName = "Python Basics", isEnrolledInCanvas = true)
        coEvery { repository.enrollLearningLibraryItem("item1") } returns enrolledItem
        val viewModel = getViewModel()

        viewModel.uiState.value.onEnrollClicked("item1")

        val item = viewModel.uiState.value.items.first { it.id == "item1" }
        assertFalse(item.enrollLoading)
        coVerify { repository.enrollLearningLibraryItem("item1") }
    }

    @Test
    fun `onEnrollItem only modifies the targeted item`() {
        val enrolledItem = createTestCollectionItem(id = "item1", isEnrolledInCanvas = true)
        coEvery { repository.enrollLearningLibraryItem("item1") } returns enrolledItem
        val viewModel = getViewModel()

        viewModel.uiState.value.onEnrollClicked("item1")

        val otherItem = viewModel.uiState.value.items.first { it.id == "item2" }
        assertFalse(otherItem.enrollLoading)
    }

    @Test
    fun `onEnrollItem shows error message and resets loading on failure`() {
        coEvery { repository.enrollLearningLibraryItem("item1") } throws Exception("Network error")
        every { resources.getString(R.string.learnLearningLibraryFailedToEnrollMessage) } returns "Failed to enroll"
        val viewModel = getViewModel()

        viewModel.uiState.value.onEnrollClicked("item1")

        val state = viewModel.uiState.value
        val item = state.items.first { it.id == "item1" }
        assertFalse(item.enrollLoading)
        assertNotNull(state.loadingState.errorMessage)
    }

    @Test
    fun `items are replaced not appended when cursor is null`() {
        val firstLoad = listOf(createTestCollectionItem(id = "item1", courseName = "Old Item"))
        val secondLoad = listOf(createTestCollectionItem(id = "item2", courseName = "New Item"))
        coEvery {
            repository.getLearningLibraryItems(isNull(), any(), any(), any(), any(), any(), any())
        } returnsMany listOf(createTestResponse(firstLoad), createTestResponse(secondLoad))

        val viewModel = getViewModel()
        viewModel.uiState.value.updateTypeFilter(LearnLearningLibraryTypeFilter.All)

        val items = viewModel.uiState.value.items
        assertEquals(1, items.size)
        assertEquals("item2", items[0].id)
    }

    private fun getViewModel(type: String = "bookmark") = LearnLearningLibraryItemViewModel(
        savedStateHandle = SavedStateHandle(mapOf(LearnRoute.LearnLearningLibraryBookmarkScreen.typeAttr to type)),
        resources = resources,
        repository = repository
    )

    private fun createTestResponse(
        items: List<LearningLibraryCollectionItem>,
        hasNextPage: Boolean = false,
        nextCursor: String? = null
    ) = LearningLibraryCollectionItemsResponse(
        items = items,
        pageInfo = LearningLibraryPageInfo(
            nextCursor = nextCursor,
            previousCursor = null,
            hasNextPage = hasNextPage,
            hasPreviousPage = false
        )
    )

    private fun createTestCollectionItem(
        id: String = "testItem",
        courseId: String = "1",
        courseName: String = "Test Course",
        completionPercentage: Double = 0.0,
        isBookmarked: Boolean = false,
        itemType: CollectionItemType = CollectionItemType.COURSE,
        isEnrolledInCanvas: Boolean = false
    ): LearningLibraryCollectionItem = LearningLibraryCollectionItem(
        id = id,
        itemType = itemType,
        isBookmarked = isBookmarked,
        completionPercentage = completionPercentage,
        isEnrolledInCanvas = isEnrolledInCanvas,
        canvasCourse = CanvasCourseInfo(
            courseId = courseId,
            courseName = courseName,
            courseImageUrl = "https://example.com/$courseId.png",
            estimatedDurationMinutes = 60.0,
            moduleCount = 5.0,
            moduleItemCount = 5.0,
            canvasUrl = "",
        ),
        libraryId = "",
        displayOrder = 1.0,
        programId = "",
        programCourseId = "",
        createdAt = Date(),
        updatedAt = Date()
    )
}
