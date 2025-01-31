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
package com.instructure.parentapp.features.login

import android.content.Intent
import android.webkit.CookieManager
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.loginapi.login.features.acceptableusepolicy.AcceptableUsePolicyRouter
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.parentapp.R
import com.instructure.parentapp.features.main.MainActivity
import com.instructure.parentapp.features.webview.HtmlContentActivity
import com.instructure.parentapp.util.ParentLogoutTask

class ParentAcceptableUsePolicyRouter(
    private val activity: FragmentActivity,
    private val alarmScheduler: AlarmScheduler,
    private val analytics: Analytics
) : AcceptableUsePolicyRouter {
    override fun openPolicy(content: String) {
        val intent = HtmlContentActivity.createIntent(activity, activity.getString(R.string.acceptableUsePolicyTitle), content, true)
        activity.startActivity(intent)
    }

    override fun startApp() {
        CookieManager.getInstance().flush()

        val intent = Intent(activity, MainActivity::class.java)
        activity.intent?.extras?.let { intent.putExtras(it) }
        activity.startActivity(intent)
    }

    override fun logout() {
        analytics.logEvent(AnalyticsEventConstants.LOGOUT)
        ParentLogoutTask(
            LogoutTask.Type.LOGOUT,
            alarmScheduler = alarmScheduler
        ).execute()
    }
}