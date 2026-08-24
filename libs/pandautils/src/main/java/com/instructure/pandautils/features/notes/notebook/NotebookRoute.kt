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

import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * The single-destination route the tray's internal [androidx.navigation.compose.NavHost] hosts, purely
 * to scope [NotebookViewModel] to one tray opening and feed it [NotebookTrayArgs] via its
 * [androidx.lifecycle.SavedStateHandle] - see [NotebookTray] for why a real multi-destination NavHost is
 * not used here. All args are query params (not path segments) so a page/course title or note id
 * containing "/" is safe after [Uri.encode].
 *
 * [ARG_PAGE_ID]/[ARG_PAGE_TITLE] are nullable: a Course-level opening (e.g. Student's
 * `CourseBrowserFragment`) passes [ARG_COURSE_ID]/[ARG_COURSE_TITLE] only, and [NotebookViewModel] picks
 * its root level from which args are present.
 */
internal object NotebookRoute {
    const val ARG_COURSE_ID = "notebookCourseId"
    const val ARG_PAGE_ID = "notebookPageId"
    const val ARG_PAGE_TITLE = "notebookPageTitle"
    const val ARG_COURSE_TITLE = "notebookCourseTitle"
    const val ARG_INITIAL_NOTE_ID = "notebookInitialNoteId"
    const val ARG_FORCE_NETWORK = "notebookForceNetwork"

    private const val BASE = "notebook"
    const val PATTERN =
        "$BASE?$ARG_COURSE_ID={$ARG_COURSE_ID}&$ARG_PAGE_ID={$ARG_PAGE_ID}" +
            "&$ARG_PAGE_TITLE={$ARG_PAGE_TITLE}&$ARG_COURSE_TITLE={$ARG_COURSE_TITLE}" +
            "&$ARG_INITIAL_NOTE_ID={$ARG_INITIAL_NOTE_ID}&$ARG_FORCE_NETWORK={$ARG_FORCE_NETWORK}"

    fun route(args: NotebookTrayArgs): String = buildString {
        append("$BASE?$ARG_COURSE_ID=${args.courseId}")
        args.pageId?.let { append("&$ARG_PAGE_ID=${Uri.encode(it)}") }
        args.pageTitle?.let { append("&$ARG_PAGE_TITLE=${Uri.encode(it)}") }
        append("&$ARG_COURSE_TITLE=${Uri.encode(args.courseTitle)}")
        args.initialNoteId?.let { append("&$ARG_INITIAL_NOTE_ID=${Uri.encode(it)}") }
        append("&$ARG_FORCE_NETWORK=${args.forceNetwork}")
    }

    val arguments = listOf(
        navArgument(ARG_COURSE_ID) { type = NavType.LongType },
        navArgument(ARG_PAGE_ID) { type = NavType.StringType; nullable = true; defaultValue = null },
        navArgument(ARG_PAGE_TITLE) { type = NavType.StringType; nullable = true; defaultValue = null },
        navArgument(ARG_COURSE_TITLE) { type = NavType.StringType; nullable = true; defaultValue = null },
        navArgument(ARG_INITIAL_NOTE_ID) { type = NavType.StringType; nullable = true; defaultValue = null },
        navArgument(ARG_FORCE_NETWORK) { type = NavType.BoolType; defaultValue = true },
    )
}
