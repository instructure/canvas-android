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

import android.content.Intent
import android.webkit.CookieManager
import androidx.fragment.app.FragmentActivity
import com.instructure.loginapi.login.LoginNavigation
import com.instructure.parentapp.MainActivity


class ParentLoginNavigation(
    private val activity: FragmentActivity
) : LoginNavigation(activity) {

    override val checkElementary: Boolean = false

    override fun logout() {
        // TODO: Implement
    }

    override fun initMainActivityIntent(): Intent {
        CookieManager.getInstance().flush()

        val intent = Intent(activity, MainActivity::class.java)
        activity.intent?.extras?.let { intent.putExtras(it) }
        return intent
    }
}
