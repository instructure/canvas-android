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
package com.instructure.teacher.ui.pages.classic

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

/**
 * Represents the To Do Page.
 *
 * This page extends the BasePage class and provides functionality for interacting with the elements on the To Do page.
 * It contains methods for waiting for the page to render, asserting the display of To Do element details,
 * refreshing the page, asserting the presence of an empty view, asserting the needs grading count of a To Do element,
 * and asserting the count of To Do elements.
 */
class TodoPage : BasePage() {

    /**
     * Waits for the To Do page to render by asserting the display of the To Do toolbar.
     */
    fun waitForRender() {
        onView(withId(R.id.toDoToolbar)).assertDisplayed()
    }

    /**
     * Asserts that the details of a To Do element are displayed.
     *
     * @param courseName The name of the course associated with the To Do element.
     */
    fun assertTodoElementDetailsDisplayed(courseName: String, todoTitle: String) {
        onView(withId(R.id.toDoCourse) + withText(courseName) + hasSibling(withId(R.id.toDoTitle) + withText(todoTitle))).assertDisplayed()
        onView(withId(R.id.dueDate) + hasSibling(withId(R.id.toDoTitle) + withText(todoTitle))).assertDisplayed()
        onView(withId(R.id.toDoTitle) + withText(todoTitle)).assertDisplayed()
    }

    /**
     * Refreshes the To Do page by performing a swipe down action on the swipe refresh layout.
     */
    fun refresh() {
        onView(withId(R.id.swipeRefreshLayout)).swipeDown()
    }

    /**
     * Asserts that an empty view is displayed on the To Do page.
     */
    fun assertEmptyView() {
        onView(withId(R.id.emptyPandaView)).assertDisplayed()
    }

    /**
     * Asserts the needs grading count of a To Do element.
     *
     * @param todoTitle The title of the To Do element.
     * @param ungradedCount The number of ungraded items.
     */
    fun assertNeedsGradingCountOfTodoElement(todoTitle: String, ungradedCount: Int) {
        onView(withId(R.id.ungradedCount) + withText("$ungradedCount needs grading") + hasSibling(withId(R.id.toDoTitle) + withText(todoTitle))).assertDisplayed()
    }

    /**
     * Asserts the count of To Do elements on the To Do page.
     *
     * @param count The expected count of To Do elements.
     */
    fun assertTodoElementCount(count: Int) {
        onView(withId(R.id.toDoRecyclerView)).waitForCheck(RecyclerViewItemCountAssertion(count))
    }
}
