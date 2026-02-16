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

package com.instructure.teacher.features.dashboard.widget.todo

import androidx.fragment.app.FragmentActivity
import com.instructure.pandautils.features.dashboard.widget.todo.TodoWidgetRouter
import javax.inject.Inject

class TeacherTodoWidgetRouter @Inject constructor() : TodoWidgetRouter {

    override fun routeToTodo(activity: FragmentActivity, htmlUrl: String) = Unit

    override fun routeToCreateTodo(activity: FragmentActivity, initialDateString: String?) = Unit
}