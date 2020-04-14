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
package com.instructure.student.util

import com.instructure.canvasapi2.utils.*

object StudentPrefs : PrefManager("candroidSP") {

    @JvmStatic
    var tempCaptureUri by NStringPref()

    @JvmStatic
    var showGradesOnCard by BooleanPref(true)

    var hideCourseColorOverlay by BooleanPref(false)

    @JvmStatic
    var weekStartsOnMonday by BooleanPref(false, "calendarStartDayPrefs")

    @JvmStatic
    var calendarYearPref by IntPref(-1)

    @JvmStatic
    var calendarMonthPref by IntPref(-1)

    @JvmStatic
    var calendarDayPref by IntPref(-1)

    @JvmStatic
    var calendarPrefFlag by BooleanPref()

    @JvmStatic
    var calendarViewType by IntPref()

    @JvmStatic
    var calendarFilters by StringSetPref(keyName = "calFilterPrefsKey")

    @JvmStatic
    var staleFolderIds by SetPref(Long::class)

    var conferenceDashboardBlacklist by StringSetPref()

    override fun keepBaseProps() = listOf(
            ::showGradesOnCard,
            ::weekStartsOnMonday
    )
}
