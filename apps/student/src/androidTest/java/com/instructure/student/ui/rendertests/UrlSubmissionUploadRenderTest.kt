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
 */
package com.instructure.student.ui.rendertests

import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.espresso.assertHasText
import com.instructure.espresso.replaceText
import com.instructure.espresso.waitForCheck
import com.instructure.student.mobius.assignmentDetails.submission.url.ui.UrlSubmissionUploadFragment
import com.instructure.student.ui.utils.StudentRenderTest
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.not
import org.junit.Test

@HiltAndroidTest
class UrlSubmissionUploadRenderTest : StudentRenderTest() {

    private val testUrl = "https://www.instructure.com"

    @Test
    fun displaysUrl() {
        loadPageWithData("https://www.instructure.com")
        urlSubmissionUploadRenderPage.url.assertHasText(testUrl)
    }

    @Test
    fun displaysCleartextError() {
        loadPageWithData("http://www.instructure.com")
        urlSubmissionUploadRenderPage.errorMsg.waitForCheck(matches(withText("No preview available for URLs using \'http://\'")))
    }

    @Test
    fun displaysCleartextErrorOnFailedSubmission() {
        loadPageWithData("http://www.instructure.com", true)
        urlSubmissionUploadRenderPage.errorMsg.waitForCheck(matches(withText("Something went wrong on submission upload. Submit again.\nNo preview available for URLs using \'http://\'")))
    }

    @Test
    fun displaysFailedSubmissionError() {
        loadPageWithData("http://www.instructure.com", true)
        urlSubmissionUploadRenderPage.errorMsg.waitForCheck(matches(withText("Something went wrong on submission upload. Submit again.")))
    }

    @Test
    fun invalidUrlDisablesSubmitButton() {
        loadPageWithData("www.instructure.com")
        urlSubmissionUploadRenderPage.url.replaceText("abc123")
        urlSubmissionUploadRenderPage.submit.waitForCheck(matches(not(isEnabled())))
    }

    @Test
    fun validUrlEnablesSubmitButton() {
        loadPageWithData("abc123")
        urlSubmissionUploadRenderPage.url.replaceText("www.instructure.com")
        urlSubmissionUploadRenderPage.submit.waitForCheck(matches(isEnabled()))
    }

    private fun loadPageWithData(initialUrl: String? = null, isFailure: Boolean = false, course: Course = Course(), assignment: Assignment = Assignment()) {
        val route = UrlSubmissionUploadFragment.makeRoute(course, assignment.id, assignment.name, initialUrl, isFailure)
        val fragment = UrlSubmissionUploadFragment.newInstance(route)!!
        activityRule.activity.loadFragment(fragment)
    }
}
