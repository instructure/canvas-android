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
package com.instructure.pandautils.features.notes.notebook

/**
 * The contract the host (e.g. [com.instructure.pandautils.features.pages.details.composables.PageDetailsScreen])
 * hands to [NotebookTray] to open it. [initialNoteId] non-null (highlight tap) makes the tray push that
 * note's detail level on top of the list; null (toolbar button) lands on the list root. [pageTitle] is
 * shown as the list level header's subtitle.
 *
 * [courseId] is required explicitly here because shared Compose code (unlike the old Fragment-hosted
 * feature) has no [com.instructure.canvasapi2.models.CanvasContext] of its own to derive it from - the
 * host ViewModel already holds it and must pass it through.
 *
 * [forceNetwork] defaults to true for any host that hasn't already fetched this page's notes itself.
 * A host that has (e.g. [com.instructure.pandautils.features.pages.details.PageDetailsViewModel], which
 * fetches them to render WebView highlights before the tray can even be opened) should pass false to
 * avoid a redundant network round-trip for data it already has.
 */
data class NotebookTrayArgs(
    val courseId: Long,
    val pageId: String,
    val pageTitle: String,
    val initialNoteId: String? = null,
    val forceNetwork: Boolean = true
)
