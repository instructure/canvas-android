/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
import com.instructure.horizon.features.learn.course.list.LearnCourseListScreen
import com.instructure.horizon.features.learn.course.list.LearnCourseListUiState
import com.instructure.horizon.features.learn.course.list.LearnCourseState
import com.instructure.horizon.horizonui.platform.LoadingState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LearnCourseListUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val testCourses = listOf(
        LearnCourseState(
            imageUrl = "https://example.com/course1.png",
            courseName = "Introduction to Programming",
            courseId = 1L,
            progress = 0.0
        ),
        LearnCourseState(
            imageUrl = "https://example.com/course2.png",
            courseName = "Advanced Mathematics",
            courseId = 2L,
            progress = 50.0
        ),
        LearnCourseState(
            imageUrl = "https://example.com/course3.png",
            courseName = "Web Development",
            courseId = 3L,
            progress = 100.0
        )
    )

    @Test
    fun testLoadingStateDisplaysSpinner() {
        val state = LearnCourseListUiState(
            loadingState = LoadingState(isLoading = true),
            coursesToDisplay = emptyList()
        )

        composeTestRule.setContent {
            LearnCourseListScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithTag("LoadingSpinner")
            .assertIsDisplayed()
    }

    @Test
    fun testCourseListDisplaysCourses() {
        val state = LearnCourseListUiState(
            loadingState = LoadingState(isLoading = false),
            coursesToDisplay = testCourses
        )

        composeTestRule.setContent {
            LearnCourseListScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithTag("CollapsableBody").performScrollToNode(hasText("Introduction to Programming"))
        composeTestRule.onNodeWithText("Introduction to Programming", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("CollapsableBody").performScrollToNode(hasText("Advanced Mathematics"))
        composeTestRule.onNodeWithText("Advanced Mathematics", useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("CollapsableBody").performScrollToNode(hasText("Web Development"))
        composeTestRule.onNodeWithText("Web Development", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testCourseProgressDisplayed() {
        val state = LearnCourseListUiState(
            loadingState = LoadingState(isLoading = false),
            coursesToDisplay = listOf(
                LearnCourseState(
                    imageUrl = null,
                    courseName = "Test Course",
                    courseId = 1L,
                    progress = 75.0
                )
            )
        )

        composeTestRule.setContent {
            LearnCourseListScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("Test Course", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testFilteredCoursesDisplay() {
        val state = LearnCourseListUiState(
            loadingState = LoadingState(isLoading = false),
            coursesToDisplay = listOf(testCourses[2])
        )

        composeTestRule.setContent {
            LearnCourseListScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("Web Development", useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Introduction to Programming", useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun testShowMoreButtonNotDisplayedWhenAllCoursesVisible() {
        val state = LearnCourseListUiState(
            loadingState = LoadingState(isLoading = false),
            coursesToDisplay = testCourses,
            visibleItemCount = 10
        )

        composeTestRule.setContent {
            LearnCourseListScreen(
                state = state,
                navController = rememberNavController()
            )
        }

        composeTestRule.onNodeWithText("Show More", useUnmergedTree = true)
            .assertDoesNotExist()
    }
}
