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
import androidx.test.espresso.web.model.Atoms
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
import org.junit.Assert

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

    fun editDiscussion(newTitle: String) {
        Web.onWebView()
            .withElement(DriverAtoms.findElement(Locator.XPATH, "//ul[@role='menu']//span[text()='Edit']"))
            .perform(webClick())

        waitForWebElement(
            webViewMatcher = withId(R.id.canvasWebView),
            locator = Locator.XPATH,
            value = "//input[@placeholder='Topic Title']",
            timeoutMillis = 10000,
            intervalMillis = 500
        )

        /*Web.onWebView()
            .withElement(DriverAtoms.findElement(Locator.XPATH, "//input[@placeholder='Topic Title']"))
            .perform(Atoms.script("arguments[1].focus();"))  // Focus on the input field using JavaScript
          //  .perform(webKeys(CharArray(100) { '\b' }.joinToString("")))
            .perform(Atoms.script("arguments[1].value = '$newTitle'"))
        //  .perform(Atoms.script("arguments[0].value = ''")) //This clears the previous value so that's why we need it.*/

    /*    Web.onWebView()
            .withElement(DriverAtoms.findElement(Locator.XPATH, "//input[@placeholder='Topic Title']"))
            .perform(Atoms.script("""
        const input = arguments[0];
        input.value = '';
        input.dispatchEvent(new Event('input', { bubbles: true }));
        input.dispatchEvent(new Event('change', { bubbles: true }));
        input.blur();
        arguments[0].focus();
        input.value = '$newTitle';  // Directly inject the new title
        input.dispatchEvent(new Event('input', { bubbles: true }));
        input.dispatchEvent(new Event('change', { bubbles: true }));
        input.blur();
    """))*/


        Web.onWebView()
            .withElement(DriverAtoms.findElement(Locator.XPATH, "//input[@placeholder='Topic Title']"))
            .perform(Atoms.script("""
        function updateInputValue(input, newValue) {
            input.focus();
            input.value = '';
            input.dispatchEvent(new Event('input', { bubbles: true }));

            function typeCharacter(index) {
                if (index < newValue.length) {
                    input.value += newValue[index];
                    input.dispatchEvent(new Event('input', { bubbles: true }));
                    setTimeout(() => typeCharacter(index + 1), 500);
                } else {
                    input.dispatchEvent(new Event('change', { bubbles: true }));
                    input.blur();
                }
            }

            typeCharacter(0);
        }
        updateInputValue(arguments[0], "$newTitle");
    """))


        /*  Web.onWebView()
              .withElement(DriverAtoms.findElement(Locator.XPATH, "//input[@placeholder='Topic Title']"))
              .perform(Atoms.script("arguments[0].focus()"))  // Focus the input

          Web.onWebView()
              .withElement(DriverAtoms.findElement(Locator.XPATH, "//input[@placeholder='Topic Title']"))
              .perform(webKeys(CharArray(100) { '\b' }.joinToString("")))  // Clear input field with backspaces

          Web.onWebView()
              .withElement(DriverAtoms.findElement(Locator.XPATH, "//input[@placeholder='Topic Title']"))
              .perform(webKeys(newTitle))  // Type the new title
  */
        Web.onWebView()
            .withElement(DriverAtoms.findElement(Locator.XPATH, "//button[@data-testid='announcement-submit-button']"))
            .perform(webClick())

        print("asd")
    }

    private fun replyButtonNotDisplayed(): Boolean {
        return try {
            Web.onWebView()
                .check(WebViewAssertions.webContent(DomMatchers.hasElementWithXpath("//button[@data-testid='discussion-topic-reply']")))
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
        Web.onWebView()
            .check(WebViewAssertions.webContent(DomMatchers.hasElementWithXpath("//span[text()='$entryMessage']")))
    }

    fun assertToolbarDiscussionTitle(discussionTitle: String) {
        onView(withText(discussionTitle) + withAncestor(withId(R.id.toolbar) + ViewMatchers.hasSibling(
            withId(R.id.discussionWebView)
        )
        )).assertDisplayed()
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