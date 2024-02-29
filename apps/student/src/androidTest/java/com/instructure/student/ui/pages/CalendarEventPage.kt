/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.student.ui.pages

import com.instructure.espresso.page.BasePage

class CalendarEventPage : BasePage() {

    //TODO compose tests

    fun verifyTitle(title: String) {
        //onView(allOf(withParent(withId(R.id.toolbar)), containsTextCaseInsensitive(title))).assertDisplayed()
    }

    fun verifyDescription(description: String) {
        /*onWebView(withId(R.id.contentWebView) + withAncestor(R.id.calendarEventWebViewWrapper))
            .withElement(findElement(Locator.ID, "content"))
            .check(webMatches(getText(), Matchers.comparesEqualTo(description)))*/
    }
}