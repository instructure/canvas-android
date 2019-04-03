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
import com.instructure.student.BuildConfig
import com.instructure.student.R
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.interactions.router.Route
import com.instructure.loginapi.login.activities.BaseLoginInitActivity
import com.instructure.pandautils.services.PushNotificationRegistrationService
import com.instructure.pandautils.utils.Utils

class LoginActivity : BaseLoginInitActivity() {

    override fun launchApplicationMainActivityIntent(): Intent {
        PushNotificationRegistrationService.scheduleJob(this, ApiPrefs.isMasquerading, BuildConfig.PUSH_SERVICE_PROJECT_ID)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().flush()
        }

        val intent = Intent(this, NavigationActivity.startActivityClass)
        if (getIntent() != null && getIntent().extras != null) {
            intent.putExtras(getIntent().extras)
        }
        return intent
    }

    override fun beginLoginFlowIntent(): Intent {
        return LoginLandingPageActivity.createIntent(this);
    }

    override fun themeColor(): Int {
        return ContextCompat.getColor(this, R.color.login_studentAppTheme)
    }

    override fun userAgent(): String {
        return "candroid"
    }

    override fun finish() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        super.finish()
    }

    override fun isTesting(): Boolean {
        return BuildConfig.IS_TESTING
    }

    /**
     * ONLY USE FOR UI TESTING
     * Skips the traditional login process by directly setting the domain, token, and user info.
     */
    fun loginWithToken(token: String, domain: String, user: User) {
        ApiPrefs.token = token
        ApiPrefs.domain = domain
        ApiPrefs.user = user
        ApiPrefs.userAgent = Utils.generateUserAgent(this, userAgent())
        finish()
        val intent = Intent(this, NavigationActivity.startActivityClass).apply { intent?.extras?.let { putExtras(it) } }
        startActivity(intent)
    }

    companion object {
        fun createIntent(context: Context): Intent {
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return intent
        }

        fun createIntent(context: Context, route: Route): Intent {
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(Route.ROUTE, route)
            return intent
        }
    }
}
