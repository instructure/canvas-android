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
package com.instructure.teacher.ui.renderTests.pages

import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.teacher.R
import org.hamcrest.CoreMatchers.allOf

class CalendarEventRenderPage : BasePage(R.id.fragmentCalendarEvent) {

    private val dateIcon by OnViewWithId(R.id.dateIcon)
    private val dateTitle by OnViewWithId(R.id.dateTitle)
    private val dateSubtitle by OnViewWithId(R.id.dateSubtitle)
    private val locationIcon by OnViewWithId(R.id.locationIcon)
    private val locationTitle by OnViewWithId(R.id.locationTitle)
    private val locationSubtitle by OnViewWithId(R.id.locationSubtitle)
    private val webView by WaitForViewWithId(R.id.contentWebView)

    fun assertDisplaysToolbarText(text: String) {
        findChildTextInToolbar(text).assertDisplayed()
    }

    private fun findChildTextInToolbar(text: String) = onView(allOf(withText(text), withParent(R.id.toolbar)))
}