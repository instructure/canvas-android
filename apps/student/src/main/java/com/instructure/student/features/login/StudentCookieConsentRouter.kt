/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.student.features.login

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.instructure.horizon.HorizonActivity
import com.instructure.loginapi.login.CANVAS_CAREER
import com.instructure.loginapi.login.features.cookieconsent.CookieConsentRouter
import com.instructure.student.activity.NavigationActivity

class StudentCookieConsentRouter(
    private val activity: FragmentActivity
) : CookieConsentRouter {

    override fun startApp() {
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
}
