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
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.horizon.features.learn.course.details.CourseDetailsScreen
import com.instructure.horizon.features.learn.course.details.CourseDetailsUiState
import com.instructure.horizon.horizonui.platform.LoadingState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CourseDetailsUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoadingStateDisplaysSpinner() {
        val state = CourseDetailsUiState(
            loadingState = LoadingState(isLoading = true),
            courseName = "",
            courseProgress = 0.0,
            courseId = 1L
        )

        composeTestRule.setContent {
            val navController = rememberNavController()
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            CourseDetailsScreen(
                state = state,
                navController = navController
            )
        }

        composeTestRule.onNodeWithTag("LoadingSpinner")
            .assertIsDisplayed()
    }

    @Test
    fun testCourseDetailsDisplaysCourseName() {
        val state = CourseDetailsUiState(
            loadingState = LoadingState(isLoading = false),
            courseName = "Test Course Name",
            courseProgress = 75.0,
            courseId = 1L
        )

        composeTestRule.setContent {
            val navController = rememberNavController()
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            CourseDetailsScreen(
                state = state,
                navController = navController
            )
        }

        composeTestRule.onNodeWithText("Test Course Name")
            .assertIsDisplayed()
    }

    @Test
    fun testErrorStateDisplayed() {
        val state = CourseDetailsUiState(
            loadingState = LoadingState(isLoading = false, isError = true, errorMessage = "Failed to load course"),
            courseName = "",
            courseProgress = 0.0,
            courseId = 1L
        )

        composeTestRule.setContent {
            val navController = rememberNavController()
            navController.navigatorProvider.addNavigator(ComposeNavigator())

            CourseDetailsScreen(
                state = state,
                navController = navController
            )
        }

        composeTestRule.onNodeWithText("Failed to load course", substring = true)
            .assertIsDisplayed()
    }
}
