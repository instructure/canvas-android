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
 */
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.AttendanceAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Attendance
import com.instructure.canvasapi2.utils.ApiPrefs
import java.util.*

object AttendanceManager {

    fun getAttendance(
        sectionId: Long,
        date: Calendar,
        token: String,
        cookie: String,
        callback: StatusCallback<List<Attendance>>,
        forceNetwork: Boolean
    ) {
        val protocol = ApiPrefs.protocol
        var domain = if (ApiPrefs.domain.contains(".beta.")) {
            AttendanceAPI.BASE_TEST_DOMAIN
        } else {
            AttendanceAPI.BASE_DOMAIN
        }

        domain = "$protocol://$domain"

        val adapter = RestBuilder(callback)
        val params = RestParams(domain = domain, isForceReadFromNetwork = forceNetwork)

        AttendanceAPI.getAttendance(sectionId, date, token, cookie, adapter, callback, params)
    }

    fun markAttendance(
        attendance: Attendance,
        token: String,
        cookie: String,
        callback: StatusCallback<Attendance>,
        forceNetwork: Boolean
    ) {
        val protocol = ApiPrefs.protocol
        var domain = if (ApiPrefs.domain.contains(".beta.")) {
            AttendanceAPI.BASE_TEST_DOMAIN
        } else {
            AttendanceAPI.BASE_DOMAIN
        }

        domain = "$protocol://$domain"

        val adapter = RestBuilder(callback)
        val params = RestParams(domain = domain, isForceReadFromNetwork = forceNetwork)

        AttendanceAPI.markAttendance(attendance, token, cookie, adapter, callback, params)
    }

}
