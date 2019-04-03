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
package com.instructure.teacher.utils

import android.content.Context
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.teacher.R
import java.text.SimpleDateFormat
import java.util.*

fun Date?.getSubmissionFormattedDate(context: Context): String {
    val atSeparator = context.getString(R.string.at)
    return DateHelper.getMonthDayAtTime(context, this, atSeparator) ?: ""
}

// Quick extension function for formatting - Returns '--' for null dates
fun SimpleDateFormat.formatOrDoubleDash(date: Date?): String {
    return if (date == null)
        "--"
    else format(date)
}
