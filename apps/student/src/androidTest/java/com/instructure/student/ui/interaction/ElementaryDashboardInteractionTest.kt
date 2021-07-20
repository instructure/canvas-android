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

import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigPrefs
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLoginElementary
import com.instructure.student.util.FeatureFlagPrefs
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class ElementaryDashboardInteractionTest : StudentTest() {

    override fun displaysPageObjects() = Unit

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testNavigateToElementaryDashboard() {
        // User should be able to tap and navigate to dashboard page
        goToElementaryDashboard(courseCount = 1, favoriteCourseCount = 1)
        elementaryDashboardPage.assertPageObjects()
        elementaryDashboardPage.clickInboxTab()
        inboxPage.goToDashboard()
        elementaryDashboardPage.assertToolbarTitle()
        elementaryDashboardPage.assertHomeroomTabVisibleAndSelected()
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
    fun testTabsNavigation() {
        goToElementaryDashboard(courseCount = 1, favoriteCourseCount = 1)
        elementaryDashboardPage.assertHomeroomTabVisibleAndSelected()
        homeroomPage.assertPageObjects()

        elementaryDashboardPage.selectScheduleTab()
        elementaryDashboardPage.assertScheduleTabVisibleAndSelected()
        schedulePage.assertPageObjects()

        elementaryDashboardPage.selectGradesTab()
        elementaryDashboardPage.assertGradesTabVisibleAndSelected()
        gradesPage.assertPageObjects()

        elementaryDashboardPage.selectResourcesTab()
        elementaryDashboardPage.assertResourcesTabVisibleAndSelected()
        resourcesPage.assertPageObjects()

        elementaryDashboardPage.selectHomeroomTab()
        elementaryDashboardPage.assertHomeroomTabVisibleAndSelected()
        homeroomPage.assertPageObjects()
    }

    @Test
    @TestMetaData(Priority.P0, FeatureCategory.K5_DASHBOARD, TestCategory.INTERACTION)
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

        // We have to add this delay to be sure that the remote config is already fetched before we want to override remote config values.
        Thread.sleep(3000)
        RemoteConfigPrefs.putString(RemoteConfigParam.K5_DESIGN.rc_name, "true")
        FeatureFlagPrefs.showInProgressK5Tabs = true

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