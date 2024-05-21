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
package com.instructure.canvas.espresso.common.pages.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.instructure.espresso.page.BasePage

class CalendarToDoCreateUpdatePage(private val composeTestRule: ComposeTestRule) : BasePage() {

    fun assertPageTitle(pageTitle: String) {
        composeTestRule.onNodeWithText(pageTitle).assertIsDisplayed()
    }

    fun typeTodoTitle(todoTitle: String) {
        composeTestRule.onNodeWithTag("addTitleField").assertExists().performTextReplacement(todoTitle)
        composeTestRule.waitForIdle()
    }

    fun assertTodoTitle(todoTitle: String) {
        composeTestRule.onNodeWithTag("addTitleField").assertTextEquals(todoTitle)
    }

    fun typeDetails(details: String) {
        composeTestRule.onNodeWithTag("TodoDetailsTextField").performTextReplacement(details)
        composeTestRule.waitForIdle()
    }

    fun assertDetails(details: String) {
        composeTestRule.onNodeWithTag("TodoDetailsTextField").assertTextEquals(details)
    }

    fun clickSave() {
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.waitForIdle()
    }
}