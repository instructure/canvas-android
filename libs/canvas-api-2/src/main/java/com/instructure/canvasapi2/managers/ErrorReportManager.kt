/*
 * Copyright (C) 2018 - present Instructure, Inc.
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
 *
 */
package com.instructure.canvasapi2.managers

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.apis.ErrorReportAPI
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.ErrorReport
import com.instructure.canvasapi2.models.ErrorReportResult

object ErrorReportManager {

    fun postErrorReport(errorReport: ErrorReport, isGeneric: Boolean, callback: StatusCallback<ErrorReportResult>) {
        val adapter = RestBuilder(callback)
        val params = RestParams(
            domain = if (isGeneric) ErrorReportAPI.DEFAULT_DOMAIN else null,
            isForceReadFromNetwork = true
        )

        ErrorReportAPI.postErrorReport(errorReport, callback, adapter, params)
    }
}
