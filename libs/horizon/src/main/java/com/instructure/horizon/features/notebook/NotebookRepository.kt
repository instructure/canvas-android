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
package com.instructure.horizon.features.notebook

import com.google.gson.Gson
import com.instructure.canvasapi2.managers.NoteHighlightedData
import com.instructure.canvasapi2.managers.RedwoodApiManager
import com.instructure.pandautils.utils.toJson
import com.instructure.redwood.QueryNotesQuery
import javax.inject.Inject

class NotebookRepository @Inject constructor(
    private val redwoodApiManager: RedwoodApiManager,
) {
    suspend fun getNotes(): QueryNotesQuery.Notes {
        return redwoodApiManager.getNotes(firstN = 1)
    }

    fun parseHighlightedData(highlightData: Any?): NoteHighlightedData? {
        val result = try {
            Gson().fromJson(highlightData?.toJson(), NoteHighlightedData::class.java)
        } catch (e: Exception) {
            null
        }

        return result
    }
}