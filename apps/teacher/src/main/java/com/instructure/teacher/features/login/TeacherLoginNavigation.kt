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
package com.instructure.teacher.features.login

import android.content.Intent
import android.webkit.CookieManager
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.loginapi.login.CANVAS_FOR_ELEMENTARY
import com.instructure.loginapi.login.LoginNavigation
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.loginapi.login.viewmodel.Experience
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.pandautils.services.PushNotificationRegistrationWorker
import com.instructure.teacher.activities.SplashActivity
import com.instructure.teacher.tasks.TeacherLogoutTask

class TeacherLoginNavigation(
    private val activity: FragmentActivity,
    private val alarmScheduler: AlarmScheduler
) : LoginNavigation(activity) {
    override val checkElementary: Boolean = false

    override fun logout() {
        TeacherLogoutTask(
            LogoutTask.Type.LOGOUT,
            alarmScheduler = alarmScheduler
        ).execute()
    }

    override fun initMainActivityIntent(experience: Experience): Intent {
        PushNotificationRegistrationWorker.scheduleJob(activity, ApiPrefs.isMasquerading)

        CookieManager.getInstance().flush()

        return SplashActivity.createIntent(activity, activity.intent?.extras).apply {
            if (experience is Experience.Academic) {
                putExtra(CANVAS_FOR_ELEMENTARY, experience.elementary)
            }
        }
    }
}