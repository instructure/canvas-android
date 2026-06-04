/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.pandautils.R
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.URLDecoder

class HtmlContentFormatterTest {

    private val context: Context = mockk(relaxed = true)
    private val crashlytics: FirebaseCrashlytics = mockk(relaxed = true)
    private val oAuthManager: OAuthManager = mockk(relaxed = true)
    private val featureFlagProvider: FeatureFlagProvider = mockk(relaxed = true)

    private lateinit var formatter: HtmlContentFormatter

    @Before
    fun setUp() {
        every { context.getString(R.string.loadFullContent) } returns "Load Full Content"
        formatter = HtmlContentFormatter(context, crashlytics, oAuthManager, featureFlagProvider)
    }

    // C1 — iframeWithLink: URL with single quote must not break out of JS string context

    @Test
    fun `iframeWithLink encodes single-quote so it cannot break out of JS string`() {
        val malicious = "https://example.com/x'); alert(document.cookie); //"
        val iframe = "<iframe src=\"$malicious\" style=\"overflow: scroll\"></iframe>"

        val result = formatter.iframeWithLink(malicious, iframe, context)
        val onClick = extractOnClickValue(result)

        assertFalse(
            "Raw single quote must not appear inside the JS string literal",
            onClick.contains("'); alert")
        )
        assertTrue(
            "URL-encoded single quote (%27) must be present in the onClick handler",
            onClick.contains("%27")
        )
        assertTrue(
            "onClick must wrap the encoded URL in decodeURIComponent so navigation is recoverable at runtime",
            onClick.startsWith("location.href=decodeURIComponent('")
        )
    }

    @Test
    fun `iframeWithLink encoded URL round-trips back to original via decodeURIComponent semantics`() {
        val originalUrl = "https://example.com/path?q=hello world&a=b'c"
        val iframe = "<iframe src=\"$originalUrl\" style=\"overflow: scroll\"></iframe>"

        val result = formatter.iframeWithLink(originalUrl, iframe, context)
        val encoded = extractBetween(result, "decodeURIComponent('", "')\"")

        assertEquals(originalUrl, URLDecoder.decode(encoded, "UTF-8"))
        assertFalse(
            "URLEncoder produces '+' for spaces but decodeURIComponent does not decode '+'; we must emit %20",
            encoded.contains("+")
        )
    }

    // C1 — iframeWithGoogleDocsButton: URL with single quote must not break out

    @Test
    fun `iframeWithLink rejects javascript scheme and returns the iframe untouched`() {
        val malicious = "javascript:alert(document.cookie)"
        val iframe = "<iframe src=\"$malicious\" style=\"overflow: scroll\"></iframe>"

        val result = formatter.iframeWithLink(malicious, iframe, context)

        assertEquals(iframe, result)
        assertFalse(
            "No onClick button must be appended when scheme is not http(s)",
            result.contains("onClick=")
        )
    }

    @Test
    fun `iframeWithStudioEmbed rejects javascript scheme and returns the iframe untouched`() {
        val malicious = "javascript:alert(1)"
        val iframe = "<iframe src=\"$malicious\"></iframe>"

        val result = formatter.iframeWithStudioEmbed(malicious, iframe)

        assertEquals(iframe, result)
        assertFalse(
            "No onClick button must be appended when scheme is not http(s)",
            result.contains("onClick=")
        )
    }

    @Test
    fun `iframeWithGoogleDocsButton rejects javascript scheme and returns the iframe untouched`() {
        val malicious = "javascript:alert(1)"
        val iframe = "<iframe src=\"$malicious\"></iframe>"

        val result = formatter.iframeWithGoogleDocsButton(malicious, iframe, "Open")

        assertEquals(iframe, result)
        assertFalse(
            "No onClick button must be appended when scheme is not http(s)",
            result.contains("onClick=")
        )
    }

    @Test
    fun `iframeWithGoogleDocsButton encodes spaces as percent-20 not plus for consistency with siblings`() {
        val urlWithSpace = "https://docs.google.com/path with space"
        val iframe = "<iframe src=\"$urlWithSpace\"></iframe>"

        val result = formatter.iframeWithGoogleDocsButton(urlWithSpace, iframe, "Open")
        val onClick = extractOnClickValue(result)

        assertFalse(
            "Helpers must agree on percent-encoding form for spaces; emit %20 everywhere",
            onClick.contains("+")
        )
        assertTrue(
            "Spaces must be percent-encoded as %20",
            onClick.contains("%20")
        )
    }

    @Test
    fun `iframeWithGoogleDocsButton encodes single-quote so it cannot break out of JS string`() {
        val malicious = "https://docs.google.com/a'); alert(document.cookie); //"
        val iframe = "<iframe src=\"$malicious\"></iframe>"

        val result = formatter.iframeWithGoogleDocsButton(malicious, iframe, "Open")
        val onClick = extractOnClickValue(result)

        assertFalse(
            "Raw single quote must not appear inside the JS string literal",
            onClick.contains("'); alert")
        )
        assertTrue(
            "URL-encoded single quote (%27) must appear in the onClick handler",
            onClick.contains("%27")
        )
        assertTrue(
            "onClick must still invoke the googleDocs JS bridge",
            onClick.startsWith("googleDocs.onGoogleDocsButtonPressed('")
        )
    }

    // C1 — iframeWithStudioEmbed: URL with single quote must not break out

    @Test
    fun `iframeWithStudioEmbed encodes single-quote so it cannot break out of JS string`() {
        val malicious = "https://canvas.instructure.com/courses/1/external_tools/retrieve?url=a'); alert(1); //"
        val iframe = "<iframe src=\"$malicious\"></iframe>"

        val result = formatter.iframeWithStudioEmbed(malicious, iframe)
        val onClick = extractOnClickValue(result)

        assertFalse(
            "Raw single quote must not appear inside the JS string literal",
            onClick.contains("'); alert")
        )
        assertTrue(
            "URL-encoded single quote (%27) must appear in the onClick handler",
            onClick.contains("%27")
        )
        assertTrue(
            "onClick must wrap the encoded URL in decodeURIComponent",
            onClick.startsWith("location.href=decodeURIComponent('")
        )
    }

    @Test
    fun `iframeWithStudioEmbed encoded URL round-trips back to original via decodeURIComponent semantics`() {
        val originalUrl = "https://canvas.example.com/external_tools/retrieve?url=https%3A%2F%2Fstudio.test&q=a b"
        val iframe = "<iframe src=\"$originalUrl\"></iframe>"

        val result = formatter.iframeWithStudioEmbed(originalUrl, iframe)
        val encoded = extractBetween(result, "decodeURIComponent('", "')\"")

        assertEquals(originalUrl, URLDecoder.decode(encoded, "UTF-8"))
        assertFalse(
            "URLEncoder produces '+' for spaces but decodeURIComponent does not decode '+'; we must emit %20",
            encoded.contains("+")
        )
    }

    private fun extractOnClickValue(html: String): String = extractBetween(html, "onClick=\"", "\">")

    private fun extractBetween(haystack: String, prefix: String, suffix: String): String {
        val start = haystack.indexOf(prefix).also {
            assertTrue("expected prefix '$prefix' in output", it >= 0)
        } + prefix.length
        val end = haystack.indexOf(suffix, start).also {
            assertTrue("expected suffix '$suffix' in output", it >= 0)
        }
        return haystack.substring(start, end)
    }
}
