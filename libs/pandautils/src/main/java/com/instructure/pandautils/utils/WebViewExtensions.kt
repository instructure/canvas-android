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

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.utils.weave.weave
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
    html: String?,
    loadHtml: (newHtml: String) -> Unit,
    onLtiButtonPressed: ((ltiUrl: String) -> Unit)? = null,
): Job {
    return weave {
        val formatter = HtmlContentFormatter(context, FirebaseCrashlytics.getInstance(), OAuthManager)

        if (HtmlContentFormatter.hasExternalTools(html) && onLtiButtonPressed != null) {
            addJavascriptInterface(JsExternalToolInterface(onLtiButtonPressed), "accessor")
        }

        if (HtmlContentFormatter.hasGoogleDocsUrl(html)) {
            addJavascriptInterface(JsGoogleDocsInterface(context), "accessor")
        }

        loadHtml(formatter.formatHtmlWithIframes(html.orEmpty()))
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
        callback(ltiUrl)
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

fun WebView.setDarkModeSupport(webThemeDarkeningOnly: Boolean = false) {
    if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
        val nightModeFlags: Int = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_ON)
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
                setForceDarkStrategy(webThemeDarkeningOnly, settings)
            }
        } else {
            WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_OFF)
        }
    }
}

private fun setForceDarkStrategy(webThemeDarkeningOnly: Boolean, settings: WebSettings) {
    if (webThemeDarkeningOnly) {
        WebSettingsCompat.setForceDarkStrategy(settings, WebSettingsCompat.DARK_STRATEGY_WEB_THEME_DARKENING_ONLY)
    } else {
        WebSettingsCompat.setForceDarkStrategy(settings, WebSettingsCompat.DARK_STRATEGY_PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING)
    }
}
