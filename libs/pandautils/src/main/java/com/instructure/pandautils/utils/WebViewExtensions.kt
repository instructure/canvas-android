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
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.HttpHelper
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.R
import com.instructure.pandautils.discussions.DiscussionHtmlTemplates
import com.instructure.pandautils.views.CanvasWebView
import kotlinx.coroutines.Job
import org.json.JSONObject
import java.net.URLEncoder
import java.util.regex.Pattern

fun WebView.loadHtmlWithLTIs(context: Context, isTablet: Boolean, html: String, loadHtml: (newUrl: String) -> Unit): Job? {
    return this.tryWeave {
        var newHTML: String = html

        // First we need to find LTIs by looking for iframes
        val iframeMatcher = Pattern.compile("<iframe(.|\\n)*?iframe>").matcher(html)

        while (iframeMatcher.find()) { // TODO find fix for duplicates?
            val iframe = iframeMatcher.group(0)
            if (iframe.contains("external_tool")) {
                // We found an LTI tool, we need to do a few things...
                val matcher = Pattern.compile("src=\"([^\"]+)\"").matcher(iframe)
                // First we find the src
                if (matcher.find()) {
                    val url = matcher.group(1)
                    // Make sure this REALLY is an LTI src, this check might need to be upgraded in the future...
                    if (url.contains("external_tools")) {
                        // We need to authenticate the src url and replace it within the iframe
                        var authenticatedUrl: String? = null
                        val ltiUrl = URLEncoder.encode(url, "UTF-8")

                        inBackground {
                            authenticateLTIUrl(context, url) {
                                authenticatedUrl = it
                            }
                        }

                        // Now we need to replace the iframes src url with the authenticated url
                        val newIframe = iframe.replace(url.orEmpty(), authenticatedUrl.orEmpty())

                        // With that done, we need to make the LTI launch button
                        val button = "</br><p><div class=\"lti_button\" onClick=\"onLtiToolButtonPressed('%s')\">%s</div></p>"
                        val htmlButton = String.format(button, ltiUrl, context.resources.getString(R.string.utils_launchExternalTool))

                        // Nowe we add the launch button along with the new iframe with the updated URL
                        newHTML = newHTML.replace(iframe, newIframe + htmlButton)
                    }
                }
            }
        }

        val document = DiscussionHtmlTemplates.getTopicHeader(context)
        newHTML = document.replace("__HEADER_CONTENT__", newHTML)
        newHTML = newHTML.replace("__LTI_BUTTON_WIDTH__", if (isTablet) "320px" else "100%")
        newHTML = newHTML.replace("__LTI_BUTTON_MARGIN__", if (isTablet) "0px" else "auto")

        loadHtml(CanvasWebView.applyWorkAroundForDoubleSlashesAsUrlSource(newHTML))
    } catch {
        Logger.d("loadHtmlWithLTIs caught an exception: " + it.message)
    }
}

fun handleLTIPlaceHolders(placeHolderList: ArrayList<Placeholder>, html: String): String {
    var newHtml = html
    for(holder in placeHolderList) {
        if(newHtml.contains(holder.placeHolderHtml)) {
            newHtml = newHtml.replace(holder.placeHolderHtml, holder.iframeHtml)
        }
    }

    return newHtml
}

fun authenticateLTIUrl(context: Context, ltiUrl: String, callback: (ltiUrl: String?) -> Unit) {
    val newUrl = ApiPrefs.fullDomain + "/api/v1/accounts/self/external_tools/sessionless_launch?url=" + ltiUrl
    var result: String? = null

    val response = HttpHelper.externalHttpGet(context, newUrl, true).responseBody
    if (response != null) {
        val ltiJSON = JSONObject(response)
        result = ltiJSON.getString("url")
    }

    if (result != null) {
        val uri = Uri.parse(result).buildUpon()
                .appendQueryParameter("display", "borderless")
                .appendQueryParameter("platform", "android")
                .build()
        callback(uri.toString())
    } else {
        callback(null)
    }
}

data class Placeholder(val iframeHtml: String, val placeHolderHtml: String)


class JsExternalToolInterface(val callback: (ltiUrl: String) -> Unit) {
    @Suppress("UNUSED_PARAMETER")
    @JavascriptInterface
    fun onLtiToolButtonPressed(ltiUrl: String) {
        callback(ltiUrl)
    }
}
