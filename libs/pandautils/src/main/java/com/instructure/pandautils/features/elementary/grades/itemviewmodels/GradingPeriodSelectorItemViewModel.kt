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
import com.instructure.pandautils.features.elementary.grades.GradesItemViewType
import com.instructure.pandautils.features.elementary.grades.GradingPeriod
import com.instructure.pandautils.mvvm.ItemViewModel

class GradingPeriodSelectorItemViewModel(
    val gradingPeriod: List<GradingPeriod>,
    val selectedGradingPeriod: GradingPeriod
) : ItemViewModel {

    override val layoutId: Int = R.layout.item_grading_period_selector

    override val viewType: Int = GradesItemViewType.GRADING_PERIOD_SELECTOR.viewType
}