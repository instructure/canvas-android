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
package com.instructure.student.ui.interaction

import com.instructure.canvas.espresso.Stub
import com.instructure.student.ui.utils.StudentTest
import org.junit.Test

class DashboardInteractionTest : StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Stub
    @Test
    fun testNavigateToDashboard() {
        // User should be able to tap and navigate to dashboard page
    }

    @Stub
    @Test
    fun testDashboardCourses_emptyState() {
        // Empty state should be displayed with a 'Add Courses' button, when nothing is favorited (and courses are completed/concluded)
        // With the new DashboardCard api being used, if nothing is a favorite it will default to active enrollments
    }

    @Stub
    @Test
    fun testDashboardCourses_addFavorite() {
        // Starring should add course from favorite list
    }

    @Stub
    @Test
    fun testDashboardCourses_removeFavorite() {
        // Un-starring should remove course from favorite list
    }

    @Stub
    @Test
    fun testDashboardCourses_seeAll() {

    }

    @Stub
    @Test
    fun testDashboardAnnouncement_refresh() {
        // Pull to refresh loads new announcements
    }

    @Stub
    @Test
    fun testDashboardAnnouncement_dismiss() {
        // Tapping dismiss should remove the announcement. Refresh should not display it again.
    }

    @Stub
    @Test
    fun testDashboardAnnouncement_view() {
        // Tapping global announcement displays the content
    }
}
