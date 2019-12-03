/*
 * Copyright (C) 2019 - present Instructure, Inc.
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

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.canvasapi2.models.Course
import com.instructure.espresso.OnViewWithStringTextIgnoreCase
import com.instructure.espresso.OnViewWithText
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.matchers.WaitForViewMatcher.waitForView
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.scrollTo
import com.instructure.espresso.typeText
import com.instructure.student.R

// This is a little hokey, as the options that appear are somewhat governed by the results of
// the /api/v1/accounts/self/help_links call.  If that changes a lot over time (thus breaking
// this test), we can back off to some easier test like "some options are visible".
class HelpPage : BasePage(R.id.helpDialog) {
    private val askInstructorLabel by OnViewWithText(R.string.askInstructor)
    private val searchGuidesLabel by OnViewWithText(R.string.searchGuides)
    private val reportProblemLabel by OnViewWithText(R.string.reportProblem)
    private val submitFeatureLabel by OnViewWithStringTextIgnoreCase("Submit a Feature Idea")
    private val shareLoveLabel by OnViewWithText(R.string.shareYourLove)

    fun verifyAskAQuestion(course: Course, question: String) {
        askInstructorLabel.scrollTo().click()
        waitForView(withText(course.name)).assertDisplayed() // Verify that our course is selected in the spinner
        onView(withId(R.id.message)).perform(withCustomConstraints(typeText(question), isDisplayingAtLeast(1)))
        Espresso.closeSoftKeyboard()
        // Let's just make sure that the "Send" button is displayed, rather than actually pressing it
        onView(containsTextCaseInsensitive("Send")).assertDisplayed()
    }

    fun launchGuides() {
        searchGuidesLabel.scrollTo().click()
    }

    fun verifyReportAProblem(subject: String, description: String) {
        reportProblemLabel.scrollTo().click()
        onView(withId(R.id.subjectEditText)).typeText(subject)
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.descriptionEditText)).typeText(description)
        Espresso.closeSoftKeyboard()
        // Let's just make sure that the "Send" button is displayed, rather than actually pressing it
        onView(containsTextCaseInsensitive("Send")).assertDisplayed()
    }

    fun shareYourLove() {
        shareLoveLabel.scrollTo().click()
    }

    fun submitFeature() {
        submitFeatureLabel.scrollTo().click()
    }
}
