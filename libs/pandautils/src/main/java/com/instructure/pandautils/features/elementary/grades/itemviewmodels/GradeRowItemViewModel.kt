/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.pandautils.features.elementary.grades.itemviewmodels

import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.grades.GradeRowViewData
import com.instructure.pandautils.features.elementary.grades.GradesItemViewType
import com.instructure.pandautils.mvvm.ItemViewModel

class GradeRowItemViewModel(
    val data: GradeRowViewData,
    val onRowClicked: () -> Unit
) : ItemViewModel {

    override val layoutId: Int = R.layout.item_grade_row

    override val viewType: Int = GradesItemViewType.GRADE_ROW.viewType

    val percentage: Float
        get() {
            if (data.score == null) return 0.0f
            return data.score.toFloat() / 100
        }
}