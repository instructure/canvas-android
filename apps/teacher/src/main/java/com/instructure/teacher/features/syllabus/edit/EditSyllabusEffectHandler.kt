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

import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.teacher.mobius.common.ui.EffectHandler
import kotlinx.coroutines.launch

class EditSyllabusEffectHandler : EffectHandler<EditSyllabusView, EditSyllabusEvent, EditSyllabusEffect>() {

    override fun accept(effect: EditSyllabusEffect) {
        when (effect) {
            is EditSyllabusEffect.SaveData -> saveData(effect)
            EditSyllabusEffect.CloseEdit -> view?.closeEditSyllabus()
            EditSyllabusEffect.ShowSaveSuccess -> view?.showSaveSuccess()
            EditSyllabusEffect.ShowSaveError -> view?.showSaveError()
            EditSyllabusEffect.ShowCloseConfirmationDialog -> view?.showCloseConfirmationDialog()
        }.exhaustive
    }

    private fun saveData(effect: EditSyllabusEffect.SaveData) {
        launch {
            val id = effect.course.id
            val syllabusBody = effect.newContent
            val editedCourse = CourseManager.editCourseSyllabusAsync(id, syllabusBody).await()
            val courseSettings = CourseManager.editCourseSettingsAsync(id, effect.summaryAllowed).await()

            if (editedCourse.isFail || courseSettings.isFail) {
                consumer.accept(EditSyllabusEvent.SyllabusSaveError)
            } else {
                consumer.accept(EditSyllabusEvent.SyllabusSaveSuccess(syllabusBody, effect.summaryAllowed))
            }
        }
    }

}