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
package com.instructure.teacher.ui.pages

import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.web.assertion.WebViewAssertions
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.page.BasePage
import com.instructure.teacher.R
import org.hamcrest.Matchers

class CalendarEventPage : BasePage(R.id.fragmentCalendarEvent) {

    fun verifyTitle(title: String) {
        Espresso.onView(Matchers.allOf(ViewMatchers.withParent(ViewMatchers.withId(R.id.toolbar)), containsTextCaseInsensitive(title))).assertDisplayed()
    }

    fun verifyDescription(description: String) {
        Web.onWebView(ViewMatchers.withId(R.id.contentWebView))
            .withElement(DriverAtoms.findElement(Locator.ID, "content"))
            .check(WebViewAssertions.webMatches(DriverAtoms.getText(), Matchers.comparesEqualTo(description)))
    }
}