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
import androidx.annotation.VisibleForTesting
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
class DomainVerificationResult(
    val authorized: Boolean = false,
    @SerializedName("result")
    var resultCode: Int = 0,
    @SerializedName("client_id")
    val clientId: String = "",
    @SerializedName("client_secret")
    val clientSecret: String = "",
    @SerializedName("api_key")
    val apiKey: String = "",
    @SerializedName("base_url")
    val url: String = ""
) : Parcelable {
    val baseUrl: String get() = url.substringAfter("://")
    val protocol: String get() = url.substringBefore("://", "https")

    /**
     * Success      = 0
     * Other        = 1 # generic "you aren't authorized cuz i said so"
     * BadSite      = 2 # ['domain'] isn't authorized for mobile apps
     * BadUserAgent = 3 # the user agent given wasn't recognized
     */
    enum class DomainVerificationCode {
        @SerializedName("0") Success,
        @SerializedName("1") GeneralError,
        @SerializedName("2") DomainNotAuthorized,
        @SerializedName("3") UnknownUserAgent,
        @SerializedName("4") UnknownError
    }

    var result: DomainVerificationCode
        get() {
            return when (resultCode) {
                0 -> DomainVerificationCode.Success // Success
                1 -> DomainVerificationCode.GeneralError // General error
                2 -> DomainVerificationCode.DomainNotAuthorized // Unauthorized domain
                3 -> DomainVerificationCode.UnknownUserAgent // Bad user agent
                else -> DomainVerificationCode.UnknownError // Send an unknown error
            }
        }
        set(value) {
            resultCode = when (value) {
                DomainVerificationCode.Success -> 0
                DomainVerificationCode.GeneralError -> 1
                DomainVerificationCode.DomainNotAuthorized -> 2
                DomainVerificationCode.UnknownUserAgent -> 3
                else -> 4
            }
        }
}
