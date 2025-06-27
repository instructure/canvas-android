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

    var tempCaptureUri by NStringPref()

    var showGradesOnCard by BooleanPref(false)

    var hideCourseColorOverlay by BooleanPref(false)

    var staleFolderIds by SetPref(Long::class)

    var conferenceDashboardBlacklist by StringSetPref()

    var listDashboard by BooleanPref()

    var gradeWidgetIds by BooleanMapPref()

    override fun keepBaseProps() = listOf(
        ::showGradesOnCard,
        ::gradeWidgetIds
    )
}
