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
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.scrollRecyclerView
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Tab
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.espresso.TextViewColorAssertion
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertHasText
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.scrollTo
import com.instructure.espresso.swipeUp
import com.instructure.pandautils.views.SwipeRefreshLayoutAppBar
import com.instructure.student.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.not

open class CourseBrowserPage : BasePage(R.id.courseBrowserPage) {

    private val initialBrowserTitle by WaitForViewWithId(R.id.courseBrowserTitle)

    fun selectAssignments() {
        val matcher = withText("Assignments")
        selectSection(matcher)
    }

    fun selectPeople() {
        val matcher = withText("People")
        selectSection(matcher)
    }

    fun selectModules() {
        val matcher = allOf(withText("Modules"), withId(R.id.label))
        selectSection(matcher)
    }

    fun selectDiscussions() {
        val matcher = withText("Discussions")
        selectSection(matcher)
    }

    fun selectSyllabus() {
        val matcher = withText("Syllabus")
        selectSection(matcher)
    }

    fun selectPages() {
        val matcher = withText("Pages")
        selectSection(matcher)
    }

    fun selectQuizzes() {
        val matcher = withText("Quizzes")
        selectSection(matcher)
    }

    fun selectAnnouncements() {
        val matcher = withText("Announcements")
        selectSection(matcher)
    }

    fun selectGrades() {
        val matcher = withText("Grades")
        selectSection(matcher)
    }

    fun selectConferences() {
        val matcher = withText("BigBlueButton")
        selectSection(matcher)
    }

    fun selectCollaborations() {
        val matcher = withText("Collaborations")
        selectSection(matcher)
    }

    fun selectFiles() {
        val matcher = allOf(withText("Files"), withId(R.id.label))
        selectSection(matcher)
    }

    fun selectHome() {
        onView(allOf(withId(R.id.homeLabel), isDisplayed())).click()
    }

    private fun selectSection(matcher: Matcher<View>) {
        // Scroll RecyclerView item into view, if necessary
        recyclerViewScrollTo(matcher)
        onView(matcher).click()
    }

    fun clickOnSmartSearch() {
        onView(withId(R.id.searchBar) + withAncestor(R.id.courseBrowserPage)).click()
    }

    fun assertTitleCorrect(course: Course) {
        // You might have multiple of these if you navigate from one course to another.
        // In that event, we'll have to choose the one that is displayed.
        onView(allOf(withId(R.id.courseBrowserTitle), isDisplayed())).assertHasText(course.originalName!!)
    }

    fun assertInitialBrowserTitle(course: CourseApiModel) {
        initialBrowserTitle.assertHasText(course.name)
    }

    fun assertTabDisplayed(tab: Tab) {
        assertTabDisplayed(tab.label!!)
    }

    fun assertTabLabelTextColor(tabTitle: String, expectedColor: String) {
        onView(withText(tabTitle)).check(TextViewColorAssertion(expectedColor))
    }

    fun assertTabDisplayed(tabTitle: String) {
        recyclerViewScrollTo(allOf(withText(tabTitle),withId(R.id.label)))
    }

    fun assertTabNotDisplayed(tabTitle: String) {
        onView(allOf(withText(tabTitle), withId(R.id.label))).check(doesNotExist())
    }

    //OfflineMethod
    fun assertTabDisabled(tabTitle: String) {
        onView(allOf(anyOf(isAssignableFrom(LinearLayout::class.java), isAssignableFrom(ConstraintLayout::class.java)), withChild(anyOf(withId(R.id.label), withId(R.id.unsupportedLabel)) + withText(tabTitle)))).scrollTo().check(matches(not(isEnabled())))
    }

    fun assertTabEnabled(tabTitle: String) {
        onView(allOf(anyOf(isAssignableFrom(LinearLayout::class.java), isAssignableFrom(ConstraintLayout::class.java)), withChild(anyOf(withId(R.id.label), withId(R.id.unsupportedLabel)) + withText(tabTitle)))).scrollTo().check(matches(isEnabled()))
    }

    // Minimizes toolbar if it is not already minimized
    private fun minimizeToolbar() {
        try {
            onView(allOf(withId(R.id.collapsingToolbarLayout), isDisplayed())).swipeUp()
        }
        catch(pe: PerformException) {
            // Eat this exception.  It will occur if the toolbar is already minimized.
        }
    }

    fun refresh() {
        // This gets our view in a state where it can be refreshed via pull-to-refresh.
        onView(allOf(withId(R.id.swipeRefreshLayout), isDisplayed()))
                .perform(ScrollRefreshLayoutToTop())

        // Swiping on swipeRefreshLayout normally doesn't work because it is not 90%
        // visible (thanks to the expanded toolbar).  So we'll call it with  a custom constraint.
        //
        // Also, depending on the current state of the toolbar (inflated or minimized), we may
        // need either one or two swipe-downs to effect a refresh.  We'll go with two to cover
        // our bases.
        onView(allOf(withId(R.id.swipeRefreshLayout), isDisplayed()))
                .perform(withCustomConstraints(ViewActions.swipeDown(), isDisplayingAtLeast(5)))
                .perform(withCustomConstraints(ViewActions.swipeDown(), isDisplayingAtLeast(5)))
    }

    // When the toolbar is maximized, you can't  do any operations with the recyclerView
    // because it is not 90% displayed.  So we funnel all scroll ops through this method, which
    // minimizes the toolbar before scrolling.
    private fun recyclerViewScrollTo(matcher: Matcher<View>) {
        minimizeToolbar()
        scrollRecyclerView(R.id.courseBrowserRecyclerView, matcher)
    }
}

// Custom action to scroll to top of SwipeRefreshLayoutAppBar
private class ScrollRefreshLayoutToTop : ViewAction {
    override fun getDescription(): String {
        return "Scroll to the top of a SwipeRefreshLayoutAppBar"
    }

    override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(SwipeRefreshLayoutAppBar::class.java)
    }

    override fun perform(uiController: UiController?, view: View?) {
        when(view) {
            is SwipeRefreshLayoutAppBar -> {
                view.scrollTo(0,0)
            }
        }
    }

}

