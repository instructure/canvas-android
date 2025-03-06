/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.utils

import android.content.Context
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.ApiPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val HOUR_IN_MILLIS = 1000 * 60 * 60

class WebViewAuthenticator(private val oAuthApi: OAuthAPI.OAuthInterface, private val apiPrefs: ApiPrefs) {

    fun authenticateWebViews(coroutineScope: CoroutineScope, context: Context) {
        val currentTime = System.currentTimeMillis()
        val lastAuthenticated = apiPrefs.webViewAuthenticationTimestamp
        if (currentTime - lastAuthenticated > HOUR_IN_MILLIS) {
            coroutineScope.launch {
                oAuthApi.getAuthenticatedSession(
                    apiPrefs.fullDomain,
                    RestParams(isForceReadFromNetwork = true)
                ).dataOrNull?.sessionUrl?.let {
                    loadUrlIntoHeadlessWebView(context, it)
                    apiPrefs.webViewAuthenticationTimestamp = currentTime
                }
            }
        }
    }
}