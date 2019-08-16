/*
 * Copyright (C) 2019 - present Instructure, Inc.
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

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Tab
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertHasText
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.student.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf

class CourseBrowserPage : BasePage(R.id.courseBrowserPage) {

    private val initialBrowserTitle by WaitForViewWithId(R.id.courseBrowserTitle)

    fun selectAssignments() {
        val matcher = withText("Assignments")
        selectSection(matcher)
    }

    private fun selectSection(matcher: Matcher<View>) {
        // Scroll RecyclerView item into view, if necessary
        onView(allOf(withId(R.id.courseBrowserRecyclerView), isDisplayed()))
                .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(hasDescendant(matcher)))

        onView(matcher).click()
    }

    fun assertTabPresent(tab: Tab) {
        onView(allOf(withId(R.id.courseBrowserRecyclerView), isDisplayed()))
                .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(hasDescendant(withText(tab.label))))
    }

    fun assertTitleCorrect(course: Course) {
        initialBrowserTitle.assertHasText(course.originalName!!)
    }
}