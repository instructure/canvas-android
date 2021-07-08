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

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import com.instructure.pandautils.R
import com.instructure.pandautils.features.elementary.grades.GradesAction
import com.instructure.pandautils.features.elementary.grades.GradesItemViewType
import com.instructure.pandautils.features.elementary.grades.GradingPeriod
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ItemViewModel

class GradingPeriodSelectorItemViewModel(
    private val events: MutableLiveData<Event<GradesAction>>,
    private val gradingPeriods: List<GradingPeriod>,
    @get:Bindable var selectedGradingPeriod: GradingPeriod
) : BaseObservable(), ItemViewModel {

    override val layoutId: Int = R.layout.item_grading_period_selector

    override val viewType: Int = GradesItemViewType.GRADING_PERIOD_SELECTOR.viewType

    fun onClick() {
        val index = gradingPeriods.indexOfFirst { it.id == selectedGradingPeriod.id }
        events.postValue(Event(GradesAction.OpenGradingPeriodsDialog(gradingPeriods, index)))
    }

    fun isNotEmpty() = gradingPeriods.isNotEmpty()
}