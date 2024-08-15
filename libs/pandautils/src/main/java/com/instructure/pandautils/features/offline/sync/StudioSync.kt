/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.offline.sync

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import com.instructure.canvasapi2.apis.LaunchDefinitionsAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.LaunchDefinition
import com.instructure.canvasapi2.models.StudioLoginSession
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.poll
import com.instructure.pandautils.views.CanvasWebView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.net.URL

class StudioSync(
    private val context: Context,
    private val launchDefinitionsApi: LaunchDefinitionsAPI.LaunchDefinitionsInterface,
    private val apiPrefs: ApiPrefs
) {

    public suspend fun syncStudioVideos() {
        val studioSession = authenticateStudio()
        Log.d("asdasd", "Studio session: $studioSession")
    }

    private suspend fun authenticateStudio(): StudioLoginSession? {
        val launchDefinitions = launchDefinitionsApi.getLaunchDefinitions(RestParams(isForceReadFromNetwork = true)).dataOrNull.orEmpty()
        val studioLaunchDefinition = launchDefinitions.firstOrNull {
            it.domain == LaunchDefinition.STUDIO_DOMAIN
        } ?: return null

        val studioUrl = "${apiPrefs.fullDomain}/api/v1/accounts/self/external_tools/sessionless_launch?url=${studioLaunchDefinition.url}"
        val studioLti = launchDefinitionsApi.getLtiFromAuthenticationUrl(studioUrl, RestParams(isForceReadFromNetwork = true)).dataOrNull ?: return null

        return studioLti.url?.let {
            val webView = withTimeoutOrNull(10000) { loadUrlIntoHeadlessWebView(context, it) }
            if (webView == null) return null

            // Get base url for Studio api calls
            val url = URL(studioLaunchDefinition.url)
            val baseUrl = "${url.protocol}://${url.host}"

            poll(block = {
                val token = webView.evaluateJavascriptSuspend("sessionStorage.getItem('token')")
                val userId = webView.evaluateJavascriptSuspend("sessionStorage.getItem('userId')")
                StudioLoginSession(userId, token, baseUrl)
            }, validate = {
                it.userId != "null" && it.token != "null"
            })
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun loadUrlIntoHeadlessWebView(context: Context, url: String): WebView = suspendCancellableCoroutine { continuation ->
        Handler(Looper.getMainLooper()).post {
            val webView = CanvasWebView(context)
            webView.settings.javaScriptEnabled = true
            webView.loadUrl(url)
            val webViewClient = webView.webViewClient
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if (url?.contains("login") == true) {
                        webView.webViewClient = webViewClient
                        continuation.resume(webView, null)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun WebView.evaluateJavascriptSuspend(script: String): String = suspendCancellableCoroutine { continuation ->
    Handler(Looper.getMainLooper()).post {
        this.evaluateJavascript(script) { result ->
            continuation.resume(result, null)
        }
    }
}