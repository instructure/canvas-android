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
package com.instructure.student.ui.pages.classic.k5

import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.web.assertion.WebViewAssertions
import androidx.test.espresso.web.sugar.Web
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.onViewWithId
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withDescendant
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.espresso.scrollTo
import com.instructure.espresso.swipeDown
import com.instructure.student.R
import org.hamcrest.Matchers

class ResourcesPage : BasePage(R.id.resourcesPage) {

    private val swipeRefreshLayout by OnViewWithId(R.id.resourcesSwipeRefreshLayout)
    private val importantLinksTitle by OnViewWithId(R.id.importantLinksTitle, autoAssert = false)
    private val importantLinksContainer by OnViewWithId(R.id.importantLinksContainer)
    private val coursesRecyclerView by OnViewWithId(R.id.actionItemsRecyclerView)

    fun assertImportantLinksAndWebContentDisplayed(content: String) {
        importantLinksTitle.scrollTo().assertDisplayed()
        Web.onWebView(withId(R.id.contentWebView))
            .withElement(DriverAtoms.findElement(Locator.TAG_NAME, "html"))
            .check(WebViewAssertions.webMatches(DriverAtoms.getText(), Matchers.comparesEqualTo(content)))
    }

    fun assertImportantLinksHeaderDisplayed() {
        importantLinksTitle.assertDisplayed()
    }

    fun assertCourseNameDisplayed(courseName: String) {
        onView(withId(R.id.importantLinksCourseName) + withText(courseName)).scrollTo().assertDisplayed()
    }

    fun assertStudentApplicationsHeaderDisplayed() {
        onView(withText(R.string.studentApplications)).assertDisplayed()
    }

    fun assertLtiToolDisplayed(name: String) {
        onView(withId(R.id.ltiAppCardView) + withDescendant(withText(name))).assertDisplayed()
    }

    fun assertStaffInfoHeaderDisplayed() {
        onView(withText(R.string.staffContactInfo)).scrollTo().assertDisplayed()
    }

    fun assertStaffDisplayed(name: String) {
        onView(withId(R.id.contactInfoLayout) + withDescendant(withText(name))).scrollTo().assertDisplayed()
    }

    fun assertImportantLinksNotDisplayed() {
        importantLinksTitle.assertNotDisplayed()
        importantLinksContainer.check(ViewAssertions.matches(ViewMatchers.hasChildCount(0)))
    }

    fun assertStaffInfoNotDisplayed() {
        onView(withText(R.string.staffContactInfo)).check(ViewAssertions.doesNotExist())
        onView(withId(R.id.contactInfoLayout)).check(ViewAssertions.doesNotExist())
    }

    fun assertStudentApplicationsNotDisplayed() {
        onView(withText(R.string.studentApplications)).check(ViewAssertions.doesNotExist())
        onView(withId(R.id.ltiAppCardView)).check(ViewAssertions.doesNotExist())
    }

    fun assertEmptyViewDisplayed() {
        onViewWithId(R.id.resourcesEmptyView).assertDisplayed()
        onViewWithText(R.string.resourcesEmptyMessage).assertDisplayed()
    }

    fun refresh() {
        swipeRefreshLayout.swipeDown()
    }

    fun openLtiApp(name: String) {
        onView(withId(R.id.ltiAppCardView) + withDescendant(withText(name))).click()
    }

    fun assertCourseShown(courseName: String) {
        onView(withText(courseName))
    }

    fun openComposeMessage(teacherName: String) {
        onView(withId(R.id.contactInfoLayout) + withDescendant(withText(teacherName))).scrollTo().click()
    }
}