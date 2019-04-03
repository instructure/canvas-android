/*
 * Copyright (C) 2018 - present  Instructure, Inc.
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
package com.instructure.androidpolling.app.asynctasks

import android.content.Context
import android.content.Intent
import com.instructure.androidpolling.app.activities.InitLoginActivity
import com.instructure.androidpolling.app.util.ApplicationManager.PREF_FILE_NAME
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.loginapi.login.tasks.LogoutTask

class PollsLogoutTask(type: Type) : LogoutTask(type) {

    override fun onCleanup() {
        // Clear shared preferences
        val settings = ContextKeeper.appContext.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.clear()
        editor.apply()

    }

    override fun createLoginIntent(context: Context): Intent {
        return InitLoginActivity.createIntent(ContextKeeper.appContext)
    }

}
