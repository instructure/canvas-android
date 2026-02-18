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
package com.instructure.horizon.features.learn.learninglibrary.details

import android.content.res.Resources
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.journey.learninglibrary.CanvasCourseInfo
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import com.instructure.horizon.R
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
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class LearnLearningLibraryDetailsViewModelTest {
    private val resources: Resources = mockk(relaxed = true)
    private val repository: LearnLearningLibraryDetailsRepository = mockk(relaxed = true)
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testCollectionId = "testCollection123"
    private val testCollection = createTestCollection(
        id = testCollectionId,
        name = "Software Engineering",
        items = listOf(
            createTestCollectionItem(
                id = "item1",
                courseName = "Introduction to Programming",
                courseId = "1",
                completionPercentage = 0.0,
                isBookmarked = false,
                itemType = CollectionItemType.COURSE
            ),
            createTestCollectionItem(
                id = "item2",
                courseName = "Advanced Algorithms",
                courseId = "2",
                completionPercentage = 50.0,
                isBookmarked = true,
                itemType = CollectionItemType.ASSIGNMENT
            ),
            createTestCollectionItem(
                id = "item3",
                courseName = "Data Structures",
                courseId = "3",
                completionPercentage = 100.0,
                isBookmarked = false,
                itemType = CollectionItemType.PAGE
            ),
            createTestCollectionItem(
                id = "item4",
                courseName = "Web Development Guide",
                courseId = "4",
                completionPercentage = 0.0,
                isBookmarked = true,
                itemType = CollectionItemType.FILE
            ),
            createTestCollectionItem(
                id = "item5",
                courseName = "External Learning Resource",
                courseId = "5",
                completionPercentage = 100.0,
                isBookmarked = false,
                itemType = CollectionItemType.EXTERNAL_URL
            ),
            createTestCollectionItem(
                id = "item6",
                courseName = "Tool Integration",
                courseId = "6",
                completionPercentage = 0.0,
                isBookmarked = false,
                itemType = CollectionItemType.EXTERNAL_TOOL
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
        every { resources.getQuantityString(any(), any()) } returns "items"
        every { resources.getQuantityString(any(), any(), *anyVararg()) } returns "items"

        every { savedStateHandle.get<String>(LearnRoute.LearnLearningLibraryDetailsScreen.collectionIdIdAttr) } returns testCollectionId

        coEvery { repository.getLearningLibraryItems(any(), any()) } returns testCollection
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Initial state loads collection successfully`() = runTest {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.loadingState.isLoading)
        assertFalse(state.loadingState.isError)
        assertEquals("Software Engineering", state.collectionName)
        assertEquals(6, state.items.size)
        coVerify { repository.getLearningLibraryItems(testCollectionId, false) }
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
        assertEquals(10, state.itemsToDisplays)
    }

    @Test
    fun `Initial state has default filter values`() = runTest {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertEquals(LearnLearningLibraryDetailsStatusFilter.All, state.selectedStatusFilter)
        assertEquals(LearnLearningLibraryDetailsTypeFilter.All, state.selectedTypeFilter)
    }

    @Test
    fun `Loading state shows error when repository fails`() = runTest {
        coEvery { repository.getLearningLibraryItems(any(), any()) } throws Exception("Network error")
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.loadingState.isError)
        assertFalse(state.loadingState.isLoading)
    }

    @Test
    fun `Empty collection loads successfully`() = runTest {
        val emptyCollection = testCollection.copy(items = emptyList())
        coEvery { repository.getLearningLibraryItems(any(), any()) } returns emptyCollection
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.loadingState.isLoading)
        assertFalse(state.loadingState.isError)
        assertEquals(0, state.items.size)
    }

    @Test
    fun `Search query filters items by name case-insensitive`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSearchQuery(TextFieldValue("programming"))

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals("Introduction to Programming", state.items[0].name)
    }

    @Test
    fun `Search query with partial match filters correctly`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSearchQuery(TextFieldValue("web"))

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals("Web Development Guide", state.items[0].name)
    }

    @Test
    fun `Search query with no match returns empty list`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSearchQuery(TextFieldValue("NonExistentItem"))

        val state = viewModel.uiState.value
        assertEquals(0, state.items.size)
    }

    @Test
    fun `Search query trims whitespace`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSearchQuery(TextFieldValue("  algorithms  "))

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals("Advanced Algorithms", state.items[0].name)
    }

    @Test
    fun `All status filter shows all items`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSelectedStatusFilter(LearnLearningLibraryDetailsStatusFilter.All)

        val state = viewModel.uiState.value
        assertEquals(6, state.items.size)
    }

    @Test
    fun `Completed status filter shows only completed items`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSelectedStatusFilter(LearnLearningLibraryDetailsStatusFilter.Completed)

        val state = viewModel.uiState.value
        assertEquals(2, state.items.size)
        assertTrue(state.items.all { it.isCompleted })
    }

    @Test
    fun `Bookmarked status filter shows only bookmarked items`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSelectedStatusFilter(LearnLearningLibraryDetailsStatusFilter.Bookmarked)

        val state = viewModel.uiState.value
        assertEquals(2, state.items.size)
        assertTrue(state.items.all { it.isBookmarked })
    }

    @Test
    fun `All type filter shows all items`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateTypeFilter(LearnLearningLibraryDetailsTypeFilter.All)

        val state = viewModel.uiState.value
        assertEquals(6, state.items.size)
    }

    @Test
    fun `Assignments type filter shows only assignments`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateTypeFilter(LearnLearningLibraryDetailsTypeFilter.Assignments)

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals(CollectionItemType.ASSIGNMENT, state.items[0].type)
    }

    @Test
    fun `Pages type filter shows only pages`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateTypeFilter(LearnLearningLibraryDetailsTypeFilter.Pages)

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals(CollectionItemType.PAGE, state.items[0].type)
    }

    @Test
    fun `Files type filter shows only files`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateTypeFilter(LearnLearningLibraryDetailsTypeFilter.Files)

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals(CollectionItemType.FILE, state.items[0].type)
    }

    @Test
    fun `ExternalLinks type filter shows only external URLs`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateTypeFilter(LearnLearningLibraryDetailsTypeFilter.ExternalLinks)

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals(CollectionItemType.EXTERNAL_URL, state.items[0].type)
    }

    @Test
    fun `ExternalTools type filter shows only external tools`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateTypeFilter(LearnLearningLibraryDetailsTypeFilter.ExternalTools)

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals(CollectionItemType.EXTERNAL_TOOL, state.items[0].type)
    }

    @Test
    fun `Assessments type filter returns empty list`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateTypeFilter(LearnLearningLibraryDetailsTypeFilter.Assessments)

        val state = viewModel.uiState.value
        assertEquals(0, state.items.size)
    }

    @Test
    fun `Search and status filter work together`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSearchQuery(TextFieldValue("Advanced"))
        viewModel.uiState.value.updateSelectedStatusFilter(LearnLearningLibraryDetailsStatusFilter.Bookmarked)

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals("Advanced Algorithms", state.items[0].name)
        assertTrue(state.items[0].isBookmarked)
    }

    @Test
    fun `Search and type filter work together`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSearchQuery(TextFieldValue("Development"))
        viewModel.uiState.value.updateTypeFilter(LearnLearningLibraryDetailsTypeFilter.Files)

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals("Web Development Guide", state.items[0].name)
        assertEquals(CollectionItemType.FILE, state.items[0].type)
    }

    @Test
    fun `All three filters work together`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSearchQuery(TextFieldValue("External"))
        viewModel.uiState.value.updateSelectedStatusFilter(LearnLearningLibraryDetailsStatusFilter.Completed)
        viewModel.uiState.value.updateTypeFilter(LearnLearningLibraryDetailsTypeFilter.ExternalLinks)

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals("External Learning Resource", state.items[0].name)
        assertTrue(state.items[0].isCompleted)
        assertEquals(CollectionItemType.EXTERNAL_URL, state.items[0].type)
    }

    @Test
    fun `increaseItemsToDisplay increases count by pageSize`() = runTest {
        val viewModel = getViewModel()
        val initialCount = viewModel.uiState.value.itemsToDisplays

        viewModel.uiState.value.increaseItemsToDisplay()

        val state = viewModel.uiState.value
        assertEquals(initialCount + 10, state.itemsToDisplays)
    }

    @Test
    fun `Multiple increaseItemsToDisplay calls accumulate`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.increaseItemsToDisplay()
        viewModel.uiState.value.increaseItemsToDisplay()

        val state = viewModel.uiState.value
        assertEquals(30, state.itemsToDisplays)
    }

    @Test
    fun `Pagination works with filtered items`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSelectedStatusFilter(LearnLearningLibraryDetailsStatusFilter.Bookmarked)
        viewModel.uiState.value.increaseItemsToDisplay()

        val state = viewModel.uiState.value
        assertEquals(20, state.itemsToDisplays)
        assertEquals(2, state.items.size)
    }

    @Test
    fun `onBookmarkClicked sets loading state and updates bookmark to true`() = runTest {
        val viewModel = getViewModel()
        coEvery { repository.toggleLearningLibraryItemIsBookmarked("item1") } returns true

        viewModel.uiState.value.onBookmarkClicked("item1")

        val state = viewModel.uiState.value
        val item = state.items.find { it.id == "item1" }
        assertNotNull(item)
        assertTrue(item!!.isBookmarked)
        assertFalse(item.bookmarkLoading)
        coVerify { repository.toggleLearningLibraryItemIsBookmarked("item1") }
    }

    @Test
    fun `onBookmarkClicked updates bookmark to false`() = runTest {
        val viewModel = getViewModel()
        coEvery { repository.toggleLearningLibraryItemIsBookmarked("item2") } returns false

        viewModel.uiState.value.onBookmarkClicked("item2")

        val state = viewModel.uiState.value
        val item = state.items.find { it.id == "item2" }
        assertNotNull(item)
        assertFalse(item!!.isBookmarked)
        assertFalse(item.bookmarkLoading)
    }

    @Test
    fun `onBookmarkClicked handles errors and shows error message`() = runTest {
        val viewModel = getViewModel()
        every { resources.getString(R.string.learnLearningLibraryFailedToUpdateBookmarkMessage) } returns "Failed to update bookmark"
        coEvery { repository.toggleLearningLibraryItemIsBookmarked("item1") } throws Exception("Network error")

        viewModel.uiState.value.onBookmarkClicked("item1")

        val state = viewModel.uiState.value
        val item = state.items.find { it.id == "item1" }
        assertNotNull(item)
        assertFalse(item!!.bookmarkLoading)
        assertNotNull(state.loadingState.errorMessage)
    }

    @Test
    fun `onBookmarkClicked updates only the correct item`() = runTest {
        val viewModel = getViewModel()
        coEvery { repository.toggleLearningLibraryItemIsBookmarked("item1") } returns true

        val initialItem2Bookmark = viewModel.uiState.value.items.find { it.id == "item2" }!!.isBookmarked

        viewModel.uiState.value.onBookmarkClicked("item1")

        val state = viewModel.uiState.value
        val item1 = state.items.find { it.id == "item1" }
        val item2 = state.items.find { it.id == "item2" }

        assertTrue(item1!!.isBookmarked)
        assertEquals(initialItem2Bookmark, item2!!.isBookmarked)
    }

    @Test
    fun `Bookmark state persists through filters`() = runTest {
        val viewModel = getViewModel()
        coEvery { repository.toggleLearningLibraryItemIsBookmarked("item1") } returns true

        viewModel.uiState.value.onBookmarkClicked("item1")
        viewModel.uiState.value.updateSelectedStatusFilter(LearnLearningLibraryDetailsStatusFilter.Bookmarked)

        val state = viewModel.uiState.value
        val item = state.items.find { it.id == "item1" }
        assertNotNull(item)
        assertTrue(item!!.isBookmarked)
    }

    @Test
    fun `Refresh calls repository with forceRefresh true`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.loadingState.onRefresh()

        coVerify { repository.getLearningLibraryItems(testCollectionId, true) }
    }

    @Test
    fun `Refresh updates collection data`() = runTest {
        val viewModel = getViewModel()
        val updatedCollection = createTestCollection(
            id = testCollectionId,
            name = "Updated Collection Name",
            items = listOf(
                createTestCollectionItem(
                    id = "newItem",
                    courseName = "New Course",
                    courseId = "999",
                    completionPercentage = 0.0,
                    isBookmarked = false,
                    itemType = CollectionItemType.COURSE
                )
            )
        )
        coEvery { repository.getLearningLibraryItems(testCollectionId, true) } returns updatedCollection

        viewModel.uiState.value.loadingState.onRefresh()

        val state = viewModel.uiState.value
        assertFalse(state.loadingState.isRefreshing)
        assertEquals("Updated Collection Name", state.collectionName)
        assertEquals(1, state.items.size)
        assertEquals("New Course", state.items[0].name)
    }

    @Test
    fun `Refresh on error shows snackbar message`() = runTest {
        val viewModel = getViewModel()
        every { resources.getString(R.string.learnLarningLibraryFailedToLoadCollectionMessage) } returns "Failed to load"
        coEvery { repository.getLearningLibraryItems(testCollectionId, true) } throws Exception("Network error")

        viewModel.uiState.value.loadingState.onRefresh()

        val state = viewModel.uiState.value
        assertFalse(state.loadingState.isRefreshing)
        assertNotNull(state.loadingState.snackbarMessage)
    }

    @Test
    fun `Refresh maintains applied filters`() = runTest {
        val viewModel = getViewModel()
        coEvery { repository.getLearningLibraryItems(testCollectionId, true) } returns testCollection

        viewModel.uiState.value.updateSelectedStatusFilter(LearnLearningLibraryDetailsStatusFilter.Completed)
        val itemCountBeforeRefresh = viewModel.uiState.value.items.size

        viewModel.uiState.value.loadingState.onRefresh()

        val state = viewModel.uiState.value
        assertEquals(LearnLearningLibraryDetailsStatusFilter.Completed, state.selectedStatusFilter)
        assertEquals(itemCountBeforeRefresh, state.items.size)
    }

    @Test
    fun `Dismiss snackbar clears snackbar message`() = runTest {
        val viewModel = getViewModel()
        every { resources.getString(R.string.learnLarningLibraryFailedToLoadCollectionMessage) } returns "Failed to load"
        coEvery { repository.getLearningLibraryItems(testCollectionId, true) } throws Exception("Network error")
        viewModel.uiState.value.loadingState.onRefresh()

        viewModel.uiState.value.loadingState.onSnackbarDismiss()

        val state = viewModel.uiState.value
        assertNull(state.loadingState.snackbarMessage)
    }

    @Test
    fun `Collection name is mapped correctly`() = runTest {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertEquals("Software Engineering", state.collectionName)
    }

    @Test
    fun `Items are mapped correctly to UI state`() = runTest {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        val firstItem = state.items[0]
        assertEquals("item1", firstItem.id)
        assertEquals("Introduction to Programming", firstItem.name)
        assertFalse(firstItem.isBookmarked)
        assertFalse(firstItem.isCompleted)
        assertEquals(CollectionItemType.COURSE, firstItem.type)
    }

    private fun getViewModel(): LearnLearningLibraryDetailsViewModel {
        return LearnLearningLibraryDetailsViewModel(savedStateHandle, resources, repository)
    }

    private fun createTestCollection(
        id: String,
        name: String,
        items: List<LearningLibraryCollectionItem>
    ): EnrolledLearningLibraryCollection = EnrolledLearningLibraryCollection(
        id = id,
        name = name,
        publicName = name,
        description = "Test description",
        createdAt = Date(),
        updatedAt = Date(),
        items = items
    )

    private fun createTestCollectionItem(
        id: String,
        courseName: String,
        courseId: String,
        completionPercentage: Double,
        isBookmarked: Boolean,
        itemType: CollectionItemType,
        isEnrolledInCanvas: Boolean = true
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
            moduleItemCount = 10.0,
            canvasUrl = "https://example.com",
        ),
        libraryId = "library1",
        displayOrder = 1.0,
        programId = null,
        programCourseId = null,
        createdAt = Date(),
        updatedAt = Date()
    )
}
