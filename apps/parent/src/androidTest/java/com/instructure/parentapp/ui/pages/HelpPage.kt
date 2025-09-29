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
 *
 */
package com.instructure.parentapp.ui.pages

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
import com.instructure.parentapp.R
import org.hamcrest.CoreMatchers


class HelpPage : BasePage(R.id.helpDialog) {


    private val searchGuidesLabel by OnViewWithText(R.string.searchGuides)

    private val reportProblemLabel by OnViewWithStringTextIgnoreCase("Report a Problem")

    private val submitFeatureLabel by OnViewWithStringTextIgnoreCase("Submit a Feature Idea")

    private val shareLoveLabel by OnViewWithText(R.string.shareYourLove)

    private fun clickSearchGuidesLabel() {
        searchGuidesLabel.scrollTo().click()
    }

    fun clickReportProblemLabel() {
        reportProblemLabel.scrollTo().click()
    }

    private fun clickSubmitFeatureLabel() {
        submitFeatureLabel.scrollTo().click()
    }

    private fun clickShareLoveLabel() {
        shareLoveLabel.scrollTo().click()
    }

    fun assertHelpMenuDisplayed() {
        onView(withId(R.id.alertTitle) + withText(R.string.help)).assertDisplayed()
        onView(withId(R.id.helpDialog)).assertDisplayed()
    }

    fun assertReportProblemDialogDisplayed() {
        waitForViewWithText("Report a problem").assertDisplayed()
    }

    fun clickCancelReportProblem() {
        onView(withId(R.id.cancelButton)).click()
    }

    fun fillReportProblemForm(subject: String, description: String) {
        onView(withId(R.id.subjectEditText)).typeText(subject)
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.descriptionEditText)).typeText(description)
        Espresso.closeSoftKeyboard()
    }

    fun clickSendReportProblem() {
        onView(containsTextCaseInsensitive("Send")).scrollTo().click()
    }

    fun assertHelpMenuContent() {

        onView(withId(R.id.title) + withText("Search the Canvas Guides")).assertDisplayed()
        onView(withId(R.id.subtitle) + withText("Find answers to common questions")).assertDisplayed()

        onView(withId(R.id.title) + withText("CUSTOM LINK")).assertDisplayed()
        onView(withId(R.id.subtitle) + withText("This is a custom help link.")).assertDisplayed()

        onView(withId(R.id.title) + withText("Report a Problem")).assertDisplayed()
        onView(withId(R.id.subtitle) + withText("If Canvas misbehaves, tell us about it")).assertDisplayed()

        onView(withId(R.id.title) + withText("Submit a Feature Idea")).assertDisplayed()
        onView(withId(R.id.subtitle) + withText("Have an idea to improve Canvas?")).assertDisplayed()

        onView(withId(R.id.title) + withText("Share Your Love for the App")).assertDisplayed()
        onView(withId(R.id.subtitle) + withText("Tell us about your favorite parts of the app")).assertDisplayed()
    }

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
        }

        Intents.intended(expectedIntent)
    }
}
