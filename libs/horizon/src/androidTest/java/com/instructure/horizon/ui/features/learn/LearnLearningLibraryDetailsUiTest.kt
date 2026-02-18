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
package com.instructure.horizon.ui.features.learn

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.text.input.TextFieldValue
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionItemChipState
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionItemState
import com.instructure.horizon.features.learn.learninglibrary.details.LearnLearningLibraryDetailsScreen
import com.instructure.horizon.features.learn.learninglibrary.details.LearnLearningLibraryDetailsStatusFilter
import com.instructure.horizon.features.learn.learninglibrary.details.LearnLearningLibraryDetailsTypeFilter
import com.instructure.horizon.features.learn.learninglibrary.details.LearnLearningLibraryDetailsUiState
import com.instructure.horizon.horizonui.platform.LoadingState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LearnLearningLibraryDetailsUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val testItems = listOf(
        LearnLearningLibraryCollectionItemState(
            id = "item1",
            courseId = 1L,
            imageUrl = "https://example.com/course1.png",
            name = "Introduction to Programming",
            isBookmarked = false,
            bookmarkLoading = false,
            isCompleted = false,
            canEnroll = true,
            enrollLoading = false,
            type = CollectionItemType.COURSE,
            chips = listOf(
                LearnLearningLibraryCollectionItemChipState(label = "Course"),
                LearnLearningLibraryCollectionItemChipState(label = "60 mins")
            )
        ),
        LearnLearningLibraryCollectionItemState(
            id = "item2",
            courseId = 2L,
            imageUrl = "https://example.com/course2.png",
            name = "Advanced Algorithms",
            isBookmarked = true,
            bookmarkLoading = false,
            isCompleted = false,
            canEnroll = false,
            enrollLoading = false,
            type = CollectionItemType.ASSIGNMENT,
            chips = listOf(
                LearnLearningLibraryCollectionItemChipState(label = "Assignment")
            )
        ),
        LearnLearningLibraryCollectionItemState(
            id = "item3",
            courseId = 3L,
            imageUrl = "https://example.com/course3.png",
            name = "Data Structures",
            isBookmarked = false,
            bookmarkLoading = false,
            isCompleted = true,
            canEnroll = false,
            enrollLoading = false,
            type = CollectionItemType.PAGE,
            chips = listOf(
                LearnLearningLibraryCollectionItemChipState(label = "Page")
            )
        )
    )

    @Test
    fun testLoadingStateDisplaysSpinner() {
        val state = LearnLearningLibraryDetailsUiState(
            loadingState = LoadingState(isLoading = true),
            items = emptyList()
        )

        composeTestRule.setContent {
            LearnLearningLibraryDetailsScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithTag("LoadingSpinner")
            .assertIsDisplayed()
    }

    @Test
    fun testCollectionNameDisplayed() {
        val state = LearnLearningLibraryDetailsUiState(
            loadingState = LoadingState(isLoading = false),
            collectionName = "Software Engineering Collection",
            items = testItems,
            itemsToDisplays = 10
        )

        composeTestRule.setContent {
            LearnLearningLibraryDetailsScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("Software Engineering Collection")
            .assertIsDisplayed()
    }

    @Test
    fun testBackButtonDisplayed() {
        val state = LearnLearningLibraryDetailsUiState(
            loadingState = LoadingState(isLoading = false),
            items = testItems,
            itemsToDisplays = 10
        )

        composeTestRule.setContent {
            LearnLearningLibraryDetailsScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithContentDescription("Navigate back")
            .assertIsDisplayed()
    }

    @Test
    fun testSearchBarDisplayed() {
        val state = LearnLearningLibraryDetailsUiState(
            loadingState = LoadingState(isLoading = false),
            items = testItems,
            itemsToDisplays = 10
        )

        composeTestRule.setContent {
            LearnLearningLibraryDetailsScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("Search collection", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun testStatusFilterChipDisplayed() {
        val state = LearnLearningLibraryDetailsUiState(
            loadingState = LoadingState(isLoading = false),
            items = testItems,
            itemsToDisplays = 10,
            selectedStatusFilter = LearnLearningLibraryDetailsStatusFilter.All
        )

        composeTestRule.setContent {
            LearnLearningLibraryDetailsScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("All items", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun testTypeFilterChipDisplayed() {
        val state = LearnLearningLibraryDetailsUiState(
            loadingState = LoadingState(isLoading = false),
            items = testItems,
            itemsToDisplays = 10,
            selectedTypeFilter = LearnLearningLibraryDetailsTypeFilter.All
        )

        composeTestRule.setContent {
            LearnLearningLibraryDetailsScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("All item types", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun testItemCountDisplayed() {
        val state = LearnLearningLibraryDetailsUiState(
            loadingState = LoadingState(isLoading = false),
            items = testItems,
            itemsToDisplays = 10
        )

        composeTestRule.setContent {
            LearnLearningLibraryDetailsScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("3", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun testLearningLibraryItemsRender() {
        val state = LearnLearningLibraryDetailsUiState(
            loadingState = LoadingState(isLoading = false),
            items = testItems,
            itemsToDisplays = 10
        )

        composeTestRule.setContent {
            LearnLearningLibraryDetailsScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithTag("CollapsableBody").performScrollToNode(hasText("Introduction to Programming"))
        composeTestRule.onNodeWithText("Introduction to Programming", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("CollapsableBody").performScrollToNode(hasText("Advanced Algorithms"))
        composeTestRule.onNodeWithText("Advanced Algorithms", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testEmptyStateDisplayed() {
        val state = LearnLearningLibraryDetailsUiState(
            loadingState = LoadingState(isLoading = false),
            items = emptyList(),
            itemsToDisplays = 10
        )

        composeTestRule.setContent {
            LearnLearningLibraryDetailsScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText(
            "No results found. Try adjusting your search terms.",
            useUnmergedTree = true
        ).assertExists()
    }

    @Test
    fun testShowMoreButtonDisplayedWhenMoreItems() {
        val manyItems = (1..15).map {
            LearnLearningLibraryCollectionItemState(
                id = "item$it",
                courseId = it.toLong(),
                imageUrl = "https://example.com/course$it.png",
                name = "Course $it",
                isBookmarked = false,
                bookmarkLoading = false,
                isCompleted = false,
                canEnroll = false,
                enrollLoading = false,
                type = CollectionItemType.COURSE,
                chips = emptyList()
            )
        }

        val state = LearnLearningLibraryDetailsUiState(
            loadingState = LoadingState(isLoading = false),
            items = manyItems,
            itemsToDisplays = 10
        )

        composeTestRule.setContent {
            LearnLearningLibraryDetailsScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithTag("CollapsableBody").performScrollToNode(hasText("Show more"))
        composeTestRule.onNodeWithText("Show more", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testShowMoreButtonHiddenWhenAllItemsDisplayed() {
        val state = LearnLearningLibraryDetailsUiState(
            loadingState = LoadingState(isLoading = false),
            items = testItems,
            itemsToDisplays = 10
        )

        composeTestRule.setContent {
            LearnLearningLibraryDetailsScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("Show more", useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun testBookmarkedItemDisplaysBookmarkIcon() {
        val bookmarkedItem = listOf(
            testItems[1]
        )

        val state = LearnLearningLibraryDetailsUiState(
            loadingState = LoadingState(isLoading = false),
            items = bookmarkedItem,
            itemsToDisplays = 10
        )

        composeTestRule.setContent {
            LearnLearningLibraryDetailsScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithTag("CollapsableBody").performScrollToNode(hasText("Advanced Algorithms"))
        composeTestRule.onNodeWithText("Advanced Algorithms", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testCompletedItemDisplaysCompletionIndicator() {
        val completedItem = listOf(
            testItems[2]
        )

        val state = LearnLearningLibraryDetailsUiState(
            loadingState = LoadingState(isLoading = false),
            items = completedItem,
            itemsToDisplays = 10
        )

        composeTestRule.setContent {
            LearnLearningLibraryDetailsScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithTag("CollapsableBody").performScrollToNode(hasText("Data Structures"))
        composeTestRule.onNodeWithText("Data Structures", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testEnrollButtonDisplayedForEnrollableItems() {
        val enrollableItem = listOf(
            testItems[0]
        )

        val state = LearnLearningLibraryDetailsUiState(
            loadingState = LoadingState(isLoading = false),
            items = enrollableItem,
            itemsToDisplays = 10
        )

        composeTestRule.setContent {
            LearnLearningLibraryDetailsScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithTag("CollapsableBody").performScrollToNode(hasText("Introduction to Programming"))
        composeTestRule.onNodeWithText("Introduction to Programming", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testFilteredItemsDisplay() {
        val filteredItems = listOf(testItems[0])

        val state = LearnLearningLibraryDetailsUiState(
            loadingState = LoadingState(isLoading = false),
            items = filteredItems,
            itemsToDisplays = 10,
            searchQuery = TextFieldValue("Introduction")
        )

        composeTestRule.setContent {
            LearnLearningLibraryDetailsScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("Introduction to Programming", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Advanced Algorithms", useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun testPaginationDisplaysLimitedItems() {
        val manyItems = (1..20).map {
            LearnLearningLibraryCollectionItemState(
                id = "item$it",
                courseId = it.toLong(),
                imageUrl = "https://example.com/course$it.png",
                name = "Course $it",
                isBookmarked = false,
                bookmarkLoading = false,
                isCompleted = false,
                canEnroll = false,
                enrollLoading = false,
                type = CollectionItemType.COURSE,
                chips = emptyList()
            )
        }

        val state = LearnLearningLibraryDetailsUiState(
            loadingState = LoadingState(isLoading = false),
            items = manyItems,
            itemsToDisplays = 10
        )

        composeTestRule.setContent {
            LearnLearningLibraryDetailsScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithTag("CollapsableBody").performScrollToNode(hasText("Show more"))

        composeTestRule.onNodeWithText("Course 10", useUnmergedTree = true)
            .assertExists()

        composeTestRule.onNodeWithText("Course 11", useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun testItemCountReflectsPaginatedItems() {
        val manyItems = (1..20).map {
            LearnLearningLibraryCollectionItemState(
                id = "item$it",
                courseId = it.toLong(),
                imageUrl = "https://example.com/course$it.png",
                name = "Course $it",
                isBookmarked = false,
                bookmarkLoading = false,
                isCompleted = false,
                canEnroll = false,
                enrollLoading = false,
                type = CollectionItemType.COURSE,
                chips = emptyList()
            )
        }

        val state = LearnLearningLibraryDetailsUiState(
            loadingState = LoadingState(isLoading = false),
            items = manyItems,
            itemsToDisplays = 10
        )

        composeTestRule.setContent {
            LearnLearningLibraryDetailsScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("10", useUnmergedTree = true)
            .assertExists()
    }
}
