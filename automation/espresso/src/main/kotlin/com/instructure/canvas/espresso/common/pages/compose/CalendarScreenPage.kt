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
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.getStringFromResource
import com.instructure.pandautils.R
import org.junit.Assert.assertTrue

class CalendarScreenPage(private val composeTestRule: ComposeTestRule) : BasePage() {

    fun assertCalendarPageTitle() {
        composeTestRule.onNodeWithText("Calendar").assertIsDisplayed()
    }

    fun clickOnAddButton() {
        composeTestRule.onNodeWithContentDescription(getStringFromResource(R.string.calendarAddButtonContentDescription))
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun clickAddEvent() {
        composeTestRule.onNodeWithText("Add Event").performClick()
        composeTestRule.waitForIdle()
    }

    fun clickOnItem(itemTitle: String) {
        composeTestRule.onNodeWithText(itemTitle).performClick()
        composeTestRule.waitForIdle()
    }

    fun assertEventDetails(eventTitle: String, contextName: String, eventDate: String? = null, eventStatus: String? = null) {
        composeTestRule.onNodeWithText(eventTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(contextName).assertIsDisplayed()
        if(eventDate != null) composeTestRule.onNodeWithText(eventDate).assertIsDisplayed()
        if(eventStatus != null) composeTestRule.onNodeWithText(eventStatus).assertIsDisplayed()
    }

    fun assertEmptyEventsView() {
        assertTrue(composeTestRule.onAllNodesWithTag("calendarEventsEmpty").fetchSemanticsNodes().isNotEmpty())
    }

}