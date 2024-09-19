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
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.instructure.canvasapi2.models.AlertType
import com.instructure.espresso.page.BasePage

class AlertSettingsPage(private val composeTestRule: ComposeTestRule) : BasePage() {

    fun assertPercentageThreshold(alertType: AlertType, title: String, threshold: String) {
        composeTestRule.onNodeWithTag("${alertType.name}_thresholdItem", useUnmergedTree = true)
            .assertHasClickAction()
        composeTestRule.onNodeWithTag("${alertType.name}_thresholdTitle", useUnmergedTree = true)
            .assertTextEquals(title)
        composeTestRule.onNodeWithTag("${alertType.name}_thresholdValue", useUnmergedTree = true)
            .assertTextEquals(threshold)
    }

    fun assertSwitchThreshold(alertType: AlertType, title: String, isOn: Boolean) {
        composeTestRule.onNodeWithTag("${alertType.name}_thresholdItem", useUnmergedTree = true)
            .assertHasClickAction()
        composeTestRule.onNodeWithTag("${alertType.name}_thresholdTitle", useUnmergedTree = true)
            .assertTextEquals(title)
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
        composeTestRule.onNodeWithTag("thresholdDialogInput").performTextInput(threshold)
    }
}