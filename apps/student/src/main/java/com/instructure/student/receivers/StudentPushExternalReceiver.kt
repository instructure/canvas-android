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

package com.instructure.student.receivers

import android.app.Activity
import android.content.Context
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.HorizonActivity
import com.instructure.pandautils.receivers.PushExternalReceiver
import com.instructure.pandautils.utils.orDefault
import com.instructure.student.R
import com.instructure.student.activity.NavigationActivity

class StudentPushExternalReceiver : PushExternalReceiver() {
    override fun getAppColor() = R.color.login_studentAppTheme

    override fun getAppName(context: Context): String = context.getString(R.string.student_app_name)

    override fun getStartingActivityClass(): Class<out Activity> {
        return if (ApiPrefs.canvasCareerView.orDefault()) HorizonActivity::class.java else NavigationActivity.startActivityClass
    }
}
