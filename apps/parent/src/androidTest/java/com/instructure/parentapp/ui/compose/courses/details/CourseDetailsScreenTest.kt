/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 *
 */

package com.instructure.parentapp.ui.compose.courses.details

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.parentapp.features.courses.details.CourseDetailsScreen
import com.instructure.parentapp.features.courses.details.CourseDetailsUiState
import com.instructure.parentapp.features.courses.details.TabType
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class CourseDetailsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun assertLoadingContent() {
        composeTestRule.setContent {
            CourseDetailsScreen(
                uiState = CourseDetailsUiState(
                    isLoading = true
                ),
                actionHandler = {},
                navigationActionClick = {}
            )
        }

        composeTestRule.onNodeWithTag("loading")
            .assertIsDisplayed()
    }

    @Test
    fun assertErrorContent() {
        composeTestRule.setContent {
            CourseDetailsScreen(
                uiState = CourseDetailsUiState(
                    isLoading = false,
                    isError = true
                ),
                actionHandler = {},
                navigationActionClick = {}
            )
        }

        composeTestRule.onNodeWithText("We're having trouble loading your student's course details. Please try reloading the page or check back later.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun assertCourseDetailsContent() {
        composeTestRule.setContent {
            CourseDetailsScreen(
                uiState = CourseDetailsUiState(
                    isLoading = false,
                    isError = false,
                    courseName = "Course 1",
                    tabs = listOf(TabType.SYLLABUS, TabType.SUMMARY)
                ),
                actionHandler = {},
                navigationActionClick = {}
            )
        }

        composeTestRule.onNodeWithText("Course 1")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("SYLLABUS")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("SUMMARY")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("courseDetailsTabRow")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("courseDetailsPager")
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Send a message about this course")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun assertCourseDetailsContentWithJustOnTab() {
        composeTestRule.setContent {
            CourseDetailsScreen(
                uiState = CourseDetailsUiState(
                    isLoading = false,
                    isError = false,
                    courseName = "Course 1",
                    tabs = listOf(TabType.SYLLABUS)
                ),
                actionHandler = {},
                navigationActionClick = {}
            )
        }

        composeTestRule.onNodeWithText("Course 1")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("courseDetailsTabRow")
            .assertIsNotDisplayed()
        composeTestRule.onNodeWithTag("courseDetailsPager")
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Send a message about this course")
            .assertIsDisplayed()
            .assertHasClickAction()
    }
}
