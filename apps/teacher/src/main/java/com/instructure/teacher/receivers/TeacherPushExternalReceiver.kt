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

package com.instructure.teacher.receivers

import android.app.Activity
import android.content.Context
import com.instructure.pandautils.receivers.PushExternalReceiver
import com.instructure.teacher.R
import com.instructure.teacher.activities.LoginActivity

class TeacherPushExternalReceiver : PushExternalReceiver() {
    override fun getAppColor(): Int = R.color.login_teacherAppTheme

    override fun getAppName(context: Context): String = context.getString(R.string.app_name)

    override fun getStartingActivityClass(): Class<out Activity> = LoginActivity::class.java
}