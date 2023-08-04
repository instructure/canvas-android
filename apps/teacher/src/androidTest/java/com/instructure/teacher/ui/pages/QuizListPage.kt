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

import androidx.test.espresso.action.ViewActions
import com.instructure.canvasapi2.models.Quiz
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.espresso.page.withId
import com.instructure.espresso.replaceText
import com.instructure.espresso.swipeDown
import com.instructure.espresso.waitForCheck
import com.instructure.teacher.R
import com.instructure.teacher.ui.interfaces.SearchablePage

/**
 * Represents the Quiz List Page.
 *
 * This page extends the BasePage class and provides functionality for interacting with the elements on the "Quiz List" page.
 * It contains properties for accessing various views on the page such as the quiz list toolbar, quiz recycler view, search button, search input, and empty panda view.
 * Additionally, it provides methods for asserting the display of the "No Quizzes" view, checking the presence of a quiz, clicking on a quiz, opening the search bar, entering a search query,
 * asserting the quiz count, and refreshing the page.
 */
class QuizListPage : BasePage(), SearchablePage {

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
     * Opens the search bar.
     */
    fun openSearch() {
        searchButton.click()
    }

    /**
     * Enters a search query in the search input.
     *
     * @param query The search query to be entered.
     */
    fun enterSearchQuery(query: String) {
        searchInput.perform(ViewActions.replaceText(query))
    }

    /**
     * Asserts the count of quizzes on the page.
     *
     * @param count The expected count of quizzes.
     */
    fun assertQuizCount(count: Int) {
        quizRecyclerView.waitForCheck(RecyclerViewItemCountAssertion(count))
    }

    /**
     * Refreshes the page.
     */
    fun refresh() {
        onView(withId(R.id.swipeRefreshLayout)).swipeDown()
    }

    /**
     * Clicks the search button.
     */
    override fun clickOnSearchButton() {
        onView(withId(R.id.search)).click()
    }

    /**
     * Types the specified search text into the search input field.
     *
     * @param textToType The text to be typed in the search input field.
     */
    override fun typeToSearchBar(textToType: String) {
        onView(withId(R.id.queryInput)).replaceText(textToType)
    }

    /**
     * Clicks the reset search text button.
     */
    override fun clickOnClearSearchButton() {
        waitForView(withId(R.id.clearButton)).click()
        onView(withId(R.id.backButton)).click()
    }

    /**
     * Presses the back button in the search view.
     */
    override fun pressSearchBackButton() {
        onView(withId(R.id.backButton)).click()
    }
}

