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
package com.instructure.teacher.ui.pages

import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertVisible
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.scrollTo
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.teacher.R

@Suppress("unused")
class SpeedGraderQuizSubmissionPage : BasePage() {

    private val quizIconView by WaitForViewWithId(R.id.quizIconView, autoAssert = true)
    private val quizSubmissionLabel by WaitForViewWithId(R.id.quizSubmissionLabelView, autoAssert = true)

    private val pendingReviewLabel by WaitForViewWithId(R.id.pendingReviewLabel)
    private val gradeSubmissionButton by WaitForViewWithId(R.id.gradeQuizButton)
    private val viewQuizButton by WaitForViewWithId(R.id.viewQuizButton)

    fun assertShowsNoSubmissionState() {
        waitForViewWithText(R.string.noSubmissionTeacher)
    }

    fun assertShowsViewQuizState() {
        viewQuizButton.assertVisible()
    }

    fun assertShowsPendingReviewState() {
        pendingReviewLabel.assertVisible()
        gradeSubmissionButton.assertVisible()
    }

    fun startReview() {
        scrollTo(R.id.gradeQuizButton)
        gradeSubmissionButton.click()
    }

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
