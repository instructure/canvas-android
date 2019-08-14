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

import android.view.View
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertContainsText
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.closeSoftKeyboard
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.scrollTo
import com.instructure.espresso.swipeDown
import com.instructure.espresso.typeText
import com.instructure.student.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString

open class AssignmentDetailsPage : BasePage(R.id.assignmentDetailsPage) {
    fun verifyAssignmentSubmitted() {
        onView(withText(R.string.submissionStatusSuccessTitle)).assertDisplayed()
        onView(allOf(withId(R.id.submissionStatus), withText(R.string.submitted))).assertDisplayed()
    }

    fun verifyAssignmentGraded(score: String) {
        onView(allOf(withId(R.id.gradeContainer), isDisplayed())).scrollTo().assertDisplayed()
        onView(allOf(withId(R.id.score), isDisplayed())).scrollTo().assertContainsText(score)
        onView(allOf(withId(R.id.submissionStatus), withText(R.string.gradedSubmissionLabel))).assertDisplayed()
    }

    fun refresh() {
        onView(allOf(withId(R.id.swipeRefreshLayout),  isDisplayed())).swipeDown()
    }

    fun goToSubmissionDetails() {
        onView(withId(R.id.submissionAndRubricLabel)).scrollTo().click()
    }
}

