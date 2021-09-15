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

import android.os.Bundle
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.models.CanvasAuthError
import okhttp3.*
import org.greenrobot.eventbus.EventBus

private const val AUTH_HEADER = "Authorization"
private const val RETRY_HEADER = "mobile_refresh"

class CanvasAuthenticator : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header(RETRY_HEADER) != null) {
            logAuthAnalytics(AnalyticsEventConstants.TOKEN_REFRESH_FAILURE)
            EventBus.getDefault().post(CanvasAuthError("Failed to authenticate"))
            return null // Give up, we've already failed to authenticate
        }

        if (response.request.url.toUrl().path.contains("accounts/self")) {
            // We are likely just checking if the user can masquerade or not, which happens on login - don't try to re-auth here
            return null
        }

        if (ApiPrefs.clientId.isBlank() || ApiPrefs.clientSecret.isBlank()) {
            logAuthAnalytics(AnalyticsEventConstants.TOKEN_REFRESH_FAILURE_NO_SECRET)
            // Can't refresh the users access token - log the user out
            EventBus.getDefault().post(CanvasAuthError("No client id or secret for refresh token"))
            return null // Indicate authentication was not successful
        }

        val refreshTokenResponse =  OAuthManager.refreshToken()

        refreshTokenResponse.onSuccess {
            refreshTokenResponse.dataOrNull?.accessToken?.let {
                ApiPrefs.accessToken = it
                EventBus.getDefault().post(CanvasTokenRefreshedEvent())
            }

            return response.request.newBuilder()
                .header(AUTH_HEADER, OAuthAPI.authBearer(ApiPrefs.accessToken))
                .header(RETRY_HEADER, RETRY_HEADER) // Mark retry to prevent infinite recursion
                .build()
        }

        refreshTokenResponse.onFail<Failure.Authorization> {
            logAuthAnalytics(AnalyticsEventConstants.TOKEN_REFRESH_FAILURE_TOKEN_NOT_VALID)
            // We got a 401 trying to get a new access token (refresh token is no longer valid) - log user out
            EventBus.getDefault().post(CanvasAuthError("Refresh token expired - cannot get new access token"))
        }

        logAuthAnalytics(AnalyticsEventConstants.TOKEN_REFRESH_FAILURE)
        return null // Indicate authentication was not successful
    }

    private fun logAuthAnalytics(eventString: String) {
        val bundle = Bundle().apply {
            putString(AnalyticsParamConstants.DOMAIN_PARAM, ApiPrefs.domain)
            putString(AnalyticsParamConstants.USER_CONTEXT_ID, ApiPrefs.user?.contextId)
        }
        Analytics.logEvent(eventString, bundle)
    }
}

/** An event sent via the event bus whenever the user's Canvas authentication token has been refreshed */
class CanvasTokenRefreshedEvent
