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
package com.instructure.student.ui.pages

import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.web.assertion.WebViewAssertions.webContent
import androidx.test.espresso.web.matcher.DomMatchers.hasElementWithXpath
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.espresso.ModuleItemInteractions
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.onView
import com.instructure.espresso.pages.plus
import com.instructure.espresso.pages.withAncestor
import com.instructure.espresso.pages.withId
import com.instructure.espresso.pages.withParent
import com.instructure.espresso.pages.withText
import com.instructure.espresso.waitForWebElement
import com.instructure.student.R
import org.junit.Assert


class DiscussionDetailsPage(val moduleItemInteractions: ModuleItemInteractions) : BasePage(R.id.discussionDetailsPage) {

    fun assertReplyButtonDisplayed() {
        onWebView().check(webContent(hasElementWithXpath("//button[@data-testid='discussion-topic-reply']")))
    }

    private fun replyButtonNotDisplayed(): Boolean {
        return try {
            onWebView().check(webContent(hasElementWithXpath("//button[@data-testid='discussion-topic-reply']")))
            false
        }
        catch (e: AssertionError) {
            true
        }
    }

    fun assertReplyButtonNotDisplayed() {
        Assert.assertTrue("Reply button has displayed but it should not.",replyButtonNotDisplayed())
    }

    fun assertEntryDisplayed(entryMessage: String) {
        onWebView().check(webContent(hasElementWithXpath("//span[text()='$entryMessage']")))
    }

    fun assertModulesToolbarDiscussionTitle(discussionTitle: String) {
        onView(withText(discussionTitle) + withAncestor(R.id.moduleProgressionPage)).assertDisplayed()
    }

    fun assertHomeroomToolbarDiscussionTitle(discussionTitle: String) {
        onView(withText(discussionTitle) + withAncestor(R.id.toolbar) + withAncestor(R.id.fullScreenCoordinatorLayout)).assertDisplayed()
    }

    fun assertToolbarDiscussionTitle(discussionTitle: String) {
        onView(withText(discussionTitle) + withAncestor(withId(R.id.toolbar) + hasSibling(withId(R.id.discussionWebView)))).assertDisplayed()
    }

    //OfflineMethod
    fun assertDetailsNotAvailableOffline() {
        onView(withId(R.id.notAvailableIcon) + withAncestor(R.id.moduleProgressionPage)).assertDisplayed()
        onView(withId(R.id.title) + withText(R.string.notAvailableOfflineScreenTitle) + withParent(R.id.textViews) + withAncestor(R.id.moduleProgressionPage)).assertDisplayed()
        onView(withId(R.id.description) + withText(R.string.notAvailableOfflineDescriptionForTabs) + withParent(R.id.textViews) + withAncestor(R.id.moduleProgressionPage)).assertDisplayed()
    }

    fun waitForReplyButton() {
        waitForWebElement(
            webViewMatcher = withId(R.id.discussionWebView),
            locator = Locator.XPATH,
            value = "//button[@data-testid='discussion-topic-reply']",
            timeoutMillis = 10000,
            intervalMillis = 500
        )
    }

    fun waitForEntryDisplayed(entryMessage: String) {
        waitForWebElement(
            webViewMatcher = withId(R.id.discussionWebView),
            locator = Locator.XPATH,
            value = "//span[text()='$entryMessage']",
            timeoutMillis = 10000,
            intervalMillis = 500
        )
    }
}
