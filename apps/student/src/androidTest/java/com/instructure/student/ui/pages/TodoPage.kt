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

import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Quiz
import com.instructure.dataseeding.model.AssignmentApiModel
import com.instructure.dataseeding.model.QuizApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.espresso.scrollTo
import com.instructure.student.R
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import java.lang.Thread.sleep

class TodoPage: BasePage(R.id.todoPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)

    fun assertAssignmentDisplayed(assignment: AssignmentApiModel) {
        assertTextDisplayedInRecyclerView(assignment.name)
    }

    fun assertAssignmentDisplayedWithRetries(assignment: AssignmentApiModel, retryAttempt: Int) {

        run assignmentDisplayedRepeat@{
            repeat(retryAttempt) {
                try {
                    sleep(3000)
                    assertTextDisplayedInRecyclerView(assignment.name)
                    return@assignmentDisplayedRepeat
                } catch (e: AssertionError) {
                    println("Attempt failed. The '${assignment.name}' assignment is not displayed, probably because of the API slowness.")
                }
            }
        }
    }

    fun assertAssignmentNotDisplayed(assignment: AssignmentApiModel) {
        onView(withText(assignment.name)).check(doesNotExist())
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
        onView(withText(R.string.favoritedCoursesLabel) + withParent(R.id.select_dialog_listview)).click()
        onView(withText(android.R.string.ok)).click()
    }

    fun clearFilter() {
        onView(withId(R.id.clearFilterTextView)).click()
    }

    fun assertEmptyView() {
        onView(withId(R.id.emptyView) + withAncestor(withId(R.id.todoPage))).assertDisplayed()
        onView(withText(R.string.noTodos) + withId(R.id.title)).assertDisplayed()
    }

    fun assertFavoritedCoursesFilterHeader() {
        onView(allOf(withId(R.id.todoFilterTitle), withText(R.string.favoritedCoursesLabel), withParent(R.id.todoFilterContainer))).assertDisplayed()
        onView(allOf(withId(R.id.clearFilterTextView), withText(R.string.clearFilter), withParent(R.id.todoFilterContainer))).assertDisplayed()
    }

    // Assert that a string is displayed somewhere in the RecyclerView
    private fun assertTextDisplayedInRecyclerView(s: String) {
        // Common matcher
        val matcher = withText(Matchers.containsString(s))

        // Now make sure that it is displayed
        onView(matcher).scrollTo().assertDisplayed()
    }
}
