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
package com.instructure.horizon.offline

object InlineRceMedia {

    private const val LOCAL_SCHEME = "file://"

    private val localSrcRegex = Regex("""src\s*=\s*"($LOCAL_SCHEME[^"]*)"""")

    fun findLocalImageSrcs(html: String?): List<String> {
        if (html.isNullOrEmpty()) return emptyList()
        return localSrcRegex.findAll(html)
            .map { it.groupValues[1] }
            .distinct()
            .toList()
    }

    fun rewriteLocalImageSrc(html: String, localSrc: String, realUrl: String): String {
        return html.replace("src=\"$localSrc\"", "src=\"$realUrl\"")
    }
}
