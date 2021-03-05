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

import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.page.*
import com.instructure.espresso.swipeDown
import com.instructure.teacher.R

class TodoPage : BasePage() {

    fun waitForRender() {
        onView(withId(R.id.toDoToolbar)).assertDisplayed()
    }

    fun assertTodoElementIsDisplayed(courseName: String) {
        onView(withId(R.id.toDoCourse) + withText(courseName)).assertDisplayed()
    }

    fun refresh() {
        onView(withId(R.id.swipeRefreshLayout)).swipeDown()
    }

    fun assertEmptyView() {
        onView(withId(R.id.emptyPandaView)).assertDisplayed()
    }
}