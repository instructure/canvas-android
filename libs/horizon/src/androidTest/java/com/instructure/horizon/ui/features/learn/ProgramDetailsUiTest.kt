/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.horizon.ui.features.learn

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.horizon.features.learn.program.details.ProgramDetailTag
import com.instructure.horizon.features.learn.program.details.ProgramDetailsScreen
import com.instructure.horizon.features.learn.program.details.ProgramDetailsUiState
import com.instructure.horizon.features.learn.program.details.ProgressBarStatus
import com.instructure.horizon.features.learn.program.details.ProgressBarUiState
import com.instructure.horizon.features.learn.program.details.components.CourseCardChipState
import com.instructure.horizon.features.learn.program.details.components.CourseCardStatus
import com.instructure.horizon.features.learn.program.details.components.ProgramCourseCardState
import com.instructure.horizon.features.learn.program.details.components.ProgramProgressItemState
import com.instructure.horizon.features.learn.program.details.components.ProgramProgressItemStatus
import com.instructure.horizon.features.learn.program.details.components.ProgramProgressState
import com.instructure.horizon.features.learn.program.details.components.SequentialProgramProgressProperties
import com.instructure.horizon.horizonui.platform.LoadingState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgramDetailsUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoadingStateDisplaysSpinner() {
        val state = ProgramDetailsUiState(
            loadingState = LoadingState(isLoading = true)
        )

        composeTestRule.setContent {
            ProgramDetailsScreen(
                uiState = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithTag("LoadingSpinner")
            .assertIsDisplayed()
    }

    @Test
    fun testProgramNameDisplayed() {
        val state = ProgramDetailsUiState(
            loadingState = LoadingState(isLoading = false),
            programName = "Software Engineering Program"
        )

        composeTestRule.setContent {
            ProgramDetailsScreen(
                uiState = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("Software Engineering Program", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testProgressBarDisplayedWhenEnabled() {
        val state = ProgramDetailsUiState(
            loadingState = LoadingState(isLoading = false),
            programName = "Test Program",
            showProgressBar = true,
            progressBarUiState = ProgressBarUiState(
                progress = 50.0,
                progressBarStatus = ProgressBarStatus.IN_PROGRESS
            )
        )

        composeTestRule.setContent {
            ProgramDetailsScreen(
                uiState = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("Test Program", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testDescriptionDisplayed() {
        val state = ProgramDetailsUiState(
            loadingState = LoadingState(isLoading = false),
            programName = "Test Program",
            description = "This is a comprehensive program covering multiple courses."
        )

        composeTestRule.setContent {
            ProgramDetailsScreen(
                uiState = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("This is a comprehensive program covering multiple courses.", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testTagsDisplayed() {
        val state = ProgramDetailsUiState(
            loadingState = LoadingState(isLoading = false),
            programName = "Test Program",
            tags = listOf(
                ProgramDetailTag(name = "Jan 1, 2024 - Dec 31, 2024"),
                ProgramDetailTag(name = "3 hours")
            )
        )

        composeTestRule.setContent {
            ProgramDetailsScreen(
                uiState = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("Jan 1, 2024 - Dec 31, 2024", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("3 hours", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testCourseCardsDisplayed() {
        val state = ProgramDetailsUiState(
            loadingState = LoadingState(isLoading = false),
            programName = "Test Program",
            programProgressState = ProgramProgressState(
                courses = listOf(
                    ProgramProgressItemState(
                        courseCard = ProgramCourseCardState(
                            id = 1L,
                            courseName = "Introduction to Programming",
                            status = CourseCardStatus.Active,
                            courseProgress = 0.0,
                            chips = listOf(
                                CourseCardChipState(label = "Required")
                            ),
                            enabled = false
                        )
                    ),
                    ProgramProgressItemState(
                        courseCard = ProgramCourseCardState(
                            id = 2L,
                            courseName = "Advanced Algorithms",
                            status = CourseCardStatus.InProgress,
                            courseProgress = 50.0,
                            chips = listOf(
                                CourseCardChipState(label = "2 hours")
                            ),
                            enabled = true
                        )
                    )
                )
            )
        )

        composeTestRule.setContent {
            ProgramDetailsScreen(
                uiState = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("Introduction to Programming", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Advanced Algorithms", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testLinearProgramShowsSequentialIndicators() {
        val state = ProgramDetailsUiState(
            loadingState = LoadingState(isLoading = false),
            programName = "Linear Program",
            programProgressState = ProgramProgressState(
                courses = listOf(
                    ProgramProgressItemState(
                        courseCard = ProgramCourseCardState(
                            id = 1L,
                            courseName = "Course 1",
                            status = CourseCardStatus.Completed,
                            courseProgress = 100.0,
                            chips = emptyList(),
                            enabled = true
                        ),
                        sequentialProperties = SequentialProgramProgressProperties(
                            status = ProgramProgressItemStatus.Completed,
                            index = 1,
                            first = true,
                            last = false,
                            previousCompleted = false
                        )
                    ),
                    ProgramProgressItemState(
                        courseCard = ProgramCourseCardState(
                            id = 2L,
                            courseName = "Course 2",
                            status = CourseCardStatus.InProgress,
                            courseProgress = 25.0,
                            chips = emptyList(),
                            enabled = true
                        ),
                        sequentialProperties = SequentialProgramProgressProperties(
                            status = ProgramProgressItemStatus.Active,
                            index = 2,
                            first = false,
                            last = true,
                            previousCompleted = true
                        )
                    )
                )
            )
        )

        composeTestRule.setContent {
            ProgramDetailsScreen(
                uiState = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("Course 1", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Course 2", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testNonLinearProgramShowsDashedBordersForOptional() {
        val state = ProgramDetailsUiState(
            loadingState = LoadingState(isLoading = false),
            programName = "Non-Linear Program",
            programProgressState = ProgramProgressState(
                courses = listOf(
                    ProgramProgressItemState(
                        courseCard = ProgramCourseCardState(
                            id = 1L,
                            courseName = "Optional Course",
                            status = CourseCardStatus.Active,
                            courseProgress = 0.0,
                            chips = emptyList(),
                            dashedBorder = true,
                            enabled = false
                        )
                    ),
                    ProgramProgressItemState(
                        courseCard = ProgramCourseCardState(
                            id = 2L,
                            courseName = "Required Course",
                            status = CourseCardStatus.Active,
                            courseProgress = 0.0,
                            chips = emptyList(),
                            dashedBorder = false,
                            enabled = false
                        )
                    )
                )
            )
        )

        composeTestRule.setContent {
            ProgramDetailsScreen(
                uiState = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("Optional Course", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Required Course", useUnmergedTree = true)
            .assertIsDisplayed()
    }
}
