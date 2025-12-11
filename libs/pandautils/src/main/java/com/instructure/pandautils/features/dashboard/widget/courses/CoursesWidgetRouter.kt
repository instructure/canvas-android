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

package com.instructure.pandautils.features.dashboard.widget.courses

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group

interface CoursesWidgetRouter {
    fun routeToCourse(activity: FragmentActivity, course: Course)
    fun routeToGroup(activity: FragmentActivity, group: Group)
    fun routeToManageOfflineContent(activity: FragmentActivity, course: Course)
    fun routeToCustomizeCourse(activity: FragmentActivity, course: Course)
    fun routeToAllCourses(activity: FragmentActivity)
}