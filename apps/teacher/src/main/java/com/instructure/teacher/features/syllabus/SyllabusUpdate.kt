/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.teacher.features.syllabus

import com.instructure.teacher.mobius.common.ui.UpdateInit
import com.spotify.mobius.First
import com.spotify.mobius.Next

class SyllabusUpdate : UpdateInit<SyllabusModel, SyllabusEvent, SyllabusEffect>() {

    override fun performInit(model: SyllabusModel): First<SyllabusModel, SyllabusEffect> {
        return First.first<SyllabusModel, SyllabusEffect>(SyllabusModel("Stonks"))
    }

    override fun update(model: SyllabusModel, event: SyllabusEvent): Next<SyllabusModel, SyllabusEffect> {
        return Next.next<SyllabusModel, SyllabusEffect>(SyllabusModel("Stonks"))
    }
}