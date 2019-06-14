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
@file:Suppress("unused")

package com.instructure.student.ui.pages

import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.espresso.OnViewWithContentDescription
import com.instructure.student.R
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.*

class DashboardPage : BasePage(R.id.dashboardPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val emptyView by OnViewWithId(R.id.emptyCoursesView, autoAssert = false)
    private val listView by WaitForViewWithId(R.id.listView, autoAssert = false)
    private val hamburgerButton by OnViewWithContentDescription(R.string.navigation_drawer_open)

    fun assertDisplaysCourses() {
        emptyView.assertNotDisplayed()
        onView(withParent(R.id.toolbar) + withText(R.string.dashboard)).assertDisplayed()
        listView.assertDisplayed()
        onViewWithText("Courses").assertDisplayed()
        onViewWithText("See All").assertDisplayed()
    }

    fun assertDisplaysAddCourseMessage() {
        emptyView.assertDisplayed()
        onViewWithText(R.string.welcome).assertDisplayed()
        onViewWithText(R.string.emptyCourseListMessage).assertDisplayed()
        onViewWithId(R.id.addCoursesButton).assertDisplayed()
    }

    fun signOut() {
        hamburgerButton.click()
        onViewWithId(R.id.navigationDrawerItem_logout).click()
        onViewWithText(android.R.string.yes).click()
    }

    fun pressChangeUser() {
        hamburgerButton.click()
        onViewWithId(R.id.navigationDrawerItem_changeUser).click()
    }

    fun assertUserLoggedIn(user: CanvasUserApiModel) {
        hamburgerButton.click()
        onViewWithText(user.shortName).assertDisplayed()
        Espresso.pressBack()
    }

    fun waitForRender() {
        listView.assertDisplayed() // Oddly, this seems sufficient as a wait-for-render mechanism
    }
}
