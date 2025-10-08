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
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick

class HorizonInboxPage(private val composeTestRule: ComposeTestRule) {
    fun assertInboxItemDisplayed(subject: String) {
        composeTestRule.onNodeWithText(subject)
            .assertIsDisplayed()
    }

    fun clickInboxItem(subject: String) {
        composeTestRule.onNodeWithText(subject)
            .performClick()
    }

    fun clickComposeButton() {
        composeTestRule.onNodeWithContentDescription("Compose")
            .performClick()
    }

    fun assertEmptyState() {
        composeTestRule.onNode(hasText("No messages", substring = true))
            .assertIsDisplayed()
    }

    fun assertConversationCount(count: Int) {
        composeTestRule.onNodeWithText("$count conversations", substring = true)
            .assertIsDisplayed()
    }
}
