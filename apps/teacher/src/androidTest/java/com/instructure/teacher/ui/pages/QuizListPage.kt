/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.ui.pages

import com.instructure.canvasapi2.models.Quiz
import com.instructure.espresso.DoesNotExistAssertion
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.Searchable
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.onView
import com.instructure.espresso.pages.plus
import com.instructure.espresso.pages.waitForView
import com.instructure.espresso.pages.waitForViewWithText
import com.instructure.espresso.pages.withId
import com.instructure.espresso.pages.withText
import com.instructure.espresso.swipeDown
import com.instructure.espresso.waitForCheck
import com.instructure.teacher.R
import org.hamcrest.Matchers.allOf

/**
 * Represents the Quiz List Page.
 *
 * This page extends the BasePage class and provides functionality for interacting with the elements on the "Quiz List" page.
 * It contains properties for accessing various views on the page such as the quiz list toolbar, quiz recycler view, search button, search input, and empty panda view.
 * Additionally, it provides methods for asserting the display of the "No Quizzes" view, checking the presence of a quiz, clicking on a quiz, opening the search bar, entering a search query,
 * asserting the quiz count, and refreshing the page.
 */
class QuizListPage(val searchable: Searchable) : BasePage() {

    /**
     * The quiz list toolbar view on the page.
     */
    private val quizListToolbar by OnViewWithId(R.id.quizListToolbar)

    /**
     * The quiz recycler view on the page.
     */
    private val quizRecyclerView by OnViewWithId(R.id.quizRecyclerView)

    /**
     * The search button on the page.
     */
    private val searchButton by OnViewWithId(R.id.search)

    /**
     * The search input view on the page.
     */
    private val searchInput by WaitForViewWithId(androidx.appcompat.R.id.search_src_text)

    /**
     * The empty panda view displayed when the quiz list is empty.
     */
    private val emptyPandaView by WaitForViewWithId(R.id.emptyPandaView)

    /**
     * Asserts the display of the "No Quizzes" view.
     */
    fun assertDisplaysNoQuizzesView() {
        emptyPandaView.assertDisplayed()
    }

    /**
     * Asserts the presence of a quiz on the page.
     *
     * @param quiz The quiz object representing the quiz to be checked.
     */
    fun assertHasQuiz(quiz: Quiz) {
        waitForViewWithText(quiz.title!!).assertDisplayed()
    }

    /**
     * Asserts the presence of a quiz on the page.
     *
     * @param quizTitle The quiz title to check.
     */
    fun assertHasQuiz(quizTitle: String) {
        waitForView(withId(R.id.quizTitle) + withText(quizTitle)).assertDisplayed()
    }

    /**
     * Asserts the non-existence of a quiz on the page.
     *
     * @param quizTitle The quiz title to check.
     */
    fun assertQuizNotDisplayed(quizTitle: String) {
        onView(allOf(withText(quizTitle) + withId(R.id.quizTitle))).check(DoesNotExistAssertion(5))
    }

    /**
     * Clicks on a quiz.
     *
     * @param quiz The quiz object representing the quiz to be clicked.
     */
    fun clickQuiz(quiz: Quiz) {
        clickQuiz(quiz.title!!)
    }

    /**
     * Clicks on a quiz based on its title.
     *
     * @param quizTitle The title of the quiz to be clicked.
     */
    fun clickQuiz(quizTitle: String) {
        waitForViewWithText(quizTitle).click()
    }

    /**
     * Asserts the count of quizzes on the page.
     *
     * @param count The expected count of quizzes.
     */
    fun assertQuizCount(count: Int) {
        quizRecyclerView.waitForCheck(RecyclerViewItemCountAssertion(count + 1)) // +1 needed because we don't want to count the 'Assignment Quizzes' group label.
    }

    /**
     * Refreshes the page.
     */
    fun refresh() {
        onView(withId(R.id.swipeRefreshLayout)).swipeDown()
    }
}

