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

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.loginapi.login.activities.BaseLoginLandingPageActivity
import com.instructure.parentapp.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LoginLandingPageActivity : BaseLoginLandingPageActivity() {

    override fun beginFindSchoolFlow(): Intent {
        return FindSchoolActivity.createIntent(this)
    }

    override fun signInActivityIntent(accountDomain: AccountDomain): Intent {
        return SignInActivity.createIntent(this, accountDomain)
    }

    override fun beginCanvasNetworkFlow(url: String): Intent {
        return SignInActivity.createIntent(this, AccountDomain(url))
    }

    override fun themeColor(): Int {
        return ContextCompat.getColor(this, R.color.login_parentAppTheme)
    }

    override fun appTypeName(): String {
        return getString(R.string.appUserTypeParent)
    }

    override fun loginWithQRCodeEnabled() = true

    override fun loginWithQRIntent(): Intent {
        return Intent(this, ParentLoginWithQRActivity::class.java)
    }

    override fun qrLoginClicked() {
        LoginBottomSheetDialogFragment().show(
            supportFragmentManager,
            LoginBottomSheetDialogFragment::class.java.simpleName
        )
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, LoginLandingPageActivity::class.java)
        }
    }
}
