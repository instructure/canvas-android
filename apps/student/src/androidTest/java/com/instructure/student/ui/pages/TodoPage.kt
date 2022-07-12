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

import android.widget.Button
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Quiz
import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.dataseeding.model.QuizApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.scrollTo
import com.instructure.student.R
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf

class TodoPage: BasePage(R.id.todoPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)

    fun assertAssignmentDisplayed(assignment: AssignmentApiModel) {
        assertTextDisplayedInRecyclerView(assignment.name)
    }

    fun assertAssignmentDisplayed(assignment: Assignment) {
        assertTextDisplayedInRecyclerView(assignment.name!!)
    }

    fun assertQuizDisplayed(quiz: QuizApiModel) {
        assertTextDisplayedInRecyclerView(quiz.title)
    }

    fun assertQuizDisplayed(quiz: Quiz) {
        assertTextDisplayedInRecyclerView(quiz.title!!)
    }

    fun assertQuizNotDisplayed(quiz: QuizApiModel) {
        onView(withText(quiz.title!!)).check(doesNotExist())
    }

    fun assertQuizNotDisplayed(quiz: Quiz) {
        onView(withText(quiz.title!!)).check(doesNotExist())
    }

    fun selectAssignment(assignment: Assignment) {
        assertTextDisplayedInRecyclerView(assignment.name!!)
        onView(withText(assignment.name!!)).click()
    }

    fun selectQuiz(quiz: Quiz) {
        assertTextDisplayedInRecyclerView(quiz.title!!)
        onView(withText(quiz.title!!)).click()
    }



    fun chooseFavoriteCourseFilter() {
        onView(withId(R.id.todoListFilter)).click()
        onView(containsTextCaseInsensitive("Favorited Courses")).click()
        onView(allOf(isAssignableFrom(Button::class.java), containsTextCaseInsensitive("OK"))).click()
    }

    fun clearFilter() {
        onView(withId(R.id.clearFilterTextView)).click()
    }

    // Assert that a string is displayed somewhere in the RecyclerView
    private fun assertTextDisplayedInRecyclerView(s: String) {
        // Common matcher
        val matcher = withText(Matchers.containsString(s))

        // Now make sure that it is displayed
        onView(matcher).scrollTo().assertDisplayed()
    }
}
