/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.teacher.features.dashboard.widget.courses

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.features.dashboard.edit.EditDashboardFragment
import com.instructure.pandautils.features.dashboard.widget.courses.CoursesWidgetRouter
import com.instructure.teacher.fragments.CourseBrowserFragment
import com.instructure.teacher.router.RouteMatcher

class TeacherCoursesWidgetRouter : CoursesWidgetRouter {

    override fun routeToCourse(activity: FragmentActivity, course: Course) {
        RouteMatcher.route(activity, CourseBrowserFragment.makeRoute(course))
    }

    override fun routeToGroup(activity: FragmentActivity, group: Group) {
        RouteMatcher.route(activity, CourseBrowserFragment.makeRoute(group))
    }

    override fun routeToManageOfflineContent(activity: FragmentActivity, course: Course) {
        // TODO: Navigate to manage offline content screen
    }

    override fun routeToCustomizeCourse(activity: FragmentActivity, course: Course) {
        // TODO: Navigate to customize course screen (color/nickname)
    }

    override fun routeToAllCourses(activity: FragmentActivity) {
        RouteMatcher.route(activity, EditDashboardFragment.makeRoute())
    }
}