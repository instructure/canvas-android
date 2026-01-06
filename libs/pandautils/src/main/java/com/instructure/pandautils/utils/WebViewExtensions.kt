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
package com.instructure.pandautils.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.pandautils.R
import com.instructure.pandautils.views.CanvasWebView
import kotlinx.coroutines.Job

/**
 * WebView helper function for handling all iframe related cases
 *
 * This currently handles three iframe cases:
 *   -cnvs_content src authentication
 *   -lti iframe src auth and launch button
 *
 * We should now be able to call this function, preceded by a simple check for iframes, for all html webview content
 */
fun WebView.loadHtmlWithIframes(
    context: Context,
    featureFlagProvider: FeatureFlagProvider,
    html: String?,
    loadHtml: (newHtml: String) -> Unit,
    onLtiButtonPressed: ((ltiUrl: String) -> Unit)? = null,
    courseId: Long? = null,
): Job {
    return weave {
        val formatter = HtmlContentFormatter(context, FirebaseCrashlytics.getInstance(), OAuthManager, featureFlagProvider)

        if (HtmlContentFormatter.hasExternalTools(html) && onLtiButtonPressed != null) {
            addJavascriptInterface(JsExternalToolInterface(onLtiButtonPressed), Const.LTI_TOOL)
        }

        if (HtmlContentFormatter.hasGoogleDocsUrl(html)) {
            addJavascriptInterface(JsGoogleDocsInterface(context), Const.GOOGLE_DOCS)
        }

        loadHtml(formatter.formatHtmlWithIframes(html.orEmpty(), courseId))
    }
}

fun handleLTIPlaceHolders(placeHolderList: ArrayList<Placeholder>, html: String): String {
    var newHtml = html
    for (holder in placeHolderList) {
        if (newHtml.contains(holder.placeHolderHtml)) {
            newHtml = newHtml.replace(holder.placeHolderHtml, holder.iframeHtml)
        }
    }

    return newHtml
}

data class Placeholder(val iframeHtml: String, val placeHolderHtml: String)

@Suppress("UNUSED_PARAMETER")
class JsExternalToolInterface(private val callback: (ltiUrl: String) -> Unit) {
    @JavascriptInterface
    fun onLtiToolButtonPressed(ltiUrl: String) {
        val isOnline = Utils.isNetworkAvailable(ContextKeeper.appContext)
        if (isOnline) {
            callback(ltiUrl)
        } else {
            android.widget.Toast.makeText(ContextKeeper.appContext, R.string.ltiToolsOffline, android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}

@Suppress("UNUSED_PARAMETER")
class JsGoogleDocsInterface(private val context: Context) {
    @JavascriptInterface
    fun onGoogleDocsButtonPressed(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }
}

fun WebView.enableAlgorithmicDarkening() {
    if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
        WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, true)
    }
}

@SuppressLint("SetJavaScriptEnabled")
fun loadUrlIntoHeadlessWebView(context: Context, url: String) {
    val webView = CanvasWebView(context)
    webView.settings.javaScriptEnabled = true
    webView.loadUrl(url)
}
