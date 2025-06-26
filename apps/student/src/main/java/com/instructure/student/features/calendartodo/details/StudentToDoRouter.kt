/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.student.features.calendartodo.details

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.pandautils.features.calendartodo.createupdate.CreateUpdateToDoFragment
import com.instructure.pandautils.features.calendartodo.details.ToDoRouter

import com.instructure.student.router.RouteMatcher

class StudentToDoRouter(private val activity: FragmentActivity) : ToDoRouter {
    override fun openEditToDo(plannerItem: PlannerItem) {
        val route = CreateUpdateToDoFragment.makeRoute(plannerItem)
        RouteMatcher.route(activity, route)
    }
}
