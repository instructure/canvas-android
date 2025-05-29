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
package com.instructure.horizon.features.notebook.add

import com.instructure.canvasapi2.managers.NoteHighlightedData
import com.instructure.canvasapi2.managers.RedwoodApiManager
import com.instructure.horizon.features.notebook.common.model.NotebookType
import javax.inject.Inject

class AddNoteRepository @Inject constructor(
    private val redwoodApiManager: RedwoodApiManager
) {
    suspend fun addNote(
        courseId: String,
        objectId: String,
        objectType: String,
        highlightedData: NoteHighlightedData,
        userComment: String,
        type: NotebookType?
    ) {
        redwoodApiManager.createNote(
            courseId = courseId,
            objectId = objectId,
            objectType = objectType,
            userText = userComment,
            highlightData = highlightedData,
            notebookType = type?.name
        )
    }
}