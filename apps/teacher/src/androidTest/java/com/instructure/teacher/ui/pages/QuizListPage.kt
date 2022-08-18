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
import com.instructure.espresso.*
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.waitForViewWithText
import com.instructure.espresso.page.withId
import com.instructure.teacher.R

class QuizListPage : BasePage() {
    private val quizListToolbar by OnViewWithId(R.id.quizListToolbar)

    private val quizRecyclerView by OnViewWithId(R.id.quizRecyclerView)

    private val searchButton by OnViewWithId(R.id.search)

    private val searchInput by WaitForViewWithId(androidx.appcompat.R.id.search_src_text)

    //Only displayed when assignment list is empty
    private val emptyPandaView by WaitForViewWithId(R.id.emptyPandaView)

    fun assertDisplaysNoQuizzesView() {
        emptyPandaView.assertDisplayed()
    }

    fun assertHasQuiz(quiz: Quiz) {
        waitForViewWithText(quiz.title!!).assertDisplayed()
    }

    fun clickQuiz(quiz: Quiz) {
        clickQuiz(quiz.title!!)
    }

    fun clickQuiz(quizTitle: String) {
        waitForViewWithText(quizTitle).click()
    }

    fun openSearch() {
        searchButton.click()
    }

    fun enterSearchQuery(query: String) {
        searchInput.perform(ViewActions.replaceText(query))
    }

    fun assertQuizCount(count: Int) {
        quizRecyclerView.waitForCheck(RecyclerViewItemCountAssertion(count))
    }

    fun refresh() {
        onView(withId(R.id.swipeRefreshLayout)).swipeDown()
    }
}
