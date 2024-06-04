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
package com.instructure.canvas.espresso.common.pages.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.pandautils.R
import org.hamcrest.Matchers

class CalendarEventDetailsPage(private val composeTestRule: ComposeTestRule) : BasePage() {

    fun assertEventDetailsPageTitle() {
        composeTestRule.onNodeWithText("Event").assertIsDisplayed()
    }

    fun verifyDescription(description: String) {
        onWebView(withId(R.id.contentWebView) + withAncestor(withId(R.id.eventFragment)))
            .withElement(findElement(Locator.ID, "content"))
            .check(
                webMatches(
                    getText(),
                    Matchers.comparesEqualTo(description)
                )
            )
    }

    fun assertEventCalendar(calendar: String) {
        composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasText(calendar))).assertIsDisplayed()
    }

    fun assertEventTitle(title: String) {
        composeTestRule.onNode(hasTestTag("eventTitle") and hasText(title)).assertIsDisplayed()
    }

    fun assertEventDateContains(date: String) {
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

    fun assertDescription(description: String) {
        onWebView(withId(R.id.contentWebView) + withAncestor(withId(R.id.eventFragment)))
            .withElement(findElement(Locator.ID, "content"))
            .check(webMatches(getText(), Matchers.comparesEqualTo(description)))
    }

    fun clickOverflowMenu() {
        composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasContentDescription("More options"))).performClick()
    }

    fun clickEditMenu() {
        composeTestRule.onNodeWithText("Edit").performClick()
        composeTestRule.waitForIdle()
    }

    fun clickDeleteMenu() {
        composeTestRule.onNodeWithText("Delete").performClick()
        composeTestRule.waitForIdle()
    }

    fun assertDeleteDialog() {
        composeTestRule.onNodeWithText("Delete Event?").assertIsDisplayed()
    }

    fun confirmDelete() {
        composeTestRule.onNodeWithText("Delete").performClick()
        composeTestRule.waitForIdle()
    }
}
