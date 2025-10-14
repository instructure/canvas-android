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

class HorizonModuleItemSequencePage(private val composeTestRule: ComposeTestRule) {
    fun assertModuleItemDisplayed(title: String) {
        composeTestRule.onNodeWithText(title)
            .assertIsDisplayed()
    }

    fun clickNextButton() {
        composeTestRule.onNodeWithContentDescription("Next")
            .performClick()
    }

    fun clickPreviousButton() {
        composeTestRule.onNodeWithContentDescription("Previous")
            .performClick()
    }

    fun clickProgressButton() {
        composeTestRule.onNodeWithContentDescription("Progress")
            .performClick()
    }

    fun assertProgressDisplayed() {
        composeTestRule.onNodeWithText("Progress", substring = true)
            .assertIsDisplayed()
    }

    fun clickMarkAsDone() {
        composeTestRule.onNodeWithText("Mark as done")
            .performClick()
    }

    fun assertMarkedAsDone() {
        composeTestRule.onNodeWithText("Marked as done", substring = true)
            .assertIsDisplayed()
    }
}
