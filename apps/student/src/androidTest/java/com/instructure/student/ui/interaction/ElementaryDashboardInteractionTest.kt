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
package com.instructure.student.ui.interaction

import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.student.ui.pages.classic.k5.ElementaryDashboardPage
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.extensions.tokenLoginElementary
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class ElementaryDashboardInteractionTest : StudentTest() {

    override fun displaysPageObjects() = Unit

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testNavigateToElementaryDashboard() {
        // User should be able to tap and navigate to dashboard page
        goToElementaryDashboard(courseCount = 1, favoriteCourseCount = 1)
        elementaryDashboardPage.assertPageObjects()
        elementaryDashboardPage.clickOnBottomNavigationBarInbox()
        dashboardPage.goToDashboard()
        elementaryDashboardPage.assertToolbarTitle()
        elementaryDashboardPage.assertElementaryTabVisibleAndSelected(ElementaryDashboardPage.ElementaryTabType.HOMEROOM)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testTabsNavigation() {
        goToElementaryDashboard(courseCount = 1, favoriteCourseCount = 1)
        elementaryDashboardPage.assertElementaryTabVisibleAndSelected(ElementaryDashboardPage.ElementaryTabType.HOMEROOM)
        homeroomPage.assertPageObjects()

        elementaryDashboardPage.selectTab(ElementaryDashboardPage.ElementaryTabType.SCHEDULE)
        elementaryDashboardPage.assertElementaryTabVisibleAndSelected(ElementaryDashboardPage.ElementaryTabType.SCHEDULE)
        schedulePage.assertPageObjects()

        elementaryDashboardPage.selectTab(ElementaryDashboardPage.ElementaryTabType.GRADES)
        elementaryDashboardPage.assertElementaryTabVisibleAndSelected(ElementaryDashboardPage.ElementaryTabType.GRADES)
        gradesPage.assertPageObjects()

        elementaryDashboardPage.selectTab(ElementaryDashboardPage.ElementaryTabType.RESOURCES)
        elementaryDashboardPage.assertElementaryTabVisibleAndSelected(ElementaryDashboardPage.ElementaryTabType.RESOURCES)
        resourcesPage.assertPageObjects()

        elementaryDashboardPage.selectTab(ElementaryDashboardPage.ElementaryTabType.HOMEROOM)
        elementaryDashboardPage.assertElementaryTabVisibleAndSelected(ElementaryDashboardPage.ElementaryTabType.HOMEROOM)
        homeroomPage.assertPageObjects()
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.CANVAS_FOR_ELEMENTARY, TestCategory.INTERACTION)
    fun testOnlyElementarySpecificNavigationItemsShownInTheNavigationDrawer() {
        goToElementaryDashboard(courseCount = 1, favoriteCourseCount = 1)
        elementaryDashboardPage.openDrawer()
        elementaryDashboardPage.assertElementaryMenuItemsShownInDrawer()
        elementaryDashboardPage.assertNotElementaryMenuItemsDontShowInDrawer()
    }

    private fun goToElementaryDashboard(
        courseCount: Int = 1,
        pastCourseCount: Int = 0,
        favoriteCourseCount: Int = 0,
        announcementCount: Int = 0): MockCanvas {

        val data = MockCanvas.init(
            studentCount = 1,
            courseCount = courseCount,
            pastCourseCount = pastCourseCount,
            favoriteCourseCount = favoriteCourseCount,
            accountNotificationCount = announcementCount)

        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLoginElementary(data.domain, token, student)
        elementaryDashboardPage.waitForRender()
        return data
    }
}