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
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import com.instructure.canvasapi2.models.journey.learninglibrary.LearningLibraryCollectionItem
import com.instructure.horizon.R
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
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class LearnLearningLibraryListViewModelTest {
    private val resources: Resources = mockk(relaxed = true)
    private val repository: LearnLearningLibraryListRepository = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

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
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Initial state loads collections successfully`() = runTest {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.loadingState.isLoading)
        assertFalse(state.loadingState.isError)
        assertEquals(3, state.collections.size)
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
        assertEquals(3, state.itemsToDisplays)
    }

    @Test
    fun `Loading state shows error when repository fails`() = runTest {
        coEvery { repository.getEnrolledLearningLibraries(any()) } throws Exception("Network error")
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.loadingState.isError)
        assertFalse(state.loadingState.isLoading)
    }

    @Test
    fun `Empty collections list loads successfully`() = runTest {
        coEvery { repository.getEnrolledLearningLibraries(any()) } returns emptyList()
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.loadingState.isLoading)
        assertFalse(state.loadingState.isError)
        assertEquals(0, state.collections.size)
    }

    @Test
    fun `Search query filters collections by name case-insensitive`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSearchQuery(TextFieldValue("programming"))

        val state = viewModel.uiState.value
        assertEquals(1, state.collections.size)
        assertEquals("Introduction to Programming", state.collections[0].name)
    }

    @Test
    fun `Search query with partial match filters correctly`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSearchQuery(TextFieldValue("web"))

        val state = viewModel.uiState.value
        assertEquals(1, state.collections.size)
        assertEquals("Advanced Web Development", state.collections[0].name)
    }

    @Test
    fun `Search query with no match returns empty list`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSearchQuery(TextFieldValue("NonExistentCollection"))

        val state = viewModel.uiState.value
        assertEquals(0, state.collections.size)
    }

    @Test
    fun `Search query trims whitespace`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateSearchQuery(TextFieldValue("  data science  "))

        val state = viewModel.uiState.value
        assertEquals(1, state.collections.size)
        assertEquals("Data Science Fundamentals", state.collections[0].name)
    }

    @Test
    fun `increaseItemsToDisplay increases count by pageSize`() = runTest {
        val viewModel = getViewModel()
        val initialCount = viewModel.uiState.value.itemsToDisplays

        viewModel.uiState.value.increaseItemsToDisplay()

        val state = viewModel.uiState.value
        assertEquals(initialCount + 3, state.itemsToDisplays)
    }

    @Test
    fun `Multiple increaseItemsToDisplay calls accumulate`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.increaseItemsToDisplay()
        viewModel.uiState.value.increaseItemsToDisplay()

        val state = viewModel.uiState.value
        assertEquals(9, state.itemsToDisplays)
    }

    @Test
    fun `Refresh calls repository with forceNetwork true`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.loadingState.onRefresh()

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
        coEvery { repository.getEnrolledLearningLibraries(true) } returns updatedCollections

        viewModel.uiState.value.loadingState.onRefresh()

        val state = viewModel.uiState.value
        assertFalse(state.loadingState.isRefreshing)
        assertEquals(1, state.collections.size)
        assertEquals("New Collection", state.collections[0].name)
    }

    @Test
    fun `Refresh on error shows snackbar message`() = runTest {
        val viewModel = getViewModel()
        coEvery { repository.getEnrolledLearningLibraries(true) } throws Exception("Network error")

        viewModel.uiState.value.loadingState.onRefresh()

        val state = viewModel.uiState.value
        assertFalse(state.loadingState.isRefreshing)
        assertTrue(state.loadingState.snackbarMessage != null)
    }

    @Test
    fun `Dismiss snackbar clears snackbar message`() = runTest {
        val viewModel = getViewModel()
        coEvery { repository.getEnrolledLearningLibraries(true) } throws Exception("Network error")
        viewModel.uiState.value.loadingState.onRefresh()

        viewModel.uiState.value.loadingState.onSnackbarDismiss()

        val state = viewModel.uiState.value
        assertNull(state.loadingState.snackbarMessage)
    }

    @Test
    fun `Collections are mapped correctly to LearnLearningLibraryCollectionState`() = runTest {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        val firstCollection = state.collections[0]
        assertEquals("collection1", firstCollection.id)
        assertEquals("Introduction to Programming", firstCollection.name)
        assertEquals(1, firstCollection.itemCount)
        assertEquals(1, firstCollection.items.size)
    }

    @Test
    fun `Collection items are mapped correctly`() = runTest {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        val firstItem = state.collections[0].items[0]
        assertEquals("item1", firstItem.id)
        assertEquals("Python Basics", firstItem.name)
        assertFalse(firstItem.isBookmarked)
        assertFalse(firstItem.isCompleted)
    }

    @Test
    fun `onBookmarkClicked sets loading state and updates bookmark`() = runTest {
        val viewModel = getViewModel()
        coEvery { repository.toggleLearningLibraryItemIsBookmarked("item1") } returns true

        viewModel.uiState.value.onBookmarkClicked("item1")

        val state = viewModel.uiState.value
        val firstItem = state.collections[0].items[0]
        assertTrue(firstItem.isBookmarked)
        assertFalse(firstItem.bookmarkLoading)
        coVerify { repository.toggleLearningLibraryItemIsBookmarked("item1") }
    }

    @Test
    fun `onBookmarkClicked handles errors and shows error message`() = runTest {
        val viewModel = getViewModel()
        every { resources.getString(R.string.learnLearningLibraryFailedToUpdateBookmarkMessage) } returns "Failed to update bookmark"
        coEvery { repository.toggleLearningLibraryItemIsBookmarked("item1") } throws Exception("Network error")

        viewModel.uiState.value.onBookmarkClicked("item1")

        val state = viewModel.uiState.value
        val firstItem = state.collections[0].items[0]
        assertFalse(firstItem.bookmarkLoading)
        assertTrue(state.loadingState.errorMessage != null)
    }

    @Test
    fun `onEnrollClicked sets loading state and updates item`() = runTest {
        val viewModel = getViewModel()
        val enrolledItem = createTestCollectionItem(
            id = "item1",
            courseId = "1",
            courseName = "Python Basics Enrolled",
            completionPercentage = 0.0,
            isBookmarked = false,
            isEnrolledInCanvas = true
        )
        coEvery { repository.enrollLearningLibraryItem("item1") } returns enrolledItem

        viewModel.uiState.value.onEnrollClicked("item1")

        val state = viewModel.uiState.value
        val firstItem = state.collections[0].items[0]
        assertFalse(firstItem.enrollLoading)
        coVerify { repository.enrollLearningLibraryItem("item1") }
    }

    @Test
    fun `onEnrollClicked handles errors and shows error message`() = runTest {
        val viewModel = getViewModel()
        every { resources.getString(R.string.learnLearningLibraryFailedToEnrollMessage) } returns "Failed to enroll"
        coEvery { repository.enrollLearningLibraryItem("item1") } throws Exception("Network error")

        viewModel.uiState.value.onEnrollClicked("item1")

        val state = viewModel.uiState.value
        val firstItem = state.collections[0].items[0]
        assertFalse(firstItem.enrollLoading)
        assertTrue(state.loadingState.errorMessage != null)
    }

    @Test
    fun `Item with completion percentage 100 is marked as completed`() = runTest {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        val thirdItem = state.collections[2].items[0]
        assertTrue(thirdItem.isCompleted)
    }

    @Test
    fun `Item with completion percentage less than 100 is not marked as completed`() = runTest {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        val secondItem = state.collections[1].items[0]
        assertFalse(secondItem.isCompleted)
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
        val firstItem = state.collections[0].items[0]
        assertTrue(firstItem.canEnroll)
    }

    @Test
    fun `Course item already enrolled in Canvas cannot enroll`() = runTest {
        val viewModel = getViewModel()

        val state = viewModel.uiState.value
        val firstItem = state.collections[0].items[0]
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
        val firstItem = state.collections[0].items[0]
        assertFalse(firstItem.canEnroll)
    }

    private fun getViewModel(): LearnLearningLibraryListViewModel {
        return LearnLearningLibraryListViewModel(resources, repository)
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
        items = items
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
        isEnrolledInCanvas = isEnrolledInCanvas
    )
}
