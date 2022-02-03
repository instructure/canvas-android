/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.student.ui.pages

import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.*
import com.instructure.espresso.swipeDown
import com.instructure.student.R


class ImportantDatesPage : BasePage(R.id.importantDatesPage) {

    fun assertItemDisplayed(itemName: String) {
        waitForView(withAncestor(R.id.importantDatesRecyclerView) + withText(itemName)).assertDisplayed()
    }

    fun assertEmptyViewDisplayed() {
        onView(withId(R.id.importantDatesEmptyView)).assertDisplayed()
    }

    fun pullToRefresh() {
        onView(withId(R.id.importantDatesRecyclerView)).swipeDown()
    }

    fun clickImportantDatesItem(title: String) {
        waitForView(withAncestor(R.id.importantDatesRecyclerView) + withText(title)).click()
    }
}