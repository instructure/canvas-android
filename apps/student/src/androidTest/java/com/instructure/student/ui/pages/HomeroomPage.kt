/*
 * Copyright (C) 2021 - present Instructure, Inc.
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

import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.web.assertion.WebViewAssertions
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.student.R
import org.hamcrest.Matchers

class HomeroomPage : BasePage(R.id.homeroomPage) {

    private val swipeRefreshLayout by OnViewWithId(R.id.homeroomSwipeRefreshLayout)
    private val welcomeText by OnViewWithId(R.id.welcomeText)
    private val announcementsContainer by OnViewWithId(R.id.announcementsContainer)
    private val mySubjectsTitle by OnViewWithId(R.id.mySubjectsTitle)
    private val coursesRecyclerView by OnViewWithId(R.id.coursesRecyclerView)
    private val noSubjectsText by OnViewWithId(R.id.noSubjectsText, autoAssert = false)

    fun assertWelcomeText(studentName: String) {
        welcomeText.assertHasText(getStringFromResource(R.string.homeroomWelcomeMessage, studentName))
    }

    fun assertAnnouncementDisplayed(courseName: String, title: String, content: String) {
        onView(withAncestor(R.id.announcementsContainer) + withText(courseName)).assertDisplayed()
        onView(withAncestor(R.id.announcementsContainer) + withText(title)).assertDisplayed()

        Web.onWebView()
                .withElement(DriverAtoms.findElement(Locator.TAG_NAME, "html"))
                .check(WebViewAssertions.webMatches(DriverAtoms.getText(), Matchers.comparesEqualTo(content)))

        onView(withAncestor(R.id.announcementsContainer) + withText(R.string.viewPreviousAnnouncements))
                .scrollTo()
                .assertDisplayed()
    }

    fun assertAnnouncementNotDisplayed() {
        announcementsContainer.check(ViewAssertions.matches(ViewMatchers.hasChildCount(0)))
    }

    fun assertCourseItemsCount(coursesCount: Int) {
        coursesRecyclerView.check(RecyclerViewItemCountAssertion(coursesCount))
    }

    fun assertCourseDisplayed(courseName: String, todoText: String, announcementText: String) {
        val titleMatcher = withId(R.id.courseNameText) + withText(courseName)
        val todoTextMatcher = withId(R.id.todoText) + withText(todoText)
        val announcementMatcher = withId(R.id.announcementText) + withText(announcementText)

        onView(withId(R.id.cardView) + withDescendant(titleMatcher) + withDescendant(todoTextMatcher) + withDescendant(announcementMatcher))
                .scrollTo()
                .assertDisplayed()
    }

    fun assertNoSubjectsTextDisplayed() {
        noSubjectsText
                .scrollTo()
                .assertDisplayed()
                .assertHasText(R.string.homeroomNoSubjects)
    }

    fun assertHomeroomContentNotDisplayed() {
        onViewWithId(R.id.homeroomContent).assertNotDisplayed()
    }

    fun assertEmptyViewDisplayed() {
        onViewWithId(R.id.emptyView).assertDisplayed()
        onViewWithText(R.string.homeroomEmptyTitle).assertDisplayed()
        onViewWithText(R.string.homeroomEmptyMessage).assertDisplayed()
    }

    fun refresh() {
        swipeRefreshLayout.swipeDown()
    }

    fun clickOnViewPreviousAnnouncements() {
        onViewWithId(R.id.viewPreviousAnnouncements)
                .click()
    }

    fun openCourseAnnouncement(announcementText: String) {
        swipeRefreshLayout.swipeUp()
        onView(withId(R.id.announcementText) + withText(announcementText))
                .click()
    }

    fun openCourse(courseName: String) {
        onView(withId(R.id.courseNameText) + withText(courseName))
                .scrollTo()
                .click()
    }

    fun assertToDoText(todoText: String) {
        onView(withId(R.id.todoText) + withText(todoText))
                .scrollTo()
                .assertDisplayed()
    }

    fun openAssignments(todoText: String) {
        swipeRefreshLayout.swipeUp()
        onView(withId(R.id.todoText) + withText(todoText))
                .click()
    }
}