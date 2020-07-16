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

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Quiz
import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.dataseeding.model.QuizApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.student.R
import org.hamcrest.Matchers

class TodoPage: BasePage(R.id.todoPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)

    fun assertAssignmentDisplayed(assignment: AssignmentApiModel) {
        assertTextDisplayedInRecyclerView(assignment.name)
    }

    fun assertAssignmentDisplayed(assignment: Assignment) {
        assertTextDisplayedInRecyclerView(assignment.name!!)
    }

    fun assertQuizDisplayed(quiz: Quiz) {
        assertTextDisplayedInRecyclerView(quiz.title!!)
    }

    fun selectAssignment(assignment: Assignment) {
        assertTextDisplayedInRecyclerView(assignment.name!!)
        onView(withText(assignment.name!!)).click()
    }

    fun selectQuiz(quiz: Quiz) {
        assertTextDisplayedInRecyclerView(quiz.title!!)
        onView(withText(quiz.title!!)).click()
    }

    fun assertQuizDisplayed(quiz: QuizApiModel) {
        assertTextDisplayedInRecyclerView(quiz.title)
    }

    // Assert that a string is displayed somewhere in the RecyclerView
    private fun assertTextDisplayedInRecyclerView(s: String) {
        // Common matcher
        val matcher = ViewMatchers.withText(Matchers.containsString(s))

        // Scroll RecyclerView item into view, if necessary
        scrollRecyclerView(R.id.listView, matcher)

        // Now make sure that it is displayed
        Espresso.onView(matcher).assertDisplayed()
    }
}
