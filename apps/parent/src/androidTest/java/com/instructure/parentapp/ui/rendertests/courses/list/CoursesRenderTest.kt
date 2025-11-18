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

package com.instructure.parentapp.ui.rendertests.courses.list

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.espresso.assertTextColor
import com.instructure.pandares.R
import com.instructure.parentapp.features.courses.list.CourseListItemUiState
import com.instructure.parentapp.features.courses.list.CoursesScreen
import com.instructure.parentapp.features.courses.list.CoursesUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class CoursesRenderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun assertEmptyContent() {
        composeTestRule.setContent {
            CoursesScreen(
                uiState = CoursesUiState(
                    isLoading = false,
                    courseListItems = emptyList()
                ),
                actionHandler = {}
            )
        }

        composeTestRule.onNodeWithText("No Courses")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Your student’s courses might not be published yet.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(R.drawable.ic_panda_book.toString())
            .assertIsDisplayed()
    }

    @Test
    fun assertErrorContent() {
        composeTestRule.setContent {
            CoursesScreen(
                uiState = CoursesUiState(
                    isLoading = false,
                    isError = true
                ),
                actionHandler = {}
            )
        }

        composeTestRule.onNodeWithText("There was an error loading your student’s courses.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun assertCourseContent() {
        composeTestRule.setContent {
            CoursesScreen(
                uiState = CoursesUiState(
                    isLoading = false,
                    studentColor = android.graphics.Color.RED,
                    courseListItems = listOf(
                        CourseListItemUiState(
                            courseId = 1,
                            courseName = "Course 1",
                            courseCode = "C1",
                            grade = "A+"
                        )
                    )
                ),
                actionHandler = {}
            )
        }

        composeTestRule.onNodeWithText("Course 1")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("C1")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("A+", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextColor(Color(android.graphics.Color.RED))
        composeTestRule.onNodeWithTag("courseListItem")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun assertLoadingContent() {
        composeTestRule.setContent {
            CoursesScreen(
                uiState = CoursesUiState(
                    isLoading = true
                ),
                actionHandler = {}
            )
        }

        composeTestRule.onNodeWithTag("pullRefreshIndicator")
            .assertIsDisplayed()
    }
}
