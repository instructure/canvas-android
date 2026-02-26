/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

import androidx.annotation.StringRes
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertCompletelyDisplayed
import com.instructure.espresso.assertVisible
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.scrollTo
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.teacher.R

/**
 * Represents the SpeedGrader Quiz Submission Page.
 *
 * This page provides functionality for interacting with the elements on the SpeedGrader Quiz Submission page. It contains
 * methods for asserting the different states of the quiz submission, such as no submission state, view quiz state, and
 * pending review state. It also provides methods for starting and finishing the review process. This page extends the
 * BasePage class.
 */
@Suppress("unused")
class SpeedGraderQuizSubmissionPage : BasePage() {

    private val quizIconView by WaitForViewWithId(R.id.quizIconView, autoAssert = true)
    private val quizSubmissionLabel by WaitForViewWithId(R.id.quizSubmissionLabelView, autoAssert = true)

    private val pendingReviewLabel by WaitForViewWithId(R.id.pendingReviewLabel)
    private val gradeSubmissionButton by WaitForViewWithId(R.id.gradeQuizButton)
    private val viewQuizButton by WaitForViewWithId(R.id.viewQuizButton)

    /**
     * Asserts that the page shows the "No Submission" state.
     */
    fun assertShowsNoSubmissionState(@StringRes stringRes: Int) {
        waitForViewWithText(stringRes).assertCompletelyDisplayed()
    }

    /**
     * Asserts that the page shows the "View Quiz" state.
     */
    fun assertShowsViewQuizState() {
        viewQuizButton.assertVisible()
    }

    /**
     * Asserts that the page shows the "Pending Review" state.
     */
    fun assertShowsPendingReviewState() {
        pendingReviewLabel.assertVisible()
        gradeSubmissionButton.assertVisible()
    }

    /**
     * Starts the review process by clicking on the "Grade Submission" button.
     */
    fun startReview() {
        scrollTo(R.id.gradeQuizButton)
        gradeSubmissionButton.click()
    }

    /**
     * Finishes the review process by performing the necessary actions on the web view.
     */
    fun finishReview() {
        Web.onWebView()
            .withElement(
                DriverAtoms.findElement(
                    Locator.CSS_SELECTOR,
                    """button[class="btn btn-primary update-scores"]"""
                )
            )
            .perform(DriverAtoms.webClick())
    }

}
