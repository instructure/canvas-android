/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.account.reportabug

import com.instructure.canvasapi2.apis.ErrorReportAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.ErrorReportResult
import com.instructure.canvasapi2.utils.ApiPrefs
import javax.inject.Inject

class ReportABugRepository @Inject constructor(
    private val apiPrefs: ApiPrefs,
    private val errorReportAPI: ErrorReportAPI.ErrorReportInterface
) {

    suspend fun submitErrorReport(
        subject: String,
        description: String,
        email: String,
        severity: String
    ): ErrorReportResult {
        return errorReportAPI.postErrorReport(
            subject = subject,
            url = apiPrefs.fullDomain,
            email = email,
            comments = description,
            userPerceivedSeverity = severity,
            name = apiPrefs.user?.name ?: "",
            userRoles = "student",
            becomeUser = "",
            body = "",
            RestParams()
        ).dataOrThrow
    }
}
