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
 */
package com.instructure.canvasapi2.utils.pageview

import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.utils.isValid
import java.util.*

data class PandataInfo(
    @SerializedName("url")
    val postUrl: String,
    @SerializedName("auth_token")
    val authToken: String,
    @SerializedName("props_token")
    val signedProperties: String,
    @SerializedName("expires_at")
    val expiresAt: Double
) {

    class AppKey(val key: String, val appName: String)

    val isValid get() = authToken.isValid() && Date(expiresAt.toLong()).after(minDate)

    companion object {

        /** 10 minutes to account for clock skew */
        private const val CLOCK_SKEW = 10 * 60 * 1000L

        private val minDate get() = Date(System.currentTimeMillis() + CLOCK_SKEW)

    }

}
