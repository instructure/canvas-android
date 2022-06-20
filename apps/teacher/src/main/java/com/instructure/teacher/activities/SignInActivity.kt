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
package com.instructure.teacher.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.utils.ApiPrefs.isMasquerading
import com.instructure.loginapi.login.activities.BaseLoginSignInActivity
import com.instructure.pandautils.analytics.SCREEN_VIEW_SIGN_IN
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.services.PushNotificationRegistrationWorker
import dagger.hilt.android.AndroidEntryPoint

@ScreenView(SCREEN_VIEW_SIGN_IN)
@AndroidEntryPoint
class SignInActivity : BaseLoginSignInActivity() {
    override fun launchApplicationMainActivityIntent(): Intent {
        PushNotificationRegistrationWorker.scheduleJob(this, isMasquerading)
        CookieManager.getInstance().flush()
        return SplashActivity.createIntent(this, null)
    }

    override fun userAgent(): String = "androidTeacher"

    override fun refreshWidgets() {
        //No Widgets in Teacher
    }

    override fun handleLaunchApplicationMainActivityIntent() {
        val intent = launchApplicationMainActivityIntent()
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    companion object {
        fun createIntent(context: Context?, accountDomain: AccountDomain?): Intent {
            val intent = Intent(context, SignInActivity::class.java)
            val extras = Bundle()
            extras.putParcelable(ACCOUNT_DOMAIN, accountDomain)
            intent.putExtras(extras)
            return intent
        }
    }
}
