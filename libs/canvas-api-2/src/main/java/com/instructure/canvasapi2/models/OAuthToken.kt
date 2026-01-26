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

data class OAuthToken(
        @SerializedName("access_token")
        val accessToken: String? = null
)

data class OAuthTokenResponse(
        @SerializedName("access_token")
        val accessToken: String? = null,
        @SerializedName("refresh_token")
        val refreshToken: String? = null,
        @SerializedName("real_user")
        val realUser: TokenUser? = null,
        val user: TokenUser? = null,
        @SerializedName("canvas_region")
        val canvasRegion: String? = null,
)

/**
 * Used in OAuthTokenResponse as "real_user" to identify a token from a masquerade request
 *
 * Currently being used to identify when a user generates a login QR code for a user they are masquerading as
 */
data class TokenUser(
        val id: Long,
        val name: String? = null,
        val globalId: String
)

