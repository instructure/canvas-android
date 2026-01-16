/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.student.features.login

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.webkit.CookieManager
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.HorizonActivity
import com.instructure.loginapi.login.CANVAS_FOR_ELEMENTARY
import com.instructure.loginapi.login.LoginNavigation
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.loginapi.login.viewmodel.Experience
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.pandautils.room.offline.DatabaseProvider
import com.instructure.pandautils.services.PushNotificationRegistrationWorker
import com.instructure.student.BuildConfig
import com.instructure.student.activity.NavigationActivity
import com.instructure.student.tasks.StudentLogoutTask
import com.instructure.student.widget.NotificationWidgetProvider
import com.instructure.student.widget.grades.list.GradesWidgetReceiver
import com.instructure.student.widget.grades.singleGrade.SingleGradeWidgetReceiver
import com.instructure.student.widget.todo.ToDoWidgetReceiver

class StudentLoginNavigation(
    private val activity: FragmentActivity,
    private val databaseProvider: DatabaseProvider,
    private val alarmScheduler: AlarmScheduler
) : LoginNavigation(activity) {
    override val checkElementary: Boolean = true

    override fun logout() {
        StudentLogoutTask(LogoutTask.Type.LOGOUT, databaseProvider = databaseProvider, alarmScheduler = alarmScheduler).execute()
    }

    override fun initMainActivityIntent(experience: Experience): Intent {
        // Skip push notification scheduling in test builds to prevent early WorkManager initialization
        // Tests will initialize WorkManager with HiltWorkerFactory first
        if (!BuildConfig.IS_TESTING) {
            PushNotificationRegistrationWorker.scheduleJob(activity, ApiPrefs.isMasquerading)
        }

        CookieManager.getInstance().flush()

        return when (experience) {
            Experience.Career -> {
                disableWidgets(activity)
                val intent = Intent(activity, HorizonActivity::class.java)
                activity.intent?.extras?.let { extras ->
                    intent.putExtras(extras)
                }
                intent
            }
            is Experience.Academic -> {
                enableWidgets(activity)
                val intent = Intent(activity, NavigationActivity.startActivityClass)
                activity.intent?.extras?.let { extras ->
                    intent.putExtras(extras)
                }
                intent.putExtra(CANVAS_FOR_ELEMENTARY, experience.elementary)
                intent
            }
        }
    }

    private fun disableWidgets(activity: FragmentActivity) {
        changeWidgetAvailability(activity, false, ToDoWidgetReceiver::class.java)
        changeWidgetAvailability(activity, false, SingleGradeWidgetReceiver::class.java)
        changeWidgetAvailability(activity, false, GradesWidgetReceiver::class.java)
        changeWidgetAvailability(activity, false, NotificationWidgetProvider::class.java)
    }

    private fun enableWidgets(activity: FragmentActivity) {
        changeWidgetAvailability(activity, true, ToDoWidgetReceiver::class.java)
        changeWidgetAvailability(activity, true, SingleGradeWidgetReceiver::class.java)
        changeWidgetAvailability(activity, true, GradesWidgetReceiver::class.java)
        changeWidgetAvailability(activity, true, NotificationWidgetProvider::class.java)
    }

    private fun changeWidgetAvailability(activity: FragmentActivity, enabled: Boolean, widgetClass: Class<*>) {
        val componentName = ComponentName(activity, widgetClass)
        activity.packageManager.setComponentEnabledSetting(
            componentName,
            if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}