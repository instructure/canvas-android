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


package com.instructure.pandautils.features.dashboard.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.Logger
import com.instructure.pandautils.R
import com.instructure.pandautils.features.dashboard.edit.itemviewmodels.EditDashboardCourseItemViewModel
import com.instructure.pandautils.features.dashboard.edit.itemviewmodels.EditDashboardDescriptionItemViewModel
import com.instructure.pandautils.features.dashboard.edit.itemviewmodels.EditDashboardEnrollmentItemViewModel
import com.instructure.pandautils.features.dashboard.edit.itemviewmodels.EditDashboardGroupItemViewModel
import com.instructure.pandautils.features.dashboard.edit.itemviewmodels.EditDashboardHeaderViewModel
import com.instructure.pandautils.features.dashboard.edit.itemviewmodels.EditDashboardNoteItemViewModel
import com.instructure.pandautils.features.calendar.CalendarSharedEvents
import com.instructure.pandautils.features.calendar.SharedCalendarAction
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.NetworkStateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditDashboardViewModel @Inject constructor(
    private val courseManager: CourseManager,
    private val groupManager: GroupManager,
    private val repository: EditDashboardRepository,
    private val networkStateProvider: NetworkStateProvider,
    private val calendarSharedEvents: CalendarSharedEvents
) : ViewModel() {

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

    private lateinit var currentCourses: List<Course>
    private lateinit var pastCourses: List<Course>
    private lateinit var futureCourses: List<Course>
    private lateinit var groups: List<Group>

    private lateinit var groupHeader: EditDashboardHeaderViewModel
    private lateinit var courseHeader: EditDashboardHeaderViewModel
    private var noteItem: EditDashboardNoteItemViewModel? = null
    private var noteItemHidden = false

    private lateinit var currentCoursesViewData: List<EditDashboardCourseItemViewModel>
    private lateinit var pastCoursesViewData: List<EditDashboardCourseItemViewModel>
    private lateinit var futureCoursesViewData: List<EditDashboardCourseItemViewModel>
    private lateinit var groupsViewData: List<EditDashboardGroupItemViewModel>

    private var syncedCourseIds = emptySet<Long>()
    private var offlineEnabled = false

    var hasChanges = false

    init {
        _state.postValue(ViewState.Loading)
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            try {
                val courses = repository.getCourses()
                currentCourses = courses.getOrNull(0).orEmpty()
                pastCourses = courses.getOrNull(1).orEmpty()
                futureCourses = courses.getOrNull(2).orEmpty()

                syncedCourseIds = repository.getSyncedCourseIds()
                offlineEnabled = repository.offlineEnabled()

                courseMap = (currentCourses + pastCourses + futureCourses).associateBy { it.id }

                groups = repository.getGroups()
                groups = groups.filter { it.isActive(courseMap?.get(it.courseId)) }
                groupMap = groups.associateBy { it.id }

                val items = createListItems(currentCourses, pastCourses, futureCourses, groups)
                _data.postValue(EditDashboardViewData(items))
                if (items.isEmpty()) {
                    postEmptyState()
                } else {
                    _state.postValue(ViewState.Success)
                }

            } catch (e: Exception) {
                _state.postValue(ViewState.Error())
                Logger.d("Failed to grab courses: ${e.printStackTrace()}")
            }
        }
    }

    private fun postEmptyState(isFiltered: Boolean = false) {
        val offlineMode = offlineEnabled && !networkStateProvider.isOnline()
        when {
            isFiltered -> {
                _state.postValue(ViewState.Empty(R.string.editDashboardNoResults, R.string.editDashboardNoResultsMessage, R.drawable.ic_panda_nocourses))
            }
            offlineMode -> {
                _state.postValue(ViewState.Empty(R.string.editDashboardOfflineMode, R.string.editDashboardOfflineModeMessage, R.drawable.ic_panda_nocourses))
            }
            else -> {
                _state.postValue(ViewState.Empty(R.string.edit_dashboard_empty_title,R.string.edit_dashboard_empty_message, R.drawable.ic_panda_nocourses))
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
                        addCourseToFavorites(action.itemViewModel)
                        courseManager.addCourseToFavoritesAsync(action.itemViewModel.id).await().dataOrThrow
                        _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.added_to_dashboard)))
                        calendarSharedEvents.sendEvent(viewModelScope, SharedCalendarAction.RefreshToDoList)
                    } catch (e: Exception) {
                        Logger.d("Failed to select course: ${e.printStackTrace()}")
                        removeCourseFromFavorites(action.itemViewModel)
                        _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.errorOccurred)))
                    }
                }

            }

            is EditDashboardItemAction.FavoriteGroup -> {
                viewModelScope.launch {
                    try {
                        addGroupToFavorites(action.itemViewModel)
                        groupManager.addGroupToFavoritesAsync(action.itemViewModel.id).await().dataOrThrow
                        _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.added_to_dashboard)))
                    } catch (e: Exception) {
                        Logger.d("Failed to select group: ${e.printStackTrace()}")
                        removeGroupFromFavorites(action.itemViewModel)
                        _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.errorOccurred)))
                    }
                }
            }

            is EditDashboardItemAction.UnfavoriteCourse -> {
                viewModelScope.launch {
                    try {
                        removeCourseFromFavorites(action.itemViewModel)
                        courseManager.removeCourseFromFavoritesAsync(action.itemViewModel.id).await().dataOrThrow
                        _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.removed_from_dashboard)))
                        calendarSharedEvents.sendEvent(viewModelScope, SharedCalendarAction.RefreshToDoList)
                    } catch (e: Exception) {
                        Logger.d("Failed to deselect course: ${e.printStackTrace()}")
                        addCourseToFavorites(action.itemViewModel)
                        _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.errorOccurred)))
                    }
                }
            }

            is EditDashboardItemAction.UnfavoriteGroup -> {
                viewModelScope.launch {
                    try {
                        removeGroupFromFavorites(action.itemViewModel)
                        groupManager.removeGroupFromFavoritesAsync(action.itemViewModel.id).await().dataOrThrow
                        _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.removed_from_dashboard)))
                    } catch (e: Exception) {
                        Logger.d("Failed to deselect group: ${e.printStackTrace()}")
                        addGroupToFavorites(action.itemViewModel)
                        _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.errorOccurred)))
                    }
                }
            }

            is EditDashboardItemAction.ShowSnackBar -> {
                _events.postValue(Event(action))
            }
            is EditDashboardItemAction.OpenItem -> {}
        }
    }

    private fun addCourseToFavorites(item: EditDashboardCourseItemViewModel) {
        hasChanges = true
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

    private fun removeCourseFromFavorites(item: EditDashboardCourseItemViewModel) {
        hasChanges = true
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

    private fun addGroupToFavorites(item: EditDashboardGroupItemViewModel) {
        hasChanges = true
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

    private fun removeGroupFromFavorites(item: EditDashboardGroupItemViewModel) {
        hasChanges = true
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
        val groupsToFavorite = groupsViewData.filter { !it.isFavorite }
        var counter = 0
        groupsToFavorite.forEach {
            viewModelScope.launch {
                try {
                    addGroupToFavorites(it)
                    groupManager.addGroupToFavoritesAsync(it.id).await().dataOrThrow
                    counter++
                    if (counter == groupsToFavorite.size) {
                        _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.all_added_to_dashboard)))
                    }
                } catch (e: Exception) {
                    Logger.d("Failed to select all groups: ${e.printStackTrace()}")
                    removeGroupFromFavorites(it)
                    _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.errorOccurred)))
                }
            }
        }
    }

    private fun deselectAllGroups() {
        val groupsToUnfavorite = groupsViewData.filter { it.isFavorite }
        var counter = 0
        groupsToUnfavorite.forEach {
            viewModelScope.launch {
                try {
                    removeGroupFromFavorites(it)
                    groupManager.removeGroupFromFavoritesAsync(it.id).await().dataOrThrow
                    counter++
                    if (counter == groupsToUnfavorite.size) {
                        _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.all_removed_from_dashboard)))
                    }
                } catch (e: Exception) {
                    Logger.d("Failed to select all courses: ${e.printStackTrace()}")
                    addGroupToFavorites(it)
                    _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.errorOccurred)))
                }
            }
        }
    }

    private fun selectAllCourses() {
        val coursesToFavorite = (currentCoursesViewData + futureCoursesViewData).filter { !it.isFavorite && it.favoritableOnline }
        var counter = 0
        coursesToFavorite.forEach {
            viewModelScope.launch {
                try {
                    addCourseToFavorites(it)
                    courseManager.addCourseToFavoritesAsync(it.id).await().dataOrThrow
                    counter++
                    if (counter == coursesToFavorite.size) {
                        _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.all_added_to_dashboard)))
                    }
                } catch (e: Exception) {
                    Logger.d("Failed to select all courses: ${e.printStackTrace()}")
                    removeCourseFromFavorites(it)
                    _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.errorOccurred)))
                }
            }
        }
    }

    private fun deselectAllCourses() {
        val coursesToUnfavorite = (currentCoursesViewData + futureCoursesViewData).filter { it.isFavorite }
        var counter = 0
        coursesToUnfavorite.forEach {
            viewModelScope.launch {
                try {
                    removeCourseFromFavorites(it)
                    courseManager.removeCourseFromFavoritesAsync(it.id).await().dataOrThrow
                    counter++
                    if (counter == coursesToUnfavorite.size) {
                        _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.all_removed_from_dashboard)))
                    }
                } catch (e: Exception) {
                    Logger.d("Failed to select all courses: ${e.printStackTrace()}")
                    addCourseToFavorites(it)
                    _events.postValue(Event(EditDashboardItemAction.ShowSnackBar(R.string.errorOccurred)))
                }
            }
        }
    }

    private fun getCurrentCourses(courses: List<Course>): List<EditDashboardCourseItemViewModel> {
        favoriteCourseMap.clear()
        favoriteCourseMap.putAll(courses.filter { it.isFavorite }.associateBy { it.id })
        return courses.map { createCourseItem(it) }
    }

    private fun getPastCourses(courses: List<Course>): List<EditDashboardCourseItemViewModel> {
        return courses.map { createCourseItem(it) }
    }

    private fun getFutureCourses(courses: List<Course>): List<EditDashboardCourseItemViewModel> {
        favoriteCourseMap.putAll(courses.filter { it.isFavorite }.associateBy { it.id })
        return courses.map { createCourseItem(it) }
    }

    private fun createCourseItem(course: Course): EditDashboardCourseItemViewModel {
        val termName = course.term?.name
        val enrollmentType = course.enrollments?.firstOrNull()?.type?.apiTypeString.orEmpty()
        val termTitle = if (termName != null) "$termName | $enrollmentType" else enrollmentType
        val availableOffline = syncedCourseIds.contains(course.id)

        return EditDashboardCourseItemViewModel(
            id = course.id,
            name = course.name,
            isFavorite = course.isFavorite,
            favoritableOnline = repository.isFavoriteable(course),
            openable = repository.isOpenable(course),
            termTitle = termTitle,
            online = networkStateProvider.isOnline(),
            availableOffline = availableOffline,
            enabled = !offlineEnabled || networkStateProvider.isOnline() || availableOffline,
            actionHandler = ::handleAction
        )
    }

    private fun getGroups(groups: List<Group>): List<EditDashboardGroupItemViewModel> {
        favoriteGroupMap.clear()
        favoriteGroupMap.putAll(groups.filter { it.isFavorite }.associateBy { it.id })
        return groups.map {
            val course = courseMap?.get(it.courseId)
            EditDashboardGroupItemViewModel(it.id, it.name, it.isFavorite, course?.name, course?.term?.name, ::handleAction)
        }
    }

    private fun createListItems(currentCourses: List<Course>, pastCourses: List<Course>, futureCourses: List<Course>, groups: List<Group>, isFiltered: Boolean = false): List<ItemViewModel> {
        currentCoursesViewData = getCurrentCourses(currentCourses)
        pastCoursesViewData = getPastCourses(pastCourses)
        futureCoursesViewData = getFutureCourses(futureCourses)
        groupsViewData = getGroups(groups)

        val items = mutableListOf<ItemViewModel>()
        if (currentCoursesViewData.isNotEmpty() || pastCoursesViewData.isNotEmpty() || futureCoursesViewData.isNotEmpty()) {
            createNoteItem()?.let { items.add(it) }

            courseHeader = EditDashboardHeaderViewModel(
                R.string.courses,
                favoriteCourseMap.isNotEmpty(),
                ::selectAllCourses,
                ::deselectAllCourses,
                networkStateProvider.isOnline()
            )
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
            groupHeader = EditDashboardHeaderViewModel(
                R.string.groups,
                favoriteGroupMap.isNotEmpty(),
                ::selectAllGroups,
                ::deselectAllGroups,
                networkStateProvider.isOnline()
            )
            items.add(groupHeader)
            items.add(EditDashboardDescriptionItemViewModel(R.string.edit_dashboard_group_description))
            items.addAll(groupsViewData)
        }

        return items
    }

    private fun createNoteItem(): EditDashboardNoteItemViewModel? {
        noteItem = if (!networkStateProvider.isOnline() && offlineEnabled && !noteItemHidden) {
            EditDashboardNoteItemViewModel {
                val newItems = _data.value?.items?.minus(noteItem)?.filterNotNull()
                _data.postValue(EditDashboardViewData(newItems.orEmpty()))
                noteItemHidden = true
            }
        } else null

        return noteItem
    }

    fun queryItems(query: String) {
        val items = if (query.isBlank()) {
            createListItems(currentCourses, pastCourses, futureCourses, groups)
        } else {
            val queriedCurrentCourses = currentCourses.filter { it.name.contains(query, true) }
            val queriedPastCourses = pastCourses.filter { it.name.contains(query, true) }
            val queriedFutureCourses = futureCourses.filter { it.name.contains(query, true) }
            val queriedGroups = groups.filter { it.name?.contains(query, true) ?: false || it.description?.contains(query, true) ?: false || courseMap?.get(it.courseId)?.name?.contains(query, true) ?: false }

            createListItems(queriedCurrentCourses, queriedPastCourses, queriedFutureCourses, queriedGroups, true)
        }
        if (items.isEmpty()) {
            postEmptyState(query.isNotBlank())
        } else {
            _state.postValue(ViewState.Success)
        }
        _data.postValue(EditDashboardViewData(items))
    }

    fun refresh() {
        if (networkStateProvider.isOnline()) {
            _state.postValue(ViewState.Refresh)
            loadItems()
        } else {
            _state.postValue(ViewState.Success)
        }
    }

}