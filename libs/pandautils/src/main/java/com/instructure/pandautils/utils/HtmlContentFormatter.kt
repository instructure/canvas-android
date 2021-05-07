/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.pandautils.utils

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.pandautils.R
import com.instructure.pandautils.discussions.DiscussionHtmlTemplates
import com.instructure.pandautils.views.CanvasWebView
import java.net.URLEncoder
import java.util.regex.Pattern

/**
 * Class that handles different kind of html content modifications, in case we have Iframes,
 * because the Android WebView can't handle it well. Implementation is based on the [WebWiewExtensions.kt]
 */
class HtmlContentFormatter(
    private val context: Context,
    private val crashlytics: FirebaseCrashlytics,
    private val oAuthManager: OAuthManager
) {

    suspend fun formatHtmlWithIframes(html: String): String {
        try {
            if (html.contains("<iframe")) {
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
                            val newIframe = externalToolIframe(srcUrl, iframe, context)
                            newHTML = newHTML.replace(iframe, newIframe)
                        } else if (srcUrl.contains("media_objects_iframe")) {
                            // Handle the new RCE iframe case
                            val dataMediaIdMatcher = Pattern.compile("data-media-id=\"([^\"]+)\"").matcher(iframe)
                            if (dataMediaIdMatcher.find()) {
                                val dataMediaId = dataMediaIdMatcher.group(1)

                                val newIframe = newRceVideoElement(dataMediaId);
                                newHTML = newHTML.replace(iframe, newIframe)
                            }
                        } else if (iframe.contains("id=\"cnvs_content\"")) {
                            // Handle the cnvs_content special case for some schools
                            val authenticatedUrl = authenticateLTIUrl(srcUrl)
                            val newIframe = iframe.replace(srcUrl, authenticatedUrl)

                            newHTML = newHTML.replace(iframe, newIframe)
                        }
                    }
                }

                val document = DiscussionHtmlTemplates.getTopicHeader(context)
                val isTablet = context.resources.getBoolean(R.bool.isDeviceTablet)

                newHTML = document.replace("__HEADER_CONTENT__", newHTML)
                newHTML = newHTML.replace("__LTI_BUTTON_WIDTH__", if (isTablet) "320px" else "100%")
                newHTML = newHTML.replace("__LTI_BUTTON_MARGIN__", if (isTablet) "0px" else "auto")

                return CanvasWebView.applyWorkAroundForDoubleSlashesAsUrlSource(newHTML)
            } else {
                return html
            }
        } catch (e: Exception) {
            crashlytics.recordException(e)
            return html
        }
    }

    private suspend fun externalToolIframe(srcUrl: String, iframe: String, context: Context): String {
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

    private suspend fun authenticateLTIUrl(ltiUrl: String): String {
        return awaitApi<AuthenticatedSession> { oAuthManager.getAuthenticatedSession(ltiUrl, it) }.sessionUrl
    }

    private fun newRceVideoElement(dataMediaId: String): String {
        // We need to make a new src url with the dataMediaId
        val newSrcUrl = "/users/self/media_download?entryId=$dataMediaId&media_type=video&redirect=1"

        // We can't just update the src url in the iframe, as iframes always auto load/play the src
        return """
        <video controls poster=/media_objects/$dataMediaId/thumbnail?width=550&height=448'>
            <source src="$newSrcUrl" type="video/mp4">
        </video>
    """.trimIndent()
    }

    fun createAuthenticatedLtiUrl(html: String, authenticatedSessionUrl: String?): String {
        if (authenticatedSessionUrl == null) return html
        // Now we need to swap out part of the old url for this new authenticated url
        val matcher = Pattern.compile("src=\"([^;]+)").matcher(html)
        var newHTML: String = html
        if (matcher.find()) {
            // We only want to change the urls that are part of an external tool, not everything (like avatars)
            for (index in 0..matcher.groupCount()) {
                val newUrl = matcher.group(index)
                if (newUrl.contains("external_tools")) {
                    newHTML = html.replace(newUrl, authenticatedSessionUrl)
                }
            }
        }
        return newHTML
    }
}