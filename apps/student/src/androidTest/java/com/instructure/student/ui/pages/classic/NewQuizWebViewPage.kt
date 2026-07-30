/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.DriverAtoms.webScrollIntoView
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.withElementRepeat
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.withId
import com.instructure.student.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString

class NewQuizWebViewPage : BasePage(R.id.webView) {

    fun waitForWebView() {
        waitForView(allOf(withId(R.id.webView), isDisplayed()))
    }

    fun assertDescriptionDisplayed(description: String) {
        onWebView(allOf(withId(R.id.webView), isDisplayed()))
            .withElementRepeat(findElement(Locator.XPATH, "//*[contains(., '$description')]"), 15)
            .perform(webScrollIntoView())
            .check(webMatches(getText(), containsString(description)))
    }

    fun assertErrorMessageDisplayed() {
        onWebView(allOf(withId(R.id.webView), isDisplayed()))
            .withElementRepeat(findElement(Locator.XPATH, "//*[contains(., 'There is a problem with this assessment')]"), 15)
            .perform(webScrollIntoView())
            .check(webMatches(getText(), containsString("There is a problem with this assessment")))
    }

    fun assertBeginButtonDisplayed() {
        onWebView(allOf(withId(R.id.webView), isDisplayed()))
            .withElementRepeat(findElement(Locator.XPATH, "//button[contains(., 'Begin')]"), 15)
            .perform(webScrollIntoView())
            .check(webMatches(getText(), containsString("Begin")))
    }

    fun clickBeginButton() {
        onWebView(allOf(withId(R.id.webView), isDisplayed()))
            .withElementRepeat(findElement(Locator.XPATH, "//button[contains(., 'Begin')]"), 15)
            .perform(webScrollIntoView())
            .perform(webClick())
    }

    fun assertQuestionDisplayed(questionText: String) {
        onWebView(allOf(withId(R.id.webView), isDisplayed()))
            .withElementRepeat(findElement(Locator.XPATH, "//*[contains(., '$questionText')]"), 15)
            .perform(webScrollIntoView())
            .check(webMatches(getText(), containsString(questionText)))
    }

    fun clickAnswerByTextAtPosition(answer: String, position: Int) {
        onWebView(allOf(withId(R.id.webView), isDisplayed()))
            .withElementRepeat(findElement(Locator.XPATH, "(//label[contains(., '$answer')])[$position]"), 15)
            .perform(webScrollIntoView())
            .perform(webClick())
    }

    fun assertResumeButtonDisplayed() {
        onWebView(allOf(withId(R.id.webView), isDisplayed()))
            .withElementRepeat(findElement(Locator.XPATH, "//button[contains(., 'Resume')]"), 15)
            .perform(webScrollIntoView())
            .check(webMatches(getText(), containsString("Resume")))
    }

    fun clickResumeButton() {
        onWebView(allOf(withId(R.id.webView), isDisplayed()))
            .withElementRepeat(findElement(Locator.XPATH, "//button[contains(., 'Resume')]"), 15)
            .perform(webScrollIntoView())
            .perform(webClick())
    }

    fun clickSubmitButton() {
        onWebView(allOf(withId(R.id.webView), isDisplayed()))
            .withElementRepeat(findElement(Locator.XPATH, "//button[normalize-space(.)='Submit']"), 15)
            .perform(webScrollIntoView())
            .perform(webClick())
    }

    fun assertSubmitConfirmationDisplayed() {
        onWebView(allOf(withId(R.id.webView), isDisplayed()))
            .withElementRepeat(findElement(Locator.XPATH, "//*[contains(., 'Upon submission you will not be able to change your answers. Are you ready to submit?')]"), 15)
            .check(webMatches(getText(), containsString("Upon submission you will not be able to change your answers. Are you ready to submit?")))
    }

    fun clickConfirmSubmitButton() {
        onWebView(allOf(withId(R.id.webView), isDisplayed()))
            .withElementRepeat(findElement(Locator.XPATH, "//button[normalize-space(.)='Cancel']/following-sibling::button[normalize-space(.)='Submit']"), 15)
            .perform(webScrollIntoView())
            .perform(webClick())
    }

    fun assertViewResultsButtonDisplayed() {
        onWebView(allOf(withId(R.id.webView), isDisplayed()))
            .withElementRepeat(findElement(Locator.XPATH, "//button[contains(., 'View Results')]"), 15)
            .perform(webScrollIntoView())
            .check(webMatches(getText(), containsString("View Results")))
    }

    fun clickViewResultsButton() {
        onWebView(allOf(withId(R.id.webView), isDisplayed()))
            .withElementRepeat(findElement(Locator.XPATH, "//button[contains(., 'View Results')]"), 15)
            .perform(webScrollIntoView())
            .perform(webClick())
    }

    fun assertResultScoreDisplayed(score: String) {
        onWebView(allOf(withId(R.id.webView), isDisplayed()))
            .withElementRepeat(findElement(Locator.XPATH, "//*[contains(., '$score')]"), 15)
            .perform(webScrollIntoView())
            .check(webMatches(getText(), containsString(score)))
    }

    fun assertResultPercentageDisplayed(percentage: String) {
        onWebView(allOf(withId(R.id.webView), isDisplayed()))
            .withElementRepeat(findElement(Locator.XPATH, "//*[contains(., '$percentage')]"), 15)
            .perform(webScrollIntoView())
            .check(webMatches(getText(), containsString(percentage)))
    }
}