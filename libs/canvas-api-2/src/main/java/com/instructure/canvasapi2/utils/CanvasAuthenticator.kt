/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.canvasapi2.utils

import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.managers.OAuthManager
import okhttp3.*

private const val AUTH_HEADER = "Authorization"
private const val RETRY_HEADER = "mobile_refresh"

class CanvasAuthenticator : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request().header(RETRY_HEADER) != null) {
            return null // Give up, we've already failed to authenticate.
        }

        val tokenResult = OAuthManager.refreshToken()

        if (tokenResult.isSuccess) {
            tokenResult.dataOrNull?.refreshToken.let { ApiPrefs.refreshToken = it!! }
            tokenResult.dataOrNull?.accessToken.let { ApiPrefs.accessToken = it!! }

            return response.request().newBuilder()
                .header(AUTH_HEADER, OAuthAPI.authBearer(ApiPrefs.accessToken))
                .header(RETRY_HEADER, RETRY_HEADER) // Mark retry to prevent infinite recursion
                .build()
        }

        return null // Not a success
    }
}