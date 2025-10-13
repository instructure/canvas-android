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
package com.instructure.horizon.pages

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick

class HorizonHomePage(private val composeTestRule: ComposeTestRule) {
    fun assertBottomNavigationVisible() {
        composeTestRule.onNodeWithText("Home")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Learn")
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("AI assist")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Skillspace")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Account")
            .assertIsDisplayed()
    }

    fun clickHomeTab() {
        composeTestRule.onNodeWithText("Home")
            .performClick()
    }

    fun clickLearnTab() {
        composeTestRule.onNodeWithText("Learn")
            .performClick()
    }

    fun clickAiAssistantTab() {
        composeTestRule.onNodeWithContentDescription("AI assist")
            .performClick()
    }

    fun clickSkillspaceTab() {
        composeTestRule.onNodeWithText("Skillspace")
            .performClick()
    }

    fun clickAccountTab() {
        composeTestRule.onNodeWithText("Account")
            .performClick()
    }
}
