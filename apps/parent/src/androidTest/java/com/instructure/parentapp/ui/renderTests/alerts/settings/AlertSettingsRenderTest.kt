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
 */
package com.instructure.parentapp.ui.renderTests.alerts.settings

import android.graphics.Color
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.AlertThreshold
import com.instructure.canvasapi2.models.AlertType
import com.instructure.canvasapi2.models.ThresholdWorkflowState
import com.instructure.canvasapi2.models.User
import com.instructure.parentapp.features.alerts.settings.AlertSettingsScreen
import com.instructure.parentapp.features.alerts.settings.AlertSettingsUiState
import com.instructure.parentapp.ui.pages.compose.StudentAlertSettingsPage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertSettingsRenderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val page = StudentAlertSettingsPage(composeTestRule)

    @Test
    fun assertLoading() {
        composeTestRule.setContent {
            AlertSettingsScreen(
                uiState = AlertSettingsUiState(
                    isLoading = true,
                    isError = false,
                    thresholds = emptyMap(),
                    actionHandler = {},
                    avatarUrl = "",
                    studentName = "",
                    userColor = Color.BLUE,
                    student = User(),
                    studentPronouns = null
                ),
                navigationActionClick = {}
            )
        }

        composeTestRule.onNodeWithTag("loading").assertExists()
    }

    @Test
    fun assertError() {
        composeTestRule.setContent {
            AlertSettingsScreen(
                uiState = AlertSettingsUiState(
                    isLoading = false,
                    isError = true,
                    thresholds = emptyMap(),
                    actionHandler = {},
                    avatarUrl = "",
                    studentName = "",
                    userColor = Color.BLUE,
                    student = User(),
                    studentPronouns = null
                ),
                navigationActionClick = {}
            )
        }

        composeTestRule.onNodeWithText("An error occurred while fetching the alert settings.")
            .assertExists()
        composeTestRule.onNodeWithText("Retry").assertExists().assertHasClickAction()
    }

    @Test
    fun assertUserInfo() {
        composeTestRule.setContent {
            AlertSettingsScreen(
                uiState = AlertSettingsUiState(
                    isLoading = false,
                    isError = false,
                    thresholds = emptyMap(),
                    actionHandler = {},
                    avatarUrl = "avatarUrl",
                    studentName = "studentName",
                    userColor = Color.BLUE,
                    student = User(),
                    studentPronouns = "studentPronouns"
                ),
                navigationActionClick = {}
            )
        }

        composeTestRule.onNodeWithText("studentName (studentPronouns)").assertExists()
    }

    @Test
    fun assertEmptyThresholds() {
        composeTestRule.setContent {
            AlertSettingsScreen(
                uiState = AlertSettingsUiState(
                    isLoading = false,
                    isError = false,
                    thresholds = emptyMap(),
                    actionHandler = {},
                    avatarUrl = "",
                    studentName = "",
                    userColor = Color.BLUE,
                    student = User(),
                    studentPronouns = null
                ),
                navigationActionClick = {}
            )
        }

        page.assertPercentageThreshold(AlertType.COURSE_GRADE_LOW, "Never")
        page.assertPercentageThreshold(AlertType.COURSE_GRADE_HIGH, "Never")
        page.assertSwitchThreshold(AlertType.ASSIGNMENT_MISSING, false)
        page.assertPercentageThreshold(AlertType.ASSIGNMENT_GRADE_LOW, "Never")
        page.assertPercentageThreshold(AlertType.ASSIGNMENT_GRADE_HIGH, "Never")
        page.assertSwitchThreshold(AlertType.COURSE_ANNOUNCEMENT, false)
        page.assertSwitchThreshold(AlertType.INSTITUTION_ANNOUNCEMENT, false)
    }

    @Test
    fun assertThresholds() {
        composeTestRule.setContent {
            AlertSettingsScreen(
                uiState = AlertSettingsUiState(
                    isLoading = false,
                    isError = false,
                    thresholds = mapOf(
                        AlertType.COURSE_GRADE_LOW to AlertThreshold(
                            1,
                            AlertType.COURSE_GRADE_LOW,
                            "40",
                            1,
                            2,
                            ThresholdWorkflowState.ACTIVE
                        ),
                        AlertType.COURSE_GRADE_HIGH to AlertThreshold(
                            2,
                            AlertType.COURSE_GRADE_HIGH,
                            "80",
                            1,
                            2,
                            ThresholdWorkflowState.ACTIVE
                        ),
                        AlertType.ASSIGNMENT_MISSING to AlertThreshold(
                            3,
                            AlertType.ASSIGNMENT_MISSING,
                            null,
                            1,
                            2,
                            ThresholdWorkflowState.ACTIVE
                        ),
                        AlertType.ASSIGNMENT_GRADE_LOW to AlertThreshold(
                            4,
                            AlertType.ASSIGNMENT_GRADE_LOW,
                            "40",
                            1,
                            2,
                            ThresholdWorkflowState.ACTIVE
                        ),
                        AlertType.ASSIGNMENT_GRADE_HIGH to AlertThreshold(
                            5,
                            AlertType.ASSIGNMENT_GRADE_HIGH,
                            "80",
                            1,
                            2,
                            ThresholdWorkflowState.ACTIVE
                        ),
                        AlertType.COURSE_ANNOUNCEMENT to AlertThreshold(
                            6,
                            AlertType.COURSE_ANNOUNCEMENT,
                            null,
                            1,
                            2,
                            ThresholdWorkflowState.ACTIVE
                        ),
                        AlertType.INSTITUTION_ANNOUNCEMENT to AlertThreshold(
                            7,
                            AlertType.INSTITUTION_ANNOUNCEMENT,
                            null,
                            1,
                            2,
                            ThresholdWorkflowState.ACTIVE
                        )
                    ),
                    actionHandler = {},
                    avatarUrl = "",
                    studentName = "",
                    userColor = Color.BLUE,
                    student = User(),
                    studentPronouns = null
                ),
                navigationActionClick = {}
            )
        }

        page.assertPercentageThreshold(AlertType.COURSE_GRADE_LOW, "40%")
        page.assertPercentageThreshold(AlertType.COURSE_GRADE_HIGH, "80%")
        page.assertSwitchThreshold(AlertType.ASSIGNMENT_MISSING, true)
        page.assertPercentageThreshold(AlertType.ASSIGNMENT_GRADE_LOW, "40%")
        page.assertPercentageThreshold(AlertType.ASSIGNMENT_GRADE_HIGH, "80%")
        page.assertSwitchThreshold(AlertType.COURSE_ANNOUNCEMENT, true)
        page.assertSwitchThreshold(AlertType.INSTITUTION_ANNOUNCEMENT, true)
    }

    @Test
    fun assertOverflowMenu() {
        composeTestRule.setContent {
            AlertSettingsScreen(
                uiState = AlertSettingsUiState(
                    isLoading = false,
                    isError = false,
                    thresholds = emptyMap(),
                    actionHandler = {},
                    avatarUrl = "",
                    studentName = "",
                    userColor = Color.BLUE,
                    student = User(),
                    studentPronouns = null
                ),
                navigationActionClick = {}
            )
        }

        composeTestRule.onNodeWithTag("overflowMenu").assertExists().assertHasClickAction()
        page.clickOverflowMenu()
        composeTestRule.onNodeWithTag("deleteMenuItem").assertExists().assertHasClickAction()
    }

    @Test
    fun assertDeleteConfirmationDialog() {
        composeTestRule.setContent {
            AlertSettingsScreen(
                uiState = AlertSettingsUiState(
                    isLoading = false,
                    isError = false,
                    thresholds = emptyMap(),
                    actionHandler = {},
                    avatarUrl = "",
                    studentName = "",
                    userColor = Color.BLUE,
                    student = User(),
                    studentPronouns = null
                ),
                navigationActionClick = {}
            )
        }

        page.clickOverflowMenu()
        page.clickDeleteStudent()
        page.assertDeleteStudentDialogDetails()
    }

    @Test
    fun assertThresholdDialog() {
        composeTestRule.setContent {
            AlertSettingsScreen(
                uiState = AlertSettingsUiState(
                    isLoading = false,
                    isError = false,
                    thresholds = mapOf(
                        AlertType.COURSE_GRADE_LOW to AlertThreshold(
                            1,
                            AlertType.COURSE_GRADE_LOW,
                            "40",
                            1,
                            2,
                            ThresholdWorkflowState.ACTIVE
                        )

                    ),
                    actionHandler = {},
                    avatarUrl = "",
                    studentName = "",
                    userColor = Color.BLUE,
                    student = User(),
                    studentPronouns = null
                ),
                navigationActionClick = {}
            )
        }

        page.clickThreshold(AlertType.COURSE_GRADE_LOW)
        composeTestRule.onNodeWithTag("thresholdDialogTitle")
            .assertExists()
            .assertTextEquals("Course grade below")

        composeTestRule.onNodeWithTag("thresholdDialogInput")
            .assertExists()
            .assertTextContains("40")

        composeTestRule.onNodeWithTag("thresholdDialogNeverButton")
            .assertExists()
            .assertTextEquals("Never")
            .assertHasClickAction()

        composeTestRule.onNodeWithTag("thresholdDialogSaveButton")
            .assertExists()
            .assertTextEquals("Save")
            .assertHasClickAction()

        composeTestRule.onNodeWithTag("thresholdDialogCancelButton")
            .assertExists()
            .assertTextEquals("Cancel")
            .assertHasClickAction()
    }

    @Test
    fun assertThresholdDialogMinError() {
        composeTestRule.setContent {
            AlertSettingsScreen(
                uiState = AlertSettingsUiState(
                    isLoading = false,
                    isError = false,
                    thresholds = mapOf(
                        AlertType.COURSE_GRADE_LOW to AlertThreshold(
                            1,
                            AlertType.COURSE_GRADE_LOW,
                            "40",
                            1,
                            2,
                            ThresholdWorkflowState.ACTIVE
                        )

                    ),
                    actionHandler = {},
                    avatarUrl = "",
                    studentName = "",
                    userColor = Color.BLUE,
                    student = User(),
                    studentPronouns = null
                ),
                navigationActionClick = {}
            )
        }

        page.clickThreshold(AlertType.COURSE_GRADE_HIGH)
        page.enterThreshold("39")

        composeTestRule.onNodeWithText("Must be above 40")
            .assertExists()
    }

    @Test
    fun assertThresholdDialogMaxError() {
        composeTestRule.setContent {
            AlertSettingsScreen(
                uiState = AlertSettingsUiState(
                    isLoading = false,
                    isError = false,
                    thresholds = mapOf(
                        AlertType.ASSIGNMENT_GRADE_HIGH to AlertThreshold(
                            1,
                            AlertType.ASSIGNMENT_GRADE_HIGH,
                            "40",
                            1,
                            2,
                            ThresholdWorkflowState.ACTIVE
                        )

                    ),
                    actionHandler = {},
                    avatarUrl = "",
                    studentName = "",
                    userColor = Color.BLUE,
                    student = User(),
                    studentPronouns = null
                ),
                navigationActionClick = {}
            )
        }

        page.clickThreshold(AlertType.ASSIGNMENT_GRADE_LOW)
        page.enterThreshold("41")

        composeTestRule.onNodeWithText("Must be below 40")
            .assertExists()
    }
}