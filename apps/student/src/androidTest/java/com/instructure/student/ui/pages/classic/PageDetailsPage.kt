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
 *
 */
package com.instructure.student.ui.pages.classic

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.model.Atoms
import androidx.test.espresso.web.sugar.Web.onWebView
import com.instructure.espresso.ModuleItemInteractions
import com.instructure.espresso.page.BasePage
import com.instructure.student.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString

/**
 * Page object for the (now Jetpack Compose) Page Details screen. The toolbar title is asserted via
 * [composeTestRule]; the page body is still a hosted Android WebView, matched with Espresso, same
 * as before.
 */
class PageDetailsPage(
    val moduleItemInteractions: ModuleItemInteractions,
    val composeTestRule: ComposeTestRule
) : BasePage() {

    override fun assertPageObjects(duration: Long) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("pageDetailsPage").assertIsDisplayed()
    }

    // The previous screen is also Compose and can stay in the semantics tree behind this one, so
    // scope the toolbar text match to this screen's root to keep it unambiguous.
    private fun inToolbar(text: String) = hasText(text) and
        hasAnyAncestor(hasTestTag("toolbar")) and
        hasAnyAncestor(hasTestTag("pageDetailsPage"))

    fun webAssertPageUrl(pageUrl: String) {
        onWebView(allOf(withId(R.id.contentWebView), isDisplayed()))
            .check(webMatches(Atoms.getCurrentUrl(), containsString(pageUrl)))
    }

    fun assertToolbarTitle(pageName: String) {
        composeTestRule.waitForIdle()
        composeTestRule.onNode(inToolbar(pageName)).assertIsDisplayed()
    }
}
