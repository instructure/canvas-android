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

fun shouldOpenMediaInternally(url: String?, mimeType: String?, mimeClass: String? = null): Boolean {
    if (!mimeClass.isNullOrBlank()) {
        val cls = mimeClass.trim().lowercase()
        if (cls == "audio" || cls == "video") return true
    }
    if (!mimeType.isNullOrBlank()) {
        val mime = mimeType.trim().lowercase()
        if (mime.startsWith("audio/") || mime.startsWith("video/")) return true
        if (mime == "application/dash+xml") return true
    }
    if (!url.isNullOrBlank()) {
        val path = url.substringBefore("?").lowercase()
        if (path.endsWith(".mpd") || path.endsWith(".m3u8") || path.endsWith(".mp4") ||
            path.endsWith(".mp3") || path.endsWith(".m4a") || path.endsWith(".webm") ||
            path.endsWith("/cmaf")
        ) return true
    }
    return false
}
