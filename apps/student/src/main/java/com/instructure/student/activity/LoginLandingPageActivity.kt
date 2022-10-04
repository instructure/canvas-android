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
package com.instructure.student.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import android.webkit.CookieManager
import com.instructure.student.R
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.loginapi.login.activities.BaseLoginLandingPageActivity
import com.instructure.loginapi.login.snicker.SnickerDoodle
import com.instructure.pandautils.analytics.SCREEN_VIEW_LOGIN_LANDING
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.services.PushNotificationRegistrationWorker
import dagger.hilt.android.AndroidEntryPoint

@ScreenView(SCREEN_VIEW_LOGIN_LANDING)
@AndroidEntryPoint
class LoginLandingPageActivity : BaseLoginLandingPageActivity() {

    override fun launchApplicationMainActivityIntent(): Intent {
        PushNotificationRegistrationWorker.scheduleJob(this, ApiPrefs.isMasquerading)

        CookieManager.getInstance().flush()

        val intent = Intent(this, NavigationActivity.startActivityClass)
        if (getIntent() != null && getIntent().extras != null) {
            intent.putExtras(getIntent().extras!!)
        }
        return intent
    }

    override fun beginFindSchoolFlow(): Intent {
        return FindSchoolActivity.createIntent(this)
    }

    override fun beginCanvasNetworkFlow(url: String): Intent {
        return SignInActivity.createIntent(this, AccountDomain(url))
    }

    override fun appTypeName(): Int {
        return R.string.appUserTypeStudent
    }

    override fun themeColor(): Int {
        return ContextCompat.getColor(this, R.color.login_studentAppTheme)
    }

    override fun signInActivityIntent(accountDomain: AccountDomain): Intent {
        return SignInActivity.createIntent(this, accountDomain)
    }

    override fun loginWithQRCodeEnabled(): Boolean = true

    override fun loginWithQRIntent(): Intent = Intent(this, StudentLoginWithQRActivity::class.java)

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, LoginLandingPageActivity::class.java)
        }
    }
}
