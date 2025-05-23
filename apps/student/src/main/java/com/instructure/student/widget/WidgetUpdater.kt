/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.student.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.student.widget.todo.ToDoWidgetReceiver

/**
 * Responsible for refreshing widgets.
 */
object WidgetUpdater {

    fun updateWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(ContextKeeper.appContext)

        updateNotificationsWidget(appWidgetManager)
        updateGradesWidget(appWidgetManager)
        updateTodoWidget(appWidgetManager)
    }

    private fun updateNotificationsWidget(appWidgetManager: AppWidgetManager) {
        ContextKeeper.appContext.sendBroadcast(getNotificationWidgetUpdateIntent(appWidgetManager))
    }

    private fun updateGradesWidget(appWidgetManager: AppWidgetManager) {
        ContextKeeper.appContext.sendBroadcast(getGradesWidgetUpdateIntent(appWidgetManager))
    }

    private fun updateTodoWidget(appWidgetManager: AppWidgetManager) {
        ContextKeeper.appContext.sendBroadcast(getTodoWidgetUpdateIntent(appWidgetManager))
    }

    fun getNotificationWidgetUpdateIntent(appWidgetManager: AppWidgetManager): Intent {
        val intent = Intent(ContextKeeper.appContext, NotificationWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(ContextKeeper.appContext, NotificationWidgetProvider::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        return intent
    }

    fun getGradesWidgetUpdateIntent(appWidgetManager: AppWidgetManager): Intent {
        val intent = Intent(ContextKeeper.appContext, GradesWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(ContextKeeper.appContext, GradesWidgetProvider::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        return intent
    }

    fun getTodoWidgetUpdateIntent(appWidgetManager: AppWidgetManager): Intent {
        val intent = Intent(ContextKeeper.appContext, ToDoWidgetReceiver::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(ContextKeeper.appContext, ToDoWidgetReceiver::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        return intent
    }
}
