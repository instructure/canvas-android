/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
import com.instructure.espresso.page.plus
import com.instructure.espresso.scrollTo
import com.instructure.espresso.typeText
import com.instructure.teacher.R

/**
 * A page representing the Help menu in the application.
 *
 */
class HelpPage : BasePage(R.id.helpDialog) {

    /**
     * The label for asking an instructor.
     */
    private val askInstructorLabel by OnViewWithText(R.string.askInstructor)

    /**
     * The label for searching guides.
     */
    private val searchGuidesLabel by OnViewWithText(R.string.searchGuides)

    /**
     * The label for reporting a problem.
     */
    private val reportProblemLabel by OnViewWithText(R.string.reportProblem)

    /**
     * The label for submitting a feature idea.
     */
    private val submitFeatureLabel by OnViewWithStringTextIgnoreCase("Submit a Feature Idea")

    /**
     * The label for sharing your love.
     */
    private val shareLoveLabel by OnViewWithText(R.string.shareYourLove)

    /**
     * Verifies asking a question to an instructor.
     *
     * @param course The course to select in the spinner.
     * @param question The question to type in the message field.
     */
    fun verifyAskAQuestion(course: Course, question: String) {
        askInstructorLabel.scrollTo().click()
        waitForView(withText(course.name)).assertDisplayed()
        onView(withId(R.id.message)).scrollTo().perform(withCustomConstraints(typeText(question), isDisplayingAtLeast(1)))
        Espresso.closeSoftKeyboard()
        onView(containsTextCaseInsensitive("Send")).assertDisplayed()
    }

    /**
     * Launches the guides page.
     */
    fun launchGuides() {
        searchGuidesLabel.scrollTo().click()
    }

    /**
     * Verifies reporting a problem.
     *
     * @param subject The subject of the problem.
     * @param description The description of the problem.
     */
    fun verifyReportAProblem(subject: String, description: String) {
        reportProblemLabel.scrollTo().click()
        onView(withId(R.id.subjectEditText)).typeText(subject)
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.descriptionEditText)).typeText(description)
        Espresso.closeSoftKeyboard()
        onView(containsTextCaseInsensitive("Send")).scrollTo().assertDisplayed()
    }

    /**
     * Launches the share your love page.
     */
    fun shareYourLove() {
        shareLoveLabel.scrollTo().click()
    }

    /**
     * Submits a feature idea.
     */
    fun submitFeature() {
        submitFeatureLabel.scrollTo().click()
    }

    /**
     * Asserts that the Help menu is displayed.
     */
    fun assertHelpMenuDisplayed() {
        onView(withId(R.id.alertTitle) + withText(R.string.help)).assertDisplayed()
        onView(withId(R.id.helpDialog)).assertDisplayed()
    }

    /**
     * Asserts the content of the Help menu.
     */
    fun assertHelpMenuContent() {
        onView(withId(R.id.title) + withText(R.string.searchGuides))
        onView(withId(R.id.subtitle) + withText(R.string.searchGuidesDetails))

        onView(withId(R.id.title) + withText(R.string.askInstructor))
        onView(withId(R.id.subtitle) + withText(R.string.askInstructorDetails))

        onView(withId(R.id.title) + withText(R.string.reportProblem))
        onView(withId(R.id.subtitle) + withText(R.string.reportProblemDetails))

        onView(withId(R.id.title) + withText(R.string.shareYourLove))
        onView(withId(R.id.subtitle) + withText(R.string.shareYourLoveDetails))

        onView(withId(R.id.title) + withText("Submit a Feature Idea"))
        onView(withId(R.id.subtitle) + withText("Have an idea to improveCanvas?"))

        onView(withId(R.id.title) + withText("COVID-19 Canvas Resources"))
        onView(withId(R.id.subtitle) + withText("Tips for teaching and learning online"))
    }
}
