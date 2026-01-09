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
package com.instructure.canvasapi2.apis

import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.builders.RestBuilder
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.ErrorReport
import com.instructure.canvasapi2.models.ErrorReportResult
import com.instructure.canvasapi2.utils.DataResult
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Tag


object ErrorReportAPI {

    const val DEFAULT_DOMAIN = "https://canvas.instructure.com"

    enum class Severity(val tag: String) {
        COMMENT("just_a_comment"),
        NOT_URGENT("not_urgent"),
        WORKAROUND_POSSIBLE("workaround_possible"),
        BLOCKING("blocks_what_i_need_to_do"),
        CRITICAL("extreme_critical_emergency")
    }

    interface ErrorReportInterface {
        @POST("/api/v1/error_reports")
        fun postErrorReport(
            @Query("error[subject]") subject: String,
            @Query("error[url]") url: String,
            @Query("error[email]") email: String,
            @Query("error[comments]") comments: String,
            @Query("error[user_perceived_severity]") userPerceivedSeverity: String,
            @Query("error[name]") name: String,
            @Query("error[user_roles]") userRoles: String,
            @Query("error[become_user]") becomeUser: String,
            @Body body: String
        ): Call<ErrorReportResult>

        @POST("/api/v1/error_reports")
        suspend fun postErrorReport(
            @Query("error[subject]") subject: String,
            @Query("error[url]") url: String,
            @Query("error[email]") email: String,
            @Query("error[comments]") comments: String,
            @Query("error[user_perceived_severity]") userPerceivedSeverity: String,
            @Query("error[name]") name: String,
            @Query("error[user_roles]") userRoles: String,
            @Query("error[become_user]") becomeUser: String,
            @Body body: String,
            @Tag params: RestParams,
        ): DataResult<ErrorReportResult>
    }

    fun postErrorReport(
        errorReport: ErrorReport,
        callback: StatusCallback<ErrorReportResult>,
        adapter: RestBuilder,
        params: RestParams
    ) {
        callback
            .addCall(
                adapter.build(ErrorReportInterface::class.java, params)
                    .postErrorReport(
                        errorReport.subject,
                        errorReport.url,
                        errorReport.email,
                        errorReport.comment,
                        errorReport.severity,
                        errorReport.name,
                        errorReport.userRoles,
                        errorReport.becomeUser,
                        ""
                    )
            ).enqueue(callback)
    }
}
