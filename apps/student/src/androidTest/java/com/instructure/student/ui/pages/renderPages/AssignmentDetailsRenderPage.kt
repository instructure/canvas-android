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
package com.instructure.student.ui.pages.renderPages

import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.getText
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.espresso.*
import com.instructure.espresso.page.onViewWithText
import com.instructure.student.R
import com.instructure.student.ui.pages.AssignmentDetailsPage
import org.hamcrest.Matchers

class AssignmentDetailsRenderPage : AssignmentDetailsPage() {

    val toolbar by OnViewWithId(R.id.toolbar)
    val assignmentName by OnViewWithId(R.id.assignmentName)
    val points by OnViewWithId(R.id.points)
    val submissionStatus by OnViewWithId(R.id.submissionStatus)
    val date by OnViewWithId(R.id.dueDateTextView)
    val submissionTypes by OnViewWithId(R.id.submissionTypesTextView)
    val fileTypes by OnViewWithId(R.id.fileTypesTextView)
    val noDescription by OnViewWithId(R.id.noDescriptionContainer)
    val descriptionWebView by OnViewWithId(R.id.descriptionWebView)
    val gradeContainer by OnViewWithId(R.id.gradeContainer)

    fun assertDisplaysToolbarTitle(text: String) {
        onViewWithText(text).assertDisplayed()
    }

    fun assertDisplaysToolbarSubtitle(text: String) {
        onViewWithText(text).assertDisplayed()
    }

    fun assertDisplaysAssignmentName(name: String) {
        assignmentName.assertHasText(name)
    }

    fun assertDisplaysPoints(text: String) {
        points.assertHasText(text)
    }

    fun assertDisplaysSubmissionStatus(text: String) {
        submissionStatus.assertHasText(text)
    }

    fun assertDisplaysDate(text: String) {
        date.assertHasText(text)
    }

    fun assertDisplaysSubmissionTypes(text: String) {
        submissionTypes.assertHasText(text)
    }

    fun assertDisplaysFileTypes(text: String) {
        fileTypes.assertHasText(text)
    }

    fun assertDisplaysNoDescription() {
        noDescription.assertVisible()
        descriptionWebView.assertGone()
    }

    fun assertDisplaysGrade() {
        gradeContainer.assertVisible()
    }

    fun assertDisplaysDescription(text: String) {
        descriptionWebView.assertVisible()
        noDescription.assertGone()
        onWebView().withElement(findElement(Locator.TAG_NAME, "p")).check(webMatches(getText(), Matchers.comparesEqualTo(text)))
    }
}
