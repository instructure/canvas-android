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
package com.instructure.student.ui.pages.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.instructure.espresso.page.BasePage

class CalendarEventDetailsPage(private val composeTestRule: ComposeTestRule) : BasePage() {

    fun assertEventDetailsPageTitle() {
        composeTestRule.onNodeWithText("Event").assertIsDisplayed()
    }

    fun assertEventTitle(title: String) {
        composeTestRule.onNode(hasTestTag("eventTitle") and hasText(title)).assertIsDisplayed()
    }

    fun assertEventDate(date: String) {
        composeTestRule.onNode(hasTestTag("eventDate")).assertTextContains(date, substring = true)
    }

    fun assertRecurrence(recurrence: String) {
        composeTestRule.onNode(hasTestTag("recurrence") and hasText(recurrence)).assertIsDisplayed()
    }

    fun assertLocationNotDisplayed() {
        composeTestRule.onNode(hasTestTag("locationLabel")).assertIsNotDisplayed()
        composeTestRule.onNode(hasTestTag("location")).assertIsNotDisplayed()
    }

    fun assertAddressNotDisplayed() {
        composeTestRule.onNode(hasTestTag("addressLabel")).assertIsNotDisplayed()
        composeTestRule.onNode(hasTestTag("address")).assertIsNotDisplayed()
    }

    fun assertLocationDisplayed(location: String) {
        composeTestRule.onNode(hasTestTag("locationLabel")).assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("location")).assertIsDisplayed().assertTextEquals(location)
    }

    fun assertAddressDisplayed(address: String) {
        composeTestRule.onNode(hasTestTag("addressLabel")).assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("address")).assertIsDisplayed().assertTextEquals(address)
    }

    fun clickOverflowMenu() {
        composeTestRule.waitForIdle()
        val canvasThemedAppBar = composeTestRule.onNodeWithTag("canvasThemedAppBar").assertIsDisplayed()
        canvasThemedAppBar.onChildren().filterToOne(hasContentDescription("More options")).performClick()
        composeTestRule.waitForIdle()
    }

    fun clickEditMenu() {
        composeTestRule.onNodeWithText("Edit").performClick()
        composeTestRule.waitForIdle()
    }

    fun clickDeleteMenu() {
        composeTestRule.onNodeWithText("Delete").performClick()
        composeTestRule.waitForIdle()
    }

    fun confirmDelete() {
        composeTestRule.onNodeWithText("Delete").performClick()
        composeTestRule.waitForIdle()
    }
}