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
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withParent
import com.instructure.teacher.R
import org.hamcrest.Matchers

/**
 * Represents a page displaying a calendar event.
 *
 * This class extends the `BasePage` class and provides methods for verifying the title and description
 * of the calendar event.
 *
 * @param pageResourceId The resource ID of the calendar event page.
 * @constructor Creates an instance of the `CalendarEventPage` class.
 */
class CalendarEventPage : BasePage(R.id.fragmentCalendarEvent) {

    /**
     * Verifies that the title of the calendar event matches the specified title.
     *
     * @param title The expected title of the calendar event.
     * @throws AssertionError if the title does not match the expected title.
     */
    fun verifyTitle(title: String) {
        Espresso.onView(Matchers.allOf(ViewMatchers.withParent(ViewMatchers.withId(R.id.toolbar)), containsTextCaseInsensitive(title))).assertDisplayed()
    }

    /**
     * Verifies that the description of the calendar event matches the specified description.
     *
     * @param description The expected description of the calendar event.
     * @throws AssertionError if the description does not match the expected description.
     */
    fun verifyDescription(description: String) {
        Web.onWebView(ViewMatchers.withId(R.id.contentWebView) + withParent(R.id.calendarEventWebViewWrapper))
            .withElement(DriverAtoms.findElement(Locator.ID, "content"))
            .check(WebViewAssertions.webMatches(DriverAtoms.getText(), Matchers.comparesEqualTo(description)))
    }
}