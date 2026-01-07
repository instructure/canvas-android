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
package com.instructure.student.ui.pages.classic

import android.app.Instrumentation
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.BundleMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.StringConstants.HelpMenu
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.canvasapi2.models.Course
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.espresso.OnViewWithStringTextIgnoreCase
import com.instructure.espresso.OnViewWithText
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.matchers.WaitForViewMatcher.waitForView
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.espresso.scrollTo
import com.instructure.espresso.typeText
import com.instructure.student.R
import org.hamcrest.CoreMatchers

class HelpPage : BasePage(R.id.helpDialog) {

    private val askInstructorLabel by OnViewWithText(R.string.askInstructor)
    private val searchGuidesLabel by OnViewWithText(R.string.searchGuides)
    private val reportProblemLabel by OnViewWithStringTextIgnoreCase("Report a Problem")
    private val submitFeatureLabel by OnViewWithStringTextIgnoreCase("Submit a Feature Idea")
    private val shareLoveLabel by OnViewWithText(R.string.shareYourLove)

    fun assertAskYourInstructorDialogDetails(course: Course, question: String) {
        askInstructorLabel.scrollTo().click()
        waitForView(withText(course.name)).assertDisplayed() // Verify that our course is selected in the spinner
        onView(withId(R.id.message)).scrollTo().perform(withCustomConstraints(typeText(question), isDisplayingAtLeast(1)))
        Espresso.closeSoftKeyboard()
        // Let's just make sure that the "Send" button is displayed, rather than actually pressing it
        onView(containsTextCaseInsensitive("Send")).assertDisplayed()
    }

    private fun assertAskYourInstructorDialogDetails(course: CourseApiModel, question: String) {
        askInstructorLabel.scrollTo().click()
        waitForView(withText(course.name)).assertDisplayed() // Verify that our course is selected in the spinner
        onView(withId(R.id.message)).scrollTo().perform(withCustomConstraints(typeText(question), isDisplayingAtLeast(1)))
        Espresso.closeSoftKeyboard()
        // Let's just make sure that the "Send" button is displayed, rather than actually pressing it
        onView(containsTextCaseInsensitive("Send")).assertDisplayed()
    }

    fun sendQuestionToInstructor(course: CourseApiModel, question: String) {
        assertAskYourInstructorDialogDetails(course, question)
        onView(containsTextCaseInsensitive("Send")).click()
    }

    fun clickSearchGuidesLabel() {
        searchGuidesLabel.scrollTo().click()
    }

    fun assertReportProblemDialogDetails(subject: String, description: String) {
        reportProblemLabel.scrollTo().click()
        onView(withId(R.id.subjectEditText)).typeText(subject)
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.descriptionEditText)).typeText(description)
        Espresso.closeSoftKeyboard()
        // Let's just make sure that the "Send" button is displayed, rather than actually pressing it
        assertSendReportProblemButtonDisplayed()
    }

    fun assertReportProblemDialogDisplayed() {
        waitForViewWithText("Report a problem").assertDisplayed()
    }

    fun clickCancelReportProblem() {
        onView(withId(R.id.cancelButton)).click()
    }

    fun clickSendReportProblem() {
        onView(containsTextCaseInsensitive("Send")).scrollTo().click()
    }

    private fun assertSendReportProblemButtonDisplayed() {
        onView(containsTextCaseInsensitive("Send")).scrollTo().assertDisplayed()
    }

    fun clickShareLoveLabel() {
        shareLoveLabel.scrollTo().click()
    }

    fun clickSubmitFeatureLabel() {
        submitFeatureLabel.scrollTo().click()
    }

    fun assertHelpMenuDisplayed() {
        onView(withId(R.id.alertTitle) + withText(R.string.help)).assertDisplayed()
        onView(withId(R.id.helpDialog)).assertDisplayed()
    }

    fun assertHelpMenuContent() {

        onView(withId(R.id.title) + withText(HelpMenu.SEARCH_GUIDES_TITLE)).assertDisplayed()
        onView(withId(R.id.subtitle) + withText(HelpMenu.SEARCH_GUIDES_SUBTITLE)).assertDisplayed()

        onView(withId(R.id.title) + withText(HelpMenu.CUSTOM_LINK_TITLE)).assertDisplayed()
        onView(withId(R.id.subtitle) + withText(HelpMenu.CUSTOM_LINK_SUBTITLE)).assertDisplayed()

        onView(withId(R.id.title) + withText(HelpMenu.Student.ASK_INSTRUCTOR_TITLE)).assertDisplayed()
        onView(withId(R.id.subtitle) + withText(HelpMenu.Student.ASK_INSTRUCTOR_SUBTITLE)).assertDisplayed()

        onView(withId(R.id.title) + withText(HelpMenu.REPORT_PROBLEM_TITLE)).assertDisplayed()
        onView(withId(R.id.subtitle) + withText(HelpMenu.REPORT_PROBLEM_SUBTITLE)).assertDisplayed()

        onView(withId(R.id.title) + withText(HelpMenu.SUBMIT_FEATURE_TITLE)).assertDisplayed()
        onView(withId(R.id.subtitle) + withText(HelpMenu.SUBMIT_FEATURE_SUBTITLE)).assertDisplayed()

        onView(withId(R.id.title) + withText(HelpMenu.SHARE_LOVE_TITLE)).assertDisplayed()
        onView(withId(R.id.subtitle) + withText(HelpMenu.SHARE_LOVE_SUBTITLE)).assertDisplayed()
    }

    fun assertHelpMenuURL(helpMenuText: String, expectedURL: String) {
        val expectedIntent = CoreMatchers.allOf(
            IntentMatchers.hasExtras(
                BundleMatchers.hasEntry(
                    "bundledExtras",
                    BundleMatchers.hasEntry("internalURL", expectedURL)
                )
            )
        )

        Intents.intending(expectedIntent).respondWith(Instrumentation.ActivityResult(0, null))

        when (helpMenuText) {
            HelpMenu.SEARCH_GUIDES_TITLE -> clickSearchGuidesLabel()
            HelpMenu.SUBMIT_FEATURE_TITLE -> clickSubmitFeatureLabel()
            HelpMenu.SHARE_LOVE_TITLE -> clickShareLoveLabel()
        }

        Intents.intended(expectedIntent)
    }
}
