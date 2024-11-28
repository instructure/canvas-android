/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.parentapp.ui.pages

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick

class SummaryPage(private val composeTestRule: ComposeTestRule) {
    fun assertItemDisplayed(itemName: String, date: String) {
        composeTestRule.onNode(hasText(itemName).and(hasAnySibling(hasText(date)))).assertIsDisplayed()
    }

    fun selectItem(itemName: String, date: String) {
        composeTestRule.onNode(hasText(itemName).and(hasAnySibling(hasText(date)))).performClick()
    }
}