/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
package com.instructure.parentapp.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import android.webkit.CookieManager
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.loginapi.login.activities.BaseLoginLandingPageActivity
import com.instructure.loginapi.login.snicker.SnickerDoodle
import com.instructure.pandautils.services.PushNotificationRegistrationService
import com.instructure.parentapp.R

class LoginLandingPageActivity : BaseLoginLandingPageActivity() {

    override fun launchApplicationMainActivityIntent(): Intent {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().flush()
        }

        val intent = Intent(this, SplashActivity::class.java)
        if (getIntent() != null && getIntent().extras != null) {
            intent.putExtras(getIntent().extras)
        }
        return intent
    }

    override fun beginFindSchoolFlow(): Intent {
        return FindSchoolActivity.createIntent(this, false)
    }

    override fun beginCanvasNetworkFlow(url: String): Intent {
        return SignInActivity.createIntent(this, AccountDomain(url), false)
    }

    override fun appTypeName(): Int {
        return R.string.appUserTypeParent
    }

    override fun themeColor(): Int {
        return ContextCompat.getColor(this, R.color.login_parentAppTheme)
    }

    override fun signInActivityIntent(snickerDoodle: SnickerDoodle): Intent {
        return SignInActivity.createIntent(this, AccountDomain(snickerDoodle.domain), false)
    }

    override fun appChangesLink(): String {
        return "https://s3.amazonaws.com/tr-learncanvas/docs/WhatsNewCanvasParent.pdf"
    }

    override fun loginWithQRIntent(): Intent? = null

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, LoginLandingPageActivity::class.java)
        }
    }
}
