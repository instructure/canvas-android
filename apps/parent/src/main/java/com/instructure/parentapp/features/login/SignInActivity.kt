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
import android.os.Bundle
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.loginapi.login.activities.BaseLoginSignInActivity
import com.instructure.pandautils.utils.Const
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SignInActivity : BaseLoginSignInActivity() {

    override fun refreshWidgets() = Unit

    override fun userAgent() = Const.PARENT_USER_AGENT

    companion object {
        fun createIntent(context: Context, accountDomain: AccountDomain): Intent {
            val intent = Intent(context, SignInActivity::class.java)
            val extras = Bundle()
            extras.putParcelable(ACCOUNT_DOMAIN, accountDomain)
            intent.putExtras(extras)
            return intent
        }
    }
}
