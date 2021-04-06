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
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.Logger
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.isCourse
import com.instructure.pandautils.utils.isGroup
import com.instructure.student.BR
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.associateBy as associateBy

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

    private var courseMap: Map<Long, Course>? = null
    private var groupMap: Map<Long, Group>? = null

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            _state.postValue(ViewState.Loading)

            try {
                val courses = courseManager.getCoursesAsync(true).await().dataOrThrow
                val coursesViewData = courses.map {
                    EditDashboardItemViewModel(it.id, it.name, it.isFavorite, null, "${it.term?.name} | ${it.enrollments?.get(0)?.type?.apiTypeString}", it.type, ::handleAction)
                }
                courseMap = courses.associateBy { it.id }

                val groups = groupManager.getAllGroupsAsync(true).await().dataOrThrow
                val groupsViewData = groups.map {
                    val course = courseMap?.get(it.courseId)
                    EditDashboardItemViewModel(it.id, it.name, it.isFavorite, course?.name, course?.term?.name, it.type, ::handleAction)
                }
                groupMap = groups.associateBy { it.id }
                
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
            is EditDashboardItemAction.OpenGroup -> {
                _events.postValue(Event(EditDashboardItemAction.OpenItem(groupMap?.get(action.id))))
            }

            is EditDashboardItemAction.OpenCourse -> {
                _events.postValue(Event(EditDashboardItemAction.OpenItem(courseMap?.get(action.id))))
            }

            is EditDashboardItemAction.FavoriteItem -> {
                val item = action.itemViewModel
                val index = _data.value?.items?.indexOf(item)
                viewModelScope.launch {
                    try {
                        if (item.type == CanvasContext.Type.COURSE) {
                            courseManager.addCourseToFavoritesAsync(item.id, true).await().dataOrThrow
                        }

                        if (item.type == CanvasContext.Type.COURSE) {
                            groupManager.addGroupToFavoritesAsync(item.id).await().dataOrThrow
                        }
                        item.isFavorite = true
                        _events.postValue(Event(EditDashboardItemAction.UpdateItem(index)))
                    } catch (e: Exception) {

                    }
                }
            }

            is EditDashboardItemAction.UnfavoriteItem -> {
                val item = action.itemViewModel
                val index = _data.value?.items?.indexOf(item)
                viewModelScope.launch {
                    try {
                        if (item.type == CanvasContext.Type.COURSE) {
                            courseManager.removeCourseFromFavoritesAsync(item.id, true).await().dataOrThrow
                        }

                        if (item.type == CanvasContext.Type.GROUP) {
                            groupManager.removeGroupFromFavoritesAsync(item.id).await().dataOrThrow
                        }
                        item.isFavorite = false
                        _events.postValue(Event(EditDashboardItemAction.UpdateItem(index)))
                    } catch (e: Exception) {

                    }
                }
            }
        }
    }

}