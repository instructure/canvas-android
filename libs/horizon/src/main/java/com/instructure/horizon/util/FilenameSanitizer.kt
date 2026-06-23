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
package com.instructure.horizon.util

private const val MAX_FILENAME_LENGTH = 80
private val UNSAFE_FILENAME_CHARS = Regex("[/\\\\:*?\"<>|\\u0000-\\u001F]")

fun String.sanitizeAsFilename(): String {
    val replaced = replace(UNSAFE_FILENAME_CHARS, "_").trim().trim('.')
    val safe = replaced.ifEmpty { "file" }
    return if (safe.length > MAX_FILENAME_LENGTH) {
        val dot = safe.lastIndexOf('.')
        if (dot > 0 && safe.length - dot <= 10) {
            val ext = safe.substring(dot)
            val baseLen = MAX_FILENAME_LENGTH - ext.length
            safe.substring(0, baseLen.coerceAtLeast(1)) + ext
        } else {
            safe.substring(0, MAX_FILENAME_LENGTH)
        }
    } else {
        safe
    }
}
