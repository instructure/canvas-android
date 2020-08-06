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
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.crashlytics.android.Crashlytics
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.R
import com.instructure.pandautils.discussions.DiscussionHtmlTemplates
import com.instructure.pandautils.views.CanvasWebView
import kotlinx.coroutines.Job
import java.net.URLEncoder
import java.util.regex.Pattern

/**
 * WebView helper function for handling all iframe related cases
 *
 * This currently handles three iframe cases:
 *   -cnvs_content src authentication
 *   -lti iframe src auth and launch button
 *   -new rce videos in iframes
 *
 * We should now be able to call this function, preceded by a simple check for iframes, for all html webview content
 */
fun WebView.loadHtmlWithIframes(context: Context, isTablet: Boolean, html: String, loadHtml: (newHtml: String, contentDescription: String?) -> Unit, jsCallback: ((ltiUrl: String) -> Unit)? = null, contentDescription: String? = null): Job? {
    if(html.contains("<iframe")) {
        return this.tryWeave {
            var hasLtiTool = false
            var newHTML: String = html

            // First we need to find LTIs by looking for iframes
            val iframeMatcher = Pattern.compile("<iframe(.|\\n)*?iframe>").matcher(html)

            while (iframeMatcher.find()) {
                val iframe = iframeMatcher.group(0)
                // We found an iframe, we need to do a few things...
                val matcher = Pattern.compile("src=\"([^\"]+)\"").matcher(iframe)
                // First we find the src
                if (matcher.find()) {
                    // Snag that src
                    val srcUrl = matcher.group(1)

                    if (srcUrl.contains("external_tools")) {
                        // Handle the LTI case
                        hasLtiTool = true
                        val newIframe = inBackground { externalToolIframe(srcUrl, iframe, context); }
                        newHTML = newHTML.replace(iframe, newIframe)
                    } else if(srcUrl.contains("media_objects_iframe")) {
                        // Handle the new RCE iframe case
                        val dataMediaIdMatcher = Pattern.compile("data-media-id=\"([^\"]+)\"").matcher(iframe)
                        if (dataMediaIdMatcher.find()) {
                            val dataMediaId = dataMediaIdMatcher.group(1)

                            val newIframe = newRceVideoElement(dataMediaId);
                            newHTML = newHTML.replace(iframe, newIframe)
                        }
                    } else if(iframe.contains("id=\"cnvs_content\"")) {
                        // Handle the cnvs_content special case for some schools
                        val authenticatedUrl = inBackground { authenticateLTIUrl(srcUrl) }
                        val newIframe = iframe.replace(srcUrl, authenticatedUrl)

                        newHTML = newHTML.replace(iframe, newIframe)
                    }
                }
            }

            val document = DiscussionHtmlTemplates.getTopicHeader(context)
            newHTML = document.replace("__HEADER_CONTENT__", newHTML)
            newHTML = newHTML.replace("__LTI_BUTTON_WIDTH__", if (isTablet) "320px" else "100%")
            newHTML = newHTML.replace("__LTI_BUTTON_MARGIN__", if (isTablet) "0px" else "auto")

            // Add the JS interface
            if(hasLtiTool && jsCallback != null) {
                // Its possible, i.e. for discussions, that the js interface is already configured
                this@loadHtmlWithIframes.addJavascriptInterface(JsExternalToolInterface(jsCallback), "accessor")
            }

            loadHtml(CanvasWebView.applyWorkAroundForDoubleSlashesAsUrlSource(handleBlankTarget(newHTML)), contentDescription)
        } catch {
            Crashlytics.logException(it)
            Logger.e("loadHtmlWithIframe caught an exception: " + it.message)
        }
    } else {
        loadHtml(handleBlankTarget(html), contentDescription)
        return null
    }
}

suspend fun externalToolIframe(srcUrl: String, iframe: String, context: Context): String {
    // We need to authenticate the src url and replace it within the iframe
    val ltiUrl = URLEncoder.encode(srcUrl, "UTF-8")

    val authenticatedUrl = authenticateLTIUrl(srcUrl)

    // Now we need to replace the iframes src url with the authenticated url
    val newIframe = iframe.replace(srcUrl, authenticatedUrl)

    // With that done, we need to make the LTI launch button
    val button = "</br><p><div class=\"lti_button\" onClick=\"onLtiToolButtonPressed('%s')\">%s</div></p>"
    val htmlButton = String.format(button, ltiUrl, context.resources.getString(R.string.utils_launchExternalTool))

    // Now we add the launch button along with the new iframe with the updated URL
    return newIframe + htmlButton
}

fun newRceVideoElement(dataMediaId: String): String {
    // We need to make a new src url with the dataMediaId
    val newSrcUrl = "/users/self/media_download?entryId=$dataMediaId&media_type=video&redirect=1"

    // We can't just update the src url in the iframe, as iframes always auto load/play the src
    return """
        <video controls poster=/media_objects/$dataMediaId/thumbnail?width=550&height=448'>
            <source src="$newSrcUrl" type="video/mp4">
        </video>
    """.trimIndent()
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

/**
 * Goal is to strip target="_blank" from a tags i.e. <a href="https://google.com" target="_blank">
 *
 * This enables us to keep canvasWebView.settings.setSupportMultipleWindows(true) set to true
 * but still keep RCE image/links working properly in a webview
 */
fun handleBlankTarget(html: String): String {
    if(html.contains("<a href")) {
        val matcher = Pattern.compile("<a href=\"([^\"]+)\" target=\"_blank\">").matcher(html)

        while(matcher.find()) {
            val originalLink = matcher.group(0)
            val srcUrl = matcher.group(1)

            val newLink = "<a href=\"$srcUrl\">"
            return html.replace(originalLink, newLink)
        }

        return html
    } else {
        return html
    }
}


suspend fun authenticateLTIUrl(ltiUrl: String): String {
    return awaitApi<AuthenticatedSession> { OAuthManager.getAuthenticatedSession(ltiUrl, it) }.sessionUrl
}

data class Placeholder(val iframeHtml: String, val placeHolderHtml: String)

class JsExternalToolInterface(val callback: (ltiUrl: String) -> Unit) {
    @Suppress("UNUSED_PARAMETER")
    @JavascriptInterface
    fun onLtiToolButtonPressed(ltiUrl: String) {
        callback(ltiUrl)
    }
}
