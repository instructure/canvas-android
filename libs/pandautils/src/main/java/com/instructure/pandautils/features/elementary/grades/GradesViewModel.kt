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
package com.instructure.pandautils.features.elementary.grades

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.UserManager
import com.instructure.pandautils.features.elementary.grades.itemviewmodels.GradeRowItemViewModel
import com.instructure.pandautils.features.elementary.grades.itemviewmodels.GradingPeriodSelectorItemViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GradesViewModel @Inject constructor(
    private val userManager: UserManager
) : ViewModel() {


    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<GradesViewData>
        get() = _data
    private val _data = MutableLiveData(GradesViewData(emptyList()))

    val events: LiveData<Event<GradesAction>>
        get() = _events
    private val _events = MutableLiveData<Event<GradesAction>>()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        _state.postValue(ViewState.Loading)
        loadData(false)
    }

    // Probably should add grading period parameter here
    private fun loadData(forceNetwork: Boolean) {
        viewModelScope.launch {
            try {
                delay(2000)
                val viewData = createDummyViewData()
                _data.postValue(viewData)
                _state.postValue(ViewState.Success)
            } catch (e: Exception) {
                _state.postValue(ViewState.Error("Failed to load grades"))
            }
        }
    }

    private fun createDummyViewData(): GradesViewData {
        val gradingPeriod = GradingPeriod(1, "Current Grading Period")

        return GradesViewData(listOf(
            GradingPeriodSelectorItemViewModel(listOf(gradingPeriod), gradingPeriod),
            GradeRowItemViewModel(GradeRowViewData(1, "Math", "#123456", 90.0f, "90%")),
            GradeRowItemViewModel(GradeRowViewData(2, "Art", "#12ff56", 80.0f, "80%")),
            GradeRowItemViewModel(GradeRowViewData(3, "History", "#55ff56", 10.0f, "10%")),
            GradeRowItemViewModel(GradeRowViewData(4, "Social Studies", "#55ffaa", 10.0f, "10%")),
            GradeRowItemViewModel(GradeRowViewData(5, "Music", "#00ffaa", 100.0f, "100%")),
            GradeRowItemViewModel(GradeRowViewData(6, "P. E.", "#88ffaa", null, "Not graded")),
        ))
    }

    fun refresh() {
        _state.postValue(ViewState.Refresh)
        loadData(true)
    }
}