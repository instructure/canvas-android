/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.teacher.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.utils.Logger

object LoggingUtility {
    fun log(s: String?, priority: Int = Log.DEBUG, tag: String = Logger.LOG_TAG) {
        //Will write to crashlytics and logcat
        if (Logger.canLogUserDetails()) FirebaseCrashlytics.getInstance().log("Priority: $priority, Tag: $tag, Message: $s")
    }
}
