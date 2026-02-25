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
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.horizon.features.learn.learninglibrary.bookmarked.LearnLearningLibraryBookmarkedScreen
import com.instructure.horizon.features.learn.learninglibrary.bookmarked.LearnLearningLibraryBookmarkedUiState
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionItemChipState
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionItemState
import com.instructure.horizon.horizonui.platform.LoadingState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LearnLearningLibraryBookmarkedUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val testItems = listOf(
        LearnLearningLibraryCollectionItemState(
            id = "item1",
            courseId = 1L,
            imageUrl = null,
            name = "Python Basics",
            isBookmarked = false,
            bookmarkLoading = false,
            isCompleted = false,
            canEnroll = true,
            enrollLoading = false,
            type = CollectionItemType.COURSE,
            chips = listOf(LearnLearningLibraryCollectionItemChipState(label = "Course"))
        ),
        LearnLearningLibraryCollectionItemState(
            id = "item2",
            courseId = 2L,
            imageUrl = null,
            name = "React Advanced",
            isBookmarked = true,
            bookmarkLoading = false,
            isCompleted = false,
            canEnroll = false,
            enrollLoading = false,
            type = CollectionItemType.PAGE,
            chips = listOf(LearnLearningLibraryCollectionItemChipState(label = "Page"))
        ),
        LearnLearningLibraryCollectionItemState(
            id = "item3",
            courseId = 3L,
            imageUrl = null,
            name = "Machine Learning",
            isBookmarked = false,
            bookmarkLoading = false,
            isCompleted = true,
            canEnroll = false,
            enrollLoading = false,
            type = CollectionItemType.COURSE,
            chips = listOf(LearnLearningLibraryCollectionItemChipState(label = "Course"))
        )
    )

    @Test
    fun testLoadingStateDisplaysSpinner() {
        val state = LearnLearningLibraryBookmarkedUiState(
            loadingState = LoadingState(isLoading = true),
        )

        composeTestRule.setContent {
            LearnLearningLibraryBookmarkedScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithTag("LoadingSpinner").assertIsDisplayed()
    }

    @Test
    fun testTitleDisplayedInHeader() {
        val state = LearnLearningLibraryBookmarkedUiState(
            loadingState = LoadingState(isLoading = false),
            items = testItems
        )

        composeTestRule.setContent {
            LearnLearningLibraryBookmarkedScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Bookmarks").assertIsDisplayed()
    }

    @Test
    fun testBackButtonDisplayedInHeader() {
        val state = LearnLearningLibraryBookmarkedUiState(
            loadingState = LoadingState(isLoading = false),
        )

        composeTestRule.setContent {
            LearnLearningLibraryBookmarkedScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithContentDescription("Navigate back").assertIsDisplayed()
    }

    @Test
    fun testSearchBarDisplayedInHeader() {
        val state = LearnLearningLibraryBookmarkedUiState(
            loadingState = LoadingState(isLoading = false),
        )

        composeTestRule.setContent {
            LearnLearningLibraryBookmarkedScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Search").assertIsDisplayed()
    }

    @Test
    fun testItemsAreDisplayed() {
        val state = LearnLearningLibraryBookmarkedUiState(
            loadingState = LoadingState(isLoading = false),
            items = testItems
        )

        composeTestRule.setContent {
            LearnLearningLibraryBookmarkedScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithTag("CollapsableBody").performScrollToNode(hasText("Python Basics"))
        composeTestRule.onNodeWithText("Python Basics").assertIsDisplayed()
    }

    @Test
    fun testMultipleItemsAreDisplayed() {
        val state = LearnLearningLibraryBookmarkedUiState(
            loadingState = LoadingState(isLoading = false),
            items = testItems
        )

        composeTestRule.setContent {
            LearnLearningLibraryBookmarkedScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithTag("CollapsableBody").performScrollToNode(hasText("React Advanced"))
        composeTestRule.onNodeWithText("React Advanced").assertIsDisplayed()
    }

    @Test
    fun testEmptyMessageDisplayedWhenNoItems() {
        val state = LearnLearningLibraryBookmarkedUiState(
            loadingState = LoadingState(isLoading = false),
            items = emptyList()
        )

        composeTestRule.setContent {
            LearnLearningLibraryBookmarkedScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("No results found. Try adjusting your search terms.").assertIsDisplayed()
    }

    @Test
    fun testEmptyMessageNotDisplayedWhenItemsExist() {
        val state = LearnLearningLibraryBookmarkedUiState(
            loadingState = LoadingState(isLoading = false),
            items = testItems
        )

        composeTestRule.setContent {
            LearnLearningLibraryBookmarkedScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("No results found. Try adjusting your search terms.").assertDoesNotExist()
    }

    @Test
    fun testItemCountDisplayedInFilterBar() {
        val state = LearnLearningLibraryBookmarkedUiState(
            loadingState = LoadingState(isLoading = false),
            items = testItems
        )

        composeTestRule.setContent {
            LearnLearningLibraryBookmarkedScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun testShowMoreButtonDisplayedWhenEnabled() {
        val state = LearnLearningLibraryBookmarkedUiState(
            loadingState = LoadingState(isLoading = false),
            items = testItems,
            showMoreButton = true
        )

        composeTestRule.setContent {
            LearnLearningLibraryBookmarkedScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithTag("CollapsableBody").performScrollToNode(hasText("Show more"))
        composeTestRule.onNodeWithText("Show more").assertIsDisplayed()
    }

    @Test
    fun testShowMoreButtonNotDisplayedWhenDisabled() {
        val state = LearnLearningLibraryBookmarkedUiState(
            loadingState = LoadingState(isLoading = false),
            items = testItems,
            showMoreButton = false
        )

        composeTestRule.setContent {
            LearnLearningLibraryBookmarkedScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Show more").assertDoesNotExist()
    }

    @Test
    fun testEnrollButtonDisplayedForEnrollableItem() {
        val state = LearnLearningLibraryBookmarkedUiState(
            loadingState = LoadingState(isLoading = false),
            items = listOf(testItems[0])
        )

        composeTestRule.setContent {
            LearnLearningLibraryBookmarkedScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Enroll").assertIsDisplayed()
    }

    @Test
    fun testEnrollButtonNotDisplayedForNonEnrollableItem() {
        val state = LearnLearningLibraryBookmarkedUiState(
            loadingState = LoadingState(isLoading = false),
            items = listOf(testItems[1])
        )

        composeTestRule.setContent {
            LearnLearningLibraryBookmarkedScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Enroll").assertDoesNotExist()
    }

    @Test
    fun testBookmarkIconDisplayedForUnbookmarkedItem() {
        val state = LearnLearningLibraryBookmarkedUiState(
            loadingState = LoadingState(isLoading = false),
            items = listOf(testItems[0])
        )

        composeTestRule.setContent {
            LearnLearningLibraryBookmarkedScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithContentDescription("Bookmark").assertIsDisplayed()
    }

    @Test
    fun testRemoveBookmarkIconDisplayedForBookmarkedItem() {
        val state = LearnLearningLibraryBookmarkedUiState(
            loadingState = LoadingState(isLoading = false),
            items = listOf(testItems[1])
        )

        composeTestRule.setContent {
            LearnLearningLibraryBookmarkedScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithContentDescription("Remove bookmark").assertIsDisplayed()
    }

    @Test
    fun testCompletedIconDisplayedForCompletedItem() {
        val state = LearnLearningLibraryBookmarkedUiState(
            loadingState = LoadingState(isLoading = false),
            items = listOf(testItems[2])
        )

        composeTestRule.setContent {
            LearnLearningLibraryBookmarkedScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithContentDescription("Completed").assertIsDisplayed()
    }

    @Test
    fun testCompletedIconNotDisplayedForIncompleteItem() {
        val state = LearnLearningLibraryBookmarkedUiState(
            loadingState = LoadingState(isLoading = false),
            items = listOf(testItems[0])
        )

        composeTestRule.setContent {
            LearnLearningLibraryBookmarkedScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithContentDescription("Completed").assertDoesNotExist()
    }

    @Test
    fun testItemChipsAreDisplayed() {
        val state = LearnLearningLibraryBookmarkedUiState(
            loadingState = LoadingState(isLoading = false),
            items = listOf(testItems[1])
        )

        composeTestRule.setContent {
            LearnLearningLibraryBookmarkedScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Page").assertIsDisplayed()
    }
}
