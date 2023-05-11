/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.emeritus.student.ui.pages

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.canvasapi2.models.Quiz
import com.instructure.dataseeding.model.QuizApiModel
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.emeritus.student.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf

class QuizListPage : BasePage(R.id.quizListPage) {
    fun assertQuizDisplayed(quiz: QuizApiModel) {
        assertMatcherDisplayed(allOf(withId(R.id.title), withText(quiz.title)))
    }

    fun assertQuizDisplayed(quiz: Quiz) {
        assertMatcherDisplayed(allOf(withId(R.id.title), withText(quiz.title)))
    }

    fun selectQuiz(quiz: QuizApiModel) {
        clickMatcher(allOf(withId(R.id.title), withText(quiz.title)))
    }

    fun selectQuiz(quiz: Quiz) {
        clickMatcher(allOf(withId(R.id.title), withText(quiz.title)))
    }

    private fun clickMatcher(matcher: Matcher<View>) {
        scrollRecyclerView(R.id.listView, matcher)
        onView(matcher).click()
    }

    private fun assertMatcherDisplayed(matcher: Matcher<View>) {
        scrollRecyclerView(R.id.listView, matcher)
        onView(matcher).assertDisplayed()
    }

    fun assertQuizNotDisplayed(quiz: QuizApiModel) {
        onView(withText(quiz.title)).check(doesNotExist())
    }

    fun refresh() {
        onView(allOf(withId(R.id.swipeRefreshLayout), isDisplayed()))
                .perform(withCustomConstraints(swipeDown(), isDisplayingAtLeast(10)))
    }
}