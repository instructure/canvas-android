/*
 * Copyright (C) 2020 - present Instructure, Inc.
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

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.requestFocus
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.pandautils.R
import com.instructure.student.ui.utils.TypeInRCETextEditor
import org.hamcrest.Matchers

class CalendarEventPage(private val composeTestRule: ComposeTestRule) : BasePage() {

    fun assertTitle(title: String) {
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    fun verifyDescription(description: String) {
        onWebView(withId(R.id.contentWebView) + withAncestor(withId(R.id.eventFragment)))
            .withElement(findElement(Locator.ID, "content"))
            .check(webMatches(getText(), Matchers.comparesEqualTo(description)))
    }

    fun typeTitle(title: String) {
        composeTestRule.onNodeWithTag("addTitleField").assertExists().performTextReplacement(title)
        composeTestRule.waitForIdle()
    }

    fun typeLocation(location: String) {
        composeTestRule.onNodeWithTag("locationTextField").onChildAt(0).performTextReplacement(location)
        composeTestRule.waitForIdle()
    }

    fun typeAddress(address: String) {
        composeTestRule.onNodeWithTag("addressTextField").onChildAt(0).performTextReplacement(address)
        composeTestRule.waitForIdle()
    }

    fun typeDetails(details: String) {
        composeTestRule.onNodeWithTag("detailsComposeRCE").performTextReplacement(details)
        composeTestRule.waitForIdle()
    }

    fun clickSave() {
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.waitForIdle()
    }
}