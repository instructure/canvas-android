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
package com.instructure.student.mobius.elementary.grades

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Tab
import com.instructure.pandautils.features.elementary.grades.GradesRouter
import com.instructure.student.features.elementary.course.ElementaryCourseFragment
import com.instructure.student.fragment.GradesListFragment
import com.instructure.student.router.RouteMatcher

class StudentGradesRouter(private val activity: FragmentActivity) : GradesRouter {

    override fun openCourseGrades(course: Course) {
        val route = ElementaryCourseFragment.makeRoute(course, Tab.GRADES_ID)
        RouteMatcher.route(activity, route)
    }
}