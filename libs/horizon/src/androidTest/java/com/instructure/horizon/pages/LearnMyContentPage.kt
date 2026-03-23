/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick

class LearnMyContentPage(private val composeTestRule: ComposeTestRule) {

    fun assertTabDisplayed(tabLabel: String) {
        composeTestRule.onNodeWithText(tabLabel)
            .assertIsDisplayed()
    }

    fun assertTabIsSelected(tabLabel: String) {
        composeTestRule.onNodeWithText(tabLabel)
            .assertIsSelected()
    }

    fun clickTab(tabLabel: String) {
        composeTestRule.onNodeWithText(tabLabel)
            .performClick()
    }

    fun assertItemCardDisplayed(name: String) {
        composeTestRule.onNodeWithText(name)
            .assertIsDisplayed()
    }

    fun assertEmptyMessageDisplayed() {
        composeTestRule.onNodeWithText("No results found", substring = true)
            .assertIsDisplayed()
    }

    fun assertItemCountDisplayed(count: Int) {
        composeTestRule.onNodeWithText("$count item", substring = true)
            .assertIsDisplayed()
    }

    fun assertShowMoreButtonDisplayed() {
        composeTestRule.onNodeWithText("Show more", substring = true)
            .assertIsDisplayed()
    }

    fun assertShowMoreButtonNotDisplayed() {
        composeTestRule.onNodeWithText("Show more", substring = true)
            .assertDoesNotExist()
    }

    fun clickShowMore() {
        composeTestRule.onNodeWithText("Show more", substring = true)
            .performClick()
    }
}
