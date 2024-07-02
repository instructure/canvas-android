/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.parentapp.ui.pages

import com.instructure.canvasapi2.models.User
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.onViewWithContentDescription
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withText
import com.instructure.parentapp.R

class DashboardPage : BasePage(R.id.drawer_layout) {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val bottomNavigationView by OnViewWithId(R.id.bottom_nav)

    fun assertObserverData(user: User) {
        onViewWithText(user.name).assertDisplayed()
        onViewWithText(user.email.orEmpty()).assertDisplayed()
    }

    fun openNavigationDrawer() {
        onViewWithContentDescription(R.string.navigation_drawer_open).click()
    }

    fun assertSelectedStudent(name: String) {
        onView(withText(name) + withAncestor(R.id.selected_student_container)).assertDisplayed()
    }

    fun openStudentSelector() {
        toolbar.click()
    }

    fun selectStudent(name: String) {
        onView(withText(name) + withAncestor(R.id.student_list)).click()
    }
}
