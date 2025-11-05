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
    ) : NotebookRoute(route) {
        companion object {
            private const val ROUTE = "notebook_list"
            const val COURSE_ID = "courseId"
            const val OBJECT_TYPE = "objectType"
            const val OBJECT_ID = "objectId"
            const val route = "$ROUTE?$COURSE_ID={$COURSE_ID}&$OBJECT_TYPE={$OBJECT_TYPE}&$OBJECT_ID={$OBJECT_ID}"

            fun route(
                courseId: String? = null,
                objectType: String? = null,
                objectId: String? = null
            ): String {
                return "$ROUTE?COURSE_ID=${courseId}&$OBJECT_TYPE=${objectType}&$OBJECT_ID=${objectId}"
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