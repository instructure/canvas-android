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

package com.emeritus.student.receivers

import android.app.Activity
import android.content.Context

import com.emeritus.student.R
import com.emeritus.student.activity.NavigationActivity
import com.instructure.pandautils.receivers.PushExternalReceiver

class StudentPushExternalReceiver : PushExternalReceiver() {
    override fun getAppColor() = R.color.login_studentAppTheme

    override fun getAppName(context: Context): String = context.getString(R.string.student_app_name)

    override fun getStartingActivityClass(): Class<out Activity> = NavigationActivity.startActivityClass
}
