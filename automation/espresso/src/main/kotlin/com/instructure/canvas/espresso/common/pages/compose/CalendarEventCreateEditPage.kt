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
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.web.assertion.WebViewAssertions
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.pandautils.R
import org.hamcrest.Matchers

class CalendarEventCreateEditPage(private val composeTestRule: ComposeTestRule) {

    fun assertTitle(title: String) {
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
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