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
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Favorite
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Logger
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
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

    private val favoriteCourseMap: MutableMap<Long, Course> = mutableMapOf()
    private val favoriteGroupMap: MutableMap<Long, Group> = mutableMapOf()

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            _state.postValue(ViewState.Loading)

            try {
                val courses = courseManager.getCoursesAsync(true).await().dataOrThrow
                val coursesViewData = courses.map {
                    if (it.isFavorite) {
                        favoriteCourseMap[it.id] = it
                    }
                    EditDashboardItemViewModel(it.id, it.name, it.isFavorite, null, "${it.term?.name} | ${it.enrollments?.get(0)?.type?.apiTypeString}", it.type, ::handleAction)
                }
                courseMap = courses.associateBy { it.id }

                val groups = groupManager.getAllGroupsAsync(true).await().dataOrThrow
                val groupsViewData = groups.map {
                    if (it.isFavorite) {
                        favoriteGroupMap[it.id] = it
                    }
                    val course = courseMap?.get(it.courseId)
                    EditDashboardItemViewModel(it.id, it.name, it.isFavorite, course?.name, course?.term?.name, it.type, ::handleAction)
                }
                groupMap = groups.associateBy { it.id }
                val groupHeader = EditDashboardHeaderViewModel("All groups", favoriteGroupMap.isNotEmpty(), ::selectAllGroups, ::deselectAllGroups)

                _data.postValue(EditDashboardViewData(coursesViewData + groupHeader + groupsViewData))
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

            is EditDashboardItemAction.FavoriteCourse -> {
                toggleFavorite(action.itemViewModel, courseManager::addCourseToFavoritesAsync)
            }

            is EditDashboardItemAction.FavoriteGroup -> {
                toggleFavorite(action.itemViewModel, groupManager::addGroupToFavoritesAsync)
            }

            is EditDashboardItemAction.UnfavoriteCourse -> {
                toggleFavorite(action.itemViewModel, courseManager::removeCourseFromFavoritesAsync)
            }

            is EditDashboardItemAction.UnfavoriteGroup -> {
                toggleFavorite(action.itemViewModel, groupManager::removeGroupFromFavoritesAsync)
            }
        }
    }

    private fun toggleFavorite(item: EditDashboardItemViewModel, handler: (id: Long) -> Deferred<DataResult<Favorite>>) {
        val index = _data.value?.items?.indexOf(item)
        viewModelScope.launch {
            try {
                handler(item.id).await().dataOrThrow
                item.isFavorite = !item.isFavorite
                _events.postValue(Event(EditDashboardItemAction.UpdateItem(index)))
            } catch (e: Exception) {

            }
        }
    }

    private fun selectAllGroups() {

    }

    private fun deselectAllGroups() {

    }

    private fun selectAllCourses() {

    }

    private fun deselectAllCourses() {

    }

}