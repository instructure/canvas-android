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
package com.instructure.canvasapi2.models

import android.os.Parcelable
import com.instructure.canvasapi2.apis.ErrorReportAPI
import kotlinx.parcelize.Parcelize


data class ErrorReport(
        // The users problem summary, like an email subject line
        val subject: String = "",

        // Long form documentation of what was witnessed
        val comment: String = "",

        /* Categorization of how bad the user thinks the problem is.  Should be one of
        [just_a_comment, not_urgent, workaround_possible, blocks_what_i_need_to_do,
        extreme_critical_emergency] */
        var severity: String = "",

        // URL of the page on which the error was reported
        val url: String = "",

        // The email address of the reporting user
        val email: String = "",

        // The name of the reporting user
        var name: String = "",

        var userRoles: String = "",

        var becomeUser: String = ""
)

@Parcelize
data class ErrorReportPreFill(
        val title: String? = null,
        val subject: String? = null,
        val email: String? = null,
        val comment: String? = null,
        val severity: ErrorReportAPI.Severity? = null
) : Parcelable
