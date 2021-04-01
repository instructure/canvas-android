/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.instructure.student.features.dashboard.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.utils.Logger
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.isCourse
import com.instructure.pandautils.utils.isGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditDashboardViewModel @Inject constructor(private val courseManager: CourseManager,
                                                 private val groupManager: GroupManager) : ViewModel() {

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<EditDashboardViewData>
        get() = _data
    private val _data = MutableLiveData<EditDashboardViewData>()

    val events: LiveData<Event<EditDashboardItemAction>>
        get() = _events
    private val _events = MutableLiveData<Event<EditDashboardItemAction>>()

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            _state.postValue(ViewState.Loading)

            try {
                val courses = courseManager.getCoursesAsync(true).await().dataOrThrow
                val coursesViewData = courses.map {
                    EditDashboardItemViewModel(it.name, it.isFavorite, it, ::handleAction)
                }
                val groups = groupManager.getAllGroupsAsync(true).await().dataOrThrow
                val groupsViewData = groups.map {
                    EditDashboardItemViewModel(it.name, it.isFavorite, it, ::handleAction)
                }
                _data.postValue(EditDashboardViewData(coursesViewData + groupsViewData))
                _state.postValue(ViewState.Success)
            } catch (e: Exception) {
                _state.postValue(ViewState.Error(e.message ?: ""))
                Logger.d("Failed to grab courses: ${e.printStackTrace()}")
            }
        }
    }

    private fun handleAction(action: EditDashboardItemAction) {
        when (action) {
            is EditDashboardItemAction.OpenItem -> {
                _events.postValue(Event(action))
            }
            is EditDashboardItemAction.FavoriteItem -> {
                val item = action.model
                viewModelScope.launch {
                    try {
                        if (item.isCourse) {
                            courseManager.addCourseToFavoritesAsync(item.id, true).await().dataOrThrow
                        }

                        if (item.isGroup) {
                            groupManager.addGroupToFavoritesAsync(item.id).await().dataOrThrow
                        }
                    } catch (e: Exception) {

                    }
                }

            }
        }
    }

}