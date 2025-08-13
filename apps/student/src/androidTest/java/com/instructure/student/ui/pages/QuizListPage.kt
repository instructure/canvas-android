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
package com.instructure.student.ui.pages

import android.view.View
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.canvasapi2.models.Quiz
import com.instructure.dataseeding.model.QuizApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.Searchable
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.espresso.replaceText
import com.instructure.student.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf

class QuizListPage(val searchable: Searchable) : BasePage(R.id.quizListPage) {

    /**
     * The search button on the page.
     */
    private val searchButton by OnViewWithId(R.id.search)

    /**
     * The search input view on the page.
     */
    private val searchInput by WaitForViewWithId(androidx.appcompat.R.id.search_src_text)


    fun openSearchBar() {
        searchButton.click()
    }

    fun enterSearchQuery(query: String) {
        searchInput.click()
        searchInput.replaceText(query)
    }

    fun clearSearchButton(){
        searchable.clickOnClearSearchButton()
    }

    fun assertNoQuizzesTextDisplayed() {
        onView(withText("No Quizzes"))
            .check(matches(isDisplayed()))
    }

    fun assertNoQuizDisplayed() {
        onView(allOf(withId(R.id.emptyView), isDisplayed())).assertDisplayed()
    }

    fun assertQuizDisplayed(quiz: QuizApiModel) {
        assertMatcherDisplayed(allOf(withId(R.id.title), withText(quiz.title)))
    }

    fun assertQuizDisplayed(quiz: Quiz) {
        assertMatcherDisplayed(allOf(withId(R.id.title), withText(quiz.title)))
    }

    fun assertQuizItemCount(count: Int) {
        onView(withId(R.id.listView) + withAncestor(R.id.quizListPage)).check(RecyclerViewItemCountAssertion(count + 1))
    }

    fun selectQuiz(quiz: QuizApiModel) {
        clickMatcher(allOf(withId(R.id.title), withText(quiz.title)))
    }

    fun selectQuiz(quiz: Quiz) {
        clickMatcher(allOf(withId(R.id.title), withText(quiz.title)))
    }

    fun assertQuizNotDisplayed(quiz: QuizApiModel) {
        onView(withText(quiz.title)).check(doesNotExist())
    }

    fun refresh() {
        onView(allOf(withId(R.id.swipeRefreshLayout), isDisplayed()))
            .perform(withCustomConstraints(swipeDown(), isDisplayingAtLeast(10)))
    }

    fun assertPointsDisplayed(points: String?) {
        assertMatcherDisplayed(allOf(withId(R.id.points), withText(points)))
    }

    fun assertPointsNotDisplayed() {
        onView(withId(R.id.points)).assertNotDisplayed()
    }

    private fun clickMatcher(matcher: Matcher<View>) {
        scrollRecyclerView(R.id.listView, matcher)
        onView(matcher).click()
    }

    private fun assertMatcherDisplayed(matcher: Matcher<View>) {
        scrollRecyclerView(R.id.listView, matcher)
        onView(matcher).assertDisplayed()
    }
}
