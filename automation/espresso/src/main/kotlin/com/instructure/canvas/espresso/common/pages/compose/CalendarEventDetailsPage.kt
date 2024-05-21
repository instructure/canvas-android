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
import androidx.test.espresso.web.assertion.WebViewAssertions
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms
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
        Web.onWebView(withId(R.id.contentWebView) + withAncestor(withId(R.id.eventFragment)))
            .withElement(DriverAtoms.findElement(Locator.ID, "content"))
            .check(
                WebViewAssertions.webMatches(
                    DriverAtoms.getText(),
                    Matchers.comparesEqualTo(description)
                )
            )
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

    fun assertDescription(description: String) {
        onWebView(withId(R.id.contentWebView) + withAncestor(withId(R.id.eventFragment)))
            .withElement(findElement(Locator.ID, "content"))
            .check(webMatches(getText(), Matchers.comparesEqualTo(description)))
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