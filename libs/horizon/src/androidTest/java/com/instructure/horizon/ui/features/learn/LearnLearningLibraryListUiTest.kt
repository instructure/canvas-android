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
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToNode
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionItemChipState
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionItemState
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionState
import com.instructure.horizon.features.learn.learninglibrary.list.LearnLearningLibraryListScreen
import com.instructure.horizon.features.learn.learninglibrary.list.LearnLearningLibraryListUiState
import com.instructure.horizon.horizonui.platform.LoadingState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LearnLearningLibraryListUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val testCollections = listOf(
        LearnLearningLibraryCollectionState(
            id = "collection1",
            name = "Software Engineering Basics",
            itemCount = 2,
            items = listOf(
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
                    type = CollectionItemType.PAGE,
                    chips = listOf(
                        LearnLearningLibraryCollectionItemChipState(label = "Page")
                    )
                )
            )
        ),
        LearnLearningLibraryCollectionState(
            id = "collection2",
            name = "Data Science Fundamentals",
            itemCount = 1,
            items = listOf(
                LearnLearningLibraryCollectionItemState(
                    id = "item3",
                    courseId = 3L,
                    imageUrl = "https://example.com/course3.png",
                    name = "Machine Learning",
                    isBookmarked = false,
                    bookmarkLoading = false,
                    isCompleted = true,
                    canEnroll = false,
                    enrollLoading = false,
                    type = CollectionItemType.COURSE,
                    chips = listOf(
                        LearnLearningLibraryCollectionItemChipState(label = "Course")
                    )
                )
            )
        ),
        LearnLearningLibraryCollectionState(
            id = "collection3",
            name = "Web Development",
            itemCount = 1,
            items = listOf(
                LearnLearningLibraryCollectionItemState(
                    id = "item4",
                    courseId = 4L,
                    imageUrl = "https://example.com/course4.png",
                    name = "React Basics",
                    isBookmarked = false,
                    bookmarkLoading = false,
                    isCompleted = false,
                    canEnroll = true,
                    enrollLoading = false,
                    type = CollectionItemType.COURSE,
                    chips = listOf(
                        LearnLearningLibraryCollectionItemChipState(label = "Course")
                    )
                )
            )
        )
    )

    @Test
    fun testLoadingStateDisplaysSpinner() {
        val state = LearnLearningLibraryListUiState(
            loadingState = LoadingState(isLoading = true),
            collections = emptyList()
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithTag("LoadingSpinner")
            .assertIsDisplayed()
    }

    @Test
    fun testCollectionListDisplaysCollections() {
        val state = LearnLearningLibraryListUiState(
            loadingState = LoadingState(isLoading = false),
            collections = testCollections,
            itemsToDisplays = 10
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onRoot().performScrollToNode(hasText("Software Engineering Basics"))
        composeTestRule.onNodeWithText("Software Engineering Basics", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testFilteredCollectionsDisplay() {
        val state = LearnLearningLibraryListUiState(
            loadingState = LoadingState(isLoading = false),
            collections = listOf(testCollections[1]),
            itemsToDisplays = 10
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("Data Science Fundamentals", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testEmptyStateDisplayedWhenNoCollections() {
        val state = LearnLearningLibraryListUiState(
            loadingState = LoadingState(isLoading = false),
            collections = emptyList()
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("Software Engineering Basics", useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun testBookmarkIconButtonDisplayed() {
        val state = LearnLearningLibraryListUiState(
            loadingState = LoadingState(isLoading = false),
            collections = testCollections,
            itemsToDisplays = 10
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithContentDescription("Bookmarked Learning Library items")
            .assertIsDisplayed()
    }

    @Test
    fun testHistoryIconButtonDisplayed() {
        val state = LearnLearningLibraryListUiState(
            loadingState = LoadingState(isLoading = false),
            collections = testCollections,
            itemsToDisplays = 10
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithContentDescription("Completed Learning Library items")
            .assertIsDisplayed()
    }
}