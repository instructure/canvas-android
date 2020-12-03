/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.teacher.features.syllabus.edit

import com.instructure.canvasapi2.models.Course

sealed class EditSyllabusEvent {
    data class SaveClicked(val content: String, val summaryAllowed: Boolean) : EditSyllabusEvent()
    data class SyllabusSaveSuccess(val content: String, val summaryAllowed: Boolean) : EditSyllabusEvent()
    data class BackClicked(val content: String, val summaryAllowed: Boolean) : EditSyllabusEvent()
    data class SaveState(val content: String, val summaryAllowed: Boolean) : EditSyllabusEvent()
    object SyllabusSaveError : EditSyllabusEvent()
}

sealed class EditSyllabusEffect {
    data class SaveData(val course: Course, val newContent: String, val summaryAllowed: Boolean) : EditSyllabusEffect()
    object CloseEdit : EditSyllabusEffect()
    object ShowSaveSuccess : EditSyllabusEffect()
    object ShowSaveError : EditSyllabusEffect()
    object ShowCloseConfirmationDialog : EditSyllabusEffect()
}

data class EditSyllabusModel(val course: Course, val summaryAllowed: Boolean, val isSaving: Boolean = false, val isChanged: Boolean = false)