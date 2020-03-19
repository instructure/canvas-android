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
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.loginapi.login.activities.BaseLoginLandingPageActivity
import com.instructure.loginapi.login.snicker.SnickerDoodle
import com.instructure.pandautils.services.PushNotificationRegistrationService
import com.instructure.teacher.BuildConfig
import com.instructure.teacher.R

class LoginLandingPageActivity : BaseLoginLandingPageActivity() {

    override fun launchApplicationMainActivityIntent(): Intent {
        PushNotificationRegistrationService.scheduleJob(this, ApiPrefs.isMasquerading)
        return SplashActivity.createIntent(this, null)
    }

    override fun loginWithQRIntent(): Intent? = null

    override fun beginFindSchoolFlow(): Intent = FindSchoolActivity.createIntent(this)

    override fun beginCanvasNetworkFlow(url: String): Intent = SignInActivity.createIntent(this, AccountDomain(url))

    override fun appTypeName(): Int = R.string.appUserTypeTeacher

    override fun themeColor(): Int = ContextCompat.getColor(this, R.color.login_teacherAppTheme)

    override fun signInActivityIntent(snickerDoodle: SnickerDoodle): Intent = SignInActivity.createIntent(this, AccountDomain(snickerDoodle.domain))

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, LoginLandingPageActivity::class.java)
        }
    }
}
