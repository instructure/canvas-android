/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.pandautils.features.dashboard.widget.courses

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Group
import com.instructure.pandautils.domain.usecase.courses.LoadCourseUseCase
import com.instructure.pandautils.domain.usecase.courses.LoadCourseUseCaseParams
import com.instructure.pandautils.domain.usecase.courses.LoadFavoriteCoursesParams
import com.instructure.pandautils.domain.usecase.courses.LoadFavoriteCoursesUseCase
import com.instructure.pandautils.domain.usecase.courses.LoadGroupsParams
import com.instructure.pandautils.domain.usecase.courses.LoadGroupsUseCase
import com.instructure.pandautils.features.dashboard.widget.courses.model.CourseCardItem
import com.instructure.pandautils.features.dashboard.widget.courses.model.GradeDisplay
import com.instructure.pandautils.features.dashboard.widget.courses.model.GroupCardItem
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoursesWidgetViewModel @Inject constructor(
    private val loadFavoriteCoursesUseCase: LoadFavoriteCoursesUseCase,
    private val loadGroupsUseCase: LoadGroupsUseCase,
    private val loadCourseUseCase: LoadCourseUseCase,
    private val sectionExpandedStateDataStore: SectionExpandedStateDataStore,
    private val coursesWidgetBehavior: CoursesWidgetBehavior,
    private val courseSyncSettingsDao: CourseSyncSettingsDao,
    private val networkStateProvider: NetworkStateProvider,
    private val featureFlagProvider: FeatureFlagProvider,
    private val crashlytics: FirebaseCrashlytics,
    private val localBroadcastManager: LocalBroadcastManager
) : ViewModel() {

    private var courses: List<Course> = emptyList()
    private var groups: List<Group> = emptyList()

    private val _uiState = MutableStateFlow(
        CoursesWidgetUiState(
            onCourseClick = ::onCourseClick,
            onGroupClick = ::onGroupClick,
            onToggleCoursesExpanded = ::toggleCoursesExpanded,
            onToggleGroupsExpanded = ::toggleGroupsExpanded,
            onManageOfflineContent = ::onManageOfflineContent,
            onCustomizeCourse = ::onCustomizeCourse,
            onAllCourses = ::onAllCourses
        )
    )
    val uiState: StateFlow<CoursesWidgetUiState> = _uiState.asStateFlow()

    private val somethingChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent?.extras?.getBoolean(Const.COURSE_FAVORITES) == true) {
                refresh()
            }
        }
    }

    init {
        loadData()
        observeExpandedStates()
        observeGradeVisibility()
        observeColorOverlay()
        localBroadcastManager.registerReceiver(somethingChangedReceiver, IntentFilter(Const.COURSE_THING_CHANGED))
    }

    override fun onCleared() {
        super.onCleared()
        localBroadcastManager.unregisterReceiver(somethingChangedReceiver)
    }

    private fun onCourseClick(activity: FragmentActivity, courseId: Long) {
        val course = courses.find { it.id == courseId } ?: return
        coursesWidgetBehavior.onCourseClick(activity, course)
    }

    private fun onGroupClick(activity: FragmentActivity, groupId: Long) {
        val group = groups.find { it.id == groupId } ?: return
        coursesWidgetBehavior.onGroupClick(activity, group)
    }

    private fun onManageOfflineContent(activity: FragmentActivity, courseId: Long) {
        val course = courses.find { it.id == courseId } ?: return
        coursesWidgetBehavior.onManageOfflineContent(activity, course)
    }

    private fun onCustomizeCourse(activity: FragmentActivity, courseId: Long) {
        val course = courses.find { it.id == courseId } ?: return
        coursesWidgetBehavior.onCustomizeCourse(activity, course)
    }

    fun refresh() {
        loadData(forceRefresh = true)
    }

    private fun toggleCoursesExpanded() {
        viewModelScope.launch {
            val newState = !_uiState.value.isCoursesExpanded
            sectionExpandedStateDataStore.setCoursesExpanded(newState)
        }
    }

    private fun toggleGroupsExpanded() {
        viewModelScope.launch {
            val newState = !_uiState.value.isGroupsExpanded
            sectionExpandedStateDataStore.setGroupsExpanded(newState)
        }
    }

    private fun loadData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isError = false) }

            try {
                courses = loadFavoriteCoursesUseCase(LoadFavoriteCoursesParams(forceRefresh))
                groups = loadGroupsUseCase(LoadGroupsParams(forceRefresh))

                val courseCards = mapCoursesToCardItems(courses)
                val groupCards = mapGroupsToCardItems(groups)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        courses = courseCards,
                        groups = groupCards
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isError = true
                    )
                }
                crashlytics.recordException(e)
            }
        }
    }

    private suspend fun mapCoursesToCardItems(courses: List<Course>): List<CourseCardItem> {
        val syncedIds = getSyncedCourseIds()
        val isOnline = networkStateProvider.isOnline()

        return courses.map { course ->
            val isSynced = syncedIds.contains(course.id)
            CourseCardItem(
                id = course.id,
                name = course.name,
                courseCode = course.courseCode,
                imageUrl = course.imageUrl,
                grade = mapGrade(course),
                announcementCount = 0,
                isSynced = isSynced,
                isClickable = isOnline || isSynced
            )
        }
    }

    private suspend fun mapGroupsToCardItems(groups: List<Group>): List<GroupCardItem> {
        return groups.map { group ->
            val parentCourse = courses.find { it.id == group.courseId } ?: if (group.courseId != 0L) {
                try {
                    loadCourseUseCase(LoadCourseUseCaseParams(group.courseId, false))
                } catch (e: Exception) {
                    crashlytics.recordException(e)
                    null
                }
            } else {
                null
            }

            GroupCardItem(
                id = group.id,
                name = group.name.orEmpty(),
                parentCourseName = parentCourse?.name,
                memberCount = group.membersCount
            )
        }
    }

    private suspend fun getSyncedCourseIds(): Set<Long> {
        if (!featureFlagProvider.offlineEnabled()) return emptySet()

        val courseSyncSettings = courseSyncSettingsDao.findAll()
        return courseSyncSettings
            .filter { it.anySyncEnabled }
            .map { it.courseId }
            .toSet()
    }

    private fun mapGrade(course: Course): GradeDisplay {
        val courseGrade = course.getCourseGrade(false)

        return when {
            courseGrade == null -> GradeDisplay.Hidden
            courseGrade.isLocked -> GradeDisplay.Locked
            courseGrade.noCurrentGrade -> GradeDisplay.NotAvailable
            courseGrade.currentGrade != null -> GradeDisplay.Letter(courseGrade.currentGrade!!)
            courseGrade.currentScore != null -> {
                val score = courseGrade.currentScore
                GradeDisplay.Percentage("${score?.toInt()}%")
            }
            else -> GradeDisplay.NotAvailable
        }
    }

    private fun observeExpandedStates() {
        viewModelScope.launch {
            combine(
                sectionExpandedStateDataStore.observeCoursesExpanded(),
                sectionExpandedStateDataStore.observeGroupsExpanded()
            ) { coursesExpanded, groupsExpanded ->
                Pair(coursesExpanded, groupsExpanded)
            }.collect { (coursesExpanded, groupsExpanded) ->
                _uiState.update {
                    it.copy(
                        isCoursesExpanded = coursesExpanded,
                        isGroupsExpanded = groupsExpanded
                    )
                }
            }
        }
    }

    private fun observeGradeVisibility() {
        viewModelScope.launch {
            coursesWidgetBehavior.observeGradeVisibility()
                .catch { }
                .collect { showGrades ->
                    _uiState.update { it.copy(showGrades = showGrades) }
                }
        }
    }

    private fun observeColorOverlay() {
        viewModelScope.launch {
            coursesWidgetBehavior.observeColorOverlay()
                .catch { }
                .collect { showColorOverlay ->
                    _uiState.update { it.copy(showColorOverlay = showColorOverlay) }
                }
        }
    }

    private fun onAllCourses(activity: FragmentActivity) {
        coursesWidgetBehavior.onAllCoursesClicked(activity)
    }
}