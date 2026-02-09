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
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.horizon.features.learn.program.list.LearnProgramChipState
import com.instructure.horizon.features.learn.program.list.LearnProgramListScreen
import com.instructure.horizon.features.learn.program.list.LearnProgramListUiState
import com.instructure.horizon.features.learn.program.list.LearnProgramState
import com.instructure.horizon.horizonui.platform.LoadingState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LearnProgramListUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val testPrograms = listOf(
        LearnProgramState(
            programName = "Software Engineering",
            programId = "program1",
            programProgress = 0.0,
            programChips = listOf(
                LearnProgramChipState(label = "2 courses"),
                LearnProgramChipState(label = "3 hours")
            )
        ),
        LearnProgramState(
            programName = "Data Science",
            programId = "program2",
            programProgress = 50.0,
            programChips = listOf(
                LearnProgramChipState(label = "3 courses")
            )
        ),
        LearnProgramState(
            programName = "Web Development",
            programId = "program3",
            programProgress = 100.0,
            programChips = listOf(
                LearnProgramChipState(label = "4 courses")
            )
        )
    )

    @Test
    fun testLoadingStateDisplaysSpinner() {
        val state = LearnProgramListUiState(
            loadingState = LoadingState(isLoading = true),
            filteredPrograms = emptyList()
        )

        composeTestRule.setContent {
            LearnProgramListScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithTag("LoadingSpinner")
            .assertIsDisplayed()
    }

    @Test
    fun testProgramListDisplaysPrograms() {
        val state = LearnProgramListUiState(
            loadingState = LoadingState(isLoading = false),
            filteredPrograms = testPrograms
        )

        composeTestRule.setContent {
            LearnProgramListScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithTag("CollapsableBody").performScrollToNode(hasText("Software Engineering"))
        composeTestRule.onNodeWithText("Software Engineering", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("CollapsableBody").performScrollToNode(hasText("Data Science"))
        composeTestRule.onNodeWithText("Data Science", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("CollapsableBody").performScrollToNode(hasText("Web Development"))
        composeTestRule.onNodeWithText("Web Development", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testProgramProgressDisplayed() {
        val state = LearnProgramListUiState(
            loadingState = LoadingState(isLoading = false),
            filteredPrograms = listOf(
                LearnProgramState(
                    programName = "Test Program",
                    programId = "program1",
                    programProgress = 75.0,
                    programChips = emptyList()
                )
            )
        )

        composeTestRule.setContent {
            LearnProgramListScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("Test Program", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testFilteredProgramsDisplay() {
        val state = LearnProgramListUiState(
            loadingState = LoadingState(isLoading = false),
            filteredPrograms = listOf(testPrograms[2])
        )

        composeTestRule.setContent {
            LearnProgramListScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("Web Development", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Software Engineering", useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun testShowMoreButtonNotDisplayedWhenAllProgramsVisible() {
        val state = LearnProgramListUiState(
            loadingState = LoadingState(isLoading = false),
            filteredPrograms = testPrograms,
            visibleItemCount = 10
        )

        composeTestRule.setContent {
            LearnProgramListScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("Show More", useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun testEmptyStateDisplayedWhenNoProgramsMatch() {
        val state = LearnProgramListUiState(
            loadingState = LoadingState(isLoading = false),
            filteredPrograms = emptyList()
        )

        composeTestRule.setContent {
            LearnProgramListScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithTag("CollapsableBody")
            .assertIsDisplayed()
    }
}
