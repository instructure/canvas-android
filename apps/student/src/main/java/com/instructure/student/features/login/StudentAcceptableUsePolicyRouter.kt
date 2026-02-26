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
import com.instructure.horizon.HorizonActivity
import com.instructure.loginapi.login.CANVAS_CAREER
import com.instructure.loginapi.login.features.acceptableusepolicy.AcceptableUsePolicyRouter
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.pandautils.room.offline.DatabaseProvider
import com.instructure.pandautils.services.PushNotificationRegistrationWorker
import com.instructure.student.R
import com.instructure.student.activity.InternalWebViewActivity
import com.instructure.student.activity.NavigationActivity
import com.instructure.student.tasks.StudentLogoutTask

class StudentAcceptableUsePolicyRouter(
    private val activity: FragmentActivity,
    private val databaseProvider: DatabaseProvider,
    private val alarmScheduler: AlarmScheduler
) : AcceptableUsePolicyRouter {

    override fun openPolicy(content: String) {
        val intent = InternalWebViewActivity.createIntent(activity, null, content, activity.getString(R.string.acceptableUsePolicyTitle), false)
        activity.startActivity(intent)
    }

    override fun startApp() {
        PushNotificationRegistrationWorker.scheduleJob(activity, ApiPrefs.isMasquerading)

        CookieManager.getInstance().flush()

        val isCanvasCareer = activity.intent?.getBooleanExtra(CANVAS_CAREER, false) ?: false
        val intent = if (isCanvasCareer) {
            Intent(activity, HorizonActivity::class.java)
        } else {
            Intent(activity, NavigationActivity.startActivityClass)
        }

        activity.intent?.extras?.let { extras ->
            intent.putExtras(extras)
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
    }

    override fun logout() {
        StudentLogoutTask(LogoutTask.Type.LOGOUT, databaseProvider = databaseProvider, alarmScheduler = alarmScheduler).execute()
    }
}