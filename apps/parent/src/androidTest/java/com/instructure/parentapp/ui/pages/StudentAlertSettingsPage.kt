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
package com.instructure.parentapp.ui.pages

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.instructure.canvasapi2.models.AlertType
import com.instructure.espresso.page.BasePage

class StudentAlertSettingsPage(private val composeTestRule: ComposeTestRule) : BasePage() {

    fun assertPercentageThreshold(alertType: AlertType, threshold: String) {
        composeTestRule.onNodeWithTag("${alertType.name}_thresholdItem", useUnmergedTree = true)
            .assertHasClickAction()
        composeTestRule.onNodeWithTag("${alertType.name}_thresholdTitle", useUnmergedTree = true)
            .assertTextEquals(getAlertTitle(alertType))
        composeTestRule.onNodeWithTag("${alertType.name}_thresholdValue", useUnmergedTree = true)
            .assertTextEquals(threshold)
    }

    fun assertSwitchThreshold(alertType: AlertType, isOn: Boolean) {
        composeTestRule.onNodeWithTag("alertSettingsContent")
            .performScrollToNode(hasTestTag("${alertType.name}_thresholdItem"))
        composeTestRule.onNodeWithTag("${alertType.name}_thresholdItem", useUnmergedTree = true)
            .assertHasClickAction()
        composeTestRule.onNodeWithTag("${alertType.name}_thresholdTitle", useUnmergedTree = true)
            .assertTextEquals(getAlertTitle(alertType))
        if (isOn) {
            composeTestRule.onNodeWithTag("${alertType.name}_thresholdSwitch", useUnmergedTree = true)
                .assertIsOn()
        } else {
            composeTestRule.onNodeWithTag("${alertType.name}_thresholdSwitch", useUnmergedTree = true)
                .assertIsOff()
        }
    }

    fun clickOverflowMenu() {
        composeTestRule.onNodeWithTag("overflowMenu").performClick()
    }

    fun clickDeleteStudent() {
        composeTestRule.onNodeWithTag("deleteMenuItem").performClick()
    }

    fun clickThreshold(alertType: AlertType) {
        composeTestRule.onNodeWithTag("${alertType.name}_thresholdItem").performClick()
    }

    fun enterThreshold(threshold: String) {
        composeTestRule.onNodeWithTag("thresholdDialogInput").performTextClearance()
        composeTestRule.onNodeWithTag("thresholdDialogInput").performTextInput(threshold)
    }

    fun assertThresholdDialogError() {
        composeTestRule.onNodeWithTag("thresholdDialogError").assertExists()
        composeTestRule.onNodeWithTag("thresholdDialogSaveButton").assertIsNotEnabled()
    }

    fun assertThresholdDialogNotError() {
        composeTestRule.onNodeWithTag("thresholdDialogError").assertDoesNotExist()
        composeTestRule.onNodeWithTag("thresholdDialogSaveButton").assertIsEnabled()
    }

    fun tapThresholdSaveButton() {
        composeTestRule.onNodeWithTag("thresholdDialogSaveButton").performClick()
    }

    fun tapThresholdNeverButton() {
        composeTestRule.onNodeWithTag("thresholdDialogNeverButton").performClick()
    }

    fun clickDeleteStudentButton() {
        composeTestRule.onNodeWithTag("deleteConfirmButton").performClick()
    }

    fun assertToolbarTitle() {
        composeTestRule.onNodeWithText("Alert Settings").assertIsDisplayed()
    }

    fun assertDeleteStudentDialogDetails() {
        composeTestRule.onNodeWithTag("deleteDialogTitle")
            .assertExists()
            .assertTextEquals("Delete")
        composeTestRule.onNodeWithText("This will unpair and remove all enrollments for this student from you account.")
            .assertExists()
        composeTestRule.onNodeWithTag("deleteConfirmButton")
            .assertTextEquals("Delete")
            .assertExists()
            .assertHasClickAction()
        composeTestRule.onNodeWithTag("deleteCancelButton")
            .assertTextEquals("Cancel")
            .assertExists()
            .assertHasClickAction()
    }

    private fun getAlertTitle(alertType: AlertType): String {
        return when (alertType) {
            AlertType.COURSE_GRADE_HIGH -> "Course grade above"
            AlertType.COURSE_GRADE_LOW -> "Course grade below"
            AlertType.COURSE_ANNOUNCEMENT -> "Course Announcements"
            AlertType.ASSIGNMENT_MISSING -> "Assignment missing"
            AlertType.ASSIGNMENT_GRADE_HIGH -> "Assignment grade above"
            AlertType.ASSIGNMENT_GRADE_LOW -> "Assignment grade below"
            AlertType.INSTITUTION_ANNOUNCEMENT -> "Global Announcements"
        }
    }
}