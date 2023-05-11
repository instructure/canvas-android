/*
 * Copyright (C) 2021 - present Instructure, Inc.
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

package com.emeritus.student.features.dashboard.edit

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.features.dashboard.edit.EditDashboardRouter
import com.emeritus.student.fragment.CourseBrowserFragment
import com.emeritus.student.router.RouteMatcher

class StudentEditDashboardRouter(private val activity: FragmentActivity) : EditDashboardRouter {
    override fun routeCourse(canvasContext: CanvasContext?) {
        RouteMatcher.route(activity, CourseBrowserFragment.makeRoute(canvasContext))
    }
}
