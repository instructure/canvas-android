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

package com.instructure.student.features.dashboard.widget.todo

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.instructure.pandautils.features.dashboard.widget.todo.TodoWidgetBehavior
import com.instructure.pandautils.features.dashboard.widget.todo.TodoWidgetRouter
import com.instructure.student.widget.WidgetUpdater
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StudentTodoWidgetBehavior @Inject constructor(
    private val router: TodoWidgetRouter,
    @ApplicationContext private val context: Context,
    private val widgetUpdater: WidgetUpdater,
    private val appWidgetManager: AppWidgetManager
) : TodoWidgetBehavior {

    override fun onTodoClick(activity: FragmentActivity, htmlUrl: String) {
        router.routeToTodo(activity, htmlUrl)
    }

    override fun onAddTodoClick(activity: FragmentActivity, initialDateString: String?) {
        router.routeToCreateTodo(activity, initialDateString)
    }

    override fun updateWidget(forceRefresh: Boolean) {
        context.sendBroadcast(widgetUpdater.getTodoWidgetUpdateIntent(appWidgetManager, forceRefresh = forceRefresh))
    }
}