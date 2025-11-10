/*
 * Copyright (C) 2017 - present Instructure, Inc.
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

import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.utils.toDate
import kotlinx.parcelize.Parcelize

@Parcelize
data class AccountNotification(
        override val id: Long = 0,
        val subject: String = "",
        val message: String = "",
        @SerializedName("start_at")
        val startAt: String = "",
        @SerializedName("end_at")
        val endAt: String = "",
        val icon: String = "",
        val closed: Boolean = false
) : CanvasModel<AccountNotification>() {
    override val comparisonString get() = subject
    override val comparisonDate get() = startDate

    val startDate get() = startAt.toDate()
    val endDate get() = endAt.toDate()

    companion object {
        const val ACCOUNT_NOTIFICATION_WARNING = "warning"
        const val ACCOUNT_NOTIFICATION_QUESTION = "question"
        const val ACCOUNT_NOTIFICATION_ERROR = "error"
        const val ACCOUNT_NOTIFICATION_CALENDAR = "calendar"
    }
}
