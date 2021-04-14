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
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.*
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.student.R
import com.instructure.student.features.dashboard.edit.itemViewModel.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
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

    private var courseMap: Map<Long, Course>? = null
    private var groupMap: Map<Long, Group>? = null

    private val favoriteCourseMap: MutableMap<Long, Course> = mutableMapOf()
    private val favoriteGroupMap: MutableMap<Long, Group> = mutableMapOf()

    private lateinit var courses: List<Course>
    private lateinit var groups: List<Group>

    private lateinit var groupsViewData: List<EditDashboardGroupItemViewModel>

    private lateinit var groupHeader: EditDashboardHeaderViewModel
    private lateinit var courseHeader: EditDashboardHeaderViewModel

    private lateinit var currentCoursesViewData: List<EditDashboardCourseItemViewModel>
    private lateinit var pastCoursesViewData: List<EditDashboardCourseItemViewModel>
    private lateinit var futureCoursesViewData: List<EditDashboardCourseItemViewModel>

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            _state.postValue(ViewState.Loading)

            try {
                courses = courseManager.getCoursesAsync(true).await().dataOrThrow
                courseMap = courses.associateBy { it.id }

                groups = groupManager.getAllGroupsAsync(true).await().dataOrThrow
                groups = groups.filter { it.isActive(courseMap?.get(it.courseId)) }
                groupMap = groups.associateBy { it.id }

                val items = createListItems(courses, groups)
                _data.postValue(EditDashboardViewData(items))
                if (items.isEmpty()) {
                    _state.postValue(ViewState.Empty(R.string.edit_dashboard_empty_title, R.string.edit_dashboard_empty_message, R.drawable.ic_panda_nocourses))
                } else {
                    _state.postValue(ViewState.Success)
                }

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
                viewModelScope.launch {
                    try {
                        favoriteCourse(action.itemViewModel)
                        _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.added_to_dashboard)))
                    } catch (e: Exception) {
                        Logger.d("Failed to select course: ${e.printStackTrace()}")
                        _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.errorOccurred)))
                    }
                }

            }

            is EditDashboardItemAction.FavoriteGroup -> {
                viewModelScope.launch {
                    try {
                        favoriteGroup(action.itemViewModel)
                        _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.added_to_dashboard)))
                    } catch (e: Exception) {
                        Logger.d("Failed to select group: ${e.printStackTrace()}")
                        _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.errorOccurred)))
                    }
                }
            }

            is EditDashboardItemAction.UnfavoriteCourse -> {
                viewModelScope.launch {
                    try {
                        unfavoriteCourse(action.itemViewModel)
                        _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.removed_from_dashboard)))
                    } catch (e: Exception) {
                        Logger.d("Failed to deselect course: ${e.printStackTrace()}")
                        _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.errorOccurred)))
                    }
                }
            }

            is EditDashboardItemAction.UnfavoriteGroup -> {
                viewModelScope.launch {
                    try {
                        unfavoriteGroup(action.itemViewModel)
                        _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.removed_from_dashboard)))
                    } catch (e: Exception) {
                        Logger.d("Failed to deselect group: ${e.printStackTrace()}")
                        _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.errorOccurred)))
                    }
                }
            }

            is EditDashboardItemAction.ShowSnackBar -> {
                _events.postValue(Event(action))
            }
        }
    }

    private suspend fun favoriteCourse(item: EditDashboardCourseItemViewModel) {
        courseManager.addCourseToFavoritesAsync(item.id).await().dataOrThrow
        favoriteCourseMap[item.id] = courseMap?.get(item.id)
                ?: error("Course does not exist")
        courseMap?.get(item.id)?.isFavorite = true
        item.apply {
            isFavorite = true
            notifyChange()
        }
        courseHeader.apply {
            hasItemSelected = favoriteCourseMap.isNotEmpty()
            notifyChange()
        }
    }

    private suspend fun unfavoriteCourse(item: EditDashboardCourseItemViewModel) {
        courseManager.removeCourseFromFavoritesAsync(item.id).await().dataOrThrow
        favoriteCourseMap.remove(item.id)
        courseMap?.get(item.id)?.isFavorite = false
        item.apply {
            isFavorite = false
            notifyChange()
        }
        courseHeader.apply {
            hasItemSelected = favoriteCourseMap.isNotEmpty()
            notifyChange()
        }
    }

    private suspend fun favoriteGroup(item: EditDashboardGroupItemViewModel) {
        groupManager.addGroupToFavoritesAsync(item.id).await().dataOrThrow
        favoriteGroupMap[item.id] = groupMap?.get(item.id) ?: error("Group does not exist")
        groupMap?.get(item.id)?.isFavorite = true
        item.apply {
            isFavorite = true
            notifyChange()
        }
        groupHeader.apply {
            hasItemSelected = favoriteGroupMap.isNotEmpty()
            notifyChange()
        }
    }

    private suspend fun unfavoriteGroup(item: EditDashboardGroupItemViewModel) {
        groupManager.removeGroupFromFavoritesAsync(item.id).await().dataOrThrow
        favoriteGroupMap.remove(item.id)
        groupMap?.get(item.id)?.isFavorite = false
        item.apply {
            isFavorite = false
            notifyChange()
        }
        groupHeader.apply {
            hasItemSelected = favoriteGroupMap.isNotEmpty()
            notifyChange()
        }
    }

    private fun selectAllGroups() {
        viewModelScope.launch {
            try {
                groupsViewData.forEach { if (!it.isFavorite) favoriteGroup(it) }
                _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.all_added_to_dashboard)))
            } catch (e: Exception) {
                Logger.d("Failed to select all groups: ${e.printStackTrace()}")
                _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.errorOccurred)))
            }
        }

    }

    private fun deselectAllGroups() {
        viewModelScope.launch {
            try {
                groupsViewData.forEach { if (it.isFavorite) unfavoriteGroup(it) }
                _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.all_removed_from_dashboard)))
            } catch (e: Exception) {
                Logger.d("Failed to deselect all groups: ${e.printStackTrace()}")
                _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.errorOccurred)))
            }
        }
    }

    private fun selectAllCourses() {
        viewModelScope.launch {
            try {
                currentCoursesViewData.forEach { if (!it.isFavorite && it.favoriteable) favoriteCourse(it) }
                futureCoursesViewData.forEach { if (!it.isFavorite && it.favoriteable) favoriteCourse(it) }
                _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.all_added_to_dashboard)))
            } catch (e: Exception) {
                Logger.d("Failed to select all courses: ${e.printStackTrace()}")
                _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.errorOccurred)))
            }
        }
    }

    private fun deselectAllCourses() {
        viewModelScope.launch {
            try {
                currentCoursesViewData.forEach { if (it.isFavorite) unfavoriteCourse(it) }
                futureCoursesViewData.forEach { if (it.isFavorite) unfavoriteCourse(it) }
                _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.all_removed_from_dashboard)))
            } catch (e: Exception) {
                Logger.d("Failed to deselect all courses: ${e.printStackTrace()}")
                _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.errorOccurred)))
            }
        }
    }

    private fun getCurrentCourses(courses: List<Course>): List<EditDashboardCourseItemViewModel> {
        favoriteCourseMap.clear()
        val currentCourses = courses.filter { it.hasActiveEnrollment() && it.isBetweenValidDateRange() }
        return currentCourses.map {
            if (it.isFavorite) {
                favoriteCourseMap[it.id] = it
            }
            EditDashboardCourseItemViewModel(
                    id = it.id,
                    name = it.name,
                    isFavorite = it.isFavorite,
                    favoriteable = it.isValidTerm() && it.isNotDeleted() && it.isPublished(),
                    openable = it.isNotDeleted() && it.isPublished(),
                    termTitle = "${it.term?.name} | ${it.enrollments?.get(0)?.type?.apiTypeString}",
                    actionHandler = ::handleAction
            )
        }
    }

    private fun getPastCourses(courses: List<Course>): List<EditDashboardCourseItemViewModel> {
        val pastCourses = courses.filter { it.endDate?.before(Date()) ?: false }
        return pastCourses.map {
            EditDashboardCourseItemViewModel(
                    id = it.id,
                    name = it.name,
                    isFavorite = it.isFavorite,
                    favoriteable = false,
                    openable = it.isNotDeleted() && it.isPublished(),
                    termTitle = "${it.term?.name} | ${it.enrollments?.get(0)?.type?.apiTypeString}",
                    actionHandler = ::handleAction
            )
        }
    }

    private fun getFutureCourses(courses: List<Course>): List<EditDashboardCourseItemViewModel> {
        val futureCourses = courses.filter { it.startDate?.after(Date()) ?: false }
        return futureCourses.map {
            if (it.isFavorite) {
                favoriteCourseMap[it.id] = it
            }
            EditDashboardCourseItemViewModel(
                    id = it.id,
                    name = it.name,
                    isFavorite = it.isFavorite,
                    favoriteable = it.isValidTerm() && it.isNotDeleted() && it.isPublished(),
                    openable = it.isNotDeleted() && it.isPublished(),
                    termTitle = "${it.term?.name} | ${it.enrollments?.get(0)?.type?.apiTypeString}",
                    actionHandler = ::handleAction
            )
        }
    }

    private fun getGroups(groups: List<Group>): List<EditDashboardGroupItemViewModel> {
        favoriteGroupMap.clear()
        return groups.map {
            if (it.isFavorite) {
                favoriteGroupMap[it.id] = it
            }
            val course = courseMap?.get(it.courseId)
            EditDashboardGroupItemViewModel(it.id, it.name, it.isFavorite, course?.name, course?.term?.name, ::handleAction)
        }
    }

    private fun createListItems(courses: List<Course>, groups: List<Group>): List<ItemViewModel> {
        currentCoursesViewData = getCurrentCourses(courses)
        pastCoursesViewData = getPastCourses(courses)
        futureCoursesViewData = getFutureCourses(courses)
        groupsViewData = getGroups(groups)

        val items = mutableListOf<ItemViewModel>()
        if (currentCoursesViewData.isNotEmpty() || pastCoursesViewData.isNotEmpty() || futureCoursesViewData.isNotEmpty()) {
            courseHeader = EditDashboardHeaderViewModel(R.string.all_courses, favoriteCourseMap.isNotEmpty(), ::selectAllCourses, ::deselectAllCourses)
            items.add(courseHeader)
            items.add(EditDashboardDescriptionItemViewModel(R.string.edit_dashboard_course_description))
        }

        if (currentCoursesViewData.isNotEmpty()) {
            items.add(EditDashboardEnrollmentItemViewModel(R.string.current_enrollments))
            items.addAll(currentCoursesViewData)
        }
        if (pastCoursesViewData.isNotEmpty()) {
            items.add(EditDashboardEnrollmentItemViewModel(R.string.past_enrollments))
            items.addAll(pastCoursesViewData)
        }
        if (futureCoursesViewData.isNotEmpty()) {
            items.add(EditDashboardEnrollmentItemViewModel(R.string.future_enrollments))
            items.addAll(futureCoursesViewData)
        }
        if (groupsViewData.isNotEmpty()) {
            groupHeader = EditDashboardHeaderViewModel(R.string.all_groups, favoriteGroupMap.isNotEmpty(), ::selectAllGroups, ::deselectAllGroups)
            items.add(groupHeader)
            items.add(EditDashboardDescriptionItemViewModel(R.string.edit_dashboard_group_description))
            items.addAll(groupsViewData)
        }

        return items
    }

    fun queryItems(query: String) {
        if (query.isBlank()) {
            createListItems(courses, groups)
        } else {
            val queriedCourses = courses.filter { it.name.contains(query, true) }
            val queriedGroups = groups.filter { it.name?.contains(query, true) ?: false || it.description?.contains(query, true) ?: false || courseMap?.get(it.courseId)?.name?.contains(query, true) ?: false }

            val items = createListItems(queriedCourses, queriedGroups)
            _data.postValue(EditDashboardViewData(items))
            if (items.isEmpty()) {
                _state.postValue(ViewState.Empty(R.string.edit_dashboard_empty_title, R.string.edit_dashboard_empty_message, R.drawable.ic_panda_nocourses))
            } else {
                _state.postValue(ViewState.Success)
            }
        }
    }

}