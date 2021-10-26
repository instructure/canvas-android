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

import android.content.res.Resources
import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.grades.GradeRowViewData
import com.instructure.pandautils.features.elementary.grades.GradesItemViewType
import com.instructure.pandautils.mvvm.ItemViewModel

class GradeRowItemViewModel(
    private val resources: Resources,
    val data: GradeRowViewData,
    val onRowClicked: () -> Unit
) : ItemViewModel {

    override val layoutId: Int = R.layout.item_grade_row

    override val viewType: Int = GradesItemViewType.GRADE_ROW.viewType

    val gradeContentDescription: String
        get() {
            return if (data.gradeText == "--") {
                resources.getString(R.string.a11y_gradesNotAvailableContentDescription)
            } else {
                data.gradeText
            }
        }

    val percentage: Float
        get() {
            if (data.score == null || data.score < 0) return 0f
            return data.score.toFloat() / 100
        }
}