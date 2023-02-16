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
import android.net.Uri
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.interactions.router.Route
import com.instructure.loginapi.login.activities.BaseLoginInitActivity
import com.instructure.loginapi.login.util.QRLogin
import com.instructure.pandautils.analytics.SCREEN_VIEW_LOGIN
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.Utils
import com.instructure.student.R
import dagger.hilt.android.AndroidEntryPoint

@ScreenView(SCREEN_VIEW_LOGIN)
@AndroidEntryPoint
class LoginActivity : BaseLoginInitActivity() {

    override fun beginLoginFlowIntent(): Intent {
        return SignInActivity.createIntent(this)
//        return LoginLandingPageActivity.createIntent(this);
    }

    override fun themeColor(): Int {
        return ContextCompat.getColor(this, R.color.login_studentAppTheme)
    }

    override fun userAgent(): String = Const.STUDENT_USER_AGENT

    override fun finish() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        super.finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check to see if we are switching users from an external QR scan
        if (QRLogin.verifySSOLoginUri(intent.data)) {
            startActivity(InterwebsToApplication.createIntent(this, intent.data!!))
            finish()
        }
    }

    /**
     * ONLY USE FOR UI TESTING
     * Skips the traditional login process by directly setting the domain, token, and user info.
     */
    fun loginWithToken(token: String, domain: String, user: User, canvasForElementary: Boolean = false) {
        ApiPrefs.accessToken = token
        ApiPrefs.domain = domain
        ApiPrefs.user = user
        ApiPrefs.userAgent = Utils.generateUserAgent(this, userAgent())
        finish()
        val intent = Intent(this, NavigationActivity.startActivityClass).apply {
            intent?.extras?.let { putExtras(it) }
            putExtra("canvas_for_elementary", canvasForElementary)
        }
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

        // Used specifically for a QR Scan user switch
        fun createIntent(context: Context, uri: Uri): Intent {
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.data = uri
            return intent
        }
    }
}
