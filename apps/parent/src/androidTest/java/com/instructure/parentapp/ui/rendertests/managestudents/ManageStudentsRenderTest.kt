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

package com.instructure.parentapp.ui.rendertests.managestudents

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandares.R
import com.instructure.pandautils.utils.ThemedColor
import com.instructure.parentapp.features.managestudents.ColorPickerDialogUiState
import com.instructure.parentapp.features.managestudents.ManageStudentsScreen
import com.instructure.parentapp.features.managestudents.ManageStudentsUiState
import com.instructure.parentapp.features.managestudents.StudentItemUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ManageStudentsRenderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun assertEmptyContent() {
        composeTestRule.setContent {
            ManageStudentsScreen(
                uiState = ManageStudentsUiState(
                    isLoading = false,
                    studentListItems = emptyList()
                ),
                actionHandler = {},
                navigationActionClick = {}
            )
        }

        composeTestRule.onNodeWithText("You are not observing any students.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("EmptyContent")
            .performScrollToNode(hasText("Refresh"))
        composeTestRule.onNodeWithText("Refresh")
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNodeWithTag(R.drawable.panda_manage_students.toString())
            .assertIsDisplayed()
    }

    @Test
    fun assertErrorContent() {
        composeTestRule.setContent {
            ManageStudentsScreen(
                uiState = ManageStudentsUiState(
                    isLoadError = true,
                    studentListItems = emptyList()
                ),
                actionHandler = {},
                navigationActionClick = {}
            )
        }

        composeTestRule.onNodeWithText("There was an error loading your students.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun assertStudentListContent() {
        composeTestRule.setContent {
            ManageStudentsScreen(
                uiState = ManageStudentsUiState(
                    studentListItems = listOf(
                        StudentItemUiState(
                            studentId = 1,
                            studentName = "John Doe",
                            studentPronouns = "He/Him",
                            studentColor = ThemedColor(R.color.studentGreen)
                        ),
                        StudentItemUiState(
                            studentId = 2,
                            studentName = "Jane Doe",
                            studentColor = ThemedColor(R.color.studentPink)
                        )
                    )
                ),
                actionHandler = {},
                navigationActionClick = {}
            )
        }

        fun studentItemMatcher(name: String) = hasTestTag("studentListItem") and hasAnyChild(hasText(name))
        composeTestRule.onNode(studentItemMatcher("John Doe (He/Him)"), true)
            .assertIsDisplayed()
        composeTestRule.onNode(studentItemMatcher("Jane Doe"), true)
            .assertIsDisplayed()
    }

    @Test
    fun assertColorPickerDialogError() {
        composeTestRule.setContent {
            ManageStudentsScreen(
                uiState = ManageStudentsUiState(
                    studentListItems = listOf(
                        StudentItemUiState(
                            studentId = 1,
                            studentName = "John Doe",
                            studentColor = ThemedColor(R.color.studentGreen)
                        )
                    ),
                    colorPickerDialogUiState = ColorPickerDialogUiState(
                        showColorPickerDialog = true,
                        studentId = 1,
                        initialUserColor = null,
                        userColors = emptyList(),
                        isSavingColorError = true
                    )
                ),
                actionHandler = {},
                navigationActionClick = {}
            )
        }

        composeTestRule.onNodeWithText("Select Student Color")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("An error occurred while saving your selection. Please try again.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel")
            .assertIsDisplayed()
            .assertHasClickAction()
        composeTestRule.onNodeWithText("OK")
            .assertIsDisplayed()
            .assertHasClickAction()
    }
}
