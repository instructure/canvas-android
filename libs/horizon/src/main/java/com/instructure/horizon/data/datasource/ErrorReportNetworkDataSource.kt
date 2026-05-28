/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.horizon.data.datasource

import com.instructure.canvasapi2.apis.ErrorReportAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.ErrorReportResult
import com.instructure.canvasapi2.utils.ApiPrefs
import javax.inject.Inject

class ErrorReportNetworkDataSource @Inject constructor(
    private val apiPrefs: ApiPrefs,
    private val errorReportApi: ErrorReportAPI.ErrorReportInterface,
) {
    suspend fun submitErrorReport(
        subject: String,
        description: String,
        email: String,
        severity: String,
    ): ErrorReportResult {
        return errorReportApi.postErrorReport(
            subject = subject,
            url = apiPrefs.fullDomain,
            email = email,
            comments = description,
            userPerceivedSeverity = severity,
            name = apiPrefs.user?.name ?: "",
            userRoles = "student",
            becomeUser = "",
            body = "",
            RestParams(),
        ).dataOrThrow
    }
}
