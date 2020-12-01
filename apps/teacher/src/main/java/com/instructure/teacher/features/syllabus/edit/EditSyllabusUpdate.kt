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

import com.instructure.teacher.mobius.common.ui.UpdateInit
import com.spotify.mobius.First
import com.spotify.mobius.Next

class EditSyllabusUpdate : UpdateInit<EditSyllabusModel, EditSyllabusEvent, EditSyllabusEffect>() {

    override fun performInit(model: EditSyllabusModel): First<EditSyllabusModel, EditSyllabusEffect> {
        return First.first(model.copy())
    }

    override fun update(model: EditSyllabusModel, event: EditSyllabusEvent): Next<EditSyllabusModel, EditSyllabusEffect> {
        return when (event) {
            is EditSyllabusEvent.SaveClicked -> handleSaveClicked(model, event)
            is EditSyllabusEvent.SyllabusSaveSuccess -> handleSyllabusSaved(model, event)
            is EditSyllabusEvent.SyllabusSaveError -> handleSyllabusSaveError(model)
            is EditSyllabusEvent.BackClicked -> handleBackClick(model, event)
        }
    }

    private fun handleBackClick(model: EditSyllabusModel, event: EditSyllabusEvent.BackClicked): Next<EditSyllabusModel, EditSyllabusEffect> {
        return if (isSyllabusChanged(model, event)) {
            Next.dispatch(setOf(EditSyllabusEffect.CloseEdit))
        } else {
            Next.dispatch(setOf(EditSyllabusEffect.ShowCloseConfirmationDialog))
        }
    }

    private fun isSyllabusChanged(model: EditSyllabusModel, event: EditSyllabusEvent.BackClicked): Boolean {
        return model.summaryAllowed == event.summaryAllowed && model.course.syllabusBody == event.content
    }

    private fun handleSaveClicked(model: EditSyllabusModel, event: EditSyllabusEvent.SaveClicked): Next<EditSyllabusModel, EditSyllabusEffect> {
        return Next.next(model.copy(isSaving = true), setOf(EditSyllabusEffect.SaveData(model.course, event.content, event.summaryAllowed)))
    }

    private fun handleSyllabusSaved(model: EditSyllabusModel, event: EditSyllabusEvent.SyllabusSaveSuccess): Next<EditSyllabusModel, EditSyllabusEffect> {
        val course = model.course.copy(syllabusBody = event.content)
        return Next.next(model.copy(isSaving = false, course = course, summaryAllowed = event.summaryAllowed), setOf(EditSyllabusEffect.CloseEdit, EditSyllabusEffect.ShowSaveSuccess))
    }

    private fun handleSyllabusSaveError(model: EditSyllabusModel): Next<EditSyllabusModel, EditSyllabusEffect> {
        return Next.next(model.copy(isSaving = false), setOf(EditSyllabusEffect.ShowSaveError))
    }
}