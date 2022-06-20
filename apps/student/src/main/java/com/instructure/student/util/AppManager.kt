/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.util

import com.instructure.canvasapi2.utils.MasqueradeHelper
import com.instructure.loginapi.login.tasks.LogoutTask
import com.instructure.pandautils.typeface.TypefaceBehavior
import com.instructure.student.tasks.StudentLogoutTask
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AppManager : BaseAppManager() {

    @Inject
    lateinit var typefaceBehavior: TypefaceBehavior

    override fun onCreate() {
        super.onCreate()
        MasqueradeHelper.masqueradeLogoutTask = Runnable { StudentLogoutTask(LogoutTask.Type.LOGOUT, typefaceBehavior = typefaceBehavior).execute() }
    }

    override fun performLogoutOnAuthError() {
        StudentLogoutTask(LogoutTask.Type.LOGOUT, typefaceBehavior = typefaceBehavior).execute()
    }
}
