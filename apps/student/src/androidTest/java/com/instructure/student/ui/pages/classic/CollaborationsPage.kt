/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.ui.pages.classic

import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.checkRepeat
import com.instructure.student.R
import org.hamcrest.Matchers
import org.hamcrest.Matchers.containsString

/**
 * Singleton for collaborations page web view assertions
 */
object CollaborationsPage {

    fun assertCurrentCollaborationsHeaderPresent() {
        Web.onWebView(Matchers.allOf(withId(R.id.contentWebView), isDisplayed()))
                .withElement(DriverAtoms.findElement(Locator.CLASS_NAME, "collaborations-header"))
                .perform(DriverAtoms.webScrollIntoView())
                .check(webMatches(getText(), containsString("Current Collaborations") ))
    }

    fun assertStartANewCollaborationPresent() {
        Web.onWebView(Matchers.allOf(withId(R.id.contentWebView), isDisplayed()))
                .withElement(DriverAtoms.findElement(Locator.TAG_NAME, "h2"))
                .perform(DriverAtoms.webScrollIntoView())
                .checkRepeat(webMatches(getText(), containsString("Start a New Collaboration") ), 30)
    }

    fun assertGoogleDocsChoicePresentAsDefaultOption() {
        Web.onWebView(Matchers.allOf(withId(R.id.contentWebView), isDisplayed()))
                .withElement(DriverAtoms.findElement(Locator.ID, "collaboration_collaboration_type"))
                .perform(DriverAtoms.webScrollIntoView())
                .check(webMatches(getText(), containsString("Google Docs") ))
    }

    fun assertGoogleDocsWarningDescriptionPresent() {
        Web.onWebView(Matchers.allOf(withId(R.id.contentWebView), isDisplayed()))
                .withElement(DriverAtoms.findElement(Locator.ID, "google_docs_description"))
                .perform(DriverAtoms.webScrollIntoView())
                .check(webMatches(getText(), containsString("Warning:") ))
    }

}