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
package com.instructure.teacher.ui.rendertests.renderpages

import androidx.test.espresso.action.ViewActions
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.teacher.R
import com.instructure.teacher.ui.pages.classic.SyllabusPage
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers

class SyllabusRenderPage : SyllabusPage() {

    private val tabs by OnViewWithId(R.id.syllabusTabLayout)
    private val webView by WaitForViewWithId(R.id.contentWebView)
    private val eventsRecycler by WaitForViewWithId(R.id.syllabusEventsRecyclerView)
    private val eventsEmpty by WaitForViewWithId(R.id.syllabusEmptyView)
    private val eventsError by WaitForViewWithId(R.id.syllabusEventsError)
    private val editIcon by WaitForViewWithId(R.id.menu_edit)

    fun assertDisplaysToolbarText(text: String) {
        findChildTextInToolbar(text).assertDisplayed()
    }

    private fun findChildTextInToolbar(text: String) = onView(Matchers.allOf(withText(text), withParent(R.id.toolbar)))

    fun assertDoesNotDisplaySyllabus() {
        tabs.assertNotDisplayed()
        webView.assertNotDisplayed()
    }

    fun assertDisplaysEmpty() {
        eventsEmpty.assertDisplayed()
    }

    fun assertDisplaysError() {
        eventsError.assertDisplayed()
    }

    fun assertDisplaysEvents() {
        eventsRecycler.assertDisplayed()
    }

    fun clickEventsTab() {
        onView(CoreMatchers.allOf(withAncestor(R.id.syllabusTabLayout), withText("Summary"))).click()
    }

    fun swipeToEventsTab() {
        onView(withId(R.id.syllabusPager)).perform(ViewActions.swipeLeft())
    }

    fun swipeToSyllabusTab() {
        onView(withId(R.id.syllabusPager)).perform(ViewActions.swipeRight())
    }

    fun assertDisplayEditIcon() {
        editIcon.assertDisplayed()
    }
}