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
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.espresso.waitForCheck
import com.instructure.student.mobius.assignmentDetails.submission.text.ui.TextSubmissionUploadFragment
import com.instructure.student.ui.rendertests.renderpages.TextSubmissionUploadRenderPage
import com.instructure.student.ui.utils.StudentRenderTest
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.not
import org.junit.Test

@HiltAndroidTest
class TextSubmissionUploadRenderTest : StudentRenderTest() {
    private val page = TextSubmissionUploadRenderPage()

    @Test
    fun submitButtonEnabledOnInput() {
        val text = "Canvas by Instructure"
        loadPageWithData(text)
        page.submitButton.waitForCheck(matches(isEnabled()))
    }

    @Test
    fun submitButtonDisabledOnNoInput() {
        val text = ""
        loadPageWithData(text)
        page.submitButton.waitForCheck(matches(not(isEnabled())))
    }

    @Test
    fun displaysFailedSubmissionError() {
        loadPageWithData("http://www.instructure.com", true)
        page.errorMessage.waitForCheck(matches(ViewMatchers.withText("Something went wrong on submission upload. Submit again.")))
    }

    private fun loadPageWithData(initialText: String? = null, isFailure: Boolean = false, course: Course = Course(), assignment: Assignment = Assignment()) {
        val route = TextSubmissionUploadFragment.makeRoute(course, assignment.id, assignment.name, initialText, isFailure)
        val fragment = TextSubmissionUploadFragment.newInstance(route)!!
        activityRule.activity.loadFragment(fragment)
    }
}