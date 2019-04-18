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
package com.instructure.student.ui.pages.renderPages

import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertContainsText
import com.instructure.espresso.assertDisplayed
import com.instructure.student.R
import com.instructure.student.ui.pages.SubmissionDetailsEmptyContentPage
import com.instructure.student.ui.pages.SubmissionDetailsPage
import org.hamcrest.CoreMatchers.not

class SubmissionDetailsEmptyContentRenderPage : SubmissionDetailsEmptyContentPage() {
    val dueDateTextView by OnViewWithId(R.id.message)
    val submitButton by OnViewWithId(R.id.submitButton)

    fun assertSubmitButtonEnabled() {
        submitButton.check(matches(isEnabled()))
    }

    fun assertSubmitButtonHidden() {
        submitButton.check(matches(not(isDisplayed())))
    }

    fun assertExpectedDueDate(expectedText: String) {
        dueDateTextView.assertContainsText(expectedText)
    }

}