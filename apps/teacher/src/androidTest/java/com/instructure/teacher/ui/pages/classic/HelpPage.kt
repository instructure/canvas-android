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
package com.instructure.teacher.ui.pages.classic

import android.app.Instrumentation
import android.content.Intent
import androidx.test.espresso.Espresso
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.espresso.OnViewWithStringTextIgnoreCase
import com.instructure.espresso.OnViewWithText
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.espresso.scrollTo
import com.instructure.espresso.typeText
import com.instructure.teacher.R
import org.hamcrest.CoreMatchers

/**
 * A page representing the Help menu in the Teacher application.
 *
 */
class HelpPage : BasePage(R.id.helpDialog) {

    /**
     * The label for searching guides.
     */
    private val searchGuidesLabel by OnViewWithText(R.string.searchGuides)

    /**
     * The label for reporting a problem.
     */
    private val reportProblemLabel by OnViewWithText(R.string.report_problem)

    /**
     * The label for submitting a feature idea.
     */
    private val submitFeatureLabel by OnViewWithStringTextIgnoreCase("Submit a Feature Idea")

    /**
     * The label for sharing your love.
     */
    private val shareLoveLabel by OnViewWithText(R.string.shareYourLove)

    /**
     * Clicks on the 'Search the Canvas Guides' help menu.
     */
    private fun clickSearchGuidesLabel() {
        searchGuidesLabel.scrollTo().click()
    }

    /**
     * Asserts the details of the 'Report a problem' dialog by filling in the subject and description fields and checking for the 'Send' button.
     *
     * @param subject The subject of the problem.
     * @param description The description of the problem.
     */
    fun assertReportProblemDialogDetails(subject: String, description: String) {
        reportProblemLabel.scrollTo().click()
        onView(withId(R.id.subjectEditText)).typeText(subject)
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.descriptionEditText)).typeText(description)
        Espresso.closeSoftKeyboard()
        onView(containsTextCaseInsensitive("Send")).scrollTo().assertDisplayed()
    }

    /**
     * Clicks on the 'Send' button on the 'Report a problem' dialog.
     */
    fun clickSendReportProblem() {
        onView(containsTextCaseInsensitive("Send")).scrollTo().click()
    }

    /**
     * Assert that the 'Report a problem' dialog is displayed and then dismiss the dialog.
     */
    fun assertReportProblemDialogDisplayed() {
        waitForViewWithText("Report a problem").assertDisplayed()
    }

    /**
     * Click on the 'Cancel' button on the 'Report a problem' dialog.
     */
    fun clickCancelReportProblem() {
        onView(withId(R.id.cancelButton)).click()
    }

    /**
     * Clicks on the 'Conference Guides for Remote Classrooms' help menu.
     */
    private fun clickConferenceGuidesForRemoteClassroomsLabel() {
        onView(withText("Conference Guides for Remote Classrooms")).scrollTo().click()
    }

    /**
     * Clicks on the 'Ask the Community' help menu.
     */
    private fun clickAskTheCommunityLabel() {
        onView(withText("Ask the Community")).scrollTo().click()
    }

    /**
     * Clicks on the 'Training Services Portal' help menu.
     */
    private fun clickTrainingServicesPortalLabel() {
        onView(withText("Training Services Portal")).scrollTo().click()
    }

    /**
     * Clicks on the 'Share Your Love for the App' help menu.
     */
    private fun clickShareLoveLabel() {
        shareLoveLabel.scrollTo().click()
    }

    /**
     * Clicks on the 'Submit a Feature Idea' help menu.
     */
    private fun clickSubmitFeatureLabel() {
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

        onView(withId(R.id.title) + withText("Search the Canvas Guides")).assertDisplayed()
        onView(withId(R.id.subtitle) + withText("Find answers to common questions")).assertDisplayed()

        onView(withId(R.id.title) + withText("CUSTOM LINK")).assertDisplayed()
        onView(withId(R.id.subtitle) + withText("This is a custom help link.")).assertDisplayed()

        onView(withId(R.id.title) + withText("Conference Guides for Remote Classrooms")).assertDisplayed()
        onView(withId(R.id.subtitle) + withText("Get help on how to use and configure conferences in canvas.")).assertDisplayed()

        onView(withId(R.id.title) + withText("Report a Problem")).assertDisplayed()
        onView(withId(R.id.subtitle) + withText("If Canvas misbehaves, tell us about it")).assertDisplayed()

        onView(withId(R.id.title) + withText("Ask the Community")).assertDisplayed()
        onView(withId(R.id.subtitle) + withText("Get help from a Canvas expert")).assertDisplayed()

        onView(withId(R.id.title) + withText("Submit a Feature Idea")).assertDisplayed()
        onView(withId(R.id.subtitle) + withText("Have an idea to improve Canvas?")).assertDisplayed()

        onView(withId(R.id.title) + withText("Training Services Portal")).assertDisplayed()
        onView(withId(R.id.subtitle) + withText("Access Canvas training videos and courses")).assertDisplayed()

        onView(withId(R.id.title) + withText("Share Your Love for the App")).assertDisplayed()
        onView(withId(R.id.subtitle) + withText("Tell us about your favorite parts of the app")).assertDisplayed()
    }

    /**
     * Asserts that clicking on a Help menu item launches an intent with the expected URL.
     *
     * @param helpMenuText The text of the Help menu item to click.
     * @param expectedURL The expected URL that should be opened.
     */
    fun assertHelpMenuURL(helpMenuText: String, expectedURL: String) {
        val expectedIntent = CoreMatchers.allOf(
            IntentMatchers.hasAction(Intent.ACTION_VIEW),
            CoreMatchers.anyOf(
                IntentMatchers.hasData(expectedURL),
            )
        )
        Intents.intending(expectedIntent).respondWith(Instrumentation.ActivityResult(0, null))

        when (helpMenuText) {
            "Search the Canvas Guides" -> clickSearchGuidesLabel()
            "Submit a Feature Idea" -> clickSubmitFeatureLabel()
            "Share Your Love for the App" -> clickShareLoveLabel()
            "Conference Guides for Remote Classrooms" -> clickConferenceGuidesForRemoteClassroomsLabel()
            "Ask the Community" -> clickAskTheCommunityLabel()
            "Training Services Portal" -> clickTrainingServicesPortalLabel()
        }

        Intents.intended(expectedIntent)
    }
}
