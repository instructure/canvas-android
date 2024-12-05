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

import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.assertSelected
import com.instructure.espresso.click
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.onView
import com.instructure.espresso.pages.plus
import com.instructure.espresso.pages.withAncestor
import com.instructure.espresso.pages.withId
import com.instructure.espresso.pages.withParent
import com.instructure.espresso.pages.withText
import com.instructure.espresso.scrollTo
import com.instructure.espresso.waitForCheck
import com.instructure.student.R
import org.hamcrest.CoreMatchers

class ElementaryDashboardPage : BasePage(R.id.elementaryDashboardPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val tabLayout by OnViewWithId(R.id.dashboardTabLayout)
    private val pager by OnViewWithId(R.id.dashboardPager)

    private val hamburgerButtonMatcher = CoreMatchers.allOf(withContentDescription(R.string.navigation_drawer_open), isDisplayed())

    fun selectTab(elementaryTabType: ElementaryTabType) {
        onView(withAncestor(R.id.dashboardTabLayout) + withText(elementaryTabType.tabType))
            .scrollTo()
            .click()
    }

    fun assertElementaryTabVisibleAndSelected(elementaryTabType: ElementaryTabType) {
        onView(withAncestor(R.id.dashboardTabLayout) + withText(elementaryTabType.tabType) + isDisplayed()).assertDisplayed()
        onView(withAncestor(R.id.dashboardTabLayout) + withText(elementaryTabType.tabType) + isDisplayed()).assertSelected()
    }

    fun waitForRender() {
        onView(hamburgerButtonMatcher).waitForCheck(matches(isDisplayed()))
    }

    fun openDrawer() {
        onView(hamburgerButtonMatcher).click()
    }

    fun assertToolbarTitle() {
        onView(withParent(R.id.toolbar) + withText(R.string.dashboard) + isDisplayed()).assertDisplayed()
    }

    fun clickOnBottomNavigationBarInbox() {
        onView(withId(R.id.bottomNavigationInbox)).click()
    }

    fun assertElementaryMenuItemsShownInDrawer() {
        onView(withText(R.string.files)).scrollTo().assertDisplayed()
        onView(withText(R.string.settings)).scrollTo().assertDisplayed()
        onView(withText(R.string.help)).scrollTo().assertDisplayed()
        onView(withText(R.string.changeUser)).scrollTo().assertDisplayed()
        onView(withText(R.string.logout)).scrollTo().assertDisplayed()
    }

    fun assertNotElementaryMenuItemsDontShowInDrawer() {
        onView(withText(R.string.showGrades)).assertNotDisplayed()
        onView(withText(R.string.colorOverlay)).assertNotDisplayed()
    }

    enum class ElementaryTabType(val tabType: Int) {
        HOMEROOM(R.string.dashboardTabHomeroom),
        SCHEDULE(R.string.dashboardTabSchedule),
        GRADES(R.string.dashboardTabGrades),
        RESOURCES(R.string.dashboardTabResources),
        IMPORTANT_DATES(R.string.dashboardTabImportantDates)
    }
}