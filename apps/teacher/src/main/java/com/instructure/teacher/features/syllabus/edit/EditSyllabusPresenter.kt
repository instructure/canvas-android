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

import android.content.Context
import com.instructure.teacher.mobius.common.ui.Presenter

class EditSyllabusPresenter : Presenter<EditSyllabusModel, EditSyllabusViewState> {

    override fun present(model: EditSyllabusModel, context: Context): EditSyllabusViewState {
        return EditSyllabusViewState.Loaded(model.course.syllabusBody, model.summaryAllowed)
    }
}