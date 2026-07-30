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

/**
 * Returns true when [html] contains no visible content.
 *
 * The Rich Content Editor leaves residual markup (e.g. `<br>`, `<p></p>`) in an
 * otherwise-empty field. This helper strips those artefacts before deciding whether
 * the string is blank, so callers can make consistent save / enable decisions without
 * duplicating the stripping logic.
 *
 * Treated as blank:
 * - Self-closing and space-variant `<br>` tags: `<br>`, `<br/>`, `<br />`
 * - Empty paragraph tags: `<p></p>`
 * - Non-breaking space entities: `&nbsp;`
 * - Regular whitespace
 */
fun isHtmlBlank(html: String): Boolean {
    return html
        .replace(Regex("<br\\s*/?>"), "")
        .replace("<p></p>", "")
        .replace("&nbsp;", "")
        .isBlank()
}
