/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.features.notebook.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class NotebookRoute(open val route: String) {
    data class Notebook(
        val courseId: String? = null,
        val objectType: String? = null,
        val objectId: String? = null,
        val showTopBar: Boolean? = null,
        val navigateToEdit: Boolean? = null
    ) : NotebookRoute(route) {
        companion object {
            private const val ROUTE = "notebook_list"
            const val COURSE_ID = "courseId"
            const val OBJECT_TYPE = "objectType"
            const val OBJECT_ID = "objectId"
            const val SHOW_TOP_BAR = "showTopBar"
            const val SHOW_FILTERS = "showFilters"
            const val NAVIGATE_TO_EDIT = "navigateToEdit"
            const val route = "$ROUTE?$COURSE_ID={$COURSE_ID}&$OBJECT_TYPE={$OBJECT_TYPE}&$OBJECT_ID={$OBJECT_ID}&$SHOW_TOP_BAR={$SHOW_TOP_BAR}&$SHOW_FILTERS={$SHOW_FILTERS}&$NAVIGATE_TO_EDIT={$NAVIGATE_TO_EDIT}"

            fun route(
                courseId: String? = null,
                objectType: String? = null,
                objectId: String? = null,
                showTopBar: Boolean? = null,
                showFilters: Boolean? = null,
                navigateToEdit: Boolean? = null
            ): String {
                val params = buildList {
                    courseId?.let { add("$COURSE_ID=$it") }
                    objectType?.let { add("$OBJECT_TYPE=$it") }
                    objectId?.let { add("$OBJECT_ID=$it") }
                    showTopBar?.let { add("$SHOW_TOP_BAR=$it") }
                    showFilters?.let { add("$SHOW_FILTERS=$it") }
                    navigateToEdit?.let { add("$NAVIGATE_TO_EDIT=$it") }
                }
                return if (params.isNotEmpty()) "$ROUTE?${params.joinToString("&")}" else ROUTE
            }
        }
    }

    @Serializable
    data class AddNotebook(
        val courseId: String,
        val objectType: String,
        val objectId: String,
        val highlightedTextStartOffset: Int,
        val highlightedTextEndOffset: Int,
        val highlightedTextStartContainer: String,
        val highlightedTextEndContainer: String,
        val textSelectionStart: Int,
        val textSelectionEnd: Int,
        val highlightedText: String,
        val noteType: String?,
    ): NotebookRoute("add_notebook")

    @Serializable
    data class EditNotebook(
        val noteId: String,
        val highlightedTextStartOffset: Int,
        val highlightedTextEndOffset: Int,
        val highlightedTextStartContainer: String,
        val highlightedTextEndContainer: String,
        val textSelectionStart: Int,
        val textSelectionEnd: Int,
        val highlightedText: String,
        val noteType: String,
        val userComment: String,
    ): NotebookRoute("edit_notebook")
}