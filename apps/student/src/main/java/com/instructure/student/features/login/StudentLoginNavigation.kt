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

import android.content.Intent
import android.webkit.CookieManager
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.loginapi.login.LoginNavigation
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.pandautils.room.offline.DatabaseProvider
import com.instructure.pandautils.services.PushNotificationRegistrationWorker
import com.instructure.student.activity.NavigationActivity
import com.instructure.student.tasks.StudentLogoutTask

class StudentLoginNavigation(
    private val activity: FragmentActivity,
    private val databaseProvider: DatabaseProvider,
    private val alarmScheduler: AlarmScheduler
) : LoginNavigation(activity) {
    override val checkElementary: Boolean = true

    override fun logout() {
        StudentLogoutTask(LogoutTask.Type.LOGOUT, databaseProvider = databaseProvider, alarmScheduler = alarmScheduler).execute()
    }

    override fun initMainActivityIntent(): Intent {
        PushNotificationRegistrationWorker.scheduleJob(activity, ApiPrefs.isMasquerading)

        CookieManager.getInstance().flush()

        val intent = Intent(activity, NavigationActivity.startActivityClass)
        activity.intent?.extras?.let { extras ->
            intent.putExtras(extras)
        }
        return intent
    }
}