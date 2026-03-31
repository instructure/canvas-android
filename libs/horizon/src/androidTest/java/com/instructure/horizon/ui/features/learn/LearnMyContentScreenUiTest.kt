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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.journey.learninglibrary.CollectionItemType
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionItemChipState
import com.instructure.horizon.features.learn.learninglibrary.common.LearnLearningLibraryCollectionItemState
import com.instructure.horizon.features.learn.mycontent.LearnMyContentTab
import com.instructure.horizon.features.learn.mycontent.common.LearnContentCardButtonState
import com.instructure.horizon.features.learn.mycontent.common.LearnContentCardChipState
import com.instructure.horizon.features.learn.mycontent.common.LearnContentCardState
import com.instructure.horizon.features.learn.mycontent.common.LearnMyContentUiState
import com.instructure.horizon.features.learn.mycontent.completed.LearnMyContentCompletedContent
import com.instructure.horizon.features.learn.mycontent.inprogress.LearnMyContentInProgressContent
import com.instructure.horizon.features.learn.mycontent.saved.LearnMyContentSavedContent
import com.instructure.horizon.horizonui.molecules.FilterChip
import com.instructure.horizon.horizonui.molecules.FilterChipSize
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.horizon.pages.LearnMyContentPage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LearnMyContentScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val page = LearnMyContentPage(composeTestRule)

    @Test
    fun allThreeTabsAreDisplayed() {
        composeTestRule.setContent {
            TabSelectorContent(selectedTab = LearnMyContentTab.InProgress, onTabSelected = {})
        }

        page.assertTabDisplayed("In Progress")
        page.assertTabDisplayed("Completed")
        page.assertTabDisplayed("Saved")
    }

    @Test
    fun inProgressTabIsSelectedByDefault() {
        composeTestRule.setContent {
            TabSelectorContent(selectedTab = LearnMyContentTab.InProgress, onTabSelected = {})
        }

        composeTestRule.onNodeWithText("In Progress").assertIsSelected()
    }

    @Test
    fun clickingCompletedTabCallsOnTabSelectedWithCompleted() {
        var selectedTab = LearnMyContentTab.InProgress
        composeTestRule.setContent {
            TabSelectorContent(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
        }

        composeTestRule.onNodeWithText("Completed").performClick()

        assert(selectedTab == LearnMyContentTab.Completed)
    }

    @Test
    fun clickingSavedTabCallsOnTabSelectedWithSaved() {
        var selectedTab = LearnMyContentTab.InProgress
        composeTestRule.setContent {
            TabSelectorContent(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
        }

        composeTestRule.onNodeWithText("Saved").performClick()

        assert(selectedTab == LearnMyContentTab.Saved)
    }

    @Test
    fun emptyStateMessageShownWhenInProgressContentCardsIsEmpty() {
        composeTestRule.setContent {
            LearnMyContentInProgressContent(
                uiState = emptyContentState(),
                navController = rememberNavController(),
            )
        }

        page.assertEmptyMessageDisplayed()
    }

    @Test
    fun emptyStateMessageNotShownWhenInProgressHasItems() {
        composeTestRule.setContent {
            LearnMyContentInProgressContent(
                uiState = contentStateWithItems(),
                navController = rememberNavController(),
            )
        }

        composeTestRule.onNodeWithText("No results found", substring = true)
            .assertDoesNotExist()
    }

    @Test
    fun itemCardNameIsDisplayedInInProgress() {
        composeTestRule.setContent {
            LearnMyContentInProgressContent(
                uiState = contentStateWithItems(),
                navController = rememberNavController(),
            )
        }

        page.assertItemCardDisplayed("Introduction to Programming")
    }

    @Test
    fun multipleItemCardsAreDisplayedInInProgress() {
        composeTestRule.setContent {
            LearnMyContentInProgressContent(
                uiState = contentStateWithItems(),
                navController = rememberNavController(),
            )
        }

        page.assertItemCardDisplayed("Introduction to Programming")
        page.assertItemCardDisplayed("Advanced Data Structures")
    }

    @Test
    fun itemCountDisplayedInInProgress() {
        composeTestRule.setContent {
            LearnMyContentInProgressContent(
                uiState = contentStateWithItems(totalItemCount = 5),
                navController = rememberNavController(),
            )
        }

        page.assertItemCountDisplayed(5)
    }

    @Test
    fun showMoreButtonIsVisibleWhenShowMoreButtonIsTrueInInProgress() {
        composeTestRule.setContent {
            LearnMyContentInProgressContent(
                uiState = contentStateWithItems(showMoreButton = true),
                navController = rememberNavController(),
            )
        }

        page.assertShowMoreButtonDisplayed()
    }

    @Test
    fun showMoreButtonIsNotVisibleWhenShowMoreButtonIsFalseInInProgress() {
        composeTestRule.setContent {
            LearnMyContentInProgressContent(
                uiState = contentStateWithItems(showMoreButton = false),
                navController = rememberNavController(),
            )
        }

        page.assertShowMoreButtonNotDisplayed()
    }

    @Test
    fun emptyStateMessageShownInCompletedTabWhenNoItems() {
        composeTestRule.setContent {
            LearnMyContentCompletedContent(
                uiState = emptyContentState(),
                navController = rememberNavController(),
            )
        }

        page.assertEmptyMessageDisplayed()
    }

    @Test
    fun itemCardDisplayedInCompletedTab() {
        composeTestRule.setContent {
            LearnMyContentCompletedContent(
                uiState = contentStateWithItems(),
                navController = rememberNavController(),
            )
        }

        page.assertItemCardDisplayed("Introduction to Programming")
    }

    @Test
    fun showMoreButtonDisplayedInCompletedTabWhenAvailable() {
        composeTestRule.setContent {
            LearnMyContentCompletedContent(
                uiState = contentStateWithItems(showMoreButton = true),
                navController = rememberNavController(),
            )
        }

        page.assertShowMoreButtonDisplayed()
    }

    // ─── Saved content tests ────────────────────────────────────────────────────

    @Test
    fun emptyStateMessageShownInSavedTabWhenNoItems() {
        composeTestRule.setContent {
            LearnMyContentSavedContent(
                uiState = emptyCollectionItemState(),
                navController = rememberNavController(),
            )
        }

        page.assertEmptyMessageDisplayed()
    }

    @Test
    fun savedTabRendersLearnLearningLibraryItemWithItemName() {
        composeTestRule.setContent {
            LearnMyContentSavedContent(
                uiState = savedContentStateWithItems(),
                navController = rememberNavController(),
            )
        }

        page.assertItemCardDisplayed("Saved Course")
    }

    @Test
    fun showMoreButtonDisplayedInSavedTabWhenAvailable() {
        composeTestRule.setContent {
            LearnMyContentSavedContent(
                uiState = savedContentStateWithItems(showMoreButton = true),
                navController = rememberNavController(),
            )
        }

        page.assertShowMoreButtonDisplayed()
    }

    @Composable
    private fun TabSelectorContent(
        selectedTab: LearnMyContentTab,
        onTabSelected: (LearnMyContentTab) -> Unit,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                label = stringResource(R.string.LearnMyContentInProgressLabel),
                selected = selectedTab == LearnMyContentTab.InProgress,
                onClick = { onTabSelected(LearnMyContentTab.InProgress) },
                size = FilterChipSize.LARGE,
            )
            FilterChip(
                label = stringResource(R.string.LearnMyContentCompletedLabel),
                selected = selectedTab == LearnMyContentTab.Completed,
                onClick = { onTabSelected(LearnMyContentTab.Completed) },
                size = FilterChipSize.LARGE,
            )
            FilterChip(
                label = stringResource(R.string.LearnMyContentSavedLabel),
                selected = selectedTab == LearnMyContentTab.Saved,
                onClick = { onTabSelected(LearnMyContentTab.Saved) },
                size = FilterChipSize.LARGE,
            )
        }
    }

    private fun emptyContentState(totalItemCount: Int = 0) = LearnMyContentUiState<LearnContentCardState>(
        loadingState = LoadingState(),
        contentCards = emptyList(),
        totalItemCount = totalItemCount,
        showMoreButton = false,
        isMoreLoading = false,
    )

    private fun contentStateWithItems(
        totalItemCount: Int = 2,
        showMoreButton: Boolean = false,
    ) = LearnMyContentUiState(
        loadingState = LoadingState(),
        contentCards = listOf(
            LearnContentCardState(
                name = "Introduction to Programming",
                progress = 45.0,
                route = "",
                cardChips = listOf(
                    LearnContentCardChipState(label = "Program", color = StatusChipColor.Violet),
                    LearnContentCardChipState(label = "3 courses"),
                ),
            ),
            LearnContentCardState(
                name = "Advanced Data Structures",
                progress = 10.0,
                route = "",
                buttonState = LearnContentCardButtonState(label = "Start learning", route = ""),
                cardChips = listOf(
                    LearnContentCardChipState(label = "Course", color = StatusChipColor.Institution),
                ),
            ),
        ),
        totalItemCount = totalItemCount,
        showMoreButton = showMoreButton,
        isMoreLoading = false,
    )

    private fun emptyCollectionItemState() = LearnMyContentUiState<LearnLearningLibraryCollectionItemState>(
        loadingState = LoadingState(),
        contentCards = emptyList(),
        totalItemCount = 0,
        showMoreButton = false,
        isMoreLoading = false,
    )

    private fun savedContentStateWithItems(
        showMoreButton: Boolean = false,
    ) = LearnMyContentUiState(
        loadingState = LoadingState(),
        contentCards = listOf(
            LearnLearningLibraryCollectionItemState(
                id = "item1",
                imageUrl = null,
                name = "Saved Course",
                description = null,
                isBookmarked = true,
                bookmarkLoading = false,
                canEnroll = false,
                type = CollectionItemType.COURSE,
                route = null,
                chips = listOf(LearnLearningLibraryCollectionItemChipState(label = "Course")),
            )
        ),
        totalItemCount = 1,
        showMoreButton = showMoreButton,
        isMoreLoading = false,
    )
}
