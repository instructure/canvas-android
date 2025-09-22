/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.ui.pages

import com.instructure.canvasapi2.models.User
import com.instructure.espresso.page.BasePage
import com.instructure.parentapp.R
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.text.KTextView


class DashboardPage : BasePage(R.id.drawer_layout) {

    private val toolbar = KView { withId(R.id.toolbar) }
    private val bottomNavigationView = KView { withId(R.id.bottom_nav) }
    private val alertsItem = KView { withId(R.id.alerts) }
    private val calendarItem = KView { withId(R.id.calendar) }

    fun waitForRender() {
        KView { withId(R.id.navigationButtonHolder) }.isDisplayed()
    }

    fun assertObserverData(user: User) {
        KTextView { withText(user.name) }.isDisplayed()
        KTextView { withText(user.email.orEmpty()) }.isDisplayed()
    }

    fun openLeftSideMenu() {
        KView { withId(R.id.navigationButtonHolder) }.click()
    }

    fun assertSelectedStudent(name: String) {
        KTextView {
            withText(name)
            isDescendantOfA { withId(R.id.selected_student_container) }
        }.isDisplayed()
    }

    fun openStudentSelector() {
        toolbar.click()
    }

    fun selectStudent(name: String) {
        KTextView {
            withText(name)
            isDescendantOfA { withId(R.id.student_list) }
        }.click()
    }

    fun clickAddStudent() {
        KView { withContentDescription(R.string.a11y_addStudentContentDescription) }.click()
    }

    fun assertAddStudentDisplayed() {
        KTextView {
            withText(R.string.a11y_addStudentContentDescription)
            isDescendantOfA { withId(R.id.student_list) }
        }.isDisplayed()
    }


    fun clickInbox() {
        KTextView { withText(R.string.inbox) }.click()
    }

    fun clickAlertsBottomMenu() {
        alertsItem.click()
    }

    fun clickCalendarBottomMenu() {
        calendarItem.click()
    }

    fun clickTodayButton() {
        KView { withId(R.id.todayButtonHolder) }.click()
    }

}
