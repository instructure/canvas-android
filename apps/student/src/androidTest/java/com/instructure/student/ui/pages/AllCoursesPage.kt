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
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvasapi2.models.Course
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.pandautils.utils.isVisible
import com.instructure.student.R
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString

class AllCoursesPage : BasePage(R.id.all_courses_fragment_container) {
    private val toolbar by OnViewWithId(R.id.toolbar)
    private val emptyView by OnViewWithId(R.id.emptyView, autoAssert = false)
    private val listView by WaitForViewWithId(R.id.listView, autoAssert = false)

    fun assertDisplaysCourse(course: CourseApiModel) {
        // Odd to specify isDisplayed() when I'm about to assert that it is displayed,
        // but it serves to differentiate the "all courses" version of the course from
        // the "favorites" version of the course.  We'll select whichever is currently showing.
        val matcher = allOf(withText(course.name), withId(R.id.titleTextView), isDisplayed())
        scrollAndAssertDisplayed(matcher)
    }

    fun assertDisplaysCourse(course: Course) {
        //val matcher = allOf(withText(course.originalName!!), withId(R.id.titleTextView), isDisplayed())
        val matcher = allOf(withText(course.originalName!!), withId(R.id.titleTextView), withAncestor(R.id.all_courses_fragment_container))
        scrollAndAssertDisplayed(matcher)
    }


    fun assertDisplaysAllCourses() {
        emptyView.assertNotDisplayed()
        onView(withParent(R.id.toolbar) + withText(R.string.allCourses)).assertDisplayed()
        listView.assertDisplayed()
    }

    private fun scrollAndAssertDisplayed(matcher: Matcher<View>) {

        // The recycler view scrolling is buggy.  Hold off on scroll for now. :-(
//        // Scroll RecyclerView item into view, if necessary
//        onView(CoreMatchers.allOf(withId(R.id.listView), withAncestor(R.id.all_courses_fragment_container))) // There may be other listViews
//                .perform(RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(ViewMatchers.hasDescendant(matcher)))

        // Now make sure that it is displayed
        Espresso.onView(matcher).assertDisplayed() // Probably unnecessary
    }
}