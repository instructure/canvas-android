/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.features.grades

import com.instructure.pandautils.features.grades.GradesBehaviour
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.parentapp.util.ParentPrefs


class ParentGradesBehaviour(
    parentPrefs: ParentPrefs,
    colorKeeper: ColorKeeper
) : GradesBehaviour {

    override val canvasContextColor = colorKeeper.getOrGenerateUserColor(parentPrefs.currentStudent).color()
}
