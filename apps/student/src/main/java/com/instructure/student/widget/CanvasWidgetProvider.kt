/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

import android.app.Activity
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import android.widget.RemoteViews
import com.instructure.student.R
import com.instructure.student.activity.LoginActivity
import com.instructure.student.activity.WidgetSetupActivity
import com.instructure.student.util.Analytics
import com.instructure.student.util.StudentPrefs
import com.instructure.canvasapi2.utils.Logger

abstract class CanvasWidgetProvider : AppWidgetProvider() {

    /*
     * Returns simple name for Google Analytics
     */
    abstract val analyticsName: String
    protected abstract val refreshIntentID: Int
    protected abstract fun setWidgetDependentViews(context: Context, remoteViews: RemoteViews, appWidgetId: Int, appWidgetManager: AppWidgetManager, textColor: Int)
    protected abstract fun getRefreshIntent(appWidgetManager: AppWidgetManager): Intent

    //region Broadcast Callbacks
    override fun onEnabled(context: Context) {
        if (context is Activity) {
            //This may not return a context of type activity...
            Analytics.trackWidgetFlow(context, analyticsName)
        }
        updateWidgetList(context)
    }

    /**
     * Called when the widget is added to the home screen.
     */
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        setupWidget(context, appWidgetManager, appWidgetIds)
    }

    /**
     * This is a callback that occurs when the refresh button is pressed.
     * It determines the origin of the
     * broadcast based of the action string.
     */
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, this::class.java))
        setupWidget(context, appWidgetManager, appWidgetIds)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            StudentPrefs.remove(WidgetSetupActivity.WIDGET_BACKGROUND_PREFIX + id)
        }
        super.onDeleted(context, appWidgetIds)
    }

    //endregion

    private fun setupWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Logger.d("CanvasWidgetProvider : setupWidget(" + this::class.java.simpleName + ") - " + appWidgetIds.size)
        for (appWidgetId in appWidgetIds) {
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_homescreen)
            val textColor = BaseRemoteViewsService.getWidgetTextColor(appWidgetId, context)
            val logoColor = ContextCompat.getColor(context, R.color.login_studentAppTheme)
            remoteViews.setImageViewResource(R.id.widget_logo, R.drawable.ic_canvas_logo_student)
            remoteViews.setInt(R.id.widget_logo, "setColorFilter", logoColor)
            setWidgetDependentViews(context, remoteViews, appWidgetId, appWidgetManager, textColor)

            // Tapping on the logo or title should open the app
            val launchMain = Intent(context, LoginActivity::class.java)
            val pendingMainIntent = PendingIntent.getActivity(context, 0, launchMain, PendingIntent.FLAG_IMMUTABLE)
            remoteViews.setOnClickPendingIntent(R.id.widget_title, pendingMainIntent)
            remoteViews.setOnClickPendingIntent(R.id.widget_logo, pendingMainIntent)

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.contentList)
        }
    }

    /**
     * Invalidates the collection list on the widget,
     * and makes it re-pull data from the api.
     */
    private fun updateWidgetList(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, this::class.java))
        setupWidget(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        var cycleBit = 100
    }
}
