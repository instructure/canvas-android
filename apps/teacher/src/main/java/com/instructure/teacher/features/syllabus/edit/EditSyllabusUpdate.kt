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
        return Next.next(model.copy())
    }
}