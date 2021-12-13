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
import com.instructure.espresso.*
import com.instructure.espresso.page.*
import com.instructure.student.R
import org.hamcrest.CoreMatchers

class ElementaryDashboardPage : BasePage(R.id.elementaryDashboardPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)
    private val tabLayout by OnViewWithId(R.id.dashboardTabLayout)
    private val pager by OnViewWithId(R.id.dashboardPager)

    private val hamburgerButtonMatcher = CoreMatchers.allOf(withContentDescription(R.string.navigation_drawer_open), isDisplayed())

    fun assertToolbarTitle() {
        onView(withParent(R.id.toolbar) + withText(R.string.dashboard) + isDisplayed()).assertDisplayed()
    }

    fun clickInboxTab() {
        onView(withId(R.id.bottomNavigationInbox)).click()
    }

    fun selectHomeroomTab() {
        onView(withAncestor(R.id.dashboardTabLayout) + withText(R.string.dashboardTabHomeroom))
            .scrollTo()
            .click()
    }

    fun assertHomeroomTabVisibleAndSelected() {
        onView(withAncestor(R.id.dashboardTabLayout) + withText(R.string.dashboardTabHomeroom) + isDisplayed()).assertDisplayed()
        onView(withAncestor(R.id.dashboardTabLayout) + withText(R.string.dashboardTabHomeroom) + isDisplayed()).assertSelected()
    }

    fun selectScheduleTab() {
        onView(withAncestor(R.id.dashboardTabLayout) + withText(R.string.dashboardTabSchedule))
            .scrollTo()
            .click()
    }

    fun selectTab(elementaryTabType: ElementaryTabType) {
        when(elementaryTabType) {
            ElementaryTabType.HOMEROOM -> {
                onView(withAncestor(R.id.dashboardTabLayout) + withText(R.string.dashboardTabHomeroom))
                    .scrollTo()
                    .click()
            }

            ElementaryTabType.SCHEDULE -> {
                onView(withAncestor(R.id.dashboardTabLayout) + withText(R.string.dashboardTabSchedule))
                    .scrollTo()
                    .click()
            }

            ElementaryTabType.GRADES -> {
                onView(withAncestor(R.id.dashboardTabLayout) + withText(R.string.dashboardTabGrades))
                    .scrollTo()
                    .click()
            }

            ElementaryTabType.RESOURCES -> {
                onView(withAncestor(R.id.dashboardTabLayout) + withText(R.string.dashboardTabResources))
                    .scrollTo()
                    .click()
            }
        }
    }

    fun assertScheduleTabVisibleAndSelected() {
        onView(withAncestor(R.id.dashboardTabLayout) + withText(R.string.dashboardTabSchedule) + isDisplayed()).assertDisplayed()
        onView(withAncestor(R.id.dashboardTabLayout) + withText(R.string.dashboardTabSchedule) + isDisplayed()).assertSelected()
    }

    fun selectGradesTab() {
        onView(withAncestor(R.id.dashboardTabLayout) + withText(R.string.dashboardTabGrades))
            .scrollTo()
            .click()
    }

    fun assertGradesTabVisibleAndSelected() {
        onView(withAncestor(R.id.dashboardTabLayout) + withText(R.string.dashboardTabGrades) + isDisplayed()).assertDisplayed()
        onView(withAncestor(R.id.dashboardTabLayout) + withText(R.string.dashboardTabGrades) + isDisplayed()).assertSelected()
    }

    fun selectResourcesTab() {
        onView(withAncestor(R.id.dashboardTabLayout) + withText(R.string.dashboardTabResources))
            .scrollTo()
            .click()
    }

    fun assertResourcesTabVisibleAndSelected() {
        onView(withAncestor(R.id.dashboardTabLayout) + withText(R.string.dashboardTabResources) + isDisplayed()).assertDisplayed()
        onView(withAncestor(R.id.dashboardTabLayout) + withText(R.string.dashboardTabResources) + isDisplayed()).assertSelected()
    }

    fun waitForRender() {
        onView(hamburgerButtonMatcher).waitForCheck(matches(isDisplayed()))
    }

    fun openDrawer() {
        onView(hamburgerButtonMatcher).click()
    }

    fun assertElementaryMenuItemsShownInDrawer() {
        onView(withText(R.string.files)).assertDisplayed()
        onView(withText(R.string.settings)).assertDisplayed()
        onView(withText(R.string.help)).assertDisplayed()
        onView(withText(R.string.changeUser)).assertDisplayed()
        onView(withText(R.string.logout)).assertDisplayed()
    }

    fun assertNotElementaryMenuItemsDontShowInDrawer() {
        onView(withText(R.string.showGrades)).assertNotDisplayed()
        onView(withText(R.string.colorOverlay)).assertNotDisplayed()
    }

    enum class ElementaryTabType(val tabType: Int) {
        HOMEROOM(R.string.dashboardTabHomeroom),
        SCHEDULE(R.string.dashboardTabSchedule),
        GRADES(R.string.dashboardTabGrades),
        RESOURCES(R.string.dashboardTabResources)
    }
}