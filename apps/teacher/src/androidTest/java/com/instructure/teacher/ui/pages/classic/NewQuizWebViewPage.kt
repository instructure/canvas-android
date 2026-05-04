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
package com.instructure.teacher.ui.pages.classic

import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.DriverAtoms.webScrollIntoView
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.withElementRepeat
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.withId
import com.instructure.teacher.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString

class NewQuizWebViewPage : BasePage(R.id.webView) {

    fun waitForWebView() {
        waitForView(allOf(withId(R.id.webView), isDisplayed()))
    }

    fun assertBuildViewDisplayed() {
        onWebView(allOf(withId(R.id.webView), isDisplayed()))
            .withElementRepeat(findElement(Locator.XPATH, "//*[contains(., 'Build')]"), 15)
            .perform(webScrollIntoView())
            .check(webMatches(getText(), containsString("Build")))
    }

    fun assertTitleDisplayed(title: String) {
        onWebView(allOf(withId(R.id.webView), isDisplayed()))
            .withElementRepeat(findElement(Locator.XPATH, "//*[contains(., '$title')]"), 15)
            .perform(webScrollIntoView())
            .check(webMatches(getText(), containsString(title)))
    }

    fun assertInstructionsDisplayed(instructions: String) {
        onWebView(allOf(withId(R.id.webView), isDisplayed()))
            .withElementRepeat(findElement(Locator.XPATH, "//*[contains(., '$instructions')]"), 15)
            .perform(webScrollIntoView())
            .check(webMatches(getText(), containsString(instructions)))
    }

    fun assertQuestionDisplayed(questionText: String) {
        onWebView(allOf(withId(R.id.webView), isDisplayed()))
            .withElementRepeat(findElement(Locator.XPATH, "//*[contains(., '$questionText')]"), 15)
            .perform(webScrollIntoView())
            .check(webMatches(getText(), containsString(questionText)))
    }
}