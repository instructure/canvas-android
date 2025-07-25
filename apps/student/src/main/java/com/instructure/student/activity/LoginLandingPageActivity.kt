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
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.loginapi.login.activities.BaseLoginLandingPageActivity
import com.instructure.loginapi.login.model.SignedInUser
import com.instructure.pandautils.analytics.SCREEN_VIEW_LOGIN_LANDING
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.room.offline.DatabaseProvider
import com.instructure.student.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@ScreenView(SCREEN_VIEW_LOGIN_LANDING)
@AndroidEntryPoint
class LoginLandingPageActivity : BaseLoginLandingPageActivity() {

    @Inject
    lateinit var databaseProvider: DatabaseProvider

    override fun beginFindSchoolFlow(): Intent {
        return FindSchoolActivity.createIntent(this)
    }

    override fun beginCanvasNetworkFlow(url: String): Intent {
        return SignInActivity.createIntent(this, AccountDomain(url))
    }

    override fun appTypeName(): String {
        return ""
    }

    override fun themeColor(): Int {
        return ContextCompat.getColor(this, R.color.login_studentAppTheme)
    }

    override fun signInActivityIntent(accountDomain: AccountDomain): Intent {
        return SignInActivity.createIntent(this, accountDomain)
    }

    override fun loginWithQRCodeEnabled(): Boolean = true

    override fun loginWithQRIntent(): Intent = Intent(this, StudentLoginWithQRActivity::class.java)

    override fun removePreviousUser(user: SignedInUser) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val userId = user.user.id
                File(ContextKeeper.appContext.filesDir, userId.toString()).deleteRecursively()
                databaseProvider.clearDatabase(userId)
                super.removePreviousUser(user)
            }
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, LoginLandingPageActivity::class.java)
        }
    }
}
