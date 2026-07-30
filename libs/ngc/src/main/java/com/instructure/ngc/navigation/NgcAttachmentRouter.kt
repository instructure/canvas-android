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

package com.instructure.ngc.navigation

/**
 * Opens NGC comment/submission attachment previews (files and full-screen media) outside the main
 * navigation shell. This is a purpose-named counterpart to
 * [com.instructure.pandautils.navigation.WebViewRouter] for the attachment/media-preview surfaces
 * that aren't web/link-routing concerns.
 */
interface NgcAttachmentRouter {

    /**
     * Opens a file attachment (image, pdf, doc, ...) for preview.
     */
    fun openFileAttachment(url: String, contentType: String, fileName: String)

    /**
     * Opens a video/audio file in the app's full-screen media player.
     */
    fun openMediaFullScreen(
        url: String,
        contentType: String,
        displayName: String? = null,
        thumbnailUrl: String? = null,
    )
}
