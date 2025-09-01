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
package com.instructure.canvas.espresso.common.pages.compose

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement

class SearchableToolbar(private val composeTestRule: ComposeTestRule) {
    fun clickOnSearchButton() {
        composeTestRule.onAllNodesWithTag("searchButton").onFirst()
            .performClick()
    }

    fun typeToSearchBar(textToType: String) {
        composeTestRule.onAllNodesWithTag("searchField").onFirst()
            .performClick()
            .performTextReplacement(textToType)
    }

    fun clickOnClearSearchButton() {
        composeTestRule.onAllNodesWithTag("clearButton").onFirst()
            .performClick()
    }

    fun pressSearchBarButton() {
        composeTestRule.onAllNodesWithTag("closeButton").onFirst()
            .performClick()
    }
}