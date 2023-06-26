/*
 * Copyright (C) 2020 - present Instructure, Inc.
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

import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import com.instructure.espresso.RecyclerViewItemCountAssertion
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.espresso.swipeDown
import com.instructure.espresso.waitForCheck
import com.instructure.teacher.R

class TodoPage : BasePage() {

    fun waitForRender() {
        onView(withId(R.id.toDoToolbar)).assertDisplayed()
    }

    fun assertTodoElementDetailsDisplayed(courseName: String) {
        onView(withId(R.id.toDoCourse) + withText(courseName)).assertDisplayed()
        onView(withId(R.id.dueDate)).assertDisplayed()
        onView(withId(R.id.toDoTitle)).assertDisplayed()
    }

    fun refresh() {
        onView(withId(R.id.swipeRefreshLayout)).swipeDown()
    }

    fun assertEmptyView() {
        onView(withId(R.id.emptyPandaView)).assertDisplayed()
    }

    fun assertNeedsGradingCountOfTodoElement(todoTitle: String, ungradedCount: Int) {
        onView(withId(R.id.ungradedCount) + withText("$ungradedCount needs grading") + hasSibling(withId(R.id.toDoTitle) + withText(todoTitle))).assertDisplayed()
    }

    fun assertTodoElementCount(count: Int) {
        onView(withId(R.id.toDoRecyclerView)).waitForCheck(RecyclerViewItemCountAssertion(count))
    }
}