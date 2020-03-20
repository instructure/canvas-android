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
package com.instructure.parentapp.tasks

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.parentapp.activity.LoginActivity
import com.instructure.parentapp.util.ParentPrefs

class ParentLogoutTask(type: Type) : LogoutTask(type) {

    override fun onCleanup() {
        ParentPrefs.clearPrefs()
    }

    override fun createLoginIntent(context: Context): Intent {
        return LoginActivity.createIntent(context)
    }

    override fun createQRLoginIntent(context: Context, uri: Uri): Intent? = null

    override fun getFcmToken(listener: (registrationId: String?) -> Unit) {
        listener(null)
    }
}
