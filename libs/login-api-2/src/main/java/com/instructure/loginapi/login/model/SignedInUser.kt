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
package com.instructure.loginapi.login.model

import android.os.Parcelable
import com.instructure.canvasapi2.models.CanvasComparable
import com.instructure.canvasapi2.models.User
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class SignedInUser(
        var user: User,
        var domain: String,
        var protocol: String,
        var token: String,
        var accessToken: String?,
        var refreshToken: String,
        var clientId: String?,
        var clientSecret: String?,
        var calendarFilterPrefs: ArrayList<String>?,
        var lastLogoutDate: Date? = null,
        var canvasForElementary: Boolean = false,
        var selectedStudentId: Long? = null
) : Comparable<SignedInUser>, Parcelable {
    override fun compareTo(other: SignedInUser): Int {
        // We want newest first.
        return -1 * CanvasComparable.compare(lastLogoutDate, other.lastLogoutDate)
    }
}
