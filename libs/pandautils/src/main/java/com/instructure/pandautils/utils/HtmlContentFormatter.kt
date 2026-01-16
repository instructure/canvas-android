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
import com.instructure.canvasapi2.utils.weave.apiAsync
import com.instructure.pandautils.R
import com.instructure.pandautils.discussions.DiscussionHtmlTemplates
import com.instructure.pandautils.views.CanvasWebView
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.regex.Pattern

/**
 * Class that handles different kind of html content modifications, in case we have Iframes,
 * because the Android WebView can't handle it well. Implementation is based on the [WebWiewExtensions.kt]
 */
class HtmlContentFormatter(
    private val context: Context,
    private val crashlytics: FirebaseCrashlytics,
    private val oAuthManager: OAuthManager,
    private val featureFlagProvider: FeatureFlagProvider
) {

    suspend fun formatHtmlWithIframes(html: String, courseId: Long? = null): String {
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

                        // Check Studio embed URLs first (before generic external_tools check)
                        // because Studio embeds also contain "external_tools" in their URL
                        if (hasStudioEmbedUrl(srcUrl)) {
                            val studioEmbedImprovementsEnabled = courseId?.let {
                                featureFlagProvider.checkStudioEmbedImprovementsFlag(it)
                            } ?: false

                            if (studioEmbedImprovementsEnabled) {
                                val videoTitle = extractVideoTitle(iframe)
                                val immersiveUrl = convertStudioEmbedToImmersiveView(srcUrl, videoTitle)
                                val newIframe = iframeWithStudioButton(immersiveUrl, iframe, context)
                                newHTML = newHTML.replace(iframe, newIframe)
                            }
                        } else if (hasStudioMediaUrl(srcUrl)) {
                            // Only check feature flag if we actually have a Studio URL
                            val studioEmbedImprovementsEnabled = courseId?.let {
                                featureFlagProvider.checkStudioEmbedImprovementsFlag(it)
                            } ?: false

                            if (studioEmbedImprovementsEnabled) {
                                val videoTitle = extractVideoTitle(iframe)
                                val immersiveUrl = convertToImmersiveViewUrl(srcUrl, videoTitle)
                                val newIframe = iframeWithStudioButton(immersiveUrl, iframe, context)
                                newHTML = newHTML.replace(iframe, newIframe)
                            }
                        } else if (hasExternalTools(srcUrl)) {
                            // Handle the generic LTI case (after checking for specific Studio types)
                            val newIframe = externalToolIframe(srcUrl, iframe, context)
                            newHTML = newHTML.replace(iframe, newIframe)
                        } else if (iframe.contains("id=\"cnvs_content\"")) {
                            // Handle the cnvs_content special case for some schools
                            val authenticatedUrl = authenticateLTIUrl(srcUrl)
                            val newIframe = iframe.replace(srcUrl, authenticatedUrl)

                            newHTML = newHTML.replace(iframe, newIframe)
                        }

                        if (iframe.contains("overflow: scroll")) {
                            val newIframe = iframeWithLink(srcUrl, iframe, context)
                            newHTML = newHTML.replace(iframe, newIframe)
                        }

                        if (hasGoogleDocsUrl(srcUrl)) {
                            val newIframe = iframeWithGoogleDocsButton(srcUrl, iframe, context.getString(R.string.openLtiInExternalApp))
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
        val ltiResult = apiAsync<AuthenticatedSession> { oAuthManager.getAuthenticatedSession(ltiUrl, it) }.await()
        return if (ltiResult.isSuccess) {
            return ltiResult.dataOrNull?.sessionUrl ?: ltiUrl
        } else {
            ltiUrl
        }
    }

    private fun iframeWithLink(srcUrl: String, iframe: String, context: Context): String {
        val buttonText = context.getString(R.string.loadFullContent)
        val htmlButton = "</br><p><div class=\"lti_button\" onClick=\"location.href=\'$srcUrl\'\">$buttonText</div></p>"

        return iframe + htmlButton
    }

    private fun iframeWithGoogleDocsButton(srcUrl: String, iframe: String, buttonText: String): String {
        val button = "</br><p><div class=\"lti_button\" onClick=\"googleDocs.onGoogleDocsButtonPressed('%s')\">%s</div></p>"
        val htmlButton = String.format(button, srcUrl, buttonText)
        return iframe + htmlButton
    }

    private fun convertToImmersiveViewUrl(srcUrl: String, title: String?): String {
        val baseUrl = srcUrl.replace("media_attachments_iframe", "media_attachments")
            .plus("/immersive_view?embedded=true")
        return if (title != null) {
            val encodedTitle = URLEncoder.encode(title, "UTF-8")
            "$baseUrl&title=$encodedTitle"
        } else {
            baseUrl
        }
    }

    private fun iframeWithStudioButton(immersiveUrl: String, iframe: String, context: Context): String {
        val buttonText = context.getString(R.string.openInDetailView)
        val escapedUrl = immersiveUrl.replace("&", "&amp;")

        val htmlButton = "</br><p><div class=\"lti_button\" onClick=\"location.href='$escapedUrl'\">$buttonText</div></p>"
        return iframe + htmlButton
    }

    private fun extractVideoTitle(iframe: String): String? {
        // Try to find title attribute: title="Video player for filename.mp4"
        val titlePattern = Pattern.compile("title=\"([^\"]+)\"")
        val titleMatcher = titlePattern.matcher(iframe)
        if (titleMatcher.find()) {
            val fullTitle = titleMatcher.group(1) ?: return null
            // Extract just the filename part after "Video player for "
            return fullTitle.replace("Video player for ", "").replace(".mp4", "")
        }

        // Fallback: try aria-label
        val ariaPattern = Pattern.compile("aria-label=\"([^\"]+)\"")
        val ariaMatcher = ariaPattern.matcher(iframe)
        if (ariaMatcher.find()) {
            return ariaMatcher.group(1)
        }

        return null
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
                if (newUrl?.contains("external_tools") == true) {
                    newHTML = html.replace(newUrl, authenticatedSessionUrl)
                }
            }
        }
        return newHTML
    }

    private fun convertStudioEmbedToImmersiveView(srcUrl: String, title: String?): String {
        // Normalize HTML entities before processing
        val normalizedUrl = srcUrl.replace("&amp;", "&")

        // Extract the base URL and the encoded url parameter
        val urlPattern = Pattern.compile("url=([^&]+)")
        val urlMatcher = urlPattern.matcher(normalizedUrl)

        if (urlMatcher.find()) {
            val encodedLtiUrl = urlMatcher.group(1) ?: return srcUrl

            // Decode the LTI URL to modify it
            var decodedLtiUrl = URLDecoder.decode(encodedLtiUrl, "UTF-8")

            // Replace launch type with immersive_view
            decodedLtiUrl = decodedLtiUrl
                .replace("custom_arc_launch_type=thumbnail_embed", "custom_arc_launch_type=immersive_view")
                .replace("custom_arc_launch_type=learn_embed", "custom_arc_launch_type=immersive_view")

            // Add source view type based on original embed type
            val sourceViewType = when {
                encodedLtiUrl.contains("thumbnail_embed") -> "thumbnail_embed"
                encodedLtiUrl.contains("learn_embed") -> "learn_embed"
                else -> "collaboration_embed"
            }

            if (!decodedLtiUrl.contains("custom_arc_source_view_type")) {
                decodedLtiUrl += "&custom_arc_source_view_type=$sourceViewType"
            }

            // Add platform redirect URL if not present
            if (!decodedLtiUrl.contains("platform_redirect_url")) {
                val baseCanvasUrl = srcUrl.substringBefore("/external_tools")
                val encodedRedirectUrl = URLEncoder.encode(baseCanvasUrl, "UTF-8")
                decodedLtiUrl += "&platform_redirect_url=$encodedRedirectUrl"
            }

            // Add full_win_launch_requested parameter
            if (!decodedLtiUrl.contains("full_win_launch_requested")) {
                decodedLtiUrl += "&full_win_launch_requested=1"
            }

            // Re-encode the modified LTI URL
            val newEncodedLtiUrl = URLEncoder.encode(decodedLtiUrl, "UTF-8")

            // Build the final immersive view URL
            val baseUrl = srcUrl.substringBefore("?")
            var immersiveUrl = "$baseUrl?display=full_width&url=$newEncodedLtiUrl&placement=course_navigation&embedded=true"

            // Add title if present
            if (title != null) {
                val encodedTitle = URLEncoder.encode(title, "UTF-8")
                immersiveUrl += "&title=$encodedTitle"
            }

            return immersiveUrl
        }

        return srcUrl
    }

    companion object {
        fun hasGoogleDocsUrl(text: String?) = text?.contains("docs.google.com").orDefault()
        fun hasKalturaUrl(text: String?) = text?.contains("kaltura.com").orDefault()
        fun hasExternalTools(text: String?) = text?.contains("external_tools").orDefault()
        fun hasStudioMediaUrl(text: String?) = text?.contains("media_attachments_iframe").orDefault()
        fun hasStudioEmbedUrl(text: String?): Boolean {
            if (text?.contains("external_tools/retrieve").orDefault().not()) return false

            // Check for thumbnail_embed or learn_embed (excluding collaboration_embed)
            // URLs extracted from HTML may still contain &amp; entities
            // The launch type parameters are URL-encoded within the nested url parameter
            val normalizedText = text?.replace("&amp;", "&") ?: ""
            return (normalizedText.contains("custom_arc_launch_type%3Dthumbnail_embed") ||
                    normalizedText.contains("custom_arc_launch_type%3Dlearn_embed"))
        }
    }
}

fun String.replaceWithURLQueryParameter(ifSatisfies: Boolean = true): String {
    val urlQueryParameter = this.substringAfter("url=").substringBefore('&')
    return if (ifSatisfies) {
        urlQueryParameter
    } else {
        this
    }
}