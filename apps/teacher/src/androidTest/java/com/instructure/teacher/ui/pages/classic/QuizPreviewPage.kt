/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.teacher.ui.pages.classic

import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.waitForViewWithId
import com.instructure.teacher.R
import org.hamcrest.Matchers.containsString

/**
 * Represents the quiz preview page in the Teacher app.
 *
 * This class extends the `BasePage` class and provides methods for verifying
 * the quiz preview content, including title and description assertions within the WebView.
 *
 * @constructor Creates an instance of the `QuizPreviewPage` class.
 */
class QuizPreviewPage : BasePage(R.id.canvasWebView) {

    /**
     * Asserts that the quiz preview is displayed with the expected title and description.
     *
     * This method waits for the WebView to load, then verifies that both the quiz title
     * and description are present in the rendered preview content.
     *
     * @param quizTitle The expected quiz title to verify in the preview.
     * @param description The expected quiz description to verify in the preview.
     * @throws AssertionError if the preview is not loaded or if the title or description is not displayed.
     */
    fun assertPreviewDisplayed(quizTitle: String, description: String) {
        waitForViewWithId(R.id.canvasWebView)
        assertQuizTitleDisplayed(quizTitle)
        assertQuizDescriptionDisplayed(description)
    }

    private fun assertQuizTitleDisplayed(quizTitle: String) {
        onWebView()
            .withElement(findElement(Locator.TAG_NAME, "body"))
            .check(webMatches(getText(), containsString(quizTitle)))
    }

    private fun assertQuizDescriptionDisplayed(description: String) {
        onWebView()
            .withElement(findElement(Locator.TAG_NAME, "body"))
            .check(webMatches(getText(), containsString(description)))
    }
}
