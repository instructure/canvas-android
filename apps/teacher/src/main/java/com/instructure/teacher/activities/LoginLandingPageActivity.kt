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
import com.instructure.pandautils.analytics.SCREEN_VIEW_LOGIN_LANDING
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.services.PushNotificationRegistrationWorker
import com.instructure.teacher.R
import dagger.hilt.android.AndroidEntryPoint

@ScreenView(SCREEN_VIEW_LOGIN_LANDING)
@AndroidEntryPoint
class LoginLandingPageActivity : BaseLoginLandingPageActivity() {

    override fun loginWithQRIntent(): Intent = Intent(this, TeacherLoginWithQRActivity::class.java)

    override fun loginWithQRCodeEnabled(): Boolean = true

    override fun beginFindSchoolFlow(): Intent = FindSchoolActivity.createIntent(this)

    override fun beginCanvasNetworkFlow(url: String): Intent = SignInActivity.createIntent(this, AccountDomain(url))

    override fun appTypeName(): String = getString(R.string.appUserTypeTeacher)

    override fun themeColor(): Int = ContextCompat.getColor(this, R.color.login_teacherAppTheme)

    override fun signInActivityIntent(accountDomain: AccountDomain): Intent = SignInActivity.createIntent(this, accountDomain)

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, LoginLandingPageActivity::class.java)
        }
    }
}
