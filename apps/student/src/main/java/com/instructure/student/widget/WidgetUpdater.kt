package com.instructure.student.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.student.widget.grades.list.GradesWidgetReceiver
import com.instructure.student.widget.grades.singleGrade.SingleGradeWidgetReceiver
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
         updateSingleGradeWidget(appWidgetManager)
    }

    private fun updateNotificationsWidget(appWidgetManager: AppWidgetManager) {
        ContextKeeper.appContext.sendBroadcast(getNotificationWidgetUpdateIntent(appWidgetManager))
    }

    private fun updateGradesWidget(appWidgetManager: AppWidgetManager) {
        ContextKeeper.appContext.sendBroadcast(getGradesWidgetUpdateIntent(appWidgetManager))
    }

    private fun updateSingleGradeWidget(appWidgetManager: AppWidgetManager) {
        ContextKeeper.appContext.sendBroadcast(getSingleGradeWidgetUpdateIntent(appWidgetManager))
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
        val intent = Intent(ContextKeeper.appContext, GradesWidgetReceiver::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(ContextKeeper.appContext, GradesWidgetReceiver::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        return intent
    }

    fun getSingleGradeWidgetUpdateIntent(appWidgetManager: AppWidgetManager): Intent {
        val intent = Intent(ContextKeeper.appContext, SingleGradeWidgetReceiver::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(ContextKeeper.appContext, SingleGradeWidgetReceiver::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        return intent
    }

    private fun getTodoWidgetUpdateIntent(appWidgetManager: AppWidgetManager): Intent {
        val intent = Intent(ContextKeeper.appContext, ToDoWidgetReceiver::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(ContextKeeper.appContext, ToDoWidgetReceiver::class.java))
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        return intent
    }
}
