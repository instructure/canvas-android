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
package com.instructure.teacher.ui.pages

import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.web.assertion.WebViewAssertions
import androidx.test.espresso.web.matcher.DomMatchers
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.espresso.ModuleItemInteractions
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.espresso.waitForWebElement
import com.instructure.teacher.R

class DiscussionsDetailsPage(val moduleItemInteractions: ModuleItemInteractions) : BasePage() {

    fun assertReplyButtonDisplayed() {
        Web.onWebView()
            .check(WebViewAssertions.webContent(DomMatchers.hasElementWithXpath("//button[@data-testid='discussion-topic-reply']")))
    }

    fun clickOnDiscussionMoreMenu() {
        Web.onWebView()
            .withElement(DriverAtoms.findElement(Locator.XPATH, "//button[@data-testid='discussion-post-menu-trigger']"))
            .perform(webClick())
    }

    fun assertMoreMenuButtonDisplayed(menuText: String) {
        Web.onWebView()
            .check(WebViewAssertions.webContent(DomMatchers.hasElementWithXpath("//ul[@role='menu']//span[text()='$menuText']")))
    }

    fun assertToolbarDiscussionTitle(discussionTitle: String) {
        onView(withText(discussionTitle) + withAncestor(withId(R.id.toolbar) + ViewMatchers.hasSibling(
            withId(R.id.discussionWebView)
        )
        )).assertDisplayed()
    }

    fun assertDiscussionEntryMessageDisplayed(entryMessage: String) {
        Web.onWebView()
            .check(WebViewAssertions.webContent(DomMatchers.hasElementWithXpath("//span[text()='$entryMessage']")))
    }

    fun assertReplyCounter(replyCount: Int, unreadCount: Int) {
        Web.onWebView()
            .check(WebViewAssertions.webContent(DomMatchers.hasElementWithXpath("//div[@data-testid='replies-counter' and .='$replyCount Reply ($unreadCount)']/ancestor::button[@data-testid='expand-button']")))
    }

    fun clickOnExpandRepliesButton() {
        Web.onWebView()
            .withElement(DriverAtoms.findElement(Locator.XPATH, "//div[@data-testid='replies-counter']/ancestor::button[@data-testid='expand-button']"))
            .perform(webClick())
    }

    fun assertReplyDisplayed(replyMessage: String) {
        Web.onWebView()
            .check(WebViewAssertions.webContent(DomMatchers.hasElementWithXpath("//span[text()='$replyMessage']")))
    }



    fun waitForReplyButtonDisplayed() {
        waitForWebElement(
            webViewMatcher = withId(R.id.discussionWebView),
            locator = Locator.XPATH,
            value = "//button[@data-testid='discussion-topic-reply']",
            timeoutMillis = 10000,
            intervalMillis = 500
        )
    }

    fun waitForReplyDisplayed(replyMessage: String) {
        waitForWebElement(
            webViewMatcher = withId(R.id.discussionWebView),
            locator = Locator.XPATH,
            value = "//span[text()='$replyMessage']",
            timeoutMillis = 10000,
            intervalMillis = 500
        )
    }

}