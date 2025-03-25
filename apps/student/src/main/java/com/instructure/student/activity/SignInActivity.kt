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
import android.os.Bundle
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.loginapi.login.activities.BaseLoginSignInActivity
import com.instructure.loginapi.login.util.LoginPrefs
import com.instructure.pandautils.analytics.SCREEN_VIEW_SIGN_IN
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.student.widget.WidgetUpdater
import dagger.hilt.android.AndroidEntryPoint

@ScreenView(SCREEN_VIEW_SIGN_IN)
@AndroidEntryPoint
class SignInActivity : BaseLoginSignInActivity() {

    override fun userAgent(): String {
        return "candroid"
    }

    override fun refreshWidgets() {
        WidgetUpdater.updateWidgets()
    }

    override fun signInIntent(context: Context): Intent {
        return createIntent(context, LoginPrefs.lastSavedLogin?.accountDomain ?: AccountDomain(ApiPrefs.domain))
    }

    companion object {
        fun createIntent(context: Context, accountDomain: AccountDomain): Intent {
            val intent = Intent(context, SignInActivity::class.java)
            val extras = Bundle()
            extras.putParcelable(BaseLoginSignInActivity.ACCOUNT_DOMAIN, accountDomain)
            intent.putExtras(extras)
            return intent
        }
    }
}
