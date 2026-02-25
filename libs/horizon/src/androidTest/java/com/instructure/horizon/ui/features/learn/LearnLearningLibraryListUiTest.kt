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
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.text.input.TextFieldValue
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionItemChipState
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionItemState
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionState
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryStatusFilter
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryTypeFilter
import com.instructure.horizon.features.learn.learninglibrary.list.LearnLearningLibraryListCollectionUiState
import com.instructure.horizon.features.learn.learninglibrary.list.LearnLearningLibraryListItemUiState
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
                    imageUrl = null,
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
                    imageUrl = null,
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
                    imageUrl = null,
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
                    imageUrl = null,
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

    private val testItems = listOf(
        LearnLearningLibraryCollectionItemState(
            id = "item1",
            courseId = 1L,
            imageUrl = null,
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
            imageUrl = null,
            name = "Machine Learning",
            isBookmarked = true,
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

    @Test
    fun testLoadingStateDisplaysSpinner() {
        val state = LearnLearningLibraryListUiState(
            collectionState = LearnLearningLibraryListCollectionUiState(
                loadingState = LoadingState(isLoading = true)
            )
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithTag("LoadingSpinner").assertIsDisplayed()
    }

    @Test
    fun testItemLoadingStateDisplaysSpinnerWhenFilterActive() {
        val state = LearnLearningLibraryListUiState(
            statusFilter = LearnLearningLibraryStatusFilter.Bookmarked,
            itemState = LearnLearningLibraryListItemUiState(
                loadingState = LoadingState(isLoading = true)
            )
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithTag("LoadingSpinner").assertIsDisplayed()
    }

    @Test
    fun testAllCollectionsDisplayed() {
        val state = LearnLearningLibraryListUiState(
            collectionState = LearnLearningLibraryListCollectionUiState(
                collections = testCollections,
                itemsToDisplays = 10
            )
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithTag("CollapsableBody").performScrollToNode(hasText("Software Engineering Basics"))
        composeTestRule.onNodeWithText("Software Engineering Basics", useUnmergedTree = true).assertIsDisplayed()

        composeTestRule.onNodeWithTag("CollapsableBody").performScrollToNode(hasText("Data Science Fundamentals"))
        composeTestRule.onNodeWithText("Data Science Fundamentals", useUnmergedTree = true).assertIsDisplayed()

        composeTestRule.onNodeWithTag("CollapsableBody").performScrollToNode(hasText("Web Development"))
        composeTestRule.onNodeWithText("Web Development", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testSingleFilteredCollectionDisplayed() {
        val state = LearnLearningLibraryListUiState(
            collectionState = LearnLearningLibraryListCollectionUiState(
                collections = listOf(testCollections[1]),
                itemsToDisplays = 10
            )
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Data Science Fundamentals", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Software Engineering Basics", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun testEmptyCollectionsShowsNoCollectionNames() {
        val state = LearnLearningLibraryListUiState(
            collectionState = LearnLearningLibraryListCollectionUiState(
                collections = emptyList()
            )
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Software Engineering Basics", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun testCollectionItemNameDisplayedInsideCollection() {
        val state = LearnLearningLibraryListUiState(
            collectionState = LearnLearningLibraryListCollectionUiState(
                collections = testCollections,
                itemsToDisplays = 10
            )
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onRoot().performScrollToNode(hasText("Introduction to Programming"))
        composeTestRule.onNodeWithText("Introduction to Programming", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testShowMoreButtonDisplayedInCollectionViewWhenMoreCollectionsAvailable() {
        val state = LearnLearningLibraryListUiState(
            collectionState = LearnLearningLibraryListCollectionUiState(
                collections = testCollections,
                itemsToDisplays = 2
            )
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithTag("CollapsableBody").performScrollToNode(hasText("Show more"))
        composeTestRule.onNodeWithText("Show more", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testShowMoreButtonNotDisplayedInCollectionViewWhenAllCollectionsVisible() {
        val state = LearnLearningLibraryListUiState(
            collectionState = LearnLearningLibraryListCollectionUiState(
                collections = testCollections,
                itemsToDisplays = 10
            )
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Show more", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun testHiddenCollectionNotDisplayedWhenBeyondPageSize() {
        val state = LearnLearningLibraryListUiState(
            collectionState = LearnLearningLibraryListCollectionUiState(
                collections = testCollections,
                itemsToDisplays = 2
            )
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Web Development", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun testItemListDisplayedWhenStatusFilterActive() {
        val state = LearnLearningLibraryListUiState(
            statusFilter = LearnLearningLibraryStatusFilter.Bookmarked,
            itemState = LearnLearningLibraryListItemUiState(items = testItems)
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Introduction to Programming", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testItemListDisplayedWhenTypeFilterActive() {
        val state = LearnLearningLibraryListUiState(
            typeFilter = LearnLearningLibraryTypeFilter.Pages,
            itemState = LearnLearningLibraryListItemUiState(items = testItems)
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Introduction to Programming", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testItemListDisplayedWhenSearchQuerySet() {
        val state = LearnLearningLibraryListUiState(
            searchQuery = TextFieldValue("python"),
            itemState = LearnLearningLibraryListItemUiState(items = testItems)
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Introduction to Programming", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testMultipleItemsDisplayedInItemView() {
        val state = LearnLearningLibraryListUiState(
            statusFilter = LearnLearningLibraryStatusFilter.Bookmarked,
            itemState = LearnLearningLibraryListItemUiState(items = testItems)
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onRoot().performScrollToNode(hasText("Introduction to Programming"))
        composeTestRule.onNodeWithText("Introduction to Programming", useUnmergedTree = true).assertIsDisplayed()

        composeTestRule.onRoot().performScrollToNode(hasText("Machine Learning"))
        composeTestRule.onNodeWithText("Machine Learning", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testCollectionsNotShownWhenFiltersActive() {
        val state = LearnLearningLibraryListUiState(
            statusFilter = LearnLearningLibraryStatusFilter.Bookmarked,
            collectionState = LearnLearningLibraryListCollectionUiState(collections = testCollections),
            itemState = LearnLearningLibraryListItemUiState(items = testItems)
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Software Engineering Basics", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun testEmptyMessageDisplayedWhenFilteredItemsAreEmpty() {
        val state = LearnLearningLibraryListUiState(
            statusFilter = LearnLearningLibraryStatusFilter.Bookmarked,
            itemState = LearnLearningLibraryListItemUiState(items = emptyList())
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("No results found. Try adjusting your search terms.", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testEnrollButtonDisplayedForEligibleItem() {
        val state = LearnLearningLibraryListUiState(
            statusFilter = LearnLearningLibraryStatusFilter.Bookmarked,
            itemState = LearnLearningLibraryListItemUiState(
                items = listOf(testItems[0])
            )
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Enroll", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testEnrollButtonNotDisplayedWhenItemAlreadyEnrolled() {
        val state = LearnLearningLibraryListUiState(
            statusFilter = LearnLearningLibraryStatusFilter.Bookmarked,
            itemState = LearnLearningLibraryListItemUiState(
                items = listOf(testItems[1])
            )
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Enroll", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun testCompletedIconDisplayedForCompletedItem() {
        val state = LearnLearningLibraryListUiState(
            statusFilter = LearnLearningLibraryStatusFilter.Completed,
            itemState = LearnLearningLibraryListItemUiState(
                items = listOf(testItems[1])
            )
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onAllNodes(hasContentDescription("Completed")).onLast().assertIsDisplayed()
    }

    @Test
    fun testCompletedIconNotDisplayedForIncompleteItem() {
        val state = LearnLearningLibraryListUiState(
            statusFilter = LearnLearningLibraryStatusFilter.Bookmarked,
            itemState = LearnLearningLibraryListItemUiState(
                items = listOf(testItems[0])
            )
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithContentDescription("Completed").assertDoesNotExist()
    }

    @Test
    fun testBookmarkButtonDisplayedOnNonBookmarkedItem() {
        val state = LearnLearningLibraryListUiState(
            statusFilter = LearnLearningLibraryStatusFilter.Bookmarked,
            itemState = LearnLearningLibraryListItemUiState(
                items = listOf(testItems[0])
            )
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithContentDescription("Bookmark").assertIsDisplayed()
    }

    @Test
    fun testRemoveBookmarkButtonDisplayedForBookmarkedItem() {
        val state = LearnLearningLibraryListUiState(
            statusFilter = LearnLearningLibraryStatusFilter.Bookmarked,
            itemState = LearnLearningLibraryListItemUiState(
                items = listOf(testItems[1])
            )
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithContentDescription("Remove bookmark").assertIsDisplayed()
    }

    @Test
    fun testItemChipDisplayed() {
        val state = LearnLearningLibraryListUiState(
            statusFilter = LearnLearningLibraryStatusFilter.Bookmarked,
            itemState = LearnLearningLibraryListItemUiState(
                items = listOf(testItems[0])
            )
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Course", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("60 mins", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testShowMoreButtonDisplayedInItemViewWhenMoreButtonLoading() {
        val state = LearnLearningLibraryListUiState(
            statusFilter = LearnLearningLibraryStatusFilter.Bookmarked,
            itemState = LearnLearningLibraryListItemUiState(
                items = testItems,
                isMoreButtonLoading = true
            )
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithTag("CollapsableBody").performScrollToNode(hasText("Show more"))
        composeTestRule.onNodeWithText("Show more", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testShowMoreButtonNotDisplayedInItemViewWhenNotLoading() {
        val state = LearnLearningLibraryListUiState(
            statusFilter = LearnLearningLibraryStatusFilter.Bookmarked,
            itemState = LearnLearningLibraryListItemUiState(
                items = testItems,
                isMoreButtonLoading = false
            )
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("Show more", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun testBookmarkIconButtonDisplayed() {
        val state = LearnLearningLibraryListUiState(
            collectionState = LearnLearningLibraryListCollectionUiState(
                collections = testCollections,
                itemsToDisplays = 10
            )
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithContentDescription("Bookmarked items").assertIsDisplayed()
    }

    @Test
    fun testClearFiltersButtonDisplayedWhenTypeFilterActive() {
        val state = LearnLearningLibraryListUiState(
            typeFilter = LearnLearningLibraryTypeFilter.Pages,
            itemState = LearnLearningLibraryListItemUiState(items = emptyList())
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithContentDescription("Clear filters").assertIsDisplayed()
    }

    @Test
    fun testClearFiltersButtonDisplayedWhenStatusFilterActive() {
        val state = LearnLearningLibraryListUiState(
            statusFilter = LearnLearningLibraryStatusFilter.Bookmarked,
            itemState = LearnLearningLibraryListItemUiState(items = emptyList())
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithContentDescription("Clear filters").assertIsDisplayed()
    }

    @Test
    fun testClearFiltersButtonNotDisplayedWhenNoFiltersActive() {
        val state = LearnLearningLibraryListUiState(
            collectionState = LearnLearningLibraryListCollectionUiState(
                collections = testCollections,
                itemsToDisplays = 10
            )
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithContentDescription("Clear filters").assertDoesNotExist()
    }

    @Test
    fun testItemCountDisplayedForVisibleCollections() {
        val state = LearnLearningLibraryListUiState(
            collectionState = LearnLearningLibraryListCollectionUiState(
                collections = testCollections,
                itemsToDisplays = 3
            )
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("3", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testItemCountReflectsPageSizeNotTotalCollections() {
        val state = LearnLearningLibraryListUiState(
            collectionState = LearnLearningLibraryListCollectionUiState(
                collections = testCollections,
                itemsToDisplays = 2
            )
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("2", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testItemCountDisplayedForFilteredItems() {
        val state = LearnLearningLibraryListUiState(
            statusFilter = LearnLearningLibraryStatusFilter.Bookmarked,
            itemState = LearnLearningLibraryListItemUiState(items = testItems)
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("2", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun testItemCountNotDisplayedWhenNoItemsInFilteredView() {
        val state = LearnLearningLibraryListUiState(
            statusFilter = LearnLearningLibraryStatusFilter.Bookmarked,
            itemState = LearnLearningLibraryListItemUiState(items = emptyList())
        )

        composeTestRule.setContent {
            LearnLearningLibraryListScreen(state = state, navController = rememberNavController())
        }

        composeTestRule.onNodeWithText("0", useUnmergedTree = true).assertDoesNotExist()
    }
}
